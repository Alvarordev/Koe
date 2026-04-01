package com.example.tracker.presentation.accounts.addaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.enums.CardNetwork
import com.example.tracker.data.enums.SupportedCurrency
import org.koin.androidx.compose.koinViewModel

private val colorPalette = listOf(
    "#1A73E8", "#E53935", "#2E7D32", "#F57C00",
    "#7B1FA2", "#00838F", "#AD1457", "#37474F"
)

private fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF1A73E8))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar cuenta" else "New Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AccountTypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::selectType
            )

            CommonFields(
                formState = uiState.formState,
                onNameChange = viewModel::updateName,
                onColorChange = viewModel::updateColor,
                onCurrencyChange = viewModel::updateCurrency
            )

            TypeSpecificFields(
                formState = uiState.formState,
                onInitialBalanceChange = viewModel::updateInitialBalance,
                onCreditLimitChange = viewModel::updateCreditLimit,
                onCreditUsedChange = viewModel::updateCreditUsed,
                onCardNetworkChange = viewModel::updateCardNetwork,
                onLastFourDigitsChange = viewModel::updateLastFourDigits,
                onExpirationDateChange = viewModel::updateExpirationDate,
                onPaymentDayChange = viewModel::updatePaymentDay,
                onInterestRateChange = viewModel::updateInterestRate
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.isSubmitting) "Saving..."
                    else if (uiState.isEditing) "Guardar cambios"
                    else "Save Account"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AccountTypeSelector(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit
) {
    val types = AccountType.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { type ->
            val isSelected = type == selectedType
            val label = when (type) {
                AccountType.CASH -> "Cash"
                AccountType.DEBIT -> "Debit"
                AccountType.CREDIT -> "Credit"
                AccountType.SAVINGS -> "Savings"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommonFields(
    formState: AddAccountFormState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit
) {
    OutlinedTextField(
        value = formState.name,
        onValueChange = onNameChange,
        label = { Text("Account Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Text(
        text = "Color",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colorPalette.forEach { hex ->
            val selected = formState.color == hex
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(parseColor(hex))
                    .then(
                        if (selected) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        ) else Modifier
                    )
                    .clickable { onColorChange(hex) }
            )
        }
    }

    var currencyExpanded by remember { mutableStateOf(false) }
    val currencies = SupportedCurrency.entries

    ExposedDropdownMenuBox(
        expanded = currencyExpanded,
        onExpandedChange = { currencyExpanded = it }
    ) {
        OutlinedTextField(
            value = formState.currencyCode,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = currencyExpanded,
            onDismissRequest = { currencyExpanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${currency.code} — ${currency.currencyName}") },
                    onClick = {
                        onCurrencyChange(currency.code)
                        currencyExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSpecificFields(
    formState: AddAccountFormState,
    onInitialBalanceChange: (String) -> Unit,
    onCreditLimitChange: (String) -> Unit,
    onCreditUsedChange: (String) -> Unit,
    onCardNetworkChange: (CardNetwork) -> Unit,
    onLastFourDigitsChange: (String) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onPaymentDayChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit
) {
    when (formState) {
        is AddAccountFormState.CashFormState -> {
            OutlinedTextField(
                value = formState.initialBalance,
                onValueChange = onInitialBalanceChange,
                label = { Text("Initial Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        is AddAccountFormState.DebitFormState -> {
            OutlinedTextField(
                value = formState.initialBalance,
                onValueChange = onInitialBalanceChange,
                label = { Text("Initial Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            CardNetworkDropdown(
                selected = formState.cardNetwork,
                onSelected = onCardNetworkChange
            )
            OutlinedTextField(
                value = formState.lastFourDigits,
                onValueChange = onLastFourDigitsChange,
                label = { Text("Last 4 Digits (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.expirationDate,
                onValueChange = onExpirationDateChange,
                label = { Text("Expiration Date (optional)") },
                placeholder = { Text("MM/YY") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        is AddAccountFormState.CreditFormState -> {
            OutlinedTextField(
                value = formState.creditLimit,
                onValueChange = onCreditLimitChange,
                label = { Text("Credit Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.creditUsed,
                onValueChange = onCreditUsedChange,
                label = { Text("Credit Used") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            CardNetworkDropdown(
                selected = formState.cardNetwork,
                onSelected = onCardNetworkChange
            )
            OutlinedTextField(
                value = formState.lastFourDigits,
                onValueChange = onLastFourDigitsChange,
                label = { Text("Last 4 Digits (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.expirationDate,
                onValueChange = onExpirationDateChange,
                label = { Text("Expiration Date (optional)") },
                placeholder = { Text("MM/YY") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.paymentDay,
                onValueChange = onPaymentDayChange,
                label = { Text("Payment Day (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.interestRate,
                onValueChange = onInterestRateChange,
                label = { Text("Interest Rate % (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        is AddAccountFormState.SavingsFormState -> {
            OutlinedTextField(
                value = formState.initialBalance,
                onValueChange = onInitialBalanceChange,
                label = { Text("Initial Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.interestRate,
                onValueChange = onInterestRateChange,
                label = { Text("Interest Rate % (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardNetworkDropdown(
    selected: CardNetwork,
    onSelected: (CardNetwork) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val networks = CardNetwork.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Card Network") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            networks.forEach { network ->
                DropdownMenuItem(
                    text = { Text(network.name) },
                    onClick = {
                        onSelected(network)
                        expanded = false
                    }
                )
            }
        }
    }
}
