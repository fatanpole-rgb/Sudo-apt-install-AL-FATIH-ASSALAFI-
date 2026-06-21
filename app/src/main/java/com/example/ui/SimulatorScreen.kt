package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimulatorScreen(modifier: Modifier = Modifier) {
    // Biometric Security States
    var isBiometricUnlocked by remember { mutableStateOf(false) }
    var biometricAnalysisState by remember { mutableStateOf("READY") } // READY, SCANNING, SUCCESS
    var biometricProgress by remember { mutableStateOf(0f) }

    // Selected Activity Detail Modal State
    var activeActivityDetailModal by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Selected QR Code fullscreen view State
    var showLargeQrPassInfo by remember { mutableStateOf<PermitSim?>(null) }

    // Shared Simulation State
    val childrenList = remember { mutableStateListOf<SantriProfile>().apply { addAll(SimulationData.initialSantriList) } }
    var selectedChildIndex by remember { mutableStateOf(0) }
    val currentChild = childrenList.getOrNull(selectedChildIndex) ?: childrenList[0]

    // Active sub-tab inside the simulator
    var selectedModuleTab by remember { mutableStateOf(0) }
    val modules = listOf("💰 Keuangan", "📈 Perkembangan", "📅 Kehadiran", "🔑 Surat Izin", "💬 Kabar WA")

    // Modals & Dialog State
    var payingBill by remember { mutableStateOf<BillSim?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("QRIS (Mandiri/Gopay/OVO)") }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }

    // Permit Submission State
    var showNewPermitDialog by remember { mutableStateOf(false) }
    var permitReason by remember { mutableStateOf("") }
    var permitOutDate by remember { mutableStateOf("") }
    var permitInDate by remember { mutableStateOf("") }

    // Recent Notification Logs State
    val notificationLogs = remember {
        mutableStateListOf<String>().apply {
            add("Notification System Initialized: AL-FATIH ASSALAFI BOT active.")
            add("Log: Ahmad Syarifullah Al-Fariqi completed Ziyadah on Juz 6.")
            add("Log: Fatimah Az-Zahra Al-Farani cleared SPP Bill for June 2026.")
        }
    }

    // Interactive Toast Alerts
    var activeToastMessage by remember { mutableStateOf<String?>(null) }

    fun showToast(msg: String) {
        activeToastMessage = msg
        notificationLogs.add(0, "[SYSTEM ALERT] $msg")
    }

    // Render Fullscreen Biometric Lock Screen if locked
    if (!isBiometricUnlocked) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF004D40)) // Modern Deep Green Background
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo shield
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFE0F2F1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🛡️",
                            fontSize = 32.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PROTEKSI BIOMETRIK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF004D40),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Pondok Pesantren Al-Fatih Assalafi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Divider(color = Color(0xFFE0F2F1), thickness = 1.dp)

                    Text(
                        text = "Gunakan sensor sidik jari atau Face ID untuk masuk ke portal wali santri secara aman & rahasia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scanner Fingerprint Visual Area
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                color = when (biometricAnalysisState) {
                                    "SCANNING" -> Color(0xFFE0F2F1)
                                    "SUCCESS" -> Color(0xFFC8E6C9)
                                    else -> Color(0xFFF5F5F5)
                                },
                                shape = CircleShape
                            )
                            .border(
                                2.dp,
                                color = when (biometricAnalysisState) {
                                    "SCANNING" -> Color(0xFF00675B)
                                    "SUCCESS" -> Color(0xFF2E7D32)
                                    else -> Color.LightGray
                                },
                                shape = CircleShape
                            )
                            .clickable(enabled = biometricAnalysisState == "READY") {
                                biometricAnalysisState = "SCANNING"
                                biometricProgress = 0.1f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (biometricAnalysisState == "SCANNING") {
                            CircularProgressIndicator(
                                color = Color(0xFF004D40),
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(90.dp)
                            )
                            // Simulate progressive load on fingerprint UI
                            LaunchedEffect(Unit) {
                                repeat(5) {
                                    kotlinx.coroutines.delay(200)
                                    biometricProgress += 0.2f
                                }
                                biometricAnalysisState = "SUCCESS"
                                kotlinx.coroutines.delay(400)
                                isBiometricUnlocked = true
                                showToast("Autentikasi biometric berhasil. Selamat datang!")
                            }
                        }

                        Text(
                            text = when (biometricAnalysisState) {
                                "SCANNING" -> "🔍"
                                "SUCCESS" -> "✅"
                                else -> "👆"
                            },
                            fontSize = 44.sp
                        )
                    }

                    Text(
                        text = when (biometricAnalysisState) {
                            "SCANNING" -> "Menganalisis sidik jari wali..."
                            "SUCCESS" -> "Selesai! Identitas terverifikasi."
                            else -> "Ketuk logo sidik jari di atas untuk scan"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (biometricAnalysisState) {
                            "SCANNING" -> Color(0xFF00675B)
                            "SUCCESS" -> Color(0xFF2E7D32)
                            else -> Color.Gray
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bypass button for super fast review
                    Button(
                        onClick = {
                            isBiometricUnlocked = true
                            showToast("Masuk via PIN Cadangan.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MASUK DENGAN PIN PORTAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                // Internal tab row representing the module selector
                ScrollableTabRow(
                    selectedTabIndex = selectedModuleTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF004D40),
                    edgePadding = 12.dp,
                    modifier = Modifier.testTag("modules_selector").border(1.dp, Color(0xFFE0F2F1))
                ) {
                    modules.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedModuleTab == idx,
                            onClick = { selectedModuleTab = idx },
                            text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background) // Clean Light Background
            ) {
            // Profile & Child SWITCHING Header (Multi-Anak)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Hero Pesantren Image Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_pesantren_hero),
                            contentDescription = "Pondok Pesantren Al-Fatih",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Pondok Pesantren Al-Fatih Assalafi",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aplikasi Portal Sinergi Orang Tua (Wali Santri)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    // Multi-child account switcher container
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "👥 DAFTAR ANAK (SANTRI):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        childrenList.forEachIndexed { index, child ->
                            val isSelected = selectedChildIndex == index
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedChildIndex = index
                                        showToast("Beralih ke profil santri: ${child.name}")
                                    }
                                    .testTag("switch_child_${index}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = child.avatarEmoji, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = child.name.substringBefore(" "),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "NIS: ${child.nis}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Detailed selected child overview card (Sleek Interface Style)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar section (double border)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentChild.avatarEmoji,
                                    fontSize = 28.sp
                                )
                            }
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentChild.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    // Status pill matching HTML bg-[#E0F2F1] text-[#004D40]
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE0F2F1), RoundedCornerShape(50.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Aktif",
                                            color = Color(0xFF004D40),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Interactive Re-lock triggers
                                    IconButton(
                                        onClick = {
                                            isBiometricUnlocked = false
                                            biometricAnalysisState = "READY"
                                            biometricProgress = 0f
                                            showToast("Aplikasi Terkunci Kembali dengan Biometrik.")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("🔐", fontSize = 16.sp)
                                    }
                                }
                                Text(
                                    text = "Kelas ${currentChild.kelas} • NIS: ${currentChild.nis} • Kamar: ${currentChild.kamar}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Tahfidz: ${currentChild.totalJuzHafalan} Juz", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Progress Kitab: ${currentChild.progressSelesaiKitab}%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SEKETIKA WALI SANTRI MEMBUKA APK: LIVE DAILY PRESENCE WIDGET (7 KEGIATAN UTAMA) ---
            Spacer(modifier = Modifier.height(6.dp))
            LiveDailyAttendancePanel(
                child = currentChild,
                onItemClick = { name, status, note ->
                    activeActivityDetailModal = Triple(name, status, note)
                }
            )

            // Quick Floating Toast message banner inside Simulator
            AnimatedVisibility(visible = activeToastMessage != null) {
                activeToastMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { activeToastMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Module Switcher Body Render
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedModuleTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "ModuleTabAnimation"
                ) { targetModuleTab ->
                    when (targetModuleTab) {
                    0 -> {
                        // Modul Keuangan Card list
                        Column {
                            SectionHeader(emoji = "💰", title = "Daftar Tagihan Keuangan")

                            val df = DecimalFormat("#,###")
                            val unpaidBills = currentChild.bills.filter { it.status == "BELUM LUNAS" }
                            val paidBills = currentChild.bills.filter { it.status == "LUNAS" }

                            Text(
                                text = "Kewajiban tagihan yang belum lunas (outstanding) maupun rincian pembayaran sebelumnya.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Outstanding summary
                            val totalOutstanding = unpaidBills.sumOf { it.amount }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (totalOutstanding > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total Tunggakan",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Rp ${df.format(totalOutstanding)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = if (totalOutstanding > 0) "⚠️ Selesaikan SPP" else "✅ Bebas Tagihan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (totalOutstanding > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Active unpaid list
                            if (unpaidBills.isNotEmpty()) {
                                Text(
                                    text = "Tagihan Jatuh Tempo:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                unpaidBills.forEach { bill ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bill.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Jatuh Tempo: ${bill.dueDate}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Rp ${df.format(bill.amount)}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Button(
                                                onClick = { payingBill = bill },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("pay_button_${bill.invoiceId.takeLast(4)}")
                                            ) {
                                                Text("Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Paid logs list
                            Text(
                                text = "Riwayat Lunas Pembayaran:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (paidBills.isEmpty()) {
                                Text(
                                    text = "Belum ada riwayat pembayaran di pondok.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                paidBills.forEach { bill ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bill.title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Tipe: ${bill.type}  •  Dibayar: ${bill.dueDate}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Rp ${df.format(bill.amount)}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray
                                                )
                                            }
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "LUNAS",
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Modul Akademik & Hafalan (Ziyadah/Murajaah/Kitab)
                        Column {
                            SectionHeader(emoji = "📖", title = "Hafalan Al-Qur'an & Kitab Kuning")

                            WeeklyProgressPanel(childNis = currentChild.nis)

                            // Progress target indicator
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = "Progress Tahfidz",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                             )
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "Juz ${currentChild.totalJuzHafalan} ",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 24.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "/ 30",
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    color = Color.LightGray,
                                                    modifier = Modifier.padding(bottom = 3.dp)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "📖",
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { currentChild.totalJuzHafalan / 30f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Update terakhir: Kemarin (Sinergi Halaqah Ustadz)",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    )
                                }
                            }

                            // Sub-headings tabs inside Akademik
                            Text(
                                text = "Riwayat Halaqah Tahfidz (Harian):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            currentChild.tahfidzHistory.forEach { t ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (t.type == "ZIYADAH") Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = t.type,
                                                    color = if (t.type == "ZIYADAH") Color(0xFF2E7D32) else Color(0xFF1565C0),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Juz ${t.juz} • Surah ${t.surah}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = t.date,
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Ayat: ${t.rangeAyat}   •   Predikat: ${t.grade}",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Notes: \"${t.notes}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            text = "Oleh: ${t.ustadz}",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Buku Pelajaran & Kitab Kuning:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            currentChild.kitabs.forEach { k ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = k.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Bab: ${k.bab}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Penguji: Ustadz M. Syarif   •   ${k.date}",
                                                fontSize = 9.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (k.statusPemahaman) {
                                                    "PAHAM" -> Color(0xFFC8E6C9)
                                                    "CUKUP" -> Color(0xFFFFE082)
                                                    else -> Color(0xFFFFCDD2)
                                                }
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = k.statusPemahaman,
                                                color = when (k.statusPemahaman) {
                                                    "PAHAM" -> Color(0xFF2E7D32)
                                                    "CUKUP" -> Color(0xFFF57F17)
                                                    else -> Color(0xFFC62828)
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Modul Kehadiran logs
                        Column {
                            SectionHeader(emoji = "📅", title = "Log Kehadiran Santri Harian")

                            Text(
                                text = "Buku absen digital yang diinput serentak oleh Ustadz Keamanan & Guru Kelas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Quick counts
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Hadir", currentChild.attendances.count { it.status == "HADIR" }, Color(0xFF2E7D32)),
                                    Triple("Izin", currentChild.attendances.count { it.status == "IZIN" }, Color(0xFFD4AC0D)),
                                    Triple("Sakit", currentChild.attendances.count { it.status == "SAKIT" }, Color(0xFF1565C0)),
                                    Triple("Alfa", currentChild.attendances.count { it.status == "ALFA" }, Color(0xFFC62828))
                                ).forEach { (title, count, color) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = title, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                                            Text(text = count.toString(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            currentChild.attendances.forEach { a ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    color = when (a.status) {
                                                        "HADIR" -> Color(0xFF2E7D32)
                                                        "IZIN" -> Color(0xFFF1C40F)
                                                        "SAKIT" -> Color(0xFF1565C0)
                                                        else -> Color(0xFFC62828)
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = a.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = a.notes,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Tanggal: ${a.date}",
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (a.status) {
                                                    "HADIR" -> Color(0xFFC8E6C9)
                                                    "IZIN" -> Color(0xFFFFE082)
                                                    "SAKIT" -> Color(0xFFBBDEFB)
                                                    else -> Color(0xFFFFCDD2)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = a.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (a.status) {
                                                    "HADIR" -> Color(0xFF2E7D32)
                                                    "IZIN" -> Color(0xFFF57F17)
                                                    "SAKIT" -> Color(0xFF1565C0)
                                                    else -> Color(0xFFC62828)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Modul Perizinan (Izin pulang / Keluar pondok)
                        Column {
                            SectionHeader(emoji = "🔑", title = "Modul Perizinan & QR Gate Pass")

                            Text(
                                text = "Ajukan izin pulang untuk menjemput anak secara digital. Log keluar dicatat otomatis saat penjemputan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Apply button
                            Button(
                                onClick = { showNewPermitDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("apply_permit_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajukan Perizinan Baru", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Daftar Pengajuan:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (currentChild.permits.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "Tidak ada riwayat perizinan aktif.",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                currentChild.permits.forEach { permit ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "ID: ${permit.id}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = when (permit.status) {
                                                            "APPROVED" -> Color(0xFFC8E6C9)
                                                            "PENDING" -> Color(0xFFFFE082)
                                                            else -> Color(0xFFFFCDD2)
                                                        }
                                                    )
                                                ) {
                                                    Text(
                                                        text = permit.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (permit.status) {
                                                            "APPROVED" -> Color(0xFF2E7D32)
                                                            "PENDING" -> Color(0xFFF57F17)
                                                            else -> Color(0xFFC62828)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Alasan: \"${permit.reason}\"",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Keluar: ${permit.dateOut}   •   Kembali: ${permit.dateBack}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            if (permit.approver != null) {
                                                Text(
                                                    text = "Oleh: ${permit.approver}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            if (permit.status == "PENDING") {
                                                // Dynamic simulation tool: click to approve
                                                Button(
                                                    onClick = {
                                                        val uIndex = childrenList.indexOf(currentChild)
                                                        if (uIndex != -1) {
                                                            val updatedPermits = currentChild.permits.map {
                                                                if (it.id == permit.id) it.copy(status = "APPROVED", approver = "KH. Musthofa Al-Fatih (Pengasuh)") else it
                                                            }
                                                            childrenList[uIndex] = currentChild.copy(permits = updatedPermits)
                                                            showToast("Izin disetujui Pengasuh! WhatsApp QR Pass dikirim.")
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AC0D)),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("🔒 Simulasikan Approval Pengasuh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                            } else if (permit.status == "APPROVED") {
                                                // QR Code Generator simulation container with interactive click
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFE0F2F1))
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(1.dp, Color(0xFF00675B), RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            showLargeQrPassInfo = permit
                                                            showToast("Membuka Pas Pengamanan Gerbang Utama...")
                                                        }
                                                        .padding(12.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        // Simulated digital QR Pass representation
                                                        Box(
                                                            modifier = Modifier
                                                                .size(54.dp)
                                                                .background(Color.White)
                                                                .border(2.dp, Color.Black)
                                                                .padding(4.dp)
                                                        ) {
                                                            // Custom drawing a micro QR grid using text or layout
                                                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Box(modifier = Modifier.size(10.dp).background(Color.Black))
                                                                    Box(modifier = Modifier.size(10.dp).background(Color.Black))
                                                                }
                                                                Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                                    Box(modifier = Modifier.size(12.dp).background(Color.Black))
                                                                }
                                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Box(modifier = Modifier.size(10.dp).background(Color.Black))
                                                                    Box(modifier = Modifier.size(6.dp).background(Color.Black))
                                                                }
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Column {
                                                            Text(
                                                                text = "🎫 QR DIGITAL PAS Penjemputan",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Text(
                                                                text = "Tunjukkan QR ke satpam gerbang pondok.",
                                                                fontSize = 9.sp,
                                                                color = Color.Gray
                                                            )
                                                            Text(
                                                                text = "Token: ${permit.qrToken}",
                                                                fontSize = 10.sp,
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                                color = Color.Gray
                                                            )
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
                    4 -> {
                        // Modul 6: Notifikasi Bot Simulator Log
                        Column {
                            SectionHeader(emoji = "🔔", title = "Simulator WhatsApp Bot & FCM Push")

                            Text(
                                text = "Simulasi notifikasi otomatis yang mendarat di HP Orang Tua secara real-time berdasarkan aktivitas pesantren.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Buttons to mock push alert
                            Text(
                                text = "Kirim Uji Coba Trigger Notifikasi (FCM & WhatsApp):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showToast("WA: Halaqah Tahfidz baru ${currentChild.name} berhasil direkam!")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("📝 Hafalan Baru", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        showToast("WA: Peringatan SPP belum dibayar dikirim ke Wali Santri.")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("⚠️ Tagihan Baru", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chat-like view representing WhatsApp messaging
                            Text(
                                text = "Tampilan Pesan WhatsApp Bot (AL-FATIH BOT):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE5DDD5)) // Whatsapp background color
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Chat bubble from Bot
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .widthIn(max = 260.dp)
                                            .background(Color.White, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "🟢 AL-FATIH BOT",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF128C7E)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Assalamualaikum Bapak/Ibu Wali Santri dari ananda ${currentChild.name},\n\nSistem mengonfirmasi rekam baru:\nTipe: ${currentChild.tahfidzHistory.firstOrNull()?.type ?: "Setoran"}\nHafalan: Juz ${currentChild.tahfidzHistory.firstOrNull()?.juz ?: "-"} ${currentChild.tahfidzHistory.firstOrNull()?.surah ?: "-"}\nNilai: ${currentChild.tahfidzHistory.firstOrNull()?.grade ?: "A"}\n\nTerimakasih atas sinergi dan doanya. Wasallam.\nAl-Fatih Assalafi.",
                                                fontSize = 11.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Hari Ini • Bot Official",
                                                fontSize = 8.sp,
                                                color = Color.Gray,
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Chat bubble 2 (Finance Callback notification)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .widthIn(max = 260.dp)
                                            .background(Color.White, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "🟢 AL-FATIH BOT",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF128C7E)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Yth. Wali Santri,\nPembayaran tagihan untuk invoice \"INV-2026-SPP\" nominal Rp 350,000 via VA Mandiri dinyatakan LUNAS.\n\nStatus keuangan anak diperbarui otomatis di sistem portal.",
                                                fontSize = 11.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Kemarin • Bot Official",
                                                fontSize = 8.sp,
                                                color = Color.Gray,
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Log Konsol Push Notifikasi Sistem (FCM):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                ) {
                                    notificationLogs.take(5).forEach { log ->
                                        Text(
                                            text = log,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF00FF00),
                                            lineHeight = 14.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } // closes AnimatedContent
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Bill payment gateway Checkout simulator modal
    payingBill?.let { bill ->
        val df = DecimalFormat("#,###")
        val simulatedVaNumber = when {
            selectedPaymentMethod.contains("BNI") -> "988701${currentChild.nis}46"
            selectedPaymentMethod.contains("BRI") -> "779012${currentChild.nis}77"
            selectedPaymentMethod.contains("BCA") -> "012053${currentChild.nis}92"
            selectedPaymentMethod.contains("QRIS") -> "QRIS-ALFATIH-${currentChild.nis}-SML"
            else -> "861992${currentChild.nis}08"
        }

        Dialog(onDismissRequest = { payingBill = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("checkout_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFF004D40))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💳 GERBANG PEMBAYARAN KETAT MIDTRANS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF004D40),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Nominal Tagihan: Rp ${df.format(bill.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF004D40),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Divider(color = Color(0xFFE0F2F1), modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "PILIH METODE PEMBAYARAN RESMI:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    val paymentMethods = listOf(
                        "QRIS Real-time (Mandiri/Gopay/OVO)",
                        "BNI Syariah Virtual Account",
                        "BRIVA - BRI Syariah Virtual Account",
                        "BCA Virtual Account",
                        "BSI - Bank Syariah Indonesia VA"
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        paymentMethods.forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = method }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPaymentMethod = method },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF004D40))
                                )
                                Text(
                                    text = method, 
                                    fontSize = 11.sp, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Secure Virtual Account Display Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF4F6F6), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔐 KODE VIRTUAL ACCOUNT (${selectedPaymentMethod.substringBefore(" VA").substringBefore(" (")}):",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF004D40)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = simulatedVaNumber,
                                fontSize = 14.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            // Salin button
                            Button(
                                onClick = {
                                    showToast("No. VA $simulatedVaNumber Berhasil Disalin ke Clipboard!")
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("SALIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "*Pastikan nama siswa yang muncul waktu transfer: AL-FATIH - ${currentChild.name}",
                            fontSize = 9.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { payingBill = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Batal", color = Color.DarkGray)
                        }
                        Button(
                            onClick = {
                                // Perform payment simulation update state
                                val activeSantriIndex = childrenList.indexOf(currentChild)
                                if (activeSantriIndex != -1) {
                                    val updatedBills = currentChild.bills.map {
                                        if (it.invoiceId == bill.invoiceId) {
                                            it.copy(status = "LUNAS")
                                        } else it
                                    }
                                    childrenList[activeSantriIndex] = currentChild.copy(bills = updatedBills)
                                }
                                payingBill = null
                                showPaymentSuccessDialog = true
                                showToast("Pembayaran ${bill.title} LUNAS via VA $selectedPaymentMethod!")
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("confirm_payment_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Konfirmasi Pembayaran", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Payment success celebration dialog (Receipt with SHA Hash verification)
    if (showPaymentSuccessDialog) {
        Dialog(onDismissRequest = { showPaymentSuccessDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🛡️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TRANSAKSI TERVERIFIKASI AMAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Pembayaran Rekonsiliasi Sukses!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Divider(color = Color(0xFFE8F5E9), modifier = Modifier.padding(vertical = 8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FBF9), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Siswa:", fontSize = 11.sp, color = Color.Gray)
                            Text(currentChild.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Metode:", fontSize = 11.sp, color = Color.Gray)
                            Text(selectedPaymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Kode Ledger:", fontSize = 11.sp, color = Color.Gray)
                            Text("TXN-ALF-${System.currentTimeMillis() % 1000000}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HASH VERIFICATION (SHA-256):",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "f3a4bf92a10b14c330df702bc90081bf4ef9e0489b37a1f28b492b49",
                            fontSize = 8.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color(0xFF2E7D32),
                            lineHeight = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "WhatsApp notifikasi resmi beserta invoice LUNAS telah dikirim otomatis oleh AL-FATIH BOT.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showPaymentSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selesai & Cetak Bukti", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Apply digital Permit dialog/form
    if (showNewPermitDialog) {
        Dialog(onDismissRequest = { showNewPermitDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_permit_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📝 Pengajuan Izin Santri Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(text = "Nama Santri: ${currentChild.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = permitReason,
                        onValueChange = { permitReason = it },
                        label = { Text("Alasan Izin Pulang (contoh: Acara nikahan, sakit)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_permit_reason"),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = permitOutDate,
                        onValueChange = { permitOutDate = it },
                        label = { Text("Tanggal Keluar (Pulang)") },
                        placeholder = { Text("contoh: 25 Juni 2026") },
                        modifier = Modifier.fillMaxWidth().testTag("input_permit_out"),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = permitInDate,
                        onValueChange = { permitInDate = it },
                        label = { Text("Tanggal Kembali (Kembaill ke Pondok)") },
                        placeholder = { Text("contoh: 28 Juni 2026") },
                        modifier = Modifier.fillMaxWidth().testTag("input_permit_in"),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showNewPermitDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (permitReason.isNotBlank() && permitOutDate.isNotBlank() && permitInDate.isNotBlank()) {
                                    val newP = PermitSim(
                                        id = "PRM-${100 + (currentChild.permits.size * 3 + 7)}",
                                        requestDate = "Hari Ini",
                                        reason = permitReason,
                                        dateOut = permitOutDate,
                                        dateBack = permitInDate,
                                        status = "PENDING",
                                        approver = null,
                                        qrToken = "PASS_${currentChild.nis}_SimulatedToken"
                                    )
                                    val santriIndex = childrenList.indexOf(currentChild)
                                    if (santriIndex != -1) {
                                        val updatedPList = currentChild.permits.toMutableList().apply {
                                            add(0, newP)
                                        }
                                        childrenList[santriIndex] = currentChild.copy(permits = updatedPList)
                                    }
                                    showNewPermitDialog = false
                                    // Reset inputs
                                    permitReason = ""
                                    permitOutDate = ""
                                    permitInDate = ""
                                    showToast("Permohonan Izin BARU disubmit dengan status PENDING!")
                                } else {
                                    showToast("Harap isi semua kolom formulir!")
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("submit_permit_form_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Ajukan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Selected activity details popup
    activeActivityDetailModal?.let { (name, status, note) ->
        Dialog(onDismissRequest = { activeActivityDetailModal = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                border = BorderStroke(2.dp, Color(0xFF004D40))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📋 ABSENSI DIGITAL VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40), letterSpacing = 1.sp)
                    Text(name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Divider(color = Color(0xFFE0F2F1))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = when (status) {
                                    "HADIR" -> Color(0xFFE8F5E9)
                                    "IZIN" -> Color(0xFFE3F2FD)
                                    "SAKIT" -> Color(0xFFFFF8E1)
                                    else -> Color(0xFFFFEBEE)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (status) {
                                "HADIR" -> "STATUS KEHADIRAN: HADIR (✅ Verified)"
                                "IZIN" -> "STATUS KEHADIRAN: IZIN (ℹ️ Resmi)"
                                "SAKIT" -> "STATUS KEHADIRAN: SAKIT (⚠️ Sakit)"
                                else -> "STATUS KEHADIRAN: ALFA (❌ Tanpa Keterangan)"
                            },
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                "HADIR" -> Color(0xFF2E7D32)
                                "IZIN" -> Color(0xFF1565C0)
                                "SAKIT" -> Color(0xFFF57F17)
                                else -> Color(0xFFC62828)
                            },
                            fontSize = 11.sp
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Pengawas / Ustdz:", fontSize = 11.sp, color = Color.Gray)
                            Text("Ust. Muhammad Fauzi, S.Pd.I", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Keterangan Catat:", fontSize = 11.sp, color = Color.Gray)
                            Text(note, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Metrik Kehadiran:", fontSize = 11.sp, color = Color.Gray)
                            Text("Sistem Kehadiran Al-Fatih", fontSize = 11.sp, color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { activeActivityDetailModal = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Fullscreen QR Gate Pass Details popup
    showLargeQrPassInfo?.let { permit ->
        Dialog(onDismissRequest = { showLargeQrPassInfo = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                border = BorderStroke(3.dp, Color(0xFF004D40))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🛡️ PAS RESMI GERBANG DIGITAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Izin Penjemputan Santri",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // QR pattern larger
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White)
                            .border(3.dp, Color(0xFF004D40))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Box(modifier = Modifier.size(24.dp).background(Color.Black))
                                Box(modifier = Modifier.size(24.dp).background(Color.Black))
                            }
                            Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(32.dp).background(Color.Black))
                                    Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color.Black))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Box(modifier = Modifier.size(24.dp).background(Color.Black))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Black))
                                    Box(modifier = Modifier.size(12.dp).background(Color.Black))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "TOKEN: ${permit.qrToken}",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Divider(color = Color(0xFFE0F2F1))

                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Santri: ${currentChild.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "Alasan: ${permit.reason}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "TanggalKeluar: ${permit.dateOut}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "TanggalKembali: ${permit.dateBack}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "Status Pass: AKTIF & VALID PENGASUH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Text(
                        text = "Scan QR ini pada scanner pos satpam di gerbang utama pesantren saat menjemput santri.",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )

                    Button(
                        onClick = { showLargeQrPassInfo = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
}

@Composable
fun SectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF004D40)
        )
    }
}

@Composable
fun LiveDailyAttendancePanel(
    child: SantriProfile,
    onItemClick: (String, String, String) -> Unit
) {
    val activities = listOf(
        Triple("Sholat Jamaah", "🕌", getStatusForActivity(child.nis, "sholat")),
        Triple("Belajar Malam", "🌙", getStatusForActivity(child.nis, "belajar")),
        Triple("Ngaji Kitab Kuning", "📖", getStatusForActivity(child.nis, "kitab")),
        Triple("Kajian Al-Miftah", "🛡️", getStatusForActivity(child.nis, "almiftah")),
        Triple("Sekolah Formal", "🏫", getStatusForActivity(child.nis, "formal")),
        Triple("Sekolah Diniyyah", "🕌", getStatusForActivity(child.nis, "diniyah")),
        Triple("Kegiatan Qur'ani", "🎯", getStatusForActivity(child.nis, "qurani"))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0F2F1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🟢 KEGIATAN HARI INI (REAL-TIME)",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF004D40),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Seketika wali santri membuka untuk pantau absensi utama",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE AKTIF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activities.forEach { (name, emoji, statusDetail) ->
                    val (status, note, time) = statusDetail
                    val badgeColor = when (status) {
                        "HADIR" -> Color(0xFFE8F5E9)
                        "IZIN" -> Color(0xFFE3F2FD)
                        "SAKIT" -> Color(0xFFFFF8E1)
                        else -> Color(0xFFFFEBEE)
                    }
                    val textColor = when (status) {
                        "HADIR" -> Color(0xFF2E7D32)
                        "IZIN" -> Color(0xFF1565C0)
                        "SAKIT" -> Color(0xFFF57F17)
                        else -> Color(0xFFC62828)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAF9), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFEAEAEA), RoundedCornerShape(16.dp))
                            .clickable { onItemClick(name, status, note) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE0F2F1), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = if (time == "-") note else "$note • Pukul $time",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = badgeColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressPanel(childNis: String) {
    val isAhmad = childNis == "120401"
    
    val totalZiyadah = if (isAhmad) "3 Kali Setoran" else "4 Kali Setoran"
    val ziyadahGrade = "Sangat Lancar (A)"
    
    val totalMurajaah = if (isAhmad) "4 Kali Murajaah" else "3 Kali Murajaah"
    val murajaahGrade = if (isAhmad) "Lancar (B)" else "Sangat Lancar (A)"
    
    val kitabProgress = if (isAhmad) "Paham 2 Bab baru Kitab Safinatun Najah (Shalat)" else "Paham Bab Najis & Cara Mensucikan (Kitab Al-Mabadi')"
    val attendanceRate = if (isAhmad) "Hadir 100% penuh (Kemajuan subuh & kelas)" else "Hadir 92% (Izin sakit demam ringan selama 1 hari)"
    
    val ustadzNote = if (isAhmad) 
        "Ahmad sangat rajin seminggu ini. Dia konsisten di barisan depan shalat berjamaah dan hafalan barunya lancar matang."
    else 
        "Fatimah aktif memimpin ulasan materi adab bergaul antarsantri putri. Motivasi belajarnya sangat tinggi menyenangkan."
    
    val ustadzName = if (isAhmad) "Sayyid H. Abdurrahman" else "Ustadzah Fatmatul Munawwarah"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)), // soft green
        border = BorderStroke(1.dp, Color(0xFF80CBC4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📈", fontSize = 20.sp)
                Column {
                    Text(
                        text = "Laporan Perkembangan Mingguan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF004D40)
                    )
                    Text(
                        text = "Catatan santri selama 1 minggu terakhir",
                        fontSize = 10.sp,
                        color = Color(0xFF00796B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFB2DFDB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("📚 Setor Baru (Ziyadah)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = totalZiyadah, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF004D40))
                        Text(text = ziyadahGrade, fontSize = 10.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFB2DFDB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("🔄 Mengulang (Murajaah)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = totalMurajaah, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF004D40))
                        Text(text = murajaahGrade, fontSize = 10.sp, color = Color(0xFF00796B), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("📖", fontSize = 14.sp)
                        Column {
                            Text("Mengaji Kitab Kuning", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(text = kitabProgress, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        }
                    }
                    
                    Divider(color = Color(0xFFE0F2F1), thickness = 1.dp)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🕌", fontSize = 14.sp)
                        Column {
                            Text("Absensi Sekolah & Shalat", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(text = attendanceRate, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFDCEDC8))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💬 Catatan Pembimbing ($ustadzName):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33691E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$ustadzNote\"",
                        fontSize = 11.sp,
                        color = Color(0xFF1B5E20),
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SmartAiAdvisorPanel(childNis = childNis)
        }
    }
}

@Composable
fun SmartAiAdvisorPanel(childNis: String) {
    val isAhmad = childNis == "120401"
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var showAiAnalysis by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Button(
            onClick = {
                scope.launch {
                    isAnalyzing = true
                    showAiAnalysis = false
                    delay(1200)
                    isAnalyzing = false
                    showAiAnalysis = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🤖 Analisis Cerdas & Saran AI Wali",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isAnalyzing) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF80CBC4))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF004D40),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI sedang menganalisis pola belajar & adab santri...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF004D40)
                    )
                }
            }
        }

        if (showAiAnalysis) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FBF9)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Text(
                            text = "Rekomendasi Cerdas AI Al-Fatih",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF004D40)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAhmad) {
                        AiRecommendationItem(
                            title = "Akselerasi Memori Auditorial",
                            description = "Ahmad memiliki kecepatan serap hafalan yang tinggi ketika mendengarkan bacaan murottal berulang-ulang."
                        )
                        Divider(color = Color(0xFFE0F2F1), modifier = Modifier.padding(vertical = 6.dp))
                        AiRecommendationItem(
                            title = "Saran Pendampingan Rumah",
                            description = "Putar murattal Syaikh Mishary Rasyid sebelum tidur saat di rumah untuk memantapkan fashohah & tajwid."
                        )
                        Divider(color = Color(0xFFE0F2F1), modifier = Modifier.padding(vertical = 6.dp))
                        AiRecommendationItem(
                            title = "Apresiasi & Motivasi",
                            description = "Berikan hadiah buku kisah teladan sahabat nabi agar semangat rajin shalat barisan depannya konsisten terjaga."
                        )
                    } else {
                        AiRecommendationItem(
                            title = "Kecerdasan Sosial & Adab",
                            description = "Fatimah sangat aktif memimpin ulasan adab harian dengan santri putri lain secara percaya diri."
                        )
                        Divider(color = Color(0xFFE0F2F1), modifier = Modifier.padding(vertical = 6.dp))
                        AiRecommendationItem(
                            title = "Saran Pendampingan Rumah",
                            description = "Ajak Fatimah untuk mengajarkan poin-poin adab praktis yang dipelajari kepada adik-adiknya di rumah saat masa liburan."
                        )
                        Divider(color = Color(0xFFE0F2F1), modifier = Modifier.padding(vertical = 6.dp))
                        AiRecommendationItem(
                            title = "Bimbingan Studi Kognitif",
                            description = "Latih merangkum ulasan materi kitab kuning di sebuah jurnal cantik guna menstabilkan struktur pemahaman hadits."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiRecommendationItem(title: String, description: String) {
    Column {
        Text(
            text = "• $title",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF004D40)
        )
        Text(
            text = description,
            fontSize = 10.5.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

fun getStatusForActivity(nis: String, type: String): Triple<String, String, String> {
    return if (nis == "120401") {
        // Ahmad Syarifullah Al-Fariqi
        when (type) {
            "sholat" -> Triple("HADIR", "Masjid Sunan Ampel (Saf 1)", "04:35")
            "belajar" -> Triple("HADIR", "Muthala'ah Mandiri Aula", "20:00")
            "kitab" -> Triple("HADIR", "Fathul Qorib Bab Shalat", "18:45")
            "almiftah" -> Triple("HADIR", "Tasrifan Tsulatsi Jilid 2", "16:00")
            "formal" -> Triple("HADIR", "Kelas Class X-MA (Sains)", "07:30")
            "diniyah" -> Triple("HADIR", "Madrasah Wustho A-2", "14:15")
            "qurani" -> Triple("HADIR", "Setor Ziyadah Juz 6", "05:45")
            else -> Triple("HADIR", "-", "-")
        }
    } else {
        // Fatimah Az-Zahra Al-Farani
        when (type) {
            "sholat" -> Triple("HADIR", "Masjid Khadijah (Jama'ah Putri)", "04:40")
            "belajar" -> Triple("SAKIT", "Izin istirahat di Kamar Aisyah", "-")
            "kitab" -> Triple("HADIR", "Mabadi Fiqih Juz 1", "18:50")
            "almiftah" -> Triple("HADIR", "Nadhom Al-Miftah Bait 1-20", "16:10")
            "formal" -> Triple("HADIR", "Kelas VIII-A MTs", "07:30")
            "diniyah" -> Triple("HADIR", "Madrasah Ula B-12", "14:20")
            "qurani" -> Triple("IZIN", "Sedang Udzur Syar'i (Murojaah)", "-")
            else -> Triple("HADIR", "-", "-")
        }
    }
}

