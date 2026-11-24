package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppSettings
import com.example.data.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sarangier_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val BUSINESS_SUBTITLE = stringPreferencesKey("business_subtitle")
        val RECEIPT_FOOTER = stringPreferencesKey("receipt_footer")
        val DEFAULT_BASE_INICIAL = longPreferencesKey("default_base_inicial")
        val DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
        val CONFIRM_BEFORE_SALE = booleanPreferencesKey("confirm_before_sale")
        val SHOW_OUT_OF_STOCK_IN_POS = booleanPreferencesKey("show_out_of_stock_in_pos")
        val SHOW_COSTS_MARGINS_IN_INVENTORY = booleanPreferencesKey("show_costs_margins_in_inventory")
        val DEFAULT_MIN_STOCK = intPreferencesKey("default_min_stock")
        val AUTO_OPEN_RECEIPT_ON_CLOSE = booleanPreferencesKey("auto_open_receipt_on_close")
        val MOVEMENTS_STRIP_LIMIT = intPreferencesKey("movements_strip_limit")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val defaults = AppSettings()
            val payMethod = prefs[Keys.DEFAULT_PAYMENT_METHOD]?.let { raw ->
                PaymentMethod.entries.firstOrNull { it.name == raw }
            } ?: defaults.defaultPaymentMethod

            AppSettings(
                businessName = prefs[Keys.BUSINESS_NAME]?.takeIf { it.isNotBlank() } ?: defaults.businessName,
                businessSubtitle = prefs[Keys.BUSINESS_SUBTITLE] ?: defaults.businessSubtitle,
                receiptFooter = prefs[Keys.RECEIPT_FOOTER] ?: defaults.receiptFooter,
                defaultBaseInicial = (prefs[Keys.DEFAULT_BASE_INICIAL] ?: defaults.defaultBaseInicial).coerceAtLeast(0L),
                defaultPaymentMethod = payMethod,
                confirmBeforeSale = prefs[Keys.CONFIRM_BEFORE_SALE] ?: defaults.confirmBeforeSale,
                showOutOfStockInPos = prefs[Keys.SHOW_OUT_OF_STOCK_IN_POS] ?: defaults.showOutOfStockInPos,
                showCostsAndMarginsInInventory = prefs[Keys.SHOW_COSTS_MARGINS_IN_INVENTORY] ?: defaults.showCostsAndMarginsInInventory,
                defaultMinStock = (prefs[Keys.DEFAULT_MIN_STOCK] ?: defaults.defaultMinStock).coerceAtLeast(0),
                autoOpenReceiptOnClose = prefs[Keys.AUTO_OPEN_RECEIPT_ON_CLOSE] ?: defaults.autoOpenReceiptOnClose,
                movementsStripLimit = (prefs[Keys.MOVEMENTS_STRIP_LIMIT] ?: defaults.movementsStripLimit).coerceIn(5, 20)
            )
        }

    suspend fun saveSettings(newSettings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BUSINESS_NAME] = newSettings.businessName.trim().ifBlank { "Distribuidora Sarangier" }
            prefs[Keys.BUSINESS_SUBTITLE] = newSettings.businessSubtitle.trim()
            prefs[Keys.RECEIPT_FOOTER] = newSettings.receiptFooter.trim()
            prefs[Keys.DEFAULT_BASE_INICIAL] = newSettings.defaultBaseInicial.coerceAtLeast(0L)
            prefs[Keys.DEFAULT_PAYMENT_METHOD] = newSettings.defaultPaymentMethod.name
            prefs[Keys.CONFIRM_BEFORE_SALE] = newSettings.confirmBeforeSale
            prefs[Keys.SHOW_OUT_OF_STOCK_IN_POS] = newSettings.showOutOfStockInPos
            prefs[Keys.SHOW_COSTS_MARGINS_IN_INVENTORY] = newSettings.showCostsAndMarginsInInventory
            prefs[Keys.DEFAULT_MIN_STOCK] = newSettings.defaultMinStock.coerceAtLeast(0)
            prefs[Keys.AUTO_OPEN_RECEIPT_ON_CLOSE] = newSettings.autoOpenReceiptOnClose
            prefs[Keys.MOVEMENTS_STRIP_LIMIT] = newSettings.movementsStripLimit.coerceIn(5, 20)
        }
    }

    suspend fun resetToDefaults() {
        context.settingsDataStore.edit { it.clear() }
    }
}
