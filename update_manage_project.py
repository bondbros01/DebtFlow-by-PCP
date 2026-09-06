import re

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'r') as f:
    content = f.read()

# Modify ExpenseFormDialog
# We need to add "expenseTitle" field.
expense_dialog_new = """@Composable
fun ExpenseFormDialog(
  people: List<PersonRecord>,
  title: String,
  initialItem: ExpenseLineItem?,
  onDismiss: () -> Unit,
  onConfirm: (ExpenseLineItem) -> Unit,
) {
  var participant by remember { mutableStateOf(initialItem?.participantName ?: "You") }
  var expenseTitle by remember { mutableStateOf(initialItem?.title ?: "") }
  var amountText by remember { mutableStateOf(initialItem?.let { "${it.amount}" } ?: "") }
  var selectedType by remember { mutableStateOf(initialItem?.contributionType ?: "Cash") }
  var relativeDate by remember { mutableStateOf(initialItem?.relativeDate ?: "Sep 04, 2026") }

  val types = listOf("Cash", "Goods", "Services")
  val contributorList = (listOf("You") + people.map { it.name }).distinct()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = 440.dp)
          .clip(RoundedCornerShape(28.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(28.dp))
          .testTag("expense_form_dialog"),
      color = HighDensitySurface,
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Box(modifier = Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(HighDensityBorder))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HighDensityTextPrimary)
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighDensityTextSecondary)
          }
        }

        OutlinedTextField(
          value = expenseTitle,
          onValueChange = { expenseTitle = it },
          label = { Text("Expense Title / Label") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
        )

        Text(
          text = "Select Contributor",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(contributorList) { p ->
             FilterChip(
              selected = participant.equals(p, ignoreCase = true),
              onClick = { participant = p },
              label = { Text(p, fontSize = 12.sp) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HighDensityPrimaryContainer,
                  selectedLabelColor = HighDensityTextPrimary,
                ),
            )
          }
        }

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Amount ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
        )

        Text(
          text = "Contribution Type",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          types.forEach { t ->
            FilterChip(
              selected = selectedType.equals(t, ignoreCase = true),
              onClick = { selectedType = t },
              label = { Text(t, fontSize = 12.sp) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HighDensityPrimaryContainer,
                  selectedLabelColor = HighDensityTextPrimary,
                ),
            )
          }
        }

        OutlinedTextField(
          value = relativeDate,
          onValueChange = { relativeDate = it },
          label = { Text("Date (e.g., Sep 04, 2026)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text("Cancel", color = HighDensityTextSecondary)
          }
          Button(
            onClick = {
              val amt = amountText.toDoubleOrNull() ?: 0.0
              if (amt > 0.0) {
                onConfirm(
                  ExpenseLineItem(
                    id = initialItem?.id ?: "exp_${System.currentTimeMillis()}",
                    participantName = participant,
                    title = expenseTitle.trim(),
                    contributionType = selectedType,
                    amount = amt,
                    relativeDate = relativeDate,
                  ),
                )
              }
            },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
          ) {
            Text("Save", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}"""

# regex match ExpenseFormDialog until RecordProjectSalePromptDialog
content = re.sub(r'@Composable\s+fun ExpenseFormDialog\(.*?\}\s*\n\s*\n@Composable\s+fun RecordProjectSalePromptDialog', expense_dialog_new + '\n\n@Composable\nfun RecordProjectSalePromptDialog', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'w') as f:
    f.write(content)

