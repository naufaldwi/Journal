package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.dp
import com.example.model.EducationType
import com.example.model.LifeState
import com.example.model.SkillActivity
import com.example.model.SkillType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SkillsOverviewCard(
    life: LifeState,
    modifier: Modifier = Modifier
) {
    val autoBoostedSkills = remember(life.inSchool, life.educationType, life.currentAge) {
        when {
            life.inSchool -> boostedSkillsForEducation(life.educationType)
            life.currentAge in 6..17 -> setOf(
                SkillType.ENGINEERING_TECH,
                SkillType.DEDUCTION_LAW,
                SkillType.PERSUASION_MEDIA,
                SkillType.PAINTING_ART
            )
            else -> emptySet()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Skill Bars", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = when {
                            life.inSchool -> "Attending ${life.educationType.label} auto-boosts highlighted skills each year."
                            life.currentAge in 6..17 -> "School years gently grow foundational skills automatically."
                            else -> "Skills gate career entry and boost salaries & promotions."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (autoBoostedSkills.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = "School auto-boost active",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Auto ↑",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SkillType.entries.forEach { skill ->
                val value = life.skills.levelOf(skill)
                val isAutoBoosted = skill in autoBoostedSkills
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = skill.label,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (isAutoBoosted) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "School ↑",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "$value/100",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (value / 100f).coerceIn(0f, 1f) },
                        color = if (isAutoBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = skill.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SkillActivitiesCard(
    life: LifeState,
    onTrainSkill: (SkillActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val canTrain = life.isAlive && life.currentAge >= 6 && life.prisonYearsLeft == 0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text("Skill-Building Activities", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (life.currentAge < 6) {
                            "Activities unlock at age 6."
                        } else {
                            "Practice dedicated crafts to raise your skill bars and unlock higher-tier careers."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SkillActivity.entries.forEach { activity ->
                val costText = if (activity.cashCost > 0) "$${activity.cashCost} · -${activity.energyCost} Energy" else "Free · -${activity.energyCost} Energy"
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(activity.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = activity.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = costText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = { onTrainSkill(activity) },
                            enabled = canTrain,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("train_skill_${activity.name}")
                        ) {
                            Text("Train")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdultLoansCard(
    life: LifeState,
    onTakeLoan: (Int) -> Unit,
    onRepayLoan: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val isAdult = life.currentAge >= 18
    val canBorrow = life.isAlive && isAdult && life.prisonYearsLeft == 0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = "Finances and Adult Loans",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text("Cash Reserve & Adult Loans", style = MaterialTheme.typography.titleLarge)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Cash On Hand", style = MaterialTheme.typography.labelSmall)
                        Text(moneyFmt.format(life.netWorth), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Surface(
                    color = if (life.loanDebt > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Loan Debt (4%/yr)", style = MaterialTheme.typography.labelSmall)
                        Text(moneyFmt.format(life.loanDebt), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (!isAdult) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cash never drops below $0. Loans unlock at Age 18 when adult expenses and tuition begin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = "Need capital for university, conservatory tuition, or merch? Borrow up to $150,000 total (4% annual interest).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onTakeLoan(5_000) },
                        enabled = canBorrow && life.loanDebt + 5_000 <= 150_000,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("take_loan_5000")
                    ) {
                        Text("Borrow $5k")
                    }
                    OutlinedButton(
                        onClick = { onTakeLoan(15_000) },
                        enabled = canBorrow && life.loanDebt + 15_000 <= 150_000,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("take_loan_15000")
                    ) {
                        Text("Borrow $15k")
                    }
                    OutlinedButton(
                        onClick = { onTakeLoan(40_000) },
                        enabled = canBorrow && life.loanDebt + 40_000 <= 150_000,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("take_loan_40000")
                    ) {
                        Text("Borrow $40k")
                    }
                }

                if (life.loanDebt > 0 && life.isAlive) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Repay Loan", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onRepayLoan(1_000) },
                            enabled = life.netWorth > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("repay_loan_1000")
                        ) {
                            Text("Repay $1k")
                        }
                        Button(
                            onClick = { onRepayLoan(5_000) },
                            enabled = life.netWorth > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("repay_loan_5000")
                        ) {
                            Text("Repay $5k")
                        }
                        FilledTonalButton(
                            onClick = { onRepayLoan(life.loanDebt) },
                            enabled = life.netWorth > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("repay_loan_all")
                        ) {
                            Text("Pay All")
                        }
                    }
                }
            }
        }
    }
}

private fun boostedSkillsForEducation(edu: EducationType): Set<SkillType> = when (edu) {
    EducationType.LAW_SCHOOL -> setOf(SkillType.DEDUCTION_LAW, SkillType.PERSUASION_MEDIA, SkillType.ENGINEERING_TECH)
    EducationType.ART_UNI -> setOf(SkillType.PAINTING_ART, SkillType.MUSIC_PERFORMANCE, SkillType.ACTING_STAGE, SkillType.PERSUASION_MEDIA)
    EducationType.MUSIC_ACADEMY -> setOf(SkillType.MUSIC_PERFORMANCE, SkillType.ACTING_STAGE, SkillType.PAINTING_ART)
    EducationType.UNIVERSITY -> setOf(SkillType.ENGINEERING_TECH, SkillType.DEDUCTION_LAW, SkillType.PERSUASION_MEDIA)
    EducationType.TRADE_SCHOOL -> setOf(SkillType.ATHLETIC_DRIVING, SkillType.CULINARY_CRAFT, SkillType.ENGINEERING_TECH)
    EducationType.NONE -> emptySet()
}
