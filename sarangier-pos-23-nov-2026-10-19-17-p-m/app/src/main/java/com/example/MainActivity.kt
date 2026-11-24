package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PosDatabase
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PosRepository
import com.example.ui.NavTab
import com.example.ui.PosViewModel
import com.example.ui.components.BaseInicialDialog
import com.example.ui.components.RestockModal
import com.example.ui.components.SarangierTopBar
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CajaScreen
import com.example.ui.screens.InventarioScreen
import com.example.ui.screens.LiquidacionScreen
import com.example.ui.screens.ProveedoresScreen
import com.example.ui.theme.CanvasSandLinen
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CoralBadgeText
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.PrimaryContainerSoft
import com.example.ui.theme.SarangierTheme
import com.example.ui.theme.SlateTechnical

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = PosDatabase.getInstance(applicationContext)
        val repository = PosRepository(db.posDao())
        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            SarangierTheme {
                val vm: PosViewModel = viewModel(factory = PosViewModel.Factory(repository, settingsRepository))
                SarangierApp(viewModel = vm)
            }
        }
    }
}

@Composable
fun SarangierApp(viewModel: PosViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFactoryResetConfirm by remember { mutableStateOf(false) }
    var showBaseInicialDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showReopenCashConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    if (state.activeTab != NavTab.CAJA) {
        BackHandler {
            viewModel.selectTab(NavTab.CAJA)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasSandLinen)
    ) {
        val useRail = maxWidth >= 840.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                SarangierTopBar(
                    businessName = state.settings.businessName,
                    estimatedCashInDrawer = state.estimatedCashInDrawerToday,
                    cashStatus = state.todaySession.estado,
                    onRequestOpenSettings = { showSettingsDialog = true },
                    onRequestEditBase = { showBaseInicialDialog = true },
                    onRequestReopenCash = { showReopenCashConfirm = true },
                    onRequestFactoryReset = { showFactoryResetConfirm = true }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = CardWhite,
                        tonalElevation = 4.dp
                    ) {
                        NavTab.entries.forEach { tab ->
                            val selected = state.activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldDeep,
                                    selectedTextColor = EmeraldDeep,
                                    indicatorColor = PrimaryContainerSoft,
                                    unselectedIconColor = SlateTechnical,
                                    unselectedTextColor = SlateTechnical
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CardWhite,
                        modifier = Modifier
                            .fillMaxHeight()
                            .navigationBarsPadding()
                    ) {
                        Spacer(Modifier.weight(0.1f))
                        NavTab.entries.forEach { tab ->
                            val selected = state.activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = EmeraldDeep,
                                    selectedTextColor = EmeraldDeep,
                                    indicatorColor = PrimaryContainerSoft,
                                    unselectedIconColor = SlateTechnical,
                                    unselectedTextColor = SlateTechnical
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name}")
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { showSettingsDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Configuración",
                                tint = EmeraldDeep
                            )
                        }
                        IconButton(
                            onClick = { showFactoryResetConfirm = true },
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Reiniciar Todo en Cero",
                                tint = SlateTechnical
                            )
                        }
                    }
                }

                val pagerState = rememberPagerState(
                    initialPage = state.activeTab.ordinal,
                    pageCount = { NavTab.entries.size }
                )
                val currentActiveTab by rememberUpdatedState(state.activeTab)

                LaunchedEffect(state.activeTab) {
                    if (pagerState.settledPage != state.activeTab.ordinal) {
                        pagerState.animateScrollToPage(state.activeTab.ordinal)
                    }
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.settledPage }
                        .distinctUntilChanged()
                        .collect { page ->
                            val swipedTab = NavTab.entries[page]
                            if (swipedTab != currentActiveTab) {
                                viewModel.selectTab(swipedTab)
                            }
                        }
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("main_horizontal_pager")
                ) { page ->
                    when (NavTab.entries[page]) {
                        NavTab.CAJA -> CajaScreen(
                            state = state,
                            onSearchChange = viewModel::updatePosSearch,
                            onSearchSubmit = viewModel::onPosSearchSubmit,
                            onSelectCategory = viewModel::selectPosCategory,
                            onAddProduct = viewModel::addToCart,
                            onDecrementCart = viewModel::decrementCartItem,
                            onSetCartQuantity = viewModel::setCartQuantity,
                            onRemoveCartItem = viewModel::removeFromCart,
                            onClearCart = viewModel::clearCart,
                            onSelectPaymentMethod = viewModel::selectPaymentMethod,
                            onCashReceivedChange = viewModel::updateCashReceivedInput,
                            onQuickCash = viewModel::applyQuickCash,
                            onConfirmSale = viewModel::confirmSale,
                            onRecordExpense = viewModel::recordQuickExpense,
                            onAnnulMovement = viewModel::annulMovement,
                            onSaveProduct = viewModel::saveProduct,
                            onEditBase = { showBaseInicialDialog = true },
                            onOpenSettings = { showSettingsDialog = true }
                        )

                        NavTab.INVENTARIO -> InventarioScreen(
                            state = state,
                            onSearchChange = viewModel::updateInventorySearch,
                            onSelectFilter = viewModel::selectInventoryFilter,
                            onOpenRestock = viewModel::openRestockModal,
                            onSaveProduct = viewModel::saveProduct,
                            onDeleteProduct = viewModel::deleteProduct
                        )

                        NavTab.LIQUIDACION -> LiquidacionScreen(
                            state = state,
                            onSelectDate = viewModel::selectSettlementDate,
                            onCountedCashChange = viewModel::updateCountedCashInput,
                            onNotesChange = viewModel::updateClosureNotesInput,
                            onUpdateBaseInicial = viewModel::updateBaseInicial,
                            onCloseCashDay = viewModel::closeCashDay,
                            onDismissClosureSuccessAlert = viewModel::dismissClosureSuccessAlert,
                            onOpenReceiptModal = viewModel::openClosureReceiptModal,
                            onDismissReceiptModal = viewModel::dismissClosureReceiptModal
                        )

                        NavTab.PROVEEDORES -> ProveedoresScreen(
                            state = state,
                            onSelectSupplier = viewModel::selectSupplier,
                            onSaveSupplier = viewModel::saveSupplier,
                            onOpenRestockForSupplier = { supId -> viewModel.openRestockModal(supId, null) }
                        )
                    }
                }
            }
        }
    }

    if (state.showRestockDialog) {
        RestockModal(
            products = state.products,
            suppliers = state.suppliers,
            prefill = state.restockPrefill,
            onDismiss = viewModel::dismissRestockModal,
            onSubmit = viewModel::submitRestock
        )
    }

    if (showBaseInicialDialog) {
        BaseInicialDialog(
            currentBase = state.todaySession.baseInicial,
            onDismiss = { showBaseInicialDialog = false },
            onSave = { amount -> viewModel.updateBaseInicial(state.todayDate, amount) }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = state.settings,
            todayBaseInicial = state.todaySession.baseInicial,
            isTodayClosed = state.isTodayClosed,
            onDismiss = { showSettingsDialog = false },
            onSaveSettings = viewModel::updateSettings,
            onResetSettingsDefaults = viewModel::resetSettingsToDefaults,
            onOpenEditTodayBase = { showBaseInicialDialog = true },
            onReopenTodayCash = { showReopenCashConfirm = true },
            onRequestFactoryReset = { showFactoryResetConfirm = true }
        )
    }

    if (showReopenCashConfirm) {
        AlertDialog(
            onDismissRequest = { showReopenCashConfirm = false },
            title = { Text("Reabrir Caja de Hoy") },
            text = {
                Text("La jornada de hoy volverá al estado ABIERTA para permitir nuevas ventas, egresos o modificar la base inicial. ¿Deseas continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reopenTodayCashDay()
                        showReopenCashConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDeep)
                ) {
                    Text("Sí, Reabrir Caja")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReopenCashConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showFactoryResetConfirm) {
        AlertDialog(
            onDismissRequest = { showFactoryResetConfirm = false },
            title = { Text("Reiniciar Todo en Cero") },
            text = {
                Text("Se borrarán todos los productos, proveedores, ventas y egresos para dejar la aplicación completamente vacía. ¿Deseas continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllToZero()
                        showFactoryResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralBadgeText)
                ) {
                    Text("Sí, Dejar en Cero")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun tabIcon(tab: NavTab, selected: Boolean) = when (tab) {
    NavTab.CAJA -> if (selected) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale
    NavTab.INVENTARIO -> if (selected) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2
    NavTab.LIQUIDACION -> if (selected) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
    NavTab.PROVEEDORES -> if (selected) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping
}
