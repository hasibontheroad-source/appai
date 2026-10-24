const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-codeforg-3fc0583d-d6df-4b27-85ce-350dc702e1d5";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085,
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

test("unauthenticated user cannot read or write projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const projRef = unauthDb.doc(`users/alice/projects/proj1`);

  await assertFails(projRef.get());
  await assertFails(projRef.set({
    userId: "alice",
    title: "Test",
    htmlContent: "<h1>Hi</h1>",
    cssContent: "body { color: red; }",
    jsContent: "console.log('test')",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("authenticated owner can create and read their project", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const projRef = aliceDb.doc(`users/alice/projects/proj1`);

  await assertSucceeds(projRef.set({
    userId: "alice",
    title: "My Portfolio",
    description: "Personal site",
    htmlContent: "<h1>Portfolio</h1>",
    cssContent: "body { margin: 0; }",
    jsContent: "console.log('ready');",
    activeLanguage: "web",
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  await assertSucceeds(projRef.get());
});

test("user cannot read or write another user's project", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const bobDb = testEnv.authenticatedContext("bob").firestore();

  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().doc(`users/alice/projects/proj1`).set({
      userId: "alice",
      title: "Alice Secret",
      htmlContent: "<h1>Alice</h1>",
      cssContent: "",
      jsContent: "",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobsAccessToAlice = bobDb.doc(`users/alice/projects/proj1`);
  await assertFails(bobsAccessToAlice.get());
  await assertFails(bobsAccessToAlice.update({ title: "Hacked" }));
});

test("cannot create project with invalid schema or mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const projRef = aliceDb.doc(`users/alice/projects/proj2`);

  // Mismatched userId
  await assertFails(projRef.set({
    userId: "bob",
    title: "Spoofed",
    htmlContent: "",
    cssContent: "",
    jsContent: "",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});
