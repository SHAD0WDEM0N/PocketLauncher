package com.example.pocketlauncher.library

import android.content.Context
import java.security.MessageDigest

class FavouriteStore(context: Context) {
    private val prefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE)

    fun isFavourite(game: GameEntry): Boolean = prefs.getBoolean(key(game), false)

    fun hasAnyFavourites(): Boolean = prefs.all.values.any { it == true }

    fun setFavourite(game: GameEntry, favourite: Boolean) {
        prefs.edit().putBoolean(key(game), favourite).apply()
    }

    fun toggle(game: GameEntry): Boolean {
        val next = !isFavourite(game)
        setFavourite(game, next)
        return next
    }

    private fun key(game: GameEntry): String {
        val input = "${game.platform.name}|${game.fileName.lowercase()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
