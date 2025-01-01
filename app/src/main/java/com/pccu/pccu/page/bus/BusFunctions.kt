package com.pccu.pccu.page.bus

import android.graphics.Color
import android.text.format.DateFormat
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pccu.pccu.R
import com.pccu.pccu.internet.EstimateTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * 公車系統 共用函式
 * @author KILNETA
 * @since Alpha_5.0
 */
object BusFunctions{

    /**
     * 公車系統 共用函式
     * @param holder [RecyclerView.ViewHolder]  視圖控件持有者
     * @param estimateTime [EstimateTime]       預估到站資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun setEstimateTimeView(holder: RecyclerView.ViewHolder, estimateTime: EstimateTime){
        val timeState = holder.itemView.findViewById<TextView>(R.id.TimeState)
        val estimateTimeTextView = holder.itemView.findViewById<TextView>(R.id.EstimateTime)
        val timeMin = holder.itemView.findViewById<TextView>(R.id.TimeMin)
        //如果剩餘時間 > 3min  {顯示剩餘時間}
        if(estimateTime.EstimateTime in 181..3599) {
            timeState.textSize = 22f
            estimateTimeTextView.setTextColor(Color.parseColor("#FFFFFF"))
            estimateTimeTextView.text = (estimateTime.EstimateTime / 60).toString()
            timeMin.text = "分"
            timeState.text = ""

            //  剩餘時間 < 3min {剩餘時間欄位清空}
        }else{
            timeMin.text = ""
            estimateTimeTextView.text = ""

            if(estimateTime.EstimateTime >= 3600){
                val nextBusTimeDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.TAIWAN)
                        .parse(estimateTime.UpdateTime)
                nextBusTimeDate.seconds += estimateTime.EstimateTime
                timeState.setTextColor(Color.parseColor("#FFFFFF"))
                timeState.textSize = 22f
                timeState.text =
                    DateFormat.format("HH:mm", nextBusTimeDate!!.time).toString()
            }
            //如果剩餘時間 < 3min && 正在 0:運營中 {即將進站}
            else if(estimateTime.EstimateTime in 20..180 && arrayOf(1,0).contains(estimateTime.StopStatus) ) {
                timeState.setTextColor(Color.parseColor("#F5B939"))
                timeState.textSize = 18f
                timeState.text = "將到站"
            }
            //如果剩餘時間 < 5s && 正在 0:運營中 {進站中}
            else if(estimateTime.EstimateTime in 0..19 && arrayOf(0).contains(estimateTime.StopStatus) ) {
                timeState.setTextColor(Color.parseColor("#ff6363"))
                timeState.textSize = 18f
                timeState.text = "進站中"
            }
            //有顯示最近的發車時間
            else if(estimateTime.NextBusTime!=null){
                val nextBusTimeDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.TAIWAN)
                        .parse(estimateTime.NextBusTime)
                timeState.setTextColor(Color.parseColor("#FFFFFF"))
                timeState.textSize = 22f
                timeState.text =
                    DateFormat.format("HH:mm", nextBusTimeDate!!.time).toString()
            }

            //如果剩餘時間 < 5s && 不是 0:運營中 {未運營狀況}
            else if(estimateTime.EstimateTime<5 && estimateTime.StopStatus!=0){
                timeState.setTextColor(Color.parseColor("#7d7d7d"))
                //當前交通狀況 1:未發車 2:交管不停 3:末班已過 4:今日未營運
                when(estimateTime.StopStatus){
                    1->{timeState.textSize = 18f
                        timeState.text = "未發車"}
                    2->{timeState.textSize = 16f
                        timeState.text = "交管不停"}
                    3->{timeState.textSize = 16f
                        timeState.text = "末班已過"}
                    4->{timeState.textSize = 18f
                        timeState.text = "未營運"}
                }
            }
        }
    }
}