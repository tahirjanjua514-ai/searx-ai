package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE downloadManagerId = :dmId LIMIT 1")
    suspend fun getDownloadByDmId(dmId: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET progressPercent = :progress, status = :status, filePath = :filePath WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, status: String, filePath: String?)

    @Query("UPDATE downloads SET aiSummary = :summary, aiTags = :tags WHERE id = :id")
    suspend fun updateAiInsights(id: Long, summary: String, tags: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()
}
