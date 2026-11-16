package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChoiceOption
import com.example.model.DietPlan
import com.example.model.EntryCategory
import com.example.model.ExerciseRoutine
import com.example.model.JournalEntry
import com.example.model.LifeChoicePrompt
import com.example.model.LifeState
import com.example.model.SleepSchedule
import com.example.ui.components.CharacterAvatar
import com.example.ui.components.DailyMissionsCard
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun JournalHomeScreen(
    life: LifeState,
    sparkles: Int,
    showOptionalEndOfLifeAds: Boolean,
    onAgeUp: () -> Unit,
    onResolveChoice: (ChoiceOption) -> Unit,
    onSetDiet: (DietPlan) -> Unit,
    onSetExercise: (ExerciseRoutine) -> Unit,
    onSetSleep: (SleepSchedule) -> Unit,
    onPerformWellnessAction: (String) -> Unit,
    onClaimOptionalEndOfLifeAd: () -> Unit,
    onStartNewLife: () -> Unit,
    onContinueAsHeir: () -> Unit,
    onOpenFamilyTree: () -> Unit,
    onCompleteDailyMission: (String) -> Unit,
    onClaimDailyMissionBonus: () -> Unit
) {
    var showWellnessDialog by remember { mutableStateOf(false) }
    var showOptionalAdModal by remember { mutableStateOf(false) }
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CharacterSummaryCard(
                        life = life,
                        sparkles = sparkles,
                        moneyText = moneyFmt.format(life.netWorth),
                        loanText = if (life.loanDebt > 0) "Loan ${moneyFmt.format(life.loanDebt)}" else null,
                        onOpenWellness = { showWellnessDialog = true }
                    )
                }

                if (!life.isAlive) {
                    item {
                        EndOfLifeLegacyCard(
                            life = life,
                            moneyText = moneyFmt.format(life.netWorth),
                            showOptionalAd = showOptionalEndOfLifeAds && !life.endOfLifeAdClaimed,
                            onWatchOptionalAd = { showOptionalAdModal = true },
                            onStartNewLife = onStartNewLife,
                            onContinueAsHeir = onContinueAsHeir,
                            onOpenFamilyTree = onOpenFamilyTree
                        )
                    }
                } else {
                    if (life.pendingPrompt != null) {
                        item {
                            PendingChoiceCard(
                                prompt = life.pendingPrompt,
                                onSelectOption = onResolveChoice
                            )
                        }
                    }
                    if (life.currentAge >= 13) {
                        item {
                            DailyMissionsCard(
                                missionState = life.missionState,
                                isCharacterAlive = life.isAlive,
                                onCompleteMission = onCompleteDailyMission,
                                onClaimDailyBonus = onClaimDailyMissionBonus
                            )
                        }
                    } else {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_missions_locked_card")
                            ) {
                                Text(
                                    text = "Daily Missions unlock at Age 13 — enjoy childhood & build foundational skills first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Life Chronicle",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                items(life.entries, key = { it.id }) { entry ->
                    JournalEntryCard(entry = entry)
                }
            }

            // Bottom Action Bar for Advancing Year
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (life.isAlive) {
                        OutlinedButton(
                            onClick = { showWellnessDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(Icons.Outlined.Spa, contentDescription = "Open Wellness", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Wellness")
                        }
                        Button(
                            onClick = onAgeUp,
                            enabled = life.pendingPrompt == null,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("age_up_button")
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (life.pendingPrompt != null) "Choose Above First" else "Age +1 Year (${life.currentAge + 1})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onStartNewLife,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("new_life_after_death_button")
                        ) {
                            Text("New Life", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = onContinueAsHeir,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("continue_heir_home_button")
                        ) {
                            Text("Continue as Gen ${life.generation + 1} Heir", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    if (showWellnessDialog) {
        WellnessModalDialog(
            life = life,
            onDismiss = { showWellnessDialog = false },
            onSetDiet = onSetDiet,
            onSetExercise = onSetExercise,
            onSetSleep = onSetSleep,
            onPerformWellnessAction = onPerformWellnessAction
        )
    }

    if (showOptionalAdModal) {
        OptionalSupporterAdDialog(
            onDismiss = { showOptionalAdModal = false },
            onClaimReward = {
                onClaimOptionalEndOfLifeAd()
                showOptionalAdModal = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacterSummaryCard(
    life: LifeState,
    sparkles: Int,
    moneyText: String,
    loanText: String?,
    onOpenWellness: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CharacterAvatar(
                    appearance = life.appearance,
                    age = life.currentAge,
                    size = 66.dp,
                    health = life.stats.health,
                    fame = life.fame,
                    isInPrison = life.prisonYearsLeft > 0
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = life.characterName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${life.lifeStage.label} · ${life.currentCareerTitle} · ${life.birthplace}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (life.currentAge >= 13) {
                            StatBadge("Lv.${life.missionState.level}")
                        }
                        StatBadge("Gen ${life.generation}")
                        StatBadge("Age ${life.currentAge}")
                        StatBadge("${life.currentYear}")
                        StatBadge(moneyText)
                        if (loanText != null) {
                            StatBadge(loanText)
                        }
                        StatBadge("$sparkles ✦")
                        if (life.fame > 0 || life.careerTrack.isFameTrack) {
                            StatBadge("Fame ${life.fame}")
                        }
                        if (life.crimeActive || life.prisonYearsLeft > 0) {
                            StatBadge(
                                if (life.prisonYearsLeft > 0) "Prison ${life.prisonYearsLeft}y"
                                else "Heat ${life.notoriety}%"
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Physical & Mental Wellness Snapshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WellnessMiniMeter("Health", life.stats.health, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                WellnessMiniMeter("Energy", life.stats.energy, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                WellnessMiniMeter("Happy", life.stats.happiness, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WellnessMiniMeter("Fitness", life.stats.fitness, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                WellnessMiniMeter("Stress", life.stats.stress, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                WellnessMiniMeter("Risk", life.stats.diseaseRisk, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${life.diet.label} · ${life.sleep.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onOpenWellness) {
                    Text("Adjust Routine")
                }
            }
        }
    }
}

@Composable
private fun StatBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun WellnessMiniMeter(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun PendingChoiceCard(
    prompt: LifeChoicePrompt,
    onSelectOption: (ChoiceOption) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = prompt.category.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = prompt.narrative,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            prompt.options.forEach { option ->
                Button(
                    onClick = { onSelectOption(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(option.label, style = MaterialTheme.typography.titleSmall)
                        Text(
                            option.effectHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entry.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "Age ${entry.age} · ${entry.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EndOfLifeLegacyCard(
    life: LifeState,
    moneyText: String,
    showOptionalAd: Boolean,
    onWatchOptionalAd: () -> Unit,
    onStartNewLife: () -> Unit,
    onContinueAsHeir: () -> Unit,
    onOpenFamilyTree: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_legacy_banner),
                contentDescription = "Serene meadow horizon at dusk",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Legacy of ${life.characterName}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = life.epitaph,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge("Score ${life.legacyScore}")
                StatBadge("Age ${life.currentAge}")
                StatBadge(moneyText)
                StatBadge("${life.socialChannel.followers} Fans")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onContinueAsHeir,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Continue Gen ${life.generation + 1}")
                }
                OutlinedButton(
                    onClick = onOpenFamilyTree,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_tree_after_death_button")
                ) {
                    Text("Family Tree")
                }
            }

            AnimatedVisibility(visible = showOptionalAd) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(
                                text = "Optional End-of-Life Supporter Highlight",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "Never paywalled or forced. Optionally view a calm 3s supporter card to carry +250 ✦ Sparkles into your next life.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onWatchOptionalAd,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Watch (+250 ✦)")
                            }
                            TextButton(onClick = onStartNewLife) {
                                Text("Skip & New Life")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WellnessModalDialog(
    life: LifeState,
    onDismiss: () -> Unit,
    onSetDiet: (DietPlan) -> Unit,
    onSetExercise: (ExerciseRoutine) -> Unit,
    onSetSleep: (SleepSchedule) -> Unit,
    onPerformWellnessAction: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Health & Mental Wellness", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Physical: Health ${life.stats.health} · Energy ${life.stats.energy} · Fitness ${life.stats.fitness} · Disease Risk ${life.stats.diseaseRisk}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Mental: Happiness ${life.stats.happiness} · Stress ${life.stats.stress} · Anxiety ${life.stats.anxiety}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text("Daily Diet", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DietPlan.entries.forEach { plan ->
                        FilterChip(
                            selected = life.diet == plan,
                            onClick = { onSetDiet(plan) },
                            label = { Text(plan.label) }
                        )
                    }
                }

                Text("Movement Routine", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseRoutine.entries.forEach { routine ->
                        FilterChip(
                            selected = life.exercise == routine,
                            onClick = { onSetExercise(routine) },
                            label = { Text(routine.label) }
                        )
                    }
                }

                Text("Sleep Schedule", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SleepSchedule.entries.forEach { sched ->
                        FilterChip(
                            selected = life.sleep == sched,
                            onClick = { onSetSleep(sched) },
                            label = { Text(sched.label) }
                        )
                    }
                }

                HorizontalDivider()
                Text("Care Actions", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { onPerformWellnessAction("checkup") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Preventive Checkup ($180, -12% Risk)")
                }
                OutlinedButton(
                    onClick = { onPerformWellnessAction("therapy") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Therapy & Mindfulness ($140, -14 Stress)")
                }
                OutlinedButton(
                    onClick = { onPerformWellnessAction("retreat") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Nature Retreat ($450, +18 Energy)")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun OptionalSupporterAdDialog(
    onDismiss: () -> Unit,
    onClaimReward: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supporter Note", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Thank you for playing Journal ad-light and paywall-free. Every feature remains 100% open to all players.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (secondsLeft > 0) "Reward ready in ${secondsLeft}s..." else "Ready to claim +250 ✦ Sparkles!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onClaimReward,
                enabled = secondsLeft == 0
            ) {
                Text("Claim +250 ✦")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
