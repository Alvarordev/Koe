package com.hazard.koe.presentation.accounts.addaccount

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.CardNetwork
import com.hazard.koe.data.enums.SupportedCurrency
import com.hazard.koe.presentation.accounts.components.AccountCard
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import com.hazard.koe.presentation.util.ColorPalette

private fun parseColor(hex: String): Color = runCatching {
    Color(hex.toColorInt())
}.getOrDefault(Color(0xFF1A73E8))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    accountId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(accountId) {
        accountId?.let { viewModel.loadAccountForEdit(it) }
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onNavigateBack()
        }
    }

    if (uiState.isEditing) {
        EditAccountScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        WizardScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountScreen(
    uiState: AddAccountUiState,
    viewModel: AddAccountViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar cuenta") },
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

            EditAccountTypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::selectType
            )

            EditCommonFields(
                formState = uiState.formState,
                onNameChange = viewModel::updateName,
                onColorChange = viewModel::updateColor,
                onCurrencyChange = viewModel::updateCurrency
            )

            EditTypeSpecificFields(
                formState = uiState.formState,
                onInitialBalanceChange = viewModel::updateInitialBalance,
                onCreditLimitChange = viewModel::updateCreditLimit,
                onCreditUsedChange = viewModel::updateCreditUsed,
                onCardNetworkChange = viewModel::updateCardNetwork,
                onLastFourDigitsChange = viewModel::updateLastFourDigits,
                onExpirationDateChange = viewModel::updateExpirationDate,
                onPaymentDayChange = viewModel::updatePaymentDay,
                onClosingDayChange = viewModel::updateClosingDay,
                onInterestRateChange = viewModel::updateInterestRate
            )

            val editError = uiState.errorMessage
            if (editError != null) {
                Text(
                    text = editError,
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
                    if (uiState.isSubmitting) "Guardando..."
                    else "Guardar cambios"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardScreen(
    uiState: AddAccountUiState,
    viewModel: AddAccountViewModel,
    onNavigateBack: () -> Unit
) {
    val isLastStep = uiState.currentStep == uiState.totalSteps

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    WizardStepIndicator(
                        currentStep = uiState.currentStep,
                        totalSteps = uiState.totalSteps,
                        hasCardDetailsStep = uiState.hasCardDetailsStep
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep == 1) onNavigateBack()
                            else viewModel.previousStep()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atr\u00e1s"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val wizardError = uiState.errorMessage
                if (wizardError != null) {
                    Text(
                        text = wizardError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (isLastStep) viewModel.submit()
                        else viewModel.nextStep()
                    },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastStep) "Ver Cuenta" else "Continuar",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn())
                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "wizard_step"
        ) { step ->
            val actualStep = if (!uiState.hasCardDetailsStep && step >= 3) step + 1 else step

            when (actualStep) {
                1 -> WizardStepAccountType(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewModel::selectType,
                    modifier = Modifier.padding(innerPadding)
                )
                2 -> WizardStepDetails(
                    formState = uiState.formState,
                    selectedType = uiState.selectedType,
                    onCurrencyChange = viewModel::updateCurrency,
                    onInitialBalanceChange = viewModel::updateInitialBalance,
                    onCreditLimitChange = viewModel::updateCreditLimit,
                    onCreditUsedChange = viewModel::updateCreditUsed,
                    onCardNetworkChange = viewModel::updateCardNetwork,
                    onPaymentDayChange = viewModel::updatePaymentDay,
                    onClosingDayChange = viewModel::updateClosingDay,
                    onInterestRateChange = viewModel::updateInterestRate,
                    modifier = Modifier.padding(innerPadding)
                )
                3 -> WizardStepCardDetails(
                    formState = uiState.formState,
                    onLastFourDigitsChange = viewModel::updateLastFourDigits,
                    onExpirationDateChange = viewModel::updateExpirationDate,
                    modifier = Modifier.padding(innerPadding)
                )
                4 -> WizardStepName(
                    formState = uiState.formState,
                    selectedType = uiState.selectedType,
                    onNameChange = viewModel::updateName,
                    modifier = Modifier.padding(innerPadding)
                )
                5 -> WizardStepColor(
                    selectedColor = uiState.formState.color,
                    onColorChange = viewModel::updateColor,
                    modifier = Modifier.padding(innerPadding)
                )
                6 -> WizardStepConfirmation(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun WizardStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    hasCardDetailsStep: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val minWidth = 20.dp
        val maxWidth = 40.dp

        for (i in 1..totalSteps) {
            val isCurrent = i == currentStep

            val color = if (isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            val animatedWidth by animateDpAsState(
                targetValue = if (isCurrent) maxWidth else minWidth,
                animationSpec = tween(300),
                label = "container_width_$i"
            )

            Box(
                modifier = Modifier
                    .width(animatedWidth) // 🔥 layout dinámico
                    .height(4.dp),
                contentAlignment = Alignment.Center // 🔥 clave
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // ocupa todo el contenedor
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun WizardStepAccountType(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tipo de Cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige el tipo de cuenta que deseas agregar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        val typeItems = listOf(
            Triple(AccountType.CASH, "Efectivo", Icons.Default.AccountBalanceWallet),
            Triple(AccountType.DEBIT, "D\u00e9bito", Icons.Default.CreditCard),
            Triple(AccountType.CREDIT, "Cr\u00e9dito", Icons.Default.Payment),
            Triple(AccountType.SAVINGS, "Ahorros", Icons.Default.Savings)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (colIndex in 0..1) {
                        val index = rowIndex * 2 + colIndex
                        val (type, label, icon) = typeItems[index]
                        AccountTypeTile(
                            label = label,
                            icon = icon,
                            isSelected = type == selectedType,
                            onClick = { onTypeSelected(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountTypeTile(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        Color.Transparent

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardStepDetails(
    formState: AddAccountFormState,
    selectedType: AccountType,
    onCurrencyChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onCreditLimitChange: (String) -> Unit,
    onCreditUsedChange: (String) -> Unit,
    onCardNetworkChange: (CardNetwork) -> Unit,
    onPaymentDayChange: (String) -> Unit,
    onClosingDayChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Detalles de la Cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        var currencyExpanded by remember { mutableStateOf(false) }
        val currencies = SupportedCurrency.entries

        WizardFieldLabel(text = "Moneda")
        ExposedDropdownMenuBox(
            expanded = currencyExpanded,
            onExpandedChange = { currencyExpanded = it }
        ) {
            WizardDropdownField(
                value = formState.currencyCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = currencyExpanded,
                onDismissRequest = { currencyExpanded = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text("${currency.code} \u2014 ${currency.currencyName}") },
                        onClick = {
                            onCurrencyChange(currency.code)
                            currencyExpanded = false
                        }
                    )
                }
            }
        }

        when (formState) {
            is AddAccountFormState.CashFormState -> {
                WizardFieldLabel(text = "Saldo Inicial")
                WizardTextField(
                    value = formState.initialBalance,
                    onValueChange = onInitialBalanceChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
            is AddAccountFormState.DebitFormState -> {
                WizardFieldLabel(text = "Saldo Inicial")
                WizardTextField(
                    value = formState.initialBalance,
                    onValueChange = onInitialBalanceChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                WizardFieldLabel(text = "Red de tarjeta")
                WizardCardNetworkDropdown(
                    selected = formState.cardNetwork,
                    onSelected = onCardNetworkChange
                )
            }
            is AddAccountFormState.CreditFormState -> {
                WizardFieldLabel(text = "L\u00edmite de Cr\u00e9dito")
                WizardTextField(
                    value = formState.creditLimit,
                    onValueChange = onCreditLimitChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                WizardFieldLabel(text = "Cr\u00e9dito Usado")
                WizardTextField(
                    value = formState.creditUsed,
                    onValueChange = onCreditUsedChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                WizardFieldLabel(text = "Red de tarjeta")
                WizardCardNetworkDropdown(
                    selected = formState.cardNetwork,
                    onSelected = onCardNetworkChange
                )
                WizardFieldLabel(text = "D\u00eda de vencimiento")
                WizardTextField(
                    value = formState.paymentDay,
                    onValueChange = onPaymentDayChange,
                    placeholder = "1-31",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                WizardFieldLabel(text = "D\u00eda de cierre (opcional)")
                WizardTextField(
                    value = formState.closingDay,
                    onValueChange = onClosingDayChange,
                    placeholder = "1-31",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                Text(
                    text = "Si lo dejas vac\u00edo, se toma todo el mes calendario y vence al mes siguiente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WizardFieldLabel(text = "Tasa de inter\u00e9s % (opcional)")
                WizardTextField(
                    value = formState.interestRate,
                    onValueChange = onInterestRateChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
            is AddAccountFormState.SavingsFormState -> {
                WizardFieldLabel(text = "Saldo Inicial")
                WizardTextField(
                    value = formState.initialBalance,
                    onValueChange = onInitialBalanceChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                WizardFieldLabel(text = "Tasa de inter\u00e9s % (opcional)")
                WizardTextField(
                    value = formState.interestRate,
                    onValueChange = onInterestRateChange,
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WizardStepCardDetails(
    formState: AddAccountFormState,
    onLastFourDigitsChange: (String) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val lastFour = when (formState) {
        is AddAccountFormState.DebitFormState -> formState.lastFourDigits
        is AddAccountFormState.CreditFormState -> formState.lastFourDigits
        else -> ""
    }
    val expDate = when (formState) {
        is AddAccountFormState.DebitFormState -> formState.expirationDate
        is AddAccountFormState.CreditFormState -> formState.expirationDate
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Datos de la Tarjeta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Estos datos son opcionales.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        WizardFieldLabel(text = "\u00daltimos 4 d\u00edgitos")
        WizardTextField(
            value = lastFour,
            onValueChange = { value ->
                if (value.all { it.isDigit() } && value.length <= 4) {
                    onLastFourDigitsChange(value)
                }
            },
            placeholder = "0000",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        WizardFieldLabel(text = "Fecha de vencimiento")
        WizardTextField(
            value = expDate,
            onValueChange = onExpirationDateChange,
            placeholder = "MM/YY",
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WizardStepName(
    formState: AddAccountFormState,
    selectedType: AccountType,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val placeholder = when (selectedType) {
        AccountType.CASH -> "Mi Efectivo"
        AccountType.DEBIT -> "Mi Tarjeta de D\u00e9bito"
        AccountType.CREDIT -> "Mi Tarjeta de Cr\u00e9dito"
        AccountType.SAVINGS -> "Mi Cuenta de Ahorros"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nombre de la Cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Elige un nombre para tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        WizardFieldLabel(text = "Nombre")
        WizardTextField(
            value = formState.name,
            onValueChange = onNameChange,
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Text(
            text = "Tu nombre es privado y solo visible para ti. Puedes cambiarlo despu\u00e9s.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WizardStepColor(
    selectedColor: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Elige un Color",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige un color para tu cuenta \u2014 puedes cambiarlo despu\u00e9s.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ColorPalette) { hex ->
                val isSelected = selectedColor == hex
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(parseColor(hex))
                        .then(
                            if (isSelected) Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            ) else Modifier
                        )
                        .clickable { onColorChange(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Solo t\u00fa ver\u00e1s el color de tu cuenta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WizardStepConfirmation(
    viewModel: AddAccountViewModel,
    modifier: Modifier = Modifier
) {
    val previewAccount = remember(
        viewModel.uiState.collectAsState().value.formState,
        viewModel.uiState.collectAsState().value.selectedType
    ) {
        viewModel.buildPreviewAccount()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row {
            Text(
                text = "Tu cuenta est\u00e1 ",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "lista.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        AccountCard(
            account = previewAccount,
            cardHeight = 200.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu cuenta fue agregada exitosamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WizardFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardDropdownField(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardCardNetworkDropdown(
    selected: CardNetwork,
    onSelected: (CardNetwork) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val networks = CardNetwork.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        WizardDropdownField(
            value = selected.name,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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

@Composable
private fun EditAccountTypeSelector(
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
private fun EditCommonFields(
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

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ColorPalette) { hex ->
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = currencyExpanded,
            onDismissRequest = { currencyExpanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${currency.code} \u2014 ${currency.currencyName}") },
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
private fun EditTypeSpecificFields(
    formState: AddAccountFormState,
    onInitialBalanceChange: (String) -> Unit,
    onCreditLimitChange: (String) -> Unit,
    onCreditUsedChange: (String) -> Unit,
    onCardNetworkChange: (CardNetwork) -> Unit,
    onLastFourDigitsChange: (String) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onPaymentDayChange: (String) -> Unit,
    onClosingDayChange: (String) -> Unit,
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
                label = { Text("D\u00eda de vencimiento (payment day)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.closingDay,
                onValueChange = onClosingDayChange,
                label = { Text("D\u00eda de cierre (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text("Si lo dejas vac\u00edo, se toma todo el mes calendario y vence al mes siguiente.")
                },
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
            label = { Text("Red de tarjeta") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
