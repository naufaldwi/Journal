package com.example.engine

import com.example.model.AvatarAppearance
import com.example.model.BondCategory
import com.example.model.CareerTrack
import com.example.model.CharacterGender
import com.example.model.CharacterSkills
import com.example.model.DailyEventCategory
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
import com.example.model.RelationshipPriority
import com.example.model.RomanceMethod
import com.example.model.RomanceOrientation
import com.example.model.SkillActivity
import com.example.model.SleepSchedule
import com.example.model.SocialChannel
import com.example.model.SocialPostTopic
import com.example.model.StreamFormat
import com.example.model.TemperamentTrait
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
                isPartner = true,
                interactedThisYear = true
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
        ).updatedLife.copy(
            currentAge = 22,
            completedEducation = EducationType.MUSIC_ACADEMY,
            hasDegree = true,
            skills = CharacterSkills(musicPerformance = 55)
        )

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
    fun dailyMissions_gatedUntilAge13ThenGenerateThreeTasksAndAwardRewards() {
        val infant = LifeEngine.createNewLife(
            characterName = "Tari Wijaya",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2001,
            pronouns = "she/her",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(fitness = 60, intellect = 60)
        ).updatedLife

        // Under age 13: Daily Missions must remain empty/locked
        assertEquals(0, infant.currentAge)
        assertTrue(infant.missionState.missions.isEmpty())
        val preTeen = LifeEngine.ensureDailyMissions(infant.copy(currentAge = 12))
        assertTrue(preTeen.missionState.missions.isEmpty())

        // Starting at age 13: Daily Missions generate 3 distinct tasks
        val base = LifeEngine.ensureDailyMissions(infant.copy(currentAge = 13))
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

    @Test
    fun schoolAutoBoostsSkills_andSkillsGateCareers() {
        val youngAdult = LifeEngine.createNewLife(
            characterName = "Farhan Pratama",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2000,
            pronouns = "he/him",
            trait = TemperamentTrait.RESILIENT,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(intellect = 65, creativity = 55)
        ).updatedLife.copy(currentAge = 18, netWorth = 10_000)

        // Cannot enter Law career before training Deduction & Law skill + degree
        val (deniedLife, deniedMsg) = LifeEngine.applyForCareer(youngAdult, CareerTrack.LAW)
        assertEquals(CareerTrack.UNEMPLOYED, deniedLife.careerTrack)
        assertTrue(deniedMsg.contains("Requires"))

        // Enrolling in Law School automatically boosts Deduction & Law, Persuasion, and Intellect
        val (inLawSchool, lawMsg) = LifeEngine.enrollInEducation(youngAdult, EducationType.LAW_SCHOOL)
        assertTrue(inLawSchool.inSchool)
        assertTrue(lawMsg.contains("Law School"))
        assertTrue(inLawSchool.skills.deductionLaw > youngAdult.skills.deductionLaw)
        assertTrue(inLawSchool.skills.persuasionMedia > youngAdult.skills.persuasionMedia)
        assertTrue(inLawSchool.stats.intellect > youngAdult.stats.intellect)

        // Aging in Law School continues auto-boosting until graduation at age 22
        var lawGrad = inLawSchool
        repeat(4) {
            lawGrad = LifeEngine.ageUp(lawGrad).updatedLife.copy(pendingPrompt = null)
        }
        assertFalse(lawGrad.inSchool)
        assertTrue(lawGrad.hasDegree)
        assertEquals(EducationType.LAW_SCHOOL, lawGrad.completedEducation)
        assertTrue(lawGrad.skills.deductionLaw >= CareerTrack.LAW.minPrimarySkill)

        val (attorneyLife, joinMsg) = LifeEngine.applyForCareer(lawGrad, CareerTrack.LAW)
        assertEquals(CareerTrack.LAW, attorneyLife.careerTrack)
        assertTrue(joinMsg.contains("Legal Paralegal"))

        // Art & Design Conservatory auto-boosts painting, music, acting & creativity
        val artApplicant = youngAdult.copy(netWorth = 10_000)
        val (artEnrolled, _) = LifeEngine.enrollInEducation(artApplicant, EducationType.ART_UNI)
        assertTrue(artEnrolled.skills.paintingArt > artApplicant.skills.paintingArt)
        assertTrue(artEnrolled.skills.musicPerformance > artApplicant.skills.musicPerformance)
        assertTrue(artEnrolled.skills.actingStage > artApplicant.skills.actingStage)
        assertTrue(artEnrolled.stats.creativity > artApplicant.stats.creativity)
    }

    @Test
    fun skillActivity_increasesSkillBarAndRespectsCashGuard() {
        val student = LifeEngine.createNewLife(
            characterName = "Nadia Putri",
            birthplace = "Bandung, Indonesia",
            birthYear = 2004,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(energy = 80)
        ).updatedLife.copy(currentAge = 16, netWorth = 20)

        // Paid activity blocked when cash is insufficient (never drops below 0)
        val (blockedRes, blockedMsg) = LifeEngine.performSkillActivity(student, SkillActivity.PAINT_STUDIO)
        assertEquals(20, blockedRes.updatedLife.netWorth)
        assertTrue(blockedMsg.contains("Need $50"))

        // Free activity succeeds and boosts skill bar + intellect
        val (trialRes, trialMsg) = LifeEngine.performSkillActivity(student, SkillActivity.MOCK_TRIAL)
        assertTrue(trialRes.updatedLife.skills.deductionLaw > student.skills.deductionLaw)
        assertTrue(trialMsg.contains("/100"))
        assertTrue(trialRes.updatedLife.netWorth >= 0)
    }

    @Test
    fun moneyNeverGoesNegative_andOnlyAdultsCanTakeLoans() {
        val minor = LifeEngine.createNewLife(
            characterName = "Budi Santoso",
            birthplace = "Surabaya, Indonesia",
            birthYear = 2010,
            pronouns = "he/him",
            trait = TemperamentTrait.AMBITIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats()
        ).updatedLife.copy(currentAge = 15, netWorth = 10)

        // Minor cannot borrow
        val (stillMinor, minorMsg) = LifeEngine.takeLoan(minor, 5_000)
        assertEquals(0, stillMinor.loanDebt)
        assertEquals(10, stillMinor.netWorth)
        assertTrue(minorMsg.contains("18+"))

        // Adult with $0 cannot go negative on expenses or aging
        val brokeAdult = minor.copy(currentAge = 24, netWorth = 0)
        val agedBroke = LifeEngine.ageUp(brokeAdult).updatedLife
        assertTrue(agedBroke.netWorth >= 0)
        val wellnessBlocked = LifeEngine.performWellnessAction(brokeAdult, "retreat").updatedLife
        assertTrue(wellnessBlocked.netWorth >= 0)

        // Adult can take a loan, use cash, and repay cleanly without going negative
        val (borrowed, loanMsg) = LifeEngine.takeLoan(brokeAdult, 15_000)
        assertEquals(15_000, borrowed.netWorth)
        assertEquals(15_000, borrowed.loanDebt)
        assertTrue(loanMsg.contains("Approved"))

        val (repaid, repayMsg) = LifeEngine.repayLoan(borrowed.copy(netWorth = 4_000), 10_000)
        assertEquals(0, repaid.netWorth)
        assertEquals(11_000, repaid.loanDebt)
        assertTrue(repayMsg.contains("Paid"))
    }

    @Test
    fun indonesianNames_respectGenderedListsAndParentRoles() {
        val indonesia = CountryCatalog.findCountryForBirthplace("Jakarta, Indonesia")
        assertTrue(indonesia.maleFirstNames.contains("Dimas"))
        assertFalse(indonesia.maleFirstNames.contains("Putri"))
        assertTrue(indonesia.femaleFirstNames.contains("Putri"))
        assertFalse(indonesia.femaleFirstNames.contains("Dimas"))

        repeat(20) {
            val (maleFirst, _) = CountryCatalog.randomNameForBirthplace("Jakarta, Indonesia", isMale = true)
            assertTrue(indonesia.maleFirstNames.contains(maleFirst))
            val (femaleFirst, _) = CountryCatalog.randomNameForBirthplace("Jakarta, Indonesia", isMale = false)
            assertTrue(indonesia.femaleFirstNames.contains(femaleFirst))
        }

        val life = LifeEngine.createNewLife(
            characterName = "Dimas Nugroho",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2002,
            pronouns = "he/him",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.MALE
        ).updatedLife
        val mom = life.relationships.first { it.role == "Mother" }
        val dad = life.relationships.first { it.role == "Father" }
        assertEquals(CharacterGender.FEMALE, mom.gender)
        assertEquals(CharacterGender.MALE, dad.gender)
    }

    @Test
    fun romanceSystem_enforcesAge14OneTimeOnboardingRejectionAndAntiCheatingCheck() {
        val teen = LifeEngine.createNewLife(
            characterName = "Raka Putra",
            birthplace = "Bandung, Indonesia",
            birthYear = 2006,
            pronouns = "he/him",
            trait = TemperamentTrait.EMPATHETIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 80, stress = 15),
            gender = CharacterGender.MALE
        ).updatedLife.copy(
            currentAge = 13,
            relationships = listOf(
                Relationship(
                    id = "friend_f",
                    name = "Putri Ayu",
                    role = "Classmate",
                    age = 14,
                    closeness = 70,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.SCHOOL
                ),
                Relationship(
                    id = "adult_f",
                    name = "Sari Dewi",
                    role = "Professor",
                    age = 34,
                    closeness = 80,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.SCHOOL
                )
            )
        )

        // Blocked before age 14 & onboarding prompt hasn't been shown yet
        assertFalse(teen.orientationPromptShown)
        val (_, age13Msg) = LifeEngine.seekRomance(teen, RomanceMethod.BLIND_DATE)
        assertTrue(age13Msg.contains("14"))

        // At age 14, requires orientation first; dismissing onboarding marks orientationPromptShown once
        val at14 = teen.copy(currentAge = 14)
        val dismissedOnce = LifeEngine.dismissOrientationOnboarding(at14)
        assertTrue(dismissedOnce.orientationPromptShown)
        val (_, orientMsg) = LifeEngine.seekRomance(dismissedOnce, RomanceMethod.BLIND_DATE)
        assertTrue(orientMsg.contains("orientation", ignoreCase = true))

        // Set orientation once -> heterosexual & marks onboarding shown
        val oriented = LifeEngine.setOrientation(at14, RomanceOrientation.HETEROSEXUAL).updatedLife
        assertEquals(RomanceOrientation.HETEROSEXUAL, oriented.orientation)
        assertTrue(oriented.orientationPromptShown)

        // Age-appropriate & role-appropriate check: minor cannot match with adult professor
        assertTrue(LifeEngine.isCompatibleRomanceTarget(oriented, oriented.relationships.first { it.id == "friend_f" }))
        assertFalse(LifeEngine.isCompatibleRomanceTarget(oriented, oriented.relationships.first { it.id == "adult_f" }))

        // Rejection hurts mood & friend closeness on failed confession
        val (rejectedTurn, rejMsg) = LifeEngine.seekRomance(
            life = oriented,
            method = RomanceMethod.FRIEND_TO_PARTNER,
            friendId = "friend_f",
            outcomeRoll = 0.01f
        )
        assertTrue(rejMsg.contains("turned down"))
        assertTrue(rejectedTurn.updatedLife.stats.happiness < oriented.stats.happiness)
        assertTrue(rejectedTurn.updatedLife.relationships.first { it.id == "friend_f" }.closeness < 70)

        // Rejection on Blind Date & Dating App lowers happiness & raises stress
        val (blindMissTurn, blindMissMsg) = LifeEngine.seekRomance(
            life = oriented,
            method = RomanceMethod.BLIND_DATE,
            outcomeRoll = 0.01f
        )
        assertTrue(blindMissMsg.contains("No Spark"))
        assertTrue(blindMissTurn.updatedLife.stats.happiness < oriented.stats.happiness)

        // Successful romance produces partner of compatible gender & enforces anti-cheating check
        val (datedTurn, _) = LifeEngine.seekRomance(
            life = oriented.copy(currentAge = 19),
            method = RomanceMethod.DATING_APP,
            outcomeRoll = 0.99f
        )
        val datingLife = datedTurn.updatedLife
        assertEquals(1, datingLife.activePartnerCount)
        assertEquals(CharacterGender.FEMALE, datingLife.relationships.first { it.isPartner }.gender)

        // Anti-cheating check strictly blocks dating someone else while already partnered
        val (blockedTurn, antiCheatMsg) = LifeEngine.seekRomance(
            life = datingLife,
            method = RomanceMethod.BLIND_DATE,
            outcomeRoll = 0.99f
        )
        assertTrue(antiCheatMsg.contains("Cheating prevented"))
        assertEquals(1, blockedTurn.updatedLife.activePartnerCount)

        // Breaking up cleanly allows pursuing a new romance (e.g., friend-to-partner)
        val currentPartnerId = datingLife.relationships.first { it.isPartner }.id
        val singleAgain = LifeEngine.breakUpWithPartner(datingLife, currentPartnerId).updatedLife.copy(
            relationships = listOf(
                Relationship(
                    id = "adult_friend",
                    name = "Nadia Putri",
                    role = "College Classmate",
                    age = 19,
                    closeness = 78,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.SCHOOL
                )
            )
        )
        assertEquals(0, singleAgain.activePartnerCount)
        val (friendConverted, friendMsg) = LifeEngine.seekRomance(
            life = singleAgain,
            method = RomanceMethod.FRIEND_TO_PARTNER,
            friendId = "adult_friend",
            outcomeRoll = 0.99f
        )
        assertTrue(friendMsg.contains("Now dating Nadia Putri"))
        assertEquals(1, friendConverted.updatedLife.activePartnerCount)
    }

    @Test
    fun bondNeglectDecaysCloseness_hatredCausesMisery_andMultipleChildrenSupported() {
        val base = LifeEngine.createNewLife(
            characterName = "Dewi Sari",
            birthplace = "Surabaya, Indonesia",
            birthYear = 1998,
            pronouns = "she/her",
            trait = TemperamentTrait.RESILIENT,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 82, stress = 15),
            gender = CharacterGender.FEMALE
        ).updatedLife.copy(
            currentAge = 26,
            relationships = listOf(
                Relationship(id = "f1", name = "Ayu", role = "Friend", age = 26, closeness = 65, interactedThisYear = false),
                Relationship(id = "h1", name = "Colleague 1", role = "Office Colleague", age = 28, closeness = 15, category = BondCategory.WORK),
                Relationship(id = "h2", name = "Colleague 2", role = "Team Manager", age = 34, closeness = 18, category = BondCategory.WORK),
                Relationship(id = "h3", name = "Neighbor", role = "Neighbor", age = 30, closeness = 20, category = BondCategory.FRIEND),
                Relationship(id = "sp", name = "Bagas", role = "Spouse", age = 27, closeness = 90, isPartner = true, isSpouse = true, gender = CharacterGender.MALE, category = BondCategory.ROMANCE, interactedThisYear = true)
            )
        )

        // Neglected friend loses closeness while hated contacts trigger misery penalty on ageUp
        val aged = LifeEngine.ageUp(base).updatedLife
        val neglectedFriend = aged.relationships.first { it.id == "f1" }
        assertTrue("Neglected contact closeness should decay", neglectedFriend.closeness < 65)
        assertTrue("Neglected counter should increment", neglectedFriend.yearsNeglected >= 1)
        assertTrue(aged.isMiserableFromHatred)
        assertTrue(aged.entries.any { it.title == "Surrounded by Bitterness" })

        // Direct interaction resets yearsNeglected
        val reconnected = LifeEngine.interactRelationship(aged, "f1", "time").updatedLife
        assertEquals(0, reconnected.relationships.first { it.id == "f1" }.yearsNeglected)

        // Multiple children supported cleanly
        val beforeChildren = reconnected.childrenCount
        val (child1Turn, _) = LifeEngine.tryHaveOrAdoptChild(reconnected, "sp")
        val (child2Turn, _) = LifeEngine.tryHaveOrAdoptChild(child1Turn.updatedLife, "sp")
        assertEquals(beforeChildren + 2, child2Turn.updatedLife.childrenCount)
    }

    @Test
    fun annualGlobalNewsFeed_updatesEveryYearWithRegionEraAndCategories() {
        var life = LifeEngine.createNewLife(
            characterName = "Sakura Tanaka",
            birthplace = "Tokyo, Japan",
            birthYear = 2000,
            pronouns = "she/her",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.FEMALE
        ).updatedLife

        assertEquals(1, life.globalNewsFeed.size)
        assertEquals("East Asia", life.globalNewsFeed.first().region)

        repeat(3) {
            life = LifeEngine.ageUp(life).updatedLife.copy(pendingPrompt = null)
        }
        assertEquals(4, life.globalNewsFeed.size)
        assertEquals(2003, life.globalNewsFeed.first().year)
        assertEquals("East Asia", life.globalNewsFeed.first().region)
        assertEquals(3, life.globalNewsFeed.map { it.category }.distinct().size)

        // Era & region sensitivity: 1975 Cold War vs 2040 Orbital/Fusion, Europe vs Southeast Asia
        val coldWarEurope = LifeEngine.generateAnnualGlobalNews(1975, "London, UK")
        val futureSea = LifeEngine.generateAnnualGlobalNews(2041, "Jakarta, Indonesia")
        assertEquals("Europe", coldWarEurope.region)
        assertEquals("Southeast Asia", futureSea.region)
        assertNotEquals(coldWarEurope.headline, futureSea.headline)
        assertTrue(
            "Future tech or orbital news should reflect modern/future era",
            futureSea.headline.contains("Fusion", ignoreCase = true) ||
                futureSea.headline.contains("Orbital", ignoreCase = true) ||
                futureSea.headline.contains("Longevity", ignoreCase = true) ||
                futureSea.headline.contains("Civic", ignoreCase = true)
        )

        // Legacy/empty save backfill produces dispatch for currentYear and birthplace region
        val emptyFeedSave = life.copy(globalNewsFeed = emptyList())
        val backfilled = LifeEngine.ensureGlobalNewsFeed(emptyFeedSave)
        assertEquals(1, backfilled.globalNewsFeed.size)
        assertEquals(backfilled.currentYear, backfilled.globalNewsFeed.first().year)
        assertEquals("East Asia", backfilled.globalNewsFeed.first().region)
    }

    @Test
    fun moodTrends_recordsUpToTenYearsWithCareerStressAndEncounterMarkers() {
        var life = LifeEngine.createNewLife(
            characterName = "Aditya Pratama",
            birthplace = "Jakarta, Indonesia",
            birthYear = 1995,
            pronouns = "he/him",
            trait = TemperamentTrait.RESILIENT,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 80, stress = 18, anxiety = 14),
            gender = CharacterGender.MALE
        ).updatedLife.copy(
            currentAge = 20,
            careerTrack = CareerTrack.MEDICINE,
            careerRung = 0,
            skills = CharacterSkills(engineeringTech = 75, deductionLaw = 70)
        )

        repeat(12) {
            life = LifeEngine.ageUp(life).updatedLife.let { next ->
                next.copy(
                    pendingPrompt = null,
                    relationships = next.relationships.map {
                        if (it.category == BondCategory.WORK) it.copy(closeness = 80, interactedThisYear = true) else it
                    }
                )
            }
        }

        assertEquals(32, life.currentAge)
        assertEquals(10, life.moodHistory.size)
        assertEquals(23, life.moodHistory.first().age)
        assertEquals(32, life.moodHistory.last().age)
        assertTrue(life.moodHistory.any { it.careerStressLoad >= 14 })
        assertTrue(life.moodHistory.last().careerStressLoad >= 14)

        val encounterTurn = LifeEngine.triggerRandomEncounter(life, roll = 0.60f).updatedLife
        val latestSnap = encounterTurn.moodHistory.last()
        assertTrue(latestSnap.hadRandomEncounter)
        assertTrue(latestSnap.encounterLabel.isNotBlank())

        // Legacy backfill builds up to 10 snapshots when moodHistory is empty
        val legacySave = encounterTurn.copy(moodHistory = emptyList())
        val restored = LifeEngine.ensureMoodHistory(legacySave)
        assertEquals(10, restored.moodHistory.size)
        assertEquals(32, restored.moodHistory.last().age)
    }

    @Test
    fun socialStream_scalesLikesAndNpcCommentsByPopularityAndRelations() {
        val child = LifeEngine.createNewLife(
            characterName = "Raka Putra",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2005,
            pronouns = "he/him",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.MALE
        ).updatedLife.copy(currentAge = 11)

        // Blocked below age 13
        val (_, blockedMsg) = LifeEngine.postToSocialStream(child, SocialPostTopic.LIFE, "Hello!")
        assertTrue(blockedMsg.contains("Age 13"))
        assertTrue(child.socialFeed.isEmpty())

        // Low-popularity teen at age 15 with minimal circle
        val lowPopLife = child.copy(
            currentAge = 15,
            fame = 0,
            socialChannel = SocialChannel(handle = "@raka_notes", followers = 12, reputation = 50),
            relationships = listOf(
                Relationship(id = "f_low", name = "Budi Santoso", role = "Classmate", age = 15, closeness = 58, category = BondCategory.SCHOOL)
            )
        )
        val lowScore = LifeEngine.computePopularityScore(lowPopLife)
        val (lowResult, _) = LifeEngine.postToSocialStream(lowPopLife, SocialPostTopic.LIFE, "Studying late tonight", rollSeed = 42)
        val lowPost = lowResult.updatedLife.socialFeed.first()
        assertEquals(SocialPostTopic.LIFE, lowPost.topic)
        assertTrue(lowPost.comments.isNotEmpty())

        // High-popularity adult with fame, big following, close partner, colleague & hostile rival
        val highPopLife = child.copy(
            currentAge = 27,
            careerTrack = CareerTrack.BAND_GUITARIST,
            careerRung = 2,
            fame = 88,
            socialChannel = SocialChannel(handle = "@raka_live", followers = 85_000, reputation = 92),
            relationships = listOf(
                Relationship(id = "partner_1", name = "Nadia Putri", role = "Partner", age = 26, closeness = 95, isPartner = true, gender = CharacterGender.FEMALE, category = BondCategory.ROMANCE),
                Relationship(id = "work_1", name = "Dimas Wijaya", role = "Studio Producer", age = 36, closeness = 86, category = BondCategory.WORK),
                Relationship(id = "hater_1", name = "Bagus Pratama", role = "Rival Colleague", age = 28, closeness = 12, category = BondCategory.WORK)
            )
        )
        val highScore = LifeEngine.computePopularityScore(highPopLife)
        assertTrue("High popularity ($highScore) should exceed low popularity ($lowScore)", highScore > lowScore)
        assertTrue(highScore >= 72)

        val (highCareerTurn, _) = LifeEngine.postToSocialStream(
            highPopLife,
            SocialPostTopic.CAREER,
            "Sold out our acoustic set in Jakarta!",
            rollSeed = 17
        )
        val careerPost = highCareerTurn.updatedLife.socialFeed.first()
        assertEquals(SocialPostTopic.CAREER, careerPost.topic)
        assertTrue("Popular post likes (${careerPost.likes}) should far exceed low popularity likes (${lowPost.likes})", careerPost.likes > lowPost.likes * 10)
        assertEquals(4, careerPost.comments.size)
        assertTrue(careerPost.comments.any { it.authorName == "Nadia Putri" || it.authorName == "Dimas Wijaya" })
        assertTrue(careerPost.comments.any { it.authorName == "Bagus Pratama" && !it.isSupportive })

        // Posting Relationship update preserves chronological prepended feed
        val (relTurn, _) = LifeEngine.postToSocialStream(
            highCareerTurn.updatedLife,
            SocialPostTopic.RELATIONSHIP,
            "Dinner date with Nadia tonight",
            rollSeed = 5
        )
        assertEquals(2, relTurn.updatedLife.socialFeed.size)
        assertEquals(SocialPostTopic.RELATIONSHIP, relTurn.updatedLife.socialFeed.first().topic)
    }

    @Test
    fun dailyRandomEvents_tailorByTraitCoverMoneyLostItemAndAcquaintanceAndAffectStats() {
        val baseCurious = LifeEngine.createNewLife(
            characterName = "Raka Putra",
            birthplace = "Jakarta, Indonesia",
            birthYear = 2000,
            pronouns = "he/him",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 72, intellect = 60, stress = 25, anxiety = 20, karma = 60),
            gender = CharacterGender.MALE
        ).updatedLife

        // Gated below age 6
        val infantCheck = LifeEngine.checkDailyRandomEvent(baseCurious, dateKey = "2026-11-17", roll = 0.1f)
        assertEquals(null, infantCheck.pendingDailyEvent)

        // Curious trait -> stumbling upon money inside a used book
        val adultCurious = baseCurious.copy(currentAge = 24, netWorth = 1000)
        val triggeredCurious = LifeEngine.checkDailyRandomEvent(adultCurious, dateKey = "2026-11-17", roll = 0.1f)
        val curiousEv = triggeredCurious.pendingDailyEvent!!
        assertEquals(TemperamentTrait.CURIOUS, curiousEv.trait)
        assertEquals(DailyEventCategory.MONEY_FIND, curiousEv.category)

        // Idempotent for same dateKey
        val duplicateCheck = LifeEngine.checkDailyRandomEvent(triggeredCurious, dateKey = "2026-11-17", roll = 0.8f)
        assertEquals(curiousEv.id, duplicateCheck.pendingDailyEvent?.id)

        // Resolving primary choice boosts cash, happiness & intellect and clears pendingDailyEvent
        val resolvedCurious = LifeEngine.resolveDailyRandomEvent(triggeredCurious, curiousEv.choices.first()).updatedLife
        assertEquals(null, resolvedCurious.pendingDailyEvent)
        assertEquals("2026-11-17", resolvedCurious.lastDailyEventDateKey)
        assertTrue(resolvedCurious.netWorth > adultCurious.netWorth)
        assertTrue(resolvedCurious.stats.happiness > adultCurious.stats.happiness)
        assertTrue(resolvedCurious.stats.intellect > adultCurious.stats.intellect)
        assertTrue(resolvedCurious.entries.any { it.category == EntryCategory.RANDOM_EVENT && it.title == curiousEv.title })

        // Empathetic trait -> lost item can boost or reduce mood depending on choice
        val empLife = adultCurious.copy(trait = TemperamentTrait.EMPATHETIC, lastDailyEventDateKey = "")
        val triggeredEmp = LifeEngine.checkDailyRandomEvent(empLife, dateKey = "2026-11-18", roll = 0.15f)
        val empEv = triggeredEmp.pendingDailyEvent!!
        assertEquals(DailyEventCategory.LOST_ITEM, empEv.category)
        val pawnChoice = empEv.choices.last()
        val reducedMoodLife = LifeEngine.resolveDailyRandomEvent(triggeredEmp, pawnChoice).updatedLife
        assertTrue(reducedMoodLife.stats.happiness < empLife.stats.happiness)
        assertTrue(reducedMoodLife.stats.anxiety > empLife.stats.anxiety)
        assertTrue(reducedMoodLife.stats.karma < empLife.stats.karma)

        // Resilient trait -> encountering an old acquaintance adds/updates relationship & improves mood/fitness
        val resLife = adultCurious.copy(trait = TemperamentTrait.RESILIENT, lastDailyEventDateKey = "")
        val triggeredRes = LifeEngine.checkDailyRandomEvent(resLife, dateKey = "2026-11-19", roll = 0.85f)
        val resEv = triggeredRes.pendingDailyEvent!!
        assertEquals(DailyEventCategory.OLD_ACQUAINTANCE, resEv.category)
        val relBefore = resLife.relationships.size
        val resolvedRes = LifeEngine.resolveDailyRandomEvent(triggeredRes, resEv.choices.first()).updatedLife
        assertTrue(resolvedRes.stats.happiness > resLife.stats.happiness)
        assertTrue(resolvedRes.stats.stress < resLife.stats.stress)
        assertTrue(resolvedRes.relationships.size >= relBefore)

        // Artistic trait -> negative daily twist reduces happiness & cash and raises stress
        val artLife = adultCurious.copy(trait = TemperamentTrait.ARTISTIC, lastDailyEventDateKey = "")
        val triggeredArt = LifeEngine.checkDailyRandomEvent(artLife, dateKey = "2026-11-20", roll = 0.5f)
        val artEv = triggeredArt.pendingDailyEvent!!
        val resolvedArt = LifeEngine.resolveDailyRandomEvent(triggeredArt, artEv.choices.first()).updatedLife
        assertTrue(resolvedArt.stats.happiness < artLife.stats.happiness)
        assertTrue(resolvedArt.stats.stress > artLife.stats.stress)
        assertTrue(resolvedArt.netWorth < artLife.netWorth)
    }

    @Test
    fun relationshipSettings_prioritizesInteractionAndGiftingAcrossBoostAndAgeUp() {
        val base = LifeEngine.createNewLife(
            characterName = "Dina Kusuma",
            birthplace = "Surabaya, Indonesia",
            birthYear = 1997,
            pronouns = "she/her",
            trait = TemperamentTrait.EMPATHETIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 72, stress = 30, anxiety = 22),
            gender = CharacterGender.FEMALE
        ).updatedLife.copy(
            currentAge = 27,
            netWorth = 500,
            relationships = listOf(
                Relationship(
                    id = "r_interact",
                    name = "Raka Putra",
                    role = "Close Friend",
                    age = 27,
                    closeness = 60,
                    yearsNeglected = 2,
                    category = BondCategory.FRIEND
                ),
                Relationship(
                    id = "r_gift",
                    name = "Bagas Pratama",
                    role = "Partner",
                    age = 28,
                    closeness = 65,
                    isPartner = true,
                    yearsNeglected = 1,
                    gender = CharacterGender.MALE,
                    category = BondCategory.ROMANCE
                ),
                Relationship(
                    id = "r_std",
                    name = "Budi Santoso",
                    role = "Office Colleague",
                    age = 31,
                    closeness = 60,
                    interactedThisYear = false,
                    category = BondCategory.WORK
                )
            )
        )

        // Assigning priorities updates state cleanly
        val withInteract = LifeEngine.setRelationshipPriority(base, "r_interact", RelationshipPriority.INTERACTION)
        val withBoth = LifeEngine.setRelationshipPriority(withInteract, "r_gift", RelationshipPriority.GIFTING)
        assertEquals(RelationshipPriority.INTERACTION, withBoth.relationships.first { it.id == "r_interact" }.priority)
        assertEquals(RelationshipPriority.GIFTING, withBoth.relationships.first { it.id == "r_gift" }.priority)
        assertEquals(RelationshipPriority.STANDARD, withBoth.relationships.first { it.id == "r_std" }.priority)

        // Executing prioritized bond boost raises closeness (+8 interact, +14 gift), deducts $150, clears neglect, and leaves Standard untouched
        val (boostTurn, boostMsg) = LifeEngine.executePrioritizedBondBoost(withBoth)
        val boosted = boostTurn.updatedLife
        assertEquals(350, boosted.netWorth)
        assertEquals(68, boosted.relationships.first { it.id == "r_interact" }.closeness)
        assertEquals(0, boosted.relationships.first { it.id == "r_interact" }.yearsNeglected)
        assertEquals(79, boosted.relationships.first { it.id == "r_gift" }.closeness)
        assertEquals(0, boosted.relationships.first { it.id == "r_gift" }.yearsNeglected)
        assertEquals(60, boosted.relationships.first { it.id == "r_std" }.closeness)
        assertTrue(boostMsg.contains("checked in with 1 prioritized contact(s)", ignoreCase = true))
        assertTrue(boostMsg.contains("sent $150 gifts to 1 contact(s)", ignoreCase = true))
        assertTrue(boosted.stats.happiness > withBoth.stats.happiness)

        // Cash shortfall for gifting-only priority blocks gifting cleanly
        val giftOnlyLowCash = withBoth.copy(
            netWorth = 80,
            relationships = withBoth.relationships.map {
                if (it.id == "r_interact") it.copy(priority = RelationshipPriority.STANDARD) else it
            }
        )
        val (shortfallTurn, shortfallMsg) = LifeEngine.executePrioritizedBondBoost(giftOnlyLowCash)
        assertEquals(80, shortfallTurn.updatedLife.netWorth)
        assertTrue(shortfallMsg.contains("Need $150"))

        // Annual ageUp auto-maintains INTERACTION (+3) and auto-gifts GIFTING (+9 & -$150) while STANDARD drifts down
        val preAge = withBoth.copy(
            netWorth = 25_000,
            relationships = withBoth.relationships.map { it.copy(interactedThisYear = false, yearsNeglected = 0) }
        )
        val aged = LifeEngine.ageUp(preAge).updatedLife
        val agedInteract = aged.relationships.first { it.id == "r_interact" }
        val agedGift = aged.relationships.first { it.id == "r_gift" }
        val agedStd = aged.relationships.first { it.id == "r_std" }
        assertEquals(0, agedInteract.yearsNeglected)
        assertTrue("Interact priority should boost closeness on ageUp", agedInteract.closeness > 60)
        assertEquals(0, agedGift.yearsNeglected)
        assertTrue("Gift priority should boost closeness on ageUp", agedGift.closeness > 65)
        assertTrue("Standard untouched contact should neglect & lose closeness on ageUp", agedStd.closeness < 60)
    }
}
