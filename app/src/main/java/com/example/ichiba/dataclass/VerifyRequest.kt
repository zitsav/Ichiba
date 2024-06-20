package com.example.ichiba.dataclass

data class VerifyRequest(
    val phoneNumber: String,
    val upiId: String
)