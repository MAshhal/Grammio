package com.mystic.grammio.di

import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    databaseModule,
)
