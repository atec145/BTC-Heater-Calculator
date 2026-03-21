package com.example.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.domain.model.HourlyPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceBarChart(
    prices: List<HourlyPrice>,
    breakEvenPrice: Double,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val cheapColor = Color(0xFF4CAF50)
    val expensiveColor = Color(0xFFF44336)
    val lineColor = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (prices.isEmpty()) return@Canvas

        val maxPrice = prices.maxOf { it.priceEurKwh }.coerceAtLeast(0.001)
        val barWidth = size.width / prices.size
        val padding = barWidth * 0.15f

        prices.forEachIndexed { i, point ->
            val barHeight = (point.priceEurKwh / maxPrice * size.height).toFloat().coerceAtLeast(2f)
            val color = if (point.priceEurKwh <= breakEvenPrice) cheapColor else expensiveColor
            drawRect(
                color = color,
                topLeft = Offset(i * barWidth + padding, size.height - barHeight),
                size = Size(barWidth - padding * 2, barHeight)
            )
        }

        // Break-Even Linie
        val lineY = (size.height * (1f - (breakEvenPrice / maxPrice).toFloat())).coerceIn(0f, size.height)
        drawLine(
            color = lineColor,
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2f
        )
    }
}
