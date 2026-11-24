package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.CartItem
import com.example.data.model.CashSession
import com.example.data.model.CashSessionStatus
import com.example.data.model.ConsolidatedProductRow
import com.example.data.model.DailySettlementSummary
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.MovementItem
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.Supplier
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PosRepository
import com.example.util.Formatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavTab(val label: String) {
    CAJA("Caja POS"),
    INVENTARIO("Inventario"),
    LIQUIDACION("Liquidación"),
    PROVEEDORES("Proveedores")
}

enum class InventoryFilter(val label: String) {
    ALL("Todos"),
    LOW_STOCK("Stock Bajo"),
    OUT_OF_STOCK("Agotados"),
    HEALTHY("Óptimo")
}

data class RestockLineInput(
    val productId: String,
    val quantity: Int,
    val updateCost: Boolean,
    val newCost: Long
)

data class RestockPrefill(
    val supplierId: String? = null,
    val productId: String? = null
)

data class PosUiState(
    val isLoading: Boolean = false,
    val activeTab: NavTab = NavTab.CAJA,
    val products: List<Product> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val cashSessions: List<CashSession> = emptyList(),
    val todayDate: String = Formatters.todayDate(),
    val todaySession: CashSession = CashSession(
        fechaActiva = Formatters.todayDate(),
        estado = CashSessionStatus.ABIERTA,
        baseInicial = 0L
    ),
    val estimatedCashInDrawerToday: Long = 0L,

    // Caja POS state
    val posSearchQuery: String = "",
    val posSelectedCategory: ProductCategory = ProductCategory.TODOS,
    val cartItems: List<CartItem> = emptyList(),
    val paymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val cashReceivedInput: String = "",
    val todayMovements: List<MovementItem> = emptyList(),

    // Inventory state
    val inventorySearchQuery: String = "",
    val inventoryFilter: InventoryFilter = InventoryFilter.ALL,
    val restockPrefill: RestockPrefill? = null,
    val showRestockDialog: Boolean = false,

    // Liquidación state
    val selectedSettlementDate: String = Formatters.todayDate(),
    val availableDates: List<String> = listOf(Formatters.todayDate()),
    val settlementSummary: DailySettlementSummary? = null,
    val countedCashInput: String = "",
    val closureNotesInput: String = "",
    val showClosureSuccessAlert: Boolean = false,
    val showClosureReceiptModal: Boolean = false,

    // Proveedores state
    val selectedSupplierId: String? = null,

    // Configuración global
    val settings: AppSettings = AppSettings()
) {
    val filteredPosProducts: List<Product>
        get() {
            val q = posSearchQuery.trim().lowercase()
            return products.filter { p ->
                val matchesCat = posSelectedCategory == ProductCategory.TODOS || p.categoria == posSelectedCategory
                val matchesQuery = q.isEmpty() ||
                    p.nombre.lowercase().contains(q) ||
                    p.presentacion.lowercase().contains(q)
                val matchesStock = settings.showOutOfStockInPos || p.stockActual > 0
                matchesCat && matchesQuery && matchesStock
            }
        }

    val cartTotal: Long get() = cartItems.sumOf { it.subtotal }
    val cartUnitsCount: Int get() = cartItems.sumOf { it.quantity }
    val cashReceivedAmount: Long get() = Formatters.parseCOPInput(cashReceivedInput) ?: 0L
    val cashChange: Long get() = cashReceivedAmount - cartTotal
    val isTodayClosed: Boolean get() = todaySession.estado == CashSessionStatus.CERRADA

    val canConfirmSale: Boolean
        get() {
            if (isTodayClosed || cartItems.isEmpty()) return false
            return if (paymentMethod == PaymentMethod.EFECTIVO) {
                cashReceivedAmount >= cartTotal
            } else {
                true
            }
        }

    // Inventory KPIs
    val totalReferences: Int get() = products.size
    val totalInventoryCostValue: Long get() = products.sumOf { it.stockActual.toLong() * it.costoCompra }
    val lowStockCount: Int get() = products.count { it.isLowStock }
    val outOfStockCount: Int get() = products.count { it.isOutOfStock }

    val filteredInventoryProducts: List<Product>
        get() {
            val q = inventorySearchQuery.trim().lowercase()
            return products.filter { p ->
                val matchesQuery = q.isEmpty() ||
                    p.nombre.lowercase().contains(q) ||
                    p.presentacion.lowercase().contains(q) ||
                    p.categoria.label.lowercase().contains(q)
                val matchesFilter = when (inventoryFilter) {
                    InventoryFilter.ALL -> true
                    InventoryFilter.LOW_STOCK -> p.isLowStock
                    InventoryFilter.OUT_OF_STOCK -> p.isOutOfStock
                    InventoryFilter.HEALTHY -> !p.isOutOfStock && !p.isLowStock
                }
                matchesQuery && matchesFilter
            }
        }
}

private data class LocalUiControls(
    val activeTab: NavTab = NavTab.CAJA,
    val posSearchQuery: String = "",
    val posSelectedCategory: ProductCategory = ProductCategory.TODOS,
    val cartQuantities: Map<String, Int> = emptyMap(),
    val paymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val cashReceivedInput: String = "",
    val inventorySearchQuery: String = "",
    val inventoryFilter: InventoryFilter = InventoryFilter.ALL,
    val restockPrefill: RestockPrefill? = null,
    val showRestockDialog: Boolean = false,
    val selectedSettlementDate: String = Formatters.todayDate(),
    val countedCashInput: String = "",
    val closureNotesInput: String = "",
    val showClosureSuccessAlert: Boolean = false,
    val showClosureReceiptModal: Boolean = false,
    val selectedSupplierId: String? = null
)

class PosViewModel(
    private val repository: PosRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val controls = MutableStateFlow(LocalUiControls())
    private val fallbackSettings = MutableStateFlow(AppSettings())
    private val settingsFlow: Flow<AppSettings> = settingsRepository?.settings ?: fallbackSettings
    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    val uiState: StateFlow<PosUiState> = combine(
        repository.products,
        repository.suppliers,
        repository.sales,
        repository.expenses,
        repository.cashSessions,
        controls,
        settingsFlow
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val products = arr[0] as List<Product>
        @Suppress("UNCHECKED_CAST")
        val suppliers = arr[1] as List<Supplier>
        @Suppress("UNCHECKED_CAST")
        val sales = arr[2] as List<Sale>
        @Suppress("UNCHECKED_CAST")
        val expenses = arr[3] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val sessions = arr[4] as List<CashSession>
        val ctrl = arr[5] as LocalUiControls
        val appSettings = arr[6] as AppSettings

        val today = Formatters.todayDate()
        val todaySession = sessions.find { it.fechaActiva == today }
            ?: CashSession(
                fechaActiva = today,
                estado = CashSessionStatus.ABIERTA,
                baseInicial = appSettings.defaultBaseInicial
            )

        val todaySales = sales.filter { it.fecha == today }
        val todayExpenses = expenses.filter { it.fecha == today }

        val todayCashSales = todaySales.filter { it.metodoPago == PaymentMethod.EFECTIVO }.sumOf { it.montoTotal }
        val todayCashExpenses = todayExpenses.filter { it.metodoPago == PaymentMethod.EFECTIVO }.sumOf { it.monto }
        val estimatedDrawer = todaySession.baseInicial + todayCashSales - todayCashExpenses

        // Map cart quantities to live products
        val productMap = products.associateBy { it.id }
        val cartItems = ctrl.cartQuantities.mapNotNull { (prodId, qty) ->
            val prod = productMap[prodId] ?: return@mapNotNull null
            val safeQty = qty.coerceAtMost(prod.stockActual)
            if (safeQty > 0) CartItem(prod, safeQty) else null
        }

        val todayMovements: List<MovementItem> = (
            todaySales.map { MovementItem.SaleMovement(it) } +
                todayExpenses.map { MovementItem.ExpenseMovement(it) }
            ).sortedWith(compareByDescending<MovementItem> { it.hora }.thenByDescending { it.id })
            .take(appSettings.movementsStripLimit.coerceIn(5, 20))

        val availableDates = (
            sessions.map { it.fechaActiva } +
                sales.map { it.fecha } +
                expenses.map { it.fecha } +
                listOf(today)
            ).distinct().sortedDescending()

        val selectedDate = if (ctrl.selectedSettlementDate in availableDates) {
            ctrl.selectedSettlementDate
        } else {
            today
        }

        val summary = computeDailySummary(
            date = selectedDate,
            sessions = sessions,
            sales = sales,
            expenses = expenses,
            defaultBaseForNewDay = appSettings.defaultBaseInicial
        )

        val resolvedSupplierId = if (ctrl.selectedSupplierId != null && suppliers.any { it.id == ctrl.selectedSupplierId }) {
            ctrl.selectedSupplierId
        } else {
            suppliers.firstOrNull()?.id
        }

        PosUiState(
            isLoading = false,
            activeTab = ctrl.activeTab,
            products = products,
            suppliers = suppliers,
            sales = sales,
            expenses = expenses,
            cashSessions = sessions,
            todayDate = today,
            todaySession = todaySession,
            estimatedCashInDrawerToday = estimatedDrawer,
            posSearchQuery = ctrl.posSearchQuery,
            posSelectedCategory = ctrl.posSelectedCategory,
            cartItems = cartItems,
            paymentMethod = ctrl.paymentMethod,
            cashReceivedInput = ctrl.cashReceivedInput,
            todayMovements = todayMovements,
            inventorySearchQuery = ctrl.inventorySearchQuery,
            inventoryFilter = ctrl.inventoryFilter,
            restockPrefill = ctrl.restockPrefill,
            showRestockDialog = ctrl.showRestockDialog,
            selectedSettlementDate = selectedDate,
            availableDates = availableDates,
            settlementSummary = summary,
            countedCashInput = ctrl.countedCashInput,
            closureNotesInput = ctrl.closureNotesInput,
            showClosureSuccessAlert = ctrl.showClosureSuccessAlert,
            showClosureReceiptModal = ctrl.showClosureReceiptModal,
            selectedSupplierId = resolvedSupplierId,
            settings = appSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PosUiState()
    )

    init {
        settingsRepository?.let { repo ->
            viewModelScope.launch {
                var initialized = false
                repo.settings.collect { appSettings ->
                    if (!initialized) {
                        initialized = true
                        controls.value = controls.value.copy(paymentMethod = appSettings.defaultPaymentMethod)
                    }
                }
            }
        }
    }

    private fun computeDailySummary(
        date: String,
        sessions: List<CashSession>,
        sales: List<Sale>,
        expenses: List<Expense>,
        defaultBaseForNewDay: Long = 0L
    ): DailySettlementSummary {
        val session = sessions.find { it.fechaActiva == date }
            ?: CashSession(
                fechaActiva = date,
                estado = CashSessionStatus.ABIERTA,
                baseInicial = if (date == Formatters.todayDate()) defaultBaseForNewDay else 0L
            )

        val daySales = sales.filter { it.fecha == date }
        val dayExpenses = expenses.filter { it.fecha == date }

        val grossIncome = daySales.sumOf { it.montoTotal }
        val cashSales = daySales.filter { it.metodoPago == PaymentMethod.EFECTIVO }.sumOf { it.montoTotal }
        val transferSales = daySales.filter { it.metodoPago == PaymentMethod.TRANSFERENCIA }.sumOf { it.montoTotal }
        val cardSales = daySales.filter { it.metodoPago == PaymentMethod.TARJETA }.sumOf { it.montoTotal }

        val cmv = daySales.sumOf { sale ->
            sale.items.sumOf { it.cantidad.toLong() * it.costoUnitarioSnapshot }
        }

        val operatingExpenses = dayExpenses
            .filter { it.tipo == ExpenseType.GASTO_OPERATIVO }
            .sumOf { it.monto }
        val supplierPayments = dayExpenses
            .filter { it.tipo == ExpenseType.PAGO_PROVEEDOR }
            .sumOf { it.monto }
        val cashExpenses = dayExpenses
            .filter { it.metodoPago == PaymentMethod.EFECTIVO }
            .sumOf { it.monto }

        val netProfit = (grossIncome - cmv) - operatingExpenses
        val expectedCash = session.baseInicial + cashSales - cashExpenses

        val consolidated = daySales
            .flatMap { it.items }
            .groupBy { it.productId }
            .map { (productId, items) ->
                ConsolidatedProductRow(
                    productId = productId,
                    productName = items.first().nombre,
                    unitsSold = items.sumOf { it.cantidad },
                    totalCost = items.sumOf { it.totalCost },
                    totalSales = items.sumOf { it.subtotal }
                )
            }
            .sortedByDescending { it.totalSales }

        return DailySettlementSummary(
            date = date,
            session = session,
            grossIncome = grossIncome,
            cashSales = cashSales,
            transferSales = transferSales,
            cardSales = cardSales,
            cmv = cmv,
            operatingExpenses = operatingExpenses,
            supplierPayments = supplierPayments,
            cashExpenses = cashExpenses,
            netProfit = netProfit,
            expectedCashInDrawer = expectedCash,
            consolidatedProducts = consolidated,
            salesCount = daySales.size,
            expensesCount = dayExpenses.size
        )
    }

    // Navigation
    fun selectTab(tab: NavTab) {
        controls.value = controls.value.copy(activeTab = tab)
    }

    // Caja POS actions
    fun updatePosSearch(query: String) {
        controls.value = controls.value.copy(posSearchQuery = query)
    }

    fun selectPosCategory(category: ProductCategory) {
        controls.value = controls.value.copy(posSelectedCategory = category)
    }

    fun onPosSearchSubmit() {
        val matches = uiState.value.filteredPosProducts
        if (matches.size == 1) {
            addToCart(matches.first())
        }
    }

    fun addToCart(product: Product) {
        if (uiState.value.isTodayClosed) {
            emitToast("Caja cerrada hoy. Reabra o consulte días anteriores en Liquidación.")
            return
        }
        if (product.stockActual <= 0) {
            emitToast("Producto agotado")
            return
        }
        val currentMap = controls.value.cartQuantities.toMutableMap()
        val currentQty = currentMap[product.id] ?: 0
        if (currentQty >= product.stockActual) {
            emitToast("Stock máximo alcanzado (${product.stockActual} disp.)")
            return
        }
        currentMap[product.id] = currentQty + 1
        controls.value = controls.value.copy(cartQuantities = currentMap)
    }

    fun decrementCartItem(productId: String) {
        val currentMap = controls.value.cartQuantities.toMutableMap()
        val currentQty = currentMap[productId] ?: return
        if (currentQty <= 1) {
            currentMap.remove(productId)
        } else {
            currentMap[productId] = currentQty - 1
        }
        controls.value = controls.value.copy(cartQuantities = currentMap)
    }

    fun setCartQuantity(productId: String, requestedQty: Int) {
        val product = uiState.value.products.find { it.id == productId } ?: return
        val currentMap = controls.value.cartQuantities.toMutableMap()
        when {
            requestedQty <= 0 -> currentMap.remove(productId)
            requestedQty > product.stockActual -> {
                currentMap[productId] = product.stockActual
                emitToast("Stock máximo alcanzado (${product.stockActual} disp.)")
            }
            else -> currentMap[productId] = requestedQty
        }
        controls.value = controls.value.copy(cartQuantities = currentMap)
    }

    fun removeFromCart(productId: String) {
        val currentMap = controls.value.cartQuantities.toMutableMap()
        currentMap.remove(productId)
        controls.value = controls.value.copy(cartQuantities = currentMap)
    }

    fun clearCart() {
        controls.value = controls.value.copy(
            cartQuantities = emptyMap(),
            cashReceivedInput = "",
            paymentMethod = uiState.value.settings.defaultPaymentMethod
        )
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        controls.value = controls.value.copy(paymentMethod = method)
    }

    fun updateCashReceivedInput(input: String) {
        val cleaned = input.filter { it.isDigit() }
        controls.value = controls.value.copy(cashReceivedInput = cleaned)
    }

    fun applyQuickCash(amount: Long) {
        controls.value = controls.value.copy(cashReceivedInput = amount.toString())
    }

    fun confirmSale() {
        val state = uiState.value
        if (state.isTodayClosed) {
            emitToast("Caja cerrada: no se pueden registrar ventas hoy")
            return
        }
        if (state.cartItems.isEmpty()) {
            emitToast("Agrega al menos un producto al carrito")
            return
        }
        val total = state.cartTotal
        val received = if (state.paymentMethod == PaymentMethod.EFECTIVO) {
            state.cashReceivedAmount
        } else {
            total
        }
        if (state.paymentMethod == PaymentMethod.EFECTIVO && received < total) {
            emitToast("El monto recibido es menor al total")
            return
        }

        val sale = Sale(
            id = "sale-${UUID.randomUUID().toString().take(8)}",
            fecha = Formatters.todayDate(),
            hora = Formatters.currentTime(),
            metodoPago = state.paymentMethod,
            montoTotal = total,
            montoRecibido = received,
            cambio = (received - total).coerceAtLeast(0L),
            items = state.cartItems.map { cartItem ->
                SaleItem(
                    productId = cartItem.product.id,
                    nombre = cartItem.product.nombre,
                    cantidad = cartItem.quantity,
                    precioVenta = cartItem.product.precioVenta,
                    costoUnitarioSnapshot = cartItem.product.costoCompra
                )
            }
        )

        viewModelScope.launch {
            repository.recordSale(sale)
            controls.value = controls.value.copy(
                cartQuantities = emptyMap(),
                cashReceivedInput = "",
                paymentMethod = state.settings.defaultPaymentMethod
            )
            emitToast("Venta registrada por ${Formatters.formatCOP(total)}")
        }
    }

    fun recordQuickExpense(
        tipo: ExpenseType,
        categoria: String,
        concepto: String,
        monto: Long,
        metodoPago: PaymentMethod,
        proveedorId: String?
    ) {
        if (uiState.value.isTodayClosed) {
            emitToast("Caja cerrada: no se pueden registrar egresos hoy")
            return
        }
        if (monto <= 0L || concepto.isBlank()) {
            emitToast("Completa el concepto y un monto válido")
            return
        }
        val expense = Expense(
            id = "exp-${UUID.randomUUID().toString().take(8)}",
            fecha = Formatters.todayDate(),
            hora = Formatters.currentTime(),
            tipo = tipo,
            categoria = categoria.ifBlank { if (tipo == ExpenseType.PAGO_PROVEEDOR) "Compra de Mercancía" else "Operativo" },
            concepto = concepto.trim(),
            monto = monto,
            metodoPago = metodoPago,
            proveedorId = if (tipo == ExpenseType.PAGO_PROVEEDOR) proveedorId else null
        )
        viewModelScope.launch {
            repository.recordExpense(expense)
            emitToast("Egreso registrado: ${Formatters.formatCOP(monto)}")
        }
    }

    fun annulMovement(movement: MovementItem) {
        if (uiState.value.isTodayClosed) {
            emitToast("Caja cerrada: no se pueden anular movimientos de hoy")
            return
        }
        viewModelScope.launch {
            when (movement) {
                is MovementItem.SaleMovement -> {
                    repository.annulSale(movement.sale.id)
                    emitToast("Venta anulada y stock restaurado al inventario")
                }
                is MovementItem.ExpenseMovement -> {
                    repository.deleteExpense(movement.expense.id)
                    emitToast("Egreso anulado")
                }
            }
        }
    }

    // Inventory Actions
    fun updateInventorySearch(query: String) {
        controls.value = controls.value.copy(inventorySearchQuery = query)
    }

    fun selectInventoryFilter(filter: InventoryFilter) {
        val next = if (controls.value.inventoryFilter == filter && filter != InventoryFilter.ALL) {
            InventoryFilter.ALL
        } else {
            filter
        }
        controls.value = controls.value.copy(inventoryFilter = next)
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.upsertProduct(product)
            emitToast("Producto guardado: ${product.nombre}")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            val currentMap = controls.value.cartQuantities.toMutableMap()
            currentMap.remove(productId)
            controls.value = controls.value.copy(cartQuantities = currentMap)
            emitToast("Producto eliminado")
        }
    }

    fun openRestockModal(supplierId: String? = null, productId: String? = null) {
        if (uiState.value.products.isEmpty()) {
            emitToast("Primero agrega al menos un producto en Inventario")
            return
        }
        controls.value = controls.value.copy(
            restockPrefill = RestockPrefill(supplierId = supplierId, productId = productId),
            showRestockDialog = true
        )
    }

    fun dismissRestockModal() {
        controls.value = controls.value.copy(showRestockDialog = false, restockPrefill = null)
    }

    fun submitRestock(
        lines: List<RestockLineInput>,
        registerCashOut: Boolean,
        paymentMethod: PaymentMethod,
        supplierId: String?
    ) {
        val validLines = lines.filter { it.quantity > 0 }
        if (validLines.isEmpty()) {
            emitToast("Ingresa al menos una cantidad mayor a 0")
            return
        }
        if (registerCashOut && uiState.value.isTodayClosed) {
            emitToast("Caja cerrada hoy: desactiva el registro de salida de caja o reabre")
            return
        }

        val productMap = uiState.value.products.associateBy { it.id }
        val updates = validLines.map { line ->
            Triple(
                line.productId,
                line.quantity,
                if (line.updateCost && line.newCost > 0) line.newCost else null
            )
        }

        val expense: Expense? = if (registerCashOut) {
            val totalCost = validLines.sumOf { line ->
                val prod = productMap[line.productId]
                val unitCost = if (line.updateCost && line.newCost > 0) line.newCost else (prod?.costoCompra ?: 0L)
                unitCost * line.quantity
            }
            val namesSummary = validLines.joinToString(", ") { line ->
                val pName = productMap[line.productId]?.nombre ?: "Producto"
                "${line.quantity}x $pName"
            }.take(72)
            Expense(
                id = "exp-${UUID.randomUUID().toString().take(8)}",
                fecha = Formatters.todayDate(),
                hora = Formatters.currentTime(),
                tipo = ExpenseType.PAGO_PROVEEDOR,
                categoria = "Compra de Mercancía",
                concepto = "Surtido: $namesSummary",
                monto = totalCost,
                metodoPago = paymentMethod,
                proveedorId = supplierId ?: validLines.firstOrNull()?.let { productMap[it.productId]?.proveedorId }
            )
        } else {
            null
        }

        viewModelScope.launch {
            repository.restockProducts(updates, expense)
            controls.value = controls.value.copy(showRestockDialog = false, restockPrefill = null)
            emitToast(
                if (registerCashOut) "Stock actualizado y pago a proveedor registrado"
                else "Stock actualizado correctamente"
            )
        }
    }

    // Liquidación Actions
    fun selectSettlementDate(date: String) {
        controls.value = controls.value.copy(
            selectedSettlementDate = date,
            countedCashInput = "",
            closureNotesInput = ""
        )
    }

    fun updateCountedCashInput(input: String) {
        controls.value = controls.value.copy(countedCashInput = input.filter { it.isDigit() })
    }

    fun updateClosureNotesInput(notes: String) {
        controls.value = controls.value.copy(closureNotesInput = notes)
    }

    fun closeCashDay() {
        val state = uiState.value
        val summary = state.settlementSummary ?: return
        if (summary.session.estado == CashSessionStatus.CERRADA) {
            controls.value = controls.value.copy(showClosureReceiptModal = true)
            return
        }
        val counted = Formatters.parseCOPInput(state.countedCashInput)
        if (counted == null) {
            emitToast("Digita el efectivo contado físicamente para cerrar la caja")
            return
        }
        val closedSession = summary.session.copy(
            estado = CashSessionStatus.CERRADA,
            efectivoContado = counted,
            observacionesCierre = state.closureNotesInput.trim().ifBlank { null },
            horaCierre = Formatters.currentTime()
        )
        viewModelScope.launch {
            repository.saveCashSession(closedSession)
            if (state.settings.autoOpenReceiptOnClose) {
                controls.value = controls.value.copy(showClosureReceiptModal = true)
            } else {
                controls.value = controls.value.copy(showClosureSuccessAlert = true)
            }
        }
    }

    fun reopenTodayCashDay() {
        val state = uiState.value
        val session = state.todaySession
        if (session.estado == CashSessionStatus.ABIERTA) return
        viewModelScope.launch {
            repository.saveCashSession(
                session.copy(
                    estado = CashSessionStatus.ABIERTA,
                    efectivoContado = null,
                    observacionesCierre = null,
                    horaCierre = null
                )
            )
            controls.value = controls.value.copy(
                showClosureSuccessAlert = false,
                showClosureReceiptModal = false,
                countedCashInput = "",
                closureNotesInput = ""
            )
            emitToast("Caja de hoy reabierta correctamente")
        }
    }

    fun dismissClosureSuccessAlert() {
        controls.value = controls.value.copy(showClosureSuccessAlert = false)
    }

    fun openClosureReceiptModal() {
        controls.value = controls.value.copy(showClosureReceiptModal = true)
    }

    fun dismissClosureReceiptModal() {
        controls.value = controls.value.copy(showClosureReceiptModal = false)
    }

    // Proveedores Actions
    fun selectSupplier(supplierId: String) {
        controls.value = controls.value.copy(selectedSupplierId = supplierId)
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.upsertSupplier(supplier)
            controls.value = controls.value.copy(selectedSupplierId = supplier.id)
            emitToast("Proveedor guardado: ${supplier.empresa}")
        }
    }

    fun updateBaseInicial(date: String, amount: Long) {
        val summary = uiState.value.settlementSummary
        val currentSession = if (summary != null && summary.date == date) {
            summary.session
        } else {
            uiState.value.cashSessions.find { it.fechaActiva == date }
                ?: CashSession(fechaActiva = date, estado = CashSessionStatus.ABIERTA, baseInicial = 0L)
        }
        if (currentSession.estado == CashSessionStatus.CERRADA) {
            emitToast("No se puede modificar la base de una caja cerrada")
            return
        }
        viewModelScope.launch {
            repository.saveCashSession(currentSession.copy(baseInicial = amount.coerceAtLeast(0L)))
            emitToast("Base inicial actualizada: ${Formatters.formatCOP(amount.coerceAtLeast(0L))}")
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        val previous = uiState.value.settings
        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.saveSettings(newSettings)
            } else {
                fallbackSettings.value = newSettings
            }
            if (newSettings.defaultPaymentMethod != previous.defaultPaymentMethod) {
                controls.value = controls.value.copy(paymentMethod = newSettings.defaultPaymentMethod)
            }
            emitToast("Configuración guardada")
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.resetToDefaults()
            } else {
                fallbackSettings.value = AppSettings()
            }
            controls.value = controls.value.copy(paymentMethod = PaymentMethod.EFECTIVO)
            emitToast("Preferencias restauradas a valores predeterminados")
        }
    }

    // Reset all to zero
    fun resetAllToZero() {
        viewModelScope.launch {
            repository.clearAllData()
            controls.value = LocalUiControls(paymentMethod = uiState.value.settings.defaultPaymentMethod)
            emitToast("Toda la información quedó en cero")
        }
    }

    private fun emitToast(msg: String) {
        _toastMessages.tryEmit(msg)
    }

    class Factory(
        private val repository: PosRepository,
        private val settingsRepository: SettingsRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PosViewModel(repository, settingsRepository) as T
        }
    }
}
