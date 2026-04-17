package com.msi.stockmanager.data.notify

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface INotifyRepository {
    /**
     * 取得未讀且未刪除的數量（給徽章用）
     */
    fun getUnreadCount(): Single<Int>
    
    /**
     * 取得未刪除的列表（依 createdAt 倒序）
     */
    fun getList(): Flowable<List<NotifyEntity>>
    
    /**
     * 同步取得列表（用於一次性查詢）
     */
    fun getListSync(): List<NotifyEntity>
    
    /**
     * 新增通知
     */
    fun add(item: NotifyEntity): Single<Long>
    
    /**
     * 標記為已讀
     */
    fun markRead(id: Long): Single<Int>
    
    /**
     * 標記為已刪除
     */
    fun markDeleted(id: Long): Single<Int>
    
    /**
     * 全部標記為已讀
     */
    fun markAllRead(): Single<Int>
    
    /**
     * 全部標記為已刪除
     */
    fun markAllDeleted(): Single<Int>
    
    /**
     * 根據類型、載荷和日期查找通知（用於檢查是否已存在相同事件）
     */
    fun findByTypeAndPayloadAndDate(type: String, payload: String, date: Long): Maybe<NotifyEntity>
}
