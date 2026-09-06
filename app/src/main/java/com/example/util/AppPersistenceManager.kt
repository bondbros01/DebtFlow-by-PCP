package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AuditLogEntry
import com.example.model.ExpenseLineItem
import com.example.model.PersonRecord
import com.example.model.ProjectRecord
import com.example.model.ReminderItem
import org.json.JSONArray
import org.json.JSONObject

object AppPersistenceManager {

  private const val PREFS_NAME = "debtflow_app_storage"
  private const val KEY_PEOPLE = "saved_people"
  private const val KEY_REMINDERS = "saved_reminders"
  private const val KEY_PROJECTS = "saved_projects"
  private const val KEY_AUDIT_LOGS = "saved_audit_logs"
  private const val KEY_DARK_MODE = "saved_dark_mode"
  private const val KEY_CALENDAR_VIEW = "saved_calendar_view"
  private const val KEY_SHOW_PROJECT_COSTS = "saved_show_project_costs"
  private const val KEY_USER_NAME = "saved_user_name"
  private const val KEY_CURRENCY_SYMBOL = "saved_currency_symbol"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  // -------------------------------------------------------------
  // People Persistence
  // -------------------------------------------------------------
  fun loadPeople(context: Context): List<PersonRecord> {
    val jsonString = getPrefs(context).getString(KEY_PEOPLE, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<PersonRecord>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          PersonRecord(
            id = obj.optString("id", "p_${System.currentTimeMillis()}_$i"),
            name = obj.optString("name", "Unknown"),
            colorHex = obj.optLong("colorHex", 0xFF7C3AEDL),
            notes = obj.optString("notes", ""),
          )
        )
      }
      list
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  fun savePeople(context: Context, people: List<PersonRecord>) {
    try {
      val array = JSONArray()
      for (p in people) {
        val obj = JSONObject().apply {
          put("id", p.id)
          put("name", p.name)
          put("colorHex", p.colorHex)
          put("notes", p.notes)
        }
        array.put(obj)
      }
      getPrefs(context).edit().putString(KEY_PEOPLE, array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // -------------------------------------------------------------
  // Reminders / Debts Persistence
  // -------------------------------------------------------------
  fun loadReminders(context: Context): List<ReminderItem> {
    val jsonString = getPrefs(context).getString(KEY_REMINDERS, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<ReminderItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          ReminderItem(
            id = obj.optString("id", "r_${System.currentTimeMillis()}_$i"),
            title = obj.optString("title", "Untitled Debt"),
            relativeDateText = obj.optString("relativeDateText", "today"),
            amount = obj.optDouble("amount", 0.0),
            isPositive = obj.optBoolean("isPositive", true),
            secondaryText = obj.optString("secondaryText", ""),
            iconType = obj.optString("iconType", "One-Off"),
            isPaid = obj.optBoolean("isPaid", false),
            personName = obj.optString("personName", ""),
            createdDate = obj.optString("createdDate", "Sep 04, 2026"),
            paidDate = if (obj.has("paidDate") && !obj.isNull("paidDate")) obj.getString("paidDate") else null,
            paymentMethod = if (obj.has("paymentMethod") && !obj.isNull("paymentMethod")) obj.getString("paymentMethod") else null,
            settledByPaymentId = if (obj.has("settledByPaymentId") && !obj.isNull("settledByPaymentId")) obj.getString("settledByPaymentId") else null,
            settledDebtIds = if (obj.has("settledDebtIds") && !obj.isNull("settledDebtIds")) obj.getString("settledDebtIds") else null,
            deductedAmount = if (obj.has("deductedAmount") && !obj.isNull("deductedAmount")) obj.getDouble("deductedAmount") else null,
          )
        )
      }
      list
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  fun saveReminders(context: Context, reminders: List<ReminderItem>) {
    try {
      val array = JSONArray()
      for (r in reminders) {
        val obj = JSONObject().apply {
          put("id", r.id)
          put("title", r.title)
          put("relativeDateText", r.relativeDateText)
          put("amount", r.amount)
          put("isPositive", r.isPositive)
          put("secondaryText", r.secondaryText)
          put("iconType", r.iconType)
          put("isPaid", r.isPaid)
          put("personName", r.personName)
          put("createdDate", r.createdDate)
          if (r.paidDate != null) put("paidDate", r.paidDate) else put("paidDate", JSONObject.NULL)
          if (r.paymentMethod != null) put("paymentMethod", r.paymentMethod) else put("paymentMethod", JSONObject.NULL)
          if (r.settledByPaymentId != null) put("settledByPaymentId", r.settledByPaymentId) else put("settledByPaymentId", JSONObject.NULL)
          if (r.settledDebtIds != null) put("settledDebtIds", r.settledDebtIds) else put("settledDebtIds", JSONObject.NULL)
          if (r.deductedAmount != null) put("deductedAmount", r.deductedAmount) else put("deductedAmount", JSONObject.NULL)
        }
        array.put(obj)
      }
      getPrefs(context).edit().putString(KEY_REMINDERS, array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // -------------------------------------------------------------
  // Projects Persistence
  // -------------------------------------------------------------
  fun loadProjects(context: Context): List<ProjectRecord> {
    val jsonString = getPrefs(context).getString(KEY_PROJECTS, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<ProjectRecord>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val lineItemsArray = obj.optJSONArray("lineItems") ?: JSONArray()
        val lineItemsList = mutableListOf<ExpenseLineItem>()
        for (j in 0 until lineItemsArray.length()) {
          val liObj = lineItemsArray.getJSONObject(j)
          lineItemsList.add(
            ExpenseLineItem(
              id = liObj.optString("id", "li_${System.currentTimeMillis()}_$j"),
              participantName = liObj.optString("participantName", ""),
              contributionType = liObj.optString("contributionType", "Cash"),
              amount = liObj.optDouble("amount", 0.0),
              relativeDate = liObj.optString("relativeDate", "today"),
            )
          )
        }

        list.add(
          ProjectRecord(
            id = obj.optString("id", "proj_${System.currentTimeMillis()}_$i"),
            name = obj.optString("name", "Untitled Project"),
            totalEstimatedBudget = obj.optDouble("totalEstimatedBudget", 0.0),
            participantsCount = obj.optInt("participantsCount", 1),
            relativeTime = obj.optString("relativeTime", "today"),
            lineItems = lineItemsList,
            isSold = obj.optBoolean("isSold", false),
            salePrice = if (obj.has("salePrice") && !obj.isNull("salePrice")) obj.getDouble("salePrice") else null,
          )
        )
      }
      list
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  fun saveProjects(context: Context, projects: List<ProjectRecord>) {
    try {
      val array = JSONArray()
      for (p in projects) {
        val projObj = JSONObject().apply {
          put("id", p.id)
          put("name", p.name)
          put("totalEstimatedBudget", p.totalEstimatedBudget)
          put("participantsCount", p.participantsCount)
          put("relativeTime", p.relativeTime)
          put("isSold", p.isSold)
          if (p.salePrice != null) put("salePrice", p.salePrice) else put("salePrice", JSONObject.NULL)

          val liArray = JSONArray()
          for (li in p.lineItems) {
            val liObj = JSONObject().apply {
              put("id", li.id)
              put("participantName", li.participantName)
              put("contributionType", li.contributionType)
              put("amount", li.amount)
              put("relativeDate", li.relativeDate)
            }
            liArray.put(liObj)
          }
          put("lineItems", liArray)
        }
        array.put(projObj)
      }
      getPrefs(context).edit().putString(KEY_PROJECTS, array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // -------------------------------------------------------------
  // Audit Logs Persistence
  // -------------------------------------------------------------
  fun loadAuditLogs(context: Context): List<AuditLogEntry> {
    val jsonString = getPrefs(context).getString(KEY_AUDIT_LOGS, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<AuditLogEntry>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          AuditLogEntry(
            id = obj.optString("id", "log_${System.currentTimeMillis()}_$i"),
            narrative = obj.optString("narrative", ""),
            relativeTime = obj.optString("relativeTime", "today"),
            amountText = obj.optString("amountText", ""),
            isUndone = obj.optBoolean("isUndone", false),
            undoAction = null,
          )
        )
      }
      list
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  fun saveAuditLogs(context: Context, logs: List<AuditLogEntry>) {
    try {
      val array = JSONArray()
      for (log in logs.take(100)) { // Preserve top 100 most recent audit logs
        val obj = JSONObject().apply {
          put("id", log.id)
          put("narrative", log.narrative)
          put("relativeTime", log.relativeTime)
          put("amountText", log.amountText)
          put("isUndone", log.isUndone)
        }
        array.put(obj)
      }
      getPrefs(context).edit().putString(KEY_AUDIT_LOGS, array.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // -------------------------------------------------------------
  // Settings Persistence
  // -------------------------------------------------------------
  fun loadDarkMode(context: Context, defaultVal: Boolean = true): Boolean {
    return getPrefs(context).getBoolean(KEY_DARK_MODE, defaultVal)
  }

  fun saveDarkMode(context: Context, isDark: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_DARK_MODE, isDark).apply()
  }

  fun loadCalendarView(context: Context, defaultVal: Boolean = false): Boolean {
    return getPrefs(context).getBoolean(KEY_CALENDAR_VIEW, defaultVal)
  }

  fun saveCalendarView(context: Context, isCalendar: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_CALENDAR_VIEW, isCalendar).apply()
  }

  fun loadShowProjectCosts(context: Context, defaultVal: Boolean = false): Boolean {
    return getPrefs(context).getBoolean(KEY_SHOW_PROJECT_COSTS, defaultVal)
  }

  fun saveShowProjectCosts(context: Context, show: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_SHOW_PROJECT_COSTS, show).apply()
  }

  fun loadUserName(context: Context, defaultVal: String = "User"): String {
    return getPrefs(context).getString(KEY_USER_NAME, defaultVal) ?: defaultVal
  }

  fun saveUserName(context: Context, userName: String) {
    getPrefs(context).edit().putString(KEY_USER_NAME, userName).apply()
  }

  fun loadEnableNotifications(context: Context, defaultVal: Boolean = false): Boolean {
    return getPrefs(context).getBoolean("saved_enable_notifications", defaultVal)
  }

  fun saveEnableNotifications(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean("saved_enable_notifications", enabled).apply()
  }

  fun loadExcludeCompletedFromExports(context: Context, defaultVal: Boolean = false): Boolean {
    return getPrefs(context).getBoolean("saved_exclude_completed_exports", defaultVal)
  }

  fun saveExcludeCompletedFromExports(context: Context, exclude: Boolean) {
    getPrefs(context).edit().putBoolean("saved_exclude_completed_exports", exclude).apply()
  }

  fun loadShowCompletedDebtsInCalendar(context: Context, defaultVal: Boolean = false): Boolean {
    return getPrefs(context).getBoolean("saved_show_completed_calendar", defaultVal)
  }

  fun saveShowCompletedDebtsInCalendar(context: Context, show: Boolean) {
    getPrefs(context).edit().putBoolean("saved_show_completed_calendar", show).apply()
  }

  fun loadCurrencySymbol(context: Context, defaultVal: String = "$"): String {
    return getPrefs(context).getString(KEY_CURRENCY_SYMBOL, defaultVal) ?: defaultVal
  }

  fun saveCurrencySymbol(context: Context, symbol: String) {
    getPrefs(context).edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
  }
}
