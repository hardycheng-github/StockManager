package com.msi.stockmanager.data.demo;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SimulatedDataImporter {

    public static final class ImportResult {
        public final int successCount;
        public final int failCount;

        public ImportResult(int successCount, int failCount) {
            this.successCount = successCount;
            this.failCount = failCount;
        }
    }

    public ImportResult importAll() {
        List<SimulatedTransRecord> sorted = new ArrayList<>(SimulatedPortfolioData.entries());
        sorted.sort(Comparator.comparing(r -> r.date));

        int ok = 0;
        int fail = 0;
        for (SimulatedTransRecord record : sorted) {
            Transaction trans = SimulatedTransactionFactory.build(record);
            if (ApiUtil.transApi.addTrans(trans) >= 0) {
                ok++;
            } else {
                fail++;
            }
        }
        return new ImportResult(ok, fail);
    }
}
