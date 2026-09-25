package com.mystic.grammio.presentation.process

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mystic.grammio.presentation.common.copyToClipboard
import com.mystic.grammio.presentation.settings.SettingsActivity
import com.mystic.grammio.presentation.theme.GrammioTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Entry point from the system text-selection menu. Kept thin: it translates the Intent into
 * [ProcessTextInput], hosts the UI, and performs the Android side of [ProcessTextEffect]s.
 */
class ProcessTextActivity : ComponentActivity() {

    private val viewModel: ProcessTextViewModel by viewModel(parameters = {
        parametersOf(ProcessTextInput.from(intent, callingActivity))
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GrammioTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    viewModel.effects.collect(::handleEffect)
                }
                ProcessTextSheet(state = state, onAction = viewModel::onAction)
            }
        }
    }

    private fun handleEffect(effect: ProcessTextEffect) {
        when (effect) {
            is ProcessTextEffect.CopyToClipboard -> copyToClipboard(effect.text)

            is ProcessTextEffect.ReturnResult -> {
                setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, effect.text))
                finish()
            }

            ProcessTextEffect.Close -> finish()

            ProcessTextEffect.OpenSettings -> {
                // We run inside the caller's task; open settings in Grammio's own task instead.
                startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }
        }
    }
}
