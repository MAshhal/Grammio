package com.mystic.grammio.data.transform.log

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.usecase.TransformTextUseCase
import org.junit.Test

class TransformErrorStorageKeyTest {

    @Test
    fun `every error reads back from its key`() {
        val errors = listOf(
            TransformError.EmptyInput,
            TransformError.InputTooLong(TransformTextUseCase.MAX_INPUT_CHARS),
            TransformError.MissingApiKey,
            TransformError.InvalidApiKey,
            TransformError.ProviderNotConfigured,
            TransformError.RateLimited,
            TransformError.ContentBlocked,
            TransformError.Network,
            TransformError.Timeout,
            TransformError.ServiceUnavailable,
            TransformError.Unknown,
        )
        errors.forEach { assertThat(transformErrorForStorageKey(it.storageKey)).isEqualTo(it) }
    }

    @Test
    fun `an unknown key reads as null`() {
        assertThat(transformErrorForStorageKey("from_the_future")).isNull()
    }
}
