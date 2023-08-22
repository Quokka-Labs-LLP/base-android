package com.example.baseandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.ui.screens.HomeScreen
import com.example.baseandroid.features.main.ui.screens.SettingsScreen

@Composable
fun SetNavGraph(
    navController: NavHostController,
    modifier: Modifier,
    userList: List<UserResponse>,
) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(
            route = Screen.Main.route
        ) {
            HomeScreen(modifier = modifier, userList = userList, navController = navController)
        }
        composable(
            route = Screen.Setting.route
        ) {
            SettingsScreen(modifier = modifier, navController = navController)
        }
    }
}