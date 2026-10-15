package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "Creator",
    val sourceUrl: String,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val platform: String,
    val qualityLabel: String,
    val fileExtension: String,
    val filePath: String? = null,
    val fileSizeFormatted: String = "15 MB",
    val downloadManagerId: Long? = null,
    val status: String = "COMPLETED", // "PENDING", "DOWNLOADING", "COMPLETED", "FAILED"
    val progressPercent: Int = 100,
    val aiSummary: String? = null,
    val aiTags: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
