package com.alifba.alifba.features.authentication.model

data class ChildProfile(
    val parentName: String,
    val childName: String,
    val age: Int,
    val avatarUrl: String,
    val userId: String
)
