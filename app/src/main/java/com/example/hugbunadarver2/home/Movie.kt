package com.example.hugbunadarver2.home

data class Movie(
    val id: Int,
    val title: String,
    val genre: String?,
    val ageRating: String?,
    val duration: Int?,
    val posterBase64: String?
)
