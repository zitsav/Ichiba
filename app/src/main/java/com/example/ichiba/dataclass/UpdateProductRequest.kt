package com.example.ichiba.dataclass

data class UpdateProductRequest(
    val description: String?,
    val name: String?,
    val price: Int?,
    val isSold: Boolean?
)
