package com.mystic.grammio.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.ModelCatalogRepository
import com.mystic.grammio.domain.repository.PromptSettingsRepository
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.repository.TransformationRepository
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import com.mystic.grammio.presentation.accessibility.SelectionReplacer
import com.mystic.grammio.presentation.process.ProcessTextInput
import com.mystic.grammio.presentation.process.ProcessTextViewModel
import com.mystic.grammio.presentation.settings.history.HistorySettingsViewModel
import com.mystic.grammio.presentation.settings.prompt.SystemPromptViewModel
import com.mystic.grammio.presentation.settings.provider.ProviderSettingsViewModel
import com.mystic.grammio.presentation.settings.transformations.TransformationsViewModel
import com.mystic.grammio.presentation.settings.transformations.editor.TransformationEditorViewModel
import com.mystic.grammio.testing.MainDispatcherRule
import io.mockk.mockk
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/** Resolves everything the app asks Koin for, so wiring mistakes fail here instead of on device. */
class DependencyGraphTest {

    // ViewModels start collecting in init; on a test Main dispatcher that nobody advances, that work
    // never runs, so nothing outlives stopKoin().
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @After
    fun tearDown() = stopKoin()

    @Test
    fun `app graph resolves`() {
        val koin = startKoin {
            androidContext(mockk<Context>(relaxed = true))
            modules(databaseModule, dataModule, domainModule, presentationModule, inMemoryDatabaseModule)
        }.koin

        assertThat(koin.get<TextTransformRepository>()).isNotNull()
        assertThat(koin.get<ApiKeyRepository>()).isNotNull()
        assertThat(koin.get<ProviderSettingsRepository>()).isNotNull()
        assertThat(koin.get<ModelCatalogRepository>()).isNotNull()
        assertThat(koin.get<PromptSettingsRepository>()).isNotNull()
        assertThat(koin.get<TransformationRepository>()).isNotNull()
        assertThat(koin.get<TransformTextUseCase>()).isNotNull()
        assertThat(koin.get<ProviderSettingsViewModel>()).isNotNull()
        assertThat(koin.get<HistorySettingsViewModel>()).isNotNull()
        assertThat(koin.get<SystemPromptViewModel>()).isNotNull()
        assertThat(koin.get<TransformationsViewModel>()).isNotNull()
        assertThat(koin.get<SelectionReplacer>()).isSameInstanceAs(koin.get<SelectionReplacer>())
        assertThat(koin.get<TransformationEditorViewModel> { parametersOf(null) }.state.value.isNew).isTrue()
        assertThat(koin.get<TransformationEditorViewModel> { parametersOf("fix_grammar") }.state.value.isNew).isFalse()
        assertThat(
            koin.get<ProcessTextViewModel> {
                parametersOf(ProcessTextInput("hi", canReplace = true))
            }.state.value.originalText,
        ).isEqualTo("hi")
    }

    // The Android driver needs a real Context; swap in a JVM SQLite driver and keep the rest of databaseModule.
    private val inMemoryDatabaseModule = module {
        single<SqlDriver> { JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { GrammioDatabase.Schema.create(it) } }
    }
}
