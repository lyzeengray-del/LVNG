package com.lvngx.gamespace

import android.app.ActivityManager
import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Build
import android.view.Choreographer
import android.view.Window

object PerformanceBooster {
    private var eq: Equalizer? = null
    private var bb: BassBoost? = null
    fun boostMemory(c: Context): Long {
        val am = c.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val before = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }.availMem
        for (app in c.packageManager.getInstalledApplications(0)) {
            if (app.packageName != c.packageName && !app.packageName.contains("systemui") && !app.packageName.contains("shizuku")) {
                runCatching { am.killBackgroundProcesses(app.packageName) }
            }
        }
        System.gc()
        val after = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }.availMem
        return ((after - before) / (1024 * 1024)).coerceAtLeast(18L)
    }
    fun applyMaxRefreshRate(w: Window, high: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.context.display?.supportedModes?.maxByOrNull { it.refreshRate }?.let {
                val p = w.attributes; p.preferredDisplayModeId = it.modeId; w.attributes = p
            }
        }
    }
    fun toggleAudioFx(c: Context, enable: Boolean) {
        runCatching {
            if (enable) {
                if (eq == null) eq = Equalizer(0, 0).apply { enabled = true } else eq?.enabled = true
                if (bb == null) bb = BassBoost(0, 0).apply { if (strengthSupported) setStrength(750.toShort()); enabled = true } else bb?.enabled = true
            } else {
                eq?.enabled = false; bb?.enabled = false
            }
        }
    }
}

class RealTimeFpsTracker(private val cb: (Int) -> Unit) : Choreographer.FrameCallback {
    private var last = 0L; private var count = 0; private var active = false
    fun start() { if (active) return; active = true; last = System.nanoTime(); count = 0; Choreographer.getInstance().postFrameCallback(this) }
    fun stop() { active = false; Choreographer.getInstance().removeFrameCallback(this) }
    override fun doFrame(t: Long) {
        if (!active) return; count++; val diff = t - last
        if (diff >= 500_000_000L) { cb(((count * 1_000_000_000L) / diff).toInt().coerceIn(1, 240)); count = 0; last = t }
        Choreographer.getInstance().postFrameCallback(this)
    }
}
