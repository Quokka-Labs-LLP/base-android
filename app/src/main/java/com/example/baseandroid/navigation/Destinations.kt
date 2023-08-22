package com.example.baseandroid.navigation

sealed class Screen(val route: String) {
    object Main : Screen(route = "main_screen")
    object Setting : Screen(route = "setting_screen")
}