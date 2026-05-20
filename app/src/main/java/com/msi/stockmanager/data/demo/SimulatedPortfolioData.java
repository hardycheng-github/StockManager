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

        // 2016
        records.add(SimulatedTransRecord.cashIn("2016-01-05", 2_500_000, "初始準備金投入"));
        records.add(SimulatedTransRecord.stockBuy("2016-01-10", "2330", 5_000, 150, "建倉"));
        records.add(SimulatedTransRecord.stockBuy("2016-01-10", "2317", 5_000, 80, "建倉"));
        records.add(SimulatedTransRecord.stockBuy("2016-01-10", "2377", 2_000, 50, "建倉（自家公司）"));
        records.add(SimulatedTransRecord.stockBuy("2016-01-10", "2884", 20_000, 18, "建倉"));
        records.add(SimulatedTransRecord.stockBuy("2016-01-10", "2609", 10_000, 9.5, "建倉"));
        records.add(SimulatedTransRecord.cashDividend("2016-07-20", "2330", 30_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2016-07-25", "2317", 20_000, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2016-07-25", "2317", 500, "每千股配 100 股"));
        records.add(SimulatedTransRecord.cashDividend("2016-08-05", "2377", 7_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2016-08-15", "2884", 8_600, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2016-08-15", "2884", 860, "每千股配 43 股"));

        // 2017
        records.add(SimulatedTransRecord.stockBuy("2017-02-10", "2377", 1_000, 75, "紀律加碼"));
        records.add(SimulatedTransRecord.stockReduction("2017-03-01", "2609", 5_327, "減資 53.27% 彌補虧損"));
        records.add(SimulatedTransRecord.cashDividend("2017-07-20", "2330", 35_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2017-07-25", "2317", 24_750, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2017-07-25", "2317", 275, "每千股配 50 股"));
        records.add(SimulatedTransRecord.cashDividend("2017-08-05", "2377", 13_500, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2017-08-15", "2884", 15_416, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2017-08-15", "2884", 1_541, "每千股配 73.9 股"));

        // 2018
        records.add(SimulatedTransRecord.stockBuy("2018-02-10", "2377", 1_000, 85, "紀律加碼"));
        records.add(SimulatedTransRecord.cashDividend("2018-07-20", "2330", 40_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2018-07-25", "2317", 11_550, "年度配息 (減資前發放)"));
        records.add(SimulatedTransRecord.cashDividend("2018-08-05", "2377", 18_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2018-08-15", "2884", 13_709, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2018-08-15", "2884", 1_600, "每千股配 71.4 股"));
        records.add(SimulatedTransRecord.cashReduction(
                "2018-10-18", "2317", 1_155, 2, "減資 20%，每股退 2 元"));

        // 2019
        records.add(SimulatedTransRecord.stockSell("2019-01-15", "2609", 4_673, 8.5, "認賠出場 (含稅費概算)"));
        records.add(SimulatedTransRecord.stockBuy("2019-02-10", "2377", 1_000, 85, "紀律加碼"));
        records.add(SimulatedTransRecord.cashDividend("2019-07-20", "2330", 50_000, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2019-07-25", "2317", 18_480, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2019-08-05", "2377", 25_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2019-08-15", "2884", 17_000, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2019-08-15", "2884", 1_700, "每千股配 70.8 股"));

        // 2020
        records.add(SimulatedTransRecord.stockBuy("2020-02-10", "2377", 1_000, 110, "紀律加碼"));
        records.add(SimulatedTransRecord.cashOut("2020-03-15", 200_000, "疫情期間提領現金備用"));
        records.add(SimulatedTransRecord.cashDividend("2020-07-20", "2330", 50_000, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2020-07-25", "2317", 19_404, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2020-08-05", "2377", 25_200, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2020-08-15", "2884", 20_000, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2020-08-15", "2884", 2_000, "每千股配 77.8 股"));

        // 2021
        records.add(SimulatedTransRecord.stockBuy("2021-02-10", "2377", 1_000, 150, "紀律加碼"));
        records.add(SimulatedTransRecord.cashDividend("2021-07-20", "2330", 52_500, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2021-07-25", "2317", 18_480, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2021-08-05", "2377", 42_700, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2021-08-15", "2884", 17_000, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2021-08-15", "2884", 1_700, "每千股配 61.3 股"));

        // 2022
        records.add(SimulatedTransRecord.stockBuy("2022-02-10", "2377", 1_000, 120, "紀律加碼"));
        records.add(SimulatedTransRecord.cashDividend("2022-07-20", "2330", 55_000, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2022-07-25", "2317", 24_024, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2022-08-05", "2377", 84_000, "年度配息 (歷史新高)"));
        records.add(SimulatedTransRecord.cashDividend("2022-08-15", "2884", 19_000, "年度配息"));
        records.add(SimulatedTransRecord.stockDividend("2022-08-15", "2884", 1_900, "每千股配 64.6 股"));

        // 2023
        records.add(SimulatedTransRecord.stockBuy("2023-02-10", "2377", 1_000, 160, "紀律加碼"));
        records.add(SimulatedTransRecord.cashDividend("2023-07-20", "2330", 60_000, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2023-07-25", "2317", 24_486, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2023-08-05", "2377", 59_400, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2023-08-15", "2884", 6_000, "年度配息 (防疫險影響縮水)"));
        records.add(SimulatedTransRecord.stockDividend("2023-08-15", "2884", 1_200, "每千股配 38.3 股"));

        // 2024
        records.add(SimulatedTransRecord.stockBuy("2024-02-10", "2377", 1_000, 180, "紀律加碼"));
        records.add(SimulatedTransRecord.stockSell("2024-06-15", "2317", 4_620, 200, "達標停利全數出清"));
        records.add(SimulatedTransRecord.stockSell("2024-07-10", "2330", 2_000, 900, "逢高獲利了結部分持股"));
        records.add(SimulatedTransRecord.cashDividend("2024-07-20", "2330", 56_000, "年度季配息加總"));
        records.add(SimulatedTransRecord.cashDividend("2024-08-05", "2377", 54_000, "年度配息"));
        records.add(SimulatedTransRecord.cashDividend("2024-08-15", "2884", 39_000, "年度配息 (獲利回穩)"));
        records.add(SimulatedTransRecord.stockDividend("2024-08-15", "2884", 650, "每千股配 20 股"));

        // 2025
        records.add(SimulatedTransRecord.stockBuy("2025-02-10", "2377", 1_000, 190, "紀律加碼"));
        records.add(SimulatedTransRecord.stockBuy("2025-02-15", "2884", 10_000, 28, "現金過多，增加防禦部位"));
        records.add(SimulatedTransRecord.cashDividend("2025-05-20", "2330", 24_000, "上半年季配息概算"));

        return Collections.unmodifiableList(records);
    }
}
