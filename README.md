# MSI Stock Manager

一個簡單上手且兼具多功能便利性的股票記帳小幫手

* [APP Introduction](#-app-introduction)
* [User Guide](#-user-guide)
* [Development Notes](#-development-notes)
* [Next Stage](#%EF%B8%8F-next-stage)

## 👨‍🏫 APP Introduction

### 特色功能
* [簡單易懂的介面設計](#簡單易懂的介面設計)
* [視覺化資訊總覽](#視覺化資訊總覽)
* [交割帳戶管理](#交割帳戶管理)
* [股利與減資管理](#股利與減資管理)
* [漲跌顏色客製](#漲跌顏色客製)
* [實時股價對接](#實時股價對接)
* [財經新聞彙整](#財經新聞彙整)
* [走勢與技術線圖](#走勢與技術線圖)
* [智慧分析建議](#智慧分析建議)

### 簡單易懂的介面設計

直覺的使用者介面設計，初次使用也能快速上手

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/20220702_235413.gif" />

### 視覺化資訊總覽
投資分析圖表化，快速了解最新投資狀況

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/Screenshot_2022-07-02-13-33-03-788_com.msi.stockmanager.jpg" />

### 交割帳戶管理
資金記帳，投資比例與可用餘額一目瞭然

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/Screenshot_2022-07-02-13-40-29-918_com.msi.stockmanager.jpg" />

### 股利與減資管理
可管理股利與減資紀錄，投資損益更精準

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/Screenshot_2022-07-02-13-40-44-905_com.msi.stockmanager.jpg" />

### 漲跌顏色客製
隨喜好選擇漲跌顏色，最直覺的損益視覺呈現

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/20220704_150607.gif" />

### 實時股價對接
免去東查西找的手續，記帳時幫你帶入即時股價

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/20220704_140911.gif" />

### 財經新聞彙整
整合多方新聞來源，隨時掌握最新情報

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/Screenshot_2022-08-15-16-03-21-830_com.msi.stockmanager.jpg" />

### 走勢與技術線圖
歷史走勢與技術指標呈現，綜觀局勢專業分析

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/20220815_175607.gif" />

### 智慧分析建議
創新個股智慧分析，懶人投資福音

<img width="360" src="https://github.com/hardycheng-github/StockManager/blob/demo/demo/Screenshot_2022-08-15-15-58-40-562_com.msi.stockmanager.jpg" />


## 👀 User Guide

### 帳戶總覽

<img src="https://user-images.githubusercontent.com/55072235/177241041-746052a4-0358-4fc1-a9a5-2345cd3a4462.png" />

### 滑動選單頁面

<img src="https://user-images.githubusercontent.com/55072235/177242157-4ef4efb5-55ac-487e-b825-a1af4c6aa046.png" />

### 交易紀錄編輯

<img src="https://user-images.githubusercontent.com/55072235/177253702-affd5671-361c-4201-a96f-632e59ad2cc8.png" />

### 歷史紀錄搜尋

<img src="https://user-images.githubusercontent.com/55072235/177242175-65a7b08f-9e11-4744-9fc9-d8ab9b8d5845.png" />

### The Demo

使用情境的快速展示
* 視覺化總覽
* 滑動式選單
* 新增/編輯交易紀錄
* 歷史紀錄搜尋

[MSI Stock Manager Stage-1 Demo](https://user-images.githubusercontent.com/55072235/177121818-dd6a86d6-ea83-43dd-862a-5ef97e72e81f.mov)

## 🛠 Development Notes

跳脫舊時代的開發思維，在專案中使用強大的新型開發工具

* [Kotlin](#kotlin)
* [Facebook Stetho](#facebook-stetho)
* [MPAndroidChart](#mpandroidchart)
* [Jetpack Compose](#jetpack-compose)
* [Compose EasyForms](#compose-easyforms)
* [Ta4j](#ta4j)

### [Kotlin](https://kotlinlang.org/)

> Kotlin是一種在Java虛擬機上執行的靜態型別程式語言

作為一個基於Java的高階程式語言，Kotlin已經取代了Java躍升為Google的Android開發主推語言，善用Kotlin特性可以大幅的降低開發成本。

本專案中使用了Java+Kotlin混和式開發，Kotlin可以與Java共生的性質讓入門學習者可以更容易的開始使用Kotlin。

對於資深的Java開發者而言，也可直接在習慣的Java環境中呼叫Kotlin物件。

### [Facebook Stetho](https://github.com/facebook/stetho)

> Stetho is a sophisticated debug bridge for Android applications

Android採用SQLite作為資料庫的預設開發選項，在除錯的方式上一直是開發人員的困擾，而Facebook Stetho可作為資料庫除錯的解套方式，其強大的Database Inspection大大簡化了資料庫除錯的難度，我們可以透過Stetho串連手機與電腦，在Chrome上檢視資料庫架構、資料內容、甚至可以直接執行SQL指令。

### [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)

> ⚡ A powerful & easy to use chart library for Android ⚡

MPAndroidChart是最多人使用的圖表產生工具，可以輕鬆製作各種圖表

* 線性圖
* 離散圖
* 圓餅圖
* 組合圖表

另外這個第三方庫還有[IOS版本](https://github.com/danielgindi/Charts)

### [Jetpack Compose](https://developer.android.com/jetpack/compose?hl=zh-tw)

> Jetpack Compose 是 Android 的新型工具包，可用來建構原生 UI。這可簡化及加快 Android 平台上的 UI 開發作業。透過較少的程式碼、強大的工具和直觀的 Kotlin API，讓您的應用程式更貼近生活。

對於資深的Android開發者而言，可能無法輕易的將所有開發模式過渡到Compose，因為它擁有非常獨特的設計邏輯，但以使用者介面設計的角度而言，Compose是一個非常強大且簡潔的建構工具，設計模式採用[MATERIAL DESIGN](https://material.io/)，能夠以最少的程式碼完成極具質感的使用者介面。

Android使用者所熟知[Play商店](https://play.google.com/)，以及非常熱門的社群軟體[Twitter](https://twitter.com/)，最新的程式版本都是使用Compose開發建構。

考慮到Compose架構對於排列式設計的適用性，我們在專案中將交易編輯介面採用Compose設計 ([參考](#簡單易懂的介面設計))

### [Compose EasyForms](https://github.com/k0shk0sh/ComposeEasyForms)

> Focus on building your form UI while the library do the heavy work for you.

EasyForms是在Compose環境中使用的表單建構工具，幫助我們管理表單的狀態與驗證，讓開發者可以專注於設計美觀的界面。

股票交易編輯就需要管理眾多輸入內容的狀態，非常適合使用EasyForms來協助表單驗證。

### [Ta4j](https://github.com/ta4j/ta4j)

> Ta4j is an open source Java library for technical analysis. It provides the basic components for creation, evaluation and execution of trading strategies.

Ta4j是非常方便的投資工具，開發者可以在java環境中簡單的計算超過130種技術指標，在製作與分析技術線圖時搭配ta4j，可以大大提升開發效率。

## 🏃‍♀️ Next Stage

- [x] `交易資料的增加/刪除/修改`
- [x] `呈現庫存股票的總覽(交易資料的查詢)`
- [x] `連結股票現值(計算個股損益/總損益)`
- [x] `財經新聞與資訊彙整`
- [x] `股票走勢圖與技術分析線圖`
- [x] `個股投資分析建議`
