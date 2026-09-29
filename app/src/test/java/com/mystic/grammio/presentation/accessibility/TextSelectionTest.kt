package com.mystic.grammio.presentation.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextSelectionTest {

    @Test
    fun `reads the selected part of the text`() {
        val selection = TextSelection.of("I has a apple.", 2, 5, editable = true)

        assertThat(selection?.selectedText).isEqualTo("has")
        assertThat(selection?.editable).isTrue()
    }

    @Test
    fun `accepts a selection made backwards`() {
        val selection = TextSelection.of("hello world", 11, 6, editable = false)

        assertThat(selection).isEqualTo(TextSelection("hello world", 6, 11, editable = false))
    }

    @Test
    fun `clamps indices past the end of text that got shorter`() {
        val selection = TextSelection.of("short", 2, 40, editable = true)

        assertThat(selection?.selectedText).isEqualTo("ort")
    }

    @Test
    fun `nothing selected is no selection`() {
        assertThat(TextSelection.of(null, 0, 3, editable = true)).isNull()
        assertThat(TextSelection.of("text", -1, -1, editable = true)).isNull()
        assertThat(TextSelection.of("text", 2, 2, editable = true)).isNull()
        assertThat(TextSelection.of("text", 9, 12, editable = true)).isNull()
        assertThat(TextSelection.of("a   b", 1, 4, editable = true)).isNull()
    }

    @Test
    fun `replacing swaps only the selection and puts the cursor after the new text`() {
        val replaced = TextSelection("Dear Sam, I has a apple. Bye", 10, 24, editable = true)
            .replacedWith("I have an apple.")

        assertThat(replaced.text).isEqualTo("Dear Sam, I have an apple. Bye")
        assertThat(replaced.cursor).isEqualTo(26)
    }

    @Test
    fun `replacing the whole text`() {
        val replaced = TextSelection("helo", 0, 4, editable = true).replacedWith("hello")

        assertThat(replaced).isEqualTo(TextSelection.Replaced("hello", 5))
    }
}
