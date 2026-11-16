package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AncestorRecord
import com.example.model.AvatarAppearance
import com.example.model.EntryCategory
import com.example.model.LifeState
import com.example.model.Relationship
import com.example.ui.components.CharacterAvatar
import java.text.NumberFormat
import java.util.Locale

private sealed interface TreeSelection {
    data class Ancestor(val record: AncestorRecord) : TreeSelection
    data object ActiveCharacter : TreeSelection
    data class FamilyBond(val relationship: Relationship) : TreeSelection
}

private data class GenerationChronicleGroup(
    val generation: Int,
    val memberName: String,
    val spanLabel: String,
    val careerLabel: String,
    val accomplishments: List<String>
)

@Composable
fun FamilyTreeScreen(
    life: LifeState,
    onContinueAsHeir: () -> Unit
) {
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val sortedAncestors = remember(life.lineageHistory) {
        life.lineageHistory.sortedBy { it.generation }
    }
    var selectedNode by remember(life.lifeId, life.lineageHistory.size) {
        mutableStateOf<TreeSelection>(
            sortedAncestors.lastOrNull()?.let { TreeSelection.Ancestor(it) } ?: TreeSelection.ActiveCharacter
        )
    }

    val parents = remember(life.relationships) {
        life.relationships.filter { it.role == "Mother" || it.role == "Father" || it.role == "Parent" }
    }
    val siblings = remember(life.relationships) {
        life.relationships.filter { it.role.contains("Sibling", ignoreCase = true) }
    }
    val spouseOrPartner = remember(life.relationships) {
        life.relationships.firstOrNull { it.isSpouse }
            ?: life.relationships.firstOrNull { it.isPartner }
    }
    val children = remember(life.relationships) {
        life.relationships.filter { it.role == "Child" }
    }

    val dynastyLegacyScore = remember(life, sortedAncestors) {
        sortedAncestors.sumOf { it.legacyScore } + life.legacyScore
    }
    val dynastyPeakWealth = remember(life, sortedAncestors) {
        maxOf(life.netWorth, sortedAncestors.maxOfOrNull { it.netWorth } ?: 0)
    }
    val historicalGroups = remember(life, sortedAncestors) {
        buildChronicleGroups(life, sortedAncestors)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DynastyOverviewCard(
                    life = life,
                    ancestorCount = sortedAncestors.size,
                    dynastyLegacyScore = dynastyLegacyScore,
                    dynastyPeakWealthText = moneyFmt.format(dynastyPeakWealth)
                )
            }

            item {
                VisualTreeCard(
                    life = life,
                    ancestors = sortedAncestors,
                    parents = parents,
                    siblings = siblings,
                    spouseOrPartner = spouseOrPartner,
                    children = children,
                    selectedNode = selectedNode,
                    onSelect = { selectedNode = it },
                    onContinueAsHeir = onContinueAsHeir
                )
            }

            item {
                SelectedMemberInspectorCard(
                    life = life,
                    selection = selectedNode,
                    moneyFmt = moneyFmt
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Historical Accomplishments Across Generations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(historicalGroups, key = { "gen_${it.generation}_${it.memberName}" }) { group ->
                GenerationChronicleCard(group = group)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DynastyOverviewCard(
    life: LifeState,
    ancestorCount: Int,
    dynastyLegacyScore: Int,
    dynastyPeakWealthText: String
) {
    val surname = life.characterName.substringAfterLast(" ", life.characterName)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountTree,
                        contentDescription = "Family lineage icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "The $surname Lineage",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rooted in ${life.birthplace} · Generation ${life.generation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TreeMetricChip("Generation ${life.generation}")
                TreeMetricChip("${ancestorCount + 1} Recorded Lives")
                TreeMetricChip("Dynasty Score $dynastyLegacyScore")
                TreeMetricChip("Peak Estate $dynastyPeakWealthText")
            }
        }
    }
}

@Composable
private fun VisualTreeCard(
    life: LifeState,
    ancestors: List<AncestorRecord>,
    parents: List<Relationship>,
    siblings: List<Relationship>,
    spouseOrPartner: Relationship?,
    children: List<Relationship>,
    selectedNode: TreeSelection,
    onSelect: (TreeSelection) -> Unit,
    onContinueAsHeir: () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val bondColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lineage Tree",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Tap any portrait to inspect their era, career, and accomplishments.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 14.dp)
            )

            // 1. Past Generations (Gen 1 -> Gen N-1)
            ancestors.forEach { ancestor ->
                val isSelected = selectedNode is TreeSelection.Ancestor && selectedNode.record.lifeId == ancestor.lifeId
                AncestorTierRow(
                    ancestor = ancestor,
                    isSelected = isSelected,
                    bondColor = bondColor,
                    onClick = { onSelect(TreeSelection.Ancestor(ancestor)) }
                )
                VerticalTrunkConnector(lineColor = lineColor)
            }

            // 2. Immediate Parents & Siblings (when no recorded multi-gen history yet, or to show second parent)
            if (ancestors.isEmpty() && parents.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    parents.forEachIndexed { idx, parent ->
                        val isSel = selectedNode is TreeSelection.FamilyBond && selectedNode.relationship.id == parent.id
                        FamilyMemberNodeCard(
                            name = parent.name,
                            subtitle = "${parent.role} · ${if (parent.isAlive) "Age ${parent.age}" else "Remembered"}",
                            badge = "Origin",
                            appearance = AvatarAppearance(
                                skinToneIndex = life.appearance.skinToneIndex,
                                hairColorIndex = life.appearance.hairColorIndex,
                                hairStyleIndex = (life.appearance.hairStyleIndex + idx + 1) % 6,
                                outfitIndex = (idx + 2) % 5,
                                backdropIndex = (idx + 1) % 5
                            ),
                            age = parent.age.coerceAtLeast(32),
                            isHighlighted = false,
                            isSelected = isSel,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tree_node_parent_$idx"),
                            onClick = { onSelect(TreeSelection.FamilyBond(parent)) }
                        )
                    }
                }
                if (siblings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Siblings: ${siblings.joinToString { it.name }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                VerticalTrunkConnector(lineColor = lineColor)
            }

            // 3. Active Generation: Protagonist + Spouse/Partner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeSelected = selectedNode is TreeSelection.ActiveCharacter
                FamilyMemberNodeCard(
                    name = life.characterName,
                    subtitle = if (life.isAlive) "Age ${life.currentAge} · ${life.currentCareerTitle}"
                    else "Lived ${life.currentAge}y · ${life.birthYear}–${life.currentYear}",
                    badge = "Gen ${life.generation}${if (life.isAlive) " · Active" else " · Legacy"}",
                    appearance = life.appearance,
                    age = life.currentAge,
                    health = life.stats.health,
                    fame = life.fame,
                    isInPrison = life.prisonYearsLeft > 0,
                    isHighlighted = true,
                    isSelected = activeSelected,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tree_node_active"),
                    onClick = { onSelect(TreeSelection.ActiveCharacter) }
                )

                if (spouseOrPartner != null) {
                    HorizontalBondConnector(bondColor = bondColor)
                    val partnerSelected = selectedNode is TreeSelection.FamilyBond &&
                        selectedNode.relationship.id == spouseOrPartner.id
                    FamilyMemberNodeCard(
                        name = spouseOrPartner.name,
                        subtitle = "${spouseOrPartner.role} · ${if (spouseOrPartner.isAlive) "Age ${spouseOrPartner.age}" else "Remembered"}",
                        badge = if (spouseOrPartner.isSpouse) "Spouse" else "Partner",
                        appearance = AvatarAppearance(
                            skinToneIndex = (life.appearance.skinToneIndex + 2) % 6,
                            hairStyleIndex = (life.appearance.hairStyleIndex + 3) % 6,
                            hairColorIndex = (life.appearance.hairColorIndex + 1) % 6,
                            outfitIndex = 2,
                            backdropIndex = 1
                        ),
                        age = spouseOrPartner.age,
                        isHighlighted = false,
                        isSelected = partnerSelected,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tree_node_spouse"),
                        onClick = { onSelect(TreeSelection.FamilyBond(spouseOrPartner)) }
                    )
                }
            }

            // 4. Next Generation Branch (Children & Continue Heir Action)
            if (children.isNotEmpty() || !life.isAlive) {
                VerticalTrunkConnector(lineColor = lineColor, hasLeafBranch = children.size > 1)
            }

            if (children.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    children.forEachIndexed { idx, child ->
                        val childSelected = selectedNode is TreeSelection.FamilyBond && selectedNode.relationship.id == child.id
                        FamilyMemberNodeCard(
                            name = child.name,
                            subtitle = "Child · Age ${child.age}",
                            badge = "Gen ${life.generation + 1} Heir",
                            appearance = AvatarAppearance(
                                skinToneIndex = life.appearance.skinToneIndex,
                                hairStyleIndex = (life.appearance.hairStyleIndex + idx + 2) % 6,
                                hairColorIndex = life.appearance.hairColorIndex,
                                outfitIndex = 0,
                                backdropIndex = 2
                            ),
                            age = child.age,
                            isHighlighted = false,
                            isSelected = childSelected,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tree_node_child_$idx"),
                            onClick = { onSelect(TreeSelection.FamilyBond(child)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (!life.isAlive) {
                Button(
                    onClick = onContinueAsHeir,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("continue_heir_button")
                ) {
                    Icon(Icons.Outlined.ChildCare, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (children.isNotEmpty()) {
                            "Continue Lineage as ${children.first().name} (Gen ${life.generation + 1})"
                        } else {
                            "Continue Lineage as Gen ${life.generation + 1} Heir"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (children.isNotEmpty()) {
                            "${children.first().name} stands ready to carry Generation ${life.generation + 1} forward when this chapter concludes."
                        } else {
                            "Marry or live past age 32 to welcome children, or carry a new heir forward once ${life.characterName}'s memoir finishes."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AncestorTierRow(
    ancestor: AncestorRecord,
    isSelected: Boolean,
    bondColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FamilyMemberNodeCard(
            name = ancestor.characterName,
            subtitle = "${ancestor.birthYear}–${ancestor.deathYear} (${ancestor.ageReached}y) · ${ancestor.careerTitle}",
            badge = "Gen ${ancestor.generation}",
            appearance = ancestor.appearance,
            age = ancestor.ageReached,
            fame = ancestor.peakFame,
            isHighlighted = false,
            isSelected = isSelected,
            modifier = Modifier
                .weight(1f)
                .testTag("tree_node_gen_${ancestor.generation}"),
            onClick = onClick
        )
        if (ancestor.spouseName.isNotBlank()) {
            HorizontalBondConnector(bondColor = bondColor)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(0.72f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = "Spouse",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = ancestor.spouseName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Spouse",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberNodeCard(
    name: String,
    subtitle: String,
    badge: String,
    appearance: AvatarAppearance,
    age: Int,
    health: Int = 82,
    fame: Int = 0,
    isInPrison: Boolean = false,
    isHighlighted: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val border = when {
        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        isHighlighted -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
        else -> null
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        border = border,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            CharacterAvatar(
                appearance = appearance,
                age = age,
                size = if (isHighlighted) 58.dp else 48.dp,
                health = health,
                fame = fame,
                isInPrison = isInPrison
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VerticalTrunkConnector(
    lineColor: Color,
    hasLeafBranch: Boolean = false
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val cx = size.width / 2f
        drawLine(
            color = lineColor,
            start = Offset(cx, 0f),
            end = Offset(cx, size.height),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(cx, size.height / 2f)
        )
        if (hasLeafBranch) {
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.25f, size.height - 2.dp.toPx()),
                end = Offset(size.width * 0.75f, size.height - 2.dp.toPx()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun HorizontalBondConnector(bondColor: Color) {
    Canvas(
        modifier = Modifier
            .width(24.dp)
            .height(40.dp)
    ) {
        val cy = size.height / 2f
        drawLine(
            color = bondColor,
            start = Offset(0f, cy),
            end = Offset(size.width, cy),
            strokeWidth = 2.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = bondColor,
            radius = 3.5.dp.toPx(),
            center = Offset(size.width / 2f, cy)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedMemberInspectorCard(
    life: LifeState,
    selection: TreeSelection,
    moneyFmt: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selection) {
                is TreeSelection.Ancestor -> {
                    val anc = selection.record
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CharacterAvatar(
                            appearance = anc.appearance,
                            age = anc.ageReached,
                            size = 56.dp,
                            fame = anc.peakFame
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = anc.characterName,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Gen ${anc.generation} · ${anc.birthYear}–${anc.deathYear} (Age ${anc.ageReached}) · ${anc.birthplace}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TreeMetricChip(anc.careerTitle)
                        TreeMetricChip("Score ${anc.legacyScore}")
                        TreeMetricChip(moneyFmt.format(anc.netWorth))
                        if (anc.peakFame > 0) TreeMetricChip("Peak Fame ${anc.peakFame}")
                        if (anc.followers > 0) TreeMetricChip("${anc.followers} Fans")
                    }
                    if (anc.highlights.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Recorded Highlights",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        anc.highlights.forEach { item ->
                            HighlightBulletRow(item)
                        }
                    }
                }

                TreeSelection.ActiveCharacter -> {
                    val currentHighlights = remember(life.entries) {
                        life.entries
                            .filter {
                                it.category == EntryCategory.HISTORICAL ||
                                    it.category == EntryCategory.MILESTONE ||
                                    it.category == EntryCategory.FAME ||
                                    it.category == EntryCategory.CAREER
                            }
                            .distinctBy { it.title }
                            .take(4)
                            .map { "${it.year}: ${it.title}" }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CharacterAvatar(
                            appearance = life.appearance,
                            age = life.currentAge,
                            size = 56.dp,
                            health = life.stats.health,
                            fame = life.fame,
                            isInPrison = life.prisonYearsLeft > 0
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(life.characterName, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Gen ${life.generation} · ${life.lifeStage.label} · ${life.birthplace}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TreeMetricChip(life.currentCareerTitle)
                        TreeMetricChip("Legacy ${life.legacyScore}")
                        TreeMetricChip(moneyFmt.format(life.netWorth))
                        TreeMetricChip("Achievements ${life.unlockedAchievementIds.size}")
                    }
                    if (currentHighlights.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Chapter Highlights So Far",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        currentHighlights.forEach { item ->
                            HighlightBulletRow(item)
                        }
                    }
                }

                is TreeSelection.FamilyBond -> {
                    val rel = selection.relationship
                    Text(rel.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${rel.role} · ${rel.statusLabel} · ${if (rel.isAlive) "Age ${rel.age}" else "Passed away at ${rel.age}"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TreeMetricChip("Closeness ${rel.closeness}%")
                        TreeMetricChip(if (rel.isAlive) "Living Bond" else "Remembered")
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightBulletRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GenerationChronicleCard(group: GenerationChronicleGroup) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MilitaryTech,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Gen ${group.generation} · ${group.memberName}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = group.spanLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = group.careerLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            group.accomplishments.forEach { item ->
                HighlightBulletRow(item)
            }
        }
    }
}

@Composable
private fun TreeMetricChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun buildChronicleGroups(
    life: LifeState,
    ancestors: List<AncestorRecord>
): List<GenerationChronicleGroup> {
    val activeHighlights = life.entries
        .filter {
            it.category == EntryCategory.HISTORICAL ||
                it.category == EntryCategory.MILESTONE ||
                it.category == EntryCategory.FAME ||
                it.category == EntryCategory.CAREER ||
                it.category == EntryCategory.LEGACY
        }
        .distinctBy { it.title }
        .take(6)
        .map { "${it.year}: ${it.title} — ${it.detail}" }
        .ifEmpty {
            listOf("${life.birthYear}: Born in ${life.birthplace} as Generation ${life.generation}")
        }

    val currentGroup = GenerationChronicleGroup(
        generation = life.generation,
        memberName = life.characterName,
        spanLabel = if (life.isAlive) "${life.birthYear}–Present" else "${life.birthYear}–${life.currentYear}",
        careerLabel = "${life.currentCareerTitle} · Legacy Score ${life.legacyScore}",
        accomplishments = activeHighlights
    )

    val pastGroups = ancestors.sortedByDescending { it.generation }.map { anc ->
        GenerationChronicleGroup(
            generation = anc.generation,
            memberName = anc.characterName,
            spanLabel = "${anc.birthYear}–${anc.deathYear}",
            careerLabel = "${anc.careerTitle} · Legacy Score ${anc.legacyScore}",
            accomplishments = anc.highlights.ifEmpty {
                listOf("Lived ${anc.ageReached} years in ${anc.birthplace}")
            }
        )
    }

    return listOf(currentGroup) + pastGroups
}
