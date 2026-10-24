package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.Project
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProjectRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProjects(): Flow<List<Project>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects"
        emitAll(
            db.collection("users").document(uid).collection("projects")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Project::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeProject(projectId: String): Flow<Project?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects/$projectId"
        emitAll(
            db.collection("users").document(uid).collection("projects").document(projectId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(Project::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.READ, path)
                    throw error
                }
        )
    }

    suspend fun createProject(project: Project): String {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("projects")
        val docRef = if (project.id.isNotBlank()) collectionRef.document(project.id) else collectionRef.document()
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to project.title.ifBlank { "Untitled App" },
            "description" to project.description,
            "htmlContent" to project.htmlContent,
            "cssContent" to project.cssContent,
            "jsContent" to project.jsContent,
            "activeLanguage" to project.activeLanguage,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProject(project: Project) {
        val uid = requireUserId()
        require(project.id.isNotBlank()) { "Project ID must not be blank for update" }
        val docRef = db.collection("users").document(uid).collection("projects").document(project.id)
        try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "title" to project.title.ifBlank { "Untitled App" },
                "description" to project.description,
                "htmlContent" to project.htmlContent,
                "cssContent" to project.cssContent,
                "jsContent" to project.jsContent,
                "activeLanguage" to project.activeLanguage,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            // Preserve createdAt if already present
            if (project.createdAt != null) {
                payload["createdAt"] = project.createdAt
            }
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProject(projectId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("projects").document(projectId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeMessages(projectId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects/$projectId/messages"
        emitAll(
            db.collection("users").document(uid).collection("projects").document(projectId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addMessage(projectId: String, sender: String, content: String, thinking: String? = null) {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("projects").document(projectId).collection("messages")
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "projectId" to projectId,
            "sender" to sender,
            "content" to content,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            collection.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, collection.path)
            throw e
        }
    }
}
