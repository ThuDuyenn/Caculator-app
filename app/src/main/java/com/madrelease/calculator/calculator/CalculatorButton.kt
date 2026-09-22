package com.madrelease.calculator.calculator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madrelease.calculator.R

enum class CalculatorButtonType {
    DIGIT,
    OPERATOR,
    FUNCTION,
    EQUALS
}

@Composable
fun CalculatorButton(
    symbol: String,
    contentDescription: String,
    type: CalculatorButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (type) {
        CalculatorButtonType.FUNCTION -> Color(0xFFA5A5A5) to Color(0xFF000000)
        CalculatorButtonType.OPERATOR, CalculatorButtonType.EQUALS -> Color(0xFFFF9F0A) to Color(0xFFFFFFFF)
        CalculatorButtonType.DIGIT -> Color(0xFF333333) to Color(0xFFFFFFFF)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (symbol == "CALC_MODE") {
                Icon(
                    painter = painterResource(R.drawable.ic_calc_mode),
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                val fontSize = when {
                    symbol == "+/−" || symbol == "+/-" -> 28.sp
                    symbol == "AC" -> 32.sp
                    symbol == "%" -> 32.sp
                    type == CalculatorButtonType.OPERATOR || type == CalculatorButtonType.EQUALS -> 44.sp
                    else -> 38.sp
                }

                Text(
                    text = if (symbol == "+/−") "+/-" else symbol,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
