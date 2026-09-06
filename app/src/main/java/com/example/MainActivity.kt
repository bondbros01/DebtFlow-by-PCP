package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_PERSON_COLORS
import com.example.model.AuditLogEntry
import com.example.model.ExpenseLineItem
import com.example.model.NavTab
import com.example.model.PersonRecord
import com.example.model.ProjectRecord
import com.example.model.ReminderItem
import com.example.util.AppPersistenceManager
import com.example.util.ExportManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.CalendarScreenContent
import com.example.ui.HighDensityEmptyState
import com.example.ui.ManageProjectScreen
import com.example.ui.NotificationsDialog
import com.example.ui.PeopleScreenContent
import com.example.ui.RedesignedAddDebtDialog
import com.example.ui.EditDebtDialog
import com.example.ui.RedesignedAddProjectDialog
import com.example.ui.SettingsDialog
import com.example.ui.parseItemCalendar
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
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      var isDarkMode by remember {
        mutableStateOf(AppPersistenceManager.loadDarkMode(context, true))
      }
      MyApplicationTheme(darkTheme = isDarkMode) {
        HighDensityApp(
          isDarkMode = isDarkMode,
          onToggleDarkMode = {
            isDarkMode = it
            AppPersistenceManager.saveDarkMode(context, it)
          },
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun HighDensityApp(
  isDarkMode: Boolean = true,
  onToggleDarkMode: (Boolean) -> Unit = {},
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(NavTab.HOME) }
  val tabBackStack = remember { mutableStateListOf(NavTab.HOME) }
  var showNewDebtDialog by remember { mutableStateOf(false) }
  var debtToEdit by remember { mutableStateOf<ReminderItem?>(null) }
  var showNewProjectDialog by remember { mutableStateOf(false) }
  var showNotificationsDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var isActualCalendarView by remember {
    mutableStateOf(AppPersistenceManager.loadCalendarView(context, false))
  }
  var showProjectCostsInPeople by remember {
    mutableStateOf(AppPersistenceManager.loadShowProjectCosts(context, false))
  }
  var preselectedPersonForDebt by remember { mutableStateOf<String?>(null) }
  var selectedProjectForManagement by remember { mutableStateOf<ProjectRecord?>(null) }
  var userName by remember { mutableStateOf(AppPersistenceManager.loadUserName(context, "User")) }
  var enableNotifications by remember { mutableStateOf(AppPersistenceManager.loadEnableNotifications(context, false)) }
  var excludeCompletedFromExports by remember {
    mutableStateOf(AppPersistenceManager.loadExcludeCompletedFromExports(context, false))
  }
  var showCompletedDebtsInCalendar by remember {
    mutableStateOf(AppPersistenceManager.loadShowCompletedDebtsInCalendar(context, false))
  }
  var currencySymbol by remember {
    mutableStateOf(AppPersistenceManager.loadCurrencySymbol(context, "$"))
  }

  fun navigateToTab(tab: NavTab) {
    if (selectedTab != tab) {
      tabBackStack.add(tab)
      selectedTab = tab
    }
  }

  // Handle Android Back button: pop dialogs/screens or go to previous tab
  BackHandler(
    enabled = selectedProjectForManagement != null ||
      showNewDebtDialog ||
      showNewProjectDialog ||
      showNotificationsDialog ||
      showSettingsDialog ||
      tabBackStack.size > 1,
  ) {
    when {
      selectedProjectForManagement != null -> {
        selectedProjectForManagement = null
      }
      showNewDebtDialog -> {
        showNewDebtDialog = false
        preselectedPersonForDebt = null
      }
      showNewProjectDialog -> {
        showNewProjectDialog = false
      }
      showNotificationsDialog -> {
        showNotificationsDialog = false
      }
      showSettingsDialog -> {
        showSettingsDialog = false
      }
      tabBackStack.size > 1 -> {
        tabBackStack.removeAt(tabBackStack.lastIndex)
        selectedTab = tabBackStack.last()
      }
    }
  }

  // People directory state loaded from persistent storage
  val people = remember {
    mutableStateListOf<PersonRecord>().apply {
      addAll(AppPersistenceManager.loadPeople(context))
    }
  }

  // Reminders/Debts state loaded from persistent storage
  val reminders = remember {
    mutableStateListOf<ReminderItem>().apply {
      addAll(AppPersistenceManager.loadReminders(context))
    }
  }

  // Projects state loaded from persistent storage
  val projects = remember {
    mutableStateListOf<ProjectRecord>().apply {
      addAll(AppPersistenceManager.loadProjects(context))
    }
  }

  // Audit activity logs loaded from persistent storage
  val auditLogs = remember {
    mutableStateListOf<AuditLogEntry>().apply {
      addAll(AppPersistenceManager.loadAuditLogs(context))
    }
  }

  // File picker launcher for CSV import
  val csvImportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      val content = ExportManager.readCsvFromUri(context, uri)
      if (content != null) {
        val result = ExportManager.parseCsvImport(content)
        var addedDebtsCount = 0
        var addedProjectsCount = 0

        result.importedDebts.forEach { debt ->
          if (reminders.none { it.id == debt.id }) {
            reminders.add(0, debt)
            addedDebtsCount++
            val pName = debt.getAssociatedPerson()
            if (pName.isNotBlank() && people.none { it.name.equals(pName, ignoreCase = true) }) {
              val nextColor = AVAILABLE_PERSON_COLORS[people.size % AVAILABLE_PERSON_COLORS.size]
              people.add(PersonRecord(id = "p_${System.currentTimeMillis()}_${people.size}", name = pName, colorHex = nextColor))
            }
          }
        }

        result.importedProjects.forEach { proj ->
          if (projects.none { it.id == proj.id }) {
            projects.add(0, proj)
            addedProjectsCount++
          }
        }

        AppPersistenceManager.saveReminders(context, reminders)
        AppPersistenceManager.savePeople(context, people)
        AppPersistenceManager.saveProjects(context, projects)

        if (addedDebtsCount > 0 || addedProjectsCount > 0) {
          Toast.makeText(context, "Imported $addedDebtsCount debts and $addedProjectsCount projects!", Toast.LENGTH_LONG).show()
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Imported $addedDebtsCount debts and $addedProjectsCount projects from CSV",
              relativeTime = "just now",
              amountText = "Imported",
            ),
          )
        } else {
          Toast.makeText(context, "No new records found in CSV file.", Toast.LENGTH_SHORT).show()
        }
      } else {
        Toast.makeText(context, "Failed to read CSV file.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Automatically sync state mutations to persistent storage
  LaunchedEffect(people.toList()) {
    AppPersistenceManager.savePeople(context, people.toList())
  }

  LaunchedEffect(reminders.toList()) {
    AppPersistenceManager.saveReminders(context, reminders.toList())
    com.example.util.NotificationHelper.checkAndTriggerDueDebtNotifications(context, reminders.toList())
  }

  LaunchedEffect(projects.toList()) {
    AppPersistenceManager.saveProjects(context, projects.toList())
  }

  LaunchedEffect(auditLogs.toList()) {
    AppPersistenceManager.saveAuditLogs(context, auditLogs.toList())
  }

  LaunchedEffect(isActualCalendarView) {
    AppPersistenceManager.saveCalendarView(context, isActualCalendarView)
  }

  LaunchedEffect(showProjectCostsInPeople) {
    AppPersistenceManager.saveShowProjectCosts(context, showProjectCostsInPeople)
  }

  LaunchedEffect(userName) {
    AppPersistenceManager.saveUserName(context, userName)
  }

  LaunchedEffect(enableNotifications) {
    AppPersistenceManager.saveEnableNotifications(context, enableNotifications)
  }

  LaunchedEffect(currencySymbol) {
    AppPersistenceManager.saveCurrencySymbol(context, currencySymbol)
  }

  // Calculate live dynamic balances
  val youOwe = reminders.filter { !it.isPaid && !it.isPositive }.sumOf { it.amount }
  val owedToYou = reminders.filter { !it.isPaid && it.isPositive }.sumOf { it.amount }
  val netPosition = owedToYou - youOwe

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = HighDensityBackground,
    contentWindowInsets = WindowInsets.statusBars,
    bottomBar = {
      HighDensityBottomNav(
        selectedTab = selectedTab,
        onTabSelected = { navigateToTab(it) },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      // Top Header with Settings & Notification Icons
      HighDensityHeader(
        onNotificationsClick = { showNotificationsDialog = true },
        onSettingsClick = { showSettingsDialog = true },
      )

      val handleDeleteDebt: (String) -> Unit = { debtId ->
        val index = reminders.indexOfFirst { it.id == debtId }
        if (index >= 0) {
          val removed = reminders.removeAt(index)
          val isPayment = ExportManager.isPaymentItem(removed) ||
            removed.id.startsWith("pay_") ||
            (removed.iconType.equals("Cash", ignoreCase = true) && removed.title.contains("Cash Payment", ignoreCase = true)) ||
            (removed.settledDebtIds != null && removed.settledDebtIds.isNotBlank())

          val unapprovedSnapshots = mutableListOf<Pair<Int, ReminderItem>>()
          val unapprovedTitles = mutableListOf<String>()
          val removedCredits = mutableListOf<ReminderItem>()

          if (isPayment) {
            val settledIds = (removed.settledDebtIds?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()).toSet()
            for (i in reminders.indices) {
              val item = reminders[i]
              val wasFulfilled = item.settledByPaymentId == debtId || settledIds.contains(item.id)
              if (wasFulfilled) {
                unapprovedSnapshots.add(i to item)
                unapprovedTitles.add(item.title)
                val cleanedSecondary = item.secondaryText
                  .replace(Regex("""\s*•?\s*Fully paid in cash on.*"""), "")
                  .replace(Regex("""\s*•?\s*Settled in cash on.*"""), "")
                  .replace(Regex("""\s*\(Paid \$[0-9.]+\s*cash on.*\)\s*"""), "")
                  .trim()

                // DO NOT modify debt pricing: keep item.amount completely unchanged!
                reminders[i] = item.copy(
                  isPaid = false,
                  paidDate = null,
                  paymentMethod = null,
                  settledByPaymentId = null,
                  deductedAmount = null,
                  secondaryText = cleanedSecondary,
                )
              }
            }

            // Also remove any excess credit item created with this payment
            val creditsToDrop = reminders.filter { it.settledByPaymentId == debtId && it.id.startsWith("credit_") }
            removedCredits.addAll(creditsToDrop)
            reminders.removeAll { it.settledByPaymentId == debtId && it.id.startsWith("credit_") }
          }

          AppPersistenceManager.saveReminders(context, reminders)

          val narrative = if (isPayment && unapprovedTitles.isNotEmpty()) {
            "Deleted payment of $${String.format(Locale.US, "%.2f", removed.amount)}. Re-opened ${unapprovedTitles.size} debt(s): ${unapprovedTitles.joinToString(", ")}."
          } else if (isPayment) {
            "Deleted payment of $${String.format(Locale.US, "%.2f", removed.amount)} today"
          } else {
            "Removed debt '${removed.title}' today"
          }

          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = narrative,
              relativeTime = "today",
              amountText = "$${String.format("%.2f", removed.amount)}",
              undoAction = {
                reminders.add(index.coerceAtMost(reminders.size), removed)
                for ((_, snapshot) in unapprovedSnapshots) {
                  val currIdx = reminders.indexOfFirst { it.id == snapshot.id }
                  if (currIdx >= 0) {
                    reminders[currIdx] = snapshot
                  }
                }
                for (c in removedCredits) {
                  reminders.add(0, c)
                }
                AppPersistenceManager.saveReminders(context, reminders)
              },
            ),
          )
        }
      }

      // Active Screen View
      Box(
        modifier = Modifier.weight(1f),
      ) {
        when (selectedTab) {
        NavTab.HOME -> {
          HomeScreenContent(
            netPosition = netPosition,
            youOwe = youOwe,
            owedToYou = owedToYou,
            reminders = reminders,
            currencySymbol = currencySymbol,
            onNewDebtClick = { showNewDebtDialog = true },
            onNewProjectClick = { showNewProjectDialog = true },
            onViewCalendarClick = { navigateToTab(NavTab.CALENDAR) },
            onEditDebt = { debtToEdit = it },
            onDeleteDebt = handleDeleteDebt,
            onTogglePaid = { item ->
              val index = reminders.indexOfFirst { it.id == item.id }
              if (index >= 0) {
                val prevPaid = item.isPaid
                val todayStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
                val updated = if (!item.isPaid) {
                  item.copy(
                    isPaid = true,
                    paidDate = item.paidDate ?: todayStr,
                    paymentMethod = item.paymentMethod ?: "Cash",
                  )
                } else {
                  item.copy(
                    isPaid = false,
                    paidDate = null,
                    paymentMethod = null,
                    settledByPaymentId = null,
                  )
                }
                reminders[index] = updated
                AppPersistenceManager.saveReminders(context, reminders)
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "${item.title} marked as ${if (updated.isPaid) "paid today" else "unpaid"}",
                    relativeTime = "just now",
                    amountText = "$${String.format("%.2f", item.amount)}",
                    undoAction = {
                      val i = reminders.indexOfFirst { it.id == item.id }
                      if (i >= 0) {
                        reminders[i] = reminders[i].copy(isPaid = prevPaid)
                        AppPersistenceManager.saveReminders(context, reminders)
                      }
                    },
                  ),
                )
              }
            },
          )
        }

        NavTab.PEOPLE -> {
          PeopleScreenContent(
            people = people,
            reminders = reminders,
            projects = projects,
            showProjectCosts = showProjectCostsInPeople,
            userName = userName,
            onAddPerson = { name, colorHex, notes ->
              val newP =
                PersonRecord(
                  id = "p_${System.currentTimeMillis()}",
                  name = name,
                  colorHex = colorHex,
                  notes = notes,
                )
              people.add(newP)
              auditLogs.add(
                0,
                AuditLogEntry(
                  id = "log_${System.currentTimeMillis()}",
                  narrative = "Added $name to contacts directory",
                  relativeTime = "today",
                  amountText = "Profile",
                  undoAction = {
                    people.removeAll { it.id == newP.id }
                  },
                ),
              )
            },
            onUpdatePersonColor = { personId, newColorHex ->
              val idx = people.indexOfFirst { it.id == personId }
              if (idx >= 0) {
                val oldColor = people[idx].colorHex
                val updated = people[idx].copy(colorHex = newColorHex)
                people[idx] = updated
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "Updated ${updated.name}'s calendar tag color",
                    relativeTime = "today",
                    amountText = "Calendar",
                    undoAction = {
                      val i = people.indexOfFirst { it.id == personId }
                      if (i >= 0) people[i] = people[i].copy(colorHex = oldColor)
                    },
                  ),
                )
              }
            },
            onDeletePerson = { personId ->
              val idx = people.indexOfFirst { it.id == personId }
              if (idx >= 0) {
                val removed = people.removeAt(idx)
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "Removed ${removed.name} from contacts directory",
                    relativeTime = "today",
                    amountText = "Profile",
                    undoAction = {
                      people.add(idx.coerceAtMost(people.size), removed)
                    },
                    redoAction = {
                      val i = people.indexOfFirst { it.id == removed.id }
                      if (i < 0) people.add(idx.coerceAtMost(people.size), removed)
                    },
                  ),
                )
              }
            },
            onUpdatePersonName = { personId, newName ->
              val idx = people.indexOfFirst { it.id == personId }
              if (idx >= 0) {
                val oldName = people[idx].name
                val updated = people[idx].copy(name = newName)
                people[idx] = updated
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "Renamed contact from $oldName to $newName",
                    relativeTime = "today",
                    amountText = "Profile",
                    undoAction = {
                      val i = people.indexOfFirst { it.id == personId }
                      if (i >= 0) people[i] = people[i].copy(name = oldName)
                    },
                    redoAction = {
                      val i = people.indexOfFirst { it.id == personId }
                      if (i >= 0) people[i] = people[i].copy(name = newName)
                    },
                  ),
                )
              } else {
                val newP = PersonRecord(
                  id = "p_${System.currentTimeMillis()}",
                  name = newName,
                  colorHex = 0xFF625B71,
                )
                people.add(newP)
              }
            },
            onToggleDebtPaid = { item ->
              val index = reminders.indexOfFirst { it.id == item.id }
              if (index >= 0) {
                val prevPaid = item.isPaid
                val todayStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
                val updated = if (!item.isPaid) {
                  item.copy(
                    isPaid = true,
                    paidDate = item.paidDate ?: todayStr,
                    paymentMethod = item.paymentMethod ?: "Cash",
                  )
                } else {
                  item.copy(
                    isPaid = false,
                    paidDate = null,
                    paymentMethod = null,
                    settledByPaymentId = null,
                  )
                }
                reminders[index] = updated
                AppPersistenceManager.saveReminders(context, reminders)
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "${item.title} marked as ${if (updated.isPaid) "paid today" else "unpaid"}",
                    relativeTime = "just now",
                    amountText = "$${String.format("%.2f", item.amount)}",
                    undoAction = {
                      val i = reminders.indexOfFirst { it.id == item.id }
                      if (i >= 0) {
                        reminders[i] = reminders[i].copy(isPaid = prevPaid)
                        AppPersistenceManager.saveReminders(context, reminders)
                      }
                    },
                  ),
                )
              }
            },
            onEditDebt = { updatedDebt ->
              val index = reminders.indexOfFirst { it.id == updatedDebt.id }
              if (index >= 0) {
                val oldDebt = reminders[index]
                reminders[index] = updatedDebt
                AppPersistenceManager.saveReminders(context, reminders)
                auditLogs.add(
                  0,
                  AuditLogEntry(
                    id = "log_${System.currentTimeMillis()}",
                    narrative = "Modified debt '${updatedDebt.title}' today",
                    relativeTime = "today",
                    amountText = "$${String.format("%.2f", updatedDebt.amount)}",
                    undoAction = {
                      val i = reminders.indexOfFirst { it.id == updatedDebt.id }
                      if (i >= 0) {
                        reminders[i] = oldDebt
                        AppPersistenceManager.saveReminders(context, reminders)
                      }
                    },
                  ),
                )
              }
            },
            onDeleteDebt = handleDeleteDebt,
            onAddNewDebtForPerson = { personName ->
              preselectedPersonForDebt = personName
              showNewDebtDialog = true
            },
            onExportLog = { narrative ->
              auditLogs.add(
                0,
                AuditLogEntry(
                  id = "log_${System.currentTimeMillis()}",
                  narrative = narrative,
                  relativeTime = "just now",
                  amountText = "Export",
                ),
              )
            },
            onRecordPayment = { personName, amount, isReceived, note, paymentDate, linkedDebtId ->
              val dirStr = if (isReceived) "from" else "to"
              val snapshots = mutableListOf<Pair<Int, ReminderItem>>()
              val addedItems = mutableListOf<ReminderItem>()
              val paymentId = "pay_${System.currentTimeMillis()}"
              val fulfilledDebtIds = mutableListOf<String>()

              // Find matching unpaid debts in this payment direction
              val relevantDebts = reminders.filter { debt ->
                !debt.isPaid && (debt.isPositive == isReceived) &&
                  (debt.getAssociatedPerson().equals(personName, ignoreCase = true) ||
                   debt.secondaryText.contains(personName, ignoreCase = true) ||
                   debt.personName.equals(personName, ignoreCase = true))
              }.toMutableList()

              var remainingPayment = amount

              if (linkedDebtId != null) {
                val idx = reminders.indexOfFirst { it.id == linkedDebtId }
                if (idx >= 0) {
                  val oldDebt = reminders[idx]
                  snapshots.add(idx to oldDebt)
                  if (remainingPayment >= oldDebt.amount) {
                    fulfilledDebtIds.add(oldDebt.id)
                    // Enable/disable (mark paid) WITHOUT modifying amount
                    val updated = oldDebt.copy(
                      isPaid = true,
                      paidDate = paymentDate,
                      paymentMethod = "Cash",
                      settledByPaymentId = paymentId,
                      secondaryText = if (oldDebt.secondaryText.isNotBlank()) "${oldDebt.secondaryText} • Fully paid in cash on $paymentDate" else "Fully paid in cash on $paymentDate",
                    )
                    reminders[idx] = updated
                    remainingPayment -= oldDebt.amount
                  }
                }
              }

              // Apply remaining payment across other relevant unpaid debts for this person
              if (remainingPayment > 0.0 && relevantDebts.isNotEmpty()) {
                for (debt in relevantDebts.filter { it.id != linkedDebtId }) {
                  if (remainingPayment <= 0.0) break
                  val idx = reminders.indexOfFirst { it.id == debt.id }
                  if (idx >= 0) {
                    val oldDebt = reminders[idx]
                    snapshots.add(idx to oldDebt)
                    if (remainingPayment >= oldDebt.amount) {
                      fulfilledDebtIds.add(oldDebt.id)
                      // Enable/disable (mark paid) WITHOUT modifying amount
                      reminders[idx] = oldDebt.copy(
                        isPaid = true,
                        paidDate = paymentDate,
                        paymentMethod = "Cash",
                        settledByPaymentId = paymentId,
                        secondaryText = if (oldDebt.secondaryText.isNotBlank()) "${oldDebt.secondaryText} • Fully paid in cash on $paymentDate" else "Fully paid in cash on $paymentDate",
                      )
                      remainingPayment -= oldDebt.amount
                    }
                  }
                }
              }

              // If excess payment remains (and no unpaid debts left), add a cash credit item
              if (remainingPayment > 0.0 && fulfilledDebtIds.isEmpty()) {
                val creditItem = ReminderItem(
                  id = "credit_${System.currentTimeMillis()}",
                  title = "Cash Credit - $personName",
                  relativeDateText = "Available credit",
                  amount = remainingPayment,
                  isPositive = !isReceived,
                  secondaryText = "Overpayment credit ($personName) • Recorded on $paymentDate",
                  iconType = "Cash",
                  isPaid = false,
                  personName = personName,
                  createdDate = paymentDate,
                  settledByPaymentId = paymentId,
                )
                reminders.add(0, creditItem)
                addedItems.add(creditItem)
              }

              // ALWAYS create the cash payment record item
              val paymentRecord = ReminderItem(
                id = paymentId,
                title = "Cash Payment - ${note.ifBlank { if (isReceived) "Cash received from $personName" else "Cash paid to $personName" }}",
                relativeDateText = "Paid on $paymentDate",
                amount = amount,
                isPositive = isReceived,
                secondaryText = if (fulfilledDebtIds.isNotEmpty()) {
                  "Cash payment ($personName) • Fulfilled ${fulfilledDebtIds.size} debt(s)"
                } else {
                  "Cash payment ($personName)"
                },
                iconType = "Cash",
                isPaid = true,
                personName = personName,
                createdDate = paymentDate,
                paidDate = paymentDate,
                paymentMethod = "Cash",
                settledDebtIds = fulfilledDebtIds.joinToString(","),
              )
              reminders.add(0, paymentRecord)
              addedItems.add(paymentRecord)

              AppPersistenceManager.saveReminders(context, reminders)

              val auditNarrative = if (fulfilledDebtIds.isNotEmpty()) {
                "Recorded cash payment of $${String.format(Locale.US, "%.2f", amount)} $dirStr $personName. Settled ${fulfilledDebtIds.size} debt(s)."
              } else {
                "Recorded cash payment of $${String.format(Locale.US, "%.2f", amount)} $dirStr $personName."
              }

              auditLogs.add(
                0,
                AuditLogEntry(
                  id = "log_${System.currentTimeMillis()}",
                  narrative = auditNarrative,
                  relativeTime = "just now",
                  amountText = "$${String.format(Locale.US, "%.2f", amount)}",
                  undoAction = {
                    for (item in addedItems) {
                      reminders.removeAll { it.id == item.id }
                    }
                    for ((idx, snapshot) in snapshots) {
                      val currentIdx = reminders.indexOfFirst { it.id == snapshot.id }
                      if (currentIdx >= 0) {
                        reminders[currentIdx] = snapshot
                      } else if (idx in 0..reminders.size) {
                        reminders.add(idx, snapshot)
                      }
                    }
                    AppPersistenceManager.saveReminders(context, reminders)
                  },
                ),
              )
            },
            onReconcile = { reconciledReminders, narrative ->
              val oldReminders = reminders.toList()
              reminders.clear()
              reminders.addAll(reconciledReminders)
              AppPersistenceManager.saveReminders(context, reminders)
              auditLogs.add(
                0,
                AuditLogEntry(
                  id = "log_${System.currentTimeMillis()}",
                  narrative = narrative,
                  relativeTime = "just now",
                  amountText = "Audit",
                  undoAction = {
                    reminders.clear()
                    reminders.addAll(oldReminders)
                    AppPersistenceManager.saveReminders(context, reminders)
                  },
                ),
              )
            },
          )
        }

        NavTab.PROJECTS -> {
          ProjectsScreenContent(
            projects = projects,
            currencySymbol = currencySymbol,
            onNewProjectClick = { showNewProjectDialog = true },
            onEditProject = { project ->
              selectedProjectForManagement = project
            },
          )
        }

        NavTab.CALENDAR -> {
          CalendarScreenContent(
            reminders = if (showCompletedDebtsInCalendar) reminders else reminders.filter { !it.isPaid },
            people = people,
            isActualCalendarView = isActualCalendarView,
            currencySymbol = currencySymbol,
            onToggleView = { isActualCalendarView = it },
            onOpenSettings = { showSettingsDialog = true },
            onBackToDashboard = { selectedTab = NavTab.HOME },
            onEditDebt = { debtToEdit = it }
          )
        }

        NavTab.LOG -> {
          AuditLogScreenContent(
            logs = auditLogs,
            onUndoLog = { logToUndo ->
              if (!logToUndo.isUndone) {
                logToUndo.undoAction?.invoke()
                auditLogs.removeIf { it.id == logToUndo.id }
                val newLogId = "log_${System.currentTimeMillis()}"
                val undoneLog = AuditLogEntry(
                  id = newLogId,
                  narrative = "Reverted action: ${logToUndo.narrative}",
                  relativeTime = "just now",
                  amountText = "Undone",
                  isUndone = true,
                  redoAction = {
                    logToUndo.redoAction?.invoke() ?: logToUndo.undoAction?.invoke()
                    auditLogs.removeIf { it.id == newLogId }
                    auditLogs.add(0, logToUndo.copy(isUndone = false))
                  },
                )
                auditLogs.add(0, undoneLog)
              }
            },
            onRedoLog = { logRedo ->
              if (logRedo.isUndone && logRedo.redoAction != null) {
                logRedo.redoAction?.invoke()
                auditLogs.removeIf { it.id == logRedo.id }
              }
            },
          )
        }
      }
      }
    }
  }

  // Manage Project Full Screen / Dialog (pops up when clicking edit icon on a project)
  selectedProjectForManagement?.let { currentProj ->
    ManageProjectScreen(
      project = currentProj,
      people = people,
      onClose = { selectedProjectForManagement = null },
       onAddExpense = { newExpense ->
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val updatedLineItems = currentProj.lineItems + newExpense
          val updated = currentProj.copy(lineItems = updatedLineItems)
          projects[pIndex] = updated
          selectedProjectForManagement = updated
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Added ${newExpense.contributionType} expense ($${String.format("%.2f", newExpense.amount)}) to ${currentProj.name} today",
              relativeTime = "today",
              amountText = "$${String.format("%.2f", newExpense.amount)}",
              undoAction = {
                val pIdx = projects.indexOfFirst { it.id == currentProj.id }
                if (pIdx >= 0) {
                  val p = projects[pIdx]
                  val reverted = p.copy(lineItems = p.lineItems.filter { it.id != newExpense.id })
                  projects[pIdx] = reverted
                  if (selectedProjectForManagement?.id == currentProj.id) {
                    selectedProjectForManagement = reverted
                  }
                }
              },
            ),
          )
        }
      },
      onEditExpense = { editedExpense ->
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val oldExpense = currentProj.lineItems.find { it.id == editedExpense.id }
          val updatedLineItems =
            currentProj.lineItems.map { if (it.id == editedExpense.id) editedExpense else it }
          val updated = currentProj.copy(lineItems = updatedLineItems)
          projects[pIndex] = updated
          selectedProjectForManagement = updated
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Updated expense ${editedExpense.participantName} ($${String.format("%.2f", editedExpense.amount)}) in ${currentProj.name}",
              relativeTime = "today",
              amountText = "$${String.format("%.2f", editedExpense.amount)}",
              undoAction = {
                if (oldExpense != null) {
                  val pIdx = projects.indexOfFirst { it.id == currentProj.id }
                  if (pIdx >= 0) {
                    val p = projects[pIdx]
                    val reverted = p.copy(lineItems = p.lineItems.map { if (it.id == editedExpense.id) oldExpense else it })
                    projects[pIdx] = reverted
                    if (selectedProjectForManagement?.id == currentProj.id) {
                      selectedProjectForManagement = reverted
                    }
                  }
                }
              },
            ),
          )
        }
      },
      onDeleteExpense = { expenseId ->
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val removedExpense = currentProj.lineItems.find { it.id == expenseId }
          val updatedLineItems = currentProj.lineItems.filter { it.id != expenseId }
          val updated = currentProj.copy(lineItems = updatedLineItems)
          projects[pIndex] = updated
          selectedProjectForManagement = updated
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Removed expense from ${currentProj.name} today",
              relativeTime = "today",
              amountText = "-$0.00",
              undoAction = {
                if (removedExpense != null) {
                  val pIdx = projects.indexOfFirst { it.id == currentProj.id }
                  if (pIdx >= 0) {
                    val p = projects[pIdx]
                    val reverted = p.copy(lineItems = p.lineItems + removedExpense)
                    projects[pIdx] = reverted
                    if (selectedProjectForManagement?.id == currentProj.id) {
                      selectedProjectForManagement = reverted
                    }
                  }
                }
              },
            ),
          )
        }
      },
      onRecordSale = { salePrice, liquidationSummary ->
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val updated = currentProj.copy(isSold = true, salePrice = salePrice)
          projects[pIndex] = updated
          selectedProjectForManagement = updated
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = liquidationSummary,
              relativeTime = "today",
              amountText = "+$$salePrice",
              undoAction = {
                val pIdx = projects.indexOfFirst { it.id == currentProj.id }
                if (pIdx >= 0) {
                  val p = projects[pIdx]
                  val unsold = p.copy(isSold = false, salePrice = null)
                  projects[pIdx] = unsold
                  if (selectedProjectForManagement?.id == currentProj.id) {
                    selectedProjectForManagement = unsold
                  }
                }
              },
            ),
          )
        }
      },
      onExportProject = {
        ExportManager.exportProjectCsv(context, currentProj, userName = userName)
      },
      onExportProjectPdf = {
        ExportManager.exportProjectPdf(context, currentProj, userName = userName)
      },
      onRevertSale = {
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val updated = currentProj.copy(isSold = false, salePrice = null)
          projects[pIndex] = updated
          selectedProjectForManagement = updated
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Reverted sale for project '${currentProj.name}'",
              relativeTime = "today",
              amountText = "Reverted",
            ),
          )
        }
      },
      onDeleteProject = {
        val pIndex = projects.indexOfFirst { it.id == currentProj.id }
        if (pIndex >= 0) {
          val removed = projects.removeAt(pIndex)
          selectedProjectForManagement = null
          auditLogs.add(
            0,
            AuditLogEntry(
              id = "log_${System.currentTimeMillis()}",
              narrative = "Deleted project '${removed.name}'",
              relativeTime = "just now",
              amountText = "Deleted",
              undoAction = {
                projects.add(pIndex.coerceAtMost(projects.size), removed)
              },
            ),
          )
          Toast.makeText(context, "Project deleted: ${removed.name}", Toast.LENGTH_SHORT).show()
        }
      },
    )
  }


  // Edit Debt Dialog (Global)
  debtToEdit?.let { debt ->
    EditDebtDialog(
      debt = debt,
      people = people,
      onDismiss = { debtToEdit = null },
      onConfirm = { updatedDebt ->
        val idx = reminders.indexOfFirst { it.id == updatedDebt.id }
        if (idx >= 0) {
          reminders[idx] = updatedDebt
        }
        debtToEdit = null
      }
    )
  }

  // Redesigned Dialogs
  if (showNewDebtDialog) {
    RedesignedAddDebtDialog(
      people = people,
      initialPerson = preselectedPersonForDebt,
      onDismiss = {
        showNewDebtDialog = false
        preselectedPersonForDebt = null
      },
      onConfirm = { title, amount, relativeDate, payer, category, isPositive ->
        val trimmedPayer = payer.trim()
        if (trimmedPayer.isNotBlank() && people.none { it.name.equals(trimmedPayer, ignoreCase = true) }) {
          val nextColor = AVAILABLE_PERSON_COLORS[people.size % AVAILABLE_PERSON_COLORS.size]
          people.add(
            PersonRecord(
              id = "p_${System.currentTimeMillis()}",
              name = trimmedPayer,
              colorHex = nextColor,
            ),
          )
        }

        val newDebt =
          ReminderItem(
            id = "debt_${System.currentTimeMillis()}",
            title = title,
            relativeDateText = relativeDate,
            amount = amount,
            isPositive = isPositive,
            secondaryText = "Payer: $trimmedPayer",
            iconType = if (category.contains("Subscription", ignoreCase = true)) "subscription" else "person",
            isPaid = false,
            personName = trimmedPayer,
          )
        reminders.add(0, newDebt)
        auditLogs.add(
          0,
          AuditLogEntry(
            id = "log_${System.currentTimeMillis()}",
            narrative = "New debt '$title' ($category) added for $trimmedPayer",
            relativeTime = "today",
            amountText = "$${String.format("%.2f", amount)}",
            undoAction = {
              reminders.removeAll { it.id == newDebt.id }
            },
          ),
        )
        showNewDebtDialog = false
        preselectedPersonForDebt = null
      },
    )
  }

  if (showNewProjectDialog) {
    RedesignedAddProjectDialog(
      people = people,
      onDismiss = { showNewProjectDialog = false },
      onConfirm = { newProj ->
        projects.add(0, newProj)
        auditLogs.add(
          0,
          AuditLogEntry(
            id = "log_${System.currentTimeMillis()}",
            narrative = "Project '${newProj.name}' created with budget $${String.format("%.2f", newProj.totalEstimatedBudget)}",
            relativeTime = "today",
            amountText = "$${String.format("%.2f", newProj.totalEstimatedBudget)}",
          ),
        )
        showNewProjectDialog = false
      },
    )
  }

  if (showSettingsDialog) {
    SettingsDialog(
      isActualCalendarView = isActualCalendarView,
      onToggleCalendarView = { isActualCalendarView = it },
      isDarkMode = isDarkMode,
      onToggleDarkMode = onToggleDarkMode,
      showProjectCostsInPeople = showProjectCostsInPeople,
      onToggleShowProjectCostsInPeople = { showProjectCostsInPeople = it },
      userName = userName,
      onUserNameChange = { userName = it },
      enableNotifications = enableNotifications,
      onToggleNotifications = { enableNotifications = it },
      excludeCompletedFromExports = excludeCompletedFromExports,
      onToggleExcludeCompletedFromExports = {
        excludeCompletedFromExports = it
        AppPersistenceManager.saveExcludeCompletedFromExports(context, it)
      },
      showCompletedDebtsInCalendar = showCompletedDebtsInCalendar,
      onToggleShowCompletedDebtsInCalendar = {
        showCompletedDebtsInCalendar = it
        AppPersistenceManager.saveShowCompletedDebtsInCalendar(context, it)
      },
      currencySymbol = currencySymbol,
      onCurrencySymbolChange = {
        currencySymbol = it
        AppPersistenceManager.saveCurrencySymbol(context, it)
      },
      onImportCsv = {
        showSettingsDialog = false
        csvImportLauncher.launch("text/*")
      },
      onDismiss = { showSettingsDialog = false },
      onExportCsv = {
        ExportManager.exportAllDebtsCsv(context, reminders, userName = userName, excludeCompleted = excludeCompletedFromExports)
        showSettingsDialog = false
      },
      onExportPdf = {
        ExportManager.exportAllDebtsPdf(context, reminders, userName = userName, excludeCompleted = excludeCompletedFromExports)
        showSettingsDialog = false
      },
    )
  }

  if (showNotificationsDialog) {
    NotificationsDialog(
      reminders = reminders.filter { !it.isPaid },
      onDismiss = { showNotificationsDialog = false },
      onEditDebt = { selectedDebt ->
        showNotificationsDialog = false
        debtToEdit = selectedDebt
      }
    )
  }
}

// -------------------------------------------------------------
// Top Header Component with Settings Icon
// -------------------------------------------------------------
@Composable
fun HighDensityHeader(
  onNotificationsClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val formattedCurrentDate = remember {
    SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
  }
  var showCoffeeDialog by remember { mutableStateOf(false) }
  val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

  if (showCoffeeDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showCoffeeDialog = false },
      title = { Text("Buy me a coffee?") },
      text = { Text("Would you like to buy me coffee and support development?") },
      confirmButton = {
        TextButton(onClick = {
          showCoffeeDialog = false
          uriHandler.openUri("https://buymeacoffee.com/problemchildperformance")
        }) {
          Text("Yes", color = HighDensityPrimary)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCoffeeDialog = false }) {
          Text("No", color = HighDensityTextSecondary)
        }
      },
      containerColor = HighDensityBackground,
      titleContentColor = HighDensityTextPrimary,
      textContentColor = HighDensityTextPrimary,
    )
  }

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
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
            .width(85.dp)
            .height(40.dp)
            .clickable { showCoffeeDialog = true },
        contentAlignment = Alignment.Center,
      ) {
        androidx.compose.foundation.Image(
          painter = androidx.compose.ui.res.painterResource(id = R.drawable.pcp_logo),
          contentDescription = "Buy me a coffee",
          modifier = Modifier.fillMaxSize(),
          contentScale = androidx.compose.ui.layout.ContentScale.Fit,
          colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(HighDensityTextPrimary)
        )
      }
      Column {
        Text(
          text = "DebtFlow",
          fontSize = 20.sp,
          fontWeight = FontWeight.SemiBold,
          color = HighDensityTextPrimary,
          lineHeight = 24.sp,
        )
        Text(
          text = formattedCurrentDate,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
          color = HighDensityTextSecondary,
          letterSpacing = 1.sp,
        )
      }
    }

    // Top Right Action Buttons (Notifications + Settings)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      IconButton(
        onClick = onNotificationsClick,
        modifier =
          Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(HighDensitySurface)
            .testTag("notifications_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Notifications",
          tint = HighDensityTextPrimary,
          modifier = Modifier.size(20.dp),
        )
      }
      IconButton(
        onClick = onSettingsClick,
        modifier =
          Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(HighDensitySurface)
            .testTag("settings_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = HighDensityTextPrimary,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

// -------------------------------------------------------------
// Debt Sorting Options
// -------------------------------------------------------------
enum class DebtSortOption(val label: String) {
  DATE_DESC("Date (Newest First)"),
  DATE_ASC("Date (Oldest First)"),
  AMOUNT_DESC("Amount (Highest First)"),
  AMOUNT_ASC("Amount (Lowest First)"),
  ALPHA_ASC("Name (A to Z)"),
  ALPHA_DESC("Name (Z to A)"),
}

private fun getReminderTimestamp(item: ReminderItem): Long {
  return try {
    parseItemCalendar(item.relativeDateText, item.createdDate)?.timeInMillis
      ?: SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(item.createdDate)?.time
      ?: item.id.filter { it.isDigit() }.toLongOrNull()
      ?: 0L
  } catch (e: Exception) {
    item.id.filter { it.isDigit() }.toLongOrNull() ?: 0L
  }
}

// -------------------------------------------------------------
// Home Screen Content (High Density Layout)
// -------------------------------------------------------------
@Composable
fun HomeScreenContent(
  netPosition: Double,
  youOwe: Double,
  owedToYou: Double,
  reminders: List<ReminderItem>,
  currencySymbol: String = "$",
  onNewDebtClick: () -> Unit,
  onNewProjectClick: () -> Unit,
  onViewCalendarClick: () -> Unit,
  onEditDebt: (ReminderItem) -> Unit = {},
  onDeleteDebt: (String) -> Unit = {},
  onTogglePaid: (ReminderItem) -> Unit,
) {
  var debtSearchQuery by remember { mutableStateOf("") }
  var sortOption by remember { mutableStateOf(DebtSortOption.DATE_DESC) }
  var isSortMenuExpanded by remember { mutableStateOf(false) }

  val paidCount = reminders.count { ExportManager.isPaymentItem(it) }
  val totalPaidSum = reminders.filter { ExportManager.isPaymentItem(it) }.sumOf { it.amount }

  // Filter based on search query
  val filteredReminders = reminders.filter { item ->
    if (debtSearchQuery.isBlank()) true
    else {
      item.title.contains(debtSearchQuery, ignoreCase = true) ||
        item.secondaryText.contains(debtSearchQuery, ignoreCase = true) ||
        item.getAssociatedPerson().contains(debtSearchQuery, ignoreCase = true)
    }
  }

  // Sort filtered items
  val sortedReminders = when (sortOption) {
    DebtSortOption.DATE_DESC -> filteredReminders.sortedByDescending { getReminderTimestamp(it) }
    DebtSortOption.DATE_ASC -> filteredReminders.sortedBy { getReminderTimestamp(it) }
    DebtSortOption.AMOUNT_DESC -> filteredReminders.sortedByDescending { it.amount }
    DebtSortOption.AMOUNT_ASC -> filteredReminders.sortedBy { it.amount }
    DebtSortOption.ALPHA_ASC -> filteredReminders.sortedBy { it.title.lowercase(Locale.US) }
    DebtSortOption.ALPHA_DESC -> filteredReminders.sortedByDescending { it.title.lowercase(Locale.US) }
  }

  var isPaymentsRecordExpanded by remember { mutableStateOf(false) }
  var isCompletedDebtsExpanded by remember { mutableStateOf(false) }

  LazyColumn(
    modifier =
      Modifier
        .fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      // Net Position Card
      Card(
      modifier =
        Modifier
          .fillMaxWidth()
          .testTag("net_position_card"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column {
            Text(
              text = "NET POSITION",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = HighDensityTextSecondary,
              letterSpacing = 0.5.sp,
            )
            Text(
              text = "${currencySymbol}${String.format("%,.2f", netPosition)}",
              fontSize = 30.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityTextPrimary,
              modifier = Modifier.padding(top = 4.dp),
            )
          }

          Box(
            modifier =
              Modifier
                .clip(CircleShape)
                .background(HighDensityPrimaryContainer)
                .padding(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Text(
              text = if (totalPaidSum > 0.0) "+ ${currencySymbol}${String.format("%.2f", totalPaidSum)} settled" else "Live Balance",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = HighDensityOnPrimaryContainer,
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // You Owe Box
          Box(
            modifier =
              Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(HighDensitySurface.copy(alpha = 0.85f))
                .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
          ) {
            Column {
              Text(
                text = "YOU OWE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
              Text(
                text = "${currencySymbol}${String.format("%,.2f", youOwe)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HighDensityYouOwe,
                modifier = Modifier.padding(top = 2.dp),
              )
            }
          }

          // Owed To You Box
          Box(
            modifier =
              Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(HighDensitySurface.copy(alpha = 0.85f))
                .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
          ) {
            Column {
              Text(
                text = "OWED TO YOU",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
              Text(
                text = "${currencySymbol}${String.format("%,.2f", owedToYou)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HighDensityOwedToYou,
                modifier = Modifier.padding(top = 2.dp),
              )
            }
          }
        }
      }
    }
    }

    item {
    // Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Button(
        onClick = onNewDebtClick,
        modifier =
          Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("new_debt_button"),
        shape = RoundedCornerShape(16.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = HighDensityPrimary,
            contentColor = Color.White,
          ),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "New Debt",
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Debt",
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
        )
      }

      Button(
        onClick = onNewProjectClick,
        modifier =
          Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("new_project_button"),
        shape = RoundedCornerShape(16.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = HighDensityPrimaryContainer,
            contentColor = HighDensityOnPrimaryContainer,
          ),
      ) {
        Icon(
          imageVector = Icons.Default.AccountTree,
          contentDescription = "Project",
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Project",
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
        )
      }
    }
    }

    item {
    // Search Bar and Sort Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = debtSearchQuery,
        onValueChange = { debtSearchQuery = it },
        placeholder = { Text("Search debts, notes...", fontSize = 13.sp, color = HighDensityTextSecondary) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = HighDensityTextSecondary, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (debtSearchQuery.isNotEmpty()) {
            IconButton(onClick = { debtSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = HighDensityTextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = HighDensitySurface,
          unfocusedContainerColor = HighDensitySurface,
          focusedBorderColor = HighDensityPrimary,
          unfocusedBorderColor = HighDensityBorder,
          focusedTextColor = HighDensityTextPrimary,
          unfocusedTextColor = HighDensityTextPrimary,
        ),
        modifier = Modifier.weight(1f).testTag("debts_search_input"),
      )

      Box {
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
            .clickable { isSortMenuExpanded = true }
            .testTag("debts_sort_dropdown_button"),
          color = HighDensitySurface,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Sort,
              contentDescription = "Sort Debts",
              tint = HighDensityPrimary,
              modifier = Modifier.size(18.dp),
            )
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = null,
              tint = HighDensityTextSecondary,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        DropdownMenu(
          expanded = isSortMenuExpanded,
          onDismissRequest = { isSortMenuExpanded = false },
          modifier = Modifier.background(HighDensitySurface).border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp)),
        ) {
          DebtSortOption.values().forEach { option ->
            DropdownMenuItem(
              text = {
                Text(
                  text = option.label,
                  fontSize = 13.sp,
                  fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                  color = if (sortOption == option) HighDensityPrimary else HighDensityTextPrimary,
                )
              },
              onClick = {
                sortOption = option
                isSortMenuExpanded = false
              },
              modifier = Modifier.testTag("sort_option_${option.name}"),
            )
          }
        }
      }
    }
    }

    item {
    // Upcoming Reminders Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = if (debtSearchQuery.isNotBlank()) "Search Results (${sortedReminders.size})" else "Upcoming Reminders",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = HighDensityTextPrimary,
      )

      TextButton(
        onClick = onViewCalendarClick,
        modifier = Modifier.testTag("view_all_reminders_button"),
      ) {
        Text(
          text = "Calendar",
          color = HighDensityPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
    }

    if (reminders.isEmpty()) {
      item {
      HighDensityEmptyState(
        icon = Icons.Default.ReceiptLong,
        title = "No Debts or Reminders Yet",
        subtitle = "Keep track of money you owe or are owed with live balance calculations.",
        guideSteps = listOf(
          "Create a Debt" to "Tap 'New Debt' above to log loans, shared purchases, or subscriptions.",
          "Track & Settle" to "Record payments, toggle paid statuses, and export clean reports."
        ),
        actionLabel = "New Debt",
        onActionClick = onNewDebtClick,
        testTag = "home_empty_state",
      )
      }
    } else if (sortedReminders.isEmpty()) {
      item {
      HighDensityEmptyState(
        icon = Icons.Default.Search,
        title = "No Matching Debts",
        subtitle = "No records match '$debtSearchQuery'. Check your search spelling or clear the filter.",
        guideSteps = listOf(
          "Search Suggestions" to "Search by debtor name, title, or reference notes.",
          "Reset Filter" to "Clear the search bar above to view all debt entries."
        ),
        actionLabel = "Clear Search",
        onActionClick = { debtSearchQuery = "" },
        testTag = "home_search_empty_state",
      )
      }
    } else {
      val cashCredits = sortedReminders.filter {
        !it.isPaid && (it.id.startsWith("credit_") || it.title.startsWith("Cash Credit", ignoreCase = true))
      }
      val cashPayments = sortedReminders.filter { ExportManager.isPaymentItem(it) }
      val activeDebts = sortedReminders.filter {
        !it.isPaid && !it.id.startsWith("credit_") && !it.title.startsWith("Cash Credit", ignoreCase = true) && !ExportManager.isPaymentItem(it)
      }
      val completedDebts = sortedReminders.filter {
        it.isPaid && !ExportManager.isPaymentItem(it)
      }

        // 1. Cash credits at the top
        if (cashCredits.isNotEmpty()) {
          item(key = "cash_credits_header") {
            Text(
              text = "Available Cash Credits",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = HighDensityOwedToYou,
              modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
          }
          items(cashCredits, key = { it.id }) { item ->
            ReminderCard(
              item = item,
              currencySymbol = currencySymbol,
              onClick = { onEditDebt(item) },
              onTogglePaid = { onTogglePaid(item) },
            )
          }
        }

        // 2. Expandable Payments Record at the top
        if (cashPayments.isNotEmpty()) {
          item(key = "payments_record_card") {
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
                .testTag("home_payments_record_toggle"),
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
                        text = "${cashPayments.size} recorded",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighDensityPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                      text = if (isPaymentsRecordExpanded) "Collapse" else "View All",
                      fontSize = 11.sp,
                      color = HighDensityTextSecondary,
                    )
                    Icon(
                      imageVector = if (isPaymentsRecordExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                      contentDescription = if (isPaymentsRecordExpanded) "Collapse" else "Expand",
                      tint = HighDensityTextSecondary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                }

                if (isPaymentsRecordExpanded) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cashPayments.forEach { payment ->
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
                          }
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                          ) {
                            Text(
                              text = (if (payment.isPositive) "+${currencySymbol}" else "-${currencySymbol}") + String.format(Locale.US, "%.2f", payment.amount),
                              fontSize = 13.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (payment.isPositive) HighDensityOwedToYou else HighDensityYouOwe,
                            )
                            IconButton(
                              onClick = { onDeleteDebt(payment.id) },
                              modifier = Modifier.size(24.dp).testTag("home_delete_payment_${payment.id}"),
                            ) {
                              Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete payment",
                                tint = HighDensityTextSecondary,
                                modifier = Modifier.size(14.dp),
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
        }

        // 3. Active Debts
        if (activeDebts.isNotEmpty()) {
          items(activeDebts, key = { it.id }) { item ->
            ReminderCard(
              item = item,
              currencySymbol = currencySymbol,
              onClick = { onEditDebt(item) },
              onTogglePaid = { onTogglePaid(item) },
            )
          }
        } else if (cashCredits.isEmpty() && cashPayments.isEmpty() && completedDebts.isNotEmpty()) {
          item(key = "no_active_debts_msg") {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HighDensityOwedToYou.copy(alpha = 0.08f))
                .border(1.dp, HighDensityOwedToYou.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "All debts are paid off! See completed debts below.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = HighDensityOwedToYou,
              )
            }
          }
        }

        // 4. Expandable Completed Debts section below all debts and cash payments
        if (completedDebts.isNotEmpty()) {
          item(key = "completed_debts_expandable_section") {
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
                .testTag("home_completed_debts_toggle"),
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
                        text = "${completedDebts.size} settled • ${currencySymbol}${String.format(Locale.US, "%.2f", completedDebts.sumOf { it.amount })}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighDensityOwedToYou,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    completedDebts.forEach { item ->
                      ReminderCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onClick = { onEditDebt(item) },
                        onTogglePaid = { onTogglePaid(item) },
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

// -------------------------------------------------------------
// Reminder Card Component
// -------------------------------------------------------------
@Composable
fun ReminderCard(
  item: ReminderItem,
  currencySymbol: String = "$",
  onClick: () -> Unit = {},
  onTogglePaid: () -> Unit,
) {
  val iconVector: ImageVector =
    when (item.iconType) {
      "subscription" -> Icons.Default.Subscriptions
      "project" -> Icons.Default.AccountTree
      else -> Icons.Default.Person
    }

  val iconBgColor: Color =
    when (item.iconType) {
      "subscription" -> HighDensityRedBadgeBg
      "project" -> HighDensityBlueBadgeBg
      else -> HighDensitySurface
    }

  val iconTintColor: Color =
    when (item.iconType) {
      "subscription" -> HighDensityRedBadgeText
      "project" -> HighDensityBlueBadgeText
      else -> HighDensityTextPrimary
    }

  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
        .clickable { onClick() }
        .testTag("reminder_item_${item.id}"),
    color = HighDensityCard,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(iconBgColor),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconTintColor,
            modifier = Modifier.size(20.dp),
          )
        }

        Column {
          Text(
            text = item.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = HighDensityTextPrimary,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = item.relativeDateText,
              fontSize = 12.sp,
              color = if (item.isPaid) HighDensityOwedToYou else HighDensityYouOwe,
              fontWeight = FontWeight.Medium,
            )
            Text(
              text = "•",
              fontSize = 10.sp,
              color = HighDensityTextSecondary,
            )
            Text(
              text = item.secondaryText,
              fontSize = 12.sp,
              color = HighDensityTextSecondary,
            )
          }
        }
      }

      Column(
        horizontalAlignment = Alignment.End,
      ) {
        Text(
          text = "${currencySymbol}${String.format("%.2f", item.amount)}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (item.isPaid) HighDensityOwedToYou else HighDensityYouOwe,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                if (item.isPaid) HighDensityOwedToYou.copy(alpha = 0.12f)
                else HighDensityBorder.copy(alpha = 0.5f),
              )
              .clickable { onTogglePaid() }
              .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
          Text(
            text = if (item.isPaid) "Paid ✓" else "Mark Paid",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (item.isPaid) HighDensityOwedToYou else HighDensityTextSecondary,
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Projects Screen Content (With Search, Empty State & Currency)
// -------------------------------------------------------------
@Composable
fun ProjectsScreenContent(
  projects: List<ProjectRecord>,
  currencySymbol: String = "$",
  onNewProjectClick: () -> Unit,
  onEditProject: (ProjectRecord) -> Unit,
) {
  var projectSearchQuery by remember { mutableStateOf("") }

  val filteredProjects = projects.filter { proj ->
    if (projectSearchQuery.isBlank()) true
    else {
      proj.name.contains(projectSearchQuery, ignoreCase = true) ||
        proj.lineItems.any { it.title.contains(projectSearchQuery, ignoreCase = true) || it.participantName.contains(projectSearchQuery, ignoreCase = true) }
    }
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Shared Projects",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
        )
        Text(
          text = "Cost sharing & live contributions",
          fontSize = 12.sp,
          color = HighDensityTextSecondary,
        )
      }

      Button(
        onClick = onNewProjectClick,
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("New Project", fontSize = 12.sp)
      }
    }

    // Projects Search Bar
    OutlinedTextField(
      value = projectSearchQuery,
      onValueChange = { projectSearchQuery = it },
      placeholder = { Text("Search projects, line items...", fontSize = 13.sp, color = HighDensityTextSecondary) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = HighDensityTextSecondary, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (projectSearchQuery.isNotEmpty()) {
          IconButton(onClick = { projectSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Clear", tint = HighDensityTextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = HighDensitySurface,
        unfocusedContainerColor = HighDensitySurface,
        focusedBorderColor = HighDensityPrimary,
        unfocusedBorderColor = HighDensityBorder,
        focusedTextColor = HighDensityTextPrimary,
        unfocusedTextColor = HighDensityTextPrimary,
      ),
      modifier = Modifier.fillMaxWidth().testTag("projects_search_input"),
    )

    if (projects.isEmpty()) {
      HighDensityEmptyState(
        icon = Icons.Default.AccountTree,
        title = "No Shared Projects Yet",
        subtitle = "Track group purchases, split contractor expenses, and monitor profit margins.",
        guideSteps = listOf(
          "Create a Project" to "Set a project name, total estimated budget, and itemized lines.",
          "Split & Calculate" to "Allocate costs to friends and compute profit margins when sold."
        ),
        actionLabel = "New Project",
        onActionClick = onNewProjectClick,
        testTag = "projects_empty_state",
      )
    } else if (filteredProjects.isEmpty()) {
      HighDensityEmptyState(
        icon = Icons.Default.Search,
        title = "No Matching Projects",
        subtitle = "No projects match '$projectSearchQuery'. Check your search spelling or clear the filter.",
        guideSteps = listOf(
          "Search Suggestions" to "Search by project title, line item, or participant name.",
          "Reset Filter" to "Clear the search bar above to view all projects."
        ),
        actionLabel = "Clear Search",
        onActionClick = { projectSearchQuery = "" },
        testTag = "projects_search_empty_state",
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(filteredProjects, key = { it.id }) { proj ->
          val totalSpent = proj.lineItems.sumOf { it.amount }

          Card(
            modifier =
              Modifier
                .fillMaxWidth()
                .testTag("project_item_${proj.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder(),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = proj.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = HighDensityTextPrimary,
                    )
                    if (proj.isSold) {
                      Box(
                        modifier =
                          Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HighDensityOwedToYou.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                      ) {
                        Text(
                          text = "SOLD",
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = HighDensityOwedToYou,
                        )
                      }
                    }
                  }

                  Text(
                    text = "Updated ${proj.relativeTime} • ${proj.lineItems.size} line items",
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                  )
                }

                // Edit Icon Button to open Manage Project Screen
                IconButton(
                  onClick = { onEditProject(proj) },
                  modifier =
                    Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(HighDensitySurface)
                      .testTag("edit_project_${proj.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Manage Project",
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "Spent: ${currencySymbol}${String.format("%,.2f", totalSpent)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextPrimary,
                )
                Text(
                  text = "Budget: ${currencySymbol}${String.format("%,.2f", proj.totalEstimatedBudget)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityPrimary,
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Line items sample
              proj.lineItems.take(2).forEach { item ->
                Text(
                  text = "• ${item.participantName}: ${currencySymbol}${String.format("%.2f", item.amount)} (${item.contributionType})",
                  fontSize = 11.sp,
                  color = HighDensityTextSecondary,
                  modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                )
              }

              if (proj.isSold) {
                Spacer(modifier = Modifier.height(8.dp))
                val salePrice = proj.salePrice ?: 0.0
                val netProfit = salePrice - totalSpent
                val margin = if (salePrice > 0.0) (netProfit / salePrice) * 100.0 else 0.0
                val participantNames = (proj.lineItems.map { it.participantName } + "You (Owner)").map { if (it.startsWith("You", ignoreCase = true)) "You" else it }.distinct()
                val participantCount = maxOf(1, participantNames.size)
                val personalProfit = netProfit / participantCount

                Box(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(HighDensitySurface)
                      .padding(8.dp),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(text = "Personal Profit", fontSize = 10.sp, color = HighDensityTextSecondary)
                      Text(
                        text = (if (personalProfit >= 0) "+${currencySymbol}" else "-${currencySymbol}") + String.format("%,.2f", Math.abs(personalProfit)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (personalProfit >= 0) HighDensityOwedToYou else HighDensityYouOwe,
                      )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(text = "Net Profit", fontSize = 10.sp, color = HighDensityTextSecondary)
                      Text(
                        text = (if (netProfit >= 0) "+${currencySymbol}" else "-${currencySymbol}") + String.format("%,.2f", Math.abs(netProfit)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netProfit >= 0) HighDensityOwedToYou else HighDensityYouOwe,
                      )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                      Text(text = "Project Margin", fontSize = 10.sp, color = HighDensityTextSecondary)
                      Text(
                        text = "${String.format("%.1f", margin)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary,
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
}

// -------------------------------------------------------------
// Audit Log Screen View
// -------------------------------------------------------------
@Composable
fun AuditLogScreenContent(
  logs: List<AuditLogEntry>,
  onUndoLog: (AuditLogEntry) -> Unit = {},
  onRedoLog: (AuditLogEntry) -> Unit = {},
) {
  var searchQuery by remember { mutableStateOf("") }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column {
      Text(
        text = "Audit Log & History",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = HighDensityTextPrimary,
      )
      Text(
        text = "Complete immutable activity ledger",
        fontSize = 12.sp,
        color = HighDensityTextSecondary,
      )
    }

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth().testTag("audit_search_input"),
      placeholder = { Text("Search logs...") },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityTextSecondary)
      },
      singleLine = true,
    )

    val filtered =
      if (searchQuery.isBlank()) logs
      else logs.filter { it.narrative.contains(searchQuery, ignoreCase = true) }

    if (filtered.isEmpty()) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(HighDensityCard)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
            .padding(24.dp)
            .testTag("audit_empty_state"),
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
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = HighDensityPrimary,
              modifier = Modifier.size(32.dp),
            )
          }
          Text(
            text = if (searchQuery.isBlank()) "No audit logs yet" else "No matching log entries",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )
          Text(
            text = if (searchQuery.isBlank()) "Any transaction changes, expense edits, or payment records will appear in this immutable activity ledger." else "Try adjusting your search keywords to find logged entries.",
            fontSize = 13.sp,
            color = HighDensityTextSecondary,
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(filtered, key = { it.id }) { log ->
          Surface(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp)),
            color = HighDensityCard,
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                  text = log.narrative,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = HighDensityTextPrimary,
                )
                Text(
                  text = log.relativeTime,
                  fontSize = 10.sp,
                  color = HighDensityTextSecondary,
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = log.amountText,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityPrimary,
                )

                if (log.isUndone) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HighDensityPrimaryContainer,
                    modifier =
                      Modifier
                        .clickable { onRedoLog(log) }
                        .testTag("redo_button_${log.id}"),
                  ) {
                    Text(
                      text = "Undone (Tap to Restore)",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = HighDensityPrimary,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                  }
                } else {
                  OutlinedButton(
                    onClick = { onUndoLog(log) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("undo_button_${log.id}"),
                  ) {
                    Icon(
                      Icons.AutoMirrored.Filled.Undo,
                      contentDescription = "Undo",
                      modifier = Modifier.size(12.dp),
                      tint = HighDensityPrimary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Undo",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = HighDensityPrimary,
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

// -------------------------------------------------------------
// High Density Bottom Navigation Component
// -------------------------------------------------------------
@Composable
fun HighDensityBottomNav(
  selectedTab: NavTab,
  onTabSelected: (NavTab) -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
    color = HighDensityBackground,
    tonalElevation = 8.dp,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            color = HighDensityBorder,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
          )
          .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      NavTab.values().forEach { tab ->
        val isSelected = selectedTab == tab
        val label =
          when (tab) {
            NavTab.HOME -> "Home"
            NavTab.PEOPLE -> "People"
            NavTab.PROJECTS -> "Projects"
            NavTab.CALENDAR -> "Calendar"
            NavTab.LOG -> "Log"
          }
        val icon =
          when (tab) {
            NavTab.HOME -> Icons.Default.AccountBalanceWallet
            NavTab.PEOPLE -> Icons.Default.Group
            NavTab.PROJECTS -> Icons.Default.AccountTree
            NavTab.CALENDAR -> Icons.Default.CalendarToday
            NavTab.LOG -> Icons.Default.History
          }

        Column(
          modifier =
            Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onTabSelected(tab) }
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("nav_tab_${tab.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) HighDensityPrimaryContainer else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = icon,
              contentDescription = label,
              tint = if (isSelected) HighDensityOnPrimaryContainer else HighDensityTextSecondary,
              modifier = Modifier.size(20.dp),
            )
          }
          Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
    }
  }
}
