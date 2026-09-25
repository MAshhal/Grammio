package com.mystic.grammio.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mystic.grammio.data.db.GrammioDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = GrammioDatabase.Schema,
            context = androidContext(),
            name = "grammio.db",
        )
    }
    single<GrammioDatabase> { GrammioDatabase(get()) }
}
