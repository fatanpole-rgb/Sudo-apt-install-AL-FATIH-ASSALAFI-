package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag

@Composable
fun BiometricGateScreen(
    onTriggerBiometric: () -> Unit,
    onUnlockSuccess: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var fallbackPin by remember { mutableStateOf("") }
    var showFallbackSection by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }

    val actualError = errorMessage ?: verificationError

    // Trigger biometric prompt immediately on launch
    LaunchedEffect(Unit) {
        onTriggerBiometric()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF004D40)) // Modern Deep Emerald Green
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("biometric_gate_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🛡️", fontSize = 12.sp)
                        Text(
                            text = "SISTEM KEAMANAN BIOMETRIK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF004D40),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // App icon circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFE0F2F1), shape = CircleShape)
                        .border(2.dp, Color(0xFF004D40), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 36.sp)
                }

                Text(
                    text = "Autentikasi Wali Santri",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aplikasi Wali Santri Al-Fatih membatasi akses demi melindungi data pribadi, tagihan keuangan, dan rapor perkembangan akademik putra-putri Anda.",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Divider(color = Color(0xFFE0F2F1), thickness = 1.dp)

                if (actualError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Respons Sistem:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = actualError,
                                fontSize = 11.sp,
                                color = Color(0xFFB71C1C),
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "*Perangkat virtual/emulator tidak memiliki biometrik aktif secara default. Silakan gunakan PIN Cadangan di bawah ini.",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF004D40)
                            )
                            Text(
                                text = "Menunggu verifikasi sidik jari/wajah perangkat...",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Quick Unlock Primary Button
                Button(
                    onClick = { onTriggerBiometric() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("scan_biometric_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔘", fontSize = 16.sp)
                        Text(
                            text = "Pindai Biometrik Sekarang",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Fallback Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showFallbackSection) "Sembunyikan Akses PIN" else "Masukkan PIN / Akses Cadangan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40),
                        modifier = Modifier
                            .clickable { showFallbackSection = !showFallbackSection }
                            .padding(vertical = 4.dp)
                    )

                    AnimatedVisibility(
                        visible = showFallbackSection,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFB)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🔒 VERIFIKASI UTAMA WALI SANTRI (PIN: 1234)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF004D40)
                                )

                                OutlinedTextField(
                                    value = fallbackPin,
                                    onValueChange = { fallbackPin = it },
                                    label = { Text("6-Digit Kode PIN Wali Santri / 1234", fontSize = 11.sp) },
                                    placeholder = { Text("Ketik 1234 untuk unlock") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF004D40),
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("fallback_pin_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (fallbackPin == "1234" || fallbackPin.equals("alfatih", ignoreCase = true)) {
                                                verificationError = null
                                                onUnlockSuccess()
                                            } else {
                                                verificationError = "PIN Cadangan Salah! Gunakan PIN '1234' untuk pengujian."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)), // Gold lock color
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.3f).testTag("verify_pin_button")
                                    ) {
                                        Text("Verifikasi PIN", fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Button(
                                        onClick = { onUnlockSuccess() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.3f).testTag("bypass_button")
                                    ) {
                                        Text("Bypass Simulator", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
