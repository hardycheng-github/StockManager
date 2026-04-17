package com.msi.stockmanager.data.notify

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notify_entity")
data class NotifyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = "", // 預留，例如 "MACD_DEATH"
    val title: String,
    val body: String = "", // 可選
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false, // 已讀／未讀
    val deleted: Boolean = false, // true = 已刪除，不顯示、不計入未讀
    val actionType: String = "", // 預留：如 "MARK_READ" / "OPEN_ANALYSIS" / "OPEN_STOCK"
    val actionPayload: String = "" // 預留，如 stockId、Intent 所需資料
)
