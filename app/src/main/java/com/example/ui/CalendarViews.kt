package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PersonRecord
import com.example.model.ReminderItem
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCard
import com.example.ui.theme.HighDensityOnPrimaryContainer
import com.example.ui.theme.HighDensityOwedToYou
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensityRedBadgeBg
import com.example.ui.theme.HighDensityRedBadgeText
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.HighDensityYouOwe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

fun isItemInMonthAndYear(item: ReminderItem, targetYear: Int, targetMonth: Int): Boolean {
  val cal = parseItemCalendar(item.relativeDateText, item.createdDate) ?: return false
  val startYear = cal.get(Calendar.YEAR)
  val startMonth = cal.get(Calendar.MONTH)

  val isSubscription = item.iconType == "subscription" || item.secondaryText.contains("Subscription", ignoreCase = true)

  if (isSubscription) {
    val isCancelled = item.secondaryText.contains("Cancelled", ignoreCase = true) || item.secondaryText.contains("Canceled", ignoreCase = true)
    if (isCancelled) {
      return startYear == targetYear && startMonth == targetMonth
    } else {
      return (targetYear > startYear) || (targetYear == startYear && targetMonth >= startMonth)
    }
  }

  return startYear == targetYear && startMonth == targetMonth
}

fun getItemDayInMonth(item: ReminderItem, targetYear: Int, targetMonth: Int, maxDaysInMonth: Int): Int? {
  val cal = parseItemCalendar(item.relativeDateText, item.createdDate) ?: return null
  val startYear = cal.get(Calendar.YEAR)
  val startMonth = cal.get(Calendar.MONTH)
  val startDay = cal.get(Calendar.DAY_OF_MONTH)

  val isSubscription = item.iconType == "subscription" || item.secondaryText.contains("Subscription", ignoreCase = true)

  if (isSubscription) {
    val isCancelled = item.secondaryText.contains("Cancelled", ignoreCase = true) || item.secondaryText.contains("Canceled", ignoreCase = true)
    val isApplicable = if (isCancelled) {
      startYear == targetYear && startMonth == targetMonth
    } else {
      (targetYear > startYear) || (targetYear == startYear && targetMonth >= startMonth)
    }
    if (!isApplicable) return null
    return startDay.coerceIn(1, maxDaysInMonth)
  }

  if (startYear == targetYear && startMonth == targetMonth) {
    return startDay.coerceIn(1, maxDaysInMonth)
  }
  return null
}

fun getSubscriptionRecurringAmount(item: ReminderItem): Double {
  val recurringMatch = Regex("""Recurring:\s*\$?([0-9.]+)""").find(item.secondaryText)
  if (recurringMatch != null) {
    val amt = recurringMatch.groupValues[1].toDoubleOrNull()
    if (amt != null && amt > 0.0) return amt
  }
  val backpayMatch = Regex("""Backpay:\s*\+\$?([0-9.]+)""").find(item.secondaryText)
    ?: Regex("""\+\$?([0-9.]+)\s*backpay""").find(item.secondaryText)
  if (backpayMatch != null) {
    val bp = backpayMatch.groupValues[1].toDoubleOrNull() ?: 0.0
    val diff = item.amount - bp
    if (diff > 0.0) return diff
  }
  return item.amount
}

fun getSubscriptionDisplaySecondaryText(item: ReminderItem, isFirstMonth: Boolean): String {
  if (isFirstMonth) return item.secondaryText
  var text = item.secondaryText
  text = text.replace(Regex("""Backpay:\s*\+\$?[0-9.]+\s*\(Recurring:\s*(\$?[0-9.]+(?:/[a-zA-Z]+)?)\)"""), "Recurring: $1")
  text = text.replace(Regex("""[•·,]?\s*\+\$?[0-9.]+\s*backpay"""), "").trim()
  text = text.replace(Regex("""•\s*•"""), "•").trim(' ', '•', ',')
  if (text.isBlank()) {
    text = "Subscription (monthly)"
  }
  return text
}

fun formatItemForMonthAndYear(item: ReminderItem, targetYear: Int, targetMonth: Int): ReminderItem {
  val isSubscription = item.iconType == "subscription" || item.secondaryText.contains("Subscription", ignoreCase = true)
  if (!isSubscription) return item

  val cal = parseItemCalendar(item.relativeDateText, item.createdDate)
  val startYear = cal?.get(Calendar.YEAR) ?: targetYear
  val startMonth = cal?.get(Calendar.MONTH) ?: targetMonth
  val isFirstMonth = (startYear == targetYear && startMonth == targetMonth)

  if (isFirstMonth) {
    return item
  } else {
    val recurringAmt = getSubscriptionRecurringAmount(item)
    val cleanSecondary = getSubscriptionDisplaySecondaryText(item, isFirstMonth = false)
    return item.copy(
      amount = recurringAmt,
      secondaryText = cleanSecondary,
    )
  }
}

fun concreteDateText(text: String): String {
  val t = text.trim()
  if (t.contains("Today", ignoreCase = true)) {
    return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
  }
  if (t.contains("Tomorrow", ignoreCase = true)) {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(cal.time)
  }
  if (t.length >= 3) return t
  return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
}

@Composable
fun CalendarScreenContent(
  reminders: List<ReminderItem>,
  people: List<PersonRecord> = emptyList(),
  isActualCalendarView: Boolean,
  currencySymbol: String = "$",
  onToggleView: (Boolean) -> Unit,
  onOpenSettings: () -> Unit,
  onBackToDashboard: () -> Unit,
  onEditDebt: (ReminderItem) -> Unit,
) {
  var displayedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
  val displayedYear = displayedCalendar.get(Calendar.YEAR)
  val displayedMonth = displayedCalendar.get(Calendar.MONTH)

  val onPrevMonth = {
    val newCal = displayedCalendar.clone() as Calendar
    newCal.add(Calendar.MONTH, -1)
    displayedCalendar = newCal
  }

  val onNextMonth = {
    val newCal = displayedCalendar.clone() as Calendar
    newCal.add(Calendar.MONTH, 1)
    displayedCalendar = newCal
  }

  val onResetToCurrentMonth = {
    displayedCalendar = Calendar.getInstance()
  }

  // Strictly filter items to only include items from the currently displayed month and year
  val monthReminders = remember(reminders, displayedYear, displayedMonth) {
    reminders.filter { isItemInMonthAndYear(it, displayedYear, displayedMonth) }
  }

  if (isActualCalendarView) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header with Navigation & Mode Toggle
      CalendarTopHeader(
        isActualCalendarView = true,
        displayedCalendar = displayedCalendar,
        displayedYear = displayedYear,
        displayedMonth = displayedMonth,
        onToggleView = onToggleView,
        onOpenSettings = onOpenSettings,
      )

      ActualMonthCalendarGridView(
        reminders = monthReminders,
        displayedYear = displayedYear,
        displayedMonth = displayedMonth,
        people = people,
        currencySymbol = currencySymbol,
        onPrevMonth = onPrevMonth,
        onNextMonth = onNextMonth,
        onResetToCurrentMonth = onResetToCurrentMonth,
        onEditDebt = onEditDebt,
      )
    }
  } else {
    // List view: non-scrolling parent so list expands to fill available height, with month switch buttons at the bottom
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header with Navigation & Mode Toggle
      CalendarTopHeader(
        isActualCalendarView = false,
        displayedCalendar = displayedCalendar,
        displayedYear = displayedYear,
        displayedMonth = displayedMonth,
        onToggleView = onToggleView,
        onOpenSettings = onOpenSettings,
      )

      // Middle: Flexible scrollable list of items for the month
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        RelativeHorizonsListView(
          reminders = reminders,
          displayedYear = displayedYear,
          displayedMonth = displayedMonth,
          people = people,
          currencySymbol = currencySymbol,
          onEditDebt = onEditDebt,
        )
      }

      // Bottom: Dedicated Month Switcher bar at bottom of screen
      CalendarBottomMonthSwitcher(
        displayedCalendar = displayedCalendar,
        displayedYear = displayedYear,
        displayedMonth = displayedMonth,
        onPrevMonth = onPrevMonth,
        onNextMonth = onNextMonth,
        onResetToCurrentMonth = onResetToCurrentMonth,
      )
    }
  }
}

@Composable
private fun CalendarTopHeader(
  isActualCalendarView: Boolean,
  displayedCalendar: Calendar,
  displayedYear: Int,
  displayedMonth: Int,
  onToggleView: (Boolean) -> Unit,
  onOpenSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
        text = if (isActualCalendarView) "Calendar Grid View" else "Upcoming Schedule",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = HighDensityTextPrimary,
      )
      val monthYearStr = remember(displayedYear, displayedMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.US).format(displayedCalendar.time)
      }
      val tzStr = remember { java.util.TimeZone.getDefault().id }
      Text(
        text = "$monthYearStr • $tzStr",
        fontSize = 12.sp,
        color = HighDensityTextSecondary,
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HighDensitySurface)
            .clickable { onToggleView(!isActualCalendarView) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("toggle_calendar_view_button"),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isActualCalendarView) Icons.Default.CalendarToday else Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = HighDensityPrimary,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isActualCalendarView) "List View" else "Grid View",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = HighDensityPrimary,
          )
        }
      }

      IconButton(onClick = onOpenSettings, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = HighDensityTextSecondary,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun CalendarBottomMonthSwitcher(
  displayedCalendar: Calendar,
  displayedYear: Int,
  displayedMonth: Int,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onResetToCurrentMonth: () -> Unit,
) {
  val now = remember { Calendar.getInstance() }
  val isCurrentMonth = displayedYear == now.get(Calendar.YEAR) && displayedMonth == now.get(Calendar.MONTH)
  val monthYearHeaderStr = remember(displayedYear, displayedMonth) {
    SimpleDateFormat("MMMM yyyy", Locale.US).format(displayedCalendar.time)
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp)),
    color = HighDensitySurface,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onPrevMonth,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(HighDensitySurfaceVariant)
          .testTag("list_prev_month_bottom_btn"),
      ) {
        Icon(
          imageVector = Icons.Default.ChevronLeft,
          contentDescription = "Previous Month",
          tint = HighDensityTextPrimary,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = monthYearHeaderStr,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
        )
        if (!isCurrentMonth) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = HighDensityPrimaryContainer,
            modifier = Modifier.clickable { onResetToCurrentMonth() },
          ) {
            Text(
              text = "Today",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityOnPrimaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
      }

      IconButton(
        onClick = onNextMonth,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(HighDensitySurfaceVariant)
          .testTag("list_next_month_bottom_btn"),
      ) {
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Next Month",
          tint = HighDensityTextPrimary,
        )
      }
    }
  }
}

// -------------------------------------------------------------
// Actual Monthly Calendar Grid View with Swipe & Bottom Month Arrows
// -------------------------------------------------------------
@Composable
fun ActualMonthCalendarGridView(
  reminders: List<ReminderItem>,
  displayedYear: Int,
  displayedMonth: Int,
  people: List<PersonRecord> = emptyList(),
  currencySymbol: String = "$",
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onResetToCurrentMonth: () -> Unit,
  onEditDebt: (ReminderItem) -> Unit,
) {
  val now = remember { Calendar.getInstance() }
  val todayYear = remember { now.get(Calendar.YEAR) }
  val todayMonth = remember { now.get(Calendar.MONTH) }
  val todayDayNumber = remember { now.get(Calendar.DAY_OF_MONTH) }

  val isCurrentMonth = (displayedYear == todayYear && displayedMonth == todayMonth)

  var selectedDay by remember(displayedYear, displayedMonth) {
    mutableStateOf<Int?>(null)
  }

  val monthCal = remember(displayedYear, displayedMonth) {
    Calendar.getInstance().apply {
      set(Calendar.YEAR, displayedYear)
      set(Calendar.MONTH, displayedMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
  }

  val daysInMonth = remember(monthCal) { monthCal.getActualMaximum(Calendar.DAY_OF_MONTH) }
  val dayOfWeekOfFirst = monthCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
  val leadingNulls = (dayOfWeekOfFirst + 5) % 7 // Monday-first

  val calendarCells = remember(displayedYear, displayedMonth, daysInMonth, leadingNulls) {
    buildList {
      for (i in 0 until leadingNulls) add(null)
      for (d in 1..daysInMonth) add(d)
      while (size % 7 != 0) add(null)
    }
  }

  // Map ONLY items that strictly match the displayed year & month, formatting subscriptions without backpay on subsequent months
  val debtDayMap = remember(reminders, displayedYear, displayedMonth, daysInMonth) {
    val map = mutableMapOf<Int, MutableList<ReminderItem>>()
    reminders.forEach { rawDebt ->
      val isSub = rawDebt.iconType == "subscription" || rawDebt.secondaryText.contains("Subscription", ignoreCase = true)
      if (isSub) {
        val dNum = getItemDayInMonth(rawDebt, displayedYear, displayedMonth, daysInMonth)
        if (dNum != null) {
          val formattedDebt = formatItemForMonthAndYear(rawDebt, displayedYear, displayedMonth)
          map.getOrPut(dNum) { mutableListOf() }.add(formattedDebt)
        }
      } else {
        val cal = parseItemCalendar(rawDebt.relativeDateText, rawDebt.createdDate)
        if (cal != null && cal.get(Calendar.YEAR) == displayedYear && cal.get(Calendar.MONTH) == displayedMonth) {
          val dNum = cal.get(Calendar.DAY_OF_MONTH)
          map.getOrPut(dNum) { mutableListOf() }.add(rawDebt)
        }
      }
    }
    map
  }

  val monthYearHeaderStr = remember(displayedYear, displayedMonth) {
    SimpleDateFormat("MMMM yyyy", Locale.US).format(monthCal.time)
  }

  var dragAccumulator by remember { mutableFloatStateOf(0f) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .pointerInput(displayedYear, displayedMonth) {
          detectHorizontalDragGestures(
            onDragEnd = {
              if (dragAccumulator < -40f) {
                onNextMonth()
              } else if (dragAccumulator > 40f) {
                onPrevMonth()
              }
              dragAccumulator = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
              dragAccumulator += dragAmount
            },
          )
        }
        .testTag("actual_calendar_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Weekday Headers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
        ) {
          listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Text(
              text = day,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.width(36.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of days
        LazyVerticalGrid(
          columns = GridCells.Fixed(7),
          modifier = Modifier.fillMaxWidth().height(260.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(calendarCells.size) { index ->
            val day = calendarCells[index]
            if (day == null) {
              Box(modifier = Modifier.aspectRatio(1f))
            } else {
              val isToday = isCurrentMonth && day == todayDayNumber
              val isSelected = day == selectedDay
              val dayDebts = debtDayMap[day] ?: emptyList()
              val hasDebts = dayDebts.isNotEmpty()

              Box(
                modifier =
                  Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      when {
                        isSelected -> HighDensityPrimary
                        isToday -> HighDensityPrimaryContainer
                        hasDebts -> HighDensityRedBadgeBg
                        else -> Color.Transparent
                      },
                    )
                    .border(
                      width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                      color = if (isToday && !isSelected) HighDensityPrimary else Color.Transparent,
                      shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { selectedDay = if (selectedDay == day) null else day },
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "$day",
                  fontSize = 13.sp,
                  fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                  color =
                    when {
                      isSelected -> Color.White
                      isToday -> HighDensityOnPrimaryContainer
                      hasDebts -> HighDensityRedBadgeText
                      else -> HighDensityTextPrimary
                    },
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation arrows at the bottom of the calendar to swipe / navigate between months
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onPrevMonth,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(HighDensitySurfaceVariant)
              .testTag("calendar_prev_month_bottom_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Previous Month",
              tint = HighDensityTextPrimary,
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = monthYearHeaderStr,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )
            if (!isCurrentMonth) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = HighDensityPrimaryContainer,
                modifier = Modifier.clickable { onResetToCurrentMonth() },
              ) {
                Text(
                  text = "Today",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityOnPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
          }

          IconButton(
            onClick = onNextMonth,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(HighDensitySurfaceVariant)
              .testTag("calendar_next_month_bottom_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next Month",
              tint = HighDensityTextPrimary,
            )
          }
        }
      }
    }

    // Selected Day Inspection Card (Only shown when a specific day is clicked)
    if (selectedDay != null) {
      val dayNum = selectedDay!!
      val selectedDayDebts = debtDayMap[dayNum] ?: emptyList()
      val concreteDateStr = remember(dayNum, displayedYear, displayedMonth) {
        val cal = Calendar.getInstance().apply {
          set(Calendar.YEAR, displayedYear)
          set(Calendar.MONTH, displayedMonth)
          set(Calendar.DAY_OF_MONTH, dayNum)
        }
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(cal.time)
      }

      Surface(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp)),
      color = HighDensityCard,
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = concreteDateStr,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "${selectedDayDebts.size} debt events",
            fontSize = 11.sp,
            color = HighDensityTextSecondary,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDayDebts.isEmpty()) {
          Text(
            text = "No debt obligations scheduled on this date.",
            fontSize = 12.sp,
            color = HighDensityTextSecondary,
            fontStyle = FontStyle.Italic,
          )
        } else {
          selectedDayDebts.forEach { item ->
            val pName = item.getAssociatedPerson()
            val matchedPerson = people.find { it.name.equals(pName, ignoreCase = true) || item.secondaryText.contains(it.name, ignoreCase = true) }
            val personColor = matchedPerson?.colorHex?.let { Color(it) }

            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onEditDebt(item) }
                  .padding(vertical = 4.dp, horizontal = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextPrimary,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  if (personColor != null) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(personColor))
                  }
                  Text(
                    text = item.secondaryText,
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                  )
                }
              }

              Text(
                text = "${currencySymbol}${String.format(Locale.US, "%.2f", item.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isPaid) HighDensityOwedToYou else HighDensityYouOwe,
              )
            }
          }
        }
      }
    }
  }
}
}

// -------------------------------------------------------------
// Relative Horizons List View (Filtered to displayed month & year)
// -------------------------------------------------------------
@Composable
fun RelativeHorizonsListView(
  reminders: List<ReminderItem>,
  displayedYear: Int,
  displayedMonth: Int,
  people: List<PersonRecord> = emptyList(),
  currencySymbol: String = "$",
  onEditDebt: (ReminderItem) -> Unit,
) {
  val monthCal = remember(displayedYear, displayedMonth) {
    Calendar.getInstance().apply {
      set(Calendar.YEAR, displayedYear)
      set(Calendar.MONTH, displayedMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
  }
  val daysInMonth = remember(displayedYear, displayedMonth) {
    monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }
  val monthYearHeaderStr = remember(displayedYear, displayedMonth) {
    SimpleDateFormat("MMMM yyyy", Locale.US).format(monthCal.time)
  }

  // Map each day of the displayed month strictly to its events, formatting subscriptions without backpay on subsequent months
  val monthEventsByDay = remember(reminders, displayedYear, displayedMonth, daysInMonth) {
    val map = sortedMapOf<Int, MutableList<ReminderItem>>()
    reminders.forEach { rawItem ->
      val day = getItemDayInMonth(rawItem, displayedYear, displayedMonth, daysInMonth)
      if (day != null) {
        val formattedItem = formatItemForMonthAndYear(rawItem, displayedYear, displayedMonth)
        map.getOrPut(day) { mutableListOf() }.add(formattedItem)
      }
    }
    map
  }

  val activeDays = remember(monthEventsByDay) {
    monthEventsByDay.keys.toList()
  }

  if (activeDays.isEmpty()) {
    HighDensityEmptyState(
      icon = Icons.Default.CalendarToday,
      title = "No Scheduled Events in $monthYearHeaderStr",
      subtitle = "No debts or recurring subscriptions are scheduled for this month.",
      guideSteps = listOf(
        "Browse Other Months" to "Use the switcher at the bottom to navigate between past and upcoming months.",
        "Add Debt Due Dates" to "Set due dates when creating debts to view their schedule here."
      ),
      testTag = "calendar_empty_state",
    )
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(activeDays) { day ->
        val matched = monthEventsByDay[day] ?: emptyList()
        val dayCal = Calendar.getInstance().apply {
          set(Calendar.YEAR, displayedYear)
          set(Calendar.MONTH, displayedMonth)
          set(Calendar.DAY_OF_MONTH, day)
        }
        val groupDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(dayCal.time)

        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(HighDensitySurface)
              .padding(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = groupDate,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = HighDensityTextPrimary,
            )
            Text(
              text = "${matched.size} ${if (matched.size == 1) "event" else "events"}",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = HighDensityPrimary,
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          matched.forEach { item ->
            val pName = item.getAssociatedPerson()
            val matchedPerson = people.find { it.name.equals(pName, ignoreCase = true) || item.secondaryText.contains(it.name, ignoreCase = true) }
            val personColor = matchedPerson?.colorHex?.let { Color(it) }

            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onEditDebt(item) }
                  .padding(vertical = 6.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextPrimary,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  if (personColor != null) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(personColor))
                  }
                  Text(
                    text = item.secondaryText,
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                  )
                }
              }

              Text(
                text = "${currencySymbol}${String.format(Locale.US, "%.2f", item.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isPaid) HighDensityOwedToYou else HighDensityYouOwe,
              )
            }
          }
        }
      }
    }
  }
}
