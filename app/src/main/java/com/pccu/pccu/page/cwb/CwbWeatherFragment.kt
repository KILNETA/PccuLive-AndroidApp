package com.pccu.pccu.page.cwb

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pccu.pccu.internet.TimePeriodForecast
import com.pccu.pccu.R
import java.util.*

/**
 * 氣象預報分頁 頁面建構類 : "Fragment(cwb_weather_item)"
 * @param weatherForecast [TimePeriodForecast] 屬於該頁面顯示的預報資料
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class CwbWeatherFragment (
    private val weatherForecast : TimePeriodForecast?
) : Fragment (R.layout.cwb_weather_item){

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
            this.view?.findViewById<TextView>(R.id.Cwb_Date)?.text =
                DateFormat.format("MM/dd",weatherForecast.startTime).toString()

            //設置預報 時間區間 (vvv 避免系統警告的寫法 vvv)
            /**預報時間範圍*/
            val date =
                DateFormat.format("HH:mm", weatherForecast.startTime).toString() +
                "~" +
                DateFormat.format("HH:mm", weatherForecast.endTime).toString()
            this.view?.findViewById<TextView>(R.id.Cwb_Time)?.text = date
            //設置預報 降雨機率
            this.view?.findViewById<TextView>(R.id.Cwb_rainChance)?.text = weatherForecast.rainChance.parameterName
            //設置預報 天氣描述
            this.view?.findViewById<TextView>(R.id.Cwb_Weather_context)?.text = weatherForecast.weather_context.parameterName
            //設置預報 最小氣溫
            this.view?.findViewById<TextView>(R.id.Cwb_Min_temperature)?.text = weatherForecast.minTemperature.parameterName
            //設置預報 最大氣溫
            this.view?.findViewById<TextView>(R.id.Cwb_Max_temperature)?.text = weatherForecast.maxTemperature.parameterName
            //設置預報 體感描述
            this.view?.findViewById<TextView>(R.id.Cwb_comfort)?.text = weatherForecast.comfort.parameterName

            //取得當前日期
            val presentDate = DateFormat.format("MM/dd",Calendar.getInstance().time).toString()
            //取得預報開始與結束日期時間
            val startTimeMD = DateFormat.format("MM/dd",weatherForecast.startTime).toString()
            val endTimeMD = DateFormat.format("MM/dd",weatherForecast.endTime).toString()
            val startTimeH = DateFormat.format("HH", weatherForecast.startTime).toString().toInt()
            val endTimeH = DateFormat.format("HH", weatherForecast.endTime).toString().toInt()

            val cwbDateContext = this.view?.findViewById<TextView>(R.id.Cwb_DateContext)
            //篩選 預報時間描述 並設置
            if     ( presentDate == startTimeMD && presentDate == endTimeMD ) {
                if     ( startTimeH >= 0 && endTimeH <= 6 )
                    cwbDateContext?.text="今日凌晨"
                else if( startTimeH >= 6 && endTimeH <= 18 )
                    cwbDateContext?.text="今日白天"
                else if( startTimeH >= 18 && endTimeH < 24 )
                    cwbDateContext?.text="今日晚上"
            }
            else if( presentDate == startTimeMD && presentDate != endTimeMD ) {
                if     ( startTimeH >= 0 && endTimeH <= 6 )
                    cwbDateContext?.text="今晚明晨"
            }
            else if( presentDate != startTimeMD && presentDate != endTimeMD ) {
                if     ( startTimeH <= 6 && endTimeH <= 6 )
                    cwbDateContext?.text="今晚明晨"
                else if( startTimeH >= 6 && endTimeH >= 18 )
                    cwbDateContext?.text="明日白天"
                else if( startTimeH >= 18 && endTimeH < 24 )
                    cwbDateContext?.text="明日晚上"
            }

            val cwbWeatherImage = this.view?.findViewById<ImageView>(R.id.Cwb_Weather_image)
            //選擇匹配 天氣描述 的Icon並顯示
            when (weatherForecast.weather_context.parameterValue.toInt()) {
                in arrayOf(1) ->
                    cwbWeatherImage?.setImageResource(R.drawable.sunny)
                in arrayOf(2, 3) ->
                    cwbWeatherImage?.setImageResource(R.drawable.sunny_cloudy)
                in arrayOf(4, 5, 6) ->
                    cwbWeatherImage?.setImageResource(R.drawable.muti_cloudy)
                in arrayOf(7) ->
                    cwbWeatherImage?.setImageResource(R.drawable.cloudy)
                in arrayOf(8, 9, 10, 11, 12, 13, 14, 30) ->
                    cwbWeatherImage?.setImageResource(R.drawable.cloudy_rain)
                in arrayOf(15, 16, 17, 18, 33, 34) ->
                    cwbWeatherImage?.setImageResource(R.drawable.cloudy_rain_thunder)
                in arrayOf(19, 20, 21, 22, 29) ->
                    cwbWeatherImage?.setImageResource(R.drawable.sunny_cloudy_rain)
                in arrayOf(24, 25, 26, 27) ->
                    cwbWeatherImage?.setImageResource(R.drawable.sunny_fog)
                in arrayOf(28) ->
                    cwbWeatherImage?.setImageResource(R.drawable.cloudy_fog)
                in arrayOf(31, 32, 38, 39) ->
                    cwbWeatherImage?.setImageResource(R.drawable.fog_rain )
                in arrayOf(35, 36, 41) ->
                    cwbWeatherImage?.setImageResource(R.drawable.fog_thunder)
                in arrayOf(23, 37, 42) ->
                    cwbWeatherImage?.setImageResource(R.drawable.cloudy_snow)
                else ->
                    cwbWeatherImage?.setImageResource(R.drawable.question)
            }
        }
    }
}