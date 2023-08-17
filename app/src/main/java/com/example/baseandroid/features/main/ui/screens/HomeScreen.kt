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
import com.example.baseandroid.features.main.models.UserResponse
import com.example.baseandroid.features.main.ui.components.AdapterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier, userList: List<UserResponse>, launchSettings: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            title = {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Compose-Koin")
                    IconButton(onClick = { launchSettings() }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings, contentDescription = "settings"
                        )
                    }
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