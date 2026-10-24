package com.example.data.repository

import android.util.Log

enum class OperationType {
    CREATE, READ, UPDATE, DELETE, LIST
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String) {
    Log.e(
        "FirestoreError",
        "Firestore error during ${operationType.name} at '$path': ${exception.localizedMessage}",
        exception
    )
}
