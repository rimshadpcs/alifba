// Checks ../firestore.rules against what the iOS app, the Android app and the Android QA build
// actually do in Firestore, plus what a stranger with the public API key must no longer be able
// to do. Run with: npm test (starts the Firestore emulator, nothing touches production).
import { test, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} from '@firebase/rules-unit-testing';
import {
  doc, getDoc, setDoc, updateDoc, deleteDoc, collection, getDocs, query, where, runTransaction,
} from 'firebase/firestore';

let env;

const PARENT = 'parentUid';
const OTHER = 'otherUid';
const EMAIL = 'parent@example.com';

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-alifba-rules',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

after(async () => { await env.cleanup(); });

beforeEach(async () => {
  await env.clearFirestore();
  // Seed data as an admin would (rules don't apply here)
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, 'chapters/101'), { id: 101, title: 'Lesson 1', chapterType: 'lesson' });
    await setDoc(doc(db, 'lessons/level1'), { chapters: [] });
    await setDoc(doc(db, 'badges/barakah_builder'), { title: 'Barakah Builder' });
    for (const c of ['stories', 'prophet_muhammad_stories', 'sahaba_stories', 'women_and_mothers', 'miracles']) {
      await setDoc(doc(db, `${c}/101`), { title: 'Story', status: 'free' });
    }
    await setDoc(doc(db, 'companions_stories/1'), { title: 'A future content collection' });
    await setDoc(doc(db, `users/${PARENT}`), { userId: PARENT, email: EMAIL, parentName: 'P', profiles: [], premiumStatus: 'active' });
    await setDoc(doc(db, `users/${PARENT}/devices/device1`), { fcmToken: 't' });
    await setDoc(doc(db, `users/${PARENT}/children/c1`), { name: 'kid' });
    await setDoc(doc(db, `users/${PARENT}/progress/p1`), { xp: 1 });
    // The same parent's second account under the same email (19 of these exist in production)
    await setDoc(doc(db, 'users/olderDuplicateUid'), { userId: 'olderDuplicateUid', email: EMAIL, profiles: [] });
    await setDoc(doc(db, `users/${OTHER}`), { userId: OTHER, email: 'other@example.com', profiles: [] });
    await setDoc(doc(db, `activity_feedback/${PARENT}`), { selected_activity: 'colouring' });
  });
});

const stranger = () => env.unauthenticatedContext().firestore();
const parent = (claims = { email: EMAIL, email_verified: true }) => env.authenticatedContext(PARENT, claims).firestore();
const guest = () => env.authenticatedContext('anonUid', { firebase: { sign_in_provider: 'anonymous' } }).firestore();

// ---- A stranger with the public API key (the hole being closed) ----

test('stranger cannot read, list, change or delete user accounts', async () => {
  const db = stranger();
  await assertFails(getDoc(doc(db, `users/${PARENT}`)));
  await assertFails(getDocs(collection(db, 'users')));
  await assertFails(getDoc(doc(db, `users/${PARENT}/devices/device1`)));
  await assertFails(setDoc(doc(db, `users/${PARENT}`), { hacked: true }));
  await assertFails(deleteDoc(doc(db, `users/${PARENT}`)));
  await assertFails(getDoc(doc(db, `activity_feedback/${PARENT}`)));
});

test('stranger cannot change or delete content', async () => {
  const db = stranger();
  await assertFails(setDoc(doc(db, 'chapters/101'), { title: 'replaced' }));
  await assertFails(deleteDoc(doc(db, 'chapters/101')));
  await assertFails(setDoc(doc(db, 'stories/999'), { title: 'injected' }));
  await assertFails(deleteDoc(doc(db, 'badges/barakah_builder')));
});

test('signed-in users cannot change content either', async () => {
  const db = parent();
  await assertFails(setDoc(doc(db, 'chapters/101'), { title: 'replaced' }));
  await assertFails(updateDoc(doc(db, 'badges/barakah_builder'), { title: 'x' }));
});

// ---- Signed out: onboarding preview, Android QA build, lesson downloads ----

test('anyone can read all content collections, including new ones', async () => {
  const db = stranger();
  await assertSucceeds(getDocs(collection(db, 'chapters')));
  await assertSucceeds(getDoc(doc(db, 'chapters/101')));
  await assertSucceeds(getDocs(query(collection(db, 'chapters'), where('id', '==', 101))));
  await assertSucceeds(getDoc(doc(db, 'lessons/level1')));
  await assertSucceeds(getDocs(collection(db, 'badges')));
  for (const c of ['stories', 'prophet_muhammad_stories', 'sahaba_stories', 'women_and_mothers', 'miracles']) {
    await assertSucceeds(getDocs(collection(db, c)));
  }
  await assertSucceeds(getDocs(collection(db, 'companions_stories')));
});

test('private top-level names stay closed even if created later', async () => {
  const db = stranger();
  for (const c of ['devices', 'progress', 'children', 'gifts']) {
    await assertFails(getDocs(collection(db, c)));
  }
});

// ---- Signed-in parent: their own account ----

test('parent can create, read, update and delete their own account', async () => {
  const db = env.authenticatedContext('newParentUid', { email: 'new@example.com', email_verified: false }).firestore();
  await assertSucceeds(setDoc(doc(db, 'users/newParentUid'), { userId: 'newParentUid', email: 'new@example.com', profiles: [] }));
  await assertSucceeds(getDoc(doc(db, 'users/newParentUid')));
  await assertSucceeds(updateDoc(doc(db, 'users/newParentUid'), { timeZone: 'Europe/London' }));
  await assertSucceeds(setDoc(doc(db, 'users/newParentUid'), { userId: 'newParentUid', profiles: [{ childName: 'A' }] })); // full overwrite (iOS add-child, Android ProfileViewModel)
  await assertSucceeds(deleteDoc(doc(db, 'users/newParentUid')));
});

test('lesson completion transaction on own account works', async () => {
  const db = parent();
  await assertSucceeds(runTransaction(db, async (tx) => {
    const ref = doc(db, `users/${PARENT}`);
    const snap = await tx.get(ref);
    tx.update(ref, { profiles: [...(snap.data().profiles || []), { chaptersCompleted: ['101'] }], lastUpdated: Date.now() });
  }));
});

test('parent can use devices, children and progress under their own account', async () => {
  const db = parent();
  await assertSucceeds(setDoc(doc(db, `users/${PARENT}/devices/device2`), { fcmToken: 'x', platform: 'android' }, { merge: true }));
  await assertSucceeds(getDoc(doc(db, `users/${PARENT}/devices/device1`)));
  await assertSucceeds(getDocs(collection(db, `users/${PARENT}/children`)));
  await assertSucceeds(deleteDoc(doc(db, `users/${PARENT}/children/c1`)));
  await assertSucceeds(getDocs(collection(db, `users/${PARENT}/progress`)));
  await assertSucceeds(deleteDoc(doc(db, `users/${PARENT}/progress/p1`)));
});

test('parent can read and write their own activity feedback', async () => {
  const db = parent();
  await assertSucceeds(getDoc(doc(db, `activity_feedback/${PARENT}`)));
  await assertSucceeds(setDoc(doc(db, `activity_feedback/${PARENT}`), { selected_activity: 'tracing', timestamp: 1 }));
});

// ---- Signed-in parent: nobody else's data ----

test('parent cannot read or change another parent', async () => {
  const db = parent();
  await assertFails(getDoc(doc(db, `users/${OTHER}`)));
  await assertFails(updateDoc(doc(db, `users/${OTHER}`), { profiles: [] }));
  await assertFails(deleteDoc(doc(db, `users/${OTHER}`)));
  await assertFails(getDoc(doc(db, `users/${OTHER}/devices/d`)));
  await assertFails(setDoc(doc(db, `activity_feedback/${OTHER}`), { selected_activity: 'x' }));
  await assertFails(getDocs(collection(db, 'users'))); // no unfiltered listing
});

// ---- iOS email login: whereField("email", isEqualTo:) then uses the found doc ----

test('iOS email lookup finds only accounts with your own login email', async () => {
  await assertSucceeds(getDocs(query(collection(parent(), 'users'), where('email', '==', EMAIL))));
  await assertFails(getDocs(query(collection(parent(), 'users'), where('email', '==', 'other@example.com'))));
  await assertFails(getDocs(collection(parent(), 'users')));
});

test('iOS lookup right after verifying works while the token still says unverified', async () => {
  const db = parent({ email: EMAIL, email_verified: false });
  await assertSucceeds(getDocs(query(collection(db, 'users'), where('email', '==', EMAIL))));
  await assertSucceeds(getDoc(doc(db, `users/${PARENT}`)));
});

test('a login without an email (guest, some Apple sign-ins) gets no email-based access', async () => {
  const db = env.authenticatedContext('noEmailUid', {}).firestore();
  await assertFails(getDocs(query(collection(db, 'users'), where('email', '==', EMAIL))));
  await assertFails(getDoc(doc(db, `users/${PARENT}`)));
});

test('parent with two accounts under one email can still open and update the older one', async () => {
  const db = parent();
  await assertSucceeds(getDoc(doc(db, 'users/olderDuplicateUid')));
  await assertSucceeds(updateDoc(doc(db, 'users/olderDuplicateUid'), { timeZone: 'Asia/Kolkata' }));
  await assertFails(deleteDoc(doc(db, 'users/olderDuplicateUid'))); // deletes stay owner-only
});

// ---- iOS guest (anonymous) users ----

test('guest can read content and only their own (possibly missing) account', async () => {
  const db = guest();
  await assertSucceeds(getDocs(collection(db, 'chapters')));
  await assertSucceeds(getDoc(doc(db, 'users/anonUid')));
  await assertSucceeds(setDoc(doc(db, 'activity_feedback/anonUid'), { selected_activity: 'x' }));
  await assertFails(getDoc(doc(db, `users/${PARENT}`)));
});
