package com.mystic.grammio.di

import com.mystic.grammio.domain.usecase.SaveApiKeyUseCase
import com.mystic.grammio.domain.usecase.SaveCustomEndpointUseCase
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory

val domainModule = module {
    factory<TransformTextUseCase>()
    factory<SaveApiKeyUseCase>()
    factory<SaveCustomEndpointUseCase>()
}
