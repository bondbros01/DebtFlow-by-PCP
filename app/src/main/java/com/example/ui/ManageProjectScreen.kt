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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ExpenseLineItem
import com.example.model.ProjectRecord
import com.example.model.PersonRecord
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCard
import com.example.ui.theme.HighDensityOwedToYou
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensityRedBadgeBg
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.HighDensityYouOwe
import java.util.Locale

@Composable
fun ManageProjectScreen(
  people: List<PersonRecord> = emptyList(),
  project: ProjectRecord,
  onClose: () -> Unit,
  onAddExpense: (ExpenseLineItem) -> Unit,
  onEditExpense: (ExpenseLineItem) -> Unit,
  onDeleteExpense: (String) -> Unit,
  onRecordSale: (salePrice: Double, liquidationSummary: String) -> Unit,
  onExportProject: () -> Unit = {},
  onExportProjectPdf: () -> Unit = {},
  onRevertSale: () -> Unit = {},
  onDeleteProject: () -> Unit = {},
) {
  var showAddExpenseDialog by remember { mutableStateOf(false) }
  var expenseToEdit by remember { mutableStateOf<ExpenseLineItem?>(null) }
  var showSalePromptDialog by remember { mutableStateOf(false) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  BackHandler {
    onClose()
  }

  val totalSpent = project.lineItems.sumOf { it.amount }
  val budgetProgress =
    if (project.totalEstimatedBudget > 0.0) {
      (totalSpent / project.totalEstimatedBudget).toFloat().coerceIn(0f, 1f)
    } else {
      0f
    }
  val remainingBudget = project.totalEstimatedBudget - totalSpent

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = HighDensityBackground,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top Bar with properly spaced Delete, Export, and Close icons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Manage Project",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HighDensityTextPrimary,
          )
          Text(
            text = project.name,
            fontSize = 13.sp,
            color = HighDensityTextSecondary,
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Delete Project Button
          IconButton(
            onClick = { showDeleteConfirmDialog = true },
            modifier =
              Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(HighDensityRedBadgeBg)
                .testTag("delete_project_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Project",
              tint = HighDensityYouOwe,
              modifier = Modifier.size(18.dp),
            )
          }
          
          // Close Button
          IconButton(
            onClick = onClose,
            modifier =
              Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(HighDensitySurface)
                .testTag("close_manage_project"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = HighDensityTextPrimary,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      // If Sold, show ROI card FIRST (before cost breakdown)
      if (project.isSold) {
        val salePrice = project.salePrice ?: 0.0
        val netProfit = salePrice - totalSpent
        val roi = if (totalSpent > 0.0) (netProfit / totalSpent) * 100.0 else 0.0

        Card(
          modifier = Modifier.fillMaxWidth().testTag("sold_profit_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
          border = CardDefaults.outlinedCardBorder(),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "PROJECT SOLD & PROFIT ANALYSIS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column {
                Text(text = "Sale Price", fontSize = 11.sp, color = HighDensityTextSecondary)
                Text(text = "$${String.format("%,.2f", salePrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "Net Profit", fontSize = 11.sp, color = HighDensityTextSecondary)
                Text(
                  text = (if (netProfit >= 0) "+$" else "-$") + String.format("%,.2f", Math.abs(netProfit)),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (netProfit >= 0) HighDensityOwedToYou else HighDensityYouOwe,
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = "Return on Investment (ROI):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensityTextSecondary)
              Text(
                text = "${String.format("%.1f", roi)}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (roi >= 0) HighDensityOwedToYou else HighDensityYouOwe,
              )
            }
          }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Export Project Section
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Download, contentDescription = "Export", tint = HighDensityPrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Text("Export Project", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HighDensityTextPrimary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = onExportProject,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
              ) {
                Text("Export CSV", fontSize = 12.sp, color = HighDensityPrimary)
              }
              Button(
                onClick = onExportProjectPdf,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp),
              ) {
                Text("Export PDF", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
              }
            }
          }
        }
      }

      // Budget & Status Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = "TOTAL EXPENDITURES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
              Text(
                text = "$${String.format("%,.2f", totalSpent)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextPrimary,
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "BUDGET: $${String.format("%,.2f", project.totalEstimatedBudget)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = HighDensityPrimary,
              )
              Text(
                text =
                  if (remainingBudget >= 0) {
                    "$${String.format("%,.2f", remainingBudget)} left"
                  } else {
                    "$${String.format("%,.2f", -remainingBudget)} over budget"
                  },
                fontSize = 11.sp,
                color = if (remainingBudget >= 0) HighDensityOwedToYou else HighDensityYouOwe,
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { budgetProgress },
            modifier =
              Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (remainingBudget >= 0) HighDensityPrimary else HighDensityYouOwe,
            trackColor = HighDensityBorder,
          )
        }
      }

      // Action Buttons Row (Add Expense & Record/View Sale)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = {
            expenseToEdit = null
            showAddExpenseDialog = true
          },
          modifier = Modifier.weight(1f).height(44.dp).testTag("add_expense_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("+ Add Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (!project.isSold) {
          Button(
            onClick = { showSalePromptDialog = true },
            modifier = Modifier.weight(1f).height(44.dp).testTag("record_sale_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimaryContainer),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityPrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Record Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
          }
        }
      }

      // Expenses List Header & Items
      Text(
        text = "Associated Costs & Expenses (${project.lineItems.size})",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = HighDensityTextPrimary,
      )

      if (project.lineItems.isEmpty()) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(HighDensityCard)
              .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
              .padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No expenses recorded yet. Tap '+ Add Expense' above.",
            fontSize = 12.sp,
            color = HighDensityTextSecondary,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(project.lineItems, key = { it.id }) { item ->
            ExpenseItemRow(
              item = item,
              onEdit = {
                expenseToEdit = item
                showAddExpenseDialog = true
              },
              onDelete = { onDeleteExpense(item.id) },
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (showAddExpenseDialog) {
    ExpenseFormDialog(
      people = people,
      title = if (expenseToEdit == null) "Add Project Expense" else "Edit Expense",
      initialItem = expenseToEdit,
      onDismiss = {
        showAddExpenseDialog = false
        expenseToEdit = null
      },
      onConfirm = { item ->
        if (expenseToEdit == null) {
          onAddExpense(item)
        } else {
          onEditExpense(item)
        }
        showAddExpenseDialog = false
        expenseToEdit = null
      },
    )
  }

  if (showSalePromptDialog) {
    RecordProjectSalePromptDialog(
      project = project,
      totalSpent = totalSpent,
      onDismiss = { showSalePromptDialog = false },
      onConfirm = { price, summary ->
        showSalePromptDialog = false
        onRecordSale(price, summary)
      },
    )
  }

  if (showDeleteConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmDialog = false },
      title = { Text("Delete Project?", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to delete '${project.name}'? This cannot be undone easily.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmDialog = false
            onDeleteProject()
          },
          colors = ButtonDefaults.buttonColors(containerColor = HighDensityYouOwe),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmDialog = false }) {
          Text("Cancel", color = HighDensityTextSecondary)
        }
      },
    )
  }
}

@Composable
fun ExpenseItemRow(
  item: ExpenseLineItem,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("expense_item_${item.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = HighDensityCard),
    border = CardDefaults.outlinedCardBorder(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        if (item.title.isNotBlank()) {
          Text(
            text = item.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "${item.participantName} • ${item.contributionType} • ${item.relativeDate}",
            fontSize = 11.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
          )
        } else {
          Text(
            text = item.participantName,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "${item.contributionType} • ${item.relativeDate}",
            fontSize = 11.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "$${String.format("%.2f", item.amount)}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityPrimary,
        )

        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(32.dp).testTag("edit_expense_${item.id}"),
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Expense",
            tint = HighDensityTextSecondary,
            modifier = Modifier.size(16.dp),
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp).testTag("delete_expense_${item.id}"),
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Expense",
            tint = HighDensityYouOwe,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
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

        // Hero Amount Card with Quick Increments (Same style as Record Sale screen)
        val expenseQuickAmounts = listOf(10, 25, 50, 100, 250)
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
              text = "EXPENSE AMOUNT",
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
                color = HighDensityYouOwe,
              )
              Spacer(modifier = Modifier.width(6.dp))
              OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
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
              expenseQuickAmounts.forEach { amt ->
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
                      .padding(horizontal = 8.dp, vertical = 6.dp),
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
                  fontWeight = FontWeight.SemiBold,
                  color = HighDensityTextSecondary,
                )
              }
            }
          }
        }

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
}

@Composable
fun RecordProjectSalePromptDialog(
  project: ProjectRecord,
  totalSpent: Double,
  onDismiss: () -> Unit,
  onConfirm: (salePrice: Double, summary: String) -> Unit,
) {
  var salePriceText by remember { mutableStateOf("") }
  var splitMode by remember { mutableStateOf("Fair") } // "Fair", "Even", "Custom"

  val participants =
    project.lineItems.map { it.participantName }.distinct().ifEmpty { listOf("You", "Alex Green") }
  val count = participants.size.coerceAtLeast(1)
  val totalContributed = project.lineItems.sumOf { it.amount }

  val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
  val customSplits = remember { mutableStateMapOf<String, String>() }

  fun updateSplits(mode: String, price: Double) {
    participants.forEach { p ->
      val contributedP = project.lineItems.filter { it.participantName.equals(p, ignoreCase = true) }.sumOf { it.amount }
      when (mode) {
        "Fair" -> {
          val payout = contributedP + (price - totalContributed) / count
          customSplits[p] = String.format(Locale.US, "%.2f", payout)
        }
        "Even" -> {
          val perPerson = price / count
          customSplits[p] = String.format(Locale.US, "%.2f", perPerson)
        }
      }
    }
  }

  participants.forEach { p ->
    if (!customSplits.containsKey(p)) {
      val contributedP = project.lineItems.filter { it.participantName.equals(p, ignoreCase = true) }.sumOf { it.amount }
      val payout = contributedP + (salePrice - totalContributed) / count
      customSplits[p] = String.format(Locale.US, "%.2f", payout)
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
                  updateSplits(splitMode, newTotal)
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
                        updateSplits(splitMode, newTotal)
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
                      updateSplits(splitMode, 0.0)
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
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
              FilterChip(
                selected = splitMode == "Fair",
                onClick = {
                  splitMode = "Fair"
                  updateSplits("Fair", salePrice)
                },
                label = { Text("Fair Split", fontSize = 11.sp) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HighDensityPrimaryContainer,
                    selectedLabelColor = HighDensityTextPrimary,
                  ),
              )
            }
            item {
              FilterChip(
                selected = splitMode == "Even",
                onClick = {
                  splitMode = "Even"
                  updateSplits("Even", salePrice)
                },
                label = { Text("Even Split", fontSize = 11.sp) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HighDensityPrimaryContainer,
                    selectedLabelColor = HighDensityTextPrimary,
                  ),
              )
            }
            item {
              FilterChip(
                selected = splitMode == "Custom",
                onClick = { splitMode = "Custom" },
                label = { Text("Custom", fontSize = 11.sp) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HighDensityPrimaryContainer,
                    selectedLabelColor = HighDensityTextPrimary,
                  ),
              )
            }
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

              if (splitMode == "Custom") {
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
}
