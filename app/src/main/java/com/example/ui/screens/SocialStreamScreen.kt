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
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.engine.LifeEngine
import com.example.model.LifeState
import com.example.model.SocialPostTopic
import com.example.model.SocialStreamComment
import com.example.model.SocialStreamPost
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialStreamScreen(
    life: LifeState,
    onPostUpdate: (SocialPostTopic, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val popularity = remember(life) { LifeEngine.computePopularityScore(life) }
    val tierLabel = remember(popularity) { LifeEngine.popularityTierLabel(popularity) }
    val warmCircleCount = remember(life.relationships) {
        life.relationships.count { it.isAlive && it.closeness >= 65 }
    }
    val numFmt = remember { NumberFormat.getIntegerInstance(Locale.US) }

    var selectedTopic by rememberSaveable { mutableStateOf(SocialPostTopic.LIFE) }
    var draftMessage by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf<SocialPostTopic?>(null) }

    val prompts = remember(life, selectedTopic) {
        LifeEngine.suggestedSocialPostPrompts(life, selectedTopic)
    }
    val canPost = life.isAlive && life.prisonYearsLeft == 0 && life.currentAge >= 13
    val filteredPosts = remember(life.socialFeed, activeFilter) {
        if (activeFilter == null) life.socialFeed else life.socialFeed.filter { it.topic == activeFilter }
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
                .testTag("social_stream_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PopularityOverviewCard(
                    life = life,
                    popularity = popularity,
                    tierLabel = tierLabel,
                    warmCircleCount = warmCircleCount,
                    numFmt = numFmt
                )
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
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "Share social update",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Share a Social Update",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "Choose a topic, customize your status or tap a prompt, and see how NPCs react based on your popularity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SocialPostTopic.entries.forEach { topic ->
                                FilterChip(
                                    selected = selectedTopic == topic,
                                    onClick = { selectedTopic = topic },
                                    label = { Text(topic.label) },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("social_topic_${topic.name.lowercase()}")
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            prompts.forEachIndexed { idx, suggestion ->
                                AssistChip(
                                    onClick = { draftMessage = suggestion },
                                    label = {
                                        Text(
                                            text = suggestion,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("social_prompt_chip_$idx")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draftMessage,
                            onValueChange = { if (it.length <= 200) draftMessage = it },
                            label = { Text("What's happening in ${life.birthplace.substringBefore(",")}?") },
                            placeholder = { Text(prompts.firstOrNull() ?: "Share a quick thought...") },
                            supportingText = { Text("${draftMessage.length}/200") },
                            minLines = 2,
                            maxLines = 4,
                            enabled = canPost,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("social_post_input")
                        )

                        if (!canPost) {
                            Text(
                                text = when {
                                    !life.isAlive -> "Posting is closed for completed chapters."
                                    life.prisonYearsLeft > 0 -> "Social posting is disabled while incarcerated."
                                    else -> "Social Stream unlocks at Age 13."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = {
                                onPostUpdate(selectedTopic, draftMessage)
                                draftMessage = ""
                            },
                            enabled = canPost,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("publish_social_post_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "  Publish Update",
                                style = MaterialTheme.typography.labelLarge
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
                        .testTag("social_filter_row")
                ) {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { activeFilter = null },
                        label = { Text("All (${life.socialFeed.size})") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("social_filter_all")
                    )
                    SocialPostTopic.entries.forEach { topic ->
                        val count = life.socialFeed.count { it.topic == topic }
                        FilterChip(
                            selected = activeFilter == topic,
                            onClick = { activeFilter = if (activeFilter == topic) null else topic },
                            label = { Text("${topic.label} ($count)") },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("social_filter_${topic.name.lowercase()}")
                        )
                    }
                }
            }

            if (filteredPosts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("social_empty_state")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Forum,
                                contentDescription = "Empty stream",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "No posts in this stream yet",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Publish a life, career, or relationship update above to receive likes and replies from NPCs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    SocialStreamPostCard(post = post, numFmt = numFmt)
                }
            }
        }
    }
}

@Composable
private fun PopularityOverviewCard(
    life: LifeState,
    popularity: Int,
    tierLabel: String,
    warmCircleCount: Int,
    numFmt: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("social_popularity_card")
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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${life.characterName} · ${life.socialChannel.handle}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Popularity $popularity/100",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = tierLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (popularity / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMiniLabel("Followers", numFmt.format(life.socialChannel.followers))
                MetricMiniLabel("Fame", "${life.fame}/100")
                MetricMiniLabel("Warm Bonds", "$warmCircleCount")
                MetricMiniLabel("Posts", "${life.socialFeed.size}")
            }
        }
    }
}

@Composable
private fun MetricMiniLabel(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SocialStreamPostCard(
    post: SocialStreamPost,
    numFmt: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("social_post_card_${post.id}")
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
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${post.authorHandle} · Age ${post.age} (${post.year})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = post.topic.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${numFmt.format(post.likes)} likes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "${post.comments.size} replies",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (post.comments.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.comments.forEach { comment ->
                        NpcCommentRow(comment = comment)
                    }
                }
            }
        }
    }
}

@Composable
private fun NpcCommentRow(comment: SocialStreamComment) {
    val bgColor = if (comment.isSupportive) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }
    val roleColor = if (comment.isSupportive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = comment.authorRelation,
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor
                )
            }
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
