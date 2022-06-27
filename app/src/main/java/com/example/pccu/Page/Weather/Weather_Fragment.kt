package com.example.pccu.Page.Weather

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pccu.Internet.Weather_Data
import com.example.pccu.R
import kotlinx.android.synthetic.main.weather_item.*
import java.io.Serializable

/**
 * 用於調用的資料組 (數據包) : (Serializable 可序列化)
 * @param weather [Weather_Data] 氣象資料來源表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class ImageSources_Data(weather: Weather_Data) : Serializable {
    //轉存引入的資料至本地
    val weather: Weather_Data = weather //即時影像來源表

    /**調取氣象資料表*/
    fun GetWeather() : Weather_Data {
        return weather
    }
}

/**
 * 首頁 氣象列表-物件 片段建構類 : "Fragment(weather_fragment)"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class Weather_Fragment : Fragment(R.layout.weather_item) {

    var weather: Weather_Data? = null //即時影像來源

    /**
     * 設置參數 (導入頁面已存儲實例狀態)
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
    }

    /**
     * 設置新實例 "伴生對象" 用於轉存欲傳入的資料
     * @author KILNETA
     * @since Alpha_2.0
     */
    companion object {
        fun newInstance( weather: Weather_Data): Weather_Fragment {
            val Bundle = Bundle()
            val Sources = ImageSources_Data(weather) //打包資料組 (以備調用) { 即時影像來源 }
            Bundle.putSerializable("Weather", Sources) //即時影像來源
            return Weather_Fragment().apply{
                arguments = Bundle //回傳已建置的資料組 (數據包)
            }
        }
    }

    /**
     * Weather_Fragment頁面預建構 "先導入資料"
     * @param WeatherSources [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun onCreate(WeatherSources: Bundle?) {
        super.onCreate(WeatherSources) //WeatherSources 已保存實例狀態
        val args = getArguments() //導入 已建置的資料組 (數據包)
        if (args != null) {
            val Sources =
                args.getSerializable("Weather") as ImageSources_Data //反序列化 資料組
            weather = Sources.GetWeather()
        }
    }

    /**
     * 設置頁面顯示資料
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun setViewData(){
        weatherDesciption.text =
            if(weather!!.WeatherDesciption=="-99") "null"                           //天氣描述
            else weather!!.WeatherDesciption                                        //
        temperature.text = weather!!.Tempature                                      //溫度
        humidity.text = weather!!.Humidity                                          //濕度
        windSpeed.text = weather!!.WindSpeed                                        //風速
        rainFall.text = weather!!.RainFall                                          //雨量
        location.text =weather!!.Location                                           //資料來源
        if(weather!!.WeatherDesciption.contains("晴", ignoreCase = true))      //天氣狀態圖
            weather_image.setImageResource(R.drawable.sunny)                        //
        else if(weather!!.WeatherDesciption.contains("雨", ignoreCase = true)) //
            weather_image.setImageResource(R.drawable.cloudy_rain)                  //
    }

    /**
     * 更新頁面顯示資料
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun reUpData(weather: Weather_Data){
        this.weather = weather
        setViewData()
    }

    /**
     * Weather_Fragment建構頁面
     * @param view [View] 該頁面的父類
     * @param WeatherSources [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun onViewCreated(view: View, WeatherSources: Bundle?) {
        //創建頁面
        super.onViewCreated(view, WeatherSources)
        setViewData()
    }
}