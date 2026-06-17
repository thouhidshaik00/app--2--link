package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String, // Stored securely for private session simulation
    val username: String, // Unique public handle / slug (e.g. app.com/username)
    val bio: String = "Creative Engineer & Product Designer. Welcome to my personal space!",
    val avatarUrl: String = "",
    val themeGradient: String = "sunset", // sunset, slate, mint, ocean, cosmos
    val layoutStyle: String = "glassmorphic" // minimalist, glassmorphic, brutalist
)
