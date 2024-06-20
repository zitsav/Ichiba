package com.example.ichiba.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time

@Entity(tableName = "auth_token")
data class AuthToken(
    @PrimaryKey val id: Int = 1,
    val token: String?,
    val expirationTime: Time,
    val userId: Int?,
    val name: String?,
    val enrollmentNumber: String?,
    val isVerified: Boolean?,
    val program: String?,
    val batchYear: Int?,
    val batch: String?,
    val upiId: String?,
    val phoneNumber: String?,
    val profilePicture: String?
)