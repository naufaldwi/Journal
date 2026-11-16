package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.LifeState
import com.example.model.SocialChannel
import com.example.model.toDomain
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class JournalRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_andSaveLifeState_roundTripSuccessfully() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = JournalRepository(firestore)

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Alice Chronicler")
        }
        assertEquals(uid, profile.userId)

        val lifeId = "life_${UUID.randomUUID().toString().take(8)}"
        val life = LifeState(
            lifeId = lifeId,
            characterName = "Elena Thorne",
            birthplace = "Lisbon, Portugal",
            birthYear = 1996,
            currentAge = 28,
            socialChannel = SocialChannel(handle = "@elena", followers = 4500)
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveLifeState(life)
        }

        val saves = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeLifeSaves(uid).first { list -> list.any { it.id == lifeId } }
        }
        val savedDoc = saves.first { it.id == lifeId }
        val restored = savedDoc.toDomain()
        assertEquals("Elena Thorne", restored.characterName)
        assertEquals(4500, restored.socialChannel.followers)
    }

    @Test
    fun publishLeaderboardBest_andObservePublicLeaderboard_succeedsWhenOptedIn() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = JournalRepository(firestore)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile("Alice Chronicler")
        }

        val life = LifeState(
            lifeId = "life_${UUID.randomUUID().toString().take(8)}",
            characterName = "Marcus Sterling",
            birthplace = "Portland, USA",
            birthYear = 1988,
            currentAge = 72,
            isAlive = false,
            netWorth = 190000
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.publishLeaderboardBest(life, profile)
        }

        val board = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePublicLeaderboard().first { list -> list.any { it.characterName == "Marcus Sterling" } }
        }
        assertTrue(board.isNotEmpty())
    }

    @Test
    fun observeLifeSaves_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        signInTestUser(BOB_EMAIL)
        val bobRepo = JournalRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeLifeSaves(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for cross-user life saves")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    @Test
    fun observePublicLeaderboard_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = JournalRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observePublicLeaderboard().first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated leaderboard read")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_journal@test.com"
        const val BOB_EMAIL = "bob_journal@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
