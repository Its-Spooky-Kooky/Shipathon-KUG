package com.eta.assistant.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "eta_user_preferences")

data class UserPreferences(
    val businessName: String,
    val ownerName: String,
    val businessCategory: String,
    val currencySymbol: String,
    val defaultLowStockThreshold: Double,
    val themeMode: String, // "SYSTEM", "LIGHT", "DARK"
    val languageCode: String, // "en", "hi", "ta", "bn"
    val notificationsEnabled: Boolean,
    val subscriptionPlan: String, // "FREE", "PLUS", "BUSINESS"
    val isOnboarded: Boolean,
    val lastSyncTimestamp: Long
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val BUSINESS_CATEGORY = stringPreferencesKey("business_category")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val LOW_STOCK_THRESHOLD = doublePreferencesKey("low_stock_threshold")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SUBSCRIPTION_PLAN = stringPreferencesKey("subscription_plan")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            businessName = preferences[PreferencesKeys.BUSINESS_NAME] ?: "Ravi Store",
            ownerName = preferences[PreferencesKeys.OWNER_NAME] ?: "Ravi Kumar",
            businessCategory = preferences[PreferencesKeys.BUSINESS_CATEGORY] ?: "Groceries",
            currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "₹",
            defaultLowStockThreshold = preferences[PreferencesKeys.LOW_STOCK_THRESHOLD] ?: 5.0,
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            languageCode = preferences[PreferencesKeys.LANGUAGE_CODE] ?: "en",
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            subscriptionPlan = preferences[PreferencesKeys.SUBSCRIPTION_PLAN] ?: "FREE",
            isOnboarded = preferences[PreferencesKeys.IS_ONBOARDED] ?: false,
            lastSyncTimestamp = preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: System.currentTimeMillis()
        )
    }

    suspend fun updateOnboarding(
        businessName: String,
        ownerName: String,
        category: String,
        currency: String,
        threshold: Double
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUSINESS_NAME] = businessName
            preferences[PreferencesKeys.OWNER_NAME] = ownerName
            preferences[PreferencesKeys.BUSINESS_CATEGORY] = category
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = currency
            preferences[PreferencesKeys.LOW_STOCK_THRESHOLD] = threshold
            preferences[PreferencesKeys.IS_ONBOARDED] = true
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSubscriptionPlan(plan: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_PLAN] = plan
        }
    }

    suspend fun updateBusinessInfo(businessName: String, ownerName: String, category: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUSINESS_NAME] = businessName
            preferences[PreferencesKeys.OWNER_NAME] = ownerName
            preferences[PreferencesKeys.BUSINESS_CATEGORY] = category
        }
    }

    suspend fun updateLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
