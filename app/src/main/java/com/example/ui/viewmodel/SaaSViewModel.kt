package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Link
import com.example.data.model.User
import com.example.data.repository.LinkSaaSRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface PublicProfileUiState {
    object Idle : PublicProfileUiState
    object Loading : PublicProfileUiState
    data class Success(val user: User, val links: List<Link>) : PublicProfileUiState
    object NotFound : PublicProfileUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SaaSViewModel(
    application: Application,
    private val repository: LinkSaaSRepository
) : AndroidViewModel(application) {

    // Auth states
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Current Logged In User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Current edited user state for real-time live preview (React-like state sync)
    // When the user edits in the Left Panel, they update this _editorUserState immediately.
    // We can save to Database with a slight debounce.
    private val _editorUserState = MutableStateFlow<User?>(null)
    val editorUserState: StateFlow<User?> = _editorUserState.asStateFlow()

    // Links for the logged in user
    val linksList: StateFlow<List<Link>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            repository.getLinksByUserIdFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct stream of total link clicks accumulated
    val totalClicks: StateFlow<Int> = _currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            repository.getTotalClickCountFlow(user.id)
        }
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Global Theme Preference
    private val _isGlobalDarkTheme = MutableStateFlow<Boolean>(true)
    val isGlobalDarkTheme: StateFlow<Boolean> = _isGlobalDarkTheme.asStateFlow()

    // Public view states
    private val _publicProfileState = MutableStateFlow<PublicProfileUiState>(PublicProfileUiState.Idle)
    val publicProfileState: StateFlow<PublicProfileUiState> = _publicProfileState.asStateFlow()

    // Debounce Save job
    private var dbSaveJob: Job? = null

    init {
        val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        _isGlobalDarkTheme.value = prefs.getBoolean("dark_theme", true)

        viewModelScope.launch {
            repository.seedMockDataIfNeeded()
            // Auto login to seed user for instant UI preview and onboarding
            val defaultUser = repository.getUserByUsername("skthouhid")
            if (defaultUser != null) {
                _currentUser.value = defaultUser
                _editorUserState.value = defaultUser
            }
        }
    }

    fun fetchLinkMetadata(url: String, onResult: (title: String, iconUrl: String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var validUrl = url.trim()
                if (!validUrl.startsWith("http://") && !validUrl.startsWith("https://")) {
                    validUrl = "https://$validUrl"
                }
                
                val conn = java.net.URL(validUrl).openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                
                val content = conn.inputStream.bufferedReader().use { it.readText() }
                
                val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
                val rawTitle = titleRegex.find(content)?.groupValues?.get(1)?.trim() ?: ""
                // Decode simple HTML entities if any
                val title = rawTitle.replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")

                val iconRegex = "<link[^>]*rel=\"(?:shortcut icon|icon)\"[^>]*href=\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE)
                var icon = iconRegex.find(content)?.groupValues?.get(1)?.trim() ?: ""
                
                if (icon.isNotEmpty() && !icon.startsWith("http")) {
                    val uri = java.net.URI(validUrl)
                    icon = uri.resolve(icon).toString()
                }
                if (icon.isEmpty()) {
                    val uri = java.net.URI(validUrl)
                    icon = "${uri.scheme}://${uri.host}/favicon.ico"
                }
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(title, icon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // On failure, return empty, user can fill manually
            }
        }
    }

    fun toggleGlobalTheme() {
        val nextTheme = !_isGlobalDarkTheme.value
        _isGlobalDarkTheme.value = nextTheme
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", nextTheme).apply()
    }

    fun clearAuthError() {
        _authState.value = AuthUiState.Idle
    }

    // Login Simulation
    fun login(email: String, word: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            delay(800) // Aesthetic delay for login transition
            val user = repository.getUserByEmail(email)
            if (user != null && user.password == word) {
                _currentUser.value = user
                _editorUserState.value = user
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error("Invalid email or password. Seeded default login: skthouhid641@gmail.com / password123")
            }
        }
    }

    fun oauthLogin(provider: String, email: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            delay(800)
            
            // In a real application, this would exchange an OAuth token with your backend.
            var user = repository.getUserByEmail(email)
            if (user == null) {
                // Auto-signup the OAuth user
                val newUsername = email.substringBefore("@").replace(Regex("[^a-zA-Z0-9]"), "") + (10..99).random()
                val newUser = com.example.data.model.User(
                    email = email,
                    password = "OAUTH_USER", // Real app wouldn't store password for OAuth
                    username = newUsername,
                    bio = "Newly joined via $provider."
                )
                repository.insertUser(newUser)
                user = repository.getUserByEmail(email)
            }
            
            if (user != null) {
                _currentUser.value = user
                _editorUserState.value = user
                _authState.value = AuthUiState.Success
            } else {
                 _authState.value = AuthUiState.Error("Failed to authenticate with $provider.")
            }
        }
    }

    fun signup(email: String, word: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            delay(800)
            val cleanUsername = username.trim().lowercase()
            if (cleanUsername.isEmpty()) {
                _authState.value = AuthUiState.Error("Username cannot be empty")
                return@launch
            }
            val existingEmail = repository.getUserByEmail(email)
            if (existingEmail != null) {
                _authState.value = AuthUiState.Error("Email already registered")
                return@launch
            }
            val existingUsername = repository.getUserByUsername(cleanUsername)
            if (existingUsername != null) {
                _authState.value = AuthUiState.Error("Username already taken. Please pick another.")
                return@launch
            }

            val newUser = User(
                email = email,
                password = word,
                username = cleanUsername
            )
            val rowId = repository.insertUser(newUser)
            val createdUser = repository.getUserById(rowId.toInt())
            if (createdUser != null) {
                _currentUser.value = createdUser
                _editorUserState.value = createdUser
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error("Failed to register account. Please try again.")
            }
        }
    }

    // Signout
    fun signout() {
        _currentUser.value = null
        _editorUserState.value = null
        _authState.value = AuthUiState.Idle
    }

    // Update real-time preview user profile settings
    fun updateProfileInfo(bio: String, avatarUrl: String, themeGradient: String, layoutStyle: String) {
        val currentEdit = _editorUserState.value ?: return
        val updated = currentEdit.copy(
            bio = bio,
            avatarUrl = avatarUrl,
            themeGradient = themeGradient,
            layoutStyle = layoutStyle
        )
        _editorUserState.value = updated

        // Real-time automatic database saving with short debounce
        dbSaveJob?.cancel()
        dbSaveJob = viewModelScope.launch {
            delay(1000) // Debounce delay
            repository.updateUser(updated)
            // also sync with actual current user
            _currentUser.value = updated
        }
    }

    // Save profile change instantly (manual trigger fallback)
    fun persistProfileChanges() {
        val updated = _editorUserState.value ?: return
        viewModelScope.launch {
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // Link CRUD sections
    fun addNewLink(title: String, url: String, description: String = "", iconUrl: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val currentLinks = repository.getLinksByUserId(user.id)
            val newPosition = currentLinks.size
            val defaultIcons = listOf("globe", "link", "instagram", "github", "linkedin")
            // Infer icon name from title/url
            val lowerTitle = title.lowercase()
            val inferredIcon = when {
                lowerTitle.contains("github") -> "github"
                lowerTitle.contains("instagram") -> "instagram"
                lowerTitle.contains("youtube") -> "youtube"
                lowerTitle.contains("linkedin") -> "linkedin"
                lowerTitle.contains("twitter") || lowerTitle.contains("x.com") -> "twitter"
                lowerTitle.contains("email") || lowerTitle.contains("mail") -> "email"
                else -> "globe"
            }

            val newLink = Link(
                userId = user.id,
                title = title.ifBlank { "New Link Card" },
                description = description,
                url = url.ifBlank { "https://example.com" },
                iconUrl = iconUrl,
                iconName = inferredIcon,
                isActive = true,
                position = newPosition
            )
            repository.insertLink(newLink)
        }
    }

    fun updateLinkDetails(link: Link) {
        viewModelScope.launch {
            repository.updateLink(link)
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            repository.deleteLink(link)
        }
    }

    fun generateAIBio() {
        val currentUser = _editorUserState.value ?: return
        val previousBio = currentUser.bio
        // Set loading state visually (optional, could just set to "Generating AI bio...")
        _editorUserState.value = currentUser.copy(bio = "Generating AI bio...")
        viewModelScope.launch {
            val generated = com.example.data.api.generateBio(previousBio)
            _editorUserState.value = currentUser.copy(bio = generated.take(150)) // trim logic if needed
            // save immediately to database layout
            _editorUserState.value?.let { updated ->
                repository.updateUser(updated)
            }
        }
    }

    // Reorder links position
    fun moveLinkUp(link: Link) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val list = repository.getLinksByUserId(user.id).toMutableList()
            val index = list.indexOfFirst { it.id == link.id }
            if (index > 0) {
                val above = list[index - 1]
                list[index - 1] = link.copy(position = above.position)
                list[index] = above.copy(position = link.position)
                repository.updateLink(list[index - 1])
                repository.updateLink(list[index])
            }
        }
    }

    fun moveLinkDown(link: Link) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val list = repository.getLinksByUserId(user.id).toMutableList()
            val index = list.indexOfFirst { it.id == link.id }
            if (index != -1 && index < list.size - 1) {
                val below = list[index + 1]
                list[index + 1] = link.copy(position = below.position)
                list[index] = below.copy(position = link.position)
                repository.updateLink(list[index + 1])
                repository.updateLink(list[index])
            }
        }
    }

    // Public profile lookup
    fun lookupPublicProfile(username: String) {
        viewModelScope.launch {
            _publicProfileState.value = PublicProfileUiState.Loading
            val normalized = username.trim().lowercase()
            val targetUser = repository.getUserByUsername(normalized)
            if (targetUser != null) {
                // Return active links flow / list
                repository.getActiveLinksByUserIdFlow(targetUser.id).collectLatest { activeLinks ->
                    _publicProfileState.value = PublicProfileUiState.Success(targetUser, activeLinks)
                }
            } else {
                _publicProfileState.value = PublicProfileUiState.NotFound
            }
        }
    }

    // Handle profile link click analytics increments
    fun recordLinkClick(linkId: Int) {
        viewModelScope.launch {
            repository.incrementClickCount(linkId)
        }
    }

    // Simple Factory pattern for ViewModel
    class Factory(
        private val application: Application,
        private val repository: LinkSaaSRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaaSViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaaSViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
