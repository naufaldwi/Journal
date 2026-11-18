package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.LifeEngine
import com.example.model.LifeState
import com.example.model.MoodSnapshot
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodTrendsChartCard(
    life: LifeState,
    modifier: Modifier = Modifier
) {
    val points = remember(life) {
        LifeEngine.ensureMoodHistory(life).moodHistory.takeLast(10)
    }
    var selectedIdx by remember(points.size) {
        mutableIntStateOf((points.size - 1).coerceAtLeast(0))
    }
    LaunchedEffect(points.size) {
        selectedIdx = (points.size - 1).coerceAtLeast(0)
    }

    val selectedPoint = points.getOrNull(selectedIdx)
    val encounterCount = remember(points) { points.count { it.hadRandomEncounter } }
    val firstHappy = points.firstOrNull()?.happiness ?: life.stats.happiness
    val lastHappy = points.lastOrNull()?.happiness ?: life.stats.happiness
    val netMoodDelta = lastHappy - firstHappy
    val avgCareerLoad = remember(points) {
        if (points.isEmpty()) 0 else (points.sumOf { it.careerStressLoad } / points.size)
    }

    val happyColor = MaterialTheme.colorScheme.primary
    val stressColor = MaterialTheme.colorScheme.error
    val anxietyColor = MaterialTheme.colorScheme.tertiary
    val careerBarColor = MaterialTheme.colorScheme.secondary
    val encounterMarkerColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceBg = MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mood_trends_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoGraph,
                        contentDescription = "Mood Trends Chart",
                        tint = happyColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "10-Year Mood Trends",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Happiness vs. stress & random encounters",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (points.size <= 1) "Baseline" else "Last ${points.size} yrs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Series Legend
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LegendDot("Happiness", happyColor)
                LegendDot("Stress", stressColor)
                LegendDot("Anxiety", anxietyColor)
                LegendDot("Career Load", careerBarColor.copy(alpha = 0.55f))
                LegendDot("◆ Encounter", encounterMarkerColor)
            }

            if (points.size <= 1) {
                Surface(
                    color = surfaceBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Year ${life.currentYear} Baseline Recorded",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Age forward to watch your 10-year Happiness, Stress, Anxiety, career load, and random encounter impact chart unfold.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Surface(
                    color = surfaceBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .testTag("mood_trends_canvas")
                                    .pointerInput(points.size) {
                                        detectTapGestures { offset ->
                                            if (points.size > 1) {
                                                val leftPad = 14.dp.toPx()
                                                val rightPad = 14.dp.toPx()
                                                val usableW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                                                val relX = ((offset.x - leftPad) / usableW).coerceIn(0f, 1f)
                                                val idx = (relX * (points.size - 1)).roundToInt()
                                                    .coerceIn(0, points.lastIndex)
                                                selectedIdx = idx
                                            }
                                        }
                                    }
                            ) {
                                drawMoodTrendsChart(
                                    points = points,
                                    selectedIdx = selectedIdx,
                                    happyColor = happyColor,
                                    stressColor = stressColor,
                                    anxietyColor = anxietyColor,
                                    careerBarColor = careerBarColor,
                                    encounterMarkerColor = encounterMarkerColor,
                                    gridColor = gridColor
                                )
                            }
                        }

                        // Year & Age Axis Labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val first = points.first()
                            val mid = points[points.size / 2]
                            val last = points.last()
                            Text(
                                text = "${first.year} (${first.age}y)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (points.size >= 4) {
                                Text(
                                    text = "${mid.year} (${mid.age}y)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${last.year} (${last.age}y)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Tap-to-inspect Selected Year Panel
            if (selectedPoint != null) {
                SelectedYearInspector(point = selectedPoint)
            }

            // 10-Year Impact Summary Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatChip(
                    label = "Net Mood",
                    value = if (netMoodDelta >= 0) "+$netMoodDelta" else "$netMoodDelta",
                    isAlert = netMoodDelta < -5,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatChip(
                    label = "Encounters",
                    value = "$encounterCount yr(s)",
                    isAlert = false,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatChip(
                    label = "Career Load",
                    value = when {
                        avgCareerLoad >= 12 -> "High ($avgCareerLoad)"
                        avgCareerLoad >= 7 -> "Med ($avgCareerLoad)"
                        else -> "Calm ($avgCareerLoad)"
                    },
                    isAlert = avgCareerLoad >= 12,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedYearInspector(point: MoodSnapshot) {
    val careerLabel = when {
        point.careerStressLoad >= 13 -> "Intense (${point.careerStressLoad})"
        point.careerStressLoad >= 8 -> "Moderate (${point.careerStressLoad})"
        else -> "Low (${point.careerStressLoad})"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mood_year_inspector")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Age ${point.age} (${point.year})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Career Load: $careerLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (point.careerStressLoad >= 13) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Happy ${point.happiness}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stress ${point.stress}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Anxiety ${point.anxiety}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (point.hadRandomEncounter && point.encounterLabel.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = "Random Encounter Impact",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Encounter: ${point.encounterLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatChip(
    label: String,
    value: String,
    isAlert: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun DrawScope.drawMoodTrendsChart(
    points: List<MoodSnapshot>,
    selectedIdx: Int,
    happyColor: Color,
    stressColor: Color,
    anxietyColor: Color,
    careerBarColor: Color,
    encounterMarkerColor: Color,
    gridColor: Color
) {
    if (points.size < 2) return

    val padX = 12.dp.toPx()
    val padTop = 10.dp.toPx()
    val padBottom = 8.dp.toPx()
    val chartW = (size.width - padX * 2f).coerceAtLeast(1f)
    val chartH = (size.height - padTop - padBottom).coerceAtLeast(1f)

    // Horizontal reference grid lines at 0, 25, 50, 75, 100
    listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { ratio ->
        val y = padTop + chartH * (1f - ratio)
        drawLine(
            color = gridColor.copy(alpha = if (ratio == 0.5f) 0.7f else 0.4f),
            start = Offset(padX, y),
            end = Offset(padX + chartW, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    fun xFor(idx: Int): Float = padX + (idx.toFloat() / points.lastIndex) * chartW
    fun yFor(value: Int): Float = padTop + (1f - (value.coerceIn(0, 100) / 100f)) * chartH

    // Career Stress Load vertical bars along baseline (scaled cleanly to lower 35% of plot)
    val barWidth = (chartW / (points.size * 2.4f)).coerceIn(6.dp.toPx(), 16.dp.toPx())
    points.forEachIndexed { idx, snap ->
        val normalizedLoad = (snap.careerStressLoad / 18f).coerceIn(0f, 1f)
        val barH = normalizedLoad * (chartH * 0.34f)
        if (barH > 1f) {
            drawRoundRect(
                color = careerBarColor.copy(alpha = 0.22f),
                topLeft = Offset(xFor(idx) - barWidth / 2f, padTop + chartH - barH),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }

    // Selected Year Vertical Guide Line
    val clampedSel = selectedIdx.coerceIn(0, points.lastIndex)
    drawLine(
        color = happyColor.copy(alpha = 0.32f),
        start = Offset(xFor(clampedSel), padTop),
        end = Offset(xFor(clampedSel), padTop + chartH),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    )

    // Smooth Happiness Area Fill
    val happyCoords = points.mapIndexed { idx, snap -> Offset(xFor(idx), yFor(snap.happiness)) }
    val areaPath = Path().apply {
        moveTo(happyCoords.first().x, padTop + chartH)
        happyCoords.forEach { pt -> lineTo(pt.x, pt.y) }
        lineTo(happyCoords.last().x, padTop + chartH)
        close()
    }
    drawPath(
        path = areaPath,
        brush = Brush.verticalGradient(
            colors = listOf(happyColor.copy(alpha = 0.26f), happyColor.copy(alpha = 0.02f)),
            startY = padTop,
            endY = padTop + chartH
        )
    )

    // Plot Anxiety (dashed line)
    val anxietyCoords = points.mapIndexed { idx, snap -> Offset(xFor(idx), yFor(snap.anxiety)) }
    val anxietyPath = Path().apply {
        moveTo(anxietyCoords.first().x, anxietyCoords.first().y)
        for (i in 1 until anxietyCoords.size) {
            lineTo(anxietyCoords[i].x, anxietyCoords[i].y)
        }
    }
    drawPath(
        path = anxietyPath,
        color = anxietyColor.copy(alpha = 0.85f),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
    )

    // Plot Stress line
    val stressCoords = points.mapIndexed { idx, snap -> Offset(xFor(idx), yFor(snap.stress)) }
    val stressPath = Path().apply {
        moveTo(stressCoords.first().x, stressCoords.first().y)
        for (i in 1 until stressCoords.size) {
            lineTo(stressCoords[i].x, stressCoords[i].y)
        }
    }
    drawPath(
        path = stressPath,
        color = stressColor,
        style = Stroke(
            width = 2.4.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Plot Happiness primary line
    val happyPath = Path().apply {
        moveTo(happyCoords.first().x, happyCoords.first().y)
        for (i in 1 until happyCoords.size) {
            lineTo(happyCoords[i].x, happyCoords[i].y)
        }
    }
    drawPath(
        path = happyPath,
        color = happyColor,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Data point dots + Random Encounter diamond pins
    points.forEachIndexed { idx, snap ->
        val hPt = happyCoords[idx]
        val sPt = stressCoords[idx]
        val isSel = idx == clampedSel

        drawCircle(
            color = stressColor,
            radius = if (isSel) 4.5.dp.toPx() else 2.5.dp.toPx(),
            center = sPt
        )
        drawCircle(
            color = happyColor,
            radius = if (isSel) 5.5.dp.toPx() else 3.2.dp.toPx(),
            center = hPt
        )

        if (snap.hadRandomEncounter) {
            val dSize = if (isSel) 7.dp.toPx() else 5.5.dp.toPx()
            val diamond = Path().apply {
                moveTo(hPt.x, hPt.y - dSize - 6.dp.toPx())
                lineTo(hPt.x + dSize, hPt.y - 6.dp.toPx())
                lineTo(hPt.x, hPt.y + dSize - 6.dp.toPx())
                lineTo(hPt.x - dSize, hPt.y - 6.dp.toPx())
                close()
            }
            drawPath(path = diamond, color = encounterMarkerColor)
        }
    }
}
