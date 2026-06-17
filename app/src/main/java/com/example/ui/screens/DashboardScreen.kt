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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Link
import com.example.data.model.User
import com.example.ui.theme.DesignPresets
import com.example.ui.viewmodel.SaaSViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SaaSViewModel,
    onNavigateToPublic: (String) -> Unit,
    onSignout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val editorUser by viewModel.editorUserState.collectAsState()
    val links by viewModel.linksList.collectAsState()
    val totalClicks by viewModel.totalClicks.collectAsState()

    // Tab state for compact widths (0 = Editor form, 1 = Live Phone Preview)
    var selectedTab by remember { mutableIntStateOf(0) }

    // State for the "+ Add New Link" form
    var showAddDialogue by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    var newIconUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(newUrl) {
        val validUrlPattern = "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?\$".toRegex()
        if (newUrl.isNotBlank() && validUrlPattern.matches(newUrl)) {
            kotlinx.coroutines.delay(1000)
            if (newTitle.isBlank() && newDescription.isBlank()) {
                viewModel.fetchLinkMetadata(newUrl) { title, icon ->
                    if (title.isNotEmpty() && newTitle.isBlank()) newTitle = title
                    if (icon.isNotEmpty() && newIconUrl.isBlank()) newIconUrl = icon
                }
            }
        }
    }

    if (currentUser == null || editorUser == null) {
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWideScreen = maxWidth >= 800.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { SaaSLogoInline(logoSize = 28.dp) },
                    actions = {
                        // Global Theme Toggle
                        val isDark by viewModel.isGlobalDarkTheme.collectAsState()
                        IconButton(
                            onClick = { viewModel.toggleGlobalTheme() },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }

                        // Quick badge
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(Color(0xFF6366F1).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "PRO",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PRO ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC7D2FE)
                                )
                            }
                        }

                        // Open Public Profile mimic
                        IconButton(
                            onClick = { onNavigateToPublic(currentUser!!.username) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF38BDF8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "View Public Portal"
                            )
                        }

                        // Sign out icon
                        IconButton(
                            onClick = {
                                viewModel.signout()
                                onSignout()
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFDA4AF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = androidx.compose.ui.graphics.Color(0xFF27272A), // Zinc-800 divider color
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                )
            },
            bottomBar = {
                // Return bottom navigation switcher only for mobile sizing!
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = Color(0xFF09090B), // Sophisticated dark background
                        tonalElevation = 8.dp,
                        modifier = Modifier.drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = strokeWidth / 2
                            drawLine(
                                color = Color(0xFF27272A), // Zinc-800 divider color
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Edit, contentDescription = "Editor Controls") },
                            label = { Text("Editor Workspace", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color.White.copy(alpha = 0.52f),
                                unselectedTextColor = Color.White.copy(alpha = 0.52f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Smartphone, contentDescription = "Live Preview") },
                            label = { Text("Device View", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color.White.copy(alpha = 0.52f),
                                unselectedTextColor = Color.White.copy(alpha = 0.52f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            val contentColor = MaterialTheme.colorScheme.onBackground
            val borderColor = MaterialTheme.colorScheme.outline
            val surfaceColor = MaterialTheme.colorScheme.surface

            if (isWideScreen) {
                // Split Screen Desktop/Tablet Panel Row Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Left editor panel (60%)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.2f)
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val x = size.width - strokeWidth / 2
                                drawLine(
                                    color = borderColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(horizontal = 24.dp)
                    ) {
                        WorkspaceEditorContent(
                            editorUser = editorUser!!,
                            links = links,
                            totalClicks = totalClicks,
                            viewModel = viewModel,
                            onOpenAddDialog = { showAddDialogue = true }
                        )
                    }

                    // Right sticky Preview panel (40%)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.8f)
                            .background(surfaceColor) 
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LIVE MOBILE SIMULATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            PhoneMockupFrame(
                                user = editorUser!!,
                                activeLinks = links.filter { it.isActive }
                            )
                        }
                    }
                }
            } else {
                // Mobile tab panel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (selectedTab == 0) {
                        // Left controls
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            WorkspaceEditorContent(
                                editorUser = editorUser!!,
                                links = links,
                                totalClicks = totalClicks,
                                viewModel = viewModel,
                                onOpenAddDialog = { showAddDialogue = true }
                            )
                        }
                    } else {
                        // Sticky preview frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0B0F19))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PhoneMockupFrame(
                                user = editorUser!!,
                                activeLinks = links.filter { it.isActive }
                            )
                        }
                    }
                }
            }
        }

        // Add new link bottom dialog
        if (showAddDialogue) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialogue = false
                    newTitle = ""
                    newUrl = ""
                    newIconUrl = ""
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddLink, contentDescription = null, tint = Color(0xFF6366F1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Link to Stack",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "Destination URL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = newUrl,
                            onValueChange = { newUrl = it },
                            placeholder = { Text("https://bsky.app/profile/you", color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_link_url_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Link Title & Description",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (newUrl.isNotBlank()) {
                                            newTitle = "Generating..."
                                            newDescription = "Generating..."
                                            scope.launch {
                                                val info = com.example.data.api.suggestLinkInfo(newUrl)
                                                newTitle = info.first
                                                newDescription = info.second
                                            }
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Magic Writer",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Magic Writer", color = Color(0xFF6366F1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("e.g. Follow me on Bluesky", color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_link_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newDescription,
                            onValueChange = { newDescription = it },
                            placeholder = { Text("e.g. Daily updates and tips", color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_link_desc_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                                viewModel.addNewLink(newTitle, newUrl, newDescription, newIconUrl)
                                showAddDialogue = false
                                newTitle = ""
                                newDescription = ""
                                newUrl = ""
                                newIconUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Add to Stack", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialogue = false
                            newTitle = ""
                            newDescription = ""
                            newUrl = ""
                            newIconUrl = ""
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color(0xFF18181B), // Zinc-900 dialog container
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp)) // Zinc-800 border
            )
        }
    }
}

@Composable
fun WorkspaceEditorContent(
    editorUser: User,
    links: List<Link>,
    totalClicks: Int,
    viewModel: SaaSViewModel,
    onOpenAddDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        // Section: Analytics Dashboard Badge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp)), // Zinc-800 border
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900 card
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF18181B), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=https://linkos.bio/${editorUser.username}&bgcolor=18181B&color=6366F1",
                            contentDescription = "QR Code for Profile",
                            modifier = Modifier.size(52.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "PORTAL USER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.42f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "linkos.bio/${editorUser.username}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL CLICKS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.42f)
                        )
                        Text(
                            text = "$totalClicks",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.testTag("analytics_total_clicks")
                        )
                    }
                }
            }
        }

        // Section: Profile Customizer panel
        item {
            Text(
                text = "PROFILE SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.0.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp)), // Zinc-800 border
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f)), // Zinc-900 with 50% opacity
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Profile bio edits
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customize Profile Bio",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            Modifier.clickable { viewModel.generateAIBio() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Magic",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(14.dp)
                            )
                            Text("AI Magic", color = Color(0xFF6366F1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(
                        value = editorUser.bio,
                        onValueChange = {
                            viewModel.updateProfileInfo(
                                bio = it,
                                avatarUrl = editorUser.avatarUrl,
                                themeGradient = editorUser.themeGradient,
                                layoutStyle = editorUser.layoutStyle
                            )
                        },
                        placeholder = { Text("Introduce yourself...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .testTag("bio_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF09090B), // Zinc-950
                            unfocusedContainerColor = Color(0xFF09090B), // Zinc-950
                            focusedBorderColor = Color(0xFF6366F1), // Indigo 500
                            unfocusedBorderColor = Color(0xFF27272A) // Zinc-800
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar placeholder settings
                    Text(
                        text = "Brand Logo / Avatar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        uri?.let {
                            viewModel.updateProfileInfo(
                                bio = editorUser.bio,
                                avatarUrl = it.toString(),
                                themeGradient = editorUser.themeGradient,
                                layoutStyle = editorUser.layoutStyle
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload Logo")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload")
                        }

                        OutlinedTextField(
                            value = editorUser.avatarUrl,
                            onValueChange = {
                                viewModel.updateProfileInfo(
                                    bio = editorUser.bio,
                                    avatarUrl = it,
                                    themeGradient = editorUser.themeGradient,
                                    layoutStyle = editorUser.layoutStyle
                                )
                            },
                            placeholder = { Text("https://example.com/photo.png") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("avatar_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF09090B), // Zinc-950
                                unfocusedContainerColor = Color(0xFF09090B), // Zinc-950
                                focusedBorderColor = Color(0xFF6366F1), // Indigo 500
                                unfocusedBorderColor = Color(0xFF27272A) // Zinc-800
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Layout preset sliders
                    Text(
                        text = "Select Layout Theme",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesignPresets.layouts.forEach { layout ->
                            val isSelected = editorUser.layoutStyle == layout.key
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF27272A)) // Zinc-800 for unselected, Indigo-500 for selected
                                    .clickable {
                                        viewModel.updateProfileInfo(
                                            bio = editorUser.bio,
                                            avatarUrl = editorUser.avatarUrl,
                                            themeGradient = editorUser.themeGradient,
                                            layoutStyle = layout.key
                                        )
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = layout.label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gradient Customizer selector
                    Text(
                        text = "Customize Gradient Scheme",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesignPresets.themes.forEach { theme ->
                            val isSelected = editorUser.themeGradient == theme.key
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .aspectRatio(1.6f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.brush)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.updateProfileInfo(
                                            bio = editorUser.bio,
                                            avatarUrl = editorUser.avatarUrl,
                                            themeGradient = theme.key,
                                            layoutStyle = editorUser.layoutStyle
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Links Header and CRUD Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINKS STACK (${links.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Button(
                    onClick = onOpenAddDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo-500
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_new_link_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List builder for Link Cards
        if (links.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFF27272A).copy(alpha = 0.3f), RoundedCornerShape(14.dp)) // Zinc-800 background
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp)) // Zinc-800 border
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LinkOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Links Added Yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap '+ Add Link' above to display beautiful buttons in your stack.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(links, key = { it.id }) { link ->
                LinkRowCard(
                    link = link,
                    onUpdate = { viewModel.updateLinkDetails(it) },
                    onDelete = { viewModel.deleteLink(link) },
                    onMoveUp = { viewModel.moveLinkUp(link) },
                    onMoveDown = { viewModel.moveLinkDown(link) }
                )
            }
        }
    }
}

@Composable
fun LinkRowCard(
    link: Link,
    onUpdate: (Link) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(12.dp)), // Zinc-800 border
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900 container
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Link Icon selector indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (link.iconUrl.isNotEmpty()) {
                        AsyncImage(
                            model = link.iconUrl,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = getIconForName(link.iconName),
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1.0f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inline Editable Title input
                        BasicEditableTextField(
                            value = link.title,
                            onValueChange = { onUpdate(link.copy(title = it)) },
                            placeholder = "Link Display Title",
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    if (link.url.isNotBlank()) {
                                        onUpdate(link.copy(title = "Generating...", description = "Generating..."))
                                        scope.launch {
                                            val info = com.example.data.api.suggestLinkInfo(link.url)
                                            onUpdate(link.copy(title = info.first, description = info.second))
                                        }
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Magic Writer",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Inline Editable Description
                    BasicEditableTextField(
                        value = link.description,
                        onValueChange = { onUpdate(link.copy(description = it)) },
                        placeholder = "Link Description (optional)",
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Inline Editable URL input
                    BasicEditableTextField(
                        value = link.url,
                        onValueChange = { onUpdate(link.copy(url = it)) },
                        placeholder = "Destination URL (https://...)",
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Show/Hide active toggle switch
                Switch(
                    checked = link.isActive,
                    onCheckedChange = { onUpdate(link.copy(isActive = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))
            HorizontalDivider(color = Color(0xFF27272A), thickness = 1.dp) // Zinc-800 divider
            Spacer(modifier = Modifier.padding(top = 4.dp))

            // Footer controls: Clicks counter, Ordering stack and Trash bin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Analytics label inside node
                Box(
                    modifier = Modifier
                        .background(Color(0xFF27272A).copy(alpha = 0.5f), RoundedCornerShape(6.dp)) // Zinc-800 background
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${link.clickCount} clicks",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Icon Selector
                    var expandedMenu by remember { mutableStateOf(false) }
                    Box {
                        TextButton(
                            onClick = { expandedMenu = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Icon: ${getLabelForIcon(link.iconName)}",
                                fontSize = 11.sp,
                                color = Color(0xFF818CF8),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            modifier = Modifier.background(Color(0xFF18181B)) // Zinc-900 background
                        ) {
                            listOf("globe", "link", "instagram", "github", "linkedin", "youtube", "email", "twitter").forEach { iconKey ->
                                DropdownMenuItem(
                                    text = { Text(getLabelForIcon(iconKey), color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        onUpdate(link.copy(iconName = iconKey))
                                        expandedMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            getIconForName(iconKey),
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Reorder arrows
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Delete Link
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BasicEditableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val focusManager = LocalFocusManager.current
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = textStyle.color.copy(alpha = 0.25f))
                    )
                }
                innerTextField()
            }
        }
    )
}

// Custom Scaling layout extension for preview switches
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val width = (placeable.width * scale).toInt()
        val height = (placeable.height * scale).toInt()
        layout(width, height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
            }
        }
    }
)

@Composable
fun PhoneMockupFrame(
    user: User,
    activeLinks: List<Link>
) {
    val theme = DesignPresets.getTheme(user.themeGradient)
    val layout = user.layoutStyle

    // Sticky Device silhouette card mimicking and styled as an iPhone display
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(560.dp)
            .border(8.dp, Color(0xFF334155), RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(theme.brush)
    ) {
        // iPhone camera Notch pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .width(110.dp)
                .height(20.dp)
                .background(Color.Black, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Camera lens dot
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(5.dp)
                    .background(Color(0xFF1E293B), CircleShape)
            )
        }

        // Notch Status Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "08:00 AM",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor.copy(alpha = 0.8f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    tint = theme.textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = theme.textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = theme.textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        // Main preview container view scroll lists
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image bubble or Initial fallback
            Spacer(modifier = Modifier.height(10.dp))
            if (user.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar preview",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, theme.accentColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.5.dp, theme.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.username.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor,
                        fontSize = 20.sp
                    )
                }
            }

            // Public username slug
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "@${user.username}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Bio
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = user.bio,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textColor.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stack of active links inside the Phone mockup scroll viewport
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeLinks.isEmpty()) {
                    item {
                        Text(
                            text = "No active links in stack",
                            fontSize = 11.sp,
                            color = theme.textColor.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(activeLinks) { link ->
                        PreviewLinkButton(
                            link = link,
                            themeSetting = theme,
                            layoutPreset = layout
                        )
                    }
                }
            }

            // Power badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                LogoChain(size = 14.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Powered by LinkOS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun PreviewLinkButton(
    link: Link,
    themeSetting: DesignPresets.GradientTheme,
    layoutPreset: String
) {
    // Dynamically draw buttons depending on minimalist, glassmorphic or brutalist
    val buttonModifier = when (layoutPreset) {
        "brutalist" -> Modifier
            .fillMaxWidth()
            .background(themeSetting.textColor, RoundedCornerShape(6.dp)) // Flat bright background
            .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
            .padding(10.dp)
        "glassmorphic" -> Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        else -> Modifier // minimalist
            .fillMaxWidth()
            .background(themeSetting.cardColor, RoundedCornerShape(24.dp))
            .border(1.dp, themeSetting.previewBorderColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
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
        modifier = buttonModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (link.iconUrl.isNotEmpty()) {
            AsyncImage(
                model = link.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = getIconForName(link.iconName),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = link.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = itemTextColor,
            modifier = Modifier.weight(1.0f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = itemTextColor.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}
