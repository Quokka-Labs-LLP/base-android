package com.example.baseandroid.features.main.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId")
    val userId :Int?,
    @SerializedName("id")
    val id:Int?,
    @SerializedName("title")
    val title:String?,
    @SerializedName("body")
    val body:String?
)
