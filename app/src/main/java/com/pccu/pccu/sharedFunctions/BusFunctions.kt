package com.pccu.pccu.sharedFunctions

import android.graphics.Color
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pccu.pccu.R
import com.pccu.pccu.internet.EstimateTime
import kotlinx.android.synthetic.main.bus_station_item.view.*
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
        //如果剩餘時間 > 3min  {顯示剩餘時間}
        if(estimateTime.EstimateTime in 181..3599) {
            holder.itemView.TimeState.textSize = 22f
            holder.itemView.EstimateTime.text = (estimateTime.EstimateTime / 60).toString()
            holder.itemView.TimeMin.text = "分"
            holder.itemView.TimeState.text = ""

            //  剩餘時間 < 3min {剩餘時間欄位清空}
        }else{
            holder.itemView.TimeMin.text = ""
            holder.itemView.EstimateTime.text = ""

            if(estimateTime.EstimateTime >= 3600){
                val nextBusTimeDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.TAIWAN)
                        .parse(estimateTime.UpdateTime)
                nextBusTimeDate.seconds += estimateTime.EstimateTime
                holder.itemView.TimeState.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.TimeState.textSize = 22f
                holder.itemView.TimeState.text =
                    DateFormat.format("HH:mm", nextBusTimeDate!!.time).toString()
            }
            //如果剩餘時間 < 3min && 正在 0:運營中 {即將進站}
            else if(estimateTime.EstimateTime in 5..180 && arrayOf(1,0).contains(estimateTime.StopStatus) ) {
                holder.itemView.TimeState.setTextColor(Color.parseColor("#F5B939"))
                holder.itemView.TimeState.textSize = 18f
                holder.itemView.TimeState.text = "將到站"
            }
            //如果剩餘時間 < 5s && 正在 0:運營中 {進站中}
            else if(estimateTime.EstimateTime in 0..4 && arrayOf(0).contains(estimateTime.StopStatus) ) {
                holder.itemView.TimeState.setTextColor(Color.parseColor("#ff6363"))
                holder.itemView.TimeState.textSize = 18f
                holder.itemView.TimeState.text = "進站中"
            }
            //有顯示最近的發車時間
            else if(estimateTime.NextBusTime!=null){
                val nextBusTimeDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.TAIWAN)
                        .parse(estimateTime.NextBusTime)
                holder.itemView.TimeState.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.TimeState.textSize = 22f
                holder.itemView.TimeState.text =
                    DateFormat.format("HH:mm", nextBusTimeDate!!.time).toString()
            }

            //如果剩餘時間 < 5s && 不是 0:運營中 {未運營狀況}
            else if(estimateTime.EstimateTime<5 && estimateTime.StopStatus!=0){
                holder.itemView.TimeState.setTextColor(Color.parseColor("#7d7d7d"))
                //當前交通狀況 1:未發車 2:交管不停 3:末班已過 4:今日未營運
                when(estimateTime.StopStatus){
                    1->{holder.itemView.TimeState.textSize = 18f
                        holder.itemView.TimeState.text = "未發車"}
                    2->{holder.itemView.TimeState.textSize = 16f
                        holder.itemView.TimeState.text = "交管不停"}
                    3->{holder.itemView.TimeState.textSize = 16f
                        holder.itemView.TimeState.text = "末班已過"}
                    4->{holder.itemView.TimeState.textSize = 18f
                        holder.itemView.TimeState.text = "未營運"}
                }
            }
        }
    }
}