package com.mystic.grammio.domain.usecase

import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.domain.repository.TransformationRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Saves a transformation from the editor. Name and task are trimmed and must not be blank. A new
 * transformation gets a fresh id and starts turned on; an edited one keeps its id, switch and
 * temperature.
 */
class SaveTransformationUseCase(private val repository: TransformationRepository) {

    /**
     * @param id null for a new transformation.
     * @return false when the name or task is blank and nothing was saved.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        id: String?,
        name: String,
        taskPrompt: String,
        icon: TransformationIcon,
    ): Boolean {
        val trimmedName = name.trim()
        val trimmedPrompt = taskPrompt.trim()
        if (trimmedName.isEmpty() || trimmedPrompt.isEmpty()) return false

        val existing = id?.let { repository.transformation(it) }
        repository.save(
            existing?.copy(name = trimmedName, taskPrompt = trimmedPrompt, icon = icon)
                ?: Transformation(
                    id = Uuid.random().toString(),
                    name = trimmedName,
                    taskPrompt = trimmedPrompt,
                    icon = icon,
                ),
        )
        return true
    }
}
