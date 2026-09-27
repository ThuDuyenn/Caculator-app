package com.madrelease.calculator.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madrelease.calculator.calculator.CalculatorScreen
import com.madrelease.calculator.calculator.CalculatorViewModel

// Official iOS Dark Theme
private val IosDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9F0A), // Apple Orange
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFF9F0A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFA5A5A5), // Apple Light Gray
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFA5A5A5),
    onSecondaryContainer = Color(0xFF000000),
    surfaceVariant = Color(0xFF333333), // Apple Dark Gray
    onSurfaceVariant = Color(0xFFFFFFFF),
    background = Color(0xFF000000), // Pure OLED Black
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C1E), // Apple System Gray 6 (Dark)
    onSurface = Color(0xFFFFFFFF),
    outline = Color(0xFF38383A)
)

@Composable
fun CalculatorApp(
    calculatorViewModel: CalculatorViewModel = viewModel()
) {
    MaterialTheme(
        colorScheme = IosDarkColorScheme
    ) {
        val state by calculatorViewModel.state.collectAsState()
        val isBuggyFeatureEnabled by calculatorViewModel.isBuggyFeatureEnabled.collectAsState()

        CalculatorScreen(
            state = state,
            onAction = calculatorViewModel::onAction,
            isBuggyFeatureEnabled = isBuggyFeatureEnabled,
            onTriggerCrash = calculatorViewModel::triggerFatalCrashDemo,
            onRefreshConfig = calculatorViewModel::fetchRemoteConfig
        )
    }
}

