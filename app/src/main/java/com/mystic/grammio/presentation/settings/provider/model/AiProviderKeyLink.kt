package com.mystic.grammio.presentation.settings.provider.model

import androidx.annotation.StringRes
import com.mystic.grammio.R
import com.mystic.grammio.domain.model.AiProvider

/** Where the user can create an API key for a provider. */
data class AiProviderKeyLink(
    val url: String,
    @StringRes val label: Int,
)

/** Null for a custom endpoint: only the user knows where its keys come from. */
val AiProvider.keyLink: AiProviderKeyLink?
    get() = when (this) {
        AiProvider.Gemini -> AiProviderKeyLink("https://aistudio.google.com/apikey", R.string.settings_get_key_gemini)

        AiProvider.OpenAi -> AiProviderKeyLink("https://platform.openai.com/api-keys", R.string.settings_get_key_openai)

        AiProvider.Anthropic ->
            AiProviderKeyLink("https://platform.claude.com/settings/keys", R.string.settings_get_key_anthropic)

        AiProvider.OpenAiCompatible -> null
    }
