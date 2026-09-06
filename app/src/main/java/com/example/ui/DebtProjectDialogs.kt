package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ExpenseLineItem
import com.example.model.PersonRecord
import com.example.model.ProjectRecord
import com.example.model.ReminderItem
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensityBlueBadgeBg
import com.example.ui.theme.HighDensityBlueBadgeText
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// Month Date Picker Dialog (Navigate months & pick any date)
// -------------------------------------------------------------
@Composable
fun MonthDatePickerDialog(
  initialYear: Int = Calendar.getInstance().get(Calendar.YEAR),
  initialMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
  initialDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
  onDismiss: () -> Unit,
  onDateSelected: (formattedDate: String) -> Unit,
) {
  var displayYear by remember { mutableIntStateOf(initialYear) }
  var displayMonth by remember { mutableIntStateOf(initialMonth) }
  var selectedDay by remember { mutableIntStateOf(initialDay) }
  var selectedYear by remember { mutableIntStateOf(initialYear) }
  var selectedMonth by remember { mutableIntStateOf(initialMonth) }

  val monthNamesFull =
    listOf(
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December",
    )
  val monthNamesShort =
    listOf(
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
  val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

  val cal =
    Calendar.getInstance().apply {
      set(Calendar.YEAR, displayYear)
      set(Calendar.MONTH, displayMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
  val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
  val leadingSpaces = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
  val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = 440.dp)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("month_date_picker_dialog"),
      color = HighDensitySurface,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Month navigation header (Back and Forth)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = {
              if (displayMonth == 0) {
                displayMonth = 11
                displayYear--
              } else {
                displayMonth--
              }
            },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(HighDensityCard).testTag("prev_month_button"),
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Previous Month",
              tint = HighDensityTextPrimary,
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${monthNamesFull[displayMonth]} $displayYear",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )
            Text(
              text = "Past & future dates are selectable",
              fontSize = 10.sp,
              color = HighDensityTextSecondary,
            )
          }

          IconButton(
            onClick = {
              if (displayMonth == 11) {
                displayMonth = 0
                displayYear++
              } else {
                displayMonth++
              }
            },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(HighDensityCard).testTag("next_month_button"),
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next Month",
              tint = HighDensityTextPrimary,
            )
          }
        }

        // Weekday column headers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
        ) {
          dayNames.forEach { dayName ->
            Text(
              text = dayName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextSecondary,
              modifier = Modifier.width(36.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
          }
        }

        // Calendar Grid
        val totalGridSlots = leadingSpaces + maxDaysInMonth
        LazyVerticalGrid(
          columns = GridCells.Fixed(7),
          modifier = Modifier.fillMaxWidth().height(320.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          items(totalGridSlots) { index ->
            if (index < leadingSpaces) {
              Box(modifier = Modifier.size(36.dp))
            } else {
              val dayNumber = index - leadingSpaces + 1
              val isSelected = selectedDay == dayNumber && selectedMonth == displayMonth && selectedYear == displayYear
              val nowCal = Calendar.getInstance()
              val isToday = dayNumber == nowCal.get(Calendar.DAY_OF_MONTH) &&
                  displayMonth == nowCal.get(Calendar.MONTH) &&
                  displayYear == nowCal.get(Calendar.YEAR)

              Box(
                modifier =
                  Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      when {
                        isSelected -> HighDensityPrimary
                        isToday -> HighDensityPrimaryContainer
                        else -> HighDensityCard
                      },
                    )
                    .border(
                      width = if (isToday && !isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected || isToday) HighDensityPrimary else HighDensityBorder,
                      shape = RoundedCornerShape(8.dp),
                    )
                    .clickable {
                      selectedDay = dayNumber
                      selectedMonth = displayMonth
                      selectedYear = displayYear
                    }
                    .testTag("calendar_day_$dayNumber"),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "$dayNumber",
                  fontSize = 12.sp,
                  fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else if (isToday) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                )
              }
            }
          }
        }

        // Selected Date Summary Bar
        Surface(
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
          color = HighDensityCard,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Chosen Date",
                fontSize = 10.sp,
                color = HighDensityTextSecondary,
              )
              Text(
                text = "${monthNamesShort[selectedMonth]} $selectedDay, $selectedYear",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityPrimary,
              )
            }
            TextButton(
              onClick = {
                val now = Calendar.getInstance()
                displayYear = now.get(Calendar.YEAR)
                displayMonth = now.get(Calendar.MONTH)
                selectedYear = now.get(Calendar.YEAR)
                selectedMonth = now.get(Calendar.MONTH)
                selectedDay = now.get(Calendar.DAY_OF_MONTH)
              },
            ) {
              Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
            }
          }
        }

        // Action Buttons (Cancel / Confirm)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Cancel", color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = {
              val formatted = "${monthNamesShort[selectedMonth]} $selectedDay, $selectedYear"
              onDateSelected(formatted)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.3f).height(44.dp).testTag("confirm_date_picker_button"),
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Select Date", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Entirely Modernized Add Debt Screen (Full-Surface Dialog)
// -------------------------------------------------------------
@Composable
fun RedesignedAddDebtDialog(
  people: List<PersonRecord> = emptyList(),
  initialPerson: String? = null,
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, relativeDate: String, payer: String, category: String, isPositive: Boolean) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("One-Off") } // Default: One-Off
  var debtDirection by remember { mutableStateOf("Owed to Me") } // Default: Owed to You
  var payerName by remember(initialPerson) { mutableStateOf(initialPerson ?: "") }
  val todayFormatted = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date()) }
  var relativeDueChoice by remember { mutableStateOf(todayFormatted) } // Default: Due Today (Current device date)
  var customSelectedDate by remember { mutableStateOf<String?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }

  // Sub-options for categories
  var subscriptionFrequency by remember { mutableStateOf("Monthly") }
  val subscriptionFrequencies = listOf("Weekly", "Bi-weekly", "Monthly", "Yearly", "Custom")
  var customFrequencyText by remember { mutableStateOf("") }
  var enableBackpay by remember { mutableStateOf(false) }
  var backpayAmountText by remember { mutableStateOf("") }

  var loanInterestRate by remember { mutableStateOf("0% (Interest-Free)") }
  val commonLoanRates = listOf("0% (Interest-Free)", "3.5%", "5.0%", "7.5%", "10.0%", "12.0%", "15.0%")

  val categories =
    listOf(
      Triple("One-Off", "Single bill, meal or expense", Icons.Default.AccountBalanceWallet),
      Triple("Subscription", "Recurring subscription", Icons.Default.Subscriptions),
      Triple("Fixed Loan", "Agreed installment or loan", Icons.Default.AccountTree),
    )
  val relativeChoices = listOf("Due Today", "Due Tomorrow", "This Saturday", "Next Saturday", "First of next month")
  val samplePayers = listOf("Sarah L.", "Marcus K.", "Alex Green", "Elena R.")
  val quickAmounts = listOf(15, 25, 50, 100)

  val currentAmt = amountText.toDoubleOrNull() ?: 0.0
  val scrollState = rememberScrollState()

  if (showDatePicker) {
    MonthDatePickerDialog(
      onDismiss = { showDatePicker = false },
      onDateSelected = { dateStr ->
        customSelectedDate = dateStr
        relativeDueChoice = dateStr
      },
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.95f)
          .widthIn(max = 520.dp)
          .fillMaxHeight(0.88f)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("add_debt_dialog"),
      color = HighDensityBackground,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        // Drag Pill & Top Bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(HighDensityBorder),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "New Debt Entry",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text = "Phoenix UTC-07 • Relative Timeline",
                fontSize = 10.sp,
                color = HighDensityTextSecondary,
              )
            }

            IconButton(
              onClick = onDismiss,
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(HighDensitySurface),
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = HighDensityTextPrimary,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }

        // Scrollable middle section
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

        // 1. Directional Selector Cards (You Owe vs Owed to You)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // You Owe Option
          val isOwe = debtDirection == "I Owe"
          Surface(
            modifier =
              Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isOwe) 1.5.dp else 1.dp,
                  color = if (isOwe) HighDensityYouOwe else HighDensityBorder,
                  shape = RoundedCornerShape(10.dp),
                )
                .clickable { debtDirection = "I Owe" }
                .testTag("direction_i_owe"),
            color = if (isOwe) HighDensityRedBadgeBg else HighDensityCard,
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isOwe) HighDensityYouOwe else HighDensitySurfaceVariant),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = if (isOwe) Color.White else HighDensityTextSecondary,
                  modifier = Modifier.size(11.dp),
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "You Owe",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isOwe) HighDensityRedBadgeText else HighDensityTextPrimary,
              )
              if (isOwe) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = HighDensityYouOwe,
                  modifier = Modifier.size(12.dp),
                )
              }
            }
          }

          // Owed to You Option
          val isOwedToMe = debtDirection == "Owed to Me"
          Surface(
            modifier =
              Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isOwedToMe) 1.5.dp else 1.dp,
                  color = if (isOwedToMe) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(10.dp),
                )
                .clickable { debtDirection = "Owed to Me" }
                .testTag("direction_owed_to_me"),
            color = if (isOwedToMe) HighDensityPrimaryContainer else HighDensityCard,
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isOwedToMe) HighDensityPrimary else HighDensitySurfaceVariant),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = if (isOwedToMe) Color.White else HighDensityTextSecondary,
                  modifier = Modifier.size(11.dp),
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Owed to You",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isOwedToMe) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
              )
              if (isOwedToMe) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = HighDensityPrimary,
                  modifier = Modifier.size(12.dp),
                )
              }
            }
          }
        }

        // 2. Hero Amount Card with Quick Increments
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
          border = CardDefaults.outlinedCardBorder(),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(vertical = 0.dp),
            ) {
              Text(
                text = "$",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (debtDirection == "I Owe") HighDensityYouOwe else HighDensityOwedToYou,
              )
              Spacer(modifier = Modifier.width(4.dp))
              OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                placeholder = { Text("0.00", fontSize = 22.sp, color = HighDensityTextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(140.dp).testTag("debt_amount_input"),
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                  ),
                textStyle =
                  androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  ),
              )
            }

            // Quick increment chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
              quickAmounts.forEach { amt ->
                Box(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(HighDensityCard)
                      .border(1.dp, HighDensityBorder, RoundedCornerShape(6.dp))
                      .clickable {
                        val current = amountText.toDoubleOrNull() ?: 0.0
                        amountText = String.format("%.2f", current + amt)
                      }
                      .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                  Text(
                    text = "+$$amt",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensityPrimary,
                  )
                }
              }
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HighDensityCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(6.dp))
                    .clickable { amountText = "" }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
              ) {
                Text(
                  text = "Clear",
                  fontSize = 10.sp,
                  color = HighDensityTextSecondary,
                )
              }
            }
          }
        }

        // 3. Title Input (Below Enter Amount and Above Category)
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Debt Title / Label", fontSize = 11.sp) },
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
          modifier = Modifier.fillMaxWidth().testTag("debt_title_input"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true,
        )

        // 4. Category Tiles
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Category",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            categories.forEach { (catName, _, icon) ->
              val isSelected = selectedCategory == catName
              Surface(
                modifier =
                  Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) HighDensityPrimary else HighDensityBorder,
                      shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { selectedCategory = catName },
                color = if (isSelected) HighDensityPrimaryContainer else HighDensityCard,
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = catName,
                    tint = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextSecondary,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = catName,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                  )
                }
              }
            }
          }

          // Category Sub-Options: Subscription Frequencies & Backpay
          if (selectedCategory == "Subscription") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
              border = CardDefaults.outlinedCardBorder(),
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "Recurring Billing Frequency",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextPrimary,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(subscriptionFrequencies) { freq ->
                    val isFreqSelected = subscriptionFrequency == freq
                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .border(
                            width = if (isFreqSelected) 1.5.dp else 1.dp,
                            color = if (isFreqSelected) HighDensityPrimary else HighDensityBorder,
                            shape = RoundedCornerShape(6.dp),
                          )
                          .clickable { subscriptionFrequency = freq },
                      color = if (isFreqSelected) HighDensityPrimaryContainer else HighDensityCard,
                    ) {
                      Text(
                        text = freq,
                        fontSize = 10.sp,
                        fontWeight = if (isFreqSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFreqSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      )
                    }
                  }
                }

                val activeFrequency = if (subscriptionFrequency == "Custom") customFrequencyText.ifBlank { "Custom" } else subscriptionFrequency

                if (subscriptionFrequency == "Custom") {
                  OutlinedTextField(
                    value = customFrequencyText,
                    onValueChange = { customFrequencyText = it },
                    label = { Text("Custom Frequency", fontSize = 10.sp) },
                    placeholder = { Text("e.g. Every 10 days, Quarterly, etc.", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("subscription_custom_frequency_input"),
                    shape = RoundedCornerShape(8.dp),
                  )
                }

                HorizontalDivider(color = HighDensityBorder, thickness = 0.5.dp)

                // Backpay / Starting Fee Option
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Include Backpay / Starting Fee",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = HighDensityTextPrimary,
                    )
                    Text(
                      text = "Adds a starting cost on top of regular $activeFrequency cost",
                      fontSize = 9.sp,
                      color = HighDensityTextSecondary,
                    )
                  }
                  Switch(
                    checked = enableBackpay,
                    onCheckedChange = { enableBackpay = it },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = Color.White,
                      checkedTrackColor = HighDensityPrimary,
                    ),
                    modifier = Modifier.testTag("add_backpay_switch"),
                  )
                }

                if (enableBackpay) {
                  OutlinedTextField(
                    value = backpayAmountText,
                    onValueChange = { backpayAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Starting Backpay / Setup Fee ($)", fontSize = 10.sp) },
                    placeholder = { Text("0.00", fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("subscription_backpay_input"),
                    shape = RoundedCornerShape(8.dp),
                  )

                  val backpayVal = backpayAmountText.toDoubleOrNull() ?: 0.0
                  val firstChargeTotal = currentAmt + backpayVal

                  Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                    color = HighDensityPrimaryContainer.copy(alpha = 0.4f),
                  ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                      Text(
                        text = "1st Payment Total: $${String.format(Locale.US, "%.2f", firstChargeTotal)} ($${String.format(Locale.US, "%.2f", currentAmt)} $activeFrequency + $${String.format(Locale.US, "%.2f", backpayVal)} backpay)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityOnPrimaryContainer,
                      )
                      Text(
                        text = "Next Payments: $${String.format(Locale.US, "%.2f", currentAmt)} / $activeFrequency",
                        fontSize = 9.sp,
                        color = HighDensityTextSecondary,
                      )
                    }
                  }
                }
              }
            }
          }

          // Category Sub-Options: Fixed Loan Interest Rates
          if (selectedCategory == "Fixed Loan") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
              border = CardDefaults.outlinedCardBorder(),
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "Interest Rate (APR)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensityTextPrimary,
                  )
                  if (loanInterestRate != "0% (Interest-Free)" && currentAmt > 0.0) {
                    val rateClean = loanInterestRate.replace("%", "").toDoubleOrNull() ?: 0.0
                    val estInterest = currentAmt * (rateClean / 100.0)
                    Text(
                      text = "+$${String.format("%.2f", estInterest)} est. interest",
                      fontSize = 9.sp,
                      color = HighDensityPrimary,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(commonLoanRates) { rate ->
                    val isRateSelected = loanInterestRate == rate
                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .border(
                            width = if (isRateSelected) 1.5.dp else 1.dp,
                            color = if (isRateSelected) HighDensityPrimary else HighDensityBorder,
                            shape = RoundedCornerShape(6.dp),
                          )
                          .clickable { loanInterestRate = rate },
                      color = if (isRateSelected) HighDensityPrimaryContainer else HighDensityCard,
                    ) {
                      Text(
                        text = rate,
                        fontSize = 10.sp,
                        fontWeight = if (isRateSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRateSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // 5. People Directory (Required)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "People Directory (Required)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )
            if (payerName.isNotBlank()) {
              Text(
                text = "Clear selection",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityPrimary,
                modifier = Modifier.clickable { payerName = "" },
              )
            }
          }

          val availablePeople = people

          if (availablePeople.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(availablePeople) { person ->
                val pName = person.name
                val pColor = Color(person.colorHex)
                val isSelected = payerName.equals(pName, ignoreCase = true)
                Surface(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(16.dp))
                      .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) pColor else HighDensityBorder,
                        shape = RoundedCornerShape(16.dp),
                      )
                      .clickable {
                        payerName = if (isSelected) "" else pName
                      },
                  color = if (isSelected) HighDensityPrimaryContainer else HighDensityCard,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Box(
                      modifier = Modifier.size(16.dp).clip(CircleShape).background(pColor),
                      contentAlignment = Alignment.Center,
                    ) {
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(10.dp),
                        )
                      } else {
                        Text(
                          text = pName.take(1).uppercase(),
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White,
                        )
                      }
                    }
                    Text(
                      text = pName,
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                    )
                  }
                }
              }
            }
          }
        }

        // 6. Schedule (Due Today or Pick Date)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Due Date",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // "Due Today" button
            val isDueToday = relativeDueChoice == todayFormatted && customSelectedDate == null
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isDueToday) 1.5.dp else 1.dp,
                  color = if (isDueToday) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(10.dp),
                )
                .clickable {
                  relativeDueChoice = todayFormatted
                  customSelectedDate = null
                }
                .testTag("due_today_button"),
              color = if (isDueToday) HighDensityPrimaryContainer else HighDensityCard,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = if (isDueToday) HighDensityOnPrimaryContainer else HighDensityTextSecondary,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Due Today",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDueToday) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                )
              }
            }

            // "Pick Date" button
            val isCustomPicked = customSelectedDate != null
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isCustomPicked) 1.5.dp else 1.dp,
                  color = if (isCustomPicked) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(10.dp),
                )
                .clickable { showDatePicker = true }
                .testTag("open_due_calendar_button"),
              color = if (isCustomPicked) HighDensityPrimaryContainer else HighDensityCard,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = if (isCustomPicked) HighDensityOnPrimaryContainer else HighDensityPrimary,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isCustomPicked) customSelectedDate!! else "Pick Date",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCustomPicked) HighDensityOnPrimaryContainer else HighDensityPrimary,
                )
              }
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            color = HighDensitySurface,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Scheduled Due Date:",
                fontSize = 10.sp,
                color = HighDensityTextSecondary,
              )
              Text(
                text = relativeDueChoice,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityPrimary,
              )
            }
          }
        }

        } // Close scrollable middle section

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(40.dp),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Cancel", color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }

          Button(
            onClick = {
              if (title.isNotBlank() && currentAmt > 0.0 && payerName.isNotBlank()) {
                val isPositive = debtDirection == "Owed to Me"
                val backpayVal = if (enableBackpay && selectedCategory == "Subscription") (backpayAmountText.toDoubleOrNull() ?: 0.0) else 0.0
                val totalInitialAmt = currentAmt + backpayVal
                val activeFreq = if (subscriptionFrequency == "Custom") customFrequencyText.ifBlank { "Custom" } else subscriptionFrequency
                val finalCategory =
                  when (selectedCategory) {
                    "Subscription" -> {
                      if (backpayVal > 0.0) "Subscription ($activeFreq) • +$${String.format(Locale.US, "%.2f", backpayVal)} backpay"
                      else "Subscription ($activeFreq)"
                    }
                    "Fixed Loan" -> "Fixed Loan ($loanInterestRate)"
                    else -> "One-Off"
                  }
                onConfirm(title, totalInitialAmt, relativeDueChoice, payerName, finalCategory, isPositive)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (title.isNotBlank() && currentAmt > 0.0 && payerName.isNotBlank()) HighDensityPrimary else HighDensityBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1.5f).height(40.dp).testTag("confirm_add_debt_button"),
            enabled = title.isNotBlank() && currentAmt > 0.0 && payerName.isNotBlank(),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Entirely Modernized Create Project Screen (Slimmed High-Density Dialog)
// -------------------------------------------------------------
@Composable
fun RedesignedAddProjectDialog(
  people: List<PersonRecord> = emptyList(),
  onDismiss: () -> Unit,
  onConfirm: (ProjectRecord) -> Unit,
) {
  var projectName by remember { mutableStateOf("") }
  var budgetText by remember { mutableStateOf("") }
  var selectedProjectCategory by remember { mutableStateOf("Shared Purchase") }
  val participantsList = remember { mutableStateListOf("You (Owner)") }
  var newParticipantInput by remember { mutableStateOf("") }

  // Initial Line Item (Optional)
  var initialAmountText by remember { mutableStateOf("") }
  var contributionType by remember { mutableStateOf("Cash") }
  var initialContributor by remember { mutableStateOf("You (Owner)") }

  val projectCategories = listOf("Shared Purchase", "Home & Reno", "Office & Studio", "Travel & Event")
  val contributionTypes = listOf("Cash", "Goods", "Services")
  val quickBudgets = listOf(500, 1200, 2500, 5000)

  val currentBudget = budgetText.toDoubleOrNull() ?: 0.0
  val scrollState = rememberScrollState()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.95f)
          .widthIn(max = 480.dp)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("add_project_dialog"),
      color = HighDensityBackground,
      tonalElevation = 6.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Drag Pill & Top Bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(HighDensityBorder),
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Create Shared Project",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )

            IconButton(
              onClick = onDismiss,
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(HighDensitySurface),
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = HighDensityTextPrimary,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }

        // 1. Category Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(projectCategories) { cat ->
            val isSelected = selectedProjectCategory == cat
            FilterChip(
              selected = isSelected,
              onClick = { selectedProjectCategory = cat },
              label = { Text(cat, fontSize = 11.sp) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HighDensityPrimaryContainer,
                  selectedLabelColor = HighDensityOnPrimaryContainer,
                ),
            )
          }
        }

        // 2. Project Name
        OutlinedTextField(
          value = projectName,
          onValueChange = { projectName = it },
          label = { Text("Project Title") },
          placeholder = { Text("e.g., Office Renovation") },
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
          leadingIcon = {
            Icon(Icons.Default.AccountTree, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth().testTag("project_name_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
        )

        // 3. Compact Target Budget Section
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "TARGET BUDGET",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
              if (currentBudget > 0.0) {
                Text(
                  text = "$${String.format("%,.2f", currentBudget)}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityPrimary,
                )
              }
            }

            OutlinedTextField(
              value = budgetText,
              onValueChange = { budgetText = it.filter { ch -> ch.isDigit() || ch == '.' } },
              placeholder = { Text("Enter target budget ($)") },
              leadingIcon = {
                Text(
                  text = "$",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityPrimary,
                  modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.fillMaxWidth().testTag("project_budget_input"),
              shape = RoundedCornerShape(10.dp),
              singleLine = true,
            )

            // Quick budget shortcuts
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              quickBudgets.forEach { bVal ->
                val isCurrent = budgetText == "$bVal.00" || budgetText == "$bVal"
                Box(
                  modifier =
                    Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isCurrent) HighDensityPrimaryContainer else HighDensityCard)
                      .border(1.dp, if (isCurrent) HighDensityPrimary else HighDensityBorder, RoundedCornerShape(8.dp))
                      .clickable { budgetText = "$bVal.00" }
                      .padding(vertical = 5.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = "$$bVal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrent) HighDensityOnPrimaryContainer else HighDensityPrimary,
                  )
                }
              }
            }
          }
        }

        // 4. Participants Roster Builder
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Participants (${participantsList.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              if (currentBudget > 0.0) {
                Text(
                  text = "$${String.format("%,.2f", currentBudget / maxOf(1, participantsList.size))} / person",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityPrimary,
                )
              } else {
                Text(
                  text = "Split evenly",
                  fontSize = 10.sp,
                  color = HighDensityTextSecondary,
                )
              }
            }

            // 4a. Select from People Screen Contacts
            if (people.isNotEmpty()) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Select from People contacts:",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextSecondary,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(people) { person ->
                    val isIncluded = participantsList.contains(person.name)
                    val pColor = Color(person.colorHex)
                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(16.dp))
                          .border(
                            width = if (isIncluded) 1.5.dp else 1.dp,
                            color = if (isIncluded) pColor else HighDensityBorder,
                            shape = RoundedCornerShape(16.dp),
                          )
                          .clickable {
                            if (isIncluded) {
                              participantsList.remove(person.name)
                              if (initialContributor == person.name) {
                                initialContributor = "You (Owner)"
                              }
                            } else {
                              participantsList.add(person.name)
                            }
                          },
                      color = if (isIncluded) HighDensityPrimaryContainer else HighDensityCard,
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                      ) {
                        Box(
                          modifier = Modifier.size(14.dp).clip(CircleShape).background(pColor),
                          contentAlignment = Alignment.Center,
                        ) {
                          if (isIncluded) {
                            Icon(
                              Icons.Default.Check,
                              contentDescription = "Selected",
                              tint = Color.White,
                              modifier = Modifier.size(10.dp),
                            )
                          }
                        }
                        Text(
                          text = person.name,
                          fontSize = 11.sp,
                          fontWeight = if (isIncluded) FontWeight.Bold else FontWeight.Medium,
                          color = if (isIncluded) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        )
                        Icon(
                          imageVector = if (isIncluded) Icons.Default.Check else Icons.Default.Add,
                          contentDescription = if (isIncluded) "Selected" else "Add",
                          tint = if (isIncluded) HighDensityPrimary else HighDensityTextSecondary,
                          modifier = Modifier.size(12.dp),
                        )
                      }
                    }
                  }
                }
              }
            }

            // 4b. Active Selected Participant Badges
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "Selected for project:",
                fontSize = 10.sp,
                color = HighDensityTextSecondary,
              )

              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(participantsList) { p ->
                  val matchingPerson = people.find { it.name.equals(p, ignoreCase = true) }
                  val badgeColor = matchingPerson?.let { Color(it.colorHex) } ?: HighDensityPrimary

                  Surface(
                    modifier =
                      Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp)),
                    color = HighDensityCard,
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Box(
                        modifier =
                          Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center,
                      ) {
                        Text(
                          text = p.take(1).uppercase(),
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White,
                        )
                      }
                      Text(text = p, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                      if (!p.contains("Owner")) {
                        Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Remove",
                          tint = HighDensityTextSecondary,
                          modifier =
                            Modifier
                              .size(13.dp)
                              .clickable {
                                participantsList.remove(p)
                                if (initialContributor == p) {
                                  initialContributor = "You (Owner)"
                                }
                              },
                        )
                      }
                    }
                  }
                }
              }
            }

            // 4c. Quick add other participant input (if not in contacts)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                value = newParticipantInput,
                onValueChange = { newParticipantInput = it },
                placeholder = { Text("Or type other participant name...", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
              )
              Button(
                onClick = {
                  if (newParticipantInput.isNotBlank() && !participantsList.contains(newParticipantInput.trim())) {
                    participantsList.add(newParticipantInput.trim())
                    newParticipantInput = ""
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimaryContainer),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              ) {
                Text("+ Add", fontSize = 11.sp, color = HighDensityOnPrimaryContainer, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // 5. Initial Contribution Starter (Optional)
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensityCard,
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Initial Contribution (Optional)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )

            // Contributor Chip Selector from Participants
            if (participantsList.isNotEmpty()) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Contributor:",
                  fontSize = 10.sp,
                  color = HighDensityTextSecondary,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(participantsList) { p ->
                    val isContributorSelected = initialContributor == p
                    val matchingPerson = people.find { it.name.equals(p, ignoreCase = true) }
                    val dotColor = matchingPerson?.let { Color(it.colorHex) } ?: HighDensityPrimary

                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(14.dp))
                          .border(
                            width = if (isContributorSelected) 1.5.dp else 1.dp,
                            color = if (isContributorSelected) HighDensityPrimary else HighDensityBorder,
                            shape = RoundedCornerShape(14.dp),
                          )
                          .clickable { initialContributor = p },
                      color = if (isContributorSelected) HighDensityPrimaryContainer else HighDensitySurface,
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                      ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                        Text(
                          text = p,
                          fontSize = 10.sp,
                          fontWeight = if (isContributorSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isContributorSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        )
                      }
                    }
                  }
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                value = initialAmountText,
                onValueChange = { initialAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
              )

              Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Type:", fontSize = 10.sp, color = HighDensityTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  contributionTypes.forEach { t ->
                    val isSelected = contributionType == t
                    Box(
                      modifier =
                        Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(8.dp))
                          .background(if (isSelected) HighDensityPrimaryContainer else HighDensitySurface)
                          .border(
                            1.dp,
                            if (isSelected) HighDensityPrimary else HighDensityBorder,
                            RoundedCornerShape(8.dp),
                          )
                          .clickable { contributionType = t }
                          .padding(vertical = 5.dp),
                      contentAlignment = Alignment.Center,
                    ) {
                      Text(
                        text = t,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Cancel", color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }

          Button(
            onClick = {
              val initAmt = initialAmountText.toDoubleOrNull() ?: 0.0
              if (projectName.isNotBlank()) {
                val initialLineItems =
                  if (initAmt > 0.0 && initialContributor.isNotBlank()) {
                    listOf(
                      ExpenseLineItem(
                        id = "exp_init_${System.currentTimeMillis()}",
                        participantName = initialContributor.trim(),
                        contributionType = contributionType,
                        amount = initAmt,
                        relativeDate = "today",
                      ),
                    )
                  } else {
                    emptyList()
                  }

                val proj =
                  ProjectRecord(
                    id = "proj_${System.currentTimeMillis()}",
                    name = projectName.trim(),
                    totalEstimatedBudget = currentBudget,
                    participantsCount = participantsList.size,
                    relativeTime = "today",
                    lineItems = initialLineItems,
                  )
                onConfirm(proj)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.5f).height(44.dp).testTag("confirm_add_project_button"),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Create Project", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Settings Dialog with Calendar View Toggle
// -------------------------------------------------------------
@Composable
fun SettingsDialog(
  isActualCalendarView: Boolean,
  onToggleCalendarView: (Boolean) -> Unit,
  isDarkMode: Boolean = true,
  onToggleDarkMode: (Boolean) -> Unit = {},
  showProjectCostsInPeople: Boolean = false,
  onToggleShowProjectCostsInPeople: (Boolean) -> Unit = {},
  userName: String = "You",
  onUserNameChange: (String) -> Unit = {},
  enableNotifications: Boolean = false,
  onToggleNotifications: (Boolean) -> Unit = {},
  excludeCompletedFromExports: Boolean = false,
  onToggleExcludeCompletedFromExports: (Boolean) -> Unit = {},
  showCompletedDebtsInCalendar: Boolean = false,
  onToggleShowCompletedDebtsInCalendar: (Boolean) -> Unit = {},
  currencySymbol: String = "$",
  onCurrencySymbolChange: (String) -> Unit = {},
  onImportCsv: () -> Unit = {},
  onDismiss: () -> Unit,
  onExportCsv: () -> Unit,
  onExportPdf: () -> Unit,
) {
  val commonCurrencies = listOf(
    "$" to "USD/AUD ($)",
    "€" to "EUR (€)",
    "£" to "GBP (£)",
    "¥" to "JPY/CNY (¥)",
    "₹" to "INR (₹)",
    "C$" to "CAD (C$)",
    "A$" to "AUD (A$)",
    "CHF" to "CHF (CHF)",
    "kr" to "SEK/NOK (kr)",
    "R$" to "BRL (R$)",
    "₱" to "PHP (₱)",
    "₩" to "KRW (₩)",
    "zł" to "PLN (zł)",
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.95f)
          .widthIn(max = 520.dp)
          .fillMaxHeight(0.88f)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("settings_dialog"),
      color = HighDensityBackground,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Drag Pill
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(HighDensityBorder),
          )
        }

        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Settings & Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HighDensityTextPrimary)
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(HighDensitySurface).testTag("close_settings_button"),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighDensityTextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        HorizontalDivider(color = HighDensityBorder)

        // Scrollable Settings
        Column(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
          // --- Developer Donation Box ---
          val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityPrimary, RoundedCornerShape(14.dp))
              .clickable { uriHandler.openUri("https://buymeacoffee.com/problemchildperformance") },
            color = HighDensityPrimaryContainer
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                androidx.compose.foundation.Image(
                  painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.pcp_logo),
                  contentDescription = "Problem Child Performance Logo",
                  modifier = Modifier.size(54.dp),
                  colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(HighDensityOnPrimaryContainer)
                )
                Text(
                  text = "Made by Problem Child Performance",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityOnPrimaryContainer,
                  modifier = Modifier.weight(1f)
                )
              }
              Text(
                text = "I am a solo developer working on this project on the side. Your donations help keep my apps free and completely ad-free! Tap here to buy me a coffee.",
                fontSize = 11.sp,
                color = HighDensityOnPrimaryContainer.copy(alpha = 0.9f)
              )
            }
          }

          // Compact Profile & Currency Settings Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Your Name Card
            Surface(
              modifier = Modifier
                .weight(1.3f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
              color = HighDensitySurface,
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Your Profile Name",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityTextPrimary,
                )
                OutlinedTextField(
                  value = userName,
                  onValueChange = onUserNameChange,
                  placeholder = { Text("Enter name...", fontSize = 12.sp) },
                  modifier = Modifier.fillMaxWidth().testTag("user_name_input"),
                  singleLine = true,
                  textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                  shape = RoundedCornerShape(10.dp),
                )
              }
            }

            // Local Currency Card
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
              color = HighDensitySurface,
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Currency Symbol",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityTextPrimary,
                )
                OutlinedTextField(
                  value = currencySymbol,
                  onValueChange = onCurrencySymbolChange,
                  placeholder = { Text("e.g. $, €, £", fontSize = 12.sp) },
                  modifier = Modifier.fillMaxWidth().testTag("custom_currency_input"),
                  singleLine = true,
                  textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                  shape = RoundedCornerShape(10.dp),
                )
              }
            }
          }

        // Toggle for Dark Mode vs Light Mode
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Dark Mode",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text =
                  if (isDarkMode) {
                    "High density dark theme enabled."
                  } else {
                    "High density light theme enabled."
                  },
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = isDarkMode,
              onCheckedChange = onToggleDarkMode,
              modifier = Modifier.testTag("toggle_dark_mode_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }

        // Toggle for Actual Calendar View vs Relative Horizons
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Actual Calendar Grid View",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text =
                  if (isActualCalendarView) {
                    "Calendar tab shows monthly interactive calendar grid."
                  } else {
                    "Calendar tab shows relative time horizons (today, tomorrow, this week)."
                  },
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = isActualCalendarView,
              onCheckedChange = onToggleCalendarView,
              modifier = Modifier.testTag("toggle_calendar_grid_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }

        // Toggle for Showing Project Costs in People Section
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Show Project Costs in People",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text =
                  if (showProjectCostsInPeople) {
                    "Showing shared project expenses & contributions in people section."
                  } else {
                    "Hiding shared project expenses in people section."
                  },
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = showProjectCostsInPeople,
              onCheckedChange = onToggleShowProjectCostsInPeople,
              modifier = Modifier.testTag("toggle_project_costs_people_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }

        // Device Notifications Toggle
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Device Notifications",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text = if (enableNotifications) "App can send system notifications to this device." else "Notifications disabled.",
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = enableNotifications,
              onCheckedChange = onToggleNotifications,
              modifier = Modifier.testTag("toggle_notifications_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }

        // Toggle to Exclude Paid Off Debts & Associated Recorded Payments from Exports
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Exclude Completed from Exports",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text =
                  if (excludeCompletedFromExports) {
                    "Paid off debts and associated recorded payments will not appear on exports (active debts only)."
                  } else {
                    "Exports will show all payments and debts whether completed or not."
                  },
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = excludeCompletedFromExports,
              onCheckedChange = onToggleExcludeCompletedFromExports,
              modifier = Modifier.testTag("toggle_exclude_completed_exports_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }
          
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = "Show Paid Debts in Calendar",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text =
                  if (showCompletedDebtsInCalendar) {
                    "Calendar will display all debts, including those that have been paid off."
                  } else {
                    "Calendar will hide debts that have already been paid off (cleaner view)."
                  },
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                lineHeight = 14.sp,
              )
            }

            Switch(
              checked = showCompletedDebtsInCalendar,
              onCheckedChange = onToggleShowCompletedDebtsInCalendar,
              modifier = Modifier.testTag("toggle_show_completed_calendar_switch"),
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = HighDensityPrimary,
                  uncheckedThumbColor = HighDensityTextSecondary,
                  uncheckedTrackColor = HighDensityBorder,
                ),
            )
          }
        }

        // Export & Import Data section
        Text("Data Backup & Migration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = onExportCsv,
            modifier = Modifier.weight(1f).height(40.dp).testTag("export_csv_button"),
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export CSV", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = onExportPdf,
            modifier = Modifier.weight(1f).height(40.dp).testTag("export_pdf_button"),
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export PDF", fontSize = 11.sp)
          }
        }

        Button(
          onClick = onImportCsv,
          modifier = Modifier.fillMaxWidth().height(42.dp).testTag("import_csv_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimaryContainer),
        ) {
          Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityOnPrimaryContainer)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Import Data from CSV File", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityOnPrimaryContainer)
        }
      }

      // Done Button
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("settings_done_button"),
      ) {
        Text("Done", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}
}

// -------------------------------------------------------------
// Notifications Dialog
// -------------------------------------------------------------
@Composable
fun NotificationsDialog(
  reminders: List<ReminderItem>,
  onDismiss: () -> Unit,
  onEditDebt: (ReminderItem) -> Unit = {},
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("notifications_dialog"),
    title = { Text("Upcoming Notifications", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Local time: Phoenix (UTC-07:00)",
          fontSize = 12.sp,
          color = HighDensityTextSecondary,
        )
        if (reminders.isEmpty()) {
          Text("No pending debt alerts.")
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            reminders.forEach { r ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onEditDebt(r) }
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "• ${r.title}: ${r.relativeDateText} ($${String.format("%.2f", r.amount)})",
                  fontSize = 13.sp,
                  color = HighDensityTextPrimary,
                  modifier = Modifier.weight(1f)
                )
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Debt",
                  tint = HighDensityPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = HighDensityPrimary)
      }
    },
  )
}

// -------------------------------------------------------------
// Record Cash Payment Dialog (For Manual Cash Payments)
// -------------------------------------------------------------
@Composable
fun RecordCashPaymentDialog(
  person: PersonRecord,
  unpaidDebts: List<ReminderItem> = emptyList(),
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, isReceived: Boolean, note: String, paymentDate: String, linkedDebtId: String?) -> Unit,
) {
  var amountText by remember { mutableStateOf("") }

  // Direction: if first debt isPositive (they owe you), default to Received. If !isPositive (you owe them), default to Paid.
  val defaultIsReceived = unpaidDebts.firstOrNull()?.isPositive ?: true
  var isReceived by remember { mutableStateOf(defaultIsReceived) }

  val relevantDebts = remember(unpaidDebts, isReceived) {
    unpaidDebts.filter { it.isPositive == isReceived }
  }
  val relevantTotal = remember(relevantDebts) {
    relevantDebts.sumOf { it.amount }
  }

  var paymentDate by remember {
    mutableStateOf(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()))
  }
  var noteText by remember { mutableStateOf("Cash settlement") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val personColor = Color(person.colorHex)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = 480.dp)
          .clip(RoundedCornerShape(20.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
          .testTag("record_cash_payment_dialog"),
      color = HighDensityBackground,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(personColor),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = person.name.take(1).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
            }
            Column {
              Text(
                text = "Record Cash Payment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text = "Person: ${person.name}",
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp).testTag("close_record_payment_dialog"),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighDensityTextSecondary)
          }
        }

        HorizontalDivider(color = HighDensityBorder)

        // Direction Selector (Received vs Paid)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Payment Direction", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HighDensityTextSecondary)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              modifier =
                Modifier
                  .weight(1f)
                  .height(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .border(
                    1.dp,
                    if (isReceived) HighDensityOwedToYou else HighDensityBorder,
                    RoundedCornerShape(10.dp),
                  )
                  .clickable {
                    if (!isReceived) {
                      isReceived = true
                      amountText = ""
                      errorMessage = null
                    }
                  },
              color = if (isReceived) HighDensityOwedToYou.copy(alpha = 0.15f) else HighDensitySurface,
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  Icons.Default.ArrowDownward,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (isReceived) HighDensityOwedToYou else HighDensityTextSecondary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Received Cash",
                  fontSize = 11.sp,
                  fontWeight = if (isReceived) FontWeight.Bold else FontWeight.Normal,
                  color = if (isReceived) HighDensityOwedToYou else HighDensityTextSecondary,
                )
              }
            }

            Surface(
              modifier =
                Modifier
                  .weight(1f)
                  .height(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .border(
                    1.dp,
                    if (!isReceived) HighDensityYouOwe else HighDensityBorder,
                    RoundedCornerShape(10.dp),
                  )
                  .clickable {
                    if (isReceived) {
                      isReceived = false
                      amountText = ""
                      errorMessage = null
                    }
                  },
              color = if (!isReceived) HighDensityYouOwe.copy(alpha = 0.15f) else HighDensitySurface,
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  Icons.Default.ArrowUpward,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (!isReceived) HighDensityYouOwe else HighDensityTextSecondary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Paid Cash",
                  fontSize = 11.sp,
                  fontWeight = if (!isReceived) FontWeight.Bold else FontWeight.Normal,
                  color = if (!isReceived) HighDensityYouOwe else HighDensityTextSecondary,
                )
              }
            }
          }
        }

        // Amount Input Card & Percentage Autofill Buttons
        Card(
          colors = CardDefaults.cardColors(containerColor = HighDensityCard),
          shape = RoundedCornerShape(12.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder)),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Cash Amount ($)",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = HighDensityTextSecondary,
            )
            OutlinedTextField(
              value = amountText,
              onValueChange = {
                amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                errorMessage = null
              },
              placeholder = null,
              prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = HighDensityPrimary) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.fillMaxWidth().testTag("cash_amount_input"),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = HighDensityPrimary,
                  unfocusedBorderColor = HighDensityBorder,
                  focusedTextColor = HighDensityTextPrimary,
                  unfocusedTextColor = HighDensityTextPrimary,
                ),
            )

            // Autofill buttons: 100%, 75%, 50%, 25% of total debt
            val totalForAutofill = if (relevantTotal > 0.0) relevantTotal else amountText.toDoubleOrNull() ?: 0.0
            if (totalForAutofill > 0.0) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Autofill % of Total Debt ($${String.format(Locale.US, "%.2f", totalForAutofill)}):",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextSecondary,
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  val percentages = listOf(1.00 to "100%", 0.75 to "75%", 0.50 to "50%", 0.25 to "25%")
                  percentages.forEach { (factor, label) ->
                    val calcVal = totalForAutofill * factor
                    val formattedVal = String.format(Locale.US, "%.2f", calcVal)
                    val isSelected = amountText == formattedVal
                    Surface(
                      modifier =
                        Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(8.dp))
                          .background(if (isSelected) HighDensityPrimaryContainer else HighDensitySurface)
                          .border(
                            1.dp,
                            if (isSelected) HighDensityPrimary else HighDensityBorder,
                            RoundedCornerShape(8.dp),
                          )
                          .clickable {
                            amountText = formattedVal
                            errorMessage = null
                          }
                          .testTag("autofill_${label.replace("%", "pct")}_btn"),
                      color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurface,
                    ) {
                      Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                      ) {
                        Text(
                          text = label,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityPrimary,
                        )
                        Text(
                          text = "$$formattedVal",
                          fontSize = 9.sp,
                          color = if (isSelected) HighDensityOnPrimaryContainer.copy(alpha = 0.85f) else HighDensityTextSecondary,
                        )
                      }
                    }
                  }
                }
              }
            }

            // Live deduction preview
            if (relevantTotal > 0.0) {
              val currentAmtVal = amountText.toDoubleOrNull() ?: 0.0
              val newRemaining = (relevantTotal - currentAmtVal).coerceAtLeast(0.0)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = HighDensitySurface,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = if (currentAmtVal > 0.0) "Balance after deduction:" else "Current balance owed:",
                    fontSize = 10.sp,
                    color = HighDensityTextSecondary,
                  )
                  Text(
                    text = "$${String.format(Locale.US, "%.2f", if (currentAmtVal > 0.0) newRemaining else relevantTotal)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (newRemaining == 0.0 && currentAmtVal > 0.0) HighDensityOwedToYou else HighDensityTextPrimary,
                  )
                }
              }
            }
          }
        }

        // Payment Date Input
        Card(
          colors = CardDefaults.cardColors(containerColor = HighDensityCard),
          shape = RoundedCornerShape(12.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder)),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Payment Date Recorded",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = HighDensityTextSecondary,
            )
            OutlinedTextField(
              value = paymentDate,
              onValueChange = { paymentDate = it },
              placeholder = { Text("e.g. Sep 04, 2026", color = HighDensityTextSecondary) },
              leadingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityPrimary)
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("payment_date_input"),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = HighDensityPrimary,
                  unfocusedBorderColor = HighDensityBorder,
                  focusedTextColor = HighDensityTextPrimary,
                  unfocusedTextColor = HighDensityTextPrimary,
                ),
            )
          }
        }

        // Note / Memo Input
        Card(
          colors = CardDefaults.cardColors(containerColor = HighDensityCard),
          shape = RoundedCornerShape(12.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder)),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Note / Description",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = HighDensityTextSecondary,
            )
            OutlinedTextField(
              value = noteText,
              onValueChange = { noteText = it },
              placeholder = { Text("e.g. Cash in person, Lunch repayment", color = HighDensityTextSecondary) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("cash_note_input"),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = HighDensityPrimary,
                  unfocusedBorderColor = HighDensityBorder,
                  focusedTextColor = HighDensityTextPrimary,
                  unfocusedTextColor = HighDensityTextPrimary,
                ),
            )
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            fontSize = 11.sp,
            color = HighDensityYouOwe,
          )
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Cancel", fontSize = 12.sp, color = HighDensityTextSecondary)
          }

          Button(
            onClick = {
              val parsedAmount = amountText.toDoubleOrNull()
              if (parsedAmount == null || parsedAmount <= 0.0) {
                errorMessage = "Please enter a valid amount greater than $0.00"
              } else {
                onConfirm(parsedAmount, isReceived, noteText.ifBlank { "Cash payment" }, paymentDate, null)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityOwedToYou),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1.3f).height(44.dp).testTag("confirm_record_payment_btn"),
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Record Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          }
        }
      }
    }
  }
}
@Composable
fun EditDebtDialog(
  debt: ReminderItem,
  people: List<PersonRecord> = emptyList(),
  onDismiss: () -> Unit,
  onConfirm: (ReminderItem) -> Unit,
) {
  val initialBackpayAmount = remember {
    val text = debt.secondaryText
    val match = Regex("""Backpay:\s*\+\$?([0-9.]+)""").find(text) ?: Regex("""\+\$?([0-9.]+)\s*backpay""").find(text)
    match?.groupValues?.get(1) ?: ""
  }
  val initialEnableBackpay = remember { initialBackpayAmount.isNotEmpty() }
  val initialBackpayVal = initialBackpayAmount.toDoubleOrNull() ?: 0.0
  val initialAmt = if (initialBackpayVal > 0.0) (debt.amount - initialBackpayVal).coerceAtLeast(0.0) else debt.amount

  var title by remember { mutableStateOf(debt.title) }
  var amountText by remember { mutableStateOf(if (initialAmt > 0.0) String.format(Locale.US, "%.2f", initialAmt) else "") }
  var selectedCategory by remember {
    mutableStateOf(
      if (debt.iconType == "subscription" || debt.secondaryText.contains("Subscription", ignoreCase = true)) "Subscription"
      else if (debt.title.contains("Loan", ignoreCase = true) || debt.secondaryText.contains("Loan", ignoreCase = true)) "Fixed Loan"
      else "One-Off"
    )
  }
  var debtDirection by remember { mutableStateOf(if (debt.isPositive) "Owed to Me" else "I Owe") }
  var payerName by remember { mutableStateOf(debt.personName.ifBlank { debt.getAssociatedPerson() }) }
  val todayFormatted = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date()) }
  var relativeDueChoice by remember { mutableStateOf(debt.relativeDateText.ifBlank { todayFormatted }) }
  var customSelectedDate by remember {
    mutableStateOf<String?>(
      if (debt.relativeDateText != "Due Today" && debt.relativeDateText != todayFormatted && debt.relativeDateText.isNotBlank()) debt.relativeDateText else null
    )
  }
  var showDatePicker by remember { mutableStateOf(false) }

  // Sub-options for categories
  val subscriptionFrequencies = listOf("Weekly", "Bi-weekly", "Monthly", "Yearly", "Custom")
  val initialFreq = remember {
    val text = debt.secondaryText
    when {
      text.contains("/Weekly", ignoreCase = true) || text.contains("(Weekly)", ignoreCase = true) -> "Weekly"
      text.contains("/Bi-weekly", ignoreCase = true) || text.contains("(Bi-weekly)", ignoreCase = true) -> "Bi-weekly"
      text.contains("/Monthly", ignoreCase = true) || text.contains("(Monthly)", ignoreCase = true) -> "Monthly"
      text.contains("/Yearly", ignoreCase = true) || text.contains("(Yearly)", ignoreCase = true) -> "Yearly"
      else -> {
        val r1 = Regex("""Subscription \(([^)]+)\)""").find(text)
        val r2 = Regex("""/([^•\s]+)""").find(text)
        val match = r1?.groupValues?.get(1) ?: r2?.groupValues?.get(1)
        if (match != null && match !in listOf("Weekly", "Bi-weekly", "Monthly", "Yearly")) {
          "Custom"
        } else {
          "Monthly"
        }
      }
    }
  }
  val initialCustomFreqText = remember {
    val text = debt.secondaryText
    val r1 = Regex("""Subscription \(([^)]+)\)""").find(text)
    val r2 = Regex("""/([^•\s]+)""").find(text)
    val match = r1?.groupValues?.get(1) ?: r2?.groupValues?.get(1)
    if (match != null && match !in listOf("Weekly", "Bi-weekly", "Monthly", "Yearly")) {
      match
    } else {
      ""
    }
  }

  var subscriptionFrequency by remember { mutableStateOf(initialFreq) }
  var customFrequencyText by remember { mutableStateOf(initialCustomFreqText) }
  var enableBackpay by remember { mutableStateOf(initialEnableBackpay) }
  var backpayAmountText by remember { mutableStateOf(initialBackpayAmount) }
  var isSubscriptionCancelled by remember {
    mutableStateOf(
      debt.secondaryText.contains("Cancelled", ignoreCase = true) ||
      debt.secondaryText.contains("Canceled", ignoreCase = true)
    )
  }

  val isFutureMonth = remember(debt.createdDate, debt.relativeDateText) {
    val startCal = parseItemCalendar(debt.createdDate) ?: parseItemCalendar(debt.relativeDateText)
    if (startCal != null) {
      val now = Calendar.getInstance()
      val currentYear = now.get(Calendar.YEAR)
      val currentMonth = now.get(Calendar.MONTH)
      val startYear = startCal.get(Calendar.YEAR)
      val startMonth = startCal.get(Calendar.MONTH)
      (currentYear > startYear) || (currentYear == startYear && currentMonth > startMonth)
    } else {
      false
    }
  }

  var loanInterestRate by remember { mutableStateOf("0% (Interest-Free)") }
  val commonLoanRates = listOf("0% (Interest-Free)", "3.5%", "5.0%", "7.5%", "10.0%", "12.0%", "15.0%")

  val categories =
    listOf(
      Triple("One-Off", "Single bill, meal or expense", Icons.Default.AccountBalanceWallet),
      Triple("Subscription", "Recurring subscription", Icons.Default.Subscriptions),
      Triple("Fixed Loan", "Agreed installment or loan", Icons.Default.AccountTree),
    )
  val quickAmounts = listOf(15, 25, 50, 100)

  val currentAmt = amountText.toDoubleOrNull() ?: 0.0
  val scrollState = rememberScrollState()

  if (showDatePicker) {
    MonthDatePickerDialog(
      onDismiss = { showDatePicker = false },
      onDateSelected = { dateStr ->
        customSelectedDate = dateStr
        relativeDueChoice = dateStr
      },
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.95f)
          .widthIn(max = 520.dp)
          .fillMaxHeight(0.88f)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("edit_debt_dialog"),
      color = HighDensityBackground,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Drag Pill & Top Bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(HighDensityBorder),
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = if (selectedCategory == "Subscription") "Edit Subscription Debt" else "Edit Debt Entry",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
              Text(
                text = if (selectedCategory == "Subscription") "Manage recurring rate, schedule & subscription status" else "Update balance, direction & scheduled due date",
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
              )
            }

            IconButton(
              onClick = onDismiss,
              modifier =
                Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(HighDensitySurface),
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = HighDensityTextPrimary,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        // Scrollable middle section for input fields
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        // 1. Directional Selector Cards (You Owe vs Owed to You)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // You Owe Option
          val isOwe = debtDirection == "I Owe"
          Surface(
            modifier =
              Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isOwe) 1.5.dp else 1.dp,
                  color = if (isOwe) HighDensityYouOwe else HighDensityBorder,
                  shape = RoundedCornerShape(12.dp),
                )
                .clickable { debtDirection = "I Owe" }
                .testTag("direction_i_owe"),
            color = if (isOwe) HighDensityRedBadgeBg else HighDensityCard,
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isOwe) HighDensityYouOwe else HighDensitySurfaceVariant),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = if (isOwe) Color.White else HighDensityTextSecondary,
                  modifier = Modifier.size(13.dp),
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "You Owe",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isOwe) HighDensityRedBadgeText else HighDensityTextPrimary,
              )
              if (isOwe) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = HighDensityYouOwe,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }

          // Owed to You Option
          val isOwedToMe = debtDirection == "Owed to Me"
          Surface(
            modifier =
              Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isOwedToMe) 1.5.dp else 1.dp,
                  color = if (isOwedToMe) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(12.dp),
                )
                .clickable { debtDirection = "Owed to Me" }
                .testTag("direction_owed_to_me"),
            color = if (isOwedToMe) HighDensityPrimaryContainer else HighDensityCard,
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isOwedToMe) HighDensityPrimary else HighDensitySurfaceVariant),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = if (isOwedToMe) Color.White else HighDensityTextSecondary,
                  modifier = Modifier.size(13.dp),
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Owed to You",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isOwedToMe) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
              )
              if (isOwedToMe) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = HighDensityPrimary,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
        }

        // 2. Hero Amount Card with Quick Increments
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
          border = CardDefaults.outlinedCardBorder(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "ENTER AMOUNT",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextSecondary,
              letterSpacing = 1.sp,
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(vertical = 6.dp),
            ) {
              Text(
                text = "$",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (debtDirection == "I Owe") HighDensityYouOwe else HighDensityOwedToYou,
              )
              Spacer(modifier = Modifier.width(6.dp))
              OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                placeholder = { Text("0.00", fontSize = 28.sp, color = HighDensityTextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(180.dp).testTag("edit_debt_amount_input"),
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                  ),
                textStyle =
                  androidx.compose.ui.text.TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  ),
              )
            }

            // Quick increment chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
              quickAmounts.forEach { amt ->
                Box(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(HighDensityCard)
                      .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                      .clickable {
                        val current = amountText.toDoubleOrNull() ?: 0.0
                        amountText = String.format(Locale.US, "%.2f", current + amt)
                      }
                      .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                  Text(
                    text = "+$$amt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensityPrimary,
                  )
                }
              }
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HighDensityCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                    .clickable { amountText = "" }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
              ) {
                Text(
                  text = "Clear",
                  fontSize = 11.sp,
                  color = HighDensityTextSecondary,
                )
              }
            }
          }
        }

        // 3. Title Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Debt Title / Label") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth().testTag("edit_debt_title_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
          )
        }

        // 4. Category Tiles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Category",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            categories.forEach { (catName, _, icon) ->
              val isSelected = selectedCategory == catName
              Surface(
                modifier =
                  Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) HighDensityPrimary else HighDensityBorder,
                      shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { selectedCategory = catName },
                color = if (isSelected) HighDensityPrimaryContainer else HighDensityCard,
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = catName,
                    tint = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextSecondary,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = catName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                  )
                }
              }
            }
          }

          // Category Sub-Options: Subscription Frequencies & Backpay (Slimmed down)
          if (selectedCategory == "Subscription") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
              border = CardDefaults.outlinedCardBorder(),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "Recurring Billing Frequency",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextPrimary,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(subscriptionFrequencies) { freq ->
                    val isFreqSelected = subscriptionFrequency == freq
                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .border(
                            width = if (isFreqSelected) 1.5.dp else 1.dp,
                            color = if (isFreqSelected) HighDensityPrimary else HighDensityBorder,
                            shape = RoundedCornerShape(8.dp),
                          )
                          .clickable { subscriptionFrequency = freq },
                      color = if (isFreqSelected) HighDensityPrimaryContainer else HighDensityCard,
                    ) {
                      Text(
                        text = freq,
                        fontSize = 11.sp,
                        fontWeight = if (isFreqSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFreqSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      )
                    }
                  }
                }

                val activeFrequency = if (subscriptionFrequency == "Custom") customFrequencyText.ifBlank { "Custom" } else subscriptionFrequency

                if (subscriptionFrequency == "Custom") {
                  OutlinedTextField(
                    value = customFrequencyText,
                    onValueChange = { customFrequencyText = it },
                    label = { Text("Custom Frequency", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Every 10 days, Quarterly, etc.", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_subscription_custom_frequency_input"),
                    shape = RoundedCornerShape(10.dp),
                  )
                }

                // Hide setup fee/starting fee option entirely after the first month!
                if (!isFutureMonth) {
                  HorizontalDivider(color = HighDensityBorder, thickness = 0.5.dp)

                  // Backpay / Starting Fee Option
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "Include Backpay / Starting Fee",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextPrimary,
                      )
                      Text(
                        text = "Adds a one-time starting fee.",
                        fontSize = 10.sp,
                        color = HighDensityTextSecondary,
                      )
                    }
                    Switch(
                      checked = enableBackpay,
                      onCheckedChange = { enableBackpay = it },
                      colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HighDensityPrimary,
                      ),
                      modifier = Modifier.testTag("edit_backpay_switch"),
                    )
                  }

                  if (enableBackpay) {
                    OutlinedTextField(
                      value = backpayAmountText,
                      onValueChange = { backpayAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                      label = { Text("Starting Backpay / Setup Fee ($)", fontSize = 11.sp) },
                      placeholder = { Text("0.00", fontSize = 12.sp) },
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      modifier = Modifier.fillMaxWidth().testTag("edit_subscription_backpay_input"),
                      shape = RoundedCornerShape(10.dp),
                    )

                    val backpayVal = backpayAmountText.toDoubleOrNull() ?: 0.0
                    val firstChargeTotal = currentAmt + backpayVal

                    Surface(
                      modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                      color = HighDensityPrimaryContainer.copy(alpha = 0.4f),
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                          text = "1st Payment Total: $${String.format(Locale.US, "%.2f", firstChargeTotal)} ($${String.format(Locale.US, "%.2f", currentAmt)} $activeFrequency + $${String.format(Locale.US, "%.2f", backpayVal)} backpay)",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = HighDensityOnPrimaryContainer,
                        )
                        Text(
                          text = "Next Payments: $${String.format(Locale.US, "%.2f", currentAmt)} / $activeFrequency",
                          fontSize = 10.sp,
                          color = HighDensityTextSecondary,
                        )
                      }
                    }
                  }
                }

                HorizontalDivider(color = HighDensityBorder, thickness = 0.5.dp)

                // Cancel Subscription Section
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                      text = if (isSubscriptionCancelled) "Subscription Cancelled" else "Cancel Subscription",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSubscriptionCancelled) HighDensityYouOwe else HighDensityTextPrimary,
                    )
                    Text(
                      text = if (isSubscriptionCancelled)
                        "Cancelled. Balance ($${String.format(Locale.US, "%.2f", currentAmt)}) is preserved."
                      else
                        "Stops future recurring occurrences.",
                      fontSize = 10.sp,
                      color = HighDensityTextSecondary,
                    )
                  }
                  Button(
                    onClick = { isSubscriptionCancelled = !isSubscriptionCancelled },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isSubscriptionCancelled) HighDensityPrimaryContainer else HighDensityYouOwe,
                      contentColor = if (isSubscriptionCancelled) HighDensityOnPrimaryContainer else Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("toggle_cancel_subscription_btn"),
                  ) {
                    Text(
                      text = if (isSubscriptionCancelled) "Reactivate" else "Cancel Sub",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
              }
            }
          }

          // Category Sub-Options: Fixed Loan Interest Rates
          if (selectedCategory == "Fixed Loan") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
              border = CardDefaults.outlinedCardBorder(),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "Interest Rate (APR)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensityTextPrimary,
                  )
                  if (loanInterestRate != "0% (Interest-Free)" && currentAmt > 0.0) {
                    val rateClean = loanInterestRate.replace("%", "").toDoubleOrNull() ?: 0.0
                    val estInterest = currentAmt * (rateClean / 100.0)
                    Text(
                      text = "+$${String.format(Locale.US, "%.2f", estInterest)} est. interest",
                      fontSize = 10.sp,
                      color = HighDensityPrimary,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(commonLoanRates) { rate ->
                    val isRateSelected = loanInterestRate == rate
                    Surface(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .border(
                            width = if (isRateSelected) 1.5.dp else 1.dp,
                            color = if (isRateSelected) HighDensityPrimary else HighDensityBorder,
                            shape = RoundedCornerShape(8.dp),
                          )
                          .clickable { loanInterestRate = rate },
                      color = if (isRateSelected) HighDensityPrimaryContainer else HighDensityCard,
                    ) {
                      Text(
                        text = rate,
                        fontSize = 11.sp,
                        fontWeight = if (isRateSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRateSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // 5. People Directory (Required)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "People Directory (Required)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
            )
            if (payerName.isNotBlank()) {
              Text(
                text = "Clear selection",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityPrimary,
                modifier = Modifier.clickable { payerName = "" },
              )
            }
          }

          val availablePeople = people

          if (availablePeople.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(availablePeople) { person ->
                val pName = person.name
                val pColor = Color(person.colorHex)
                val isSelected = payerName.equals(pName, ignoreCase = true)
                Surface(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(20.dp))
                      .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) pColor else HighDensityBorder,
                        shape = RoundedCornerShape(20.dp),
                      )
                      .clickable {
                        payerName = if (isSelected) "" else pName
                      },
                  color = if (isSelected) HighDensityPrimaryContainer else HighDensityCard,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Box(
                      modifier = Modifier.size(18.dp).clip(CircleShape).background(pColor),
                      contentAlignment = Alignment.Center,
                    ) {
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(12.dp),
                        )
                      } else {
                        Text(
                          text = pName.take(1).uppercase(),
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White,
                        )
                      }
                    }
                    Text(
                      text = pName,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                    )
                  }
                }
              }
            }
          }
        }

        // 6. Schedule (Due Today or Pick Date)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Due Date",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // "Due Today" button
            val isDueToday = relativeDueChoice == todayFormatted && customSelectedDate == null
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isDueToday) 1.5.dp else 1.dp,
                  color = if (isDueToday) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(12.dp),
                )
                .clickable {
                  relativeDueChoice = todayFormatted
                  customSelectedDate = null
                }
                .testTag("due_today_button"),
              color = if (isDueToday) HighDensityPrimaryContainer else HighDensityCard,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = if (isDueToday) HighDensityOnPrimaryContainer else HighDensityTextSecondary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Due Today",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDueToday) HighDensityOnPrimaryContainer else HighDensityTextPrimary,
                )
              }
            }

            // "Pick Date" button
            val isCustomPicked = customSelectedDate != null
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isCustomPicked) 1.5.dp else 1.dp,
                  color = if (isCustomPicked) HighDensityPrimary else HighDensityBorder,
                  shape = RoundedCornerShape(12.dp),
                )
                .clickable { showDatePicker = true }
                .testTag("open_due_calendar_button"),
              color = if (isCustomPicked) HighDensityPrimaryContainer else HighDensityCard,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = if (isCustomPicked) HighDensityOnPrimaryContainer else HighDensityPrimary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isCustomPicked) customSelectedDate!! else "Pick Date",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCustomPicked) HighDensityOnPrimaryContainer else HighDensityPrimary,
                )
              }
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
            color = HighDensitySurface,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Scheduled Due Date:",
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
              )
              Text(
                text = relativeDueChoice,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityPrimary,
              )
            }
          }
        }

        } // Close scrollable middle section

        HorizontalDivider(
          color = HighDensityBorder,
          thickness = 0.5.dp,
          modifier = Modifier.padding(vertical = 4.dp),
        )

        // Action Buttons (Pinned to Bottom, Always Visible)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Cancel", fontSize = 13.sp, color = HighDensityTextSecondary)
          }

          Button(
            onClick = {
              val amt = amountText.toDoubleOrNull() ?: debt.amount
              if (amt > 0.0) {
                val trimmedPayer = payerName.trim()
                val isPositiveBool = (debtDirection == "Owed to Me")
                val backpayVal = if (!isFutureMonth && enableBackpay && selectedCategory == "Subscription") (backpayAmountText.toDoubleOrNull() ?: 0.0) else 0.0
                val totalAmt = amt + backpayVal
                val activeFreq = if (subscriptionFrequency == "Custom") customFrequencyText.ifBlank { "Custom" } else subscriptionFrequency
                val baseSecondaryStr = if (selectedCategory == "Subscription" && backpayVal > 0.0) {
                  "Payer: $trimmedPayer • Backpay: +$${String.format(Locale.US, "%.2f", backpayVal)} (Recurring: $${String.format(Locale.US, "%.2f", amt)}/$activeFreq)"
                } else if (selectedCategory == "Subscription") {
                  "Payer: $trimmedPayer • Recurring: $${String.format(Locale.US, "%.2f", amt)}/$activeFreq"
                } else if (trimmedPayer.isNotBlank()) {
                  "Payer: $trimmedPayer"
                } else {
                  debt.secondaryText
                }

                val secondaryTextStr = if (selectedCategory == "Subscription" && isSubscriptionCancelled) {
                  "$baseSecondaryStr • [Subscription Cancelled]"
                } else {
                  baseSecondaryStr.replace(" • [Subscription Cancelled]", "").replace(" [Subscription Cancelled]", "")
                }

                onConfirm(
                  debt.copy(
                    title = title.ifBlank { "Debt Entry" }.trim(),
                    amount = totalAmt,
                    isPositive = isPositiveBool,
                    relativeDateText = relativeDueChoice,
                    personName = trimmedPayer,
                    secondaryText = secondaryTextStr,
                    iconType = if (selectedCategory == "Subscription") "subscription" else "person",
                  ),
                )
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.4f).height(44.dp).testTag("save_edited_debt_button"),
            enabled = title.isNotBlank() && currentAmt > 0.0 && payerName.isNotBlank(),
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}
