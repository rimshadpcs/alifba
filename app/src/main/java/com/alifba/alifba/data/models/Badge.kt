package com.alifba.alifba.data.models

data class Badge(
    val id: String = "",
    val title: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val criteria: Criteria = Criteria()
)

data class Criteria(
    val type: String = "",
    val count: Int? = null
)
