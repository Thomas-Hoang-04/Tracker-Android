package com.thomas.cargotracker.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(
    onBack: () -> Unit,
    onCreateUser: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.userListState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateUser) {
                Icon(Icons.Default.Add, contentDescription = "Create User")
            }
        }
    ) { innerPadding ->
        
        if (state.isLoading) {
             androidx.compose.foundation.layout.Box(
                 modifier = Modifier.fillMaxSize().padding(innerPadding),
                 contentAlignment = Alignment.Center
             ) {
                 CircularProgressIndicator()
             }
        } else {
             LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                 // Filter Chips Row could be added here
                 
                items(state.filteredUsers) { user ->
                    UserListItem(
                        user = user,
                        onActivateToggle = { viewModel.toggleUserActivation(user.id, user.isActive) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserResponse,
    onActivateToggle: () -> Unit
) {
    ListItem(
        headlineContent = { Text(user.fullName) },
        supportingContent = { Text("${user.role} • ${user.email}") },
        trailingContent = {
            TextButton(onClick = onActivateToggle) {
                Text(
                    text = if (user.isActive) "Deactivate" else "Activate",
                    color = if (user.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (!user.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else Color.Transparent
        )
    )
}
