package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.ui.dialogs.AddTransactionDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseHeatTile
import com.example.ui.theme.ExpenseHeatTileActive
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TrophyYellow
import com.example.ui.theme.TrophyYellowLight
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyHelper

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onAddUpcomingClick: () -> Unit,
    onLevelClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalWalletBalance.collectAsState()
    val upcomingItems by viewModel.upcomingItems.collectAsState()
    val calendarExpenses by viewModel.calendarDailyExpenses.collectAsState()
    val selectedMonth by viewModel.selectedMonthIndex.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val monthName = when (selectedMonth) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        else -> "Desember"
    }

    var upcomingFilter by remember { mutableStateOf("Semua") }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Dasbor",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showTransferDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Tukar / Transfer Antar Akun",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = { showNotificationsDialog = true }) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = ExpenseRed, contentColor = Color.White) {
                                Text("1", fontSize = 10.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi & Pengingat",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: "Yang akan datang" (Upcoming Flow)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yang akan datang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Semua", "Masuk", "Keluar").forEach { filter ->
                                    val isSelected = upcomingFilter == filter
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) DarkSurfaceElevated else Color.Transparent,
                                        modifier = Modifier.clickable { upcomingFilter = filter }
                                    ) {
                                        Text(
                                            text = filter,
                                            fontSize = 11.sp,
                                            color = if (isSelected) TextWhite else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.clickable(onClick = onAddUpcomingClick)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = TextWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Tambah",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Untuk bertahan hingga pemasukan berikutnya, 1 Nov",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "4 hari tersisa · Rp114.557 sehari",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Timeline Milestone Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Step 1: Hari Ini
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rp${CurrencyHelper.formatCompact(totalBalance)} sekarang",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(2.dp)
                                            .background(DarkCardBorder)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hari Ini",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            // Step 2: 1 Nov (Income)
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = "Rp3.1M tersisa",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .border(2.dp, IncomeGreen, CircleShape)
                                            .background(DarkSurface)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(2.dp)
                                            .background(DarkCardBorder)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1 Nov\nMin",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IncomeGreenSoft
                                ) {
                                    Text(
                                        text = "↙ Rp2.7M Gaji Pokok",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = IncomeGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Step 3: 16 Nov (Expense)
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "Rp3.1M tersisa",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .border(2.dp, ExpenseRed, CircleShape)
                                            .background(DarkSurface)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "16 Nov\nSen",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExpenseRedSoft
                                ) {
                                    Text(
                                        text = "↗ Rp8K Admin BRI Istri",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ExpenseRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: "Gamifikasi & Target Keuangan"
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLevelClick() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular trophy LV 2 badge
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(3.dp, TrophyYellow, CircleShape)
                                    .background(Color(0x1AF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Trophy",
                                        tint = TrophyYellowLight,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = TrophyYellow
                                    ) {
                                        Text(
                                            text = "LV 2",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LEVEL SAAT INI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Dana cadangan pertama",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TARGET BERIKUTNYA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Rp17.673.149",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrophyYellowLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "KEMAJUAN KE LEVEL BERIKUTNYA",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "88%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrophyYellowLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { 0.88f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TrophyYellow,
                            trackColor = DarkSurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2.8 bulan pengeluaran",
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onLevelClick() }
                            ) {
                                Text(
                                    text = "Semua level",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: "Aktivitas Pengeluaran" (Calendar Matrix)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aktivitas Pengeluaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$monthName $selectedYear",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calendar Weekday Headers
                        val daysOfWeek = listOf("S", "S", "R", "K", "J", "S", "M")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(38.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Days 1..31 in calendar layout
                        // In Oct 2026, Oct 1 is Thursday (index 3 in 0-indexed Mon-based week: Sen=0, Sel=1, Rab=2, Kam=3)
                        val startOffset = 3 // starts on Thursday
                        val totalDays = 31

                        val totalSlots = startOffset + totalDays
                        val rows = (totalSlots + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 7) {
                                    val slotIndex = r * 7 + c
                                    val dayNumber = slotIndex - startOffset + 1

                                    if (dayNumber in 1..totalDays) {
                                        val dayExpense = calendarExpenses[dayNumber] ?: 0.0
                                        val hasExpense = dayExpense > 0

                                        Surface(
                                            modifier = Modifier
                                                .size(width = 42.dp, height = 48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (hasExpense) ExpenseHeatTile else DarkSurfaceElevated
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(3.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    fontSize = 10.sp,
                                                    color = if (hasExpense) Color(0xFFE2E8F0) else TextSubtle
                                                )
                                                if (hasExpense) {
                                                    Text(
                                                        text = CurrencyHelper.formatCalendarExpense(dayExpense),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ExpenseRed,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier.size(width = 42.dp, height = 48.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Dialogs
        if (showTransferDialog) {
            AddTransactionDialog(
                viewModel = viewModel,
                initialCategory = "Transfer",
                onDismiss = { showTransferDialog = false }
            )
        }

        if (showNotificationsDialog) {
            DashboardNotificationsDialog(
                viewModel = viewModel,
                onDismiss = { showNotificationsDialog = false }
            )
        }
    }
}

@Composable
fun DashboardNotificationsDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val upcomingItems by viewModel.upcomingItems.collectAsState()
    val totalBalance by viewModel.totalWalletBalance.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengingat & Notifikasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Status Keuangan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrophyYellowLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total saldo likuid saat ini: ${CurrencyHelper.formatRupiah(totalBalance)}. Semua dompet aktif.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = "Kewajiban Mendatang (${upcomingItems.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                if (upcomingItems.isEmpty()) {
                    Text(
                        text = "Tidak ada tagihan atau pembayaran yang tertunda saat ini. Semua lancar!",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                } else {
                    upcomingItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Jatuh tempo: ${item.dateLabel} (${item.dayLabel})",
                                        fontSize = 10.sp,
                                        color = ExpenseRed
                                    )
                                }
                                Text(
                                    text = CurrencyHelper.formatRupiah(item.amount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}
