package com.mystic.grammio.presentation.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TypingFieldTest {

    private val own = "com.mystic.grammio"
    private val field = TypingField(
        packageName = "com.example.chat",
        className = "android.widget.EditText",
        editable = true,
        acceptsSetText = true,
        password = false,
    )
    private val notText = field.copy(className = "android.widget.TextView", editable = false, acceptsSetText = false)

    @Test
    fun `offers the button for a text field in another app`() {
        assertThat(field.offersButton(own)).isTrue()
    }

    @Test
    fun `not for text the user can't edit`() {
        assertThat(notText.isTextField).isFalse()
        assertThat(notText.offersButton(own)).isFalse()
    }

    @Test
    fun `a custom field not marked editable still counts when it accepts text or is an EditText`() {
        assertThat(notText.copy(acceptsSetText = true).offersButton(own)).isTrue()
        assertThat(notText.copy(className = "com.twitter.ui.widget.TwitterEditText").offersButton(own)).isTrue()
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
