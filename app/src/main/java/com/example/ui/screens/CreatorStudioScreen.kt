package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.StreamOutcome
import com.example.model.LifeState
import com.example.model.SocialNiche
import com.example.model.StreamFormat
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorStudioScreen(
    life: LifeState,
    sparkles: Int,
    lastStreamOutcome: StreamOutcome?,
    onDismissStreamOutcome: () -> Unit,
    onUpdateChannel: (String, SocialNiche) -> Unit,
    onPostContent: (Boolean) -> Unit,
    onInteractFollowers: (String) -> Unit,
    onCompleteLiveStream: (StreamFormat, String, Boolean) -> Unit,
    onLaunchMerch: (String) -> Unit,
    onSignSponsor: (String, Int, Int) -> Unit,
    onUpgradeGear: () -> Unit
) {
    val ch = life.socialChannel
    var selectedSection by remember { mutableIntStateOf(0) }
    var handleInput by remember(ch.handle) { mutableStateOf(ch.handle) }
    var merchNameInput by remember { mutableStateOf(ch.merchName) }
    var showLiveStreamModal by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf("Live", "Channel", "Monetize")
                tabs.forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
                        modifier = Modifier.testTag("studio_tab_$idx")
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
                when (selectedSection) {
                    0 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_stream_studio),
                                        contentDescription = "Cozy creator streaming desk",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(ch.handle, style = MaterialTheme.typography.titleLarge)
                                            Text(
                                                "${ch.niche.label} · Gear Lv ${ch.gearLevel}/5",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "$sparkles ✦",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StudioMetricChip("Followers", "${ch.followers}", Modifier.weight(1f))
                                        StudioMetricChip("Rep", "${ch.reputation}%", Modifier.weight(1f))
                                        StudioMetricChip("Streams", "${ch.totalStreams}", Modifier.weight(1f))
                                    }

                                    Button(
                                        onClick = { showLiveStreamModal = true },
                                        enabled = life.isAlive && life.currentAge >= 13,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("go_live_button")
                                    ) {
                                        Icon(Icons.Outlined.LiveTv, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.size(8.dp))
                                        Text(
                                            text = if (life.currentAge < 13) "Streaming Unlocks at Age 13" else "Go Live (Earn ✦ & $)",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                        Column {
                                            Text("Trend: ${ch.currentTrendTitle}", style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Matches ${ch.currentTrendNiche.label} (+2.6x aligned boost)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onPostContent(true) },
                                            enabled = life.isAlive && life.currentAge >= 13,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Ride Trend")
                                        }
                                        OutlinedButton(
                                            onClick = { onPostContent(false) },
                                            enabled = life.isAlive && life.currentAge >= 13,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Niche Post")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Channel Niche & Handle", style = MaterialTheme.typography.titleMedium)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = handleInput,
                                            onValueChange = { handleInput = it.take(28) },
                                            label = { Text("Creator Handle") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = { onUpdateChannel(handleInput, ch.niche) },
                                            enabled = life.isAlive,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SocialNiche.entries.forEach { niche ->
                                            FilterChip(
                                                selected = ch.niche == niche,
                                                onClick = { onUpdateChannel(handleInput, niche) },
                                                label = { Text(niche.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Follower Interactions", style = MaterialTheme.typography.titleMedium)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onInteractFollowers("letters") },
                                            enabled = life.isAlive && life.currentAge >= 13,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Reply to Superfans")
                                        }
                                        OutlinedButton(
                                            onClick = { onInteractFollowers("poll") },
                                            enabled = life.isAlive && life.currentAge >= 13,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Community Poll")
                                        }
                                        OutlinedButton(
                                            onClick = { onInteractFollowers("calm_reply") },
                                            enabled = life.isAlive && life.currentAge >= 13,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Calm Q&A Thread")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("Merchandise & Sponsors", style = MaterialTheme.typography.titleMedium)
                                    }

                                    if (ch.merchLaunched) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Active Merch Line: ${ch.merchName} (Earning annual royalties)",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = merchNameInput,
                                                onValueChange = { merchNameInput = it.take(36) },
                                                label = { Text("Merch Item Name") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Button(
                                                onClick = { onLaunchMerch(merchNameInput) },
                                                enabled = life.isAlive && ch.followers >= 500,
                                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                            ) {
                                                Text("Launch ($1.5k)")
                                            }
                                        }
                                        if (ch.followers < 500) {
                                            Text(
                                                "Reach 500 followers to unlock your merchandise drop.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        text = if (ch.activeSponsorName.isNotBlank()) {
                                            "Active Sponsor: ${ch.activeSponsorName} ($${ch.annualSponsorIncome}/yr)"
                                        } else {
                                            "Brand Sponsorships"
                                        },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onSignSponsor("Earthwise Linen Co.", 6000, 5) },
                                            enabled = life.isAlive && ch.followers >= 1000,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Earthwise ($6k/yr)")
                                        }
                                        OutlinedButton(
                                            onClick = { onSignSponsor("Kopi Nusantara Roasters", 14000, 4) },
                                            enabled = life.isAlive && ch.followers >= 5000,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Roasters ($14k/yr)")
                                        }
                                        OutlinedButton(
                                            onClick = { onSignSponsor("Studio Desk Audio", 28000, 3) },
                                            enabled = life.isAlive && ch.followers >= 20000,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Studio Audio ($28k/yr)")
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    val gearCost = ch.gearLevel * 120
                                    OutlinedButton(
                                        onClick = onUpgradeGear,
                                        enabled = life.isAlive && ch.gearLevel < 5,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.size(8.dp))
                                        Text(
                                            if (ch.gearLevel >= 5) "Studio Gear Maxed (Tier 5)"
                                            else "Upgrade Gear to Tier ${ch.gearLevel + 1} ($gearCost ✦)"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLiveStreamModal) {
        LiveStreamBroadcastDialog(
            nicheLabel = ch.niche.label,
            onDismiss = { showLiveStreamModal = false },
            onFinishStream = { format, topic, engaged ->
                showLiveStreamModal = false
                onCompleteLiveStream(format, topic, engaged)
            }
        )
    }

    if (lastStreamOutcome != null) {
        AlertDialog(
            onDismissRequest = onDismissStreamOutcome,
            title = { Text("Stream Summary", style = MaterialTheme.typography.headlineMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Peak Concurrent Viewers: ${lastStreamOutcome.peakViewers}", style = MaterialTheme.typography.bodyLarge)
                    Text("New Followers: +${lastStreamOutcome.followersGained}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Earned: +${lastStreamOutcome.sparklesEarned} ✦ Sparkles & +$${lastStreamOutcome.cashEarned}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissStreamOutcome) { Text("Collect Payout") }
            }
        )
    }
}

@Composable
private fun StudioMetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.labelLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LiveStreamBroadcastDialog(
    nicheLabel: String,
    onDismiss: () -> Unit,
    onFinishStream: (StreamFormat, String, Boolean) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(StreamFormat.COZY_SESSION) }
    var topic by remember { mutableStateOf("Evening $nicheLabel Session") }
    var isLive by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(5) }
    var engagedChat by remember { mutableStateOf(false) }
    var currentViewers by remember { mutableIntStateOf(42) }
    val chatFeed = remember { mutableStateListOf<String>() }

    val sampleComments = remember(nicheLabel) {
        listOf(
            "maya_reads: So calming to tune into tonight!",
            "raka_id: Love the lighting setup in this studio.",
            "sora_k: Dropping cozy vibes from Kyoto ✨",
            "tessa_m: What inspired today's $nicheLabel topic?",
            "noah_dev: Just subscribed! Keep it grounded."
        )
    }

    LaunchedEffect(isLive) {
        if (isLive) {
            chatFeed.clear()
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining -= 1
                currentViewers += Random.nextInt(18, 55) + if (engagedChat) 25 else 0
                chatFeed.add(sampleComments.random())
            }
            onFinishStream(selectedFormat, topic, engagedChat)
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLive) onDismiss() },
        title = { Text(if (isLive) "ON AIR · Live Broadcast" else "Start Live Stream", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isLive) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it.take(45) },
                        label = { Text("Stream Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Stream Format", style = MaterialTheme.typography.titleSmall)
                    StreamFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text("${fmt.label} (~${fmt.baseSparkles} ✦ + $${fmt.baseCash})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        "Watching live: $currentViewers viewers · ${secondsRemaining}s left",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { (5 - secondsRemaining) / 5f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            chatFeed.takeLast(4).forEach { msg ->
                                Text(msg, style = MaterialTheme.typography.bodySmall)
                            }
                            if (chatFeed.isEmpty()) {
                                Text("Connecting cozy chat...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Button(
                        onClick = { engagedChat = true },
                        enabled = !engagedChat,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (engagedChat) "Chat Engaged (+25% Sparkles!)" else "Answer Chat & Shoutout Mods (+25% ✦)")
                    }
                }
            }
        },
        confirmButton = {
            if (!isLive) {
                Button(
                    onClick = { isLive = true },
                    modifier = Modifier.testTag("start_broadcast_confirm")
                ) {
                    Text("Go Live Now")
                }
            }
        },
        dismissButton = {
            if (!isLive) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
