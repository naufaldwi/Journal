package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.CountryCatalog
import com.example.engine.LifeEngine
import com.example.model.GlobalNewsItem
import com.example.model.LifeState
import com.example.model.NewsCategory
import com.example.model.RomanceMethod
import com.example.model.RomanceOrientation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GlobalNewsFeedSheet(
    newsFeed: List<GlobalNewsItem>,
    birthplace: String,
    currentYear: Int,
    economyMultiplier: Float = 1.0f,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val region = remember(birthplace) { CountryCatalog.regionForBirthplace(birthplace) }
    var selectedCategory by remember { mutableStateOf<NewsCategory?>(null) }

    val filteredFeed = remember(newsFeed, selectedCategory) {
        if (selectedCategory == null) newsFeed
        else newsFeed.filter { it.category == selectedCategory }
    }

    val economyPercent = ((economyMultiplier - 1.0f) * 100f).roundToInt()
    val economyClimate = when {
        economyMultiplier >= 1.08f -> "Booming (${if (economyPercent >= 0) "+" else ""}$economyPercent%)"
        economyMultiplier <= 0.93f -> "Tightening ($economyPercent%)"
        else -> "Steady (${if (economyPercent >= 0) "+" else ""}$economyPercent%)"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Public,
                        contentDescription = "World Wire",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Global News Wire", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "$region Edition · $currentYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Close")
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "World Economy Climate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        economyClimate,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (economyMultiplier < 0.95f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${newsFeed.size})") },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("news_filter_ALL")
                )
                NewsCategory.entries.forEach { cat ->
                    val count = newsFeed.count { it.category == cat }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.label} ($count)") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("news_filter_${cat.name}")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (filteredFeed.isEmpty()) {
                Text(
                    if (newsFeed.isEmpty()) {
                        "Annual world dispatches begin arriving as each year turns."
                    } else {
                        "No ${selectedCategory?.label ?: ""} dispatches recorded yet. Age up to receive fresh world events."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredFeed.forEach { item ->
                    val badgeColors = when (item.category) {
                        NewsCategory.WAR_GEOPOLITICS ->
                            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        NewsCategory.SCIENCE_TECH ->
                            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                        NewsCategory.CULTURAL_SHIFT ->
                            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    }
                    val impactPct = (item.economyDelta * 100f).roundToInt()
                    val impactText = when {
                        impactPct > 0 -> "Economy +${impactPct}%"
                        impactPct < 0 -> "Economy ${impactPct}%"
                        else -> "Economy Steady"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_item_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = badgeColors.first,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = item.category.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColors.second,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${item.year} · ${item.region}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(item.headline, style = MaterialTheme.typography.titleSmall)
                            Text(
                                item.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = impactText,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    impactPct < 0 -> MaterialTheme.colorScheme.error
                                    impactPct > 0 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrientationChoiceDialog(
    onSelect: (RomanceOrientation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose Romantic Orientation", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Choose once for this character before seeking romance. Potential partners and matches will follow your preference.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { onSelect(RomanceOrientation.HETEROSEXUAL) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("choose_heterosexual_button")
                ) {
                    Text(RomanceOrientation.HETEROSEXUAL.label)
                }
                OutlinedButton(
                    onClick = { onSelect(RomanceOrientation.HOMOSEXUAL) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("choose_homosexual_button")
                ) {
                    Text(RomanceOrientation.HOMOSEXUAL.label)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekRomanceSheet(
    life: LifeState,
    onSeekRomance: (RomanceMethod, String?) -> Unit,
    onBreakUpPartner: (String) -> Unit,
    onMeetFriend: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activePartner = remember(life.relationships) {
        life.relationships.firstOrNull { it.isAlive && (it.isPartner || it.isSpouse) }
    }
    val compatibleFriends = remember(life.relationships, life.orientation, life.gender, life.currentAge) {
        life.relationships.filter { LifeEngine.isCompatibleRomanceTarget(life, it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Seek Romance",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Seek Romance", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${life.orientation.label} · Rejection can lower mood",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Close")
                }
            }

            if (activePartner != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anti_cheating_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = "Partner commitment warning",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Currently with ${activePartner.name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            "Cheating prevention active: you can only date one partner at a time. Break up with ${activePartner.name} first before pursuing someone new.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { onBreakUpPartner(activePartner.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("prevent_cheating_breakup_button")
                        ) {
                            Text("Break Up with ${activePartner.name}")
                        }
                    }
                }
            }

            val canAttemptNow = activePartner == null

            // Dating App (18+)
            OutlinedButton(
                onClick = {
                    onSeekRomance(RomanceMethod.DATING_APP, null)
                    onDismiss()
                },
                enabled = canAttemptNow && life.currentAge >= RomanceMethod.DATING_APP.minAge,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("romance_dating_app_button")
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = if (life.currentAge >= 18) "Try Dating App" else "Dating App (Unlocks at 18)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        RomanceMethod.DATING_APP.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Blind Date (14+)
            OutlinedButton(
                onClick = {
                    onSeekRomance(RomanceMethod.BLIND_DATE, null)
                    onDismiss()
                },
                enabled = canAttemptNow,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("romance_blind_date_button")
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Go on Blind Date", style = MaterialTheme.typography.titleSmall)
                    Text(
                        RomanceMethod.BLIND_DATE.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Confess to a Friend", style = MaterialTheme.typography.titleSmall)

            if (compatibleFriends.isEmpty()) {
                Text(
                    "No compatible age-matched friends yet — meet a new friend or peer first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onMeetFriend,
                    enabled = canAttemptNow,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("meet_friend_from_romance_button")
                ) {
                    Text("Meet New Friend")
                }
            } else {
                compatibleFriends.forEach { friend ->
                    OutlinedButton(
                        onClick = {
                            onSeekRomance(RomanceMethod.FRIEND_TO_PARTNER, friend.id)
                            onDismiss()
                        },
                        enabled = canAttemptNow,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text(friend.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${friend.role} · Age ${friend.age}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${friend.closeness}% Bond",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
