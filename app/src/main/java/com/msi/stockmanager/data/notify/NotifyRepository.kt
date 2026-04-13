package com.msi.stockmanager.data.notify

import android.content.Context
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class NotifyRepository(context: Context) : INotifyRepository {
    
    private val dao: NotifyDao = NotifyDatabase.getDatabase(context).notifyDao()
    
    override fun getUnreadCount(): Single<Int> {
        return dao.getUnreadCount()
            .subscribeOn(Schedulers.io())
    }
    
    override fun getList(): Flowable<List<NotifyEntity>> {
        return dao.getList()
            .subscribeOn(Schedulers.io())
    }
    
    override fun getListSync(): List<NotifyEntity> {
        // 注意：此方法會阻塞執行緒，應在背景執行緒呼叫
        return dao.getListSync()
    }
    
    override fun add(item: NotifyEntity): Single<Long> {
        return dao.insert(item)
            .subscribeOn(Schedulers.io())
    }
    
    override fun markRead(id: Long): Single<Int> {
        return dao.markRead(id)
            .subscribeOn(Schedulers.io())
    }
    
    override fun markDeleted(id: Long): Single<Int> {
        return dao.markDeleted(id)
            .subscribeOn(Schedulers.io())
    }
    
    override fun markAllRead(): Single<Int> {
        return dao.markAllRead()
            .subscribeOn(Schedulers.io())
    }
    
    override fun markAllDeleted(): Single<Int> {
        return dao.markAllDeleted()
            .subscribeOn(Schedulers.io())
    }
    
    override fun findByTypeAndPayloadAndDate(type: String, payload: String, date: Long): Maybe<NotifyEntity> {
        return dao.findByTypeAndPayloadAndDate(type, payload, date)
            .subscribeOn(Schedulers.io())
    }
}
