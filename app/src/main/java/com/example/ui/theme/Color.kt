package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkMode = compositionLocalOf { true }

// High Density Sleek Adaptive Palette (Dark by default, togglable to Light via Settings)
val HighDensityBackground: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF0F0F12) else Color(0xFFF3F4F6)

val HighDensitySurface: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF18181D) else Color(0xFFFFFFFF)

val HighDensityCard: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF22222A) else Color(0xFFFFFFFF)

val HighDensitySurfaceVariant: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF2A2A35) else Color(0xFFE5E7EB)

val HighDensityBorder: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF333340) else Color(0xFFD1D5DB)

val HighDensityTextPrimary: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFF4F4F6) else Color(0xFF111827)

val HighDensityTextSecondary: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFA1A1AA) else Color(0xFF6B7280)

val HighDensityPrimary: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFA78BFA) else Color(0xFF7C3AED)

val HighDensityPrimaryContainer: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF382A59) else Color(0xFFEDE9FE)

val HighDensityOnPrimaryContainer: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFEDE9FE) else Color(0xFF5B21B6)

val HighDensityOwedToYou: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF34D399) else Color(0xFF059669)

val HighDensityYouOwe: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFF87171) else Color(0xFFDC2626)

val HighDensityRedBadgeBg: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF3D1F24) else Color(0xFFFEE2E2)

val HighDensityRedBadgeText: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFCA5A5) else Color(0xFF991B1B)

val HighDensityBlueBadgeBg: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF1E293B) else Color(0xFFDBEAFE)

val HighDensityBlueBadgeText: Color
  @Composable get() = if (LocalIsDarkMode.current) Color(0xFF93C5FD) else Color(0xFF1E40AF)

