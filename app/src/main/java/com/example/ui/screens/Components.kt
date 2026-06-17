package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Translator to return appropriate premium Material Icons based on icon names
fun getIconForName(name: String): ImageVector {
    return when (name.lowercase()) {
        "instagram" -> Icons.Default.PhotoCamera
        "github" -> Icons.Default.Code
        "youtube" -> Icons.Default.PlayCircle
        "linkedin" -> Icons.Default.BusinessCenter
        "twitter" -> Icons.Default.AlternateEmail
        "email" -> Icons.Default.Email
        "link" -> Icons.Default.Link
        else -> Icons.Default.Language
    }
}

// Map string icon name to user-friendly label
fun getLabelForIcon(name: String): String {
    return when (name.lowercase()) {
        "instagram" -> "Instagram"
        "github" -> "GitHub"
        "youtube" -> "YouTube"
        "linkedin" -> "LinkedIn"
        "twitter" -> "X / Twitter"
        "email" -> "Email Client"
        "link" -> "Generic Link"
        else -> "Website URL"
    }
}

@Composable
fun LogoChain(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_1781710087944),
        contentDescription = "App Logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(size * 0.28f))
    )
}

@Composable
fun SaaSLogoInline(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoChain(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Admin",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Link Platform",
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
