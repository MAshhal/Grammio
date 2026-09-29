package com.mystic.grammio.presentation.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TypingFieldTest {

    private val own = "com.mystic.grammio"
    private val field = TypingField(packageName = "com.example.chat", editable = true, password = false)

    @Test
    fun `offers the button for a text field in another app`() {
        assertThat(field.offersButton(own)).isTrue()
    }

    @Test
    fun `not for text the user can't edit`() {
        assertThat(field.copy(editable = false).offersButton(own)).isFalse()
    }

    @Test
    fun `never for passwords`() {
        assertThat(field.copy(password = true).offersButton(own)).isFalse()
    }

    @Test
    fun `never over Grammio itself`() {
        assertThat(field.copy(packageName = own).offersButton(own)).isFalse()
    }
}
