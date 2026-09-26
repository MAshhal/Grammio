package com.mystic.grammio.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon
import com.mystic.grammio.testing.InMemoryTransformationRepository
import com.mystic.grammio.testing.TestTransformations.fixGrammar
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveTransformationUseCaseTest {

    private val repository =
        InMemoryTransformationRepository(listOf(fixGrammar.copy(isEnabled = false, temperature = 0.1)))
    private val save = SaveTransformationUseCase(repository)

    @Test
    fun `a new transformation is trimmed, gets its own id and starts turned on`() = runTest {
        assertThat(save(null, "  Tweet ", " Make it a tweet. \n", TransformationIcon.Mail)).isTrue()
        assertThat(save(null, "Tweet", "Make it a tweet.", TransformationIcon.Mail)).isTrue()

        val added = repository.transformations.value.drop(1)
        assertThat(added).hasSize(2)
        assertThat(added.map { it.id }.toSet()).hasSize(2)
        with(added.first()) {
            assertThat(id).isNotEmpty()
            assertThat(name).isEqualTo("Tweet")
            assertThat(taskPrompt).isEqualTo("Make it a tweet.")
            assertThat(icon).isEqualTo(TransformationIcon.Mail)
            assertThat(isEnabled).isTrue()
            assertThat(temperature).isEqualTo(Transformation.DEFAULT_TEMPERATURE)
        }
    }

    @Test
    fun `an edit keeps the id, switch and temperature`() = runTest {
        save(fixGrammar.id, "Grammar", "Fix it.", TransformationIcon.Edit)

        assertThat(repository.transformations.value.single()).isEqualTo(
            Transformation(
                id = fixGrammar.id,
                name = "Grammar",
                taskPrompt = "Fix it.",
                icon = TransformationIcon.Edit,
                isEnabled = false,
                temperature = 0.1,
            ),
        )
    }

    @Test
    fun `a blank name or task saves nothing`() = runTest {
        assertThat(save(null, "  ", "Do it.", TransformationIcon.Sparkle)).isFalse()
        assertThat(save(fixGrammar.id, "Name", "\n", TransformationIcon.Sparkle)).isFalse()

        assertThat(repository.transformations.value).hasSize(1)
        assertThat(repository.transformations.value.single().name).isEqualTo(fixGrammar.name)
    }
}
