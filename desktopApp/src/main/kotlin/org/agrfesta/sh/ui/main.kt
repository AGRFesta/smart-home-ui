package org.agrfesta.sh.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.agrfesta.sh.ui.home.HomeScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Pikesta",
    ) {
        HomeScreen()
    }
}