package com.example.engine

import com.example.model.AncestorRecord
import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.ChoiceOption
import com.example.model.DailyMission
import com.example.model.DailyMissionState
import com.example.model.DietPlan
import com.example.model.EducationType
import com.example.model.EntryCategory
import com.example.model.ExerciseRoutine
import com.example.model.JournalEntry
import com.example.model.LifeChoicePrompt
import com.example.model.LifeStage
import com.example.model.LifeState
import com.example.model.LifeStats
import com.example.model.MissionType
import com.example.model.Relationship
import com.example.model.SleepSchedule
import com.example.model.SocialChannel
import com.example.model.SocialNiche
import com.example.model.StreamFormat
import com.example.model.TemperamentTrait
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class TurnResult(
    val updatedLife: LifeState,
    val newlyUnlockedAchievements: Set<String>,
    val sparklesEarned: Int
)

data class MissionOutcome(
    val updatedLife: LifeState,
    val completedMission: DailyMission?,
    val xpEarned: Int,
    val sparklesEarned: Int,
    val leveledUpTo: Int? = null
)

data class StreamOutcome(
    val updatedLife: LifeState,
    val peakViewers: Int,
    val followersGained: Int,
    val sparklesEarned: Int,
    val cashEarned: Int,
    val newlyUnlockedAchievements: Set<String>
)

object LifeEngine {

    val birthplaces: List<String> get() = CountryCatalog.allBirthplaces

    private val trendPool = listOf(
        "Cozy Desk Setups" to SocialNiche.TECH_MINIMALISM,
        "24h Pantry Cook-Off" to SocialNiche.CULINARY_ARTS,
        "Slow Morning Routine" to SocialNiche.DAILY_VLOGGING,
        "Indie Farm Sim Revival" to SocialNiche.COZY_GAMING,
        "Annotated Classics" to SocialNiche.BOOKISH_ESSAYS,
        "Rainy Day Acoustic" to SocialNiche.INDIE_MUSIC
    )

    fun createNewLife(
        characterName: String,
        birthplace: String,
        birthYear: Int,
        pronouns: String,
        trait: TemperamentTrait,
        appearance: AvatarAppearance,
        startingStats: LifeStats
    ): TurnResult {
        val cleanName = characterName.trim().ifBlank {
            val (fn, ln) = CountryCatalog.randomNameForBirthplace(birthplace)
            "$fn $ln"
        }.take(60)
        val lastName = cleanName.substringAfterLast(" ", CountryCatalog.randomCompanionLastName(birthplace))
        val handleBase = cleanName.lowercase().replace("[^a-z0-9]".toRegex(), "").take(14).ifBlank { "journaler" }

        val traitStats = when (trait) {
            TemperamentTrait.CURIOUS -> startingStats.copy(intellect = startingStats.intellect + 10)
            TemperamentTrait.RESILIENT -> startingStats.copy(health = startingStats.health + 8, stress = startingStats.stress - 5)
            TemperamentTrait.EMPATHETIC -> startingStats.copy(karma = startingStats.karma + 12, happiness = startingStats.happiness + 6)
            TemperamentTrait.ARTISTIC -> startingStats.copy(creativity = startingStats.creativity + 12)
            TemperamentTrait.AMBITIOUS -> startingStats.copy(energy = startingStats.energy + 8, intellect = startingStats.intellect + 5)
        }.clamped()

        val mom = Relationship(
            id = "mom_${UUID.randomUUID().toString().take(6)}",
            name = "${CountryCatalog.randomCompanionFirstName(birthplace)} $lastName",
            role = "Mother",
            age = Random.nextInt(24, 36),
            closeness = Random.nextInt(78, 95)
        )
        val dad = Relationship(
            id = "dad_${UUID.randomUUID().toString().take(6)}",
            name = "${CountryCatalog.randomCompanionFirstName(birthplace)} $lastName",
            role = "Father",
            age = Random.nextInt(25, 38),
            closeness = Random.nextInt(74, 93)
        )
        val initialRels = mutableListOf(mom, dad)
        if (Random.nextFloat() < 0.55f) {
            initialRels.add(
                Relationship(
                    id = "sib_${UUID.randomUUID().toString().take(6)}",
                    name = "${CountryCatalog.randomCompanionFirstName(birthplace)} $lastName",
                    role = "Older Sibling",
                    age = Random.nextInt(2, 7),
                    closeness = Random.nextInt(60, 86)
                )
            )
        }

        val initialEntry = JournalEntry(
            id = "e_0_${UUID.randomUUID().toString().take(6)}",
            age = 0,
            year = birthYear,
            category = EntryCategory.BIRTH,
            title = "A Quiet Beginning",
            detail = "$cleanName was born in $birthplace in $birthYear ($pronouns), carrying a ${trait.label.lowercase()} spirit."
        )

        val rawLife = LifeState(
            lifeId = "life_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            characterName = cleanName,
            birthplace = birthplace,
            birthYear = birthYear,
            currentAge = 0,
            isAlive = true,
            pronouns = pronouns,
            trait = trait,
            appearance = appearance,
            stats = traitStats,
            netWorth = 500,
            socialChannel = SocialChannel(
                handle = "@$handleBase",
                niche = SocialNiche.DAILY_VLOGGING,
                currentTrendTitle = trendPool.first().first,
                currentTrendNiche = trendPool.first().second
            ),
            relationships = initialRels,
            entries = listOf(initialEntry),
            unlockedAchievementIds = setOf("first_breath")
        )

        val withMissions = ensureDailyMissions(rawLife)
        val (evaluated, newlyUnlocked, reward) = evaluateAchievements(withMissions)
        return TurnResult(evaluated, newlyUnlocked + "first_breath", reward + 50)
    }

    fun continueAsHeir(parentLife: LifeState): TurnResult {
        val spouseName = parentLife.relationships.firstOrNull { it.isSpouse }?.name
            ?: parentLife.relationships.firstOrNull { it.isPartner }?.name
            ?: ""
        val childrenNames = parentLife.relationships.filter { it.role == "Child" }.map { it.name }

        val entryHighlights = parentLife.entries
            .filter {
                it.category == EntryCategory.HISTORICAL ||
                    it.category == EntryCategory.MILESTONE ||
                    it.category == EntryCategory.FAME ||
                    it.category == EntryCategory.CAREER ||
                    it.category == EntryCategory.LEGACY
            }
            .distinctBy { it.title }
            .take(5)
            .map { "${it.year}: ${it.title}" }
            .toMutableList()

        if (entryHighlights.isEmpty()) {
            entryHighlights.add("Lived ${parentLife.currentAge} years as ${parentLife.currentCareerTitle}")
        } else if (parentLife.peakFame >= 50 && entryHighlights.size < 5) {
            entryHighlights.add("Reached ${parentLife.peakFame}/100 Peak Fame")
        }

        val ancestorRecord = AncestorRecord(
            lifeId = parentLife.lifeId,
            generation = parentLife.generation,
            characterName = parentLife.characterName,
            birthplace = parentLife.birthplace,
            birthYear = parentLife.birthYear,
            deathYear = parentLife.currentYear,
            ageReached = parentLife.currentAge,
            careerTitle = parentLife.currentCareerTitle,
            legacyScore = parentLife.legacyScore,
            netWorth = parentLife.netWorth,
            peakFame = parentLife.peakFame,
            followers = parentLife.socialChannel.followers,
            spouseName = spouseName,
            childrenNames = childrenNames,
            highlights = entryHighlights.take(6),
            appearance = parentLife.appearance
        )

        val nextGen = parentLife.generation + 1
        val surname = parentLife.characterName.substringAfterLast(
            " ",
            CountryCatalog.randomCompanionLastName(parentLife.birthplace)
        )
        val heirName = childrenNames.firstOrNull()?.take(60)
            ?: "${CountryCatalog.randomCompanionFirstName(parentLife.birthplace)} $surname".take(60)
        val heirBirthYear = (parentLife.birthYear + 28).coerceAtMost(parentLife.currentYear.coerceAtLeast(parentLife.birthYear + 18))
        val inheritedEstate = (parentLife.netWorth.coerceAtLeast(0) * 0.45).toInt().coerceAtLeast(1500)
        val inheritedFollowers = (parentLife.socialChannel.followers * 0.08).toInt().coerceAtMost(25_000)

        val handleBase = heirName.lowercase().replace("[^a-z0-9]".toRegex(), "").take(14).ifBlank { "journaler" }
        val heirTrait = TemperamentTrait.entries.random()
        val heirPronouns = listOf("they/them", "she/her", "he/him").random()
        val heirAppearance = parentLife.appearance.copy(
            hairStyleIndex = (parentLife.appearance.hairStyleIndex + Random.nextInt(1, 5)) % 6,
            outfitIndex = Random.nextInt(0, 5),
            accessoryIndex = 0,
            backdropIndex = (parentLife.appearance.backdropIndex + 1) % 5
        )

        val parentRel = Relationship(
            id = "parent_main_${UUID.randomUUID().toString().take(6)}",
            name = parentLife.characterName,
            role = "Parent",
            age = parentLife.currentAge,
            closeness = 88,
            isAlive = parentLife.isAlive
        )
        val heirRels = mutableListOf(parentRel)
        if (spouseName.isNotBlank()) {
            val coParent = parentLife.relationships.firstOrNull { it.isSpouse || it.isPartner }
            heirRels.add(
                Relationship(
                    id = "parent_co_${UUID.randomUUID().toString().take(6)}",
                    name = spouseName,
                    role = "Parent",
                    age = coParent?.age ?: parentLife.currentAge,
                    closeness = 84,
                    isAlive = coParent?.isAlive ?: false
                )
            )
        }
        childrenNames.drop(1).forEachIndexed { idx, sibName ->
            heirRels.add(
                Relationship(
                    id = "sib_heir_${idx}_${UUID.randomUUID().toString().take(5)}",
                    name = sibName,
                    role = "Older Sibling",
                    age = idx + 2,
                    closeness = 76
                )
            )
        }

        val initialEntry = JournalEntry(
            id = "e_0_gen${nextGen}_${UUID.randomUUID().toString().take(6)}",
            age = 0,
            year = heirBirthYear,
            category = EntryCategory.BIRTH,
            title = "Generation $nextGen Begins",
            detail = "$heirName was born in ${parentLife.birthplace} ($heirBirthYear), carrying forward ${parentLife.characterName}'s lineage and $$inheritedEstate estate."
        )

        val rawHeir = LifeState(
            lifeId = "life_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            characterName = heirName,
            birthplace = parentLife.birthplace,
            birthYear = heirBirthYear,
            currentAge = 0,
            isAlive = true,
            pronouns = heirPronouns,
            trait = heirTrait,
            appearance = heirAppearance,
            stats = LifeStats().clamped(),
            netWorth = inheritedEstate,
            generation = nextGen,
            parentName = parentLife.characterName,
            parentLifeId = parentLife.lifeId,
            lineageHistory = (parentLife.lineageHistory + ancestorRecord).takeLast(25),
            missionState = DailyMissionState(totalXp = parentLife.missionState.totalXp),
            socialChannel = SocialChannel(
                handle = "@${handleBase}_g$nextGen",
                niche = parentLife.socialChannel.niche,
                followers = inheritedFollowers,
                currentTrendTitle = trendPool.first().first,
                currentTrendNiche = trendPool.first().second
            ),
            relationships = heirRels,
            entries = listOf(initialEntry),
            unlockedAchievementIds = setOf("first_breath")
        )

        val withMissions = ensureDailyMissions(rawHeir)
        val (evaluated, newlyUnlocked, reward) = evaluateAchievements(withMissions)
        return TurnResult(evaluated, newlyUnlocked + "first_breath", reward + 50)
    }

    fun ageUp(life: LifeState): TurnResult {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0)

        val prevStage = life.lifeStage
        val nextAge = life.currentAge + 1
        val nextStage = LifeStage.fromAge(nextAge)
        val nextYear = life.birthYear + nextAge
        val newEntries = mutableListOf<JournalEntry>()
        var updatedPrompt: LifeChoicePrompt? = null

        var prisonLeft = life.prisonYearsLeft
        val inPrisonThisYear = prisonLeft > 0
        if (prisonLeft > 0) {
            prisonLeft -= 1
            if (prisonLeft == 0) {
                newEntries.add(
                    JournalEntry(
                        id = "e_${nextAge}_release",
                        age = nextAge,
                        year = nextYear,
                        category = EntryCategory.CRIME,
                        title = "Released from Prison",
                        detail = "Completed your sentence and stepped back out into ${life.birthplace}."
                    )
                )
            }
        }

        // 1. Visual Avatar & Stage Milestone Evolution
        var updatedAppearance = life.appearance
        if (nextStage != prevStage) {
            val evolvedOutfit = when (nextStage) {
                LifeStage.ADOLESCENCE -> (life.appearance.outfitIndex + 1) % 5
                LifeStage.YOUNG_ADULT -> (life.appearance.outfitIndex + 1) % 5
                LifeStage.MIDLIFE -> 3
                LifeStage.SENIOR -> 0
                else -> life.appearance.outfitIndex
            }
            updatedAppearance = life.appearance.copy(outfitIndex = evolvedOutfit)
            newEntries.add(
                JournalEntry(
                    id = "e_stage_${nextAge}",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.MILESTONE,
                    title = "Entered ${nextStage.label}",
                    detail = stageTransitionDetail(nextStage, nextAge)
                )
            )
        }

        // 2. Age-Milestone Health & Lifestyle Calculations
        var s = life.stats
        val diet = life.diet
        val ex = life.exercise
        val sleep = life.sleep

        val careerStress = when {
            inPrisonThisYear -> 16
            life.inSchool -> 8
            life.crimeActive -> 15
            life.careerTrack == CareerTrack.MEDICINE || life.careerTrack == CareerTrack.RACE_DRIVER -> 14
            life.careerTrack.isFameTrack -> 11
            life.careerTrack != CareerTrack.UNEMPLOYED -> 9
            else -> 2
        }
        val resilientFactor = if (life.trait == TemperamentTrait.RESILIENT) -3 else 0
        val warmBondsCount = life.relationships.count { it.isAlive && it.closeness >= 65 }

        val milestoneFitnessShift = when (nextStage) {
            LifeStage.INFANCY -> 1
            LifeStage.CHILDHOOD -> 1
            LifeStage.ADOLESCENCE -> 2
            LifeStage.YOUNG_ADULT -> 0
            LifeStage.MIDLIFE -> -2
            LifeStage.SENIOR -> -3
            LifeStage.ELDER -> -4
        }
        val milestoneRiskShift = when (nextStage) {
            LifeStage.INFANCY, LifeStage.CHILDHOOD -> -1
            LifeStage.ADOLESCENCE, LifeStage.YOUNG_ADULT -> 0
            LifeStage.MIDLIFE -> 2
            LifeStage.SENIOR -> 3
            LifeStage.ELDER -> 5
        }

        val deltaFitness = diet.fitnessDelta + ex.fitnessDelta + milestoneFitnessShift
        val deltaEnergy = diet.energyDelta + ex.energyDelta + sleep.energyDelta - (careerStress / 3) - (if (nextAge >= 70) 2 else 0)
        val deltaStress = ex.stressDelta + sleep.stressDelta + (careerStress / 2) + resilientFactor - warmBondsCount.coerceAtMost(3)
        val deltaAnxiety = ex.anxietyDelta + sleep.anxietyDelta + (if (life.netWorth < 0) 6 else -1) + resilientFactor
        val deltaRisk = diet.riskDelta + sleep.riskDelta + milestoneRiskShift + (if (s.stress > 70) 4 else -1) + (if (inPrisonThisYear) 4 else 0)

        val newFitness = (s.fitness + deltaFitness).coerceIn(5, 100)
        val newEnergy = (s.energy + deltaEnergy).coerceIn(10, 100)
        val newStress = (s.stress + deltaStress).coerceIn(0, 100)
        val newAnxiety = (s.anxiety + deltaAnxiety).coerceIn(0, 100)
        val newRisk = (s.diseaseRisk + deltaRisk).coerceIn(2, 95)

        val stageHealthBonus = when (nextStage) {
            LifeStage.INFANCY -> if (s.health < 90) 2 else 0
            LifeStage.CHILDHOOD, LifeStage.ADOLESCENCE -> 1
            LifeStage.YOUNG_ADULT -> if (ex != ExerciseRoutine.SEDENTARY) 1 else 0
            LifeStage.MIDLIFE -> 0
            LifeStage.SENIOR -> -1
            LifeStage.ELDER -> -2
        }
        val healthDrift = ((newFitness - 50) / 14) - (newRisk / 22) + stageHealthBonus - (if (nextAge > 75) (nextAge - 72) / 6 else 0)
        val happyDrift = ((warmBondsCount * 2) - (newStress / 18) - (newAnxiety / 20) + (if (life.netWorth > 10000) 2 else 0)) - (if (inPrisonThisYear) 10 else 0)

        s = s.copy(
            fitness = newFitness,
            energy = newEnergy,
            stress = newStress,
            anxiety = newAnxiety,
            diseaseRisk = newRisk,
            health = (s.health + healthDrift).coerceIn(0, 100),
            happiness = (s.happiness + happyDrift).coerceIn(0, 100),
            intellect = (s.intellect + if (life.inSchool || nextAge in 6..18) 3 else 0).coerceIn(0, 100),
            creativity = (s.creativity + if (life.trait == TemperamentTrait.ARTISTIC || life.educationType == EducationType.ART_UNI || life.educationType == EducationType.MUSIC_ACADEMY) 2 else 0).coerceIn(0, 100)
        )

        // 3. Finances, Career & Fame Decay Mechanics
        var cash = life.netWorth
        var economyMult = life.economyMultiplier
        var perf = life.careerPerformance
        var rung = life.careerRung
        val track = life.careerTrack
        var hasDegree = life.hasDegree
        var inSchool = life.inSchool
        var eduType = life.educationType
        var completedEdu = life.completedEducation
        var fame = life.fame
        var peakFame = life.peakFame

        if (fame > 0) {
            val decay = if (fame >= 70) 8 else 5
            val sustainOffset = if (!inPrisonThisYear && (track.isFameTrack || life.socialChannel.totalStreams > 0) && perf >= 72) 2 else 0
            fame = (fame - decay + sustainOffset).coerceIn(0, 100)
        }

        if (nextAge >= 18) {
            val salary = if (!inPrisonThisYear && !inSchool && track != CareerTrack.UNEMPLOYED) {
                val base = track.rungs.getOrElse(rung) { track.rungs.last() }.second
                val fameMult = if (track.isFameTrack) 1.0f + (fame * 0.008f) else 1.0f
                (base * economyMult * sleep.productivityBoost * fameMult).toInt()
            } else 0
            val livingCost = when {
                inPrisonThisYear -> 0
                inSchool -> eduType.annualCost + 2500
                else -> 16000 + diet.annualCost + (if (life.relationships.any { it.isSpouse }) 5000 else 0)
            }
            val merchPassive = if (!inPrisonThisYear && life.socialChannel.merchLaunched) {
                (life.socialChannel.followers * 0.14 * (life.socialChannel.reputation / 60.0)).toInt().coerceAtMost(95000)
            } else 0
            val sponsorIncome = if (!inPrisonThisYear) life.socialChannel.annualSponsorIncome else 0
            cash += (salary + merchPassive + sponsorIncome - livingCost)

            if (!inPrisonThisYear && !inSchool && track != CareerTrack.UNEMPLOYED) {
                val perfGain = ((s.energy - 50) / 12) + ((s.intellect - 50) / 18) + Random.nextInt(-3, 6)
                perf = (perf + perfGain).coerceIn(15, 100)
                if (perf >= 82 && rung < track.rungs.lastIndex && Random.nextFloat() < 0.45f) {
                    rung += 1
                    perf = 68
                    val newRole = track.rungs[rung].first
                    if (track.isFameTrack) {
                        fame = (fame + 14 + rung * 4).coerceIn(0, 100)
                        peakFame = maxOf(peakFame, fame)
                    }
                    newEntries.add(
                        JournalEntry(
                            id = "e_${nextAge}_promo_${Random.nextInt(999)}",
                            age = nextAge,
                            year = nextYear,
                            category = if (track.isFameTrack) EntryCategory.FAME else EntryCategory.CAREER,
                            title = "Promoted to $newRole",
                            detail = if (track.isFameTrack) {
                                "Breakthrough acclaim lifted your profile as $newRole (Fame $fame)."
                            } else {
                                "Steady craft and poise earned a promotion in ${track.title}."
                            }
                        )
                    )
                }
            }
        }

        // 4. Trend rotation & subtle organic follower growth
        val trend = trendPool.random()
        val organicFollowers = if (!inPrisonThisYear && life.socialChannel.followers > 0) {
            ((life.socialChannel.followers * 0.04).toInt() + Random.nextInt(15, 140) + (fame * 8))
        } else 0
        var updatedChannel = life.socialChannel.copy(
            followers = life.socialChannel.followers + organicFollowers,
            currentTrendTitle = trend.first,
            currentTrendNiche = trend.second
        )

        // 5. Year-by-Year Relationship Aging & Milestone Evolution
        val updatedRels = life.relationships.map { rel ->
            if (!rel.isAlive) rel
            else {
                val newRelAge = rel.age + 1
                val passedAway = newRelAge > 80 && Random.nextFloat() < ((newRelAge - 78) * 0.038f)
                if (passedAway) {
                    newEntries.add(
                        JournalEntry(
                            id = "e_${nextAge}_loss_${rel.id}",
                            age = nextAge,
                            year = nextYear,
                            category = EntryCategory.RELATIONSHIP,
                            title = "Farewell to ${rel.name}",
                            detail = "Your ${rel.role.lowercase()} passed away peacefully at age $newRelAge."
                        )
                    )
                    s = s.copy(happiness = (s.happiness - 18).coerceIn(0, 100), stress = (s.stress + 12).coerceIn(0, 100))
                    rel.copy(age = newRelAge, isAlive = false)
                } else {
                    // Organic milestone drift in relationships
                    val closenessShift = when {
                        inPrisonThisYear -> -3
                        rel.isSpouse && s.stress < 60 -> 1
                        rel.isPartner && rel.closeness >= 70 -> 2
                        nextAge == 18 && (rel.role == "Mother" || rel.role == "Father") -> -2 // Leaving the nest
                        nextAge == 60 && (rel.isSpouse || rel.role == "Child") -> 4 // Silver horizon warmth
                        nextAge >= 18 && !rel.isSpouse && s.karma < 50 -> -2
                        else -> 0
                    }
                    // Upgrade long-term partner role label organically when closeness peaks
                    val evolvedRole = when {
                        rel.isSpouse -> "Spouse"
                        rel.isPartner && (rel.closeness + closenessShift) >= 82 -> "Committed Partner"
                        rel.role == "Childhood Friend" && nextAge >= 18 -> "Lifelong Friend"
                        else -> rel.role
                    }
                    rel.copy(
                        age = newRelAge,
                        closeness = (rel.closeness + closenessShift).coerceIn(5, 100),
                        role = evolvedRole
                    )
                }
            }
        }.toMutableList()

        // 6. Key Age Milestones & Branching Prompts
        when (nextAge) {
            6 -> {
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace)
                val friend = Relationship(
                    id = "child_friend_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "Childhood Friend",
                    age = 6,
                    closeness = Random.nextInt(64, 80)
                )
                if (updatedRels.none { it.role.contains("Friend", ignoreCase = true) }) {
                    updatedRels.add(friend)
                }
                newEntries.add(
                    JournalEntry(
                        id = "e_6_school",
                        age = 6,
                        year = nextYear,
                        category = EntryCategory.MILESTONE,
                        title = "First Day of School & New Friend",
                        detail = "Started primary school and bonded with ${friend.name} during recess."
                    )
                )
            }
            13 -> {
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace)
                val teenPeer = Relationship(
                    id = "teen_peer_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "School Peer",
                    age = 13,
                    closeness = Random.nextInt(54, 72)
                )
                updatedRels.add(teenPeer)
            }
            14 -> if (!inPrisonThisYear) {
                updatedPrompt = LifeChoicePrompt(
                    id = "p_14_club",
                    category = EntryCategory.CHOICE,
                    title = "High School Horizons",
                    narrative = "At 14, you have room in your schedule for one signature afternoon pursuit.",
                    options = listOf(
                        ChoiceOption(
                            id = "opt_journal_club",
                            label = "Art Studio, Band & Camera Club",
                            effectHint = "+Creativity, +180 Followers, +5 Fame",
                            outcomeSummary = "You sketched posters, jammed after class, and shared clips online.",
                            creativityDelta = 10,
                            happinessDelta = 6,
                            followersDelta = 180,
                            fameDelta = 5
                        ),
                        ChoiceOption(
                            id = "opt_track_team",
                            label = "Cross-Country & Debate",
                            effectHint = "+Fitness, +Intellect, -Stress",
                            outcomeSummary = "Morning miles and debate rounds sharpened both stamina and focus.",
                            fitnessDelta = 10,
                            intellectDelta = 7,
                            stressDelta = -6
                        ),
                        ChoiceOption(
                            id = "opt_part_time",
                            label = "Neighborhood Bookshop Shift",
                            effectHint = "+$1,800, +Karma",
                            outcomeSummary = "Sorting dusty shelves paid out steady pocket savings and lifelong stories.",
                            cashDelta = 1800,
                            karmaDelta = 5,
                            energyDelta = -4
                        )
                    )
                )
            }
            18 -> if (!inPrisonThisYear) {
                updatedPrompt = LifeChoicePrompt(
                    id = "p_18_path",
                    category = EntryCategory.CHOICE,
                    title = "Turning Eighteen",
                    narrative = "Secondary school is behind you. Choose how to shape your next chapter.",
                    options = listOf(
                        ChoiceOption(
                            id = "opt_art_uni",
                            label = "Enroll in Art & Design Conservatory",
                            effectHint = "Unlocks Fine Artist, Actor, Model · +Creativity",
                            outcomeSummary = "You stepped into sunlit painting and theatre studios at the art conservatory.",
                            creativityDelta = 14,
                            stressDelta = 6,
                            cashDelta = -3600,
                            enrollEducation = EducationType.ART_UNI
                        ),
                        ChoiceOption(
                            id = "opt_music_academy",
                            label = "Enroll in Music Conservatory",
                            effectHint = "Unlocks Singer, Guitarist, Drummer · +Fame",
                            outcomeSummary = "You auditioned with your instrument and joined a vibrant band conservatory.",
                            creativityDelta = 12,
                            fameDelta = 8,
                            cashDelta = -3600,
                            enrollEducation = EducationType.MUSIC_ACADEMY
                        ),
                        ChoiceOption(
                            id = "opt_uni",
                            label = "Enroll in University",
                            effectHint = "Unlocks Software, Medicine, Design · +Intellect",
                            outcomeSummary = "You unpacked in a sunlit dorm and committed to university studies.",
                            intellectDelta = 12,
                            stressDelta = 8,
                            cashDelta = -4000,
                            enrollEducation = EducationType.UNIVERSITY
                        ),
                        ChoiceOption(
                            id = "opt_creator_leap",
                            label = "Go Full-Time Indie Creator",
                            effectHint = "+1,200 Followers, +150 ✦, +8 Fame",
                            outcomeSummary = "You turned your corner desk into a cozy creator studio and went live.",
                            creativityDelta = 10,
                            followersDelta = 1200,
                            sparklesDelta = 150,
                            fameDelta = 8,
                            happinessDelta = 8
                        )
                    )
                )
            }
            22 -> if (inSchool) {
                inSchool = false
                hasDegree = true
                completedEdu = eduType
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace)
                val peer = Relationship(
                    id = "grad_peer_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "Alumni Friend",
                    age = 22,
                    closeness = 70
                )
                updatedRels.add(peer)
                newEntries.add(
                    JournalEntry(
                        id = "e_22_grad",
                        age = 22,
                        year = nextYear,
                        category = EntryCategory.MILESTONE,
                        title = "${eduType.label} Graduation",
                        detail = "Completed ${eduType.label}, unlocking specialized creative and professional paths."
                    )
                )
                s = s.copy(happiness = (s.happiness + 12).coerceIn(0, 100), intellect = (s.intellect + 8).coerceIn(0, 100))
            }
            32 -> if (!inPrisonThisYear && updatedRels.any { it.isSpouse && it.isAlive } && updatedRels.none { it.role == "Child" }) {
                val (fn, _) = CountryCatalog.randomNameForBirthplace(life.birthplace)
                val lastName = life.characterName.substringAfterLast(" ", "")
                val childName = if (lastName.isNotBlank()) "$fn $lastName" else fn
                updatedRels.add(
                    Relationship(
                        id = "child_${UUID.randomUUID().toString().take(6)}",
                        name = childName,
                        role = "Child",
                        age = 0,
                        closeness = 92
                    )
                )
                newEntries.add(
                    JournalEntry(
                        id = "e_32_family",
                        age = 32,
                        year = nextYear,
                        category = EntryCategory.RELATIONSHIP,
                        title = "Welcomed $childName",
                        detail = "Your home grew warmer with the arrival of a new family member."
                    )
                )
                s = s.copy(happiness = (s.happiness + 14).coerceIn(0, 100))
            }
            40, 50 -> newEntries.add(
                JournalEntry(
                    id = "e_health_check_$nextAge",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.WELLNESS,
                    title = "Age $nextAge Vitality Milestone",
                    detail = "Midlife metabolism shifted subtly — preventive care and sleep balance matter even more now."
                )
            )
            60 -> newEntries.add(
                JournalEntry(
                    id = "e_60_horizon",
                    age = 60,
                    year = nextYear,
                    category = EntryCategory.MILESTONE,
                    title = "Silver Horizon at Sixty",
                    detail = "Silver hair and decades of relationships invite a slower, more intentional pace."
                )
            )
        }

        // Complete intensive adult education in 1 year if enrolled after age 22
        if (inSchool && nextAge > 22) {
            inSchool = false
            hasDegree = true
            completedEdu = eduType
            newEntries.add(
                JournalEntry(
                    id = "e_adult_grad_$nextAge",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.MILESTONE,
                    title = "Completed ${eduType.label}",
                    detail = "Earned your diploma in ${eduType.label}, opening fresh doors in craft and career."
                )
            )
        }

        // 7. Historical Era Events
        val historicalNote = historicalEventForYear(nextYear)
        if (historicalNote != null) {
            economyMult = (economyMult + historicalNote.second).coerceIn(0.75f, 1.35f)
            newEntries.add(
                JournalEntry(
                    id = "e_hist_$nextYear",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.HISTORICAL,
                    title = historicalNote.first,
                    detail = historicalNote.third
                )
            )
            if (historicalNote.fourthFollowerBoost > 0 && updatedChannel.followers > 0) {
                updatedChannel = updatedChannel.copy(
                    followers = updatedChannel.followers + historicalNote.fourthFollowerBoost
                )
            }
        }

        // 8. Periodic Random Life Events & Country-Specific Current Issues
        if (updatedPrompt == null && !inPrisonThisYear && nextAge >= 10 && Random.nextFloat() < 0.42f) {
            val randomOutcome = generateRandomLifeEvent(life.birthplace, nextAge, nextYear, s, cash, updatedChannel)
            if (randomOutcome.prompt != null) {
                updatedPrompt = randomOutcome.prompt
            } else if (randomOutcome.entry != null) {
                newEntries.add(randomOutcome.entry)
                s = randomOutcome.statsAfter
                cash += randomOutcome.cashDelta
                economyMult = (economyMult + randomOutcome.economyDelta).coerceIn(0.75f, 1.35f)
                updatedChannel = updatedChannel.copy(
                    followers = (updatedChannel.followers + randomOutcome.followerDelta).coerceAtLeast(0)
                )
                if (randomOutcome.newConnection != null && updatedRels.size < 35) {
                    updatedRels.add(randomOutcome.newConnection)
                }
            }
        }

        // 9. Mortality / End of Life
        var isAlive = true
        var epitaph = life.epitaph
        var deathCause = life.deathCause

        val mortalityChance = when {
            s.health <= 0 -> 1.0f
            nextAge >= 105 -> 0.45f
            nextAge >= 92 -> 0.14f + ((100 - s.health) * 0.004f)
            nextAge >= 80 -> 0.04f + (s.diseaseRisk * 0.0015f)
            nextAge >= 65 && s.health < 25 -> 0.06f
            else -> 0f
        }

        if (mortalityChance > 0f && Random.nextFloat() < mortalityChance) {
            isAlive = false
            deathCause = if (s.health <= 15) "Declining health" else "Peaceful old age"
            epitaph = buildEpitaph(life.copy(currentAge = nextAge, netWorth = cash, socialChannel = updatedChannel, peakFame = peakFame))
            newEntries.add(
                JournalEntry(
                    id = "e_legacy_$nextAge",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.LEGACY,
                    title = "The Final Page",
                    detail = "At age $nextAge ($nextYear), ${life.characterName}'s chapter closed peacefully. $epitaph"
                )
            )
        } else if (newEntries.isEmpty()) {
            newEntries.add(
                JournalEntry(
                    id = "e_quiet_$nextAge",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.MILESTONE,
                    title = quietYearTitle(nextAge),
                    detail = quietYearDetail(nextAge, life.currentCareerTitle, updatedChannel.followers)
                )
            )
        }

        val combinedEntries = (newEntries + life.entries).take(120)

        val nextLife = life.copy(
            currentAge = nextAge,
            isAlive = isAlive,
            appearance = updatedAppearance,
            stats = s.clamped(),
            netWorth = cash,
            economyMultiplier = economyMult,
            hasDegree = hasDegree,
            inSchool = inSchool,
            educationType = eduType,
            completedEducation = completedEdu,
            careerTrack = track,
            careerRung = rung,
            careerPerformance = perf,
            fame = fame,
            peakFame = peakFame,
            prisonYearsLeft = prisonLeft,
            socialChannel = updatedChannel,
            relationships = updatedRels,
            entries = combinedEntries,
            pendingPrompt = if (isAlive) updatedPrompt else null,
            epitaph = epitaph,
            deathCause = deathCause
        )

        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun resolveChoice(life: LifeState, option: ChoiceOption): TurnResult {
        val s = life.stats.copy(
            health = life.stats.health + option.healthDelta,
            happiness = life.stats.happiness + option.happinessDelta,
            intellect = life.stats.intellect + option.intellectDelta,
            creativity = life.stats.creativity + option.creativityDelta,
            energy = life.stats.energy + option.energyDelta,
            fitness = life.stats.fitness + option.fitnessDelta,
            stress = life.stats.stress + option.stressDelta,
            anxiety = life.stats.anxiety + option.anxietyDelta,
            diseaseRisk = life.stats.diseaseRisk + option.diseaseRiskDelta,
            karma = life.stats.karma + option.karmaDelta
        ).clamped()

        val updatedRels = if (option.relationshipBoostAll != 0) {
            life.relationships.map {
                if (it.isAlive) it.copy(closeness = (it.closeness + option.relationshipBoostAll).coerceIn(0, 100))
                else it
            }
        } else life.relationships

        val newEntry = JournalEntry(
            id = "e_choice_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CHOICE,
            title = option.label,
            detail = option.outcomeSummary
        )

        val nextTrack = if (option.id == "opt_creator_leap") CareerTrack.CREATOR else life.careerTrack
        val newFame = (life.fame + option.fameDelta).coerceIn(0, 100)
        val newEdu = option.enrollEducation ?: life.educationType

        val nextLife = life.copy(
            stats = s,
            netWorth = life.netWorth + option.cashDelta,
            inSchool = if (option.enrollEducation != null) true else life.inSchool,
            educationType = newEdu,
            careerTrack = nextTrack,
            careerPerformance = if (option.careerBonus) (life.careerPerformance + 14).coerceAtMost(100) else life.careerPerformance,
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            socialChannel = life.socialChannel.copy(
                followers = (life.socialChannel.followers + option.followersDelta).coerceAtLeast(0)
            ),
            relationships = updatedRels,
            entries = (listOf(newEntry) + life.entries).take(120),
            pendingPrompt = null
        )

        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward + option.sparklesDelta)
    }

    fun enrollInEducation(life: LifeState, edu: EducationType): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return life to "Cannot enroll while incarcerated."
        if (life.currentAge < 18) return life to "Available starting at age 18."
        if (life.inSchool) return life to "Already enrolled in ${life.educationType.label}."
        if (edu == EducationType.NONE) return life to "Choose a conservatory or university."

        val entry = JournalEntry(
            id = "e_edu_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.MILESTONE,
            title = "Enrolled in ${edu.label}",
            detail = "Committed to ${edu.label} (${edu.description})."
        )
        return life.copy(
            inSchool = true,
            educationType = edu,
            crimeActive = false,
            netWorth = life.netWorth - edu.annualCost,
            entries = (listOf(entry) + life.entries).take(120)
        ) to "Enrolled in ${edu.label}!"
    }

    fun applyForCareer(life: LifeState, track: CareerTrack): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return life to "Finish your sentence before applying for jobs."
        if (life.currentAge < 18) return life to "Wait until age 18 to start a career."
        if (life.inSchool) return life to "Finish your studies first (age 22 or next year)."

        val hasMatchingEdu = life.completedEducation == track.preferredEducation || life.hasDegree
        if (track.requiresDegree && !hasMatchingEdu && life.stats.intellect < 78) {
            return life to "${track.title} requires ${track.preferredEducation.label} or 78+ Intellect."
        }
        if ((track == CareerTrack.FINE_ART || track == CareerTrack.ACTING || track == CareerTrack.MODELING) &&
            life.completedEducation != EducationType.ART_UNI && life.stats.creativity < 72
        ) {
            return life to "${track.title} recommends Art & Design Conservatory or 72+ Creativity."
        }
        if ((track == CareerTrack.MUSIC_SINGER || track == CareerTrack.BAND_GUITARIST || track == CareerTrack.BAND_DRUMMER) &&
            life.completedEducation != EducationType.MUSIC_ACADEMY && life.stats.creativity < 70
        ) {
            return life to "${track.title} recommends Music Conservatory or 70+ Creativity."
        }

        val startFame = if (track.isFameTrack) maxOf(life.fame, 12) else life.fame
        val entry = JournalEntry(
            id = "e_job_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Joined ${track.title}",
            detail = "Began a new chapter as ${track.rungs.first().first}."
        )
        return life.copy(
            careerTrack = track,
            careerRung = 0,
            careerPerformance = 64,
            crimeActive = false,
            fame = startFame,
            peakFame = maxOf(life.peakFame, startFame),
            entries = (listOf(entry) + life.entries).take(120)
        ) to "Started as ${track.rungs.first().first}!"
    }

    fun workExtraHours(life: LifeState): TurnResult {
        if (life.careerTrack == CareerTrack.UNEMPLOYED || !life.isAlive || life.prisonYearsLeft > 0) {
            return TurnResult(life, emptySet(), 0)
        }
        val s = life.stats.copy(
            energy = life.stats.energy - 12,
            stress = life.stats.stress + 9,
            anxiety = life.stats.anxiety + 5
        ).clamped()
        val bonus = (life.currentSalary * 0.08).toInt().coerceAtLeast(400)
        val fameGain = if (life.careerTrack.isFameTrack) 6 else 0
        val newFame = (life.fame + fameGain).coerceIn(0, 100)
        val entry = JournalEntry(
            id = "e_work_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Focused Sprint at Work",
            detail = "Invested extra hours into ${life.currentCareerTitle}, earning a $$bonus bonus."
        )
        val updated = life.copy(
            stats = s,
            netWorth = life.netWorth + bonus,
            careerPerformance = (life.careerPerformance + 12).coerceAtMost(100),
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            entries = (listOf(entry) + life.entries).take(120)
        )
        return evaluateAchievements(updated)
    }

    fun boostFameAndCraft(life: LifeState): TurnResult {
        if (!life.isAlive || life.prisonYearsLeft > 0 || life.currentAge < 16) return TurnResult(life, emptySet(), 0)
        val track = life.careerTrack
        val actionTitle = when (track) {
            CareerTrack.FINE_ART -> "Gallery Exhibition & Studio Drop"
            CareerTrack.ACTING -> "Festival Premiere & Press Junket"
            CareerTrack.MODELING -> "Fashion Week Runway Walk"
            CareerTrack.MUSIC_SINGER -> "Single Release & Acoustic Session"
            CareerTrack.BAND_GUITARIST -> "Live Guitar Solo Showcase"
            CareerTrack.BAND_DRUMMER -> "Arena Tour Drum Feature"
            CareerTrack.RACE_DRIVER -> "Grand Prix Qualifier & Paddock Press"
            else -> "Public Press & Portfolio Showcase"
        }
        val isHit = Random.nextFloat() < (0.35f + life.stats.creativity / 220f)
        val fameGain = if (isHit) Random.nextInt(14, 24) else Random.nextInt(7, 13)
        val payout = if (isHit) Random.nextInt(1800, 4800) else Random.nextInt(400, 1200)
        val newFame = (life.fame + fameGain).coerceIn(0, 100)
        val newFollowers = if (life.socialChannel.followers > 0 || track.isFameTrack) fameGain * 140 else 0

        val entry = JournalEntry(
            id = "e_fame_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.FAME,
            title = if (isHit) "Breakout Hit: $actionTitle" else actionTitle,
            detail = "Gained +$fameGain Fame (now $newFame/100) and earned $$payout from appearances."
        )
        val updated = life.copy(
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            netWorth = life.netWorth + payout,
            careerPerformance = (life.careerPerformance + 8).coerceAtMost(100),
            stats = life.stats.copy(
                energy = life.stats.energy - 10,
                stress = life.stats.stress + 6,
                creativity = life.stats.creativity + 3
            ).clamped(),
            socialChannel = life.socialChannel.copy(
                followers = life.socialChannel.followers + newFollowers
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )
        return evaluateAchievements(updated)
    }

    fun joinCrimeSyndicate(life: LifeState): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return life to "Currently serving a prison sentence."
        if (life.currentAge < 16) return life to "Available from age 16."
        val syndicate = CountryCatalog.syndicateForBirthplace(life.birthplace)
        val entry = JournalEntry(
            id = "e_crime_join_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CRIME,
            title = "Initiated into ${syndicate.organizationName}",
            detail = "Entered the local ${syndicate.localTerm} underworld as ${syndicate.ranks.first()}."
        )
        return life.copy(
            crimeActive = true,
            crimeRank = 0,
            notoriety = maxOf(life.notoriety, 15),
            careerTrack = CareerTrack.UNEMPLOYED,
            stats = life.stats.copy(karma = life.stats.karma - 12, stress = life.stats.stress + 8).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        ) to "Joined ${syndicate.organizationName} as ${syndicate.ranks.first()}."
    }

    fun commitSyndicateOp(life: LifeState, opIndex: Int): TurnResult {
        if (!life.isAlive || life.prisonYearsLeft > 0 || !life.crimeActive) return TurnResult(life, emptySet(), 0)
        val syndicate = CountryCatalog.syndicateForBirthplace(life.birthplace)
        val safeIdx = opIndex.coerceIn(0, syndicate.hustleTitles.lastIndex)
        val op = syndicate.hustleTitles[safeIdx]

        val riskChance = (0.14f + (safeIdx * 0.08f) + (life.notoriety / 420f)).coerceAtMost(0.45f)
        val caught = Random.nextFloat() < riskChance

        return if (caught) {
            val sentence = Random.nextInt(2, 5)
            val fine = Random.nextInt(1500, 5000)
            val entry = JournalEntry(
                id = "e_arrest_${life.currentAge}_${Random.nextInt(999)}",
                age = life.currentAge,
                year = life.currentYear,
                category = EntryCategory.CRIME,
                title = "Arrested in ${syndicate.localTerm} Crackdown",
                detail = "Caught during '${op.first}'. Sentenced to $sentence years and fined $$fine."
            )
            val updated = life.copy(
                prisonYearsLeft = sentence,
                netWorth = life.netWorth - fine,
                notoriety = (life.notoriety + 14).coerceAtMost(100),
                stats = life.stats.copy(
                    happiness = life.stats.happiness - 18,
                    stress = life.stats.stress + 20,
                    karma = life.stats.karma - 10
                ).clamped(),
                entries = (listOf(entry) + life.entries).take(120)
            )
            evaluateAchievements(updated)
        } else {
            val haul = (3500 + (safeIdx * 4200) + (life.crimeRank * 3000) + Random.nextInt(800, 2800))
            val newNotoriety = (life.notoriety + 10 + safeIdx * 4).coerceAtMost(100)
            val nextRank = if (newNotoriety >= (life.crimeRank + 1) * 28 && life.crimeRank < syndicate.ranks.lastIndex) {
                life.crimeRank + 1
            } else life.crimeRank
            val rankLabel = syndicate.ranks[nextRank]

            val entry = JournalEntry(
                id = "e_op_${life.currentAge}_${Random.nextInt(999)}",
                age = life.currentAge,
                year = life.currentYear,
                category = EntryCategory.CRIME,
                title = "${syndicate.localTerm}: ${op.first}",
                detail = "${op.second} Netted $$haul (Rank: $rankLabel)."
            )
            val updated = life.copy(
                netWorth = life.netWorth + haul,
                crimeRank = nextRank,
                notoriety = newNotoriety,
                stats = life.stats.copy(
                    karma = life.stats.karma - 8,
                    stress = life.stats.stress + 9,
                    energy = life.stats.energy - 10
                ).clamped(),
                entries = (listOf(entry) + life.entries).take(120)
            )
            evaluateAchievements(updated)
        }
    }

    fun leaveCrimeLife(life: LifeState): TurnResult {
        if (!life.crimeActive || life.prisonYearsLeft > 0) return TurnResult(life, emptySet(), 0)
        val syndicate = CountryCatalog.syndicateForBirthplace(life.birthplace)
        val entry = JournalEntry(
            id = "e_leave_crime_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CRIME,
            title = "Stepped Away from ${syndicate.organizationName}",
            detail = "Left underworld ties behind to rebuild a quieter chapter."
        )
        val updated = life.copy(
            crimeActive = false,
            stats = life.stats.copy(karma = life.stats.karma + 10, stress = life.stats.stress - 8).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        return evaluateAchievements(updated)
    }

    fun completeLiveStream(
        life: LifeState,
        format: StreamFormat,
        topic: String,
        engagedChat: Boolean
    ): StreamOutcome {
        val ch = life.socialChannel
        val gearMult = 1.0f + ((ch.gearLevel - 1) * 0.28f)
        val trendBonus = if (ch.niche == ch.currentTrendNiche) 1.45f else 1.0f
        val chatBonus = if (engagedChat) 1.25f else 1.0f
        val fameMult = 1.0f + (life.fame * 0.01f)

        val baseViewers = (35 + (ch.followers * 0.045f)).toInt().coerceAtLeast(25)
        val peakViewers = (baseViewers * gearMult * trendBonus * chatBonus * fameMult * Random.nextDouble(0.85, 1.35)).toInt()
        val followersGained = ((peakViewers * 0.42f * format.followerMultiplier * trendBonus) + Random.nextInt(30, 120)).toInt()
        val sparklesEarned = (format.baseSparkles * gearMult * chatBonus).toInt()
        val cashEarned = (format.baseCash * gearMult * (1f + ch.followers / 40000f)).toInt()

        val newFame = (life.fame + if (engagedChat) 4 else 2).coerceIn(0, 100)

        val s = life.stats.copy(
            energy = life.stats.energy - format.energyCost,
            stress = life.stats.stress + format.stressCost - (if (engagedChat) 2 else 0),
            happiness = life.stats.happiness + (if (engagedChat) 5 else 2),
            creativity = life.stats.creativity + 2
        ).clamped()

        val entry = JournalEntry(
            id = "e_stream_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = "Live: ${topic.ifBlank { format.label }}",
            detail = "Peaked at $peakViewers viewers, welcomed $followersGained followers, and earned $sparklesEarned ✦ + $$cashEarned."
        )

        val nextLife = life.copy(
            stats = s,
            netWorth = life.netWorth + cashEarned,
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            socialChannel = ch.copy(
                followers = ch.followers + followersGained,
                reputation = (ch.reputation + if (engagedChat) 3 else 1).coerceIn(0, 100),
                totalStreams = ch.totalStreams + 1
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )

        val (evaluated, unlocked, achSparkles) = evaluateAchievements(nextLife)
        return StreamOutcome(
            updatedLife = evaluated,
            peakViewers = peakViewers,
            followersGained = followersGained,
            sparklesEarned = sparklesEarned + achSparkles,
            cashEarned = cashEarned,
            newlyUnlockedAchievements = unlocked
        )
    }

    fun postCreatorContent(life: LifeState, rideTrend: Boolean): TurnResult {
        val ch = life.socialChannel
        val aligned = rideTrend && ch.niche == ch.currentTrendNiche
        val mult = when {
            aligned -> 2.6f
            rideTrend -> 1.6f
            else -> 1.15f
        }
        val gained = ((85 + ch.followers * 0.06f) * mult * (1f + ch.gearLevel * 0.15f)).toInt()
        val newFame = (life.fame + if (aligned) 5 else 2).coerceIn(0, 100)
        val entry = JournalEntry(
            id = "e_post_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = if (rideTrend) "Trend Post: ${ch.currentTrendTitle}" else "${ch.niche.label} Dispatch",
            detail = "Shared a polished post in ${ch.niche.label} and grew by $gained followers."
        )
        val nextLife = life.copy(
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            stats = life.stats.copy(
                energy = life.stats.energy - 6,
                creativity = life.stats.creativity + 2
            ).clamped(),
            socialChannel = ch.copy(
                followers = ch.followers + gained,
                reputation = (ch.reputation + 1).coerceAtMost(100)
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward + 15)
    }

    fun interactWithFollowers(life: LifeState, actionType: String): TurnResult {
        val ch = life.socialChannel
        val (title, detail, folDelta, repDelta, stressDelta, happyDelta) = when (actionType) {
            "poll" -> Tuple6("Community Poll", "Asked followers to vote on your next ${ch.niche.label} project.", 160 + (ch.followers / 40), 2, -2, 4)
            "calm_reply" -> Tuple6("Thoughtful Q&A Thread", "Addressed community questions with warmth and transparency.", 90 + (ch.followers / 60), 6, -4, 3)
            else -> Tuple6("Superfan Letters", "Replied to heartfelt messages from longtime supporters.", 110 + (ch.followers / 50), 4, -5, 6)
        }
        val entry = JournalEntry(
            id = "e_fan_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = title,
            detail = detail
        )
        val nextLife = life.copy(
            stats = life.stats.copy(
                energy = life.stats.energy - 5,
                stress = life.stats.stress + stressDelta,
                happiness = life.stats.happiness + happyDelta
            ).clamped(),
            socialChannel = ch.copy(
                followers = ch.followers + folDelta,
                reputation = (ch.reputation + repDelta).coerceIn(0, 100)
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward + 10)
    }

    fun launchMerchLine(life: LifeState, merchName: String): TurnResult {
        val cleanName = merchName.trim().ifBlank { "Minimalist Linen Tote" }.take(40)
        val entry = JournalEntry(
            id = "e_merch_${life.currentAge}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = "Merchandise Drop: $cleanName",
            detail = "Launched a signature creator collection that earns recurring annual revenue."
        )
        val nextLife = life.copy(
            netWorth = (life.netWorth - 1500).coerceAtLeast(-5000),
            socialChannel = life.socialChannel.copy(
                merchLaunched = true,
                merchName = cleanName
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun signSponsorship(life: LifeState, sponsorName: String, annualPay: Int, repDelta: Int): TurnResult {
        val ch = life.socialChannel
        val tier = when {
            annualPay >= 40000 -> "Cultural Partner"
            annualPay >= 15000 -> "Featured Creator"
            else -> "Micro Partner"
        }
        val entry = JournalEntry(
            id = "e_spons_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = "Sponsor: $sponsorName",
            detail = "Signed a $$annualPay/yr creator partnership ($tier)."
        )
        val nextLife = life.copy(
            socialChannel = ch.copy(
                sponsorshipTier = tier,
                activeSponsorName = sponsorName,
                annualSponsorIncome = annualPay,
                reputation = (ch.reputation + repDelta).coerceIn(0, 100)
            ),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun performWellnessAction(life: LifeState, action: String): TurnResult {
        val (title, detail, cost, healthD, happyD, fitD, energyD, stressD, anxD, riskD) = when (action) {
            "checkup" -> Tuple10("Annual Checkup", "Preventive screening and lab review kept long-term risks low.", 180, 6, 2, 2, 3, -3, -5, -12)
            "therapy" -> Tuple10("Therapy & Mindfulness", "Unpacked pressure calmly and practiced grounding habits.", 140, 2, 9, 0, 5, -14, -15, -2)
            else -> Tuple10("Quiet Nature Retreat", "Spent a slow weekend among pines without screens.", 450, 5, 12, 4, 18, -18, -16, -4)
        }
        val entry = JournalEntry(
            id = "e_well_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.WELLNESS,
            title = title,
            detail = detail
        )
        val nextLife = life.copy(
            netWorth = life.netWorth - cost,
            stats = life.stats.copy(
                health = life.stats.health + healthD,
                happiness = life.stats.happiness + happyD,
                fitness = life.stats.fitness + fitD,
                energy = life.stats.energy + energyD,
                stress = life.stats.stress + stressD,
                anxiety = life.stats.anxiety + anxD,
                diseaseRisk = life.stats.diseaseRisk + riskD
            ).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun interactRelationship(life: LifeState, relId: String, action: String): TurnResult {
        val target = life.relationships.firstOrNull { it.id == relId } ?: return TurnResult(life, emptySet(), 0)
        if (!target.isAlive) return TurnResult(life, emptySet(), 0)

        var cashDelta = 0
        var closenessDelta = 8
        var happyDelta = 6
        var stressDelta = -4
        var becomesPartner = target.isPartner
        var becomesSpouse = target.isSpouse
        var title = "Time with ${target.name}"
        var detail = "Shared tea and unhurried conversation."

        when (action) {
            "gift" -> {
                cashDelta = -150
                closenessDelta = 14
                happyDelta = 8
                title = "Thoughtful Gift for ${target.name}"
                detail = "Picked out a handwritten card and artisan ceramics ($150)."
            }
            "deep_talk" -> {
                closenessDelta = 11
                stressDelta = -8
                title = "Heart-to-Heart with ${target.name}"
                detail = "An honest evening conversation eased lingering tension."
            }
            "propose" -> {
                if (target.closeness >= 75) {
                    becomesPartner = true
                    becomesSpouse = true
                    closenessDelta = 15
                    happyDelta = 16
                    title = "Married ${target.name}"
                    detail = "Exchanged quiet vows beneath warm lantern light."
                } else {
                    closenessDelta = -5
                    happyDelta = -4
                    title = "Not Quite Ready"
                    detail = "${target.name} hopes to build a bit more closeness first."
                }
            }
        }

        val updatedRels = life.relationships.map {
            if (it.id == relId) {
                it.copy(
                    closeness = (it.closeness + closenessDelta).coerceIn(0, 100),
                    isPartner = becomesPartner,
                    isSpouse = becomesSpouse,
                    role = if (becomesSpouse) "Spouse" else it.role
                )
            } else it
        }

        val entry = JournalEntry(
            id = "e_rel_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = title,
            detail = detail
        )

        val nextLife = life.copy(
            netWorth = life.netWorth + cashDelta,
            stats = life.stats.copy(
                happiness = life.stats.happiness + happyDelta,
                stress = life.stats.stress + stressDelta,
                anxiety = life.stats.anxiety - 3,
                energy = life.stats.energy - 4
            ).clamped(),
            relationships = updatedRels,
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun meetNewPerson(life: LifeState, romantic: Boolean): TurnResult {
        if (life.relationships.size >= 30) return TurnResult(life, emptySet(), 0)
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace)
        val fullName = "$fn $ln"
        val role = if (romantic) "Romantic Interest" else listOf("Friend", "Studio Peer", "Reading Partner").random()
        val newRel = Relationship(
            id = "rel_${UUID.randomUUID().toString().take(6)}",
            name = fullName,
            role = role,
            age = (life.currentAge + Random.nextInt(-3, 5)).coerceAtLeast(16),
            closeness = Random.nextInt(48, 68),
            isPartner = romantic
        )
        val entry = JournalEntry(
            id = "e_meet_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Met $fullName",
            detail = "Crossed paths at a neighborhood cafe and clicked as $role."
        )
        val nextLife = life.copy(
            relationships = life.relationships + newRel,
            stats = life.stats.copy(happiness = life.stats.happiness + 5).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun currentDayKey(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun ensureDailyMissions(life: LifeState, dateKey: String = currentDayKey()): LifeState {
        val ms = life.missionState
        if (ms.dateKey == dateKey && ms.missions.size == 3) {
            return life
        }
        val eligiblePool = MissionType.entries.filter { type ->
            when (type) {
                MissionType.AGE_MILESTONE -> life.isAlive
                MissionType.WORK_SPRINT -> life.currentAge >= 18
                else -> true
            }
        }
        val picked = eligiblePool.shuffled().take(3)
        val generated = picked.mapIndexed { idx, type ->
            DailyMission(
                id = "dm_${dateKey}_${idx}_${type.name}",
                type = type,
                title = type.defaultTitle,
                description = type.description,
                xpReward = type.xpReward,
                sparkleReward = type.sparkleReward,
                cashReward = type.cashReward,
                isCompleted = false
            )
        }
        return life.copy(
            missionState = DailyMissionState(
                dateKey = dateKey,
                missions = generated,
                dailyBonusClaimed = false,
                totalXp = ms.totalXp
            )
        )
    }

    fun progressMissionByType(
        life: LifeState,
        type: MissionType,
        dateKey: String = currentDayKey()
    ): MissionOutcome {
        val ensured = ensureDailyMissions(life, dateKey)
        val ms = ensured.missionState
        val target = ms.missions.firstOrNull { it.type == type && !it.isCompleted }
            ?: return MissionOutcome(ensured, null, 0, 0, null)

        val prevLevel = ms.level
        val updatedList = ms.missions.map { if (it.id == target.id) it.copy(isCompleted = true) else it }
        val updatedMs = ms.copy(
            missions = updatedList,
            totalXp = ms.totalXp + target.xpReward
        )
        val newLevel = updatedMs.level
        val updatedLife = ensured.copy(
            missionState = updatedMs,
            netWorth = ensured.netWorth + target.cashReward
        )
        return MissionOutcome(
            updatedLife = updatedLife,
            completedMission = target.copy(isCompleted = true),
            xpEarned = target.xpReward,
            sparklesEarned = target.sparkleReward,
            leveledUpTo = if (newLevel > prevLevel) newLevel else null
        )
    }

    fun executeDailyMissionAction(
        life: LifeState,
        missionId: String,
        dateKey: String = currentDayKey()
    ): MissionOutcome {
        val ensured = ensureDailyMissions(life, dateKey)
        val ms = ensured.missionState
        val target = ms.missions.firstOrNull { it.id == missionId && !it.isCompleted }
            ?: return MissionOutcome(ensured, null, 0, 0, null)

        if (target.type == MissionType.AGE_MILESTONE && ensured.isAlive && ensured.pendingPrompt == null) {
            val aged = ageUp(ensured).updatedLife
            val prog = progressMissionByType(aged, MissionType.AGE_MILESTONE, dateKey)
            return prog
        }

        var nextStats = ensured.stats
        var nextChannel = ensured.socialChannel
        var nextCash = ensured.netWorth
        var nextFame = ensured.fame
        var nextPeakFame = ensured.peakFame
        var nextPerf = ensured.careerPerformance
        var nextRels = ensured.relationships
        var entryTitle = target.title
        var entryDetail = "Completed today's mission for +${target.xpReward} XP and +${target.sparkleReward} ✦."

        when (target.type) {
            MissionType.GO_TO_GYM -> {
                nextStats = nextStats.copy(
                    fitness = nextStats.fitness + 5,
                    health = nextStats.health + 3,
                    stress = nextStats.stress - 5,
                    energy = nextStats.energy - 6
                ).clamped()
                entryTitle = "Daily Gym Workout"
                entryDetail = "Completed today's gym session (+5 Fitness, +3 Health, -5 Stress)."
            }
            MissionType.POST_SOCIAL -> {
                val gainedFollowers = 140 + (nextChannel.followers / 50).coerceAtMost(800)
                nextChannel = nextChannel.copy(
                    followers = nextChannel.followers + gainedFollowers,
                    reputation = (nextChannel.reputation + 1).coerceAtMost(100)
                )
                nextFame = (nextFame + 2).coerceIn(0, 100)
                nextPeakFame = maxOf(nextPeakFame, nextFame)
                entryTitle = "Posted Social Photo"
                entryDetail = "Shared a calm photo update on social media (+$gainedFollowers followers)."
            }
            MissionType.READ_BOOK -> {
                nextStats = nextStats.copy(
                    intellect = nextStats.intellect + 4,
                    creativity = nextStats.creativity + 3,
                    stress = nextStats.stress - 4,
                    happiness = nextStats.happiness + 3
                ).clamped()
                entryTitle = "Quiet Book Reading"
                entryDetail = "Read a book chapter this evening (+4 Intellect, +3 Creativity)."
            }
            MissionType.CONNECT_BOND -> {
                val targetRel = nextRels.firstOrNull { it.isAlive }
                if (targetRel != null) {
                    nextRels = nextRels.map {
                        if (it.id == targetRel.id) it.copy(closeness = (it.closeness + 5).coerceAtMost(100))
                        else it
                    }
                    entryDetail = "Checked in with ${targetRel.name} (+5 Closeness, +4 Happiness)."
                }
                nextStats = nextStats.copy(
                    happiness = nextStats.happiness + 4,
                    stress = nextStats.stress - 3
                ).clamped()
                entryTitle = "Warm Check-In"
            }
            MissionType.WORK_SPRINT -> {
                nextPerf = (nextPerf + 4).coerceAtMost(100)
                nextStats = nextStats.copy(energy = nextStats.energy - 5).clamped()
                entryTitle = "Daily Craft Focus"
                entryDetail = "Wrapped up a focused work block (+$${target.cashReward} reward)."
            }
            MissionType.MINDFUL_CHECKIN -> {
                nextStats = nextStats.copy(
                    stress = nextStats.stress - 7,
                    anxiety = nextStats.anxiety - 6,
                    happiness = nextStats.happiness + 4
                ).clamped()
                entryTitle = "Mindful Pause"
                entryDetail = "Took time for breathing and mental clarity (-7 Stress, -6 Anxiety)."
            }
            MissionType.GO_LIVE_STREAM -> {
                val gainedFollowers = 180 + (nextChannel.followers / 40).coerceAtMost(1000)
                nextChannel = nextChannel.copy(
                    followers = nextChannel.followers + gainedFollowers,
                    totalStreams = nextChannel.totalStreams + 1
                )
                nextFame = (nextFame + 2).coerceIn(0, 100)
                nextPeakFame = maxOf(nextPeakFame, nextFame)
                entryTitle = "Mini Creator Livestream"
                entryDetail = "Went live for a cozy Q&A check-in (+$gainedFollowers followers)."
            }
            MissionType.AGE_MILESTONE -> {
                nextStats = nextStats.copy(happiness = nextStats.happiness + 3).clamped()
            }
        }

        val missionEntry = JournalEntry(
            id = "e_dm_${ensured.currentAge}_${Random.nextInt(9999)}",
            age = ensured.currentAge,
            year = ensured.currentYear,
            category = EntryCategory.MILESTONE,
            title = entryTitle,
            detail = entryDetail
        )

        val prevLevel = ms.level
        val updatedList = ms.missions.map { if (it.id == target.id) it.copy(isCompleted = true) else it }
        val updatedMs = ms.copy(
            missions = updatedList,
            totalXp = ms.totalXp + target.xpReward
        )
        val newLevel = updatedMs.level

        val updatedLife = ensured.copy(
            stats = nextStats,
            socialChannel = nextChannel,
            netWorth = nextCash + target.cashReward,
            fame = nextFame,
            peakFame = nextPeakFame,
            careerPerformance = nextPerf,
            relationships = nextRels,
            missionState = updatedMs,
            entries = (listOf(missionEntry) + ensured.entries).take(120)
        )
        val (evaluated, _, achSparkles) = evaluateAchievements(updatedLife)
        return MissionOutcome(
            updatedLife = evaluated,
            completedMission = target.copy(isCompleted = true),
            xpEarned = target.xpReward,
            sparklesEarned = target.sparkleReward + achSparkles,
            leveledUpTo = if (newLevel > prevLevel) newLevel else null
        )
    }

    fun claimDailyMissionBonus(
        life: LifeState,
        dateKey: String = currentDayKey()
    ): MissionOutcome {
        val ensured = ensureDailyMissions(life, dateKey)
        val ms = ensured.missionState
        if (!ms.allCompleted || ms.dailyBonusClaimed) {
            return MissionOutcome(ensured, null, 0, 0, null)
        }
        val bonusXp = 120
        val bonusSparkles = 60
        val bonusCash = 500
        val prevLevel = ms.level
        val updatedMs = ms.copy(
            dailyBonusClaimed = true,
            totalXp = ms.totalXp + bonusXp
        )
        val newLevel = updatedMs.level
        val entry = JournalEntry(
            id = "e_dm_chest_${ensured.currentAge}_${Random.nextInt(9999)}",
            age = ensured.currentAge,
            year = ensured.currentYear,
            category = EntryCategory.MILESTONE,
            title = "Daily Trio Complete",
            detail = "Finished all 3 daily missions (+$bonusXp XP, +$bonusSparkles ✦, +$$bonusCash)."
        )
        val updated = ensured.copy(
            missionState = updatedMs,
            netWorth = ensured.netWorth + bonusCash,
            entries = (listOf(entry) + ensured.entries).take(120)
        )
        return MissionOutcome(
            updatedLife = updated,
            completedMission = null,
            xpEarned = bonusXp,
            sparklesEarned = bonusSparkles,
            leveledUpTo = if (newLevel > prevLevel) newLevel else null
        )
    }

    fun evaluateAchievements(life: LifeState): TurnResult {
        val current = life.unlockedAchievementIds.toMutableSet()
        val newlyUnlocked = mutableSetOf<String>()
        var reward = 0

        fun unlock(id: String, sparkles: Int) {
            if (current.add(id)) {
                newlyUnlocked.add(id)
                reward += sparkles
            }
        }

        unlock("first_breath", 50)
        if (life.hasDegree) unlock("graduate", 100)
        if (life.careerTrack != CareerTrack.UNEMPLOYED && life.careerRung >= life.careerTrack.rungs.lastIndex) {
            unlock("career_peak", 200)
        }
        if (life.fame >= 80 || life.peakFame >= 80) unlock("fame_icon", 220)
        if (life.socialChannel.totalStreams >= 1) unlock("first_stream", 75)
        if (life.socialChannel.followers >= 100_000) unlock("viral_creator", 250)
        if (life.socialChannel.merchLaunched) unlock("merch_mogul", 120)
        if (life.stats.happiness >= 80 && life.stats.stress < 20) unlock("zen_balance", 150)
        if (life.stats.fitness >= 90 && life.stats.health >= 90) unlock("marathon_fit", 125)
        if (life.relationships.any { it.isSpouse || (it.isPartner && it.closeness >= 95) }) unlock("golden_bond", 150)
        if (life.netWorth >= 250_000) unlock("self_made", 200)
        if (life.generation >= 3) unlock("dynasty_three", 250)
        if (life.currentAge >= 80) unlock("elder_wisdom", 180)
        if (life.currentAge >= 100) unlock("centenarian", 300)
        if (!life.isAlive) unlock("legacy_keeper", 100)

        return TurnResult(
            updatedLife = life.copy(unlockedAchievementIds = current),
            newlyUnlockedAchievements = newlyUnlocked,
            sparklesEarned = reward
        )
    }

    private fun stageTransitionDetail(stage: LifeStage, age: Int): String = when (stage) {
        LifeStage.CHILDHOOD -> "Stepped into childhood at age $age with new school routines and friendships."
        LifeStage.ADOLESCENCE -> "Entered teen years at age $age — growing independence, hobbies, and social awareness."
        LifeStage.YOUNG_ADULT -> "Reached young adulthood at $age, unlocking careers, higher education, and personal freedom."
        LifeStage.MIDLIFE -> "Entered midlife at $age — balancing career stride, wellness routines, and enduring bonds."
        LifeStage.SENIOR -> "Entered the silver years at $age with changing health rhythms and deeper reflection."
        LifeStage.ELDER -> "Reached age $age in the golden chapter of elder wisdom and legacy."
        LifeStage.INFANCY -> "Early childhood wonder."
    }

    private fun historicalEventForYear(year: Int): HistoricalNote? = when (year) {
        1995 -> HistoricalNote("The Web Goes Public", 0.08f, "Early dial-up forums & personal homepages spark a creative frontier.", 0)
        2001 -> HistoricalNote("Indie Digital Shift", -0.05f, "Markets wobble briefly while digital studios and blogs blossom.", 120)
        2008 -> HistoricalNote("Global Financial Shift", -0.14f, "An economic downturn tightens hiring and household budgets worldwide.", 0)
        2014 -> HistoricalNote("Creator Economy Wave", 0.07f, "Live video platforms turn bedroom studios into real careers.", 650)
        2020 -> HistoricalNote("The Quiet Remote Shift", -0.04f, "Neighborhood walks, home cooking, and online communities surge.", 900)
        2026 -> HistoricalNote("Clean-Tech & Slow Living", 0.09f, "Sustainable design and mindful digital spaces define the era.", 400)
        else -> null
    }

    private fun generateRandomLifeEvent(
        birthplace: String,
        age: Int,
        year: Int,
        stats: LifeStats,
        netWorth: Int,
        channel: SocialChannel
    ): RandomEventResult {
        val roll = Random.nextFloat()
        if (age >= 17 && roll < 0.35f) {
            val localPrompt = CountryCatalog.randomLocalIssuePrompt(birthplace, age)
            if (localPrompt != null) {
                return RandomEventResult(prompt = localPrompt)
            }
        }
        return when {
            roll < (0.45f + stats.diseaseRisk / 400f) -> {
                RandomEventResult(
                    prompt = LifeChoicePrompt(
                        id = "p_health_$age",
                        category = EntryCategory.RANDOM_EVENT,
                        title = "Sudden Burnout & Health Scare",
                        narrative = "Persistent fatigue and elevated strain forced a pause. How do you respond?",
                        options = listOf(
                            ChoiceOption(
                                id = "opt_rest",
                                label = "Take Restorative Medical Care",
                                effectHint = "-$1,200, +14 Health, -18 Stress",
                                outcomeSummary = "Stepped back for proper diagnostics and deep rest.",
                                cashDelta = -1200,
                                healthDelta = 14,
                                diseaseRiskDelta = -12,
                                stressDelta = -18,
                                anxietyDelta = -14
                            ),
                            ChoiceOption(
                                id = "opt_push",
                                label = "Push Through Anyway",
                                effectHint = "-10 Health, +12 Stress",
                                outcomeSummary = "Kept working through exhaustion, leaving a lingering toll.",
                                healthDelta = -10,
                                diseaseRiskDelta = 8,
                                stressDelta = 12,
                                anxietyDelta = 10
                            )
                        )
                    )
                )
            }
            roll < 0.62f -> RandomEventResult(
                entry = JournalEntry(
                    id = "e_recession_$age",
                    age = age,
                    year = year,
                    category = EntryCategory.RANDOM_EVENT,
                    title = "Regional Economic Downturn",
                    detail = "Inflation and cautious spending trimmed portfolio value and slowed raises."
                ),
                statsAfter = stats.copy(stress = stats.stress + 8, anxiety = stats.anxiety + 7),
                cashDelta = -(netWorth.coerceAtLeast(0) * 0.08).toInt().coerceAtMost(8000),
                economyDelta = -0.07f
            )
            roll < 0.74f -> {
                val prize = listOf(2500, 5000, 18000).random()
                RandomEventResult(
                    entry = JournalEntry(
                        id = "e_windfall_$age",
                        age = age,
                        year = year,
                        category = EntryCategory.RANDOM_EVENT,
                        title = "Unexpected Lottery Windfall",
                        detail = "A community raffle ticket matched every number for a $$prize surprise!"
                    ),
                    statsAfter = stats.copy(happiness = stats.happiness + 12, stress = stats.stress - 6),
                    cashDelta = prize
                )
            }
            roll < 0.88f -> {
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(birthplace)
                val mentorName = "$fn $ln"
                RandomEventResult(
                    entry = JournalEntry(
                        id = "e_mentor_$age",
                        age = age,
                        year = year,
                        category = EntryCategory.RANDOM_EVENT,
                        title = "Met Influential Patron $mentorName",
                        detail = "A respected creative director praised your voice and opened new doors (+600 followers)."
                    ),
                    statsAfter = stats.copy(creativity = stats.creativity + 5, happiness = stats.happiness + 7),
                    followerDelta = if (channel.followers > 0) 600 else 250,
                    newConnection = Relationship(
                        id = "mentor_${UUID.randomUUID().toString().take(6)}",
                        name = mentorName,
                        role = "Influential Mentor",
                        age = (age + Random.nextInt(8, 18)).coerceAtMost(75),
                        closeness = 72
                    )
                )
            }
            else -> RandomEventResult(
                entry = JournalEntry(
                    id = "e_boom_$age",
                    age = age,
                    year = year,
                    category = EntryCategory.RANDOM_EVENT,
                    title = "Neighborhood Arts Grant & Market Upturn",
                    detail = "Local investments surged and a small creative grant brightened the year."
                ),
                statsAfter = stats.copy(happiness = stats.happiness + 6),
                cashDelta = 3200,
                economyDelta = 0.06f
            )
        }
    }

    private fun buildEpitaph(life: LifeState): String {
        val peakRole = life.currentCareerTitle
        return when {
            life.peakFame >= 75 -> "Remembered across borders for reaching ${life.peakFame} peak fame as $peakRole."
            life.socialChannel.followers >= 50_000 -> "Remembered as a warm voice to ${life.socialChannel.followers} listeners and a devoted friend."
            life.netWorth >= 200_000 -> "Built a quiet, generous fortune through years as $peakRole."
            life.relationships.count { it.isSpouse || it.closeness >= 80 } >= 2 -> "Surrounded by deep love and pages filled with kindness."
            else -> "Lived thoughtfully across ${life.currentAge} seasons in ${life.birthplace}."
        }
    }

    private fun quietYearTitle(age: Int): String = when {
        age < 6 -> "Early Wonder (Age $age)"
        age < 13 -> "Childhood Pages"
        age < 20 -> "Coming of Age"
        age < 40 -> "Quiet Momentum"
        age < 65 -> "Steadfast Seasons"
        else -> "Golden Reflections"
    }

    private fun quietYearDetail(age: Int, role: String, followers: Int): String = when {
        age < 6 -> "Spent sunny afternoons learning new words and exploring the garden."
        age < 18 -> "Balanced school notebooks, quiet friendships, and evening walks."
        followers >= 5000 -> "Continued growing as $role while tending to a community of $followers followers."
        else -> "Moved steadily through another year as $role, keeping life's rhythm calm."
    }

    private data class HistoricalNote(
        val first: String,
        val second: Float,
        val third: String,
        val fourthFollowerBoost: Int
    )

    private data class RandomEventResult(
        val prompt: LifeChoicePrompt? = null,
        val entry: JournalEntry? = null,
        val statsAfter: LifeStats = LifeStats(),
        val cashDelta: Int = 0,
        val economyDelta: Float = 0f,
        val followerDelta: Int = 0,
        val newConnection: Relationship? = null
    )

    private data class Tuple6(
        val a: String, val b: String, val c: Int, val d: Int, val e: Int, val f: Int
    )

    private data class Tuple10(
        val a: String, val b: String, val c: Int, val d: Int, val e: Int,
        val f: Int, val g: Int, val h: Int, val i: Int, val j: Int
    )
}
