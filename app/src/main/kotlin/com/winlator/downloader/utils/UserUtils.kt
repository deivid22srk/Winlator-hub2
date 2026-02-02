package com.winlator.downloader.utils

import android.content.Context
import java.util.UUID

object UserUtils {
    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString("user_id", userId).apply()
        }
        return userId
    }
}
