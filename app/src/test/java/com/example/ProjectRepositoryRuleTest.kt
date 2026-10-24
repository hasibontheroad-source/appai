package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Project
import com.example.data.repository.ProjectRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testCreateAndReadProject_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser("alice_test@example.com")
    val repository = ProjectRepository(firestore)

    val project = Project(
      userId = uid,
      title = "Test Web App",
      description = "Test description",
      htmlContent = "<h1>Hello</h1>",
      cssContent = "h1 { color: cyan; }",
      jsContent = "console.log('hi');"
    )

    val docId = repository.createProject(project)
    assertNotNull(docId)

    val projects = repository.observeProjects().first()
    assertTrue(projects.isNotEmpty())
    val found = projects.find { it.id == docId }
    assertNotNull(found)
    assertEquals("Test Web App", found?.title)
  }

  @Test
  fun testUnauthenticated_failsFast() = runBlocking {
    auth.signOut()
    val repository = ProjectRepository(firestore)

    val project = Project(
      userId = "unauth_user",
      title = "Fail",
      htmlContent = "",
      cssContent = "",
      jsContent = ""
    )

    try {
      repository.createProject(project)
      fail("Expected IllegalStateException for unauthenticated write")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("User must be signed in") == true)
    }
  }

  @Test
  fun testCrossUserAccess_rejectedByRules() = runBlocking {
    val aliceUid = signInTestUser("alice_cross@example.com")
    val repositoryAlice = ProjectRepository(firestore)

    val project = Project(
      userId = aliceUid,
      title = "Alice Secret Project",
      htmlContent = "<div>Secret</div>",
      cssContent = "",
      jsContent = ""
    )
    val docId = repositoryAlice.createProject(project)

    // Switch to Bob
    val bobUid = signInTestUser("bob_cross@example.com")
    val repositoryBob = ProjectRepository(firestore)

    // Bob trying to read Alice's project path directly from firestore
    try {
      val doc = firestore.collection("users").document(aliceUid).collection("projects").document(docId).get()
      com.google.android.gms.tasks.Tasks.await(doc)
      fail("Expected PERMISSION_DENIED for Bob accessing Alice's project")
    } catch (e: Exception) {
      val firestoreEx = e.unwrapFirestoreException()
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }
  }
}
