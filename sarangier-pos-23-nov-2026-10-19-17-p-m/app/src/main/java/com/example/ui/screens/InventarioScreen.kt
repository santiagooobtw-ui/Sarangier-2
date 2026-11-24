package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.Supplier
import com.example.ui.InventoryFilter
import com.example.ui.PosUiState
import com.example.ui.components.BotanicalCard
import com.example.ui.components.ProductFormDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.categoryAccentBg
import com.example.ui.components.categoryIcon
import com.example.ui.components.stockStatusColors
import com.example.ui.theme.AmberBadgeBg
import com.example.ui.theme.AmberBadgeText
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.BorderInput
import com.example.ui.theme.CanvasSandLinen
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralBadgeBg
import com.example.ui.theme.CoralBadgeText
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.SlateTechnical
import com.example.ui.theme.StockOkBg
import com.example.ui.theme.StockOkText
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.tabular
import com.example.util.Formatters

@Composable
fun InventarioScreen(
    state: PosUiState,
    onSearchChange: (String) -> Unit,
    onSelectFilter: (InventoryFilter) -> Unit,
    onOpenRestock: (supplierId: String?, productId: String?) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showCreateProductDialog by remember { mutableStateOf(false) }
    val supplierMap = remember(state.suppliers) { state.suppliers.associateBy { it.id } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasSandLinen)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Reactive KPIs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryKpiCard(
                        title = "Referencias",
                        value = state.totalReferences.toString(),
                        icon = Icons.Filled.Inventory,
                        bgColor = CardWhite,
                        accentColor = EmeraldDeep,
                        selected = state.inventoryFilter == InventoryFilter.ALL,
                        onClick = { onSelectFilter(InventoryFilter.ALL) },
                        modifier = Modifier.weight(1f)
                    )
                    InventoryKpiCard(
                        title = "Valor Costo",
                        value = if (state.settings.showCostsAndMarginsInInventory) {
                            Formatters.formatCOP(state.totalInventoryCostValue)
                        } else {
                            "Oculto"
                        },
                        icon = Icons.Filled.MonetizationOn,
                        bgColor = CardWhite,
                        accentColor = StockOkText,
                        selected = false,
                        onClick = { onSelectFilter(InventoryFilter.ALL) },
                        modifier = Modifier.weight(1.3f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryKpiCard(
                        title = "Stock Bajo",
                        value = state.lowStockCount.toString(),
                        icon = Icons.Filled.Warning,
                        bgColor = if (state.inventoryFilter == InventoryFilter.LOW_STOCK) AmberBadgeBg else CardWhite,
                        accentColor = AmberBadgeText,
                        selected = state.inventoryFilter == InventoryFilter.LOW_STOCK,
                        onClick = { onSelectFilter(InventoryFilter.LOW_STOCK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_low_stock")
                    )
                    InventoryKpiCard(
                        title = "Agotados",
                        value = state.outOfStockCount.toString(),
                        icon = Icons.Filled.LocalShipping,
                        bgColor = if (state.inventoryFilter == InventoryFilter.OUT_OF_STOCK) CoralBadgeBg else CardWhite,
                        accentColor = CoralBadgeText,
                        selected = state.inventoryFilter == InventoryFilter.OUT_OF_STOCK,
                        onClick = { onSelectFilter(InventoryFilter.OUT_OF_STOCK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_out_of_stock")
                    )
                }
            }
        }

        // Action Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.inventorySearchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Filtrar inventario...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SlateTechnical) },
                    trailingIcon = {
                        if (state.inventorySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = EmeraldMedium,
                        unfocusedBorderColor = BorderInput
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenRestock(null, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("surtir_stock_button")
                    ) {
                        Icon(Icons.Filled.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+ Surtir Stock", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showCreateProductDialog = true },
                        border = BorderStroke(1.3.dp, EmeraldMedium),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardWhite,
                            contentColor = EmeraldDeep
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("new_product_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Nuevo Producto", maxLines = 1)
                    }
                }
            }
        }

        if (state.inventoryFilter != InventoryFilter.ALL) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtro activo: ${state.inventoryFilter.label} (${state.filteredInventoryProducts.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldDeep
                    )
                    TextButton(onClick = { onSelectFilter(InventoryFilter.ALL) }) {
                        Text("Ver todos")
                    }
                }
            }
        }

        // Inventory list or empty state
        if (state.filteredInventoryProducts.isEmpty()) {
            item {
                BotanicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Inventory,
                            contentDescription = null,
                            tint = EmeraldDeep.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (state.products.isEmpty()) "Sin productos registrados" else "Sin resultados con ese filtro",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalText
                        )
                        Text(
                            text = if (state.products.isEmpty()) {
                                "Toca 'Nuevo Producto' para registrar tus productos y precios."
                            } else {
                                "Prueba limpiando la búsqueda o el filtro activo."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTechnical
                        )
                    }
                }
            }
        } else {
            items(state.filteredInventoryProducts, key = { it.id }) { product ->
                InventoryProductCard(
                    product = product,
                    supplier = supplierMap[product.proveedorId],
                    showCostsAndMargins = state.settings.showCostsAndMarginsInInventory,
                    onEdit = { editingProduct = product },
                    onRestock = { onOpenRestock(product.proveedorId, product.id) }
                )
            }
        }
    }

    if (showCreateProductDialog) {
        ProductFormDialog(
            initialProduct = null,
            suppliers = state.suppliers,
            defaultMinStock = state.settings.defaultMinStock,
            onDismiss = { showCreateProductDialog = false },
            onSave = onSaveProduct
        )
    }

    editingProduct?.let { prod ->
        ProductFormDialog(
            initialProduct = prod,
            suppliers = state.suppliers,
            defaultMinStock = state.settings.defaultMinStock,
            onDismiss = { editingProduct = null },
            onSave = onSaveProduct,
            onDelete = onDeleteProduct
        )
    }
}

@Composable
private fun InventoryKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    accentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BotanicalCard(
        onClick = onClick,
        containerColor = bgColor,
        borderColor = if (selected) accentColor else BorderBotanical,
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = SlateTechnical,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.tabular(),
            color = accentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InventoryProductCard(
    product: Product,
    supplier: Supplier?,
    showCostsAndMargins: Boolean,
    onEdit: () -> Unit,
    onRestock: () -> Unit
) {
    val (stockText, pillBg, pillText) = stockStatusColors(product.stockActual, product.stockMinimo)

    BotanicalCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryAccentBg(product.categoria)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(product.categoria),
                        contentDescription = null,
                        tint = EmeraldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${product.presentacion} • ${supplier?.empresa ?: "Sin proveedor"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTechnical,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            StatusPill(text = stockText, backgroundColor = pillBg, textColor = pillText)
        }

        Spacer(Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SurfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCostsAndMargins) {
                    MetricCol(label = "COSTO", value = Formatters.formatCOP(product.costoCompra), valueColor = SlateTechnical)
                    MetricCol(label = "VENTA", value = Formatters.formatCOP(product.precioVenta), valueColor = CharcoalText)
                    MetricCol(
                        label = "MARGEN $",
                        value = Formatters.formatCOP(product.marginAmount),
                        valueColor = StockOkText
                    )
                    MetricCol(
                        label = "MARGEN %",
                        value = "${String.format("%.1f", product.marginPercent)}%",
                        valueColor = EmeraldDeep
                    )
                } else {
                    MetricCol(label = "PRECIO VENTA", value = Formatters.formatCOP(product.precioVenta), valueColor = CharcoalText)
                    MetricCol(label = "STOCK ACTUAL", value = "${product.stockActual} uds.", valueColor = EmeraldDeep)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showCostsAndMargins) {
                    "Stock mín: ${product.stockMinimo} • Valor: ${Formatters.formatCOP(product.stockActual.toLong() * product.costoCompra)}"
                } else {
                    "Stock mín: ${product.stockMinimo}"
                },
                style = MaterialTheme.typography.bodySmall.tabular(),
                color = SlateTechnical
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onRestock) {
                    Icon(Icons.Filled.AddBox, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Surtir")
                }
            }
        }
    }
}

@Composable
private fun MetricCol(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SlateTechnical
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.tabular(),
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
