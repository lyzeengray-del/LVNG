package com.lvngx.gamespace

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.widget.*

class FloatingPanelService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private var crosshair: View? = null
    private var macroBtn: View? = null
    private var fpsTxt: TextView? = null
    private var fpsTracker: RealTimeFpsTracker? = null
    private var isPanelOpen = false
    private var isMacroSetting = false
    private var curFps = 60
    private var isHolding = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val cNavy = Color.parseColor("#EE060E20")
    private val cCard = Color.parseColor("#FF0D1B38")
    private val cCrimson = Color.parseColor("#FFE91E5A")
    private val cCyan = Color.parseColor("#FF38BDF8")

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundNotification()
        createBubble()
        createMacroButton()
        fpsTracker = RealTimeFpsTracker { curFps = it; fpsTxt?.text = "FPS $it" }.also { it.start() }
        if (GameSpaceStore.isCrosshairEnabled(this)) showCrosshair(true)
        runnable = object : Runnable {
            override fun run() {
                if (!isHolding) return
                if (ShizukuHelper.isShizukuRunning() && ShizukuHelper.hasShizukuPermission()) {
                    macroBtn?.let {
                        val loc = intArrayOf(0, 0); it.getLocationOnScreen(loc)
                        ShizukuHelper.tap(loc[0] + it.width / 2f, loc[1] + it.height / 2f)
                    }
                }
                handler.postDelayed(this, (1000L / GameSpaceStore.getMacroCps(this@FloatingPanelService)).coerceAtLeast(20L))
            }
        }
    }

    private fun startForegroundNotification() {
        val cid = "lvngx_ch"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(cid, "LVNG-X", NotificationManager.IMPORTANCE_LOW))
        }
        val notif = Notification.Builder(this, cid).setContentTitle("LVNG-X Active").setSmallIcon(R.drawable.ic_lvngx_logo).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) startForeground(101, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(101, notif)
    }

    private fun overlayType() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
    private fun bg(color: Int, stroke: Int = Color.TRANSPARENT, r: Float = 16f) = GradientDrawable().apply { setColor(color); cornerRadius = r * resources.displayMetrics.density; if (stroke != Color.TRANSPARENT) setStroke(2, stroke) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubble() {
        val view = ImageView(this).apply {
            setImageResource(R.drawable.ic_lvngx_logo)
            background = bg(cNavy, cCrimson, 18f)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val p = WindowManager.LayoutParams(dp(44), dp(44), overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START; x = dp(12); y = dp(160)
        }
        var sx = 0; var sy = 0; var ix = 0f; var iy = 0f; var moved = false
        view.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = p.x; sy = p.y; ix = e.rawX; iy = e.rawY; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - ix).toInt(); val dy = (e.rawY - iy).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    p.x = sx + dx; p.y = sy + dy; wm.updateViewLayout(view, p); true
                }
                MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); true }
                else -> false
            }
        }
        bubble = view; wm.addView(view, p)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createMacroButton() {
        val (mx, my) = GameSpaceStore.getMacroCoord(this)
        val view = TextView(this).apply {
            text = "MACRO"; setTextColor(Color.WHITE); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            background = bg(Color.argb(215, 6, 14, 32), cCrimson, 24f)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        val p = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START; x = mx; y = my
        }
        var sx = 0; var sy = 0; var ix = 0f; var iy = 0f
        view.setOnTouchListener { _, e ->
            val locked = GameSpaceStore.isMacroLocked(this)
            if (!locked || isMacroSetting) {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { sx = p.x; sy = p.y; ix = e.rawX; iy = e.rawY; true }
                    MotionEvent.ACTION_MOVE -> {
                        p.x = sx + (e.rawX - ix).toInt(); p.y = sy + (e.rawY - iy).toInt()
                        wm.updateViewLayout(view, p); GameSpaceStore.setMacroCoord(this, p.x, p.y); true
                    }
                    else -> false
                }
            } else {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { isHolding = true; handler.post(runnable); view.scaleX = 0.92f; view.scaleY = 0.92f; true }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { isHolding = false; handler.removeCallbacks(runnable); view.scaleX = 1f; view.scaleY = 1f; true }
                    else -> false
                }
            }
        }
        macroBtn = view; wm.addView(view, p)
    }

    private fun togglePanel() { if (isPanelOpen) closePanel() else openPanel() }
    private fun closePanel() { panel?.let { runCatching { wm.removeView(it) } }; panel = null; isPanelOpen = false; isMacroSetting = false }

    @SuppressLint("SetTextI18n")
    private fun openPanel() {
        closePanel(); isPanelOpen = true
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = bg(cNavy, cCrimson, 20f); setPadding(dp(16), dp(14), dp(16), dp(16))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply { text = "LVNG-X GAME SPACE"; setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        header.addView(TextView(this).apply { text = "  X "; setTextColor(cCrimson); textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setOnClickListener { closePanel() } })
        root.addView(header)
        root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)) })

        if (isMacroSetting) {
            val cps = GameSpaceStore.getMacroCps(this)
            val locked = GameSpaceStore.isMacroLocked(this)
            root.addView(TextView(this).apply { text = "PENGATURAN MACRO ($cps CPS)"; setTextColor(Color.WHITE); textSize = 13f; typeface = Typeface.DEFAULT_BOLD })
            root.addView(TextView(this).apply { text = "Geser tombol transparan MACRO di layar, lalu kunci posisinya."; setTextColor(cCyan); textSize = 11f })
            root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)) })
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(Button(this).apply { text = "-"; setOnClickListener { GameSpaceStore.setMacroCps(this@FloatingPanelService, cps - 2); openPanel() } })
            row.addView(TextView(this).apply { text = "$cps Klik/detik"; setTextColor(Color.WHITE); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
            row.addView(Button(this).apply { text = "+"; setOnClickListener { GameSpaceStore.setMacroCps(this@FloatingPanelService, cps + 2); openPanel() } })
            root.addView(row)
            root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)) })
            root.addView(TextView(this).apply {
                text = if (locked) "BUKA KUNCI POSISI" else "SIMPAN DAN KUNCI POSISI"
                setTextColor(Color.WHITE); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
                background = bg(if (locked) Color.parseColor("#FFD97706") else cCrimson, Color.TRANSPARENT, 10f)
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setOnClickListener { GameSpaceStore.setMacroLocked(this@FloatingPanelService, !locked); closePanel() }
            })
        } else {
            val r1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            r1.addView(makeTile("BOOST", "Clear RAM") {
                val freed = PerformanceBooster.boostMemory(this)
                Toast.makeText(this, "Boosted +$freed MB", Toast.LENGTH_SHORT).show()
            })
            r1.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)) })
            var audio = GameSpaceStore.isAudioBoost(this)
            r1.addView(makeTile("AUDIO", if (audio) "FX ON" else "FX OFF") {
                audio = !audio; GameSpaceStore.setAudioBoost(this, audio); PerformanceBooster.toggleAudioFx(this, audio); openPanel()
            })
            r1.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)) })
            r1.addView(makeTile("MACRO", "Hold & Setting") { isMacroSetting = true; openPanel() })
            root.addView(r1)
        }

        val p = WindowManager.LayoutParams(dp(300), WindowManager.LayoutParams.WRAP_CONTENT, overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        panel = root; wm.addView(root, p)
    }

    private fun makeTile(title: String, sub: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = bg(cCard, cCrimson, 12f)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { onClick() }
            addView(TextView(this@FloatingPanelService).apply { text = title; setTextColor(Color.WHITE); textSize = 11f; typeface = Typeface.DEFAULT_BOLD })
            addView(TextView(this@FloatingPanelService).apply { text = sub; setTextColor(cCrimson); textSize = 10f })
        }
    }

    private fun showCrosshair(show: Boolean) {
        crosshair?.let { runCatching { wm.removeView(it) } }; crosshair = null
        if (!show) return
        val v = object : View(this) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cCrimson; style = Paint.Style.STROKE; strokeWidth = 4f }
            val c = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cCyan; style = Paint.Style.FILL }
            override fun onDraw(canvas: Canvas) {
                canvas.drawCircle(width / 2f, height / 2f, 16f, p)
                canvas.drawCircle(width / 2f, height / 2f, 4f, c)
            }
        }
    crosshair = v
        wm.addView(v, WindowManager.LayoutParams(dp(64), dp(64), overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER })
    }

    override fun onDestroy() {
        fpsTracker?.stop()
        bubble?.let { runCatching { wm.removeView(it) } }
        panel?.let { runCatching { wm.removeView(it) } }
        crosshair?.let { runCatching { wm.removeView(it) } }
        macroBtn?.let { runCatching { wm.removeView(it) } }
        super.onDestroy()
    }
    override fun onBind(i: Intent?): IBinder? = null
}
