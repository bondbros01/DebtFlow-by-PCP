import re

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'r') as f:
    content = f.read()

# Replace RecordProjectSalePromptDialog
record_sale_new = """@Composable
fun RecordProjectSalePromptDialog(
  project: ProjectRecord,
  totalSpent: Double,
  onDismiss: () -> Unit,
  onConfirm: (salePrice: Double, summary: String) -> Unit,
) {
  var salePriceText by remember { mutableStateOf("") }
  var isUnevenSplit by remember { mutableStateOf(false) }

  val participants =
    project.lineItems.map { it.participantName }.distinct().ifEmpty { listOf("You", "Alex Green") }
  val count = participants.size.coerceAtLeast(1)

  val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
  val customSplits = remember { mutableStateMapOf<String, String>() }

  participants.forEach { p ->
    if (!customSplits.containsKey(p)) {
      customSplits[p] = String.format(Locale.US, "%.2f", salePrice / count)
    }
  }
  
  val quickAmounts = listOf(100, 500, 1000, 2500)

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
          .testTag("record_project_sale_dialog"),
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
          Text("Record Project Sale", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HighDensityTextPrimary)
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = HighDensityTextSecondary)
          }
        }

        Text(
          text = "Selling: ${project.name}",
          fontSize = 13.sp,
          color = HighDensityTextSecondary,
        )

        // Hero Amount Card with Quick Increments (Same style as Debt entry)
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
              text = "SALE PRICE",
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
                color = HighDensityOwedToYou,
              )
              Spacer(modifier = Modifier.width(6.dp))
              OutlinedTextField(
                value = salePriceText,
                onValueChange = { 
                  salePriceText = it.filter { ch -> ch.isDigit() || ch == '.' }
                  val newTotal = salePriceText.toDoubleOrNull() ?: 0.0
                  if (!isUnevenSplit) {
                    val perPerson = newTotal / count
                    participants.forEach { p ->
                      customSplits[p] = String.format(Locale.US, "%.2f", perPerson)
                    }
                  }
                },
                placeholder = { Text("0.00", fontSize = 28.sp, color = HighDensityTextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(180.dp),
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
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
                        val current = salePriceText.toDoubleOrNull() ?: 0.0
                        val newTotal = current + amt
                        salePriceText = String.format(Locale.US, "%.2f", newTotal)
                        if (!isUnevenSplit) {
                          val perPerson = newTotal / count
                          participants.forEach { p ->
                            customSplits[p] = String.format(Locale.US, "%.2f", perPerson)
                          }
                        }
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
                    .clickable { 
                      salePriceText = "" 
                      if (!isUnevenSplit) {
                        participants.forEach { p ->
                          customSplits[p] = "0.00"
                        }
                      }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
              ) {
                Text(
                  text = "Clear",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextSecondary,
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Split Mode",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = !isUnevenSplit,
              onClick = {
                isUnevenSplit = false
                val perPerson = salePrice / count
                participants.forEach { p ->
                  customSplits[p] = String.format(Locale.US, "%.2f", perPerson)
                }
              },
              label = { Text("Even Split", fontSize = 11.sp) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HighDensityPrimaryContainer,
                  selectedLabelColor = HighDensityTextPrimary,
                ),
            )
            FilterChip(
              selected = isUnevenSplit,
              onClick = { isUnevenSplit = true },
              label = { Text("Uneven / Custom", fontSize = 11.sp) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = HighDensityPrimaryContainer,
                  selectedLabelColor = HighDensityTextPrimary,
                ),
            )
          }
        }

        Text(
          text = "Proceeds & Balances Breakdown:",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
          modifier = Modifier.padding(top = 4.dp),
        )

        participants.forEach { participant ->
          val contributed = project.lineItems.filter { it.participantName == participant }.sumOf { it.amount }
          val splitAmtStr = customSplits[participant] ?: "0.00"
          val splitAmt = splitAmtStr.toDoubleOrNull() ?: 0.0
          val net = splitAmt - contributed

          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder(),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(participant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val netColor = if (net >= 0) HighDensityOwedToYou else HighDensityYouOwe
                val netPrefix = if (net >= 0) "+" else ""
                Text("Net: $netPrefix$${String.format(Locale.US, "%.2f", net)}", color = netColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  "Contributed: $${String.format(Locale.US, "%.2f", contributed)} • Share: $${String.format(Locale.US, "%.2f", splitAmt)}",
                  fontSize = 11.sp,
                  color = HighDensityTextSecondary,
                )
              }

              if (isUnevenSplit) {
                OutlinedTextField(
                  value = splitAmtStr,
                  onValueChange = {
                    customSplits[participant] = it.filter { ch -> ch.isDigit() || ch == '.' }
                  },
                  label = { Text("Custom Share ($)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                  singleLine = true,
                  shape = RoundedCornerShape(8.dp),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
              val summary =
                participants.joinToString(", ") { p ->
                  "$p share: $${customSplits[p] ?: "0.00"}"
                }
              onConfirm(salePrice, summary)
            },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
          ) {
            Text("Confirm Sale", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}"""

content = re.sub(r'@Composable\s+fun RecordProjectSalePromptDialog\(.*?\}\s*\n\s*\}\s*\n\}', record_sale_new, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'w') as f:
    f.write(content)

