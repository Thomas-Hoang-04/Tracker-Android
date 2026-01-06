package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.viewmodel.CustomerViewModel

data class MockProvider(val name: String, val id: String, val role: String = "Provider")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCreateOrderScreen(
    onBack: () -> Unit,
    onOrderCreated: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    var goodsDescription by remember { mutableStateOf("General Cargo") }
    var pickupAddress by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    
    var showProviderDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<MockProvider?>(null) }
    
    val createOrderState by viewModel.createOrderState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createOrderState) {
        when (val state = createOrderState) {
            is CustomerViewModel.CreateOrderState.Success -> {
                viewModel.resetCreateOrderState()
                onOrderCreated()
            }
            is CustomerViewModel.CreateOrderState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetCreateOrderState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isPickupAddressValid = pickupAddress.length >= 10
            val isDeliveryAddressValid = deliveryAddress.length >= 10

            OutlinedTextField(
                value = goodsDescription,
                onValueChange = { goodsDescription = it },
                label = { Text("Goods Description") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = pickupAddress,
                onValueChange = { pickupAddress = it },
                label = { Text("Pickup Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = pickupAddress.isNotEmpty() && !isPickupAddressValid,
                supportingText = {
                    if (pickupAddress.isNotEmpty() && !isPickupAddressValid) {
                        Text("Must be at least 10 characters")
                    }
                }
            )

            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = { deliveryAddress = it },
                label = { Text("Delivery Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = deliveryAddress.isNotEmpty() && !isDeliveryAddressValid,
                supportingText = {
                    if (deliveryAddress.isNotEmpty() && !isDeliveryAddressValid) {
                        Text("Must be at least 10 characters")
                    }
                }
            )

            Card(
                onClick = { showProviderDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Provider", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedProvider != null) {
                        Text(
                            text = selectedProvider!!.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${selectedProvider!!.id}", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Select a Provider",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Create Order",
                onClick = {
                    if (selectedProvider != null) {
                        viewModel.createOrder(
                            providerId = selectedProvider!!.id,
                            goodsDescription = goodsDescription,
                            pickupAddress = pickupAddress,
                            deliveryAddress = deliveryAddress
                        )
                    }
                },
                enabled = selectedProvider != null && goodsDescription.isNotEmpty() && isPickupAddressValid && isDeliveryAddressValid,
                isLoading = createOrderState is CustomerViewModel.CreateOrderState.Loading
            )
        }
    }
    
    if (showProviderDialog) {
        ProviderSelectionDialog(
            onDismiss = { showProviderDialog = false },
            onSelect = { 
                selectedProvider = it
                showProviderDialog = false
            }
        )
    }
}

@Composable
fun ProviderSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (MockProvider) -> Unit
) {
    // Only one provider available
    val providers = remember {
        listOf(
            MockProvider("Provider Company", "e811533d-1f5a-4eee-9456-b33d682969d8")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Provider") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(providers) { provider ->
                    Card(
                        onClick = { onSelect(provider) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("ID: ${provider.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
