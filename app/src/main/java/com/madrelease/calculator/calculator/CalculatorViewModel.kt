package com.madrelease.calculator.calculator

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel(
    private val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

    private val _state = MutableStateFlow(engine.state)
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    // Cờ trạng thái quản lý tính năng lỗi thông qua Remote Config (Mặc định ban đầu = true)
    private val _isBuggyFeatureEnabled = MutableStateFlow(true)
    val isBuggyFeatureEnabled: StateFlow<Boolean> = _isBuggyFeatureEnabled.asStateFlow()

    init {
        setupRemoteConfig()
    }

    private fun setupRemoteConfig() {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // 0 giây để cập nhật tức thì trong buổi Demo
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(mapOf("feature_cloud_sync_enabled" to true))

            fetchRemoteConfig()
        } catch (e: Exception) {
            Log.w("CalculatorViewModel", "Firebase Remote Config not available in current environment: ${e.message}")
        }
    }

    fun fetchRemoteConfig() {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isEnabled = remoteConfig.getBoolean("feature_cloud_sync_enabled")
                    _isBuggyFeatureEnabled.value = isEnabled
                    setCrashlyticsCustomKey("feature_cloud_sync_enabled", isEnabled)
                    Log.i("RemoteConfigHotfix", "Fetched from Firebase Remote Config: $isEnabled")
                } else {
                    toggleFeatureFlagForLocalDemo()
                }
            }
        } catch (e: Exception) {
            Log.w("CalculatorViewModel", "Remote Config offline fallback: ${e.message}")
            toggleFeatureFlagForLocalDemo()
        }
    }

    fun toggleFeatureFlagForLocalDemo() {
        _isBuggyFeatureEnabled.value = !_isBuggyFeatureEnabled.value
        setCrashlyticsCustomKey("feature_cloud_sync_enabled", _isBuggyFeatureEnabled.value)
        Log.i("RemoteConfigHotfix", "Feature flag set to: ${_isBuggyFeatureEnabled.value}")
    }


    fun onAction(action: CalculatorAction) {
        // TỰ ĐỘNG GHI BREADCRUMBS VÀO CRASHLYTICS TRƯỚC KHI THỰC THI LOGIC
        recordBreadcrumb(action)

        // Thực thi tính toán trong Engine
        _state.value = engine.onAction(action)

        // Cập nhật trạng thái hiển thị vào Custom Keys của Crashlytics
        setCrashlyticsCustomKey("last_display_value", _state.value.displayValue)
        setCrashlyticsCustomKey("is_error_state", _state.value.isError)
    }

    private fun recordBreadcrumb(action: CalculatorAction) {
        val logMessage = when (action) {
            is CalculatorAction.Digit -> "User tapped Digit: ${action.value}"
            is CalculatorAction.Operation -> "User tapped Operator: ${action.operator.symbol}"
            is CalculatorAction.Equals -> "User tapped Equals (=)"
            is CalculatorAction.Clear -> "User tapped Clear (AC)"
            is CalculatorAction.Backspace -> "User swiped/tapped Backspace"
            is CalculatorAction.Decimal -> "User tapped Decimal (.)"
            is CalculatorAction.ToggleSign -> "User tapped ToggleSign (+/-)"
            is CalculatorAction.Percent -> "User tapped Percent (%)"
            is CalculatorAction.ClearHistory -> "User cleared history"
            is CalculatorAction.RestoreHistory -> "User restored history: ${action.item.expression}"
        }
        Log.i("CrashlyticsBreadcrumb", logMessage)
        try {
            FirebaseCrashlytics.getInstance().log(logMessage)
        } catch (_: Exception) {}
    }


    private fun setCrashlyticsCustomKey(key: String, value: Any) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            when (value) {
                is String -> crashlytics.setCustomKey(key, value)
                is Boolean -> crashlytics.setCustomKey(key, value)
                is Int -> crashlytics.setCustomKey(key, value)
                is Long -> crashlytics.setCustomKey(key, value)
                is Double -> crashlytics.setCustomKey(key, value)
                is Float -> crashlytics.setCustomKey(key, value)
                else -> crashlytics.setCustomKey(key, value.toString())
            }
        } catch (_: Exception) {
            // Safe fallback when running outside Firebase context
        }
    }

    /**
     * KÍCH HOẠT SỰ CỐ FATAL CRASH CHO PHẦN 3 DEMO:
     * Ghi nhận log cảnh báo chí mạng và gọi engine ném ArithmeticException làm sập tiến trình.
     */
    fun triggerFatalCrashDemo() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                log("CRITICAL: User explicitly tapped 'Cloud Sync (Bug)' button on Calculator Top Bar")
                setCustomKey("crash_trigger_source", "CalculatorScreen_CloudSyncButton")
                setCustomKey("current_expression", _state.value.expression)
                setCustomKey("current_display", _state.value.displayValue)
            }
        } catch (_: Exception) {}

        // Kích hoạt ngoại lệ unhandled trong CalculatorEngine để làm sập app (Process Termination)
        engine.triggerSimulatedEngineCrash()
    }
}
