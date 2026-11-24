package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.Supplier
import com.example.ui.PosUiState
import com.example.ui.components.BotanicalCard
import com.example.ui.components.StatusPill
import com.example.ui.components.SupplierFormDialog
import com.example.ui.components.stockStatusColors
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.CanvasSandLinen
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.SageLeaf
import com.example.ui.theme.SlateTechnical
import com.example.ui.theme.StockOkBg
import com.example.ui.theme.StockOkText
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.tabular
import com.example.util.Formatters

@Composable
fun ProveedoresScreen(
    state: PosUiState,
    onSelectSupplier: (String) -> Unit,
    onSaveSupplier: (Supplier) -> Unit,
    onOpenRestockForSupplier: (supplierId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var showNewSupplierDialog by remember { mutableStateOf(false) }

    val selectedSupplier = state.suppliers.find { it.id == state.selectedSupplierId }
        ?: state.suppliers.firstOrNull()
    val suppliedProducts = remember(selectedSupplier, state.products) {
        if (selectedSupplier == null) emptyList()
        else state.products.filter { it.proveedorId == selectedSupplier.id }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasSandLinen)
    ) {
        val isWide = maxWidth >= 700.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Master list left
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proveedores (${state.suppliers.size})", style = MaterialTheme.typography.titleLarge)
                        Button(
                            onClick = { showNewSupplierDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nuevo")
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.suppliers, key = { it.id }) { supplier ->
                            val count = state.products.count { it.proveedorId == supplier.id }
                            SupplierMasterCard(
                                supplier = supplier,
                                productCount = count,
                                selected = supplier.id == selectedSupplier?.id,
                                onClick = { onSelectSupplier(supplier.id) }
                            )
                        }
                    }
                }

                // Detail right
                Box(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                ) {
                    if (selectedSupplier != null) {
                        SupplierDetailPane(
                            supplier = selectedSupplier,
                            products = suppliedProducts,
                            onEdit = { editingSupplier = selectedSupplier },
                            onRegisterRestock = { onOpenRestockForSupplier(selectedSupplier.id) },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        EmptySuppliersCard(onAdd = { showNewSupplierDialog = true })
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Directorio (${state.suppliers.size})", style = MaterialTheme.typography.titleLarge)
                    OutlinedButton(
                        onClick = { showNewSupplierDialog = true },
                        border = BorderStroke(1.2.dp, EmeraldMedium),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = EmeraldDeep),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nuevo Proveedor")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.suppliers, key = { it.id }) { supplier ->
                        val count = state.products.count { it.proveedorId == supplier.id }
                        SupplierMasterCard(
                            supplier = supplier,
                            productCount = count,
                            selected = supplier.id == selectedSupplier?.id,
                            onClick = { onSelectSupplier(supplier.id) },
                            modifier = Modifier.width(240.dp)
                        )
                    }
                }

                if (selectedSupplier != null) {
                    SupplierDetailPane(
                        supplier = selectedSupplier,
                        products = suppliedProducts,
                        onEdit = { editingSupplier = selectedSupplier },
                        onRegisterRestock = { onOpenRestockForSupplier(selectedSupplier.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EmptySuppliersCard(onAdd = { showNewSupplierDialog = true })
                }
            }
        }
    }

    if (showNewSupplierDialog) {
        SupplierFormDialog(
            initialSupplier = null,
            onDismiss = { showNewSupplierDialog = false },
            onSave = onSaveSupplier
        )
    }

    editingSupplier?.let { sup ->
        SupplierFormDialog(
            initialSupplier = sup,
            onDismiss = { editingSupplier = null },
            onSave = onSaveSupplier
        )
    }
}

@Composable
private fun EmptySuppliersCard(onAdd: () -> Unit) {
    BotanicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.Business,
                contentDescription = null,
                tint = EmeraldDeep.copy(alpha = 0.7f),
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "Sin proveedores registrados",
                style = MaterialTheme.typography.titleMedium,
                color = CharcoalText
            )
            Text(
                text = "Agrega tus proveedores para asociar productos y registrar entradas.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateTechnical
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Agregar Proveedor")
            }
        }
    }
}

@Composable
private fun SupplierMasterCard(
    supplier: Supplier,
    productCount: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BotanicalCard(
        onClick = onClick,
        containerColor = if (selected) StockOkBg else CardWhite,
        borderColor = if (selected) EmeraldDeep else BorderBotanical,
        contentPadding = PaddingValues(12.dp),
        modifier = modifier.testTag("supplier_card_${supplier.id}")
    ) {
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) EmeraldDeep else SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = null,
                        tint = if (selected) Color.White else EmeraldDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = supplier.empresa,
                        style = MaterialTheme.typography.titleSmall,
                        color = CharcoalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = supplier.contacto,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTechnical,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(
                text = "$productCount refs",
                backgroundColor = if (selected) EmeraldDeep else SurfaceContainerLow,
                textColor = if (selected) Color.White else SlateTechnical
            )
        }
    }
}

@Composable
private fun SupplierDetailPane(
    supplier: Supplier,
    products: List<Product>,
    onEdit: () -> Unit,
    onRegisterRestock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BotanicalCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(supplier.empresa, style = MaterialTheme.typography.headlineSmall, color = CharcoalText)
                    Spacer(Modifier.height(4.dp))
                    StatusPill(
                        text = "Entregas: ${supplier.diasEntrega}",
                        backgroundColor = StockOkBg,
                        textColor = StockOkText
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar proveedor", tint = EmeraldDeep)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerSubtle)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = SlateTechnical, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Contacto: ${supplier.contacto}", style = MaterialTheme.typography.bodyMedium, color = CharcoalText)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = SlateTechnical, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Teléfono: ${supplier.telefono}", style = MaterialTheme.typography.bodyMedium.tabular(), color = CharcoalText)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = SlateTechnical, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Días de entrega: ${supplier.diasEntrega}", style = MaterialTheme.typography.bodyMedium, color = CharcoalText)
            }

            if (supplier.notas.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = supplier.notas,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTechnical,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val digits = supplier.telefono.filter { it.isDigit() }
                        val waNumber = if (digits.startsWith("57")) digits else "57$digits"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "WhatsApp o navegador no disponible ($waNumber)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    border = BorderStroke(1.3.dp, SageLeaf),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = StockOkBg,
                        contentColor = EmeraldDeep
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("whatsapp_supplier_button")
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", maxLines = 1)
                }

                Button(
                    onClick = onRegisterRestock,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("supplier_restock_button")
                ) {
                    Icon(Icons.Filled.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Registrar Pedido / Entrada", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        BotanicalCard {
            Text(
                text = "Productos que Suministra (${products.size})",
                style = MaterialTheme.typography.titleMedium,
                color = CharcoalText
            )
            Spacer(Modifier.height(10.dp))

            if (products.isEmpty()) {
                Text(
                    text = "Aún no hay productos asignados a este proveedor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTechnical
                )
            } else {
                products.forEachIndexed { idx, prod ->
                    val (pillText, pillBg, pillColor) = stockStatusColors(prod.stockActual, prod.stockMinimo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.nombre, style = MaterialTheme.typography.titleSmall, color = CharcoalText)
                            Text(
                                text = "${prod.presentacion} • Costo: ${Formatters.formatCOP(prod.costoCompra)}",
                                style = MaterialTheme.typography.bodySmall.tabular(),
                                color = SlateTechnical
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        StatusPill(text = pillText, backgroundColor = pillBg, textColor = pillColor)
                    }
                    if (idx < products.lastIndex) {
                        HorizontalDivider(color = DividerSubtle)
                    }
                }
            }
        }
    }
}
