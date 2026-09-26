package com.mystic.grammio.data.provider

import com.mystic.grammio.domain.model.AiProvider

/**
 * Stable name for [AiProvider] in anything persisted. Spelled out rather than derived from the
 * enum name so renaming an entry can never orphan a stored key or setting.
 */
val AiProvider.storageKey: String
    get() = when (this) {
        AiProvider.Gemini -> "gemini"
        AiProvider.OpenAi -> "openai"
        AiProvider.Anthropic -> "anthropic"
        AiProvider.OpenAiCompatible -> "openai_compatible"
    }

/** The provider stored under [key], or null for a key this version doesn't know. */
fun aiProviderForStorageKey(key: String): AiProvider? = AiProvider.entries.firstOrNull { it.storageKey == key }
