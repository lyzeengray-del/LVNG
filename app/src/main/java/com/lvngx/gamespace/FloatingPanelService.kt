package com.lvngx.gamespace

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class FloatingPanelService : LifecycleService(), SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingPanelService)
            setViewTreeViewModelStoreOwner(ViewModelStore())
            setViewTreeSavedStateRegistryOwner(this@FloatingPanelService)

            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFFE91E5A),
                        background = Color(0xFF160609),
                        surface = Color(0xFF220A10)
                    )
                ) {
                    FloatingControlPanel(
                        onClose = { stopSelf() },
                        onToggleMacroButton = { startService(Intent(this@FloatingPanelService, MacroButtonService::class.java)) }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 100
        }

        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun FloatingControlPanel(onClose: () -> Unit, onToggleMacroButton: () -> Unit) {
    var performanceMode by remember { mutableStateOf(GameSpaceState.performanceMode) }
    var crosshairEnabled by remember { mutableStateOf(GameSpaceState.isCrosshairEnabled) }
    var macroCps by remember { mutableStateOf(GameSpaceState.macroCps.toFloat()) }
    var isLocked by remember { mutableStateOf(GameSpaceState.isMacroLocked) }

    Box(
        modifier = Modifier
            .width(310.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF160609))
            .border(1.dp, Color(0xFFE91E5A), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LVNG-X CONTROL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(text = "TUTUP", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color(0xFF4A0E1C), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "FPS: 60", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = "RAM: 42%", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = "CPU: 39 C", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Text(text = "Mode Performa", color = Color.Gray, fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Eco", "Balanced", "Ultra").forEach { mode ->
                    Button(
                        onClick = {
                            performanceMode = mode
                            GameSpaceState.performanceMode = mode
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (performanceMode == mode) Color(0xFFE91E5A) else Color(0xFF220A10)
                        ),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(text = mode, fontSize = 10.sp, color = Color.White)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Virtual Crosshair", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = crosshairEnabled,
                    onCheckedChange = {
                        crosshairEnabled = it
                        GameSpaceState.isCrosshairEnabled = it
                    }
                )
            }

            Text(text = "Kecepatan Macro: \${macroCps.toInt()} CPS", color = Color.Gray, fontSize = 11.sp)
            Slider(
                value = macroCps,
                onValueChange = {
                    macroCps = it
                    GameSpaceState.macroCps = it.toInt()
                },
                valueRange = 1f..50f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Kunci Posisi Tombol", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = isLocked,
                    onCheckedChange = {
                        isLocked = it
                        GameSpaceState.isMacroLocked = it
                    }
                )
            }

            Button(
                onClick = onToggleMacroButton,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "AKTIFKAN TOMBOL MACRO", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}