package com.mystic.grammio.di

import com.mystic.grammio.data.fake.FakeTextTransformRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dataModule = module {
    single<FakeTextTransformRepository>() bind TextTransformRepository::class
}
