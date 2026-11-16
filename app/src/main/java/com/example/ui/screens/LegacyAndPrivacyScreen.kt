package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.onGoogleSignInClicked
import com.example.data.signOutUser
import com.example.model.AchievementCatalog
import com.example.model.LifeSaveDoc
import com.example.ui.JournalUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegacyAndPrivacyScreen(
    uiState: JournalUiState,
    onResumeCloudSave: (LifeSaveDoc) -> Unit,
    onStartNewLife: () -> Unit,
    onSyncNow: () -> Unit,
    onSavePrivacy: (String, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onSetThemeOverride: (Boolean?) -> Unit,
    onDeleteCloudData: () -> Unit,
    onUpgradeGuestWithGoogle: () -> Unit,
    onSignedOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val unlockedIds = remember(uiState) {
        val fromProfile = uiState.userProfile?.unlockedAchievementIds ?: emptyList()
        val fromGuest = uiState.guestSettings.unlockedAchievementIds
        val fromLife = uiState.activeLife?.unlockedAchievementIds ?: emptySet()
        (fromProfile + fromGuest + fromLife).toSet()
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Profile & Legacy", style = MaterialTheme.typography.headlineMedium)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Board", "Badges", "Saves", "Privacy").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.isGuest) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Global Leaderboards", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "Sign in with Google to view global legacy rankings and sync your best chapters across devices.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Button(
                                            onClick = {
                                                onGoogleSignInClicked(
                                                    context = context,
                                                    credentialManager = credentialManager,
                                                    onAuthSuccess = { onUpgradeGuestWithGoogle() },
                                                    onAuthError = {},
                                                    scope = scope
                                                )
                                            }
                                        ) {
                                            Text("Sign in with Google")
                                        }
                                    }
                                }
                            }
                        } else if (uiState.leaderboard.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Public Entries Yet",
                                    subtitle = "Complete a life chapter with Leaderboard Visibility enabled to appear here."
                                )
                            }
                        } else {
                            itemsIndexed(uiState.leaderboard, key = { _, item -> item.id }) { idx, entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "#${idx + 1}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(entry.leaderboardName, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "${entry.characterName} · ${entry.careerTitle} (Age ${entry.ageReached})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                "${entry.legacyScore} pts",
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(AchievementCatalog.all, key = { it.id }) { ach ->
                            val unlocked = unlockedIds.contains(ach.id)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Outlined.CheckCircle else Icons.Outlined.EmojiEvents,
                                        contentDescription = ach.title,
                                        tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ach.title, style = MaterialTheme.typography.titleSmall)
                                        Text(ach.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = if (unlocked) "Unlocked" else "+${ach.sparkleReward} ✦",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onStartNewLife,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("New Character")
                                }
                                if (!uiState.isGuest) {
                                    OutlinedButton(
                                        onClick = onSyncNow,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Outlined.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.size(6.dp))
                                        Text(if (uiState.isSyncing) "Syncing..." else "Sync Cloud")
                                    }
                                }
                            }
                        }

                        if (uiState.isGuest) {
                            item {
                                EmptyStateCard(
                                    title = "Local Guest Save Active",
                                    subtitle = "Your current journal is safely stored on this device. Connect Google Sign-In in Privacy to back up across platforms."
                                )
                            }
                        } else if (uiState.cloudSaves.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Cloud Saves Yet",
                                    subtitle = "Your active life automatically syncs here for cross-platform resume."
                                )
                            }
                        } else {
                            items(uiState.cloudSaves, key = { it.id }) { save ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(save.characterName, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "${save.careerTitle} · Age ${save.currentAge} · ${if (save.isAlive) "In Progress" else "Completed"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        OutlinedButton(onClick = { onResumeCloudSave(save) }) {
                                            Text("Load")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    val prof = uiState.userProfile
                    var alias by remember(prof?.leaderboardAlias) {
                        mutableStateOf(prof?.leaderboardAlias ?: "QuietReader")
                    }
                    var showBoard by remember(prof?.showOnLeaderboard) {
                        mutableStateOf(prof?.showOnLeaderboard ?: true)
                    }
                    var anonBoard by remember(prof?.anonymousLeaderboard) {
                        mutableStateOf(prof?.anonymousLeaderboard ?: false)
                    }
                    var autoSync by remember(prof?.autoCloudSync) {
                        mutableStateOf(prof?.autoCloudSync ?: true)
                    }
                    var showEolAds by remember(prof?.showOptionalEndOfLifeAds, uiState.guestSettings.showOptionalEndOfLifeAds) {
                        mutableStateOf(
                            if (uiState.isGuest) uiState.guestSettings.showOptionalEndOfLifeAds
                            else prof?.showOptionalEndOfLifeAds ?: true
                        )
                    }
                    var confirmDelete by remember { mutableStateOf(false) }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("Privacy & Data Controls", style = MaterialTheme.typography.titleMedium)
                                    }

                                    if (!uiState.isGuest) {
                                        OutlinedTextField(
                                            value = alias,
                                            onValueChange = { alias = it.take(36) },
                                            label = { Text("Public Leaderboard Alias") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        PrivacySwitchRow(
                                            title = "Publish to Global Leaderboard",
                                            subtitle = "Opt out anytime to keep your legacies private.",
                                            checked = showBoard,
                                            onCheckedChange = { showBoard = it }
                                        )
                                        PrivacySwitchRow(
                                            title = "Mask Alias as 'Quiet Chronicler'",
                                            subtitle = "Compete on the board without showing your custom handle.",
                                            checked = anonBoard,
                                            onCheckedChange = { anonBoard = it }
                                        )
                                        PrivacySwitchRow(
                                            title = "Automatic Cloud Sync",
                                            subtitle = "Sync life progress after each year for cross-device resume.",
                                            checked = autoSync,
                                            onCheckedChange = { autoSync = it }
                                        )
                                    }

                                    PrivacySwitchRow(
                                        title = "Optional End-of-Life Supporter Card",
                                        subtitle = "Turn off to hide even the optional end-of-life reward prompt.",
                                        checked = showEolAds,
                                        onCheckedChange = { showEolAds = it }
                                    )

                                    Button(
                                        onClick = {
                                            onSavePrivacy(alias, showBoard, anonBoard, autoSync, showEolAds)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("save_privacy_button")
                                    ) {
                                        Text("Save Privacy Preferences")
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Reading Palette", style = MaterialTheme.typography.titleMedium)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = uiState.guestSettings.darkThemeOverride == null,
                                            onClick = { onSetThemeOverride(null) },
                                            label = { Text("System") }
                                        )
                                        FilterChip(
                                            selected = uiState.guestSettings.darkThemeOverride == false,
                                            onClick = { onSetThemeOverride(false) },
                                            label = { Text("Oat Paper") }
                                        )
                                        FilterChip(
                                            selected = uiState.guestSettings.darkThemeOverride == true,
                                            onClick = { onSetThemeOverride(true) },
                                            label = { Text("Forest Night") }
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Account & Cross-Platform Access", style = MaterialTheme.typography.titleMedium)
                                    if (uiState.isGuest) {
                                        Button(
                                            onClick = {
                                                onGoogleSignInClicked(
                                                    context = context,
                                                    credentialManager = credentialManager,
                                                    onAuthSuccess = { onUpgradeGuestWithGoogle() },
                                                    onAuthError = {},
                                                    scope = scope
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Connect Google Account for Cloud Save")
                                        }
                                        TextButton(
                                            onClick = onSignedOut,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Return to Welcome Screen")
                                        }
                                    } else {
                                        Text(
                                            text = "Signed in as ${uiState.userProfile?.displayName ?: "Chronicler"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                signOutUser(credentialManager, scope) {
                                                    onSignedOut()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Sign Out")
                                        }
                                        HorizontalDivider()
                                        OutlinedButton(
                                            onClick = { confirmDelete = true },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Delete All Cloud Saves & Profile")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (confirmDelete) {
                        AlertDialog(
                            onDismissRequest = { confirmDelete = false },
                            title = { Text("Delete Cloud Data?") },
                            text = { Text("This permanently deletes your cloud saves, leaderboard entry, and cloud profile. This cannot be undone.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        confirmDelete = false
                                        onDeleteCloudData()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
