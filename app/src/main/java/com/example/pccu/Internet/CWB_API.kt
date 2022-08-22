package com.example.pccu.Internet

import com.example.pccu.BuildConfig
import okhttp3.ResponseBody
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * 中央氣象局_API InterFace "class"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class CWB_API{
    /**
     * 36小時內天氣預報
     * @return CWB_API氣象預報資料 : [CwbWeatherSource]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun GetWeatherForecast(): CwbWeatherSource? {
        //API 主網域
        val Url = "https://opendata.cwb.gov.tw/"
        //設置條件
        val key = BuildConfig.CWB_API_KEY

        //回傳取得的CWB 36小時天氣預報資料
        return HttpRetrofit.create_Json(ApiServce::class.java,Url).getCWB(key).execute().body()
    }

    //CWB_API時間格式
    val TimeFormat = "yyyy-MM-dd HH:mm:ss"

    /**
     * 重構天氣預報資料
     * @param Source [CwbWeatherSource] CWB_API氣象預報資料
     * @return 氣象預報資料 : [CwbForecast_Save] 存儲用氣象預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun refactorCwbSource(Source:CwbWeatherSource):CwbForecast_Save{
        val CwbForecastSaveState = CwbForecast_Save(
            SimpleDateFormat(TimeFormat).parse(Source.records.location[0].weatherElement[0].time[0].endTime)!!,
            arrayListOf()
        )

        for(i in Source.records.location.indices){
            val locationForecast = Source.records.location[i]
            val forecast = CwbForecast(
                locationForecast.locationName,
                arrayListOf()
            )
            for(j in 0 until 3) {
                val forecastValue = TimePeriodForecast(
                    SimpleDateFormat(TimeFormat).parse(locationForecast.weatherElement[0].time[j].startTime)!!,
                    SimpleDateFormat(TimeFormat).parse(locationForecast.weatherElement[0].time[j].endTime)!!,
                    locationForecast.weatherElement[0].time[j].parameter,
                    locationForecast.weatherElement[1].time[j].parameter,
                    locationForecast.weatherElement[2].time[j].parameter,
                    locationForecast.weatherElement[3].time[j].parameter,
                    locationForecast.weatherElement[4].time[j].parameter
                )
                forecast.timePeriod.add(forecastValue)
            }
            CwbForecastSaveState.CwbForecast.add(forecast)
        }

        return CwbForecastSaveState
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
data class field(
    val id : String,
    val type : String
) : Serializable

/**
 * 資料的結構 -數據結構
 * @param resource_id  [String] 資料源ID
 * @param fields [String] 各資料的結構
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class Result(
    val resource_id : String,
    val fields : ArrayList<field>
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
 * @param parameter  ArrayList<[weatherElement]> 預測資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CWB_TimePeriod_WeatherData(
    val startTime : String,
    val endTime : String,
    val parameter : Parameter
) : Serializable

/**
 * 氣象資料 -數據結構
 * @param elementName    [String] 資料名稱
 * @param time           ArrayList<[weatherElement]> 時間段
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class weatherElement(
    val elementName : String,
    val time : ArrayList<CWB_TimePeriod_WeatherData>
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
    val locationName : String,
    val weatherElement : ArrayList<weatherElement>
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
    val datasetDescription : String,
    val location : ArrayList<Location>
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
data class CwbWeatherSource(
    val success : Boolean,
    val result : Result,
    val records : Record
) : Serializable

/*-----------------------存儲用cwb天氣預報資料結構-----------------------*/

/**
 * 時間段預報資料 -數據結構
 * @param startTime         [Date] 成功
 * @param endTime           [Date] 資料的結構
 *
 * @param weather_context   [Parameter] 紀錄資料
 * @param rainChance        [Parameter] 紀錄資料
 * @param minTemperature    [Parameter] 紀錄資料
 * @param comfort           [Parameter] 紀錄資料
 * @param maxTemperature    [Parameter] 紀錄資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class TimePeriodForecast(

    val startTime : Date,
    val endTime : Date,

    val weather_context : Parameter,
    val rainChance : Parameter,
    val minTemperature : Parameter,
    val comfort : Parameter,
    val maxTemperature : Parameter

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
    val Location : String,
    val timePeriod : ArrayList<TimePeriodForecast>
) : Serializable

/**
 * CWB預報資料 存儲用 -數據結構
 * @param endTime      [Date] 最早結束時間
 * @param CwbForecast    ArrayList<[CwbForecast]> 地區預報資料表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbForecast_Save(
    val endTime : Date,
    val CwbForecast : ArrayList<CwbForecast>
) : Serializable