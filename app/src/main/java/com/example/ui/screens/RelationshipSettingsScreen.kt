package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BondCategory
import com.example.model.LifeState
import com.example.model.Relationship
import com.example.model.RelationshipPriority
import java.text.NumberFormat
import java.util.Locale

private enum class SettingsTieFilter(val label: String) {
    ALL("All Ties"),
    PRIORITIZED("Prioritized"),
    FAMILY("Family"),
    FRIENDS("Friends"),
    WORK_SCHOOL("Work & School"),
    ROMANCE("Romance")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelationshipSettingsScreen(
    life: LifeState,
    onSetPriority: (String, RelationshipPriority) -> Unit,
    onRunPriorityBoost: () -> Unit,
    onQuickInteract: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    var activeFilter by rememberSaveable { mutableStateOf(SettingsTieFilter.ALL) }

    val livingTies = remember(life.relationships) {
        life.relationships
            .filter { it.isAlive }
            .sortedWith(
                compareByDescending<Relationship> { it.priority != RelationshipPriority.STANDARD }
                    .thenByDescending { it.closeness }
            )
    }

    val interactCount = remember(livingTies) {
        livingTies.count { it.priority == RelationshipPriority.INTERACTION }
    }
    val giftCount = remember(livingTies) {
        livingTies.count { it.priority == RelationshipPriority.GIFTING }
    }
    val totalPrioritized = interactCount + giftCount
    val estimatedGiftCost = giftCount * 150

    val filteredTies = remember(livingTies, activeFilter) {
        when (activeFilter) {
            SettingsTieFilter.ALL -> livingTies
            SettingsTieFilter.PRIORITIZED -> livingTies.filter { it.priority != RelationshipPriority.STANDARD }
            SettingsTieFilter.FAMILY -> livingTies.filter { it.category == BondCategory.FAMILY }
            SettingsTieFilter.FRIENDS -> livingTies.filter {
                it.category == BondCategory.FRIEND || it.category == BondCategory.SOCIAL_CONTACT
            }
            SettingsTieFilter.WORK_SCHOOL -> livingTies.filter {
                it.category == BondCategory.WORK || it.category == BondCategory.SCHOOL
            }
            SettingsTieFilter.ROMANCE -> livingTies.filter {
                it.category == BondCategory.ROMANCE || it.isPartner || it.isSpouse
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("relationship_settings_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("relationship_settings_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Relationship priority settings",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Active Social Tie Priorities",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "Assign NPCs to Interact (prevents neglect & checks in annually) or Gift ($150 thoughtful gifts for rapid closeness growth).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PriorityMetric("Active Ties", "${livingTies.size}")
                            PriorityMetric("Interact", "$interactCount")
                            PriorityMetric("Gift", "$giftCount")
                            PriorityMetric("Cash", moneyFmt.format(life.netWorth))
                        }

                        Button(
                            onClick = onRunPriorityBoost,
                            enabled = life.isAlive && totalPrioritized > 0,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("run_priority_boost_button")
                        ) {
                            Text(
                                text = if (totalPrioritized == 0) {
                                    "Choose NPCs Below to Boost"
                                } else if (giftCount > 0) {
                                    "Boost $totalPrioritized Prioritized Bond(s) · Est. ${moneyFmt.format(estimatedGiftCost)}"
                                } else {
                                    "Check In With $interactCount Prioritized Contact(s)"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rel_settings_filter_row")
                ) {
                    SettingsTieFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { activeFilter = filter },
                            label = { Text(filter.label) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("rel_settings_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            if (filteredTies.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rel_settings_empty_state")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Diversity3,
                                contentDescription = "No matching ties",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = if (activeFilter == SettingsTieFilter.PRIORITIZED) {
                                    "No NPCs prioritized yet"
                                } else {
                                    "No active ties in this category"
                                },
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Switch filters or assign Interact / Gift priorities on any NPC card to manage them here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTies, key = { it.id }) { rel ->
                    RelationshipPrioritySettingCard(
                        rel = rel,
                        canAct = life.isAlive,
                        canAffordGift = life.netWorth >= 150,
                        onSelectPriority = { prio -> onSetPriority(rel.id, prio) },
                        onQuickInteract = { onQuickInteract(rel.id, "time") },
                        onQuickGift = { onQuickInteract(rel.id, "gift") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RelationshipPrioritySettingCard(
    rel: Relationship,
    canAct: Boolean,
    canAffordGift: Boolean,
    onSelectPriority: (RelationshipPriority) -> Unit,
    onQuickInteract: () -> Unit,
    onQuickGift: () -> Unit
) {
    val isHostile = rel.closeness < 25
    val isPrioritized = rel.priority != RelationshipPriority.STANDARD

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rel_setting_card_${rel.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${rel.role} · ${rel.category.label} · Age ${rel.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when {
                        isHostile -> MaterialTheme.colorScheme.errorContainer
                        isPrioritized -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${rel.statusLabel} (${rel.closeness}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isHostile -> MaterialTheme.colorScheme.onErrorContainer
                            isPrioritized -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (rel.closeness / 100f).coerceIn(0f, 1f) },
                color = if (isHostile) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            if (rel.yearsNeglected > 0 && rel.priority == RelationshipPriority.STANDARD) {
                Text(
                    text = "Neglected ${rel.yearsNeglected}y — set to Interact or Gift to stop silent drift.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = rel.priority.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = RelationshipPriority.entries
                options.forEachIndexed { idx, prio ->
                    SegmentedButton(
                        selected = rel.priority == prio,
                        onClick = { if (canAct) onSelectPriority(prio) },
                        enabled = canAct,
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = options.size),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("rel_priority_${rel.id}_${prio.name.lowercase()}")
                    ) {
                        Text(
                            text = when (prio) {
                                RelationshipPriority.STANDARD -> "Standard"
                                RelationshipPriority.INTERACTION -> "Interact"
                                RelationshipPriority.GIFTING -> "Gift"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (canAct) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onQuickInteract,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("rel_quick_interact_${rel.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Quick interact",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("  Interact (+8)", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onQuickGift,
                        enabled = canAffordGift,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("rel_quick_gift_${rel.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CardGiftcard,
                            contentDescription = "Quick gift",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("  Gift $150", maxLines = 1)
                    }
                }
            }
        }
    }
}
