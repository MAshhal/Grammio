package com.mystic.grammio.presentation.process

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mystic.grammio.R
import com.mystic.grammio.presentation.accessibility.SelectionReplacer
import com.mystic.grammio.presentation.common.copyToClipboard
import com.mystic.grammio.presentation.process.ProcessTextInput.Companion.isFromAccessibility
import com.mystic.grammio.presentation.settings.SettingsActivity
import com.mystic.grammio.presentation.theme.GrammioTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Entry point from the system text-selection menu, and from the accessibility service for apps whose
 * menu doesn't offer Grammio. Kept thin: it translates the Intent into [ProcessTextInput], hosts the
 * UI, and performs the Android side of [ProcessTextEffect]s.
 */
class ProcessTextActivity : ComponentActivity() {

    private val selectionReplacer: SelectionReplacer by inject()

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
                if (intent.isFromAccessibility()) {
                    // No caller to return a result to: write into the other app's field ourselves.
                    if (!selectionReplacer.replace(effect.text)) {
                        copyToClipboard(effect.text)
                        Toast.makeText(this, R.string.accessibility_replace_failed, Toast.LENGTH_LONG).show()
                    }
                } else {
                    setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, effect.text))
                }
                finish()
            }

            ProcessTextEffect.Close -> finish()

            ProcessTextEffect.OpenSettings -> {
                // We run inside the caller's task; open settings in Grammio's own task instead. Errors
                // that offer this are all about the provider, so go straight to its page.
                startActivity(SettingsActivity.providerIntent(this).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }

            ProcessTextEffect.OpenTransformationSettings -> {
                startActivity(SettingsActivity.transformationsIntent(this).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }
        }
    }

    companion object {
        /** Opens the sheet on [text] selected in another app, found by the accessibility service. */
        fun accessibilityIntent(
            context: Context,
            text: String,
            readOnly: Boolean,
        ): Intent = Intent(Intent.ACTION_PROCESS_TEXT)
            .setComponent(ComponentName(context, ProcessTextInput.ACCESSIBILITY_ALIAS))
            .setType("text/plain")
            .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, readOnly)
            // A service has no task to join.
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
