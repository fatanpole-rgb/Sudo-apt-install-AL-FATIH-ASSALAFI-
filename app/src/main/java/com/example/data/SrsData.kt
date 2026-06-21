package com.example.data

data class SrsSection(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val content: String,
    val userFlow: List<String>,
    val tableSchema: String
)

object SrsData {
    val sections = listOf(
        SrsSection(
            title = "1. Profil & Autentikasi",
            subtitle = "Sistem login aman dan pengalihan profil multi-anak",
            iconName = "person",
            content = """
                Modul ini mengatur siklus hidup akses pengguna (Wali Santri). Karena wali santri sering kali memiliki lebih dari satu anak (multi-anak) di satu pesantren, sistem dirancang agar wali santri tidak perlu melakukan logout untuk memantau data anak yang berbeda. Mereka cukup menekan menu switcher anak untuk mengganti identitas santri yang dipantau.
                
                Ketentuan Utama:
                • Login menggunakan nomor HP/WhatsApp terdaftar atau ID Santri (NIS) dan password.
                • Autentikasi OTP WhatsApp opsional untuk login pertama kali sebelum setup password.
                • Sesi tetap aktif secara offline untuk data terenkripsi lokal (Caching).
                • Dropdown profile swicther yang mendeteksi relasi wali_id ke baris tabel santri secara real-time.
            """.trimIndent(),
            userFlow = listOf(
                "Wali Santri membuka aplikasi Mobile/Web AL-FATIH ASSALAFI.",
                "Sistem meminta input Nomor HP/WhatsApp / NIS dan Password.",
                "Setelah login sukses, sistem memanggil API Auth `/api/auth/me` untuk mendapatkan wali_id.",
                "Mengambil daftar anak terelasi via API `/api/parents/children`.",
                "Wali Santri masuk ke Dashboard dengan data anak pertama secara default.",
                "Untuk beralih anak, klik avatar anak di pojok kanan atas, pilih nama anak lainnya.",
                "Sistem mereload state Dashboard dengan data anak terpilih tanpa menghapus session token."
            ),
            tableSchema = """
                CREATE TABLE wali_santri (
                    id VARCHAR(50) PRIMARY KEY,
                    nama VARCHAR(100) NOT NULL,
                    no_whatsapp VARCHAR(20) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    fcm_token VARCHAR(255), -- untuk push notification
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE santri (
                    nis VARCHAR(20) PRIMARY KEY,
                    nama VARCHAR(100) NOT NULL,
                    kelas VARCHAR(20) NOT NULL,
                    kamar VARCHAR(30) NOT NULL,
                    wali_id VARCHAR(50) NOT NULL,
                    avatar_url VARCHAR(255),
                    FOREIGN KEY (wali_id) REFERENCES wali_santri(id) ON DELETE CASCADE
                );
            """.trimIndent()
        ),
        SrsSection(
            title = "2. Riwayat Keuangan",
            subtitle = "Tagihan SPP bulanan, uang makan, dan simulasi e-wallet",
            iconName = "payment",
            content = """
                Portal keuangan mandiri yang transparan untuk mencatat kewajiban syahriah (SPP bulanan), pembangunan, catering/uang makan, dan agenda insidentil. Wali santri dapat memantau status secara instan dan membayar tagihan langsung via Payment Gateway.
                
                Ketentuan Utama:
                • Tagihan dikelompokkan menjadi: 'Belum Lunas' (Outstanding) dan 'Lunas'.
                • Setiap riwayat memiliki rincian invoice nominal rupiah, tanggal jatuh tempo, dan deskripsi tujuan.
                • Integrasi Payment Gateway (seperti Midtrans/Xendit) untuk pembuatan Virtual Account (VA) otomatis atau kode QR (GOPAY, OVO, ShopeePay).
                • Rekonsiliasi instan via callback Webhook dari Gateway untuk status perubahan real-time.
            """.trimIndent(),
            userFlow = listOf(
                "Wali Santri mengklik menu atau tab 'Keuangan'.",
                "Sistem memproses request ke `/api/billing?nis={nis}`.",
                "Dashboard menghitung sisa tunggakan dan mengelompokkan invoice.",
                "Wali Santri memilih salah satu tagihan yang berstatus 'Belum Bayar' dan menekan tombol 'Bayar Sekarang'.",
                "Sistem memunculkan dialog metode pembayaran (Pilih bank VA atau modern QR/E-Wallet).",
                "Wali Santri melakukan transfer simulasi.",
                "Setelah konfirmasi pembayaran, API menerima webhook dari gateway, mengubah status menjadi 'Lunas', dan memicu Push Notification ke HP orang tua."
            ),
            tableSchema = """
                CREATE TABLE tagihan (
                    invoice_id VARCHAR(50) PRIMARY KEY,
                    nis VARCHAR(20) NOT NULL,
                    nama_tagihan VARCHAR(100) NOT NULL,
                    tipe_tagihan VARCHAR(30) NOT NULL, -- e.g., 'SPP', 'Uang Makan', 'Seragam'
                    nominal DECIMAL(12, 2) NOT NULL,
                    jatuh_tempo DATE NOT NULL,
                    status_pembayaran VARCHAR(20) DEFAULT 'BELUM LUNAS', -- 'BELUM LUNAS', 'LUNAS', 'PENDING'
                    tanggal_bayar TIMESTAMP NULL,
                    va_number VARCHAR(50) NULL,
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );
            """.trimIndent()
        ),
        SrsSection(
            title = "3. Input Kehadiran & Kegiatan",
            subtitle = "Presensi kelas formal, diniyah, shalat berjamaah, & jurnal harian",
            iconName = "event",
            content = """
                Modul presensi dirancang untuk memberikan ketenangan pikiran kepada orang tua mengenai kehadiran dan kepatuhan anak. Guru/Ustadz pengampu di pondok menginput presensi melalui panel kepengasuhan, yang langsung tersinkronisasi ke aplikasi wali santri secara real-time.
                
                Ketentuan Utama:
                • Presensi mencakup 3 pilar: Sekolah Formal, Madrasah Diniyah (Keagamaan), dan Shalat Berjamaah 5 Waktu.
                • Status kehadiran: HADIR, SAKIT, IZIN, ALFA.
                • Jurnal agenda memuat daftar kegiatan harian terintegrasi, misalnya kegiatan gotong-royong, pengajian umum, atau ekstrakurikuler santri.
            """.trimIndent(),
            userFlow = listOf(
                "Ustadz/Asatidzah melakukan scan presensi santri atau input manual di aplikasi Admin.",
                "Sistem menyimpan data dan menghitung statistika kehadiran bulanan santri.",
                "Wali Santri di rumah membuka menu 'Kehadiran' di aplikasinya.",
                "Aplikasi memanggil endpoint `/api/presence?nis={nis}`.",
                "Sistem menyajikan visualisasi presentase kehadiran, detail status per shalat lima waktu hari ini, dan kalender kegiatan bulanan santri."
            ),
            tableSchema = """
                CREATE TABLE kehadiran (
                    id BIGSERIAL PRIMARY KEY,
                    nis VARCHAR(20) NOT NULL,
                    kategori VARCHAR(30) NOT NULL, -- 'FORMAL', 'DINIYAH', 'SHALAT_SUBUH', 'SHALAT_DZUHUR', etc.
                    tanggal DATE NOT NULL,
                    status VARCHAR(15) NOT NULL, -- 'HADIR', 'SAKIT', 'IZIN', 'ALFA'
                    catatan TEXT NULL,
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );

                CREATE TABLE jurnal_kegiatan (
                    id BIGSERIAL PRIMARY KEY,
                    nis VARCHAR(20) NOT NULL,
                    nama_kegiatan VARCHAR(100) NOT NULL,
                    waktu_mulai TIMESTAMP NOT NULL,
                    waktu_selesai TIMESTAMP NOT NULL,
                    deskripsi TEXT,
                    petugas_pengawas VARCHAR(100),
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );
            """.trimIndent()
        ),
        SrsSection(
            title = "4. Akademik & Keagamaan",
            subtitle = "Kemajuan Hafalan Al-Qur'an (Ziyadah/Murajaah) & Pengajian Kitab",
            iconName = "school",
            content = """
                Merupakan jantung perkembangan santri. Modul ini secara rinci melacak setoran hafalan harian (Tabungan Ziyadah & Murajaah) serta kurikulum pengajian kitab kuning klasik (fikh, akhlak, nahwu, dll) yang berjalan di Pondok Pesantren Al-Fatih Assalafi.
                
                Ketentuan Utama:
                • Hafalan Al-Qur'an: Mencakup penanda Ziyadah (hafalan baru) dan Murajaah (pengulangan). Informasi mencantumkan nomor Juz, nama Surah, ayat mulai-selesai, nilai kelancaran (A/B/C/D), dan catatan khusus ustadz tahfidz.
                • Pengajian Kitab: Meliputi nama Kitab (misal: Safinatun Najah, Riyadhus Shalihin), Bab yang dipelajari hari ini, status pemahaman santri, dan keaktifan.
            """.trimIndent(),
            userFlow = listOf(
                "Setiap malam, Ustadz Tahfidz menginput hasil halaqah Al-Qur'an di kelas pembimbing melalui modul ustadz.",
                "Sistem memperbarui kemajuan total Juz yang dihafal secara agregatif.",
                "Wali Santri mengklik menu 'Akademik' di aplikasi.",
                "Wali Santri melihat tab 'Hafalan' yang menampilkan kemajuan visual, perolehan Juz terakhir, rincian ayat ziyadah baru, dan penugasan murajaah.",
                "Wali Santri bergeser ke tab 'Kitab Kuning' untuk melihat kitab apa saja yang dipelajari anak dan status bimbingannya hari ini."
            ),
            tableSchema = """
                CREATE TABLE progress_tahfidz (
                    id BIGSERIAL PRIMARY KEY,
                    nis VARCHAR(20) NOT NULL,
                    tanggal DATE NOT NULL,
                    tipe_tahfidz VARCHAR(15) NOT NULL, -- 'ZIYADAH' atau 'MURAJAAH'
                    juz INT NOT NULL,
                    surat VARCHAR(100) NOT NULL,
                    dari_ayat INT NOT NULL,
                    sampai_ayat INT NOT NULL,
                    predikat_kelancaran VARCHAR(5) NOT NULL, -- 'A', 'B', 'C', 'D'
                    catatan_ustadz TEXT NULL,
                    ustadz_nama VARCHAR(100) NOT NULL,
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );

                CREATE TABLE pengajian_kitab (
                    id BIGSERIAL PRIMARY KEY,
                    nis VARCHAR(20) NOT NULL,
                    tanggal DATE NOT NULL,
                    nama_kitab VARCHAR(100) NOT NULL,
                    bab_materi VARCHAR(100) NOT NULL,
                    kehadiran_pengajian VARCHAR(15) NOT NULL, -- 'HADIR', 'SAKIT', 'IZIN', 'ALFA'
                    status_pemahaman VARCHAR(20) NOT NULL, -- 'PAHAM', 'CUKUP', 'KURANG'
                    catatan_ustadz TEXT,
                    ustadz_nama VARCHAR(100),
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );
            """.trimIndent()
        ),
        SrsSection(
            title = "5. Perizinan Santri",
            subtitle = "Pengajuan izin digital dari rumah & gerbang QR Code Digital Pass",
            iconName = "security",
            content = """
                Prosedur perizinan digital meminimalisir kesalahan administratif dan meningkatkan keamanan area pondok. Wali santri dapat mengajukan izin pulang/pergi bagi santri melalui aplikasi. Pengurus keamanan/pengasuh melihat antrean permohonan izin di dashboard pengasuh dan dapat menyetujuinya. 
                
                Ketentuan Utama:
                • Pengajuan menyertakan alasan izin, durasi, tanggal keluar, dan rencana tanggal kembali.
                • Status izin: PENDING (Awaiting Approval), APPROVED (Izin Disetujui), REJECTED (Izin Ditolak/Ditangguhkan).
                • Jika disetujui, sistem otomatis memproduksi Token Penjemputan dalam bentuk QR Code. QR Code ini dipindai oleh satpam/keamanan gerbang utama pesantren saat santri dijemput.
            """.trimIndent(),
            userFlow = listOf(
                "Wali Santri menekan tombol '+' di halaman 'Perizinan'.",
                "Mengisi form: Alasan (misal: acara keluarga, sakit, kontrol dokter), tanggal jemput, dan tanggal kembali.",
                "Submisi dikirim ke `/api/permits/create` dengan status awal 'PENDING'.",
                "Kepala Pengasuh / Tim Keamanan mendapatkan push notification untuk review.",
                "Pengasuh memilih setuju / tolak izin.",
                "Status ter-update menjadi 'APPROVED' di HP Wali Santri, dan menampilkan QR Code Pass.",
                "Wali Santri menjemput anak di gerbang pondok, satpam memindai QR Code untuk mencatat log keluar riil."
            ),
            tableSchema = """
                CREATE TABLE perizinan (
                    id VARCHAR(50) PRIMARY KEY, -- e.g., 'PERMIT-2026-0001'
                    nis VARCHAR(20) NOT NULL,
                    tanggal_pengajuan TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    alasan TEXT NOT NULL,
                    tanggal_keluar DATE NOT NULL,
                    tanggal_kembali DATE NOT NULL,
                    status_perizinan VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
                    catatan_pengasuh TEXT NULL,
                    token_qr VARCHAR(100) UNIQUE NULL,
                    is_active BOOLEAN DEFAULT TRUE,
                    disetujui_oleh VARCHAR(100) NULL,
                    FOREIGN KEY (nis) REFERENCES santri(nis) ON DELETE CASCADE
                );
            """.trimIndent()
        ),
        SrsSection(
            title = "6. Sistem Notifikasi",
            subtitle = "Sinergi WhatsApp Bot Gateway dan Push Notification Real-Time",
            iconName = "notification",
            content = """
                Jembatan komunikasi real-time antara pondok pesantren dan wali santri. Karena tidak semua orang tua selalu mengaktifkan aplikasi mobile, integrasi pesan langsung ke WhatsApp melalui bot sangat vital untuk menjamin penyebaran kabar penting dan urgensi keuangan/perizinan diterima dengan cepat.
                
                Ketentuan Utama:
                • WhatsApp API Gateway (e.g. Fonnte, Wablas, Twilio) mengirimkan pesan otomatis (Template Message) ke nomor wali jika terpicu trigger database.
                • Firebase Cloud Messaging (FCM) mengirimkan Push Notification standar untuk HP yang menginstal aplikasi.
                • Kategori Push Alerts: Akademik (Hafalan baru), Keuangan (Tagihan diterbitkan / SPP dibayar), Gerbang (Santri keluar/masuk komplek pesantren).
            """.trimIndent(),
            userFlow = listOf(
                "Sebuah Event terjadi di backend (misal: Ustadz memasukkan hafalan Qur'an baru).",
                "Event Dispatcher membagi tugas: (a) kirim Push Notification via FCM, (b) panggil WhatsApp Gateway API.",
                "WhatsApp API Gateway memformat pesan (misal: 'Assalamualaikum Bapak/Ibu, Alhamdulillah ananda Ahmad baru saja menyetorkan Murajaah Juz 29 Surat Al-Mulk ayat 1-10 dengan nilai kelancaran (A).').",
                "Orang tua menerima notifikasi instan di WhatsApp mereka serta alert notifikasi di ponsel."
            ),
            tableSchema = """
                CREATE TABLE notifikasi_log (
                    id BIGSERIAL PRIMARY KEY,
                    wali_id VARCHAR(50) NOT NULL,
                    judul VARCHAR(100) NOT NULL,
                    pesan TEXT NOT NULL,
                    tipe_notif VARCHAR(30) NOT NULL, -- 'AKADEMIK', 'KEUANGAN', 'IZIN', 'UMUM'
                    kanal_kirim VARCHAR(20) NOT NULL, -- 'WHATSAPP', 'PUSH', 'KEDUANYA'
                    status_kirim VARCHAR(15) DEFAULT 'SENT', -- 'SENT', 'FAILED'
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (wali_id) REFERENCES wali_santri(id) ON DELETE CASCADE
                );
            """.trimIndent()
        )
    )

    val techStackRecommendation = """
        Rekomendasi Arsitektur & Teknologi (Tech Stack) yang Cocok:
        
        1. STRUKTUR APLIKASI WEB DAN MOBILE (Frontend):
           • Mobile App (Wali Santri):
             - Framework: Jetpack Compose (Modern Kotlin) untuk performa native, render UI instan, dan konsumsi baterai ramah.
             - State Management: StateFlow + ViewModel + Kotlin Coroutines.
             - Local DB Cache: Room Database (SQLite) untuk menyimpan profil anak secara luring.
             - Media & Network: Retrofit / Ktor-Client untuk REST API + Coil Compose untuk pemuatan avatar santri.
           • Web Dashboard & Admin (Pengurus / Ustadz / Pengasuh):
             - ReactJS / NextJS dengan Tailwind CSS & Shadcn UI untuk kemudahan manajemen antarmuka.

        2. ARSITEKTUR BACKEND (API & Layanan):
           • Bahasa Pemrograman & Runtime: NodeJS (NestJS / TypeScript) atau Go (Golang) untuk mengolah request dengan latensi rendah dan konsumsi memory hemat.
           • Database Utama: PostgreSQL (Relational) untuk menjamin kekonsistenan data (ACID Compliance) antara keuangan, perizinan, dan profilitas.
           • Redis Cache: Untuk menyimpan sesi login token beralih anak, caching jurnal agenda harian, dan antrean pengiriman notifikasi (Queueing) agar tidak membebani database utama.

        3. INTEGRASI PIHAK KETIGA:
           • Payment Gateway: Midtrans atau Xendit API (VA Bank Mandiri, BNI, BRI, BCA, QRIS, dll).
           • WhatsApp Gateway: Fonnte / Wablas atau Green API untuk mengirimkan alert otomatis tanpa membakar biaya pulsa SMS tradisional.
           • Push Alerts: Firebase Cloud Messaging (FCM) SDK.
    """.trimIndent()
}
