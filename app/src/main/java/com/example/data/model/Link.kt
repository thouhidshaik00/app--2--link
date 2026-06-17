package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "links",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Link(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String = "",
    val url: String,
    val iconUrl: String = "",
    val iconName: String = "globe", // globe, twitter, instagram, github, linkedin, youtube, email
    val isActive: Boolean = true,
    val clickCount: Int = 0,
    val position: Int = 0
)
