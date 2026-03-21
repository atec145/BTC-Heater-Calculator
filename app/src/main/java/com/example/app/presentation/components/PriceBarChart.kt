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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
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

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (prices.isEmpty()) return@Canvas

        val labelHeightPx = 36f
        val chartHeight = size.height - labelHeightPx
        val maxPrice = prices.maxOf { it.priceEurKwh }.coerceAtLeast(0.001)
        val barWidth = size.width / prices.size
        val padding = (barWidth * 0.1f).coerceAtMost(6f)

        val paint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        prices.forEachIndexed { i, point ->
            val cal = Calendar.getInstance().apply { timeInMillis = point.epochStart * 1000 }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val isCurrent = hour == currentHour && dayOfYear == currentDayOfYear

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

            // Label bei 0h, 6h, 12h, 18h
            if (hour % 6 == 0) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${hour}h",
                        i * barWidth + barWidth / 2f,
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
