package com.msi.stockmanager.data.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 模擬投資組合 hardcode 清單。增減紀錄只需修改 {@link #entries()}。
 */
public final class SimulatedPortfolioData {

    private SimulatedPortfolioData() {}

    public static List<SimulatedTransRecord> entries() {
        List<SimulatedTransRecord> records = new ArrayList<>();

        // --- 使用者指定 ---
        records.add(SimulatedTransRecord.cashIn("2009-07-11", 1_000_000));
        records.add(SimulatedTransRecord.stockBuy("2022-09-02", "2377", 5000, 111));
        records.add(SimulatedTransRecord.stockSell("2023-05-10", "2377", 1000, 130));
        records.add(SimulatedTransRecord.cashDividend("2024-03-18", "2377", 15_000));
        records.add(SimulatedTransRecord.cashIn("2025-01-01", 5_000_000));
        records.add(SimulatedTransRecord.stockBuy("2025-04-18", "2330", 1000, 485));
        records.add(SimulatedTransRecord.stockBuy("2025-06-09", "1201", 10000, 16));
        records.add(SimulatedTransRecord.stockSell("2025-08-20", "1201", 3000, 18));
        records.add(SimulatedTransRecord.cashOut("2025-09-25", 200_000));
        records.add(SimulatedTransRecord.stockBuy("2025-11-05", "7722", 1000, 576));

        return Collections.unmodifiableList(records);
    }
}
