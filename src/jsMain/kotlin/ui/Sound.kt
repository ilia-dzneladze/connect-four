package ui

import game.PLAYER_1
import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

private fun audio(src: String): HTMLAudioElement {
    val a = document.createElement("audio") as HTMLAudioElement
    a.src = src
    a.preload = "auto"
    return a
}

private val clink1 = audio("clink1.mp3")
private val clink2 = audio("clink2.mp3")

fun playDropSound(player: Int) {
    val a = if (player == PLAYER_1) clink1 else clink2
    a.currentTime = 0.0
    a.play()
}
