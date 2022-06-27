package com.example.pccu.Internet

import java.io.Serializable

/**
 * 連接PCCU的氣溫API
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Weather_API{

    /**
     * 取得 Weather_Data 資料
     * @return 氣溫資料 : [Weather_Data]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun Get(): List<Weather_Data>? {
        val Url = "https://api.pccu.edu.tw/" //API 主網域
        return HttpRetrofit.create_Json(ApiServce::class.java,Url).getWeather().execute().body()
    }
}

/**
 * PCCU氣溫API 資料結構
 * @param Location          [String] 地點
 * @param Tempature         [String] 溫度
 * @param Humidity          [String] 濕度
 * @param WindDirection     [String] 風向
 * @param WindSpeed         [String] 風速
 * @param Atmosph           [String] 氣壓
 * @param RainFall          [String] 雨量
 * @param UpdateTime        [String] 更新時間
 * @param InfoSource        [String] 信息來源
 * @param WeatherDesciption [String] 天氣描述
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class Weather_Data( //天氣資料 數據結構
    val Location: String,           //地點
    val Tempature: String,          //溫度
    val Humidity: String,           //濕度
    val WindDirection: String,      //風向
    val WindSpeed: String,          //風速
    val Atmosph: String,            //氣壓
    val RainFall: String,           //雨量
    val UpdateTime: String,         //更新時間
    val InfoSource: String,         //信息來源
    val WeatherDesciption: String   //天氣描述
): Serializable