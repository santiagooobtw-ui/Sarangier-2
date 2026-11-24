package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.model.CashSession
import com.example.data.model.CashSessionStatus
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.Supplier

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val presentacion: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val costoCompra: Long,
    val precioVenta: Long,
    val proveedorId: String
) {
    fun toDomain(): Product = Product(
        id = id,
        nombre = nombre,
        presentacion = presentacion,
        categoria = ProductCategory.entries.find { it.name == categoria } ?: ProductCategory.OTROS,
        stockActual = stockActual,
        stockMinimo = stockMinimo,
        costoCompra = costoCompra,
        precioVenta = precioVenta,
        proveedorId = proveedorId
    )

    companion object {
        fun fromDomain(p: Product): ProductEntity = ProductEntity(
            id = p.id,
            nombre = p.nombre,
            presentacion = p.presentacion,
            categoria = p.categoria.name,
            stockActual = p.stockActual,
            stockMinimo = p.stockMinimo,
            costoCompra = p.costoCompra,
            precioVenta = p.precioVenta,
            proveedorId = p.proveedorId
        )
    }
}

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val empresa: String,
    val contacto: String,
    val telefono: String,
    val diasEntrega: String,
    val notas: String
) {
    fun toDomain(): Supplier = Supplier(
        id = id,
        empresa = empresa,
        contacto = contacto,
        telefono = telefono,
        diasEntrega = diasEntrega,
        notas = notas
    )

    companion object {
        fun fromDomain(s: Supplier): SupplierEntity = SupplierEntity(
            id = s.id,
            empresa = s.empresa,
            contacto = s.contacto,
            telefono = s.telefono,
            diasEntrega = s.diasEntrega,
            notas = s.notas
        )
    }
}

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val fecha: String,
    val hora: String,
    val metodoPago: String,
    val montoTotal: Long,
    val montoRecibido: Long,
    val cambio: Long
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val saleId: String,
    val productId: String,
    val nombre: String,
    val cantidad: Int,
    val precioVenta: Long,
    val costoUnitarioSnapshot: Long
) {
    fun toDomain(): SaleItem = SaleItem(
        productId = productId,
        nombre = nombre,
        cantidad = cantidad,
        precioVenta = precioVenta,
        costoUnitarioSnapshot = costoUnitarioSnapshot
    )
}

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
) {
    fun toDomain(): Sale = Sale(
        id = sale.id,
        fecha = sale.fecha,
        hora = sale.hora,
        metodoPago = PaymentMethod.entries.find { it.name == sale.metodoPago } ?: PaymentMethod.EFECTIVO,
        montoTotal = sale.montoTotal,
        montoRecibido = sale.montoRecibido,
        cambio = sale.cambio,
        items = items.map { it.toDomain() }
    )
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val fecha: String,
    val hora: String,
    val tipo: String,
    val categoria: String,
    val concepto: String,
    val monto: Long,
    val metodoPago: String,
    val proveedorId: String?
) {
    fun toDomain(): Expense = Expense(
        id = id,
        fecha = fecha,
        hora = hora,
        tipo = ExpenseType.entries.find { it.name == tipo } ?: ExpenseType.GASTO_OPERATIVO,
        categoria = categoria,
        concepto = concepto,
        monto = monto,
        metodoPago = PaymentMethod.entries.find { it.name == metodoPago } ?: PaymentMethod.EFECTIVO,
        proveedorId = proveedorId
    )

    companion object {
        fun fromDomain(e: Expense): ExpenseEntity = ExpenseEntity(
            id = e.id,
            fecha = e.fecha,
            hora = e.hora,
            tipo = e.tipo.name,
            categoria = e.categoria,
            concepto = e.concepto,
            monto = e.monto,
            metodoPago = e.metodoPago.name,
            proveedorId = e.proveedorId
        )
    }
}

@Entity(tableName = "cash_sessions")
data class CashSessionEntity(
    @PrimaryKey val fechaActiva: String,
    val estado: String,
    val baseInicial: Long,
    val efectivoContado: Long?,
    val observacionesCierre: String?,
    val horaCierre: String?
) {
    fun toDomain(): CashSession = CashSession(
        fechaActiva = fechaActiva,
        estado = CashSessionStatus.entries.find { it.name == estado } ?: CashSessionStatus.ABIERTA,
        baseInicial = baseInicial,
        efectivoContado = efectivoContado,
        observacionesCierre = observacionesCierre,
        horaCierre = horaCierre
    )

    companion object {
        fun fromDomain(c: CashSession): CashSessionEntity = CashSessionEntity(
            fechaActiva = c.fechaActiva,
            estado = c.estado.name,
            baseInicial = c.baseInicial,
            efectivoContado = c.efectivoContado,
            observacionesCierre = c.observacionesCierre,
            horaCierre = c.horaCierre
        )
    }
}
