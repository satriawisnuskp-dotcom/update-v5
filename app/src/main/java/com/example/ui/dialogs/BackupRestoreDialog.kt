package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SegmentAccountBlue
import com.example.ui.theme.SegmentAssetGreen
import com.example.ui.theme.SegmentDebtRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.allTransactions.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var pendingTxCount by remember { mutableStateOf(0) }
    var pendingWalletCount by remember { mutableStateOf(0) }
    var pendingAssetCount by remember { mutableStateOf(0) }
    var pendingDebtCount by remember { mutableStateOf(0) }

    var replaceExistingChoice by remember { mutableStateOf(false) } // false = merge, true = replace
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    var lastActionStatus by remember { mutableStateOf<String?>(null) }

    // SAF Document Creators and Openers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = viewModel.exportTransactionsJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                lastActionStatus = "Cadangan berhasil! Data transaksi, akun, aset, & utang tersimpan di file .json."
                Toast.makeText(
                    context,
                    "Berhasil mencadangkan ${transactions.size} transaksi, ${wallets.size} akun, ${assets.size} aset, ${debts.size} utang!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                lastActionStatus = "Gagal mencadangkan: ${e.localizedMessage}"
                Toast.makeText(context, "Gagal mencadangkan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                }
                if (content.isNullOrBlank()) {
                    Toast.makeText(context, "File JSON kosong!", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }

                // Inspect JSON to count all data entities
                val trimmed = content.trim()
                var txCount = 0
                var walletCount = 0
                var assetCount = 0
                var debtCount = 0

                if (trimmed.startsWith("[")) {
                    txCount = JSONArray(trimmed).length()
                } else {
                    val obj = JSONObject(trimmed)
                    txCount = obj.optJSONArray("transactions")?.length() ?: 0
                    walletCount = obj.optJSONArray("wallets")?.length() ?: 0
                    assetCount = obj.optJSONArray("assets")?.length() ?: 0
                    debtCount = obj.optJSONArray("debts")?.length() ?: 0
                }

                if (txCount == 0 && walletCount == 0 && assetCount == 0 && debtCount == 0) {
                    Toast.makeText(context, "Tidak ditemukan data keuangan yang valid dalam file JSON", Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }

                pendingImportJson = content
                pendingTxCount = txCount
                pendingWalletCount = walletCount
                pendingAssetCount = assetCount
                pendingDebtCount = debtCount
                replaceExistingChoice = false
                showImportConfirmDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun shareJsonFile() {
        try {
            val jsonString = viewModel.exportTransactionsJson()
            val fileName = "keuangan_cadangan_lengkap_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
            } catch (e: Exception) {
                null
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                if (uri != null) {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, jsonString)
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Cadangan Keuangan Lengkap"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentBlue.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cadangkan & Pulihkan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Semua transaksi, akun, aset, & utang (.json)",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Data Scope Card (Requirement 1: shows all 4 entities)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IncomeGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cakupan Cadangan Lengkap",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Semua data berikut dicadangkan secara terpadu:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Entity badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${transactions.size}", fontWeight = FontWeight.Bold, color = AccentBlue, fontSize = 15.sp)
                                Text("Transaksi", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${wallets.size}", fontWeight = FontWeight.Bold, color = SegmentAccountBlue, fontSize = 15.sp)
                                Text("Akun", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${assets.size}", fontWeight = FontWeight.Bold, color = SegmentAssetGreen, fontSize = 15.sp)
                                Text("Aset", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${debts.size}", fontWeight = FontWeight.Bold, color = SegmentDebtRed, fontSize = 15.sp)
                                Text("Utang", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            if (lastActionStatus != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IncomeGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lastActionStatus ?: "",
                            fontSize = 12.sp,
                            color = IncomeGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Ekspor Semua Data
            Text(
                text = "CADANGKAN SEMUA DATA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simpan Cadangan ke Penyimpanan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "Mencadangkan seluruh riwayat transaksi, semua dompet & akun, data aset investasi, serta utang piutang ke file .json.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = {
                            val defaultName = "keuangan_cadangan_lengkap_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                            exportLauncher.launch(defaultName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangkan Seluruh Data (.JSON)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { shareJsonFile() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bagikan File Cadangan",
                            fontSize = 13.sp,
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Pulihkan Data
            Text(
                text = "PULIHKAN DATA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pulihkan dari File Cadangan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "Pilih file .json cadangan untuk memulihkan seluruh transaksi, akun, aset, dan utang piutang Anda.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih File .JSON untuk Dipulihkan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Anda dapat memilih untuk menggabungkan (merge) atau menggantikan semua data saat memulihkan.",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Import Options & Confirmation Dialog
    if (showImportConfirmDialog && pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportJson = null
            },
            containerColor = DarkSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Konfirmasi Pemulihan Data",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Ditemukan data dalam file cadangan:",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• $pendingTxCount Catatan Transaksi", color = TextWhite, fontSize = 12.sp)
                            if (pendingWalletCount > 0) {
                                Text("• $pendingWalletCount Akun / Dompet", color = SegmentAccountBlue, fontSize = 12.sp)
                            }
                            if (pendingAssetCount > 0) {
                                Text("• $pendingAssetCount Aset Investasi", color = SegmentAssetGreen, fontSize = 12.sp)
                            }
                            if (pendingDebtCount > 0) {
                                Text("• $pendingDebtCount Catatan Utang Piutang", color = SegmentDebtRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pilih metode pemulihan data:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 1: Merge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!replaceExistingChoice) AccentBlue.copy(alpha = 0.15f) else DarkCardSubtle,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!replaceExistingChoice) AccentBlue else DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replaceExistingChoice = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !replaceExistingChoice,
                                onClick = { replaceExistingChoice = false },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Gabungkan Data (Merge)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Menambahkan transaksi, akun, aset, & utang baru tanpa menghapus data yang ada.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Replace
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (replaceExistingChoice) ExpenseRed.copy(alpha = 0.15f) else DarkCardSubtle,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (replaceExistingChoice) ExpenseRed else DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replaceExistingChoice = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = replaceExistingChoice,
                                onClick = { replaceExistingChoice = true },
                                colors = RadioButtonDefaults.colors(selectedColor = ExpenseRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ganti Seluruh Data (Replace)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Menghapus data saat ini dan menggantikan dengan data lengkap dari file cadangan.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    if (replaceExistingChoice) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Peringatan: Seluruh data saat ini akan digantikan.",
                                color = ExpenseRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val content = pendingImportJson
                        if (content != null) {
                            val result = viewModel.importTransactionsFromJson(content, replaceExistingChoice)
                            result.onSuccess { summary ->
                                val msg = "Berhasil memulihkan ${summary.transactionCount} transaksi, ${summary.walletCount} akun, ${summary.assetCount} aset, ${summary.debtCount} utang!"
                                lastActionStatus = msg
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                lastActionStatus = "Gagal memulihkan: ${err.localizedMessage}"
                                Toast.makeText(context, "Gagal memulihkan: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                        showImportConfirmDialog = false
                        pendingImportJson = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (replaceExistingChoice) ExpenseRed else IncomeGreen
                    )
                ) {
                    Text(
                        text = if (replaceExistingChoice) "Ganti Sekarang" else "Pulihkan & Gabungkan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportJson = null
                }) {
                    Text("Batal", color = TextSubtle)
                }
            }
        )
    }
}
