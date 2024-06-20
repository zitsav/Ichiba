package com.example.ichiba.dataclass

data class SearchRequest(
    val category: String,
    val order: String,
    val search: String,
    val sortBy: String
)