package com.mystic.grammio.presentation.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SelectionReplacerTest {

    private val replacer = SelectionReplacer()
    private val field = RecordingField()
    private val editable = TextSelection("Hi, I has a apple!", 4, 17, editable = true)

    @Test
    fun `writes the whole text back with the selection replaced`() {
        replacer.hold(field, editable)

        assertThat(replacer.replace("I have an apple")).isTrue()

        assertThat(field.writes).containsExactly("Hi, I have an apple!" to 19)
    }

    @Test
    fun `replaces once`() {
        replacer.hold(field, editable)
        replacer.replace("first")

        assertThat(replacer.replace("second")).isFalse()
        assertThat(field.writes).hasSize(1)
    }

    @Test
    fun `nothing held, nothing replaced`() {
        assertThat(replacer.replace("text")).isFalse()
    }

    @Test
    fun `does not write into read-only text`() {
        replacer.hold(field, editable.copy(editable = false))

        assertThat(replacer.replace("text")).isFalse()
        assertThat(field.writes).isEmpty()
    }

    @Test
    fun `a newer selection wins, and one without a field forgets the older`() {
        val other = RecordingField()
        replacer.hold(field, editable)
        replacer.hold(other, TextSelection("abc", 0, 1, editable = true))
        replacer.replace("A")

        assertThat(field.writes).isEmpty()
        assertThat(other.writes).containsExactly("Abc" to 1)

        replacer.hold(field, editable)
        replacer.hold(null, editable)
        assertThat(replacer.replace("text")).isFalse()
    }

    @Test
    fun `reports a field that refused`() {
        replacer.hold({ _, _ -> false }, editable)

        assertThat(replacer.replace("text")).isFalse()
    }

    @Test
    fun `clear forgets the selection`() {
        replacer.hold(field, editable)
        replacer.clear()

        assertThat(replacer.replace("text")).isFalse()
    }

    private class RecordingField : EditableField {
        val writes = mutableListOf<Pair<String, Int>>()

        override fun setText(
            text: String,
            cursor: Int,
        ): Boolean {
            writes += text to cursor
            return true
        }
    }
}
