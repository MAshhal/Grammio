package com.mystic.grammio.data.transform.log.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.data.db.Transformation_log
import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.result.Outcome
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SqlDelightTransformationLogLocalDataSourceTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { GrammioDatabase.Schema.create(it) }
    private val database = GrammioDatabase(driver)
    private val dataSource = SqlDelightTransformationLogLocalDataSource(database)

    @After
    fun tearDown() = driver.close()

    @Test
    fun `records a successful transformation`() = runTest {
        dataSource.record(
            entry(
                transformation = Transformation.Translate("es"),
                result = Outcome.Success("Hola."),
            ),
        )

        assertThat(rows()).containsExactly(
            Transformation_log(
                id = 1,
                created_at = 1_700_000_000_000,
                transformation = "translate",
                target_language = "es",
                provider = "anthropic",
                model_id = "claude-haiku-4-5",
                input_text = "Hello.",
                output_text = "Hola.",
                error = null,
                duration_ms = 1_234,
            ),
        )
    }

    @Test
    fun `records a failure and the model it happened on`() = runTest {
        dataSource.record(entry(result = Outcome.Failure(TransformError.RateLimited)))

        val row = rows().single()
        assertThat(row.transformation).isEqualTo("fix_grammar")
        assertThat(row.target_language).isNull()
        assertThat(row.model_id).isEqualTo("claude-haiku-4-5")
        assertThat(row.output_text).isNull()
        assertThat(row.error).isEqualTo("rate_limited")
    }

    @Test
    fun `records a failure before any model was chosen`() = runTest {
        dataSource.record(entry(modelId = null, result = Outcome.Failure(TransformError.MissingApiKey)))

        val row = rows().single()
        assertThat(row.model_id).isNull()
        assertThat(row.error).isEqualTo("missing_api_key")
    }

    @Test
    fun `lists the most recent entries first`() = runTest {
        dataSource.record(entry(inputText = "first", startedAt = Instant.fromEpochMilliseconds(1)))
        dataSource.record(entry(inputText = "second", startedAt = Instant.fromEpochMilliseconds(2)))

        assertThat(rows().map { it.input_text }).containsExactly("second", "first").inOrder()
    }

    @Test
    fun `a database failure is swallowed`() = runTest {
        driver.close()

        dataSource.record(entry())
    }

    private fun rows() = database.transformationLogQueries.selectRecent(limit = 10).executeAsList()

    private fun entry(
        startedAt: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000),
        transformation: Transformation = Transformation.FixGrammar,
        modelId: String? = "claude-haiku-4-5",
        inputText: String = "Hello.",
        result: Outcome<String, TransformError> = Outcome.Success("Hello."),
    ) = TransformationLogEntry(
        startedAt = startedAt,
        transformation = transformation,
        provider = AiProvider.Anthropic,
        modelId = modelId,
        inputText = inputText,
        result = result,
        duration = 1_234.milliseconds,
    )
}
