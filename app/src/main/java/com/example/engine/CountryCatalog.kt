package com.example.engine

import com.example.model.ChoiceOption
import com.example.model.EntryCategory
import com.example.model.LifeChoicePrompt

data class CrimeSyndicateProfile(
    val organizationName: String,
    val localTerm: String,
    val ranks: List<String>,
    val hustleTitles: List<Pair<String, String>>
)

data class CountryProfile(
    val countryName: String,
    val cities: List<String>,
    val firstNames: List<String>,
    val lastNames: List<String>,
    val crimeSyndicate: CrimeSyndicateProfile,
    val localIssues: List<LocalIssueEvent>
)

data class LocalIssueEvent(
    val id: String,
    val title: String,
    val narrative: String,
    val options: List<ChoiceOption>
)

object CountryCatalog {

    val countries: List<CountryProfile> = listOf(
        CountryProfile(
            countryName = "Indonesia",
            cities = listOf(
                "Jakarta, Indonesia",
                "Yogyakarta, Indonesia",
                "Bandung, Indonesia",
                "Surabaya, Indonesia",
                "Denpasar, Indonesia",
                "Medan, Indonesia",
                "Makassar, Indonesia"
            ),
            firstNames = listOf(
                "Aditya", "Sekar", "Bagas", "Nadia", "Raka", "Larasati",
                "Dimas", "Kartika", "Budi", "Ayu", "Fajar", "Putri",
                "Bayu", "Citra", "Ilham", "Dewi"
            ),
            lastNames = listOf(
                "Pratama", "Kusuma", "Wijaya", "Santoso", "Lestari", "Nugroho",
                "Rahayu", "Saputra", "Hidayat", "Mahendra", "Puspita", "Siregar",
                "Hartono", "Utami", "Permana", "Wibowo"
            ),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Kelompok Preman Pelabuhan",
                localTerm = "Preman",
                ranks = listOf("Anak Buah", "Jagoan Wilayah", "Koordinator Lapangan", "Bos Preman"),
                hustleTitles = listOf(
                    "Night Market Protection Fee" to "Collected unofficial parking & stall fees around the night bazaar.",
                    "Back-Alley Illegal Import Cut" to "Moved untaxed electronics crates past dockside warehouses.",
                    "Underground Debt Settlement" to "Pressured an overdue syndicate borrower into paying up."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "id_macet_krl",
                    title = "Jakarta-Bandung Commute & Macet",
                    narrative = "Heavy traffic and crowded commuter lines are draining hours from your week while living costs rise.",
                    options = listOf(
                        ChoiceOption(
                            id = "id_opt_kos",
                            label = "Move to a Cozy Kos Near Transit",
                            effectHint = "-$1,800, +12 Energy, -14 Stress",
                            outcomeSummary = "Renting a clean transit-side kos reclaimed two hours every morning for rest and side projects.",
                            cashDelta = -1800,
                            energyDelta = 12,
                            stressDelta = -14,
                            anxietyDelta = -8
                        ),
                        ChoiceOption(
                            id = "id_opt_remote",
                            label = "Negotiate Hybrid WFA Schedule",
                            effectHint = "+Career, +Creativity, +500 Followers",
                            outcomeSummary = "Shifted to hybrid remote days, filming calm coffee-shop work vlogs along the way.",
                            careerBonus = true,
                            creativityDelta = 6,
                            followersDelta = 500,
                            stressDelta = -6
                        )
                    )
                ),
                LocalIssueEvent(
                    id = "id_umkm_kopi",
                    title = "Local UMKM & Artisan Coffee Wave",
                    narrative = "Independent neighborhood coffee shops and local creator collectives are booming across your city.",
                    options = listOf(
                        ChoiceOption(
                            id = "id_opt_invest_umkm",
                            label = "Co-Fund a Neighborhood Warung Kopi",
                            effectHint = "-$2,500 now, +$7,200 return, +Karma",
                            outcomeSummary = "Backed a local specialty roastery with friends; weekend acoustic nights drew a loyal crowd.",
                            cashDelta = 4700,
                            karmaDelta = 8,
                            happinessDelta = 10,
                            relationshipBoostAll = 4
                        ),
                        ChoiceOption(
                            id = "id_opt_collab_creator",
                            label = "Host a Local Creator Pop-Up Stream",
                            effectHint = "+1,400 Followers, +120 ✦, +Creativity",
                            outcomeSummary = "Spotlighted local weavers and baristas on stream, winning genuine community love.",
                            followersDelta = 1400,
                            sparklesDelta = 120,
                            creativityDelta = 8,
                            karmaDelta = 6
                        )
                    )
                ),
                LocalIssueEvent(
                    id = "id_mudik_bpjs",
                    title = "Mudik Season & Family Care",
                    narrative = "Annual homecoming travel prices spiked just as an older relative needs routine preventive checkups.",
                    options = listOf(
                        ChoiceOption(
                            id = "id_opt_mudik_family",
                            label = "Organize Mudik & Family Checkups",
                            effectHint = "-$950, +16 Family Bonds, +Karma",
                            outcomeSummary = "Booked early train tickets, brought home buah tangan, and helped coordinate BPJS care.",
                            cashDelta = -950,
                            relationshipBoostAll = 14,
                            karmaDelta = 10,
                            happinessDelta = 9,
                            stressDelta = -5
                        ),
                        ChoiceOption(
                            id = "id_opt_send_remit",
                            label = "Send Extra Allowance & Video Call",
                            effectHint = "-$1,400, +Work Focus, +8 Bonds",
                            outcomeSummary = "Covered travel comforts from afar while finishing a tight project deadline.",
                            cashDelta = -1400,
                            relationshipBoostAll = 8,
                            careerBonus = true,
                            stressDelta = 4
                        )
                    )
                ),
                LocalIssueEvent(
                    id = "id_monsoon_flood",
                    title = "Monsoon Resilience & Community Aid",
                    narrative = "Heavy tropical rains caused localized flooding in nearby districts, testing city infrastructure.",
                    options = listOf(
                        ChoiceOption(
                            id = "id_opt_gotong",
                            label = "Join Gotong Royong Relief Drive",
                            effectHint = "-$400, +15 Karma, +900 Followers",
                            outcomeSummary = "Organized clean water kits with neighbors and ran a transparent charity stream.",
                            cashDelta = -400,
                            karmaDelta = 15,
                            happinessDelta = 10,
                            followersDelta = 900,
                            sparklesDelta = 90
                        ),
                        ChoiceOption(
                            id = "id_opt_drainage",
                            label = "Waterproof Home & Check Air Quality",
                            effectHint = "-$1,100, -10 Disease Risk, +Health",
                            outcomeSummary = "Upgraded home filtration and drainage before the peak monsoon weeks.",
                            cashDelta = -1100,
                            diseaseRiskDelta = -10,
                            healthDelta = 6
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Japan",
            cities = listOf("Kyoto, Japan", "Tokyo, Japan", "Osaka, Japan", "Sapporo, Japan"),
            firstNames = listOf("Ren", "Hina", "Haruto", "Mei", "Sora", "Yui", "Kenji", "Aoi"),
            lastNames = listOf("Tanaka", "Sato", "Takahashi", "Watanabe", "Kobayashi", "Nakamura"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Kansai Yakuza Gumi",
                localTerm = "Yakuza",
                ranks = listOf("Chinpira", "Shatei", "Wakagashira", "Kumicho"),
                hustleTitles = listOf(
                    "Kabukicho Club Protection" to "Enforced quiet security deals across neon nightlife venues.",
                    "Grey-Market Construction Kickback" to "Brokered off-book subcontracting favors near the bay.",
                    "High-Stakes Mahjong Parlor" to "Collected house cuts from a hidden late-night parlor."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "jp_worklife",
                    title = "Work-Style Reform & Regional Revival",
                    narrative = "Rising urban rents and long overtime hours spark a debate over moving to a quiet machiya town.",
                    options = listOf(
                        ChoiceOption(
                            id = "jp_opt_kyoto",
                            label = "Embrace Slow Craft & Remote Hours",
                            effectHint = "-14 Stress, +Creativity, +9 Happiness",
                            outcomeSummary = "Restored a sunlit workspace near cedar hills and prioritized evening rest.",
                            stressDelta = -14,
                            anxietyDelta = -10,
                            creativityDelta = 8,
                            happinessDelta = 9
                        ),
                        ChoiceOption(
                            id = "jp_opt_tokyo",
                            label = "Lead Major City Project Sprint",
                            effectHint = "+$4,500, +Career, +8 Stress",
                            outcomeSummary = "Delivered a high-profile launch in central Tokyo with strong financial rewards.",
                            cashDelta = 4500,
                            careerBonus = true,
                            stressDelta = 8
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Hong Kong",
            cities = listOf("Kowloon, Hong Kong", "Central, Hong Kong", "Sha Tin, Hong Kong"),
            firstNames = listOf("Anson", "Chloe", "Marcus", "Siu-Ming", "Wing-Yan", "Kelvin", "Tsz-Hin"),
            lastNames = listOf("Chan", "Wong", "Cheung", "Lau", "Leung", "Ng", "Tam"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Kowloon Triad Society",
                localTerm = "Triad",
                ranks = listOf("49er Initiate", "Red Pole Enforcer", "White Paper Fan", "Dragon Head"),
                hustleTitles = listOf(
                    "Mong Kok Street Market Cut" to "Managed underground licensing across late-night stalls.",
                    "Victoria Harbour Cargo Smuggling" to "Moved luxury goods off-manifest under harbor fog.",
                    "Underground Mahjong Syndicate" to "Oversaw private high-roller tables in an industrial loft."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "hk_micro_flat",
                    title = "Harbour Rent & Side-Hustle Culture",
                    narrative = "Sky-high apartment rents clash with Hong Kong's vibrant independent design and indie-film scene.",
                    options = listOf(
                        ChoiceOption(
                            id = "hk_opt_studio",
                            label = "Co-Rent an Industrial Loft in Kwun Tong",
                            effectHint = "-$1,400, +Creativity, +10 Bonds",
                            outcomeSummary = "Converted an old factory floor into a shared sunlit art and filming collective.",
                            cashDelta = -1400,
                            creativityDelta = 10,
                            relationshipBoostAll = 10,
                            stressDelta = -6
                        ),
                        ChoiceOption(
                            id = "hk_opt_finance",
                            label = "Take Night-Shift FinTech Contracting",
                            effectHint = "+$6,200, +9 Stress, -6 Energy",
                            outcomeSummary = "Powered through harbor nights to build real savings despite tiny living quarters.",
                            cashDelta = 6200,
                            stressDelta = 9,
                            energyDelta = -6
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Italy",
            cities = listOf("Milan, Italy", "Naples, Italy", "Rome, Italy", "Bologna, Italy"),
            firstNames = listOf("Lorenzo", "Giulia", "Matteo", "Chiara", "Marco", "Sofia", "Luca"),
            lastNames = listOf("Rossi", "Esposito", "Russo", "Conti", "Moretti", "Ricci"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "La Famiglia Syndicate",
                localTerm = "Mafia",
                ranks = listOf("Picciotto", "Soldato", "Caporegime", "Don"),
                hustleTitles = listOf(
                    "Port Customs Bribe" to "Redirected luxury leather shipments through private docks.",
                    "Piazza Protection Pact" to "Collected quiet 'insurance' retainers from late-night venues.",
                    "Vintage Art Forgery Ring" to "Laundered questionable canvases through private auctions."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "it_artisan_economy",
                    title = "Youth Brain Drain & Artisan Revival",
                    narrative = "Graduates weigh moving abroad against revitalizing historic family ateliers and coastal tourism.",
                    options = listOf(
                        ChoiceOption(
                            id = "it_opt_atelier",
                            label = "Digitize Local Craft & Export Globally",
                            effectHint = "+$4,800, +1,200 Followers, +Creativity",
                            outcomeSummary = "Filmed Tuscan and Neapolitan workshops for international buyers with huge acclaim.",
                            cashDelta = 4800,
                            followersDelta = 1200,
                            creativityDelta = 9,
                            happinessDelta = 8
                        ),
                        ChoiceOption(
                            id = "it_opt_slow",
                            label = "Join Regional Culinary Slow-Food Guild",
                            effectHint = "+Health, +10 Bonds, -10 Stress",
                            outcomeSummary = "Built a grounded network around neighborhood trattorias and weekend markets.",
                            healthDelta = 6,
                            relationshipBoostAll = 10,
                            stressDelta = -10
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "United States",
            cities = listOf("Portland, USA", "Seattle, USA", "Austin, USA", "Brooklyn, USA", "Los Angeles, USA"),
            firstNames = listOf("Clara", "Julian", "Rowan", "Maya", "Theo", "Nora", "Owen", "Elena"),
            lastNames = listOf("Vance", "Mercer", "Bennett", "Morales", "Kerr", "Hayes"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Metro Street Syndicate",
                localTerm = "Gangster",
                ranks = listOf("Lookout", "Runner", "Lieutenant", "Kingpin"),
                hustleTitles = listOf(
                    "Armored Van Intercept" to "Pulled off a tense midnight heist outside the rail yard.",
                    "Underground Street Racing Book" to "Ran unlicensed high-stakes wagers under highway overpasses.",
                    "Warehouse Heist Crew" to "Cleared out high-end studio hardware before security arrived."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "us_housing_health",
                    title = "Housing Costs & Healthcare Navigation",
                    narrative = "Rent renewals and insurance deductibles jumped sharply this year across metro neighborhoods.",
                    options = listOf(
                        ChoiceOption(
                            id = "us_opt_coop",
                            label = "Join a Community Co-Op & Preventive Plan",
                            effectHint = "-$1,200, -10 Disease Risk, +Bonds",
                            outcomeSummary = "Shared living costs and locked in preventive wellness coverage.",
                            cashDelta = -1200,
                            diseaseRiskDelta = -10,
                            stressDelta = -8,
                            relationshipBoostAll = 6
                        ),
                        ChoiceOption(
                            id = "us_opt_side",
                            label = "Launch Weekend Freelance Retainer",
                            effectHint = "+$5,800, +10 Stress, -6 Energy",
                            outcomeSummary = "Took on extra consulting clients to build a sturdy emergency cushion.",
                            cashDelta = 5800,
                            stressDelta = 10,
                            energyDelta = -6
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "United Kingdom",
            cities = listOf("London, UK", "Manchester, UK", "Edinburgh, UK"),
            firstNames = listOf("Arthur", "Freya", "Callum", "Imogen", "Jude", "Poppy", "Miles"),
            lastNames = listOf("Whitaker", "Benson", "Fletcher", "Gallagher", "Fraser", "Sterling"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "The East End Firm",
                localTerm = "Firm Enforcer",
                ranks = listOf("Grafter", "Heavy", "Face", "Guv'nor"),
                hustleTitles = listOf(
                    "Thames Lockup Job" to "Liberated crates of vintage watches from a riverside depot.",
                    "Unlicensed Boxing Purse" to "Rigged odds at a smoky backroom fight night.",
                    "Mayfair Art Lift" to "Swapped a collector's frame during a gallery blackout."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "uk_cost_living",
                    title = "Energy Bills & Creative Grassroots",
                    narrative = "Winter utility hikes and rail strikes challenge independent artists across British cities.",
                    options = listOf(
                        ChoiceOption(
                            id = "uk_opt_pub",
                            label = "Host Indie Acoustic Nights at Local Venue",
                            effectHint = "+$2,900, +1,000 Followers, +Bonds",
                            outcomeSummary = "Kept a beloved neighborhood venue alive with packed acoustic showcases.",
                            cashDelta = 2900,
                            followersDelta = 1000,
                            relationshipBoostAll = 8,
                            happinessDelta = 8
                        ),
                        ChoiceOption(
                            id = "uk_opt_insulate",
                            label = "Retrofit Flat & Join Commuter Cycle Scheme",
                            effectHint = "-$850, +Fitness, -8 Stress",
                            outcomeSummary = "Skipped rail delays on a commuter bike and lowered monthly utility bills.",
                            cashDelta = -850,
                            fitnessDelta = 8,
                            stressDelta = -8
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "South Korea",
            cities = listOf("Seoul, South Korea", "Busan, South Korea", "Jeju, South Korea"),
            firstNames = listOf("Minjun", "Seo-yeon", "Jiwon", "Ha-eun", "Doyun", "Yuna"),
            lastNames = listOf("Park", "Kim", "Lee", "Choi", "Jung", "Kang"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Busan Port Jopok",
                localTerm = "Jopok",
                ranks = listOf("Maknae", "Haengdongdaejang", "Bujang", "Hyungnim"),
                hustleTitles = listOf(
                    "Gangnam VIP Club Skim" to "Diverted bottle-service cash from back-room lounges.",
                    "Busan Pier Contraband" to "Passed unmarked shipping containers through night security.",
                    "Crypto-Wash Syndicate" to "Shuffled illicit winnings through shell arcade tokens."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "kr_digital_burnout",
                    title = "Hyper-Competitive Pace & Cafe Culture",
                    narrative = "Sleepless work culture collides with a thriving neighborhood stationery & bakery scene.",
                    options = listOf(
                        ChoiceOption(
                            id = "kr_opt_jeju",
                            label = "Take a Jeju Digital Detox Sabbatical",
                            effectHint = "-$1,100, -16 Anxiety, +12 Happiness",
                            outcomeSummary = "Walked coastal Olle trails and reset sleep rhythm away from notifications.",
                            cashDelta = -1100,
                            anxietyDelta = -16,
                            stressDelta = -14,
                            happinessDelta = 12
                        ),
                        ChoiceOption(
                            id = "kr_opt_wave",
                            label = "Ride Global Culture Creator Wave",
                            effectHint = "+1,600 Followers, +110 ✦, +$2,200",
                            outcomeSummary = "Shared bilingual aesthetic vlogs that resonated with international audiences.",
                            followersDelta = 1600,
                            sparklesDelta = 110,
                            cashDelta = 2200
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Portugal",
            cities = listOf("Lisbon, Portugal", "Porto, Portugal", "Coimbra, Portugal"),
            firstNames = listOf("Tiago", "Ines", "Mateo", "Sofia", "Duarte", "Beatriz"),
            lastNames = listOf("Almeida", "Ferreira", "Silva", "Costa", "Sousa", "Oliveira"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Atlantic Dock Smugglers",
                localTerm = "Contrabandista",
                ranks = listOf("Vigía", "Barqueiro", "Capitão", "Patrão"),
                hustleTitles = listOf(
                    "Tagus Midnight Boat Drop" to "Ran fast-boat cargo past coastal patrol beacons.",
                    "Alfama Counterfeit Pass Ring" to "Sold forged transit and festival passes to tourists.",
                    "Antique Tile Heist" to "Stripped rare azulejos from an abandoned manor."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "pt_housing_sun",
                    title = "Historic Neighborhoods & Rising Rents",
                    narrative = "Short-term tourism surges are reshaping quiet tiled streets and local bakeries.",
                    options = listOf(
                        ChoiceOption(
                            id = "pt_opt_local",
                            label = "Advocate for Neighborhood Rent Cap",
                            effectHint = "+12 Karma, +8 Bonds, +Happiness",
                            outcomeSummary = "Organized community dinners that kept longtime artisans in the quarter.",
                            karmaDelta = 12,
                            relationshipBoostAll = 8,
                            happinessDelta = 8
                        ),
                        ChoiceOption(
                            id = "pt_opt_inland",
                            label = "Move to a Sunlit Studio in Coimbra",
                            effectHint = "+$2,400 saved, -10 Stress, +Creativity",
                            outcomeSummary = "Traded crowded avenues for river views and lower monthly bills.",
                            cashDelta = 2400,
                            stressDelta = -10,
                            creativityDelta = 6
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Denmark",
            cities = listOf("Copenhagen, Denmark", "Aarhus, Denmark"),
            firstNames = listOf("Freja", "Sander", "Astrid", "Mikkel", "Clara", "Emil"),
            lastNames = listOf("Lindqvist", "Jensen", "Nielsen", "Madsen", "Krog"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Harbour Crew Syndicate",
                localTerm = "Underworld Crew",
                ranks = listOf("Runner", "Fixer", "Shot-Caller", "Harbour Boss"),
                hustleTitles = listOf(
                    "Christiania Grey-Market Shift" to "Handled off-book distribution through canal warehouses.",
                    "Vintage Design Chair Heist" to "Lifted mid-century furniture from an auction storeroom.",
                    "Baltic Ferry Smuggle" to "Moved untaxed luxury goods across the strait."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "dk_green_cycle",
                    title = "Green Transit & Winter Light",
                    narrative = "Short Nordic winter days challenge mood even as cycling and clean energy thrive.",
                    options = listOf(
                        ChoiceOption(
                            id = "dk_opt_hygge",
                            label = "Host Candlelit Reading & Soup Nights",
                            effectHint = "-10 Anxiety, +12 Happiness, +10 Bonds",
                            outcomeSummary = "Turned dark February evenings into warm weekly gatherings.",
                            anxietyDelta = -10,
                            happinessDelta = 12,
                            relationshipBoostAll = 10
                        ),
                        ChoiceOption(
                            id = "dk_opt_green",
                            label = "Join Civic Climate Innovation Lab",
                            effectHint = "+Intellect, +Karma, +$3,000",
                            outcomeSummary = "Contributed to bike-lane telemetry and neighborhood solar grants.",
                            intellectDelta = 7,
                            karmaDelta = 10,
                            cashDelta = 3000
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "India",
            cities = listOf("Bengaluru, India", "Mumbai, India", "Jaipur, India", "Kochi, India"),
            firstNames = listOf("Aarav", "Ananya", "Vikram", "Diya", "Rohan", "Meera", "Kabir", "Ishani"),
            lastNames = listOf("Sharma", "Nair", "Patel", "Iyer", "Kapoor", "Menon", "Desai"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Mumbai Dock Underworld",
                localTerm = "D-Company Fixer",
                ranks = listOf("Tapori", "Bhai's Scout", "Area Don", "Kingpin"),
                hustleTitles = listOf(
                    "Hawala Underground Transfer" to "Routed unrecorded cash bundles across textile bazaars.",
                    "Pirated Film Print Syndicate" to "Leaked early cinema reels to underground distributors.",
                    "Cricket Match-Fixing Book" to "Brokered illegal shadow odds during T20 season."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "in_tech_air",
                    title = "Tech Boom & Urban Air Quality",
                    narrative = "Startup opportunities are booming while seasonal smog and metro traffic test daily wellness.",
                    options = listOf(
                        ChoiceOption(
                            id = "in_opt_wellness",
                            label = "Invest in Home Air Care & Morning Yoga",
                            effectHint = "-$900, +10 Health, -12 Disease Risk",
                            outcomeSummary = "Protected lung health and grounded mornings before logging online.",
                            cashDelta = -900,
                            healthDelta = 10,
                            diseaseRiskDelta = -12,
                            stressDelta = -8
                        ),
                        ChoiceOption(
                            id = "in_opt_fintech",
                            label = "Join Regional Creator-Fintech Sprint",
                            effectHint = "+$5,200, +1,100 Followers, +Career",
                            outcomeSummary = "Built tools for vernacular creators and grew a vibrant subscriber base.",
                            cashDelta = 5200,
                            followersDelta = 1100,
                            careerBonus = true
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Brazil",
            cities = listOf("São Paulo, Brazil", "Florianópolis, Brazil", "Curitiba, Brazil"),
            firstNames = listOf("Lucas", "Beatriz", "Gabriel", "Camila", "Rafael", "Larissa"),
            lastNames = listOf("Silva", "Santos", "Oliveira", "Souza", "Pereira", "Costa"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Comando do Porto",
                localTerm = "Comando",
                ranks = listOf("Olheiro", "Gerente", "Frente", "Chefe"),
                hustleTitles = listOf(
                    "Santos Cargo Diversion" to "Siphoned high-value electronics off port manifests.",
                    "Nighttime Luxury Car Ring" to "Moved custom sports cars through chop-shop networks.",
                    "Illegal Bingo Syndicate" to "Managed underground gambling halls in the city center."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "br_creator_solar",
                    title = "Mobile Creator Surge & Cost of Living",
                    narrative = "Inflation nudges household budgets while Brazilian music and creator collectives soar globally.",
                    options = listOf(
                        ChoiceOption(
                            id = "br_opt_music",
                            label = "Collaborate on Neighborhood Session",
                            effectHint = "+1,500 Followers, +100 ✦, +Happiness",
                            outcomeSummary = "Recorded a sunset acoustic live stream that trended across local feeds.",
                            followersDelta = 1500,
                            sparklesDelta = 100,
                            happinessDelta = 10,
                            creativityDelta = 7
                        ),
                        ChoiceOption(
                            id = "br_opt_coop",
                            label = "Start Community Solar & Bulk Co-Op",
                            effectHint = "+$2,800 saved, +10 Karma, +Bonds",
                            outcomeSummary = "Cut monthly utility bills and brought neighbors closer together.",
                            cashDelta = 2800,
                            karmaDelta = 10,
                            relationshipBoostAll = 8
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Australia",
            cities = listOf("Melbourne, Australia", "Sydney, Australia", "Brisbane, Australia"),
            firstNames = listOf("Liam", "Chloe", "Noah", "Isla", "Oliver", "Ruby"),
            lastNames = listOf("Campbell", "Taylor", "Reid", "Wilson", "Fraser", "Clarke"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Bikie Docks Crew",
                localTerm = "Underbelly Crew",
                ranks = listOf("Prospect", "Enforcer", "Sergeant", "President"),
                hustleTitles = listOf(
                    "Laneway Poker Den" to "Skimmed stakes from a hidden back-alley card club.",
                    "Outback Opals Run" to "Fenced uncertified gemstones to overseas buyers.",
                    "Warehouse Tobacco Raid" to "Moved untaxed crates through suburban storage units."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "au_coastal",
                    title = "Cost of Rent & Coastal Heatwaves",
                    narrative = "Summer heatwaves and tight rental markets push households toward resilient living.",
                    options = listOf(
                        ChoiceOption(
                            id = "au_opt_garden",
                            label = "Build Shade Garden & Early Surf Routine",
                            effectHint = "+10 Fitness, -10 Stress, -6 Disease Risk",
                            outcomeSummary = "Beat the afternoon heat with sunrise movement and shaded courtyards.",
                            fitnessDelta = 10,
                            stressDelta = -10,
                            diseaseRiskDelta = -6,
                            happinessDelta = 8
                        ),
                        ChoiceOption(
                            id = "au_opt_studio",
                            label = "Open Laneway Design & Podcast Niche",
                            effectHint = "+$3,400, +900 Followers",
                            outcomeSummary = "Turned Melbourne laneway stories into a thriving listener series.",
                            cashDelta = 3400,
                            followersDelta = 900,
                            creativityDelta = 6
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Canada",
            cities = listOf("Montreal, Canada", "Vancouver, Canada", "Toronto, Canada"),
            firstNames = listOf("Camille", "Hugo", "Avery", "Liam", "Zoe", "Etienne"),
            lastNames = listOf("Duval", "Tremblay", "Roy", "MacLeod", "Bouchard", "Chen"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "St. Lawrence Dock Ring",
                localTerm = "Syndicate",
                ranks = listOf("Runner", "Collector", "Capo", "Port Boss"),
                hustleTitles = listOf(
                    "Maple Reserve Heist" to "Siphoned specialty warehouse reserves into unmarked trucks.",
                    "Cross-Border Grey Goods" to "Moved untaxed electronics across snowy backroads.",
                    "Old Port Protection Cut" to "Pressured late-night venues for seasonal retainers."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "ca_winter_housing",
                    title = "Housing Crunch & Long Winters",
                    narrative = "Winter heating bills and housing waitlists test patience across major cities.",
                    options = listOf(
                        ChoiceOption(
                            id = "ca_opt_arts",
                            label = "Apply for Community Arts Residency",
                            effectHint = "+$2,600, +Creativity, -8 Anxiety",
                            outcomeSummary = "Secured a warm studio grant and built meaningful creative friendships.",
                            cashDelta = 2600,
                            creativityDelta = 9,
                            anxietyDelta = -8,
                            happinessDelta = 7
                        ),
                        ChoiceOption(
                            id = "ca_opt_outdoor",
                            label = "Embrace Nordic Ski & Winter Wellness",
                            effectHint = "+10 Fitness, +8 Health, -10 Stress",
                            outcomeSummary = "Turned snowy months into energizing outdoor rituals.",
                            fitnessDelta = 10,
                            healthDelta = 8,
                            stressDelta = -10
                        )
                    )
                )
            )
        ),
        CountryProfile(
            countryName = "Nigeria",
            cities = listOf("Lagos, Nigeria", "Abuja, Nigeria", "Ibadan, Nigeria"),
            firstNames = listOf("Tunde", "Amara", "Chidi", "Zainab", "Kemi", "Emeka"),
            lastNames = listOf("Okafor", "Adeyemi", "Bello", "Balogun", "Eze", "Olawale"),
            crimeSyndicate = CrimeSyndicateProfile(
                organizationName = "Apapa Wharf Syndicate",
                localTerm = "Area Boys Crew",
                ranks = listOf("Street Scout", "Area Captain", "Wharf Fixer", "Chief Agbero"),
                hustleTitles = listOf(
                    "Motor Park Levy Skim" to "Collected unofficial route tolls at dawn rush hour.",
                    "Apapa Container Diversion" to "Rerouted luxury car imports out of dockside queues.",
                    "Underground Generator Fuel Ring" to "Sold unrecorded diesel drums during peak grid outages."
                )
            ),
            localIssues = listOf(
                LocalIssueEvent(
                    id = "ng_creator_power",
                    title = "Tech Hub Hustle & Solar Independence",
                    narrative = "Vibrant creative tech hubs are surging, though grid reliability and fuel costs require ingenuity.",
                    options = listOf(
                        ChoiceOption(
                            id = "ng_opt_solar",
                            label = "Invest in Home Solar Inverter Studio",
                            effectHint = "-$1,600, +1,400 Followers, -12 Stress",
                            outcomeSummary = "Uninterrupted power kept live streams crisp and workdays calm.",
                            cashDelta = -1600,
                            followersDelta = 1400,
                            stressDelta = -12,
                            sparklesDelta = 100
                        ),
                        ChoiceOption(
                            id = "ng_opt_hub",
                            label = "Mentor Young Creators at Tech Hub",
                            effectHint = "+14 Karma, +$3,200, +Bonds",
                            outcomeSummary = "Shared production workflows and earned consulting stipends.",
                            karmaDelta = 14,
                            cashDelta = 3200,
                            relationshipBoostAll = 8
                        )
                    )
                )
            )
        )
    )

    val allBirthplaces: List<String> = countries.flatMap { it.cities }

    fun findCountryForBirthplace(birthplace: String): CountryProfile {
        return countries.firstOrNull { country ->
            country.cities.any { it.equals(birthplace, ignoreCase = true) } ||
                birthplace.contains(country.countryName, ignoreCase = true)
        } ?: countries.first()
    }

    fun syndicateForBirthplace(birthplace: String): CrimeSyndicateProfile {
        return findCountryForBirthplace(birthplace).crimeSyndicate
    }

    fun randomNameForBirthplace(birthplace: String): Pair<String, String> {
        val country = findCountryForBirthplace(birthplace)
        return country.firstNames.random() to country.lastNames.random()
    }

    fun randomCompanionFirstName(birthplace: String): String {
        return findCountryForBirthplace(birthplace).firstNames.random()
    }

    fun randomCompanionLastName(birthplace: String): String {
        return findCountryForBirthplace(birthplace).lastNames.random()
    }

    fun randomLocalIssuePrompt(birthplace: String, age: Int): LifeChoicePrompt? {
        val issues = findCountryForBirthplace(birthplace).localIssues
        if (issues.isEmpty()) return null
        val issue = issues.random()
        return LifeChoicePrompt(
            id = "${issue.id}_$age",
            category = EntryCategory.RANDOM_EVENT,
            title = issue.title,
            narrative = issue.narrative,
            options = issue.options
        )
    }
}
