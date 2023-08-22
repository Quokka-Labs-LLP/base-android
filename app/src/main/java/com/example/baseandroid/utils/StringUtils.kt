package com.example.baseandroid.utils

import java.util.*

fun String.capitalizeFirstLetter(): String {
    if (this.isNullOrEmpty()) {
        return this
    }
    return this.substring(0, 1).uppercase(Locale.ROOT) + this.substring(1)
}

fun String.stripHtmlTags(): String {
    return this.replace(Regex("<.*?>"), "")
}

fun String.truncate(maxLength: Int): String {
    if (this.length <= maxLength) {
        return this
    }
    return this.substring(0, maxLength) + "..."
}
