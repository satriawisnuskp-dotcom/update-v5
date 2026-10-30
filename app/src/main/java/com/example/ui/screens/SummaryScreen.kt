package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.MonthYearPickerDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBright
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.util.CurrencyHelper

@Composable
fun SummaryScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonthIndex.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val monthOptions by viewModel.monthOptions.collectAsState()
    val selectedWallet by viewModel.selectedWalletFilter.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val summaryTab by viewModel.summaryTab.collectAsState()
    val summaryPeriod by viewModel.summaryPeriod.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()

    var showWalletDropdown by remember { mutableStateOf(false) }
    var isPlannedActive by remember { mutableStateOf(true) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var chartPeriod by remember { mutableStateOf(ChartPeriod.ONE_MONTH) }
    var selectedCalendarDay by remember { mutableIntStateOf(1) }

    // Filter transactions based on selected period and wallet
    val periodTransactions = remember(allTransactions, selectedMonth, selectedYear, selectedWallet, summaryPeriod) {
        allTransactions.filter { tx ->
            val matchPeriod = when (summaryPeriod) {
                0 -> tx.monthIndex == selectedMonth && tx.year == selectedYear // Bulanan
                1 -> tx.year == selectedYear // Tahunan
                else -> true // Kustom / Semua
            }
            val matchWallet = selectedWallet == "Semua" || tx.accountName.equals(selectedWallet, ignoreCase = true)
            matchPeriod && matchWallet
        }
    }

    val currentIncome = remember(periodTransactions) {
        periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    val currentExpense = remember(periodTransactions) {
        periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val netCashFlow = currentIncome - currentExpense

    val chartData = remember(periodTransactions, chartPeriod, selectedMonth, selectedYear) {
        ChartDataHelper.getExpenseTrend(periodTransactions, chartPeriod, selectedMonth, selectedYear)
    }

    val cumulativeData = remember(allTransactions, selectedMonth, selectedYear, selectedWallet) {
        ChartDataHelper.getCumulativeComparison(allTransactions, selectedMonth, selectedYear, selectedWallet)
    }

    // Category breakdown
    val categoryBreakdown = remember(periodTransactions) {
        val expenses = periodTransactions.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        expenses.groupBy { it.category }
            .map { (cat, list) ->
                val amount = list.sumOf { it.amount }
                val ratio = if (total > 0) (amount / total).toFloat() else 0f
                Triple(cat, amount, ratio)
            }
            .sortedByDescending { it.second }
    }

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
                text = "Ringkasan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Wallet Filter
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.clickable { showWalletDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWallet,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showWalletDropdown,
                    onDismissRequest = { showWalletDropdown = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Semua", color = TextWhite) },
                        onClick = {
                            viewModel.selectWalletFilter("Semua")
                            showWalletDropdown = false
                        }
                    )
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet.name, color = TextWhite) },
                            onClick = {
                                viewModel.selectWalletFilter(wallet.name)
                                showWalletDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Month Selector: Fixed Calendar Button on the left + Horizontally scrollable Month Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed Calendar Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .clickable { showMonthYearPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Bulan dan Tahun",
                        tint = AccentBlueBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Scrollable Month Chips
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                monthOptions.forEach { (index, label) ->
                    val isSelected = selectedMonth == index
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                        modifier = Modifier.clickable { viewModel.selectMonth(index) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Sub-tabs row: Statistik, Laporan, Kalender
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple(0, "Statistik", Icons.Default.BarChart),
                Triple(1, "Laporan", Icons.Default.Description),
                Triple(2, "Kalender", Icons.Default.CalendarMonth)
            )

            tabs.forEach { (index, title, icon) ->
                val isSelected = summaryTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setSummaryTab(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // TAB CONTENT
        when (summaryTab) {
            0 -> {
                // STATISTIK TAB
                StatistikView(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthOptions = monthOptions,
                    summaryPeriod = summaryPeriod,
                    chartPeriod = chartPeriod,
                    chartData = chartData,
                    cumulativeData = cumulativeData,
                    currentIncome = currentIncome,
                    currentExpense = currentExpense,
                    isPlannedActive = isPlannedActive,
                    recurringTransactions = recurringTransactions,
                    categoryBreakdown = categoryBreakdown,
                    onSummaryPeriodChange = { viewModel.setSummaryPeriod(it) },
                    onChartPeriodChange = { chartPeriod = it },
                    onPlannedActiveToggle = { isPlannedActive = it },
                    onQuickFilterMonth = {
                        chartPeriod = ChartPeriod.ONE_MONTH
                        viewModel.setSummaryPeriod(0)
                    },
                    onQuickFilter7Days = {
                        chartPeriod = ChartPeriod.SEVEN_DAYS
                    }
                )
            }
            1 -> {
                // LAPORAN TAB
                LaporanView(
                    selectedMonthLabel = "${monthOptions.find { it.first == selectedMonth }?.second ?: "Okt"} $selectedYear",
                    selectedWallet = selectedWallet,
                    currentIncome = currentIncome,
                    currentExpense = currentExpense,
                    netCashFlow = netCashFlow,
                    transactions = periodTransactions,
                    categoryBreakdown = categoryBreakdown
                )
            }
            else -> {
                // KALENDER TAB
                KalenderView(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Okt",
                    selectedDay = selectedCalendarDay,
                    transactions = periodTransactions,
                    onSelectDay = { selectedCalendarDay = it },
                    onPrevMonth = {
                        val prev = if (selectedMonth > 1) selectedMonth - 1 else 12
                        val y = if (selectedMonth > 1) selectedYear else selectedYear - 1
                        viewModel.selectMonthAndYear(prev, y)
                    },
                    onNextMonth = {
                        val next = if (selectedMonth < 12) selectedMonth + 1 else 1
                        val y = if (selectedMonth < 12) selectedYear else selectedYear + 1
                        viewModel.selectMonthAndYear(next, y)
                    }
                )
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthYearPicker = false },
            onSelectMonthYear = { m, y ->
                viewModel.selectMonthAndYear(m, y)
            }
        )
    }
}

// -------------------------------------------------------------
// 1. STATISTIK VIEW
// -------------------------------------------------------------

@Composable
fun StatistikView(
    selectedMonth: Int,
    selectedYear: Int,
    monthOptions: List<com.example.ui.viewmodel.MonthOption>,
    summaryPeriod: Int,
    chartPeriod: ChartPeriod,
    chartData: com.example.util.ChartSeriesData,
    cumulativeData: com.example.util.CumulativeComparisonData,
    currentIncome: Double,
    currentExpense: Double,
    isPlannedActive: Boolean,
    recurringTransactions: List<com.example.data.model.RecurringTransaction>,
    categoryBreakdown: List<Triple<String, Double, Float>>,
    onSummaryPeriodChange: (Int) -> Unit,
    onChartPeriodChange: (ChartPeriod) -> Unit,
    onPlannedActiveToggle: (Boolean) -> Unit,
    onQuickFilterMonth: () -> Unit,
    onQuickFilter7Days: () -> Unit
) {
    val monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Okt"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Buku harian uang Anda" Banner Card with functional filter buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B2238),
                                    Color(0xFF151928),
                                    Color(0xFF111420)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x33F59E0B),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Buku harian uang Anda",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Melihat kembali dengan cermat arus keuangan Anda",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Text(text = "🌙", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (chartPeriod == ChartPeriod.ONE_MONTH) AccentBlue else Color(0x33FFFFFF),
                                modifier = Modifier.clickable(onClick = onQuickFilterMonth)
                            ) {
                                Text(
                                    text = "🌙 $monthName $selectedYear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (chartPeriod == ChartPeriod.SEVEN_DAYS) AccentBlue else Color(0x33FFFFFF),
                                modifier = Modifier.clickable(onClick = onQuickFilter7Days)
                            ) {
                                Text(
                                    text = "⭐ 7 hari terakhir",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Period Selector (Bulanan / Tahunan / Kustom)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sekilas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(14.dp))
                        .padding(2.dp)
                ) {
                    listOf("Bulanan", "Tahunan", "Semua").forEachIndexed { index, title ->
                        val isSelected = summaryPeriod == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentBlue else Color.Transparent,
                            modifier = Modifier.clickable { onSummaryPeriodChange(index) }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Card "AKTIVITAS PENGELUARAN" (Daily Trend Chart)
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
                        Column {
                            Text(
                                text = "AKTIVITAS PENGELUARAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Text(
                                text = "$monthName $selectedYear",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        // Period selector chips inside card
                        Row(
                            modifier = Modifier
                                .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                .padding(2.dp)
                        ) {
                            listOf(
                                ChartPeriod.SEVEN_DAYS,
                                ChartPeriod.ONE_WEEK,
                                ChartPeriod.ONE_MONTH,
                                ChartPeriod.ONE_YEAR
                            ).forEach { p ->
                                val isSelected = chartPeriod == p
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) AccentBlue else Color.Transparent,
                                    modifier = Modifier.clickable { onChartPeriodChange(p) }
                                ) {
                                    Text(
                                        text = p.shortLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rp${CurrencyHelper.formatCompact(chartData.total)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pengeluaran",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (chartData.percentChange <= 0) IncomeGreenSoft else Color(0x33F43F5E)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (chartData.percentChange <= 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = if (chartData.percentChange <= 0) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${if (chartData.percentChange > 0) "+" else ""}${chartData.percentChange}% vs periode lalu",
                                    fontSize = 10.sp,
                                    color = if (chartData.percentChange <= 0) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DailyExpenseLineChart(
                        points = chartData.points,
                        peakIndex = chartData.rawValues.indexOf(chartData.peakValue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(chartData.startLabel, fontSize = 10.sp, color = TextSubtle)
                        Text(chartData.midLabel, fontSize = 10.sp, color = TextSubtle)
                        Text(chartData.endLabel, fontSize = 10.sp, color = TextSubtle)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Dynamic summary metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Per hari", fontSize = 11.sp, color = TextSubtle)
                            Text("Rp${CurrencyHelper.formatCompact(chartData.averagePerDay)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Column {
                            Text("Hari terboros", fontSize = 11.sp, color = TextSubtle)
                            Text("Rp${CurrencyHelper.formatCompact(chartData.peakValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(chartData.peakDayLabel, fontSize = 10.sp, color = TextSubtle)
                        }
                        Column {
                            Text("Kategori teratas", fontSize = 11.sp, color = TextSubtle)
                            Text(chartData.topCategory, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Rp${CurrencyHelper.formatCompact(chartData.topCategoryAmount)}", fontSize = 10.sp, color = TextSubtle)
                        }
                    }
                }
            }
        }

        // Card "Ringkasan: Arus Kas"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSubtle
                    )
                    Text(
                        text = "Arus Kas Periode Ini",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Income Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyHelper.formatRupiah(currentIncome),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pemasukan",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = IncomeGreen,
                        trackColor = DarkSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expense Bar
                    val expenseRatio = if (currentIncome > 0) (currentExpense / currentIncome).toFloat().coerceIn(0f, 1f) else 1f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyHelper.formatRupiah(currentExpense),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pengeluaran",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { expenseRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ExpenseRed,
                        trackColor = DarkSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text analysis card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val net = currentIncome - currentExpense
                        val status = if (net >= 0) "positif" else "negatif"
                        val diffText = if (net >= 0) "+${CurrencyHelper.formatRupiah(net)} lebih besar dari pengeluaran" else "${CurrencyHelper.formatRupiah(-net)} lebih besar dari pemasukan"
                        Text(
                            text = "Pada $monthName $selectedYear, arus kas Anda $status. Pemasukan Anda $diffText.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Card "Terencana" (Functional with actual recurring transactions)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Terencana",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Text(
                                text = "Transaksi Berulang Terjadwal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Switch(
                            checked = isPlannedActive,
                            onCheckedChange = onPlannedActiveToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isPlannedActive && recurringTransactions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recurringTransactions.take(3).forEach { rec ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rec.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextWhite)
                                            Text("Setiap tgl ${rec.dayOfMonth} · ${rec.accountName}", fontSize = 11.sp, color = TextMuted)
                                        }
                                        Text(
                                            text = "${if (rec.type == TransactionType.INCOME) "+" else "-"}Rp${CurrencyHelper.formatCompact(rec.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (rec.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (isPlannedActive) "Belum ada transaksi berulang aktif untuk periode ini." else "Tampilan transaksi berulang dinonaktifkan.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Card "Pendapatan vs Pengeluaran" (Cumulative) - FULLY DYNAMIC
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
                            text = "Pendapatan vs Pengeluaran",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Kumulatif Bulanan",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val savedSign = if (cumulativeData.savedAmount >= 0) "+" else "-"
                            Text(
                                text = "${savedSign}Rp${CurrencyHelper.formatCompact(kotlin.math.abs(cumulativeData.savedAmount))}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cumulativeData.savedAmount >= 0) IncomeGreen else ExpenseRed
                            )
                            Text(
                                text = if (cumulativeData.savedAmount >= 0) "Ditabung periode ini" else "Defisit periode ini",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated
                        ) {
                            Text(
                                text = "${cumulativeData.savingsRatePercent}% pemasukan tersisa",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cumulativeData.isIncomeHigher) IncomeGreen else ExpenseRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp, 3.dp).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pemasukan Rp${CurrencyHelper.formatCompact(cumulativeData.totalIncome)}", fontSize = 11.sp, color = TextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp, 3.dp).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran Rp${CurrencyHelper.formatCompact(cumulativeData.totalExpense)}", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dual Line Cumulative Chart
                    CumulativeComparisonChart(
                        incomePoints = cumulativeData.incomePoints,
                        expensePoints = cumulativeData.expensePoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("1", "8", "15", "22", "31").forEach {
                            Text(text = it, fontSize = 10.sp, color = TextSubtle)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (cumulativeData.isIncomeHigher) "📈" else "📉", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cumulativeData.summaryTakeaway,
                            fontSize = 11.sp,
                            color = if (cumulativeData.isIncomeHigher) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }

        // Card "Pengeluaran Berdasarkan Kategori"
        if (categoryBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kategori Pengeluaran Terbesar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            categoryBreakdown.take(5).forEach { (catName, sum, ratio) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(catName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            CurrencyHelper.formatRupiah(sum),
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.5.dp)),
                                        color = AccentBlue,
                                        trackColor = DarkSurfaceElevated
                                    )
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
}

// -------------------------------------------------------------
// 2. LAPORAN VIEW
// -------------------------------------------------------------

@Composable
fun LaporanView(
    selectedMonthLabel: String,
    selectedWallet: String,
    currentIncome: Double,
    currentExpense: Double,
    netCashFlow: Double,
    transactions: List<Transaction>,
    categoryBreakdown: List<Triple<String, Double, Float>>
) {
    val context = LocalContext.current
    val savingsRate = if (currentIncome > 0) (((currentIncome - currentExpense) / currentIncome) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Summary Card
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
                        Column {
                            Text("LAPORAN ARUS KAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                            Text(selectedMonthLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        IconButton(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("📊 LAPORAN KEUANGAN FINTRACK")
                                    appendLine("Periode: $selectedMonthLabel")
                                    appendLine("Akun: $selectedWallet")
                                    appendLine("---------------------------")
                                    appendLine("💰 Total Pemasukan: ${CurrencyHelper.formatRupiah(currentIncome)}")
                                    appendLine("💸 Total Pengeluaran: ${CurrencyHelper.formatRupiah(currentExpense)}")
                                    appendLine("📈 Arus Kas Bersih: ${CurrencyHelper.formatRupiah(netCashFlow)}")
                                    appendLine("🎯 Rasio Tabungan: $savingsRate%")
                                    appendLine("---------------------------")
                                    appendLine("Rincian Pengeluaran Terbesar:")
                                    categoryBreakdown.take(5).forEach { (cat, amount, ratio) ->
                                        appendLine("- $cat: ${CurrencyHelper.formatRupiah(amount)} (${(ratio * 100).toInt()}%)")
                                    }
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan $selectedMonthLabel")
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Keuangan"))
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Bagikan", tint = AccentBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pemasukan", fontSize = 11.sp, color = TextMuted)
                                Text(CurrencyHelper.formatRupiah(currentIncome), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pengeluaran", fontSize = 11.sp, color = TextMuted)
                                Text(CurrencyHelper.formatRupiah(currentExpense), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Saldo Bersih Arus Kas", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    CurrencyHelper.formatRupiah(netCashFlow),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netCashFlow >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (savingsRate >= 20) IncomeGreenSoft else Color(0x33F43F5E)
                            ) {
                                Text(
                                    text = "Tabungan $savingsRate%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (savingsRate >= 20) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rincian Kategori Pengeluaran
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Pengeluaran per Kategori", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryBreakdown.isEmpty()) {
                        Text("Belum ada pengeluaran di periode ini.", color = TextMuted, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            categoryBreakdown.forEach { (cat, amount, ratio) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cat, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${CurrencyHelper.formatRupiah(amount)} (${(ratio * 100).toInt()}%)",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = AccentBlue,
                                        trackColor = DarkSurfaceElevated
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daftar Transaksi Periode Ini
        item {
            Text("Semua Transaksi (${transactions.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tidak ada transaksi pada periode ini.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(transactions.take(20)) { tx ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            Text("${tx.dayOfMonth} · ${tx.category} · ${tx.accountName}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text(
                            text = "${if (tx.type == TransactionType.INCOME) "+" else "-"}Rp${CurrencyHelper.formatCompact(tx.amount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------------------------------------------
// 3. KALENDER VIEW
// -------------------------------------------------------------

@Composable
fun KalenderView(
    selectedMonth: Int,
    selectedYear: Int,
    monthName: String,
    selectedDay: Int,
    transactions: List<Transaction>,
    onSelectDay: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    // Days in current month
    val daysInMonth = remember(selectedMonth, selectedYear) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, selectedYear)
        cal.set(java.util.Calendar.MONTH, selectedMonth - 1)
        cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    val dayTransactions = remember(transactions, selectedDay) {
        transactions.filter { it.dayOfMonth == selectedDay }
    }

    val dayIncome = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val dayExpense = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigation Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevMonth) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya", tint = TextWhite)
                    }
                    Text(
                        text = "$monthName $selectedYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    IconButton(onClick = onNextMonth) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Bulan Berikutnya", tint = TextWhite)
                    }
                }
            }
        }

        // Calendar Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Day of week headers
                    val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        dayHeaders.forEach { h ->
                            Text(
                                text = h,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSubtle,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid rows: 1..daysInMonth
                    val weeks = (1..daysInMonth).chunked(7)
                    weeks.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            week.forEach { day ->
                                val isSelected = day == selectedDay
                                val hasIncome = transactions.any { it.dayOfMonth == day && it.type == TransactionType.INCOME }
                                val hasExpense = transactions.any { it.dayOfMonth == day && it.type == TransactionType.EXPENSE }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentBlue else Color.Transparent)
                                        .clickable { onSelectDay(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$day",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextWhite
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasIncome) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(IncomeGreen))
                                            }
                                            if (hasExpense) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ExpenseRed))
                                            }
                                        }
                                    }
                                }
                            }
                            // Fill remaining spaces in the last week
                            val remaining = 7 - week.size
                            if (remaining > 0) {
                                repeat(remaining) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transactions on Selected Day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tanggal $selectedDay $monthName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (dayIncome > 0) {
                                Text("+Rp${CurrencyHelper.formatCompact(dayIncome)}", color = IncomeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (dayExpense > 0) {
                                Text("-Rp${CurrencyHelper.formatCompact(dayExpense)}", color = ExpenseRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (dayTransactions.isEmpty()) {
                        Text(
                            text = "Tidak ada transaksi pada tanggal ini.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            dayTransactions.forEach { tx ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tx.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                            Text("${tx.category} · ${tx.accountName}", fontSize = 11.sp, color = TextMuted)
                                        }
                                        Text(
                                            text = "${if (tx.type == TransactionType.INCOME) "+" else "-"}Rp${CurrencyHelper.formatCompact(tx.amount)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                                        )
                                    }
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
}

// -------------------------------------------------------------
// Interactive Charts
// -------------------------------------------------------------

@Composable
fun DailyExpenseLineChart(
    points: List<Float> = emptyList(),
    peakIndex: Int = -1,
    modifier: Modifier = Modifier
) {
    val effectivePoints = if (points.size >= 2) points else listOf(0.2f, 0.35f, 0.25f, 0.4f, 0.3f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (effectivePoints.size - 1).coerceAtLeast(1)

        // Draw baseline previous period dotted line
        val prevPath = Path()
        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            val prevVal = (value * 0.85f + 0.05f).coerceIn(0.05f, 0.95f)
            val y = height - (prevVal * height * 0.85f)
            if (i == 0) prevPath.moveTo(x, y) else prevPath.lineTo(x, y)
        }
        drawPath(
            path = prevPath,
            color = Color(0xFF64748B),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )

        // Draw current period filled gradient wave & solid line
        val path = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, height)

        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - (value * height * 0.85f).coerceAtMost(height * 0.95f)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x40F43F5E), Color(0x00F43F5E))
            )
        )

        drawPath(
            path = path,
            color = ExpenseRed,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Highlight the peak point
        val validPeakIndex = if (peakIndex in effectivePoints.indices) {
            peakIndex
        } else {
            effectivePoints.indices.maxByOrNull { effectivePoints[it] } ?: (effectivePoints.size / 2)
        }
        val peakX = validPeakIndex * stepX
        val peakY = height - (effectivePoints[validPeakIndex] * height * 0.85f).coerceAtMost(height * 0.95f)

        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = Offset(peakX, peakY)
        )
        drawCircle(
            color = ExpenseRed,
            radius = 3.dp.toPx(),
            center = Offset(peakX, peakY)
        )
    }
}

@Composable
fun CumulativeComparisonChart(
    incomePoints: List<Float> = emptyList(),
    expensePoints: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val effIncome = if (incomePoints.size >= 2) incomePoints else listOf(0.1f, 0.4f, 0.7f, 0.85f)
        val effExpense = if (expensePoints.size >= 2) expensePoints else listOf(0.1f, 0.3f, 0.5f, 0.65f)

        // Draw Income curve
        val stepXInc = width / (effIncome.size - 1).coerceAtLeast(1)
        val incomePath = Path()
        effIncome.forEachIndexed { index, normVal ->
            val x = index * stepXInc
            val y = height - (normVal * height * 0.82f).coerceIn(4f, height - 4f)
            if (index == 0) incomePath.moveTo(x, y) else incomePath.lineTo(x, y)
        }
        drawPath(
            path = incomePath,
            color = IncomeGreen,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Expense curve
        val stepXExp = width / (effExpense.size - 1).coerceAtLeast(1)
        val expensePath = Path()
        effExpense.forEachIndexed { index, normVal ->
            val x = index * stepXExp
            val y = height - (normVal * height * 0.82f).coerceIn(4f, height - 4f)
            if (index == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
        }
        drawPath(
            path = expensePath,
            color = ExpenseRed,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
