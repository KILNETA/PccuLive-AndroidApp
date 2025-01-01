package com.pccu.pccu.page.bus

import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.reflect.TypeToken
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.sharedFunctions.JsonFunctions
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.RV
import kotlinx.coroutines.*
import java.util.*

/**
 *
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
@DelicateCoroutinesApi
class BusCollectFragment : Fragment(R.layout.bus_route_fragment) {

    /**收藏的群組名*/
    private var groupName : String? = null
    /**收藏的群組*/
    var stationList : CollectGroup? = null
    /**倒計時器*/
    private var countdownTimer: Timer? = null
    /**計時器計數*/
    var timerI = 18 //計數
    /**列表適配器*/
    private var adapter = Adapter()

    private var busUpdataProgressBar : ProgressBar? = null

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
        override fun run() {
            //增加計時緩衝條數值
            busUpdataProgressBar?.setProgress(++timerI, false)
            when (timerI) {
                19 -> adapter.upData()
                20 -> timerI = 0
            }
        }
    }

    /**
     * 取得該頁面顯示之收藏站牌
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initCollectGroup(){
        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        val sp = Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>
        stationList = sp.first { it.GroupName == groupName }
    }

    /**
     * 建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //創建頁面
        super.onViewCreated(view, savedInstanceState)
        val busStationList = this.view?.findViewById<RecyclerView>(R.id.bus_StationList)

        (busStationList?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        //取得該頁展示之收藏群組名
        groupName = requireArguments().getString("CollectListGroupName")
        //創建Bus列表
        busStationList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器

        busStationList.adapter = adapter

        busUpdataProgressBar = this.view?.findViewById<ProgressBar>(R.id.BusUpdataProgressBar)
    }

    /**
     * bus_route_fragment頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStart() {
        super.onStart()
        initCollectGroup()
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
    @DelicateCoroutinesApi
    inner class Adapter : RecyclerView.Adapter<RV.ViewHolder>() {
        /**各站牌到站時間*/
        private var estimateTime = arrayListOf<EstimateTime?>()
        /**與主線程聯繫的Handler*/
        @Suppress("DEPRECATION")
        val mainHandler = Handler()
        /**HandlerThread實例化 線程名稱為(handlerThread)*/
        val mHandlerThread = HandlerThread("handlerThread")
        /**線程任務Handler*/
        private var workHandler : Handler

        init {
            //啟用HandlerThread
            mHandlerThread.start()
            //(須確保啟用HandlerThread後才可以執行mHandlerThread.looper的handleMessage定義)
            //workHandler定義
            workHandler = object : Handler(mHandlerThread.looper) {
                /**線程任務訊息處理*/
                override fun handleMessage(msg: Message) {
                    //判斷線程任務選擇
                    if(msg.what == 1) {
                        /**站牌Index*/
                        val stationIndex = msg.obj.toString().toInt()
                        /**取出站牌資訊*/
                        val station = stationList!!.SaveStationList[stationIndex]
                        /**TDX Token*/
                        val tdxToken = msg.data.getSerializable("tdxToken") as TdxToken

                        /**到站時間表*/
                        GlobalScope.launch(Dispatchers.Main) {
                            //IO線程獲取到站時間資料原始檔
                            /**到站時間原始檔*/
                            val eT = withContext(Dispatchers.IO) {
                                BusAPI.get(
                                    tdxToken,
                                    "EstimatedTimeOfArrival",
                                    BusApiRequest(
                                        station.RouteData.City,
                                        null,
                                        "RouteUID eq '${station.RouteData.RouteUID}' and StopUID eq '${station.StationUID}'"
                                    )
                                )
                            }
                            eT?.let {
                                //序列化到站時間資料Json檔
                                /**到站時間(Json檔)*/
                                val mEstimateTime: ArrayList<EstimateTime> =
                                    JsonFunctions.fromJson(
                                        eT,
                                        object :
                                            TypeToken<ArrayList<EstimateTime>>() {}.type
                                    )
                                //取得最首項添入 到站時間表資料
                                estimateTime[stationIndex] = mEstimateTime[0]
                            } ?: run {
                                //到站時間表資料為空
                                estimateTime[stationIndex] = null
                            }
                            //回傳主線程執行UI更改任務
                            mainHandler.post { notifyItemChanged(stationIndex) }
                        }
                    }
                }
            }
        }

        /**
         * 更新各站牌即時到站時間資料 (多線程分散式查詢)
         * @author KILNETA
         * @since Beta_1.2.0
         */
        @DelicateCoroutinesApi
        fun upData() {
            GlobalScope.launch ( Dispatchers.Main ) {
                //清除舊資料
                estimateTime.clear()
                //預建構各站牌資料欄位
                for (i in stationList!!.SaveStationList.indices) {
                    estimateTime.add(null)
                }

                /**TDX Token*/
                val tdxToken = withContext(Dispatchers.IO) {
                    BusAPI.getToken()
                }

                //確定Token成功取得
                tdxToken?.let {
                    //依序取得收藏群組中的各站牌到站時間資料
                    for (i in stationList!!.SaveStationList.indices) {
                        /**Bundle 用於傳遞tdxToken*/
                        val bundle = Bundle()
                        bundle.putSerializable("tdxToken", tdxToken)
                        /**Message 用於傳遞部分參數*/
                        val msg = Message.obtain()
                        msg.what = 1        //線程任務模式
                        msg.obj = i         //站牌Index
                        msg.data = bundle   //Token
                        //加入異線程執行序列
                        workHandler.sendMessage(msg)
                    }
                }
            }
        }

        /**
         * 舊的各站牌到站時間更新 upData()
         * 逐個站牌單線程線性查詢 (較耗時 且 更新緩慢)
         */
        /*
        /**
         * 更新各站牌即時到站時間資料 (單線程線性查詢)
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        fun upData(){
            GlobalScope.launch ( Dispatchers.Main ) {
                //清除舊資料
                estimateTime.clear()

                //TDX 取得協定
                val tdxToken = withContext(Dispatchers.IO) {
                    BusAPI.getToken()
                }
                tdxToken?.let{
                    //依序取得收藏群組中的各站牌到站時間資料
                    for (i in stationList!!.SaveStationList.indices) {
                        /**指定位置的 路線資料*/
                        val station = stationList!!.SaveStationList[i]

                        /**到站時間表*/
                        val eT = withContext(Dispatchers.IO) {
                            BusAPI.get(
                                tdxToken,
                                "EstimatedTimeOfArrival",
                                BusApiRequest(
                                    station.RouteData.City,
                                    null,
                                    "RouteUID eq '${station.RouteData.RouteUID}' and StopUID eq '${station.StationUID}'"
                                )
                            )
                        }
                        eT?.let {
                            val mEstimateTime: ArrayList<EstimateTime> =
                                JsonFunctions.fromJson(
                                    eT,
                                    object : TypeToken<ArrayList<EstimateTime>>() {}.type
                                )
                            //取得最首項添入 到站時間表資料
                            estimateTime.add(mEstimateTime[0])
                        }?: run {
                            estimateTime.add(null)
                        }
                        //刷新視圖列表
                        notifyItemChanged(i)
                    }
                }
            }
        }*/

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
            holder.itemView.findViewById<TextView>(R.id.BusName).text = station.RouteData.RouteName.Zh_tw
            /**控件展示之終點方向*/ //此寫法不會出現警告 vvv
            val text = "往${station.DestinationStopName}"
            holder.itemView.findViewById<TextView>(R.id.DestinationStopName).text = text
            //站牌名子
                    holder.itemView.findViewById<TextView>(R.id.StationName).text = station.StationName.Zh_tw

            //避免出界
            if(position < estimateTime.size){
                /**到站時間*/
                val estimateTime = estimateTime[position]
                //按公車統一條件設置到站時間
                estimateTime?.let{ BusFunctions.setEstimateTimeView(holder,estimateTime)}
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