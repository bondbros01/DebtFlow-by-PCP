package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AVAILABLE_PERSON_COLORS
import com.example.model.PersonRecord
import com.example.model.ProjectRecord
import com.example.model.ReminderItem
import com.example.util.ExportManager
import java.util.Locale
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
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.HighDensityYouOwe

// -------------------------------------------------------------
// Main People Screen
// -------------------------------------------------------------
@Composable
fun PeopleScreenContent(
  people: List<PersonRecord>,
  reminders: List<ReminderItem>,
  projects: List<ProjectRecord> = emptyList(),
  showProjectCosts: Boolean = true,
  onAddPerson: (name: String, colorHex: Long, notes: String) -> Unit,
  onUpdatePersonColor: (personId: String, newColorHex: Long) -> Unit,
  onDeletePerson: (personId: String) -> Unit,
  onToggleDebtPaid: (ReminderItem) -> Unit,
  onEditDebt: (ReminderItem) -> Unit,
  onDeleteDebt: (debtId: String) -> Unit,
  onAddNewDebtForPerson: (personName: String) -> Unit,
  onExportLog: ((String) -> Unit)? = null,
  onRecordPayment: ((personName: String, amount: Double, isReceived: Boolean, note: String, paymentDate: String, linkedDebtId: String?) -> Unit)? = null,
  onUpdatePersonName: ((personId: String, newName: String) -> Unit)? = null,
  onReconcile: ((List<ReminderItem>, String) -> Unit)? = null,
  userName: String = "You",
) {
  var showAddPersonDialog by remember { mutableStateOf(false) }
  var selectedPersonForDetails by remember { mutableStateOf<PersonRecord?>(null) }
  var personForColorPicker by remember { mutableStateOf<PersonRecord?>(null) }

  BackHandler(enabled = selectedPersonForDetails != null || showAddPersonDialog || personForColorPicker != null) {
    if (selectedPersonForDetails != null) {
      selectedPersonForDetails = null
    } else if (showAddPersonDialog) {
      showAddPersonDialog = false
    } else if (personForColorPicker != null) {
      personForColorPicker = null
    }
  }

  // Compute aggregate stats across people
  val totalOwedToUserByPeople =
    reminders.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
  val totalUserOwesToPeople =
    reminders.filter { !it.isPaid && !it.isPositive }.sumOf { it.amount }

  val combinedPeople = remember(people.size, people.map { Triple(it.id, it.name, it.colorHex) }, reminders.size, reminders.map { it.id }) {
    val list = people.toMutableList()
    val existingNames = list.map { it.name.trim().lowercase() }.toMutableSet()
    reminders.forEach { debt ->
      val pName = debt.getAssociatedPerson().ifBlank { "Unassigned" }
      if (!existingNames.contains(pName.trim().lowercase())) {
        existingNames.add(pName.trim().lowercase())
        list.add(
          PersonRecord(
            id = "synth_${pName.trim().lowercase()}",
            name = pName,
            colorHex = 0xFF625B71,
            notes = "Auto-generated for unassigned debts"
          )
        )
      }
    }
    list
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp)
        .testTag("people_screen_content"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "People & Contacts",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = HighDensityTextPrimary,
      )

      Button(
        onClick = { showAddPersonDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = Modifier.testTag("add_new_person_button"),
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Person", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    // Top Summary Metric Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        modifier =
          Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
        color = HighDensitySurface,
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("Owed to You", fontSize = 10.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "+$${String.format("%,.2f", totalOwedToUserByPeople)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityOwedToYou,
          )
        }
      }

      Surface(
        modifier =
          Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
        color = HighDensitySurface,
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("You Owe", fontSize = 10.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "-$${String.format("%,.2f", totalUserOwesToPeople)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityYouOwe,
          )
        }
      }

      Surface(
        modifier =
          Modifier
            .weight(0.9f)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
        color = HighDensitySurface,
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("Profiles", fontSize = 10.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${people.size} Active",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityPrimary,
          )
        }
      }
    }

    // People List or Empty State
    if (combinedPeople.isEmpty()) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(HighDensityCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
            .padding(24.dp)
            .testTag("people_empty_state"),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(HighDensityPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = HighDensityPrimary,
              modifier = Modifier.size(32.dp),
            )
          }
          Text(
            text = "No contacts added yet",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "Add friends, roommates, or clients to track shared expenses, balances, and payment settlements effortlessly.",
            fontSize = 13.sp,
            color = HighDensityTextSecondary,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Button(
            onClick = { showAddPersonDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_add_person_btn"),
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Your First Person", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(combinedPeople, key = { it.id }) { person ->
        val personDebts =
          reminders.filter {
            it.getAssociatedPerson().equals(person.name, ignoreCase = true) ||
              it.secondaryText.contains(person.name, ignoreCase = true)
          }

        val owedByPerson = personDebts.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
        val owedToPerson = personDebts.filter { !it.isPaid && !it.isPositive }.sumOf { it.amount }
        val netBalanceWithPerson = owedByPerson - owedToPerson
        val personColor = Color(person.colorHex)

        val personProjectExpenses =
          if (showProjectCosts) {
            projects.flatMap { proj ->
              proj.lineItems.filter { it.participantName.equals(person.name, ignoreCase = true) }.map { proj.name to it }
            }
          } else {
            emptyList()
          }
        val personProjectTotal = personProjectExpenses.sumOf { it.second.amount }

        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
              .clickable { selectedPersonForDetails = person }
              .testTag("person_card_${person.id}"),
          colors = CardDefaults.cardColors(containerColor = HighDensityCard),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            // Avatar + Name + Color Indicator
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f),
            ) {
              Box(
                modifier =
                  Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(personColor),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = person.name.take(1).uppercase(),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                )
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = person.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  )
                  // Clickable Tag Color Badge
                  Surface(
                    modifier =
                      Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(personColor.copy(alpha = 0.2f))
                        .border(1.dp, personColor, RoundedCornerShape(8.dp))
                        .clickable { personForColorPicker = person }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("change_color_chip_${person.id}"),
                    color = Color.Transparent,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Box(
                        modifier =
                          Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(personColor),
                      )
                      Text(
                        text = "Tag Color",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextPrimary,
                      )
                    }
                  }
                }

                Text(
                  text =
                    if (showProjectCosts && personProjectTotal > 0.0) {
                      "${personDebts.size} debts • $${String.format("%.2f", personProjectTotal)} in projects"
                    } else {
                      "${personDebts.size} debts • Calendar Tag"
                    },
                  fontSize = 11.sp,
                  color = HighDensityTextSecondary,
                )
              }
            }

            // Financial Balance Tag & Action
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text =
                    when {
                      netBalanceWithPerson > 0 -> "+$${String.format("%.2f", netBalanceWithPerson)}"
                      netBalanceWithPerson < 0 -> "-$${String.format("%.2f", -netBalanceWithPerson)}"
                      else -> "Settled"
                    },
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color =
                    when {
                      netBalanceWithPerson > 0 -> HighDensityOwedToYou
                      netBalanceWithPerson < 0 -> HighDensityYouOwe
                      else -> HighDensityTextSecondary
                    },
                )
                Text(
                  text =
                    when {
                      netBalanceWithPerson > 0 -> "Owes you"
                      netBalanceWithPerson < 0 -> "You owe"
                      else -> "$0.00 balance"
                    },
                  fontSize = 10.sp,
                  color = HighDensityTextSecondary,
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog: Add New Person
  if (showAddPersonDialog) {
    AddPersonDialog(
      onDismiss = { showAddPersonDialog = false },
      onConfirm = { name, colorHex, notes ->
        onAddPerson(name, colorHex, notes)
        showAddPersonDialog = false
      },
    )
  }

  // Dialog: Change Person Color
  personForColorPicker?.let { p ->
    ColorPickerDialog(
      personName = p.name,
      currentColorHex = p.colorHex,
      onDismiss = { personForColorPicker = null },
      onSelectColor = { newColor ->
        onUpdatePersonColor(p.id, newColor)
        // Update local reference if sheet is open
        if (selectedPersonForDetails?.id == p.id) {
          selectedPersonForDetails = selectedPersonForDetails?.copy(colorHex = newColor)
        }
        personForColorPicker = null
      },
    )
  }

  // Detailed Person Management Sheet / Modal
  selectedPersonForDetails?.let { currentPerson ->
    val livePerson = combinedPeople.find { it.id == currentPerson.id } ?: currentPerson
    PersonDetailDialog(
      person = livePerson,
      reminders = reminders,
      projects = projects,
      showProjectCosts = showProjectCosts,
      userName = userName,
      onDismiss = { selectedPersonForDetails = null },
      onChangeColorClick = { personForColorPicker = livePerson },
      onTogglePaid = onToggleDebtPaid,
      onEditDebt = onEditDebt,
      onDeleteDebt = onDeleteDebt,
      onAddDebtForPerson = {
        onAddNewDebtForPerson(livePerson.name)
        selectedPersonForDetails = null
      },
      onDeletePerson = {
        onDeletePerson(livePerson.id)
        selectedPersonForDetails = null
      },
      onExportLog = onExportLog,
      onRecordPayment = { amount, isReceived, note, paymentDate, linkedDebtId ->
        onRecordPayment?.invoke(livePerson.name, amount, isReceived, note, paymentDate, linkedDebtId)
      },
      onUpdatePersonName = onUpdatePersonName,
      onReconcile = onReconcile,
    )
  }
 }
}

// -------------------------------------------------------------
// Person Detail Dialog: View & Modify Associated Debts
// -------------------------------------------------------------
@Composable
fun PersonDetailDialog(
  person: PersonRecord,
  reminders: List<ReminderItem>,
  projects: List<ProjectRecord> = emptyList(),
  showProjectCosts: Boolean = true,
  onDismiss: () -> Unit,
  onChangeColorClick: () -> Unit,
  onTogglePaid: (ReminderItem) -> Unit,
  onEditDebt: (ReminderItem) -> Unit,
  onDeleteDebt: (debtId: String) -> Unit,
  onAddDebtForPerson: () -> Unit,
  onDeletePerson: () -> Unit,
  onExportLog: ((String) -> Unit)? = null,
  onRecordPayment: ((amount: Double, isReceived: Boolean, note: String, paymentDate: String, linkedDebtId: String?) -> Unit)? = null,
  onUpdatePersonName: ((personId: String, newName: String) -> Unit)? = null,
  onReconcile: ((List<ReminderItem>, String) -> Unit)? = null,
  userName: String = "You",
) {
  val context = LocalContext.current

  val personDebts =
    reminders.filter {
      it.getAssociatedPerson().equals(person.name, ignoreCase = true) ||
        it.secondaryText.contains(person.name, ignoreCase = true) ||
        it.personName.equals(person.name, ignoreCase = true)
    }

  val personProjectExpenses =
    projects.flatMap { proj ->
      proj.lineItems.filter { it.participantName.equals(person.name, ignoreCase = true) }.map { proj.name to it }
    }
  val personProjectTotal = personProjectExpenses.sumOf { it.second.amount }

  val owedByPerson = personDebts.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
  val owedToPerson = personDebts.filter { !it.isPaid && !it.isPositive }.sumOf { it.amount }
  val netBalance = owedByPerson - owedToPerson
  val personColor = Color(person.colorHex)

  var debtToEdit by remember { mutableStateOf<ReminderItem?>(null) }
  var showRecordPaymentDialog by remember { mutableStateOf(false) }
  var showEditNameDialog by remember { mutableStateOf(false) }
  var isPaymentsRecordExpanded by remember { mutableStateOf(false) }
  var isCompletedDebtsExpanded by remember { mutableStateOf(false) }
  var showReconciliationDialog by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  if (showEditNameDialog) {
    var editNameText by remember { mutableStateOf(person.name) }
    AlertDialog(
      onDismissRequest = { showEditNameDialog = false },
      title = { Text("Edit User Name") },
      text = {
        OutlinedTextField(
          value = editNameText,
          onValueChange = { editNameText = it },
          label = { Text("Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_person_name_input"),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (editNameText.isNotBlank()) {
              onUpdatePersonName?.invoke(person.id, editNameText.trim())
              showEditNameDialog = false
            }
          },
          modifier = Modifier.testTag("confirm_edit_person_name"),
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditNameDialog = false }) {
          Text("Cancel")
        }
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
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
          .testTag("person_detail_dialog"),
      color = HighDensityBackground,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Drag Handle & Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(personColor),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = person.name.take(1).uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
            }

            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = person.name,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityTextPrimary,
                )
                IconButton(
                  onClick = { showEditNameDialog = true },
                  modifier = Modifier.size(24.dp).testTag("edit_person_name_button"),
                ) {
                  Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(14.dp),
                  )
                }
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onChangeColorClick() },
              ) {
                Box(
                  modifier = Modifier.size(8.dp).clip(CircleShape).background(personColor),
                )
                Text(
                  text = "Calendar Color • Change",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityPrimary,
                )
              }
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(HighDensitySurface),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighDensityTextPrimary, modifier = Modifier.size(18.dp))
          }
        }

        // Net Balance Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Net Position", fontSize = 12.sp, color = HighDensityTextSecondary)
              Text(
                text =
                  when {
                    netBalance > 0 -> "Owes you $${String.format("%.2f", netBalance)}"
                    netBalance < 0 -> "You owe $${String.format("%.2f", -netBalance)}"
                    else -> "Settled Up ($0.00)"
                  },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color =
                  when {
                    netBalance > 0 -> HighDensityOwedToYou
                    netBalance < 0 -> HighDensityYouOwe
                    else -> HighDensityTextPrimary
                  },
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = HighDensityBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("They owe you: $${String.format("%.2f", owedByPerson)}", fontSize = 11.sp, color = HighDensityTextSecondary)
              Text("You owe them: $${String.format("%.2f", owedToPerson)}", fontSize = 11.sp, color = HighDensityTextSecondary)
            }

            if (showProjectCosts && personProjectTotal > 0.0) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Project contributions: $${String.format("%.2f", personProjectTotal)} (${personProjectExpenses.size} items)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityPrimary,
              )
            }
          }
        }



        // Action Buttons: Record Payment & Add New Debt (without the "Associated Debts" header)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = { showRecordPaymentDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityOwedToYou.copy(alpha = 0.2f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOwedToYou),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).height(38.dp).testTag("record_payment_for_person_btn"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityOwedToYou)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Record Payment", fontSize = 12.sp, color = HighDensityOwedToYou, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onAddDebtForPerson,
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimaryContainer),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).height(38.dp).testTag("add_debt_for_person_btn"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityOnPrimaryContainer)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add New Debt", fontSize = 12.sp, color = HighDensityOnPrimaryContainer, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
          onClick = { showReconciliationDialog = true },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().height(38.dp).testTag("person_reconciliation_btn"),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityPrimary),
        ) {
          Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityPrimary)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Scan & Reconcile Payments", fontSize = 12.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
        }

        val cashCredits = personDebts.filter {
          !it.isPaid && (it.id.startsWith("credit_") || it.title.startsWith("Cash Credit", ignoreCase = true))
        }
        val recordedPayments = personDebts.filter { ExportManager.isPaymentItem(it) }
        val activeDebts = personDebts.filter {
          !it.isPaid && !it.id.startsWith("credit_") && !it.title.startsWith("Cash Credit", ignoreCase = true) && !ExportManager.isPaymentItem(it)
        }
        val completedDebts = personDebts.filter {
          it.isPaid && !ExportManager.isPaymentItem(it)
        }

        // 1. CASH CREDITS (Keep cash credits at the top)
        if (cashCredits.isNotEmpty()) {
          Text(
            text = "Cash Credits (${cashCredits.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityOwedToYou,
            modifier = Modifier.padding(top = 2.dp),
          )
          cashCredits.forEach { credit ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, HighDensityOwedToYou.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .testTag("cash_credit_${credit.id}"),
              colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(HighDensityOwedToYou.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.AttachMoney,
                      contentDescription = null,
                      tint = HighDensityOwedToYou,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Column {
                    Text(
                      text = credit.title,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = HighDensityTextPrimary,
                    )
                    Text(
                      text = credit.secondaryText.ifBlank { "Available credit • ${credit.relativeDateText}" },
                      fontSize = 10.sp,
                      color = HighDensityTextSecondary,
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = "$${String.format(Locale.US, "%.2f", credit.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = HighDensityOwedToYou,
                  )
                  IconButton(
                    onClick = { onDeleteDebt(credit.id) },
                    modifier = Modifier.size(28.dp).testTag("delete_credit_${credit.id}"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Credit",
                      tint = HighDensityYouOwe,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              }
            }
          }
        }

        // 2. PAYMENTS RECORD (Expandable cash payments record at the top)
        if (recordedPayments.isNotEmpty()) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(
                1.dp,
                if (isPaymentsRecordExpanded) HighDensityPrimary.copy(alpha = 0.5f) else HighDensityBorder,
                RoundedCornerShape(14.dp),
              )
              .clickable { isPaymentsRecordExpanded = !isPaymentsRecordExpanded }
              .testTag("person_payments_record_toggle"),
            color = HighDensitySurface,
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Payments Record",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HighDensityPrimary.copy(alpha = 0.15f),
                  ) {
                    Text(
                      text = "${recordedPayments.size} recorded",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = HighDensityPrimary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = if (isPaymentsRecordExpanded) "Click to collapse" else "Click to expand",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                  )
                  Icon(
                    imageVector = if (isPaymentsRecordExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isPaymentsRecordExpanded) "Collapse Payments" else "Expand Payments",
                    tint = HighDensityTextSecondary,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }

              if (isPaymentsRecordExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  recordedPayments.forEach { payment ->
                    Surface(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp)),
                      color = HighDensityCard,
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = payment.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HighDensityTextPrimary,
                          )
                          Text(
                            text = payment.secondaryText.ifBlank { payment.relativeDateText },
                            fontSize = 10.sp,
                            color = HighDensityTextSecondary,
                          )
                          if (!payment.paidDate.isNullOrBlank()) {
                            Text(
                              text = "Recorded date: ${payment.paidDate}",
                              fontSize = 10.sp,
                              color = HighDensityPrimary,
                            )
                          }
                        }

                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                          Text(
                            text = (if (payment.isPositive) "+$" else "-$") + String.format(Locale.US, "%.2f", payment.amount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (payment.isPositive) HighDensityOwedToYou else HighDensityYouOwe,
                          )
                          IconButton(
                            onClick = { onDeleteDebt(payment.id) },
                            modifier = Modifier.size(28.dp).testTag("delete_payment_${payment.id}"),
                          ) {
                            Icon(
                              Icons.Default.Delete,
                              contentDescription = "Delete & un-approve fulfilled debts",
                              tint = HighDensityYouOwe,
                              modifier = Modifier.size(16.dp),
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // 3. ACTIVE DEBTS
        if (activeDebts.isNotEmpty()) {
          activeDebts.forEach { debt ->
            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                  .testTag("debt_item_${debt.id}"),
              colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                  ) {
                    IconButton(
                      onClick = { onTogglePaid(debt) },
                      modifier = Modifier.size(28.dp).testTag("toggle_paid_${debt.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Mark Paid",
                        tint = HighDensityTextSecondary,
                        modifier = Modifier.size(20.dp),
                      )
                    }

                    Column {
                      Text(
                        text = debt.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = HighDensityTextPrimary,
                      )
                      Text(
                        text = "Rec: ${debt.createdDate} • ${debt.relativeDateText} • ${if (debt.isPositive) "Owed to you" else "You owe"}",
                        fontSize = 10.sp,
                        color = HighDensityTextSecondary,
                      )
                      if (debt.secondaryText.isNotBlank()) {
                        Text(
                          text = debt.secondaryText,
                          fontSize = 10.sp,
                          color = HighDensityPrimary,
                        )
                      }
                    }
                  }

                  Text(
                    text = "$${String.format(Locale.US, "%.2f", debt.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (debt.isPositive) HighDensityOwedToYou else HighDensityYouOwe,
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  TextButton(
                    onClick = { debtToEdit = debt },
                    modifier = Modifier.height(30.dp).testTag("edit_debt_${debt.id}"),
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = HighDensityPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.sp, color = HighDensityPrimary)
                  }

                  TextButton(
                    onClick = { onDeleteDebt(debt.id) },
                    modifier = Modifier.height(30.dp).testTag("delete_debt_${debt.id}"),
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp), tint = HighDensityYouOwe)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 11.sp, color = HighDensityYouOwe)
                  }
                }
              }
            }
          }
        }

        // 4. COMPLETED DEBTS (Expandable section below all debts and cash payments)
        if (completedDebts.isNotEmpty()) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(
                1.dp,
                if (isCompletedDebtsExpanded) HighDensityOwedToYou.copy(alpha = 0.5f) else HighDensityBorder,
                RoundedCornerShape(14.dp),
              )
              .clickable { isCompletedDebtsExpanded = !isCompletedDebtsExpanded }
              .testTag("person_completed_debts_toggle"),
            color = HighDensitySurface,
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HighDensityOwedToYou,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Completed Debts",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HighDensityOwedToYou.copy(alpha = 0.15f),
                  ) {
                    Text(
                      text = "${completedDebts.size} settled • $${String.format(Locale.US, "%.2f", completedDebts.sumOf { it.amount })}",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = HighDensityOwedToYou,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = if (isCompletedDebtsExpanded) "Click to collapse" else "Click to expand",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                  )
                  Icon(
                    imageVector = if (isCompletedDebtsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isCompletedDebtsExpanded) "Collapse Completed" else "Expand Completed",
                    tint = HighDensityTextSecondary,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }

              if (isCompletedDebtsExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  completedDebts.forEach { debt ->
                    Card(
                      modifier =
                        Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(12.dp))
                          .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                          .testTag("completed_debt_item_${debt.id}"),
                      colors = CardDefaults.cardColors(containerColor = HighDensityCard),
                    ) {
                      Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                          ) {
                            IconButton(
                              onClick = { onTogglePaid(debt) },
                              modifier = Modifier.size(28.dp).testTag("toggle_unpaid_${debt.id}"),
                            ) {
                              Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark Unpaid",
                                tint = HighDensityOwedToYou,
                                modifier = Modifier.size(20.dp),
                              )
                            }

                            Column {
                              Text(
                                text = debt.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = HighDensityTextSecondary,
                                textDecoration = TextDecoration.LineThrough,
                              )
                              val pDate = debt.paidDate ?: debt.createdDate
                              val pMethod = debt.paymentMethod ?: "Cash"
                              Text(
                                text = "Settled: $pDate ($pMethod)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = HighDensityOwedToYou,
                              )
                              if (debt.secondaryText.isNotBlank()) {
                                Text(
                                  text = debt.secondaryText,
                                  fontSize = 10.sp,
                                  color = HighDensityTextSecondary,
                                )
                              }
                            }
                          }

                          Text(
                            text = "$${String.format(Locale.US, "%.2f", debt.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HighDensityTextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                          )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.End,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          TextButton(
                            onClick = { debtToEdit = debt },
                            modifier = Modifier.height(30.dp),
                          ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = HighDensityPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 11.sp, color = HighDensityPrimary)
                          }

                          TextButton(
                            onClick = { onDeleteDebt(debt.id) },
                            modifier = Modifier.height(30.dp),
                          ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp), tint = HighDensityYouOwe)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 11.sp, color = HighDensityYouOwe)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Empty state when no debts or payments exist at all
        if (personDebts.isEmpty()) {
          Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            color = HighDensitySurface,
          ) {
            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
              Text(
                text = "No debts associated with ${person.name}.",
                fontSize = 12.sp,
                color = HighDensityTextSecondary,
              )
            }
          }
        }

        if (showProjectCosts && personProjectExpenses.isNotEmpty()) {
          Text(
            text = "Project Expenses & Contributions (${personProjectExpenses.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )

          personProjectExpenses.forEach { (projectName, expense) ->
            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            ) {
              Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                  Text(
                    text = "$projectName • ${expense.contributionType}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary,
                  )
                  Text(
                    text = "Logged: ${expense.relativeDate}",
                    fontSize = 10.sp,
                    color = HighDensityTextSecondary,
                  )
                }
                Text(
                  text = "$${String.format("%.2f", expense.amount)}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityPrimary,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Export Profile Debts (CSV / PDF to Downloads folder)
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                Icons.Default.FileDownload,
                contentDescription = null,
                tint = HighDensityPrimary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                "Export Debts",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HighDensityTextPrimary,
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = {
                  ExportManager.exportPersonDebtsCsv(context, person, personDebts, userName = userName)
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("export_person_csv_btn"),
              ) {
                Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
              }

              Button(
                onClick = {
                  ExportManager.exportPersonDebtsPdf(context, person, personDebts, userName = userName)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("export_person_pdf_btn"),
              ) {
                Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Delete person button
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }
        
        OutlinedButton(
          onClick = { showDeleteConfirmDialog = true },
          modifier = Modifier.fillMaxWidth().height(42.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityYouOwe),
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Remove ${person.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        
        if (showDeleteConfirmDialog) {
          AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Remove Person") },
            text = { Text("Are you sure you want to remove ${person.name}? This will delete all associated debts.") },
            confirmButton = {
              TextButton(
                onClick = {
                  showDeleteConfirmDialog = false
                  onDeletePerson()
                },
              ) {
                Text("Remove", color = HighDensityYouOwe)
              }
            },
            dismissButton = {
              TextButton(onClick = { showDeleteConfirmDialog = false }) {
                Text("Cancel", color = HighDensityTextPrimary)
              }
            },
            containerColor = HighDensitySurface,
            titleContentColor = HighDensityTextPrimary,
            textContentColor = HighDensityTextSecondary,
          )
        }
      }
    }
  }

  // Edit Debt Dialog within Person view
  debtToEdit?.let { debt ->
    EditDebtDialog(
      debt = debt,
      onDismiss = { debtToEdit = null },
      onConfirm = { updatedDebt ->
        onEditDebt(updatedDebt)
        debtToEdit = null
      },
    )
  }

  // Record Cash Payment Dialog
  if (showRecordPaymentDialog) {
    RecordCashPaymentDialog(
      person = person,
      unpaidDebts = personDebts.filter { !it.isPaid },
      onDismiss = { showRecordPaymentDialog = false },
      onConfirm = { amount, isReceived, note, paymentDate, linkedDebtId ->
        showRecordPaymentDialog = false
        onRecordPayment?.invoke(amount, isReceived, note, paymentDate, linkedDebtId)
      },
    )
  }

  // Reconciliation Audit Dialog
  if (showReconciliationDialog) {
    ReconciliationDialog(
      reminders = reminders,
      userName = userName,
      targetPerson = person.name,
      onDismiss = { showReconciliationDialog = false },
      onApplyReconciliation = { reconciledDebts, narrative ->
        onReconcile?.invoke(reconciledDebts, narrative)
        showReconciliationDialog = false
      },
    )
  }
}

// -------------------------------------------------------------
// Quick Dialog to Edit an Existing Debt
// -------------------------------------------------------------
// -------------------------------------------------------------
// Color Picker Dialog: Assign custom calendar color to person
// -------------------------------------------------------------
@Composable
fun ColorPickerDialog(
  personName: String,
  currentColorHex: Long,
  onDismiss: () -> Unit,
  onSelectColor: (Long) -> Unit,
) {
  var selectedHex by remember { mutableStateOf(currentColorHex) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("color_picker_dialog"),
    title = {
      Column {
        Text("Calendar Color Tag", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Choose the color for $personName on the calendar", fontSize = 11.sp, color = HighDensityTextSecondary)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Color Swatches Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
        ) {
          AVAILABLE_PERSON_COLORS.take(4).forEach { hex ->
            val color = Color(hex)
            val isSelected = selectedHex == hex
            Box(
              modifier =
                Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(color)
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) HighDensityTextPrimary else Color.Transparent,
                    shape = CircleShape,
                  )
                  .clickable { selectedHex = hex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
        ) {
          AVAILABLE_PERSON_COLORS.drop(4).forEach { hex ->
            val color = Color(hex)
            val isSelected = selectedHex == hex
            Box(
              modifier =
                Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(color)
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) HighDensityTextPrimary else Color.Transparent,
                    shape = CircleShape,
                  )
                  .clickable { selectedHex = hex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
            }
          }
        }

        // Preview Banner
        Surface(
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(selectedHex)))
            Text(
              text = "$personName's debts will show with this color dot on the calendar.",
              fontSize = 11.sp,
              color = HighDensityTextPrimary,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSelectColor(selectedHex) },
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
      ) {
        Text("Apply Color")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

// -------------------------------------------------------------
// Dialog: Add New Person Profile
// -------------------------------------------------------------
@Composable
fun AddPersonDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, colorHex: Long, notes: String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var selectedColorHex by remember { mutableStateOf(AVAILABLE_PERSON_COLORS[0]) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_person_dialog"),
    title = { Text("Add Person Profile", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name / Identifier") },
          placeholder = { Text("e.g., Sarah L., Marcus K.") },
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
          modifier = Modifier.fillMaxWidth().testTag("new_person_name_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
        )

        Text("Select Calendar Tag Color", fontSize = 11.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          AVAILABLE_PERSON_COLORS.take(4).forEach { hex ->
            val isSelected = selectedColorHex == hex
            Box(
              modifier =
                Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(hex))
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) HighDensityTextPrimary else Color.Transparent,
                    shape = CircleShape,
                  )
                  .clickable { selectedColorHex = hex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
          AVAILABLE_PERSON_COLORS.drop(4).take(4).forEach { hex ->
            val isSelected = selectedColorHex == hex
            Box(
              modifier =
                Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(hex))
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) HighDensityTextPrimary else Color.Transparent,
                    shape = CircleShape,
                  )
                  .clickable { selectedColorHex = hex },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name.trim(), selectedColorHex, notes.trim())
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
      ) {
        Text("Save Person")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
