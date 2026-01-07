package fr.axllvy.tarotmeter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin

fun main() = application {
  startKoin { modules(*initKoinModules()) }
  Window(onCloseRequest = ::exitApplication, title = "TarotMeter") { App() }
}
