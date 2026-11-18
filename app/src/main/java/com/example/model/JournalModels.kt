package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LifeStage(val label: String, val badge: String, val minAge: Int) {
    INFANCY("Infancy", "Early Years", 0),
    CHILDHOOD("Childhood", "Primary School", 6),
    ADOLESCENCE("Adolescence", "Teen Years", 13),
    YOUNG_ADULT("Young Adulthood", "Coming of Age", 18),
    MIDLIFE("Midlife", "Prime & Stride", 35),
    SENIOR("Senior Years", "Silver Chapter", 60),
    ELDER("Elder Wisdom", "Golden Chapter", 80);

    companion object {
        fun fromAge(age: Int): LifeStage = entries.lastOrNull { age >= it.minAge } ?: INFANCY
    }
}

enum class TemperamentTrait(val label: String, val summary: String) {
    CURIOUS("Curious", "+Intellect & faster skill mastery"),
    RESILIENT("Resilient", "+Lower stress & stronger disease resistance"),
    EMPATHETIC("Empathetic", "+Deeper bonds & higher karma"),
    ARTISTIC("Artistic", "+Creativity & viral storytelling boost"),
    AMBITIOUS("Ambitious", "+Faster promotions & higher creator payouts")
}

enum class CharacterGender(val label: String, val defaultPronouns: String) {
    MALE("Male", "he/him"),
    FEMALE("Female", "she/her"),
    NON_BINARY("Non-binary", "they/them");

    companion object {
        fun fromPronouns(pronouns: String): CharacterGender = when {
            pronouns.startsWith("he", ignoreCase = true) -> MALE
            pronouns.startsWith("she", ignoreCase = true) -> FEMALE
            else -> NON_BINARY
        }
    }
}

enum class RomanceOrientation(val label: String, val description: String) {
    UNSET("Not Chosen", "Choose once before seeking romance"),
    HETEROSEXUAL("Heterosexual", "Seek opposite-gender romantic matches"),
    HOMOSEXUAL("Homosexual", "Seek same-gender romantic matches")
}

enum class RomanceMethod(val label: String, val minAge: Int, val summary: String) {
    DATING_APP("Dating App", 18, "Match with locals nearby (18+ · rejection risk)"),
    BLIND_DATE("Blind Date", 14, "Friend-arranged cafe or ice-cream meetup (14+ · may have no spark)"),
    FRIEND_TO_PARTNER("Confess to Friend", 14, "Ask a close friend to date (14+ · risks bond if rejected)")
}

enum class BondCategory(val label: String) {
    FAMILY("Family"),
    FRIEND("Friend"),
    SCHOOL("School"),
    WORK("Work"),
    ROMANCE("Romance"),
    SOCIAL_CONTACT("Community")
}

enum class NewsCategory(val label: String) {
    WAR_GEOPOLITICS("Conflict & Geopolitics"),
    SCIENCE_TECH("Science & Breakthroughs"),
    CULTURAL_SHIFT("Cultural Shift")
}

data class GlobalNewsItem(
    val id: String,
    val year: Int,
    val region: String,
    val category: NewsCategory,
    val headline: String,
    val summary: String,
    val economyDelta: Float = 0f
)

data class MoodSnapshot(
    val age: Int,
    val year: Int,
    val happiness: Int,
    val stress: Int,
    val anxiety: Int,
    val careerStressLoad: Int = 0,
    val hadRandomEncounter: Boolean = false,
    val encounterLabel: String = ""
)

enum class SocialPostTopic(val label: String) {
    LIFE("Life Update"),
    CAREER("Career"),
    RELATIONSHIP("Relationship")
}

data class SocialStreamComment(
    val id: String,
    val authorName: String,
    val authorRelation: String,
    val body: String,
    val isSupportive: Boolean = true
)

data class SocialStreamPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val topic: SocialPostTopic,
    val age: Int,
    val year: Int,
    val content: String,
    val likes: Int,
    val comments: List<SocialStreamComment> = emptyList()
)

enum class DailyEventCategory(val label: String) {
    MONEY_FIND("Money Find"),
    LOST_ITEM("Lost Item"),
    OLD_ACQUAINTANCE("Old Acquaintance"),
    DAILY_TWIST("Daily Twist")
}

data class DailyEventChoice(
    val id: String,
    val label: String,
    val effectHint: String,
    val outcomeSummary: String,
    val cashDelta: Int = 0,
    val happinessDelta: Int = 0,
    val healthDelta: Int = 0,
    val intellectDelta: Int = 0,
    val creativityDelta: Int = 0,
    val energyDelta: Int = 0,
    val fitnessDelta: Int = 0,
    val stressDelta: Int = 0,
    val anxietyDelta: Int = 0,
    val karmaDelta: Int = 0,
    val newAcquaintanceName: String = "",
    val acquaintanceRole: String = ""
)

data class DailyRandomEvent(
    val id: String,
    val dateKey: String,
    val trait: TemperamentTrait,
    val category: DailyEventCategory,
    val title: String,
    val narrative: String,
    val isPositiveTone: Boolean = true,
    val choices: List<DailyEventChoice> = emptyList()
)

enum class SkillType(val label: String, val tagline: String) {
    DEDUCTION_LAW("Deduction & Law", "Case logic, statutes & investigation"),
    ENGINEERING_TECH("Engineering & Logic", "Systems, math & technical design"),
    PAINTING_ART("Painting & Visual Art", "Fine art, drawing & composition"),
    MUSIC_PERFORMANCE("Music, Guitar & Vocals", "Singing, guitar, drums & theory"),
    ACTING_STAGE("Acting & Stagecraft", "Screen presence, voice & theatre"),
    ATHLETIC_DRIVING("Athletics & Racecraft", "Reflexes, endurance & track skill"),
    CULINARY_CRAFT("Culinary & Craft", "Kitchen technique, palate & plating"),
    PERSUASION_MEDIA("Persuasion & Rhetoric", "Debate, interviews & media poise")
}

data class CharacterSkills(
    val deductionLaw: Int = 18,
    val engineeringTech: Int = 18,
    val paintingArt: Int = 18,
    val musicPerformance: Int = 18,
    val actingStage: Int = 18,
    val athleticDriving: Int = 18,
    val culinaryCraft: Int = 18,
    val persuasionMedia: Int = 18
) {
    fun levelOf(type: SkillType): Int = when (type) {
        SkillType.DEDUCTION_LAW -> deductionLaw
        SkillType.ENGINEERING_TECH -> engineeringTech
        SkillType.PAINTING_ART -> paintingArt
        SkillType.MUSIC_PERFORMANCE -> musicPerformance
        SkillType.ACTING_STAGE -> actingStage
        SkillType.ATHLETIC_DRIVING -> athleticDriving
        SkillType.CULINARY_CRAFT -> culinaryCraft
        SkillType.PERSUASION_MEDIA -> persuasionMedia
    }

    fun withBoost(type: SkillType, delta: Int): CharacterSkills = when (type) {
        SkillType.DEDUCTION_LAW -> copy(deductionLaw = deductionLaw + delta)
        SkillType.ENGINEERING_TECH -> copy(engineeringTech = engineeringTech + delta)
        SkillType.PAINTING_ART -> copy(paintingArt = paintingArt + delta)
        SkillType.MUSIC_PERFORMANCE -> copy(musicPerformance = musicPerformance + delta)
        SkillType.ACTING_STAGE -> copy(actingStage = actingStage + delta)
        SkillType.ATHLETIC_DRIVING -> copy(athleticDriving = athleticDriving + delta)
        SkillType.CULINARY_CRAFT -> copy(culinaryCraft = culinaryCraft + delta)
        SkillType.PERSUASION_MEDIA -> copy(persuasionMedia = persuasionMedia + delta)
    }.clamped()

    fun clamped(): CharacterSkills = copy(
        deductionLaw = deductionLaw.coerceIn(0, 100),
        engineeringTech = engineeringTech.coerceIn(0, 100),
        paintingArt = paintingArt.coerceIn(0, 100),
        musicPerformance = musicPerformance.coerceIn(0, 100),
        actingStage = actingStage.coerceIn(0, 100),
        athleticDriving = athleticDriving.coerceIn(0, 100),
        culinaryCraft = culinaryCraft.coerceIn(0, 100),
        persuasionMedia = persuasionMedia.coerceIn(0, 100)
    )
}

enum class SkillActivity(
    val title: String,
    val skill: SkillType,
    val skillGain: Int,
    val cashCost: Int,
    val energyCost: Int,
    val summary: String
) {
    MOCK_TRIAL("Mock Trial & Logic Cases", SkillType.DEDUCTION_LAW, 9, 0, 8, "+9 Deduction & Law, +3 Intellect"),
    PAINT_STUDIO("Life Drawing & Canvas Studio", SkillType.PAINTING_ART, 9, 50, 8, "+9 Painting & Art, +3 Creativity"),
    BAND_REHEARSAL("Guitar, Drums & Vocal Jam", SkillType.MUSIC_PERFORMANCE, 9, 40, 8, "+9 Music & Vocals, +3 Creativity"),
    THEATRE_WORKSHOP("Acting & Stage Improv", SkillType.ACTING_STAGE, 9, 50, 8, "+9 Acting & Stage, +2 Persuasion"),
    SYSTEMS_LAB("Systems & Coding Workshop", SkillType.ENGINEERING_TECH, 9, 0, 8, "+9 Engineering & Logic, +3 Intellect"),
    TRACK_DRILLS("Track Karting & Reflex Drills", SkillType.ATHLETIC_DRIVING, 9, 80, 9, "+9 Athletics & Racecraft, +3 Fitness"),
    TEST_KITCHEN("Test Kitchen & Knife Prep", SkillType.CULINARY_CRAFT, 9, 45, 8, "+9 Culinary & Craft, +2 Creativity"),
    DEBATE_CLUB("Public Debate & Media Pitch", SkillType.PERSUASION_MEDIA, 9, 0, 7, "+9 Persuasion & Rhetoric, +2 Intellect")
}

enum class EducationType(val label: String, val annualCost: Int, val description: String) {
    NONE("No Degree", 0, "Open self-taught path"),
    UNIVERSITY("University", 4000, "Boosts Engineering, Logic & Intellect"),
    LAW_SCHOOL("Law School", 4500, "Auto-boosts Deduction, Intellect & Law"),
    ART_UNI("Art & Design Conservatory", 3600, "Auto-boosts Painting, Music, Acting & Creativity"),
    MUSIC_ACADEMY("Music Conservatory", 3600, "Auto-boosts Music, Guitar, Drums & Stagecraft"),
    TRADE_SCHOOL("Craft & Motorsport Institute", 2600, "Auto-boosts Racecraft & Culinary Mastery")
}

enum class DietPlan(
    val label: String,
    val annualCost: Int,
    val fitnessDelta: Int,
    val energyDelta: Int,
    val riskDelta: Int
) {
    WHOLE_FOOD("Whole-Food Balanced", 2400, 4, 4, -3),
    PLANT_FORWARD("Plant-Forward", 2100, 5, 3, -4),
    CONVENIENCE("Quick Takeout", 1400, -4, -3, 5),
    ARTISAN_GOURMET("Artisan Gourmet", 4200, 2, 5, -1)
}

enum class ExerciseRoutine(
    val label: String,
    val fitnessDelta: Int,
    val stressDelta: Int,
    val anxietyDelta: Int,
    val energyDelta: Int
) {
    YOGA_MINDFUL("Morning Yoga", 4, -8, -8, 4),
    RUNNING_CLUB("Running Club", 8, -6, -5, 2),
    STRENGTH("Strength Training", 7, -4, -3, 3),
    QUIET_WALKS("Evening Walks", 3, -5, -6, 3),
    SEDENTARY("Rest & Indoors", -5, 4, 4, -4)
}

enum class SleepSchedule(
    val label: String,
    val energyDelta: Int,
    val stressDelta: Int,
    val anxietyDelta: Int,
    val riskDelta: Int,
    val productivityBoost: Float
) {
    RESTORATIVE("8h Restorative", 8, -6, -6, -3, 1.0f),
    HUSTLE("6.5h Hustle", -2, 4, 3, 2, 1.15f),
    NIGHT_OWL("5h Late Grind", -9, 9, 8, 5, 1.25f)
}

enum class SocialNiche(val label: String, val tagline: String) {
    COZY_GAMING("Cozy Gaming", "Calm indie playthrus & lore"),
    DAILY_VLOGGING("Daily Vlogging", "Slow living & honest diaries"),
    CULINARY_ARTS("Culinary Arts", "Seasonal recipes & pantry craft"),
    TECH_MINIMALISM("Tech & Design", "Thoughtful tools & studio setups"),
    BOOKISH_ESSAYS("Bookish Essays", "Literary reflections & annotations"),
    INDIE_MUSIC("Indie Music", "Acoustic sessions & lo-fi beats")
}

enum class StreamFormat(
    val label: String,
    val baseSparkles: Int,
    val baseCash: Int,
    val followerMultiplier: Float,
    val energyCost: Int,
    val stressCost: Int
) {
    QA_LOUNGE("Q&A Tea Lounge", 45, 120, 1.0f, 8, 3),
    COZY_SESSION("Cozy Studio Session", 65, 180, 1.25f, 12, 5),
    VIRAL_CHALLENGE("Trend Challenge Live", 95, 280, 1.65f, 18, 11),
    CHARITY_MARATHON("Charity Marathon", 120, 90, 1.5f, 20, 9)
}

enum class EntryCategory(val label: String) {
    BIRTH("Beginning"),
    MILESTONE("Milestone"),
    CHOICE("Crossroads"),
    CAREER("Career"),
    FAME("Spotlight"),
    CRIME("Underworld"),
    SOCIAL("Creator"),
    RELATIONSHIP("Bonds"),
    WELLNESS("Wellness"),
    RANDOM_EVENT("Life Twist"),
    HISTORICAL("Era Event"),
    LEGACY("Legacy")
}

enum class CareerTrack(
    val title: String,
    val fieldLabel: String,
    val requiresDegree: Boolean,
    val isFameTrack: Boolean = false,
    val preferredEducation: EducationType = EducationType.NONE,
    val primarySkill: SkillType = SkillType.PERSUASION_MEDIA,
    val secondarySkill: SkillType = SkillType.ENGINEERING_TECH,
    val minPrimarySkill: Int = 20,
    val rungs: List<Pair<String, Int>>
) {
    UNEMPLOYED("Independent", "Open Chapter", false, false, EducationType.NONE, SkillType.PERSUASION_MEDIA, SkillType.ENGINEERING_TECH, 0, listOf("Exploring" to 0)),
    LAW(
        "Law & Advocacy",
        "Legal & Courts",
        true,
        false,
        EducationType.LAW_SCHOOL,
        SkillType.DEDUCTION_LAW,
        SkillType.PERSUASION_MEDIA,
        40,
        listOf(
            "Legal Paralegal" to 46000,
            "Associate Attorney" to 92000,
            "Trial Lawyer" to 168000,
            "Supreme Court Justice" to 285000
        )
    ),
    SOFTWARE(
        "Software & Systems",
        "Technology",
        true,
        false,
        EducationType.UNIVERSITY,
        SkillType.ENGINEERING_TECH,
        SkillType.DEDUCTION_LAW,
        35,
        listOf(
            "Junior Engineer" to 64000,
            "Product Engineer" to 96000,
            "Systems Architect" to 138000,
            "VP of Engineering" to 195000
        )
    ),
    EDITORIAL(
        "Editorial & Writing",
        "Publishing",
        false,
        false,
        EducationType.UNIVERSITY,
        SkillType.PERSUASION_MEDIA,
        SkillType.DEDUCTION_LAW,
        24,
        listOf(
            "Editorial Assistant" to 44000,
            "Staff Columnist" to 68000,
            "Senior Editor" to 98000,
            "Bestselling Novelist" to 155000
        )
    ),
    MEDICINE(
        "Medicine & Care",
        "Healthcare",
        true,
        false,
        EducationType.UNIVERSITY,
        SkillType.DEDUCTION_LAW,
        SkillType.ENGINEERING_TECH,
        42,
        listOf(
            "Clinical Intern" to 58000,
            "Resident Physician" to 82000,
            "Attending Physician" to 185000,
            "Chief of Medicine" to 255000
        )
    ),
    CULINARY(
        "Culinary & Hospitality",
        "Culinary",
        false,
        false,
        EducationType.TRADE_SCHOOL,
        SkillType.CULINARY_CRAFT,
        SkillType.PERSUASION_MEDIA,
        24,
        listOf(
            "Line Cook" to 38000,
            "Sous Chef" to 58000,
            "Executive Chef" to 94000,
            "Restaurateur" to 148000
        )
    ),
    DESIGN(
        "Design & Architecture",
        "Design",
        true,
        false,
        EducationType.ART_UNI,
        SkillType.PAINTING_ART,
        SkillType.ENGINEERING_TECH,
        34,
        listOf(
            "Junior Designer" to 54000,
            "Product Designer" to 86000,
            "Art Director" to 124000,
            "Principal Studio Lead" to 176000
        )
    ),
    FINE_ART(
        "Fine Artist & Painter",
        "Visual Arts",
        false,
        true,
        EducationType.ART_UNI,
        SkillType.PAINTING_ART,
        SkillType.PERSUASION_MEDIA,
        32,
        listOf(
            "Street Portrait Painter" to 28000,
            "Gallery Exhibitor" to 64000,
            "Acclaimed Painter" to 132000,
            "Biennale Master Artist" to 240000
        )
    ),
    ACTING(
        "Stage & Screen Actor",
        "Cinema & Theatre",
        false,
        true,
        EducationType.ART_UNI,
        SkillType.ACTING_STAGE,
        SkillType.PERSUASION_MEDIA,
        32,
        listOf(
            "Theatre Understudy" to 32000,
            "Indie Film Lead" to 78000,
            "Box Office Star" to 185000,
            "Festival Laureate Actor" to 320000
        )
    ),
    MODELING(
        "Editorial & Runway Model",
        "Fashion",
        false,
        true,
        EducationType.ART_UNI,
        SkillType.ACTING_STAGE,
        SkillType.PAINTING_ART,
        28,
        listOf(
            "Lookbook Model" to 34000,
            "Runway Walk" to 72000,
            "Global Campaign Face" to 165000,
            "Supermodel Icon" to 280000
        )
    ),
    MUSIC_SINGER(
        "Solo Vocalist & Singer",
        "Music",
        false,
        true,
        EducationType.MUSIC_ACADEMY,
        SkillType.MUSIC_PERFORMANCE,
        SkillType.ACTING_STAGE,
        30,
        listOf(
            "Acoustic Cafe Singer" to 26000,
            "Indie Recording Vocalist" to 68000,
            "Chart-Topping Singer" to 175000,
            "Global Arena Headliner" to 340000
        )
    ),
    BAND_GUITARIST(
        "Lead Guitarist (Band)",
        "Rock & Indie Band",
        false,
        true,
        EducationType.MUSIC_ACADEMY,
        SkillType.MUSIC_PERFORMANCE,
        SkillType.PERSUASION_MEDIA,
        30,
        listOf(
            "Garage Band Guitarist" to 24000,
            "Touring Rhythm Guitarist" to 62000,
            "Virtuoso Lead Guitarist" to 148000,
            "Hall of Fame Guitarist" to 275000
        )
    ),
    BAND_DRUMMER(
        "Band Drummer & Percussion",
        "Rock & Jazz Band",
        false,
        true,
        EducationType.MUSIC_ACADEMY,
        SkillType.MUSIC_PERFORMANCE,
        SkillType.ATHLETIC_DRIVING,
        30,
        listOf(
            "Session Club Drummer" to 25000,
            "Festival Tour Drummer" to 64000,
            "Legendary Band Drummer" to 152000,
            "Iconic Percussion Master" to 265000
        )
    ),
    RACE_DRIVER(
        "Pro Race Driver",
        "Motorsport",
        false,
        true,
        EducationType.TRADE_SCHOOL,
        SkillType.ATHLETIC_DRIVING,
        SkillType.ENGINEERING_TECH,
        34,
        listOf(
            "Karting Prospect" to 30000,
            "Formula Regional Driver" to 88000,
            "Grand Prix Contender" to 210000,
            "World Racing Champion" to 390000
        )
    ),
    CREATOR(
        "Full-Time Creator",
        "Social Media",
        false,
        true,
        EducationType.NONE,
        SkillType.PERSUASION_MEDIA,
        SkillType.ACTING_STAGE,
        18,
        listOf(
            "Hobbyist Streamer" to 18000,
            "Rising Creator" to 52000,
            "Partnered Influencer" to 112000,
            "Cultural Icon" to 220000
        )
    )
}

data class AvatarAppearance(
    val skinToneIndex: Int = 2,
    val hairStyleIndex: Int = 1,
    val hairColorIndex: Int = 0,
    val outfitIndex: Int = 1,
    val accessoryIndex: Int = 0,
    val backdropIndex: Int = 0
)

data class LifeStats(
    val health: Int = 88,
    val happiness: Int = 82,
    val intellect: Int = 65,
    val appearance: Int = 68,
    val creativity: Int = 72,
    val energy: Int = 85,
    val fitness: Int = 75,
    val diseaseRisk: Int = 10,
    val stress: Int = 15,
    val anxiety: Int = 14,
    val karma: Int = 60
) {
    fun clamped(): LifeStats = copy(
        health = health.coerceIn(0, 100),
        happiness = happiness.coerceIn(0, 100),
        intellect = intellect.coerceIn(0, 100),
        appearance = appearance.coerceIn(0, 100),
        creativity = creativity.coerceIn(0, 100),
        energy = energy.coerceIn(0, 100),
        fitness = fitness.coerceIn(0, 100),
        diseaseRisk = diseaseRisk.coerceIn(0, 100),
        stress = stress.coerceIn(0, 100),
        anxiety = anxiety.coerceIn(0, 100),
        karma = karma.coerceIn(0, 100)
    )
}

data class SocialChannel(
    val handle: String = "@journaler",
    val niche: SocialNiche = SocialNiche.DAILY_VLOGGING,
    val followers: Int = 0,
    val reputation: Int = 55,
    val totalStreams: Int = 0,
    val merchLaunched: Boolean = false,
    val merchName: String = "Linen Studio Tote",
    val sponsorshipTier: String = "None",
    val activeSponsorName: String = "",
    val annualSponsorIncome: Int = 0,
    val gearLevel: Int = 1,
    val currentTrendTitle: String = "Slow Morning Routine",
    val currentTrendNiche: SocialNiche = SocialNiche.DAILY_VLOGGING
)

enum class RelationshipPriority(val label: String, val summary: String) {
    STANDARD("Standard", "Normal annual bond tracking"),
    INTERACTION("Interact", "Prioritized for check-ins & protected from silent neglect"),
    GIFTING("Gift ($150)", "Prioritized for thoughtful gifting & annual bond boost")
}

data class Relationship(
    val id: String,
    val name: String,
    val role: String,
    val age: Int,
    val closeness: Int,
    val isPartner: Boolean = false,
    val isSpouse: Boolean = false,
    val isAlive: Boolean = true,
    val gender: CharacterGender = CharacterGender.FEMALE,
    val category: BondCategory = BondCategory.FRIEND,
    val interactedThisYear: Boolean = false,
    val yearsNeglected: Int = 0,
    val priority: RelationshipPriority = RelationshipPriority.STANDARD
) {
    val statusLabel: String
        get() = when {
            !isAlive -> "Remembered"
            closeness < 15 -> "Despises You"
            closeness < 25 -> "Hostile"
            isSpouse && closeness >= 80 -> "Devoted Spouse"
            isSpouse -> "Married"
            isPartner && closeness >= 80 -> "Committed Partner"
            isPartner -> "Dating"
            closeness >= 85 -> "Devoted"
            closeness >= 65 -> "Warm"
            closeness >= 40 -> "Cordial"
            else -> "Distant"
        }
}

data class JournalEntry(
    val id: String,
    val age: Int,
    val year: Int,
    val category: EntryCategory,
    val title: String,
    val detail: String
)

data class ChoiceOption(
    val id: String,
    val label: String,
    val effectHint: String,
    val outcomeSummary: String,
    val healthDelta: Int = 0,
    val happinessDelta: Int = 0,
    val intellectDelta: Int = 0,
    val creativityDelta: Int = 0,
    val energyDelta: Int = 0,
    val fitnessDelta: Int = 0,
    val stressDelta: Int = 0,
    val anxietyDelta: Int = 0,
    val diseaseRiskDelta: Int = 0,
    val karmaDelta: Int = 0,
    val cashDelta: Int = 0,
    val followersDelta: Int = 0,
    val sparklesDelta: Int = 0,
    val fameDelta: Int = 0,
    val careerBonus: Boolean = false,
    val enrollEducation: EducationType? = null,
    val relationshipBoostAll: Int = 0
)

data class LifeChoicePrompt(
    val id: String,
    val category: EntryCategory,
    val title: String,
    val narrative: String,
    val options: List<ChoiceOption>
)

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val sparkleReward: Int
)

data class AncestorRecord(
    val lifeId: String,
    val generation: Int,
    val characterName: String,
    val birthplace: String,
    val birthYear: Int,
    val deathYear: Int,
    val ageReached: Int,
    val careerTitle: String,
    val legacyScore: Int,
    val netWorth: Int,
    val peakFame: Int,
    val followers: Int,
    val spouseName: String = "",
    val childrenNames: List<String> = emptyList(),
    val highlights: List<String> = emptyList(),
    val appearance: AvatarAppearance = AvatarAppearance()
)

enum class MissionType(
    val defaultTitle: String,
    val description: String,
    val actionLabel: String,
    val xpReward: Int,
    val sparkleReward: Int,
    val cashReward: Int = 0
) {
    GO_TO_GYM(
        defaultTitle = "Go to the gym",
        description = "Complete a focused workout for fitness & vitality.",
        actionLabel = "Work Out",
        xpReward = 150,
        sparkleReward = 35
    ),
    POST_SOCIAL(
        defaultTitle = "Post a photo on social media",
        description = "Share a calm snapshot or moment with followers.",
        actionLabel = "Post Photo",
        xpReward = 160,
        sparkleReward = 40
    ),
    READ_BOOK(
        defaultTitle = "Read a book",
        description = "Spend quiet evening pages sharpening intellect.",
        actionLabel = "Read",
        xpReward = 140,
        sparkleReward = 30
    ),
    CONNECT_BOND(
        defaultTitle = "Call someone you love",
        description = "Check in with family, friends, or a partner.",
        actionLabel = "Check In",
        xpReward = 145,
        sparkleReward = 35
    ),
    WORK_SPRINT(
        defaultTitle = "Deep work session",
        description = "Put extra craft into work or studio output.",
        actionLabel = "Focus",
        xpReward = 170,
        sparkleReward = 45,
        cashReward = 350
    ),
    MINDFUL_CHECKIN(
        defaultTitle = "Mindful wellness break",
        description = "Take a therapy breath, checkup, or nature pause.",
        actionLabel = "Unwind",
        xpReward = 150,
        sparkleReward = 35
    ),
    GO_LIVE_STREAM(
        defaultTitle = "Host a mini livestream",
        description = "Go live briefly in the Creator Studio.",
        actionLabel = "Go Live",
        xpReward = 185,
        sparkleReward = 50,
        cashReward = 150
    ),
    AGE_MILESTONE(
        defaultTitle = "Turn a new page",
        description = "Advance your character's life by 1 year.",
        actionLabel = "Age +1",
        xpReward = 130,
        sparkleReward = 30
    )
}

data class DailyMission(
    val id: String,
    val type: MissionType,
    val title: String,
    val description: String,
    val xpReward: Int,
    val sparkleReward: Int,
    val cashReward: Int = 0,
    val isCompleted: Boolean = false
)

data class DailyMissionState(
    val dateKey: String = "",
    val missions: List<DailyMission> = emptyList(),
    val dailyBonusClaimed: Boolean = false,
    val totalXp: Int = 0
) {
    val level: Int get() = 1 + (totalXp / 500)
    val xpInLevel: Int get() = totalXp % 500
    val xpPerLevel: Int get() = 500
    val completedCount: Int get() = missions.count { it.isCompleted }
    val allCompleted: Boolean get() = missions.isNotEmpty() && completedCount == missions.size
}

object AchievementCatalog {
    val all: List<AchievementDefinition> = listOf(
        AchievementDefinition("first_breath", "First Chapter", "Begin a new life journal.", 50),
        AchievementDefinition("graduate", "Cap & Gown", "Complete higher education or conservatory.", 100),
        AchievementDefinition("career_peak", "Master of Craft", "Reach the top rung of a career track.", 200),
        AchievementDefinition("fame_icon", "Household Name", "Reach 80+ Fame in music, art, screen, or racing.", 220),
        AchievementDefinition("first_stream", "Going Live", "Host your very first creator live stream.", 75),
        AchievementDefinition("viral_creator", "Cultural Voice", "Reach 100,000 social followers.", 250),
        AchievementDefinition("merch_mogul", "Studio Brand", "Launch your own creator merchandise line.", 120),
        AchievementDefinition("zen_balance", "Inner Quiet", "Maintain 80+ Happiness with Stress under 20.", 150),
        AchievementDefinition("marathon_fit", "Vital Spirit", "Reach 90+ Fitness and 90+ Health.", 125),
        AchievementDefinition("golden_bond", "Devoted Heart", "Marry or reach 95+ closeness with a partner.", 150),
        AchievementDefinition("self_made", "Quiet Fortune", "Accumulate $250,000 in net worth.", 200),
        AchievementDefinition("dynasty_three", "Three Generations", "Carry a family lineage to Generation 3.", 250),
        AchievementDefinition("elder_wisdom", "Golden Chapter", "Reach age 80 with an enduring story.", 180),
        AchievementDefinition("centenarian", "Centenarian", "Live to celebrate your 100th birthday.", 300),
        AchievementDefinition("legacy_keeper", "Complete Memoir", "Finish a life and preserve its legacy.", 100)
    )
}

data class LifeState(
    val lifeId: String,
    val characterName: String,
    val birthplace: String,
    val birthYear: Int,
    val currentAge: Int = 0,
    val isAlive: Boolean = true,
    val pronouns: String = "they/them",
    val gender: CharacterGender = CharacterGender.NON_BINARY,
    val orientation: RomanceOrientation = RomanceOrientation.UNSET,
    val orientationPromptShown: Boolean = false,
    val trait: TemperamentTrait = TemperamentTrait.CURIOUS,
    val appearance: AvatarAppearance = AvatarAppearance(),
    val stats: LifeStats = LifeStats(),
    val skills: CharacterSkills = CharacterSkills(),
    val diet: DietPlan = DietPlan.WHOLE_FOOD,
    val exercise: ExerciseRoutine = ExerciseRoutine.QUIET_WALKS,
    val sleep: SleepSchedule = SleepSchedule.RESTORATIVE,
    val netWorth: Int = 500,
    val loanDebt: Int = 0,
    val hasDegree: Boolean = false,
    val inSchool: Boolean = false,
    val educationType: EducationType = EducationType.NONE,
    val completedEducation: EducationType = EducationType.NONE,
    val careerTrack: CareerTrack = CareerTrack.UNEMPLOYED,
    val careerRung: Int = 0,
    val careerPerformance: Int = 60,
    val fame: Int = 0,
    val peakFame: Int = 0,
    val crimeActive: Boolean = false,
    val crimeRank: Int = 0,
    val notoriety: Int = 0,
    val prisonYearsLeft: Int = 0,
    val generation: Int = 1,
    val parentName: String = "",
    val parentLifeId: String = "",
    val lineageHistory: List<AncestorRecord> = emptyList(),
    val missionState: DailyMissionState = DailyMissionState(),
    val socialChannel: SocialChannel = SocialChannel(),
    val relationships: List<Relationship> = emptyList(),
    val entries: List<JournalEntry> = emptyList(),
    val globalNewsFeed: List<GlobalNewsItem> = emptyList(),
    val moodHistory: List<MoodSnapshot> = emptyList(),
    val socialFeed: List<SocialStreamPost> = emptyList(),
    val lastDailyEventDateKey: String = "",
    val pendingDailyEvent: DailyRandomEvent? = null,
    val pendingPrompt: LifeChoicePrompt? = null,
    val unlockedAchievementIds: Set<String> = emptySet(),
    val epitaph: String = "",
    val deathCause: String = "",
    val economyMultiplier: Float = 1.0f,
    val endOfLifeAdClaimed: Boolean = false
) {
    val currentYear: Int get() = birthYear + currentAge
    val lifeStage: LifeStage get() = LifeStage.fromAge(currentAge)
    val hateCount: Int get() = relationships.count { it.isAlive && it.closeness < 25 }
    val isMiserableFromHatred: Boolean get() = hateCount >= 3
    val schoolWorkBonds: List<Relationship>
        get() = relationships.filter { it.isAlive && (it.category == BondCategory.SCHOOL || it.category == BondCategory.WORK) }
    val professionalStanding: Int
        get() = if (schoolWorkBonds.isEmpty()) 60 else schoolWorkBonds.sumOf { it.closeness } / schoolWorkBonds.size
    val activePartnerCount: Int
        get() = relationships.count { it.isAlive && (it.isPartner || it.isSpouse) }
    val childrenCount: Int
        get() = relationships.count { it.role == "Child" }

    val currentCareerTitle: String
        get() = when {
            prisonYearsLeft > 0 -> "Incarcerated (${prisonYearsLeft}y)"
            currentAge < 6 -> "Early Childhood"
            currentAge < 18 -> "Student"
            inSchool -> "${educationType.label} Student"
            crimeActive -> "Underworld Syndicate"
            else -> careerTrack.rungs.getOrElse(careerRung) { careerTrack.rungs.last() }.first
        }

    val currentSalary: Int
        get() = if (prisonYearsLeft > 0 || currentAge < 18 || inSchool || careerTrack == CareerTrack.UNEMPLOYED) {
            0
        } else {
            val base = careerTrack.rungs.getOrElse(careerRung) { careerTrack.rungs.last() }.second
            val fameBonus = if (careerTrack.isFameTrack) 1.0f + (fame * 0.008f) else 1.0f
            (base * economyMultiplier * fameBonus).toInt()
        }

    val legacyScore: Int
        get() {
            val equity = (netWorth - loanDebt).coerceAtLeast(0)
            val wealthScore = (equity / 1000).coerceAtMost(800)
            val socialScore = (socialChannel.followers / 500).coerceAtMost(800)
            val fameScore = peakFame * 4
            val bondScore = relationships.filter { it.isAlive }.sumOf { it.closeness } / 2
            val statScore = (stats.happiness + stats.intellect + stats.creativity + stats.karma + stats.health) * 2
            val ageScore = currentAge * 6
            val achievementScore = unlockedAchievementIds.size * 75
            return (wealthScore + socialScore + fameScore + bondScore + statScore + ageScore + achievementScore).coerceAtLeast(10)
        }
}

@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Chronicler",
    val leaderboardAlias: String = "QuietReader",
    val showOnLeaderboard: Boolean = true,
    val anonymousLeaderboard: Boolean = false,
    val autoCloudSync: Boolean = true,
    val showOptionalEndOfLifeAds: Boolean = true,
    val sparkles: Long = 200L,
    val totalLivesPlayed: Long = 0L,
    val unlockedAchievementIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LifeSaveDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val characterName: String = "",
    val birthplace: String = "",
    val birthYear: Long = 2000L,
    val currentAge: Long = 0L,
    val isAlive: Boolean = true,
    val legacyScore: Long = 0L,
    val netWorth: Long = 0L,
    val followers: Long = 0L,
    val careerTitle: String = "",
    val epitaph: String = "",
    val trait: String = TemperamentTrait.CURIOUS.name,
    val pronouns: String = "they/them",
    val stats: Map<String, Any> = emptyMap(),
    val appearance: Map<String, Any> = emptyMap(),
    val socialChannel: Map<String, Any> = emptyMap(),
    val relationships: List<Map<String, Any>> = emptyList(),
    val recentEntries: List<Map<String, Any>> = emptyList(),
    val achievedIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val leaderboardName: String = "",
    val characterName: String = "",
    val careerTitle: String = "",
    val legacyScore: Long = 0L,
    val netWorth: Long = 0L,
    val followers: Long = 0L,
    val ageReached: Long = 0L,
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

fun LifeState.toFirestorePayload(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "characterName" to characterName.take(80),
    "birthplace" to birthplace.take(80),
    "birthYear" to birthYear.toLong(),
    "currentAge" to currentAge.toLong(),
    "isAlive" to isAlive,
    "legacyScore" to legacyScore.toLong(),
    "netWorth" to netWorth.toLong(),
    "followers" to socialChannel.followers.toLong(),
    "careerTitle" to currentCareerTitle.take(80),
    "epitaph" to epitaph.take(300),
    "trait" to trait.name,
    "pronouns" to pronouns.take(30),
    "stats" to mapOf(
        "health" to stats.health.toLong(),
        "happiness" to stats.happiness.toLong(),
        "intellect" to stats.intellect.toLong(),
        "appearance" to stats.appearance.toLong(),
        "creativity" to stats.creativity.toLong(),
        "energy" to stats.energy.toLong(),
        "fitness" to stats.fitness.toLong(),
        "diseaseRisk" to stats.diseaseRisk.toLong(),
        "stress" to stats.stress.toLong(),
        "anxiety" to stats.anxiety.toLong(),
        "karma" to stats.karma.toLong(),
        "loanDebt" to loanDebt.toLong(),
        "skillDeductionLaw" to skills.deductionLaw.toLong(),
        "skillEngineeringTech" to skills.engineeringTech.toLong(),
        "skillPaintingArt" to skills.paintingArt.toLong(),
        "skillMusicPerformance" to skills.musicPerformance.toLong(),
        "skillActingStage" to skills.actingStage.toLong(),
        "skillAthleticDriving" to skills.athleticDriving.toLong(),
        "skillCulinaryCraft" to skills.culinaryCraft.toLong(),
        "skillPersuasionMedia" to skills.persuasionMedia.toLong(),
        "diet" to diet.name,
        "exercise" to exercise.name,
        "sleep" to sleep.name,
        "careerTrack" to careerTrack.name,
        "careerRung" to careerRung.toLong(),
        "careerPerformance" to careerPerformance.toLong(),
        "hasDegree" to hasDegree,
        "inSchool" to inSchool,
        "educationType" to educationType.name,
        "completedEducation" to completedEducation.name,
        "fame" to fame.toLong(),
        "peakFame" to peakFame.toLong(),
        "crimeActive" to crimeActive,
        "crimeRank" to crimeRank.toLong(),
        "notoriety" to notoriety.toLong(),
        "prisonYearsLeft" to prisonYearsLeft.toLong(),
        "generation" to generation.toLong(),
        "parentName" to parentName.take(80),
        "parentLifeId" to parentLifeId.take(120),
        "lineageHistory" to lineageHistory.take(25).map { anc ->
            mapOf(
                "lifeId" to anc.lifeId,
                "generation" to anc.generation.toLong(),
                "characterName" to anc.characterName.take(80),
                "birthplace" to anc.birthplace.take(80),
                "birthYear" to anc.birthYear.toLong(),
                "deathYear" to anc.deathYear.toLong(),
                "ageReached" to anc.ageReached.toLong(),
                "careerTitle" to anc.careerTitle.take(80),
                "legacyScore" to anc.legacyScore.toLong(),
                "netWorth" to anc.netWorth.toLong(),
                "peakFame" to anc.peakFame.toLong(),
                "followers" to anc.followers.toLong(),
                "spouseName" to anc.spouseName.take(80),
                "childrenNames" to anc.childrenNames.take(10),
                "highlights" to anc.highlights.take(6),
                "skinToneIndex" to anc.appearance.skinToneIndex.toLong(),
                "hairStyleIndex" to anc.appearance.hairStyleIndex.toLong(),
                "hairColorIndex" to anc.appearance.hairColorIndex.toLong(),
                "outfitIndex" to anc.appearance.outfitIndex.toLong(),
                "accessoryIndex" to anc.appearance.accessoryIndex.toLong(),
                "backdropIndex" to anc.appearance.backdropIndex.toLong()
            )
        },
        "gender" to gender.name,
        "orientation" to orientation.name,
        "orientationPromptShown" to orientationPromptShown,
        "globalNewsFeed" to globalNewsFeed.take(25).map { news ->
            mapOf(
                "id" to news.id,
                "year" to news.year.toLong(),
                "region" to news.region.take(50),
                "category" to news.category.name,
                "headline" to news.headline.take(100),
                "summary" to news.summary.take(220),
                "economyDelta" to news.economyDelta.toDouble()
            )
        },
        "moodHistory" to moodHistory.takeLast(10).map { snap ->
            mapOf(
                "age" to snap.age.toLong(),
                "year" to snap.year.toLong(),
                "happiness" to snap.happiness.toLong(),
                "stress" to snap.stress.toLong(),
                "anxiety" to snap.anxiety.toLong(),
                "careerStressLoad" to snap.careerStressLoad.toLong(),
                "hadRandomEncounter" to snap.hadRandomEncounter,
                "encounterLabel" to snap.encounterLabel.take(80)
            )
        },
        "socialFeed" to socialFeed.take(20).map { post ->
            mapOf(
                "id" to post.id,
                "authorName" to post.authorName.take(60),
                "authorHandle" to post.authorHandle.take(40),
                "topic" to post.topic.name,
                "age" to post.age.toLong(),
                "year" to post.year.toLong(),
                "content" to post.content.take(220),
                "likes" to post.likes.toLong(),
                "comments" to post.comments.take(5).map { c ->
                    mapOf(
                        "id" to c.id,
                        "authorName" to c.authorName.take(60),
                        "authorRelation" to c.authorRelation.take(40),
                        "body" to c.body.take(180),
                        "isSupportive" to c.isSupportive
                    )
                }
            )
        },
        "missionDateKey" to missionState.dateKey.take(20),
        "missionTotalXp" to missionState.totalXp.toLong(),
        "missionBonusClaimed" to missionState.dailyBonusClaimed,
        "lastDailyEventDateKey" to lastDailyEventDateKey.take(20),
        "pendingDailyEvent" to (pendingDailyEvent?.let { ev ->
            mapOf(
                "id" to ev.id,
                "dateKey" to ev.dateKey,
                "trait" to ev.trait.name,
                "category" to ev.category.name,
                "title" to ev.title.take(100),
                "narrative" to ev.narrative.take(260),
                "isPositiveTone" to ev.isPositiveTone,
                "choices" to ev.choices.take(3).map { ch ->
                    mapOf(
                        "id" to ch.id,
                        "label" to ch.label.take(60),
                        "effectHint" to ch.effectHint.take(90),
                        "outcomeSummary" to ch.outcomeSummary.take(200),
                        "cashDelta" to ch.cashDelta.toLong(),
                        "happinessDelta" to ch.happinessDelta.toLong(),
                        "healthDelta" to ch.healthDelta.toLong(),
                        "intellectDelta" to ch.intellectDelta.toLong(),
                        "creativityDelta" to ch.creativityDelta.toLong(),
                        "energyDelta" to ch.energyDelta.toLong(),
                        "fitnessDelta" to ch.fitnessDelta.toLong(),
                        "stressDelta" to ch.stressDelta.toLong(),
                        "anxietyDelta" to ch.anxietyDelta.toLong(),
                        "karmaDelta" to ch.karmaDelta.toLong(),
                        "newAcquaintanceName" to ch.newAcquaintanceName.take(60),
                        "acquaintanceRole" to ch.acquaintanceRole.take(40)
                    )
                }
            )
        } ?: emptyMap<String, Any>()),
        "dailyMissions" to missionState.missions.take(5).map { m ->
            mapOf(
                "id" to m.id,
                "type" to m.type.name,
                "title" to m.title.take(80),
                "description" to m.description.take(160),
                "xpReward" to m.xpReward.toLong(),
                "sparkleReward" to m.sparkleReward.toLong(),
                "cashReward" to m.cashReward.toLong(),
                "isCompleted" to m.isCompleted
            )
        }
    ),
    "appearance" to mapOf(
        "skinToneIndex" to appearance.skinToneIndex.toLong(),
        "hairStyleIndex" to appearance.hairStyleIndex.toLong(),
        "hairColorIndex" to appearance.hairColorIndex.toLong(),
        "outfitIndex" to appearance.outfitIndex.toLong(),
        "accessoryIndex" to appearance.accessoryIndex.toLong(),
        "backdropIndex" to appearance.backdropIndex.toLong()
    ),
    "socialChannel" to mapOf(
        "handle" to socialChannel.handle.take(40),
        "niche" to socialChannel.niche.name,
        "reputation" to socialChannel.reputation.toLong(),
        "totalStreams" to socialChannel.totalStreams.toLong(),
        "merchLaunched" to socialChannel.merchLaunched,
        "merchName" to socialChannel.merchName.take(50),
        "sponsorshipTier" to socialChannel.sponsorshipTier.take(30),
        "activeSponsorName" to socialChannel.activeSponsorName.take(60),
        "annualSponsorIncome" to socialChannel.annualSponsorIncome.toLong(),
        "gearLevel" to socialChannel.gearLevel.toLong(),
        "currentTrendTitle" to socialChannel.currentTrendTitle.take(60),
        "currentTrendNiche" to socialChannel.currentTrendNiche.name
    ),
    "relationships" to relationships.take(40).map { rel ->
        mapOf(
            "id" to rel.id,
            "name" to rel.name,
            "role" to rel.role,
            "age" to rel.age.toLong(),
            "closeness" to rel.closeness.toLong(),
            "isPartner" to rel.isPartner,
            "isSpouse" to rel.isSpouse,
            "isAlive" to rel.isAlive,
            "gender" to rel.gender.name,
            "category" to rel.category.name,
            "interactedThisYear" to rel.interactedThisYear,
            "yearsNeglected" to rel.yearsNeglected.toLong(),
            "priority" to rel.priority.name
        )
    },
    "recentEntries" to entries.take(150).map { entry ->
        mapOf(
            "id" to entry.id,
            "age" to entry.age.toLong(),
            "year" to entry.year.toLong(),
            "category" to entry.category.name,
            "title" to entry.title.take(80),
            "detail" to entry.detail.take(260)
        )
    },
    "achievedIds" to unlockedAchievementIds.take(50)
)

fun LifeSaveDoc.toDomain(): LifeState {
    val statsMap = stats
    val appMap = appearance
    val chMap = socialChannel

    fun Map<String, Any>.intVal(key: String, default: Int): Int =
        (this[key] as? Number)?.toInt() ?: default

    fun Map<String, Any>.strVal(key: String, default: String): String =
        (this[key] as? String) ?: default

    fun Map<String, Any>.boolVal(key: String, default: Boolean): Boolean =
        (this[key] as? Boolean) ?: default

    val parsedTrait = runCatching { TemperamentTrait.valueOf(trait) }.getOrDefault(TemperamentTrait.CURIOUS)
    val parsedDiet = runCatching { DietPlan.valueOf(statsMap.strVal("diet", DietPlan.WHOLE_FOOD.name)) }.getOrDefault(DietPlan.WHOLE_FOOD)
    val parsedEx = runCatching { ExerciseRoutine.valueOf(statsMap.strVal("exercise", ExerciseRoutine.QUIET_WALKS.name)) }.getOrDefault(ExerciseRoutine.QUIET_WALKS)
    val parsedSleep = runCatching { SleepSchedule.valueOf(statsMap.strVal("sleep", SleepSchedule.RESTORATIVE.name)) }.getOrDefault(SleepSchedule.RESTORATIVE)
    val parsedTrack = runCatching { CareerTrack.valueOf(statsMap.strVal("careerTrack", CareerTrack.UNEMPLOYED.name)) }.getOrDefault(CareerTrack.UNEMPLOYED)
    val parsedEdu = runCatching { EducationType.valueOf(statsMap.strVal("educationType", EducationType.NONE.name)) }.getOrDefault(EducationType.NONE)
    val parsedCompEdu = runCatching { EducationType.valueOf(statsMap.strVal("completedEducation", EducationType.NONE.name)) }.getOrDefault(EducationType.NONE)
    val parsedNiche = runCatching { SocialNiche.valueOf(chMap.strVal("niche", SocialNiche.DAILY_VLOGGING.name)) }.getOrDefault(SocialNiche.DAILY_VLOGGING)
    val parsedTrendNiche = runCatching { SocialNiche.valueOf(chMap.strVal("currentTrendNiche", SocialNiche.DAILY_VLOGGING.name)) }.getOrDefault(SocialNiche.DAILY_VLOGGING)

    val parsedRels = relationships.mapIndexed { idx, map ->
        val parsedGender = runCatching {
            CharacterGender.valueOf(map.strVal("gender", CharacterGender.FEMALE.name))
        }.getOrDefault(CharacterGender.FEMALE)
        val parsedCat = runCatching {
            BondCategory.valueOf(map.strVal("category", BondCategory.FRIEND.name))
        }.getOrDefault(BondCategory.FRIEND)
        val parsedPriority = runCatching {
            RelationshipPriority.valueOf(map.strVal("priority", RelationshipPriority.STANDARD.name))
        }.getOrDefault(RelationshipPriority.STANDARD)
        Relationship(
            id = map.strVal("id", "rel_$idx"),
            name = map.strVal("name", "Companion"),
            role = map.strVal("role", "Friend"),
            age = map.intVal("age", 20),
            closeness = map.intVal("closeness", 60),
            isPartner = map.boolVal("isPartner", false),
            isSpouse = map.boolVal("isSpouse", false),
            isAlive = map.boolVal("isAlive", true),
            gender = parsedGender,
            category = parsedCat,
            interactedThisYear = map.boolVal("interactedThisYear", false),
            yearsNeglected = map.intVal("yearsNeglected", 0),
            priority = parsedPriority
        )
    }

    val parsedEntries = recentEntries.mapIndexed { idx, map ->
        val cat = runCatching { EntryCategory.valueOf(map.strVal("category", EntryCategory.MILESTONE.name)) }
            .getOrDefault(EntryCategory.MILESTONE)
        JournalEntry(
            id = map.strVal("id", "entry_$idx"),
            age = map.intVal("age", 0),
            year = map.intVal("year", birthYear.toInt()),
            category = cat,
            title = map.strVal("title", "Journal Note"),
            detail = map.strVal("detail", "")
        )
    }

    val parsedNewsFeed = ((statsMap["globalNewsFeed"] as? List<*>) ?: emptyList<Any>()).mapNotNull { raw ->
        @Suppress("UNCHECKED_CAST")
        val typed = raw as? Map<String, Any> ?: return@mapNotNull null
        val newsCat = runCatching {
            NewsCategory.valueOf(typed.strVal("category", NewsCategory.CULTURAL_SHIFT.name))
        }.getOrDefault(NewsCategory.CULTURAL_SHIFT)
        GlobalNewsItem(
            id = typed.strVal("id", "news_${typed.intVal("year", birthYear.toInt())}"),
            year = typed.intVal("year", birthYear.toInt()),
            region = typed.strVal("region", "Global"),
            category = newsCat,
            headline = typed.strVal("headline", "Global Dispatch"),
            summary = typed.strVal("summary", ""),
            economyDelta = ((typed["economyDelta"] as? Number)?.toFloat()) ?: 0f
        )
    }

    val parsedMoodHistory = ((statsMap["moodHistory"] as? List<*>) ?: emptyList<Any>()).mapNotNull { raw ->
        @Suppress("UNCHECKED_CAST")
        val typed = raw as? Map<String, Any> ?: return@mapNotNull null
        MoodSnapshot(
            age = typed.intVal("age", 0),
            year = typed.intVal("year", birthYear.toInt()),
            happiness = typed.intVal("happiness", 80),
            stress = typed.intVal("stress", 15),
            anxiety = typed.intVal("anxiety", 10),
            careerStressLoad = typed.intVal("careerStressLoad", 0),
            hadRandomEncounter = typed.boolVal("hadRandomEncounter", false),
            encounterLabel = typed.strVal("encounterLabel", "")
        )
    }

    val parsedSocialFeed = ((statsMap["socialFeed"] as? List<*>) ?: emptyList<Any>()).mapNotNull { raw ->
        @Suppress("UNCHECKED_CAST")
        val typed = raw as? Map<String, Any> ?: return@mapNotNull null
        val topic = runCatching {
            SocialPostTopic.valueOf(typed.strVal("topic", SocialPostTopic.LIFE.name))
        }.getOrDefault(SocialPostTopic.LIFE)
        val comments = ((typed["comments"] as? List<*>) ?: emptyList<Any>()).mapIndexedNotNull { cIdx, cRaw ->
            @Suppress("UNCHECKED_CAST")
            val cMap = cRaw as? Map<String, Any> ?: return@mapIndexedNotNull null
            SocialStreamComment(
                id = cMap.strVal("id", "sc_$cIdx"),
                authorName = cMap.strVal("authorName", "Friend"),
                authorRelation = cMap.strVal("authorRelation", "Follower"),
                body = cMap.strVal("body", ""),
                isSupportive = cMap.boolVal("isSupportive", true)
            )
        }
        SocialStreamPost(
            id = typed.strVal("id", "post_${typed.intVal("age", 0)}"),
            authorName = typed.strVal("authorName", characterName),
            authorHandle = typed.strVal("authorHandle", "@journaler"),
            topic = topic,
            age = typed.intVal("age", 0),
            year = typed.intVal("year", birthYear.toInt()),
            content = typed.strVal("content", ""),
            likes = typed.intVal("likes", 0),
            comments = comments
        )
    }

    @Suppress("UNCHECKED_CAST")
    val pendingDailyEventMap = (statsMap["pendingDailyEvent"] as? Map<String, Any>)?.takeIf { it.isNotEmpty() }
    val parsedPendingDailyEvent = pendingDailyEventMap?.let { evMap ->
        val evTrait = runCatching {
            TemperamentTrait.valueOf(evMap.strVal("trait", parsedTrait.name))
        }.getOrDefault(parsedTrait)
        val evCat = runCatching {
            DailyEventCategory.valueOf(evMap.strVal("category", DailyEventCategory.DAILY_TWIST.name))
        }.getOrDefault(DailyEventCategory.DAILY_TWIST)
        val choices = ((evMap["choices"] as? List<*>) ?: emptyList<Any>()).mapIndexedNotNull { cIdx, cRaw ->
            @Suppress("UNCHECKED_CAST")
            val cm = cRaw as? Map<String, Any> ?: return@mapIndexedNotNull null
            DailyEventChoice(
                id = cm.strVal("id", "dec_$cIdx"),
                label = cm.strVal("label", "Respond"),
                effectHint = cm.strVal("effectHint", ""),
                outcomeSummary = cm.strVal("outcomeSummary", ""),
                cashDelta = cm.intVal("cashDelta", 0),
                happinessDelta = cm.intVal("happinessDelta", 0),
                healthDelta = cm.intVal("healthDelta", 0),
                intellectDelta = cm.intVal("intellectDelta", 0),
                creativityDelta = cm.intVal("creativityDelta", 0),
                energyDelta = cm.intVal("energyDelta", 0),
                fitnessDelta = cm.intVal("fitnessDelta", 0),
                stressDelta = cm.intVal("stressDelta", 0),
                anxietyDelta = cm.intVal("anxietyDelta", 0),
                karmaDelta = cm.intVal("karmaDelta", 0),
                newAcquaintanceName = cm.strVal("newAcquaintanceName", ""),
                acquaintanceRole = cm.strVal("acquaintanceRole", "")
            )
        }
        DailyRandomEvent(
            id = evMap.strVal("id", "daily_ev"),
            dateKey = evMap.strVal("dateKey", ""),
            trait = evTrait,
            category = evCat,
            title = evMap.strVal("title", "Daily Moment"),
            narrative = evMap.strVal("narrative", ""),
            isPositiveTone = evMap.boolVal("isPositiveTone", true),
            choices = choices
        )
    }

    val parsedGender = runCatching {
        CharacterGender.valueOf(statsMap.strVal("gender", CharacterGender.fromPronouns(pronouns).name))
    }.getOrDefault(CharacterGender.fromPronouns(pronouns))

    val parsedOrientation = runCatching {
        RomanceOrientation.valueOf(statsMap.strVal("orientation", RomanceOrientation.UNSET.name))
    }.getOrDefault(RomanceOrientation.UNSET)

    return LifeState(
        lifeId = id,
        characterName = characterName,
        birthplace = birthplace,
        birthYear = birthYear.toInt(),
        currentAge = currentAge.toInt(),
        isAlive = isAlive,
        pronouns = pronouns,
        gender = parsedGender,
        orientation = parsedOrientation,
        orientationPromptShown = statsMap.boolVal("orientationPromptShown", parsedOrientation != RomanceOrientation.UNSET),
        trait = parsedTrait,
        appearance = AvatarAppearance(
            skinToneIndex = appMap.intVal("skinToneIndex", 2),
            hairStyleIndex = appMap.intVal("hairStyleIndex", 1),
            hairColorIndex = appMap.intVal("hairColorIndex", 0),
            outfitIndex = appMap.intVal("outfitIndex", 1),
            accessoryIndex = appMap.intVal("accessoryIndex", 0),
            backdropIndex = appMap.intVal("backdropIndex", 0)
        ),
        stats = LifeStats(
            health = statsMap.intVal("health", 85),
            happiness = statsMap.intVal("happiness", 80),
            intellect = statsMap.intVal("intellect", 65),
            appearance = statsMap.intVal("appearance", 68),
            creativity = statsMap.intVal("creativity", 70),
            energy = statsMap.intVal("energy", 80),
            fitness = statsMap.intVal("fitness", 75),
            diseaseRisk = statsMap.intVal("diseaseRisk", 10),
            stress = statsMap.intVal("stress", 15),
            anxiety = statsMap.intVal("anxiety", 15),
            karma = statsMap.intVal("karma", 60)
        ),
        diet = parsedDiet,
        exercise = parsedEx,
        sleep = parsedSleep,
        skills = CharacterSkills(
            deductionLaw = statsMap.intVal("skillDeductionLaw", 18),
            engineeringTech = statsMap.intVal("skillEngineeringTech", 18),
            paintingArt = statsMap.intVal("skillPaintingArt", 18),
            musicPerformance = statsMap.intVal("skillMusicPerformance", 18),
            actingStage = statsMap.intVal("skillActingStage", 18),
            athleticDriving = statsMap.intVal("skillAthleticDriving", 18),
            culinaryCraft = statsMap.intVal("skillCulinaryCraft", 18),
            persuasionMedia = statsMap.intVal("skillPersuasionMedia", 18)
        ),
        netWorth = netWorth.toInt().coerceAtLeast(0),
        loanDebt = statsMap.intVal("loanDebt", 0).coerceAtLeast(0),
        hasDegree = statsMap.boolVal("hasDegree", false),
        inSchool = statsMap.boolVal("inSchool", false),
        educationType = parsedEdu,
        completedEducation = parsedCompEdu,
        careerTrack = parsedTrack,
        careerRung = statsMap.intVal("careerRung", 0),
        careerPerformance = statsMap.intVal("careerPerformance", 60),
        fame = statsMap.intVal("fame", 0),
        peakFame = statsMap.intVal("peakFame", 0),
        crimeActive = statsMap.boolVal("crimeActive", false),
        crimeRank = statsMap.intVal("crimeRank", 0),
        notoriety = statsMap.intVal("notoriety", 0),
        prisonYearsLeft = statsMap.intVal("prisonYearsLeft", 0),
        generation = statsMap.intVal("generation", 1),
        parentName = statsMap.strVal("parentName", ""),
        parentLifeId = statsMap.strVal("parentLifeId", ""),
        lineageHistory = ((statsMap["lineageHistory"] as? List<*>) ?: emptyList<Any>()).mapNotNull { raw ->
            val m = raw as? Map<*, *> ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            val typed = m as? Map<String, Any> ?: return@mapNotNull null
            AncestorRecord(
                lifeId = typed.strVal("lifeId", ""),
                generation = typed.intVal("generation", 1),
                characterName = typed.strVal("characterName", "Ancestor"),
                birthplace = typed.strVal("birthplace", birthplace),
                birthYear = typed.intVal("birthYear", 1970),
                deathYear = typed.intVal("deathYear", 2040),
                ageReached = typed.intVal("ageReached", 70),
                careerTitle = typed.strVal("careerTitle", "Chronicler"),
                legacyScore = typed.intVal("legacyScore", 100),
                netWorth = typed.intVal("netWorth", 0),
                peakFame = typed.intVal("peakFame", 0),
                followers = typed.intVal("followers", 0),
                spouseName = typed.strVal("spouseName", ""),
                childrenNames = (typed["childrenNames"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                highlights = (typed["highlights"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                appearance = AvatarAppearance(
                    skinToneIndex = typed.intVal("skinToneIndex", 2),
                    hairStyleIndex = typed.intVal("hairStyleIndex", 1),
                    hairColorIndex = typed.intVal("hairColorIndex", 0),
                    outfitIndex = typed.intVal("outfitIndex", 1),
                    accessoryIndex = typed.intVal("accessoryIndex", 0),
                    backdropIndex = typed.intVal("backdropIndex", 0)
                )
            )
        },
        missionState = DailyMissionState(
            dateKey = statsMap.strVal("missionDateKey", ""),
            totalXp = statsMap.intVal("missionTotalXp", 0),
            dailyBonusClaimed = statsMap.boolVal("missionBonusClaimed", false),
            missions = ((statsMap["dailyMissions"] as? List<*>) ?: emptyList<Any>()).mapNotNull { raw ->
                @Suppress("UNCHECKED_CAST")
                val typed = raw as? Map<String, Any> ?: return@mapNotNull null
                val mType = runCatching { MissionType.valueOf(typed.strVal("type", MissionType.READ_BOOK.name)) }
                    .getOrDefault(MissionType.READ_BOOK)
                DailyMission(
                    id = typed.strVal("id", mType.name),
                    type = mType,
                    title = typed.strVal("title", mType.defaultTitle),
                    description = typed.strVal("description", mType.description),
                    xpReward = typed.intVal("xpReward", mType.xpReward),
                    sparkleReward = typed.intVal("sparkleReward", mType.sparkleReward),
                    cashReward = typed.intVal("cashReward", mType.cashReward),
                    isCompleted = typed.boolVal("isCompleted", false)
                )
            }
        ),
        socialChannel = SocialChannel(
            handle = chMap.strVal("handle", "@journaler"),
            niche = parsedNiche,
            followers = followers.toInt(),
            reputation = chMap.intVal("reputation", 55),
            totalStreams = chMap.intVal("totalStreams", 0),
            merchLaunched = chMap.boolVal("merchLaunched", false),
            merchName = chMap.strVal("merchName", "Linen Studio Tote"),
            sponsorshipTier = chMap.strVal("sponsorshipTier", "None"),
            activeSponsorName = chMap.strVal("activeSponsorName", ""),
            annualSponsorIncome = chMap.intVal("annualSponsorIncome", 0),
            gearLevel = chMap.intVal("gearLevel", 1),
            currentTrendTitle = chMap.strVal("currentTrendTitle", "Slow Morning Routine"),
            currentTrendNiche = parsedTrendNiche
        ),
        relationships = parsedRels,
        entries = parsedEntries,
        globalNewsFeed = parsedNewsFeed,
        moodHistory = parsedMoodHistory,
        socialFeed = parsedSocialFeed,
        lastDailyEventDateKey = statsMap.strVal("lastDailyEventDateKey", ""),
        pendingDailyEvent = parsedPendingDailyEvent,
        unlockedAchievementIds = achievedIds.toSet(),
        epitaph = epitaph
    )
}
