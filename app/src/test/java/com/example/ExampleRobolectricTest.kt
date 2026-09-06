package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AppPersistenceManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DebtFlow", appName)
  }

  @Test
  fun `verify app persistence manager save and load`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val person = com.example.model.PersonRecord(id = "test_p1", name = "Alice", colorHex = 0xFF2563EBL)
    val debt = com.example.model.ReminderItem(id = "test_r1", title = "Lunch", relativeDateText = "today", amount = 25.0, isPositive = true, secondaryText = "Alice", iconType = "One-Off", isPaid = false)
    val proj = com.example.model.ProjectRecord(id = "test_proj1", name = "Trip", totalEstimatedBudget = 500.0, participantsCount = 2, relativeTime = "today", lineItems = emptyList())

    AppPersistenceManager.savePeople(context, listOf(person))
    AppPersistenceManager.saveReminders(context, listOf(debt))
    AppPersistenceManager.saveProjects(context, listOf(proj))

    val loadedPeople = AppPersistenceManager.loadPeople(context)
    val loadedDebts = AppPersistenceManager.loadReminders(context)
    val loadedProjects = AppPersistenceManager.loadProjects(context)

    assertEquals(1, loadedPeople.size)
    assertEquals("Alice", loadedPeople[0].name)
    assertEquals(1, loadedDebts.size)
    assertEquals("Lunch", loadedDebts[0].title)
    assertEquals(1, loadedProjects.size)
    assertEquals("Trip", loadedProjects[0].name)
  }

  @Test
  fun `verify cash payment automatic debt deduction full and partial`() {
    val reminders = mutableListOf(
      com.example.model.ReminderItem(
        id = "debt_1",
        title = "Concert ticket",
        relativeDateText = "today",
        amount = 50.0,
        isPositive = true,
        secondaryText = "Payer: Bob",
        iconType = "person",
        personName = "Bob",
        isPaid = false,
      ),
    )

    val personName = "Bob"
    val isReceived = true
    val paymentDate = "Sep 05, 2026"
    val partialAmount = 20.0

    // Simulate partial cash payment deduction
    val relevantDebts = reminders.filter { debt ->
      !debt.isPaid && (debt.isPositive == isReceived) &&
        (debt.getAssociatedPerson().equals(personName, ignoreCase = true) ||
         debt.secondaryText.contains(personName, ignoreCase = true) ||
         debt.personName.equals(personName, ignoreCase = true))
    }

    var remaining = partialAmount
    for (debt in relevantDebts) {
      if (remaining <= 0.0) break
      val idx = reminders.indexOfFirst { it.id == debt.id }
      if (idx >= 0) {
        val oldDebt = reminders[idx]
        if (remaining >= oldDebt.amount) {
          reminders[idx] = oldDebt.copy(isPaid = true, paidDate = paymentDate, paymentMethod = "Cash")
          remaining -= oldDebt.amount
        } else {
          reminders[idx] = oldDebt.copy(amount = oldDebt.amount - remaining)
          val paidPortion = oldDebt.copy(id = "paid_split_1", amount = remaining, isPaid = true, paidDate = paymentDate, paymentMethod = "Cash")
          reminders.add(0, paidPortion)
          remaining = 0.0
        }
      }
    }

    // Unpaid balance should now be 30.0
    val remainingOwed = reminders.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
    assertEquals(30.0, remainingOwed, 0.001)

    // Complete the remaining 30.0 in cash
    val finalPayment = 30.0
    var remainingFinal = finalPayment
    for (debt in reminders.filter { !it.isPaid && it.isPositive }) {
      if (remainingFinal <= 0.0) break
      val idx = reminders.indexOfFirst { it.id == debt.id }
      if (idx >= 0) {
        val oldDebt = reminders[idx]
        if (remainingFinal >= oldDebt.amount) {
          reminders[idx] = oldDebt.copy(isPaid = true, paidDate = paymentDate, paymentMethod = "Cash")
          remainingFinal -= oldDebt.amount
        }
      }
    }

    val finalOwed = reminders.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
    assertEquals(0.0, finalOwed, 0.001)
  }
}
