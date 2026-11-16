package com.example.data

import android.content.Context
import com.example.R
import com.example.model.LeaderboardEntryDoc
import com.example.model.LifeSaveDoc
import com.example.model.LifeState
import com.example.model.UserProfileDoc
import com.example.model.toFirestorePayload
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JournalRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(defaultDisplayName: String): UserProfileDoc {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfileDoc(id = uid, userId = uid)
            } else {
                val safeName = defaultDisplayName.trim().ifBlank { "Chronicler" }.take(60)
                val safeAlias = safeName.replace("\\s+".toRegex(), "").ifBlank { "QuietReader" }.take(30)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "leaderboardAlias" to safeAlias,
                    "showOnLeaderboard" to true,
                    "anonymousLeaderboard" to false,
                    "autoCloudSync" to true,
                    "showOptionalEndOfLifeAds" to true,
                    "sparkles" to 250L,
                    "totalLivesPlayed" to 0L,
                    "unlockedAchievementIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                UserProfileDoc(
                    id = uid,
                    userId = uid,
                    displayName = safeName,
                    leaderboardAlias = safeAlias,
                    showOnLeaderboard = true,
                    anonymousLeaderboard = false,
                    autoCloudSync = true,
                    showOptionalEndOfLifeAds = true,
                    sparkles = 250L,
                    totalLivesPlayed = 0L,
                    unlockedAchievementIds = emptyList()
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updatePrivacyAndProfile(
        leaderboardAlias: String,
        showOnLeaderboard: Boolean,
        anonymousLeaderboard: Boolean,
        autoCloudSync: Boolean,
        showOptionalEndOfLifeAds: Boolean
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val cleanAlias = leaderboardAlias.trim().ifBlank { "QuietReader" }.take(40)
        try {
            docRef.update(
                mapOf(
                    "leaderboardAlias" to cleanAlias,
                    "showOnLeaderboard" to showOnLeaderboard,
                    "anonymousLeaderboard" to anonymousLeaderboard,
                    "autoCloudSync" to autoCloudSync,
                    "showOptionalEndOfLifeAds" to showOptionalEndOfLifeAds,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val lbRef = db.collection("leaderboard").document(uid)
            val lbSnap = lbRef.get().await()
            if (lbSnap.exists()) {
                val effectiveName = if (anonymousLeaderboard) "Quiet Chronicler" else cleanAlias
                lbRef.update(
                    mapOf(
                        "leaderboardName" to effectiveName.take(50),
                        "isPublic" to showOnLeaderboard,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateWalletAndAchievements(
        sparklesDelta: Long,
        newAchievementIds: Set<String>,
        incrementLivesPlayed: Boolean = false
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            val existing = snap.toObject(UserProfileDoc::class.java)
            val currentSparkles = existing?.sparkles ?: 250L
            val updatedSparkles = (currentSparkles + sparklesDelta).coerceIn(0L, 100_000_000L)
            val mergedAchievements = ((existing?.unlockedAchievementIds ?: emptyList()) + newAchievementIds)
                .distinct()
                .take(50)
            val updatedLives = (existing?.totalLivesPlayed ?: 0L) + if (incrementLivesPlayed) 1L else 0L

            docRef.update(
                mapOf(
                    "sparkles" to updatedSparkles,
                    "unlockedAchievementIds" to mergedAchievements,
                    "totalLivesPlayed" to updatedLives,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeLifeSaves(userId: String): Flow<List<LifeSaveDoc>> = flow {
        val colRef = db.collection("users").document(userId).collection("lives")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LifeSaveDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveLifeState(lifeState: LifeState) {
        val uid = requireUserId()
        val docId = lifeState.lifeId.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_").take(120)
        val docRef = db.collection("users").document(uid).collection("lives").document(docId)
        val payload = lifeState.toFirestorePayload(uid).toMutableMap()
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                payload.remove("userId")
                payload["updatedAt"] = FieldValue.serverTimestamp()
                docRef.update(payload).await()
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
                payload["updatedAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteLifeSave(lifeId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("lives").document(lifeId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observePublicLeaderboard(): Flow<List<LeaderboardEntryDoc>> = flow {
        val path = "leaderboard"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .limit(100)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LeaderboardEntryDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.legacyScore }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun publishLeaderboardBest(
        lifeState: LifeState,
        profile: UserProfileDoc?
    ) {
        val uid = requireUserId()
        val docRef = db.collection("leaderboard").document(uid)
        val isPublic = profile?.showOnLeaderboard ?: true
        val effectiveName = if (profile?.anonymousLeaderboard == true) {
            "Quiet Chronicler"
        } else {
            profile?.leaderboardAlias?.ifBlank { profile.displayName } ?: "Chronicler"
        }
        try {
            val existingSnap = docRef.get().await()
            val existingScore = existingSnap.getLong("legacyScore") ?: -1L
            val newScore = lifeState.legacyScore.toLong()
            if (existingSnap.exists()) {
                if (newScore >= existingScore) {
                    docRef.update(
                        mapOf(
                            "leaderboardName" to effectiveName.take(50),
                            "characterName" to lifeState.characterName.take(80),
                            "careerTitle" to lifeState.currentCareerTitle.take(80),
                            "legacyScore" to newScore,
                            "netWorth" to lifeState.netWorth.toLong(),
                            "followers" to lifeState.socialChannel.followers.toLong(),
                            "ageReached" to lifeState.currentAge.toLong(),
                            "isPublic" to isPublic,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "leaderboardName" to effectiveName.take(50),
                        "characterName" to lifeState.characterName.take(80),
                        "careerTitle" to lifeState.currentCareerTitle.take(80),
                        "legacyScore" to newScore,
                        "netWorth" to lifeState.netWorth.toLong(),
                        "followers" to lifeState.socialChannel.followers.toLong(),
                        "ageReached" to lifeState.currentAge.toLong(),
                        "isPublic" to isPublic,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAllUserData() {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val lives = userRef.collection("lives").get().await()
            for (doc in lives.documents) {
                doc.reference.delete().await()
            }
            val lbRef = db.collection("leaderboard").document(uid)
            if (lbRef.get().await().exists()) {
                lbRef.delete().await()
            }
            if (userRef.get().await().exists()) {
                userRef.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userRef.path)
            throw e
        }
    }
}
