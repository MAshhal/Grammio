package com.mystic.grammio

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.koin.KermitKoinLogger
import com.mystic.grammio.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GrammioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.setTag("Grammio")
        Logger.setMinSeverity(if (BuildConfig.DEBUG) Severity.Verbose else Severity.Warn)

        startKoin {
            logger(KermitKoinLogger(Logger.withTag("Koin")))
            androidContext(this@GrammioApplication)
            modules(appModules)
        }
    }
}
