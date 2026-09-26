package com.mystic.grammio.data.transformation.mapper

import com.mystic.grammio.data.db.Transformation as TransformationRow
import com.mystic.grammio.data.transformation.transformationIconForStorageKey
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon

/** An icon key this version doesn't know (written by a newer one) falls back to the generic icon. */
fun TransformationRow.toDomain(): Transformation = Transformation(
    id = id,
    name = name,
    taskPrompt = task_prompt,
    icon = transformationIconForStorageKey(icon) ?: TransformationIcon.Sparkle,
    isEnabled = is_enabled != 0L,
    temperature = temperature,
)

fun Boolean.toSqlFlag(): Long = if (this) 1L else 0L
