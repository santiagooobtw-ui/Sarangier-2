package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.ExpenseType
import com.example.data.model.MovementItem
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.PosUiState
import com.example.ui.components.BotanicalCard
import com.example.ui.components.ProductFormDialog
import com.example.ui.components.QuickExpenseDialog
import com.example.ui.components.QuickQuantityStepper
import com.example.ui.components.RecentMovementsDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.categoryAccentBg
import com.example.ui.components.categoryIcon
import com.example.ui.components.stockStatusColors
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.BorderInput
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(
    state: PosUiState,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onAddProduct: (Product) -> Unit,
    onDecrementCart: (String) -> Unit,
    onSetCartQuantity: (String, Int) -> Unit,
    onRemoveCartItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    onCashReceivedChange: (String) -> Unit,
    onQuickCash: (Long) -> Unit,
    onConfirmSale: () -> Unit,
    onRecordExpense: (ExpenseType, String, String, Long, PaymentMethod, String?) -> Unit,
    onAnnulMovement: (MovementItem) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onEditBase: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showMovementsDialog by remember { mutableStateOf(false) }
    var showMobileCartSheet by remember { mutableStateOf(false) }
    var showNewProductDialog by remember { mutableStateOf(false) }
    var showConfirmSaleDialog by remember { mutableStateOf(false) }

    val triggerConfirmSale: () -> Unit = {
        if (state.settings.confirmBeforeSale) {
            showConfirmSaleDialog = true
        } else {
            onConfirmSale()
        }
    }

    val cartQtyMap = remember(state.cartItems) {
        state.cartItems.associate { it.product.id to it.quantity }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasSandLinen)
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                CatalogPane(
                    state = state,
                    cartQtyMap = cartQtyMap,
                    onSearchChange = onSearchChange,
                    onSearchSubmit = onSearchSubmit,
                    onSelectCategory = onSelectCategory,
                    onAddProduct = onAddProduct,
                    onOpenExpense = { showExpenseDialog = true },
                    onOpenMovements = { showMovementsDialog = true },
                    onAddFirstProduct = { showNewProductDialog = true },
                    onEditBase = onEditBase,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxHeight()
                )
                Surface(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight(),
                    color = CardWhite,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, BorderBotanical)
                ) {
                    CartPanelContent(
                        state = state,
                        onAddProduct = onAddProduct,
                        onDecrementCart = onDecrementCart,
                        onSetCartQuantity = onSetCartQuantity,
                        onRemoveCartItem = onRemoveCartItem,
                        onClearCart = onClearCart,
                        onSelectPaymentMethod = onSelectPaymentMethod,
                        onCashReceivedChange = onCashReceivedChange,
                        onQuickCash = onQuickCash,
                        onConfirmSale = triggerConfirmSale
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                CatalogPane(
                    state = state,
                    cartQtyMap = cartQtyMap,
                    onSearchChange = onSearchChange,
                    onSearchSubmit = onSearchSubmit,
                    onSelectCategory = onSelectCategory,
                    onAddProduct = onAddProduct,
                    onOpenExpense = { showExpenseDialog = true },
                    onOpenMovements = { showMovementsDialog = true },
                    onAddFirstProduct = { showNewProductDialog = true },
                    onEditBase = onEditBase,
                    onOpenSettings = onOpenSettings,
                    bottomExtraPadding = 88.dp,
                    modifier = Modifier.fillMaxSize()
                )

                // Sticky Bottom Tender Bar
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
                                text = if (state.cartItems.isEmpty()) "Carrito vacío" else "${state.cartUnitsCount} uds. en ticket",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTechnical
                            )
                            Text(
                                text = Formatters.formatCOP(state.cartTotal),
                                style = MaterialTheme.typography.headlineSmall.tabular(),
                                color = EmeraldDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showMobileCartSheet = true },
                            enabled = state.cartItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("open_cart_sheet_button")
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (state.cartItems.isEmpty()) "Cobrar" else "Cobrar (${state.cartItems.size})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            if (showMobileCartSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showMobileCartSheet = false },
                    sheetState = sheetState,
                    containerColor = CardWhite
                ) {
                    CartPanelContent(
                        state = state,
                        onAddProduct = onAddProduct,
                        onDecrementCart = onDecrementCart,
                        onSetCartQuantity = onSetCartQuantity,
                        onRemoveCartItem = onRemoveCartItem,
                        onClearCart = onClearCart,
                        onSelectPaymentMethod = onSelectPaymentMethod,
                        onCashReceivedChange = onCashReceivedChange,
                        onQuickCash = onQuickCash,
                        onConfirmSale = {
                            if (state.settings.confirmBeforeSale) {
                                showConfirmSaleDialog = true
                            } else {
                                onConfirmSale()
                                showMobileCartSheet = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 380.dp, max = 620.dp)
                            .navigationBarsPadding()
                            .imePadding()
                    )
                }
            }
        }
    }

    if (showConfirmSaleDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSaleDialog = false },
            title = { Text("Confirmar Venta") },
            text = {
                val baseMsg = "Total: ${Formatters.formatCOP(state.cartTotal)}\n" +
                    "Método: ${state.paymentMethod.label}"
                val changeMsg = if (state.paymentMethod == PaymentMethod.EFECTIVO) {
                    "\nRecibido: ${Formatters.formatCOP(state.cashReceivedAmount)}\nCambio: ${Formatters.formatCOP(state.cashChange.coerceAtLeast(0L))}"
                } else ""
                Text(baseMsg + changeMsg)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSaleDialog = false
                        showMobileCartSheet = false
                        onConfirmSale()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep)
                ) {
                    Text("Sí, Registrar Venta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSaleDialog = false }) {
                    Text("Revisar")
                }
            }
        )
    }

    if (showExpenseDialog) {
        QuickExpenseDialog(
            suppliers = state.suppliers,
            onDismiss = { showExpenseDialog = false },
            onSubmit = onRecordExpense
        )
    }

    if (showMovementsDialog) {
        RecentMovementsDialog(
            movements = state.todayMovements,
            isDayClosed = state.isTodayClosed,
            onAnnulMovement = onAnnulMovement,
            onDismiss = { showMovementsDialog = false }
        )
    }

    if (showNewProductDialog) {
        ProductFormDialog(
            initialProduct = null,
            suppliers = state.suppliers,
            defaultMinStock = state.settings.defaultMinStock,
            onDismiss = { showNewProductDialog = false },
            onSave = onSaveProduct
        )
    }
}

@Composable
private fun CatalogPane(
    state: PosUiState,
    cartQtyMap: Map<String, Int>,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onAddProduct: (Product) -> Unit,
    onOpenExpense: () -> Unit,
    onOpenMovements: () -> Unit,
    onAddFirstProduct: () -> Unit,
    onEditBase: () -> Unit,
    onOpenSettings: () -> Unit,
    bottomExtraPadding: androidx.compose.ui.unit.Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val filtered = state.filteredPosProducts

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 158.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 12.dp,
            bottom = bottomExtraPadding
        ),
        modifier = modifier.fillMaxSize()
    ) {
        if (state.isTodayClosed) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralBadgeBg,
                    border = BorderStroke(1.dp, CoralBadgeText.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = CoralBadgeText)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "La caja de hoy ya fue cerrada. No se permiten nuevas ventas ni egresos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralBadgeText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Action Strip: Base + Configurar + Egreso Rápido + Movimientos
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onEditBase,
                        enabled = !state.isTodayClosed,
                        shape = RoundedCornerShape(10.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, BorderBotanical),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("caja_edit_base_button")
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
                                    text = "Base Inicial de Caja",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTechnical
                                )
                                Text(
                                    text = Formatters.formatCOP(state.todaySession.baseInicial),
                                    style = MaterialTheme.typography.titleMedium.tabular(),
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!state.isTodayClosed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Modificar base",
                                        tint = EmeraldDeep,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Modificar",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldDeep
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(10.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, BorderBotanical),
                        modifier = Modifier.testTag("caja_open_settings_button")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Configuración",
                                tint = EmeraldDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Configurar",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldDeep
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenExpense,
                        enabled = !state.isTodayClosed,
                        border = BorderStroke(1.2.dp, EmeraldMedium),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardWhite,
                            contentColor = EmeraldDeep
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_expense_button")
                    ) {
                        Icon(Icons.Filled.MoneyOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Egreso Rápido", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onOpenMovements,
                        border = BorderStroke(1.2.dp, BorderInput),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardWhite,
                            contentColor = CharcoalText
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("recent_movements_button")
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Movimientos (${state.todayMovements.size})", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Omnibox Search
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = state.posSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar producto o presentación (ej. 7oz, bolsa, icopor)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = SlateTechnical) },
                trailingIcon = {
                    if (state.posSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSearchSubmit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input")
            )
        }

        // Category Pills
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ProductCategory.entries) { category ->
                    val selected = state.posSelectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldDeep,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = CardWhite,
                            labelColor = CharcoalText
                        ),
                        modifier = Modifier.testTag("category_chip_${category.name}")
                    )
                }
            }
        }

        // Cinta rápida de últimos movimientos
        if (state.todayMovements.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.todayMovements.forEach { mov ->
                        val isSale = mov is MovementItem.SaleMovement
                        Surface(
                            onClick = onOpenMovements,
                            shape = CircleShape,
                            color = if (isSale) StockOkBg else CoralBadgeBg,
                            border = BorderStroke(
                                1.dp,
                                (if (isSale) StockOkText else CoralBadgeText).copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = if (isSale) {
                                    "${mov.hora} • +${Formatters.formatCOP(mov.monto)}"
                                } else {
                                    "${mov.hora} • -${Formatters.formatCOP(mov.monto)}"
                                },
                                style = MaterialTheme.typography.labelSmall.tabular(),
                                color = if (isSale) StockOkText else CoralBadgeText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BotanicalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (state.products.isEmpty()) Icons.Filled.Inventory2 else Icons.Filled.Search,
                            contentDescription = null,
                            tint = EmeraldDeep.copy(alpha = 0.7f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (state.products.isEmpty()) {
                                "Tu catálogo está vacío"
                            } else {
                                "No se encontraron productos con ese filtro"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalText
                        )
                        if (state.products.isEmpty()) {
                            Text(
                                text = "Agrega los productos, costos y precios de venta de tu negocio para comenzar a facturar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTechnical
                            )
                            Button(
                                onClick = onAddFirstProduct,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregar Primer Producto")
                            }
                        }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { product ->
                PosProductCard(
                    product = product,
                    inCartQty = cartQtyMap[product.id] ?: 0,
                    isDayClosed = state.isTodayClosed,
                    onClick = { onAddProduct(product) }
                )
            }
        }
    }
}

@Composable
private fun PosProductCard(
    product: Product,
    inCartQty: Int,
    isDayClosed: Boolean,
    onClick: () -> Unit
) {
    val isOut = product.isOutOfStock
    val (statusLabel, badgeBg, badgeText) = stockStatusColors(product.stockActual, product.stockMinimo)

    BotanicalCard(
        onClick = if (!isOut && !isDayClosed) onClick else null,
        containerColor = if (isOut) SurfaceContainerLow else CardWhite,
        borderColor = if (inCartQty > 0) EmeraldMedium else BorderBotanical,
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier
            .alpha(if (isOut) 0.58f else 1f)
            .testTag("pos_product_card_${product.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryAccentBg(product.categoria)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(product.categoria),
                    contentDescription = product.categoria.label,
                    tint = EmeraldDeep,
                    modifier = Modifier.size(20.dp)
                )
            }

            StatusPill(
                text = statusLabel,
                backgroundColor = badgeBg,
                textColor = badgeText
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = product.nombre,
            style = MaterialTheme.typography.titleSmall,
            color = CharcoalText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(38.dp)
        )
        Text(
            text = "${product.presentacion} • ${product.categoria.label}",
            style = MaterialTheme.typography.bodySmall,
            color = SlateTechnical,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Formatters.formatCOP(product.precioVenta),
                style = MaterialTheme.typography.titleMedium.tabular(),
                color = EmeraldDeep,
                fontWeight = FontWeight.Bold
            )

            if (inCartQty > 0) {
                StatusPill(
                    text = "x$inCartQty",
                    backgroundColor = EmeraldDeep,
                    textColor = Color.White
                )
            } else if (!isOut && !isDayClosed) {
                Icon(
                    imageVector = Icons.Filled.AddShoppingCart,
                    contentDescription = "Agregar",
                    tint = EmeraldMedium,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CartPanelContent(
    state: PosUiState,
    onAddProduct: (Product) -> Unit,
    onDecrementCart: (String) -> Unit,
    onSetCartQuantity: (String, Int) -> Unit,
    onRemoveCartItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    onCashReceivedChange: (String) -> Unit,
    onQuickCash: (Long) -> Unit,
    onConfirmSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ticket de Venta", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${state.cartUnitsCount} unidades • ${state.cartItems.size} referencias",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTechnical
                )
            }
            if (state.cartItems.isNotEmpty()) {
                TextButton(onClick = onClearCart) {
                    Text("Limpiar", color = CoralBadgeText)
                }
            }
        }

        HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

        if (state.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = SlateTechnical.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Toca un producto para agregarlo al ticket",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTechnical
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.cartItems, key = { it.product.id }) { item ->
                    CartLineRow(
                        item = item,
                        onDecrement = { onDecrementCart(item.product.id) },
                        onIncrement = { onAddProduct(item.product) },
                        onQuantityEdited = { q -> onSetCartQuantity(item.product.id, q) },
                        onRemove = { onRemoveCartItem(item.product.id) }
                    )
                }
            }
        }

        HorizontalDivider(color = DividerSubtle, modifier = Modifier.padding(vertical = 8.dp))

        // Payment Method Selector
        Text("Método de Pago", style = MaterialTheme.typography.labelMedium, color = SlateTechnical)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PaymentMethod.entries.forEach { method ->
                val selected = state.paymentMethod == method
                FilterChip(
                    selected = selected,
                    onClick = { onSelectPaymentMethod(method) },
                    label = { Text(method.shortLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldDeep,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("payment_method_${method.name}")
                )
            }
        }

        // Live Change Calculator when EFECTIVO
        if (state.paymentMethod == PaymentMethod.EFECTIVO && state.cartItems.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, BorderBotanical),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickOptions = listOf(
                            "Exacto" to state.cartTotal,
                            "$10k" to 10000L,
                            "$20k" to 20000L,
                            "$50k" to 50000L
                        )
                        quickOptions.forEach { (label, amount) ->
                            OutlinedButton(
                                onClick = { onQuickCash(amount) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.cashReceivedInput,
                        onValueChange = onCashReceivedChange,
                        label = { Text("Monto Recibido") },
                        prefix = { Text("$") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_received_input")
                    )

                    val changeOk = state.cashChange >= 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (changeOk) "Cambio a devolver:" else "Falta por cubrir:",
                            style = MaterialTheme.typography.labelLarge,
                            color = SlateTechnical
                        )
                        Text(
                            text = Formatters.formatCOP(kotlin.math.abs(state.cashChange)),
                            style = MaterialTheme.typography.titleLarge.tabular(),
                            color = if (changeOk) StockOkText else CoralBadgeText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total a Cobrar", style = MaterialTheme.typography.titleMedium, color = SlateTechnical)
            Text(
                text = Formatters.formatCOP(state.cartTotal),
                style = MaterialTheme.typography.headlineMedium.tabular(),
                color = EmeraldDeep,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onConfirmSale,
            enabled = state.canConfirmSale,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_sale_button")
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Confirmar Venta", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CartLineRow(
    item: CartItem,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onQuantityEdited: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, BorderBotanical)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        color = CharcoalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${Formatters.formatCOP(item.product.precioVenta)} c/u • Disp: ${item.product.stockActual}",
                        style = MaterialTheme.typography.bodySmall.tabular(),
                        color = SlateTechnical
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Eliminar ítem",
                        tint = CoralBadgeText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickQuantityStepper(
                    quantity = item.quantity,
                    maxQuantity = item.product.stockActual,
                    onDecrement = onDecrement,
                    onIncrement = onIncrement,
                    onQuantityEdited = onQuantityEdited
                )
                Text(
                    text = Formatters.formatCOP(item.subtotal),
                    style = MaterialTheme.typography.titleMedium.tabular(),
                    color = EmeraldDeep,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
