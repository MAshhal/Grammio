package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.repository.TransformationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Transformations held in memory, in list order. [restoreDefaults] brings back [defaults]. */
class InMemoryTransformationRepository(
    initial: List<Transformation> = emptyList(),
    private val defaults: List<Transformation> = initial,
) : TransformationRepository {
    override val transformations = MutableStateFlow(initial)

    override suspend fun transformation(id: String): Transformation? = transformations.value.firstOrNull { it.id == id }

    override suspend fun save(transformation: Transformation) = transformations.update { list ->
        if (list.any { it.id == transformation.id }) {
            list.map { if (it.id == transformation.id) transformation else it }
        } else {
            list + transformation
        }
    }

    override suspend fun delete(id: String) = transformations.update { list -> list.filterNot { it.id == id } }

    override suspend fun moveUp(id: String) = move(id, -1)

    override suspend fun moveDown(id: String) = move(id, 1)

    override suspend fun setEnabled(
        id: String,
        enabled: Boolean,
    ) = transformations.update { list -> list.map { if (it.id == id) it.copy(isEnabled = enabled) else it } }

    override suspend fun restoreDefaults() = defaults.forEach { save(it) }

    private fun move(
        id: String,
        offset: Int,
    ) = transformations.update { list ->
        val from = list.indexOfFirst { it.id == id }
        val to = from + offset
        if (from == -1 || to !in list.indices) {
            list
        } else {
            list.toMutableList().apply { this[from] = this[to].also { this[to] = this[from] } }
        }
    }
}
