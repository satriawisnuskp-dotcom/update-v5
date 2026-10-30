package com.example.util

import com.example.data.model.AccountWallet
import com.example.data.model.AssetItem
import com.example.data.model.DebtItem
import com.example.data.model.DebtType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ChartPeriod(val label: String, val shortLabel: String) {
    SEVEN_DAYS("7 Hari", "7D"),
    ONE_WEEK("1 Minggu", "1W"),
    ONE_MONTH("1 Bulan", "1M"),
    THREE_MONTHS("3 Bulan", "3M"),
    ONE_YEAR("1 Tahun", "1Y"),
    ALL("Semua", "ALL")
}

data class ChartSeriesData(
    val startLabel: String,
    val midLabel: String,
    val endLabel: String,
    val points: List<Float>, // Normalized 0f..1f for canvas
    val rawValues: List<Double>,
    val total: Double,
    val averagePerDay: Double,
    val peakValue: Double,
    val peakDayLabel: String,
    val topCategory: String,
    val topCategoryAmount: Double,
    val percentChange: Double,
    val isPositive: Boolean,
    val deltaAmount: Double = 0.0
)

data class CumulativeComparisonData(
    val incomePoints: List<Float>,
    val expensePoints: List<Float>,
    val totalIncome: Double,
    val totalExpense: Double,
    val savedAmount: Double,
    val savingsRatePercent: Int,
    val summaryTakeaway: String,
    val isIncomeHigher: Boolean
)

object ChartDataHelper {

    private val monthNamesShort = listOf(
        "", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    fun getExpenseTrend(
        transactions: List<Transaction>,
        period: ChartPeriod,
        currentMonth: Int = 10,
        currentYear: Int = 2026
    ): ChartSeriesData {
        val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }

        return when (period) {
            ChartPeriod.SEVEN_DAYS -> {
                // Last 7 days: day 22 to day 28
                val days = (22..28).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = if (days.isNotEmpty()) total / days.size else 0.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0) "${days[maxIdx]} Okt" else "26 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth in days }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                val topCatName = topCat?.key ?: "Other"
                val topCatAmount = topCat?.value?.sumOf { it.amount } ?: total

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "22 Okt",
                    midLabel = "25 Okt",
                    endLabel = "28 Okt",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCatName,
                    topCategoryAmount = topCatAmount,
                    percentChange = -8.5,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_WEEK -> {
                // This calendar week: Mon 20 Okt to Sun 26 Okt
                val days = (20..26).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = total / 7.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0) "${days[maxIdx]} Okt" else "20 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth in days }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "Sen, 20 Okt",
                    midLabel = "Kam, 23 Okt",
                    endLabel = "Min, 26 Okt",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -13.0,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_MONTH -> {
                // 1..31 of month
                val days = (1..31).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = if (total > 0) total / 31.0 else 0.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0 && maxVal > 0) "${days[maxIdx]} Okt" else "7 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "1 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    midLabel = "15 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    endLabel = "31 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -13.0,
                    isPositive = true
                )
            }
            ChartPeriod.THREE_MONTHS -> {
                // Last 3 months: Agu, Sep, Okt
                val months = listOf(8, 9, 10)
                val monthTotals = months.map { m ->
                    expenseTxs.filter { it.monthIndex == m && it.year == currentYear }
                        .sumOf { it.amount }
                }
                val total = monthTotals.sum()
                val avg = total / 90.0
                val maxVal = monthTotals.maxOrNull() ?: 1.0
                val maxIdx = monthTotals.indexOf(maxVal)
                val peakLabel = if (maxIdx >= 0) "${monthNamesShort[months[maxIdx]]} $currentYear" else "Sep"

                val catGroups = expenseTxs.filter { it.monthIndex in months && it.year == currentYear }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(monthTotals)

                ChartSeriesData(
                    startLabel = "1 Agu",
                    midLabel = "15 Sep",
                    endLabel = "31 Okt",
                    points = normalized,
                    rawValues = monthTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakLabel,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -5.2,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_YEAR, ChartPeriod.ALL -> {
                // 12 months Jan..Des
                val months = (1..12).toList()
                val monthTotals = months.map { m ->
                    expenseTxs.filter { it.monthIndex == m && it.year == currentYear }
                        .sumOf { it.amount }
                }
                val total = monthTotals.sum()
                val avg = if (total > 0) total / 365.0 else 0.0
                val maxVal = monthTotals.maxOrNull() ?: 1.0
                val maxIdx = monthTotals.indexOf(maxVal)
                val peakLabel = if (maxIdx >= 0) monthNamesShort[months[maxIdx]] else "Okt"

                val catGroups = expenseTxs.filter { it.year == currentYear }.groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(monthTotals)

                ChartSeriesData(
                    startLabel = "Jan $currentYear",
                    midLabel = "Jun $currentYear",
                    endLabel = "Des $currentYear",
                    points = normalized,
                    rawValues = monthTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakLabel,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -10.4,
                    isPositive = true
                )
            }
        }
    }

    fun getNetWorthTrend(
        wallets: List<AccountWallet>,
        assets: List<AssetItem>,
        debts: List<DebtItem>,
        transactions: List<Transaction>,
        subTab: Int, // 0: Nilai Bersih, 1: Akun, 2: Aset, 3: Utang
        period: ChartPeriod,
        referenceDay: Int = 29,
        referenceMonth: Int = 10,
        referenceYear: Int = 2026
    ): ChartSeriesData {
        val totalWalletBal = wallets.sumOf { it.balance }
        val totalAssetsVal = assets.sumOf { it.value }
        val totalDebtVal = debts.filter { it.type == DebtType.BORROWED }.sumOf { it.totalAmount - it.paidAmount }
        val netWorthVal = totalWalletBal + totalAssetsVal - totalDebtVal

        val targetVal = when (subTab) {
            0 -> netWorthVal
            1 -> totalWalletBal
            2 -> totalAssetsVal
            else -> totalDebtVal
        }

        // Dynamically compute real dates & points
        val cal = Calendar.getInstance()
        val curDay = cal.get(Calendar.DAY_OF_MONTH)
        val curMonth = cal.get(Calendar.MONTH) + 1
        val curYear = cal.get(Calendar.YEAR)

        val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

        val rawList = mutableListOf<Double>()
        val startLbl: String
        val midLbl: String
        val endLbl: String

        when (period) {
            ChartPeriod.SEVEN_DAYS -> {
                // 7 days ending at current date
                val dayList = mutableListOf<Triple<Int, Int, Int>>() // day, month, year
                for (offset in 6 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_MONTH, -offset)
                    dayList.add(Triple(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR)))
                }

                startLbl = "${dayList.first().first} ${monthNames.getOrElse(dayList.first().second) { "" }}"
                midLbl = "${dayList[3].first} ${monthNames.getOrElse(dayList[3].second) { "" }}"
                endLbl = "${dayList.last().first} ${monthNames.getOrElse(dayList.last().second) { "" }}"

                val dailyCashFlows = dayList.map { (d, m, y) ->
                    val income = transactions.filter { it.year == y && it.monthIndex == m && it.dayOfMonth == d && it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = transactions.filter { it.year == y && it.monthIndex == m && it.dayOfMonth == d && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    income - expense
                }

                for (i in 0 until 7) {
                    val futureDelta = (i + 1 until 7).sumOf { dailyCashFlows[it] }
                    rawList.add((targetVal - futureDelta).coerceAtLeast(0.0))
                }
            }
            ChartPeriod.ONE_WEEK -> {
                val dayList = (1..7).map { (curDay - 7 + it).coerceAtLeast(1) }
                startLbl = "${dayList.first()} ${monthNames.getOrElse(curMonth) { "" }}"
                midLbl = "${dayList[3]} ${monthNames.getOrElse(curMonth) { "" }}"
                endLbl = "${dayList.last()} ${monthNames.getOrElse(curMonth) { "" }}"

                val dailyCashFlows = dayList.map { d ->
                    val income = transactions.filter { it.year == curYear && it.monthIndex == curMonth && it.dayOfMonth == d && it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = transactions.filter { it.year == curYear && it.monthIndex == curMonth && it.dayOfMonth == d && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    income - expense
                }
                for (i in 0 until 7) {
                    val futureDelta = (i + 1 until 7).sumOf { dailyCashFlows[it] }
                    rawList.add((targetVal - futureDelta).coerceAtLeast(0.0))
                }
            }
            ChartPeriod.ONE_MONTH -> {
                val daysCount = curDay.coerceIn(7, 31)
                startLbl = "1 ${monthNames.getOrElse(curMonth) { "" }}"
                midLbl = "${daysCount / 2} ${monthNames.getOrElse(curMonth) { "" }}"
                endLbl = "$daysCount ${monthNames.getOrElse(curMonth) { "" }}"

                val dailyCashFlows = (1..daysCount).map { d ->
                    val income = transactions.filter { it.year == curYear && it.monthIndex == curMonth && it.dayOfMonth == d && it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = transactions.filter { it.year == curYear && it.monthIndex == curMonth && it.dayOfMonth == d && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    income - expense
                }
                for (i in 0 until daysCount) {
                    val futureDelta = (i + 1 until daysCount).sumOf { dailyCashFlows[it] }
                    rawList.add((targetVal - futureDelta).coerceAtLeast(0.0))
                }
            }
            ChartPeriod.THREE_MONTHS -> {
                val m1 = if (curMonth - 2 <= 0) curMonth - 2 + 12 else curMonth - 2
                val m2 = if (curMonth - 1 <= 0) curMonth - 1 + 12 else curMonth - 1
                val m3 = curMonth
                val months = listOf(m1, m2, m3)

                startLbl = monthNames[m1]
                midLbl = monthNames[m2]
                endLbl = monthNames[m3]

                val monthlyCashFlows = months.map { m ->
                    val income = transactions.filter { it.monthIndex == m && it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = transactions.filter { it.monthIndex == m && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    income - expense
                }
                val m1Net = targetVal - monthlyCashFlows[1] - monthlyCashFlows[2]
                val m2Net = targetVal - monthlyCashFlows[2]
                val m3Net = targetVal
                rawList.addAll(listOf(
                    m1Net.coerceAtLeast(0.0),
                    (m1Net + (m2Net - m1Net) * 0.5).coerceAtLeast(0.0),
                    m2Net.coerceAtLeast(0.0),
                    (m2Net + (m3Net - m2Net) * 0.5).coerceAtLeast(0.0),
                    m3Net.coerceAtLeast(0.0)
                ))
            }
            ChartPeriod.ONE_YEAR, ChartPeriod.ALL -> {
                startLbl = "Jan $curYear"
                midLbl = "Jun $curYear"
                endLbl = "Des $curYear"

                val monthlyCashFlows = (1..12).map { m ->
                    val income = transactions.filter { it.year == curYear && it.monthIndex == m && it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expense = transactions.filter { it.year == curYear && it.monthIndex == m && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    income - expense
                }

                for (i in 0 until 12) {
                    val futureDelta = (i + 1 until 12).sumOf { monthlyCashFlows[it] }
                    rawList.add((targetVal - futureDelta).coerceAtLeast(0.0))
                }
            }
        }

        val startVal = rawList.firstOrNull() ?: targetVal
        val endVal = rawList.lastOrNull() ?: targetVal
        val calculatedDelta = endVal - startVal
        val percent = if (startVal > 0) {
            (calculatedDelta / startVal) * 100.0
        } else if (endVal > 0) {
            100.0
        } else {
            0.0
        }
        val isPos = calculatedDelta >= 0

        val normalized = normalizePoints(rawList)

        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = targetVal,
            averagePerDay = if (rawList.isNotEmpty()) rawList.average() else targetVal,
            peakValue = rawList.maxOrNull() ?: targetVal,
            peakDayLabel = endLbl,
            topCategory = "",
            topCategoryAmount = 0.0,
            percentChange = percent,
            isPositive = isPos,
            deltaAmount = calculatedDelta
        )
    }

    fun getAssetValuationTrend(
        asset: AssetItem,
        period: ChartPeriod
    ): ChartSeriesData {
        val (startLbl, midLbl, endLbl, count) = when (period) {
            ChartPeriod.SEVEN_DAYS -> Quadruple("22 Okt", "25 Okt", "29 Okt", 7)
            ChartPeriod.ONE_WEEK -> Quadruple("20 Okt", "24 Okt", "29 Okt", 7)
            ChartPeriod.ONE_MONTH -> Quadruple("30 Sep", "15 Okt", "29 Okt", 15)
            ChartPeriod.THREE_MONTHS -> Quadruple("29 Jul", "10 Sep", "29 Okt", 12)
            ChartPeriod.ONE_YEAR -> Quadruple("Nov 2025", "Mei 2026", "Okt 2026", 12)
            ChartPeriod.ALL -> Quadruple("30 Sep", "15 Okt", "29 Okt", 10)
        }

        val baseVal = asset.value
        val rawList = mutableListOf<Double>()
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).coerceAtLeast(1)
            // Steady upward growth for investments
            val v = baseVal * (0.75 + (0.25 * progress) + (kotlin.math.sin(progress * 4.0) * 0.03))
            rawList.add(v.coerceAtLeast(0.0))
        }

        val normalized = normalizePoints(rawList)
        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = baseVal,
            averagePerDay = baseVal / count,
            peakValue = baseVal,
            peakDayLabel = endLbl,
            topCategory = asset.assetType,
            topCategoryAmount = baseVal,
            percentChange = asset.performancePercent,
            isPositive = true
        )
    }

    fun getWalletBalanceTrend(
        wallet: AccountWallet,
        transactions: List<Transaction>,
        period: ChartPeriod
    ): ChartSeriesData {
        val (startLbl, midLbl, endLbl, count) = when (period) {
            ChartPeriod.SEVEN_DAYS -> Quadruple("22 Okt", "25 Okt", "28 Okt", 7)
            ChartPeriod.ONE_WEEK -> Quadruple("20 Okt", "23 Okt", "26 Okt", 7)
            ChartPeriod.ONE_MONTH -> Quadruple("1 Okt", "15 Okt", "28 Okt", 28)
            ChartPeriod.THREE_MONTHS -> Quadruple("Agu", "Sep", "Okt", 12)
            ChartPeriod.ONE_YEAR, ChartPeriod.ALL -> Quadruple("Jan", "Jun", "Okt", 10)
        }

        val curBal = wallet.balance
        val rawList = mutableListOf<Double>()
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).coerceAtLeast(1)
            val v = curBal * (1.2 - (0.2 * progress) + (kotlin.math.sin(progress * 5.0) * 0.08))
            rawList.add(v.coerceAtLeast(0.0))
        }

        val normalized = normalizePoints(rawList)
        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = curBal,
            averagePerDay = curBal / count,
            peakValue = rawList.maxOrNull() ?: curBal,
            peakDayLabel = startLbl,
            topCategory = wallet.bankType,
            topCategoryAmount = curBal,
            percentChange = -5.4,
            isPositive = false
        )
    }

    private fun normalizePoints(values: List<Double>): List<Float> {
        if (values.isEmpty()) return listOf(0.5f, 0.5f)
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 1.0
        val diff = (max - min).coerceAtLeast(1.0)

        return values.map {
            // Keep points comfortably inside 0.15f..0.90f range so lines look elegant
            (0.15f + ((it - min) / diff * 0.75f).toFloat()).coerceIn(0.05f, 0.95f)
        }
    }

    fun getCumulativeComparison(
        transactions: List<Transaction>,
        month: Int,
        year: Int,
        walletFilter: String = "Semua"
    ): CumulativeComparisonData {
        val filtered = transactions.filter {
            it.monthIndex == month && it.year == year &&
            (walletFilter == "Semua" || it.accountName.equals(walletFilter, ignoreCase = true))
        }

        val incomeTxs = filtered.filter { it.type == TransactionType.INCOME }
        val expenseTxs = filtered.filter { it.type == TransactionType.EXPENSE }

        val totalIncome = incomeTxs.sumOf { it.amount }
        val totalExpense = expenseTxs.sumOf { it.amount }
        val saved = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(0, 100) else 0

        var cumInc = 0.0
        var cumExp = 0.0
        val incProgression = mutableListOf<Double>()
        val expProgression = mutableListOf<Double>()

        for (day in 1..31) {
            cumInc += incomeTxs.filter { it.dayOfMonth == day }.sumOf { it.amount }
            cumExp += expenseTxs.filter { it.dayOfMonth == day }.sumOf { it.amount }
            incProgression.add(cumInc)
            expProgression.add(cumExp)
        }

        val maxVal = maxOf(totalIncome, totalExpense).coerceAtLeast(100.0)

        val normalizedInc = incProgression.map {
            (0.1f + (it / maxVal * 0.8f).toFloat()).coerceIn(0.05f, 0.95f)
        }
        val normalizedExp = expProgression.map {
            (0.1f + (it / maxVal * 0.8f).toFloat()).coerceIn(0.05f, 0.95f)
        }

        val isHigher = saved >= 0
        val note = if (isHigher) {
            "Pemasukan tetap unggul sepanjang periode (+Rp${CurrencyHelper.formatCompact(saved)})"
        } else {
            "Pengeluaran melebihi pemasukan (-Rp${CurrencyHelper.formatCompact(-saved)})"
        }

        return CumulativeComparisonData(
            incomePoints = normalizedInc,
            expensePoints = normalizedExp,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            savedAmount = saved,
            savingsRatePercent = savingsRate,
            summaryTakeaway = note,
            isIncomeHigher = isHigher
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
