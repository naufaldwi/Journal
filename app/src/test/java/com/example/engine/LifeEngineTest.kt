package com.example.engine

import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.DailyMission
import com.example.model.DailyMissionState
import com.example.model.DietPlan
import com.example.model.EducationType
import com.example.model.EntryCategory
import com.example.model.ExerciseRoutine
import com.example.model.JournalEntry
import com.example.model.LifeStage
import com.example.model.LifeStats
import com.example.model.MissionType
import com.example.model.Relationship
import com.example.model.SleepSchedule
import com.example.model.StreamFormat
import com.example.model.TemperamentTrait
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeEngineTest {

    @Test
    fun createNewLife_inJakarta_usesIndonesianHeritageAndNaming() {
        val result = LifeEngine.createNewLife(
            characterName = "",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2000,
            pronouns = "they/them",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats()
        )
        val life = result.updatedLife
        assertEquals("Jakarta, Indonesia", life.birthplace)
        assertTrue(life.characterName.isNotBlank())
        assertTrue(life.relationships.isNotEmpty())
        val country = CountryCatalog.findCountryForBirthplace("Jakarta, Indonesia")
        assertEquals("Indonesia", country.countryName)
        assertTrue(country.cities.contains("Yogyakarta, Indonesia"))
    }

    @Test
    fun yearByYearAging_updatesAvatarStageHealthAndRelationshipsAtMilestones() {
        var life = LifeEngine.createNewLife(
            characterName = "Dimas Nugroho",
            birthplace = "Yogyakarta, Indonesia",
            birthYear = 2000,
            pronouns = "he/him",
            trait = TemperamentTrait.EMPATHETIC,
            appearance = AvatarAppearance(outfitIndex = 0),
            startingStats = LifeStats()
        ).updatedLife

        assertEquals(LifeStage.INFANCY, life.lifeStage)
        val initialOutfit = life.appearance.outfitIndex
        val initialMomAge = life.relationships.first { it.role == "Mother" }.age

        // Age 0 -> 6 (Childhood milestone adds Childhood Friend & ages parents)
        repeat(6) {
            life = LifeEngine.ageUp(life).updatedLife
        }
        assertEquals(6, life.currentAge)
        assertEquals(LifeStage.CHILDHOOD, life.lifeStage)
        assertTrue(life.relationships.any { it.role == "Childhood Friend" })
        assertEquals(initialMomAge + 6, life.relationships.first { it.role == "Mother" }.age)

        // Age 6 -> 13 (Adolescence milestone updates outfit & adds School Peer)
        repeat(7) {
            life = LifeEngine.ageUp(life).updatedLife.copy(pendingPrompt = null)
        }
        assertEquals(13, life.currentAge)
        assertEquals(LifeStage.ADOLESCENCE, life.lifeStage)
        assertTrue(life.relationships.any { it.role == "School Peer" })
        assertNotEquals(initialOutfit, life.appearance.outfitIndex)

        // Midlife & Senior milestone health drift check (Sedentary vs Active)
        val at34 = life.copy(
            currentAge = 34,
            exercise = ExerciseRoutine.SEDENTARY,
            diet = DietPlan.CONVENIENCE,
            sleep = SleepSchedule.NIGHT_OWL,
            relationships = life.relationships + Relationship(
                id = "partner_1",
                name = "Sekar Lestari",
                role = "Romantic Interest",
                age = 33,
                closeness = 81,
                isPartner = true
            )
        )
        val at35 = LifeEngine.ageUp(at34).updatedLife
        assertEquals(LifeStage.MIDLIFE, at35.lifeStage)
        assertTrue(at35.stats.diseaseRisk > at34.stats.diseaseRisk)
        assertEquals("Committed Partner", at35.relationships.first { it.id == "partner_1" }.role)

        val at60 = LifeEngine.ageUp(at35.copy(currentAge = 59)).updatedLife
        assertEquals(LifeStage.SENIOR, at60.lifeStage)
        assertEquals(0, at60.appearance.outfitIndex)
        assertTrue(at60.entries.any { it.age == 60 && it.title.contains("Silver Horizon") })
    }

    @Test
    fun artAndMusicConservatory_unlocksFameCareersAndFameDecays() {
        val base = LifeEngine.createNewLife(
            characterName = "Ayu Kusuma",
            birthplace = "Bandung, Indonesia",
            birthYear = 2000,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(creativity = 85)
        ).updatedLife.copy(currentAge = 22, completedEducation = EducationType.MUSIC_ACADEMY, hasDegree = true)

        val (guitaristLife, msg) = LifeEngine.applyForCareer(base, CareerTrack.BAND_GUITARIST)
        assertTrue(msg.contains("Garage Band Guitarist"))
        assertEquals(CareerTrack.BAND_GUITARIST, guitaristLife.careerTrack)

        val boosted = LifeEngine.boostFameAndCraft(guitaristLife.copy(fame = 75)).updatedLife
        assertTrue(boosted.fame >= 80)
        assertTrue(boosted.unlockedAchievementIds.contains("fame_icon"))

        val nextYear = LifeEngine.ageUp(boosted.copy(careerPerformance = 50)).updatedLife
        assertTrue("Fame should decay naturally without maintenance", nextYear.fame < boosted.fame)
    }

    @Test
    fun countryCrimeSyndicates_matchCulturalRegions() {
        assertEquals("Preman", CountryCatalog.syndicateForBirthplace("Jakarta, Indonesia").localTerm)
        assertEquals("Triad", CountryCatalog.syndicateForBirthplace("Kowloon, Hong Kong").localTerm)
        assertEquals("Yakuza", CountryCatalog.syndicateForBirthplace("Osaka, Japan").localTerm)
        assertEquals("Mafia", CountryCatalog.syndicateForBirthplace("Naples, Italy").localTerm)
        assertEquals("Gangster", CountryCatalog.syndicateForBirthplace("Brooklyn, USA").localTerm)

        val adult = LifeEngine.createNewLife(
            characterName = "Kenji Sato",
            birthplace = "Osaka, Japan",
            birthYear = 1995,
            pronouns = "he/him",
            trait = TemperamentTrait.AMBITIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats()
        ).updatedLife.copy(currentAge = 21)

        val (joined, msg) = LifeEngine.joinCrimeSyndicate(adult)
        assertTrue(joined.crimeActive)
        assertTrue(msg.contains("Yakuza"))
    }

    @Test
    fun liveStream_awardsVirtualCurrencyAndFollowers() {
        val initial = LifeEngine.createNewLife(
            characterName = "Nadia Kusuma",
            birthplace = "Bandung, Indonesia",
            birthYear = 2002,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats()
        ).updatedLife

        val outcome = LifeEngine.completeLiveStream(
            life = initial,
            format = StreamFormat.COZY_SESSION,
            topic = "Evening Acoustic & Journal",
            engagedChat = true
        )
        assertTrue(outcome.sparklesEarned > 0)
        assertTrue(outcome.cashEarned > 0)
        assertTrue(outcome.followersGained > 0)
        assertEquals(1, outcome.updatedLife.socialChannel.totalStreams)
        assertTrue(outcome.updatedLife.unlockedAchievementIds.contains("first_stream"))
    }

    @Test
    fun wellnessChoices_impactPhysicalAndMentalStats() {
        val initial = LifeEngine.createNewLife(
            characterName = "Raka Wijaya",
            birthplace = "Yogyakarta, Indonesia",
            birthYear = 1998,
            pronouns = "he/him",
            trait = TemperamentTrait.RESILIENT,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(stress = 60, anxiety = 55, diseaseRisk = 30)
        ).updatedLife.copy(
            diet = DietPlan.PLANT_FORWARD,
            exercise = ExerciseRoutine.YOGA_MINDFUL,
            sleep = SleepSchedule.RESTORATIVE
        )

        val therapy = LifeEngine.performWellnessAction(initial, "therapy").updatedLife
        assertTrue(therapy.stats.stress < initial.stats.stress)
        assertTrue(therapy.stats.anxiety < initial.stats.anxiety)

        val checkup = LifeEngine.performWellnessAction(therapy, "checkup").updatedLife
        assertTrue(checkup.stats.diseaseRisk < therapy.stats.diseaseRisk)
    }

    @Test
    fun multiGenerationLineage_tracksAncestorsAccomplishmentsAndDynastyAchievement() {
        val gen1Base = LifeEngine.createNewLife(
            characterName = "Sari Hartono",
            birthplace = "Surabaya, Indonesia",
            birthYear = 1995,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats()
        ).updatedLife.copy(
            currentAge = 78,
            isAlive = false,
            netWorth = 100_000,
            peakFame = 82,
            relationships = listOf(
                Relationship(id = "sp1", name = "Bima Hartono", role = "Spouse", age = 77, closeness = 90, isPartner = true, isSpouse = true),
                Relationship(id = "ch1", name = "Laras Hartono", role = "Child", age = 46, closeness = 92)
            ),
            entries = listOf(
                JournalEntry(
                    id = "hist_2020",
                    age = 25,
                    year = 2020,
                    category = EntryCategory.HISTORICAL,
                    title = "The Quiet Remote Shift",
                    detail = "Neighborhood walks and online communities surged."
                ),
                JournalEntry(
                    id = "fame_2035",
                    age = 40,
                    year = 2035,
                    category = EntryCategory.FAME,
                    title = "Biennale Exhibition",
                    detail = "Painted a celebrated retrospective."
                )
            )
        )

        val gen2 = LifeEngine.continueAsHeir(gen1Base).updatedLife
        assertEquals(2, gen2.generation)
        assertEquals("Laras Hartono", gen2.characterName)
        assertEquals("Sari Hartono", gen2.parentName)
        assertEquals(1, gen2.lineageHistory.size)
        assertEquals(45_000, gen2.netWorth)
        assertTrue(gen2.lineageHistory.first().highlights.any { it.contains("The Quiet Remote Shift") })
        assertEquals("Bima Hartono", gen2.lineageHistory.first().spouseName)

        val gen3Turn = LifeEngine.continueAsHeir(gen2.copy(currentAge = 65, isAlive = false))
        val gen3 = gen3Turn.updatedLife
        assertEquals(3, gen3.generation)
        assertEquals(2, gen3.lineageHistory.size)
        assertTrue(gen3.unlockedAchievementIds.contains("dynasty_three"))
        assertTrue(gen3Turn.newlyUnlockedAchievements.contains("dynasty_three"))
    }

    @Test
    fun dailyMissions_generateThreeTasksAwardXpRewardsAndBonus() {
        val base = LifeEngine.createNewLife(
            characterName = "Tari Wijaya",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2001,
            pronouns = "she/her",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(fitness = 60, intellect = 60)
        ).updatedLife

        assertEquals(3, base.missionState.missions.size)
        assertEquals(3, base.missionState.missions.map { it.type }.distinct().size)

        val fixedMissions = listOf(
            DailyMission("m1", MissionType.GO_TO_GYM, MissionType.GO_TO_GYM.defaultTitle, MissionType.GO_TO_GYM.description, 180, 35),
            DailyMission("m2", MissionType.POST_SOCIAL, MissionType.POST_SOCIAL.defaultTitle, MissionType.POST_SOCIAL.description, 180, 40),
            DailyMission("m3", MissionType.READ_BOOK, MissionType.READ_BOOK.defaultTitle, MissionType.READ_BOOK.description, 160, 30)
        )
        val dayKey = "2026-11-15"
        var state = base.copy(
            missionState = DailyMissionState(dateKey = dayKey, missions = fixedMissions, totalXp = 0)
        )

        val gymOut = LifeEngine.executeDailyMissionAction(state, "m1", dayKey)
        state = gymOut.updatedLife
        assertEquals(180, state.missionState.totalXp)
        assertEquals(65, state.stats.fitness)
        assertEquals(35, gymOut.sparklesEarned)

        val socialOut = LifeEngine.progressMissionByType(state, MissionType.POST_SOCIAL, dayKey)
        state = socialOut.updatedLife
        assertEquals(360, state.missionState.totalXp)

        val bookOut = LifeEngine.executeDailyMissionAction(state, "m3", dayKey)
        state = bookOut.updatedLife
        assertEquals(520, state.missionState.totalXp)
        assertEquals(2, state.missionState.level)
        assertEquals(2, bookOut.leveledUpTo)
        assertTrue(state.missionState.allCompleted)

        val bonusOut = LifeEngine.claimDailyMissionBonus(state, dayKey)
        state = bonusOut.updatedLife
        assertTrue(state.missionState.dailyBonusClaimed)
        assertEquals(640, state.missionState.totalXp)
        assertEquals(60, bonusOut.sparklesEarned)

        // New day generates 3 fresh tasks while keeping totalXp
        val nextDay = LifeEngine.ensureDailyMissions(state, "2026-11-16")
        assertEquals("2026-11-16", nextDay.missionState.dateKey)
        assertEquals(3, nextDay.missionState.missions.size)
        assertEquals(640, nextDay.missionState.totalXp)
        assertTrue(nextDay.missionState.missions.none { it.isCompleted })
    }
}
