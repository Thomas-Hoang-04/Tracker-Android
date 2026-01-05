package com.thomas.cargotracker.ui.screens.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.data.model.OrderSummary
import com.thomas.cargotracker.ui.viewmodel.ProviderViewModel

data class MockCustomer(val name: String, val id: String, val role: String = "Customer")

enum class CreateOrderStep {
    FIND_CUSTOMER,
    ORDER_INFO,
    CONNECT_DEVICE,
    SETUP_THRESHOLD
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProviderCreateOrderScreen(
    onOrderCreated: () -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(CreateOrderStep.FIND_CUSTOMER) }
    
    // State for inputs
    var customerSearch by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<String?>(null) }
    
    var orderName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("Container") }
    
    // ... (rest of state vars)

    var isDeviceConnected by remember { mutableStateOf(false) }
    
    var tempMin by remember { mutableStateOf("20.0") }
    var tempMax by remember { mutableStateOf("30.0") }
    
    var humidityMin by remember { mutableStateOf("30.0") }
    var humidityMax by remember { mutableStateOf("60.0") }
    
    var gasThreshold by remember { mutableStateOf("100") }
    var accelThreshold by remember { mutableStateOf("1.0") }
    var gyroThreshold by remember { mutableStateOf("1.0") }
    
    var readPeriodMs by remember { mutableStateOf("30000") }

    val handleCreateOrder = {
        val newOrder = OrderSummary(
            id = "ORD-${System.currentTimeMillis() % 10000}", // Simple ID gen
            customerName = selectedCustomer?.substringBefore(" (ID") ?: "Unknown",
            productType = productType.ifEmpty { "General Cargo" },
            tempMin = tempMin,
            tempMax = tempMax,
            humidityMin = humidityMin,
            humidityMax = humidityMax
        )
        viewModel.createOrder(newOrder)
        onOrderCreated()
    }
    
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = getTitleForStep(currentStep),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentStep.ordinal + 1) / 4f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                CreateOrderStep.FIND_CUSTOMER -> {
                    Text("Search for a customer to assign this order to.")
                    OutlinedTextField(
                        value = customerSearch,
                        onValueChange = { customerSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer ID or Name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    
                    // Mock Data
                    val mockCustomers = remember {
                        listOf(
                            MockCustomer("Alice Smith", "1001"),
                            MockCustomer("Bob Jones", "1002"),
                            MockCustomer("Charlie Brown", "1003"),
                            MockCustomer("Diana Prince", "1004"),
                            MockCustomer("Evan Wright", "1005"),
                            MockCustomer("Frank Castle", "1006"),
                            MockCustomer("Grace Hopper", "1007")
                        )
                    }

                    // Filtered List
                    val filteredCustomers = if (customerSearch.isEmpty()) emptyList() else mockCustomers.filter {
                        it.name.contains(customerSearch, ignoreCase = true) || it.id.contains(customerSearch, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCustomers) { customer ->
                             Card(
                                onClick = { selectedCustomer = "${customer.name} (ID: ${customer.id})" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCustomer?.contains(customer.id) == true) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    Text("ID: ${customer.id}", style = MaterialTheme.typography.bodySmall)
                                    Text("Role: ${customer.role}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        if (filteredCustomers.isEmpty() && customerSearch.isNotEmpty()) {
                            item {
                                Text(
                                    "No customers found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { currentStep = CreateOrderStep.ORDER_INFO },
                        enabled = selectedCustomer != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next")
                    }
                }
                
                CreateOrderStep.ORDER_INFO -> {
                    OutlinedTextField(
                        value = orderName,
                        onValueChange = { orderName = it },
                        label = { Text("Order Name") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                     OutlinedTextField(
                        value = productType,
                        onValueChange = { productType = it },
                        label = { Text("Type of Product") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    
                    // Show selected Customer read-only
                     Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Customer", style = MaterialTheme.typography.labelLarge)
                            Text(selectedCustomer ?: "Unknown")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        TextButton(onClick = { currentStep = CreateOrderStep.FIND_CUSTOMER }) { Text("Back") }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { currentStep = CreateOrderStep.CONNECT_DEVICE },
                            enabled = orderName.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next")
                        }
                    }
                }
                
                CreateOrderStep.CONNECT_DEVICE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (isDeviceConnected) Icons.Default.Check else Icons.Default.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.padding(bottom = 8.dp),
                                tint = if (isDeviceConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(if (isDeviceConnected) "Device Connected" else "Connect to Device")
                            
                            if (!isDeviceConnected) {
                                Button(
                                    onClick = { isDeviceConnected = true },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Connect via Bluetooth")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         TextButton(onClick = { currentStep = CreateOrderStep.ORDER_INFO }) { Text("Back") }
                         Button(
                            onClick = { currentStep = CreateOrderStep.SETUP_THRESHOLD },
                            enabled = isDeviceConnected
                        ) {
                            Text("Set up Thresholds")
                        }
                    }
                }
                
                CreateOrderStep.SETUP_THRESHOLD -> {
                    Text(
                        text = "Smart Sensor Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f)
                    ) {
                        // Environment Card
                        ThresholdCard(title = "Environment", icon = Icons.Outlined.Thermostat) {
                            Text("Temperature Range: ${tempMin}°C - ${tempMax}°C", style = MaterialTheme.typography.bodySmall)
                            RangeSlider(
                                value = (tempMin.toFloatOrNull() ?: 20f)..(tempMax.toFloatOrNull() ?: 30f),
                                onValueChange = { range ->
                                    tempMin = range.start.toInt().toString()
                                    tempMax = range.endInclusive.toInt().toString()
                                },
                                valueRange = -20f..60f,
                                steps = 79, // 1 degree steps roughly
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Humidity Range: ${humidityMin}% - ${humidityMax}%", style = MaterialTheme.typography.bodySmall)
                            RangeSlider(
                                value = (humidityMin.toFloatOrNull() ?: 30f)..(humidityMax.toFloatOrNull() ?: 60f),
                                onValueChange = { range ->
                                    humidityMin = range.start.toInt().toString()
                                    humidityMax = range.endInclusive.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 99,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Motion Sensors Card
                        ThresholdCard(title = "Motion & Gas", icon = Icons.Outlined.Speed) {
                             Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = accelThreshold,
                                    onValueChange = { accelThreshold = it },
                                    label = { Text("Accel (g)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = gyroThreshold,
                                    onValueChange = { gyroThreshold = it },
                                    label = { Text("Gyro (°/s)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                             OutlinedTextField(
                                value = gasThreshold,
                                onValueChange = { gasThreshold = it },
                                label = { Text("Gas Sensitivity (0-1000)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        // Configuration Card
                        ThresholdCard(title = "Reporting", icon = Icons.Outlined.Timer) {
                            Text("Read Period", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val periods = listOf("30s" to "30000", "1m" to "60000", "5m" to "300000")
                                periods.forEach { (label, value) ->
                                    FilterChip(
                                        selected = readPeriodMs == value,
                                        onClick = { readPeriodMs = value },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         TextButton(onClick = { currentStep = CreateOrderStep.CONNECT_DEVICE }) { Text("Back") }
                         Button(
                            onClick = handleCreateOrder,
                        ) {
                            Text("Create Order")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThresholdCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

private fun getTitleForStep(step: CreateOrderStep): String {
    return when(step) {
        CreateOrderStep.FIND_CUSTOMER -> "Find Customer"
        CreateOrderStep.ORDER_INFO -> "Create Order"
        CreateOrderStep.CONNECT_DEVICE -> "Connect Device"
        CreateOrderStep.SETUP_THRESHOLD -> "Setup Threshold"
    }
}
