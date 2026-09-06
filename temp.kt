fun EditDebtDialog(
  debt: ReminderItem,
  onDismiss: () -> Unit,
  onConfirm: (ReminderItem) -> Unit,
) {
  var title by remember { mutableStateOf(debt.title) }
  var amountText by remember { mutableStateOf(String.format("%.2f", debt.amount)) }
  var isPositive by remember { mutableStateOf(debt.isPositive) }
  var relativeSchedule by remember { mutableStateOf(debt.relativeDateText) }

  val relativeChoices = listOf("Due Today", "Due Tomorrow", "Later this week", "Next Week", "Later This Month")

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("edit_debt_dialog"),
    title = { Text("Edit Debt", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Amount ($)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
        )

        // Direction Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = isPositive,
            onClick = { isPositive = true },
            label = { Text("Owed to You") },
            modifier = Modifier.weight(1f),
          )
          FilterChip(
            selected = !isPositive,
            onClick = { isPositive = false },
            label = { Text("You Owe") },
            modifier = Modifier.weight(1f),
          )
        }

        // Relative Schedule
        Text("Relative Schedule", fontSize = 11.sp, color = HighDensityTextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(relativeChoices) { choice ->
            FilterChip(
              selected = relativeSchedule == choice,
              onClick = { relativeSchedule = choice },
              label = { Text(choice, fontSize = 10.sp) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: debt.amount
          if (title.isNotBlank() && amt > 0.0) {
            onConfirm(
              debt.copy(
                title = title.trim(),
                amount = amt,
                isPositive = isPositive,
                relativeDateText = relativeSchedule,
              ),
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
