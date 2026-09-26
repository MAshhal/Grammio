package com.mystic.grammio.presentation.settings.transformations

import com.mystic.grammio.domain.model.Transformation

data class TransformationsUiState(
    /** Every transformation, enabled or not, in the user's order; null until loaded. */
    val transformations: List<Transformation>? = null,
) {
    val isEmpty: Boolean get() = transformations?.isEmpty() == true
}
