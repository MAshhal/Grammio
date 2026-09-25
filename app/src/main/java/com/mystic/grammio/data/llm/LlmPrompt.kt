package com.mystic.grammio.data.llm

/** A vendor-neutral prompt: what the model should do, the text to do it on, and how creative to be. */
data class LlmPrompt(
    val systemInstruction: String,
    val userText: String,
    val temperature: Double,
)
