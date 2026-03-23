package com.example.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.app.domain.model.DifficultyEpoch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DifficultyLineChart(
    epochs: List<DifficultyEpoch>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val dateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (epochs.size < 2) return@Canvas

        val labelHeightPx = 40f
        val paddingLeft = 80f
        val paddingRight = 16f
        val chartHeight = size.height - labelHeightPx
        val chartWidth = size.width - paddingLeft - paddingRight

        val minDiff = epochs.minOf { it.difficulty }
        val maxDiff = epochs.maxOf { it.difficulty }
        val diffRange = (maxDiff - minDiff).coerceAtLeast(1.0)

        fun xOf(i: Int) = paddingLeft + i * (chartWidth / (epochs.size - 1))
        fun yOf(d: Double) = (chartHeight * (1.0 - (d - minDiff) / diffRange)).toFloat().coerceIn(0f, chartHeight)

        // Horizontale Gridlinien (3 Stück)
        for (step in 0..2) {
            val y = chartHeight * step / 2f
            drawLine(color = gridColor, start = Offset(paddingLeft, y), end = Offset(size.width - paddingRight, y), strokeWidth = 1f)
        }

        // Y-Achsen-Labels (Billionen)
        val paint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 24f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
        for (step in 0..2) {
            val value = minDiff + diffRange * (1.0 - step / 2.0)
            val y = chartHeight * step / 2f
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "%.0fT".format(value / 1_000_000_000_000.0),
                    paddingLeft - 8f,
                    y + 8f,
                    paint
                )
            }
        }

        // Linie
        val path = Path()
        epochs.forEachIndexed { i, epoch ->
            val x = xOf(i)
            val y = yOf(epoch.difficulty)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))

        // Datenpunkte + X-Labels
        val labelPaint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 22f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        epochs.forEachIndexed { i, epoch ->
            val x = xOf(i)
            val y = yOf(epoch.difficulty)
            drawCircle(color = dotColor, radius = 6f, center = Offset(x, y))

            // X-Label bei jedem 2. Punkt um Überlappung zu vermeiden
            if (i % 2 == 0 || i == epochs.lastIndex) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        dateFormat.format(Date(epoch.timestamp * 1000)),
                        x,
                        size.height - 4f,
                        labelPaint
                    )
                }
            }
        }
    }
}
