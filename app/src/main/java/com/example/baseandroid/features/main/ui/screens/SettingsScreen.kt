package com.example.baseandroid.features.main.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
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
import com.example.baseandroid.features.main.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier, viewModel: MainViewModel, onLaunchChucker: () -> Unit) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compose-Koin-Settings"
                    )
                    IconButton(onClick = { viewModel.currentPage.intValue = 0 }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack, contentDescription = "back"
                        )
                    }
                }
            })
    }) {
        Surface(
            modifier = Modifier.padding(it), color = MaterialTheme.colorScheme.background
        ) {
            Button(
                onClick = {
                    onLaunchChucker()
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(text = "Launch Chucker")
            }

        }
    }
}