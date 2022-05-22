package com.msi.stockmanager.data.transaction;

import java.util.List;

public interface ITransApi {
    /**
     * 取得目前持有的股票代碼列表
     * @return 股票代碼列表
     */
    List<String> getHoldingStockList();

    /**
     * 取得歷史交易紀錄
     * @return 交易紀錄列表
     */
    List<Transaction> getHistoryTransList();

    /**
     * 新增一筆交易紀錄
     * @param trans 交易內容
     * @return 交易編號, -1: 新增失敗
     */
    long addTrans(Transaction trans);

    /**
     * 更新交易紀錄內容
     * @param trans_id 交易編號
     * @param trans 交易內容
     * @return 設定成功or失敗
     */
    boolean updateTrans(long trans_id, Transaction trans);

    /**
     * 刪除交易紀錄
     * @param trans_id 交易編號
     * @return 設定成功or失敗
     */
    boolean removeTrans(long trans_id);

}
