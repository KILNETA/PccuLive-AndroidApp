package com.pccu.pccu.internet

import android.util.Log
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 內政部環保署EPA_API "class"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
object EpaAPI{

    /**內政部環保署官網*/
    private const val epaUrl = "https://www.epa.gov.tw/Index"

    /**
     * 連線獲取空污數據資料 並 重新設置視圖
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    fun get():String? {
        //空污數據資料
        return try {
            HttpRetrofit.createHTML(epaUrl, "utf-8")
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }
}

/**
 * EPA空汙預報內容HTML解析器
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
object EpaHtmlParser{

    /**內政部環保署 更新時間格式*/
    private const val timeFormat = "yyy-MM-dd HH:mm"

    /**
     * 取得EPA空汙預報資料
     * @param Html [String] 來源網址
     * @return EPA空汙預報資料 : [EpaAirQuality]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun getContent(Html: String): EpaAirQuality {
        //初始化 空氣品質結構
        val airQuality = EpaAirQuality()
        //擷取至空氣品質主要資料內容區
        val document = Jsoup.parse(Html).select("body section div[class=weather_main]")//.outerHtml()
        val documentUpDate = Jsoup.parse(Html).select("body section div[class=Mainnews4] blockquote")[2]//.outerHtml()
        //計算資料的li總數來判斷 空氣品質的資料數 正常=10
        val trQuantity = document.outerHtml().split("<li").size-1

        //轉用Date結構乘載 空汙預報更新時間
        airQuality.upDate = SimpleDateFormat(timeFormat, Locale.TAIWAN).parse(documentUpDate.text())
        //民國記年 -> 西元紀年
        airQuality.upDate!!.year += 1911

        //遍歷各地區空汙預報資料 並儲存
        for( i in 0 until trQuantity)
            airQuality.EPA_airQualityData.add(pushData(document.select("li[class=item]")[i]))

        //回傳資料
        return airQuality
    }

    /**
     * 使用EpaAirQualityData結構乘載空汙預報資料
     * @param classification [String] 分類結構
     * @return EPA空汙預報資料 : [AnnouncementContent]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun pushData(classification: Element) : EpaAirQualityData {
        //格式拆分
        /**空汙預報資料*/
        val data = classification.text().split(' ')
        //使用EpaAirQualityData結構乘載空汙預報資料
        return EpaAirQualityData( data[0], data[1], data[2].toInt() )
    }
}

/**
 * EPA空氣品質(列表) -數據結構
 * @param upDate [Date] 更新時間
 * @param EPA_airQualityData ArrayList<[EpaAirQualityData]> 空氣品質預報表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class EpaAirQuality(
    var upDate : Date? = null,
    val EPA_airQualityData : ArrayList<EpaAirQualityData> = ArrayList()
)

/**
 * EPA空氣品質(分區) -數據結構
 * @param locationArea [String] 地區
 * @param airQualityName [String] 空氣品質描述
 * @param airQualityValue [Int] 空氣品質數值
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class EpaAirQualityData(
    val locationArea : String? = null ,
    val airQualityName : String? = null ,
    val airQualityValue : Int? = null
)