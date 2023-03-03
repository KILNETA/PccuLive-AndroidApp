package com.pccu.pccu.internet

import android.util.Log
import com.pccu.pccu.BuildConfig
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * 中央氣象局_API InterFace "class"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
object CwbAPI{
    /**
     * 36小時內天氣預報
     * @return CWB_API氣象預報資料 : [CwbWeatherSource]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun getWeatherForecast(): CwbWeatherSource? {
        //API 主網域
        val url = "https://opendata.cwb.gov.tw/"
        //設置條件
        val key = BuildConfig.CWB_API_KEY

        //回傳取得的CWB 36小時天氣預報資料
        return  try {
            HttpRetrofit.createJson(HttpRetrofit.ApiService::class.java,url)
                .getCWB(
                    key
                ).execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //CWB_API時間格式
    private const val timeFormat = "yyyy-MM-dd HH:mm:ss"

    /**
     * 重構天氣預報資料
     * @param Source [CwbWeatherSource] CWB_API氣象預報資料
     * @return 氣象預報資料 : [CwbForecastSave] 存儲用氣象預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun refactorCwbSource(Source:CwbWeatherSource):CwbForecastSave{
        /**格式化 全台各縣市 各三筆預報*/
        val cwbForecastSaveState = CwbForecastSave(
            SimpleDateFormat(timeFormat, Locale.TAIWAN)
                .parse(Source.records.location[0].weatherElement[0].time[0].startTime)!!,
            SimpleDateFormat(timeFormat, Locale.TAIWAN)
                .parse(Source.records.location[0].weatherElement[0].time[0].endTime)!!,
            arrayListOf()
        )

        //遍歷所有地區
        for(i in Source.records.location.indices){
            /**該地區 API資料*/
            val locationForecast = Source.records.location[i]
            /**格式化 單地區 三筆預報*/
            val forecast = CwbForecast(
                locationForecast.locationName,
                arrayListOf()
            )
            //單地區存有三筆預報資料
            for(j in 0 until 3) {
                /**格式化 單地區 單筆預報*/
                val forecastValue = TimePeriodForecast(
                    SimpleDateFormat(timeFormat, Locale.TAIWAN)
                        .parse(locationForecast.weatherElement[0].time[j].startTime)!!,
                    SimpleDateFormat(timeFormat, Locale.TAIWAN)
                        .parse(locationForecast.weatherElement[0].time[j].endTime)!!,
                    locationForecast.weatherElement[0].time[j].parameter,
                    locationForecast.weatherElement[1].time[j].parameter,
                    locationForecast.weatherElement[2].time[j].parameter,
                    locationForecast.weatherElement[3].time[j].parameter,
                    locationForecast.weatherElement[4].time[j].parameter
                )
                //存入格式化氣象預報資料 單地區 單筆預報
                forecast.timePeriod.add(forecastValue)
            }
            //存入格式化氣象預報資料 單地區 三筆預報
            cwbForecastSaveState.CwbForecast.add(forecast)
        }
        //回傳氣象預報資料
        return cwbForecastSaveState
    }
}

/*-----------------------接收cwb天氣預報API資料結構-----------------------*/

/**
 * 各資料的結構 -數據結構
 * @param id  [String] 資料ID
 * @param type [String] 資料結構
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbApiField(
    val id : String,
    val type : String
) : Serializable

/**
 * 資料的結構 -數據結構
 * @param resource_id   [String] 資料源ID
 * @param fields        ArrayList<[CwbApiField]> 各資料的結構
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class Result(
    val resource_id : String,       //資料源ID
    val fields : ArrayList<CwbApiField>   //各資料的結構
) : Serializable

/**
 * 天氣預測資料 -數據結構
 * @param parameterName  [String] 描述
 * @param parameterValue [String] 數值
 * @param parameterUnit  [String] 單位
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class Parameter(
    val parameterName : String,     //天氣描述、溫度、降雨機率
    val parameterValue : String,    //單純紀錄(數值)編號
    val parameterUnit : String      //資料單位(百分比)
) : Serializable

/**
 * 時間段_天氣預測資料 -數據結構
 * @param startTime  [String] 開始時間
 * @param endTime    [String] 結束時間
 * @param parameter  ArrayList<[WeatherElement]> 預測資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbTimePeriodWeatherData(
    val startTime : String,     //開始時間
    val endTime : String,       //結束時間
    val parameter : Parameter   //預測資料
) : Serializable

/**
 * 氣象資料 -數據結構
 * @param elementName    [String] 資料名稱
 * @param time           ArrayList<[CwbTimePeriodWeatherData]> 時間段
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class WeatherElement(
    val elementName : String,                       //資料名稱
    val time : ArrayList<CwbTimePeriodWeatherData>  //時間段
) : Serializable

/**
 * 地點 -數據結構
 * @param locationName    [String] 資料地點
 * @param weatherElement  ArrayList<[weatherElement]> 氣象資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class Location(
    val locationName : String,                      //資料地點
    val weatherElement : ArrayList<WeatherElement>  //氣象資料
) : Serializable

/**
 * 紀錄資料 -數據結構
 * @param datasetDescription    [String] 紀錄名稱
 * @param location              ArrayList<[Location]> 地點
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class Record(
    val datasetDescription : String,    //紀錄名稱
    val location : ArrayList<Location>  //地點
) : Serializable

/**
 * 36小時氣象預報 -數據結構
 * @param success       [Boolean] 成功
 * @param result        [Result] 資料的結構
 * @param records       [Record] 紀錄資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbWeatherSource (
    val success : Boolean,  //成功
    val result : Result,    //資料的結構
    val records : Record    //紀錄資料
) : Serializable

/*-----------------------存儲用cwb天氣預報資料結構-----------------------*/

/**
 * 時間段預報資料 -數據結構
 * @param startTime         [Date] 起始時間
 * @param endTime           [Date] 結束時間
 *
 * @param weather_context   [Parameter] 氣象描述
 * @param rainChance        [Parameter] 降雨機率
 * @param minTemperature    [Parameter] 最小氣溫值
 * @param comfort           [Parameter] 舒適度描述
 * @param maxTemperature    [Parameter] 最大氣溫值
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class TimePeriodForecast(
    val startTime : Date,           //起始時間
    val endTime : Date,             //結束時間

    val weather_context : Parameter,//氣象描述
    val rainChance : Parameter,     //降雨機率
    val minTemperature : Parameter, //最小氣溫值
    val comfort : Parameter,        //舒適度描述
    val maxTemperature : Parameter  //最大氣溫值
) : Serializable

/**
 * 地區預報資料 -數據結構
 * @param Location      [String] 地區
 * @param timePeriod    ArrayList<[TimePeriodForecast]> 時間段預報資料表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbForecast(
    val Location : String,                          //地區
    val timePeriod : ArrayList<TimePeriodForecast>  //時間段預報資料表
) : Serializable

/**
 * CWB預報資料 存儲用 -數據結構
 * @param endTime       [Date] 最早結束時間
 * @param CwbForecast   ArrayList<[CwbForecast]> 地區預報資料表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbForecastSave(
    val stratTime : Date,                   //最早開始時間
    val endTime : Date,                     //最早結束時間
    val CwbForecast : ArrayList<CwbForecast>//地區預報資料表
) : Serializable