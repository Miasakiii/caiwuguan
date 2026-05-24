package com.caiwuguan.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["source"]),
        Index(value = ["source", "timestamp"])
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val type: String,
    val category: String,
    val merchant: String = "",
    val description: String = "",
    val source: String,
    val transactionId: String? = null,
    val notificationText: String? = null,
    val isAutoRecorded: Boolean = false,
    val timestamp: Long,
    val createdAt: Long = System.currentTimeMillis()
)
