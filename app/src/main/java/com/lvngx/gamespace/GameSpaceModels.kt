package com.lvngx.gamespace

import android.content.Context
import android.graphics.drawable.Drawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class GameItem(val packageName: String, val appName: String, val icon: Drawable? = null)

object GameSpaceStore {
    private const val PREF = "lvngx_store"
    fun isCrosshairEnabled(c: Context): Boolean = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("crosshair", false)
    fun setCrosshairEnabled(c: Context, e: Boolean) { c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("crosshair", e).apply() }
    fun isAudioBoost(c: Context): Boolean = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("audio", false)
    fun setAudioBoost(c: Context, e: Boolean) { c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("audio", e).apply() }
    fun getMacroCps(c: Context): Int = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("cps", 15)
    fun setMacroCps(c: Context, v: Int) { c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt("cps", v.coerceIn(1, 50)).apply() }
    fun getMacroCoord(c: Context): Pair<Int, Int> = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("mx", 300) to c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("my", 500)
    fun setMacroCoord(c: Context, x: Int, y: Int) { c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt("mx", x).putInt("my", y).apply() }
    fun isMacroLocked(c: Context): Boolean = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("mlock", false)
    fun setMacroLocked(c: Context, l: Boolean) { c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("mlock", l).apply() }
}
