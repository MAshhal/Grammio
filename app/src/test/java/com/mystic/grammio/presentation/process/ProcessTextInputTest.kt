package com.mystic.grammio.presentation.process

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProcessTextInputTest {

    private fun input(
        readOnly: Boolean = false,
        startedForResult: Boolean = false,
        fromAccessibility: Boolean = false,
    ) = ProcessTextInput.of("text", readOnly, startedForResult, fromAccessibility)

    @Test
    fun `a selection menu caller that wants a result can take the text back`() {
        assertThat(input(startedForResult = true).canReplace).isTrue()
        assertThat(input(startedForResult = false).canReplace).isFalse()
    }

    @Test
    fun `the accessibility service can take the text back without a caller`() {
        assertThat(input(fromAccessibility = true).canReplace).isTrue()
    }

    @Test
    fun `read-only text is never replaced`() {
        assertThat(input(readOnly = true, startedForResult = true).canReplace).isFalse()
        assertThat(input(readOnly = true, fromAccessibility = true).canReplace).isFalse()
    }

    @Test
    fun `missing text reads as empty`() {
        assertThat(ProcessTextInput.of(null, readOnly = false, startedForResult = true, fromAccessibility = false))
            .isEqualTo(ProcessTextInput(text = "", canReplace = true))
    }
}
