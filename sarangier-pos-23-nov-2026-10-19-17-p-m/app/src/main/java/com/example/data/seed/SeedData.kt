package com.example.data.seed

import com.example.data.local.CashSessionEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SaleEntity
import com.example.data.local.SaleItemEntity
import com.example.data.local.SupplierEntity
import com.example.data.model.CashSessionStatus
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import com.example.util.Formatters

data class SeedBundle(
    val suppliers: List<SupplierEntity>,
    val products: List<ProductEntity>,
    val sales: List<Pair<SaleEntity, List<SaleItemEntity>>>,
    val expenses: List<ExpenseEntity>,
    val cashSessions: List<CashSessionEntity>
)

object SeedData {

    fun createFactorySeed(): SeedBundle {
        val today = Formatters.todayDate()
        val yesterday = Formatters.yesterdayDate()

        val suppliers = listOf(
            SupplierEntity(
                id = "sup-1",
                empresa = "Plásticos del Centro S.A.S.",
                contacto = "Carlos Alberto Mendoza",
                telefono = "3104567890",
                diasEntrega = "Lunes y Jueves",
                notas = "Pedido mínimo $150.000. Entrega directa en bodega principal antes de las 11:00 a.m."
            ),
            SupplierEntity(
                id = "sup-2",
                empresa = "Distribuidora Empaques y Vasos Bogotá",
                contacto = "Sandra Milena Ruiz",
                telefono = "3157891234",
                diasEntrega = "Martes y Viernes",
                notas = "3% descuento por pago de contado en línea de vasos plásticos e icopor térmico."
            ),
            SupplierEntity(
                id = "sup-3",
                empresa = "Papelería e Icopor La Sabana",
                contacto = "Jorge Eliécer Gómez",
                telefono = "3203216549",
                diasEntrega = "Miércoles y Sábados",
                notas = "Proveedor principal de servilletas institucionales, papel aluminio y portacomidas."
            )
        )

        val products = listOf(
            ProductEntity(
                id = "prod-1",
                nombre = "Vaso Plástico Transparente 7oz",
                presentacion = "Paq x50",
                categoria = ProductCategory.VASOS.name,
                stockActual = 45,
                stockMinimo = 15,
                costoCompra = 2800L,
                precioVenta = 4200L,
                proveedorId = "sup-2"
            ),
            ProductEntity(
                id = "prod-2",
                nombre = "Vaso Térmico Icopor 8oz",
                presentacion = "Paq x25",
                categoria = ProductCategory.VASOS.name,
                stockActual = 8,
                stockMinimo = 12,
                costoCompra = 2200L,
                precioVenta = 3500L,
                proveedorId = "sup-2"
            ),
            ProductEntity(
                id = "prod-3",
                nombre = "Contenedor Bisagra Icopor J1",
                presentacion = "Paq x20",
                categoria = ProductCategory.ENVASES.name,
                stockActual = 30,
                stockMinimo = 10,
                costoCompra = 3400L,
                precioVenta = 5000L,
                proveedorId = "sup-3"
            ),
            ProductEntity(
                id = "prod-4",
                nombre = "Portacomidas Plástico 3 Divisiones c/Tapa",
                presentacion = "Paq x25",
                categoria = ProductCategory.ENVASES.name,
                stockActual = 18,
                stockMinimo = 8,
                costoCompra = 8500L,
                precioVenta = 12000L,
                proveedorId = "sup-1"
            ),
            ProductEntity(
                id = "prod-5",
                nombre = "Plato Pando Desechable #23 Blanco",
                presentacion = "Paq x20",
                categoria = ProductCategory.PLATOS.name,
                stockActual = 5,
                stockMinimo = 10,
                costoCompra = 2500L,
                precioVenta = 3800L,
                proveedorId = "sup-1"
            ),
            ProductEntity(
                id = "prod-6",
                nombre = "Plato Hondo Sopero 12oz",
                presentacion = "Paq x20",
                categoria = ProductCategory.PLATOS.name,
                stockActual = 0,
                stockMinimo = 10,
                costoCompra = 2100L,
                precioVenta = 3200L,
                proveedorId = "sup-1"
            ),
            ProductEntity(
                id = "prod-7",
                nombre = "Bolsa Camiseta Blanca 2kg",
                presentacion = "Paq x100",
                categoria = ProductCategory.BOLSAS.name,
                stockActual = 60,
                stockMinimo = 20,
                costoCompra = 1900L,
                precioVenta = 3000L,
                proveedorId = "sup-1"
            ),
            ProductEntity(
                id = "prod-8",
                nombre = "Bolsa Camiseta Negra 5kg",
                presentacion = "Paq x100",
                categoria = ProductCategory.BOLSAS.name,
                stockActual = 25,
                stockMinimo = 10,
                costoCompra = 3800L,
                precioVenta = 5500L,
                proveedorId = "sup-1"
            ),
            ProductEntity(
                id = "prod-9",
                nombre = "Bolsa Hermética Tipo Ziploc 18x20cm",
                presentacion = "Paq x100",
                categoria = ProductCategory.BOLSAS.name,
                stockActual = 14,
                stockMinimo = 8,
                costoCompra = 4500L,
                precioVenta = 7000L,
                proveedorId = "sup-2"
            ),
            ProductEntity(
                id = "prod-10",
                nombre = "Servilleta Doblada Económica",
                presentacion = "Paq x150",
                categoria = ProductCategory.SERVILLETAS.name,
                stockActual = 50,
                stockMinimo = 15,
                costoCompra = 1400L,
                precioVenta = 2200L,
                proveedorId = "sup-3"
            ),
            ProductEntity(
                id = "prod-11",
                nombre = "Cuchara Sopera Plástica Blanca",
                presentacion = "Paq x100",
                categoria = ProductCategory.OTROS.name,
                stockActual = 32,
                stockMinimo = 10,
                costoCompra = 2300L,
                precioVenta = 3500L,
                proveedorId = "sup-2"
            ),
            ProductEntity(
                id = "prod-12",
                nombre = "Rollo Papel Aluminio 40 Metros",
                presentacion = "Rollo x1",
                categoria = ProductCategory.OTROS.name,
                stockActual = 12,
                stockMinimo = 6,
                costoCompra = 9000L,
                precioVenta = 13500L,
                proveedorId = "sup-3"
            )
        )

        // Today: 4 realistic sales (Efectivo & Transferencia)
        val todaySale1 = SaleEntity(
            id = "sale-t1",
            fecha = today,
            hora = "08:45",
            metodoPago = PaymentMethod.EFECTIVO.name,
            montoTotal = 24600L,
            montoRecibido = 30000L,
            cambio = 5400L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-t1",
                productId = "prod-1",
                nombre = "Vaso Plástico Transparente 7oz",
                cantidad = 3,
                precioVenta = 4200L,
                costoUnitarioSnapshot = 2800L
            ),
            SaleItemEntity(
                saleId = "sale-t1",
                productId = "prod-7",
                nombre = "Bolsa Camiseta Blanca 2kg",
                cantidad = 4,
                precioVenta = 3000L,
                costoUnitarioSnapshot = 1900L
            )
        )

        val todaySale2 = SaleEntity(
            id = "sale-t2",
            fecha = today,
            hora = "10:15",
            metodoPago = PaymentMethod.TRANSFERENCIA.name,
            montoTotal = 46000L,
            montoRecibido = 46000L,
            cambio = 0L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-t2",
                productId = "prod-4",
                nombre = "Portacomidas Plástico 3 Divisiones c/Tapa",
                cantidad = 2,
                precioVenta = 12000L,
                costoUnitarioSnapshot = 8500L
            ),
            SaleItemEntity(
                saleId = "sale-t2",
                productId = "prod-10",
                nombre = "Servilleta Doblada Económica",
                cantidad = 10,
                precioVenta = 2200L,
                costoUnitarioSnapshot = 1400L
            )
        )

        val todaySale3 = SaleEntity(
            id = "sale-t3",
            fecha = today,
            hora = "11:40",
            metodoPago = PaymentMethod.EFECTIVO.name,
            montoTotal = 31000L,
            montoRecibido = 50000L,
            cambio = 19000L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-t3",
                productId = "prod-3",
                nombre = "Contenedor Bisagra Icopor J1",
                cantidad = 4,
                precioVenta = 5000L,
                costoUnitarioSnapshot = 3400L
            ),
            SaleItemEntity(
                saleId = "sale-t3",
                productId = "prod-8",
                nombre = "Bolsa Camiseta Negra 5kg",
                cantidad = 2,
                precioVenta = 5500L,
                costoUnitarioSnapshot = 3800L
            )
        )

        val todaySale4 = SaleEntity(
            id = "sale-t4",
            fecha = today,
            hora = "14:20",
            metodoPago = PaymentMethod.EFECTIVO.name,
            montoTotal = 27500L,
            montoRecibido = 30000L,
            cambio = 2500L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-t4",
                productId = "prod-12",
                nombre = "Rollo Papel Aluminio 40 Metros",
                cantidad = 1,
                precioVenta = 13500L,
                costoUnitarioSnapshot = 9000L
            ),
            SaleItemEntity(
                saleId = "sale-t4",
                productId = "prod-9",
                nombre = "Bolsa Hermética Tipo Ziploc 18x20cm",
                cantidad = 2,
                precioVenta = 7000L,
                costoUnitarioSnapshot = 4500L
            )
        )

        // Yesterday: full historical closed day
        val ySale1 = SaleEntity(
            id = "sale-y1",
            fecha = yesterday,
            hora = "09:10",
            metodoPago = PaymentMethod.EFECTIVO.name,
            montoTotal = 55500L,
            montoRecibido = 60000L,
            cambio = 4500L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-y1",
                productId = "prod-4",
                nombre = "Portacomidas Plástico 3 Divisiones c/Tapa",
                cantidad = 3,
                precioVenta = 12000L,
                costoUnitarioSnapshot = 8500L
            ),
            SaleItemEntity(
                saleId = "sale-y1",
                productId = "prod-11",
                nombre = "Cuchara Sopera Plástica Blanca",
                cantidad = 3,
                precioVenta = 3500L,
                costoUnitarioSnapshot = 2300L
            ),
            SaleItemEntity(
                saleId = "sale-y1",
                productId = "prod-7",
                nombre = "Bolsa Camiseta Blanca 2kg",
                cantidad = 3,
                precioVenta = 3000L,
                costoUnitarioSnapshot = 1900L
            )
        )

        val ySale2 = SaleEntity(
            id = "sale-y2",
            fecha = yesterday,
            hora = "11:30",
            metodoPago = PaymentMethod.TRANSFERENCIA.name,
            montoTotal = 64000L,
            montoRecibido = 64000L,
            cambio = 0L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-y2",
                productId = "prod-1",
                nombre = "Vaso Plástico Transparente 7oz",
                cantidad = 10,
                precioVenta = 4200L,
                costoUnitarioSnapshot = 2800L
            ),
            SaleItemEntity(
                saleId = "sale-y2",
                productId = "prod-10",
                nombre = "Servilleta Doblada Económica",
                cantidad = 10,
                precioVenta = 2200L,
                costoUnitarioSnapshot = 1400L
            )
        )

        val ySale3 = SaleEntity(
            id = "sale-y3",
            fecha = yesterday,
            hora = "15:05",
            metodoPago = PaymentMethod.EFECTIVO.name,
            montoTotal = 47000L,
            montoRecibido = 50000L,
            cambio = 3000L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-y3",
                productId = "prod-3",
                nombre = "Contenedor Bisagra Icopor J1",
                cantidad = 4,
                precioVenta = 5000L,
                costoUnitarioSnapshot = 3400L
            ),
            SaleItemEntity(
                saleId = "sale-y3",
                productId = "prod-12",
                nombre = "Rollo Papel Aluminio 40 Metros",
                cantidad = 2,
                precioVenta = 13500L,
                costoUnitarioSnapshot = 9000L
            )
        )

        val ySale4 = SaleEntity(
            id = "sale-y4",
            fecha = yesterday,
            hora = "17:40",
            metodoPago = PaymentMethod.TARJETA.name,
            montoTotal = 32500L,
            montoRecibido = 32500L,
            cambio = 0L
        ) to listOf(
            SaleItemEntity(
                saleId = "sale-y4",
                productId = "prod-2",
                nombre = "Vaso Térmico Icopor 8oz",
                cantidad = 5,
                precioVenta = 3500L,
                costoUnitarioSnapshot = 2200L
            ),
            SaleItemEntity(
                saleId = "sale-y4",
                productId = "prod-5",
                nombre = "Plato Pando Desechable #23 Blanco",
                cantidad = 4,
                precioVenta = 3800L,
                costoUnitarioSnapshot = 2500L
            )
        )

        // Note: ySale4 total = 5*3500 + 4*3800 = 17500 + 15200 = 32700! Let's keep exact arithmetic:
        val ySale4Exact = ySale4.first.copy(
            montoTotal = 32700L,
            montoRecibido = 32700L
        ) to ySale4.second

        val expenses = listOf(
            ExpenseEntity(
                id = "exp-t1",
                fecha = today,
                hora = "12:15",
                tipo = ExpenseType.GASTO_OPERATIVO.name,
                categoria = "Domicilios",
                concepto = "Domicilio motorizado",
                monto = 6000L,
                metodoPago = PaymentMethod.EFECTIVO.name,
                proveedorId = null
            ),
            ExpenseEntity(
                id = "exp-y1",
                fecha = yesterday,
                hora = "10:00",
                tipo = ExpenseType.PAGO_PROVEEDOR.name,
                categoria = "Compra de Mercancía",
                concepto = "Reposición urgente bolsas camiseta",
                monto = 57000L,
                metodoPago = PaymentMethod.EFECTIVO.name,
                proveedorId = "sup-1"
            ),
            ExpenseEntity(
                id = "exp-y2",
                fecha = yesterday,
                hora = "13:20",
                tipo = ExpenseType.GASTO_OPERATIVO.name,
                categoria = "Servicios y Varios",
                concepto = "Bolsas internas y cinta de empaque",
                monto = 8500L,
                metodoPago = PaymentMethod.EFECTIVO.name,
                proveedorId = null
            )
        )

        // Yesterday expected cash = 150.000 + (55.500 + 47.000) - (57.000 + 8.500) = 187.000
        val cashSessions = listOf(
            CashSessionEntity(
                fechaActiva = today,
                estado = CashSessionStatus.ABIERTA.name,
                baseInicial = 150000L,
                efectivoContado = null,
                observacionesCierre = null,
                horaCierre = null
            ),
            CashSessionEntity(
                fechaActiva = yesterday,
                estado = CashSessionStatus.CERRADA.name,
                baseInicial = 150000L,
                efectivoContado = 187000L,
                observacionesCierre = "Cierre cuadrado sin novedades. Stock de platos soperos agotado, pedido programado.",
                horaCierre = "19:30"
            )
        )

        return SeedBundle(
            suppliers = suppliers,
            products = products,
            sales = listOf(todaySale1, todaySale2, todaySale3, todaySale4, ySale1, ySale2, ySale3, ySale4Exact),
            expenses = expenses,
            cashSessions = cashSessions
        )
    }
}
