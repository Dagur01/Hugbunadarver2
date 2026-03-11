package com.example.hugbunadarver2.home

import com.google.gson.annotations.SerializedName

data class Movie(
    val movieId: Long,
    val title: String,
    val genre: String?,
    val ageRating: Int?,
    val duration: Long?,
    val nowShowing: Boolean? = null,
    @SerializedName(value = "moviePicture", alternate = ["image", "posterBase64"])
    val posterBase64: String?
)
