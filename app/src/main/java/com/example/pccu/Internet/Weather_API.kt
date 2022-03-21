package com.example.pccu.Internet

class Weather_API{
    fun Get(): List<Weather_Data>? {
        val Url = "https://api.pccu.edu.tw/"
        return HttpRetrofit.create(ApiServce::class.java,Url).getWeather().execute().body()
    }
}

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
)