package com.lvngx.gamespace

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

object ShizukuHelper {
    fun isShizukuInstalled(context: Context): Boolean = runCatching { context.packageManager.getPackageInfo("moe.shizuku.manager", 0) != null }.getOrDefault(false)
    fun isShizukuRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
    fun hasShizukuPermission(): Boolean = runCatching { if (Shizuku.isPreV11()) false else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)
    fun requestPermission(requestCode: Int) { runCatching { if (!Shizuku.isPreV11() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(requestCode) } }
    
    fun executeShellCommand(command: String): Boolean {
        return runCatching {
            if (!isShizukuRunning()) return false
            val p = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            p.waitFor()
            p.exitValue() == 0
        }.getOrDefault(false)
    }
    fun tap(x: Float, y: Float) { executeShellCommand("input tap ${x.toInt()} ${y.toInt()}") }
}
