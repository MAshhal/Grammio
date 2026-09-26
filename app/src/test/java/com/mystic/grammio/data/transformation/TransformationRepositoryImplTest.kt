package com.mystic.grammio.data.transformation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.data.transformation.local.SqlDelightTransformationLocalDataSource
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.testing.FakeTransformationPreferencesLocalDataSource
import com.mystic.grammio.testing.TestTransformations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

/** Runs against a real in-memory database, so the SQL is exercised too. */
class TransformationRepositoryImplTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { GrammioDatabase.Schema.create(it) }
    private val localDataSource = SqlDelightTransformationLocalDataSource(GrammioDatabase(driver))
    private val preferences = FakeTransformationPreferencesLocalDataSource()

    private fun repository() = TransformationRepositoryImpl(localDataSource, preferences)

    private val defaultIds = DefaultTransformations.all.map { it.id }

    @After
    fun tearDown() = driver.close()

    @Test
    fun `the defaults are there on first use, in order`() = runTest {
        val all = repository().transformations.first()

        assertThat(all).isEqualTo(DefaultTransformations.all)
        assertThat(preferences.seeded).isTrue()
    }

    @Test
    fun `deleting every transformation doesn't bring the defaults back`() = runTest {
        val repository = repository()
        defaultIds.forEach { repository.delete(it) }

        assertThat(repository().transformations.first()).isEmpty()
    }

    @Test
    fun `new transformations go last and edits stay in place`() = runTest {
        val repository = repository()
        repository.save(TestTransformations.shorten)
        repository.save(DefaultTransformations.all.first().copy(name = "Grammar", icon = TransformationIcon.Edit))

        val all = repository.transformations.first()

        assertThat(all.map { it.id }).containsExactlyElementsIn(defaultIds + "shorten").inOrder()
        assertThat(all.first().name).isEqualTo("Grammar")
        assertThat(all.first().icon).isEqualTo(TransformationIcon.Edit)
        assertThat(repository.transformation("shorten")).isEqualTo(TestTransformations.shorten)
    }

    @Test
    fun `moving swaps with the neighbour and stops at either end`() = runTest {
        val repository = repository()

        repository.moveUp(defaultIds[0])
        repository.moveDown(defaultIds[4])
        assertThat(repository.transformations.first().map { it.id }).isEqualTo(defaultIds)

        repository.moveDown(defaultIds[0])
        repository.moveUp(defaultIds[4])

        assertThat(repository.transformations.first().map { it.id })
            .containsExactly(defaultIds[1], defaultIds[0], defaultIds[2], defaultIds[4], defaultIds[3])
            .inOrder()
    }

    @Test
    fun `disabled transformations stay listed`() = runTest {
        val repository = repository()

        repository.setEnabled(defaultIds[1], false)

        val all = repository.transformations.first()
        assertThat(all).hasSize(defaultIds.size)
        assertThat(all.single { !it.isEnabled }.id).isEqualTo(defaultIds[1])
    }

    @Test
    fun `restoring defaults resets and re-adds them, and keeps the user's own`() = runTest {
        val repository = repository()
        repository.save(TestTransformations.shorten)
        repository.save(DefaultTransformations.all[0].copy(taskPrompt = "Edited."))
        repository.setEnabled(defaultIds[1], false)
        repository.delete(defaultIds[2])

        repository.restoreDefaults()

        val all = repository.transformations.first()
        assertThat(all.filter { it.id in defaultIds }).containsExactlyElementsIn(DefaultTransformations.all)
        assertThat(all.map { it.id }).contains("shorten")
        assertThat(all.last().id).isEqualTo(defaultIds[2])
    }
}
