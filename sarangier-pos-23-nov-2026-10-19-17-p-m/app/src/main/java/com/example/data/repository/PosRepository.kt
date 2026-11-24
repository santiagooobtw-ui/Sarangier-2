package com.example.data.repository

import com.example.data.local.CashSessionEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.PosDao
import com.example.data.local.ProductEntity
import com.example.data.local.SaleEntity
import com.example.data.local.SaleItemEntity
import com.example.data.local.SupplierEntity
import com.example.data.model.CashSession
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.Supplier
import com.example.data.seed.SeedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PosRepository(private val dao: PosDao) {

    val products: Flow<List<Product>> = dao.observeProducts().map { list -> list.map { it.toDomain() } }
    val suppliers: Flow<List<Supplier>> = dao.observeSuppliers().map { list -> list.map { it.toDomain() } }
    val sales: Flow<List<Sale>> = dao.observeSalesWithItems().map { list -> list.map { it.toDomain() } }
    val expenses: Flow<List<Expense>> = dao.observeExpenses().map { list -> list.map { it.toDomain() } }
    val cashSessions: Flow<List<CashSession>> = dao.observeCashSessions().map { list -> list.map { it.toDomain() } }

    suspend fun clearAllData() {
        dao.clearAllData()
    }

    suspend fun restoreFactoryDefaults() {
        val seed = SeedData.createFactorySeed()
        dao.replaceAllWithSeed(
            suppliers = seed.suppliers,
            products = seed.products,
            sales = seed.sales,
            expenses = seed.expenses,
            sessions = seed.cashSessions
        )
    }

    suspend fun recordSale(sale: Sale) {
        val saleEntity = SaleEntity(
            id = sale.id,
            fecha = sale.fecha,
            hora = sale.hora,
            metodoPago = sale.metodoPago.name,
            montoTotal = sale.montoTotal,
            montoRecibido = sale.montoRecibido,
            cambio = sale.cambio
        )
        val itemEntities = sale.items.map { item ->
            SaleItemEntity(
                saleId = sale.id,
                productId = item.productId,
                nombre = item.nombre,
                cantidad = item.cantidad,
                precioVenta = item.precioVenta,
                costoUnitarioSnapshot = item.costoUnitarioSnapshot
            )
        }
        dao.recordSaleAndDecrementStock(saleEntity, itemEntities)
    }

    suspend fun annulSale(saleId: String) {
        dao.annulSaleAndRestoreStock(saleId)
    }

    suspend fun recordExpense(expense: Expense) {
        dao.insertExpense(ExpenseEntity.fromDomain(expense))
    }

    suspend fun deleteExpense(expenseId: String) {
        dao.deleteExpenseById(expenseId)
    }

    suspend fun upsertProduct(product: Product) {
        dao.upsertProduct(ProductEntity.fromDomain(product))
    }

    suspend fun deleteProduct(productId: String) {
        dao.deleteProductById(productId)
    }

    suspend fun restockProducts(
        updates: List<Triple<String, Int, Long?>>,
        expense: Expense?
    ) {
        dao.restockAndOptionalExpense(
            updates = updates,
            expense = expense?.let { ExpenseEntity.fromDomain(it) }
        )
    }

    suspend fun upsertSupplier(supplier: Supplier) {
        dao.upsertSupplier(SupplierEntity.fromDomain(supplier))
    }

    suspend fun saveCashSession(session: CashSession) {
        dao.upsertCashSession(CashSessionEntity.fromDomain(session))
    }
}
