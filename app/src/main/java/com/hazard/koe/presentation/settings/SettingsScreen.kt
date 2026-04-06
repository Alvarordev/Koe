package com.hazard.koe.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hazard.koe.data.preferences.ThemePreference
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onDatabaseReset: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val isResetting by viewModel.isResetting.collectAsStateWithLifecycle()
    val resetComplete by viewModel.resetComplete.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportedFile by viewModel.exportedFile.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importComplete by viewModel.importComplete.collectAsStateWithLifecycle()
    val importError by viewModel.importError.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    LaunchedEffect(resetComplete) {
        if (resetComplete) {
            viewModel.onResetCompleteHandled()
            onDatabaseReset()
        }
    }

    LaunchedEffect(importComplete) {
        if (importComplete) {
            viewModel.onImportCompleteHandled()
            onDatabaseReset()
        }
    }

    LaunchedEffect(exportedFile) {
        exportedFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar datos"))
            viewModel.onExportHandled()
        }
    }

    if (showImportDialog) {
        ImportDataDialog(
            isImporting = isImporting,
            onConfirm = {
                showImportDialog = false
                importFilePicker.launch(arrayOf("application/zip"))
            },
            onDismiss = {
                if (!isImporting) showImportDialog = false
            }
        )
    }

    if (importError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onImportErrorHandled() },
            title = { Text("Error al importar") },
            text = { Text(importError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.onImportErrorHandled() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (showResetDialog) {
        ResetDatabaseDialog(
            isResetting = isResetting,
            onConfirm = {
                viewModel.resetDatabase()
            },
            onDismiss = {
                if (!isResetting) {
                    showResetDialog = false
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        }
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        item {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        item {
            ThemeSelector(
                currentTheme = themePreference,
                onThemeSelected = viewModel::setThemePreference
            )
        }
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isExporting) { viewModel.exportData() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Exportar datos",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isImporting) { showImportDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Importar datos",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Reiniciar base de datos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit
) {
    val options = listOf(
        Triple(ThemePreference.Dark, Icons.Default.DarkMode, "Oscuro"),
        Triple(ThemePreference.Light, Icons.Default.LightMode, "Claro"),
        Triple(ThemePreference.System, Icons.Default.PhoneAndroid, "Sistema")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { (preference, icon, label) ->
            val isSelected = currentTheme == preference
            ThemeOption(
                icon = icon,
                label = label,
                isSelected = isSelected,
                onClick = { onThemeSelected(preference) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = if (isSelected) primaryColor.copy(alpha = 0.12f) else colorScheme.surface
    val borderColor = if (isSelected) primaryColor else Color.Transparent
    val contentColor = if (isSelected) primaryColor else colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun ImportDataDialog(
    isImporting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isImporting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(text = if (isImporting) "Importando..." else "Importar datos")
        },
        text = {
            Text(
                text = if (isImporting) {
                    "Restaurando datos desde el archivo de respaldo..."
                } else {
                    "Se reemplazarán todos los datos actuales con los del archivo de respaldo.\n\nEsta acción no se puede deshacer."
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isImporting
            ) {
                Text("Seleccionar archivo")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isImporting
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ResetDatabaseDialog(
    isResetting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isResetting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            Text(
                text = if (isResetting) "Reiniciando..." else "Reiniciar base de datos"
            )
        },
        text = {
            Text(
                text = if (isResetting) {
                    "Eliminando datos y recreando la base de datos..."
                } else {
                    "Se eliminarán todas las transacciones, cuentas, categorías y demás datos. Se crearán las categorías y cuenta por defecto.\n\nEsta acción no se puede deshacer."
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isResetting
            ) {
                Text(
                    text = "Reiniciar",
                    color = if (isResetting) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isResetting
            ) {
                Text("Cancelar")
            }
        }
    )
}