package org.agrfesta.sh.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "smart-home-ui",
    ) {
        App()
    }
}