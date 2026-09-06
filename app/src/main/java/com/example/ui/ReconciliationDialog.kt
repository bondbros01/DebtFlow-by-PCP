package com.example.ui

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.model.ReminderItem
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCard
import com.example.ui.theme.HighDensityOwedToYou
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.HighDensityYouOwe
import com.example.util.ExportManager
import java.util.Locale

@Composable
fun ReconciliationDialog(
  reminders: List<ReminderItem>,
  userName: String = "User",
  targetPerson: String? = null,
  onDismiss: () -> Unit,
  onApplyReconciliation: (List<ReminderItem>, String) -> Unit,
) {
  val context = LocalContext.current
  val audit = remember(reminders, targetPerson) {
    ExportManager.auditReconciliation(reminders, targetPerson)
  }

  var isReconciling by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .fillMaxWidth(0.95f)
          .widthIn(max = 540.dp)
          .clip(RoundedCornerShape(20.dp))
          .border(1.dp, HighDensityBorder, RoundedCornerShape(20.dp))
          .testTag("reconciliation_dialog"),
      color = HighDensityBackground,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(20.dp),
      ) {
        // Drag Indicator Handle
        Box(
          modifier =
            Modifier
              .width(36.dp)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(HighDensityTextSecondary.copy(alpha = 0.4f))
              .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (audit.isBalanced) HighDensityOwedToYou.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (audit.isBalanced) Icons.Default.CheckCircle else Icons.Default.Sync,
                contentDescription = null,
                tint = if (audit.isBalanced) HighDensityOwedToYou else Color(0xFFF59E0B),
                modifier = Modifier.size(22.dp),
              )
            }
            Column {
              Text(
                text = if (targetPerson != null) "Reconcile: $targetPerson" else "Payment Reconciliation Audit",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = HighDensityTextPrimary,
              )
              Text(
                text = if (audit.isBalanced) "100% Balanced • Exports Verified" else "Discrepancy Detected • Fix Available",
                fontSize = 11.sp,
                color = if (audit.isBalanced) HighDensityOwedToYou else Color(0xFFF59E0B),
                fontWeight = FontWeight.Medium,
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_reconciliation_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = HighDensityTextSecondary,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Scan Overview KPI Card
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, if (audit.isBalanced) HighDensityOwedToYou.copy(alpha = 0.3f) else Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
          color = HighDensityCard,
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "EXPORT INTEGRITY SCAN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HighDensityTextSecondary,
                letterSpacing = 0.5.sp,
              )
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (audit.isBalanced) HighDensityOwedToYou.copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
              ) {
                Text(
                  text = if (audit.isBalanced) "MATCHED 100%" else "NEEDS RECONCILING",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (audit.isBalanced) HighDensityOwedToYou else Color(0xFFF59E0B),
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              // Paid Debts Sum
              Column(modifier = Modifier.weight(1f)) {
                Text("Sum of Paid Items", fontSize = 11.sp, color = HighDensityTextSecondary)
                Text(
                  "$${String.format(Locale.US, "%.2f", audit.totalPaidDebtsAmount)}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityTextPrimary,
                )
                Text("${audit.totalPaidDebtsCount} marked paid", fontSize = 10.sp, color = HighDensityTextSecondary)
              }

              // Recorded Payments Sum
              Column(modifier = Modifier.weight(1f)) {
                Text("Payments Displayed", fontSize = 11.sp, color = HighDensityTextSecondary)
                Text(
                  "$${String.format(Locale.US, "%.2f", audit.totalPaymentsAmount)}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = HighDensityPrimary,
                )
                Text("${audit.totalPaymentsCount} cash/log entries", fontSize = 10.sp, color = HighDensityTextSecondary)
              }

              // Discrepancy
              Column(modifier = Modifier.weight(1f)) {
                Text("Variance", fontSize = 11.sp, color = HighDensityTextSecondary)
                val discSign = if (audit.discrepancy > 0) "+$" else if (audit.discrepancy < 0) "-$" else "$"
                Text(
                  "$discSign${String.format(Locale.US, "%.2f", Math.abs(audit.discrepancy))}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (audit.isBalanced) HighDensityOwedToYou else HighDensityYouOwe,
                )
                Text(if (audit.isBalanced) "Balanced" else "Difference", fontSize = 10.sp, color = HighDensityTextSecondary)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown List by Person
        Text(
          text = "CONTACT BREAKDOWNS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextSecondary,
          letterSpacing = 0.5.sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
          modifier =
            Modifier
              .fillMaxWidth()
              .weight(1f, fill = false)
              .height((audit.personBreakdowns.size.coerceAtMost(4) * 64).dp.coerceAtLeast(64.dp)),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (audit.personBreakdowns.isEmpty()) {
            item {
              Text(
                "No contact records to reconcile.",
                fontSize = 12.sp,
                color = HighDensityTextSecondary,
                modifier = Modifier.padding(vertical = 12.dp),
              )
            }
          } else {
            items(audit.personBreakdowns) { p ->
              Surface(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp)),
                color = HighDensitySurface,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = p.personName,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = HighDensityTextPrimary,
                    )
                    Text(
                      text = "Paid Items: $${String.format(Locale.US, "%.2f", p.paidDebtsAmount)} • Payments: $${String.format(Locale.US, "%.2f", p.paymentsAmount)}",
                      fontSize = 11.sp,
                      color = HighDensityTextSecondary,
                    )
                  }

                  Box(
                    modifier =
                      Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (p.isBalanced) HighDensityOwedToYou.copy(alpha = 0.15f) else HighDensityYouOwe.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                  ) {
                    val pSign = if (p.discrepancy > 0) "+$" else if (p.discrepancy < 0) "-$" else "$"
                    Text(
                      text = if (p.isBalanced) "Balanced ✓" else "$pSign${String.format(Locale.US, "%.2f", Math.abs(p.discrepancy))}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (p.isBalanced) HighDensityOwedToYou else HighDensityYouOwe,
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action: Reconcile Differences Button
        if (!audit.isBalanced || audit.unlinkedPaidDebts.isNotEmpty()) {
          Button(
            onClick = {
              val (reconciledDebts, narrative) = ExportManager.reconcileDebtsAndPayments(reminders, targetPerson, userName)
              onApplyReconciliation(reconciledDebts, narrative)
            },
            modifier =
              Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reconcile_differences_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
          ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reconcile All Differences", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Export Statements
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              ExportManager.exportReconciliationReportCsv(context, reminders, userName)
            },
            modifier =
              Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("export_reconcile_csv_btn"),
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Audit CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensityPrimary)
          }

          Button(
            onClick = {
              ExportManager.exportReconciliationReportPdf(context, reminders, userName)
            },
            modifier =
              Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("export_reconcile_pdf_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensitySurface),
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = HighDensityTextPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Audit PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensityTextPrimary)
          }
        }
      }
    }
  }
}
