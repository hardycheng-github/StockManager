# 2026 年更新計畫 Phase2（工程開發文件）

> 文件定位：內部開發者參考（Implementation-Oriented）
>
> 對應計畫文件：`demo2026/2026_Phase2.md`
>
> 範圍：金融邏輯（技術指標、突破偵測、通知）與 AI/智慧分析模組（TA 分數引擎、參數驗證流程）

---

## 1. 文件目的與邊界

### 1.1 目的

本文件用於說明 Phase2 在目前專案中的實作細節，包含：

- 實際已落地的模組與程式責任分工
- 關鍵資料流與執行流程
- 名詞定義（避免跨人員溝通歧義）
- 與原始目標的差距（Gap）
- 後續可直接承接的開發方向

### 1.2 邊界

本文件只涵蓋 Phase2 直接相關內容，不重複說明：

- 通用 UI 架構
- 非金融邏輯功能（如新聞、營收頁細節）
- 歷史版本設計決策（若無影響目前程式行為）

---

## 2. Phase2 目標與現況

## 2.1 原始目標（計畫層）

Phase2 計畫定義：

1. 開發 MACD 運算服務
2. 開發股價突破偵測演算法
3. 使用 AI 進行多參數測試，建立可快速反應的訊號觸發機制

## 2.2 現況摘要（工程層）

目前專案已具備：

- `MACD` 計算能力（`QuotaUtil.initMACD`）
- `MA`（移動平均線）突破/跌破檢測服務（`MaBreakthroughService`）
- 通知資料層（`NotifyEntity` + Room DAO/Repository）
- TA 指標分數引擎（RSI/PPO/Williams %R 綜合分數，`TaApi`）
- 設定頁的 MA 關注等級配置（LOW/DEFAULT/HIGH）

本階段已達成的目標邊界：

- 已完成 `MACD` 計算能力與 `MA` 事件偵測通知流程
- 已具備 AI/TA 參數驗證入口（`TaTestActivity`）
- 已形成可持續優化的 Phase2 技術基底

---

## 3. 模組總覽與責任切分

## 3.1 模組地圖

### A. 技術指標計算層

- `app/src/main/java/com/msi/stockmanager/kline/QuotaUtil.java`
- 職責：計算 MA、EMA、BOLL、MACD、KDJ、RSI 等指標

### B. 智慧分析分數層

- `app/src/main/java/com/msi/stockmanager/data/analytics/ITaApi.kt`
- `app/src/main/java/com/msi/stockmanager/data/analytics/TaApi.kt`
- 職責：將歷史資料轉成可評分的 TA 指標，產出 UI 可用的分數 map

### C. 事件偵測與通知生成層

- `app/src/main/java/com/msi/stockmanager/data/notify/MaBreakthroughService.java`
- `app/src/main/java/com/msi/stockmanager/data/notify/MaBreakthroughConfig.java`
- `app/src/main/java/com/msi/stockmanager/data/notify/MaAlertLevel.java`
- 職責：偵測 MA 穿越事件、組裝通知內容、做重複事件去重、寫入通知庫

### D. 通知資料層

- `app/src/main/java/com/msi/stockmanager/data/notify/NotifyEntity.kt`
- `app/src/main/java/com/msi/stockmanager/data/notify/NotifyDao.kt`
- `app/src/main/java/com/msi/stockmanager/data/notify/INotifyRepository.kt`
- `app/src/main/java/com/msi/stockmanager/data/notify/NotifyRepository.kt`
- 職責：通知 CRUD、未讀計數、軟刪除、事件去重查詢

### E. 入口與觸發層

- `app/src/main/java/com/msi/stockmanager/ui/main/overview/OverviewActivity.java`
- `app/src/main/java/com/msi/stockmanager/data/ApiUtil.java`
- `app/src/main/java/com/msi/stockmanager/data/profile/Profile.java`
- `app/src/main/java/com/msi/stockmanager/ui/main/setting/SettingsActivity.java`
- 職責：初始化 API、讀取設定、在畫面生命週期觸發事件檢測、提供參數設定

### F. 參數驗證入口（內部測試性質）

- `app/src/main/java/com/msi/stockmanager/TaTestActivity.kt`
- 職責：針對歷史資料進行分數與未來報酬率的關聯驗證（研究用途）

---

## 4. 核心流程（資料流 + 事件流）

## 4.1 應用啟動與模組初始化

1. `LaunchActivity` 內呼叫 `ApiUtil.init(context)`  
2. `ApiUtil` 建立 `stockApi/transApi/newsApi/taApi/revenueApi/notifyRepository`  
3. `Profile.load(context)` 載入使用者偏好，包含 `maAlertLevel`  

意義：

- 保證 Phase2 所需元件可透過 `ApiUtil` 單點取得
- MA 事件偵測可以讀到使用者設定的靈敏度

## 4.2 MA 突破偵測與通知落地流程

1. `OverviewActivity` 在 `ON_START` 呼叫 `MaBreakthroughService.checkWatchingList(...)`
2. 服務從 `revenueApi.getWatchingList()` 取觀察股票清單
3. 讀取 `Profile.maAlertLevel`，轉成要檢測的 MA 日數（5/10/30）
4. 逐檔股票抓歷史資料（區間 `1mo`, 週期 `1d`）
5. 轉成 `KData` 後呼叫 `QuotaUtil.initMa(...)` 計算 MA
6. 比對相鄰兩日價格與 MA 關係，判斷突破/跌破
7. 產生 `notifyType`（例如 `MA10_BREAKTHROUGH`），先做去重查詢
8. 未命中重複事件時插入 `NotifyEntity`
9. `OverviewActivity` 的通知 UI 透過 `notifyRepository.getList()/getUnreadCount()` 即時更新

## 4.3 TA 分數流程（智慧分析）

1. 呼叫端（如 `AnalysisActivity`、`AnalysisAdapter`、`AccountUtil`）提供 `List<StockHistory>`
2. `TaApi.getAllIndicatorLastScores(...)` 建立 `BarSeries`
3. 使用 ta4j 計算：
   - RSI（動能）
   - PPO（趨勢）
   - Williams %R（買賣壓力）
4. 將原始值映射為 `0~100` 分數
5. 依權重組合 `total = (RSI + PPO*2 + WR) / 4`
6. 回傳 map：`KEY_RSI`, `KEY_PPO`, `KEY_WILLIAMS_R`, `KEY_TOTAL`

---

## 5. 重要程式區塊說明（名詞 + 架構 + 行為）

## 5.1 `QuotaUtil`（技術指標引擎）

### 名詞解釋

- `MA`（Moving Average）：一定期間的收盤價平均
- `EMA`（Exponential MA）：近期資料權重更高的移動平均
- `MACD`：由快慢 EMA 差值推導出的趨勢動能指標
  - `DIF`：快 EMA - 慢 EMA
  - `DEA`：DIF 的平滑線（訊號線）
  - `MACD` 柱：常見為 `2 * (DIF - DEA)`

### 架構定位

- 提供「純計算」能力，不負責資料抓取與通知
- 被 `KLine` 視覺化與偵測服務重用

### 程式行為要點

- `initMa`：寫入 `KData` 的 MA5/10/30 值
- `initMACD`：依 fast/slow/signal 參數迭代寫入 DIF/DEA/MACD
- 透過 `isEndData` 控制增量/重算策略（避免不必要重計）

## 5.2 `TaApi` / `ITaApi`（TA 分數抽象）

### 名詞解釋

- `ITaApi`：技術分析服務介面
- `Callback: SingleObserver<Map<String, Int>>`：以 Rx 回傳單次評分結果
- `Score Mapping`：將不同尺度指標壓成可比較的 0~100

### 架構定位

- 對上提供統一「評分 API」
- 對下封裝 ta4j 實作細節
- 呼叫端不需知道每個技術指標計算方式，只讀分數結果

### 程式行為要點

- 使用 `Schedulers.computation()` 計算，主執行緒接收結果
- `PPO` 與 `Williams %R` 有額外分數轉換規則
- `KEY_TOTAL` 目前為靜態加權，不是機器學習模型輸出

## 5.3 `MaBreakthroughService`（事件偵測核心）

### 名詞解釋

- `Breakthrough`：價格由 MA 下方穿越至上方（看多訊號）
- `Breakdown`：價格由 MA 上方跌破至下方（看空訊號）
- `Idempotency`（冪等）：同一事件不重複寫入通知

### 架構定位

- Phase2 事件觸發主軸（目前是 MA 事件）
- 串接 Stock API、Profile、通知儲存層

### 程式行為要點

- 事件判斷基於「前一日 vs 當日」交叉關係
- 一次掃描整個月資料，可補抓歷史未建立事件
- 寫入前透過 `type + payload + date` 去重

## 5.4 `MaBreakthroughConfig` / `MaAlertLevel`（策略配置）

### 名詞解釋

- `Alert Level`：事件敏感度等級
  - `LOW`：僅 MA30（訊號較少，偏保守）
  - `DEFAULT`：MA10 + MA30
  - `HIGH`：MA5 + MA10 + MA30（訊號較多，偏積極）

### 架構定位

- 將「策略參數」與「偵測流程」解耦
- 便於後續新增更多週期或不同規則

## 5.5 `Notify*`（通知資料模型）

### 名詞解釋

- `read`：已讀狀態
- `deleted`：軟刪除狀態（保留資料、不顯示）
- `actionType/actionPayload`：通知點擊後行為路由預留欄位

### 架構定位

- Room + Repository 模式，對 UI 提供 reactive 資料流
- 支援 badge 未讀數、列表、刪除與全已讀

### 程式行為要點

- `getList()` 回傳 `Flowable<List<NotifyEntity>>` 可即時更新 UI
- `findByTypeAndPayloadAndDate(...)` 是事件去重關鍵

## 5.6 `OverviewActivity`（Phase2 實際觸發點）

### 架構定位

- 使用者進入總覽頁時進行通知資料更新與事件掃描

### 程式行為要點

- `ON_START` 觸發 `MaBreakthroughService.checkWatchingList`
- 同步更新通知清單與未讀徽章
- 目前為前景觸發，不是背景排程

## 5.7 `TaTestActivity`（研究型驗證工具）

### 名詞解釋

- `short/long/total valid days`：評分後驗證報酬率的觀察視窗
- `calcLoss`：評分方向與實際漲跌是否相反（簡化錯誤判準）

### 架構定位

- 提供「策略效果抽樣驗證」能力
- 本階段定位為驗證用途，作為後續優化與擴展的基礎模組

---

## 6. 設計選型與技術決策

## 6.1 為何使用 ta4j

- 可快速得到多種成熟技術指標，降低自行實作錯誤風險
- 指標組裝彈性高，後續可擴展更多評分維度

## 6.2 為何通知層採 Room + RxJava

- 本地持久化可保留事件歷史
- Rx 流可直接驅動 UI 更新，降低手動同步成本

## 6.3 目前「AI」定義

現況的「AI 模組」偏向：

- 技術指標加權評分（Heuristic/Rule-based）
- 非深度學習模型推論
- 非雲端 LLM 決策

此定義請在對外溝通時明確區分，避免誤解為模型訓練系統。

---

## 7. 穩定化與優化重點

## 7.1 事件命名與溝通一致性

- 目前實作主體為 `MA` 突破/跌破通知；`MACD` 已完成運算能力
- 對內文件建議明確區分：`MA event` 與 `MACD calculation`

## 7.2 觸發時機限制

- 目前依賴 `OverviewActivity ON_START`，後續可持續擴大觸發覆蓋面

## 7.3 分析流程潛在 null 路徑

- `AnalysisActivity`/`AnalysisAdapter` 可持續補強資料防呆與測試覆蓋，提升評分流程穩定度

## 7.4 去重策略限制

- 目前事件日去重策略可有效避免重複插入；若後續導入分時訊號，可再升級為更細粒度 key 設計

---

## 8. 後續工程建議（Phase2 收尾）

> 下列項目以「收尾驗收與穩定性」為主；延伸功能不列為本階段必做。

## 8.1 測試與驗證補強

- 單元測試：事件判斷邏輯（突破/跌破、交叉）
- 整合測試：通知去重與列表更新
- 回歸測試：設定檔影響（LOW/DEFAULT/HIGH）

## 8.2 文件與交接完備

- 完成對上回報文件與工程文件一致性檢查
- 補齊操作說明與驗收結果紀錄，銜接 Phase3

## 8.3 延伸功能（非本階段必做）

- `MACD` 事件通知化（黃金交叉/死亡交叉）
- 參數實驗平台化（參數管理、結果存檔、基準比較、自動選參）
- 背景排程觸發（WorkManager）

---

## 9. 名詞對照表（內部共識版）

- `TA`：Technical Analysis，技術分析
- `Indicator`：技術指標（RSI/PPO/MACD...）
- `Signal`：由指標或價格關係推導出的交易訊號
- `Breakthrough`：向上穿越（通常偏多）
- `Breakdown`：向下跌破（通常偏空）
- `Golden Cross`：短期線向上穿越長期線（常見看多）
- `Death Cross`：短期線向下穿越長期線（常見看空）
- `Heuristic`：規則式評分，非學習型模型
- `Idempotent`：重複執行不改變結果（去重目標）

---

## 10. 文件維護規範

每次調整以下任一項目，需同步更新本文件：

1. 指標計算公式或參數
2. 事件定義（突破/交叉條件）
3. 通知資料結構與去重鍵
4. 觸發時機（前景/背景）
5. AI 分數權重與輸出欄位

建議在 PR 描述附上：

- 「本次是否影響 Phase2 文件」檢核欄位（Yes/No）
- 若 Yes，列出更新章節編號（例如：`5.3`, `7.2`）

