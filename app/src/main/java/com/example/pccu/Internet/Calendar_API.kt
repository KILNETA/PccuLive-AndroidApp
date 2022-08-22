package com.example.pccu.Internet

import android.content.Context
import com.example.pccu.BuildConfig
import com.example.pccu.R
import java.io.Serializable

/*
import android.content.Context
import com.google.api.client.util.DateTime
import java.util.*
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

import android.content.res.AssetManager
import android.util.Log
import com.google.api.client.auth.openidconnect.IdTokenResponse.execute
import com.google.api.services.calendar.model.Events
import java.io.InputStream
import com.google.api.client.json.jackson2.JacksonFactory

import com.google.api.client.extensions.android.http.AndroidHttp

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
*/

/**
 * Google_行事曆_API InterFace "class"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class Calendar_API{
    /**
     * 取得指定"縣市","路線名稱"的公車動態定時資料(A1)"批次更新
     * @return 公車動態定時資料(A1)表 : List<[Bus_Data_A1]>
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun Get(Datum:String): CalendarSourse? {
        //API 主網域
        val Url = "https://www.googleapis.com/calendar/v3/calendars/"
        //設置條件
        val key = BuildConfig.API_KEY
        val timeMax = Datum+"T23:59:59Z"
        val timeMin = Datum+"T00:00:00Z"

        //回傳取得的今日行事曆
        return HttpRetrofit.create_Json(ApiServce::class.java,Url).getCalendar(key,timeMax,timeMin).execute().body()
    }

    /* 捨棄的API連接方案
    fun GetTEST(context: Context){
        val am: AssetManager = context.getAssets()
        val inputStream = am.open("key-file-name.json")

        var credential = GoogleCredential.fromStream(inputStream)

        credential =
            credential.createScoped(Arrays.asList("https://www.googleapis.com/calendar/v3/calendars/"))

        val client = Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory(),
            credential
        ).setApplicationName("someAppName").build()

        client.events().list("pccu.edu.tw_blcke48f8jv7rd96hs8oeb06ro@group.calendar.google.com")
            .setTimeMin(DateTime(Date(), TimeZone.getDefault()))
            .setMaxResults(5)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .setShowDeleted(false)
            .setKey("${BuildConfig.API_KEY}")    //("api-key-string_from_developer_console")
            .execute()

        Log.d("GetTEST",client.toString())
    }*/
}

/**
 * 今日行事曆 -數據結構
 * @param kind              [String] 種類
 * @param etag              [String] 電子標籤
 * @param summary           [String] 概括
 * @param updated           [String] 更新
 * @param timeZone          [String] 時區
 * @param accessRole        [String] 訪問角色
 * @param defaultReminders  Array<[String]> 默認提醒
 * @param nextSyncToken     [String] 下一個同步令牌
 * @param items             Array<[Calendars]> 行事活動表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CalendarSourse(
    val kind : String,
    val etag : String,
    val summary : String,
    val updated : String,
    val timeZone : String,
    val accessRole : String,
    val defaultReminders : Array<String>,
    val nextSyncToken : String,
    val items : Array<Calendars>,
)

/**
 * 今日行事曆 -數據結構
 * @param kind              [String] 種類
 * @param etag              [String] 電子標籤
 * @param summary           [String] 概括
 * @param updated           [String] 更新
 * @param timeZone          [String] 時區
 * @param accessRole        [String] 訪問角色
 * @param defaultReminders  Array<[String]> 默認提醒
 * @param nextSyncToken     [String] 下一個同步令牌
 * @param items             Array<[Calendars]> 行事活動表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class ToDayCalendar(
    val updated : String,
    val items : ArrayList<Calendars> = arrayListOf(),
): Serializable

/**
 * 行事活動 -數據結構
 * @param kind      [String] 種類
 * @param etag      [String] 電子標籤
 * @param id        [String] ID編號
 * @param status    [String] 狀態
 * @param htmlLink  [String] HTML連結
 * @param created   [String] 創建
 * @param updated   [String] 更新
 * @param summary   [String] 摘要
 * @param creator   [creator] 創建者
 * @param start     [start] 開始
 * @param end       [end] 結束
 * @param iCalUID   [String] 校準UID
 * @param sequence  [Int] 序列
 * @param eventType [String] 事件類型
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class Calendars(
    val kind : String,      //種類
    val etag : String,      //電子標籤
    val id : String,        //ID編號
    val status : String,    //狀態
    val htmlLink : String,  //HTML連結
    val created : String,   //創建
    val updated : String,   //更新
    val summary : String,   //摘要
    val creator : creator,  //創建者
    val start : start,      //開始
    val end : end,          //結束
    val iCalUID : String,   //校準UID
    val sequence : Int,     //序列
    val eventType : String  //事件類型
)

/**
 * 創造單位 -數據結構
 * @param email       [String] E-mail
 * @param displayName [String] 製作者
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class creator(
    val email : String,
    val displayName : String
)

/**
 * 活動開始日期 -數據結構
 * @param date     [String] 日期
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class start(
    val date : String,
)

/**
 * 活動結束日期 -數據結構
 * @param date     [String] 日期
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class end(
    val date : String,
)