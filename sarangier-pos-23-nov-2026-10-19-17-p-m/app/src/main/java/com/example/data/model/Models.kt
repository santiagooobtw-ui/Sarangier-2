package com.example.data.model

enum class ProductCategory(val label: String) {
    TODOS("Todos"),
    VASOS("Vasos"),
    ENVASES("Envases"),
    PLATOS("Platos"),
    BOLSAS("Bolsas"),
    SERVILLETAS("Servilletas"),
    OTROS("Otros");

    companion object {
        val productCategories: List<ProductCategory> =
            listOf(VASOS, ENVASES, PLATOS, BOLSAS, SERVILLETAS, OTROS)
    }
}

enum class PaymentMethod(val label: String, val shortLabel: String) {
    EFECTIVO("Efectivo", "Efectivo"),
    TRANSFERENCIA("Transferencia / Nequi", "Nequi / Transf."),
    TARJETA("Tarjeta", "Tarjeta")
}

enum class ExpenseType(val label: String) {
    GASTO_OPERATIVO("Gasto Operativo Menor"),
    PAGO_PROVEEDOR("Pago a Proveedor / Compra Rápida")
}

enum class CashSessionStatus(val label: String) {
    ABIERTA("ABIERTA"),
    CERRADA("CERRADA")
}

data class Product(
    val id: String,
    val nombre: String,
    val presentacion: String,
    val categoria: ProductCategory,
    val stockActual: Int,
    val stockMinimo: Int,
    val costoCompra: Long,
    val precioVenta: Long,
    val proveedorId: String
) {
    val marginAmount: Long get() = precioVenta - costoCompra
    val marginPercent: Double
        get() = if (precioVenta > 0) ((precioVenta - costoCompra).toDouble() / precioVenta) * 100.0 else 0.0
    val isOutOfStock: Boolean get() = stockActual <= 0
    val isLowStock: Boolean get() = stockActual in 1..stockMinimo
}

data class Supplier(
    val id: String,
    val empresa: String,
    val contacto: String,
    val telefono: String,
    val diasEntrega: String,
    val notas: String
)

data class SaleItem(
    val productId: String,
    val nombre: String,
    val cantidad: Int,
    val precioVenta: Long,
    val costoUnitarioSnapshot: Long
) {
    val subtotal: Long get() = precioVenta * cantidad
    val totalCost: Long get() = costoUnitarioSnapshot * cantidad
}

data class Sale(
    val id: String,
    val fecha: String,
    val hora: String,
    val metodoPago: PaymentMethod,
    val montoTotal: Long,
    val montoRecibido: Long,
    val cambio: Long,
    val items: List<SaleItem>
)

data class Expense(
    val id: String,
    val fecha: String,
    val hora: String,
    val tipo: ExpenseType,
    val categoria: String,
    val concepto: String,
    val monto: Long,
    val metodoPago: PaymentMethod,
    val proveedorId: String? = null
)

data class CashSession(
    val fechaActiva: String,
    val estado: CashSessionStatus,
    val baseInicial: Long,
    val efectivoContado: Long? = null,
    val observacionesCierre: String? = null,
    val horaCierre: String? = null
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Long get() = product.precioVenta * quantity
}

sealed class MovementItem {
    abstract val id: String
    abstract val fecha: String
    abstract val hora: String
    abstract val monto: Long

    data class SaleMovement(val sale: Sale) : MovementItem() {
        override val id: String = sale.id
        override val fecha: String = sale.fecha
        override val hora: String = sale.hora
        override val monto: Long = sale.montoTotal
    }

    data class ExpenseMovement(val expense: Expense) : MovementItem() {
        override val id: String = expense.id
        override val fecha: String = expense.fecha
        override val hora: String = expense.hora
        override val monto: Long = expense.monto
    }
}

data class ConsolidatedProductRow(
    val productId: String,
    val productName: String,
    val unitsSold: Int,
    val totalCost: Long,
    val totalSales: Long
) {
    val profitGenerated: Long get() = totalSales - totalCost
}

data class DailySettlementSummary(
    val date: String,
    val session: CashSession,
    val grossIncome: Long,
    val cashSales: Long,
    val transferSales: Long,
    val cardSales: Long,
    val cmv: Long,
    val operatingExpenses: Long,
    val supplierPayments: Long,
    val cashExpenses: Long,
    val netProfit: Long,
    val expectedCashInDrawer: Long,
    val consolidatedProducts: List<ConsolidatedProductRow>,
    val salesCount: Int,
    val expensesCount: Int
)

data class AppSettings(
    val businessName: String = "Distribuidora Sarangier",
    val businessSubtitle: String = "",
    val receiptFooter: String = "Gracias por su compra",
    val defaultBaseInicial: Long = 0L,
    val defaultPaymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val confirmBeforeSale: Boolean = false,
    val showOutOfStockInPos: Boolean = true,
    val showCostsAndMarginsInInventory: Boolean = true,
    val defaultMinStock: Int = 5,
    val autoOpenReceiptOnClose: Boolean = false,
    val movementsStripLimit: Int = 5
)

