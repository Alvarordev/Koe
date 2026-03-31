package com.example.tracker.presentation.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.enums.LoanDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(
    uiState: AddLoanUiState,
    onPersonNameChange: (String) -> Unit,
    onPersonSelected: (PersonSuggestion) -> Unit,
    onLoanTypeChange: (LoanType) -> Unit,
    onDirectionChange: (LoanDirection) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFormalLoanNameChange: (String) -> Unit,
    onLenderNameChange: (String) -> Unit,
    onAnnualRateChange: (String) -> Unit,
    onTermMonthsChange: (String) -> Unit,
    onMonthlyPaymentChange: (String) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Préstamo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Loan Type Selector
            Text(
                text = "Tipo de Préstamo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.loanType == LoanType.CASUAL,
                    onClick = { onLoanTypeChange(LoanType.CASUAL) },
                    label = { Text("Informal") },
                    leadingIcon = if (uiState.loanType == LoanType.CASUAL) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = uiState.loanType == LoanType.FORMAL,
                    onClick = { onLoanTypeChange(LoanType.FORMAL) },
                    label = { Text("Bancario") },
                    leadingIcon = if (uiState.loanType == LoanType.FORMAL) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.loanType == LoanType.CASUAL) {
                // Casual Loan Form
                CasualLoanForm(
                    uiState = uiState,
                    onPersonNameChange = onPersonNameChange,
                    onPersonSelected = onPersonSelected,
                    onDirectionChange = onDirectionChange,
                    onAmountChange = onAmountChange,
                    onCurrencyChange = onCurrencyChange,
                    onDescriptionChange = onDescriptionChange,
                    onSave = onSave
                )
            } else {
                // Formal Loan Form
                FormalLoanForm(
                    uiState = uiState,
                    onFormalLoanNameChange = onFormalLoanNameChange,
                    onLenderNameChange = onLenderNameChange,
                    onAmountChange = onAmountChange,
                    onCurrencyChange = onCurrencyChange,
                    onAnnualRateChange = onAnnualRateChange,
                    onTermMonthsChange = onTermMonthsChange,
                    onMonthlyPaymentChange = onMonthlyPaymentChange,
                    onAccountSelected = onAccountSelected,
                    onSave = onSave
                )
            }
        }
    }
}

@Composable
private fun CasualLoanForm(
    uiState: AddLoanUiState,
    onPersonNameChange: (String) -> Unit,
    onPersonSelected: (PersonSuggestion) -> Unit,
    onDirectionChange: (LoanDirection) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column {
        // Direction Selector (Lend/Borrow)
        Text(
            text = "¿Qué tipo es?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.direction == LoanDirection.LENT,
                onClick = { onDirectionChange(LoanDirection.LENT) },
                label = { Text("Me prestó (Deben)") },
                leadingIcon = if (uiState.direction == LoanDirection.LENT) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            FilterChip(
                selected = uiState.direction == LoanDirection.BORROWED,
                onClick = { onDirectionChange(LoanDirection.BORROWED) },
                label = { Text("Pedí prestado (Debo)") },
                leadingIcon = if (uiState.direction == LoanDirection.BORROWED) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Person Field with Suggestions
        Text(
            text = "Persona",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box {
            OutlinedTextField(
                value = uiState.personName,
                onValueChange = onPersonNameChange,
                placeholder = { Text("Nombre de la persona") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Text("👤", fontSize = 20.sp)
                }
            )

            if (uiState.showPersonSuggestions && uiState.suggestedPersons.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Column {
                        uiState.suggestedPersons.forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPersonSelected(person) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = person.emoji ?: "👤", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = person.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount
        Text(
            text = "Monto",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencySelector(
                selectedCurrency = uiState.currencyCode,
                onCurrencySelected = onCurrencyChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.amount.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Descripción (opcional)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Descripción del préstamo") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !uiState.isSaving && uiState.personName.isNotBlank() && uiState.amount.isNotBlank()
        ) {
            Text(
                text = if (uiState.isSaving) "Guardando..." else "Guardar Préstamo",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun FormalLoanForm(
    uiState: AddLoanUiState,
    onFormalLoanNameChange: (String) -> Unit,
    onLenderNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onAnnualRateChange: (String) -> Unit,
    onTermMonthsChange: (String) -> Unit,
    onMonthlyPaymentChange: (String) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onSave: () -> Unit
) {
    Column {
        // Loan Name
        OutlinedTextField(
            value = uiState.formalLoanName,
            onValueChange = onFormalLoanNameChange,
            label = { Text("Nombre del préstamo") },
            placeholder = { Text("Ej: Crédito Personal") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lender Name
        OutlinedTextField(
            value = uiState.lenderName,
            onValueChange = onLenderNameChange,
            label = { Text("Banco/Institución") },
            placeholder = { Text("Ej: BCP, Interbank") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencySelector(
                selectedCurrency = uiState.currencyCode,
                onCurrencySelected = onCurrencyChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.amount.isEmpty()) {
                            Text(
                                text = "Monto total",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Annual Rate
        OutlinedTextField(
            value = uiState.annualRate,
            onValueChange = onAnnualRateChange,
            label = { Text("Tasa anual (%)") },
            placeholder = { Text("Ej: 15.5") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Term Months
        OutlinedTextField(
            value = uiState.termMonths,
            onValueChange = onTermMonthsChange,
            label = { Text("Plazo (meses)") },
            placeholder = { Text("Ej: 12") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Monthly Payment
        OutlinedTextField(
            value = uiState.monthlyPayment,
            onValueChange = onMonthlyPaymentChange,
            label = { Text("Cuota mensual") },
            placeholder = { Text("Ej: 500") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !uiState.isSaving && 
                      uiState.formalLoanName.isNotBlank() && 
                      uiState.lenderName.isNotBlank() &&
                      uiState.amount.isNotBlank()
        ) {
            Text(
                text = if (uiState.isSaving) "Guardando..." else "Guardar Préstamo",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    val currencies = listOf("USD", "PEN", "EUR", "GBP")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        currencies.forEach { currency ->
            val isSelected = currency == selectedCurrency
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onCurrencySelected(currency) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = currency,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}