package com.lvngx.gamespace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFE91E5A),
                    background = Color(0xFF080204),
                    surface = Color(0xFF160609)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF080204)
                ) {
                    var currentScreen by remember { mutableStateOf("home") }

                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onNavigateToOptimize = { currentScreen = "optimize" },
                            onNavigateToActivation = { currentScreen = "activation" },
                            onStartOverlay = {
                                if (!Settings.canDrawOverlays(this)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:$packageName")
                                    )
                                    startActivity(intent)
                                } else {
                                    startService(Intent(this, FloatingPanelService::class.java))
                                }
                            }
                        )
                        "optimize" -> TouchOptimizeScreen(
                            onBack = { currentScreen = "home" }
                        )
                        "activation" -> ActivationScreen(
                            onBack = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToOptimize: () -> Unit,
    onNavigateToActivation: () -> Unit,
    onStartOverlay: () -> Unit
) {
    val tokenCount by remember { mutableStateOf(35) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080204))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF220A10))
                        .border(1.dp, Color(0xFFE91E5A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "LV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(text = "TOUCH OPTIMIZE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "LVNG-X v5.0", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF220A10)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF4A0E1C))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ID: 47240AAB", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160609)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE91E5A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Masuk ke", color = Color.Gray, fontSize = 12.sp)
                    Text(text = "Game Space", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Token: $tokenCount / 300", color = Color(0xFF38BDF8), fontSize = 12.sp)
                }
                Button(
                    onClick = onStartOverlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Buka", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToOptimize,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF220A10)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF4A0E1C))
            ) {
                Text(text = "Tes Sentuh", color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = onNavigateToActivation,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF220A10)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF4A0E1C))
            ) {
                Text(text = "Pusat Aktivasi", color = Color.White, fontSize = 12.sp)
            }
        }

        Text(
            text = "Perpustakaan Game (0/10)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160609)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF4A0E1C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Belum ada game",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tambahkan game favoritmu supaya LVNG-X bisa mengoptimalkannya.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Tambah Game", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TouchOptimizeScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mapping", "Tes Sentuh", "Fix Touch")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080204))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Game Optimize", color = Color.Gray, fontSize = 11.sp)
                Text(text = "Mapping & Layar Sentuh", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "KEMBALI", fontSize = 10.sp, color = Color.White)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) Color(0xFFE91E5A) else Color(0xFF220A10)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selectedTab == index) Color(0xFFE91E5A) else Color(0xFF4A0E1C))
                ) {
                    Text(text = title, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160609)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF4A0E1C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Text(text = "Pengaturan Mapping Tombol", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Atur posisi tombol on-screen untuk responsivitas maksimal.", color = Color.Gray, fontSize = 12.sp)
                    }
                    1 -> {
                        Text(text = "Informasi Layar Perangkat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Refresh Rate: 60 Hz", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        Text(text = "Sampling Sentuh: Responsif", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        Text(text = "Maksimum Jari: 10", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }
                    2 -> {
                        Text(text = "Fix Touch & Kalibrasi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Ronde Ketuk: 1 / 3", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Mulai Ukur Respons", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivationScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080204))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Pusat Aktivasi", color = Color.Gray, fontSize = 11.sp)
                Text(text = "Status Akses Shizuku", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E5A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "KEMBALI", fontSize = 10.sp, color = Color.White)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160609)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE91E5A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "STATUS AKSES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "Terbatas", color = Color(0xFFE91E5A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Aktifkan Shizuku atau Wireless Debugging agar seluruh fitur makro otomatis berjalan sempurna tanpa root.", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF220A10)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4A0E1C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Panduan Lengkap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Langkah aktivasi Shizuku", color = Color.Gray, fontSize = 11.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF220A10)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4A0E1C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Akses Shizuku", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Hubungkan layanan", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
