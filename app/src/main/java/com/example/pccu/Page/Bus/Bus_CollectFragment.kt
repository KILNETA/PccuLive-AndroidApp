package com.example.pccu.Page.Bus

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.Internet.*
import com.example.pccu.Page.Bus.Search.Search_Activity
import com.example.pccu.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.bus_route_fragment.*
import kotlinx.android.synthetic.main.bus_station_item.view.*
import kotlinx.coroutines.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Bus_CollectFragment : Fragment(R.layout.bus_route_fragment) {

    data class StationData(
        val Station: Stop,
        var EstimateTime: Bus_Data_EstimateTime? = null,
        val innerBuses: ArrayList<Bus_Data_A2> = arrayListOf(),
    )

    var StationList : SaveBusList? = null

    /**倒計時器*/
    private var CountdownTimer: Timer? = null

    //初始化 計時器計數
    var Timer_i = 18 //計數

    //初始化 列表適配器
    var adapter = Adapter()

    /**
     * 更新內容用計時器 60s/次
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class CountdownTimerTask : TimerTask() {
        /**
         * 重構 計時器運作內容
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        @RequiresApi(Build.VERSION_CODES.N)
        override fun run() {
            when (Timer_i) {
                19 -> adapter.upData()
                20 -> Timer_i = 0
            }
            //增加計時緩衝條數值
            BusUpdataProgressBar.setProgress(++Timer_i, false)
        }
    }

    fun <T> fromJson(json: String, type: Type): T {
        return Gson().fromJson(json, type)
    }

    /**
     * bus_list_page建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //創建頁面
        super.onViewCreated(view, savedInstanceState)

        StationList = getArguments()!!.getSerializable("CollectList") as SaveBusList

        //創建Bus列表
        bus_StationList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器
        bus_StationList.adapter = adapter

    }

    /**
     * home_page頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStart() {
        super.onStart()
        //重新計數
        Timer_i = 19
        //初始化計時器
        CountdownTimer = Timer()
        //套用計時器設定
        CountdownTimer!!.schedule(CountdownTimerTask(), 500, 500)
    }

    /**
     * home_page當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop() {
        super.onStop()
        //關閉計時器 (避免持續計時導致APP崩潰)
        CountdownTimer!!.cancel()
        CountdownTimer = null //如果不重新new，会报异常
    }

    /**
     * Bus列表控件的適配器 "內部類"
     * @param Station List<[Bus_Data_Station]>              站點資訊
     * @param EstimateTime Array<[Bus_Data_EstimateTime]>   到站時間
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class Adapter : RecyclerView.Adapter<MyViewHolder>() {

        var TDX_token : TDX_token? = null

        val EstimateTime = arrayListOf<Bus_Data_EstimateTime>()

        fun upData(){
            GlobalScope.launch ( Dispatchers.Main ) {
                //TDX test
                TDX_token = withContext(Dispatchers.IO) {
                    Bus_API.Get_token()
                }

                EstimateTime.clear()

                for (i in StationList!!.SaveStationList.indices){

                    val station = StationList!!.SaveStationList[i]

                    val estimateTime : ArrayList<Bus_Data_EstimateTime> =
                        withContext(Dispatchers.IO) {
                            fromJson(
                                Bus_API.Get(
                                    TDX_token!!,
                                    "EstimatedTimeOfArrival",
                                    BUS_ApiRequest(
                                        station.RouteData.City,
                                        null,
                                        "RouteUID eq '${station.RouteData.RouteUID}' and StopUID eq '${station.StationUID}'"
                                    )
                                ).string(),
                                object : TypeToken<ArrayList<Bus_Data_EstimateTime>>() {}.type
                            )
                        }

                    EstimateTime.add(estimateTime[0])
                }

                //刷新視圖列表
                notifyDataSetChanged()
            }
        }

        /**
         * 重構 創建視圖持有者 (連結bus_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //加載布局於當前的context 列表控件元素bus_item
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_station_item, parent, false)
            return MyViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            return StationList!!.SaveStationList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            GlobalScope.launch ( Dispatchers.Main ) {
                val station = StationList!!.SaveStationList[position]

                holder.itemView.BusName.text = station.RouteData.RouteName.Zh_tw
                holder.itemView.DestinationStopName.text = "往${station.DestinationStopName}"
                holder.itemView.StationName.text = station.StationName.Zh_tw

                //設置元素子控件的點擊功能
                holder.itemView.setOnClickListener {
                    //轉換當前的頁面 至 公告內文頁面
                    //新方案 (新建Activity介面展示)
                    val IntentObj = Intent()
                    val Bundle = Bundle()
                    Bundle.putSerializable("RouteData", station.RouteData) //站點資料
                    Bundle.putSerializable("Direction", station.Direction) //站點資料
                    Bundle.putSerializable("StationUID", station.StationUID) //站點資料
                    IntentObj.putExtras(Bundle)
                    IntentObj.setClass(context!!, Bus_RoutePage::class.java )
                    startActivity(IntentObj)
                }

                if(position < EstimateTime.size){
                    val estimateTime = EstimateTime[position]

                    //如果剩餘時間 > 3min  {顯示剩餘時間}
                    if(estimateTime.EstimateTime>180) {
                        holder.itemView.TimeState.textSize = 22f
                        holder.itemView.EstimateTime.text = (estimateTime.EstimateTime / 60).toString()
                        holder.itemView.TimeMin.text = "分"
                        holder.itemView.TimeState.text = ""

                        //  剩餘時間 < 3min {剩餘時間欄位清空}
                    }else{
                        holder.itemView.TimeMin.text = ""
                        holder.itemView.EstimateTime.text = ""

                        //如果剩餘時間 < 3min && 正在 0:運營中 {即將進站}
                        if(estimateTime.EstimateTime in 5..180 && arrayOf(1,0).contains(estimateTime.StopStatus) ) {
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
                            val nextBusTime_Date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(estimateTime.NextBusTime)
                            holder.itemView.TimeState.setTextColor(Color.parseColor("#FFFFFF"))
                            holder.itemView.TimeState.textSize = 22f
                            holder.itemView.TimeState.text =
                                DateFormat.format("HH:mm", nextBusTime_Date!!.time).toString()
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
        }
    }
    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}