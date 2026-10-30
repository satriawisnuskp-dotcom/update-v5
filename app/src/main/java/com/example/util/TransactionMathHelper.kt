package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlin.math.abs

data class CategorySlice(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

object TransactionMathHelper {

    /**
     * Normalizes and cleans monetary values, preventing NaN, infinite, or negative amounts.
     * Prevents double-negative bugs where subtracting a negative would increase a balance.
     */
    fun sanitizeAmount(amount: Double): Double {
        return if (amount.isNaN() || amount.isInfinite()) {
            0.0
        } else {
            abs(amount)
        }
    }

    /**
     * Sanitizes transaction to ensure amount and transferFee are strictly positive,
     * ensuring math operations behave deterministically according to transaction type.
     */
    fun sanitizeTransaction(tx: Transaction): Transaction {
        val cleanAmount = sanitizeAmount(tx.amount)
        val cleanFee = sanitizeAmount(tx.transferFee)

        val resolvedType = when {
            tx.category.equals("Salary", ignoreCase = true) ||
            tx.category.equals("Extra Income", ignoreCase = true) ||
            tx.category.equals("Bonus", ignoreCase = true) ||
            tx.category.equals("Pemberian", ignoreCase = true) -> TransactionType.INCOME

            tx.category.equals("Transfer", ignoreCase = true) ||
            tx.category.equals("Transfer Antar Bank", ignoreCase = true) ||
            tx.category.equals("Transfer Aset", ignoreCase = true) ||
            tx.category.equals("Asset", ignoreCase = true) ||
            tx.category.equals("Aset", ignoreCase = true) -> TransactionType.TRANSFER

            else -> tx.type
        }

        return tx.copy(
            amount = cleanAmount,
            transferFee = cleanFee,
            type = resolvedType
        )
    }

    /**
     * Calculates the new balance for an account wallet when applying a transaction.
     * Rule:
     * - Income: newBalance = currentBalance + amount
     * - Expense: newBalance = currentBalance - amount
     * - Transfer (Source): newBalance = currentBalance - (amount + fee)
     * - Transfer (Destination): newBalance = currentBalance + amount
     */
    fun applyTransactionToBalance(
        currentBalance: Double,
        transaction: Transaction,
        isSourceAccount: Boolean
    ): Double {
        val cleanTx = sanitizeTransaction(transaction)
        return if (isSourceAccount) {
            when (cleanTx.type) {
                TransactionType.INCOME -> currentBalance + cleanTx.amount
                TransactionType.EXPENSE -> currentBalance - cleanTx.amount
                TransactionType.TRANSFER -> currentBalance - (cleanTx.amount + cleanTx.transferFee)
            }
        } else if (cleanTx.type == TransactionType.TRANSFER) {
            currentBalance + cleanTx.amount
        } else {
            currentBalance
        }
    }

    /**
     * Calculates the reversed balance for an account wallet when deleting or reverting a transaction.
     */
    fun revertTransactionFromBalance(
        currentBalance: Double,
        transaction: Transaction,
        isSourceAccount: Boolean
    ): Double {
        val cleanTx = sanitizeTransaction(transaction)
        return if (isSourceAccount) {
            when (cleanTx.type) {
                TransactionType.INCOME -> currentBalance - cleanTx.amount
                TransactionType.EXPENSE -> currentBalance + cleanTx.amount
                TransactionType.TRANSFER -> currentBalance + (cleanTx.amount + cleanTx.transferFee)
            }
        } else if (cleanTx.type == TransactionType.TRANSFER) {
            currentBalance - cleanTx.amount
        } else {
            currentBalance
        }
    }

    /**
     * Computes category slices for donut charts and breakdown lists.
     * Guarantees that:
     * 1. Total = exact sum of all slice amounts
     * 2. Percentage = (sliceAmount / total) * 100%
     * 3. Slices are sorted by amount descending
     */
    fun buildCategoryBreakdown(
        transactions: List<Transaction>,
        type: TransactionType,
        palette: List<Color>
    ): Pair<Double, List<CategorySlice>> {
        val filtered = transactions.filter { it.type == type }
        val grouped = filtered
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { sanitizeAmount(it.amount) } }

        if (grouped.isEmpty()) {
            return Pair(0.0, emptyList())
        }

        val total = grouped.values.sum()
        val slices = grouped.entries.mapIndexed { index, (cat, amt) ->
            val percentage = if (total > 0.0) ((amt / total) * 100.0).toFloat() else 0f
            CategorySlice(
                category = cat,
                amount = amt,
                percentage = percentage,
                color = palette[index % palette.size]
            )
        }.sortedByDescending { it.amount }

        return Pair(total, slices)
    }
}
