package com.mystic.grammio.data.apikey.crypto

/** Output of [KeystoreCipher]: both parts Base64-encoded so they can be stored as plain strings. */
data class EncryptedValue(
    val iv: String,
    val cipherText: String,
)
