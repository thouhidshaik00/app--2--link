package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Link
import com.example.data.model.User
import com.example.ui.theme.DesignPresets
import com.example.ui.viewmodel.PublicProfileUiState
import com.example.ui.viewmodel.SaaSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    username: String,
    viewModel: SaaSViewModel,
    onBackToLogin: () -> Unit
) {
    val profileState by viewModel.publicProfileState.collectAsState()
    val uriHandler = LocalUriHandler.current

    // Trigger profile search on component mounting
    LaunchedEffect(username) {
        viewModel.lookupPublicProfile(username)
    }

    when (val state = profileState) {
        is PublicProfileUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF09090B)), // Sophisticated dark background
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Fetching public directory...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }
        is PublicProfileUiState.Success -> {
            val user = state.user
            val activeLinks = state.links
            val theme = DesignPresets.getTheme(user.themeGradient)
            val layout = user.layoutStyle

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme.brush)
            ) {
                // Top control anchor to return to console log-in/admin easily
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 40.dp, start = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textColor.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Console", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Public Profile Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 96.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Render Rounded Avatar with high-quality styling
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Avatar profile",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(3.dp, theme.accentColor, CircleShape)
                                .testTag("public_avatar_image"),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(3.dp, theme.accentColor, CircleShape)
                                .testTag("public_avatar_fallback"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.username.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = theme.textColor,
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "@${user.username}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor,
                        modifier = Modifier.testTag("public_username")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user.bio,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.textColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("public_bio")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Public Interactive Buttons Stack
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .testTag("public_links_list"),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activeLinks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No active resource cards linked.",
                                        color = theme.textColor.copy(alpha = 0.4f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            items(activeLinks) { link ->
                                PublicActiveLinkButton(
                                    link = link,
                                    themeSetting = theme,
                                    layoutPreset = layout,
                                    onClick = {
                                        // Save click count update in Room
                                        viewModel.recordLinkClick(link.id)
                                        // Load external Browser URL
                                        try {
                                            uriHandler.openUri(link.url)
                                        } catch (e: Exception) {
                                            // Fallback helper to format address or open safely
                                            var fixedUrl = link.url.trim()
                                            if (!fixedUrl.startsWith("http://") && !fixedUrl.startsWith("https://")) {
                                                fixedUrl = "https://$fixedUrl"
                                            }
                                            uriHandler.openUri(fixedUrl)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Share Component
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.textColor.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Share:",
                                    color = theme.textColor.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Twitter",
                                    color = theme.textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://twitter.com/intent/tweet?url=https://app.com/${user.username}&text=Check%20out%20my%20profile!")
                                    }
                                )
                                Text(
                                    text = "LinkedIn",
                                    color = theme.textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://www.linkedin.com/sharing/share-offsite/?url=https://app.com/${user.username}")
                                    }
                                )
                                Text(
                                    text = "Facebook",
                                    color = theme.textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://www.facebook.com/sharer/sharer.php?u=https://app.com/${user.username}")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer App Watermark
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LogoChain(size = 18.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Powered by LinkOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        is PublicProfileUiState.NotFound -> {
            // Pristinely Styled 404 View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF09090B)), // Zinc-950 background
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp)), // Zinc-800 border
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f)), // Zinc-900 transparent card
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "404",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Portal Not Found (404)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "The user link.bio/$username does not exist or has been deactivated. Create your own page in 30 seconds!",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.62f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onBackToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo-500
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("back_to_login_btn")
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create your own LinkOS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        else -> {
            // Fallback idle loader
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF09090B)))
        }
    }
}

@Composable
fun PublicActiveLinkButton(
    link: Link,
    themeSetting: DesignPresets.GradientTheme,
    layoutPreset: String,
    onClick: () -> Unit
) {
    // Elegant tactile components triggering dynamic click handlers
    val itemModifier = when (layoutPreset) {
        "brutalist" -> Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(themeSetting.textColor, RoundedCornerShape(8.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding(14.dp)
        "glassmorphic" -> Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp)
        else -> Modifier // minimalist
            .fillMaxWidth()
            .clickable { onClick() }
            .background(themeSetting.cardColor, RoundedCornerShape(32.dp))
            .border(1.5.dp, themeSetting.previewBorderColor, RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    }

    val itemTextColor = when (layoutPreset) {
        "brutalist" -> Color.Black
        else -> themeSetting.textColor
    }

    val iconTint = when (layoutPreset) {
        "brutalist" -> Color.Black
        else -> themeSetting.accentColor
    }

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (link.iconUrl.isNotEmpty()) {
            coil.compose.AsyncImage(
                model = link.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = getIconForName(link.iconName),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = link.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = itemTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (link.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = link.description,
                    fontSize = 12.sp,
                    color = itemTextColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = itemTextColor.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}
