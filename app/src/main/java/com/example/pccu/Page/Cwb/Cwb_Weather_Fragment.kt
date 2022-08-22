package com.example.pccu.Page.Cwb

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pccu.Internet.TimePeriodForecast
import com.example.pccu.R
import kotlinx.android.synthetic.main.cwb_weather_item.*
import java.util.*

/**
 * 氣象預報分頁 頁面建構類 : "Fragment(cwb_weather_item)"
 * @param weatherForecast [TimePeriodForecast] 屬於該頁面顯示的預報資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class Cwb_Weather_Fragment (val weatherForecast : TimePeriodForecast?) : Fragment (R.layout.cwb_weather_item){

    /**
     * 氣象預報分頁 頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //來源資料非為空 -> 設置顯示資料
        if(weatherForecast != null) {
            //設置預報 日期
            Cwb_Date.text =
                DateFormat.format("MM/dd",weatherForecast.startTime).toString()
            //設置預報 時間區間
            Cwb_Time.text =
                DateFormat.format("HH:mm", weatherForecast.startTime).toString() +
                        "~" +
                DateFormat.format("HH:mm", weatherForecast.endTime).toString()
            //設置預報 降雨機率
            Cwb_rainChance.text = weatherForecast.rainChance.parameterName
            //設置預報 天氣描述
            Cwb_Weather_context.text = weatherForecast.weather_context.parameterName
            //設置預報 最小氣溫
            Cwb_Min_temperature.text = weatherForecast.minTemperature.parameterName
            //設置預報 最大氣溫
            Cwb_Max_temperature.text = weatherForecast.maxTemperature.parameterName
            //設置預報 體感描述
            Cwb_comfort.text = weatherForecast.comfort.parameterName

            //取得當前日期
            val Datum = DateFormat.format("MM/dd",Calendar.getInstance().time).toString()

            val startTime_MD = DateFormat.format("MM/dd",weatherForecast.startTime).toString()
            val endTime_MD = DateFormat.format("MM/dd",weatherForecast.endTime).toString()
            val startTime_H = DateFormat.format("HH", weatherForecast.startTime).toString().toInt()
            val endTime_H = DateFormat.format("HH", weatherForecast.endTime).toString().toInt()

            //篩選 預報時間描述 並設置
            if     ( Datum == startTime_MD && Datum == endTime_MD ) {
                if     ( startTime_H >= 0 && endTime_H <= 6 )
                    Cwb_DateContext.text="今日凌晨"
                else if( startTime_H >= 6 && endTime_H <= 18 )
                    Cwb_DateContext.text="今日白天"
                else if( startTime_H >= 18 && endTime_H < 24 )
                    Cwb_DateContext.text="今日晚上"
            }
            else if( Datum == startTime_MD && Datum != endTime_MD ) {
                if     ( startTime_H >= 0 && endTime_H <= 6 )
                    Cwb_DateContext.text="今晚明晨"
            }
            else if( Datum != startTime_MD && Datum != endTime_MD ) {
                if     ( startTime_H <= 6 && endTime_H <= 6 )
                    Cwb_DateContext.text="今晚明晨"
                else if( startTime_H >= 6 && endTime_H >= 18 )
                    Cwb_DateContext.text="明日白天"
                else if( startTime_H >= 18 && endTime_H < 24 )
                    Cwb_DateContext.text="明日晚上"
            }

            //取得氣象預報 天氣描述值
            val iconValue = weatherForecast.weather_context.parameterValue.toInt()

            //選擇匹配 天氣描述 的Icon並顯示
            if(iconValue in arrayOf(1))
                Cwb_Weather_image.setImageResource(R.drawable.sunny)
            else if(iconValue in arrayOf(2, 3))
                Cwb_Weather_image.setImageResource(R.drawable.sunny_cloudy)
            else if(iconValue in arrayOf(4, 5, 6))
                Cwb_Weather_image.setImageResource(R.drawable.muti_cloudy)
            else if(iconValue in arrayOf(7))
                Cwb_Weather_image.setImageResource(R.drawable.cloudy)
            else if(iconValue in arrayOf(8, 9, 10, 11, 12, 13, 14, 30))
                Cwb_Weather_image.setImageResource(R.drawable.cloudy_rain)
            else if(iconValue in arrayOf(15, 16, 17, 18, 33, 34))
                Cwb_Weather_image.setImageResource(R.drawable.cloudy_rain_thunder)
            else if(iconValue in arrayOf(19, 20, 21, 22, 29))
                Cwb_Weather_image.setImageResource(R.drawable.sunny_cloudy_rain)
            else if(iconValue in arrayOf(24, 25, 26, 27))
                Cwb_Weather_image.setImageResource(R.drawable.sunny_fog)
            else if(iconValue in arrayOf(28))
                Cwb_Weather_image.setImageResource(R.drawable.cloudy_fog)
            else if(iconValue in arrayOf(31, 32, 38, 39))
                Cwb_Weather_image.setImageResource(R.drawable.fog_rain )
            else if(iconValue in arrayOf(35, 36, 41))
                Cwb_Weather_image.setImageResource(R.drawable.fog_thunder)
            else if(iconValue in arrayOf(23, 37, 42))
                Cwb_Weather_image.setImageResource(R.drawable.cloudy_snow)
            else
                Cwb_Weather_image.setImageResource(R.drawable.question)
        }
    }
}