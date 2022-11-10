package com.pccu.pccu.internet

import android.util.Log
import com.pccu.pccu.BuildConfig
import java.io.Serializable

/**
 * Google_行事曆_API InterFace "class"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
object CalendarAPI{

    /**文化大學Google行事曆連結*/
    const val PccuGoogleCalendarUrl =
        "https://calendar.google.com/calendar/u/0/embed?" +
        "ctz=Asia/Taipei" +
        "&showCalendars=0&showTabs=0" +
        "&showPrint=0" +
        "&src=pccu.edu.tw_blcke48f8jv7rd96hs8oeb06ro@group.calendar.google.com" +
        "&color=%23039BE5" +
        "&color=%2333B679" +
        "&color=%23F09300" +
        "&color=%230B8043" +
        "&color=%230B8043"

    /**
     * 取得PCCU Google行事曆 活動內容 (當日)
     * @return PccuGoogle行事曆活動內容 (當日) : [CalendarSource]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun get(Datum:String): CalendarSource? {
        //API 主網域
        val url = "https://www.googleapis.com/calendar/v3/calendars/"
        //設置條件
        val key = BuildConfig.API_KEY
        val timeMax = Datum+"T23:59:59Z"
        val timeMin = Datum+"T00:00:00Z"

        //回傳取得的今日行事曆
        return  try {
            HttpRetrofit.createJson(HttpRetrofit.ApiService::class.java,url).getCalendar(
                key,
                timeMax,
                timeMin
            ).execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("","$e")
            null
        }
    }
}

/**
 * 今日行事曆 -數據結構
 * @param kind              [String] 種類
 * @param etag              [String] 電子標籤
 * @param summary           [String] 概括
 * @param updated           [String] 更新
 * @param timeZone          [String] 時區
 * @param accessRole        [String] 訪問角色
 * @param defaultReminders  ArrayList<[String]> 默認提醒
 * @param nextSyncToken     [String] 下一個同步令牌
 * @param items             ArrayList<[Calendars]> 行事活動表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CalendarSource(
    val kind : String,
    val etag : String,
    val summary : String,
    val updated : String,
    val timeZone : String,
    val accessRole : String,
    val defaultReminders : ArrayList<String> = arrayListOf(),
    val nextSyncToken : String,
    val items : ArrayList<Calendars> = arrayListOf(),
)

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
    val kind : String,              //種類
    val etag : String,              //電子標籤
    val id : String,                //ID編號
    val status : String,            //狀態
    val htmlLink : String,          //HTML連結
    val created : String,           //創建
    val updated : String,           //更新
    val summary : String,           //摘要
    val creator : Creator,          //創建者
    val start : CalendarApiStart,   //開始
    val end : CalendarApiEnd,       //結束
    val iCalUID : String,           //校準UID
    val sequence : Int,             //序列
    val eventType : String          //事件類型
)

/**
 * 創造單位 -數據結構
 * @param email       [String] E-mail
 * @param displayName [String] 製作者
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class Creator(
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
data class CalendarApiStart(
    val date : String,
)

/**
 * 活動結束日期 -數據結構
 * @param date     [String] 日期
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CalendarApiEnd(
    val date : String,
)

/*-----------------------存儲用資料結構-----------------------*/

/**
 * 今日行事曆 -數據結構
 * @param updated   [String] 更新時間
 * @param items     Array<[Calendars]> 行事活動表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class ToDayCalendar(
    val updated : String,
    val items : ArrayList<Calendars> = arrayListOf(),
): Serializable