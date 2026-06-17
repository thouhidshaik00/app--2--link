package com.example.data.repository

import com.example.data.dao.LinkDao
import com.example.data.dao.UserDao
import com.example.data.model.Link
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class LinkSaaSRepository(
    private val userDao: UserDao,
    private val linkDao: LinkDao
) {
    suspend fun seedMockDataIfNeeded() = withContext(Dispatchers.IO) {
        // If there's no user, seed a beautiful showcase profile for skthouhid
        val existingUser = userDao.getUserByUsername("skthouhid")
        if (existingUser == null) {
            val defaultUser = User(
                email = "skthouhid641@gmail.com",
                password = "password123",
                username = "skthouhid",
                bio = "Senior Full-Stack Engineer & Product Designer. Crafting ultra-clean SaaS interfaces with elegant typography.",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                themeGradient = "slate",
                layoutStyle = "glassmorphic"
            )
            val userId = userDao.insertUser(defaultUser).toInt()

            val links = listOf(
                Link(userId = userId, title = "Design Portfolio", url = "https://dribbble.com", iconName = "instagram", position = 0, clickCount = 42),
                Link(userId = userId, title = "GitHub Repositories", url = "https://github.com/skthouhid", iconName = "github", position = 1, clickCount = 124),
                Link(userId = userId, title = "Tech YouTube Channel", url = "https://youtube.com", iconName = "youtube", position = 2, clickCount = 89),
                Link(userId = userId, title = "Connect on LinkedIn", url = "https://linkedin.com", iconName = "linkedin", position = 3, clickCount = 57),
                Link(userId = userId, title = "My Website", url = "https://example.com/website", iconName = "globe", position = 4, clickCount = 15)
            )
            for (link in links) {
                linkDao.insertLink(link)
            }
        }
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
    }

    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    fun getUserByIdFlow(userId: Int): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
    }

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    fun getLinksByUserIdFlow(userId: Int): Flow<List<Link>> {
        return linkDao.getLinksByUserIdFlow(userId)
    }

    fun getActiveLinksByUserIdFlow(userId: Int): Flow<List<Link>> {
        return linkDao.getActiveLinksByUserIdFlow(userId)
    }

    fun getTotalClickCountFlow(userId: Int): Flow<Int?> {
        return linkDao.getTotalClickCountFlow(userId)
    }

    suspend fun getLinksByUserId(userId: Int): List<Link> = withContext(Dispatchers.IO) {
        linkDao.getLinksByUserId(userId)
    }

    suspend fun insertLink(link: Link): Long = withContext(Dispatchers.IO) {
        linkDao.insertLink(link)
    }

    suspend fun updateLink(link: Link) = withContext(Dispatchers.IO) {
        linkDao.updateLink(link)
    }

    suspend fun deleteLink(link: Link) = withContext(Dispatchers.IO) {
        linkDao.deleteLink(link)
    }

    suspend fun incrementClickCount(linkId: Int) = withContext(Dispatchers.IO) {
        linkDao.incrementClickCount(linkId)
    }
}
