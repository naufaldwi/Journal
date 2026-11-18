package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.CountryCatalog
import com.example.engine.LifeEngine
import com.example.engine.StreamOutcome
import com.example.model.BondCategory
import com.example.model.CareerTrack
import com.example.model.EducationType
import com.example.model.LifeState
import com.example.model.RelationshipPriority
import com.example.model.RomanceMethod
import com.example.model.RomanceOrientation
import com.example.model.SkillActivity
import com.example.model.SkillType
import com.example.model.SocialNiche
import com.example.model.SocialPostTopic
import com.example.model.StreamFormat
import com.example.ui.components.AdultLoansCard
import com.example.ui.components.SkillActivitiesCard
import com.example.ui.components.SkillsOverviewCard
import java.text.NumberFormat
import java.util.Locale

enum class ActivitySubmenu(val title: String) {
    HUB("Activities"),
    EDUCATION_CAREERS("Education & Careers"),
    SKILLS_LOANS("Skills & Loans"),
    CREATOR_STUDIO("Creator Studio"),
    SOCIAL_STREAM("Social Stream"),
    UNDERWORLD("Underworld"),
    BONDS("Relationships & Bonds"),
    RELATIONSHIP_SETTINGS("Relationship Settings")
}

private data class HubMenuEntry(
    val submenu: ActivitySubmenu,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
fun ActivitiesHubScreen(
    life: LifeState,
    sparkles: Int,
    lastStreamOutcome: StreamOutcome?,
    onDismissStreamOutcome: () -> Unit,
    onEnrollEducation: (EducationType) -> Unit,
    onApplyCareer: (CareerTrack) -> Unit,
    onWorkExtraHours: () -> Unit,
    onBoostFame: () -> Unit,
    onTrainSkill: (SkillActivity) -> Unit,
    onTakeLoan: (Int) -> Unit,
    onRepayLoan: (Int) -> Unit,
    onJoinCrime: () -> Unit,
    onCommitSyndicateOp: (Int) -> Unit,
    onLeaveCrime: () -> Unit,
    onInteractRelationship: (String, String) -> Unit,
    onMeetNewPerson: (BondCategory?) -> Unit,
    onSetOrientation: (RomanceOrientation) -> Unit,
    onSeekRomance: (RomanceMethod, String?) -> Unit,
    onBreakUpPartner: (String) -> Unit,
    onHaveOrAdoptChild: (String?) -> Unit,
    onTriggerRandomEncounter: () -> Unit,
    onUpdateChannel: (String, SocialNiche) -> Unit,
    onPostContent: (Boolean) -> Unit,
    onInteractFollowers: (String) -> Unit,
    onCompleteLiveStream: (StreamFormat, String, Boolean) -> Unit,
    onLaunchMerch: (String) -> Unit,
    onSignSponsor: (String, Int, Int) -> Unit,
    onUpgradeGear: () -> Unit,
    onPostToSocialStream: (SocialPostTopic, String) -> Unit,
    onSetRelationshipPriority: (String, RelationshipPriority) -> Unit,
    onRunPriorityBondBoost: () -> Unit
) {
    var activeSubmenu by rememberSaveable { mutableStateOf(ActivitySubmenu.HUB) }
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val syndicate = remember(life.birthplace) { CountryCatalog.syndicateForBirthplace(life.birthplace) }

    if (activeSubmenu != ActivitySubmenu.HUB) {
        BackHandler {
            activeSubmenu = ActivitySubmenu.HUB
        }
    }

    val topSkill = remember(life.skills) {
        SkillType.entries.maxByOrNull { life.skills.levelOf(it) } ?: SkillType.DEDUCTION_LAW
    }

    val hubEntries = remember(life, topSkill, syndicate) {
        listOf(
            HubMenuEntry(
                submenu = ActivitySubmenu.EDUCATION_CAREERS,
                icon = Icons.Outlined.WorkOutline,
                title = "Education & Careers",
                subtitle = when {
                    life.isMiserableFromHatred -> "${life.currentCareerTitle} · Life Miserable (${life.hateCount} hostile)"
                    life.inSchool -> "Studying: ${life.educationType.label} · Standing ${life.professionalStanding}/100"
                    life.careerTrack != CareerTrack.UNEMPLOYED -> "${life.currentCareerTitle} · Standing ${life.professionalStanding}/100"
                    life.schoolWorkBonds.isNotEmpty() -> "Office/School Standing ${life.professionalStanding}/100 · Career ladders"
                    else -> "Conservatories, Law School & 13 career ladders"
                }
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.SKILLS_LOANS,
                icon = Icons.Outlined.FitnessCenter,
                title = "Skills & Adult Loans",
                subtitle = "Top: ${topSkill.label} (${life.skills.levelOf(topSkill)}/100) · Cash ${moneyFmt.format(life.netWorth)}"
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.CREATOR_STUDIO,
                icon = Icons.Outlined.LiveTv,
                title = "Creator Studio",
                subtitle = "${life.socialChannel.handle} · ${life.socialChannel.followers} followers · ${life.socialChannel.totalStreams} streams"
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.SOCIAL_STREAM,
                icon = Icons.Outlined.Forum,
                title = "Social Stream",
                subtitle = run {
                    val pop = LifeEngine.computePopularityScore(life)
                    "Popularity $pop/100 (${LifeEngine.popularityTierLabel(pop)}) · ${life.socialFeed.size} post(s)"
                }
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.UNDERWORLD,
                icon = Icons.Outlined.Shield,
                title = "Underworld (${syndicate.localTerm})",
                subtitle = when {
                    life.prisonYearsLeft > 0 -> "Incarcerated (${life.prisonYearsLeft}y left)"
                    life.crimeActive -> "${syndicate.organizationName} · Heat ${life.notoriety}%"
                    else -> "Shadow syndicate paths in ${life.birthplace}"
                }
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.BONDS,
                icon = Icons.Outlined.Diversity3,
                title = "Relationships & Bonds",
                subtitle = "${life.relationships.count { it.isAlive }} bonds · Romance, family & random encounters"
            ),
            HubMenuEntry(
                submenu = ActivitySubmenu.RELATIONSHIP_SETTINGS,
                icon = Icons.Outlined.Tune,
                title = "Relationship Settings",
                subtitle = run {
                    val interactCount = life.relationships.count { it.isAlive && it.priority == RelationshipPriority.INTERACTION }
                    val giftCount = life.relationships.count { it.isAlive && it.priority == RelationshipPriority.GIFTING }
                    "$interactCount Interact · $giftCount Gift prioritized"
                }
            )
        )
    }

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
            if (activeSubmenu == ActivitySubmenu.HUB) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Activities", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Pick a focused submenu to manage your chapter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { activeSubmenu = ActivitySubmenu.HUB },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("submenu_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Activities"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeSubmenu.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeSubmenu,
                label = "activities_submenu_transition",
                modifier = Modifier.weight(1f)
            ) { submenu ->
                when (submenu) {
                    ActivitySubmenu.HUB -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(hubEntries, key = { it.submenu.name }) { entry ->
                                HubMenuCard(
                                    entry = entry,
                                    onClick = { activeSubmenu = entry.submenu }
                                )
                            }
                        }
                    }

                    ActivitySubmenu.EDUCATION_CAREERS -> {
                        EducationCareersSubView(
                            life = life,
                            onEnrollEducation = onEnrollEducation,
                            onApplyCareer = onApplyCareer,
                            onWorkExtraHours = onWorkExtraHours,
                            onBoostFame = onBoostFame,
                            onInteractRelationship = onInteractRelationship,
                            onMeetProfessionalContact = {
                                val targetCat = if (life.currentAge < 18 || life.inSchool) {
                                    BondCategory.SCHOOL
                                } else {
                                    BondCategory.WORK
                                }
                                onMeetNewPerson(targetCat)
                            }
                        )
                    }

                    ActivitySubmenu.SKILLS_LOANS -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { SkillsOverviewCard(life = life) }
                            item { SkillActivitiesCard(life = life, onTrainSkill = onTrainSkill) }
                            item {
                                AdultLoansCard(
                                    life = life,
                                    onTakeLoan = onTakeLoan,
                                    onRepayLoan = onRepayLoan
                                )
                            }
                        }
                    }

                    ActivitySubmenu.CREATOR_STUDIO -> {
                        CreatorStudioScreen(
                            life = life,
                            sparkles = sparkles,
                            lastStreamOutcome = lastStreamOutcome,
                            onDismissStreamOutcome = onDismissStreamOutcome,
                            onUpdateChannel = onUpdateChannel,
                            onPostContent = onPostContent,
                            onInteractFollowers = onInteractFollowers,
                            onCompleteLiveStream = onCompleteLiveStream,
                            onLaunchMerch = onLaunchMerch,
                            onSignSponsor = onSignSponsor,
                            onUpgradeGear = onUpgradeGear
                        )
                    }

                    ActivitySubmenu.SOCIAL_STREAM -> {
                        SocialStreamScreen(
                            life = life,
                            onPostUpdate = onPostToSocialStream
                        )
                    }

                    ActivitySubmenu.UNDERWORLD -> {
                        UnderworldSubView(
                            life = life,
                            onJoinCrime = onJoinCrime,
                            onCommitSyndicateOp = onCommitSyndicateOp,
                            onLeaveCrime = onLeaveCrime
                        )
                    }

                    ActivitySubmenu.BONDS -> {
                        BondsSubView(
                            life = life,
                            onInteractRelationship = onInteractRelationship,
                            onMeetNewPerson = onMeetNewPerson,
                            onSetOrientation = onSetOrientation,
                            onSeekRomance = onSeekRomance,
                            onBreakUpPartner = onBreakUpPartner,
                            onHaveOrAdoptChild = onHaveOrAdoptChild,
                            onTriggerRandomEncounter = onTriggerRandomEncounter,
                            onOpenRelationshipSettings = { activeSubmenu = ActivitySubmenu.RELATIONSHIP_SETTINGS }
                        )
                    }

                    ActivitySubmenu.RELATIONSHIP_SETTINGS -> {
                        RelationshipSettingsScreen(
                            life = life,
                            onSetPriority = onSetRelationshipPriority,
                            onRunPriorityBoost = onRunPriorityBondBoost,
                            onQuickInteract = onInteractRelationship
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubMenuCard(
    entry: HubMenuEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .testTag("hub_card_${entry.submenu.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = entry.icon,
                        contentDescription = entry.title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Open ${entry.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
