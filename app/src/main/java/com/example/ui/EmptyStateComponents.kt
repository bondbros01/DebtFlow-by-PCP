package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCard
import com.example.ui.theme.HighDensityOnPrimaryContainer
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary

@Composable
fun HighDensityEmptyState(
  icon: ImageVector,
  title: String,
  subtitle: String,
  guideSteps: List<Pair<String, String>> = emptyList(),
  actionLabel: String? = null,
  actionIcon: ImageVector? = Icons.Default.Add,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  testTag: String = "empty_state_view",
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Illustrated Header Badge with concentric glow rings
      Box(
        modifier = Modifier.size(76.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.size(76.dp)) {
          val center = Offset(size.width / 2, size.height / 2)
          // Outer subtle pulse ring
          drawCircle(
            color = Color(0xFF6366F1).copy(alpha = 0.08f),
            radius = size.minDimension / 2,
            center = center,
          )
          // Inner dashed guideline ring
          drawCircle(
            color = Color(0xFF6366F1).copy(alpha = 0.25f),
            radius = size.minDimension / 2 - 8f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
              width = 2f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
            ),
          )
        }

        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(
                  HighDensityPrimaryContainer,
                  HighDensityPrimary.copy(alpha = 0.35f),
                )
              )
            )
            .border(1.5.dp, HighDensityPrimary.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HighDensityPrimary,
            modifier = Modifier.size(26.dp),
          )
        }
      }

      // Title & Description
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = HighDensityTextPrimary,
          textAlign = TextAlign.Center,
        )
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = HighDensityTextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 16.sp,
          modifier = Modifier.padding(horizontal = 8.dp),
        )
      }

      // Instructional Step-by-Step Guide
      if (guideSteps.isNotEmpty()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp)),
          color = HighDensityCard,
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            guideSteps.forEachIndexed { index, step ->
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(HighDensityPrimaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = "${index + 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityOnPrimaryContainer,
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(
                    text = step.first,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensityTextPrimary,
                  )
                  Text(
                    text = step.second,
                    fontSize = 11.sp,
                    color = HighDensityTextSecondary,
                    lineHeight = 14.sp,
                  )
                }
              }
            }
          }
        }
      }

      // Action Button
      if (actionLabel != null && onActionClick != null) {
        Button(
          onClick = onActionClick,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
          modifier = Modifier.testTag("${testTag}_action_btn"),
        ) {
          if (actionIcon != null) {
            Icon(actionIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
          }
          Text(
            text = actionLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}
