# MSI Stock Manager

一個簡單上手且兼具多功能便利性的股票記帳小幫手

* [👨‍🏫 APP Introduction](#-app-introduction)
* [👀 User Guide](#-user-guide)
* [🛠 Development Notes](#-development-notes)
* [🏃‍♀️ Next Stage](#%EF%B8%8F-next-stage)

## 👨‍🏫 APP Introduction

### 特色功能
* [簡單易懂的介面設計](#簡單易懂的介面設計)
* [視覺化資訊總覽](#視覺化資訊總覽)
* [交割帳戶管理](#交割帳戶管理)
* [股利與減資管理](#股利與減資管理)
* [漲跌顏色客製](#漲跌顏色客製)
* [實時股價對接](#實時股價對接)

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

## 👀 User Guide

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

### [Kotlin](https://kotlinlang.org/)

> Kotlin是一種在Java虛擬機上執行的靜態型別程式語言

作為一個基於Java的高階程式語言，Kotlin已躍升為Android Studio的主推官方語言，善用Kotlin特性可以大幅的降低開發成本。
本專案中使用了Java+Kotlin混和式開發，Kotlin可以與Java共生的性質讓入門學習者可以更容易的開始使用Kotlin。

### [Facebook Stetho](https://github.com/facebook/stetho)
> Stetho is a sophisticated debug bridge for Android applications

Android採用SQLite作為資料庫的預設開發選項，在除錯的方式上一直是開發人員的困擾，我們可以透過Facebook Stetho的強大功能之一來簡化資料庫除錯的難度。

### [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)



### [Jetpack Compose](https://developer.android.com/jetpack/compose?hl=zh-tw)



### [Compose EasyForms](https://github.com/k0shk0sh/ComposeEasyForms)



## 🏃‍♀️ Next Stage

* 交易資料的增加/刪除/修改 ✅
* 呈現庫存股票的總覽(交易資料的查詢) ✅
* 連結股票現值(計算個股損益/總損益) ✅
* <b>財經新聞與資訊彙整</b>
* <b>股票走勢圖與技術分析線圖</b>
* <b>個股投資分析建議</b>
