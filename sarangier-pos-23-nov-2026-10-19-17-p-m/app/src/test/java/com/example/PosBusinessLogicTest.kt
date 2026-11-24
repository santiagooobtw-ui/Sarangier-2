package com.example

import com.example.ui.PosUiState
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PosBusinessLogicTest {

    @Test
    fun `formatCOP formats Colombian pesos without decimals and with thousands dot`() {
        assertEquals("$12.500", Formatters.formatCOP(12500L))
        assertEquals("$0", Formatters.formatCOP(0L))
        assertEquals("-$6.000", Formatters.formatCOP(-6000L))
    }

    @Test
    fun `initial POS state starts completely empty at zero`() {
        val initialState = PosUiState()
        assertTrue(initialState.products.isEmpty())
        assertTrue(initialState.suppliers.isEmpty())
        assertTrue(initialState.sales.isEmpty())
        assertTrue(initialState.expenses.isEmpty())
        assertEquals(0L, initialState.todaySession.baseInicial)
        assertEquals(0L, initialState.estimatedCashInDrawerToday)
        assertEquals(0, initialState.totalReferences)
        assertEquals(0L, initialState.totalInventoryCostValue)
    }
}
