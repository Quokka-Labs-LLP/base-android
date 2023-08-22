package com.example.baseandroid.features.main.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.ui.components.AdapterItem
import com.example.baseandroid.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier, userList: List<UserResponse>, navController: NavController) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            title = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        navController.navigate(route = Screen.Setting.route)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings, contentDescription = "settings"
                        )
                    }
                    Text("Compose-Koin", modifier = Modifier.padding(start = 10.dp))
                }
            })
    }) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            ListOfItems(
                modifier = Modifier, userList = userList
            )
        }
    }
}

@Composable
fun ListOfItems(modifier: Modifier, userList: List<UserResponse>) {
    LazyColumn {
        item {
            for (item in userList) AdapterItem(modifier = modifier, item)
        }
    }
}