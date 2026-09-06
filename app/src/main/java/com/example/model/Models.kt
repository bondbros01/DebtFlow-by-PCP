package com.example.model

enum class NavTab {
  HOME,
  PEOPLE,
  PROJECTS,
  CALENDAR,
  LOG,
}

val AVAILABLE_PERSON_COLORS =
  listOf(
    0xFF7C3AEDL, // Vivid Purple
    0xFF2563EBL, // Ocean Blue
    0xFF059669L, // Emerald Green
    0xFFD97706L, // Amber Orange
    0xFFE11D48L, // Crimson Rose
    0xFF0D9488L, // Teal
    0xFFDB2777L, // Magenta Pink
    0xFF4F46E5L, // Deep Indigo
  )

data class PersonRecord(
  val id: String,
  val name: String,
  val colorHex: Long = 0xFF7C3AEDL,
  val notes: String = "",
)

data class ReminderItem(
  val id: String,
  val title: String,
  val relativeDateText: String,
  val amount: Double,
  val isPositive: Boolean,
  val secondaryText: String,
  val iconType: String,
  val isPaid: Boolean,
  val personName: String = "",
  val createdDate: String = "Sep 04, 2026",
  val paidDate: String? = null,
  val paymentMethod: String? = null,
  val settledByPaymentId: String? = null,
  val settledDebtIds: String? = null,
  val deductedAmount: Double? = null,
) {
  fun getAssociatedPerson(): String {
    if (personName.isNotBlank()) return personName
    if (secondaryText.startsWith("Payer: ", ignoreCase = true)) {
      return secondaryText.substring(7).trim()
    }
    return secondaryText.trim()
  }
}

data class ExpenseLineItem(
  val id: String,
  val participantName: String,
  val title: String = "",
  val contributionType: String, // "Cash", "Goods", "Services"
  val amount: Double,
  val relativeDate: String, // "today", "yesterday", "this week"
)

data class ProjectRecord(
  val id: String,
  val name: String,
  val totalEstimatedBudget: Double,
  val participantsCount: Int,
  val relativeTime: String,
  val lineItems: List<ExpenseLineItem>,
  val isSold: Boolean = false,
  val salePrice: Double? = null,
)

data class AuditLogEntry(
  val id: String,
  val narrative: String,
  val relativeTime: String,
  val amountText: String,
  val isUndone: Boolean = false,
  val undoAction: (() -> Unit)? = null,
  val redoAction: (() -> Unit)? = null,
)
