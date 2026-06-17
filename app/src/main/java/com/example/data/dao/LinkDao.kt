package com.example.data.dao

import androidx.room.*
import com.example.data.model.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    @Query("SELECT * FROM links WHERE userId = :userId ORDER BY position ASC")
    fun getLinksByUserIdFlow(userId: Int): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE userId = :userId AND isActive = 1 ORDER BY position ASC")
    fun getActiveLinksByUserIdFlow(userId: Int): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE userId = :userId ORDER BY position ASC")
    suspend fun getLinksByUserId(userId: Int): List<Link>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: Link): Long

    @Update
    suspend fun updateLink(link: Link)

    @Delete
    suspend fun deleteLink(link: Link)

    @Query("SELECT * FROM links WHERE id = :linkId LIMIT 1")
    suspend fun getLinkById(linkId: Int): Link?

    @Query("UPDATE links SET clickCount = clickCount + 1 WHERE id = :linkId")
    suspend fun incrementClickCount(linkId: Int)

    @Query("SELECT SUM(clickCount) FROM links WHERE userId = :userId")
    fun getTotalClickCountFlow(userId: Int): Flow<Int?>
}
