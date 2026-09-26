package com.mystic.grammio.di

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import com.mystic.grammio.presentation.process.ProcessTextInput
import com.mystic.grammio.presentation.process.ProcessTextViewModel
import com.mystic.grammio.presentation.settings.SettingsViewModel
import io.mockk.mockk
import org.junit.After
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf

/** Resolves everything the app asks Koin for, so wiring mistakes fail here instead of on device. */
class DependencyGraphTest {

    @After
    fun tearDown() = stopKoin()

    @Test
    fun `app graph resolves`() {
        val koin = startKoin {
            androidContext(mockk<Context>(relaxed = true))
            modules(dataModule, domainModule, presentationModule)
        }.koin

        assertThat(koin.get<TextTransformRepository>()).isNotNull()
        assertThat(koin.get<ApiKeyRepository>()).isNotNull()
        assertThat(koin.get<ProviderSettingsRepository>()).isNotNull()
        assertThat(koin.get<TransformTextUseCase>()).isNotNull()
        assertThat(koin.get<SettingsViewModel>()).isNotNull()
        assertThat(
            koin.get<ProcessTextViewModel> {
                parametersOf(ProcessTextInput("hi", canReplace = true))
            }.state.value.originalText,
        ).isEqualTo("hi")
    }
}
