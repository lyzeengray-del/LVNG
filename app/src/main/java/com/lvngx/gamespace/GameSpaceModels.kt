package com.lvngx.gamespace

import android.content.Context
import android.view.Choreographer

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

object PerformanceBooster {
    fun boostMemory(c: Context): Long {
        System.gc()
        return 32L // Dummy freed memory indicator
    }
    fun toggleAudioFx(c: Context, enable: Boolean) {
        // Audio effect toggle wrapper
    }
}

class RealTimeFpsTracker(private val cb: (Int) -> Unit) : Choreographer.FrameCallback {
    private var last = 0L
    private var count = 0
    private var active = false

    fun start() {
        if (active) return
        active = true
        last = System.nanoTime()
        count = 0
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        active = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(t: Long) {
        if (!active) return
        count++
        val diff = t - last
        if (diff >= 500_000_000L) {
            cb(((count * 1_000_000_000L) / diff).toInt().coerceIn(1, 120))
            count = 0
            last = t
        }
        Choreographer.getInstance().postFrameCallback(this)
    }
}
