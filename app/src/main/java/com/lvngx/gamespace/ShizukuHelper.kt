package com.lvngx.gamespace

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.DataOutputStream

object ShizukuHelper {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        try {
            if (!hasPermission() && Shizuku.pingBinder()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun executeShellCommand(command: String): Boolean {
        return try {
            if (isShizukuAvailable() && hasPermission()) {
                val process = Shizuku.newProcess(arrayOf("sh"), null, null)
                val outputStream = DataOutputStream(process.outputStream)
                outputStream.writeBytes("$command\n")
                outputStream.flush()
                outputStream.writeBytes("exit\n")
                outputStream.flush()
                process.waitFor()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}