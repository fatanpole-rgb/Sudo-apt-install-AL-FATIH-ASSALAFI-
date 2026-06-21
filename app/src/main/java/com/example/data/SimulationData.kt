package com.example.data

import java.util.Date

// Models for Simulation
data class SantriProfile(
    val nis: String,
    val name: String,
    val kelas: String,
    val kamar: String,
    val totalJuzHafalan: Int,
    val progressSelesaiKitab: Int,
    val avatarEmoji: String,
    val bills: List<BillSim>,
    val attendances: List<AttendanceSim>,
    val tahfidzHistory: List<TahfidzSim>,
    val kitabs: List<KitabSim>,
    val permits: List<PermitSim>
)

data class BillSim(
    val invoiceId: String,
    val title: String,
    val type: String, // "SPP", "Uang Makan", "Pembangunan"
    val amount: Long,
    val dueDate: String,
    val status: String, // "BELUM LUNAS", "LUNAS"
    val timestamp: String? = null
)

data class AttendanceSim(
    val id: Long,
    val title: String, // "Kelas Formal", "Diniyah", "Shalat Berjamaah"
    val status: String, // "HADIR", "SAKIT", "IZIN", "ALFA"
    val date: String,
    val notes: String
)

data class TahfidzSim(
    val id: Long,
    val date: String,
    val type: String, // "ZIYADAH" / "MURAJAAH"
    val juz: Int,
    val surah: String,
    val rangeAyat: String,
    val grade: String, // "A" (Sangat Lancar), "B" (Lancar), "C" (Cukup), "D" (Kurang)
    val ustadz: String,
    val notes: String
)

data class KitabSim(
    val id: Long,
    val name: String,
    val bab: String,
    val statusPemahaman: String, // "PAHAM", "CUKUP", "KURANG"
    val attendance: String,
    val date: String
)

data class PermitSim(
    val id: String,
    val requestDate: String,
    val reason: String,
    val dateOut: String,
    val dateBack: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val approver: String?,
    val qrToken: String
)

object SimulationData {
    // Initial seeded database for the interactive simulation
    val initialSantriList = listOf(
        SantriProfile(
            nis = "120401",
            name = "Ahmad Syarifullah Al-Fariqi",
            kelas = "X-B (Madrasah Aliyah)",
            kamar = "Kamar Abu Bakar No. 04",
            totalJuzHafalan = 6,
            progressSelesaiKitab = 65,
            avatarEmoji = "👦",
            bills = listOf(
                BillSim("INV-2026-06-01", "SPP Bulanan Juni 2026", "SPP", 350000, "10 Juni 2026", "BELUM LUNAS"),
                BillSim("INV-2026-06-02", "Uang Pembinaan Keagamaan", "Pembangunan", 100000, "15 Juni 2026", "LUNAS", "05 Juni 2026"),
                BillSim("INV-2026-06-03", "Katering Sehat Juni 2026", "Uang Makan", 300000, "10 Juni 2026", "BELUM LUNAS"),
                BillSim("INV-2026-05-01", "SPP Bulanan Mei 2026", "SPP", 350000, "10 Mei 2026", "LUNAS", "08 Mei 2026")
            ),
            attendances = listOf(
                AttendanceSim(1, "Shalat Subuh Berjamaah", "HADIR", "Hari Ini", "Tepat waktu di shaf depan"),
                AttendanceSim(2, "Kelas Madrasah Diniyah", "HADIR", "Hari Ini", "Sangat aktif bertanya"),
                AttendanceSim(3, "Shalat Dzuhur Berjamaah", "HADIR", "Hari Ini", "Hadir berjamaah"),
                AttendanceSim(4, "Kelas Formal MA (Sains)", "HADIR", "Kemarin", "Mengikuti kuis fisika"),
                AttendanceSim(5, "Shalat Isya Berjamaah", "HADIR", "Kemarin", "Berjamaah di Masjid"),
                AttendanceSim(6, "Shalat Ashar Berjamaah", "IZIN", "3 Hari Lalu", "Membantu bersih-bersih ndalem")
            ),
            tahfidzHistory = listOf(
                TahfidzSim(1, "Hari Ini", "ZIYADAH", 6, "An-Nasr", "Ayat 1-3", "A", "Ustadz H. Abdurrahman", "Makhraj sangat fasih, tajwid sempurna"),
                TahfidzSim(2, "Kemarin", "MURAJAAH", 5, "Al-Mulk", "Ayat 1-15", "B", "Ustadz H. Abdurrahman", "Sedikit ragu di ayat 11, tapi lancar"),
                TahfidzSim(3, "19 Juni 2026", "ZIYADAH", 5, "An-Naba", "Ayat 31-40", "A", "Ustadz H. Abdurrahman", "Sangat siap dan hafalan matang"),
                TahfidzSim(4, "17 Juni 2026", "MURAJAAH", 1, "Al-Baqarah", "Ayat 255-260", "A", "Ustadz H. Abdurrahman", "Murajaah Ayat Kursi lancar")
            ),
            kitabs = listOf(
                KitabSim(1, "Safinatun Najah (Fikh)", "Bab Ketentuan Shalat", "PAHAM", "HADIR", "Kemarin"),
                KitabSim(2, "Ta'limul Muta'allim (Akhlak)", "Adab Belajar di Pondok", "PAHAM", "HADIR", "20 Juni 2026"),
                KitabSim(3, "Tafsir Jalalain", "Surah Al-Buruj", "CUKUP", "HADIR", "18 Juni 2026"),
                KitabSim(4, "Al-Ajurrumiyah (Nahwu)", "Bab I'rab", "KURANG", "HADIR", "15 Juni 2026")
            ),
            permits = listOf(
                PermitSim("PRM-001", "10 Mei 2026", "Acara Pernikahan Kakak Kandung", "14 Mei 2026", "17 Mei 2026", "APPROVED", "K.H. Musthofa Al-Fatih", "PASS_120401_Wedding"),
                PermitSim("PRM-002", "01 April 2026", "Kontrol Gigi Behel Dokter Spesialis", "04 April 2026", "04 April 2026", "APPROVED", "Keamanan (Ustadz Bashir)", "PASS_120401_Doc")
            )
        ),
        SantriProfile(
            nis = "120402",
            name = "Fatimah Az-Zahra Al-Farani",
            kelas = "VIII-A (Madrasah Tsanawiyah)",
            kamar = "Kamar Aisyah No. 12 (Putri)",
            totalJuzHafalan = 12,
            progressSelesaiKitab = 45,
            avatarEmoji = "👧",
            bills = listOf(
                BillSim("INV-2026-06-11", "SPP Bulanan Juni 2026", "SPP", 350000, "10 Juni 2026", "LUNAS", "06 Juni 2026"),
                BillSim("INV-2026-06-12", "Uang Kegiatan Seni Hadrah", "Pembangunan", 150000, "20 Juni 2026", "BELUM LUNAS"),
                BillSim("INV-2026-06-13", "Katering Sehat Juni 2026", "Uang Makan", 300000, "10 Juni 2026", "LUNAS", "06 Juni 2026"),
                BillSim("INV-2026-05-11", "SPP Bulanan Mei 2026", "SPP", 350000, "10 Mei 2026", "LUNAS", "09 Mei 2026")
            ),
            attendances = listOf(
                AttendanceSim(1, "Shalat Subuh Berjamaah", "HADIR", "Hari Ini", "Konsisten di masjid putri"),
                AttendanceSim(2, "Kelas Madrasah Diniyah", "HADIR", "Hari Ini", "Tegas memimpin diskusi kelas"),
                AttendanceSim(3, "Shalat Dzuhur Berjamaah", "HADIR", "Hari Ini", "Hadir tepat waktu"),
                AttendanceSim(4, "Kelas Formal MTs (Matematika)", "HADIR", "Kemarin", "Berhasil menjawab soal di papan"),
                AttendanceSim(5, "Shalat Isya Berjamaah", "SAKIT", "Kemarin", "Izin istirahat di kamar (demam ringan)"),
                AttendanceSim(6, "Shalat Ashar Berjamaah", "HADIR", "3 Hari Lalu", "Tepat waktu")
            ),
            tahfidzHistory = listOf(
                TahfidzSim(1, "Hari Ini", "ZIYADAH", 12, "Yasin", "Ayat 1-20", "A", "Ustadzah Fatmatul Munawwarah", "Lancar, makhraj murni"),
                TahfidzSim(2, "Kemarin", "MURAJAAH", 11, "Ad-Dukhan", "Ayat 1-30", "A", "Ustadzah Fatmatul Munawwarah", "Hafalan super kuat"),
                TahfidzSim(3, "19 Juni 2026", "ZIYADAH", 12, "Al-Mursalat", "Ayat 1-50", "B", "Ustadzah Fatmatul Munawwarah", "Sedikit terbalik di pertengahan"),
                TahfidzSim(4, "16 Juni 2026", "MURAJAAH", 10, "Yunus", "Ayat 10-30", "C", "Ustadzah Fatmatul Munawwarah", "Perlu ditingkatkan kelancarannya")
            ),
            kitabs = listOf(
                KitabSim(1, "Al-Mabadi'ul Fiqhiyah", "Bab Najis dan Cara Mensucikannya", "PAHAM", "HADIR", "Kemarin"),
                KitabSim(2, "Al-Akhlaq Lil Banat", "Adab Bergaul Sesama Putri", "PAHAM", "HADIR", "20 Juni 2026"),
                KitabSim(3, "Amtsilatut Tashriif (Saraf)", "Tashrif Fi'il Tsulatsi", "CUKUP", "HADIR", "17 Juni 2026")
            ),
            permits = listOf(
                PermitSim("PRM-011", "25 Mei 2026", "Ibu Kandung Sakit Keras/Opname", "26 Mei 2026", "29 Mei 2026", "APPROVED", "Bunyai Hj. Al-Fatih", "PASS_120402_Urgent"),
                PermitSim("PRM-012", "01 Juni 2026", "Sakit Gigi Pembengkakan Gusi", "01 Juni 2026", "02 Juni 2026", "APPROVED", "Pengurus Kesejahteraan", "PASS_120402_Teeth")
            )
        )
    )
}
