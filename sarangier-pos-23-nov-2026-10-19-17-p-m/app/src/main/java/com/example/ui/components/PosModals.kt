package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppSettings
import com.example.data.model.DailySettlementSummary
import com.example.data.model.ExpenseType
import com.example.data.model.MovementItem
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.Supplier
import com.example.ui.RestockLineInput
import com.example.ui.RestockPrefill
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
import com.example.util.ReceiptExporter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickExpenseDialog(
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSubmit: (
        tipo: ExpenseType,
        categoria: String,
        concepto: String,
        monto: Long,
        metodoPago: PaymentMethod,
        proveedorId: String?
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(ExpenseType.GASTO_OPERATIVO) }
    val operatingCategories = listOf("Domicilios", "Bolsas internas", "Almuerzos", "Servicios", "Varios")
    var selectedCategory by remember { mutableStateOf(operatingCategories.first()) }
    var concepto by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }
    var selectedSupplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    val parsedAmount = Formatters.parseCOPInput(montoText) ?: 0L
    val isValid = concepto.isNotBlank() && parsedAmount > 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Registrar Egreso Rápido", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Text("Tipo de Salida", style = MaterialTheme.typography.labelLarge, color = SlateTechnical)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ExpenseType.entries.forEach { type ->
                        val active = selectedType == type
                        FilterChip(
                            selected = active,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    text = if (type == ExpenseType.GASTO_OPERATIVO) "Gasto Operativo" else "Pago Proveedor",
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDeep,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == ExpenseType.GASTO_OPERATIVO) AmberBadgeBg else StockOkBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedType == ExpenseType.GASTO_OPERATIVO) {
                            "Resta del efectivo en caja y descuenta de la utilidad neta del día."
                        } else {
                            "Resta de caja, pero NO castiga la utilidad operativa porque entra como inventario."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedType == ExpenseType.GASTO_OPERATIVO) AmberBadgeText else StockOkText,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (selectedType == ExpenseType.GASTO_OPERATIVO) {
                    Text("Categoría", style = MaterialTheme.typography.labelLarge, color = SlateTechnical)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        operatingCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                } else if (suppliers.isNotEmpty()) {
                    val selectedSupplier = suppliers.find { it.id == selectedSupplierId } ?: suppliers.first()
                    ExposedDropdownMenuBox(
                        expanded = supplierDropdownExpanded,
                        onExpandedChange = { supplierDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSupplier.empresa,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Proveedor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(supplierDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownExpanded,
                            onDismissRequest = { supplierDropdownExpanded = false }
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text(sup.empresa) },
                                    onClick = {
                                        selectedSupplierId = sup.id
                                        supplierDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto / Detalle") },
                    placeholder = {
                        Text(
                            if (selectedType == ExpenseType.GASTO_OPERATIVO) "Ej. Domicilio motorizado centro"
                            else "Ej. Pago factura reposición vasos"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_concept_input")
                )

                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monto (COP)") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Text("Método de Pago", style = MaterialTheme.typography.labelLarge, color = SlateTechnical)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method.shortLabel, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

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
                        onClick = {
                            onSubmit(
                                selectedType,
                                if (selectedType == ExpenseType.GASTO_OPERATIVO) selectedCategory else "Compra de Mercancía",
                                concepto,
                                parsedAmount,
                                paymentMethod,
                                selectedSupplierId
                            )
                            onDismiss()
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("expense_submit_button")
                    ) {
                        Text("Registrar Salida")
                    }
                }
            }
        }
    }
}

@Composable
fun RecentMovementsDialog(
    movements: List<MovementItem>,
    isDayClosed: Boolean,
    onAnnulMovement: (MovementItem) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingAnnul by remember { mutableStateOf<MovementItem?>(null) }

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
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Últimos 5 Movimientos (Hoy)", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Ventas y egresos ordenados por hora",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTechnical
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DividerSubtle)

                if (movements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay movimientos registrados hoy.", color = SlateTechnical)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        movements.forEach { mov ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, BorderBotanical)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            when (mov) {
                                                is MovementItem.SaleMovement -> {
                                                    StatusPill("VENTA • ${mov.sale.hora}", StockOkBg, StockOkText)
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        mov.sale.metodoPago.shortLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SlateTechnical
                                                    )
                                                }
                                                is MovementItem.ExpenseMovement -> {
                                                    StatusPill("EGRESO • ${mov.expense.hora}", CoralBadgeBg, CoralBadgeText)
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        mov.expense.metodoPago.shortLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SlateTechnical
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        val detail = when (mov) {
                                            is MovementItem.SaleMovement ->
                                                mov.sale.items.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
                                            is MovementItem.ExpenseMovement ->
                                                "${mov.expense.concepto} (${mov.expense.categoria})"
                                        }
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CharcoalText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        val amountText = when (mov) {
                                            is MovementItem.SaleMovement -> "+${Formatters.formatCOP(mov.monto)}"
                                            is MovementItem.ExpenseMovement -> "-${Formatters.formatCOP(mov.monto)}"
                                        }
                                        Text(
                                            text = amountText,
                                            style = MaterialTheme.typography.titleMedium.tabular(),
                                            color = if (mov is MovementItem.SaleMovement) StockOkText else CoralBadgeText,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!isDayClosed) {
                                            TextButton(
                                                onClick = { pendingAnnul = mov },
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text("Anular", color = CoralBadgeText)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingAnnul?.let { mov ->
        AlertDialog(
            onDismissRequest = { pendingAnnul = null },
            title = { Text("Confirmar Anulación") },
            text = {
                Text(
                    if (mov is MovementItem.SaleMovement) {
                        "¿Anular esta venta por ${Formatters.formatCOP(mov.monto)}? Se devolverán todas las unidades vendidas al inventario."
                    } else {
                        "¿Anular este egreso por ${Formatters.formatCOP(mov.monto)}?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAnnulMovement(mov)
                        pendingAnnul = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralBadgeText)
                ) {
                    Text("Sí, Anular")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAnnul = null }) { Text("Cancelar") }
            }
        )
    }
}

private data class EditableRestockLine(
    val uid: String = UUID.randomUUID().toString(),
    val productId: String,
    val quantityText: String,
    val updateCost: Boolean,
    val newCostText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockModal(
    products: List<Product>,
    suppliers: List<Supplier>,
    prefill: RestockPrefill?,
    onDismiss: () -> Unit,
    onSubmit: (
        lines: List<RestockLineInput>,
        registerCashOut: Boolean,
        paymentMethod: PaymentMethod,
        supplierId: String?
    ) -> Unit
) {
    if (products.isEmpty()) return

    val initialProduct = products.find { it.id == prefill?.productId }
        ?: products.find { prefill?.supplierId != null && it.proveedorId == prefill.supplierId }
        ?: products.first()

    val lines = remember {
        mutableStateListOf(
            EditableRestockLine(
                productId = initialProduct.id,
                quantityText = "10",
                updateCost = false,
                newCostText = initialProduct.costoCompra.toString()
            )
        )
    }

    var selectedSupplierId by remember {
        mutableStateOf(prefill?.supplierId ?: initialProduct.proveedorId)
    }
    var supplierDropdownOpen by remember { mutableStateOf(false) }
    var registerCashOut by remember { mutableStateOf(true) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }

    val productMap = products.associateBy { it.id }
    val estimatedTotalPurchase = lines.sumOf { line ->
        val prod = productMap[line.productId]
        val qty = line.quantityText.toIntOrNull() ?: 0
        val unitCost = if (line.updateCost) {
            line.newCostText.toLongOrNull() ?: (prod?.costoCompra ?: 0L)
        } else {
            prod?.costoCompra ?: 0L
        }
        qty * unitCost
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 720.dp),
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
                    Column {
                        Text("Surtir Stock (Entrada)", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Registra mercancía recibida y actualiza costos",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTechnical
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                if (suppliers.isNotEmpty()) {
                    val activeSupplier = suppliers.find { it.id == selectedSupplierId } ?: suppliers.first()
                    ExposedDropdownMenuBox(
                        expanded = supplierDropdownOpen,
                        onExpandedChange = { supplierDropdownOpen = it }
                    ) {
                        OutlinedTextField(
                            value = activeSupplier.empresa,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Proveedor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(supplierDropdownOpen) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownOpen,
                            onDismissRequest = { supplierDropdownOpen = false }
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text(sup.empresa) },
                                    onClick = {
                                        selectedSupplierId = sup.id
                                        supplierDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                lines.forEachIndexed { idx, line ->
                    var prodMenuOpen by remember(line.uid) { mutableStateOf(false) }
                    val prod = productMap[line.productId] ?: products.first()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, BorderBotanical)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Ítem #${idx + 1} • Stock actual: ${prod.stockActual}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldDeep
                                )
                                if (lines.size > 1) {
                                    IconButton(
                                        onClick = { lines.removeAt(idx) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "Quitar ítem",
                                            tint = CoralBadgeText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = prodMenuOpen,
                                onExpandedChange = { prodMenuOpen = it }
                            ) {
                                OutlinedTextField(
                                    value = "${prod.nombre} (${prod.presentacion})",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Producto") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(prodMenuOpen) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = prodMenuOpen,
                                    onDismissRequest = { prodMenuOpen = false }
                                ) {
                                    products.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text("${option.nombre} (Stock: ${option.stockActual})")
                                            },
                                            onClick = {
                                                lines[idx] = line.copy(
                                                    productId = option.id,
                                                    newCostText = option.costoCompra.toString()
                                                )
                                                prodMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = line.quantityText,
                                    onValueChange = { raw ->
                                        lines[idx] = line.copy(quantityText = raw.filter { it.isDigit() })
                                    },
                                    label = { Text("Cantidad Entra") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                if (line.updateCost) {
                                    OutlinedTextField(
                                        value = line.newCostText,
                                        onValueChange = { raw ->
                                            lines[idx] = line.copy(newCostText = raw.filter { it.isDigit() })
                                        },
                                        label = { Text("Nuevo Costo") },
                                        prefix = { Text("$") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Actualizar costo compra (${Formatters.formatCOP(prod.costoCompra)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateTechnical
                                )
                                Switch(
                                    checked = line.updateCost,
                                    onCheckedChange = { lines[idx] = line.copy(updateCost = it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = EmeraldDeep)
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        val nextProd = products.firstOrNull { p -> lines.none { it.productId == p.id } } ?: products.first()
                        lines.add(
                            EditableRestockLine(
                                productId = nextProd.id,
                                quantityText = "10",
                                updateCost = false,
                                newCostText = nextProd.costoCompra.toString()
                            )
                        )
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar otro producto al ingreso")
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (registerCashOut) StockOkBg else SurfaceContainerLow,
                    border = BorderStroke(1.dp, BorderBotanical)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Registrar salida de dinero en Caja por esta compra",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CharcoalText
                                )
                                Text(
                                    "Total compra: ${Formatters.formatCOP(estimatedTotalPurchase)} (Pago a Proveedor)",
                                    style = MaterialTheme.typography.bodySmall.tabular(),
                                    color = SlateTechnical
                                )
                            }
                            Switch(
                                checked = registerCashOut,
                                onCheckedChange = { registerCashOut = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = EmeraldDeep)
                            )
                        }

                        if (registerCashOut) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                PaymentMethod.entries.forEach { method ->
                                    FilterChip(
                                        selected = paymentMethod == method,
                                        onClick = { paymentMethod = method },
                                        label = { Text(method.shortLabel, maxLines = 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

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
                        onClick = {
                            val inputs = lines.mapNotNull { l ->
                                val q = l.quantityText.toIntOrNull() ?: 0
                                if (q > 0) {
                                    RestockLineInput(
                                        productId = l.productId,
                                        quantity = q,
                                        updateCost = l.updateCost,
                                        newCost = l.newCostText.toLongOrNull() ?: 0L
                                    )
                                } else null
                            }
                            onSubmit(inputs, registerCashOut, paymentMethod, selectedSupplierId)
                        },
                        enabled = lines.any { (it.quantityText.toIntOrNull() ?: 0) > 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("restock_confirm_button")
                    ) {
                        Text("Confirmar Entrada")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialProduct: Product?,
    suppliers: List<Supplier>,
    defaultMinStock: Int = 5,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(initialProduct?.nombre ?: "") }
    var presentacion by remember { mutableStateOf(initialProduct?.presentacion ?: "") }
    var categoria by remember { mutableStateOf(initialProduct?.categoria ?: ProductCategory.VASOS) }
    var stockActual by remember { mutableStateOf((initialProduct?.stockActual ?: 10).toString()) }
    var stockMinimo by remember { mutableStateOf((initialProduct?.stockMinimo ?: defaultMinStock).toString()) }
    var costoCompra by remember { mutableStateOf((initialProduct?.costoCompra ?: 0L).let { if (it == 0L) "" else it.toString() }) }
    var precioVenta by remember { mutableStateOf((initialProduct?.precioVenta ?: 0L).let { if (it == 0L) "" else it.toString() }) }
    var supplierId by remember { mutableStateOf(initialProduct?.proveedorId ?: suppliers.firstOrNull()?.id.orEmpty()) }

    var catExpanded by remember { mutableStateOf(false) }
    var supExpanded by remember { mutableStateOf(false) }

    val parsedCost = costoCompra.toLongOrNull() ?: 0L
    val parsedPrice = precioVenta.toLongOrNull() ?: 0L
    val marginAmount = parsedPrice - parsedCost
    val marginPct = if (parsedPrice > 0) (marginAmount.toDouble() / parsedPrice) * 100.0 else 0.0
    val isValid = nombre.isNotBlank() && parsedPrice > 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initialProduct == null) "Nuevo Producto" else "Editar Producto",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = presentacion,
                    onValueChange = { presentacion = it },
                    label = { Text("Presentación (opcional, ej. Paq x50)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = categoria.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        ProductCategory.productCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = {
                                    categoria = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                if (suppliers.isNotEmpty()) {
                    val currentSup = suppliers.find { it.id == supplierId } ?: suppliers.first()
                    ExposedDropdownMenuBox(expanded = supExpanded, onExpandedChange = { supExpanded = it }) {
                        OutlinedTextField(
                            value = currentSup.empresa,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Proveedor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(supExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = supExpanded, onDismissRequest = { supExpanded = false }) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text(sup.empresa) },
                                    onClick = {
                                        supplierId = sup.id
                                        supExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stockActual,
                        onValueChange = { stockActual = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockMinimo,
                        onValueChange = { stockMinimo = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stock Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = costoCompra,
                        onValueChange = { costoCompra = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Costo Compra") },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = precioVenta,
                        onValueChange = { precioVenta = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Precio Venta") },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (marginAmount >= 0) StockOkBg else CoralBadgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Margen unitario: ${Formatters.formatCOP(marginAmount)} (${String.format("%.1f", marginPct)}%)",
                        style = MaterialTheme.typography.titleSmall.tabular(),
                        color = if (marginAmount >= 0) StockOkText else CoralBadgeText,
                        modifier = Modifier.padding(10.dp)
                    )
                }

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
                        onClick = {
                            onSave(
                                Product(
                                    id = initialProduct?.id ?: "prod-${UUID.randomUUID().toString().take(8)}",
                                    nombre = nombre.trim(),
                                    presentacion = presentacion.trim().ifBlank { "Unidad" },
                                    categoria = categoria,
                                    stockActual = stockActual.toIntOrNull() ?: 0,
                                    stockMinimo = stockMinimo.toIntOrNull() ?: defaultMinStock,
                                    costoCompra = parsedCost,
                                    precioVenta = parsedPrice,
                                    proveedorId = supplierId
                                )
                            )
                            onDismiss()
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Guardar")
                    }
                }

                if (initialProduct != null && onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete(initialProduct.id)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = CoralBadgeText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar Producto", color = CoralBadgeText)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierFormDialog(
    initialSupplier: Supplier?,
    onDismiss: () -> Unit,
    onSave: (Supplier) -> Unit
) {
    var empresa by remember { mutableStateOf(initialSupplier?.empresa ?: "") }
    var contacto by remember { mutableStateOf(initialSupplier?.contacto ?: "") }
    var telefono by remember { mutableStateOf(initialSupplier?.telefono ?: "") }
    var diasEntrega by remember { mutableStateOf(initialSupplier?.diasEntrega ?: "") }
    var notas by remember { mutableStateOf(initialSupplier?.notas ?: "") }

    val isValid = empresa.isNotBlank() && contacto.isNotBlank() && telefono.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 660.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initialSupplier == null) "Nuevo Proveedor" else "Editar Proveedor",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                OutlinedTextField(
                    value = empresa,
                    onValueChange = { empresa = it },
                    label = { Text("Empresa / Razón Social") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Persona de Contacto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { ch -> ch.isDigit() || ch == '+' } },
                    label = { Text("Teléfono / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diasEntrega,
                    onValueChange = { diasEntrega = it },
                    label = { Text("Días de Entrega") },
                    placeholder = { Text("Ej. Lunes y Jueves") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas comerciales") },
                    minLines = 2,
                    maxLines = 4,
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
                        onClick = {
                            onSave(
                                Supplier(
                                    id = initialSupplier?.id ?: "sup-${UUID.randomUUID().toString().take(8)}",
                                    empresa = empresa.trim(),
                                    contacto = contacto.trim(),
                                    telefono = telefono.trim(),
                                    diasEntrega = diasEntrega.trim().ifBlank { "Por confirmar" },
                                    notas = notas.trim()
                                )
                            )
                            onDismiss()
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun ClosureReceiptDialog(
    summary: DailySettlementSummary,
    settings: AppSettings = AppSettings(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val counted = summary.session.efectivoContado ?: 0L
    val diff = counted - summary.expectedCashInDrawer

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            color = CardWhite
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StockOkText,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Caja cerrada correctamente", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Jornada del ${Formatters.formatDateLabel(summary.date)}" +
                                    (summary.session.horaCierre?.let { " • $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTechnical
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, BorderBotanical),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Resumen del día",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldDeep
                            )
                            ClosureSummaryRow("Ingresos brutos (${summary.salesCount} ventas)", Formatters.formatCOP(summary.grossIncome))
                            ClosureSummaryRow("  • Efectivo", Formatters.formatCOP(summary.cashSales), SlateTechnical)
                            ClosureSummaryRow("  • Transferencia / Nequi", Formatters.formatCOP(summary.transferSales), SlateTechnical)
                            ClosureSummaryRow("  • Tarjeta", Formatters.formatCOP(summary.cardSales), SlateTechnical)
                            ClosureSummaryRow("Costo mercancía (CMV)", Formatters.formatCOP(summary.cmv), SlateTechnical)
                            ClosureSummaryRow("Egresos operativos", Formatters.formatCOP(summary.operatingExpenses), CoralBadgeText)
                            ClosureSummaryRow("Pagos a proveedores", Formatters.formatCOP(summary.supplierPayments), AmberBadgeText)
                            HorizontalDivider(color = DividerSubtle)
                            ClosureSummaryRow(
                                label = "Utilidad neta estimada",
                                value = Formatters.formatCOP(summary.netProfit),
                                valueColor = if (summary.netProfit >= 0) StockOkText else CoralBadgeText,
                                bold = true
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, BorderBotanical),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Arqueo de efectivo",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldDeep
                            )
                            ClosureSummaryRow("Base inicial", Formatters.formatCOP(summary.session.baseInicial))
                            ClosureSummaryRow("Efectivo esperado en cajón", Formatters.formatCOP(summary.expectedCashInDrawer))
                            ClosureSummaryRow("Efectivo contado físicamente", Formatters.formatCOP(counted), bold = true)
                            val (diffLabel, diffBg, diffColor) = when {
                                diff == 0L -> Triple("Cuadrado (${Formatters.formatCOP(0)})", StockOkBg, StockOkText)
                                diff < 0L -> Triple("Faltante (${Formatters.formatSignedCOP(diff)})", CoralBadgeBg, CoralBadgeText)
                                else -> Triple("Sobrante (${Formatters.formatSignedCOP(diff)})", AmberBadgeBg, AmberBadgeText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Diferencia", style = MaterialTheme.typography.bodyMedium, color = SlateTechnical)
                                StatusPill(text = diffLabel, backgroundColor = diffBg, textColor = diffColor)
                            }
                            if (!summary.session.observacionesCierre.isNullOrBlank()) {
                                HorizontalDivider(color = DividerSubtle)
                                Text(
                                    text = "Observaciones: ${summary.session.observacionesCierre}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateTechnical
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { ReceiptExporter.shareClosureSummary(context, summary, asCsv = true, settings = settings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("CSV", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { ReceiptExporter.shareClosureSummary(context, summary, asCsv = false, settings = settings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Compartir", maxLines = 1)
                    }
                    Button(
                        onClick = { ReceiptExporter.printClosureReceipt(context, summary, settings = settings) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Imprimir", maxLines = 1)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Listo")
                }
            }
        }
    }
}

@Composable
private fun ClosureSummaryRow(
    label: String,
    value: String,
    valueColor: Color = CharcoalText,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (bold) CharcoalText else SlateTechnical,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = (if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium).tabular(),
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun BaseInicialDialog(
    currentBase: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var baseText by remember(currentBase) {
        mutableStateOf(if (currentBase == 0L) "" else currentBase.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Base Inicial de Caja") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Define el dinero inicial con el que abres el cajón hoy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTechnical
                )
                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monto inicial (COP)") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("base_inicial_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0L to "$0", 50000L to "$50k", 100000L to "$100k", 150000L to "$150k").forEach { (amount, label) ->
                        OutlinedButton(
                            onClick = { baseText = amount.toString() },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(baseText.toLongOrNull() ?: 0L)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                modifier = Modifier.testTag("save_base_inicial_button")
            ) {
                Text("Guardar Base")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

