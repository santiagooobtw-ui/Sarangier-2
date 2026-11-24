package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {

    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("UPDATE products SET stockActual = stockActual + :delta WHERE id = :productId")
    suspend fun adjustProductStock(productId: String, delta: Int)

    @Query("UPDATE products SET stockActual = stockActual + :delta, costoCompra = :newCost WHERE id = :productId")
    suspend fun restockProductWithCost(productId: String, delta: Int, newCost: Long)

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY empresa ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplier(supplier: SupplierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    // Sales
    @Transaction
    @Query("SELECT * FROM sales ORDER BY fecha DESC, hora DESC")
    fun observeSalesWithItems(): Flow<List<SaleWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: String)

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleWithItems(saleId: String): SaleWithItems?

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY fecha DESC, hora DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    // Cash Sessions
    @Query("SELECT * FROM cash_sessions ORDER BY fechaActiva DESC")
    fun observeCashSessions(): Flow<List<CashSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCashSession(session: CashSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashSessions(sessions: List<CashSessionEntity>)

    // Atomic Checkout
    @Transaction
    suspend fun recordSaleAndDecrementStock(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ) {
        insertSale(sale)
        insertSaleItems(items)
        items.forEach { item ->
            adjustProductStock(item.productId, -item.cantidad)
        }
    }

    // Atomic Void Sale
    @Transaction
    suspend fun annulSaleAndRestoreStock(saleId: String) {
        val saleWithItems = getSaleWithItems(saleId) ?: return
        saleWithItems.items.forEach { item ->
            adjustProductStock(item.productId, item.cantidad)
        }
        deleteSaleById(saleId)
    }

    // Atomic Restock
    @Transaction
    suspend fun restockAndOptionalExpense(
        updates: List<Triple<String, Int, Long?>>,
        expense: ExpenseEntity?
    ) {
        updates.forEach { (productId, qty, updatedCost) ->
            if (updatedCost != null) {
                restockProductWithCost(productId, qty, updatedCost)
            } else {
                adjustProductStock(productId, qty)
            }
        }
        if (expense != null) {
            insertExpense(expense)
        }
    }

    // Wipe & Seed
    @Query("DELETE FROM sale_items")
    suspend fun clearSaleItems()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM cash_sessions")
    suspend fun clearCashSessions()

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("DELETE FROM suppliers")
    suspend fun clearSuppliers()

    @Transaction
    suspend fun clearAllData() {
        clearSaleItems()
        clearSales()
        clearExpenses()
        clearCashSessions()
        clearProducts()
        clearSuppliers()
    }

    @Transaction
    suspend fun replaceAllWithSeed(
        suppliers: List<SupplierEntity>,
        products: List<ProductEntity>,
        sales: List<Pair<SaleEntity, List<SaleItemEntity>>>,
        expenses: List<ExpenseEntity>,
        sessions: List<CashSessionEntity>
    ) {
        clearSaleItems()
        clearSales()
        clearExpenses()
        clearCashSessions()
        clearProducts()
        clearSuppliers()

        insertSuppliers(suppliers)
        insertProducts(products)
        sales.forEach { (sale, items) ->
            insertSale(sale)
            insertSaleItems(items)
        }
        insertExpenses(expenses)
        insertCashSessions(sessions)
    }
}
