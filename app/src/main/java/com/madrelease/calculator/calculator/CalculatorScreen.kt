package com.madrelease.calculator.calculator

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madrelease.calculator.R
import kotlin.math.abs

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    isBuggyFeatureEnabled: Boolean = true,
    onTriggerCrash: () -> Unit = {},
    onRefreshConfig: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF000000) // True OLED Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // iOS Top Bar: History list icon bên trái + Khu vực Demo Hotfix bên phải
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút History bên trái
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    if (state.history.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFFF9F0A),
                                    contentColor = Color(0xFF000000)
                                ) {
                                    Text(
                                        text = if (state.history.size > 99) "99+" else state.history.size.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_ios_history),
                                contentDescription = stringResource(R.string.history_button_description),
                                tint = Color(0xFFFF9F0A),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_ios_history),
                            contentDescription = stringResource(R.string.history_button_description),
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // KHU VỰC THỬ NGHIỆM HOTFIX & CRASHLYTICS (PHẦN 3 DEMO)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isBuggyFeatureEnabled) {
                        // KHI CỜ REMOTE CONFIG = TRUE: Nút tính năng mới gặp lỗi Fatal Crash
                        Button(
                            onClick = onTriggerCrash,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "⚡ Cloud Sync (Bug)",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // KHI CỜ REMOTE CONFIG = FALSE: Nút đã bị vô hiệu hóa an toàn bởi Kill-Switch!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF30D158).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF30D158)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🛡️ Hotfixed (Safe Mode)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF30D158),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Nút cập nhật tức thì Remote Config để Duyên thao tác trực tiếp trên màn hình demo
                    IconButton(
                        onClick = onRefreshConfig,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Remote Config",
                            tint = Color(0xFFA5A5A5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }


            // Display Area with swipe-to-backspace gesture
            var totalDrag by remember { mutableFloatStateOf(0f) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (abs(totalDrag) > 60f) {
                                    onAction(CalculatorAction.Backspace)
                                    totalDrag = 0f
                                }
                            }
                        )
                    },
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                if (state.expression.isNotEmpty()) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(state.expression) {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                    val expressionFontSize = when {
                        state.expression.length > 30 -> 18.sp
                        state.expression.length > 22 -> 22.sp
                        state.expression.length > 16 -> 25.sp
                        else -> 28.sp
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = state.expression,
                            fontSize = expressionFontSize,
                            fontWeight = FontWeight.Light,
                            color = Color(0xFF8E8E93),
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                val fontSize = when {
                    state.displayValue.length > 12 -> 38.sp
                    state.displayValue.length > 9 -> 50.sp
                    state.displayValue.length > 7 -> 62.sp
                    state.displayValue.length > 5 -> 74.sp
                    else -> 86.sp
                }

                Text(
                    text = state.displayValue,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Light,
                    color = if (state.isError) Color(0xFFFF453A) else Color(0xFFFFFFFF),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.error_recovery),
                        fontSize = 13.sp,
                        color = Color(0xFFFF453A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // iOS Circular Keypad Layout (4 columns x 5 rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1: AC, +/-, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalculatorButton(
                        symbol = "AC",
                        contentDescription = stringResource(R.string.clear),
                        type = CalculatorButtonType.FUNCTION,
                        onClick = { onAction(CalculatorAction.Clear) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "+/-",
                        contentDescription = stringResource(R.string.toggle_sign),
                        type = CalculatorButtonType.FUNCTION,
                        onClick = { onAction(CalculatorAction.ToggleSign) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "%",
                        contentDescription = stringResource(R.string.percent),
                        type = CalculatorButtonType.FUNCTION,
                        onClick = { onAction(CalculatorAction.Percent) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "÷",
                        contentDescription = stringResource(R.string.divide),
                        type = CalculatorButtonType.OPERATOR,
                        onClick = { onAction(CalculatorAction.Operation(CalculatorOperator.DIVIDE)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalculatorButton(
                        symbol = "7",
                        contentDescription = "7",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(7)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "8",
                        contentDescription = "8",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(8)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "9",
                        contentDescription = "9",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(9)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "×",
                        contentDescription = stringResource(R.string.multiply),
                        type = CalculatorButtonType.OPERATOR,
                        onClick = { onAction(CalculatorAction.Operation(CalculatorOperator.MULTIPLY)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: 4, 5, 6, −
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalculatorButton(
                        symbol = "4",
                        contentDescription = "4",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(4)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "5",
                        contentDescription = "5",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(5)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "6",
                        contentDescription = "6",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(6)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "−",
                        contentDescription = stringResource(R.string.subtract),
                        type = CalculatorButtonType.OPERATOR,
                        onClick = { onAction(CalculatorAction.Operation(CalculatorOperator.SUBTRACT)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalculatorButton(
                        symbol = "1",
                        contentDescription = "1",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(1)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "2",
                        contentDescription = "2",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(2)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "3",
                        contentDescription = "3",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(3)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "+",
                        contentDescription = stringResource(R.string.add),
                        type = CalculatorButtonType.OPERATOR,
                        onClick = { onAction(CalculatorAction.Operation(CalculatorOperator.ADD)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: CALC_MODE (Backspace), 0, ,, =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalculatorButton(
                        symbol = "CALC_MODE",
                        contentDescription = stringResource(R.string.backspace),
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Backspace) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "0",
                        contentDescription = "0",
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Digit(0)) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = ",",
                        contentDescription = stringResource(R.string.decimal),
                        type = CalculatorButtonType.DIGIT,
                        onClick = { onAction(CalculatorAction.Decimal) },
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        symbol = "=",
                        contentDescription = stringResource(R.string.equals),
                        type = CalculatorButtonType.EQUALS,
                        onClick = { onAction(CalculatorAction.Equals) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // History Bottom Sheet
        if (showHistorySheet) {
            HistoryBottomSheet(
                history = state.history,
                onDismiss = { showHistorySheet = false },
                onSelectHistoryItem = { item ->
                    onAction(CalculatorAction.RestoreHistory(item))
                    showHistorySheet = false
                },
                onClearHistory = {
                    onAction(CalculatorAction.ClearHistory)
                }
            )
        }
    }
}
