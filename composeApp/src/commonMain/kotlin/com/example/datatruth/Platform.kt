package com.example.datatruth

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform