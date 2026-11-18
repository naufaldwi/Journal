package com.example.engine

import com.example.model.AncestorRecord
import com.example.model.AvatarAppearance
import com.example.model.BondCategory
import com.example.model.CareerTrack
import com.example.model.CharacterGender
import com.example.model.CharacterSkills
import com.example.model.ChoiceOption
import com.example.model.DailyEventCategory
import com.example.model.DailyEventChoice
import com.example.model.DailyMission
import com.example.model.DailyMissionState
import com.example.model.DailyRandomEvent
import com.example.model.DietPlan
import com.example.model.EducationType
import com.example.model.EntryCategory
import com.example.model.ExerciseRoutine
import com.example.model.GlobalNewsItem
import com.example.model.JournalEntry
import com.example.model.LifeChoicePrompt
import com.example.model.LifeStage
import com.example.model.LifeState
import com.example.model.LifeStats
import com.example.model.MissionType
import com.example.model.MoodSnapshot
import com.example.model.NewsCategory
import com.example.model.Relationship
import com.example.model.RelationshipPriority
import com.example.model.RomanceMethod
import com.example.model.RomanceOrientation
import com.example.model.SkillActivity
import com.example.model.SkillType
import com.example.model.SleepSchedule
import com.example.model.SocialChannel
import com.example.model.SocialNiche
import com.example.model.SocialPostTopic
import com.example.model.SocialStreamComment
import com.example.model.SocialStreamPost
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
        startingStats: LifeStats,
        gender: CharacterGender = CharacterGender.fromPronouns(pronouns)
    ): TurnResult {
        val isMaleName: Boolean? = when (gender) {
            CharacterGender.MALE -> true
            CharacterGender.FEMALE -> false
            CharacterGender.NON_BINARY -> null
        }
        val cleanName = characterName.trim().ifBlank {
            val (fn, ln) = CountryCatalog.randomNameForBirthplace(birthplace, isMale = isMaleName)
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
            name = "${CountryCatalog.randomCompanionFirstName(birthplace, isMale = false)} $lastName",
            role = "Mother",
            age = Random.nextInt(24, 36),
            closeness = Random.nextInt(78, 95),
            gender = CharacterGender.FEMALE,
            category = BondCategory.FAMILY
        )
        val dad = Relationship(
            id = "dad_${UUID.randomUUID().toString().take(6)}",
            name = "${CountryCatalog.randomCompanionFirstName(birthplace, isMale = true)} $lastName",
            role = "Father",
            age = Random.nextInt(25, 38),
            closeness = Random.nextInt(74, 93),
            gender = CharacterGender.MALE,
            category = BondCategory.FAMILY
        )
        val initialRels = mutableListOf(mom, dad)
        if (Random.nextFloat() < 0.55f) {
            val sibGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
            initialRels.add(
                Relationship(
                    id = "sib_${UUID.randomUUID().toString().take(6)}",
                    name = "${CountryCatalog.randomCompanionFirstName(birthplace, isMale = sibGender == CharacterGender.MALE)} $lastName",
                    role = "Older Sibling",
                    age = Random.nextInt(2, 7),
                    closeness = Random.nextInt(60, 86),
                    gender = sibGender,
                    category = BondCategory.FAMILY
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

        val traitSkills = when (trait) {
            TemperamentTrait.CURIOUS -> CharacterSkills(deductionLaw = 24, engineeringTech = 24)
            TemperamentTrait.RESILIENT -> CharacterSkills(athleticDriving = 24, culinaryCraft = 22)
            TemperamentTrait.EMPATHETIC -> CharacterSkills(persuasionMedia = 24, culinaryCraft = 22)
            TemperamentTrait.ARTISTIC -> CharacterSkills(paintingArt = 26, musicPerformance = 26, actingStage = 24)
            TemperamentTrait.AMBITIOUS -> CharacterSkills(persuasionMedia = 24, deductionLaw = 22)
        }.clamped()

        val initialNews = generateAnnualGlobalNews(birthYear, birthplace)

        val rawLife = LifeState(
            lifeId = "life_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            characterName = cleanName,
            birthplace = birthplace,
            birthYear = birthYear,
            currentAge = 0,
            isAlive = true,
            pronouns = pronouns,
            gender = gender,
            orientation = RomanceOrientation.UNSET,
            trait = trait,
            appearance = appearance,
            stats = traitStats,
            skills = traitSkills,
            netWorth = 500,
            socialChannel = SocialChannel(
                handle = "@$handleBase",
                niche = SocialNiche.DAILY_VLOGGING,
                currentTrendTitle = trendPool.first().first,
                currentTrendNiche = trendPool.first().second
            ),
            relationships = initialRels,
            entries = listOf(initialEntry),
            globalNewsFeed = listOf(initialNews),
            moodHistory = listOf(
                MoodSnapshot(
                    age = 0,
                    year = birthYear,
                    happiness = traitStats.happiness,
                    stress = traitStats.stress,
                    anxiety = traitStats.anxiety,
                    careerStressLoad = 0
                )
            ),
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
        val firstChild = parentLife.relationships.firstOrNull { it.role == "Child" }
        val heirGender = firstChild?.gender ?: if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val heirName = firstChild?.name?.take(60)
            ?: "${CountryCatalog.randomCompanionFirstName(parentLife.birthplace, isMale = heirGender == CharacterGender.MALE)} $surname".take(60)
        val heirBirthYear = (parentLife.birthYear + 28).coerceAtMost(parentLife.currentYear.coerceAtLeast(parentLife.birthYear + 18))
        val inheritedEstate = (parentLife.netWorth.coerceAtLeast(0) * 0.45).toInt().coerceAtLeast(1500)
        val inheritedFollowers = (parentLife.socialChannel.followers * 0.08).toInt().coerceAtMost(25_000)

        val handleBase = heirName.lowercase().replace("[^a-z0-9]".toRegex(), "").take(14).ifBlank { "journaler" }
        val heirTrait = TemperamentTrait.entries.random()
        val heirPronouns = heirGender.defaultPronouns
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
            isAlive = parentLife.isAlive,
            gender = parentLife.gender,
            category = BondCategory.FAMILY
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
                    isAlive = coParent?.isAlive ?: false,
                    gender = coParent?.gender ?: CharacterGender.FEMALE,
                    category = BondCategory.FAMILY
                )
            )
        }
        parentLife.relationships.filter { it.role == "Child" }.drop(1).forEachIndexed { idx, sib ->
            heirRels.add(
                Relationship(
                    id = "sib_heir_${idx}_${UUID.randomUUID().toString().take(5)}",
                    name = sib.name,
                    role = "Older Sibling",
                    age = idx + 2,
                    closeness = 76,
                    gender = sib.gender,
                    category = BondCategory.FAMILY
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
            gender = heirGender,
            orientation = RomanceOrientation.UNSET,
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
            globalNewsFeed = listOf(generateAnnualGlobalNews(heirBirthYear, parentLife.birthplace)),
            moodHistory = listOf(
                MoodSnapshot(
                    age = 0,
                    year = heirBirthYear,
                    happiness = LifeStats().happiness,
                    stress = LifeStats().stress,
                    anxiety = LifeStats().anxiety,
                    careerStressLoad = 0
                )
            ),
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

        // 2b. School & Career Automatic Skill Progression
        var skills = life.skills
        if (nextAge in 6..17) {
            skills = skills.copy(
                deductionLaw = skills.deductionLaw + 2,
                engineeringTech = skills.engineeringTech + 2,
                persuasionMedia = skills.persuasionMedia + 2,
                paintingArt = skills.paintingArt + 1,
                musicPerformance = skills.musicPerformance + 1
            ).clamped()
        }
        if (life.inSchool && life.educationType != EducationType.NONE) {
            val (boostedSkills, boostedStats) = applyEducationSkillBoost(skills, s, life.educationType)
            skills = boostedSkills
            s = boostedStats
        }

        // 3. Finances, Non-Negative Cash, Adult Loans, Career & Office Bond Mechanics
        var cash = life.netWorth.coerceAtLeast(0)
        var loanDebt = life.loanDebt.coerceAtLeast(0)
        var economyMult = life.economyMultiplier
        var perf = life.careerPerformance
        var rung = life.careerRung
        var track = life.careerTrack
        var hasDegree = life.hasDegree
        var inSchool = life.inSchool
        var eduType = life.educationType
        var completedEdu = life.completedEducation
        var fame = life.fame
        var peakFame = life.peakFame

        val livingProBonds = life.relationships.filter {
            it.isAlive && (it.category == BondCategory.SCHOOL || it.category == BondCategory.WORK)
        }
        val proStanding = if (livingProBonds.isEmpty()) 60 else livingProBonds.sumOf { it.closeness } / livingProBonds.size
        val warmProducer = life.relationships.any {
            it.isAlive && it.category == BondCategory.WORK && it.role.contains("Producer", ignoreCase = true) && it.closeness >= 75
        }

        if (fame > 0) {
            val decay = if (fame >= 70) 8 else 5
            val sustainOffset = if (!inPrisonThisYear && (track.isFameTrack || life.socialChannel.totalStreams > 0) && perf >= 72) {
                if (warmProducer) 4 else 2
            } else 0
            fame = (fame - decay + sustainOffset).coerceIn(0, 100)
        }

        if (nextAge >= 18) {
            if (loanDebt > 0) {
                loanDebt += (loanDebt * 0.04f).toInt().coerceAtLeast(100)
            }
            val salary = if (!inPrisonThisYear && !inSchool && track != CareerTrack.UNEMPLOYED) {
                val base = track.rungs.getOrElse(rung) { track.rungs.last() }.second
                val fameMult = if (track.isFameTrack) 1.0f + (fame * 0.008f) else 1.0f
                val skillBonusMult = 0.9f + (skills.levelOf(track.primarySkill) * 0.003f)
                val officeRaiseMult = when {
                    proStanding >= 80 -> 1.08f
                    proStanding < 35 -> 0.92f
                    else -> 1.0f
                }
                (base * economyMult * sleep.productivityBoost * fameMult * skillBonusMult * officeRaiseMult).toInt()
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
            // Cash never goes negative automatically
            cash = (cash + salary + merchPassive + sponsorIncome - livingCost).coerceAtLeast(0)

            if (!inPrisonThisYear && !inSchool && track != CareerTrack.UNEMPLOYED) {
                val primarySkillVal = skills.levelOf(track.primarySkill)
                val secondarySkillVal = skills.levelOf(track.secondarySkill)
                val skillFactor = ((primarySkillVal - 40) / 8) + ((secondarySkillVal - 40) / 14)
                val bondPerfShift = when {
                    proStanding >= 78 -> 6
                    proStanding < 35 -> -8
                    else -> 0
                }
                val perfGain = ((s.energy - 50) / 14) + skillFactor + bondPerfShift + Random.nextInt(-2, 5)
                perf = (perf + perfGain).coerceIn(15, 100)
                // Active work hones the career's primary skill naturally
                skills = skills.withBoost(track.primarySkill, 2)

                if (proStanding < 22 && Random.nextFloat() < 0.35f) {
                    val oldTitle = track.rungs.getOrElse(rung) { track.rungs.last() }.first
                    track = CareerTrack.UNEMPLOYED
                    rung = 0
                    perf = 50
                    s = s.copy(happiness = (s.happiness - 10).coerceIn(0, 100), stress = (s.stress + 12).coerceIn(0, 100))
                    newEntries.add(
                        JournalEntry(
                            id = "e_${nextAge}_fired_${Random.nextInt(999)}",
                            age = nextAge,
                            year = nextYear,
                            category = EntryCategory.CAREER,
                            title = "Let Go from $oldTitle",
                            detail = "Hostile workplace friction with colleagues and management cost you your position."
                        )
                    )
                } else {
                    val promoChance = if (proStanding >= 78) 0.65f else if (proStanding < 35) 0f else 0.45f
                    if (perf >= 82 && rung < track.rungs.lastIndex && Random.nextFloat() < promoChance) {
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
                                    "Your ${track.primarySkill.label} ($primarySkillVal) and producer rapport lifted your profile as $newRole (Fame $fame)."
                                } else {
                                    "Strong ${track.primarySkill.label} ($primarySkillVal) and workplace standing earned a promotion in ${track.title}."
                                }
                            )
                        )
                    }
                }
            }
        }

        // School dynamics affect young students & college learners
        if ((nextAge in 6..17 || inSchool) && livingProBonds.isNotEmpty()) {
            if (proStanding >= 75) {
                s = s.copy(intellect = (s.intellect + 2).coerceIn(0, 100), happiness = (s.happiness + 2).coerceIn(0, 100))
            } else if (proStanding < 35) {
                s = s.copy(happiness = (s.happiness - 4).coerceIn(0, 100), stress = (s.stress + 5).coerceIn(0, 100))
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

        // 5. Year-by-Year Relationship Aging, Neglect Decay & Milestone Evolution
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
                    rel.copy(age = newRelAge, isAlive = false, interactedThisYear = false)
                } else {
                    val autoGifted = !inPrisonThisYear && rel.priority == RelationshipPriority.GIFTING && cash >= 150
                    if (autoGifted) {
                        cash = (cash - 150).coerceAtLeast(0)
                    }
                    val autoInteracted = rel.interactedThisYear ||
                        rel.priority == RelationshipPriority.INTERACTION ||
                        autoGifted

                    val wasNeglected = nextAge >= 12 && !autoInteracted
                    val nextNeglectedYears = if (wasNeglected) rel.yearsNeglected + 1 else 0
                    val neglectPenalty = if (wasNeglected) {
                        when {
                            rel.isPartner || rel.isSpouse -> if (nextNeglectedYears >= 2) -7 else -5
                            rel.category == BondCategory.FAMILY -> -3
                            else -> if (nextNeglectedYears >= 2) -6 else -4
                        }
                    } else 0

                    val priorityBonus = when {
                        autoGifted -> 9
                        rel.priority == RelationshipPriority.INTERACTION && !rel.interactedThisYear -> 3
                        else -> 0
                    }

                    val milestoneShift = when {
                        inPrisonThisYear -> -3
                        rel.isSpouse && autoInteracted && s.stress < 60 -> 1
                        rel.isPartner && autoInteracted && rel.closeness >= 70 -> 2
                        nextAge == 18 && (rel.role == "Mother" || rel.role == "Father") -> -2
                        nextAge == 60 && (rel.isSpouse || rel.role == "Child") -> 4
                        nextAge >= 18 && !rel.isSpouse && s.karma < 50 -> -2
                        else -> 0
                    }

                    val nextCloseness = (rel.closeness + neglectPenalty + priorityBonus + milestoneShift).coerceIn(5, 100)
                    val evolvedRole = when {
                        rel.isSpouse -> "Spouse"
                        rel.isPartner && nextCloseness >= 82 -> "Committed Partner"
                        rel.role == "Childhood Friend" && nextAge >= 18 -> "Lifelong Friend"
                        else -> rel.role
                    }
                    rel.copy(
                        age = newRelAge,
                        closeness = nextCloseness,
                        role = evolvedRole,
                        interactedThisYear = false,
                        yearsNeglected = nextNeglectedYears
                    )
                }
            }
        }.toMutableList()

        // 5b. Auto-seed School & Workplace Bonds so dynamics matter every chapter
        if (nextAge in 6..17 && updatedRels.size < 37) {
            if (updatedRels.none { it.isAlive && it.category == BondCategory.SCHOOL && it.role.contains("Teacher", ignoreCase = true) }) {
                val teacherGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = teacherGender == CharacterGender.MALE)
                updatedRels.add(
                    Relationship(
                        id = "teacher_${UUID.randomUUID().toString().take(6)}",
                        name = "$fn $ln",
                        role = "Homeroom Teacher",
                        age = Random.nextInt(28, 52),
                        closeness = Random.nextInt(54, 74),
                        gender = teacherGender,
                        category = BondCategory.SCHOOL
                    )
                )
            }
        }
        if (track != CareerTrack.UNEMPLOYED && !inSchool && !inPrisonThisYear && updatedRels.size < 36) {
            if (updatedRels.none { it.isAlive && it.category == BondCategory.WORK }) {
                updatedRels.addAll(buildInitialWorkBonds(life.birthplace, nextAge, track))
            }
        }

        // 6. Key Age Milestones & Branching Prompts
        when (nextAge) {
            6 -> {
                val friendGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = friendGender == CharacterGender.MALE)
                val friend = Relationship(
                    id = "child_friend_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "Childhood Friend",
                    age = 6,
                    closeness = Random.nextInt(64, 80),
                    gender = friendGender,
                    category = BondCategory.SCHOOL
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
                val peerGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = peerGender == CharacterGender.MALE)
                val teenPeer = Relationship(
                    id = "teen_peer_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "School Peer",
                    age = 13,
                    closeness = Random.nextInt(54, 72),
                    gender = peerGender,
                    category = BondCategory.SCHOOL
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
                            id = "opt_law_school",
                            label = "Enroll in Law School",
                            effectHint = "Auto-boosts Deduction, Rhetoric & Intellect · Unlocks Law",
                            outcomeSummary = "You entered moot courtrooms and casebooks, sharpening deduction and rhetoric.",
                            intellectDelta = 12,
                            stressDelta = 8,
                            cashDelta = -4500,
                            enrollEducation = EducationType.LAW_SCHOOL
                        ),
                        ChoiceOption(
                            id = "opt_art_uni",
                            label = "Enroll in Art & Design Conservatory",
                            effectHint = "Auto-boosts Painting, Music, Acting & Creativity",
                            outcomeSummary = "You stepped into sunlit painting, music, and theatre studios at the conservatory.",
                            creativityDelta = 14,
                            stressDelta = 6,
                            cashDelta = -3600,
                            enrollEducation = EducationType.ART_UNI
                        ),
                        ChoiceOption(
                            id = "opt_music_academy",
                            label = "Enroll in Music Conservatory",
                            effectHint = "Auto-boosts Guitar, Drums, Vocals & Stagecraft · +Fame",
                            outcomeSummary = "You auditioned with your instrument and joined a vibrant band conservatory.",
                            creativityDelta = 12,
                            fameDelta = 8,
                            cashDelta = -3600,
                            enrollEducation = EducationType.MUSIC_ACADEMY
                        ),
                        ChoiceOption(
                            id = "opt_uni",
                            label = "Enroll in University",
                            effectHint = "Auto-boosts Engineering, Logic & Intellect",
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
                val (gradSkills, gradStats) = applyEducationSkillBoost(skills, s, eduType)
                skills = gradSkills
                s = gradStats
                val peerGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = peerGender == CharacterGender.MALE)
                val peer = Relationship(
                    id = "grad_peer_${UUID.randomUUID().toString().take(6)}",
                    name = "$fn $ln",
                    role = "Alumni Friend",
                    age = 22,
                    closeness = 70,
                    gender = peerGender,
                    category = BondCategory.FRIEND
                )
                updatedRels.add(peer)
                newEntries.add(
                    JournalEntry(
                        id = "e_22_grad",
                        age = 22,
                        year = nextYear,
                        category = EntryCategory.MILESTONE,
                        title = "${eduType.label} Graduation",
                        detail = "Graduated from ${eduType.label}; specialized skills and career readiness surged."
                    )
                )
                s = s.copy(happiness = (s.happiness + 12).coerceIn(0, 100), intellect = (s.intellect + 8).coerceIn(0, 100))
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

        // Organic family growth: allow welcoming multiple children across ages 24..42
        val currentChildCount = updatedRels.count { it.role == "Child" }
        val hasCommittedSpouseOrPartner = updatedRels.any {
            it.isAlive && (it.isSpouse || (it.isPartner && it.closeness >= 82))
        }
        val shouldHaveOrganicChild = !inPrisonThisYear &&
            hasCommittedSpouseOrPartner &&
            currentChildCount < 4 &&
            ((nextAge == 32 && currentChildCount == 0) || (nextAge in 25..42 && Random.nextFloat() < 0.22f))
        if (shouldHaveOrganicChild && updatedRels.size < 39) {
            val childGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
            val fn = CountryCatalog.randomCompanionFirstName(life.birthplace, isMale = childGender == CharacterGender.MALE)
            val lastName = life.characterName.substringAfterLast(" ", "")
            val childName = if (lastName.isNotBlank()) "$fn $lastName" else fn
            updatedRels.add(
                Relationship(
                    id = "child_${nextAge}_${UUID.randomUUID().toString().take(6)}",
                    name = childName,
                    role = "Child",
                    age = 0,
                    closeness = 92,
                    gender = childGender,
                    category = BondCategory.FAMILY
                )
            )
            newEntries.add(
                JournalEntry(
                    id = "e_${nextAge}_family_${Random.nextInt(999)}",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.RELATIONSHIP,
                    title = "Welcomed $childName",
                    detail = "Your family grew warmer with child #${currentChildCount + 1}."
                )
            )
            s = s.copy(happiness = (s.happiness + 14).coerceIn(0, 100))
        }

        // Complete intensive adult education in 1 year if enrolled after age 22
        if (inSchool && nextAge > 22) {
            inSchool = false
            hasDegree = true
            completedEdu = eduType
            val (gradSkills, gradStats) = applyEducationSkillBoost(skills, s, eduType)
            skills = gradSkills
            s = gradStats
            newEntries.add(
                JournalEntry(
                    id = "e_adult_grad_$nextAge",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.MILESTONE,
                    title = "Completed ${eduType.label}",
                    detail = "Earned your diploma in ${eduType.label}, sharply boosting matching career skills."
                )
            )
        }

        // 7. Annual Dynamic Global News Feed & Historical Era Events
        val annualNews = generateAnnualGlobalNews(nextYear, life.birthplace)
        val updatedNewsFeed = (listOf(annualNews) + life.globalNewsFeed).distinctBy { it.year }.take(25)
        economyMult = (economyMult + annualNews.economyDelta).coerceIn(0.75f, 1.35f)

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

        // 7b. Hatred / Misery Check: if 3+ living contacts hate/despise the player (< 25 closeness)
        val hateCount = updatedRels.count { it.isAlive && it.closeness < 25 }
        if (hateCount >= 3) {
            s = s.copy(
                happiness = (s.happiness - 12).coerceIn(0, 100),
                stress = (s.stress + 10).coerceIn(0, 100),
                anxiety = (s.anxiety + 10).coerceIn(0, 100)
            )
            newEntries.add(
                JournalEntry(
                    id = "e_${nextAge}_misery",
                    age = nextAge,
                    year = nextYear,
                    category = EntryCategory.RELATIONSHIP,
                    title = "Surrounded by Bitterness",
                    detail = "$hateCount people in your circle resent you, making daily life feel miserable (-12 Happiness, +10 Stress)."
                )
            )
        }

        // 8. Periodic Random Life Events & Country-Specific Current Issues
        var annualEncounterTitle = ""
        if (updatedPrompt == null && !inPrisonThisYear && nextAge >= 10 && Random.nextFloat() < 0.42f) {
            val randomOutcome = generateRandomLifeEvent(life.birthplace, nextAge, nextYear, s, cash, updatedChannel)
            if (randomOutcome.prompt != null) {
                updatedPrompt = randomOutcome.prompt
                annualEncounterTitle = randomOutcome.prompt.title
            } else if (randomOutcome.entry != null) {
                newEntries.add(randomOutcome.entry)
                annualEncounterTitle = randomOutcome.entry.title
                s = randomOutcome.statsAfter
                cash = (cash + randomOutcome.cashDelta).coerceAtLeast(0)
                economyMult = (economyMult + randomOutcome.economyDelta).coerceIn(0.75f, 1.35f)
                updatedChannel = updatedChannel.copy(
                    followers = (updatedChannel.followers + randomOutcome.followerDelta).coerceAtLeast(0)
                )
                if (randomOutcome.newConnection != null && updatedRels.size < 36) {
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

        val clampedStats = s.clamped()
        val encounterInEntries = newEntries.firstOrNull {
            it.category == EntryCategory.RANDOM_EVENT || it.title.contains("Encounter", ignoreCase = true)
        }?.title ?: ""
        val resolvedEncounterTitle = annualEncounterTitle.ifBlank { encounterInEntries }
        val newMoodSnap = MoodSnapshot(
            age = nextAge,
            year = nextYear,
            happiness = clampedStats.happiness,
            stress = clampedStats.stress,
            anxiety = clampedStats.anxiety,
            careerStressLoad = careerStress,
            hadRandomEncounter = resolvedEncounterTitle.isNotBlank(),
            encounterLabel = resolvedEncounterTitle
        )
        val updatedMoodHistory = (ensureMoodHistory(life).moodHistory + newMoodSnap)
            .distinctBy { it.age }
            .takeLast(10)

        val nextLife = life.copy(
            currentAge = nextAge,
            isAlive = isAlive,
            appearance = updatedAppearance,
            stats = clampedStats,
            skills = skills.clamped(),
            netWorth = cash.coerceAtLeast(0),
            loanDebt = loanDebt.coerceAtLeast(0),
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
            globalNewsFeed = updatedNewsFeed,
            moodHistory = updatedMoodHistory,
            pendingPrompt = if (isAlive) updatedPrompt else null,
            epitaph = epitaph,
            deathCause = deathCause
        )

        val (evaluated, unlocked, reward) = evaluateAchievements(ensureDailyMissions(nextLife))
        return TurnResult(evaluated, unlocked, reward)
    }

    fun resolveChoice(life: LifeState, option: ChoiceOption): TurnResult {
        var s = life.stats.copy(
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

        var nextSkills = life.skills
        val newEdu = option.enrollEducation ?: life.educationType
        if (option.enrollEducation != null) {
            val (bSkills, bStats) = applyEducationSkillBoost(nextSkills, s, option.enrollEducation)
            nextSkills = bSkills
            s = bStats
        }

        // Prevent negative cash: adults taking tuition shortfall can cover remaining balance with a student loan
        var nextCash = life.netWorth + option.cashDelta
        var nextDebt = life.loanDebt
        if (nextCash < 0) {
            val shortfall = -nextCash
            if (life.currentAge >= 18 && option.enrollEducation != null) {
                nextDebt += shortfall
            }
            nextCash = 0
        }

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

        val wasRandomEncounter = life.pendingPrompt?.category == EntryCategory.RANDOM_EVENT
        val encounterTitle = if (wasRandomEncounter) {
            life.pendingPrompt?.title ?: option.label
        } else null

        val nextLifeRaw = life.copy(
            stats = s,
            skills = nextSkills.clamped(),
            netWorth = nextCash.coerceAtLeast(0),
            loanDebt = nextDebt.coerceAtLeast(0),
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
        val nextLife = nextLifeRaw.copy(
            moodHistory = upsertMoodSnapshot(nextLifeRaw, encounterLabel = encounterTitle)
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
        if (life.netWorth < edu.annualCost) {
            return life to "Need $${edu.annualCost} cash to enroll. Earn cash or take an adult loan first!"
        }

        val (boostedSkills, boostedStats) = applyEducationSkillBoost(life.skills, life.stats, edu)
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
            skills = boostedSkills,
            stats = boostedStats,
            netWorth = (life.netWorth - edu.annualCost).coerceAtLeast(0),
            entries = (listOf(entry) + life.entries).take(120)
        ) to "Enrolled in ${edu.label}! Matching skills boosted."
    }

    fun applyForCareer(life: LifeState, track: CareerTrack): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return life to "Finish your sentence before applying for jobs."
        if (life.currentAge < 18) return life to "Wait until age 18 to start a career."
        if (life.inSchool) return life to "Finish your studies first (age 22 or next year)."

        val currentPrimarySkill = life.skills.levelOf(track.primarySkill)
        if (currentPrimarySkill < track.minPrimarySkill) {
            return life to "Requires ${track.minPrimarySkill}+ ${track.primarySkill.label} (currently $currentPrimarySkill). Train in Skill Activities or ${track.preferredEducation.label}!"
        }

        val hasMatchingEdu = life.completedEducation == track.preferredEducation || life.hasDegree
        if (track.requiresDegree && !hasMatchingEdu && life.stats.intellect < 78) {
            return life to "${track.title} requires ${track.preferredEducation.label} or 78+ Intellect."
        }
        if (track == CareerTrack.LAW && life.completedEducation != EducationType.LAW_SCHOOL && life.stats.intellect < 82) {
            return life to "${track.title} requires Law School or 82+ Intellect."
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
        val updatedRels = if (life.relationships.none { it.isAlive && it.category == BondCategory.WORK } && life.relationships.size < 37) {
            life.relationships + buildInitialWorkBonds(life.birthplace, life.currentAge, track)
        } else {
            life.relationships
        }
        val entry = JournalEntry(
            id = "e_job_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Joined ${track.title}",
            detail = "Entered as ${track.rungs.first().first} (${track.primarySkill.label} $currentPrimarySkill) alongside new teammates."
        )
        return life.copy(
            careerTrack = track,
            careerRung = 0,
            careerPerformance = (58 + (currentPrimarySkill / 5)).coerceIn(55, 85),
            crimeActive = false,
            fame = startFame,
            peakFame = maxOf(life.peakFame, startFame),
            relationships = updatedRels,
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
        val skillBoosted = life.skills
            .withBoost(life.careerTrack.primarySkill, 2)
            .withBoost(life.careerTrack.secondarySkill, 1)
        val bonus = (life.currentSalary * 0.08).toInt().coerceAtLeast(400)
        val fameGain = if (life.careerTrack.isFameTrack) 6 else 0
        val newFame = (life.fame + fameGain).coerceIn(0, 100)
        val entry = JournalEntry(
            id = "e_work_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Focused Sprint at Work",
            detail = "Honed ${life.careerTrack.primarySkill.label} (+2) as ${life.currentCareerTitle}, earning a $$bonus bonus."
        )
        val updated = life.copy(
            stats = s,
            skills = skillBoosted,
            netWorth = life.netWorth + bonus,
            careerPerformance = (life.careerPerformance + 12).coerceAtMost(100),
            fame = newFame,
            peakFame = maxOf(life.peakFame, newFame),
            entries = (listOf(entry) + life.entries).take(120)
        )
        return evaluateAchievements(updated)
    }

    fun performSkillActivity(life: LifeState, activity: SkillActivity): Pair<TurnResult, String> {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0) to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return TurnResult(life, emptySet(), 0) to "Unavailable while incarcerated."
        if (life.currentAge < 6) return TurnResult(life, emptySet(), 0) to "Skill training unlocks at age 6."
        if (activity.cashCost > 0 && life.netWorth < activity.cashCost) {
            return TurnResult(life, emptySet(), 0) to "Need $${activity.cashCost} cash for ${activity.title}."
        }
        if (life.stats.energy < 10) {
            return TurnResult(life, emptySet(), 0) to "Too tired — rest or eat well first."
        }

        val updatedSkills = life.skills.withBoost(activity.skill, activity.skillGain).let {
            if (activity == SkillActivity.THEATRE_WORKSHOP) it.withBoost(SkillType.PERSUASION_MEDIA, 2) else it
        }
        val updatedStats = when (activity) {
            SkillActivity.MOCK_TRIAL, SkillActivity.SYSTEMS_LAB -> life.stats.copy(intellect = life.stats.intellect + 3)
            SkillActivity.PAINT_STUDIO, SkillActivity.BAND_REHEARSAL, SkillActivity.TEST_KITCHEN -> life.stats.copy(creativity = life.stats.creativity + 3)
            SkillActivity.TRACK_DRILLS -> life.stats.copy(fitness = life.stats.fitness + 3)
            SkillActivity.THEATRE_WORKSHOP, SkillActivity.DEBATE_CLUB -> life.stats.copy(creativity = life.stats.creativity + 2, intellect = life.stats.intellect + 2)
        }.copy(energy = life.stats.energy - activity.energyCost).clamped()

        val entry = JournalEntry(
            id = "e_skill_${life.currentAge}_${Random.nextInt(9999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.MILESTONE,
            title = "Trained: ${activity.title}",
            detail = "Practiced ${activity.skill.label} (${activity.summary})."
        )
        val updatedLife = life.copy(
            skills = updatedSkills,
            stats = updatedStats,
            netWorth = (life.netWorth - activity.cashCost).coerceAtLeast(0),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val result = evaluateAchievements(updatedLife)
        return result to "${activity.skill.label} is now ${result.updatedLife.skills.levelOf(activity.skill)}/100!"
    }

    fun takeLoan(life: LifeState, amount: Int): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return life to "Cannot take a loan while incarcerated."
        if (life.currentAge < 18) return life to "Only adults (18+) can take a loan."
        if (amount <= 0) return life to "Choose a valid loan amount."
        val maxDebt = 150_000
        if (life.loanDebt + amount > maxDebt) {
            return life to "Credit limit reached (max $$maxDebt total loan)."
        }

        val entry = JournalEntry(
            id = "e_loan_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Approved $$amount Adult Loan",
            detail = "Added $$amount cash on hand (4% annual interest; total debt $${life.loanDebt + amount})."
        )
        return life.copy(
            netWorth = life.netWorth + amount,
            loanDebt = life.loanDebt + amount,
            entries = (listOf(entry) + life.entries).take(120)
        ) to "Approved $$amount loan! Cash increased."
    }

    fun repayLoan(life: LifeState, amount: Int): Pair<LifeState, String> {
        if (!life.isAlive) return life to "This chapter has closed."
        if (life.loanDebt <= 0) return life to "You have no outstanding loan balance."
        if (life.netWorth <= 0) return life to "No cash on hand to repay right now."
        val payment = minOf(amount, life.loanDebt, life.netWorth)
        if (payment <= 0) return life to "Nothing to repay."

        val remaining = life.loanDebt - payment
        val entry = JournalEntry(
            id = "e_repay_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.CAREER,
            title = "Repaid $$payment Loan",
            detail = if (remaining == 0) "Paid off your loan completely!" else "Remaining loan balance: $$remaining."
        )
        return life.copy(
            netWorth = (life.netWorth - payment).coerceAtLeast(0),
            loanDebt = remaining,
            stats = life.stats.copy(stress = life.stats.stress - 4).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        ) to if (remaining == 0) "Loan paid in full!" else "Paid $$payment toward loan ($$remaining left)."
    }

    private fun applyEducationSkillBoost(
        skills: CharacterSkills,
        stats: LifeStats,
        edu: EducationType
    ): Pair<CharacterSkills, LifeStats> = when (edu) {
        EducationType.LAW_SCHOOL -> skills.copy(
            deductionLaw = skills.deductionLaw + 12,
            persuasionMedia = skills.persuasionMedia + 8,
            engineeringTech = skills.engineeringTech + 2
        ).clamped() to stats.copy(intellect = stats.intellect + 8).clamped()

        EducationType.ART_UNI -> skills.copy(
            paintingArt = skills.paintingArt + 11,
            musicPerformance = skills.musicPerformance + 7,
            actingStage = skills.actingStage + 11,
            persuasionMedia = skills.persuasionMedia + 4
        ).clamped() to stats.copy(creativity = stats.creativity + 7).clamped()

        EducationType.MUSIC_ACADEMY -> skills.copy(
            musicPerformance = skills.musicPerformance + 14,
            actingStage = skills.actingStage + 6,
            paintingArt = skills.paintingArt + 4
        ).clamped() to stats.copy(creativity = stats.creativity + 7).clamped()

        EducationType.UNIVERSITY -> skills.copy(
            engineeringTech = skills.engineeringTech + 11,
            deductionLaw = skills.deductionLaw + 6,
            persuasionMedia = skills.persuasionMedia + 5
        ).clamped() to stats.copy(intellect = stats.intellect + 8).clamped()

        EducationType.TRADE_SCHOOL -> skills.copy(
            athleticDriving = skills.athleticDriving + 11,
            culinaryCraft = skills.culinaryCraft + 11,
            engineeringTech = skills.engineeringTech + 5
        ).clamped() to stats.copy(fitness = stats.fitness + 3).clamped()

        EducationType.NONE -> skills to stats
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
        val warmProducer = life.relationships.any {
            it.isAlive && (it.role.contains("Producer", ignoreCase = true) || it.role.contains("Mentor", ignoreCase = true)) && it.closeness >= 70
        }
        val isHit = Random.nextFloat() < (0.35f + life.stats.creativity / 220f + if (warmProducer) 0.12f else 0f)
        val fameGain = (if (isHit) Random.nextInt(14, 24) else Random.nextInt(7, 13)) + (if (warmProducer) 4 else 0)
        val rawPayout = if (isHit) Random.nextInt(1800, 4800) else Random.nextInt(400, 1200)
        val payout = if (warmProducer) (rawPayout * 1.25).toInt() else rawPayout
        val newFame = (life.fame + fameGain).coerceIn(0, 100)
        val newFollowers = if (life.socialChannel.followers > 0 || track.isFameTrack) fameGain * 140 else 0

        val entry = JournalEntry(
            id = "e_fame_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.FAME,
            title = if (isHit) "Breakout Hit: $actionTitle" else actionTitle,
            detail = "Gained +$fameGain Fame (now $newFame/100) and earned $$payout${if (warmProducer) " with producer backing" else ""}."
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
                netWorth = (life.netWorth - fine).coerceAtLeast(0),
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
        if (life.netWorth < 1500) return TurnResult(life, emptySet(), 0)
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
            netWorth = (life.netWorth - 1500).coerceAtLeast(0),
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
        if (life.netWorth < cost) return TurnResult(life, emptySet(), 0)
        val entry = JournalEntry(
            id = "e_well_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.WELLNESS,
            title = title,
            detail = detail
        )
        val nextLifeRaw = life.copy(
            netWorth = (life.netWorth - cost).coerceAtLeast(0),
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
        val nextLife = nextLifeRaw.copy(moodHistory = upsertMoodSnapshot(nextLifeRaw))
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
                if (life.netWorth < 150) return TurnResult(life, emptySet(), 0)
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
            "apologize" -> {
                closenessDelta = 15
                stressDelta = -5
                happyDelta = 4
                title = "Made Amends with ${target.name}"
                detail = "Offered a sincere apology to repair strained trust."
            }
            "argue" -> {
                closenessDelta = -20
                happyDelta = -6
                stressDelta = 8
                title = "Heated Argument with ${target.name}"
                detail = "Sharp words left hard feelings between you and ${target.name}."
            }
            "propose" -> {
                if (life.currentAge < 20) {
                    closenessDelta = -3
                    happyDelta = -2
                    title = "Too Young for Marriage"
                    detail = "Wait until age 20 before proposing marriage."
                } else if (target.closeness >= 75) {
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
                    role = if (becomesSpouse) "Spouse" else it.role,
                    interactedThisYear = true,
                    yearsNeglected = 0
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
            netWorth = (life.netWorth + cashDelta).coerceAtLeast(0),
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

    fun meetNewPerson(life: LifeState, category: BondCategory? = null): TurnResult {
        if (life.relationships.size >= 38) return TurnResult(life, emptySet(), 0)
        val pickedGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = pickedGender == CharacterGender.MALE)
        val fullName = "$fn $ln"

        val resolvedCat = category ?: when {
            life.currentAge in 6..17 || life.inSchool -> BondCategory.SCHOOL
            life.careerTrack != CareerTrack.UNEMPLOYED -> BondCategory.WORK
            else -> BondCategory.FRIEND
        }
        val role = when (resolvedCat) {
            BondCategory.SCHOOL -> if (life.currentAge >= 18) listOf("College Classmate", "Lab Partner", "Professor").random() else listOf("Classmate", "Study Partner", "Teacher").random()
            BondCategory.WORK -> when {
                life.careerTrack.isFameTrack -> listOf("Studio Producer", "Session Colleague", "Creative Manager").random()
                else -> listOf("Office Colleague", "Project Lead", "Team Manager").random()
            }
            else -> listOf("Friend", "Neighbor", "Reading Partner").random()
        }
        val npcAge = when {
            role.contains("Teacher", ignoreCase = true) || role.contains("Professor", ignoreCase = true) -> Random.nextInt(28, 56)
            role.contains("Manager", ignoreCase = true) || role.contains("Producer", ignoreCase = true) -> (life.currentAge + Random.nextInt(4, 15)).coerceIn(26, 62)
            life.currentAge < 18 -> (life.currentAge + Random.nextInt(-1, 2)).coerceIn(6, 17)
            else -> (life.currentAge + Random.nextInt(-4, 6)).coerceAtLeast(18)
        }
        val newRel = Relationship(
            id = "rel_${UUID.randomUUID().toString().take(6)}",
            name = fullName,
            role = role,
            age = npcAge,
            closeness = Random.nextInt(48, 68),
            gender = pickedGender,
            category = resolvedCat,
            interactedThisYear = true
        )
        val entry = JournalEntry(
            id = "e_meet_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Met $fullName",
            detail = "Connected with $fullName as your new $role."
        )
        val nextLife = life.copy(
            relationships = life.relationships + newRel,
            stats = life.stats.copy(happiness = life.stats.happiness + 5).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val (evaluated, unlocked, reward) = evaluateAchievements(nextLife)
        return TurnResult(evaluated, unlocked, reward)
    }

    fun dismissOrientationOnboarding(life: LifeState): LifeState =
        if (life.orientationPromptShown) life else life.copy(orientationPromptShown = true)

    fun setOrientation(life: LifeState, orientation: RomanceOrientation): TurnResult {
        if (orientation == RomanceOrientation.UNSET) return TurnResult(life, emptySet(), 0)
        val entry = JournalEntry(
            id = "e_orient_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Personal Horizon: ${orientation.label}",
            detail = "Reflected on romance and chose ${orientation.label.lowercase()} orientation."
        )
        val updated = life.copy(
            orientation = orientation,
            orientationPromptShown = true,
            entries = (listOf(entry) + life.entries).take(120)
        )
        return TurnResult(updated, emptySet(), 0)
    }

    fun compatiblePartnerGender(
        playerGender: CharacterGender,
        orientation: RomanceOrientation
    ): CharacterGender = when (orientation) {
        RomanceOrientation.HETEROSEXUAL -> when (playerGender) {
            CharacterGender.MALE -> CharacterGender.FEMALE
            CharacterGender.FEMALE -> CharacterGender.MALE
            CharacterGender.NON_BINARY -> if (Random.nextBoolean()) CharacterGender.FEMALE else CharacterGender.MALE
        }
        RomanceOrientation.HOMOSEXUAL -> when (playerGender) {
            CharacterGender.MALE -> CharacterGender.MALE
            CharacterGender.FEMALE -> CharacterGender.FEMALE
            CharacterGender.NON_BINARY -> CharacterGender.NON_BINARY
        }
        RomanceOrientation.UNSET -> CharacterGender.FEMALE
    }

    fun isCompatibleRomanceTarget(player: LifeState, candidate: Relationship): Boolean {
        if (player.orientation == RomanceOrientation.UNSET) return false
        if (!candidate.isAlive || candidate.category == BondCategory.FAMILY || candidate.isPartner || candidate.isSpouse) return false
        if (candidate.role.contains("Teacher", ignoreCase = true) ||
            candidate.role.contains("Professor", ignoreCase = true) ||
            candidate.role.contains("Parent", ignoreCase = true)
        ) return false
        val ageAppropriate = if (player.currentAge < 18) {
            candidate.age in 14..17
        } else {
            candidate.age >= 18
        }
        if (!ageAppropriate) return false
        return when (player.orientation) {
            RomanceOrientation.HETEROSEXUAL -> when (player.gender) {
                CharacterGender.MALE -> candidate.gender == CharacterGender.FEMALE
                CharacterGender.FEMALE -> candidate.gender == CharacterGender.MALE
                CharacterGender.NON_BINARY -> candidate.gender == CharacterGender.MALE || candidate.gender == CharacterGender.FEMALE
            }
            RomanceOrientation.HOMOSEXUAL -> when (player.gender) {
                CharacterGender.MALE -> candidate.gender == CharacterGender.MALE
                CharacterGender.FEMALE -> candidate.gender == CharacterGender.FEMALE
                CharacterGender.NON_BINARY -> candidate.gender == CharacterGender.NON_BINARY
            }
            RomanceOrientation.UNSET -> false
        }
    }

    fun seekRomance(
        life: LifeState,
        method: RomanceMethod,
        friendId: String? = null,
        outcomeRoll: Float = Random.nextFloat()
    ): Pair<TurnResult, String> {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0) to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return TurnResult(life, emptySet(), 0) to "Cannot pursue romance while incarcerated."
        if (life.currentAge < 14) return TurnResult(life, emptySet(), 0) to "Romance unlocks at age 14."
        if (life.currentAge < method.minAge) {
            return TurnResult(life, emptySet(), 0) to "${method.label} unlocks at age ${method.minAge}."
        }
        if (life.orientation == RomanceOrientation.UNSET) {
            return TurnResult(life, emptySet(), 0) to "Choose your orientation (Heterosexual or Homosexual) first."
        }

        // Strict anti-cheating check: prevent pursuing a new romance while already partnered or married
        val activePartners = life.relationships.filter { it.isAlive && (it.isPartner || it.isSpouse) }
        if (activePartners.isNotEmpty()) {
            val currentPartner = activePartners.first()
            return TurnResult(life, emptySet(), 0) to
                "Cheating prevented: you are already with ${currentPartner.name}. Break up first before seeking someone new."
        }

        if (method == RomanceMethod.FRIEND_TO_PARTNER) {
            val friend = life.relationships.firstOrNull { it.id == friendId }
                ?: return TurnResult(life, emptySet(), 0) to "Pick a compatible friend to confess your feelings to."
            if (!isCompatibleRomanceTarget(life, friend)) {
                return TurnResult(life, emptySet(), 0) to "${friend.name} is not a compatible romantic match."
            }

            val rejectThreshold = (0.50f - (friend.closeness / 280f)).coerceIn(0.18f, 0.55f)
            if (outcomeRoll < rejectThreshold) {
                val hurtRels = life.relationships.map {
                    if (it.id == friend.id) {
                        it.copy(
                            closeness = (it.closeness - 14).coerceAtLeast(10),
                            interactedThisYear = true,
                            yearsNeglected = 0
                        )
                    } else it
                }
                val entry = JournalEntry(
                    id = "e_rom_rej_${life.currentAge}_${Random.nextInt(999)}",
                    age = life.currentAge,
                    year = life.currentYear,
                    category = EntryCategory.RELATIONSHIP,
                    title = "Confession Rejected by ${friend.name}",
                    detail = "${friend.name} gently said they only see you as a friend, leaving things awkward (-14 Closeness, -8 Happiness)."
                )
                val nextLife = life.copy(
                    relationships = hurtRels,
                    stats = life.stats.copy(
                        happiness = life.stats.happiness - 8,
                        stress = life.stats.stress + 8,
                        anxiety = life.stats.anxiety + 7
                    ).clamped(),
                    entries = (listOf(entry) + life.entries).take(120)
                )
                return TurnResult(nextLife, emptySet(), 0) to "${friend.name} turned down your confession (-14 closeness, -8 happiness)."
            }

            val promotedRels = life.relationships.map {
                if (it.id == friend.id) {
                    it.copy(
                        isPartner = true,
                        role = "Partner",
                        category = BondCategory.ROMANCE,
                        closeness = (it.closeness + 10).coerceAtMost(100),
                        interactedThisYear = true,
                        yearsNeglected = 0
                    )
                } else it
            }
            val entry = JournalEntry(
                id = "e_rom_friend_${life.currentAge}_${Random.nextInt(999)}",
                age = life.currentAge,
                year = life.currentYear,
                category = EntryCategory.RELATIONSHIP,
                title = "Together with ${friend.name}",
                detail = "${friend.name} felt the same way — your friendship blossomed into romance!"
            )
            val nextLife = life.copy(
                relationships = promotedRels,
                stats = life.stats.copy(
                    happiness = life.stats.happiness + 11,
                    stress = life.stats.stress - 4,
                    karma = life.stats.karma + 2
                ).clamped(),
                entries = (listOf(entry) + life.entries).take(120)
            )
            val eval = evaluateAchievements(nextLife)
            return eval to "Now dating ${friend.name}!"
        }

        // Dating App (18+) or Blind Date (14+)
        val targetGender = compatiblePartnerGender(life.gender, life.orientation)
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(
            life.birthplace,
            isMale = when (targetGender) {
                CharacterGender.MALE -> true
                CharacterGender.FEMALE -> false
                CharacterGender.NON_BINARY -> null
            }
        )
        val matchName = "$fn $ln"

        val rejectChance = when (method) {
            RomanceMethod.DATING_APP -> (0.42f - (life.stats.appearance / 450f)).coerceIn(0.22f, 0.45f)
            RomanceMethod.BLIND_DATE -> 0.35f
            RomanceMethod.FRIEND_TO_PARTNER -> 0.30f
        }
        if (outcomeRoll < rejectChance) {
            val rejTitle = if (method == RomanceMethod.DATING_APP) "No Match on Dating App" else "No Spark on Blind Date"
            val rejDetail = if (method == RomanceMethod.DATING_APP) {
                "Matches fizzled out and $matchName quietly unmatched before meeting."
            } else {
                "You met $matchName on a blind date, but they felt no romantic chemistry."
            }
            val entry = JournalEntry(
                id = "e_rom_miss_${life.currentAge}_${Random.nextInt(999)}",
                age = life.currentAge,
                year = life.currentYear,
                category = EntryCategory.RELATIONSHIP,
                title = rejTitle,
                detail = "$rejDetail (-7 Happiness, +7 Stress)."
            )
            val nextLife = life.copy(
                stats = life.stats.copy(
                    happiness = life.stats.happiness - 7,
                    stress = life.stats.stress + 7,
                    anxiety = life.stats.anxiety + 6
                ).clamped(),
                entries = (listOf(entry) + life.entries).take(120)
            )
            return TurnResult(nextLife, emptySet(), 0) to "$rejTitle (-7 Happiness)."
        }

        val matchAge = if (life.currentAge < 18) {
            (life.currentAge + Random.nextInt(-1, 2)).coerceIn(14, 17)
        } else {
            (life.currentAge + Random.nextInt(-2, 4)).coerceIn(18, 85)
        }
        val newPartner = Relationship(
            id = "rom_${UUID.randomUUID().toString().take(6)}",
            name = matchName,
            role = "Romantic Partner",
            age = matchAge,
            closeness = Random.nextInt(54, 72),
            isPartner = true,
            gender = targetGender,
            category = BondCategory.ROMANCE,
            interactedThisYear = true
        )
        val entry = JournalEntry(
            id = "e_rom_new_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Started Dating $matchName",
            detail = "Connected via ${method.label} and hit it off immediately."
        )
        val nextLife = life.copy(
            relationships = if (life.relationships.size < 39) life.relationships + newPartner else life.relationships,
            stats = life.stats.copy(
                happiness = life.stats.happiness + 10,
                stress = life.stats.stress - 3
            ).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val eval = evaluateAchievements(nextLife)
        return eval to "Started dating $matchName!"
    }

    fun breakUpWithPartner(life: LifeState, relId: String): TurnResult {
        val target = life.relationships.firstOrNull { it.id == relId && (it.isPartner || it.isSpouse) }
            ?: return TurnResult(life, emptySet(), 0)
        val updatedRels = life.relationships.map {
            if (it.id == relId) {
                it.copy(
                    isPartner = false,
                    isSpouse = false,
                    role = if (target.isSpouse) "Former Spouse" else "Ex-Partner",
                    closeness = (it.closeness - 28).coerceIn(10, 100),
                    interactedThisYear = true
                )
            } else it
        }
        val entry = JournalEntry(
            id = "e_breakup_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Parted Ways with ${target.name}",
            detail = "Ended your romantic relationship cleanly so both of you can move forward."
        )
        val nextLife = life.copy(
            relationships = updatedRels,
            stats = life.stats.copy(happiness = life.stats.happiness - 8, stress = life.stats.stress + 6).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        return TurnResult(nextLife, emptySet(), 0)
    }

    fun tryHaveOrAdoptChild(life: LifeState, partnerId: String? = null): Pair<TurnResult, String> {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0) to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return TurnResult(life, emptySet(), 0) to "Unavailable while incarcerated."
        if (life.currentAge < 18) return TurnResult(life, emptySet(), 0) to "Available at age 18+."
        if (life.childrenCount >= 6 || life.relationships.size >= 39) {
            return TurnResult(life, emptySet(), 0) to "Your household is full (${life.childrenCount} children)."
        }

        val partner = if (partnerId != null) {
            life.relationships.firstOrNull { it.id == partnerId && it.isAlive && (it.isSpouse || it.isPartner) }
        } else {
            life.relationships.firstOrNull { it.isAlive && (it.isSpouse || (it.isPartner && it.closeness >= 75)) }
        }

        if (partner == null && life.currentAge < 24) {
            return TurnResult(life, emptySet(), 0) to "Build a committed partner/spouse bond (75+ closeness) first."
        }
        if (partner != null && partner.closeness < 70) {
            return TurnResult(life, emptySet(), 0) to "Build 70+ closeness with ${partner.name} first."
        }

        val childGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val firstName = CountryCatalog.randomCompanionFirstName(life.birthplace, isMale = childGender == CharacterGender.MALE)
        val lastName = life.characterName.substringAfterLast(" ", "")
        val childName = if (lastName.isNotBlank()) "$firstName $lastName" else firstName
        val newChildNum = life.childrenCount + 1

        val childRel = Relationship(
            id = "child_${life.currentAge}_${UUID.randomUUID().toString().take(6)}",
            name = childName,
            role = "Child",
            age = 0,
            closeness = 94,
            gender = childGender,
            category = BondCategory.FAMILY,
            interactedThisYear = true
        )
        val entry = JournalEntry(
            id = "e_child_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Welcomed $childName",
            detail = if (partner != null) {
                "You and ${partner.name} welcomed child #$newChildNum, $childName, into your home."
            } else {
                "Adopted child #$newChildNum, $childName, expanding your family."
            }
        )
        val nextLife = life.copy(
            relationships = life.relationships + childRel,
            stats = life.stats.copy(happiness = life.stats.happiness + 14, stress = life.stats.stress + 4).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val eval = evaluateAchievements(nextLife)
        return eval to "Welcomed $childName (Child #$newChildNum)!"
    }

    fun triggerRandomEncounter(life: LifeState, roll: Float = Random.nextFloat()): TurnResult {
        if (!life.isAlive || life.prisonYearsLeft > 0 || life.currentAge < 6) {
            return TurnResult(life, emptySet(), 0)
        }
        val encounterGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = encounterGender == CharacterGender.MALE)
        val fullName = "$fn $ln"

        return when {
            roll < 0.45f && life.pendingPrompt == null -> {
                val contextLabel = when {
                    life.careerTrack != CareerTrack.UNEMPLOYED -> "Office Breakroom"
                    life.currentAge in 6..21 || life.inSchool -> "Campus Courtyard"
                    else -> "Neighborhood Market"
                }
                val prompt = LifeChoicePrompt(
                    id = "p_enc_${life.currentAge}_${Random.nextInt(999)}",
                    category = EntryCategory.RANDOM_EVENT,
                    title = "Random Encounter: $contextLabel",
                    narrative = "$fullName struck up a lively conversation about a side collaboration and local rumors.",
                    options = listOf(
                        ChoiceOption(
                            id = "opt_enc_warm",
                            label = "Listen Warmly & Exchange Contacts",
                            effectHint = "+6 Happiness, +3 Karma, +Connections",
                            outcomeSummary = "Shared stories over coffee and gained fresh perspective with $fullName.",
                            happinessDelta = 6,
                            karmaDelta = 3,
                            relationshipBoostAll = 2
                        ),
                        ChoiceOption(
                            id = "opt_enc_blunt",
                            label = "Brush Them Off Coldly",
                            effectHint = "-3 Bonds, +4 Stress, Focus on Work",
                            outcomeSummary = "Kept headphones on and snapped back cleanly, though gossip lingered.",
                            stressDelta = 4,
                            careerBonus = true,
                            relationshipBoostAll = -3
                        )
                    )
                )
                TurnResult(life.copy(pendingPrompt = prompt), emptySet(), 0)
            }
            roll < 0.78f -> {
                val cat = when {
                    life.careerTrack != CareerTrack.UNEMPLOYED -> BondCategory.WORK
                    life.currentAge in 6..18 || life.inSchool -> BondCategory.SCHOOL
                    else -> BondCategory.FRIEND
                }
                val role = when (cat) {
                    BondCategory.WORK -> if (life.careerTrack.isFameTrack) "Studio Producer" else "Office Colleague"
                    BondCategory.SCHOOL -> "Classmate"
                    else -> "Neighborhood Acquaintance"
                }
                val newRel = Relationship(
                    id = "enc_${UUID.randomUUID().toString().take(6)}",
                    name = fullName,
                    role = role,
                    age = (life.currentAge + Random.nextInt(-3, 5)).coerceAtLeast(6),
                    closeness = Random.nextInt(52, 72),
                    gender = encounterGender,
                    category = cat,
                    interactedThisYear = true
                )
                val entry = JournalEntry(
                    id = "e_enc_${life.currentAge}_${Random.nextInt(999)}",
                    age = life.currentAge,
                    year = life.currentYear,
                    category = EntryCategory.RELATIONSHIP,
                    title = "Chance Encounter: $fullName",
                    detail = "Crossed paths unexpectedly in ${life.birthplace} and added $fullName ($role) to your circle."
                )
                val nextLifeRaw = life.copy(
                    relationships = if (life.relationships.size < 39) life.relationships + newRel else life.relationships,
                    stats = life.stats.copy(happiness = life.stats.happiness + 5).clamped(),
                    entries = (listOf(entry) + life.entries).take(120)
                )
                val nextLife = nextLifeRaw.copy(
                    moodHistory = upsertMoodSnapshot(nextLifeRaw, encounterLabel = entry.title)
                )
                evaluateAchievements(nextLife)
            }
            else -> {
                val foundCash = listOf(120, 250, 480).random()
                val entry = JournalEntry(
                    id = "e_enc_luck_${life.currentAge}_${Random.nextInt(999)}",
                    age = life.currentAge,
                    year = life.currentYear,
                    category = EntryCategory.RANDOM_EVENT,
                    title = "Unexpected Street Musician & Windfall",
                    detail = "Chatted with local busker $fullName and won a cafe raffle for $$foundCash."
                )
                val nextLifeRaw = life.copy(
                    netWorth = life.netWorth + foundCash,
                    stats = life.stats.copy(happiness = life.stats.happiness + 6, creativity = life.stats.creativity + 2).clamped(),
                    entries = (listOf(entry) + life.entries).take(120)
                )
                val nextLife = nextLifeRaw.copy(
                    moodHistory = upsertMoodSnapshot(nextLifeRaw, encounterLabel = entry.title)
                )
                evaluateAchievements(nextLife)
            }
        }
    }

    fun computeCareerStressLoad(life: LifeState): Int = when {
        life.prisonYearsLeft > 0 -> 16
        life.inSchool -> 8
        life.crimeActive -> 15
        life.careerTrack == CareerTrack.MEDICINE || life.careerTrack == CareerTrack.RACE_DRIVER -> 14
        life.careerTrack.isFameTrack -> 11
        life.careerTrack != CareerTrack.UNEMPLOYED -> 9
        else -> 2
    }

    private fun upsertMoodSnapshot(
        life: LifeState,
        encounterLabel: String? = null
    ): List<MoodSnapshot> {
        val baseHistory = ensureMoodHistory(life).moodHistory.toMutableList()
        val existingIdx = baseHistory.indexOfLast { it.age == life.currentAge }
        val existing = baseHistory.getOrNull(existingIdx)
        val mergedEncounter = encounterLabel?.takeIf { it.isNotBlank() }
            ?: existing?.encounterLabel.orEmpty()
        val updatedSnap = MoodSnapshot(
            age = life.currentAge,
            year = life.currentYear,
            happiness = life.stats.happiness,
            stress = life.stats.stress,
            anxiety = life.stats.anxiety,
            careerStressLoad = computeCareerStressLoad(life),
            hadRandomEncounter = (existing?.hadRandomEncounter == true) || mergedEncounter.isNotBlank(),
            encounterLabel = mergedEncounter
        )
        if (existingIdx >= 0) {
            baseHistory[existingIdx] = updatedSnap
        } else {
            baseHistory.add(updatedSnap)
        }
        return baseHistory.distinctBy { it.age }.sortedBy { it.age }.takeLast(10)
    }

    fun ensureMoodHistory(life: LifeState): LifeState {
        if (life.moodHistory.isNotEmpty()) return life
        val startAge = (life.currentAge - 9).coerceAtLeast(0)
        val baseCareerLoad = computeCareerStressLoad(life)
        val reconstructed = (startAge..life.currentAge).map { agePoint ->
            val yearPoint = life.birthYear + agePoint
            val yearEntries = life.entries.filter { it.age == agePoint }
            val encounterTitle = yearEntries.firstOrNull {
                it.category == EntryCategory.RANDOM_EVENT || it.title.contains("Encounter", ignoreCase = true)
            }?.title.orEmpty()
            val distanceFromNow = life.currentAge - agePoint
            if (distanceFromNow == 0) {
                MoodSnapshot(
                    age = agePoint,
                    year = yearPoint,
                    happiness = life.stats.happiness,
                    stress = life.stats.stress,
                    anxiety = life.stats.anxiety,
                    careerStressLoad = baseCareerLoad,
                    hadRandomEncounter = encounterTitle.isNotBlank(),
                    encounterLabel = encounterTitle
                )
            } else {
                val stressDeltaOffset = if (encounterTitle.isNotBlank()) 6 else -2
                MoodSnapshot(
                    age = agePoint,
                    year = yearPoint,
                    happiness = (life.stats.happiness + ((distanceFromNow % 3) - 1) * 3).coerceIn(5, 100),
                    stress = (life.stats.stress + stressDeltaOffset - (distanceFromNow % 4)).coerceIn(0, 100),
                    anxiety = (life.stats.anxiety + (stressDeltaOffset / 2)).coerceIn(0, 100),
                    careerStressLoad = if (agePoint < 6) 0 else if (agePoint < 18) 8 else baseCareerLoad,
                    hadRandomEncounter = encounterTitle.isNotBlank(),
                    encounterLabel = encounterTitle
                )
            }
        }
        return life.copy(moodHistory = reconstructed)
    }

    private fun buildInitialWorkBonds(
        birthplace: String,
        playerAge: Int,
        track: CareerTrack
    ): List<Relationship> {
        val g1 = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val (fn1, ln1) = CountryCatalog.randomNameForBirthplace(birthplace, isMale = g1 == CharacterGender.MALE)
        val g2 = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val (fn2, ln2) = CountryCatalog.randomNameForBirthplace(birthplace, isMale = g2 == CharacterGender.MALE)

        val peerRole = if (track.isFameTrack) "Studio Peer" else "Office Colleague"
        val leadRole = if (track.isFameTrack) "Studio Producer" else "Team Manager"

        return listOf(
            Relationship(
                id = "work_peer_${UUID.randomUUID().toString().take(6)}",
                name = "$fn1 $ln1",
                role = peerRole,
                age = (playerAge + Random.nextInt(-3, 6)).coerceIn(20, 62),
                closeness = Random.nextInt(52, 70),
                gender = g1,
                category = BondCategory.WORK
            ),
            Relationship(
                id = "work_lead_${UUID.randomUUID().toString().take(6)}",
                name = "$fn2 $ln2",
                role = leadRole,
                age = (playerAge + Random.nextInt(5, 16)).coerceIn(28, 65),
                closeness = Random.nextInt(48, 66),
                gender = g2,
                category = BondCategory.WORK
            )
        )
    }

    fun ensureGlobalNewsFeed(life: LifeState): LifeState {
        if (life.globalNewsFeed.isNotEmpty()) return life
        return life.copy(
            globalNewsFeed = listOf(generateAnnualGlobalNews(life.currentYear, life.birthplace))
        )
    }

    private enum class WorldEra {
        ANALOG_COLD_WAR,    // <= 1979
        EARLY_GLOBAL_NET,   // 1980..1999
        DIGITAL_BOOM,       // 2000..2019
        AI_BIOTECH_SHIFT,   // 2020..2035
        ORBITAL_FUSION      // >= 2036
    }

    private data class RegionalFocus(
        val corridor: String,
        val techCluster: String,
        val culturalHub: String
    )

    private fun eraForYear(year: Int): WorldEra = when {
        year <= 1979 -> WorldEra.ANALOG_COLD_WAR
        year <= 1999 -> WorldEra.EARLY_GLOBAL_NET
        year <= 2019 -> WorldEra.DIGITAL_BOOM
        year <= 2035 -> WorldEra.AI_BIOTECH_SHIFT
        else -> WorldEra.ORBITAL_FUSION
    }

    private fun regionalFocusFor(region: String): RegionalFocus = when (region) {
        "Southeast Asia" -> RegionalFocus(
            corridor = "Strait of Malacca & ASEAN corridors",
            techCluster = "archipelagic clean-grid & maritime robotics labs",
            culturalHub = "night-market artisans & indie cinema collectives"
        )
        "East Asia" -> RegionalFocus(
            corridor = "East Asian maritime & semiconductor lanes",
            techCluster = "precision fab & high-speed rail engineering hubs",
            culturalHub = "animation studios & street-fashion districts"
        )
        "South Asia" -> RegionalFocus(
            corridor = "Indian Ocean trade & Himalayan frontier routes",
            techCluster = "Bengaluru-to-coastal software & biotech campuses",
            culturalHub = "classical-fusion festivals & independent book fairs"
        )
        "Europe" -> RegionalFocus(
            corridor = "continental energy & Schengen transit borders",
            techCluster = "pan-European particle, climate & rail consortia",
            culturalHub = "public theater guilds & cafe-salon movements"
        )
        "North America" -> RegionalFocus(
            corridor = "Atlantic-Pacific freight & continental defense treaties",
            techCluster = "aerospace, silicon & medical research corridors",
            culturalHub = "college radio, indie stages & neighborhood studios"
        )
        "South America" -> RegionalFocus(
            corridor = "Andean lithium & South Atlantic shipping accords",
            techCluster = "hydro-grid & tropical agro-science institutes",
            culturalHub = "muralist collectives & acoustic street gatherings"
        )
        "Oceania" -> RegionalFocus(
            corridor = "Coral-Tasman maritime & Pacific island alliances",
            techCluster = "marine renewables & astronomical observatory networks",
            culturalHub = "coastal surf-craft guilds & outdoor amphitheaters"
        )
        "West Africa" -> RegionalFocus(
            corridor = "Gulf of Guinea trade & ECOWAS overland links",
            techCluster = "solar micro-grid & mobile fintech incubators",
            culturalHub = "Afro-percussion studios & textile cooperatives"
        )
        else -> RegionalFocus(
            corridor = "$region border & trade routes",
            techCluster = "$region research universities",
            culturalHub = "$region neighborhood arts spaces"
        )
    }

    fun generateAnnualGlobalNews(year: Int, birthplace: String): GlobalNewsItem {
        val country = CountryCatalog.findCountryForBirthplace(birthplace)
        val region = country.region
        val era = eraForYear(year)
        val focus = regionalFocusFor(region)
        val cat = NewsCategory.entries[Math.floorMod(year, NewsCategory.entries.size)]
        val variant = Math.floorMod(year / 3, 2)

        return when (cat) {
            NewsCategory.WAR_GEOPOLITICS -> {
                val (headline, summary, delta) = when (era) {
                    WorldEra.ANALOG_COLD_WAR -> if (variant == 0) {
                        Triple(
                            "$region Proxy Standoff Tightens ($year)",
                            "Superpower blocs contested ${focus.corridor}, raising fuel rationing concerns across ${country.countryName}.",
                            -0.04f
                        )
                    } else {
                        Triple(
                            "Non-Aligned Peace Summit in $region",
                            "Envoys stabilized border checkpoints along ${focus.corridor}, easing Cold War trade tariffs.",
                            0.03f
                        )
                    }
                    WorldEra.EARLY_GLOBAL_NET -> if (variant == 0) {
                        Triple(
                            "Post-Bloc Border Restructuring ($year)",
                            "Regional treaties redrew customs rules across ${focus.corridor}, creating short-term market turbulence.",
                            -0.03f
                        )
                    } else {
                        Triple(
                            "$region Maritime Security Accord",
                            "Coastal states signed joint anti-piracy patrols protecting merchant shipping near ${country.countryName}.",
                            0.03f
                        )
                    }
                    WorldEra.DIGITAL_BOOM -> if (variant == 0) {
                        Triple(
                            "$region Trade & Cyber Conflict Alert",
                            "Cross-border infrastructure disruptions and tariff disputes strained logistics along ${focus.corridor}.",
                            -0.04f
                        )
                    } else {
                        Triple(
                            "Ceasefire & Corridor Pact in $region",
                            "Diplomats brokered a multi-year ceasefire and energy-transit guarantee boosting confidence in $region.",
                            0.03f
                        )
                    }
                    WorldEra.AI_BIOTECH_SHIFT -> if (variant == 0) {
                        Triple(
                            "Chip & Rare-Earth Supply Conflict ($year)",
                            "Escalating sanctions and proxy flare-ups disrupted ${focus.corridor}, lifting freight and energy costs.",
                            -0.04f
                        )
                    } else {
                        Triple(
                            "$region Joint Defense & Aid Accord",
                            "Neighboring governments launched a shared disaster-response and border de-escalation pact.",
                            0.03f
                        )
                    }
                    WorldEra.ORBITAL_FUSION -> if (variant == 0) {
                        Triple(
                            "Orbital-Lane & Arctic Resource Dispute",
                            "Rival coalitions clashed over satellite spectrum and mineral rights impacting $region markets.",
                            -0.03f
                        )
                    } else {
                        Triple(
                            "$year Sovereign Commons Treaty",
                            "Coalitions spanning ${country.countryName} and $region ratified peaceful deep-sea and orbital trade rules.",
                            0.04f
                        )
                    }
                }
                GlobalNewsItem(
                    id = "news_${year}_geo",
                    year = year,
                    region = region,
                    category = NewsCategory.WAR_GEOPOLITICS,
                    headline = headline,
                    summary = summary,
                    economyDelta = delta
                )
            }

            NewsCategory.SCIENCE_TECH -> {
                val (headline, summary, delta) = when (era) {
                    WorldEra.ANALOG_COLD_WAR -> if (variant == 0) {
                        Triple(
                            "Long-Range Broadcast & Radar Milestone",
                            "Engineers in ${focus.techCluster} launched weather-tracking telemetry improving harvests across $region.",
                            0.03f
                        )
                    } else {
                        Triple(
                            "$region Public Vaccine & Sanitation Drive",
                            "University clinics in ${country.countryName} scaled low-cost immunization, lifting life expectancy.",
                            0.02f
                        )
                    }
                    WorldEra.EARLY_GLOBAL_NET -> if (variant == 0) {
                        Triple(
                            "Fiber-Optic Backbone Reaches $region",
                            "Undersea cables linked ${focus.techCluster} to early public networks, sparking tech startups.",
                            0.04f
                        )
                    } else {
                        Triple(
                            "High-Efficiency Microprocessor Wave ($year)",
                            "Labs in $region trimmed personal-computer manufacturing costs, bringing desktops into local schools.",
                            0.03f
                        )
                    }
                    WorldEra.DIGITAL_BOOM -> if (variant == 0) {
                        Triple(
                            "$region Solar & Grid Storage Breakthrough",
                            "Researchers at ${focus.techCluster} cut utility battery costs, accelerating urban electrification.",
                            0.04f
                        )
                    } else {
                        Triple(
                            "Mobile Cloud Studio Boom in $region",
                            "Creators and small firms across ${country.countryName} adopted pocket-sized 4G production tools.",
                            0.03f
                        )
                    }
                    WorldEra.AI_BIOTECH_SHIFT -> if (variant == 0) {
                        Triple(
                            "$year Preventive Biotech Milestone",
                            "Trials in ${focus.techCluster} validated rapid early disease screening across $region hospitals.",
                            0.03f
                        )
                    } else {
                        Triple(
                            "Open-Weight Local AI Tools Arrive ($year)",
                            "Engineers in $region shipped offline creative and coding assistants that supercharged indie studios.",
                            0.04f
                        )
                    }
                    WorldEra.ORBITAL_FUSION -> if (variant == 0) {
                        Triple(
                            "Compact Fusion Pilot Online in $region",
                            "A municipal reactor in ${focus.techCluster} delivered zero-carbon baseload power for ${country.countryName}.",
                            0.05f
                        )
                    } else {
                        Triple(
                            "Cellular Longevity Therapy Approved ($year)",
                            "$region medical centers rolled out tissue-repair treatments extending healthy working years.",
                            0.04f
                        )
                    }
                }
                GlobalNewsItem(
                    id = "news_${year}_sci",
                    year = year,
                    region = region,
                    category = NewsCategory.SCIENCE_TECH,
                    headline = headline,
                    summary = summary,
                    economyDelta = delta
                )
            }

            NewsCategory.CULTURAL_SHIFT -> {
                val (headline, summary, delta) = when (era) {
                    WorldEra.ANALOG_COLD_WAR -> if (variant == 0) {
                        Triple(
                            "$region Youth Radio & Vinyl Wave",
                            "Student bands and ${focus.culturalHub} reshaped urban style across ${country.countryName}.",
                            0.02f
                        )
                    } else {
                        Triple(
                            "Regional Cinema New Wave ($year)",
                            "Directors from $region won international acclaim for grounded stories of everyday family life.",
                            0.02f
                        )
                    }
                    WorldEra.EARLY_GLOBAL_NET -> if (variant == 0) {
                        Triple(
                            "Independent Zines & Cafe Revival ($year)",
                            "${focus.culturalHub.replaceFirstChar { it.uppercase() }} turned local corners into late-night creative hubs.",
                            0.02f
                        )
                    } else {
                        Triple(
                            "$region Streetwear & Arcade Crossover",
                            "Youth fashion and cassette culture in ${country.countryName} sparked an export boom across $region.",
                            0.03f
                        )
                    }
                    WorldEra.DIGITAL_BOOM -> if (variant == 0) {
                        Triple(
                            "$region Creator & Podcast Renaissance",
                            "Independent voices in ${focus.culturalHub} built global audiences straight from bedroom studios.",
                            0.03f
                        )
                    } else {
                        Triple(
                            "Slow-Food & Heritage Craft Revival",
                            "Younger households across ${country.countryName} championed regional recipes and local makers.",
                            0.02f
                        )
                    }
                    WorldEra.AI_BIOTECH_SHIFT -> if (variant == 0) {
                        Triple(
                            "Four-Day Workweek Pilots Expand ($year)",
                            "Companies in $region tested shorter schedules, energizing offline community clubs and wellness.",
                            0.02f
                        )
                    } else {
                        Triple(
                            "$region Analog Third-Space Surge",
                            "Tired of endless feeds, locals flocked to ${focus.culturalHub} across ${country.countryName}.",
                            0.02f
                        )
                    }
                    WorldEra.ORBITAL_FUSION -> if (variant == 0) {
                        Triple(
                            "Post-Commute Civic Garden Boom ($year)",
                            "Automated transit freed downtown boulevards in $region for amphitheaters and ${focus.culturalHub}.",
                            0.03f
                        )
                    } else {
                        Triple(
                            "$region Heritage Archive Renaissance",
                            "Holographic museums and live guilds revived classic crafts throughout ${country.countryName}.",
                            0.02f
                        )
                    }
                }
                GlobalNewsItem(
                    id = "news_${year}_cul",
                    year = year,
                    region = region,
                    category = NewsCategory.CULTURAL_SHIFT,
                    headline = headline,
                    summary = summary,
                    economyDelta = delta
                )
            }
        }
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
        if (life.currentAge < 13) {
            return if (ms.missions.isEmpty() && ms.dateKey == dateKey) life
            else life.copy(
                missionState = ms.copy(
                    dateKey = dateKey,
                    missions = emptyList(),
                    dailyBonusClaimed = false
                )
            )
        }
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
        if (ensured.currentAge < 13) return MissionOutcome(ensured, null, 0, 0, null)
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
        if (ensured.currentAge < 13) return MissionOutcome(ensured, null, 0, 0, null)
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
        if (ensured.currentAge < 13) return MissionOutcome(ensured, null, 0, 0, null)
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
                val patronGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                val (fn, ln) = CountryCatalog.randomNameForBirthplace(birthplace, isMale = patronGender == CharacterGender.MALE)
                val mentorName = "$fn $ln"
                RandomEventResult(
                    entry = JournalEntry(
                        id = "e_mentor_$age",
                        age = age,
                        year = year,
                        category = EntryCategory.RANDOM_EVENT,
                        title = "Met Influential Producer $mentorName",
                        detail = "A respected creative producer praised your voice and opened new doors (+600 followers)."
                    ),
                    statsAfter = stats.copy(creativity = stats.creativity + 5, happiness = stats.happiness + 7),
                    followerDelta = if (channel.followers > 0) 600 else 250,
                    newConnection = Relationship(
                        id = "mentor_${UUID.randomUUID().toString().take(6)}",
                        name = mentorName,
                        role = "Studio Producer",
                        age = (age + Random.nextInt(8, 18)).coerceAtMost(75),
                        closeness = 72,
                        gender = patronGender,
                        category = BondCategory.WORK
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

    fun computePopularityScore(life: LifeState): Int {
        val followerPts = when {
            life.socialChannel.followers >= 50_000 -> 36
            life.socialChannel.followers >= 10_000 -> 26
            life.socialChannel.followers >= 2_500 -> 18
            life.socialChannel.followers >= 500 -> 10
            life.socialChannel.followers >= 100 -> 5
            else -> 2
        }
        val famePts = (life.fame * 0.42f).toInt()
        val repPts = ((life.socialChannel.reputation - 50) / 3).coerceIn(-8, 15)
        val warmCirclePts = (life.relationships.count { it.isAlive && it.closeness >= 65 } * 3).coerceAtMost(15)
        val hatePenalty = (life.hateCount * 4).coerceAtMost(16)
        return (followerPts + famePts + repPts + warmCirclePts - hatePenalty).coerceIn(5, 100)
    }

    fun popularityTierLabel(score: Int): String = when {
        score >= 82 -> "Global Icon"
        score >= 62 -> "Viral Voice"
        score >= 40 -> "City Favorite"
        score >= 22 -> "Growing Circle"
        else -> "Quiet Local"
    }

    fun suggestedSocialPostPrompts(life: LifeState, topic: SocialPostTopic): List<String> = when (topic) {
        SocialPostTopic.LIFE -> listOf(
            "Slow morning walk around ${life.birthplace} — soaking in year ${life.currentYear}.",
            "Reflecting on turning ${life.currentAge} and keeping things grounded.",
            "Small daily wins today: brewed fresh tea and wrote in my journal."
        )
        SocialPostTopic.CAREER -> listOf(
            if (life.inSchool) "Deep into ${life.educationType.label} coursework — growing every week!"
            else if (life.careerTrack != CareerTrack.UNEMPLOYED) "Putting in focused hours as ${life.currentCareerTitle}."
            else "Exploring new creative projects and sharpening my craft!",
            if (life.professionalStanding >= 72) "Grateful for supportive teammates & mentors on the journey."
            else "Navigating office deadlines and keeping steady pace."
        )
        SocialPostTopic.RELATIONSHIP -> {
            val partner = life.relationships.firstOrNull { it.isAlive && (it.isSpouse || it.isPartner) }
            val friend = life.relationships.firstOrNull { it.isAlive && it.category == BondCategory.FRIEND }
            listOfNotNull(
                when {
                    partner != null -> "Quality evening out with ${partner.name} — wouldn't trade it."
                    friend != null -> "Long overdue catch-up coffee with ${friend.name}!"
                    else -> "Feeling grateful for the people who keep me grounded."
                },
                if (partner != null && friend != null) "Long overdue catch-up coffee with ${friend.name}!" else null,
                "Checking in with family and old friends in ${life.birthplace} tonight."
            )
        }
    }

    fun postToSocialStream(
        life: LifeState,
        topic: SocialPostTopic,
        customMessage: String,
        rollSeed: Int = Random.nextInt(1000)
    ): Pair<TurnResult, String> {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0) to "This chapter has closed."
        if (life.prisonYearsLeft > 0) return TurnResult(life, emptySet(), 0) to "Social posting is disabled while incarcerated."
        if (life.currentAge < 13) return TurnResult(life, emptySet(), 0) to "Social Stream unlocks at Age 13."

        val cleanContent = customMessage.trim().ifBlank {
            suggestedSocialPostPrompts(life, topic).first()
        }.take(200)

        val popularity = computePopularityScore(life)
        val followerBase = (life.socialChannel.followers / 45).coerceAtMost(2400)
        val fameBase = life.fame * 9
        val warmBonus = life.relationships.count { it.isAlive && it.closeness >= 60 } * 3
        val topicMult = when (topic) {
            SocialPostTopic.CAREER -> if (life.careerTrack != CareerTrack.UNEMPLOYED) 1.15f else 0.95f
            SocialPostTopic.RELATIONSHIP -> if (life.activePartnerCount > 0 || life.relationships.any { it.closeness >= 75 }) 1.2f else 1.0f
            SocialPostTopic.LIFE -> 1.05f
        }
        val rawLikes = (((popularity * 1.4f) + followerBase + fameBase + warmBonus) * topicMult).toInt()
        val jitter = ((rollSeed % 11) - 4).coerceAtLeast(0)
        val totalLikes = (rawLikes + jitter).coerceAtLeast(3)

        val livingCircle = life.relationships.filter { it.isAlive }
        val sortedWarm = livingCircle.filter { it.closeness >= 50 }.sortedByDescending { it.closeness }
        val hostilePool = livingCircle.filter { it.closeness < 25 }

        val targetCommentCount = when {
            popularity >= 72 -> 4
            popularity >= 42 -> 3
            popularity >= 18 -> 2
            else -> 1
        }

        val generatedComments = mutableListOf<SocialStreamComment>()
        // 1. Add up to 2 close NPC reactions
        sortedWarm.take(2).forEachIndexed { idx, rel ->
            val commentBody = when (topic) {
                SocialPostTopic.LIFE -> if (rel.isSpouse || rel.isPartner) {
                    "Love this snapshot of us this year ❤️"
                } else if (rel.role == "Mother" || rel.role == "Father") {
                    "So proud of how grounded you are, dear!"
                } else {
                    "Looking happy! Let's catch up in ${life.birthplace.substringBefore(",")} soon."
                }
                SocialPostTopic.CAREER -> if (rel.category == BondCategory.WORK || rel.category == BondCategory.SCHOOL) {
                    "Respect the hustle — always good collaborating with you!"
                } else {
                    "Rooting for your next milestone as ${life.currentCareerTitle}!"
                }
                SocialPostTopic.RELATIONSHIP -> if (rel.isSpouse || rel.isPartner) {
                    "Best chapter yet with you by my side!"
                } else {
                    "Warmest vibes! Good people make all the difference."
                }
            }
            generatedComments.add(
                SocialStreamComment(
                    id = "c_npc_${rel.id}_${rollSeed}_$idx",
                    authorName = rel.name,
                    authorRelation = rel.role,
                    body = commentBody,
                    isSupportive = true
                )
            )
        }

        // 2. If someone hates the character, chance for a sharp comment
        if (hostilePool.isNotEmpty() && generatedComments.size < targetCommentCount) {
            val hater = hostilePool.first()
            val snark = when (topic) {
                SocialPostTopic.CAREER -> "Funny how everything looks curated online vs. in person..."
                SocialPostTopic.RELATIONSHIP -> "Must be nice pretending everyone gets along."
                SocialPostTopic.LIFE -> "Sure, let's see how long that mood lasts."
            }
            generatedComments.add(
                SocialStreamComment(
                    id = "c_hate_${hater.id}_$rollSeed",
                    authorName = hater.name,
                    authorRelation = hater.role,
                    body = snark,
                    isSupportive = false
                )
            )
        }

        // 3. Popularity-driven public fan/follower comments to fill up to targetCommentCount
        val (fanFirst, fanLast) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = rollSeed % 2 == 0)
        val fanPool = listOf(
            Triple("$fanFirst $fanLast", "Follower", "Followed from ${life.birthplace.substringBefore(",")} — keep sharing!"),
            Triple("@cozy_reader_${(rollSeed % 89) + 10}", "Superfan", "Always brightens my feed seeing updates from ${life.socialChannel.handle}!"),
            Triple("@city_pulse_${(rollSeed % 79) + 20}", "Community", "Big energy around this ${topic.label.lowercase()}!")
        )
        var fanIdx = 0
        while (generatedComments.size < targetCommentCount && fanIdx < fanPool.size) {
            val (name, roleTag, body) = fanPool[fanIdx]
            generatedComments.add(
                SocialStreamComment(
                    id = "c_fan_${rollSeed}_$fanIdx",
                    authorName = name,
                    authorRelation = roleTag,
                    body = body,
                    isSupportive = true
                )
            )
            fanIdx++
        }

        val post = SocialStreamPost(
            id = "post_${life.currentAge}_${UUID.randomUUID().toString().take(6)}",
            authorName = life.characterName,
            authorHandle = life.socialChannel.handle,
            topic = topic,
            age = life.currentAge,
            year = life.currentYear,
            content = cleanContent,
            likes = totalLikes,
            comments = generatedComments
        )

        val followerGain = (popularity / 4) + (generatedComments.count { it.isSupportive } * 4) + 5
        val happyShift = if (generatedComments.any { !it.isSupportive } && generatedComments.none { it.isSupportive }) -3 else 4
        val entry = JournalEntry(
            id = "e_stream_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.SOCIAL,
            title = "Shared ${topic.label} on Social Stream",
            detail = "\"${cleanContent.take(80)}\" — received $totalLikes likes & ${generatedComments.size} NPC replies."
        )

        val updatedRaw = life.copy(
            socialFeed = (listOf(post) + life.socialFeed).take(20),
            socialChannel = life.socialChannel.copy(
                followers = life.socialChannel.followers + followerGain,
                reputation = (life.socialChannel.reputation + 1).coerceIn(0, 100)
            ),
            stats = life.stats.copy(happiness = life.stats.happiness + happyShift).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val updated = updatedRaw.copy(moodHistory = upsertMoodSnapshot(updatedRaw))
        val eval = evaluateAchievements(updated)
        val msg = "Posted! $totalLikes likes & ${generatedComments.size} replies (+${followerGain} followers)."
        return eval to msg
    }

    fun checkDailyRandomEvent(
        life: LifeState,
        dateKey: String = currentDayKey(),
        roll: Float = Random.nextFloat()
    ): LifeState {
        if (!life.isAlive || life.prisonYearsLeft > 0 || life.currentAge < 6) return life
        if (life.lastDailyEventDateKey == dateKey || life.pendingDailyEvent != null) return life
        val event = generateDailyRandomEvent(life, dateKey, roll)
        return life.copy(
            lastDailyEventDateKey = dateKey,
            pendingDailyEvent = event
        )
    }

    fun triggerDailyRandomEventNow(
        life: LifeState,
        dateKey: String = currentDayKey(),
        roll: Float = Random.nextFloat()
    ): LifeState {
        if (!life.isAlive || life.prisonYearsLeft > 0 || life.currentAge < 6) return life
        val event = generateDailyRandomEvent(life, dateKey, roll)
        return life.copy(
            lastDailyEventDateKey = dateKey,
            pendingDailyEvent = event
        )
    }

    fun generateDailyRandomEvent(
        life: LifeState,
        dateKey: String = currentDayKey(),
        roll: Float = Random.nextFloat()
    ): DailyRandomEvent {
        val city = life.birthplace.substringBefore(",")
        val existingAcq = life.relationships.firstOrNull {
            it.isAlive && it.category != BondCategory.FAMILY && it.closeness in 25..78
        }
        val acqGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
        val (fn, ln) = CountryCatalog.randomNameForBirthplace(life.birthplace, isMale = acqGender == CharacterGender.MALE)
        val acquaintanceName = existingAcq?.name ?: "$fn $ln"
        val r = roll.coerceIn(0f, 0.9999f)
        val cashScale = if (life.currentAge >= 18) 1 else 0

        return when (life.trait) {
            TemperamentTrait.CURIOUS -> when {
                r < 0.34f -> {
                    val found = if (cashScale == 1) 140 else 35
                    DailyRandomEvent(
                        id = "daily_${dateKey}_cur_book",
                        dateKey = dateKey,
                        trait = TemperamentTrait.CURIOUS,
                        category = DailyEventCategory.MONEY_FIND,
                        title = "Folded Bills Inside a Used Book",
                        narrative = "While browsing a corner bookstall in $city, your curious habit of checking old bookmarks uncovered $$found tucked inside a classic paperback.",
                        isPositiveTone = true,
                        choices = listOf(
                            DailyEventChoice(
                                id = "keep_book_cash",
                                label = "Pocket the Windfall & Read",
                                effectHint = "+$$found Cash, +6 Happiness, +4 Intellect",
                                outcomeSummary = "Kept the tucked-away $$found and spent an inspiring afternoon reading.",
                                cashDelta = found,
                                happinessDelta = 6,
                                intellectDelta = 4
                            ),
                            DailyEventChoice(
                                id = "leave_at_counter",
                                label = "Hand It to the Shopkeeper",
                                effectHint = "+8 Karma, +5 Happiness, +3 Intellect",
                                outcomeSummary = "Handed the $$found to the stall owner, earning warm gratitude and peace of mind.",
                                happinessDelta = 5,
                                intellectDelta = 3,
                                karmaDelta = 8
                            )
                        )
                    )
                }
                r < 0.67f -> DailyRandomEvent(
                    id = "daily_${dateKey}_cur_item",
                    dateKey = dateKey,
                    trait = TemperamentTrait.CURIOUS,
                    category = DailyEventCategory.LOST_ITEM,
                    title = "Found a Lost Brass Compass",
                    narrative = "A gleaming engraved pocket compass sat near the $city fountain. Inspecting the inscription reveals a faded neighborhood address.",
                    isPositiveTone = true,
                    choices = listOf(
                        DailyEventChoice(
                            id = "trace_owner",
                            label = "Track Down the Owner",
                            effectHint = "+60 Cash Reward, +7 Karma, +5 Intellect",
                            outcomeSummary = "Followed the engraving clues, returned the compass, and received a thank-you reward.",
                            cashDelta = if (cashScale == 1) 60 else 20,
                            happinessDelta = 6,
                            intellectDelta = 5,
                            karmaDelta = 7
                        ),
                        DailyEventChoice(
                            id = "examine_and_keep",
                            label = "Study & Keep It as a Curio",
                            effectHint = "+6 Intellect, +4 Creativity, -4 Karma",
                            outcomeSummary = "Kept the vintage compass on your desk for inspiration, though guilt lingered.",
                            happinessDelta = 2,
                            intellectDelta = 6,
                            creativityDelta = 4,
                            karmaDelta = -4
                        )
                    )
                )
                else -> DailyRandomEvent(
                    id = "daily_${dateKey}_cur_acq",
                    dateKey = dateKey,
                    trait = TemperamentTrait.CURIOUS,
                    category = DailyEventCategory.OLD_ACQUAINTANCE,
                    title = "Bumped Into Old Acquaintance $acquaintanceName",
                    narrative = "You spotted $acquaintanceName near a $city newsstand and got pulled into an intense debate that dragged longer than expected.",
                    isPositiveTone = false,
                    choices = listOf(
                        DailyEventChoice(
                            id = "debate_through",
                            label = "Stay & Debate the Mystery",
                            effectHint = "+5 Intellect, -5 Energy, +6 Stress, -4 Happiness",
                            outcomeSummary = "Shared sharp ideas with $acquaintanceName, though the heated exchange left you drained.",
                            happinessDelta = -4,
                            intellectDelta = 5,
                            energyDelta = -5,
                            stressDelta = 6,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        ),
                        DailyEventChoice(
                            id = "brief_hello",
                            label = "Share a Quick Coffee & Part",
                            effectHint = "+4 Happiness, -3 Stress, +Bond",
                            outcomeSummary = "Kept things friendly with $acquaintanceName over espresso before heading back calmly.",
                            happinessDelta = 4,
                            stressDelta = -3,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        )
                    )
                )
            }

            TemperamentTrait.EMPATHETIC -> when {
                r < 0.34f -> DailyRandomEvent(
                    id = "daily_${dateKey}_emp_item",
                    dateKey = dateKey,
                    trait = TemperamentTrait.EMPATHETIC,
                    category = DailyEventCategory.LOST_ITEM,
                    title = "Found a Lost Family Locket",
                    narrative = "Spotted a silver locket tucked beside a $city bench with a tiny family photo inside. Someone nearby must be worried sick.",
                    isPositiveTone = true,
                    choices = listOf(
                        DailyEventChoice(
                            id = "return_locket",
                            label = "Wait & Return the Locket",
                            effectHint = "+10 Happiness, +9 Karma, +50 Cash",
                            outcomeSummary = "Reunited the tearful owner with their keepsake and left glowing with warmth.",
                            cashDelta = if (cashScale == 1) 50 else 15,
                            happinessDelta = 10,
                            karmaDelta = 9,
                            stressDelta = -4
                        ),
                        DailyEventChoice(
                            id = "pawn_locket",
                            label = "Sell It Quietly Nearby",
                            effectHint = "+130 Cash, -8 Happiness, +7 Anxiety, -9 Karma",
                            outcomeSummary = "Sold the locket for quick cash, but empathy kept nagging at your mood all week.",
                            cashDelta = if (cashScale == 1) 130 else 35,
                            happinessDelta = -8,
                            anxietyDelta = 7,
                            karmaDelta = -9
                        )
                    )
                )
                r < 0.67f -> DailyRandomEvent(
                    id = "daily_${dateKey}_emp_acq",
                    dateKey = dateKey,
                    trait = TemperamentTrait.EMPATHETIC,
                    category = DailyEventCategory.OLD_ACQUAINTANCE,
                    title = "Old Acquaintance $acquaintanceName Opened Up",
                    narrative = "Running into $acquaintanceName outside a $city cafe turned into a heavy heart-to-heart about recent struggles.",
                    isPositiveTone = false,
                    choices = listOf(
                        DailyEventChoice(
                            id = "listen_deeply",
                            label = "Listen With Full Heart",
                            effectHint = "+7 Karma, -6 Happiness, +7 Anxiety, +Bond",
                            outcomeSummary = "Comforted $acquaintanceName for an hour; you felt deeply connected yet emotionally worn.",
                            happinessDelta = -6,
                            anxietyDelta = 7,
                            stressDelta = 5,
                            karmaDelta = 7,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        ),
                        DailyEventChoice(
                            id = "share_encouragement",
                            label = "Offer Warm Encouragement",
                            effectHint = "+5 Happiness, +4 Karma, +Bond",
                            outcomeSummary = "Gave $acquaintanceName a genuine hug and arranged a calmer catch-up next week.",
                            happinessDelta = 5,
                            karmaDelta = 4,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        )
                    )
                )
                else -> {
                    val cash = if (cashScale == 1) 110 else 25
                    DailyRandomEvent(
                        id = "daily_${dateKey}_emp_money",
                        dateKey = dateKey,
                        trait = TemperamentTrait.EMPATHETIC,
                        category = DailyEventCategory.MONEY_FIND,
                        title = "Stumbled Upon Unclaimed Street Cash",
                        narrative = "Wind blew crisp notes worth $$cash across an empty $city crosswalk with nobody in sight.",
                        isPositiveTone = true,
                        choices = listOf(
                            DailyEventChoice(
                                id = "split_charity",
                                label = "Keep Half & Tip a Street Musician",
                                effectHint = "+$${cash / 2} Cash, +8 Happiness, +6 Karma",
                                outcomeSummary = "Shared your lucky find generously and lifted everyone's afternoon.",
                                cashDelta = cash / 2,
                                happinessDelta = 8,
                                karmaDelta = 6
                            ),
                            DailyEventChoice(
                                id = "pocket_all",
                                label = "Pocket the Entire Find",
                                effectHint = "+$$cash Cash, +3 Happiness, -2 Karma",
                                outcomeSummary = "Tucked the stray bills away for a rainy day.",
                                cashDelta = cash,
                                happinessDelta = 3,
                                karmaDelta = -2
                            )
                        )
                    )
                }
            }

            TemperamentTrait.ARTISTIC -> when {
                r < 0.34f -> DailyRandomEvent(
                    id = "daily_${dateKey}_art_item",
                    dateKey = dateKey,
                    trait = TemperamentTrait.ARTISTIC,
                    category = DailyEventCategory.LOST_ITEM,
                    title = "Found a Lost Vintage Sketchbook",
                    narrative = "Under a gallery awning in $city, you discovered a rain-proofed sketchbook filled with charcoal studies and a local studio phone number.",
                    isPositiveTone = true,
                    choices = listOf(
                        DailyEventChoice(
                            id = "return_sketchbook",
                            label = "Return It to the Artist",
                            effectHint = "+7 Creativity, +7 Happiness, +6 Karma",
                            outcomeSummary = "Returned the sketchbook and toured the grateful artist's sunlit studio.",
                            happinessDelta = 7,
                            creativityDelta = 7,
                            karmaDelta = 6
                        ),
                        DailyEventChoice(
                            id = "sketch_on_back",
                            label = "Drop It at Lost & Found Quickly",
                            effectHint = "+3 Creativity, +3 Karma",
                            outcomeSummary = "Left the notebook safely at the reception desk before hurrying onward.",
                            creativityDelta = 3,
                            karmaDelta = 3
                        )
                    )
                )
                r < 0.67f -> DailyRandomEvent(
                    id = "daily_${dateKey}_art_twist",
                    dateKey = dateKey,
                    trait = TemperamentTrait.ARTISTIC,
                    category = DailyEventCategory.DAILY_TWIST,
                    title = "Sudden Downpour Soaked Your Bag",
                    narrative = "An unforecast cloudburst in $city drenched your satchel before you reached cover, smudging fresh notes and forcing an unexpected replacement cost.",
                    isPositiveTone = false,
                    choices = listOf(
                        DailyEventChoice(
                            id = "salvage_supplies",
                            label = "Dry What You Can & Rest",
                            effectHint = "-$45 Cash, -7 Happiness, +8 Stress, +2 Creativity",
                            outcomeSummary = "Spent a bit replacing ruined paper, though the rain light inspired tomorrow's palette.",
                            cashDelta = if (cashScale == 1) -45 else -12,
                            happinessDelta = -7,
                            creativityDelta = 2,
                            stressDelta = 8
                        ),
                        DailyEventChoice(
                            id = "brush_it_off",
                            label = "Laugh at the Mess & Improvise",
                            effectHint = "-4 Happiness, +4 Stress, +4 Creativity",
                            outcomeSummary = "Turned smudged ink pages into abstract textures instead of letting it spoil the day.",
                            happinessDelta = -4,
                            creativityDelta = 4,
                            stressDelta = 4
                        )
                    )
                )
                else -> {
                    val found = if (cashScale == 1) 160 else 40
                    DailyRandomEvent(
                        id = "daily_${dateKey}_art_acq",
                        dateKey = dateKey,
                        trait = TemperamentTrait.ARTISTIC,
                        category = DailyEventCategory.OLD_ACQUAINTANCE,
                        title = "Old Acquaintance $acquaintanceName Bought Your Print",
                        narrative = "At a $city street market, old acquaintance $acquaintanceName spotted you, raved about your style, and insisted on buying a small keepsake study.",
                        isPositiveTone = true,
                        choices = listOf(
                            DailyEventChoice(
                                id = "accept_commission",
                                label = "Catch Up & Seal the Sale",
                                effectHint = "+$$found Cash, +9 Happiness, +5 Creativity",
                                outcomeSummary = "Reconnected joyfully with $acquaintanceName and walked away richer in cash and confidence.",
                                cashDelta = found,
                                happinessDelta = 9,
                                creativityDelta = 5,
                                newAcquaintanceName = acquaintanceName,
                                acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                            )
                        )
                    )
                }
            }

            TemperamentTrait.AMBITIOUS -> when {
                r < 0.34f -> {
                    val cash = if (cashScale == 1) 240 else 50
                    DailyRandomEvent(
                        id = "daily_${dateKey}_amb_money",
                        dateKey = dateKey,
                        trait = TemperamentTrait.AMBITIOUS,
                        category = DailyEventCategory.MONEY_FIND,
                        title = "Stumbled Upon an Unclaimed Cash Envelope",
                        narrative = "Near the $city transit concourse, you spotted an unmarked crisp envelope holding $$cash beside an empty luggage rack.",
                        isPositiveTone = true,
                        choices = listOf(
                            DailyEventChoice(
                                id = "claim_windfall",
                                label = "Reinvest the $$cash Immediately",
                                effectHint = "+$$cash Cash, +7 Happiness, -5 Karma",
                                outcomeSummary = "Pocketed the $$cash windfall and put it straight into your savings momentum.",
                                cashDelta = cash,
                                happinessDelta = 7,
                                karmaDelta = -5
                            ),
                            DailyEventChoice(
                                id = "turn_in_station",
                                label = "Log It With Station Security",
                                effectHint = "+8 Karma, +4 Intellect, +4 Happiness",
                                outcomeSummary = "Left your contact info with security; acting with integrity bolstered your reputation.",
                                happinessDelta = 4,
                                intellectDelta = 4,
                                karmaDelta = 8
                            )
                        )
                    )
                }
                r < 0.67f -> DailyRandomEvent(
                    id = "daily_${dateKey}_amb_acq",
                    dateKey = dateKey,
                    trait = TemperamentTrait.AMBITIOUS,
                    category = DailyEventCategory.OLD_ACQUAINTANCE,
                    title = "Sharp Run-In With Old Acquaintance $acquaintanceName",
                    narrative = "You bumped into hyper-competitive $acquaintanceName outside a $city office lobby; their bragging about big deals tested your composure.",
                    isPositiveTone = false,
                    choices = listOf(
                        DailyEventChoice(
                            id = "push_harder",
                            label = "Match Their Energy & Overwork",
                            effectHint = "-6 Happiness, +10 Stress, +7 Anxiety, +4 Intellect",
                            outcomeSummary = "Turned the encounter into late-night hustle, sharpening your edge at the cost of heavy stress.",
                            happinessDelta = -6,
                            intellectDelta = 4,
                            stressDelta = 10,
                            anxietyDelta = 7,
                            energyDelta = -6,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        ),
                        DailyEventChoice(
                            id = "stay_composed",
                            label = "Smile Politely & Protect Focus",
                            effectHint = "+4 Happiness, -3 Stress, +Bond",
                            outcomeSummary = "Exchanged cards gracefully with $acquaintanceName without letting comparison rattle you.",
                            happinessDelta = 4,
                            stressDelta = -3,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        )
                    )
                )
                else -> DailyRandomEvent(
                    id = "daily_${dateKey}_amb_item",
                    dateKey = dateKey,
                    trait = TemperamentTrait.AMBITIOUS,
                    category = DailyEventCategory.LOST_ITEM,
                    title = "Found a Lost Executive Keycard",
                    narrative = "An unlabelled building keycard and sleek fountain pen lay near a $city cab stand.",
                    isPositiveTone = true,
                    choices = listOf(
                        DailyEventChoice(
                            id = "deliver_reception",
                            label = "Deliver to Lobby Reception",
                            effectHint = "+90 Cash Tip, +6 Happiness, +5 Karma",
                            outcomeSummary = "Returned the pen and card personally, earning gratitude and an unexpected connection.",
                            cashDelta = if (cashScale == 1) 90 else 25,
                            happinessDelta = 6,
                            karmaDelta = 5
                        )
                    )
                )
            }

            TemperamentTrait.RESILIENT -> when {
                r < 0.34f -> {
                    val found = if (cashScale == 1) 125 else 30
                    DailyRandomEvent(
                        id = "daily_${dateKey}_res_money",
                        dateKey = dateKey,
                        trait = TemperamentTrait.RESILIENT,
                        category = DailyEventCategory.MONEY_FIND,
                        title = "Morning Brisk Walk Money Find",
                        narrative = "Taking the brisk uphill shortcut through $city paid off: you spotted $$found caught in hedge leaves along the empty path.",
                        isPositiveTone = true,
                        choices = listOf(
                            DailyEventChoice(
                                id = "keep_walk_cash",
                                label = "Collect It & Finish Your Lap",
                                effectHint = "+$$found Cash, +6 Happiness, +4 Fitness",
                                outcomeSummary = "Picked up $$found and finished the morning route energized and grounded.",
                                cashDelta = found,
                                happinessDelta = 6,
                                fitnessDelta = 4,
                                energyDelta = 3
                            )
                        )
                    )
                }
                r < 0.67f -> DailyRandomEvent(
                    id = "daily_${dateKey}_res_item",
                    dateKey = dateKey,
                    trait = TemperamentTrait.RESILIENT,
                    category = DailyEventCategory.LOST_ITEM,
                    title = "Misplaced & Recovered Your House Keys",
                    narrative = "A rushed morning commute in $city left you locked out for an hour until a neighbor spotted your spare key tag.",
                    isPositiveTone = false,
                    choices = listOf(
                        DailyEventChoice(
                            id = "locksmith_check",
                            label = "Stay Calm & Secure a Spare",
                            effectHint = "-$35 Cash, -4 Happiness, +5 Stress, +3 Health",
                            outcomeSummary = "Handled the lockout without panicking and made a backup key for peace of mind.",
                            cashDelta = if (cashScale == 1) -35 else -10,
                            happinessDelta = -4,
                            healthDelta = 3,
                            stressDelta = 5
                        ),
                        DailyEventChoice(
                            id = "long_wait",
                            label = "Wait It Out Outside",
                            effectHint = "-7 Happiness, -5 Energy, +8 Stress",
                            outcomeSummary = "Lost an hour pacing on the porch before getting back indoors exhausted.",
                            happinessDelta = -7,
                            energyDelta = -5,
                            stressDelta = 8
                        )
                    )
                )
                else -> DailyRandomEvent(
                    id = "daily_${dateKey}_res_acq",
                    dateKey = dateKey,
                    trait = TemperamentTrait.RESILIENT,
                    category = DailyEventCategory.OLD_ACQUAINTANCE,
                    title = "Ran Into Old Acquaintance $acquaintanceName Jogging",
                    narrative = "Crossing paths with $acquaintanceName on the $city riverside trail sparked an uplifting pace-matching jog.",
                    isPositiveTone = true,
                    choices = listOf(
                        DailyEventChoice(
                            id = "jog_together",
                            label = "Match Strides & Catch Up",
                            effectHint = "+8 Happiness, +5 Fitness, -6 Stress, +Bond",
                            outcomeSummary = "Ran three kilometers beside $acquaintanceName and left feeling strong and centered.",
                            happinessDelta = 8,
                            fitnessDelta = 5,
                            healthDelta = 3,
                            stressDelta = -6,
                            newAcquaintanceName = acquaintanceName,
                            acquaintanceRole = existingAcq?.role ?: "Old Acquaintance"
                        )
                    )
                )
            }
        }
    }

    fun resolveDailyRandomEvent(life: LifeState, choice: DailyEventChoice): TurnResult {
        val event = life.pendingDailyEvent ?: return TurnResult(life, emptySet(), 0)
        val nextStats = life.stats.copy(
            happiness = life.stats.happiness + choice.happinessDelta,
            health = life.stats.health + choice.healthDelta,
            intellect = life.stats.intellect + choice.intellectDelta,
            creativity = life.stats.creativity + choice.creativityDelta,
            energy = life.stats.energy + choice.energyDelta,
            fitness = life.stats.fitness + choice.fitnessDelta,
            stress = life.stats.stress + choice.stressDelta,
            anxiety = life.stats.anxiety + choice.anxietyDelta,
            karma = life.stats.karma + choice.karmaDelta
        ).clamped()

        val nextCash = (life.netWorth + choice.cashDelta).coerceAtLeast(0)

        var updatedRels = life.relationships
        if (choice.newAcquaintanceName.isNotBlank()) {
            val existingIdx = updatedRels.indexOfFirst {
                it.isAlive && it.name.equals(choice.newAcquaintanceName, ignoreCase = true)
            }
            if (existingIdx >= 0) {
                updatedRels = updatedRels.mapIndexed { idx, rel ->
                    if (idx == existingIdx) {
                        val delta = if (choice.happinessDelta >= 0) 10 else 4
                        rel.copy(
                            closeness = (rel.closeness + delta).coerceIn(0, 100),
                            interactedThisYear = true,
                            yearsNeglected = 0
                        )
                    } else rel
                }
            } else if (updatedRels.size < 39) {
                val acqGender = if (Random.nextBoolean()) CharacterGender.MALE else CharacterGender.FEMALE
                updatedRels = updatedRels + Relationship(
                    id = "daily_acq_${UUID.randomUUID().toString().take(6)}",
                    name = choice.newAcquaintanceName,
                    role = choice.acquaintanceRole.ifBlank { "Old Acquaintance" },
                    age = (life.currentAge + Random.nextInt(-2, 3)).coerceAtLeast(6),
                    closeness = if (choice.happinessDelta >= 0) 62 else 46,
                    gender = acqGender,
                    category = BondCategory.FRIEND,
                    interactedThisYear = true
                )
            }
        }

        val entry = JournalEntry(
            id = "e_daily_${event.dateKey}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RANDOM_EVENT,
            title = event.title,
            detail = "${choice.outcomeSummary} (${choice.effectHint})"
        )

        val updatedRaw = life.copy(
            stats = nextStats,
            netWorth = nextCash,
            relationships = updatedRels,
            pendingDailyEvent = null,
            entries = (listOf(entry) + life.entries).take(120)
        )
        val updatedWithMood = updatedRaw.copy(
            moodHistory = upsertMoodSnapshot(updatedRaw, encounterLabel = event.title)
        )
        return evaluateAchievements(updatedWithMood)
    }

    fun dismissDailyRandomEvent(life: LifeState): LifeState {
        if (life.pendingDailyEvent == null) return life
        return life.copy(pendingDailyEvent = null)
    }

    fun setRelationshipPriority(
        life: LifeState,
        relId: String,
        priority: RelationshipPriority
    ): LifeState {
        val target = life.relationships.firstOrNull { it.id == relId && it.isAlive } ?: return life
        if (target.priority == priority) return life
        val updatedRels = life.relationships.map {
            if (it.id == relId) it.copy(priority = priority) else it
        }
        return life.copy(relationships = updatedRels)
    }

    fun executePrioritizedBondBoost(life: LifeState): Pair<TurnResult, String> {
        if (!life.isAlive) return TurnResult(life, emptySet(), 0) to "This chapter has closed."
        val prioritized = life.relationships.filter { it.isAlive && it.priority != RelationshipPriority.STANDARD }
        if (prioritized.isEmpty()) {
            return TurnResult(life, emptySet(), 0) to "Prioritize at least one NPC for interaction or gifting first."
        }

        var remainingCash = life.netWorth
        var interactedCount = 0
        var giftedCount = 0
        var skippedGifts = 0

        val updatedRels = life.relationships.map { rel ->
            if (!rel.isAlive) {
                rel
            } else when (rel.priority) {
                RelationshipPriority.INTERACTION -> {
                    interactedCount++
                    rel.copy(
                        closeness = (rel.closeness + 8).coerceIn(0, 100),
                        interactedThisYear = true,
                        yearsNeglected = 0
                    )
                }
                RelationshipPriority.GIFTING -> {
                    if (remainingCash >= 150) {
                        remainingCash -= 150
                        giftedCount++
                        rel.copy(
                            closeness = (rel.closeness + 14).coerceIn(0, 100),
                            interactedThisYear = true,
                            yearsNeglected = 0
                        )
                    } else {
                        skippedGifts++
                        rel
                    }
                }
                RelationshipPriority.STANDARD -> rel
            }
        }

        if (interactedCount == 0 && giftedCount == 0) {
            return TurnResult(life, emptySet(), 0) to "Need $150 per prioritized gift (earned cash too low)."
        }

        val summaryParts = buildList {
            if (interactedCount > 0) add("checked in with $interactedCount prioritized contact(s) (+8 bond)")
            if (giftedCount > 0) add("sent $150 gifts to $giftedCount contact(s) (+14 bond)")
            if (skippedGifts > 0) add("skipped $skippedGifts gift(s) due to cash")
        }.joinToString(", ")

        val entry = JournalEntry(
            id = "e_prio_${life.currentAge}_${Random.nextInt(999)}",
            age = life.currentAge,
            year = life.currentYear,
            category = EntryCategory.RELATIONSHIP,
            title = "Prioritized Social Circle Check-In",
            detail = summaryParts.replaceFirstChar { it.uppercase() } + "."
        )

        val nextLife = life.copy(
            netWorth = remainingCash.coerceAtLeast(0),
            relationships = updatedRels,
            stats = life.stats.copy(
                happiness = life.stats.happiness + 5,
                stress = life.stats.stress - 4,
                anxiety = life.stats.anxiety - 2
            ).clamped(),
            entries = (listOf(entry) + life.entries).take(120)
        )
        val eval = evaluateAchievements(nextLife)
        return eval to summaryParts.replaceFirstChar { it.uppercase() } + "."
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
