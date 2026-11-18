package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.CountryCatalog
import com.example.model.AvatarAppearance
import com.example.model.CharacterGender
import com.example.model.LifeStats
import com.example.model.TemperamentTrait
import com.example.ui.components.AvatarPalettes
import com.example.ui.components.CharacterAvatar
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewLifeScreen(
    canCancel: Boolean,
    onCancel: () -> Unit,
    onBeginLife: (
        name: String,
        birthplace: String,
        birthYear: Int,
        pronouns: String,
        trait: TemperamentTrait,
        appearance: AvatarAppearance,
        stats: LifeStats,
        gender: CharacterGender
    ) -> Unit
) {
    if (canCancel) {
        BackHandler { onCancel() }
    }

    var selectedCountry by remember { mutableStateOf(CountryCatalog.countries.first()) }
    var selectedCity by remember { mutableStateOf(selectedCountry.cities.first()) }
    var gender by remember { mutableStateOf(CharacterGender.FEMALE) }
    var pronouns by remember { mutableStateOf(CharacterGender.FEMALE.defaultPronouns) }
    var name by remember {
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(selectedCountry.cities.first(), isMale = false)
        mutableStateOf("$fn $ln")
    }
    var birthYear by remember { mutableIntStateOf(1998) }
    var trait by remember { mutableStateOf(TemperamentTrait.CURIOUS) }

    var appearance by remember {
        mutableStateOf(
            AvatarAppearance(
                skinToneIndex = 2,
                hairStyleIndex = 1,
                hairColorIndex = 0,
                outfitIndex = 0,
                accessoryIndex = 0,
                backdropIndex = 0
            )
        )
    }

    var health by remember { mutableIntStateOf(86) }
    var intellect by remember { mutableIntStateOf(68) }
    var creativity by remember { mutableIntStateOf(74) }
    var fitness by remember { mutableIntStateOf(72) }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Begin a New Journal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Customize heritage, portrait, and starting spirit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canCancel) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close character creator")
                    }
                }
            }

            // Live Portrait Preview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CharacterAvatar(
                            appearance = appearance,
                            age = 20,
                            size = 84.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name.ifBlank { "Unnamed Chronicler" },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "$selectedCity · Born $birthYear",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${trait.label} · $pronouns",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                appearance = AvatarAppearance(
                                    skinToneIndex = Random.nextInt(AvatarPalettes.skinTones.size),
                                    hairStyleIndex = Random.nextInt(6),
                                    hairColorIndex = Random.nextInt(AvatarPalettes.hairColors.size),
                                    outfitIndex = Random.nextInt(AvatarPalettes.outfitColors.size),
                                    accessoryIndex = Random.nextInt(4),
                                    backdropIndex = Random.nextInt(AvatarPalettes.backdrops.size)
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.Casino, contentDescription = "Randomize appearance")
                        }
                    }

                    Text("Skin Tone", style = MaterialTheme.typography.labelMedium)
                    SwatchRow(
                        colors = AvatarPalettes.skinTones,
                        selectedIndex = appearance.skinToneIndex,
                        onSelect = { appearance = appearance.copy(skinToneIndex = it) }
                    )

                    Text("Hair Style", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarPalettes.hairStyleLabels.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = appearance.hairStyleIndex == idx,
                                onClick = { appearance = appearance.copy(hairStyleIndex = idx) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Hair & Outfit Palette", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SwatchRow(
                            colors = AvatarPalettes.hairColors,
                            selectedIndex = appearance.hairColorIndex,
                            onSelect = { appearance = appearance.copy(hairColorIndex = it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SwatchRow(
                            colors = AvatarPalettes.outfitColors,
                            selectedIndex = appearance.outfitIndex,
                            onSelect = { appearance = appearance.copy(outfitIndex = it) }
                        )
                    }

                    Text("Glasses & Backdrop", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarPalettes.accessoryLabels.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = appearance.accessoryIndex == idx,
                                onClick = { appearance = appearance.copy(accessoryIndex = idx) },
                                label = { Text(label) }
                            )
                        }
                    }
                    SwatchRow(
                        colors = AvatarPalettes.backdrops,
                        selectedIndex = appearance.backdropIndex,
                        onSelect = { appearance = appearance.copy(backdropIndex = it) }
                    )
                }
            }

            // Heritage & Identity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Country & Hometown", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CountryCatalog.countries.forEach { country ->
                            FilterChip(
                                selected = selectedCountry.countryName == country.countryName,
                                onClick = {
                                    selectedCountry = country
                                    selectedCity = country.cities.first()
                                    val isMale = when (gender) {
                                        CharacterGender.MALE -> true
                                        CharacterGender.FEMALE -> false
                                        CharacterGender.NON_BINARY -> null
                                    }
                                    val (fn, ln) = CountryCatalog.randomNameForBirthplace(selectedCity, isMale = isMale)
                                    name = "$fn $ln"
                                },
                                label = { Text(country.countryName) }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedCountry.cities.forEach { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick = { selectedCity = city },
                                label = { Text(city.substringBefore(",")) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(50) },
                            label = { Text("Character Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("character_name_input")
                        )
                        TextButton(
                            onClick = {
                                val isMale = when (gender) {
                                    CharacterGender.MALE -> true
                                    CharacterGender.FEMALE -> false
                                    CharacterGender.NON_BINARY -> null
                                }
                                val (fn, ln) = CountryCatalog.randomNameForBirthplace(selectedCity, isMale = isMale)
                                name = "$fn $ln"
                            }
                        ) {
                            Text("Random")
                        }
                    }

                    Text("Gender, Pronouns & Birth Year", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CharacterGender.entries.forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = {
                                    gender = g
                                    pronouns = g.defaultPronouns
                                    val isMale = when (g) {
                                        CharacterGender.MALE -> true
                                        CharacterGender.FEMALE -> false
                                        CharacterGender.NON_BINARY -> null
                                    }
                                    val (fn, ln) = CountryCatalog.randomNameForBirthplace(selectedCity, isMale = isMale)
                                    name = "$fn $ln"
                                },
                                label = { Text("${g.label} (${g.defaultPronouns})") },
                                modifier = Modifier.testTag("gender_chip_${g.name}")
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1988, 1996, 2004, 2012).forEach { yr ->
                            FilterChip(
                                selected = birthYear == yr,
                                onClick = { birthYear = yr },
                                label = { Text("$yr") }
                            )
                        }
                    }

                    Text("Temperament Trait", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TemperamentTrait.entries.forEach { t ->
                            FilterChip(
                                selected = trait == t,
                                onClick = { trait = t },
                                label = { Text(t.label) }
                            )
                        }
                    }
                    Text(
                        text = trait.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Starting Attributes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Starting Disposition", style = MaterialTheme.typography.titleMedium)
                        TextButton(
                            onClick = {
                                health = Random.nextInt(70, 96)
                                intellect = Random.nextInt(55, 88)
                                creativity = Random.nextInt(55, 90)
                                fitness = Random.nextInt(60, 88)
                            }
                        ) {
                            Text("Roll Stats")
                        }
                    }

                    AttributeSlider("Vitality", health) { health = it }
                    AttributeSlider("Intellect", intellect) { intellect = it }
                    AttributeSlider("Creativity", creativity) { creativity = it }
                    AttributeSlider("Fitness", fitness) { fitness = it }
                }
            }

            Button(
                onClick = {
                    onBeginLife(
                        name,
                        selectedCity,
                        birthYear,
                        pronouns,
                        trait,
                        appearance,
                        LifeStats(
                            health = health,
                            intellect = intellect,
                            creativity = creativity,
                            fitness = fitness
                        ),
                        gender
                    )
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("begin_life_button")
            ) {
                Text("Open Chapter One", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SwatchRow(
    colors: List<Color>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.forEachIndexed { idx, color ->
            val selected = idx == selectedIndex
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selected) 2.5.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onSelect(idx) }
            )
        }
    }
}

@Composable
private fun AttributeSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$value / 100", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 45f..95f
        )
    }
}
