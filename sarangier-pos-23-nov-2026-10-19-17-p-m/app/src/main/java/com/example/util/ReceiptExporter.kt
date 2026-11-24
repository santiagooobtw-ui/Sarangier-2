package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import com.example.data.model.AppSettings
import com.example.data.model.DailySettlementSummary
import java.io.FileOutputStream

object ReceiptExporter {

    fun buildPlainTextReceipt(
        summary: DailySettlementSummary,
        settings: AppSettings = AppSettings()
    ): String {
        val counted = summary.session.efectivoContado ?: 0L
        val diff = counted - summary.expectedCashInDrawer
        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine(settings.businessName.uppercase())
        if (settings.businessSubtitle.isNotBlank()) {
            sb.appendLine(settings.businessSubtitle)
        }
        sb.appendLine("      ACTA DE CIERRE Y LIQUIDACION      ")
        sb.appendLine("========================================")
        sb.appendLine("Fecha: ${summary.date}")
        sb.appendLine("Estado: ${summary.session.estado.label} ${summary.session.horaCierre?.let { "($it)" } ?: ""}")
        sb.appendLine("----------------------------------------")
        sb.appendLine("RESUMEN FINANCIERO")
        sb.appendLine("Ingresos Brutos:     ${Formatters.formatCOP(summary.grossIncome)}")
        sb.appendLine("  - Efectivo:        ${Formatters.formatCOP(summary.cashSales)}")
        sb.appendLine("  - Transferencia:   ${Formatters.formatCOP(summary.transferSales)}")
        sb.appendLine("  - Tarjeta:         ${Formatters.formatCOP(summary.cardSales)}")
        sb.appendLine("Costo Merc. (CMV):   ${Formatters.formatCOP(summary.cmv)}")
        sb.appendLine("Egresos Operativos:  ${Formatters.formatCOP(summary.operatingExpenses)}")
        sb.appendLine("Pagos Proveedores:   ${Formatters.formatCOP(summary.supplierPayments)}")
        sb.appendLine("Utilidad Neta Est.:  ${Formatters.formatCOP(summary.netProfit)}")
        sb.appendLine("----------------------------------------")
        sb.appendLine("ARQUEO DE CAJA")
        sb.appendLine("Base Inicial:        ${Formatters.formatCOP(summary.session.baseInicial)}")
        sb.appendLine("Efectivo Esperado:   ${Formatters.formatCOP(summary.expectedCashInDrawer)}")
        sb.appendLine("Efectivo Contado:    ${Formatters.formatCOP(counted)}")
        sb.appendLine("Diferencia:          ${Formatters.formatSignedCOP(diff)}")
        if (!summary.session.observacionesCierre.isNullOrBlank()) {
            sb.appendLine("Notas: ${summary.session.observacionesCierre}")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("PRODUCTOS VENDIDOS (${summary.consolidatedProducts.size})")
        summary.consolidatedProducts.forEach { row ->
            sb.appendLine("${row.unitsSold}x ${row.productName}")
            sb.appendLine("   Venta: ${Formatters.formatCOP(row.totalSales)} | Util: ${Formatters.formatCOP(row.profitGenerated)}")
        }
        if (settings.receiptFooter.isNotBlank()) {
            sb.appendLine("----------------------------------------")
            sb.appendLine(settings.receiptFooter)
        }
        sb.appendLine("========================================")
        return sb.toString()
    }

    fun buildCsvExport(summary: DailySettlementSummary): String {
        val sb = StringBuilder()
        sb.appendLine("Seccion,Concepto,Unidades,Costo,Venta,Utilidad")
        sb.appendLine("Resumen,Fecha,${summary.date},,,")
        sb.appendLine("Resumen,Ingresos Brutos,${summary.salesCount},,${summary.grossIncome},")
        sb.appendLine("Resumen,Costo Mercancia Vendida,,${summary.cmv},,")
        sb.appendLine("Resumen,Egresos Operativos,${summary.expensesCount},${summary.operatingExpenses},,")
        sb.appendLine("Resumen,Pagos a Proveedores,,${summary.supplierPayments},,")
        sb.appendLine("Resumen,Utilidad Neta Estimada,,,,${summary.netProfit}")
        sb.appendLine("Arqueo,Efectivo Esperado,,,,${summary.expectedCashInDrawer}")
        sb.appendLine("Arqueo,Efectivo Contado,,,,${summary.session.efectivoContado ?: 0L}")
        summary.consolidatedProducts.forEach { row ->
            val safeName = row.productName.replace(",", " ")
            sb.appendLine("Producto,$safeName,${row.unitsSold},${row.totalCost},${row.totalSales},${row.profitGenerated}")
        }
        return sb.toString()
    }

    fun shareClosureSummary(
        context: Context,
        summary: DailySettlementSummary,
        asCsv: Boolean,
        settings: AppSettings = AppSettings()
    ) {
        val content = if (asCsv) buildCsvExport(summary) else buildPlainTextReceipt(summary, settings)
        val subject = if (asCsv) {
            "Cierre_${summary.date}.csv"
        } else {
            "Acta de Cierre ${settings.businessName} - ${summary.date}"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (asCsv) "text/csv" else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Exportar Cierre de Caja").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun printClosureReceipt(
        context: Context,
        summary: DailySettlementSummary,
        settings: AppSettings = AppSettings()
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val lines = buildPlainTextReceipt(summary, settings).lines()
        val jobName = "Cierre_${summary.date}"

        val adapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PrintedPdfDocument(context, newAttributes)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val doc = pdfDocument ?: return
                val page = doc.startPage(0)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    doc.close()
                    return
                }
                drawReceiptOnCanvas(page.canvas, lines)
                doc.finishPage(page)
                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        doc.writeTo(out)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    doc.close()
                    pdfDocument = null
                }
            }
        }

        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }

    private fun drawReceiptOnCanvas(canvas: Canvas, lines: List<String>) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        var y = 40f
        val x = 36f
        lines.forEach { line ->
            canvas.drawText(line, x, y, paint)
            y += 16f
        }
    }
}
