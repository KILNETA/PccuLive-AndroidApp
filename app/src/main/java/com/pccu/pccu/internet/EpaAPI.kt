package com.pccu.pccu.internet

import android.util.Log
import com.pccu.pccu.BuildConfig
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
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
    //private const val epaUrl = "https://www.epa.gov.tw/Index"

    /**
     * 連線獲取空污數據資料 並 重新設置視圖
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    /*
    fun get():String? {
        //空污數據資料
        return try {
            HttpRetrofit.createHTML(epaUrl, "utf-8")
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }
    */

    /**
     * 空氣品質預報資料
     * @return Moenv空氣品質預報資料 : [MoenvJson]
     *
     * @author KILNETA
     * @since Alpha_4.2
     */
    fun getAirQualityForecast(): MoenvJson? {
        //API 主網域
        val url = "https://data.moenv.gov.tw/"
        //設置條件
        val key = BuildConfig.MOENV_KEY

        //回傳取得的Moenv空氣品質預報資料
        return try {
            HttpRetrofit.createJson(HttpRetrofit.ApiService::class.java,url)
                .getAirQualityForecast(
                    key,
                    "publishtime desc",
                    //"forecastdate,eq,${todayDate}",
                    "31"    //最新四天的空汙預報 (40-9[外島僅有最近一次的預報])
                ).execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
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
    //private const val timeFormat = "yyyy/MM/dd HH:mm"

    /**
     * 取得EPA空汙預報資料
     * @param Html [String] 來源網址
     * @return EPA空汙預報資料 : [EpaAirQuality]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    /*
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
    */

    /**
     * 使用EpaAirQualityData結構乘載空汙預報資料
     * @param classification [String] 分類結構
     * @return EPA空汙預報資料 : [AnnouncementContent]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    /*
    private fun pushData(classification: Element) : EpaAirQualityData {
        //格式拆分
        /**空汙預報資料*/
        val data = classification.text().split(' ')
        //使用EpaAirQualityData結構乘載空汙預報資料
        return EpaAirQualityData( data[0], data[1], data[2].toInt() )
    }
    */
}

/**
 * EPA空氣品質(列表) -數據結構
 * @param upDate [Date] 更新時間
 * @param EPA_airQualityData ArrayList<[EpaAirQualityData]> 空氣品質預報表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
/*
data class EpaAirQuality(
    var upDate : Date? = null,
    val EPA_airQualityData : ArrayList<EpaAirQualityData> = ArrayList()
)
*/

/**
 * EPA空氣品質(分區) -數據結構
 * @param locationArea [String] 地區
 * @param airQualityName [String] 空氣品質描述
 * @param airQualityValue [Int] 空氣品質數值
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
/*
data class EpaAirQualityData(
    val locationArea : String? = null ,
    val airQualityName : String? = null ,
    val airQualityValue : Int? = null
)
*/

/**
 * moenv空氣品質(分區) -數據結構 --NEW--
 * @param fields            ArrayList<[MoenvFields]> 條目解釋與數據結構
 * @param resource_id       [String] 資源ID
 * @param __extras          [MoenvExtras] 額外資料
 * @param include_total     [Boolean] 是否資料總數都在本頁
 * @param total             [String] 總筆數
 * @param resource_format   [String] 資源格式
 * @param limit             [String] 最大筆數
 * @param offset            [String] 忽略筆數
 * @param _links            [MoenvLink] 查詢起始、結束頁面連結
 * @param records           ArrayList<[MoenvAirQualityForecast]> 空氣品質(分區)
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvJson(
    val fields : ArrayList<MoenvFields>? = null ,
    val resource_id : String? = null ,
    val __extras : MoenvExtras? = null,
    val include_total : Boolean? = null,
    val total : String? = null,
    val resource_format : String? = null,
    val limit : String? = null,
    val offset : String? = null,
    val _links : MoenvLink? = null,
    val records : ArrayList<MoenvAirQualityForecast>? = null
) : Serializable

/**
 * moenv條目解釋與數據結構 -數據結構 --NEW--
 * @param id         [String] 名稱ID
 * @param type       [String] 資料結構類別
 * @param info       [MoenvInfo] 條目中文名稱
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvFields(
    val id : String? = null,
    val type : String? = null,
    val info : MoenvInfo? = null
) : Serializable

/**
 * moenv條目中文名稱 -數據結構 --NEW--
 * @param label      [String] 條目名稱
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvInfo(
    val label : String? = null
) : Serializable


/**
 * moenv額外資料 -數據結構 --NEW--
 * @param api_key      [String] apiKey
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvExtras(
    val api_key : String? = null
) : Serializable

/**
 * moenv查詢起始、結束頁面連結 -數據結構 --NEW--
 * @param start      [String] 起始頁
 * @param next       [String] 結束頁
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvLink(
    val start : String? = null ,
    val next : String? = null
) : Serializable

/**
 * moenv空氣品質(分區) -數據結構 --NEW--
 * @param content           [String] 預報文字
 * @param publishtime       [String] 發布時間
 * @param area              [String] 空品區名稱
 * @param majorpollutant    [String] 主要污染物名稱
 * @param forecastdate      [String] 預報日期
 * @param aqi               [String] 空氣品質指標預報值
 * @param minorpollutant    [String] 次要污染物名稱
 * @param minorpollutantaqi [String] 次要污染物指標預報值
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvAirQualityForecast(
    val content : String,
    val publishtime : String,
    val area : String,
    val majorpollutant : String? = null,
    val forecastdate : String,
    val aqi : String,
    val minorpollutant : String? = null,
    val minorpollutantaqi : String? = null
) : Serializable

/**
 * Moenv空氣品質(列表) -數據結構
 * @param upDate [Date] 更新時間
 * @param EPA_airQualityData ArrayList<[EpaAirQualityData]> 空氣品質預報表
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvAirQuality(
    var upDate : Date? = null,
    var Moenv_airQualityData : ArrayList<MoenvAirQualityData> = ArrayList()
)

/**
 * moenv 空氣品質(分區) -數據結構 --NEW-- (整理僅保留需要的部分)
 * @param locationArea [String] 地區
 * @param airQualityValue [Int] 空氣品質數值
 *
 * @author KILNETA
 * @since Alpha_4.2
 */
data class MoenvAirQualityData(
    val locationArea : String? = null ,
    val airQualityValue : Int? = null
)