package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.LifeState
import com.example.model.Relationship

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfficeProfessionalDynamicCard(
    life: LifeState,
    onInteractRelationship: (String, String) -> Unit,
    onMeetProfessionalContact: () -> Unit
) {
    val contacts = remember(life.relationships) {
        life.schoolWorkBonds.sortedWith(compareBy<Relationship> { it.closeness }.thenBy { it.name })
    }
    val standing = life.professionalStanding
    val isStudentPhase = life.currentAge < 18 || life.inSchool
    val contextLabel = if (isStudentPhase) "School & Teacher Dynamic" else "Office / Professional Dynamic"
    val subContext = if (isStudentPhase) {
        "Classmates, teachers & campus mentors"
    } else {
        "Colleagues, managers & studio producers"
    }
    val proHateCount = contacts.count { it.closeness < 25 }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("school_office_standing_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isStudentPhase) Icons.Outlined.School else Icons.Outlined.WorkOutline,
                                contentDescription = contextLabel,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(contextLabel, style = MaterialTheme.typography.titleMedium)
                        Text(
                            subContext,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = when {
                        standing >= 72 -> MaterialTheme.colorScheme.primaryContainer
                        standing < 25 -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (contacts.isEmpty()) "No Bonds" else "$standing/100",
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            standing >= 72 -> MaterialTheme.colorScheme.onPrimaryContainer
                            standing < 25 -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Aggregate standing meter & career impact pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (standing / 100f).coerceIn(0f, 1f) },
                    color = when {
                        standing < 25 -> MaterialTheme.colorScheme.error
                        standing >= 72 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val promoText = when {
                        standing >= 78 -> "Promotion: +65%"
                        standing < 35 -> "Promotion: Stalled"
                        else -> "Promotion: 45%"
                    }
                    val raiseText = when {
                        standing >= 72 -> "Raise: +10% Bonus"
                        standing < 25 -> "Raise: Frozen"
                        else -> "Raise: Standard"
                    }
                    val riskText = if (standing < 22) "Risk: Termination" else "Job Security: Stable"

                    ImpactChip(promoText, highlightBad = standing < 35, highlightGood = standing >= 78)
                    ImpactChip(raiseText, highlightBad = standing < 25, highlightGood = standing >= 72)
                    ImpactChip(riskText, highlightBad = standing < 22, highlightGood = false)
                }
            }

            // Clear "Life Miserable" Status Visual Indicator
            LifeMiserableStatusPanel(
                totalHateCount = life.hateCount,
                proHateCount = proHateCount,
                isMiserable = life.isMiserableFromHatred
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Current Professional / School Bonds List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Bonds (${contacts.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                if (contacts.isNotEmpty()) {
                    Text(
                        text = "Below 25% hurts peace",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (contacts.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isStudentPhase) {
                                "No classmates or teachers recorded yet. Meet peers to improve school rapport."
                            } else {
                                "No colleagues, managers, or producers yet. Start a career or meet a workplace contact."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (life.isAlive && life.currentAge >= 6) {
                            OutlinedButton(
                                onClick = onMeetProfessionalContact,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("meet_professional_contact_button")
                            ) {
                                Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(if (isStudentPhase) "Meet Classmate / Teacher" else "Meet Colleague / Manager")
                            }
                        }
                    }
                }
            } else {
                contacts.forEach { rel ->
                    ProfessionalBondRow(
                        rel = rel,
                        canInteract = life.isAlive,
                        onCheckIn = { onInteractRelationship(rel.id, "time") },
                        onMakeAmends = { onInteractRelationship(rel.id, "apologize") }
                    )
                }

                if (life.isAlive && life.currentAge >= 6) {
                    OutlinedButton(
                        onClick = onMeetProfessionalContact,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("meet_professional_contact_button")
                    ) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(if (isStudentPhase) "Meet Classmate / Teacher" else "Meet Colleague / Manager")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactChip(
    text: String,
    highlightBad: Boolean,
    highlightGood: Boolean
) {
    val bg = when {
        highlightBad -> MaterialTheme.colorScheme.errorContainer
        highlightGood -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        highlightBad -> MaterialTheme.colorScheme.onErrorContainer
        highlightGood -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LifeMiserableStatusPanel(
    totalHateCount: Int,
    proHateCount: Int,
    isMiserable: Boolean
) {
    val isAtRisk = !isMiserable && totalHateCount > 0
    val containerColor = when {
        isMiserable -> MaterialTheme.colorScheme.errorContainer
        isAtRisk -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        isMiserable -> MaterialTheme.colorScheme.onErrorContainer
        isAtRisk -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val statusTitle = when {
        isMiserable -> "Life Miserable Status: Active"
        isAtRisk -> "Life Miserable Risk: Warning"
        else -> "Life Miserable Status: Clear"
    }
    val statusIcon = when {
        isMiserable -> Icons.Outlined.SentimentVeryDissatisfied
        isAtRisk -> Icons.Outlined.WarningAmber
        else -> Icons.Outlined.SentimentSatisfied
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("life_miserable_indicator")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusTitle,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
                Text(
                    text = "${totalHateCount}/3 Hostile",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            // 3-step visual gauge to 3 hostile bonds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { idx ->
                    val filled = totalHateCount > idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (filled) {
                                    if (isMiserable) MaterialTheme.colorScheme.error else contentColor
                                } else {
                                    contentColor.copy(alpha = 0.22f)
                                }
                            )
                    )
                }
            }

            val detail = when {
                isMiserable ->
                    "Hostility ($proHateCount in office/school, $totalHateCount total) drains -12 Happiness & adds +10 Stress/Anxiety annually. Repair bonds below!"
                isAtRisk ->
                    "$totalHateCount hostile bond(s) (<25% closeness, $proHateCount in office/school). Reaching 3 triggers Life Miserable!"
                else ->
                    "Zero hostile bonds (<25%). Keep colleagues, managers & teachers warm to prevent misery."
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ProfessionalBondRow(
    rel: Relationship,
    canInteract: Boolean,
    onCheckIn: () -> Unit,
    onMakeAmends: () -> Unit
) {
    val isHostile = rel.closeness < 25
    val isStrained = rel.closeness < 35

    Surface(
        color = if (isHostile) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("professional_bond_row_${rel.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rel.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${rel.role} · Age ${rel.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canInteract) {
                    if (isStrained) {
                        Button(
                            onClick = onMakeAmends,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("office_amends_${rel.id}")
                        ) {
                            Text("Make Amends", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCheckIn,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("office_checkin_${rel.id}")
                        ) {
                            Text("Check In", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (rel.closeness / 100f).coerceIn(0f, 1f) },
                    color = when {
                        isHostile -> MaterialTheme.colorScheme.error
                        rel.closeness >= 70 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    trackColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Text(
                    text = "${rel.closeness}% · ${rel.statusLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHostile) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isHostile || rel.yearsNeglected > 0) {
                val warnNote = buildString {
                    if (isHostile) append("Hostile bond — counts toward Life Miserable")
                    if (isHostile && rel.yearsNeglected > 0) append(" · ")
                    if (rel.yearsNeglected > 0) append("Neglected ${rel.yearsNeglected}y")
                }
                Text(
                    text = warnNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
