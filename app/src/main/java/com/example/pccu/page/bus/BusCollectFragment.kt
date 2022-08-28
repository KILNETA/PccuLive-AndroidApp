package com.example.pccu.page.bus

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.internet.*
import com.example.pccu.R
import com.example.pccu.sharedFunctions.BusFunctions
import com.example.pccu.sharedFunctions.JsonFunctions
import com.example.pccu.sharedFunctions.RV
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.bus_route_fragment.*
import kotlinx.android.synthetic.main.bus_station_item.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusCollectFragment : Fragment(R.layout.bus_route_fragment) {

    /**收藏的群組*/
    var stationList : CollectGroup? = null
    /**倒計時器*/
    private var countdownTimer: Timer? = null
    /**計時器計數*/
    var timerI = 18 //計數
    /**列表適配器*/
    var adapter = Adapter()

    /**
     * 更新內容用計時器 20s/次
     * @author KILNETA
     * @since Alpha_5.0
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
            //增加計時緩衝條數值
            BusUpdataProgressBar.setProgress(++timerI, false)
            when (timerI) {
                19 -> adapter.upData()
                20 -> timerI = 0
            }
        }
    }

    /**
     * bus_route_fragment建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //創建頁面
        super.onViewCreated(view, savedInstanceState)

        stationList = arguments!!.getSerializable("CollectList") as CollectGroup

        //創建Bus列表
        bus_StationList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器
        bus_StationList.adapter = adapter

    }

    /**
     * bus_route_fragment頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStart() {
        super.onStart()
        //重新計數
        timerI = 18
        //初始化計時器
        countdownTimer = Timer()
        //套用計時器設定
        countdownTimer!!.schedule(CountdownTimerTask(), 500, 500)
    }

    /**
     * bus_route_fragment當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStop() {
        super.onStop()
        //關閉計時器 (避免持續計時導致APP崩潰)
        countdownTimer!!.cancel()
        countdownTimer = null //如果不重新new，會報異常
    }

    /**
     * Bus列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class Adapter : RecyclerView.Adapter<RV.ViewHolder>() {

        private var tdxToken : TdxToken? = null
        val estimateTime = arrayListOf<EstimateTime>()

        @DelicateCoroutinesApi
        fun upData(){
            GlobalScope.launch ( Dispatchers.Main ) {
                //清除舊資料
                estimateTime.clear()

                //TDX 取得協定
                tdxToken = withContext(Dispatchers.IO) {
                    BusAPI.getToken()
                }

                //依序取得收藏群組中的各站牌到站時間資料
                for (i in stationList!!.SaveStationList.indices){
                    /**指定位置的 路線資料*/
                    val station = stationList!!.SaveStationList[i]
                    /**到站時間表*/
                    val mEstimateTime : ArrayList<EstimateTime> =
                        withContext(Dispatchers.IO) {
                            JsonFunctions.fromJson(
                                BusAPI.get(
                                    tdxToken!!,
                                    "EstimatedTimeOfArrival",
                                    BusApiRequest(
                                        station.RouteData.City,
                                        null,
                                        "RouteUID eq '${station.RouteData.RouteUID}' and StopUID eq '${station.StationUID}'"
                                    )
                                ),
                                object : TypeToken<ArrayList<EstimateTime>>() {}.type
                            )
                        }
                    //取得最首項添入 到站時間表資料
                    estimateTime.add(mEstimateTime[0])
                }
                //刷新視圖列表
                notifyDataSetChanged()
            }
        }

        /**
         * 重構 創建視圖持有者 (連結bus_station_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載布局列表控件元素bus_station_item*/
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_station_item, parent, false)
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            return stationList!!.SaveStationList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {
            /**指定位置 收藏站牌資料*/
            val station = stationList!!.SaveStationList[position]

            //路線名子
            holder.itemView.BusName.text = station.RouteData.RouteName.Zh_tw
            /**控件展示之終點方向*/ //此寫法不會出現警告 vvv
            val text = "往${station.DestinationStopName}"
            holder.itemView.DestinationStopName.text = text
            //站牌名子
            holder.itemView.StationName.text = station.StationName.Zh_tw

            //避免出界
            if(position < estimateTime.size){
                /**到站時間*/
                val estimateTime = estimateTime[position]
                //按公車統一條件設置到站時間
                BusFunctions.setEstimateTimeView(holder,estimateTime)
            }

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //轉換當前的頁面 至 公車路線頁面
                /**目標視圖*/
                val intentObj = Intent()
                /**傳遞資料包*/
                val bundle = Bundle()
                bundle.putSerializable("RouteData", station.RouteData) //路線資料
                bundle.putSerializable("Direction", station.Direction) //返回程方向
                bundle.putSerializable("StationUID", station.StationUID) //站牌唯一序號
                intentObj.putExtras(bundle)
                //建構頁面 Bus_RoutePage
                intentObj.setClass(context!!, BusRoutePage::class.java )
                startActivity(intentObj)
            }
        }
    }
}