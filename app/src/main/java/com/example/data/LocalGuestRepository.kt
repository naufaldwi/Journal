package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AncestorRecord
import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.CharacterSkills
import com.example.model.DailyMission
import com.example.model.DailyMissionState
import com.example.model.DietPlan
import com.example.model.EducationType
import com.example.model.EntryCategory
import com.example.model.ExerciseRoutine
import com.example.model.JournalEntry
import com.example.model.LifeState
import com.example.model.LifeStats
import com.example.model.MissionType
import com.example.model.Relationship
import com.example.model.SleepSchedule
import com.example.model.SocialChannel
import com.example.model.SocialNiche
import com.example.model.TemperamentTrait
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.journalGuestDataStore: DataStore<Preferences> by preferencesDataStore(name = "journal_guest_prefs")

data class GuestSettings(
    val isGuestActive: Boolean = false,
    val darkThemeOverride: Boolean? = null,
    val showOptionalEndOfLifeAds: Boolean = true,
    val sparkles: Int = 250,
    val totalLivesPlayed: Int = 0,
    val unlockedAchievementIds: Set<String> = emptySet()
)

class LocalGuestRepository(private val context: Context) {

    private object Keys {
        val IS_GUEST = booleanPreferencesKey("is_guest_active")
        val DARK_THEME = stringPreferencesKey("dark_theme_pref") // "system", "light", "dark"
        val SHOW_EOL_ADS = booleanPreferencesKey("show_eol_ads")
        val GUEST_SPARKLES = intPreferencesKey("guest_sparkles")
        val GUEST_LIVES_PLAYED = intPreferencesKey("guest_lives_played")
        val GUEST_ACHIEVEMENTS = stringPreferencesKey("guest_achievements_json")
        val GUEST_LIFE_JSON = stringPreferencesKey("guest_active_life_json")
    }

    val guestSettingsFlow: Flow<GuestSettings> = context.journalGuestDataStore.data.map { prefs ->
        val darkOverride = when (prefs[Keys.DARK_THEME]) {
            "light" -> false
            "dark" -> true
            else -> null
        }
        val achSet = mutableSetOf<String>()
        val rawAch = prefs[Keys.GUEST_ACHIEVEMENTS]
        if (!rawAch.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(rawAch)
                for (i in 0 until arr.length()) {
                    achSet.add(arr.getString(i))
                }
            }
        }
        GuestSettings(
            isGuestActive = prefs[Keys.IS_GUEST] ?: false,
            darkThemeOverride = darkOverride,
            showOptionalEndOfLifeAds = prefs[Keys.SHOW_EOL_ADS] ?: true,
            sparkles = prefs[Keys.GUEST_SPARKLES] ?: 250,
            totalLivesPlayed = prefs[Keys.GUEST_LIVES_PLAYED] ?: 0,
            unlockedAchievementIds = achSet
        )
    }

    val savedGuestLifeFlow: Flow<LifeState?> = context.journalGuestDataStore.data.map { prefs ->
        val jsonStr = prefs[Keys.GUEST_LIFE_JSON] ?: return@map null
        runCatching { decodeLifeState(jsonStr) }.getOrNull()
    }

    suspend fun setGuestActive(active: Boolean) {
        context.journalGuestDataStore.edit { prefs ->
            prefs[Keys.IS_GUEST] = active
        }
    }

    suspend fun setThemePreference(overrideDark: Boolean?) {
        context.journalGuestDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = when (overrideDark) {
                true -> "dark"
                false -> "light"
                null -> "system"
            }
        }
    }

    suspend fun setShowOptionalAds(show: Boolean) {
        context.journalGuestDataStore.edit { prefs ->
            prefs[Keys.SHOW_EOL_ADS] = show
        }
    }

    suspend fun updateGuestProgress(
        sparklesDelta: Int,
        newAchievements: Set<String>,
        incrementLivesPlayed: Boolean = false
    ) {
        context.journalGuestDataStore.edit { prefs ->
            val current = prefs[Keys.GUEST_SPARKLES] ?: 250
            prefs[Keys.GUEST_SPARKLES] = (current + sparklesDelta).coerceAtLeast(0)
            if (incrementLivesPlayed) {
                prefs[Keys.GUEST_LIVES_PLAYED] = (prefs[Keys.GUEST_LIVES_PLAYED] ?: 0) + 1
            }
            val currentAch = mutableSetOf<String>()
            prefs[Keys.GUEST_ACHIEVEMENTS]?.let { raw ->
                runCatching {
                    val arr = JSONArray(raw)
                    for (i in 0 until arr.length()) currentAch.add(arr.getString(i))
                }
            }
            currentAch.addAll(newAchievements)
            prefs[Keys.GUEST_ACHIEVEMENTS] = JSONArray(currentAch.toList()).toString()
        }
    }

    suspend fun saveGuestLife(life: LifeState) {
        context.journalGuestDataStore.edit { prefs ->
            prefs[Keys.GUEST_LIFE_JSON] = encodeLifeState(life)
        }
    }

    suspend fun clearGuestLife() {
        context.journalGuestDataStore.edit { prefs ->
            prefs.remove(Keys.GUEST_LIFE_JSON)
        }
    }

    private fun encodeLifeState(life: LifeState): String {
        val root = JSONObject()
        root.put("lifeId", life.lifeId)
        root.put("characterName", life.characterName)
        root.put("birthplace", life.birthplace)
        root.put("birthYear", life.birthYear)
        root.put("currentAge", life.currentAge)
        root.put("isAlive", life.isAlive)
        root.put("pronouns", life.pronouns)
        root.put("trait", life.trait.name)
        root.put("netWorth", life.netWorth)
        root.put("loanDebt", life.loanDebt)
        root.put("hasDegree", life.hasDegree)
        root.put("inSchool", life.inSchool)
        root.put("educationType", life.educationType.name)
        root.put("completedEducation", life.completedEducation.name)
        root.put("careerTrack", life.careerTrack.name)
        root.put("careerRung", life.careerRung)
        root.put("careerPerformance", life.careerPerformance)
        root.put("fame", life.fame)
        root.put("peakFame", life.peakFame)
        root.put("crimeActive", life.crimeActive)
        root.put("crimeRank", life.crimeRank)
        root.put("notoriety", life.notoriety)
        root.put("prisonYearsLeft", life.prisonYearsLeft)
        root.put("generation", life.generation)
        root.put("parentName", life.parentName)
        root.put("parentLifeId", life.parentLifeId)
        root.put("diet", life.diet.name)
        root.put("exercise", life.exercise.name)
        root.put("sleep", life.sleep.name)
        root.put("epitaph", life.epitaph)
        root.put("deathCause", life.deathCause)
        root.put("economyMultiplier", life.economyMultiplier.toDouble())
        root.put("endOfLifeAdClaimed", life.endOfLifeAdClaimed)

        root.put("lineageHistory", JSONArray().apply {
            life.lineageHistory.forEach { anc ->
                put(JSONObject().apply {
                    put("lifeId", anc.lifeId)
                    put("generation", anc.generation)
                    put("characterName", anc.characterName)
                    put("birthplace", anc.birthplace)
                    put("birthYear", anc.birthYear)
                    put("deathYear", anc.deathYear)
                    put("ageReached", anc.ageReached)
                    put("careerTitle", anc.careerTitle)
                    put("legacyScore", anc.legacyScore)
                    put("netWorth", anc.netWorth)
                    put("peakFame", anc.peakFame)
                    put("followers", anc.followers)
                    put("spouseName", anc.spouseName)
                    put("childrenNames", JSONArray(anc.childrenNames))
                    put("highlights", JSONArray(anc.highlights))
                    put("skinToneIndex", anc.appearance.skinToneIndex)
                    put("hairStyleIndex", anc.appearance.hairStyleIndex)
                    put("hairColorIndex", anc.appearance.hairColorIndex)
                    put("outfitIndex", anc.appearance.outfitIndex)
                    put("accessoryIndex", anc.appearance.accessoryIndex)
                    put("backdropIndex", anc.appearance.backdropIndex)
                })
            }
        })

        root.put("missionState", JSONObject().apply {
            put("dateKey", life.missionState.dateKey)
            put("totalXp", life.missionState.totalXp)
            put("dailyBonusClaimed", life.missionState.dailyBonusClaimed)
            put("missions", JSONArray().apply {
                life.missionState.missions.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("type", m.type.name)
                        put("title", m.title)
                        put("description", m.description)
                        put("xpReward", m.xpReward)
                        put("sparkleReward", m.sparkleReward)
                        put("cashReward", m.cashReward)
                        put("isCompleted", m.isCompleted)
                    })
                }
            })
        })

        root.put("appearance", JSONObject().apply {
            put("skinToneIndex", life.appearance.skinToneIndex)
            put("hairStyleIndex", life.appearance.hairStyleIndex)
            put("hairColorIndex", life.appearance.hairColorIndex)
            put("outfitIndex", life.appearance.outfitIndex)
            put("accessoryIndex", life.appearance.accessoryIndex)
            put("backdropIndex", life.appearance.backdropIndex)
        })

        root.put("stats", JSONObject().apply {
            put("health", life.stats.health)
            put("happiness", life.stats.happiness)
            put("intellect", life.stats.intellect)
            put("appearance", life.stats.appearance)
            put("creativity", life.stats.creativity)
            put("energy", life.stats.energy)
            put("fitness", life.stats.fitness)
            put("diseaseRisk", life.stats.diseaseRisk)
            put("stress", life.stats.stress)
            put("anxiety", life.stats.anxiety)
            put("karma", life.stats.karma)
        })

        root.put("skills", JSONObject().apply {
            put("deductionLaw", life.skills.deductionLaw)
            put("engineeringTech", life.skills.engineeringTech)
            put("paintingArt", life.skills.paintingArt)
            put("musicPerformance", life.skills.musicPerformance)
            put("actingStage", life.skills.actingStage)
            put("athleticDriving", life.skills.athleticDriving)
            put("culinaryCraft", life.skills.culinaryCraft)
            put("persuasionMedia", life.skills.persuasionMedia)
        })

        root.put("socialChannel", JSONObject().apply {
            put("handle", life.socialChannel.handle)
            put("niche", life.socialChannel.niche.name)
            put("followers", life.socialChannel.followers)
            put("reputation", life.socialChannel.reputation)
            put("totalStreams", life.socialChannel.totalStreams)
            put("merchLaunched", life.socialChannel.merchLaunched)
            put("merchName", life.socialChannel.merchName)
            put("sponsorshipTier", life.socialChannel.sponsorshipTier)
            put("activeSponsorName", life.socialChannel.activeSponsorName)
            put("annualSponsorIncome", life.socialChannel.annualSponsorIncome)
            put("gearLevel", life.socialChannel.gearLevel)
            put("currentTrendTitle", life.socialChannel.currentTrendTitle)
            put("currentTrendNiche", life.socialChannel.currentTrendNiche.name)
        })

        root.put("relationships", JSONArray().apply {
            life.relationships.forEach { rel ->
                put(JSONObject().apply {
                    put("id", rel.id)
                    put("name", rel.name)
                    put("role", rel.role)
                    put("age", rel.age)
                    put("closeness", rel.closeness)
                    put("isPartner", rel.isPartner)
                    put("isSpouse", rel.isSpouse)
                    put("isAlive", rel.isAlive)
                })
            }
        })

        root.put("entries", JSONArray().apply {
            life.entries.take(120).forEach { entry ->
                put(JSONObject().apply {
                    put("id", entry.id)
                    put("age", entry.age)
                    put("year", entry.year)
                    put("category", entry.category.name)
                    put("title", entry.title)
                    put("detail", entry.detail)
                })
            }
        })

        root.put("achievements", JSONArray(life.unlockedAchievementIds.toList()))
        return root.toString()
    }

    private fun decodeLifeState(raw: String): LifeState {
        val root = JSONObject(raw)
        val appObj = root.getJSONObject("appearance")
        val stObj = root.getJSONObject("stats")
        val chObj = root.getJSONObject("socialChannel")
        val relArr = root.getJSONArray("relationships")
        val entArr = root.getJSONArray("entries")
        val achArr = root.getJSONArray("achievements")

        val rels = buildList {
            for (i in 0 until relArr.length()) {
                val o = relArr.getJSONObject(i)
                add(
                    Relationship(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        role = o.getString("role"),
                        age = o.getInt("age"),
                        closeness = o.getInt("closeness"),
                        isPartner = o.optBoolean("isPartner", false),
                        isSpouse = o.optBoolean("isSpouse", false),
                        isAlive = o.optBoolean("isAlive", true)
                    )
                )
            }
        }

        val entries = buildList {
            for (i in 0 until entArr.length()) {
                val o = entArr.getJSONObject(i)
                add(
                    JournalEntry(
                        id = o.getString("id"),
                        age = o.getInt("age"),
                        year = o.getInt("year"),
                        category = runCatching { EntryCategory.valueOf(o.getString("category")) }
                            .getOrDefault(EntryCategory.MILESTONE),
                        title = o.getString("title"),
                        detail = o.getString("detail")
                    )
                )
            }
        }

        val achs = buildSet {
            for (i in 0 until achArr.length()) {
                add(achArr.getString(i))
            }
        }

        val lineage = buildList {
            val arr = root.optJSONArray("lineageHistory")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val chArr = o.optJSONArray("childrenNames")
                    val hiArr = o.optJSONArray("highlights")
                    add(
                        AncestorRecord(
                            lifeId = o.optString("lifeId", ""),
                            generation = o.optInt("generation", i + 1),
                            characterName = o.optString("characterName", "Ancestor"),
                            birthplace = o.optString("birthplace", root.getString("birthplace")),
                            birthYear = o.optInt("birthYear", 1970),
                            deathYear = o.optInt("deathYear", 2040),
                            ageReached = o.optInt("ageReached", 70),
                            careerTitle = o.optString("careerTitle", "Chronicler"),
                            legacyScore = o.optInt("legacyScore", 100),
                            netWorth = o.optInt("netWorth", 0),
                            peakFame = o.optInt("peakFame", 0),
                            followers = o.optInt("followers", 0),
                            spouseName = o.optString("spouseName", ""),
                            childrenNames = buildList {
                                if (chArr != null) {
                                    for (j in 0 until chArr.length()) add(chArr.getString(j))
                                }
                            },
                            highlights = buildList {
                                if (hiArr != null) {
                                    for (j in 0 until hiArr.length()) add(hiArr.getString(j))
                                }
                            },
                            appearance = AvatarAppearance(
                                skinToneIndex = o.optInt("skinToneIndex", 2),
                                hairStyleIndex = o.optInt("hairStyleIndex", 1),
                                hairColorIndex = o.optInt("hairColorIndex", 0),
                                outfitIndex = o.optInt("outfitIndex", 1),
                                accessoryIndex = o.optInt("accessoryIndex", 0),
                                backdropIndex = o.optInt("backdropIndex", 0)
                            )
                        )
                    )
                }
            }
        }

        val missionObj = root.optJSONObject("missionState")
        val decodedMissions = buildList {
            val arr = missionObj?.optJSONArray("missions")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val mType = runCatching { MissionType.valueOf(o.optString("type", MissionType.READ_BOOK.name)) }
                        .getOrDefault(MissionType.READ_BOOK)
                    add(
                        DailyMission(
                            id = o.optString("id", mType.name),
                            type = mType,
                            title = o.optString("title", mType.defaultTitle),
                            description = o.optString("description", mType.description),
                            xpReward = o.optInt("xpReward", mType.xpReward),
                            sparkleReward = o.optInt("sparkleReward", mType.sparkleReward),
                            cashReward = o.optInt("cashReward", mType.cashReward),
                            isCompleted = o.optBoolean("isCompleted", false)
                        )
                    )
                }
            }
        }
        val decodedMissionState = DailyMissionState(
            dateKey = missionObj?.optString("dateKey", "") ?: "",
            missions = decodedMissions,
            dailyBonusClaimed = missionObj?.optBoolean("dailyBonusClaimed", false) ?: false,
            totalXp = missionObj?.optInt("totalXp", 0) ?: 0
        )

        val skObj = root.optJSONObject("skills")
        val decodedSkills = CharacterSkills(
            deductionLaw = skObj?.optInt("deductionLaw", 18) ?: 18,
            engineeringTech = skObj?.optInt("engineeringTech", 18) ?: 18,
            paintingArt = skObj?.optInt("paintingArt", 18) ?: 18,
            musicPerformance = skObj?.optInt("musicPerformance", 18) ?: 18,
            actingStage = skObj?.optInt("actingStage", 18) ?: 18,
            athleticDriving = skObj?.optInt("athleticDriving", 18) ?: 18,
            culinaryCraft = skObj?.optInt("culinaryCraft", 18) ?: 18,
            persuasionMedia = skObj?.optInt("persuasionMedia", 18) ?: 18
        )

        return LifeState(
            lifeId = root.getString("lifeId"),
            characterName = root.getString("characterName"),
            birthplace = root.getString("birthplace"),
            birthYear = root.getInt("birthYear"),
            currentAge = root.getInt("currentAge"),
            isAlive = root.getBoolean("isAlive"),
            pronouns = root.optString("pronouns", "they/them"),
            trait = runCatching { TemperamentTrait.valueOf(root.getString("trait")) }
                .getOrDefault(TemperamentTrait.CURIOUS),
            appearance = AvatarAppearance(
                skinToneIndex = appObj.getInt("skinToneIndex"),
                hairStyleIndex = appObj.getInt("hairStyleIndex"),
                hairColorIndex = appObj.getInt("hairColorIndex"),
                outfitIndex = appObj.getInt("outfitIndex"),
                accessoryIndex = appObj.getInt("accessoryIndex"),
                backdropIndex = appObj.getInt("backdropIndex")
            ),
            stats = LifeStats(
                health = stObj.getInt("health"),
                happiness = stObj.getInt("happiness"),
                intellect = stObj.getInt("intellect"),
                appearance = stObj.getInt("appearance"),
                creativity = stObj.getInt("creativity"),
                energy = stObj.getInt("energy"),
                fitness = stObj.getInt("fitness"),
                diseaseRisk = stObj.getInt("diseaseRisk"),
                stress = stObj.getInt("stress"),
                anxiety = stObj.getInt("anxiety"),
                karma = stObj.getInt("karma")
            ),
            skills = decodedSkills,
            diet = runCatching { DietPlan.valueOf(root.getString("diet")) }.getOrDefault(DietPlan.WHOLE_FOOD),
            exercise = runCatching { ExerciseRoutine.valueOf(root.getString("exercise")) }.getOrDefault(ExerciseRoutine.QUIET_WALKS),
            sleep = runCatching { SleepSchedule.valueOf(root.getString("sleep")) }.getOrDefault(SleepSchedule.RESTORATIVE),
            netWorth = root.getInt("netWorth").coerceAtLeast(0),
            loanDebt = root.optInt("loanDebt", 0).coerceAtLeast(0),
            hasDegree = root.getBoolean("hasDegree"),
            inSchool = root.getBoolean("inSchool"),
            educationType = runCatching { EducationType.valueOf(root.optString("educationType", EducationType.NONE.name)) }.getOrDefault(EducationType.NONE),
            completedEducation = runCatching { EducationType.valueOf(root.optString("completedEducation", EducationType.NONE.name)) }.getOrDefault(EducationType.NONE),
            careerTrack = runCatching { CareerTrack.valueOf(root.getString("careerTrack")) }.getOrDefault(CareerTrack.UNEMPLOYED),
            careerRung = root.getInt("careerRung"),
            careerPerformance = root.getInt("careerPerformance"),
            fame = root.optInt("fame", 0),
            peakFame = root.optInt("peakFame", 0),
            crimeActive = root.optBoolean("crimeActive", false),
            crimeRank = root.optInt("crimeRank", 0),
            notoriety = root.optInt("notoriety", 0),
            prisonYearsLeft = root.optInt("prisonYearsLeft", 0),
            generation = root.optInt("generation", 1),
            parentName = root.optString("parentName", ""),
            parentLifeId = root.optString("parentLifeId", ""),
            lineageHistory = lineage,
            missionState = decodedMissionState,
            socialChannel = SocialChannel(
                handle = chObj.getString("handle"),
                niche = runCatching { SocialNiche.valueOf(chObj.getString("niche")) }.getOrDefault(SocialNiche.DAILY_VLOGGING),
                followers = chObj.getInt("followers"),
                reputation = chObj.getInt("reputation"),
                totalStreams = chObj.getInt("totalStreams"),
                merchLaunched = chObj.getBoolean("merchLaunched"),
                merchName = chObj.getString("merchName"),
                sponsorshipTier = chObj.getString("sponsorshipTier"),
                activeSponsorName = chObj.getString("activeSponsorName"),
                annualSponsorIncome = chObj.getInt("annualSponsorIncome"),
                gearLevel = chObj.getInt("gearLevel"),
                currentTrendTitle = chObj.getString("currentTrendTitle"),
                currentTrendNiche = runCatching { SocialNiche.valueOf(chObj.getString("currentTrendNiche")) }
                    .getOrDefault(SocialNiche.DAILY_VLOGGING)
            ),
            relationships = rels,
            entries = entries,
            unlockedAchievementIds = achs,
            epitaph = root.optString("epitaph", ""),
            deathCause = root.optString("deathCause", ""),
            economyMultiplier = root.optDouble("economyMultiplier", 1.0).toFloat(),
            endOfLifeAdClaimed = root.optBoolean("endOfLifeAdClaimed", false)
        )
    }
}
