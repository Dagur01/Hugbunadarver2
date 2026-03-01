package com.example.hugbunadarver2.home

data class Movie(
    val movieId: Long,
    val title: String,
    val genre: String?,
    val ageRating: Int?,
    val duration: Long?,
    val posterBase64: String?
)
