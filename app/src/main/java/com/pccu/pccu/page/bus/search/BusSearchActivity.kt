package com.pccu.pccu.page.bus.search

import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.pccu.pccu.internet.*
import com.pccu.pccu.R
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import android.graphics.Canvas
import android.widget.LinearLayout
import android.widget.TextView
import com.pccu.pccu.sharedFunctions.JsonFunctions.fromJson
import com.pccu.pccu.page.bus.BusRoutePage
import com.pccu.pccu.sharedFunctions.RV
import java.io.Serializable
import kotlin.math.max

/**
 * SearchActivity 主框架建構類 : "bus_search_main"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusSearchActivity : AppCompatActivity(R.layout.bus_search_main) {

    /**route_list 列表適配器*/
    private val routeAdapter = ResultListAdapter()
    /**filter_list 列表適配器*/
    private val filterAdapter = FilterListAdapter()
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    findViewById<TextView>(R.id.noNetWork).layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    findViewById<TextView>(R.id.noNetWork).layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                }
            },
            baseContext
        )
        val itFilter = IntentFilter()
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * PCCU_APP主框架建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        //建構主框架
        super.onCreate(savedInstanceState)
        //初始化網路接收器
        initInternetReceiver()

        val routeList = findViewById<RecyclerView>(R.id.route_list)
        //掛載 route_list 列表適配器
        routeList.adapter = routeAdapter
        //列表控件 route_list 的設置佈局管理器 (列表)
        routeList.layoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)

        //設置分組標籤
        routeAdapter.setItemDecoration()

        val filterList = findViewById<RecyclerView>(R.id.filter_list)
        //掛載 filter_list 列表適配器
        filterList.adapter = filterAdapter
        //列表控件 filter_list 的設置佈局管理器 (列表)
        filterList.layoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)

        val searchView = findViewById<SearchView>(R.id._searchView)
        //開啟搜索框焦點 (順帶啟用軟鍵盤)
        searchView.requestFocusFromTouch()
        //設置搜索文本偵聽器
        searchView.setOnQueryTextListener(searchViewOnQueryTextListener)
    }

    /**
     * 當頁面刪除時(刪除)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * SearchView 搜索控件 事件重構
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    private val searchViewOnQueryTextListener = object : SearchView.OnQueryTextListener {
        /**上一次搜索目標*/
        private var lastQuery = ""
        //當 點擊搜索按鈕 時觸發該方法
        override fun onQueryTextSubmit(query: String): Boolean {
            if(!internetReceiver!!.isConnect) return false
            //如果輸入目標跟上次搜索目標一樣 則不執行搜索
            if(lastQuery == query)  return false
            //清除上次搜索結果
            clearSearchResult()

            //主線程
            GlobalScope.launch(Dispatchers.Main) {
                /**路線*/
                val route = arrayListOf<BusRoute>()
                /**有路線的地區*/
                val location = arrayListOf<FilterOption>()

                /**Bus_API_token 權限*/
                val token = withContext(Dispatchers.IO) { BusAPI.getToken() }

                token?.let {
                    //啟用Loading動畫
                    startLoading()

                    //依序爬取全台各縣市公車資料
                    for (i in BusAPI.Locations.indices) {
                        /**取得Bus_Route 資料*/
                        val bR = withContext(Dispatchers.IO) {
                            BusAPI.get(
                                token,                    //token 權限
                                "Route",             //調用公車路線API
                                BusApiRequest(
                                    BusAPI.Locations[i].En,//英文縣市名
                                    query                   //查詢關鍵字
                                )
                            )
                        }
                        bR?.let {
                            /**取得Bus_Route to Json 資料*/
                            val busRoute =
                                fromJson<Array<BusRoute>>(
                                    bR,
                                    object : TypeToken<Array<BusRoute>>() {}.type
                                )
                            //存入取回之資料
                            if (busRoute.isNotEmpty()) {
                                location.add(FilterOption(BusAPI.Locations[i]))
                                route.addAll(busRoute)
                            }
                        }
                    }
                    //重新讀取資料並展示
                    filterAdapter.resetData(location)
                    routeAdapter.resetData(route)
                    //修改為上次搜索 避免重複搜索
                    lastQuery = query
                }?: run{
                    lastQuery = ""
                }
                //停止加載動畫
                stopLoading()
            }
            //清除焦點，收軟鍵盤
            findViewById<SearchView>(R.id._searchView).clearFocus()
            return false
        }

        /**
         * 清除上次搜索結果
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun clearSearchResult(){

            //清空上次搜索的結果
            val filterList = findViewById<RecyclerView>(R.id.filter_list)
            if (filterList.childCount > 0 ) {
                filterList.removeAllViews()
                filterAdapter.clearItems()
            }

            val routeList = findViewById<RecyclerView>(R.id.route_list)
            if (routeList.childCount > 0 ) {
                routeList.removeAllViews()
                routeAdapter.clearItems()
            }

            //清空上次搜索的結果數
            findViewById<TextView>(R.id.resultNum).text = "－"
        }

        //當搜索內容改變時觸發該方法
        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }
    }

    /**
     * 縣市篩選 列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class FilterListAdapter: RecyclerView.Adapter<RV.ViewHolder>(){
        /**篩選選項列表*/
        private var locationList: ArrayList<FilterOption> = arrayListOf()

        /**
         * 清除篩選項目 (縣市篩選)
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun clearItems(){
            this.locationList.clear()
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 刷新篩選項目 (縣市篩選)
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun resetData(LocationList: ArrayList<FilterOption>){
            this.locationList = LocationList
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載布局列表控件元素search_filter_item*/
            val view =
                LayoutInflater.from(baseContext).inflate(R.layout.search_filter_item,parent,false)
            //回傳當前持有者
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemCount(): Int {
            //列表元素數量 = 公告列表資料(AnnouncementList)的大小
            return locationList.size
        }

        /**
         * 刷新篩選項目 (縣市篩選)
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setButtonColor(filterData:FilterOption): ColorStateList {
            return if(filterData.checked)
                ColorStateList.valueOf(Color.parseColor("#F5B939"))
            else
                ColorStateList.valueOf(Color.parseColor("#BABABA"))
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

            /**取得該元素對應的篩選鈕資料*/
            val filterData = locationList[position]
            //設置篩選鈕文字
            holder.itemView.findViewById<TextView>(R.id.filterItem_text).text = filterData.location.Zh_tw
            //設置篩選鈕顏色
            holder.itemView.findViewById<LinearLayout>(R.id.filterItem).backgroundTintList = setButtonColor(filterData)

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //重設選項 & 外觀
                filterData.checked = !filterData.checked
                holder.itemView.findViewById<LinearLayout>(R.id.filterItem).backgroundTintList = setButtonColor(filterData)
                //刷新經篩選的查詢結果
                routeAdapter.resetFilter(locationList)
            }
        }
    }

    /**
     * 搜尋結果列表 控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class ResultListAdapter: RecyclerView.Adapter<RV.ViewHolder>(){
        /**原始查詢結果*/
        private var route: ArrayList<BusRoute> = arrayListOf()
        /**經篩選的結果*/
        private var displayRoute = arrayListOf<BusRoute>()

        /**
         * 清除搜尋結果
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun clearItems(){
            displayRoute.clear()
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 設置列表物件分組與顯示
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun setItemDecoration(){
            findViewById<RecyclerView>(R.id.route_list).addItemDecoration(
                ResultListItemDecoration(
                    object : ResultListItemDecoration.LinearSectionCallback {
                        override fun getItemStr(position: Int): String {
                            // 回傳每個Item的地區 (已轉中文)
                            return BusAPI.Locations.first{ it.En == displayRoute[position].City }.Zh_tw
                        }
                    }
                )
            )
        }

        /**
         * 刷新搜索結果 (搜索結果被更新)
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun resetData(Route: ArrayList<BusRoute>){
            //重置篩選的結果
            this.displayRoute.clear()
            //帶入新查詢資料 且默認重置篩選
            this.route = Route
            this.displayRoute.addAll(this.route)
            //顯示查詢結果數
            findViewById<TextView>(R.id.resultNum).text = displayRoute.size.toString()
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 刷新搜索結果 (篩選選項被更新)
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun resetFilter(locationList: List<FilterOption>){
            //重置篩選的結果
            this.displayRoute.clear()

            if(locationList.all{ !it.checked }) {
                //全部篩選選項都未勾選 (顯示全部查詢結果)
                this.displayRoute.addAll(this.route)
            }
            else {
                //根據篩選選項 顯示查詢結果
                route.forEach{ It ->
                    if(locationList.any { it.location.En == It.City && it.checked })
                        this.displayRoute.add(It)
                }
            }
            //顯示查詢結果數
            findViewById<TextView>(R.id.resultNum).text = displayRoute.size.toString()
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載布局列表控件元素bus_search_result_item*/
            val view =
                LayoutInflater.from(baseContext).inflate(R.layout.bus_search_result_item,parent,false)
            //回傳當前持有者
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemCount(): Int {
            return this.displayRoute.size
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
            //展示路線名
            val routeName = holder.itemView.findViewById<TextView>(R.id.routeName)
            if(!displayRoute[position].HasSubRoutes && displayRoute[position].SubRoutes[0].Headsign!=null) {
                // 沒有附屬路線 && 有車頭描述
                /**控件展示之路線名*/ //此寫法不會出現警告 vvv
                val text = "${displayRoute[position].RouteName.Zh_tw} ${displayRoute[position].SubRoutes[0].Headsign}"
                routeName.text = text
            }
            else {
                routeName.text = displayRoute[position].RouteName.Zh_tw
            }
            /**控件展示之路線首尾站*/ //此寫法不會出現警告 vvv
            val text = "${displayRoute[position].DepartureStopNameZh} - ${displayRoute[position].DestinationStopNameZh}"
            holder.itemView.findViewById<TextView>(R.id.routeText).text = text

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //轉換當前的頁面 至 公車路線頁面
                //新方案 (新建Activity介面展示)
                /**目標視圖*/
                val intentObj = Intent()
                /**傳遞資料包*/
                val bundle = Bundle()
                //傳遞站點資料
                bundle.putSerializable("RouteData", displayRoute[position])
                intentObj.putExtras(bundle)
                //建構頁面 Bus_RoutePage
                intentObj.setClass(applicationContext, BusRoutePage::class.java )
                startActivity(intentObj)
            }
        }
    }

    /**
     * 搜尋結果列表物品裝飾
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    class ResultListItemDecoration(
        /**分類依據*/
        private var callback: LinearSectionCallback?
    ) : ItemDecoration() {
        /**標籤大小*/
        private val sectionSize = 80
        /**繪製標籤色塊*/
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        /**繪製標籤文字*/
        private val mPaintText:Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * 初始化class中其他數值設定
         * @author KILNETA
         * @since Alpha_5.0
         */
        init {
            mPaintText.color = Color.argb(255, 255, 255, 255)
            mPaintText.textSize = 36f
            mPaintText.isDither = true

            mPaint.color = Color.parseColor("#7d7d7d")
            mPaint.style = Paint.Style.FILL
            mPaint.isDither = true
        }

        /**
         * 分組標籤部分回調 (interface)
         * @author KILNETA
         * @since Alpha_5.0
         */
        interface LinearSectionCallback {
            /**
             * 搜尋結果列表物品裝飾
             * @param position [Int]
             * @return 列表物件分類依據 : [String]
             *
             * @author KILNETA
             * @since Alpha_5.0
             */
            fun getItemStr(position: Int): String
        }

        /**
         * 按部分 設置垂直繪圖
         * @param canvas [Canvas]            畫布
         * @param parent [RecyclerView]     父類(列表)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun drawVerticalBySection(canvas: Canvas, parent: RecyclerView) {
            //參數計算
            val childCount = parent.childCount
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            //遍歷子類計數
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val position = parent.getChildAdapterPosition(child)
                //是 (有標籤分類 && 類別首位)
                if (isFirstInGroup(position)) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val top = child.top - sectionSize + params.topMargin
                    val bottom = top + sectionSize

                    //畫布繪製矩形
                    canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        mPaint
                    )
                    // 繪製標籤分類標題名稱
                    canvas.drawText(
                        callback!!.getItemStr(position),        //類別名稱
                        20f,                                 //X座標
                        (bottom - sectionSize / 3).toFloat(),   //Y座標
                        mPaintText                            //畫布
                    )
                }
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

            /**獲取子適配器位置*/
            val position: Int = parent.getChildAdapterPosition(view)
            /**獲取適配器*/
            val manager: RecyclerView.LayoutManager? = parent.layoutManager

            // 適配器 === 線性佈局管理器
            if (manager is LinearLayoutManager) {
                //如果物件是該分類項目的首位
                if (isFirstInGroup(position)) {
                    //設 偏移距離 為 sectionSize
                    outRect[0, sectionSize, 0] = 0
                }
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
                    //繪製分類群組
                    drawVerticalBySection(canvas, parent)
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
        override fun onDrawOver(canva: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(canva, parent, state)

            //參數計算
            val childCount = parent.childCount
            val itemCount = parent.adapter!!.itemCount
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            /**上個Item分組的標誌*/
            var preGroupId: String
            /**現在Item分組的標誌*/
            var nowGroupId = "-1"
            for (i in 0 until childCount) {
                /**位置控件*/
                val child = parent.getChildAt(i)
                /**位置控件父類的佈局參數*/
                val params = child.layoutParams as RecyclerView.LayoutParams
                /**控件在列表的位置*/
                val position = parent.getChildAdapterPosition(child)
                preGroupId = nowGroupId
                nowGroupId = callback!!.getItemStr(position)
                // 如果目前的Item分組的標誌等於 "-1" or 跟前1個Item一樣，就跳過
                if (nowGroupId == "-1" || nowGroupId == preGroupId) continue
                /**外界因素所造成的偏移距離*/
                val otherSize = params.topMargin + parent.paddingTop
                // 當前Item的底部位置
                val childBottom = child.bottom
                // 取得當前最大的大小(如下圖-1)，或是這個部分要請為打印出來，就知道為什麼要這樣做
                /**為了實現分類群組，固定在上方*/
                var top = max(otherSize + sectionSize, otherSize + child.top)

                if (position + 1 < itemCount) {
                    // 當前群組最後1個Item && childBottom < top
                    if (isLastInGroup(position) && childBottom < top) {
                        // 就把繪製製的top等於目前的childBottom，這樣才會達到推移的效果(如下圖-2)
                        top = childBottom
                    }
                }
                // 畫布繪製矩形
                canva.drawRect(
                    left.toFloat(),
                    (top - sectionSize).toFloat(),
                    right.toFloat(),
                    top.toFloat(),
                    mPaint
                )
                // 繪製標題名稱
                canva.drawText(
                    callback!!.getItemStr(position),        //類別名稱
                    20f,                                    //X座標
                    (top - sectionSize / 3).toFloat(),      //Y座標
                    mPaintText                            //畫布
                )
            }
        }

        /**
         * 判斷是否為相同群組的第1個
         * @param position [Int]  物件位置
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun isFirstInGroup(position: Int): Boolean {
            // 判斷是否為相同群組的第1個
            return position == 0 ||
                    callback!!.getItemStr(position) != callback!!.getItemStr(position - 1)
        }

        /**
         * 判斷是否為相同群組的最後1個
         * @param position [Int]    物件位置
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun isLastInGroup(position: Int): Boolean {
            // 判斷是否為當前群組的最後1個
            return callback!!.getItemStr(position) != callback!!.getItemStr(position + 1)
        }
    }

    /**
     * 載入動畫
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun startLoading(){
        //修改版面 (顯示)
        findViewById<LinearLayout>(R.id.loading).layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
    }

    /**
     * 停止載入動畫
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun stopLoading(){
        //修改版面 (隱藏)
        findViewById<LinearLayout>(R.id.loading).layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            )
    }


    /**
     * 過濾選項 -數據結構
     * @param location  [NameType] 地區名稱
     * @param checked   [Boolean] 是否勾選
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    data class FilterOption(
        val location: NameType,         //地區名稱
        var checked: Boolean = false,   //是否勾選
    ) : Serializable
}