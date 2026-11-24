package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CashSessionStatus
import com.example.data.model.ConsolidatedProductRow
import com.example.data.model.DailySettlementSummary
import com.example.ui.PosUiState
import com.example.ui.components.BotanicalCard
import com.example.ui.components.ClosureReceiptDialog
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberBadgeBg
import com.example.ui.theme.AmberBadgeText
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.CanvasSandLinen
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralBadgeBg
import com.example.ui.theme.CoralBadgeText
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.SlateTechnical
import com.example.ui.theme.StockOkBg
import com.example.ui.theme.StockOkText
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.tabular
import com.example.util.Formatters

@Composable
fun LiquidacionScreen(
    state: PosUiState,
    onSelectDate: (String) -> Unit,
    onCountedCashChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onUpdateBaseInicial: (date: String, amount: Long) -> Unit,
    onCloseCashDay: () -> Unit,
    onDismissClosureSuccessAlert: () -> Unit,
    onOpenReceiptModal: () -> Unit,
    onDismissReceiptModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = state.settlementSummary
    var showNetProfitTooltip by remember { mutableStateOf(false) }
    var showCloseCashDialog by remember { mutableStateOf(false) }
    var showBaseDialog by remember { mutableStateOf(false) }

    if (summary == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando liquidación...", color = SlateTechnical)
        }
        return
    }

    val isClosed = summary.session.estado == CashSessionStatus.CERRADA

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasSandLinen)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp)
        ) {
            // Date Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = EmeraldDeep, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Fecha de Liquidación", style = MaterialTheme.typography.titleMedium)
                        }
                        StatusPill(
                            text = if (isClosed) "CERRADA ${summary.session.horaCierre ?: ""}".trim() else "ABIERTA",
                            backgroundColor = if (isClosed) CoralBadgeBg else StockOkBg,
                            textColor = if (isClosed) CoralBadgeText else StockOkText
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.availableDates) { dateStr ->
                            val selected = dateStr == state.selectedSettlementDate
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDate(dateStr) },
                                label = { Text(Formatters.formatDateLabel(dateStr)) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldDeep,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardWhite
                                ),
                                modifier = Modifier.testTag("settlement_date_chip_$dateStr")
                            )
                        }
                    }

                    if (isClosed) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StockOkBg,
                            border = BorderStroke(1.dp, StockOkText.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("closure_success_banner")
                        ) {
                            Text(
                                text = "Cierre de Caja Exitoso — Jornada del ${Formatters.formatDateLabel(summary.date)} consolidada y bloqueada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StockOkText,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Financial Engine Cards
            item {
                BotanicalCard {
                    Text("Motor Financiero del Día", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))

                    // Gross Income + Payment Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ingresos Brutos (${summary.salesCount} ventas)", style = MaterialTheme.typography.bodyMedium, color = SlateTechnical)
                            Text(
                                text = Formatters.formatCOP(summary.grossIncome),
                                style = MaterialTheme.typography.headlineMedium.tabular(),
                                color = EmeraldDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BreakdownPill("Efectivo", summary.cashSales, Modifier.weight(1f))
                        BreakdownPill("Nequi/Transf.", summary.transferSales, Modifier.weight(1f))
                        BreakdownPill("Tarjeta", summary.cardSales, Modifier.weight(1f))
                    }

                    HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 12.dp))

                    FinanceLineRow(
                        label = "Costo de Mercancía Vendida (CMV)",
                        amount = summary.cmv,
                        color = SlateTechnical
                    )
                    Spacer(Modifier.height(6.dp))
                    FinanceLineRow(
                        label = "Egresos Operativos",
                        amount = summary.operatingExpenses,
                        color = CoralBadgeText
                    )
                    Spacer(Modifier.height(6.dp))
                    FinanceLineRow(
                        label = "Compras / Pagos a Proveedores",
                        amount = summary.supplierPayments,
                        color = AmberBadgeText
                    )

                    HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 12.dp))

                    // Utilidad Neta Estimada with Tooltip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Utilidad Neta Estimada",
                                style = MaterialTheme.typography.titleMedium,
                                color = CharcoalText
                            )
                            IconButton(
                                onClick = { showNetProfitTooltip = !showNetProfitTooltip },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("net_profit_tooltip_button")
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Explicación de utilidad neta",
                                    tint = EmeraldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = Formatters.formatCOP(summary.netProfit),
                            style = MaterialTheme.typography.headlineSmall.tabular(),
                            color = if (summary.netProfit >= 0) StockOkText else CoralBadgeText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showNetProfitTooltip) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StockOkBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Fórmula: (Ingresos Brutos - CMV) - Egresos Operativos. Las compras o pagos a proveedores NO restan utilidad porque ingresan como activo de inventario.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StockOkText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Arqueo Físico y Cierre
            item {
                val effectiveCounted: Long? = if (isClosed) {
                    summary.session.efectivoContado
                } else {
                    Formatters.parseCOPInput(state.countedCashInput)
                }
                val diff = effectiveCounted?.minus(summary.expectedCashInDrawer)

                BotanicalCard {
                    Text("Arqueo de Caja Físico", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Base Inicial del Día", style = MaterialTheme.typography.bodyMedium, color = SlateTechnical)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isClosed) {
                                        TextButton(
                                            onClick = { showBaseDialog = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Editar base")
                                        }
                                    }
                                    Text(
                                        text = Formatters.formatCOP(summary.session.baseInicial),
                                        style = MaterialTheme.typography.titleSmall.tabular(),
                                        color = CharcoalText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            FinanceLineRow("(+) Ventas en Efectivo", summary.cashSales, StockOkText)
                            FinanceLineRow("(-) Egresos en Efectivo", summary.cashExpenses, CoralBadgeText)
                            HorizontalDivider(color = DividerSubtle)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Efectivo Esperado en Cajón", style = MaterialTheme.typography.titleSmall, color = EmeraldDeep)
                                Text(
                                    text = Formatters.formatCOP(summary.expectedCashInDrawer),
                                    style = MaterialTheme.typography.titleLarge.tabular(),
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!isClosed) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Conteo Físico",
                                style = MaterialTheme.typography.labelLarge,
                                color = SlateTechnical
                            )
                            TextButton(
                                onClick = {
                                    onCountedCashChange(summary.expectedCashInDrawer.toString())
                                }
                            ) {
                                Text("Usar esperado (${Formatters.formatCOP(summary.expectedCashInDrawer)})")
                            }
                        }
                        OutlinedTextField(
                            value = state.countedCashInput,
                            onValueChange = onCountedCashChange,
                            label = { Text("Efectivo Contado Físicamente") },
                            prefix = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = CardWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("counted_cash_input")
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.closureNotesInput,
                            onValueChange = onNotesChange,
                            label = { Text("Observaciones de cierre (opcional)") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Efectivo Contado:", style = MaterialTheme.typography.bodyLarge, color = SlateTechnical)
                            Text(
                                text = Formatters.formatCOP(effectiveCounted ?: 0L),
                                style = MaterialTheme.typography.titleLarge.tabular(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!summary.session.observacionesCierre.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Notas: ${summary.session.observacionesCierre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTechnical
                            )
                        }
                    }

                    if (diff != null) {
                        Spacer(Modifier.height(10.dp))
                        val (badgeLabel, badgeBg, badgeText) = when {
                            diff == 0L -> Triple("Cuadrado (${Formatters.formatCOP(0)})", StockOkBg, StockOkText)
                            diff < 0L -> Triple("Faltante (${Formatters.formatSignedCOP(diff)})", CoralBadgeBg, CoralBadgeText)
                            else -> Triple("Sobrante (${Formatters.formatSignedCOP(diff)})", AmberBadgeBg, AmberBadgeText)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resultado de Arqueo:", style = MaterialTheme.typography.labelLarge, color = SlateTechnical)
                            StatusPill(text = badgeLabel, backgroundColor = badgeBg, textColor = badgeText)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (!isClosed) {
                        Button(
                            onClick = {
                                if (Formatters.parseCOPInput(state.countedCashInput) != null) {
                                    onCloseCashDay()
                                } else {
                                    showCloseCashDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("close_cash_day_button")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cerrar Caja y Consolidar Día")
                        }
                    } else {
                        Button(
                            onClick = onOpenReceiptModal,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("view_closure_receipt_button")
                        ) {
                            Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ver Tirilla / Acta de Cierre")
                        }
                    }
                }
            }

            // Consolidated Products Sold Table
            item {
                BotanicalCard {
                    Text(
                        text = "Productos Vendidos Consolidados (${summary.consolidatedProducts.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(10.dp))

                    if (summary.consolidatedProducts.isEmpty()) {
                        Text(
                            text = "Sin ventas registradas para esta fecha.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateTechnical
                        )
                    } else {
                        // Header
                        Surface(
                            color = CanvasSandLinen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PRODUCTO", style = MaterialTheme.typography.labelSmall, color = SlateTechnical, modifier = Modifier.weight(1.9f))
                                Text("UND", style = MaterialTheme.typography.labelSmall, color = SlateTechnical, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                                Text("COSTO", style = MaterialTheme.typography.labelSmall, color = SlateTechnical, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text("VENTA", style = MaterialTheme.typography.labelSmall, color = SlateTechnical, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text("UTIL.", style = MaterialTheme.typography.labelSmall, color = SlateTechnical, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        summary.consolidatedProducts.forEachIndexed { idx, row ->
                            ConsolidatedRowView(row = row)
                            if (idx < summary.consolidatedProducts.lastIndex) {
                                HorizontalDivider(color = DividerSubtle)
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bar so "Cerrar Caja" is ALWAYS visible without scrolling
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = CardWhite,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, BorderBotanical)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isClosed) "Caja consolidada" else "Efectivo esperado",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTechnical
                    )
                    Text(
                        text = Formatters.formatCOP(summary.expectedCashInDrawer),
                        style = MaterialTheme.typography.titleLarge.tabular(),
                        color = EmeraldDeep,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isClosed) {
                    Button(
                        onClick = {
                            if (Formatters.parseCOPInput(state.countedCashInput) != null) {
                                onCloseCashDay()
                            } else {
                                showCloseCashDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("sticky_close_cash_button")
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar Caja", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Button(
                        onClick = onOpenReceiptModal,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("sticky_view_receipt_button")
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ver Tirilla", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showCloseCashDialog && !isClosed) {
        CloseCashConfirmDialog(
            summary = summary,
            countedCashInput = state.countedCashInput,
            notesInput = state.closureNotesInput,
            onCountedCashChange = onCountedCashChange,
            onNotesChange = onNotesChange,
            onDismiss = { showCloseCashDialog = false },
            onConfirmClose = {
                showCloseCashDialog = false
                onCloseCashDay()
            }
        )
    }

    if (state.showClosureSuccessAlert) {
        val counted = summary.session.efectivoContado ?: 0L
        val diff = counted - summary.expectedCashInDrawer
        AlertDialog(
            onDismissRequest = onDismissClosureSuccessAlert,
            title = { Text("Cierre de Caja Exitoso") },
            text = {
                Text(
                    "La caja del ${Formatters.formatDateLabel(summary.date)} se cerró correctamente.\n\n" +
                        "Efectivo esperado: ${Formatters.formatCOP(summary.expectedCashInDrawer)}\n" +
                        "Efectivo contado: ${Formatters.formatCOP(counted)}\n" +
                        "Diferencia: ${Formatters.formatSignedCOP(diff)}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDismissClosureSuccessAlert,
                    modifier = Modifier.testTag("closure_success_alert_ok")
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (state.showClosureReceiptModal) {
        ClosureReceiptDialog(
            summary = summary,
            settings = state.settings,
            onDismiss = onDismissReceiptModal
        )
    }

    if (showBaseDialog && !isClosed) {
        var baseText by remember(summary.session.baseInicial) {
            mutableStateOf(if (summary.session.baseInicial == 0L) "" else summary.session.baseInicial.toString())
        }
        AlertDialog(
            onDismissRequest = { showBaseDialog = false },
            title = { Text("Base Inicial de Caja") },
            text = {
                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monto inicial en cajón (COP)") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateBaseInicial(summary.date, baseText.toLongOrNull() ?: 0L)
                        showBaseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CloseCashConfirmDialog(
    summary: DailySettlementSummary,
    countedCashInput: String,
    notesInput: String,
    onCountedCashChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmClose: () -> Unit
) {
    val parsedCounted = Formatters.parseCOPInput(countedCashInput)
    val diff = parsedCounted?.minus(summary.expectedCashInDrawer)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cerrar Caja del Día", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Efectivo Esperado:", style = MaterialTheme.typography.bodyMedium, color = SlateTechnical)
                        Text(
                            text = Formatters.formatCOP(summary.expectedCashInDrawer),
                            style = MaterialTheme.typography.titleLarge.tabular(),
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onCountedCashChange(summary.expectedCashInDrawer.toString()) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar efectivo esperado (${Formatters.formatCOP(summary.expectedCashInDrawer)})")
                }

                OutlinedTextField(
                    value = countedCashInput,
                    onValueChange = onCountedCashChange,
                    label = { Text("Efectivo Contado Físicamente") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_counted_cash_input")
                )

                if (diff != null) {
                    val (badgeLabel, badgeBg, badgeText) = when {
                        diff == 0L -> Triple("Cuadrado (${Formatters.formatCOP(0)})", StockOkBg, StockOkText)
                        diff < 0L -> Triple("Faltante (${Formatters.formatSignedCOP(diff)})", CoralBadgeBg, CoralBadgeText)
                        else -> Triple("Sobrante (${Formatters.formatSignedCOP(diff)})", AmberBadgeBg, AmberBadgeText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Diferencia:", style = MaterialTheme.typography.labelLarge, color = SlateTechnical)
                        StatusPill(text = badgeLabel, backgroundColor = badgeBg, textColor = badgeText)
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = onNotesChange,
                    label = { Text("Observaciones de cierre (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onConfirmClose,
                        enabled = parsedCounted != null,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("confirm_close_cash_dialog_button")
                    ) {
                        Text("Confirmar Cierre")
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownPill(label: String, amount: Long, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, BorderBotanical),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SlateTechnical, maxLines = 1)
            Text(
                text = Formatters.formatCOP(amount),
                style = MaterialTheme.typography.titleSmall.tabular(),
                color = CharcoalText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FinanceLineRow(label: String, amount: Long, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SlateTechnical, modifier = Modifier.weight(1f))
        Text(
            text = Formatters.formatCOP(amount),
            style = MaterialTheme.typography.titleSmall.tabular(),
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConsolidatedRowView(row: ConsolidatedProductRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.productName,
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalText,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.9f)
        )
        Text(
            text = row.unitsSold.toString(),
            style = MaterialTheme.typography.bodySmall.tabular(),
            color = CharcoalText,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
        Text(
            text = Formatters.formatCOP(row.totalCost),
            style = MaterialTheme.typography.bodySmall.tabular(),
            color = SlateTechnical,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = Formatters.formatCOP(row.totalSales),
            style = MaterialTheme.typography.bodySmall.tabular(),
            color = CharcoalText,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = Formatters.formatCOP(row.profitGenerated),
            style = MaterialTheme.typography.bodySmall.tabular(),
            color = StockOkText,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
