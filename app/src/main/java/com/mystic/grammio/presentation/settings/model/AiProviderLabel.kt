package com.mystic.grammio.presentation.settings.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiProvider

/** The provider's name as shown to the user. */
@Composable
fun AiProvider.label(): String = stringResource(
    when (this) {
        AiProvider.Gemini -> R.string.provider_gemini
        AiProvider.OpenAi -> R.string.provider_openai
        AiProvider.Anthropic -> R.string.provider_anthropic
        AiProvider.OpenAiCompatible -> R.string.provider_openai_compatible
    },
)

/** A line under the name, only where the name alone doesn't say what the option is. */
@get:StringRes
val AiProvider.descriptionRes: Int?
    get() = when (this) {
        AiProvider.OpenAiCompatible -> R.string.provider_openai_compatible_description
        AiProvider.Gemini, AiProvider.OpenAi, AiProvider.Anthropic -> null
    }
