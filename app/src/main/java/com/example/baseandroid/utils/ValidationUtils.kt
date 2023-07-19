package com.example.baseandroid.utils

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        val pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(pattern.toRegex())
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val pattern = "[0-9]{10}"
        return phoneNumber.matches(pattern.toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        // Add your password validation logic here
        return password.length >= 8
    }
}
