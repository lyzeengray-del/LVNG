package com.lvngx.gamespace

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class MacroButtonService : LifecycleService(), SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val handler = Handler(Looper.getMainLooper())
    private var isMacroRunning = false

    private val clickRunnable = object : Runnable {
        override fun run() {
            if (isMacroRunning) {
                val x = layoutParams.x + 36f
                val y = layoutParams.y + 36f
                ShizukuHelper.tap(x, y)
                val cps = GameSpaceStore.getMacroCps(this@MacroButtonService)
                val interval = (1000L / cps).coerceAtLeast(20L)
                handler.postDelayed(this, interval)
            }
        }
    }

    private fun startMacroClick() {
        if (!isMacroRunning) {
            isMacroRunning = true
            handler.post(clickRunnable)
        }
    }

    private fun stopMacroClick() {
        isMacroRunning = false
        handler.removeCallbacks(clickRunnable)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val (savedX, savedY) = GameSpaceStore.getMacroCoord(this)

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MacroButtonService)
            setViewTreeViewModelStoreOwner(ViewModelStore())

            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFFE91E5A),
                        background = Color(0xFF160609),
                        surface = Color(0xFF220A10)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .alpha(0.85f)
                            .clip(CircleShape)
                            .background(Color(0xFF160609))
                            .border(1.5.dp, Color(0xFFE91E5A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MACRO",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        floatingView?.setOnTouchListener { _, event ->
            val isLocked = GameSpaceStore.isMacroLocked(this)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    if (isLocked) {
                        startMacroClick()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isLocked) {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (dx * dx + dy * dy > 25) {
                            isMoving = true
                        }
                        if (isMoving) {
                            layoutParams.x = initialX + dx
                            layoutParams.y = initialY + dy
                            GameSpaceStore.setMacroCoord(this, layoutParams.x, layoutParams.y)
                            try {
                                windowManager.updateViewLayout(floatingView, layoutParams)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isLocked) {
                        stopMacroClick()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopMacroClick()
        super.onDestroy()
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)
}
