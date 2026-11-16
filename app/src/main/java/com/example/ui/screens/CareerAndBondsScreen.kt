package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.CountryCatalog
import com.example.model.CareerTrack
import com.example.model.EducationType
import com.example.model.LifeState
import com.example.model.Relationship
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EducationCareersSubView(
    life: LifeState,
    onEnrollEducation: (EducationType) -> Unit,
    onApplyCareer: (CareerTrack) -> Unit,
    onWorkExtraHours: () -> Unit,
    onBoostFame: () -> Unit
) {
    var section by remember { mutableIntStateOf(0) }
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf("Current & Schools", "Career Tracks").forEachIndexed { idx, label ->
                SegmentedButton(
                    selected = section == idx,
                    onClick = { section = idx },
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                    modifier = Modifier.testTag("career_subtab_$idx")
                ) {
                    Text(label, maxLines = 1)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (section == 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Current Chapter", style = MaterialTheme.typography.labelSmall)
                            Text(life.currentCareerTitle, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = when {
                                    life.prisonYearsLeft > 0 -> "Serving ${life.prisonYearsLeft} more year(s) in prison."
                                    life.currentSalary > 0 -> "Annual Salary: ${moneyFmt.format(life.currentSalary)} · Performance ${life.careerPerformance}%"
                                    life.inSchool -> "Enrolled in ${life.educationType.label}"
                                    else -> "Open to careers & conservatories at age 18+"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (life.careerTrack != CareerTrack.UNEMPLOYED && life.isAlive && life.prisonYearsLeft == 0) {
                                    Button(
                                        onClick = onWorkExtraHours,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Text("Focused Sprint")
                                    }
                                }
                                if (life.isAlive && life.currentAge >= 16 && life.prisonYearsLeft == 0 &&
                                    (life.careerTrack.isFameTrack || life.fame > 0)
                                ) {
                                    OutlinedButton(
                                        onClick = onBoostFame,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("boost_fame_button")
                                    ) {
                                        Text("Release / Showcase")
                                    }
                                }
                            }
                        }
                    }
                }

                if (life.fame > 0 || life.peakFame > 0 || life.careerTrack.isFameTrack) {
                    item {
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
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Public Fame", style = MaterialTheme.typography.titleMedium)
                                    Text("${life.fame}/100 (Peak ${life.peakFame})", style = MaterialTheme.typography.labelLarge)
                                }
                                LinearProgressIndicator(
                                    progress = { (life.fame / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    "Fame fades each year unless you release work, perform, race, or stream.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Higher Education & Conservatories", style = MaterialTheme.typography.titleMedium)
                            if (life.completedEducation != EducationType.NONE) {
                                Text(
                                    "Completed: ${life.completedEducation.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (life.currentAge < 18) {
                                Text(
                                    "Universities, Law School & Conservatories unlock at Age 18.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (life.inSchool) {
                                Text(
                                    "Currently studying ${life.educationType.label}. Matching skills auto-increase each year!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (life.isAlive && life.prisonYearsLeft == 0) {
                                EducationType.entries.filter { it != EducationType.NONE }.forEach { edu ->
                                    OutlinedButton(
                                        onClick = { onEnrollEducation(edu) },
                                        enabled = life.completedEducation != edu,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text("${edu.label} ($${edu.annualCost})", style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                edu.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(CareerTrack.entries.filter { it != CareerTrack.UNEMPLOYED }) { track ->
                    val isCurrent = life.careerTrack == track
                    val userSkill = life.skills.levelOf(track.primarySkill)
                    val meetsSkill = userSkill >= track.minPrimarySkill
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, style = MaterialTheme.typography.titleMedium)
                                    val reqLabel = when {
                                        track.requiresDegree -> "${track.preferredEducation.label} / High Intellect"
                                        track.preferredEducation != EducationType.NONE -> "${track.preferredEducation.label} / Talent"
                                        else -> "Open Entry"
                                    }
                                    Text(
                                        text = "${track.fieldLabel} · $reqLabel${if (track.isFameTrack) " · Fame" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCurrent) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Skill Gate: ${track.primarySkill.label} $userSkill/${track.minPrimarySkill}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (meetsSkill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Ladder: ${track.rungs.joinToString(" → ") { it.first }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isCurrent && life.isAlive) {
                                OutlinedButton(
                                    onClick = { onApplyCareer(track) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("apply_career_${track.name}")
                                ) {
                                    Text("Apply as ${track.rungs.first().first}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnderworldSubView(
    life: LifeState,
    onJoinCrime: () -> Unit,
    onCommitSyndicateOp: (Int) -> Unit,
    onLeaveCrime: () -> Unit
) {
    val syndicate = remember(life.birthplace) { CountryCatalog.syndicateForBirthplace(life.birthplace) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(syndicate.organizationName, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Local Syndicate (${life.birthplace}) · Term: ${syndicate.localTerm}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (life.prisonYearsLeft > 0) {
                            "Serving prison time (${life.prisonYearsLeft}y remaining). Age forward to finish sentence."
                        } else if (life.crimeActive) {
                            "Rank: ${syndicate.ranks.getOrElse(life.crimeRank) { syndicate.ranks.last() }} · Notoriety ${life.notoriety}/100"
                        } else {
                            "Walk the shadow path for high risk & fast money — or stay clean."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = { (life.notoriety / 100f).coerceIn(0f, 1f) },
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    if (!life.crimeActive && life.isAlive && life.prisonYearsLeft == 0) {
                        Button(
                            onClick = onJoinCrime,
                            enabled = life.currentAge >= 16,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("join_crime_button")
                        ) {
                            Text(
                                if (life.currentAge < 16) "Unlocks at Age 16"
                                else "Join ${syndicate.localTerm} Syndicate"
                            )
                        }
                    } else if (life.crimeActive && life.isAlive && life.prisonYearsLeft == 0) {
                        syndicate.hustleTitles.forEachIndexed { idx, op ->
                            OutlinedButton(
                                onClick = { onCommitSyndicateOp(idx) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("${op.first} (Tier ${idx + 1} Risk)")
                            }
                        }
                        OutlinedButton(
                            onClick = onLeaveCrime,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Leave Syndicate & Go Straight")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BondsSubView(
    life: LifeState,
    onInteractRelationship: (String, String) -> Unit,
    onMeetNewPerson: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (life.isAlive && life.currentAge >= 12) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onMeetNewPerson(false) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Meet Friend")
                    }
                    if (life.currentAge >= 18) {
                        Button(
                            onClick = { onMeetNewPerson(true) },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Seek Romance")
                        }
                    }
                }
            }
        }

        items(life.relationships, key = { it.id }) { rel ->
            RelationshipCard(
                rel = rel,
                canInteract = life.isAlive,
                canPropose = life.currentAge >= 20 && rel.isPartner && !rel.isSpouse,
                onInteract = { action -> onInteractRelationship(rel.id, action) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelationshipCard(
    rel: Relationship,
    canInteract: Boolean,
    canPropose: Boolean,
    onInteract: (String) -> Unit
) {
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
                Column {
                    Text(rel.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${rel.role} · Age ${rel.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = rel.statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (rel.closeness / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            if (canInteract && rel.isAlive) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onInteract("time") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Quality Time")
                    }
                    OutlinedButton(
                        onClick = { onInteract("deep_talk") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Deep Talk")
                    }
                    OutlinedButton(
                        onClick = { onInteract("gift") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Gift ($150)")
                    }
                    if (canPropose) {
                        Button(
                            onClick = { onInteract("propose") },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Propose")
                        }
                    }
                }
            }
        }
    }
}
