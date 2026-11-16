package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.GuestSettings
import com.example.data.JournalRepository
import com.example.data.LocalGuestRepository
import com.example.data.attemptAutoSignIn
import com.example.data.authStateFlow
import com.example.ui.JournalViewModel
import com.example.ui.screens.ActivitiesHubScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FamilyTreeScreen
import com.example.ui.screens.JournalHomeScreen
import com.example.ui.screens.LegacyAndPrivacyScreen
import com.example.ui.screens.NewLifeScreen
import com.example.ui.theme.JournalTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_JOURNAL = "journal"
const val ROUTE_ACTIVITIES = "activities"
const val ROUTE_TREE = "family_tree"
const val ROUTE_PROFILE = "profile"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val navItems = listOf(
    NavDestinationItem(ROUTE_JOURNAL, "Journal", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_journal"),
    NavDestinationItem(ROUTE_ACTIVITIES, "Activities", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_activities"),
    NavDestinationItem(ROUTE_TREE, "Tree", Icons.Filled.AccountTree, Icons.Outlined.AccountTree, "nav_tree"),
    NavDestinationItem(ROUTE_PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JournalAppRoot()
        }
    }
}

@Composable
fun JournalAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localGuestRepo = remember(context) { LocalGuestRepository(context.applicationContext) }
    val guestSettings by localGuestRepo.guestSettingsFlow.collectAsStateWithLifecycle(initialValue = GuestSettings())
    val currentUser by Firebase.auth.authStateFlow().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val darkTheme = guestSettings.darkThemeOverride ?: isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        if (currentUser == null && !guestSettings.isGuestActive) {
            val credentialManager = CredentialManager.create(context)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    JournalTheme(darkTheme = darkTheme) {
        val user = currentUser
        if (user == null && !guestSettings.isGuestActive) {
            AuthScreen(
                onGoogleAuthSuccess = {
                    scope.launch { localGuestRepo.setGuestActive(false) }
                },
                onContinueAsGuest = {
                    scope.launch { localGuestRepo.setGuestActive(true) }
                }
            )
        } else {
            val sessionKey = user?.uid ?: "guest_local_session"
            key(sessionKey) {
                JournalSessionHost(
                    currentUserId = user?.uid,
                    displayName = user?.displayName,
                    localGuestRepo = localGuestRepo,
                    onExitGuestToAuth = {
                        scope.launch { localGuestRepo.setGuestActive(false) }
                    },
                    onUpgradedToGoogle = {
                        scope.launch { localGuestRepo.setGuestActive(false) }
                    }
                )
            }
        }
    }
}

@Composable
private fun JournalSessionHost(
    currentUserId: String?,
    displayName: String?,
    localGuestRepo: LocalGuestRepository,
    onExitGuestToAuth: () -> Unit,
    onUpgradedToGoogle: () -> Unit
) {
    val sessionKey = currentUserId ?: "guest_local_session"
    val viewModel: JournalViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                val firestoreRepo = if (currentUserId != null) {
                    val dbId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(dbId)
                    JournalRepository(db)
                } else {
                    null
                }
                JournalViewModel(
                    firestoreRepository = firestoreRepo,
                    localGuestRepository = localGuestRepo,
                    currentUserId = currentUserId,
                    defaultDisplayName = displayName
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_JOURNAL

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val sparkles = if (uiState.isGuest) {
        uiState.guestSettings.sparkles
    } else {
        (uiState.userProfile?.sparkles ?: 250L).toInt()
    }

    val showOptionalAds = if (uiState.isGuest) {
        uiState.guestSettings.showOptionalEndOfLifeAds
    } else {
        uiState.userProfile?.showOptionalEndOfLifeAds ?: true
    }

    if (uiState.showNewLifeWizard || uiState.activeLife == null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NewLifeScreen(
                    canCancel = uiState.activeLife != null,
                    onCancel = { viewModel.closeNewLifeWizard() },
                    onBeginLife = { name, place, year, pronouns, trait, appearance, stats ->
                        viewModel.startNewLife(name, place, year, pronouns, trait, appearance, stats)
                    }
                )
            }
        }
        return
    }

    val activeLife = uiState.activeLife!!

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_JOURNAL,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_JOURNAL) {
                        JournalHomeScreen(
                            life = activeLife,
                            sparkles = sparkles,
                            showOptionalEndOfLifeAds = showOptionalAds,
                            onAgeUp = { viewModel.ageUp() },
                            onResolveChoice = { viewModel.resolveChoice(it) },
                            onSetDiet = { viewModel.setDiet(it) },
                            onSetExercise = { viewModel.setExercise(it) },
                            onSetSleep = { viewModel.setSleep(it) },
                            onPerformWellnessAction = { viewModel.performWellnessAction(it) },
                            onClaimOptionalEndOfLifeAd = { viewModel.claimOptionalEndOfLifeAdReward() },
                            onStartNewLife = { viewModel.openNewLifeWizard() },
                            onContinueAsHeir = { viewModel.continueAsNextGeneration() },
                            onOpenFamilyTree = {
                                navController.navigate(ROUTE_TREE) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onCompleteDailyMission = { viewModel.completeDailyMission(it) },
                            onClaimDailyMissionBonus = { viewModel.claimDailyMissionBonus() }
                        )
                    }
                    composable(ROUTE_ACTIVITIES) {
                        ActivitiesHubScreen(
                            life = activeLife,
                            sparkles = sparkles,
                            lastStreamOutcome = uiState.lastStreamOutcome,
                            onDismissStreamOutcome = { viewModel.clearLastStreamOutcome() },
                            onEnrollEducation = { viewModel.enrollEducation(it) },
                            onApplyCareer = { viewModel.applyCareer(it) },
                            onWorkExtraHours = { viewModel.workExtraHours() },
                            onBoostFame = { viewModel.boostFameAndCraft() },
                            onTrainSkill = { viewModel.performSkillActivity(it) },
                            onTakeLoan = { viewModel.takeLoan(it) },
                            onRepayLoan = { viewModel.repayLoan(it) },
                            onJoinCrime = { viewModel.joinCrimeSyndicate() },
                            onCommitSyndicateOp = { viewModel.commitSyndicateOp(it) },
                            onLeaveCrime = { viewModel.leaveCrimeLife() },
                            onInteractRelationship = { id, act -> viewModel.interactRelationship(id, act) },
                            onMeetNewPerson = { romantic -> viewModel.meetNewPerson(romantic) },
                            onUpdateChannel = { handle, niche -> viewModel.updateChannelSettings(handle, niche) },
                            onPostContent = { rideTrend -> viewModel.postCreatorContent(rideTrend) },
                            onInteractFollowers = { action -> viewModel.interactWithFollowers(action) },
                            onCompleteLiveStream = { fmt, topic, engaged -> viewModel.completeLiveStream(fmt, topic, engaged) },
                            onLaunchMerch = { name -> viewModel.launchMerchLine(name) },
                            onSignSponsor = { name, pay, rep -> viewModel.signSponsorship(name, pay, rep) },
                            onUpgradeGear = { viewModel.upgradeStudioGearWithSparkles() }
                        )
                    }
                    composable(ROUTE_TREE) {
                        FamilyTreeScreen(
                            life = activeLife,
                            onContinueAsHeir = {
                                viewModel.continueAsNextGeneration()
                                navController.navigate(ROUTE_JOURNAL) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(ROUTE_PROFILE) {
                        LegacyAndPrivacyScreen(
                            uiState = uiState,
                            onResumeCloudSave = { save ->
                                viewModel.resumeCloudSave(save)
                                navController.navigate(ROUTE_JOURNAL) {
                                    launchSingleTop = true
                                }
                            },
                            onStartNewLife = { viewModel.openNewLifeWizard() },
                            onSyncNow = { viewModel.syncNowToCloud() },
                            onSavePrivacy = { alias, showBoard, anon, autoSync, showAds ->
                                viewModel.updatePrivacySettings(alias, showBoard, anon, autoSync, showAds)
                            },
                            onSetThemeOverride = { viewModel.setThemePreference(it) },
                            onDeleteCloudData = { viewModel.deleteAllCloudData() },
                            onUpgradeGuestWithGoogle = onUpgradedToGoogle,
                            onSignedOut = onExitGuestToAuth
                        )
                    }
                }
            }
        }
    }
}
