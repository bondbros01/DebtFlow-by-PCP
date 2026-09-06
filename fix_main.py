with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add debtToEdit
content = content.replace(
    "var showNewDebtDialog by remember { mutableStateOf(false) }",
    "var showNewDebtDialog by remember { mutableStateOf(false) }\n  var debtToEdit by remember { mutableStateOf<ReminderItem?>(null) }"
)

# Update CalendarScreenContent
old_cal = """        NavTab.CALENDAR -> {
          CalendarScreenContent(
            reminders = reminders,
            people = people,
            isActualCalendarView = isActualCalendarView,
            onToggleView = { isActualCalendarView = it },
            onOpenSettings = { showSettingsDialog = true },
            onBackToDashboard = { selectedTab = NavTab.HOME },
          )
        }"""

new_cal = """        NavTab.CALENDAR -> {
          CalendarScreenContent(
            reminders = reminders,
            people = people,
            isActualCalendarView = isActualCalendarView,
            onToggleView = { isActualCalendarView = it },
            onOpenSettings = { showSettingsDialog = true },
            onBackToDashboard = { selectedTab = NavTab.HOME },
            onEditDebt = { debtToEdit = it }
          )
        }"""

content = content.replace(old_cal, new_cal)

# Add EditDebtDialog in MainActivity
edit_debt_dialog = """
  // Edit Debt Dialog (Global)
  debtToEdit?.let { debt ->
    EditDebtDialog(
      debt = debt,
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
"""

# add it before `if (showNewDebtDialog) {`
content = content.replace("  // Redesigned Dialogs\n  if (showNewDebtDialog) {", edit_debt_dialog + "\n  // Redesigned Dialogs\n  if (showNewDebtDialog) {")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
