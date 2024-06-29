package com.example.ichiba.dataclass

data class User(
    val userId: Int,
    val name: String,
    val enrollmentNumber: String,
    val program: String,
    val batchYear: Int,
    val batch: String,
    val upiId: String?,
    val phoneNumber: String?,
    val profilePicture: String?
)