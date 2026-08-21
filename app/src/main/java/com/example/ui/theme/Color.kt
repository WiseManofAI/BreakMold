package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Cyber-Neon BreakMold Palette
val NeonCyan = Color(0xFF00F5D4)
val NeonCyanGlow = Color(0xFF00E5FF)
val ElectricMagenta = Color(0xFFB5179E)
val ElectricPurple = Color(0xFF9D4EDD)
val NeonPink = Color(0xFFFF007F)
val SkyBlue = Color(0xFF38BDF8)
val AccentPurple = Color(0xFF7209B7)
val LavendarAccent = Color(0xFFE0AAFF)

// Dark Cyberpunk Backgrounds
val DarkCanvas = Color(0xFF0B0E14)
val DarkSurface = Color(0xFF131722)
val DarkSurfaceElevated = Color(0xFF1A2130)
val DarkSurfaceBorder = Color(0xFF263043)
val DarkPill = Color(0xFF1F2839)

// Status & Progress Colors
val SuccessTeal = Color(0xFF06D6A0)
val WarningAmber = Color(0xFFFFD166)
val DangerCoral = Color(0xFFEF476F)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Gradients
val BreakTheMoldGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF00F5D4),
        Color(0xFF38BDF8),
        Color(0xFF9D4EDD),
        Color(0xFFB5179E)
    )
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1E2536),
        Color(0xFF131722)
    )
)

val ActiveNavGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF7209B7),
        Color(0xFF560BAD)
    )
)
