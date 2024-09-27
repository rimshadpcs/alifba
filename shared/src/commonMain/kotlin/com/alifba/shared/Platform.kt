package com.alifba.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform