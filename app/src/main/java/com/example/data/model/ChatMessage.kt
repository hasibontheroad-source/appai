package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val sender: String = "user", // "user" or "model"
    val content: String = "",
    val thinkingContent: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
