package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppSettings
import com.example.data.model.PaymentMethod
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralBadgeText
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.SlateTechnical
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.tabular
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentSettings: AppSettings,
    todayBaseInicial: Long,
    isTodayClosed: Boolean,
    onDismiss: () -> Unit,
    onSaveSettings: (AppSettings) -> Unit,
    onResetSettingsDefaults: () -> Unit,
    onOpenEditTodayBase: () -> Unit,
    onReopenTodayCash: () -> Unit,
    onRequestFactoryReset: () -> Unit
) {
    var businessName by remember(currentSettings) { mutableStateOf(currentSettings.businessName) }
    var businessSubtitle by remember(currentSettings) { mutableStateOf(currentSettings.businessSubtitle) }
    var receiptFooter by remember(currentSettings) { mutableStateOf(currentSettings.receiptFooter) }
    var defaultBaseText by remember(currentSettings) {
        mutableStateOf(
            if (currentSettings.defaultBaseInicial == 0L) "" else currentSettings.defaultBaseInicial.toString()
        )
    }
    var defaultPaymentMethod by remember(currentSettings) { mutableStateOf(currentSettings.defaultPaymentMethod) }
    var confirmBeforeSale by remember(currentSettings) { mutableStateOf(currentSettings.confirmBeforeSale) }
    var showOutOfStockInPos by remember(currentSettings) { mutableStateOf(currentSettings.showOutOfStockInPos) }
    var showCostsAndMargins by remember(currentSettings) { mutableStateOf(currentSettings.showCostsAndMarginsInInventory) }
    var defaultMinStockText by remember(currentSettings) { mutableStateOf(currentSettings.defaultMinStock.toString()) }
    var autoOpenReceiptOnClose by remember(currentSettings) { mutableStateOf(currentSettings.autoOpenReceiptOnClose) }
    var movementsStripLimit by remember(currentSettings) { mutableStateOf(currentSettings.movementsStripLimit) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = EmeraldDeep,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Configuración", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "Personaliza tu negocio, caja e inventario",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTechnical
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar configuración")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Negocio & Recibos
                    SettingsSectionCard(
                        title = "Negocio y Recibos",
                        icon = Icons.Filled.Storefront
                    ) {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Nombre del negocio") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_business_name")
                        )
                        OutlinedTextField(
                            value = businessSubtitle,
                            onValueChange = { businessSubtitle = it },
                            label = { Text("NIT / Teléfono / Dirección (opcional)") },
                            placeholder = { Text("Ej. NIT 900.123.456 · Tel 300 123 4567") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = receiptFooter,
                            onValueChange = { receiptFooter = it },
                            label = { Text("Pie de comprobante") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Caja y Punto de Venta
                    SettingsSectionCard(
                        title = "Caja y Punto de Venta",
                        icon = Icons.Filled.PointOfSale
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderBotanical),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Base de Caja de Hoy",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTechnical
                                    )
                                    Text(
                                        text = Formatters.formatCOP(todayBaseInicial),
                                        style = MaterialTheme.typography.titleMedium.tabular(),
                                        color = EmeraldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onOpenEditTodayBase()
                                    },
                                    enabled = !isTodayClosed,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isTodayClosed) "Cerrada" else "Modificar")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = defaultBaseText,
                            onValueChange = { defaultBaseText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Base inicial predeterminada (días nuevos)") },
                            prefix = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_default_base")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0L to "$0", 50000L to "$50k", 100000L to "$100k").forEach { (amt, label) ->
                                OutlinedButton(
                                    onClick = { defaultBaseText = if (amt == 0L) "" else amt.toString() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Text(
                            text = "Método de pago predeterminado",
                            style = MaterialTheme.typography.labelMedium,
                            color = CharcoalText
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = defaultPaymentMethod == method,
                                    onClick = { defaultPaymentMethod = method },
                                    label = { Text(method.shortLabel) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldDeep,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Últimos movimientos visibles en Caja",
                            style = MaterialTheme.typography.labelMedium,
                            color = CharcoalText
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15).forEach { count ->
                                FilterChip(
                                    selected = movementsStripLimit == count,
                                    onClick = { movementsStripLimit = count },
                                    label = { Text("$count mov.") },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldDeep,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = DividerSubtle)

                        SettingsSwitchRow(
                            title = "Confirmar antes de cobrar",
                            subtitle = "Pide confirmación antes de guardar cada venta",
                            checked = confirmBeforeSale,
                            onCheckedChange = { confirmBeforeSale = it }
                        )

                        SettingsSwitchRow(
                            title = "Abrir recibo al cerrar caja",
                            subtitle = "Muestra el comprobante completo en lugar de la alerta simple",
                            checked = autoOpenReceiptOnClose,
                            onCheckedChange = { autoOpenReceiptOnClose = it }
                        )
                    }

                    // 3. Inventario y Visibilidad
                    SettingsSectionCard(
                        title = "Inventario y Visibilidad",
                        icon = Icons.Filled.Inventory2
                    ) {
                        OutlinedTextField(
                            value = defaultMinStockText,
                            onValueChange = { defaultMinStockText = it.filter { ch -> ch.isDigit() }.take(4) },
                            label = { Text("Stock mínimo sugerido en productos nuevos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SettingsSwitchRow(
                            title = "Mostrar productos agotados en Caja",
                            subtitle = "Si se desactiva, solo verás productos con stock disponible",
                            checked = showOutOfStockInPos,
                            onCheckedChange = { showOutOfStockInPos = it }
                        )

                        SettingsSwitchRow(
                            title = "Mostrar costos y márgenes en Inventario",
                            subtitle = "Desactívalo para ocultar costos de compra a otros usuarios",
                            checked = showCostsAndMargins,
                            onCheckedChange = { showCostsAndMargins = it }
                        )
                    }

                    // 4. Mantenimiento y Caja
                    SettingsSectionCard(
                        title = "Mantenimiento y Datos",
                        icon = Icons.Filled.AdminPanelSettings
                    ) {
                        if (isTodayClosed) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onReopenTodayCash()
                                },
                                border = BorderStroke(1.2.dp, EmeraldDeep),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDeep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_reopen_cash_button")
                            ) {
                                Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reabrir Caja de Hoy")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onResetSettingsDefaults()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Restaurar Preferencias Predeterminadas")
                        }

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onRequestFactoryReset()
                            },
                            border = BorderStroke(1.2.dp, CoralBadgeText),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralBadgeText),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_reset_zero_button")
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reiniciar Todo en Cero")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onSaveSettings(
                                AppSettings(
                                    businessName = businessName.trim().ifBlank { "Distribuidora Sarangier" },
                                    businessSubtitle = businessSubtitle.trim(),
                                    receiptFooter = receiptFooter.trim(),
                                    defaultBaseInicial = defaultBaseText.toLongOrNull() ?: 0L,
                                    defaultPaymentMethod = defaultPaymentMethod,
                                    confirmBeforeSale = confirmBeforeSale,
                                    showOutOfStockInPos = showOutOfStockInPos,
                                    showCostsAndMarginsInInventory = showCostsAndMargins,
                                    defaultMinStock = defaultMinStockText.toIntOrNull() ?: 5,
                                    autoOpenReceiptOnClose = autoOpenReceiptOnClose,
                                    movementsStripLimit = movementsStripLimit
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settings_save_button")
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, BorderBotanical),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = EmeraldDeep,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTechnical
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = EmeraldDeep)
        )
    }
}
