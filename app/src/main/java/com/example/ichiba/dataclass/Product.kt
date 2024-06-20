package com.example.ichiba.dataclass

data class Product(
    val productId: Int,
    val name: String,
    val description: String,
    val owner: User,
    val ownerId: Int,
    val category: Category,
    val price: Float,
    val images: List<String>,
    val isSold: Boolean,
    val createdAt: String
)