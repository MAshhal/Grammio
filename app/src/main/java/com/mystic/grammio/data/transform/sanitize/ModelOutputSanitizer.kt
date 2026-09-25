package com.mystic.grammio.data.transform.sanitize

/**
 * Removes wrapping that models sometimes add despite instructions: code fences, echoed
 * `<text>` tags, and quotes around the whole reply.
 */
class ModelOutputSanitizer {

    fun sanitize(raw: String): String {
        var text = raw.trim()
        CODE_FENCE.matchEntire(text)?.let { text = it.groupValues[1].trim() }
        if (text.startsWith(TEXT_OPEN_TAG) && text.endsWith(TEXT_CLOSE_TAG)) {
            text = text.removePrefix(TEXT_OPEN_TAG).removeSuffix(TEXT_CLOSE_TAG).trim()
        }
        QUOTE_PAIRS.firstOrNull { (open, close) ->
            text.length >= 2 && text.startsWith(open) && text.endsWith(close)
        }?.let { text = text.substring(1, text.length - 1).trim() }
        return text
    }

    private companion object {
        const val TEXT_OPEN_TAG = "<text>"
        const val TEXT_CLOSE_TAG = "</text>"
        val CODE_FENCE = Regex("^```[\\w-]*\\s*\\n(.*?)\\n?```$", RegexOption.DOT_MATCHES_ALL)
        val QUOTE_PAIRS = listOf('"' to '"', '“' to '”', '«' to '»')
    }
}
