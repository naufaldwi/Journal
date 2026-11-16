package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AvatarAppearance

object AvatarPalettes {
    val skinTones = listOf(
        Color(0xFFF6DEC9),
        Color(0xFFEBC3A2),
        Color(0xFFD7A37B),
        Color(0xFFB57C54),
        Color(0xFF8C583A),
        Color(0xFF5D3A27)
    )

    val hairColors = listOf(
        Color(0xFF282423),
        Color(0xFF5C4033),
        Color(0xFFB67647),
        Color(0xFFD6B57A),
        Color(0xFFA85745),
        Color(0xFF9EA4A8)
    )

    val outfitColors = listOf(
        Color(0xFF4A6B5D),
        Color(0xFFC27D63),
        Color(0xFF7E7B99),
        Color(0xFF4E5B66),
        Color(0xFFC89B54)
    )

    val backdrops = listOf(
        Color(0xFFE4ECE7),
        Color(0xFFF5E6DF),
        Color(0xFFE6E4EE),
        Color(0xFFF2EBDC),
        Color(0xFFDCE7EA)
    )

    val hairStyleLabels = listOf("Classic Part", "Soft Waves", "Short Crop", "Neat Bun", "Textured Curls", "Bob Cut")
    val accessoryLabels = listOf("None", "Round Glasses", "Square Frames", "Warm Smile")
}

@Composable
fun CharacterAvatar(
    appearance: AvatarAppearance,
    age: Int,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    health: Int = 88,
    fame: Int = 0,
    isInPrison: Boolean = false
) {
    val rawSkin = AvatarPalettes.skinTones[appearance.skinToneIndex.coerceIn(0, AvatarPalettes.skinTones.lastIndex)]
    val skin = if (health < 35) lerp(rawSkin, Color(0xFFDAD7D2), 0.25f) else rawSkin

    val baseHair = AvatarPalettes.hairColors[appearance.hairColorIndex.coerceIn(0, AvatarPalettes.hairColors.lastIndex)]
    val hair = when {
        age >= 80 -> Color(0xFFDDE1E4)
        age >= 60 -> lerp(baseHair, Color(0xFFB9BDC0), ((age - 58) / 22f).coerceIn(0.45f, 1f))
        age >= 45 -> lerp(baseHair, Color(0xFF92989C), ((age - 44) / 20f).coerceIn(0f, 0.45f))
        else -> baseHair
    }

    val baseOutfit = AvatarPalettes.outfitColors[appearance.outfitIndex.coerceIn(0, AvatarPalettes.outfitColors.lastIndex)]
    val outfit = when {
        isInPrison -> Color(0xFF5D666E)
        age < 6 -> lerp(baseOutfit, Color(0xFFF3EFE8), 0.35f)
        age in 6..12 -> lerp(baseOutfit, Color(0xFF5B768A), 0.22f)
        age >= 60 -> lerp(baseOutfit, Color(0xFF6A6158), 0.25f)
        else -> baseOutfit
    }

    val backdrop = AvatarPalettes.backdrops[appearance.backdropIndex.coerceIn(0, AvatarPalettes.backdrops.lastIndex)]
    val borderColor = when {
        isInPrison -> Color(0xFF6E767E)
        fame >= 60 -> Color(0xFFD6A84E)
        else -> MaterialTheme.colorScheme.outline
    }
    val borderWidth = if (fame >= 60) 2.5.dp else 1.5.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backdrop)
            .border(borderWidth, borderColor, CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f

            // Subtle botanical or spotlight halo
            drawCircle(
                color = if (fame >= 60) Color(0xFFFFF4D2).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.35f),
                radius = w * 0.36f,
                center = Offset(cx, h * 0.38f)
            )

            // Back hair layer for longer styles (lighter tuft in infancy)
            if (age >= 3) {
                when (appearance.hairStyleIndex % 6) {
                    1, 5 -> {
                        drawRoundRect(
                            color = hair,
                            topLeft = Offset(cx - w * 0.27f, h * 0.24f),
                            size = Size(w * 0.54f, h * 0.44f),
                            cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
                        )
                    }
                    3 -> {
                        drawCircle(
                            color = hair,
                            radius = w * 0.12f,
                            center = Offset(cx, h * 0.14f)
                        )
                    }
                }
            }

            // Shoulders / Age-Milestone Outfit Silhouette
            val shoulderTopY = if (age < 6) h * 0.76f else h * 0.72f
            val shoulderPath = Path().apply {
                moveTo(w * 0.14f, h * 1.02f)
                quadraticTo(w * 0.18f, shoulderTopY, cx, shoulderTopY)
                quadraticTo(w * 0.82f, shoulderTopY, w * 0.86f, h * 1.02f)
                close()
            }
            drawPath(shoulderPath, color = outfit)

            // Milestone Collar / Lapel details
            when {
                isInPrison -> {
                    // Striped prison collar band
                    drawLine(
                        color = Color(0xFFDFE3E6),
                        start = Offset(w * 0.28f, h * 0.84f),
                        end = Offset(w * 0.72f, h * 0.84f),
                        strokeWidth = w * 0.03f
                    )
                    drawLine(
                        color = Color(0xFFDFE3E6),
                        start = Offset(w * 0.24f, h * 0.93f),
                        end = Offset(w * 0.76f, h * 0.93f),
                        strokeWidth = w * 0.03f
                    )
                }
                age < 6 -> {
                    // Cozy rounded baby bib
                    drawCircle(
                        color = Color(0xFFFAF7F2),
                        radius = w * 0.14f,
                        center = Offset(cx, h * 0.82f)
                    )
                }
                age in 6..17 -> {
                    // Youth crisp collar
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(cx - w * 0.10f, shoulderTopY + h * 0.02f),
                        end = Offset(cx, shoulderTopY + h * 0.12f),
                        strokeWidth = w * 0.024f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(cx + w * 0.10f, shoulderTopY + h * 0.02f),
                        end = Offset(cx, shoulderTopY + h * 0.12f),
                        strokeWidth = w * 0.024f,
                        cap = StrokeCap.Round
                    )
                }
                age >= 60 -> {
                    // Warm cardigan / shawl neckline
                    drawArc(
                        color = Color(0xFFE7DFD3).copy(alpha = 0.85f),
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(cx - w * 0.18f, shoulderTopY - h * 0.04f),
                        size = Size(w * 0.36f, h * 0.20f),
                        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                    )
                }
                age >= 35 -> {
                    // Midlife structured lapel V
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = Offset(cx - w * 0.11f, shoulderTopY + h * 0.01f),
                        end = Offset(cx, shoulderTopY + h * 0.17f),
                        strokeWidth = w * 0.022f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = Offset(cx + w * 0.11f, shoulderTopY + h * 0.01f),
                        end = Offset(cx, shoulderTopY + h * 0.17f),
                        strokeWidth = w * 0.022f
                    )
                }
            }

            // Neck
            drawRoundRect(
                color = skin.copy(alpha = 0.92f),
                topLeft = Offset(cx - w * 0.08f, h * 0.56f),
                size = Size(w * 0.16f, h * 0.22f),
                cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
            )

            // Head proportions evolve with age milestone
            val headR = when {
                age < 6 -> w * 0.245f
                age < 13 -> w * 0.232f
                else -> w * 0.22f
            }
            val headCy = when {
                age < 6 -> h * 0.45f
                age < 13 -> h * 0.43f
                else -> h * 0.41f
            }
            drawCircle(
                color = skin,
                radius = headR,
                center = Offset(cx, headCy)
            )

            // Youthful rosy cheeks (Ages 0..15)
            if (age <= 15 && health >= 45) {
                val blushColor = Color(0xFFE78E82).copy(alpha = if (age < 6) 0.38f else 0.24f)
                drawCircle(blushColor, radius = headR * 0.17f, center = Offset(cx - headR * 0.52f, headCy + headR * 0.22f))
                drawCircle(blushColor, radius = headR * 0.17f, center = Offset(cx + headR * 0.52f, headCy + headR * 0.22f))
            }

            // Front Hair (baby sprout curl under age 3, fuller hair from 3+)
            if (age < 3) {
                drawArc(
                    color = hair,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(cx - headR * 0.35f, headCy - headR * 1.12f),
                    size = Size(headR * 0.7f, headR * 0.45f),
                    style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
                )
            } else {
                when (appearance.hairStyleIndex % 6) {
                    0 -> drawArc(
                        color = hair,
                        startAngle = 185f,
                        sweepAngle = 170f,
                        useCenter = true,
                        topLeft = Offset(cx - headR * 1.04f, headCy - headR * 1.08f),
                        size = Size(headR * 2.08f, headR * 1.35f)
                    )
                    1 -> drawArc(
                        color = hair,
                        startAngle = 170f,
                        sweepAngle = 200f,
                        useCenter = true,
                        topLeft = Offset(cx - headR * 1.12f, headCy - headR * 1.1f),
                        size = Size(headR * 2.24f, headR * 1.35f)
                    )
                    2 -> drawArc(
                        color = hair,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = true,
                        topLeft = Offset(cx - headR * 1.02f, headCy - headR * 1.05f),
                        size = Size(headR * 2.04f, headR * 1.1f)
                    )
                    3 -> drawArc(
                        color = hair,
                        startAngle = 185f,
                        sweepAngle = 170f,
                        useCenter = true,
                        topLeft = Offset(cx - headR * 1.03f, headCy - headR * 1.04f),
                        size = Size(headR * 2.06f, headR * 1.15f)
                    )
                    4 -> {
                        for (offset in listOf(-0.14f, -0.05f, 0.05f, 0.14f)) {
                            drawCircle(
                                color = hair,
                                radius = headR * 0.36f,
                                center = Offset(cx + w * offset, headCy - headR * 0.78f)
                            )
                        }
                    }
                    else -> drawArc(
                        color = hair,
                        startAngle = 175f,
                        sweepAngle = 190f,
                        useCenter = true,
                        topLeft = Offset(cx - headR * 1.08f, headCy - headR * 1.06f),
                        size = Size(headR * 2.16f, headR * 1.25f)
                    )
                }
            }

            // Eyes
            val eyeColor = Color(0xFF232824)
            val eyeY = headCy + headR * 0.05f
            val eyeDx = headR * 0.38f
            val eyeR = if (age < 6) w * 0.026f else w * 0.022f
            drawCircle(eyeColor, radius = eyeR, center = Offset(cx - eyeDx, eyeY))
            drawCircle(eyeColor, radius = eyeR, center = Offset(cx + eyeDx, eyeY))

            // Low-health under-eye fatigue hint
            if (health < 35) {
                val tiredColor = Color(0xFF6B5D58).copy(alpha = 0.45f)
                drawArc(
                    color = tiredColor,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeDx - headR * 0.14f, eyeY + headR * 0.02f),
                    size = Size(headR * 0.28f, headR * 0.12f),
                    style = Stroke(width = w * 0.012f)
                )
                drawArc(
                    color = tiredColor,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeDx - headR * 0.14f, eyeY + headR * 0.02f),
                    size = Size(headR * 0.28f, headR * 0.12f),
                    style = Stroke(width = w * 0.012f)
                )
            }

            // Age-milestone wisdom/smile lines (Midlife 45+, Senior 60+)
            if (age >= 45) {
                val lineAlpha = if (age >= 65) 0.34f else 0.18f
                val lineColor = Color(0xFF6C4F43).copy(alpha = lineAlpha)
                // Gentle crow's feet / smile creases
                drawArc(
                    color = lineColor,
                    startAngle = 110f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(cx - headR * 0.42f, headCy + headR * 0.16f),
                    size = Size(headR * 0.24f, headR * 0.36f),
                    style = Stroke(width = w * 0.011f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = lineColor,
                    startAngle = 10f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(cx + headR * 0.18f, headCy + headR * 0.16f),
                    size = Size(headR * 0.24f, headR * 0.36f),
                    style = Stroke(width = w * 0.011f, cap = StrokeCap.Round)
                )
            }
            if (age >= 65) {
                val browLine = Color(0xFF6C4F43).copy(alpha = 0.22f)
                drawLine(
                    color = browLine,
                    start = Offset(cx - headR * 0.26f, headCy - headR * 0.28f),
                    end = Offset(cx + headR * 0.26f, headCy - headR * 0.28f),
                    strokeWidth = w * 0.01f,
                    cap = StrokeCap.Round
                )
            }

            // Expression (Gentle smile, muted when low health)
            val smileSweep = if (health < 35) 80f else 140f
            val smileStart = if (health < 35) 50f else 20f
            drawArc(
                color = Color(0xFF7B4D42),
                startAngle = smileStart,
                sweepAngle = smileSweep,
                useCenter = false,
                topLeft = Offset(cx - headR * 0.25f, headCy + headR * 0.22f),
                size = Size(headR * 0.5f, headR * 0.32f),
                style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
            )

            // Accessories (Glasses - plus reading glasses option auto-appearing at 65+ if Accessory 0)
            val effectiveAccessory = if (appearance.accessoryIndex % 4 == 0 && age >= 68) 1 else appearance.accessoryIndex % 4
            if (effectiveAccessory == 1 && age >= 5) {
                val frameColor = Color(0xFF3E443F)
                drawCircle(
                    color = frameColor,
                    radius = headR * 0.28f,
                    center = Offset(cx - eyeDx, eyeY),
                    style = Stroke(width = w * 0.018f)
                )
                drawCircle(
                    color = frameColor,
                    radius = headR * 0.28f,
                    center = Offset(cx + eyeDx, eyeY),
                    style = Stroke(width = w * 0.018f)
                )
                drawLine(
                    color = frameColor,
                    start = Offset(cx - eyeDx + headR * 0.28f, eyeY),
                    end = Offset(cx + eyeDx - headR * 0.28f, eyeY),
                    strokeWidth = w * 0.018f
                )
            } else if (effectiveAccessory == 2 && age >= 5) {
                val frameColor = Color(0xFF6B5344)
                val boxS = headR * 0.48f
                drawRoundRect(
                    color = frameColor,
                    topLeft = Offset(cx - eyeDx - boxS / 2f, eyeY - boxS / 2.3f),
                    size = Size(boxS, boxS * 0.85f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = w * 0.018f)
                )
                drawRoundRect(
                    color = frameColor,
                    topLeft = Offset(cx + eyeDx - boxS / 2f, eyeY - boxS / 2.3f),
                    size = Size(boxS, boxS * 0.85f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = w * 0.018f)
                )
                drawLine(
                    color = frameColor,
                    start = Offset(cx - eyeDx + boxS / 2f, eyeY),
                    end = Offset(cx + eyeDx - boxS / 2f, eyeY),
                    strokeWidth = w * 0.018f
                )
            }
        }
    }
}
