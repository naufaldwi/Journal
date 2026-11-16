const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid) {
  return {
    userId: uid,
    displayName: "Alice Lin",
    leaderboardAlias: "QuietCedar",
    showOnLeaderboard: true,
    anonymousLeaderboard: false,
    autoCloudSync: true,
    showOptionalEndOfLifeAds: true,
    sparkles: 250,
    totalLivesPlayed: 1,
    unlockedAchievementIds: ["first_breath"],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validLifeSave(uid) {
  return {
    userId: uid,
    characterName: "Clara Vance",
    birthplace: "Kyoto, Japan",
    birthYear: 1998,
    currentAge: 24,
    isAlive: true,
    legacyScore: 420,
    netWorth: 18500,
    followers: 12400,
    careerTitle: "Indie Creator",
    epitaph: "",
    trait: "Curious",
    pronouns: "she/her",
    stats: {
      health: 86,
      happiness: 80,
      intellect: 78,
      appearance: 74,
      creativity: 88,
      energy: 75,
      fitness: 70,
      diseaseRisk: 12,
      stress: 28,
      anxiety: 22,
      karma: 65,
    },
    appearance: {
      skinToneIndex: 2,
      hairStyleIndex: 1,
      hairColorIndex: 0,
      outfitIndex: 3,
      accessoryIndex: 1,
      backdropIndex: 0,
    },
    socialChannel: {
      handle: "@claravlogs",
      niche: "Daily Vlogging",
      totalStreams: 4,
      merchLaunched: false,
      sponsorshipTier: "Micro",
      gearLevel: 2,
    },
    relationships: [
      {
        id: "mom_1",
        name: "Hana Vance",
        role: "Mother",
        closeness: 90,
        status: "Devoted",
      },
    ],
    recentEntries: [
      {
        age: 24,
        year: 2022,
        category: "Social",
        title: "First Cozy Stream",
        detail: "Streamed tea & sketching for 210 viewers.",
      },
    ],
    achievedIds: ["first_breath"],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validLeaderboardEntry(uid, isPublic = true) {
  return {
    userId: uid,
    leaderboardName: "QuietCedar",
    characterName: "Clara Vance",
    careerTitle: "Novelist & Creator",
    legacyScore: 1450,
    netWorth: 240000,
    followers: 85000,
    ageReached: 84,
    isPublic,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("leaderboard").where("isPublic", "==", true).get());
});

test("Owner can create and update their own UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ref = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(ref.set(validProfile(ALICE_UID)));
  await assertSucceeds(ref.get());
  await assertSucceeds(
    ref.update({
      showOnLeaderboard: false,
      sparkles: 500,
      updatedAt: serverTimestamp(),
    })
  );
});

test("Shadow update with ghost field on UserProfile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ref = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(ref.set(validProfile(ALICE_UID)));
  await assertFails(
    ref.update({
      isAdmin: true,
      updatedAt: serverTimestamp(),
    })
  );
});

test("Cross-user access to UserProfile and LifeSaves is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("lives").doc("life_1").set(validLifeSave(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("lives").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("lives").doc("life_2").set(validLifeSave(BOB_UID))
  );
});

test("Owner can list and update their own LifeSave", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const livesCol = aliceDb.collection("users").doc(ALICE_UID).collection("lives");
  await assertSucceeds(livesCol.doc("life_active").set(validLifeSave(ALICE_UID)));
  await assertSucceeds(livesCol.get());
  await assertSucceeds(
    livesCol.doc("life_active").update({
      currentAge: 25,
      followers: 15000,
      updatedAt: serverTimestamp(),
    })
  );
});

test("Leaderboard enforces isPublic filter on list and owner-only writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardEntry(ALICE_UID, true))
  );
  // Private entry by Bob
  await assertSucceeds(
    bobDb.collection("leaderboard").doc(BOB_UID).set(validLeaderboardEntry(BOB_UID, false))
  );

  // Unfiltered list fails (rules are not filters)
  await assertFails(aliceDb.collection("leaderboard").get());

  // Filtered public list succeeds
  await assertSucceeds(
    aliceDb.collection("leaderboard").where("isPublic", "==", true).get()
  );

  // Alice cannot read Bob's private leaderboard doc
  await assertFails(aliceDb.collection("leaderboard").doc(BOB_UID).get());

  // Bob cannot overwrite Alice's leaderboard entry
  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardEntry(BOB_UID, true))
  );
});
