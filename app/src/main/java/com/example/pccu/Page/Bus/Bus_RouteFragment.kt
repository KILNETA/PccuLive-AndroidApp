package com.example.pccu.Page.Bus

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import com.example.pccu.R
import kotlinx.android.synthetic.main.bus_route_item.view.*
import kotlinx.android.synthetic.main.bus_route_fragment.*
import com.example.pccu.Internet.*
import com.example.pccu.Menu.BusStationItem_BottomMenu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.coroutines.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.bus_search_main.*
import kotlin.collections.ArrayList
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.announcement_content_page.view.*
import kotlinx.android.synthetic.main.bus_icon_popwindow.*
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.core.view.get


/**
 * 公車系統 公車列表-子頁面 頁面建構類 : "Fragment(bus_list_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */

class  Bus_RouteFragment(
    val Direction: Int,
    val RouteData : BusRoute,
    val PositionStationUID : String? = null
) : Fragment(R.layout.bus_route_fragment){

    data class StationData(
        val Station: Stop,
        var EstimateTime : Bus_Data_EstimateTime? = null,
        val innerBuses: ArrayList<Bus_Data_A2> = arrayListOf(),
    )

    /**倒計時器*/
    private var CountdownTimer:Timer? = null
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
            when(Timer_i){
                19-> adapter.upData()
                20-> Timer_i = 0
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

        //創建Bus列表
        bus_StationList.layoutManager = LinearLayoutManager( context, LinearLayoutManager.VERTICAL, false)
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
    override fun onStop(){
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

        var init_ = true
        var Station : Bus_Data_Station? = null
        var Fare : Bus_Data_Fare? = null

        var StationDataList = arrayListOf<StationData>()

        fun tidyStationDataList(
            EstimateTime:ArrayList<Bus_Data_EstimateTime> ,
            BusData:ArrayList<Bus_Data_A2>
        ){
            StationDataList.forEach {

                for( i in EstimateTime.indices ){
                    //找到匹配的站點資訊
                    if(  EstimateTime[i].StopUID == it.Station.StopUID &&
                        (EstimateTime[i].Direction == Station!!.Direction ||
                         EstimateTime[i].Direction == 255)
                    ){
                        it.EstimateTime = EstimateTime[i]
                        EstimateTime.removeAt(i)
                        break
                    }
                }

                it.innerBuses.clear()

                for( i in BusData.indices ){
                    //找到匹配的站點資訊
                    if(  BusData[i].StopUID == it.Station.StopUID &&
                        (BusData[i].Direction == Station!!.Direction ||
                         BusData[i].Direction == 255 )
                    ){
                        it.innerBuses.add(BusData[i])
                    }
                }
            }
        }

        private fun moveToPosition(n: Int) {
            //先從RecyclerView的LayoutManager中獲取第一項和最後一項的Position
            val firstItem: Int = (bus_StationList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val lastItem: Int = (bus_StationList.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            //然後區分情況

            if (n <= firstItem || n > lastItem) {
                //當要置頂的項在當前顯示的第一個項的前面時
                //bus_StationList.smoothScrollToPosition(n)
            }
            val item_H = bus_StationList.getChildAt(0).height
            //當要置頂的項已經在螢幕上顯示時
            val top: Int = (n - (firstItem + lastItem)/2 ) * item_H +
                    item_H / 2
            bus_StationList.smoothScrollBy(0, top)

        }

        /**
         * 設置列表物件分組與顯示
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun setItemDecoration(){
           if (Fare != null && Fare!!.SectionFares != null && Fare!!.SectionFares[0].BufferZones != null && Fare!!.SectionFares[0].BufferZones.isNotEmpty()) {
               bus_StationList.addItemDecoration(
                   ItemDecoration(
                       Direction,
                       RouteData,
                       Station!!,
                       Fare!!,
                       30,
                       PositionStationUID
                   )
               )
           } else {
               bus_StationList.addItemDecoration(
                   ItemDecoration(
                       Station!!,
                       PositionStationUID
                   )
               )
           }
        }

        fun BusStatus_IconChoose( view: View, BusDataList:ArrayList<Bus_Data_A2>){

            val busStatus = listOf(
                listOf(0),
                listOf(1, 2, 4),
                listOf(3),
                listOf(5, 100, 101),
            )

            val busIcon = listOf(
                getResources().getDrawable( R.drawable.bus_1 ),
                getResources().getDrawable( R.drawable.bus_2 ),
                getResources().getDrawable( R.drawable.bus_3 ),
                getResources().getDrawable( R.drawable.bus_5 ),
                getResources().getDrawable( R.drawable.bus_4 ),
            )

            for(i in busStatus.indices) {
                if ( i < busStatus.size) {
                    if (BusDataList.any {
                            busStatus[i].contains(it.BusStatus) &&
                                    it.DutyStatus == 1 &&
                                    it.A2EventType == 1
                        }) {
                        view.BusIcon_now.setImageDrawable(busIcon[i])
                        break
                    }
                }
                else{
                    if (BusDataList.any {
                            it.A2EventType == 1
                        }) {
                        view.BusIcon_now.setImageDrawable(busIcon[i])
                        break
                    }
                }
            }

            for(i in busStatus.indices) {
                if ( i < busStatus.size) {
                    if (BusDataList.any {
                            busStatus[i].contains(it.BusStatus) &&
                                    it.DutyStatus == 1 &&
                                    it.A2EventType == 0
                        }) {
                        view.BusIcon_after.setImageDrawable(busIcon[i])
                        break
                    }
                }
                else{
                    if (BusDataList.any {
                            it.A2EventType == 0
                        }) {
                        view.BusIcon_after.setImageDrawable(busIcon[i])
                        break
                    }
                }
            }

            // 0:正常
            // 1:車禍 2:故障 4:緊急求援
            // 3:塞車
            // 5:加油 100:客滿 101:包車出租
            // 90:不明 91:去回不明 98:偏移路線 99:非營運狀態 255:未知
        }

        fun upData(){
            GlobalScope.launch ( Dispatchers.Main ) {

                //TDX test
                val test = withContext(Dispatchers.IO) {
                    Bus_API.Get_token()
                }

                if(init_){
                    //站務資料 連接BusAPI
                    val StationData : List<Bus_Data_Station> = withContext(Dispatchers.IO) {
                        fromJson(
                            Bus_API.Get(
                                test!!,
                                if(Bus_API.DisplayStopOfRoute_Locations.any{it==RouteData.City})
                                    "DisplayStopOfRoute"
                                else
                                    "StopOfRoute",
                                BUS_ApiRequest(
                                    RouteData.City,
                                    null,
                                    "RouteUID eq '${RouteData.RouteUID}' and Direction eq '${Direction}'"
                                )
                            ).string(),
                            object : TypeToken<List<Bus_Data_Station>>() {}.type
                        )
                    }
                    Station = StationData[0]
                    StationData[0].Stops.forEach { StationDataList.add( StationData( it )) }

                    if(Bus_API.RouteFare.any{ it == RouteData.City }) {
                        val FareData: List<Bus_Data_Fare> = withContext(Dispatchers.IO) {
                            fromJson(
                                Bus_API.Get(
                                    test!!,
                                    "RouteFare",
                                    BUS_ApiRequest(
                                        RouteData.City,
                                        null,
                                        "SubRouteID eq '${RouteData.SubRoutes.first { it.Direction == Direction }.SubRouteID}'"
                                    )
                                ).string(),
                                object : TypeToken<List<Bus_Data_Fare>>() {}.type
                            )
                        }
                        Fare = if(FareData.isNotEmpty()) FareData[0] else null
                    }
                    if(view!=null) {
                        setItemDecoration()
                    }
                }
                withContext(Dispatchers.IO) {
                    tidyStationDataList(
                        fromJson(
                            Bus_API.Get(
                                test!!,
                                "EstimatedTimeOfArrival",
                                BUS_ApiRequest(
                                    RouteData.City,
                                    null,
                                    "RouteUID eq '${RouteData.RouteUID}'"
                                )
                            ).string(),
                            object : TypeToken<ArrayList<Bus_Data_EstimateTime>>() {}.type
                        ),
                        fromJson(
                            Bus_API.Get(
                                test!!,
                                "RealTimeNearStop",
                                BUS_ApiRequest(
                                    RouteData.City,
                                    null,
                                    "RouteUID eq '${RouteData.RouteUID}'"
                                )
                            ).string(),
                            object : TypeToken<ArrayList<Bus_Data_A2>>() {}.type
                        )
                    )
                }

                //刷新視圖列表
                notifyDataSetChanged()

                if(view!=null && PositionStationUID!=null && init_) {
                    moveToPosition( StationDataList.indexOfFirst{ PositionStationUID==it.Station.StopUID} )
                }

                if(init_)
                    init_ = !init_

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
                LayoutInflater.from(context).inflate(R.layout.bus_route_item, parent, false)
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
            return Station?.Stops?.size ?: 0
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

            //val station = Station!!.Stops[position]

            val station = StationDataList[position].Station
            val EstimateTime = StationDataList[position].EstimateTime
            val Buses = StationDataList[position].innerBuses

            holder.itemView.StationName.text = station.StopName.Zh_tw

            holder.itemView.BusIcon_now.setImageDrawable(null)
            holder.itemView.BusIcon_after.setImageDrawable(null)

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //顯示底部彈窗列表
                val SheetFragment = BusStationItem_BottomMenu(
                    RouteData,
                    station.StopUID,
                    station.StopName,
                    Direction
                )
                SheetFragment.show(parentFragmentManager, SheetFragment.tag)
            }

            if(Buses.isNotEmpty()) {
                BusStatus_IconChoose(holder.itemView,Buses)
            }

            if(EstimateTime!=null){
                //如果剩餘時間 > 3min  {顯示剩餘時間}
                if(EstimateTime.EstimateTime>180) {
                    holder.itemView.TimeState.textSize = 22f
                    holder.itemView.EstimateTime.text = (EstimateTime.EstimateTime / 60).toString()
                    holder.itemView.TimeMin.text = "分"
                    holder.itemView.TimeState.text = ""

                    //  剩餘時間 < 3min {剩餘時間欄位清空}
                }else{
                    holder.itemView.TimeMin.text = ""
                    holder.itemView.EstimateTime.text = ""

                    //如果剩餘時間 < 3min && 正在 0:運營中 {即將進站}
                    if(EstimateTime.EstimateTime in 5..180 && arrayOf(1,0).contains(EstimateTime.StopStatus)) {
                        holder.itemView.TimeState.setTextColor(Color.parseColor("#F5B939"))
                        holder.itemView.TimeState.textSize = 18f
                        holder.itemView.TimeState.text = "將到站"
                    }
                    //如果剩餘時間 < 5s && 正在 0:運營中 {進站中}
                    else if(EstimateTime.EstimateTime in 0..4 && arrayOf(0).contains(EstimateTime.StopStatus)) {
                        holder.itemView.TimeState.setTextColor(Color.parseColor("#ff6363"))
                        holder.itemView.TimeState.textSize = 18f
                        holder.itemView.TimeState.text = "進站中"
                    }
                    //有顯示最近的發車時間
                    else if(EstimateTime.NextBusTime!=null){
                        val nextBusTime_Date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(EstimateTime.NextBusTime)
                        holder.itemView.TimeState.setTextColor(Color.parseColor("#FFFFFF"))
                        holder.itemView.TimeState.textSize = 22f
                        holder.itemView.TimeState.text =
                            DateFormat.format("HH:mm", nextBusTime_Date!!.time).toString()
                    }

                    //如果剩餘時間 < 5s && 不是 0:運營中 {未運營狀況}
                    else if(EstimateTime.EstimateTime<5 && EstimateTime.StopStatus!=0){
                        holder.itemView.TimeState.setTextColor(Color.parseColor("#7d7d7d"))
                        //當前交通狀況 1:未發車 2:交管不停 3:末班已過 4:今日未營運
                        when(EstimateTime.StopStatus){
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

    /**
     * 搜尋結果列表物品裝飾
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    class ItemDecoration : RecyclerView.ItemDecoration {

        data class BufferZone(
            val isSame : Boolean,
            val start_SationID : String,
            val start_SationName : String,
            val end_SationID : String,
            val end_SationName : String,

            var start_position : Int = -1,
            var end_position : Int = -1
        )

        var Direction : Int? = null
        var RouteData : BusRoute? = null
        var Station : Bus_Data_Station? = null
        var Fare : Bus_Data_Fare? = null

        var PositionStationUID: String? = null

        val BufferZoneList = arrayListOf<BufferZone>()
        // 分類群組的大小
        private var sectionSize = 0
        // 是否要有分類群組
        private var hasSection = false

        fun tidyBufferZoneList(){
            for(i in Fare!!.SectionFares[0].BufferZones.indices){
                val bufferZones = Fare!!.SectionFares[0].BufferZones[i]

                if(bufferZones.Direction == Direction){
                    BufferZoneList.add(
                        BufferZone(
                            bufferZones.FareBufferZoneOrigin.StopID == bufferZones.FareBufferZoneDestination.StopID,
                            bufferZones.FareBufferZoneOrigin.StopID,
                            bufferZones.FareBufferZoneOrigin.StopName,
                            bufferZones.FareBufferZoneDestination.StopID,
                            bufferZones.FareBufferZoneDestination.StopName,
                        )
                    )
                }
            }
        }

        /**
         * 搜尋結果列表物件裝飾
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        constructor(
            station : Bus_Data_Station,
            PositionStationUID: String? = null) {
            this.Station = station
            this.PositionStationUID = PositionStationUID
        }

        /**
         * 搜尋結果列表物件裝飾
         * @param sectionSize [Int]                 分組標籤大小
         * @param dividerColor [String]             分組標籤顏色(色碼)
         * @param callback [LinearSectionCallback]  分組標籤部分回調
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        constructor(
            direction : Int,
            routeData : BusRoute,
            station : Bus_Data_Station,
            fare : Bus_Data_Fare,
            sectionSize: Int,
            PositionStationUID: String? = null
        ) {
            //替代本地數值
            this.Direction = direction
            this.RouteData = routeData
            this.Station = station
            this.Fare = fare
            this.sectionSize = sectionSize
            hasSection = true
            this.PositionStationUID = PositionStationUID

            tidyBufferZoneList()
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

            //獲取子適配器位置
            val position: Int = parent.getChildAdapterPosition(view)
            //獲取適配器
            val manager: RecyclerView.LayoutManager? = parent.getLayoutManager()

            // 適配器 === 線性佈局管理器
            if (manager is LinearLayoutManager) {
                //適配器的方向 == 垂直

                if ( hasSection ) {
                    // 要減去sectionSize的大小

                    BufferZoneList.forEach{
                        if(it.start_position == -1 && Station!!.Stops[position].StopID == it.start_SationID){
                            it.start_position = position
                        }
                        else if(it.end_position == -1 && Station!!.Stops[position].StopID == it.end_SationID){
                            it.end_position = position
                        }

                        if(it.isSame){
                            if(it.start_position == position){
                                outRect.bottom += sectionSize
                            }
                        }
                        else {
                            if (it.start_position == position) {
                                outRect.top += sectionSize
                            }
                            else if (it.end_position == position) {
                                outRect.bottom += sectionSize
                            }
                        }
                    }
                }
                if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID){
                    outRect.top += 2
                    outRect.bottom += 2
                }
            }
        }

        /**
         * 繪製動作
         * @param canva [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         * @param state [RecyclerView.State]父類(列表)狀態
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onDraw(canva: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(canva!!, parent, state!!)

            //獲取父類適配器
            val manager = parent.layoutManager
            // 適配器 === 線性佈局管理器
            if (manager is LinearLayoutManager) {
                //適配器的方向 == 垂直
                if (manager.orientation == LinearLayoutManager.VERTICAL) {
                    // 如果需要繪製分類群組
                    if (hasSection) {
                        drawVerticalBySection(canva, parent)
                    } else {
                        drawVertical(canva, parent)
                    }
                }
            }
        }

        /**
         * 按部分 設置垂直繪圖
         * @param canva [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawVerticalBySection(canva: Canvas, parent: RecyclerView) {
            //參數計算
            val childCount = parent.childCount
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight - 150
            //遍歷子類計數
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = parent.getChildAdapterPosition(child)
                val top = child.top
                val bottom = child.bottom

                val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
                paintText.setColor(Color.argb(255, 255, 255, 255))
                paintText.setTextSize(24f)
                paintText.setDither(true)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setColor(Color.parseColor("#4B5155"))
                paint.setStyle(Paint.Style.FILL)
                paint.setDither(true)

                //是 類別首位
                if (BufferZoneList.any {it.isSame && it.start_position == position}) {
                    paint.setColor(Color.parseColor("#a3cc49"))

                    val paint_ = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint_.setColor(Color.parseColor("#4B5155"))
                    paint_.setStyle(Paint.Style.FILL)
                    paint_.setDither(true)
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        (child.top - sectionSize + params.topMargin - if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0).toFloat(),
                        right.toFloat(),
                        (child.bottom + sectionSize-sectionSize + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        paint_
                    )
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        (child.bottom + sectionSize-sectionSize + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        right.toFloat(),
                        (child.bottom + sectionSize + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        paint
                    )
                    canva.drawText(
                        "分段點",        //類別名稱
                        (right / 2).toFloat(),                                 //X座標
                        (child.bottom + sectionSize - sectionSize / 5).toFloat(),   //Y座標
                        paintText                            //畫布
                    )
                } else if (BufferZoneList.any {it.start_position == position}) {
                    // 要減去sectionSize的大小
                    paint.setColor(Color.parseColor("#a3cc49"))
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        (child.top - sectionSize + params.topMargin - if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0).toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        paint
                    )
                    canva.drawText(
                        "緩衝區開始",        //類別名稱
                        (right / 2).toFloat(),                                 //X座標
                        (child.top - sectionSize + params.topMargin + sectionSize / 1.2).toFloat(),   //Y座標
                        paintText                            //畫布
                    )
                } else if (BufferZoneList.any {it.end_position == position}) {
                    paint.setColor(Color.parseColor("#a3cc49"))
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        (child.bottom + sectionSize + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        paint
                    )
                    canva.drawText(
                        "緩衝區結束",        //類別名稱
                        (right / 2).toFloat(),                                 //X座標
                        (child.bottom + sectionSize - sectionSize / 5).toFloat(),   //Y座標
                        paintText                            //畫布
                    )
                } else if (BufferZoneList.any { !it.isSame && ((position > it.start_position && it.start_position != -1 && it.end_position == -1) || (position < it.end_position && position > it.start_position))}) {
                    paint.setColor(Color.parseColor("#a3cc49"))
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        (child.top - if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0).toFloat(),
                        right.toFloat(),
                        (child.bottom + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        paint
                    )
                } else {
                    paint.setColor(Color.parseColor("#4B5155"))
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        (top - if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0).toFloat(),
                        right.toFloat(),
                        (bottom + if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID) 2 else 0 ).toFloat(),
                        paint
                    )
                }

                if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID){
                    val paint_ = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint_.setColor(Color.parseColor("#f7f752"))
                    paint_.setStyle(Paint.Style.FILL)
                    paint_.setDither(true)
                    //畫布繪製矩形
                    canva.drawRect(
                        (left).toFloat(),
                        (child.top-2).toFloat(),
                        (right+2).toFloat(),
                        (child.bottom+2).toFloat(),
                        paint_
                    )
                }
            }
        }

        /**
         * 設置垂直繪圖
         * @param canva [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawVertical(canva: Canvas, parent: RecyclerView) {
            //參數計算
            val childCount = parent.childCount
            val left = parent.paddingLeft
            val right = left + 10
            //遍歷子類計數
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = parent.getChildAdapterPosition(child)

                val top = child.top
                val bottom = child.bottom

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setColor(Color.parseColor("#4B5155"))
                paint.setStyle(Paint.Style.FILL)
                paint.setDither(true)

                //畫布繪製矩形
                canva.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    paint
                )

                if(PositionStationUID != null && Station!!.Stops[position].StopUID == PositionStationUID){
                    val left = parent.paddingLeft
                    val right = parent.width - parent.paddingRight - 150
                    val paint_ = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint_.setColor(Color.parseColor("#f7f752"))
                    paint_.setStyle(Paint.Style.FILL)
                    paint_.setDither(true)
                    //畫布繪製矩形
                    canva.drawRect(
                        (left).toFloat(),
                        (child.top-2).toFloat(),
                        (right+2).toFloat(),
                        (child.bottom+2).toFloat(),
                        paint_
                    )
                }
            }
        }
    }
}