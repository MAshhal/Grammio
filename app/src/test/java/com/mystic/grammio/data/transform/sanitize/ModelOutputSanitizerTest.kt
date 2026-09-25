package com.mystic.grammio.data.transform.sanitize

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ModelOutputSanitizerTest {

    private val sanitizer = ModelOutputSanitizer()

    @Test
    fun `strips fences, echoed tags and wrapping quotes`() {
        assertThat(sanitizer.sanitize("```\nHello\nthere\n```")).isEqualTo("Hello\nthere")
        assertThat(sanitizer.sanitize("```text\nHello\n```")).isEqualTo("Hello")
        assertThat(sanitizer.sanitize("<text>\nHello\n</text>")).isEqualTo("Hello")
        assertThat(sanitizer.sanitize("\"Hello\"")).isEqualTo("Hello")
        assertThat(sanitizer.sanitize("“Hello”")).isEqualTo("Hello")
        assertThat(sanitizer.sanitize("  Hello  ")).isEqualTo("Hello")
    }

    @Test
    fun `keeps quotes that are part of the text`() {
        assertThat(sanitizer.sanitize("He said \"hi\" twice")).isEqualTo("He said \"hi\" twice")
        assertThat(sanitizer.sanitize("\"")).isEqualTo("\"")
    }
}
