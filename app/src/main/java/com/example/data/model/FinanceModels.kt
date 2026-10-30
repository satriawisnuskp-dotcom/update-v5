package com.example.data.model

import java.util.UUID

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class DebtType {
    BORROWED, // Dipinjam
    LENT      // Dipinjamkan
}

data class SubcategoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String = "",
    val isVisible: Boolean = true
)

data class MainCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val iconName: String = "",
    val isVisible: Boolean = true,
    val subcategories: List<SubcategoryItem> = emptyList()
)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val subcategory: String = "",
    val amount: Double,
    val type: TransactionType,
    val accountName: String,
    val dayOfMonth: Int,
    val monthIndex: Int, // 1 to 12 (10 = Oktober)
    val year: Int = 2026,
    val dayOfWeekName: String = "Sen",
    val note: String = "",
    val label: String = "",
    val recurrence: String = "Tidak pernah",
    val time: String = "18:51",
    val toAccountName: String = "",
    val transferFee: Double = 0.0,
    val targetAssetId: String = "",
    val targetAssetName: String = ""
)

data class AccountWallet(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bankType: String = "Bank",
    val balance: Double,
    val lastCategory: String = "Other",
    val lastAmount: Double = 0.0,
    val lastActivityTime: String = "2 hari lalu",
    val currency: String = "IDR",
    val iconName: String = "Bank",
    val excludeFromNetWorth: Boolean = false,
    val excludeFromBudget: Boolean = false
)

data class AssetRecord(
    val id: String = UUID.randomUUID().toString(),
    val assetId: String,
    val tag: String = "Buy", // "Buy", "Invest more", "Sold"
    val isIncrease: Boolean = true,
    val amount: Double,
    val sourceWalletName: String = "",
    val date: String = "7 Okt 2026",
    val time: String = "10.09",
    val note: String = ""
)

data class AssetItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val group: String = "Investasi", // "Tunai & Bank" or "Investasi"
    val assetType: String = "Bank Account", // "Bank Account", "Emas", "Saham", "Reksadana", "Kripto", "Properti", etc.
    val value: Double,
    val platform: String = "Bibit",
    val performancePercent: Double = 10.2,
    val currency: String = "IDR",
    val createdAtDate: String = "29 Okt 2026",
    val createdAtTime: String = "00.15",
    val valuationMethod: String = "Manual",
    val records: List<AssetRecord> = emptyList()
)

data class DebtItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: DebtType = DebtType.BORROWED,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: String
) {
    val progressPercent: Int
        get() = if (totalAmount > 0) ((paidAmount / totalAmount) * 100).toInt() else 0
}

data class UpcomingScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val accountName: String,
    val amount: Double,
    val isIncome: Boolean,
    val dateLabel: String, // e.g. "1 Nov"
    val dayLabel: String   // e.g. "Min"
)

data class BudgetCategory(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val allocated: Double,
    val spent: Double
) {
    val remaining: Double get() = (allocated - spent).coerceAtLeast(0.0)
    val percentage: Float get() = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f
}

data class UserSettings(
    val language: String = "Indonesia",
    val isDarkMode: Boolean = true,
    val primaryCurrency: String = "IDR",
    val monthlyStartDay: Int = 1,
    val textSizePercent: Int = 100,
    val cashAccountsOnly: Boolean = false,
    val showPlannedInBalance: Boolean = true,
    val saveReminder: Boolean = false,
    val billReminder: Boolean = false,
    val budgetAlert: Boolean = true,
    val passcodeEnabled: Boolean = false,
    val appPasscode: String = "1234",
    val themeColorName: String = "FinTrack Blue"
)

data class RecurringTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String,
    val subcategory: String = "",
    val accountName: String,
    val dayOfMonth: Int, // 1..31
    val note: String = "",
    val label: String = "",
    val isActive: Boolean = true,
    val autoDeduct: Boolean = true,
    val lastProcessedMonthYear: String = "" // format "M/yyyy", e.g. "10/2026"
)

enum class PlanSectionType {
    INCOME,
    EXPENSE,
    SAVINGS
}

data class PlannedItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "",
    val amount: Double,
    val section: PlanSectionType
)

data class FinancialPlan(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Pembagian Gaji",
    val monthIndex: Int = 10,
    val year: Int = 2026,
    val items: List<PlannedItem> = emptyList()
)

data class BackupRestoreResult(
    val transactionCount: Int,
    val walletCount: Int,
    val assetCount: Int,
    val debtCount: Int
) {
    val totalItems: Int get() = transactionCount + walletCount + assetCount + debtCount
}
