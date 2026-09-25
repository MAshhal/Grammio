package com.mystic.grammio.domain.model

data class TransformedText(
    val text: String,
    val transformation: Transformation,
)
