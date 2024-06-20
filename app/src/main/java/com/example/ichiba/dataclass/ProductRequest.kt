package com.example.ichiba.dataclass

data class ProductRequest(
    val category: String,
    val description: String,
    val images: List<String>,
    val name: String,
    val price: Int
)