package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class User(
    @PrimaryKey val id: Int = 1, // Single user profile
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val isLoggedIn: Boolean = false,
    val securityEnabled: Boolean = true,
    val loginCount: Int = 1
)
