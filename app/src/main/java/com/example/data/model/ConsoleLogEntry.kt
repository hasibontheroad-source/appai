package com.example.data.model

enum class LogLevel {
    LOG, INFO, WARN, ERROR
}

data class ConsoleLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: LogLevel = LogLevel.LOG,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
