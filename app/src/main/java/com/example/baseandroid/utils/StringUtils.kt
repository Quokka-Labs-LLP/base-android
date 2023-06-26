package com.example.baseandroid.utils

import java.util.*

object StringUtils {
    private fun isNullOrEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    fun capitalizeFirstLetter(text: String): String {
        if (isNullOrEmpty(text)) {
            return text
        }
        return text.substring(0, 1).uppercase(Locale.ROOT) + text.substring(1)
    }

    fun stripHtmlTags(html: String): String {
        return html.replace(Regex("<.*?>"), "")
    }

    fun truncate(text: String, maxLength: Int): String {
        if (text.length <= maxLength) {
            return text
        }
        return text.substring(0, maxLength) + "..."
    }
}
