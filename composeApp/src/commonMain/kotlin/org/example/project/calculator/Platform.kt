package org.example.project.calculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform