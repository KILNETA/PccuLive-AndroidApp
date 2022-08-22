package com.example.pccu.Page.Bus.Search

import android.content.Context
import android.content.Intent
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
import com.example.pccu.Internet.*
import com.example.pccu.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.bus_search_main.*
import kotlinx.android.synthetic.main.bus_search_result_item.view.*
import kotlinx.android.synthetic.main.search_filter_item.view.*
import kotlinx.coroutines.*
import java.lang.reflect.Type
import android.graphics.Canvas
import android.widget.LinearLayout
import com.example.pccu.Page.Bus.Bus_RoutePage
import java.io.Serializable

import android.graphics.drawable.Drawable
import android.widget.ImageView


/**
 * Cwb主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class Search_Activity : AppCompatActivity(R.layout.bus_search_main){

    data class FilterOption(
        val location: NameType,         //用於存取API服務的token，格式為JWT
        var checked: Boolean = false,              //token的有效期限
    ) : Serializable

    fun <T> fromJson(json: String, type: Type): T {
        return Gson().fromJson(json, type)
    }

    /**
     * 停止載入動畫
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun stopLoading(){
        //修改版面 (隱藏)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0)
        //套用設定至載入動畫物件
        loading.setLayoutParams(params)
    }

    /**
     * 載入動畫
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun startLoading(){
        //修改版面 (顯示)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //套用設定至載入動畫物件
        loading.setLayoutParams(params)
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
        //初始化 route_list 列表適配器
        val route_adapter = resultList_Adapter()
        //掛載 route_list 列表適配器
        route_list.adapter = route_adapter
        //列表控件 route_list 的設置佈局管理器 (列表)
        route_list.layoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
        //設置分組標籤
        route_adapter.setItemDecoration()

        //初始化 filter_list 列表適配器
        val filter_adapter = filterList_Adapter(route_adapter)
        //掛載 filter_list 列表適配器
        filter_list.adapter = filter_adapter
        //列表控件 filter_list 的設置佈局管理器 (列表)
        filter_list.layoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
        //
        _searchView.requestFocusFromTouch()

        /**
         * SearchView 搜索控件 事件重構
         * @author KILNETA
         * @since Alpha_5.0
         */
        _searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                val lastQuery = ""

            //當 點擊搜索按鈕 時觸發該方法
            override fun onQueryTextSubmit(query: String): Boolean {
                //如果輸入目標跟上次搜索目標一樣 則不執行搜索
                if(lastQuery == query)
                    return false

                //啟用Loading動畫
                startLoading()
                //清空上次搜索的結果
                if (filter_list.childCount > 0 ) {
                    filter_list.removeAllViews()
                    filter_adapter.clearItems()
                }
                if (route_list.childCount > 0 ) {
                    route_list.removeAllViews()
                    route_adapter.clearItems()
                }
                //清空上次搜索的結果數
                resultNum.text = "－"

                //主線程
                GlobalScope.launch(Dispatchers.Main) {
                    //欲展示至 route_list 資料表
                    val route = arrayListOf<BusRoute>()
                    var location = arrayListOf<FilterOption>()

                    //IO線程 取得Bus_API_token 權限
                    val token = withContext(Dispatchers.IO) {
                        Bus_API.Get_token()
                    }

                    //依序爬取全台各縣市公車資料
                    for (i in Bus_API.Locations.indices) {
                        //IO線程 取得Bus_Route 資料
                        val busRoute = withContext(Dispatchers.IO) {
                            Bus_API.Get(
                                token!!,                    //token 權限
                                "Route",             //調用公車路線API
                                BUS_ApiRequest(
                                    Bus_API.Locations[i].En,//英文縣市名
                                    query                   //查詢關鍵字
                                )
                            )
                        }
                        //格式化返回資料
                        val json = fromJson<Array<BusRoute>>(
                            busRoute.string(),
                            object : TypeToken<Array<BusRoute>>() {}.type
                        )
                        //存入取回之資料
                        if (json.isNotEmpty()) {
                            location.add(FilterOption(Bus_API.Locations[i]))
                            route.addAll(json)
                        }
                    }
                    filter_adapter.resetData(location)
                    //route_list 重新讀取資料並展示
                    route_adapter.resetData(route)
                }
                //清除焦點，收軟鍵盤
                _searchView.clearFocus()
                return false
            }

            //當搜索內容改變時觸發該方法
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    /**
     * 縣市篩選 列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class filterList_Adapter(
        val route_adapter:resultList_Adapter,
        var locationList: ArrayList<FilterOption> = arrayListOf()
    ): RecyclerView.Adapter<MyViewHolder>(){

        fun clearItems(){
            this.locationList.clear()
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 刷新篩選項目
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun resetData(LocationList: ArrayList<FilterOption>){
            this.locationList = LocationList
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //加載布局於當前的context 列表控件元素announcement_item
            val view =
                LayoutInflater.from(baseContext).inflate(R.layout.search_filter_item,parent,false)
            //回傳當前持有者
            return MyViewHolder(view)
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
         * 重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            //取得該元素對應的公告列表資料
            val Data = locationList[position]
            //設置公告縮圖
            holder.itemView.filterItem_text.text = Data.location.Zh_tw
            if(locationList[position].checked) {
                holder.itemView.filterItem.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.PCCU_yellow))
            }
            else{
                holder.itemView.filterItem.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#BABABA"))
            }

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                if(holder.itemView.filterItem.backgroundTintList != ColorStateList.valueOf(resources.getColor(R.color.PCCU_yellow))) {
                    holder.itemView.filterItem.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.PCCU_yellow))
                    locationList[position].checked = true
                }
                else {
                    holder.itemView.filterItem.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#BABABA"))
                    locationList[position].checked = false
                }
                route_adapter.resetfilter(locationList)
            }
        }
    }

    /**
     * 搜尋結果列表 控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class resultList_Adapter(
        var route: ArrayList<BusRoute> = arrayListOf()
    ): RecyclerView.Adapter<MyViewHolder>(){

        var displayRoute = arrayListOf<BusRoute>()

        fun clearItems(){
            displayRoute.clear()
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 設置列表物件分組與顯示
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun setItemDecoration(){
            route_list.addItemDecoration(
                resultList_ItemDecoration(
                    80,
                    "#7d7d7d",
                    object : resultList_ItemDecoration.LinearSectionCallback {
                        override fun getItemStr(poisition: Int): String {
                            // 回傳每個Item的地區 (已轉中文)
                            val index = Bus_API.Locations.first{ it.En == displayRoute[poisition].City }
                            return index.Zh_tw
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
            this.displayRoute.clear()

            this.route = Route
            this.displayRoute.addAll(this.route)
            resultNum.text = displayRoute.size.toString()
            //刷新視圖列表
            notifyDataSetChanged()
            //停止加載動畫
            stopLoading()
        }

        /**
         * 刷新搜索結果 (篩選選項被更新)
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun resetfilter(locationList: List<FilterOption>){
            this.displayRoute.clear()

            if(locationList.all{ !it.checked }) {
                this.displayRoute.addAll(this.route)
            }
            else {
                route.forEach{
                    for (i in locationList.indices) {
                        if(locationList[i].checked && it.City == locationList[i].location.En) {
                            this.displayRoute.add(it)
                            break
                        }
                    }
                }
            }
            resultNum.text = displayRoute.size.toString()
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //加載布局於當前的context 列表控件元素announcement_item
            val view =
                LayoutInflater.from(baseContext).inflate(R.layout.bus_search_result_item,parent,false)
            //回傳當前持有者
            return MyViewHolder(view)
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
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if(!displayRoute[position].HasSubRoutes && displayRoute[position].SubRoutes[0].Headsign!=null)
                holder.itemView.routeName.text = "${displayRoute[position].RouteName.Zh_tw} ${displayRoute[position].SubRoutes[0].Headsign}"
            else
                holder.itemView.routeName.text = displayRoute[position].RouteName.Zh_tw

            holder.itemView.routeText.text = "${displayRoute[position].DepartureStopNameZh} - ${displayRoute[position].DestinationStopNameZh}"

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //轉換當前的頁面 至 公告內文頁面
                //新方案 (新建Activity介面展示)
                val IntentObj = Intent()
                val Bundle = Bundle()
                Bundle.putSerializable("RouteData", displayRoute[position]) //站點資料
                IntentObj.putExtras(Bundle)
                IntentObj.setClass(applicationContext, Bus_RoutePage::class.java )
                startActivity(IntentObj)
            }
        }
    }

    /**
     * 搜尋結果列表物品裝飾
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    class resultList_ItemDecoration : ItemDecoration {


        private var context: Context? = null
        // 分類群組的大小
        private var sectionSize = 0
        // 是否要有分類群組
        private var hasSection = false
        private var mPaint: Paint? = null
        private var mPaintText:Paint? = null
        // 給外部調用的interface，回傳當前位置GroupID
        private var callback: LinearSectionCallback? = null

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
                if (manager.orientation == LinearLayoutManager.VERTICAL) {
                    //有分類群組
                    if (hasSection) {
                        // 繪製分類群組
                        setVerticalItemOfSetBySection(outRect, position)
                    }
                }
            }
        }

        /**
         * 分組標籤部分回調 (interface)
         * @author KILNETA
         * @since Alpha_5.0
         */
        interface LinearSectionCallback {
            /**
             * 搜尋結果列表物品裝飾
             * @param poisition [Int]
             * @return 列表物件分類依據 : [String]
             *
             * @author KILNETA
             * @since Alpha_5.0
             */
            fun getItemStr(poisition: Int): String
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
            sectionSize: Int,
            dividerColor: String,
            callback: LinearSectionCallback?
        ) {
            //替代本地數值
            this.context = context
            this.sectionSize = sectionSize
            this.callback = callback
            hasSection = true
            // 初始化標籤 (繪圖 Paint)
            mPaintText = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaintText!!.setColor(Color.argb(255, 255, 255, 255))
            mPaintText!!.setTextSize(36f)
            mPaintText!!.setDither(true)
            initPaint(Color.parseColor(dividerColor))
        }

        /**
         * 分類標籤 (繪圖 Paint) 初始化
         * @param color [Int]  分組標籤顏色
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun initPaint(color: Int) {
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint!!.setColor(color)
            mPaint!!.setStyle(Paint.Style.FILL)
            mPaint!!.setDither(true)
        }

        /**
         * 按部分 設置垂直項截面設置
         * @param outRect [Rect]  分組標籤顏色
         * @param position [Int]  分組標籤顏色
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setVerticalItemOfSetBySection(outRect: Rect, position: Int) {
            //如果物件是該分類項目的首位
            if (isFirstInGroup(position)) {
                //設 偏移距離 為 sectionSize
                outRect[0, sectionSize, 0] = 0
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
            return position == 0 || !callback!!.getItemStr(position)
                .equals(callback!!.getItemStr(position - 1))
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
            val right = parent.width - parent.paddingRight
            //遍歷子類計數
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = parent.getChildAdapterPosition(child)
                var top = child.top + params.topMargin
                var bottom = top
                //是 類別首位
                if (isFirstInGroup(position)) {
                    // 要減去sectionSize的大小
                    top = child.top - sectionSize + params.topMargin
                    bottom = top + sectionSize
                }
                //畫布繪製矩形
                canva.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    mPaint!!
                )
                //是 (有標籤分類 && 類別首位)
                if (hasSection && isFirstInGroup(position)) {
                    // 繪製標籤分類標題名稱
                    canva.drawText(
                        callback!!.getItemStr(position),        //類別名稱
                        20f,                                 //X座標
                        (bottom - sectionSize / 3).toFloat(),   //Y座標
                        mPaintText!!                            //畫布
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
            val right = parent.width - parent.paddingRight
            //遍歷子類計數
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = parent.getChildAdapterPosition(child)
                //非 最首項
                if (position != 0) {
                    val top = child.top + params.topMargin
                    val bottom = top
                    //畫布繪製矩形
                    canva.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        mPaint!!
                    )
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

            // 如果沒有要繪製分類群組，就不繼續執行
            if (!hasSection) return

            //參數計算
            val childCount = parent.childCount
            val itemCount = parent.adapter!!.itemCount
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            // 上個Item分組的標誌
            var preGroupId = ""
            // 現在Item分組的標誌
            var nowGroupId = "-1"
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = parent.getChildAdapterPosition(child)
                preGroupId = nowGroupId
                nowGroupId = callback!!.getItemStr(position)
                // 如果目前的Item分組的標誌等於 "-1" or 跟前1個Item一樣，就跳過
                if (nowGroupId == "-1" || nowGroupId == preGroupId) continue
                // 外界因素所造成的偏移距離
                val otherSize = params.topMargin + parent.paddingTop
                // 當前Item的底部位置
                val childBottom = child.bottom
                // 取得當前最大的大小(如下圖-1)，或是這個部分要請為打印出來，就知道為什麼要這樣做
                // 為了實現分類群組，固定在上方
                var top = Math.max(otherSize + sectionSize, otherSize + child.top)

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
                    mPaint!!
                )
                // 繪製標題名稱
                canva.drawText(
                    callback!!.getItemStr(position),        //類別名稱
                    20f,                                    //X座標
                    (top - sectionSize / 3).toFloat(),      //Y座標
                    mPaintText!!                            //畫布
                )
            }
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
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}