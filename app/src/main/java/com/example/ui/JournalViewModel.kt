package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GuestSettings
import com.example.data.JournalRepository
import com.example.data.LocalGuestRepository
import com.example.engine.LifeEngine
import com.example.engine.MissionOutcome
import com.example.engine.StreamOutcome
import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.ChoiceOption
import com.example.model.DietPlan
import com.example.model.EducationType
import com.example.model.ExerciseRoutine
import com.example.model.LifeSaveDoc
import com.example.model.LifeState
import com.example.model.LifeStats
import com.example.model.LeaderboardEntryDoc
import com.example.model.MissionType
import com.example.model.SleepSchedule
import com.example.model.SocialNiche
import com.example.model.StreamFormat
import com.example.model.TemperamentTrait
import com.example.model.UserProfileDoc
import com.example.model.toDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JournalUiState(
    val isGuest: Boolean = true,
    val currentUserId: String? = null,
    val activeLife: LifeState? = null,
    val showNewLifeWizard: Boolean = false,
    val userProfile: UserProfileDoc? = null,
    val cloudSaves: List<LifeSaveDoc> = emptyList(),
    val leaderboard: List<LeaderboardEntryDoc> = emptyList(),
    val guestSettings: GuestSettings = GuestSettings(),
    val statusMessage: String? = null,
    val lastStreamOutcome: StreamOutcome? = null,
    val isSyncing: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(
    private val firestoreRepository: JournalRepository?,
    private val localGuestRepository: LocalGuestRepository,
    private val currentUserId: String?,
    private val defaultDisplayName: String?
) : ViewModel() {

    private val _activeLife = MutableStateFlow<LifeState?>(null)
    private val _showNewLifeWizard = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)
    private val _lastStreamOutcome = MutableStateFlow<StreamOutcome?>(null)
    private val _isSyncing = MutableStateFlow(false)

    private val profileFlow = if (currentUserId != null && firestoreRepository != null) {
        firestoreRepository.observeUserProfile(currentUserId).catch { emit(null) }
    } else {
        flowOf(null)
    }

    private val cloudSavesFlow = if (currentUserId != null && firestoreRepository != null) {
        firestoreRepository.observeLifeSaves(currentUserId).catch { emit(emptyList()) }
    } else {
        flowOf(emptyList())
    }

    private val leaderboardFlow = if (currentUserId != null && firestoreRepository != null) {
        firestoreRepository.observePublicLeaderboard().catch { emit(emptyList()) }
    } else {
        flowOf(emptyList())
    }

    private val baseDataFlow = combine(
        profileFlow,
        cloudSavesFlow,
        leaderboardFlow,
        localGuestRepository.guestSettingsFlow,
        localGuestRepository.savedGuestLifeFlow
    ) { profile, saves, board, guestSettings, guestLife ->
        BaseDataBundle(profile, saves, board, guestSettings, guestLife)
    }

    val uiState: StateFlow<JournalUiState> = combine(
        baseDataFlow,
        _activeLife.asStateFlow(),
        _showNewLifeWizard.asStateFlow(),
        _statusMessage.asStateFlow(),
        _lastStreamOutcome.asStateFlow(),
        _isSyncing.asStateFlow()
    ) { arr ->
        val base = arr[0] as BaseDataBundle
        val active = arr[1] as LifeState?
        val showWizard = arr[2] as Boolean
        val status = arr[3] as String?
        val streamOut = arr[4] as StreamOutcome?
        val syncing = arr[5] as Boolean

        val rawEffectiveLife = active
            ?: base.cloudSaves.firstOrNull()?.toDomain()
            ?: base.guestLife
        val effectiveLife = rawEffectiveLife?.let { LifeEngine.ensureDailyMissions(it) }

        JournalUiState(
            isGuest = currentUserId == null,
            currentUserId = currentUserId,
            activeLife = effectiveLife,
            showNewLifeWizard = showWizard || (effectiveLife == null),
            userProfile = base.profile,
            cloudSaves = base.cloudSaves,
            leaderboard = base.board,
            guestSettings = base.guestSettings,
            statusMessage = status,
            lastStreamOutcome = streamOut,
            isSyncing = syncing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = JournalUiState(
            isGuest = currentUserId == null,
            currentUserId = currentUserId
        )
    )

    init {
        if (currentUserId != null && firestoreRepository != null) {
            viewModelScope.launch {
                runCatching {
                    firestoreRepository.ensureUserProfile(defaultDisplayName ?: "Chronicler")
                }
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearLastStreamOutcome() {
        _lastStreamOutcome.value = null
    }

    fun openNewLifeWizard() {
        _showNewLifeWizard.value = true
    }

    fun closeNewLifeWizard() {
        if (uiState.value.activeLife != null) {
            _showNewLifeWizard.value = false
        }
    }

    fun startNewLife(
        characterName: String,
        birthplace: String,
        birthYear: Int,
        pronouns: String,
        trait: TemperamentTrait,
        appearance: AvatarAppearance,
        startingStats: LifeStats
    ) {
        val result = LifeEngine.createNewLife(
            characterName = characterName,
            birthplace = birthplace,
            birthYear = birthYear,
            pronouns = pronouns,
            trait = trait,
            appearance = appearance,
            startingStats = startingStats
        )
        _activeLife.value = result.updatedLife
        _showNewLifeWizard.value = false
        persistLifeAndRewards(
            life = result.updatedLife,
            newlyUnlocked = result.newlyUnlockedAchievements,
            sparklesDelta = result.sparklesEarned,
            incrementLivesPlayed = true
        )
        _statusMessage.value = "Started a new journal for ${result.updatedLife.characterName}."
    }

    fun continueAsNextGeneration() {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.continueAsHeir(current)
        _activeLife.value = result.updatedLife
        _showNewLifeWizard.value = false
        persistLifeAndRewards(
            life = result.updatedLife,
            newlyUnlocked = result.newlyUnlockedAchievements,
            sparklesDelta = result.sparklesEarned,
            incrementLivesPlayed = true
        )
        _statusMessage.value = "Continuing lineage as ${result.updatedLife.characterName} (Gen ${result.updatedLife.generation})."
    }

    fun resumeCloudSave(saveDoc: LifeSaveDoc) {
        _activeLife.value = saveDoc.toDomain()
        _showNewLifeWizard.value = false
        _statusMessage.value = "Loaded ${saveDoc.characterName} (Age ${saveDoc.currentAge})."
    }

    fun ageUp() {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val prevStage = current.lifeStage
        val result = LifeEngine.ageUp(current)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.AGE_MILESTONE)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            life = missionOut.updatedLife,
            newlyUnlocked = result.newlyUnlockedAchievements,
            sparklesDelta = result.sparklesEarned + missionOut.sparklesEarned
        )
        if (!missionOut.updatedLife.isAlive) {
            publishLegacyIfSignedIn(missionOut.updatedLife)
        } else {
            _statusMessage.value = composeActionMessage(
                base = when {
                    result.newlyUnlockedAchievements.isNotEmpty() -> "Achievement unlocked (+${result.sparklesEarned} ✦)!"
                    missionOut.updatedLife.lifeStage != prevStage -> "Age Milestone: ${missionOut.updatedLife.lifeStage.label}!"
                    else -> null
                },
                missionOutcome = missionOut
            )
        }
    }

    fun resolveChoice(option: ChoiceOption) {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.resolveChoice(current, option)
        _activeLife.value = result.updatedLife
        persistLifeAndRewards(result.updatedLife, result.newlyUnlockedAchievements, result.sparklesEarned)
        if (result.sparklesEarned > 0) {
            _statusMessage.value = "${option.label} (+${result.sparklesEarned} ✦)"
        }
    }

    fun enrollEducation(edu: EducationType) {
        val current = uiState.value.activeLife ?: return
        val (updated, msg) = LifeEngine.enrollInEducation(current, edu)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = msg
    }

    fun applyCareer(track: CareerTrack) {
        val current = uiState.value.activeLife ?: return
        val (updated, msg) = LifeEngine.applyForCareer(current, track)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = msg
    }

    fun workExtraHours() {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.workExtraHours(current)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.WORK_SPRINT)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage("Completed focused sprint at work.", missionOut)
    }

    fun boostFameAndCraft() {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.boostFameAndCraft(current)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.WORK_SPRINT)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage(
            missionOut.updatedLife.entries.firstOrNull()?.title ?: "Spotlight showcase complete!",
            missionOut
        )
    }

    fun joinCrimeSyndicate() {
        val current = uiState.value.activeLife ?: return
        val (updated, msg) = LifeEngine.joinCrimeSyndicate(current)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = msg
    }

    fun commitSyndicateOp(opIndex: Int) {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.commitSyndicateOp(current, opIndex)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.WORK_SPRINT)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage(
            missionOut.updatedLife.entries.firstOrNull()?.title ?: "Operation finished.",
            missionOut
        )
    }

    fun leaveCrimeLife() {
        val current = uiState.value.activeLife ?: return
        val result = LifeEngine.leaveCrimeLife(current)
        _activeLife.value = result.updatedLife
        persistLifeAndRewards(result.updatedLife, result.newlyUnlockedAchievements, result.sparklesEarned)
        _statusMessage.value = "Stepped away from the syndicate."
    }

    fun setDiet(diet: DietPlan) {
        val current = uiState.value.activeLife ?: return
        val updated = current.copy(diet = diet)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = "Diet updated to ${diet.label}."
    }

    fun setExercise(exercise: ExerciseRoutine) {
        val current = uiState.value.activeLife ?: return
        val updated = current.copy(exercise = exercise)
        val missionOut = if (exercise != ExerciseRoutine.SEDENTARY) {
            LifeEngine.progressMissionByType(updated, MissionType.GO_TO_GYM)
        } else {
            MissionOutcome(updated, null, 0, 0, null)
        }
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(missionOut.updatedLife, emptySet(), missionOut.sparklesEarned)
        _statusMessage.value = composeActionMessage("Movement routine: ${exercise.label}.", missionOut)
    }

    fun setSleep(sleep: SleepSchedule) {
        val current = uiState.value.activeLife ?: return
        val updated = current.copy(sleep = sleep)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = "Sleep schedule: ${sleep.label}."
    }

    fun performWellnessAction(action: String) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.performWellnessAction(current, action)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.MINDFUL_CHECKIN)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage(
            missionOut.updatedLife.entries.firstOrNull()?.title ?: "Wellness updated.",
            missionOut
        )
    }

    fun updateChannelSettings(handle: String, niche: SocialNiche) {
        val current = uiState.value.activeLife ?: return
        val cleanHandle = handle.trim().let { if (it.startsWith("@")) it else "@$it" }.take(30)
        val updated = current.copy(
            socialChannel = current.socialChannel.copy(
                handle = cleanHandle.ifBlank { "@journaler" },
                niche = niche
            )
        )
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 0)
        _statusMessage.value = "Channel updated: ${updated.socialChannel.handle} (${niche.label})"
    }

    fun postCreatorContent(rideTrend: Boolean) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.postCreatorContent(current, rideTrend)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.POST_SOCIAL)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage("Published content (+${result.sparklesEarned} ✦).", missionOut)
    }

    fun interactWithFollowers(actionType: String) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.interactWithFollowers(current, actionType)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.POST_SOCIAL)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage("Connected with your community (+${result.sparklesEarned} ✦).", missionOut)
    }

    fun launchMerchLine(merchName: String) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.launchMerchLine(current, merchName)
        _activeLife.value = result.updatedLife
        persistLifeAndRewards(result.updatedLife, result.newlyUnlockedAchievements, result.sparklesEarned)
        _statusMessage.value = "Merch line launched! Recurring royalties enabled."
    }

    fun signSponsorship(name: String, annualPay: Int, repDelta: Int) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.signSponsorship(current, name, annualPay, repDelta)
        _activeLife.value = result.updatedLife
        persistLifeAndRewards(result.updatedLife, result.newlyUnlockedAchievements, result.sparklesEarned)
        _statusMessage.value = "Signed partnership with $name."
    }

    fun completeLiveStream(format: StreamFormat, topic: String, engagedChat: Boolean) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val outcome = LifeEngine.completeLiveStream(current, format, topic, engagedChat)
        val liveMissionOut = LifeEngine.progressMissionByType(outcome.updatedLife, MissionType.GO_LIVE_STREAM)
        _activeLife.value = liveMissionOut.updatedLife
        _lastStreamOutcome.value = outcome.copy(
            updatedLife = liveMissionOut.updatedLife,
            sparklesEarned = outcome.sparklesEarned + liveMissionOut.sparklesEarned
        )
        persistLifeAndRewards(
            life = liveMissionOut.updatedLife,
            newlyUnlocked = outcome.newlyUnlockedAchievements,
            sparklesDelta = outcome.sparklesEarned + liveMissionOut.sparklesEarned
        )
        if (liveMissionOut.completedMission != null) {
            _statusMessage.value = composeActionMessage(null, liveMissionOut)
        }
    }

    fun upgradeStudioGearWithSparkles() {
        val current = uiState.value.activeLife ?: return
        val currentGear = current.socialChannel.gearLevel
        if (currentGear >= 5) {
            _statusMessage.value = "Studio gear is already at Tier 5 (Master Studio)."
            return
        }
        val cost = currentGear * 120
        val availableSparkles = if (currentUserId != null) {
            (uiState.value.userProfile?.sparkles ?: 0L).toInt()
        } else {
            uiState.value.guestSettings.sparkles
        }
        if (availableSparkles < cost) {
            _statusMessage.value = "Need $cost ✦ Sparkles to upgrade studio gear (host live streams to earn more!)."
            return
        }
        val updated = current.copy(
            socialChannel = current.socialChannel.copy(gearLevel = currentGear + 1)
        )
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), -cost)
        _statusMessage.value = "Upgraded Studio Gear to Tier ${currentGear + 1}!"
    }

    fun interactRelationship(relId: String, action: String) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.interactRelationship(current, relId, action)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.CONNECT_BOND)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage(
            missionOut.updatedLife.entries.firstOrNull()?.title,
            missionOut
        )
    }

    fun meetNewPerson(romantic: Boolean) {
        val current = uiState.value.activeLife ?: return
        if (!current.isAlive) return
        val result = LifeEngine.meetNewPerson(current, romantic)
        val missionOut = LifeEngine.progressMissionByType(result.updatedLife, MissionType.CONNECT_BOND)
        _activeLife.value = missionOut.updatedLife
        persistLifeAndRewards(
            missionOut.updatedLife,
            result.newlyUnlockedAchievements,
            result.sparklesEarned + missionOut.sparklesEarned
        )
        _statusMessage.value = composeActionMessage(
            missionOut.updatedLife.entries.firstOrNull()?.title,
            missionOut
        )
    }

    fun completeDailyMission(missionId: String) {
        val current = uiState.value.activeLife ?: return
        val outcome = LifeEngine.executeDailyMissionAction(current, missionId)
        _activeLife.value = outcome.updatedLife
        persistLifeAndRewards(outcome.updatedLife, emptySet(), outcome.sparklesEarned)
        if (outcome.completedMission != null) {
            _statusMessage.value = composeActionMessage(null, outcome)
        }
    }

    fun claimDailyMissionBonus() {
        val current = uiState.value.activeLife ?: return
        val outcome = LifeEngine.claimDailyMissionBonus(current)
        if (outcome.xpEarned > 0 || outcome.sparklesEarned > 0) {
            _activeLife.value = outcome.updatedLife
            persistLifeAndRewards(outcome.updatedLife, emptySet(), outcome.sparklesEarned)
            val levelNote = outcome.leveledUpTo?.let { " · Level Up to Lv.$it!" } ?: ""
            _statusMessage.value = "Daily Trio Bonus: +${outcome.xpEarned} XP, +${outcome.sparklesEarned} ✦, +$500$levelNote"
        }
    }

    private fun composeActionMessage(base: String?, missionOutcome: MissionOutcome): String? {
        val completed = missionOutcome.completedMission ?: return base
        val lvlNote = missionOutcome.leveledUpTo?.let { " · Level Up Lv.$it!" } ?: ""
        val missionMsg = "Mission '${completed.title}': +${missionOutcome.xpEarned} XP, +${missionOutcome.sparklesEarned} ✦$lvlNote"
        return if (base.isNullOrBlank()) missionMsg else "$base · $missionMsg"
    }

    fun claimOptionalEndOfLifeAdReward() {
        val current = uiState.value.activeLife ?: return
        if (current.isAlive || current.endOfLifeAdClaimed) return
        val updated = current.copy(endOfLifeAdClaimed = true)
        _activeLife.value = updated
        persistLifeAndRewards(updated, emptySet(), 250)
        _statusMessage.value = "Thank you! +250 ✦ Legacy Sparkles added for your next life."
    }

    fun syncNowToCloud() {
        val current = uiState.value.activeLife ?: return
        if (currentUserId == null || firestoreRepository == null) {
            _statusMessage.value = "Sign in with Google to enable cloud sync."
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            runCatching {
                firestoreRepository.saveLifeState(current)
                if (!current.isAlive || current.currentAge >= 18) {
                    firestoreRepository.publishLeaderboardBest(current, uiState.value.userProfile)
                }
            }.onSuccess {
                _statusMessage.value = "Cloud save synced."
            }.onFailure { err ->
                _statusMessage.value = "Sync note: ${err.localizedMessage ?: "offline cached"}"
            }
            _isSyncing.value = false
        }
    }

    fun updatePrivacySettings(
        leaderboardAlias: String,
        showOnLeaderboard: Boolean,
        anonymousLeaderboard: Boolean,
        autoCloudSync: Boolean,
        showOptionalEndOfLifeAds: Boolean
    ) {
        viewModelScope.launch {
            localGuestRepository.setShowOptionalAds(showOptionalEndOfLifeAds)
            if (currentUserId != null && firestoreRepository != null) {
                runCatching {
                    firestoreRepository.updatePrivacyAndProfile(
                        leaderboardAlias = leaderboardAlias,
                        showOnLeaderboard = showOnLeaderboard,
                        anonymousLeaderboard = anonymousLeaderboard,
                        autoCloudSync = autoCloudSync,
                        showOptionalEndOfLifeAds = showOptionalEndOfLifeAds
                    )
                }.onSuccess {
                    _statusMessage.value = "Privacy preferences saved."
                }.onFailure {
                    _statusMessage.value = "Could not update cloud privacy: ${it.localizedMessage}"
                }
            } else {
                _statusMessage.value = "Local privacy preferences saved."
            }
        }
    }

    fun setThemePreference(darkOverride: Boolean?) {
        viewModelScope.launch {
            localGuestRepository.setThemePreference(darkOverride)
        }
    }

    fun deleteAllCloudData(onDone: () -> Unit = {}) {
        if (currentUserId == null || firestoreRepository == null) return
        viewModelScope.launch {
            _isSyncing.value = true
            runCatching {
                firestoreRepository.deleteAllUserData()
                localGuestRepository.clearGuestLife()
            }.onSuccess {
                _activeLife.value = null
                _showNewLifeWizard.value = true
                _statusMessage.value = "All cloud saves and leaderboard records deleted."
                onDone()
            }.onFailure {
                _statusMessage.value = "Failed to delete cloud data: ${it.localizedMessage}"
            }
            _isSyncing.value = false
        }
    }

    private fun persistLifeAndRewards(
        life: LifeState,
        newlyUnlocked: Set<String>,
        sparklesDelta: Int,
        incrementLivesPlayed: Boolean = false
    ) {
        viewModelScope.launch {
            if (currentUserId != null && firestoreRepository != null) {
                val autoSync = uiState.value.userProfile?.autoCloudSync ?: true
                if (autoSync) {
                    runCatching { firestoreRepository.saveLifeState(life) }
                }
                if (sparklesDelta != 0 || newlyUnlocked.isNotEmpty() || incrementLivesPlayed) {
                    runCatching {
                        firestoreRepository.updateWalletAndAchievements(
                            sparklesDelta = sparklesDelta.toLong(),
                            newAchievementIds = newlyUnlocked,
                            incrementLivesPlayed = incrementLivesPlayed
                        )
                    }
                }
            } else {
                localGuestRepository.saveGuestLife(life)
                if (sparklesDelta != 0 || newlyUnlocked.isNotEmpty() || incrementLivesPlayed) {
                    localGuestRepository.updateGuestProgress(
                        sparklesDelta = sparklesDelta,
                        newAchievements = newlyUnlocked,
                        incrementLivesPlayed = incrementLivesPlayed
                    )
                }
            }
        }
    }

    private fun publishLegacyIfSignedIn(life: LifeState) {
        if (currentUserId == null || firestoreRepository == null) return
        viewModelScope.launch {
            runCatching {
                firestoreRepository.publishLeaderboardBest(life, uiState.value.userProfile)
            }
        }
    }

    private data class BaseDataBundle(
        val profile: UserProfileDoc?,
        val cloudSaves: List<LifeSaveDoc>,
        val board: List<LeaderboardEntryDoc>,
        val guestSettings: GuestSettings,
        val guestLife: LifeState?
    )
}
