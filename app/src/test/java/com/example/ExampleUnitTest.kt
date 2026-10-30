package com.example

import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.CurrencyHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("Rp341.472", CurrencyHelper.formatRupiah(341472.0))
        assertEquals("Rp6.182.200", CurrencyHelper.formatRupiah(6182200.0))
        assertEquals("4.8M", CurrencyHelper.formatCompact(4800000.0))
        assertEquals("170K", CurrencyHelper.formatCompact(170000.0))
        assertEquals("-100K", CurrencyHelper.formatCalendarExpense(100000.0))
    }

    @Test
    fun testRepositoryInitialBalancesAndReactiveAddition() {
        val repo = FinanceRepository()
        val initialWallets = repo.wallets.value
        val bri = initialWallets.find { it.name == "BRI Utama" }
        val initialBalance = bri?.balance ?: 0.0

        // Add expense of 50.000 to BRI Utama
        val tx = Transaction(
            title = "Beli Kopi",
            category = "Food & Dining",
            amount = 50000.0,
            type = TransactionType.EXPENSE,
            accountName = "BRI Utama",
            dayOfMonth = 28,
            monthIndex = 10,
            dayOfWeekName = "Rab"
        )
        repo.addTransaction(tx)

        val updatedBri = repo.wallets.value.find { it.name == "BRI Utama" }
        assertEquals(initialBalance - 50000.0, updatedBri?.balance ?: 0.0, 0.01)
        assertTrue(repo.transactions.value.any { it.title == "Beli Kopi" })
    }

    @Test
    fun testNetWorthCalculation() {
        val repo = FinanceRepository()
        val totalWallets = repo.wallets.value.sumOf { it.balance }
        val totalAssets = repo.assets.value.sumOf { it.value }
        val totalDebts = repo.debts.value.sumOf { it.totalAmount - it.paidAmount }

        val netWorth = totalWallets + totalAssets - totalDebts
        assertTrue("Net worth should be positive", netWorth > 0)
    }

    @Test
    fun testMonthAndYearSelection() {
        val repo = FinanceRepository()
        val viewModel = com.example.ui.viewmodel.FinanceViewModel(repo)

        viewModel.selectMonthAndYear(11, 2027)
        assertEquals(11, viewModel.selectedMonthIndex.value)
        assertEquals(2027, viewModel.selectedYear.value)
    }

    @Test
    fun testRecurringTransactionAutoDeduct() {
        val repo = FinanceRepository()
        val bri = repo.wallets.value.find { it.name == "BRI Utama" }
        val initialBalance = bri?.balance ?: 0.0

        val recurring = com.example.data.model.RecurringTransaction(
            title = "Langganan Spotify Family",
            amount = 86000.0,
            type = TransactionType.EXPENSE,
            category = "Bills & Fees",
            accountName = "BRI Utama",
            dayOfMonth = 15,
            autoDeduct = true,
            isActive = true
        )
        // Add recurring transaction with currentDay = 28 (today is past day 15), so it should auto-deduct for month 10!
        repo.addRecurringTransaction(recurring, checkImmediateProcess = true, currentDay = 28, currentMonth = 10, currentYear = 2026)

        val updatedBri = repo.wallets.value.find { it.name == "BRI Utama" }
        assertEquals(initialBalance - 86000.0, updatedBri?.balance ?: 0.0, 0.01)

        val createdTx = repo.transactions.value.find { it.title == "Langganan Spotify Family" }
        assertTrue("Automatic transaction must be recorded in transactions list", createdTx != null)
        assertEquals(15, createdTx?.dayOfMonth)
        assertEquals(10, createdTx?.monthIndex)
    }

    @Test
    fun testPlannerCalculationsAndEdit() {
        val repo = FinanceRepository()
        val viewModel = com.example.ui.viewmodel.FinanceViewModel(repo)

        val incomeTotal = repo.plannedItems.value.filter { it.section == com.example.data.model.PlanSectionType.INCOME }.sumOf { it.amount }
        val expenseTotal = repo.plannedItems.value.filter { it.section == com.example.data.model.PlanSectionType.EXPENSE }.sumOf { it.amount }
        val savingsTotal = repo.plannedItems.value.filter { it.section == com.example.data.model.PlanSectionType.SAVINGS }.sumOf { it.amount }

        assertEquals(5758000.0, incomeTotal, 0.01)
        assertEquals(4150000.0, expenseTotal, 0.01)
        assertEquals(1600000.0, savingsTotal, 0.01)

        val unallocated = incomeTotal - (expenseTotal + savingsTotal)
        assertEquals(8000.0, unallocated, 0.01)

        // Test editing an item with pencil edit
        val gajiItem = repo.plannedItems.value.find { it.name == "Gaji Pokok" }
        assertTrue(gajiItem != null)
        repo.updatePlannedItem(gajiItem!!.copy(amount = 3000000.0))

        val updatedIncome = repo.plannedItems.value.filter { it.section == com.example.data.model.PlanSectionType.INCOME }.sumOf { it.amount }
        assertEquals(5758000.0 + (3000000.0 - 2684000.0), updatedIncome, 0.01)
    }
}
