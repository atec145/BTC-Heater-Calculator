package com.example.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.domain.model.HourlyPrice
import java.util.Calendar

@Composable
fun PriceBarChart(
    prices: List<HourlyPrice>,
    breakEvenPrice: Double,
    modifier: Modifier = Modifier
) {
    val cheapColor = Color(0xFF4CAF50)
    val expensiveColor = Color(0xFFF44336)
    val currentBarColor = Color(0xFFFFEB3B)
    val lineColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    // Nur die nächsten 24h ab Mitternacht heute
    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
    }
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val todayPrices = remember(prices) {
        prices
            .filter { it.epochStart >= todayMidnight && it.epochStart < todayMidnight + 86400 }
            .sortedBy { it.epochStart }
    }

    val displayPrices = todayPrices.ifEmpty { prices.take(24) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (displayPrices.isEmpty()) return@Canvas

        val labelHeightPx = 36f
        val chartHeight = size.height - labelHeightPx
        val maxPrice = displayPrices.maxOf { it.priceEurKwh }.coerceAtLeast(0.001)
        val barWidth = size.width / displayPrices.size
        val padding = barWidth * 0.12f

        val paint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        displayPrices.forEachIndexed { i, point ->
            val cal = Calendar.getInstance().apply { timeInMillis = point.epochStart * 1000 }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val isCurrent = hour == currentHour

            val barHeight = (point.priceEurKwh / maxPrice * chartHeight).toFloat().coerceAtLeast(2f)
            val color = when {
                isCurrent -> currentBarColor
                point.priceEurKwh <= breakEvenPrice -> cheapColor
                else -> expensiveColor
            }

            drawRect(
                color = color,
                topLeft = Offset(i * barWidth + padding, chartHeight - barHeight),
                size = Size(barWidth - padding * 2, barHeight)
            )

            // X-Achsen-Label alle 6 Stunden (0, 6, 12, 18) + letzte Stunde
            if (hour % 6 == 0) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${hour}h",
                        i * barWidth + barWidth / 2,
                        size.height - 4f,
                        paint
                    )
                }
            }
        }

        // Break-Even Linie
        val lineY = (chartHeight * (1f - (breakEvenPrice / maxPrice).toFloat())).coerceIn(0f, chartHeight)
        drawLine(
            color = lineColor,
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2f
        )
    }
}
