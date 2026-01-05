package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.viewmodel.CustomerViewModel
import com.thomas.cargotracker.ui.screens.provider.ProviderOrderCard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CustomerHistoryScreen(
    onOrderDetails: (String) -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val orders by viewModel.orders.collectAsState()
    
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by Order ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Show ALL orders or just past ones? usually history is everything or completed.
                // Let's show everything for now to be safe, filtered by search.
                val filteredOrders = orders.filter { 
                    it.id.contains(searchQuery, ignoreCase = true)
                }
                
                items(filteredOrders) { order ->
                    ProviderOrderCard(
                        order = order,
                        onMoreDetailsClick = { onOrderDetails(order.id) }
                    )
                }
                
                if (filteredOrders.isEmpty()) {
                    item {
                         Text(
                            text = "No orders found.",
                            modifier = Modifier.padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
