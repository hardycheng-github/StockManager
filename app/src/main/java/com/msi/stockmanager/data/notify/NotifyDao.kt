package com.msi.stockmanager.data.notify

import androidx.room.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface NotifyDao {
    
    @Query("SELECT COUNT(*) FROM notify_entity WHERE read = 0 AND deleted = 0")
    fun getUnreadCount(): Single<Int>
    
    @Query("SELECT * FROM notify_entity WHERE deleted = 0 ORDER BY createdAt DESC")
    fun getList(): Flowable<List<NotifyEntity>>
    
    @Query("SELECT * FROM notify_entity WHERE deleted = 0 ORDER BY createdAt DESC")
    fun getListSync(): List<NotifyEntity>
    
    @Insert
    fun insert(item: NotifyEntity): Single<Long>
    
    @Update
    fun update(item: NotifyEntity): Single<Int>
    
    @Query("UPDATE notify_entity SET read = 1 WHERE deleted = 0")
    fun markAllRead(): Single<Int>
    
    @Query("UPDATE notify_entity SET deleted = 1")
    fun markAllDeleted(): Single<Int>
    
    @Query("UPDATE notify_entity SET read = 1 WHERE id = :id")
    fun markRead(id: Long): Single<Int>
    
    @Query("UPDATE notify_entity SET deleted = 1 WHERE id = :id")
    fun markDeleted(id: Long): Single<Int>
    
    @Query("SELECT * FROM notify_entity WHERE type = :type AND actionPayload = :payload AND createdAt = :date AND deleted = 0 LIMIT 1")
    fun findByTypeAndPayloadAndDate(type: String, payload: String, date: Long): Maybe<NotifyEntity>
}
