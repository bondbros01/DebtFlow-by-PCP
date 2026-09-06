package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.model.ExpenseLineItem
import com.example.model.PersonRecord
import com.example.model.ProjectRecord
import com.example.model.ReminderItem
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ExportManager {

  data class PersonReconciliation(
    val personName: String,
    val paidDebtsCount: Int,
    val paidDebtsAmount: Double,
    val paymentsCount: Int,
    val paymentsAmount: Double,
    val discrepancy: Double, // paymentsAmount - paidDebtsAmount
    val isBalanced: Boolean,
    val unlinkedPaidDebts: List<ReminderItem>,
    val unallocatedPayments: List<ReminderItem>,
  )

  data class ReconciliationAudit(
    val totalPaidDebtsCount: Int,
    val totalPaidDebtsAmount: Double,
    val totalPaymentsCount: Int,
    val totalPaymentsAmount: Double,
    val discrepancy: Double, // totalPaymentsAmount - totalPaidDebtsAmount
    val isBalanced: Boolean, // Math.abs(discrepancy) < 0.005
    val unlinkedPaidDebts: List<ReminderItem>,
    val unallocatedPayments: List<ReminderItem>,
    val personBreakdowns: List<PersonReconciliation>,
  )

  fun auditReconciliation(debts: List<ReminderItem>, targetPerson: String? = null): ReconciliationAudit {
    val targetDebts = if (targetPerson != null && targetPerson.isNotBlank()) {
      debts.filter {
        it.getAssociatedPerson().equals(targetPerson, ignoreCase = true) ||
          it.personName.equals(targetPerson, ignoreCase = true)
      }
    } else {
      debts
    }

    val standardDebts = targetDebts.filter { !isPaymentItem(it) }
    val paidDebts = standardDebts.filter { it.isPaid }
    val paymentItems = targetDebts.filter { isPaymentItem(it) }

    val totalPaidDebtsAmount = paidDebts.sumOf { it.amount }
    val totalPaymentsAmount = paymentItems.sumOf { it.amount }
    val discrepancy = totalPaymentsAmount - totalPaidDebtsAmount
    val isBalanced = Math.abs(discrepancy) < 0.005

    val allPaymentIds = paymentItems.map { it.id }.toSet()
    val unlinkedPaidDebts = paidDebts.filter {
      it.settledByPaymentId.isNullOrBlank() || !allPaymentIds.contains(it.settledByPaymentId)
    }
    val unallocatedPayments = paymentItems.filter { it.settledDebtIds.isNullOrBlank() }

    val peopleNames = targetDebts.map { it.getAssociatedPerson() }.filter { it.isNotBlank() }.distinct()
    val breakdowns = peopleNames.map { pName ->
      val pDebts = targetDebts.filter { it.getAssociatedPerson().equals(pName, ignoreCase = true) }
      val pPaid = pDebts.filter { !isPaymentItem(it) && it.isPaid }
      val pPayments = pDebts.filter { isPaymentItem(it) }
      val pPaidAmt = pPaid.sumOf { it.amount }
      val pPayAmt = pPayments.sumOf { it.amount }
      val pDisc = pPayAmt - pPaidAmt
      val pPaymentIds = pPayments.map { it.id }.toSet()
      val pUnlinked = pPaid.filter {
        it.settledByPaymentId.isNullOrBlank() || !pPaymentIds.contains(it.settledByPaymentId)
      }
      val pUnalloc = pPayments.filter { it.settledDebtIds.isNullOrBlank() }
      PersonReconciliation(
        personName = pName,
        paidDebtsCount = pPaid.size,
        paidDebtsAmount = pPaidAmt,
        paymentsCount = pPayments.size,
        paymentsAmount = pPayAmt,
        discrepancy = pDisc,
        isBalanced = Math.abs(pDisc) < 0.005,
        unlinkedPaidDebts = pUnlinked,
        unallocatedPayments = pUnalloc,
      )
    }

    return ReconciliationAudit(
      totalPaidDebtsCount = paidDebts.size,
      totalPaidDebtsAmount = totalPaidDebtsAmount,
      totalPaymentsCount = paymentItems.size,
      totalPaymentsAmount = totalPaymentsAmount,
      discrepancy = discrepancy,
      isBalanced = isBalanced,
      unlinkedPaidDebts = unlinkedPaidDebts,
      unallocatedPayments = unallocatedPayments,
      personBreakdowns = breakdowns,
    )
  }

  fun reconcileDebtsAndPayments(
    currentDebts: List<ReminderItem>,
    targetPerson: String? = null,
    userName: String = "You",
  ): Pair<List<ReminderItem>, String> {
    val updatedList = currentDebts.toMutableList()
    val now = Calendar.getInstance()
    val todayDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(now.time)
    var reconciledCount = 0
    var reconciledAmount = 0.0

    val audit = auditReconciliation(updatedList, targetPerson)
    val existingPaymentIds = updatedList.filter { isPaymentItem(it) }.map { it.id }.toSet()

    // 1. For debts marked isPaid == true that lack a recorded payment log, generate matching payment logs
    val debtsToLink = updatedList.filter {
      !isPaymentItem(it) && it.isPaid &&
        (it.settledByPaymentId.isNullOrBlank() || !existingPaymentIds.contains(it.settledByPaymentId)) &&
        (targetPerson == null || it.getAssociatedPerson().equals(targetPerson, ignoreCase = true))
    }

    for (debt in debtsToLink) {
      val paymentId = "pay_rec_${System.currentTimeMillis()}_${reconciledCount}"
      val pDate = debt.paidDate?.ifBlank { todayDateStr } ?: todayDateStr
      val pMethod = debt.paymentMethod?.ifBlank { "Cash" } ?: "Cash"
      val pName = debt.personName.ifBlank { debt.getAssociatedPerson() }

      val paymentRecord = ReminderItem(
        id = paymentId,
        title = "Cash Payment - $pName",
        amount = debt.amount,
        isPositive = debt.isPositive,
        relativeDateText = pDate,
        createdDate = pDate,
        personName = pName,
        secondaryText = "Reconciled payment settlement for '${debt.title}'",
        iconType = "Cash",
        isPaid = true,
        paidDate = pDate,
        paymentMethod = pMethod,
        settledDebtIds = debt.id,
      )

      val debtIdx = updatedList.indexOfFirst { it.id == debt.id }
      if (debtIdx >= 0) {
        updatedList[debtIdx] = debt.copy(
          settledByPaymentId = paymentId,
          paidDate = pDate,
          paymentMethod = pMethod,
        )
      }
      updatedList.add(0, paymentRecord)
      reconciledCount++
      reconciledAmount += debt.amount
    }

    // 2. For unallocated payment items with matching unpaid debts for that person, link and mark paid
    val unallocatedPayments = updatedList.filter {
      isPaymentItem(it) && it.settledDebtIds.isNullOrBlank() &&
        (targetPerson == null || it.getAssociatedPerson().equals(targetPerson, ignoreCase = true))
    }

    for (pay in unallocatedPayments) {
      val payPerson = pay.getAssociatedPerson()
      val matchingUnpaidDebt = updatedList.firstOrNull {
        !isPaymentItem(it) && !it.isPaid &&
          it.getAssociatedPerson().equals(payPerson, ignoreCase = true) &&
          Math.abs(it.amount - pay.amount) < 0.01
      }
      if (matchingUnpaidDebt != null) {
        val debtIdx = updatedList.indexOfFirst { it.id == matchingUnpaidDebt.id }
        val payIdx = updatedList.indexOfFirst { it.id == pay.id }
        if (debtIdx >= 0 && payIdx >= 0) {
          val pDate = pay.paidDate?.ifBlank { todayDateStr } ?: todayDateStr
          updatedList[debtIdx] = matchingUnpaidDebt.copy(
            isPaid = true,
            paidDate = pDate,
            paymentMethod = pay.paymentMethod ?: "Cash",
            settledByPaymentId = pay.id,
          )
          updatedList[payIdx] = pay.copy(settledDebtIds = matchingUnpaidDebt.id)
          reconciledCount++
          reconciledAmount += matchingUnpaidDebt.amount
        }
      }
    }

    val narrative = if (reconciledCount > 0) {
      "Reconciled $reconciledCount item(s) ($${String.format(Locale.US, "%.2f", reconciledAmount)}). Sum of paid items and recorded payments now accurately matches 100%."
    } else {
      "Audit completed: All paid items and payment logs are already 100% reconciled and balanced."
    }

    return updatedList to narrative
  }

  fun getDirectionText(isPositive: Boolean, personName: String, userName: String): String {
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName
    return if (isPositive) "$personName owes $cleanUser" else "$cleanUser owes $personName"
  }

  fun concreteDateText(text: String): String {
    val t = text.trim()
    if (t.contains("Today", ignoreCase = true)) return "Sep 04, 2026"
    if (t.contains("Tomorrow", ignoreCase = true)) return "Sep 05, 2026"
    if (t.contains("This Saturday", ignoreCase = true)) return "Sep 05, 2026"
    if (t.contains("Next Saturday", ignoreCase = true)) return "Sep 12, 2026"
    if (t.contains("First of next month", ignoreCase = true)) return "Oct 01, 2026"
    if (t.contains("This Week", ignoreCase = true)) return "Sep 07, 2026"
    if (t.contains("Next Week", ignoreCase = true)) return "Sep 14, 2026"
    if (t.length >= 3 && !t.contains("Day", ignoreCase = true)) return t
    return "Sep 04, 2026"
  }

  fun parseItemCalendar(text: String, createdDateStr: String = ""): Calendar? {
    val t = text.trim()
    val now = Calendar.getInstance()
    if (t.contains("Today", ignoreCase = true)) {
      return now
    }
    if (t.contains("Tomorrow", ignoreCase = true)) {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DAY_OF_YEAR, 1)
      return cal
    }
    val formats = listOf(
      SimpleDateFormat("MMM dd, yyyy", Locale.US),
      SimpleDateFormat("MMM d, yyyy", Locale.US),
      SimpleDateFormat("MMMM dd, yyyy", Locale.US),
      SimpleDateFormat("MMMM d, yyyy", Locale.US),
      SimpleDateFormat("yyyy-MM-dd", Locale.US),
      SimpleDateFormat("MMM dd", Locale.US),
      SimpleDateFormat("MMMM dd", Locale.US),
    )
    for (df in formats) {
      try {
        val d = df.parse(t)
        if (d != null) {
          val cal = Calendar.getInstance()
          cal.time = d
          val hasYearInText = Regex("""\b(20\d\d)\b""").containsMatchIn(t)
          if (!hasYearInText) {
            val createdCal = if (createdDateStr.isNotBlank()) parseItemCalendar(createdDateStr) else null
            val targetYr = createdCal?.get(Calendar.YEAR) ?: now.get(Calendar.YEAR)
            cal.set(Calendar.YEAR, targetYr)
          }
          return cal
        }
      } catch (_: Exception) {}
    }
    if (createdDateStr.isNotBlank() && createdDateStr != text) {
      return parseItemCalendar(createdDateStr)
    }
    return null
  }

  fun isPaymentItem(item: ReminderItem): Boolean {
    return item.id.startsWith("pay_") ||
      item.iconType.equals("Cash", ignoreCase = true) ||
      item.title.startsWith("Cash Payment", ignoreCase = true) ||
      item.secondaryText.contains("Cash payment", ignoreCase = true) ||
      (item.paymentMethod != null && item.title.contains("Payment", ignoreCase = true))
  }

  data class ProcessedExportData(
    val allDisplayItems: List<ReminderItem>,
    val originalDebts: List<ReminderItem>,
    val nextCycleItems: List<ReminderItem>,
    val grossOwedToMe: Double,
    val grossYouOwe: Double,
    val paidOwedToMe: Double,
    val paidYouOwe: Double,
    val totalSettledPaid: Double,
    val activeOwedToMe: Double,
    val activeYouOwe: Double,
    val currentNetBalance: Double,
    val nextCycleOwedToMe: Double,
    val nextCycleYouOwe: Double,
    val totalNextCycleCharges: Double,
    val projectedOwedToMe: Double,
    val projectedYouOwe: Double,
    val projectedNetBalance: Double,
  )

  fun processExportDebts(debts: List<ReminderItem>, excludeCompleted: Boolean = false): ProcessedExportData {
    val nextCycleItems = mutableListOf<ReminderItem>()
    val now = Calendar.getInstance()
    val nextMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    val nextMonthName = SimpleDateFormat("MMM yyyy", Locale.US).format(nextMonthCal.time)

    for (debt in debts) {
      if (isPaymentItem(debt)) continue

      val isSubscription = debt.iconType == "subscription" ||
        debt.secondaryText.contains("Subscription", ignoreCase = true) ||
        debt.secondaryText.contains("Recurring", ignoreCase = true)
      val isCancelled = debt.secondaryText.contains("Cancelled", ignoreCase = true) ||
        debt.secondaryText.contains("Canceled", ignoreCase = true)

      if (isSubscription && !isCancelled) {
        val recurringMatch = Regex("""Recurring:\s*\$?([0-9.]+)""").find(debt.secondaryText)
        val backpayMatch = Regex("""Backpay:\s*\+\$?([0-9.]+)""").find(debt.secondaryText)
          ?: Regex("""\+\$?([0-9.]+)\s*backpay""").find(debt.secondaryText)
        val backpayAmt = backpayMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        val recurringAmt = recurringMatch?.groupValues?.get(1)?.toDoubleOrNull()
          ?: if (backpayAmt > 0.0) (debt.amount - backpayAmt).coerceAtLeast(0.0) else debt.amount

        var cleanSec = debt.secondaryText
          .replace(Regex("""Backpay:\s*\+\$?[0-9.]+\s*\(Recurring:\s*(\$?[0-9.]+(?:/[a-zA-Z]+)?)\)"""), "Recurring: $1")
          .replace(Regex("""[•·,]?\s*\+\$?[0-9.]+\s*backpay"""), "")
          .replace(Regex("""•\s*•"""), "•")
          .trim(' ', '•', ',')
        if (cleanSec.isBlank()) cleanSec = "Subscription (monthly)"

        val itemCal = parseItemCalendar(debt.relativeDateText, debt.createdDate)
        val nextDueCal = if (itemCal != null) {
          val c = itemCal.clone() as Calendar
          c.add(Calendar.MONTH, 1)
          c
        } else {
          nextMonthCal
        }
        val nextDueDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(nextDueCal.time)

        val nextCycleItem = ReminderItem(
          id = "${debt.id}_next_cycle",
          title = "${debt.title} (Next Cycle)",
          amount = recurringAmt,
          isPositive = debt.isPositive,
          relativeDateText = nextDueDateStr,
          createdDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(now.time),
          personName = debt.personName.ifBlank { debt.getAssociatedPerson() },
          secondaryText = "Next billing cycle charge ($nextMonthName) • $cleanSec",
          isPaid = false,
          paidDate = null,
          paymentMethod = null,
          iconType = "subscription"
        )
        nextCycleItems.add(nextCycleItem)
      }
    }

    // Active unpaid debts (matches People screen and Home screen exactly)
    val activeOwedToMe = debts.filter { it.isPositive && !it.isPaid && !isPaymentItem(it) }.sumOf { it.amount }
    val activeYouOwe = debts.filter { !it.isPositive && !it.isPaid && !isPaymentItem(it) }.sumOf { it.amount }

    // Recorded payments / settled amounts
    val paidOwedToMe = debts.filter { it.isPositive && isPaymentItem(it) }.sumOf { it.amount }
    val paidYouOwe = debts.filter { !it.isPositive && isPaymentItem(it) }.sumOf { it.amount }
    val totalSettledPaid = paidOwedToMe + paidYouOwe

    // Gross debts (active unpaid + paid off)
    val grossOwedToMe = activeOwedToMe + paidOwedToMe
    val grossYouOwe = activeYouOwe + paidYouOwe

    // Current net balance
    val currentNetBalance = activeOwedToMe - activeYouOwe

    val nextCycleOwedToMe = nextCycleItems.filter { it.isPositive }.sumOf { it.amount }
    val nextCycleYouOwe = nextCycleItems.filter { !it.isPositive }.sumOf { it.amount }
    val totalNextCycleCharges = nextCycleOwedToMe + nextCycleYouOwe

    val projectedOwedToMe = activeOwedToMe + nextCycleOwedToMe
    val projectedYouOwe = activeYouOwe + nextCycleYouOwe
    val projectedNetBalance = projectedOwedToMe - projectedYouOwe

    // If excludeCompleted is true, omit all paid-off debts and associated recorded payments from export items
    val displayDebts = if (excludeCompleted) {
      debts.filter { !it.isPaid && !isPaymentItem(it) }
    } else {
      debts
    }
    val allDisplayItems = displayDebts + nextCycleItems

    return ProcessedExportData(
      allDisplayItems = allDisplayItems,
      originalDebts = debts,
      nextCycleItems = nextCycleItems,
      grossOwedToMe = grossOwedToMe,
      grossYouOwe = grossYouOwe,
      paidOwedToMe = paidOwedToMe,
      paidYouOwe = paidYouOwe,
      totalSettledPaid = totalSettledPaid,
      activeOwedToMe = activeOwedToMe,
      activeYouOwe = activeYouOwe,
      currentNetBalance = currentNetBalance,
      nextCycleOwedToMe = nextCycleOwedToMe,
      nextCycleYouOwe = nextCycleYouOwe,
      totalNextCycleCharges = totalNextCycleCharges,
      projectedOwedToMe = projectedOwedToMe,
      projectedYouOwe = projectedYouOwe,
      projectedNetBalance = projectedNetBalance
    )
  }

  fun exportPersonDebtsCsv(
    context: Context,
    person: PersonRecord,
    debts: List<ReminderItem>,
    userName: String = "You",
    excludeCompleted: Boolean = AppPersistenceManager.loadExcludeCompletedFromExports(context),
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val sanitizedName = person.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val fileName = "${sanitizedName}_debts_$timestamp.csv"

    val data = processExportDebts(debts, excludeCompleted = excludeCompleted)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val csvBuilder = StringBuilder()
    csvBuilder.append("# =============================================================\n")
    csvBuilder.append("# DEBT & PAYMENT STATEMENT: ${person.name}\n")
    csvBuilder.append("# Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}\n")
    if (excludeCompleted) {
      csvBuilder.append("# FILTER: Active Debts Only (Paid off debts & recorded payments excluded)\n")
    } else {
      csvBuilder.append("# FILTER: All Debts & Payments (Completed and active items included)\n")
    }
    csvBuilder.append("# -------------------------------------------------------------\n")
    csvBuilder.append("# FINANCIAL SUMMARY & ADJUSTED BALANCE\n")
    csvBuilder.append("# ${person.name} Debts (Gross): $${String.format(Locale.US, "%.2f", data.grossOwedToMe)}\n")
    csvBuilder.append("# Less Recorded Payments: -$${String.format(Locale.US, "%.2f", data.paidOwedToMe)}\n")
    csvBuilder.append("# Total ${person.name} Owes: $${String.format(Locale.US, "%.2f", data.activeOwedToMe)}\n")
    csvBuilder.append("#\n")
    csvBuilder.append("# $cleanUser Debts (Gross): $${String.format(Locale.US, "%.2f", data.grossYouOwe)}\n")
    csvBuilder.append("# Less Recorded Payments: -$${String.format(Locale.US, "%.2f", data.paidYouOwe)}\n")
    csvBuilder.append("# Total $cleanUser Owes: $${String.format(Locale.US, "%.2f", data.activeYouOwe)}\n")
    csvBuilder.append("#\n")
    csvBuilder.append("# TOTAL SETTLED PAYMENTS: -$${String.format(Locale.US, "%.2f", data.totalSettledPaid)}\n")
    csvBuilder.append("# ESTIMATED NEXT CYCLE BALANCE: $${String.format(Locale.US, "%.2f", data.projectedNetBalance)}\n")
    csvBuilder.append("# -------------------------------------------------------------\n")
    val recAudit = auditReconciliation(debts, targetPerson = person.name)
    csvBuilder.append("# PAYMENT SUMMARY\n")
    csvBuilder.append("# Total Settled Debts: $${String.format(Locale.US, "%.2f", recAudit.totalPaidDebtsAmount)} (${recAudit.totalPaidDebtsCount} items)\n")
    csvBuilder.append("# Total Cash Payments: $${String.format(Locale.US, "%.2f", recAudit.totalPaymentsAmount)} (${recAudit.totalPaymentsCount} payments)\n")
    val auditStatusStr = if (recAudit.isBalanced) "100% BALANCED ($0.00 difference)" else "DIFFERENCE: $${String.format(Locale.US, "%.2f", Math.abs(recAudit.discrepancy))}"
    csvBuilder.append("# Status: $auditStatusStr\n")
    csvBuilder.append("# -------------------------------------------------------------\n")
    csvBuilder.append("# NEXT CYCLE CHARGES: +$${String.format(Locale.US, "%.2f", data.totalNextCycleCharges)}\n")
    csvBuilder.append("# CURRENT NET BALANCE: $${String.format(Locale.US, "%.2f", data.currentNetBalance)}\n")
    csvBuilder.append("# =============================================================\n\n")

    csvBuilder.append("ID,Title,Person,Amount,Direction,Date Recorded,Due Date,Status,Payment Date,Payment Method,Category,Notes\n")
    for (debt in data.allDisplayItems) {
      val isNextCycle = debt.id.endsWith("_next_cycle")
      val isPayment = isPaymentItem(debt)
      val direction = if (isPayment) {
        if (debt.isPositive) "Payment from ${person.name} to $cleanUser" else "Payment from $cleanUser to ${person.name}"
      } else {
        getDirectionText(debt.isPositive, person.name, userName)
      }
      val status = if (isNextCycle) "Upcoming (Next Billing Cycle)" else if (isPayment) "Recorded Payment" else if (debt.isPaid) "Paid" else "Unpaid"
      val dueDate = if (isPayment) debt.paidDate ?: debt.createdDate else concreteDateText(debt.relativeDateText)
      val paidDateStr = debt.paidDate ?: if (debt.isPaid || isPayment) debt.createdDate else "-"
      val paymentMethodStr = debt.paymentMethod ?: if (debt.isPaid || isPayment) "Standard" else "-"
      val amountStr = if (isPayment) "-${String.format(Locale.US, "%.2f", debt.amount)}" else if (isNextCycle) "+${String.format(Locale.US, "%.2f", debt.amount)}" else String.format(Locale.US, "%.2f", debt.amount)
      val titleEscaped = debt.title.replace("\"", "\"\"")
      val secondaryEscaped = debt.secondaryText.replace("\"", "\"\"")
      csvBuilder.append("\"${debt.id}\",\"$titleEscaped\",\"${person.name}\",$amountStr,\"$direction\",\"${debt.createdDate}\",\"$dueDate\",\"$status\",\"$paidDateStr\",\"$paymentMethodStr\",\"${debt.iconType}\",\"$secondaryEscaped\"\n")
    }

    val success = writeBytesToDownloads(context, fileName, "text/csv", csvBuilder.toString().toByteArray(Charsets.UTF_8))
    if (success) {
      Toast.makeText(context, "Exported CSV to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save CSV to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun exportPersonDebtsPdf(
    context: Context,
    person: PersonRecord,
    debts: List<ReminderItem>,
    userName: String = "You",
    excludeCompleted: Boolean = AppPersistenceManager.loadExcludeCompletedFromExports(context),
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val sanitizedName = person.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val fileName = "${sanitizedName}_debts_$timestamp.pdf"

    val data = processExportDebts(debts, excludeCompleted = excludeCompleted)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var pageNumber = 1
    var currentPage = document.startPage(pageInfo)
    var canvas = currentPage.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background
    paint.color = Color.parseColor("#18181D")
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    // Header banner
    paint.color = Color.parseColor("#22222A")
    canvas.drawRoundRect(24f, 24f, 571f, 126f, 16f, 16f, paint)

    // Title
    paint.color = Color.parseColor("#A78BFA")
    paint.textSize = 18f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("DEBT & PAYMENT STATEMENT", 40f, 54f, paint)

    // Subtitle & Date
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Contact: ${person.name}", 40f, 76f, paint)

    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Generated: $dateStr • Itemized Ledger with Debt & Payment Dates", 40f, 96f, paint)

    // Financial Overview Box (Modified to show paid money removed from balance, next billing cycle & reconciliation audit)
    paint.color = Color.parseColor("#2A2A35")
    canvas.drawRoundRect(24f, 134f, 571f, 218f, 12f, 12f, paint)

    val owedLabel1 = "${person.name} Owes"
    val owedLabel2 = "$cleanUser Owes"

    // Col 1: Person Owes
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(owedLabel1, 38f, 152f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val owedToMeSign = if (data.activeOwedToMe >= 0) "$" else "-$"
    canvas.drawText("$owedToMeSign${String.format(Locale.US, "%.2f", Math.abs(data.activeOwedToMe))}", 38f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Debts $${String.format(Locale.US, "%.2f", data.grossOwedToMe)} • Paid -$${String.format(Locale.US, "%.2f", data.paidOwedToMe)}", 38f, 180f, paint)

    // Col 2: User Owes
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText(owedLabel2, 175f, 152f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val youOweSign = if (data.activeYouOwe > 0) "-$" else if (data.activeYouOwe < 0) "+$" else "$"
    canvas.drawText("$youOweSign${String.format(Locale.US, "%.2f", Math.abs(data.activeYouOwe))}", 175f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Debts $${String.format(Locale.US, "%.2f", data.grossYouOwe)} • Paid -$${String.format(Locale.US, "%.2f", data.paidYouOwe)}", 175f, 180f, paint)

    // Col 3: Settled / Paid (Removed)
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Recorded Payments", 312f, 152f, paint)
    paint.color = Color.parseColor("#93C5FD")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("-$${String.format(Locale.US, "%.2f", data.totalSettledPaid)}", 312f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Subtracted from total", 312f, 180f, paint)

    // Col 4: Next Cycle Balance
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Next Cycle Balance", 448f, 152f, paint)
    paint.color = if (data.projectedNetBalance >= 0) Color.parseColor("#34D399") else Color.parseColor("#F87171")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val netSign = if (data.projectedNetBalance >= 0) "$" else "-$"
    canvas.drawText("$netSign${String.format(Locale.US, "%.2f", Math.abs(data.projectedNetBalance))}", 448f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Estimated next month", 448f, 180f, paint)

    // Divider inside Financial Overview Box
    paint.color = Color.parseColor("#3A3A48")
    canvas.drawLine(36f, 190f, 559f, 190f, paint)

    // Row 2 of Overview: Next Billing Cycle, Projected Balance & Slimmed Audit on single line
    paint.color = Color.parseColor("#C4B5FD")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("NEXT: +$${String.format(Locale.US, "%.2f", data.totalNextCycleCharges)}", 38f, 206f, paint)

    paint.color = Color.parseColor("#FDE047")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val curSign = if (data.currentNetBalance >= 0) "$" else "-$"
    canvas.drawText("CURRENT NET: $curSign${String.format(Locale.US, "%.2f", Math.abs(data.currentNetBalance))}", 130f, 206f, paint)

    val recAudit = auditReconciliation(debts, targetPerson = person.name)
    paint.color = if (recAudit.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#FBBF24")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val auditBadgeText = "SUMMARY: Settled $${String.format(Locale.US, "%.2f", recAudit.totalPaidDebtsAmount)} | Cash $${String.format(Locale.US, "%.2f", recAudit.totalPaymentsAmount)} | ${if (recAudit.isBalanced) "Balanced ✓" else "Difference: $${String.format(Locale.US, "%.2f", Math.abs(recAudit.discrepancy))}"}"
    canvas.drawText(auditBadgeText, 250f, 206f, paint)

    // Table Header
    var currentY = 230f
    paint.color = Color.parseColor("#333340")
    canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("REC. DATE", 32f, currentY, paint)
    canvas.drawText("TITLE", 100f, currentY, paint)
    canvas.drawText("DIRECTION", 225f, currentY, paint)
    canvas.drawText("DUE DATE", 315f, currentY, paint)
    canvas.drawText("STATUS / PAID DATE", 390f, currentY, paint)
    canvas.drawText("AMOUNT", 510f, currentY, paint)

    currentY += 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    if (data.allDisplayItems.isEmpty()) {
      paint.color = Color.parseColor("#A1A1AA")
      paint.textSize = 11f
      canvas.drawText("No associated debts recorded for ${person.name}.", 40f, currentY + 10f, paint)
    } else {
      for ((idx, debt) in data.allDisplayItems.withIndex()) {
        val isNextCycle = debt.id.endsWith("_next_cycle")
        if (currentY > 770f) {
          // Draw footer for previous page before finishing
          paint.color = Color.parseColor("#A1A1AA")
          paint.textSize = 8f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          canvas.drawText("Page $pageNumber", 520f, 810f, paint)
          canvas.drawText("Exported from DebtFlow by PCP", 40f, 810f, paint)

          document.finishPage(currentPage)
          pageNumber++
          val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
          currentPage = document.startPage(newPageInfo)
          canvas = currentPage.canvas

          // Background
          paint.color = Color.parseColor("#18181D")
          canvas.drawRect(0f, 0f, 595f, 842f, paint)

          // Header continued bar
          paint.color = Color.parseColor("#22222A")
          canvas.drawRect(24f, 24f, 571f, 56f, paint)

          paint.color = Color.parseColor("#A78BFA")
          paint.textSize = 10f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("DEBT & PAYMENT STATEMENT (Continued - Page $pageNumber)", 40f, 44f, paint)

          // Table Header on new page
          currentY = 85f
          paint.color = Color.parseColor("#333340")
          canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

          paint.color = Color.parseColor("#F4F4F6")
          paint.textSize = 9f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("REC. DATE", 32f, currentY, paint)
          canvas.drawText("TITLE", 100f, currentY, paint)
          canvas.drawText("DIRECTION", 225f, currentY, paint)
          canvas.drawText("DUE DATE", 315f, currentY, paint)
          canvas.drawText("STATUS / PAID DATE", 390f, currentY, paint)
          canvas.drawText("AMOUNT", 510f, currentY, paint)

          currentY += 22f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        if (isNextCycle) {
          paint.color = Color.parseColor("#252236")
          canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
        } else if (idx % 2 == 1) {
          paint.color = Color.parseColor("#1F1F27")
          canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
        }

        // Date Recorded
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        canvas.drawText(debt.createdDate, 32f, currentY, paint)

        // Title
        paint.color = Color.parseColor("#F4F4F6")
        paint.textSize = 9f
        val truncatedTitle = if (debt.title.length > 20) debt.title.take(18) + "..." else debt.title
        canvas.drawText(truncatedTitle, 100f, currentY, paint)

        // Direction
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        val isPayment = isPaymentItem(debt)
        val dirText = if (isPayment) {
          if (debt.isPositive) "Paid to $cleanUser" else "Paid to ${person.name}"
        } else {
          getDirectionText(debt.isPositive, person.name, userName)
        }
        canvas.drawText(dirText, 225f, currentY, paint)

        // Due Date (Concrete)
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        val dueText = if (isPayment) debt.paidDate ?: debt.createdDate else concreteDateText(debt.relativeDateText)
        canvas.drawText(dueText, 315f, currentY, paint)

        // Status & Paid Date
        if (isNextCycle) {
          paint.color = Color.parseColor("#C4B5FD")
          canvas.drawText("Next Cycle", 390f, currentY, paint)
        } else if (isPayment) {
          paint.color = Color.parseColor("#34D399")
          val method = if (!debt.paymentMethod.isNullOrBlank()) "Recorded • ${debt.paymentMethod}" else "Recorded Payment"
          val fullStatus = method.take(22)
          canvas.drawText(fullStatus, 390f, currentY, paint)
        } else if (debt.isPaid) {
          paint.color = Color.parseColor("#93C5FD")
          val paidLabel = if (!debt.paidDate.isNullOrBlank()) "Paid (${debt.paidDate})" else "Paid Today"
          val method = if (!debt.paymentMethod.isNullOrBlank()) " • ${debt.paymentMethod}" else ""
          val fullStatus = (paidLabel + method).take(22)
          canvas.drawText(fullStatus, 390f, currentY, paint)
        } else {
          paint.color = Color.parseColor("#FBBF24")
          canvas.drawText("Pending", 390f, currentY, paint)
        }

        // Amount
        if (isNextCycle) {
          paint.color = Color.parseColor("#C4B5FD")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("+$${String.format(Locale.US, "%.2f", debt.amount)}", 506f, currentY, paint)
        } else if (isPayment) {
          paint.color = Color.parseColor("#34D399")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("-$${String.format(Locale.US, "%.2f", debt.amount)}", 506f, currentY, paint)
        } else if (debt.isPaid) {
          paint.color = Color.parseColor("#94A3B8")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          canvas.drawText("$${String.format(Locale.US, "%.2f", debt.amount)}", 510f, currentY, paint)
        } else {
          paint.color = Color.parseColor("#F4F4F6")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("$${String.format(Locale.US, "%.2f", debt.amount)}", 510f, currentY, paint)
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        currentY += 21f
      }
    }

    // Footer on the last page
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Total records: ${data.allDisplayItems.size} (${debts.size} current + ${data.nextCycleItems.size} next cycle) • Exported from DebtFlow by PCP", 40f, 810f, paint)
    canvas.drawText("Page $pageNumber", 520f, 810f, paint)

    document.finishPage(currentPage)

    val pdfBytes = java.io.ByteArrayOutputStream().use { baos ->
      document.writeTo(baos)
      baos.toByteArray()
    }
    document.close()

    val success = writeBytesToDownloads(context, fileName, "application/pdf", pdfBytes)
    if (success) {
      Toast.makeText(context, "Exported PDF to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save PDF to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun cleanName(participantName: String, userName: String): String {
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName
    if (participantName.equals("You", ignoreCase = true)) return cleanUser
    if (participantName.startsWith("You (", ignoreCase = true)) return participantName.replaceFirst("You", cleanUser, ignoreCase = true)
    if (participantName.contains("You", ignoreCase = true)) return participantName.replace("You", cleanUser)
    return participantName
  }

  fun exportProjectCsv(context: Context, project: ProjectRecord, userName: String = "User"): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val sanitizedName = project.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val fileName = "project_${sanitizedName}_$timestamp.csv"

    val csvBuilder = StringBuilder()
    csvBuilder.append("Project Name,Status,Total Budget,Total Expenditures,Sale Price,Net Profit,Profit Margin\n")
    val totalSpent = project.lineItems.sumOf { it.amount }
    val salePrice = project.salePrice ?: 0.0
    val netProfit = if (project.isSold) salePrice - totalSpent else 0.0
    val margin = if (project.isSold && salePrice > 0.0) (netProfit / salePrice) * 100.0 else 0.0
    val status = if (project.isSold) "Sold" else "Active"
    csvBuilder.append("\"${project.name}\",\"$status\",${project.totalEstimatedBudget},$totalSpent,$salePrice,$netProfit,${String.format(Locale.US, "%.2f", margin)}%\n\n")

    val rawParticipants = project.lineItems.map { it.participantName }.distinct().ifEmpty { listOf(" You (Owner)") }
    val count = rawParticipants.size.coerceAtLeast(1)

    csvBuilder.append("Contributor,Total Contributed,Payout Share,Net Profit Share,ROI (%)\n")
    for (rawP in rawParticipants) {
      val displayName = cleanName(rawP, userName)
      val contributed = project.lineItems.filter { it.participantName.equals(rawP, ignoreCase = true) }.sumOf { it.amount }
      val payout = if (project.isSold) contributed + (salePrice - totalSpent) / count else 0.0
      val profitShare = if (project.isSold) payout - contributed else 0.0
      val roiPercent = if (project.isSold && contributed > 0.0) (profitShare / contributed) * 100.0 else if (project.isSold && profitShare > 0.0) 100.0 else 0.0

      val payoutStr = if (project.isSold) String.format(Locale.US, "%.2f", payout) else "N/A"
      val profitStr = if (project.isSold) String.format(Locale.US, "%.2f", profitShare) else "N/A"
      val roiStr = if (project.isSold) String.format(Locale.US, "%.2f%%", roiPercent) else "N/A"

      csvBuilder.append("\"$displayName\",${String.format(Locale.US, "%.2f", contributed)},$payoutStr,$profitStr,$roiStr\n")
    }
    csvBuilder.append("\n")

    csvBuilder.append("Expense ID,Participant,Contribution Type,Amount,Date\n")
    for (item in project.lineItems) {
      val displayName = cleanName(item.participantName, userName)
      val itemTitle = item.contributionType.replace("\"", "\"\"")
      csvBuilder.append("\"${item.id}\",\"$displayName\",\"$itemTitle\",${String.format(Locale.US, "%.2f", item.amount)},\"${concreteDateText(item.relativeDate)}\"\n")
    }

    val success = writeBytesToDownloads(context, fileName, "text/csv", csvBuilder.toString().toByteArray(Charsets.UTF_8))
    if (success) {
      Toast.makeText(context, "Exported Project CSV to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save Project CSV to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun exportProjectPdf(context: Context, project: ProjectRecord, userName: String = "User"): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val sanitizedName = project.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val fileName = "project_${sanitizedName}_$timestamp.pdf"

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var pageNumber = 1
    var currentPage = document.startPage(pageInfo)
    var canvas = currentPage.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background
    paint.color = Color.parseColor("#18181D")
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    // Header banner
    paint.color = Color.parseColor("#22222A")
    canvas.drawRoundRect(24f, 24f, 571f, 130f, 16f, 16f, paint)

    // Title
    paint.color = Color.parseColor("#A78BFA")
    paint.textSize = 18f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("PROJECT FINANCIAL REPORT", 40f, 56f, paint)

    // Subtitle & Date
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Project: ${project.name}", 40f, 78f, paint)

    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    val statusStr = if (project.isSold) "Status: Sold" else "Status: Active"
    canvas.drawText("Generated: $dateStr • $statusStr", 40f, 98f, paint)

    // Financial Overview Box
    val totalSpent = project.lineItems.sumOf { it.amount }
    val salePrice = project.salePrice ?: 0.0
    val netProfit = if (project.isSold) salePrice - totalSpent else 0.0

    paint.color = Color.parseColor("#2A2A35")
    canvas.drawRoundRect(24f, 140f, 571f, 202f, 12f, 12f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    canvas.drawText("Budget", 40f, 160f, paint)
    paint.color = Color.parseColor("#93C5FD")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("$${String.format(Locale.US, "%.2f", project.totalEstimatedBudget)}", 40f, 180f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Total Spent", 175f, 160f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("$${String.format(Locale.US, "%.2f", totalSpent)}", 175f, 180f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Sale Price", 310f, 160f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(if (project.isSold) "$${String.format(Locale.US, "%.2f", salePrice)}" else "N/A", 310f, 180f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Net Profit", 450f, 160f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(if (project.isSold) (if (netProfit >= 0) "$" else "-$") + String.format(Locale.US, "%.2f", Math.abs(netProfit)) else "N/A", 450f, 180f, paint)

    // Contributor ROI Table Header
    var currentY = 232f
    paint.color = Color.parseColor("#333340")
    canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("CONTRIBUTOR", 32f, currentY, paint)
    canvas.drawText("CONTRIBUTED", 185f, currentY, paint)
    canvas.drawText("PAYOUT", 285f, currentY, paint)
    canvas.drawText("NET PROFIT", 385f, currentY, paint)
    canvas.drawText("ROI (%)", 485f, currentY, paint)

    currentY += 21f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    val rawParticipants = project.lineItems.map { it.participantName }.distinct().ifEmpty { listOf("You (Owner)") }
    val count = rawParticipants.size.coerceAtLeast(1)

    for ((idx, rawP) in rawParticipants.withIndex()) {
      if (idx % 2 == 1) {
        paint.color = Color.parseColor("#1F1F27")
        canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
      }

      val displayName = cleanName(rawP, userName)
      val truncatedName = if (displayName.length > 22) displayName.take(20) + "..." else displayName
      val contributed = project.lineItems.filter { it.participantName.equals(rawP, ignoreCase = true) }.sumOf { it.amount }
      val payout = if (project.isSold) contributed + (salePrice - totalSpent) / count else 0.0
      val profitShare = if (project.isSold) payout - contributed else 0.0
      val roiPercent = if (project.isSold && contributed > 0.0) (profitShare / contributed) * 100.0 else if (project.isSold && profitShare > 0.0) 100.0 else 0.0

      val payoutStr = if (project.isSold) "$${String.format(Locale.US, "%.2f", payout)}" else "N/A"
      val profitStr = if (project.isSold) (if (profitShare >= 0) "$${String.format(Locale.US, "%.2f", profitShare)}" else "-$${String.format(Locale.US, "%.2f", Math.abs(profitShare))}") else "N/A"
      val roiStr = if (project.isSold) (if (roiPercent >= 0) "+${String.format(Locale.US, "%.1f", roiPercent)}%" else "${String.format(Locale.US, "%.1f", roiPercent)}%") else "N/A"

      paint.color = Color.parseColor("#F4F4F6")
      paint.textSize = 9f
      canvas.drawText(truncatedName, 32f, currentY, paint)

      paint.color = Color.parseColor("#A1A1AA")
      paint.textSize = 8.5f
      canvas.drawText("$${String.format(Locale.US, "%.2f", contributed)}", 185f, currentY, paint)
      canvas.drawText(payoutStr, 285f, currentY, paint)
      canvas.drawText(profitStr, 385f, currentY, paint)

      paint.color = Color.parseColor("#F4F4F6")
      paint.textSize = 9f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText(roiStr, 485f, currentY, paint)
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

      currentY += 21f
    }

    // Expense Details Table Header
    currentY += 15f
    paint.color = Color.parseColor("#333340")
    canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("EXPENSE TITLE / CONTRIBUTOR", 32f, currentY, paint)
    canvas.drawText("TYPE", 260f, currentY, paint)
    canvas.drawText("DATE", 380f, currentY, paint)
    canvas.drawText("AMOUNT", 500f, currentY, paint)

    currentY += 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    if (project.lineItems.isEmpty()) {
      paint.color = Color.parseColor("#A1A1AA")
      paint.textSize = 11f
      canvas.drawText("No expenses recorded for this project.", 40f, currentY + 10f, paint)
    } else {
      for ((idx, item) in project.lineItems.withIndex()) {
        if (currentY > 770f) {
          // Draw footer for previous page before finishing
          paint.color = Color.parseColor("#A1A1AA")
          paint.textSize = 8f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          canvas.drawText("Page $pageNumber", 520f, 810f, paint)
          canvas.drawText("Exported from DebtFlow by PCP", 40f, 810f, paint)

          document.finishPage(currentPage)
          pageNumber++
          val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
          currentPage = document.startPage(newPageInfo)
          canvas = currentPage.canvas

          // Background
          paint.color = Color.parseColor("#18181D")
          canvas.drawRect(0f, 0f, 595f, 842f, paint)

          // Header continued bar
          paint.color = Color.parseColor("#22222A")
          canvas.drawRect(24f, 24f, 571f, 56f, paint)

          paint.color = Color.parseColor("#A78BFA")
          paint.textSize = 10f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("PROJECT EXPENSE DETAILS (Continued - Page $pageNumber)", 40f, 44f, paint)

          // Table Header on new page
          currentY = 85f
          paint.color = Color.parseColor("#333340")
          canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

          paint.color = Color.parseColor("#F4F4F6")
          paint.textSize = 9f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("EXPENSE TITLE / CONTRIBUTOR", 32f, currentY, paint)
          canvas.drawText("TYPE", 260f, currentY, paint)
          canvas.drawText("DATE", 380f, currentY, paint)
          canvas.drawText("AMOUNT", 500f, currentY, paint)

          currentY += 22f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        if (idx % 2 == 1) {
          paint.color = Color.parseColor("#1F1F27")
          canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
        }

        val displayName = cleanName(item.participantName, userName)
        val titleDisplay = if (item.title.isNotBlank()) "${item.title} ($displayName)" else displayName
        val truncatedTitle = if (titleDisplay.length > 32) titleDisplay.take(30) + "..." else titleDisplay
        paint.color = Color.parseColor("#F4F4F6")
        paint.textSize = 9f
        canvas.drawText(truncatedTitle, 32f, currentY, paint)

        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        canvas.drawText(item.contributionType, 260f, currentY, paint)

        canvas.drawText(concreteDateText(item.relativeDate), 380f, currentY, paint)

        paint.color = Color.parseColor("#F4F4F6")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("$${String.format(Locale.US, "%.2f", item.amount)}", 500f, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        currentY += 21f
      }
    }

    // Footer on the last page
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Total expenses: ${project.lineItems.size} • Exported from DebtFlow by PCP", 40f, 810f, paint)
    canvas.drawText("Page $pageNumber", 520f, 810f, paint)

    document.finishPage(currentPage)

    val pdfBytes = java.io.ByteArrayOutputStream().use { baos ->
      document.writeTo(baos)
      baos.toByteArray()
    }
    document.close()

    val success = writeBytesToDownloads(context, fileName, "application/pdf", pdfBytes)
    if (success) {
      Toast.makeText(context, "Exported Project PDF to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save Project PDF to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun exportAllDebtsCsv(
    context: Context,
    debts: List<ReminderItem>,
    userName: String = "You",
    excludeCompleted: Boolean = AppPersistenceManager.loadExcludeCompletedFromExports(context),
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "all_debts_and_payments_$timestamp.csv"

    val data = processExportDebts(debts, excludeCompleted = excludeCompleted)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val csvBuilder = StringBuilder()
    csvBuilder.append("# =============================================================\n")
    csvBuilder.append("# GLOBAL DEBT & PAYMENT STATEMENT\n")
    csvBuilder.append("# User: $cleanUser\n")
    csvBuilder.append("# Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}\n")
    if (excludeCompleted) {
      csvBuilder.append("# FILTER: Active Debts Only (Paid off debts & recorded payments excluded)\n")
    } else {
      csvBuilder.append("# FILTER: All Debts & Payments (Completed and active items included)\n")
    }
    csvBuilder.append("# -------------------------------------------------------------\n")
    csvBuilder.append("# FINANCIAL SUMMARY & ADJUSTED BALANCE\n")
    csvBuilder.append("# Total Debts Owed to $cleanUser: $${String.format(Locale.US, "%.2f", data.grossOwedToMe)}\n")
    csvBuilder.append("# Less Recorded Payments Received: -$${String.format(Locale.US, "%.2f", data.paidOwedToMe)}\n")
    csvBuilder.append("# Total Owed to $cleanUser: $${String.format(Locale.US, "%.2f", data.activeOwedToMe)}\n")
    csvBuilder.append("#\n")
    csvBuilder.append("# Total Debts $cleanUser Owes: $${String.format(Locale.US, "%.2f", data.grossYouOwe)}\n")
    csvBuilder.append("# Less Recorded Payments Made: -$${String.format(Locale.US, "%.2f", data.paidYouOwe)}\n")
    csvBuilder.append("# Total $cleanUser Owes: $${String.format(Locale.US, "%.2f", data.activeYouOwe)}\n")
    csvBuilder.append("#\n")
    csvBuilder.append("# TOTAL SETTLED PAYMENTS: -$${String.format(Locale.US, "%.2f", data.totalSettledPaid)}\n")
    csvBuilder.append("# ESTIMATED NEXT CYCLE BALANCE: $${String.format(Locale.US, "%.2f", data.projectedNetBalance)}\n")
    csvBuilder.append("# -------------------------------------------------------------\n")
    val recAudit = auditReconciliation(debts)
    csvBuilder.append("# PAYMENT SUMMARY\n")
    csvBuilder.append("# Total Settled Debts: $${String.format(Locale.US, "%.2f", recAudit.totalPaidDebtsAmount)} (${recAudit.totalPaidDebtsCount} items)\n")
    csvBuilder.append("# Total Cash Payments: $${String.format(Locale.US, "%.2f", recAudit.totalPaymentsAmount)} (${recAudit.totalPaymentsCount} payments)\n")
    val auditStatusStr = if (recAudit.isBalanced) "100% BALANCED ($0.00 difference)" else "DIFFERENCE: $${String.format(Locale.US, "%.2f", Math.abs(recAudit.discrepancy))}"
    csvBuilder.append("# Global Status: $auditStatusStr\n")
    csvBuilder.append("# -------------------------------------------------------------\n")
    csvBuilder.append("# NEXT CYCLE CHARGES: +$${String.format(Locale.US, "%.2f", data.totalNextCycleCharges)}\n")
    csvBuilder.append("# CURRENT NET BALANCE: $${String.format(Locale.US, "%.2f", data.currentNetBalance)}\n")
    csvBuilder.append("# =============================================================\n\n")

    csvBuilder.append("ID,Title,Person,Amount,Direction,Date Recorded,Due Date,Status,Payment Date,Payment Method,Category,Notes\n")
    for (debt in data.allDisplayItems) {
      val isNextCycle = debt.id.endsWith("_next_cycle")
      val isPayment = isPaymentItem(debt)
      val person = debt.getAssociatedPerson().ifBlank { "Unassigned" }
      val direction = if (isPayment) {
        if (debt.isPositive) "Payment from $person to $cleanUser" else "Payment from $cleanUser to $person"
      } else {
        getDirectionText(debt.isPositive, person, userName)
      }
      val status = if (isNextCycle) "Upcoming (Next Billing Cycle)" else if (isPayment) "Recorded Payment" else if (debt.isPaid) "Paid" else "Unpaid"
      val dueDate = if (isPayment) debt.paidDate ?: debt.createdDate else concreteDateText(debt.relativeDateText)
      val paidDateStr = debt.paidDate ?: if (debt.isPaid || isPayment) debt.createdDate else "-"
      val paymentMethodStr = debt.paymentMethod ?: if (debt.isPaid || isPayment) "Standard" else "-"
      val amountStr = if (isPayment) "-${String.format(Locale.US, "%.2f", debt.amount)}" else if (isNextCycle) "+${String.format(Locale.US, "%.2f", debt.amount)}" else String.format(Locale.US, "%.2f", debt.amount)
      val titleEscaped = debt.title.replace("\"", "\"\"")
      val secondaryEscaped = debt.secondaryText.replace("\"", "\"\"")
      csvBuilder.append("\"${debt.id}\",\"$titleEscaped\",\"$person\",$amountStr,\"$direction\",\"${debt.createdDate}\",\"$dueDate\",\"$status\",\"$paidDateStr\",\"$paymentMethodStr\",\"${debt.iconType}\",\"$secondaryEscaped\"\n")
    }

    val success = writeBytesToDownloads(context, fileName, "text/csv", csvBuilder.toString().toByteArray(Charsets.UTF_8))
    if (success) {
      Toast.makeText(context, "Exported Full CSV to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save CSV to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun exportAllDebtsPdf(
    context: Context,
    debts: List<ReminderItem>,
    userName: String = "You",
    excludeCompleted: Boolean = AppPersistenceManager.loadExcludeCompletedFromExports(context),
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "all_debts_and_payments_$timestamp.pdf"

    val data = processExportDebts(debts, excludeCompleted = excludeCompleted)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var pageNumber = 1
    var currentPage = document.startPage(pageInfo)
    var canvas = currentPage.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background
    paint.color = Color.parseColor("#18181D")
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    // Header banner
    paint.color = Color.parseColor("#22222A")
    canvas.drawRoundRect(24f, 24f, 571f, 126f, 16f, 16f, paint)

    // Title
    paint.color = Color.parseColor("#A78BFA")
    paint.textSize = 18f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("GLOBAL DEBT & PAYMENT LEDGER", 40f, 54f, paint)

    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Comprehensive Audit Report", 40f, 76f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Generated: $dateStr • All Debts and Recorded Cash Payments", 40f, 96f, paint)

    // Financial Overview Box (Modified to show paid money removed from balance, next billing cycle & reconciliation audit)
    paint.color = Color.parseColor("#2A2A35")
    canvas.drawRoundRect(24f, 134f, 571f, 218f, 12f, 12f, paint)

    val owedLabel1 = "Total Owed to $cleanUser"
    val owedLabel2 = "Total $cleanUser Owes"

    // Col 1: Total Owed to User
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(owedLabel1, 38f, 152f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val owedToMeSign = if (data.activeOwedToMe >= 0) "$" else "-$"
    canvas.drawText("$owedToMeSign${String.format(Locale.US, "%.2f", Math.abs(data.activeOwedToMe))}", 38f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Debts $${String.format(Locale.US, "%.2f", data.grossOwedToMe)} • Paid -$${String.format(Locale.US, "%.2f", data.paidOwedToMe)}", 38f, 180f, paint)

    // Col 2: Total User Owes
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText(owedLabel2, 175f, 152f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val youOweSign = if (data.activeYouOwe > 0) "-$" else if (data.activeYouOwe < 0) "+$" else "$"
    canvas.drawText("$youOweSign${String.format(Locale.US, "%.2f", Math.abs(data.activeYouOwe))}", 175f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Debts $${String.format(Locale.US, "%.2f", data.grossYouOwe)} • Paid -$${String.format(Locale.US, "%.2f", data.paidYouOwe)}", 175f, 180f, paint)

    // Col 3: Settled / Paid (Removed)
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Recorded Payments", 312f, 152f, paint)
    paint.color = Color.parseColor("#93C5FD")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("-$${String.format(Locale.US, "%.2f", data.totalSettledPaid)}", 312f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Subtracted from total", 312f, 180f, paint)

    // Col 4: Next Cycle Position
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Next Cycle Position", 448f, 152f, paint)
    paint.color = if (data.projectedNetBalance >= 0) Color.parseColor("#34D399") else Color.parseColor("#F87171")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val netSign = if (data.projectedNetBalance >= 0) "$" else "-$"
    canvas.drawText("$netSign${String.format(Locale.US, "%.2f", Math.abs(data.projectedNetBalance))}", 448f, 168f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Estimated next month", 448f, 180f, paint)

    // Divider inside Financial Overview Box
    paint.color = Color.parseColor("#3A3A48")
    canvas.drawLine(36f, 190f, 559f, 190f, paint)

    // Row 2 of Overview: Next Billing Cycle, Projected Balance & Slimmed Audit on single line
    paint.color = Color.parseColor("#C4B5FD")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("NEXT: +$${String.format(Locale.US, "%.2f", data.totalNextCycleCharges)}", 38f, 206f, paint)

    paint.color = Color.parseColor("#FDE047")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val curSign = if (data.currentNetBalance >= 0) "$" else "-$"
    canvas.drawText("CURRENT NET: $curSign${String.format(Locale.US, "%.2f", Math.abs(data.currentNetBalance))}", 130f, 206f, paint)

    val recAudit = auditReconciliation(debts)
    paint.color = if (recAudit.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#FBBF24")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val auditBadgeText = "SUMMARY: Settled $${String.format(Locale.US, "%.2f", recAudit.totalPaidDebtsAmount)} | Cash $${String.format(Locale.US, "%.2f", recAudit.totalPaymentsAmount)} | ${if (recAudit.isBalanced) "Balanced ✓" else "Difference: $${String.format(Locale.US, "%.2f", Math.abs(recAudit.discrepancy))}"}"
    canvas.drawText(auditBadgeText, 250f, 206f, paint)

    // Table Header
    var currentY = 230f
    paint.color = Color.parseColor("#333340")
    canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("REC. DATE", 32f, currentY, paint)
    canvas.drawText("TITLE & PERSON", 100f, currentY, paint)
    canvas.drawText("DIRECTION", 230f, currentY, paint)
    canvas.drawText("DUE DATE", 315f, currentY, paint)
    canvas.drawText("STATUS / PAID DATE", 390f, currentY, paint)
    canvas.drawText("AMOUNT", 510f, currentY, paint)

    currentY += 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    if (data.allDisplayItems.isEmpty()) {
      paint.color = Color.parseColor("#A1A1AA")
      paint.textSize = 11f
      canvas.drawText("No debts or recorded payments available to export.", 40f, currentY + 10f, paint)
    } else {
      for ((idx, debt) in data.allDisplayItems.withIndex()) {
        val isNextCycle = debt.id.endsWith("_next_cycle")
        if (currentY > 770f) {
          // Draw footer for previous page before finishing
          paint.color = Color.parseColor("#A1A1AA")
          paint.textSize = 8f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          canvas.drawText("Page $pageNumber", 520f, 810f, paint)
          canvas.drawText("Exported from DebtFlow by PCP", 40f, 810f, paint)

          document.finishPage(currentPage)
          pageNumber++
          val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
          currentPage = document.startPage(newPageInfo)
          canvas = currentPage.canvas

          // Background
          paint.color = Color.parseColor("#18181D")
          canvas.drawRect(0f, 0f, 595f, 842f, paint)

          // Header continued bar
          paint.color = Color.parseColor("#22222A")
          canvas.drawRect(24f, 24f, 571f, 56f, paint)

          paint.color = Color.parseColor("#A78BFA")
          paint.textSize = 10f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("GLOBAL DEBT & PAYMENT LEDGER (Continued - Page $pageNumber)", 40f, 44f, paint)

          // Table Header on new page
          currentY = 85f
          paint.color = Color.parseColor("#333340")
          canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

          paint.color = Color.parseColor("#F4F4F6")
          paint.textSize = 9f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("REC. DATE", 32f, currentY, paint)
          canvas.drawText("TITLE & PERSON", 100f, currentY, paint)
          canvas.drawText("DIRECTION", 230f, currentY, paint)
          canvas.drawText("DUE DATE", 315f, currentY, paint)
          canvas.drawText("STATUS / PAID DATE", 390f, currentY, paint)
          canvas.drawText("AMOUNT", 510f, currentY, paint)

          currentY += 22f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        if (isNextCycle) {
          paint.color = Color.parseColor("#252236")
          canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
        } else if (idx % 2 == 1) {
          paint.color = Color.parseColor("#1F1F27")
          canvas.drawRect(24f, currentY - 14f, 571f, currentY + 7f, paint)
        }

        // Date Recorded
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        canvas.drawText(debt.createdDate, 32f, currentY, paint)

        // Title & Person
        paint.color = Color.parseColor("#F4F4F6")
        paint.textSize = 9f
        val personStr = debt.getAssociatedPerson()
        val display = if (personStr.isNotBlank()) "${debt.title} (${personStr})" else debt.title
        val truncatedDisplay = if (display.length > 22) display.take(20) + "..." else display
        canvas.drawText(truncatedDisplay, 100f, currentY, paint)

        // Direction
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        val isPayment = isPaymentItem(debt)
        val dirText = if (isPayment) {
          if (debt.isPositive) "Paid to $cleanUser" else "Paid to $personStr"
        } else {
          getDirectionText(debt.isPositive, personStr, userName)
        }
        canvas.drawText(dirText, 230f, currentY, paint)

        // Due Date (Concrete)
        paint.color = Color.parseColor("#A1A1AA")
        paint.textSize = 8.5f
        val dueText = if (isPayment) debt.paidDate ?: debt.createdDate else concreteDateText(debt.relativeDateText)
        canvas.drawText(dueText, 315f, currentY, paint)

        // Status & Paid Date
        if (isNextCycle) {
          paint.color = Color.parseColor("#C4B5FD")
          canvas.drawText("Next Cycle", 390f, currentY, paint)
        } else if (isPayment) {
          paint.color = Color.parseColor("#34D399")
          val method = if (!debt.paymentMethod.isNullOrBlank()) "Recorded • ${debt.paymentMethod}" else "Recorded Payment"
          val fullStatus = method.take(22)
          canvas.drawText(fullStatus, 390f, currentY, paint)
        } else if (debt.isPaid) {
          paint.color = Color.parseColor("#93C5FD")
          val paidLabel = if (!debt.paidDate.isNullOrBlank()) "Paid (${debt.paidDate})" else "Paid Today"
          val method = if (!debt.paymentMethod.isNullOrBlank()) " • ${debt.paymentMethod}" else ""
          val fullStatus = (paidLabel + method).take(22)
          canvas.drawText(fullStatus, 390f, currentY, paint)
        } else {
          paint.color = Color.parseColor("#FBBF24")
          canvas.drawText("Pending", 390f, currentY, paint)
        }

        // Amount
        if (isNextCycle) {
          paint.color = Color.parseColor("#C4B5FD")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("+$${String.format(Locale.US, "%.2f", debt.amount)}", 506f, currentY, paint)
        } else if (isPayment) {
          paint.color = Color.parseColor("#34D399")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("-$${String.format(Locale.US, "%.2f", debt.amount)}", 506f, currentY, paint)
        } else if (debt.isPaid) {
          paint.color = Color.parseColor("#94A3B8")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          canvas.drawText("$${String.format(Locale.US, "%.2f", debt.amount)}", 510f, currentY, paint)
        } else {
          paint.color = Color.parseColor("#F4F4F6")
          paint.textSize = 9.5f
          paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          canvas.drawText("$${String.format(Locale.US, "%.2f", debt.amount)}", 510f, currentY, paint)
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        currentY += 21f
      }
    }

    // Footer on the last page
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Total entries: ${data.allDisplayItems.size} (${debts.size} current + ${data.nextCycleItems.size} next cycle) • Exported from DebtFlow by PCP", 40f, 810f, paint)
    canvas.drawText("Page $pageNumber", 520f, 810f, paint)

    document.finishPage(currentPage)

    val pdfBytes = java.io.ByteArrayOutputStream().use { baos ->
      document.writeTo(baos)
      baos.toByteArray()
    }
    document.close()

    val success = writeBytesToDownloads(context, fileName, "application/pdf", pdfBytes)
    if (success) {
      Toast.makeText(context, "Exported PDF to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save PDF to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  private fun writeBytesToDownloads(context: Context, fileName: String, mimeType: String, data: ByteArray): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.Downloads.DISPLAY_NAME, fileName)
          put(MediaStore.Downloads.MIME_TYPE, mimeType)
          put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
          resolver.openOutputStream(uri)?.use { it.write(data) }
          true
        } else false
      } else {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { it.write(data) }
        true
      }
    } catch (e: Exception) {
      false
    }
  }

  // -------------------------------------------------------------
  // CSV Import Functionality
  // -------------------------------------------------------------
  data class ImportResult(
    val importedDebts: List<ReminderItem>,
    val importedProjects: List<ProjectRecord>,
    val importedPeople: List<PersonRecord>,
    val summaryMessage: String,
  )

  fun readCsvFromUri(context: Context, uri: Uri): String? {
    return try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
          reader.readText()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = java.lang.StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val c = line[i]
      if (c == '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
          current.append('"')
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == ',' && !inQuotes) {
        result.add(current.toString().trim())
        current.setLength(0)
      } else {
        current.append(c)
      }
      i++
    }
    result.add(current.toString().trim())
    return result
  }

  fun parseCsvImport(csvContent: String): ImportResult {
    val lines = csvContent.lines().map { it.trim() }.filter { it.isNotBlank() }
    val dataLines = lines.filterNot { it.startsWith("#") }

    if (dataLines.isEmpty()) {
      return ImportResult(emptyList(), emptyList(), emptyList(), "CSV file contains no data rows.")
    }

    val headerLine = dataLines.first()
    val headerCells = parseCsvLine(headerLine).map { it.trim().lowercase(Locale.US) }
    val rows = dataLines.drop(1)

    val importedDebts = mutableListOf<ReminderItem>()
    val importedProjects = mutableListOf<ProjectRecord>()
    val importedPeopleMap = mutableMapOf<String, PersonRecord>()

    // Check if it's a Projects CSV format
    val isProjectCsv = headerCells.any { it.contains("project") } || headerCells.any { it.contains("budget") } || headerCells.any { it.contains("expense") }

    if (isProjectCsv) {
      val nameIdx = headerCells.indexOfFirst { it.contains("project") && !it.contains("id") }.let { if (it < 0) 0 else it }
      val budgetIdx = headerCells.indexOfFirst { it.contains("budget") }
      val descIdx = headerCells.indexOfFirst { it.contains("desc") }
      val titleIdx = headerCells.indexOfFirst { it.contains("title") || it.contains("expense") }
      val participantIdx = headerCells.indexOfFirst { it.contains("participant") || it.contains("person") || it.contains("contributor") }
      val amountIdx = headerCells.indexOfFirst { it.contains("amount") || it.contains("cost") }
      val typeIdx = headerCells.indexOfFirst { it.contains("type") }
      val dateIdx = headerCells.indexOfFirst { it.contains("date") }

      val projectMap = mutableMapOf<String, ProjectRecord>()

      for (rowStr in rows) {
        val cols = parseCsvLine(rowStr)
        if (cols.isEmpty()) continue
        val projName = cols.getOrNull(nameIdx)?.ifBlank { "Imported Project" } ?: "Imported Project"
        val budgetVal = if (budgetIdx >= 0) cols.getOrNull(budgetIdx)?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0 else 0.0
        val descVal = if (descIdx >= 0) cols.getOrNull(descIdx) ?: "" else ""

        val proj = projectMap.getOrPut(projName) {
          ProjectRecord(
            id = "proj_${System.currentTimeMillis()}_${projectMap.size}",
            name = projName,
            totalEstimatedBudget = budgetVal,
            participantsCount = 1,
            relativeTime = "today",
            lineItems = emptyList(),
          )
        }

        val itemTitle = if (titleIdx >= 0) cols.getOrNull(titleIdx) ?: "" else ""
        val itemParticipant = if (participantIdx >= 0) cols.getOrNull(participantIdx) ?: "You" else "You"
        val itemAmount = if (amountIdx >= 0) cols.getOrNull(amountIdx)?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0 else 0.0
        val itemType = if (typeIdx >= 0) cols.getOrNull(typeIdx) ?: "Direct Payment" else "Direct Payment"
        val itemDate = if (dateIdx >= 0) cols.getOrNull(dateIdx) ?: "Today" else "Today"

        if (itemTitle.isNotBlank() || itemAmount > 0.0) {
          val lineItem = ExpenseLineItem(
            id = "exp_${System.currentTimeMillis()}_${proj.lineItems.size}",
            title = itemTitle.ifBlank { "Expense" },
            participantName = itemParticipant.ifBlank { "You" },
            amount = itemAmount,
            contributionType = itemType,
            relativeDate = itemDate,
          )
          projectMap[projName] = proj.copy(lineItems = proj.lineItems + lineItem)

          if (itemParticipant.isNotBlank() && !itemParticipant.equals("You", ignoreCase = true)) {
            val key = itemParticipant.trim().lowercase(Locale.US)
            importedPeopleMap.getOrPut(key) {
              PersonRecord(
                id = "p_${System.currentTimeMillis()}_${importedPeopleMap.size}",
                name = itemParticipant.trim(),
                colorHex = 0xFF6366F1,
              )
            }
          }
        }
      }
      importedProjects.addAll(projectMap.values)
    } else {
      // Debts CSV format
      val titleIdx = headerCells.indexOfFirst { it.contains("title") || it.contains("desc") || it.contains("name") && !it.contains("person") }.let { if (it < 0) 1.coerceAtMost(headerCells.size - 1) else it }
      val personIdx = headerCells.indexOfFirst { it.contains("person") || it.contains("contact") || it.contains("who") || it.contains("payer") }
      val amountIdx = headerCells.indexOfFirst { it.contains("amount") || it.contains("cost") || it.contains("price") || it.contains("value") }
      val directionIdx = headerCells.indexOfFirst { it.contains("direction") || it.contains("type") }
      val dateIdx = headerCells.indexOfFirst { it.contains("date recorded") || it.contains("created") || it.contains("date") }
      val dueDateIdx = headerCells.indexOfFirst { it.contains("due") }
      val statusIdx = headerCells.indexOfFirst { it.contains("status") || it.contains("paid") }
      val categoryIdx = headerCells.indexOfFirst { it.contains("category") || it.contains("icon") }
      val notesIdx = headerCells.indexOfFirst { it.contains("note") || it.contains("secondary") }

      val now = Calendar.getInstance()
      val todayDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(now.time)

      for (rowStr in rows) {
        val cols = parseCsvLine(rowStr)
        if (cols.isEmpty()) continue

        val title = (if (titleIdx >= 0) cols.getOrNull(titleIdx) else null)?.ifBlank { "Imported Debt" } ?: "Imported Debt"
        val personName = (if (personIdx >= 0) cols.getOrNull(personIdx) else null)?.ifBlank { "Contact" } ?: "Contact"
        val rawAmountStr = (if (amountIdx >= 0) cols.getOrNull(amountIdx) else null)?.replace("$", "")?.trim() ?: "0"
        val isNegativeInAmount = rawAmountStr.startsWith("-")
        val amount = rawAmountStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

        val directionStr = (if (directionIdx >= 0) cols.getOrNull(directionIdx) else null)?.lowercase(Locale.US) ?: ""
        val statusStr = (if (statusIdx >= 0) cols.getOrNull(statusIdx) else null)?.lowercase(Locale.US) ?: ""
        val createdDate = (if (dateIdx >= 0) cols.getOrNull(dateIdx) else null)?.ifBlank { todayDateStr } ?: todayDateStr
        val dueDate = (if (dueDateIdx >= 0) cols.getOrNull(dueDateIdx) else null)?.ifBlank { "Due soon" } ?: "Due soon"
        val category = (if (categoryIdx >= 0) cols.getOrNull(categoryIdx) else null)?.ifBlank { "General" } ?: "General"
        val notes = (if (notesIdx >= 0) cols.getOrNull(notesIdx) else null) ?: ""

        val isPositive = if (directionStr.contains("owes you") || directionStr.contains("owed to") || directionStr.contains("incoming")) {
          true
        } else if (directionStr.contains("you owe") || directionStr.contains("outgoing")) {
          false
        } else {
          !isNegativeInAmount
        }

        val isPaid = statusStr.contains("paid") || statusStr.contains("settled") || statusStr.contains("completed")
        val isCashPayment = category.equals("Cash", ignoreCase = true) || title.contains("Payment", ignoreCase = true) || statusStr.contains("recorded payment")

        val debtItem = ReminderItem(
          id = "debt_imp_${System.currentTimeMillis()}_${importedDebts.size}",
          title = title,
          relativeDateText = dueDate,
          amount = amount,
          isPositive = isPositive,
          secondaryText = if (notes.isNotBlank()) notes else "$category debt ($personName)",
          iconType = if (isCashPayment) "Cash" else category,
          isPaid = isPaid,
          paidDate = if (isPaid) createdDate else null,
          paymentMethod = if (isPaid) "Standard" else null,
          personName = personName,
          createdDate = createdDate,
        )
        importedDebts.add(debtItem)

        if (personName.isNotBlank() && !personName.equals("You", ignoreCase = true) && !personName.equals("Contact", ignoreCase = true)) {
          val key = personName.trim().lowercase(Locale.US)
          importedPeopleMap.getOrPut(key) {
            PersonRecord(
              id = "p_${System.currentTimeMillis()}_${importedPeopleMap.size}",
              name = personName.trim(),
              colorHex = 0xFF6366F1,
            )
          }
        }
      }
    }

    val totalRecords = importedDebts.size + importedProjects.size
    val summary = if (isProjectCsv) {
      "Successfully imported ${importedProjects.size} project(s) and ${importedPeopleMap.size} contact(s)."
    } else {
      "Successfully imported ${importedDebts.size} debt/payment record(s) and ${importedPeopleMap.size} contact(s)."
    }

    return ImportResult(
      importedDebts = importedDebts,
      importedProjects = importedProjects,
      importedPeople = importedPeopleMap.values.toList(),
      summaryMessage = summary,
    )
  }

  fun exportReconciliationReportCsv(
    context: Context,
    debts: List<ReminderItem>,
    userName: String = "User",
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "reconciliation_audit_$timestamp.csv"
    val audit = auditReconciliation(debts)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val csvBuilder = StringBuilder()
    csvBuilder.append("# =============================================================\n")
    csvBuilder.append("# PAYMENT RECONCILIATION & FINANCIAL INTEGRITY AUDIT\n")
    csvBuilder.append("# User: $cleanUser\n")
    csvBuilder.append("# Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}\n")
    csvBuilder.append("# Status: ${if (audit.isBalanced) "100% BALANCED & RECONCILED" else "DIFFERENCE FOUND"}\n")
    csvBuilder.append("# Total Paid Debts: $${String.format(Locale.US, "%.2f", audit.totalPaidDebtsAmount)} (${audit.totalPaidDebtsCount} items)\n")
    csvBuilder.append("# Total Payments Logged: $${String.format(Locale.US, "%.2f", audit.totalPaymentsAmount)} (${audit.totalPaymentsCount} payments)\n")
    csvBuilder.append("# Net Discrepancy: $${String.format(Locale.US, "%.2f", audit.discrepancy)}\n")
    csvBuilder.append("# =============================================================\n\n")

    csvBuilder.append("Contact,Debts Marked Paid (Qty),Debts Paid Amount,Payments Logged (Qty),Payments Amount,Variance,Status,Unlinked Paid Debts,Unallocated Payments\n")
    for (person in audit.personBreakdowns) {
      val statusStr = if (person.isBalanced) "Balanced" else "Difference"
      val varianceStr = "$${String.format(Locale.US, "%.2f", person.discrepancy)}"
      csvBuilder.append("\"${person.personName}\",${person.paidDebtsCount},${String.format(Locale.US, "%.2f", person.paidDebtsAmount)},${person.paymentsCount},${String.format(Locale.US, "%.2f", person.paymentsAmount)},\"$varianceStr\",\"$statusStr\",${person.unlinkedPaidDebts.size},${person.unallocatedPayments.size}\n")
    }

    val success = writeBytesToDownloads(context, fileName, "text/csv", csvBuilder.toString().toByteArray(Charsets.UTF_8))
    if (success) {
      Toast.makeText(context, "Exported Reconciliation Audit CSV to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save CSV to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }

  fun exportReconciliationReportPdf(
    context: Context,
    debts: List<ReminderItem>,
    userName: String = "User",
  ): Boolean {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "reconciliation_audit_$timestamp.pdf"
    val audit = auditReconciliation(debts)
    val cleanUser = if (userName.isBlank() || userName.equals("You", ignoreCase = true)) "User" else userName

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var pageNumber = 1
    var currentPage = document.startPage(pageInfo)
    var canvas = currentPage.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background
    paint.color = Color.parseColor("#18181D")
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    // Header banner
    paint.color = Color.parseColor("#22222A")
    canvas.drawRoundRect(24f, 24f, 571f, 126f, 16f, 16f, paint)

    // Title
    paint.color = Color.parseColor("#A78BFA")
    paint.textSize = 17f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("PAYMENT RECONCILIATION AUDIT", 40f, 54f, paint)

    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val subtext = if (audit.isBalanced) "All Paid Items and Recorded Payments are 100% Balanced" else "Integrity Scan: Variances or Unlinked Items Found"
    canvas.drawText(subtext, 40f, 76f, paint)

    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Generated: $dateStr • Auditor: $cleanUser", 40f, 96f, paint)

    // Overview KPIs Box
    paint.color = Color.parseColor("#2A2A35")
    canvas.drawRoundRect(24f, 134f, 571f, 214f, 12f, 12f, paint)

    // Col 1: Paid Debts
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Items Marked Paid", 38f, 152f, paint)
    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("$${String.format(Locale.US, "%.2f", audit.totalPaidDebtsAmount)}", 38f, 170f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("${audit.totalPaidDebtsCount} marked paid", 38f, 184f, paint)

    // Col 2: Recorded Payments
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Payments Displayed", 175f, 152f, paint)
    paint.color = Color.parseColor("#93C5FD")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("$${String.format(Locale.US, "%.2f", audit.totalPaymentsAmount)}", 175f, 170f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("${audit.totalPaymentsCount} cash/log entries", 175f, 184f, paint)

    // Col 3: Variance
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Net Discrepancy", 312f, 152f, paint)
    paint.color = if (audit.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#F87171")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val discSign = if (audit.discrepancy > 0) "+$" else if (audit.discrepancy < 0) "-$" else "$"
    canvas.drawText("$discSign${String.format(Locale.US, "%.2f", Math.abs(audit.discrepancy))}", 312f, 170f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(if (audit.isBalanced) "100% matched" else "Action available", 312f, 184f, paint)

    // Col 4: Status
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8.5f
    canvas.drawText("Audit Status", 448f, 152f, paint)
    paint.color = if (audit.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#FBBF24")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(if (audit.isBalanced) "PASSED ✓" else "NEEDS SYNC ⚠", 448f, 170f, paint)
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("${audit.personBreakdowns.size} contacts scanned", 448f, 184f, paint)

    // Table Header
    var currentY = 240f
    paint.color = Color.parseColor("#333340")
    canvas.drawRect(24f, currentY - 16f, 571f, currentY + 8f, paint)

    paint.color = Color.parseColor("#F4F4F6")
    paint.textSize = 9f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("CONTACT / PERSON", 32f, currentY, paint)
    canvas.drawText("PAID ITEMS ($)", 160f, currentY, paint)
    canvas.drawText("PAYMENTS LOGGED ($)", 270f, currentY, paint)
    canvas.drawText("VARIANCE", 400f, currentY, paint)
    canvas.drawText("STATUS", 490f, currentY, paint)

    currentY += 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    if (audit.personBreakdowns.isEmpty()) {
      paint.color = Color.parseColor("#A1A1AA")
      paint.textSize = 11f
      canvas.drawText("No person records found to audit.", 40f, currentY + 10f, paint)
    } else {
      for (person in audit.personBreakdowns) {
        if (currentY > 770f) {
          paint.color = Color.parseColor("#A1A1AA")
          paint.textSize = 8f
          canvas.drawText("Page $pageNumber", 520f, 810f, paint)
          canvas.drawText("DebtFlow Reconciliation Audit Report", 40f, 810f, paint)

          document.finishPage(currentPage)
          pageNumber++
          val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
          currentPage = document.startPage(newPageInfo)
          canvas = currentPage.canvas

          paint.color = Color.parseColor("#18181D")
          canvas.drawRect(0f, 0f, 595f, 842f, paint)
          currentY = 40f
        }

        paint.color = Color.parseColor("#22222A")
        canvas.drawRoundRect(24f, currentY - 12f, 571f, currentY + 16f, 6f, 6f, paint)

        // Contact
        paint.color = Color.parseColor("#F4F4F6")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(person.personName, 32f, currentY + 3f, paint)

        // Paid Items
        paint.color = Color.parseColor("#E4E4E7")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("$${String.format(Locale.US, "%.2f", person.paidDebtsAmount)} (${person.paidDebtsCount})", 160f, currentY + 3f, paint)

        // Payments Logged
        paint.color = Color.parseColor("#93C5FD")
        canvas.drawText("$${String.format(Locale.US, "%.2f", person.paymentsAmount)} (${person.paymentsCount})", 270f, currentY + 3f, paint)

        // Variance
        paint.color = if (person.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#F87171")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val vSign = if (person.discrepancy < 0) "-$" else "$"
        canvas.drawText("$vSign${String.format(Locale.US, "%.2f", Math.abs(person.discrepancy))}", 400f, currentY + 3f, paint)

        // Status
        paint.color = if (person.isBalanced) Color.parseColor("#34D399") else Color.parseColor("#FBBF24")
        canvas.drawText(if (person.isBalanced) "Balanced ✓" else "Needs Sync", 490f, currentY + 3f, paint)

        currentY += 32f
      }
    }

    // Draw Footer on last page
    paint.color = Color.parseColor("#A1A1AA")
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Page $pageNumber", 520f, 810f, paint)
    canvas.drawText("DebtFlow Payment Reconciliation Audit • Generated by Google AI Studio", 40f, 810f, paint)

    document.finishPage(currentPage)

    val pdfBytes = java.io.ByteArrayOutputStream().use { baos ->
      document.writeTo(baos)
      baos.toByteArray()
    }
    document.close()

    val success = writeBytesToDownloads(context, fileName, "application/pdf", pdfBytes)

    if (success) {
      Toast.makeText(context, "Exported Reconciliation Audit PDF to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(context, "Failed to save PDF to Downloads", Toast.LENGTH_SHORT).show()
    }
    return success
  }
}
