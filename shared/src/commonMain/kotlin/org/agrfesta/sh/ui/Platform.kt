package org.agrfesta.sh.ui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform