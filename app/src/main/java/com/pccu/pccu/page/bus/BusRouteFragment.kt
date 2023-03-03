package com.pccu.pccu.page.bus

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.menu.BusStationItemBottomMenu
import com.pccu.pccu.sharedFunctions.PWindow
import com.pccu.pccu.sharedFunctions.*
import com.pccu.pccu.sharedFunctions.ViewGauge.DP_PX
import kotlinx.android.synthetic.main.bus_popwindow.view.*
import kotlinx.android.synthetic.main.bus_route_fragment.*
import kotlinx.android.synthetic.main.bus_route_item.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 公車系統 路線列表-子頁面 頁面建構類 : "Fragment(bus_route_fragment)"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusRouteFragment(
    /**路線方向*/
    private val direction: Int,
    /**路線資料*/
    private val routeData : BusRoute,
    /**目標站點唯一編號 (收藏站點定位用)*/
    private val goalStationUID : String? = null
) : Fragment(R.layout.bus_route_fragment){

    /**倒計時器*/
    private var countdownTimer: Timer? = null
    /**計時器計數*/
    var timerI = 19 //計數
    /**列表適配器*/
    var adapter = Adapter()
    /**本Fragment在頁面上的位置*/
    var fragmentPosition = -1
    /**當前瀏覽的Fragment位置*/
    var fragmentPositionInView = 0

    /**
     * 更新內容用計時器 20s/次
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class CountdownTimerTask : TimerTask() {
        /**
         * 重構 計時器運作內容
         * @author KILNETA
         * @since Alpha_5.0
         */
        @DelicateCoroutinesApi
        override fun run() {
            when(timerI){
                19-> adapter.upData()
                20-> timerI = 0
            }
            //增加計時緩衝條數值
            BusUpdataProgressBar.setProgress(++timerI, false)
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
        super.onViewCreated(view, savedInstanceState)

        //更改公車站牌列表控件(用於配合淡入淡出動畫)
        bus_StationList.visibility = View.GONE

        //創建Bus列表
        bus_StationList.layoutManager =
            LinearLayoutManager( context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器
        bus_StationList.adapter = adapter
        //設置公車站牌列表控件 滾動檢測器
        adapter.initScrollListener()
    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStart() {
        super.onStart()
        //重新計數
        timerI = 19
        //初始化計時器
        countdownTimer = Timer()
        //套用計時器設定
        countdownTimer!!.schedule(CountdownTimerTask(), 500, 500)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStop(){
        super.onStop()
        //bus_StationList.removeOnLayoutChangeListener(adapter.changeListener)
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

        /**首次動作(初始化)*/
        private var isInit = true
        /**路線上的站牌 (包含路線資訊)*/
        private var stations = arrayListOf<Stop>()
        /**路線費用(取得緩衝區資料)*/
        private var fare : RouteFare? = null
        /**沒有路線費用資料*/
        private var noFare = false
        /**完整單站點顯示資料 (站牌資料、到站時間、車輛位置)*/
        private var stationDataList = arrayListOf<StationData>()
        /**已存在的Bus車牌PopupWindows*/
        private var plateNumbPopWindow = arrayListOf<PlateNumbPopWindowData>()
        /**各車號的公車資料*/
        private var vehicleDataList = arrayListOf<Vehicle>()
        /**列表內容更改檢測器*/
        private val changeListener = object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                //刪除檢測器
                bus_StationList.removeOnLayoutChangeListener(this)
                //如果當前用戶正在檢閱此Fragment頁面 則 顯示所有可見的車牌
                if(fragmentPosition == fragmentPositionInView)
                    showAllBusPlateNumbInView(bus_StationList)
            }
        }

        /**
         * 公車站牌列表控件 滾動檢測器設置
         * @author KILNETA
         * @since Beta_1.2.0
         */
        fun initScrollListener(){
            //新增Recyclerview的滾動檢測器
            bus_StationList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                /**滾動狀態改變*/
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    //SCROLL_STATE_IDLE：不是滾動狀態。
                    //SCROLL_STATE_DRAGGING：處於被外力引導的滾動狀態，比如手指正在拖著進行滾動。
                    //SCROLL_STATE_SETTLING：處於自動滾動的狀態，此時手指已經離開屏幕，滾動行為是自身的慣性在維持。
                    when(newState){
                        RecyclerView.SCROLL_STATE_DRAGGING,
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            showAllBusPlateNumbInView(recyclerView)
                        }
                    }
                }

                /**滾動中(距離)*/
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    /**取得列表控件的layoutManager*/
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    /**最首一個可見Item*/
                    val firstVisible: Int = layoutManager.findFirstVisibleItemPosition()
                    /**最尾一個可見Item*/
                    val lastVisible: Int = layoutManager.findLastVisibleItemPosition()

                    //判斷所有存在的車牌PopWindow 是否已不在可視範圍
                    //如果不在範圍 關閉PopWindow並從列表中刪除
                    for(i in plateNumbPopWindow.reversed()){
                        if( i.Station <= firstVisible ||
                            i.Station >= lastVisible
                        ){
                            i.PopWindow!!.dismiss()
                            plateNumbPopWindow.remove(i)
                        }
                    }
                }
            })
        }

        /**
         * 取得路線站牌順序資料
         * @param tdxToken [TdxToken] tdxToken協定
         * @return 路線站牌順序資料 : List<[RouteStation]>
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private suspend fun getDisplayStation(tdxToken:TdxToken):List<RouteStation>? {
            val ds = withContext(Dispatchers.IO) {
                BusAPI.get(
                    tdxToken,
                    if (BusAPI.DisplayStopOfRoute_Locations.any { it == routeData.City })
                        "DisplayStopOfRoute"
                    else
                        "StopOfRoute",
                    BusApiRequest(
                        routeData.City,
                        null,
                        "RouteUID eq '${routeData.RouteUID}' and Direction eq '${direction}'"
                    )
                )
            }

            return  ds?.let{
                JsonFunctions.fromJson<List<RouteStation>>(
                    it,
                    object : TypeToken<List<RouteStation>>() {}.type
                )
            }
        }

        /**
         * 取得路線收費規則
         * @param tdxToken [TdxToken] tdxToken協定
         * @return 收費規則資料 : List<[RouteFare]>
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private suspend fun getRouteFare(tdxToken:TdxToken):List<RouteFare>? {
            val rf = withContext(Dispatchers.IO) {
                BusAPI.get(
                    tdxToken,
                    "RouteFare",
                    BusApiRequest(
                        routeData.City,
                        null,
                        "SubRouteID eq '${
                            routeData.SubRoutes.first { it.Direction == direction }.SubRouteID}'"
                    )
                )
            }

            return rf?.let{
                JsonFunctions.fromJson<List<RouteFare>>(
                    it,
                    object : TypeToken<List<RouteFare>>() {}.type
                )
            }
        }

        /**
         * 取得公車列表展示之 站牌數據
         * @param tdxToken [TdxToken] tdxToken協定
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private suspend fun getStationData(tdxToken:TdxToken) {
            /**到站時間 原始資料*/
            val eta = withContext(Dispatchers.IO) {
                BusAPI.get(
                    tdxToken,
                    "EstimatedTimeOfArrival",
                    BusApiRequest(
                        routeData.City,
                        null,
                        "RouteUID eq '${routeData.RouteUID}'"
                    )
                )
            }
            /**路線車輛資訊 原始檔案*/
            val rtns = withContext(Dispatchers.IO) {
                BusAPI.get(
                    tdxToken,
                    "RealTimeNearStop",
                    BusApiRequest(
                        routeData.City,
                        null,
                        "RouteUID eq '${routeData.RouteUID}'"
                    )
                )
            }
            //確認資料取得不為空
            if(eta != null && rtns != null) {
                /**到站時間 Json*/
                val etaJson : ArrayList<EstimateTime> =
                    JsonFunctions.fromJson(
                        eta,
                        object : TypeToken<ArrayList<EstimateTime>>() {}.type
                    )
                /**路線車輛資訊 Json*/
                val rtnsJson : ArrayList<BusA2> =
                    JsonFunctions.fromJson(
                        rtns,
                        object : TypeToken<ArrayList<BusA2>>() {}.type
                    )
                //格式化資料記錄至各站牌資料
                tidyStationDataList(
                    etaJson,
                    rtnsJson
                )
                //取得路線上的車輛詳細資料
                getVehicleData(rtnsJson,tdxToken)
            }
        }

        /**
         * 取得路線上公車之詳細資料 車輛數據
         * @param rtnsJson ArrayList<[BusA2]> 所有在路線上的公車
         * @param tdxToken [TdxToken] tdxToken協定
         *
         * @author KILNETA
         * @since Beta_1.2.0
         */
        private suspend fun getVehicleData(rtnsJson:ArrayList<BusA2>,tdxToken:TdxToken) {
            /**不存在資訊的車輛數據 (用於發送資料請求資訊)*/
            val vehicleDataNotExist = arrayListOf<String>()
            //確認當前路線上的車輛 不含在存儲的車輛數據(需要另外執行請求)
            rtnsJson.forEach {
                if(!vehicleDataList.any { v->
                        it.PlateNumb == v.PlateNumb &&
                        it.Direction == direction
                })
                    vehicleDataNotExist.add(it.PlateNumb)
            }
            //確認當前存儲的車輛數據 不含在少路線上的車輛(需要另外刪除)
            for(it in vehicleDataList.reversed()){
                if(!rtnsJson.any { rtns-> it.PlateNumb == rtns.PlateNumb})
                    vehicleDataList.remove(it)
            }
            //如果不須執行請求 則直接跳出函式
            if(vehicleDataNotExist.isEmpty())
                return
            /**API請求用的filter過濾指令*/
            var filterCode = ""
            //將需要請求的車輛資料皆輸入
            vehicleDataNotExist.forEachIndexed { index,it->
                if(index != 0)
                    filterCode += " or "
                filterCode += "PlateNumb eq '${it}'"
            }
            /**車輛數據 原始檔案*/
            val vehicle = withContext(Dispatchers.IO) {
                BusAPI.get(
                    tdxToken,
                    "Vehicle",
                    BusApiRequest(
                        routeData.City,
                        null,
                        filterCode
                    )
                )
            }
            //確認資料取得不為空
            if(vehicle != null) {
                /**車輛數據 Json*/
                val vehicleJson: ArrayList<Vehicle> =
                    JsonFunctions.fromJson(
                        vehicle,
                        object : TypeToken<ArrayList<Vehicle>>() {}.type
                    )
                //添加至車輛數據表存儲
                vehicleDataList.addAll(vehicleJson)
            }
        }

        /**
         * 更新公車列表展示資訊
         * @author KILNETA
         * @since Alpha_5.0
         */
        @DelicateCoroutinesApi
        fun upData(){
            GlobalScope.launch ( Dispatchers.Main ) {
                /**取得TDX Token協定*/
                val tdxToken = withContext(Dispatchers.IO) {
                    BusAPI.getToken()
                }

                //確認有取得tdxToken協定 (判斷access_token是否有取得即可)
                tdxToken?.access_token?.let {
                    if (isInit) {
                        //避免重複操作出錯
                        stationDataList.clear()
                        //取得站務資料 並初始化 站牌數據列表
                        stations = getDisplayStation(tdxToken)?.get(0)?.Stops!!
                        stations.forEach { stationDataList.add(StationData(it)) }

                        //取得路線收費規定 (如果欲取得需縣市符合API要求 是否支援其縣市)
                        if (BusAPI.RouteFare.contains(routeData.City)) {
                            val rf = getRouteFare(tdxToken)
                            rf?.let{
                                if (rf.isNotEmpty())
                                    fare = rf[0]
                                else
                                    noFare = true
                            }
                        }
                        //公車站牌列表在首次加載成功後進行crossfade(淡入淡出)動畫
                        RV.crossfade(bus_StationList)
                        //由於前面經過大量網路請求
                        //需檢查視窗尚未被關閉 則初始化列表控件繪圖
                        if (stations.isNotEmpty()  && view != null) {
                            setBufferZone()
                            //刷新視圖列表
                            @Suppress("NotifyDataSetChanged")
                            notifyDataSetChanged()
                            bus_StationList.post {
                                //轉移到定位站牌
                                if (goalStationUID != null) {
                                    moveToPosition(
                                        stationDataList.indexOfFirst {
                                            goalStationUID == it.Station.StopUID }
                                    )
                                }
                            }
                            isInit = false
                        }
                    }

                    //取得公車列表展示之 站牌數據
                    getStationData(tdxToken)

                    //刷新視圖列表
                    @Suppress("NotifyDataSetChanged")
                    notifyDataSetChanged()
                    //一次清除所有顯示中的車牌PopWindow
                    clearBusPlateNumbPopWindow()
                    if(view != null) {
                        //添加 列表內容更改檢測器
                        bus_StationList.addOnLayoutChangeListener(changeListener)
                    }
                //取得tdxToken協定失敗 回報錯誤
                } ?: PToast.popLongHint(context!!,"Error:無法取得TdxAPI的Token")
            }
        }

        /**
         * 整理站牌數據(stationDataList)
         * @param EstimateTime  ArrayList<[EstimateTime]> 到站時間資料群
         * @param BusData       ArrayList<[BusA2]> 車輛位置資料群
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun tidyStationDataList(
            EstimateTime:ArrayList<EstimateTime> ,
            BusData:ArrayList<BusA2>
        ){
            stationDataList.forEachIndexed { index, it ->

                for( i in EstimateTime.indices ){
                    //找到匹配的站點 到站時間資訊
                    if(  EstimateTime[i].StopUID == it.Station.StopUID &&
                        (EstimateTime[i].Direction == direction ||
                         EstimateTime[i].Direction == 255)
                    ){
                        //帶入到站時間資料
                        it.EstimateTime = EstimateTime[i]
                        //有些迴圈路線 首尾站相同會缺少顯示
                        if(index != 0)
                            //清除已套用的車站資料 (減少重複查找的時間)
                            EstimateTime.removeAt(i)
                        break
                    }
                }

                //清除舊的車輛位置資料
                it.innerBuses.clear()
                //該車輛資料須為"非結束服務"、同"行駛方向"、對應站牌
                for( i in BusData.indices ){
                    //找到匹配的站點 到站時間資訊
                    if(  BusData[i].StopUID == it.Station.StopUID &&
                        (BusData[i].Direction == direction ||
                         BusData[i].Direction == 255 ) &&
                        (BusData[i].DutyStatus != 2 )
                    ){
                        //帶入車輛位置資料
                        it.innerBuses.add(BusData[i])
                    }
                }
            }
        }

        /**
         * 平滑滑動至目標站點
         * @param Position [Int] 目標站點座標
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun moveToPosition(Position: Int) {
            //先從RecyclerView的LayoutManager中獲取第一項和最後一項的Position
            /**RecyclerView第一項Position*/
            val firstItem: Int =
                (bus_StationList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            /**RecyclerView最後一項Position*/
            val lastItem: Int =
                (bus_StationList.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            /**單個列表控件的高度*/
            val itemH = bus_StationList.getChildAt(0).height
            /**計算將控件置中顯示在列表裡所要移動的距離*/
            val positionMove: Int = (Position - (firstItem + lastItem)/2 ) * itemH + itemH / 2
            //平滑滑動至該位置
            //bus_StationList.smoothScrollBy(0, positionMove)

            //滑至該位置(無動畫)
            bus_StationList.scrollBy(0, positionMove)
        }

        /**
         * 緩衝區繪製
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setBufferZone(){
            if (fare?.SectionFares?.get(0)?.BufferZones?.isNotEmpty() == true)
                //存在緩衝區
                bus_StationList.addItemDecoration(
                    ItemDecoration(stations, fare!!.SectionFares!![0].BufferZones!!)
                )
            else
                //不存在緩衝區
                bus_StationList.addItemDecoration(ItemDecoration(stations))
        }

        /**
         * 小公車圖示 顯示設置
         * @param view          [View] 視圖
         * @param BusDataList   ArrayList<[BusA2]> 該站牌公車資料
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun busStatusIconChoose(view: View, BusDataList:ArrayList<BusA2>, position: Int){
            /**公車域值*/
            val busStatus = arrayListOf(256,256)
            /**公車Icon控件*/
            val busIconView = arrayListOf(view.BusIcon_after,view.BusIcon_now)

            //計算顯示域值
            for(i in BusDataList.indices) {
                busStatus[BusDataList[i].A2EventType] =
                    busStatus[BusDataList[i].A2EventType].coerceAtMost(BusDataList[i].BusStatus)
            }

            //選擇圖片
            for(i in 0..1) {
                when (busStatus[i]) {
                    256 ->
                        // 256:沒有車輛 (自設值 非公車平台代表值)
                        busIconView[i].setImageDrawable(null)
                    0 ->
                        // 0:正常
                        busIconView[i].setImageDrawable(
                            ContextCompat.getDrawable( context!! , R.drawable.bus_1 ))
                    1, 2, 4 ->
                        // 1:車禍 2:故障 4:緊急求援
                        busIconView[i].setImageDrawable(
                            ContextCompat.getDrawable( context!! , R.drawable.bus_2 ))
                    3 ->
                        // 3:塞車
                        busIconView[i].setImageDrawable(
                            ContextCompat.getDrawable( context!! , R.drawable.bus_3 ))
                    5, 100, 101 ->
                        // 5:加油 100:客滿 101:包車出租
                        busIconView[i].setImageDrawable(
                            ContextCompat.getDrawable( context!! , R.drawable.bus_5 ))
                    else  ->
                        // 90:不明 91:去回不明 98:偏移路線 99:非營運狀態 255:未知
                        busIconView[i].setImageDrawable(
                            ContextCompat.getDrawable( context!! , R.drawable.bus_4 ))
                }
                //有車輛的Icon須設定點選功能 (顯示車牌PopupWindow)
                if(busStatus[i] != 256){
                    busIconView[i].setOnClickListener {
                        showBusPlateNumbPopWindow(it, BusDataList, position, i)
                    }
                }
            }
        }

        /**
         * 刪除所有可見的車牌PopWindow
         * @author KILNETA
         * @since Beta_1.2.0
         */
        fun clearBusPlateNumbPopWindow(){
            plateNumbPopWindow.forEach {
                it.PopWindow!!.dismiss()
            }
            plateNumbPopWindow.clear()
        }

        /**
         * 顯示所有當前可見的 車牌PopWindow
         * @param recyclerView  [RecyclerView] RecyclerView控件
         *
         * @author KILNETA
         * @since Beta_1.2.0
         */
        fun showAllBusPlateNumbInView(recyclerView: RecyclerView){
            /**取得LinearLayoutManager*/
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            /**RecyclerView第一項Position*/
            val firstVisible: Int = layoutManager.findFirstVisibleItemPosition()
            /**RecyclerView最後一項Position*/
            val lastVisible: Int = layoutManager.findLastVisibleItemPosition()
            /**公車Icon控件*/
            for(i in firstVisible..lastVisible) {
                /**站牌Item各停靠狀態的Bus Icon*/
                val busIconView = arrayListOf(
                    layoutManager.findViewByPosition(i)!!.BusIcon_after,
                    layoutManager.findViewByPosition(i)!!.BusIcon_now
                )
                //判斷各個Bus Icon有無須顯示車牌PopWindow
                busIconView.forEachIndexed { index, it->
                    if(stationDataList[i].innerBuses.isNotEmpty() &&
                        stationDataList[i].innerBuses.any {it.A2EventType == index})
                    {
                        showBusPlateNumbPopWindow(it,stationDataList[i].innerBuses,i,index)
                    }
                }
            }
        }

        /**
         * 車牌PopWindow 設置
         * @param view          [View] 父類視圖
         * @param BusDataList   ArrayList<[BusA2]> 在該站牌中的公車
         * @param position      [Int] 該站牌在列表中的的position
         * @param A2EventType   [Int] 該公車狀態(進離站)
         *
         * @author KILNETA
         * @since Beta_1.2.0
         */
        @SuppressLint("InflateParams")
        private fun showBusPlateNumbPopWindow(
            view: View, BusDataList:ArrayList<BusA2>, position: Int, A2EventType:Int)
        {
            //確認當前沒有已顯示的車牌PopWindow
            if(!plateNumbPopWindow.any{
                it.A2EventType == A2EventType &&
                it.Station == position &&
                it.PopWindow != null
            }) {
                /**PopWindow視圖*/
                val popView: View =
                    LayoutInflater.from(view.context)
                        .inflate(R.layout.bus_popwindow, null, false)
                //取得當前在該站牌位置的車輛資訊(進離站狀態要一樣)
                BusDataList.forEach {
                    if(it.A2EventType == A2EventType){
                        /**車牌Item*/
                        val busPlateNumbItem = LinearLayout(context)
                        /**車牌Text*/
                        val busPlateNumbText = TextView(context)

                        //設置 車牌Item 布局
                        busPlateNumbItem.layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        busPlateNumbItem.orientation = LinearLayout.HORIZONTAL
                        busPlateNumbItem.gravity = Gravity.CENTER
                        //設置 車牌Text 布局
                        busPlateNumbText.textSize = 13f
                        busPlateNumbText.text = it.PlateNumb
                        busPlateNumbText.setTextColor(Color.parseColor("#FFFFFF"))
                        //檢查該車牌是否為身障友善車型
                        if(vehicleDataList.any { v->
                            it.PlateNumb == v.PlateNumb &&
                            (   v.VehicleType == 1 ||
                                v.IsLowFloor
                            )
                        }) {
                            /**身障友善車型標示Icon*/
                            val busPlateNumbImage = ImageView(context)
                            /**當前布局Dp*/
                            val density = context!!.resources.displayMetrics.density
                            /**Icon大小*/
                            val imageLength = 12 * density
                            //設置 車牌Icon 布局
                            busPlateNumbImage.layoutParams =
                                ViewGroup.LayoutParams(
                                    imageLength.toInt(),
                                    imageLength.toInt()
                                )
                            val params = RelativeLayout.LayoutParams(
                                imageLength.toInt(),
                                imageLength.toInt()
                            )
                            params.setMargins(
                                0,
                                0,
                                (1 * density).toInt(),
                                0
                            )
                            busPlateNumbImage.layoutParams = params
                            busPlateNumbImage.setImageDrawable(
                                ContextCompat.getDrawable(context!!, R.drawable.disabled))
                            //將物件新增至車牌Item
                            busPlateNumbItem.addView(busPlateNumbImage)
                        }
                        //將 車牌Text 新增至車牌Item
                        busPlateNumbItem.addView(busPlateNumbText)
                        //將 車牌Item 新增至車牌PopupWindow
                        popView.BusPlateNumbBox.addView(busPlateNumbItem)
                    }
                }
                /**初始化自定義的PopWindow控件*/
                val mDemoWindow = PWindow(popView)
                //不允許超出視圖的裁切(可以凸出視圖)
                mDemoWindow.isClippingEnabled = false
                //外部不可被點擊
                mDemoWindow.isOutsideTouchable = false
                //禁止被標示
                mDemoWindow.isFocusable = false
                //設置顯示消失動畫為0 (尚不確定是否有用)
                mDemoWindow.animationStyle = 0
                //背景為空
                mDemoWindow.setBackgroundDrawable(null)
                //根據定位視圖顯示PopupWindow (定位:右上 ,朝向:左)
                mDemoWindow.showDropDown(view,3,1,1)
                //設置點擊檢測器 (如果被點擊則隱藏PopWindow)
                mDemoWindow.contentView.setOnClickListener{
                    mDemoWindow.dismiss()
                    plateNumbPopWindow.removeIf {
                        it.PopWindow == mDemoWindow
                    }
                }
                //紀錄新建已存在的車牌PopWindow
                plateNumbPopWindow.add(
                    PlateNumbPopWindowData(mDemoWindow,position,A2EventType)
                )
            }
        }

        /**
         * 重構 創建視圖持有者 (連結bus_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載列表控件元素bus_route_item*/
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_route_item, parent, false)
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemCount(): Int {
            return stationDataList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {
            /**指定位置的站牌資料*/
            val station = stationDataList[position].Station
            /**指定位置的到站時間資料*/
            val estimateTime = stationDataList[position].EstimateTime
            /**指定位置的公車位置資料*/
            val buses = stationDataList[position].innerBuses

            //設置站點名稱
            holder.itemView.StationName.text = station.StopName.Zh_tw
            //設置公車圖示
            busStatusIconChoose(holder.itemView,buses,position)

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                /**顯示底部彈窗列表*/
                val sheetFragment = BusStationItemBottomMenu(
                    routeData,
                    station.StopUID,
                    station.StopName,
                    direction
                )
                sheetFragment.show(parentFragmentManager, sheetFragment.tag)
            }

            //設置到站時間
            if (estimateTime != null) {
                BusFunctions.setEstimateTimeView(holder,estimateTime)
            }
        }
    }

    /**
     * 公車列表Item裝飾
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class ItemDecoration : RecyclerView.ItemDecoration {
        /**站牌資料表*/
        private var station = arrayListOf<Stop>()
        /**緩衝區列表*/
        private val bufferZoneList = arrayListOf<BufferZoneData>()
        /**緩衝區標示大小*/
        private var sectionSize = 30
        /**有緩衝區*/
        private var hasBufferZone = false

        /**繪圖 目標站牌*/
        private val paintGoal = Paint(Paint.ANTI_ALIAS_FLAG)
        /**繪圖 緩衝區*/
        private val paintBufferZone = Paint(Paint.ANTI_ALIAS_FLAG)
        /**繪圖 一般站牌*/
        private val paintUniversal = Paint(Paint.ANTI_ALIAS_FLAG)
        /**繪圖 文字*/
        private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)

        /** rightEdgeLength 右側邊界距離 */
        val REmargin = DP_PX(context!!,55f)

        /**初始化參數*/
        init {
            //目標站牌
            paintGoal.color = Color.parseColor("#f7f752")
            paintGoal.style = Paint.Style.FILL
            paintGoal.isDither = true
            //緩衝區
            paintBufferZone.color = Color.parseColor("#a3cc49")
            paintBufferZone.style = Paint.Style.FILL
            paintBufferZone.isDither = true
            //一般站牌
            paintUniversal.color = Color.parseColor("#4B5155")
            paintUniversal.style = Paint.Style.FILL
            paintUniversal.isDither = true
            //文字
            paintText.color = Color.parseColor("#FFFFFF")
            paintText.textSize = 24f
            paintText.isDither = true
        }

        /**
         * @param station List<[Stop]>  站牌資料
         */
        constructor(
            station : ArrayList<Stop>) {
            this.station = station
        }

        /**
         * @param station List<[Stop]>  站牌資料
         * @param bufferZones ArrayList<[BufferZone]>  緩衝區資料
         */
        constructor(
            station : ArrayList<Stop>,
            bufferZones : ArrayList<BufferZone>,
        ) {
            hasBufferZone = true
            //替代本地數值
            this.station = station
            tidyBufferZoneList(bufferZones)
        }

        /**
         * 整理緩衝區基本資料
         * @param bufferZones ArrayList<[BufferZone]>  緩衝區資料
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun tidyBufferZoneList(bufferZones : ArrayList<BufferZone>){
            //遍歷整理緩衝區資料
            for(i in bufferZones.indices){
                //確保緩衝區與路線方向一致
                if(bufferZones[i].Direction == direction){
                    bufferZoneList.add(
                        BufferZoneData(
                            bufferZones[i].FareBufferZoneOrigin.StopID == bufferZones[i].FareBufferZoneDestination.StopID,
                            bufferZones[i].FareBufferZoneOrigin.StopID,
                            bufferZones[i].FareBufferZoneOrigin.StopName,
                            bufferZones[i].FareBufferZoneDestination.StopID,
                            bufferZones[i].FareBufferZoneDestination.StopName,
                        )
                    )
                }
            }
        }

        /**
         * 設置Item邊界格式
         * @param outRect [Rect]  外矩形
         * @param position [Int]  Item座標
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setItemEdge(outRect: Rect, position:Int ){
            //需繪製緩衝區
            if ( hasBufferZone ) {
                // 要減去sectionSize的大小
                bufferZoneList.forEach{
                    //紀錄緩衝區起始具體Item座標 (初始化完就不會再操作了)
                    if(it.start_position == -1 && station[position].StopID == it.start_StationID){
                        it.start_position = position
                    }
                    else if(it.end_position == -1 && station[position].StopID == it.end_StationID){
                        it.end_position = position
                    }

                    //依據條件設置邊界
                    when(position){
                        it.start_position->
                            if(it.isSame)   //緩衝區開始
                                outRect.bottom += sectionSize
                            else            //分段點
                                outRect.top += sectionSize
                        it.end_position->   //緩衝區結束
                            outRect.bottom += sectionSize
                    }
                }
            }
            //該站若為目標站牌 則需設置擺放提示邊框 (不會與緩衝區重疊)
            if(station[position].StopUID == goalStationUID){
                outRect.top += 2
                outRect.bottom += 2
            }
        }

        /**
         * 設定每個Item的偏移距離
         * @param outRect [Rect]            外矩形
         * @param view [Rect]               視圖
         * @param parent [RecyclerView]     父類(列表)
         * @param state [RecyclerView.State]父類(列表)狀態
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemOffsets(
            outRect: Rect,              //外矩形
            view: View,                 //視圖
            parent: RecyclerView,       //父類(列表)
            state: RecyclerView.State   //父類(列表)狀態
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            /**子適配器位置*/
            val position: Int = parent.getChildAdapterPosition(view)
            /**獲取適配器*/
            val manager: RecyclerView.LayoutManager? = parent.layoutManager

            // 適配器 === 線性佈局管理器
            if (manager is LinearLayoutManager) {
                //設置Item邊界格式 (緩衝區、目標站點)
                setItemEdge(outRect, position)
            }
        }

        /**
         * 繪製動作
         * @param canvas [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         * @param state [RecyclerView.State]父類(列表)狀態
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(canvas, parent, state)

            /**獲取父類適配器*/
            val manager = parent.layoutManager
            // 適配器 === 線性佈局管理器
            if (manager is LinearLayoutManager) {
                //適配器的方向 == 垂直
                if (manager.orientation == LinearLayoutManager.VERTICAL) {
                    // 如果需要繪製緩衝區
                    if (hasBufferZone) {
                        drawBufferZone(canvas, parent)
                    } else {
                        drawUniversal(canvas, parent)
                    }
                }
            }
        }

        /**
         * 按部分 設置垂直繪圖
         * @param canvas [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawBufferZone(canvas: Canvas, parent: RecyclerView) {
            //遍歷子類計數
            for (i in 0 until parent.childCount) {
                /**item控件*/
                val child = parent.getChildAt(i)
                /**item座標*/
                val position = parent.getChildAdapterPosition(child)
                /**item與top的相對位置*/
                var top = child.top
                /**item與bottom的相對位置*/
                var bottom = child.bottom
                //是目標站牌的話自動退縮 2px
                if(drawGoalJudge(position)){
                    top -= 2
                    bottom += 2
                }

                //根據條件繪製圖形
                when {
                /**分界點*/
                    bufferZoneList.any {drawBZJudge(1,it,position)} -> {
                        //畫布繪製矩形
                        canvas.drawRect(
                            child.left.toFloat(),
                            (top - sectionSize).toFloat(),
                            (child.right - REmargin),
                            bottom.toFloat(),
                            paintUniversal                                             //畫布
                        )
                        //畫布繪製矩形
                        canvas.drawRect(
                            child.left.toFloat(),
                            bottom.toFloat(),
                            (child.right - REmargin),
                            (bottom + sectionSize ).toFloat(),
                            paintBufferZone                                             //畫布
                        )
                        canvas.drawText(
                            "分段點",                                               //類別名稱
                            ((child.right - REmargin) / 2),                        //X座標
                            (child.bottom + sectionSize - (sectionSize / 5)).toFloat(), //Y座標
                            paintText                                                   //畫布
                        )
                    }
                /**緩衝區開始*/
                    bufferZoneList.any {drawBZJudge(2,it,position)} -> {
                        //畫布繪製矩形
                        canvas.drawRect(
                            child.left.toFloat(),
                            (child.top - sectionSize  -
                                    if(drawGoalJudge(position)) 2 else 0).toFloat(),
                            (child.right - REmargin),
                            child.bottom.toFloat(),
                            paintBufferZone                                             //畫布
                        )
                        canvas.drawText(
                            "緩衝區開始",                                            //類別名稱
                            ((child.right - REmargin) / 2),                        //X座標
                            (child.top - sectionSize + (sectionSize / 1.2)).toFloat(),  //Y座標
                            paintText                                                   //畫布
                        )
                    }
                /**緩衝區結束*/
                    bufferZoneList.any {drawBZJudge(3,it,position)} -> {
                        //畫布繪製矩形
                        canvas.drawRect(
                            child.left.toFloat(),
                            child.top.toFloat(),
                            (child.right - REmargin),
                            (bottom + sectionSize).toFloat(),
                            paintBufferZone                                             //畫布
                        )
                        canvas.drawText(
                            "緩衝區結束",                                            //類別名稱
                            ((child.right - REmargin) / 2),                         //X座標
                            (child.bottom + sectionSize - (sectionSize / 5)).toFloat(), //Y座標
                            paintText                                                   //畫布
                        )
                    }
                /**緩衝區中間*/
                    bufferZoneList.any { drawBZJudge(4,it,position) } -> {
                        //畫布繪製矩形
                        canvas.drawRect(
                            child.left.toFloat(),
                            child.top.toFloat(),
                            (child.right - REmargin),
                            bottom.toFloat(),
                            paintBufferZone                                             //畫布
                        )
                    }
                /**非緩衝區*/
                    else -> {
                        //非緩衝區站牌 (單純填色)
                        canvas.drawRect(
                            child.left.toFloat(),
                            child.top.toFloat(),
                            (child.right - REmargin),
                            child.bottom.toFloat(),
                            paintUniversal                                             //畫布
                        )
                    }
                }

                if(drawGoalJudge(position)){
                    //目標站牌
                    canvas.drawRect(
                        child.left.toFloat(),
                        top.toFloat(),
                        (child.right - REmargin + DP_PX(context!!,2f)),
                        bottom.toFloat(),
                        paintGoal                                                       //畫布
                    )
                }
            }
        }

        /**
         * 繪製緩衝區條件判斷
         * @param mode [Canvas]             篩選模式(編號)
         * @param it [BufferZoneData]       單個緩衝區資料
         * @param position [Int]            Item座標
         * @return 篩選結果(是、否) : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawBZJudge ( mode:Int ,it:BufferZoneData ,position:Int ) : Boolean{
            return when(mode) {
                1 ->    it.isSame &&
                        it.start_position == position
                2 ->    it.start_position == position
                3 ->    it.end_position == position
                4 ->    !it.isSame &&
                        ((  position > it.start_position &&
                            it.start_position != -1 &&
                            it.end_position == -1           ) ||
                         (  position < it.end_position &&
                            position > it.start_position
                        ))
                else -> false
            }
        }

        /**
         * 繪製目標站牌條件判斷
         * @param position [Int]            Item座標
         * @return 篩選結果(是、否) : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawGoalJudge (position:Int) : Boolean{
            return station[position].StopUID == goalStationUID
        }

        /**
         * 設置垂直繪圖
         * @param canvas [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawUniversal(canvas: Canvas, parent: RecyclerView) {
            //遍歷子類計數
            for (i in 0 until parent.childCount) {
                /**item控件*/
                val child = parent.getChildAt(i)
                /**item座標*/
                val position = parent.getChildAdapterPosition(child)


                //畫布繪製矩形
                if(station[position].StopUID == goalStationUID){
                    //目標站牌
                    canvas.drawRect(
                        child.left.toFloat(),
                        (child.top - 2).toFloat(),
                        (child.right - REmargin + DP_PX(context!!,2f)),
                        (child.bottom+2).toFloat(),
                        paintGoal
                    )
                }
                else{
                    //非目標站牌 (單純填色)
                    canvas.drawRect(
                        child.left.toFloat(),
                        child.top.toFloat(),
                        (child.left + DP_PX(context!!,3f)),
                        child.bottom.toFloat(),
                        paintUniversal
                    )
                }
            }
        }
    }

    /**
     * 完整單站點顯示資料 -數據結構
     * @param Station       [Stop]              站牌資料
     * @param EstimateTime  [EstimateTime]      到站時間
     * @param innerBuses    ArrayList<[BusA2]>  車輛位置
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private data class StationData(
        val Station: Stop,
        var EstimateTime : EstimateTime? = null,
        val innerBuses: ArrayList<BusA2> = arrayListOf(),
    )

    /**
     * 車輛顯示資料 -數據結構
     * @param PopWindow     [PopupWindow] 站牌資料
     * @param Station       [Int] 站點順序位置
     * @param A2EventType   [Int] 進站離站 - ( 0:離站 1:進站 )
     *
     * @author KILNETA
     * @since Beta_1.2.0
     */
    private data class PlateNumbPopWindowData(
        var PopWindow: PopupWindow? = null,
        var Station : Int,
        val A2EventType: Int,
    )

    /**
     * 緩衝區範圍 -數據結構
     * @param isSame            [Boolean] 是同一站
     * @param start_StationID    [String]  起始站ID
     * @param start_StationName  [String]  起始站名
     * @param end_StationID      [String]  結束站ID
     * @param end_StationName    [String]  結束站名
     * @param start_position    [Int]     起始站座標
     * @param end_position      [Int]     結束站座標
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private data class BufferZoneData(
        val isSame : Boolean,
        val start_StationID : String,
        val start_StationName : String,
        val end_StationID : String,
        val end_StationName : String,

        var start_position : Int = -1,
        var end_position : Int = -1
    )
}