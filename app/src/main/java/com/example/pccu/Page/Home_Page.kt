package com.example.pccu.Page

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_page.*

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pccu.Internet.*
import com.example.pccu.Menu.BottomSheetFragment

import kotlinx.android.synthetic.main.weather_item.view.*

import com.example.pccu.R
import kotlinx.coroutines.*
import java.util.*
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.format.DateFormat
import android.widget.*
import androidx.navigation.Navigation
import com.example.pccu.Page.Live_Image.CameraItem
import com.example.pccu.Shared_Functions.MySharedPreferences
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.calendar_item.view.*
import java.util.Calendar
import com.example.pccu.Page.Live_Image.CameraUrls
import com.example.pccu.Page.Live_Image.PeriodRecommend
import com.example.pccu.Page.Live_Image.UrlSource.PCCU_ExternalLink
import com.example.pccu.Page.Live_Image.UrlSource.PCCU_ImageLink
import com.squareup.picasso.MemoryPolicy

/**
 * 程式主頁面 : "Fragment(home_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Home_Page : Fragment(R.layout.home_page){

    private var CountdownTimer:Timer? = null

    /**
     * home_page頁面被關閉
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onDestroyView() {
        CountdownTimer!!.cancel()
        CountdownTimer!!.purge()
        super.onDestroyView()
    }

    /**
     * home_page建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /**
         * 日曆視窗設置
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setCalendar(){
            /**
             * 轉換月份顯示
             * @param month [String] 數字月份
             * @return 英文月份縮寫 : [String]
             *
             * @author KILNETA
             * @since Alpha_2.0
             */
            fun monthConversion (month:String) : String{
                when(month){
                    "01"-> return "Jan"
                    "02"-> return "Feb"
                    "03"-> return "Mar"
                    "04"-> return "Apr"
                    "05"-> return "May"
                    "06"-> return "Jun"
                    "07"-> return "Jul"
                    "08"-> return "Aug"
                    "09"-> return "Sep"
                    "10"-> return "Oct"
                    "11"-> return "Nov"
                    "12"-> return "Dec"
                }
                return "--"
            }
            //點擊日曆圖示 開啟整個GOOGLE行事曆頁面
            view.findViewById<LinearLayout>(R.id.CalendarView)
                .setOnClickListener{
                startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://calendar.google.com/calendar/u/0/embed?" +
                                "ctz=Asia/Taipei" +
                                "&showCalendars=0&showTabs=0" +
                                "&showPrint=0" +
                                "&src=pccu.edu.tw_blcke48f8jv7rd96hs8oeb06ro@group.calendar.google.com" +
                                "&color=%23039BE5" +
                                "&color=%2333B679" +
                                "&color=%23F09300" +
                                "&color=%230B8043" +
                                "&color=%230B8043")
                ))
            }

            val Datum = DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time).toString()
            val Dates = Datum.split("-")

            //設置小日曆圖示內部數據(年月份)
            val CalendarYear = view.findViewById<TextView>(R.id.CalendarYear)
            val CalendarMoon = view.findViewById<TextView>(R.id.CalendarMoon)
            val CalendarDate = view.findViewById<TextView>(R.id.CalendarDate)
            CalendarYear.setText(Dates[0])
            CalendarMoon.setText(monthConversion(Dates[1]))
            CalendarDate.setText(Dates[2])

            //行事曆列表呼叫
            calendar_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            //初始化 行事曆適配器
            val CalendarAdapter = CalendarAdapter()
            //行事曆列表 連接適配器
            calendar_list.adapter = CalendarAdapter
            //首次更新數據
            CalendarAdapter.upDatas(Datum)
        }

        /**
         * 氣溫列表設置
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setWeather(weatherAdapter:WeatherAdapter) {
            //氣溫列表呼叫
            weather_list.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            //氣溫列表 連接適配器
            weather_list.adapter = weatherAdapter
            //首次更新數據
            weatherAdapter.upDatas()
        }

        /**
         * 設置快速連結列表按鈕功能
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setFastLinks(){
            val FastLinkButton = view.findViewById<Button>(R.id.FastLink)
            FastLinkButton.setOnClickListener{
                val FastLinkSheetFragment = BottomSheetFragment()
                FastLinkSheetFragment.show(parentFragmentManager, FastLinkSheetFragment.tag)
            }
        }

        /**
         * 設置即時影像
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setCameras(cameraItem: ArrayList<CameraItem>){
            //取得螢幕寬度
            val dm = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(dm)
            val vWidth = dm.widthPixels

            /**初始化即時影像視圖
             * @return [CameraItem] : 即時影像控件編號資料
             *
             * @author KILNETA
             * @since Alpha_2.0
             */
            fun initCameraItem(): CameraItem{
                //即時影像控件初始化 -編號資料-
                val Item = CameraItem(
                    TextView(this.context!!),
                    ImageView(this.context!!)
                )
                Item.imageView.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                Item.titel.setPadding(70, 10, 0, 10)
                Item.titel.textSize = 18F
                Item.titel.setBackgroundColor(Color.parseColor("#7EBCA8"))

                //影像控件 設定
                Item.imageView.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        vWidth / 16 * 9
                    )
                )

                //即時影像視圖組 設定
                val CameraItem = LinearLayout(this.context!!)
                CameraItem.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                CameraItem.orientation = LinearLayout.VERTICAL

                //將控件綁定於視圖組上
                CameraItem.addView(Item.titel)
                CameraItem.addView(Item.imageView)

                //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
                view.findViewById<LinearLayout>(R.id.Camera)
                    .addView(CameraItem)

                //回傳 影像控件 -編號資料-
                return Item
            }
            /**更新即時影像內容
             * @param periodCameras List<[CameraUrls]> 鏡頭連結資訊表
             * @param i [Int] 項目編號
             *
             * @author KILNETA
             * @since Alpha_2.0
             */
            fun loadCameraItem(periodCameras : List<CameraUrls> , i:Int){
                //返回主線程
                GlobalScope.launch ( Dispatchers.Main ){
                    //即時影像元件組 -標題-
                    cameraItem[i].titel.text = periodCameras[i].Name //設定鏡頭名稱

                    //即時影像元件組 -影像-
                    Picasso.get() //Picasso影像取得插件
                        .load( PCCU_ImageLink( periodCameras[i] ))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
                        .into(cameraItem[i].imageView) //匯入視圖

                    //設定影像點擊 連結 瀏覽器直播影像
                    cameraItem[i].imageView.setOnClickListener{
                        val uri = Uri.parse( PCCU_ExternalLink( periodCameras[i] ))
                        startActivity(Intent(Intent.ACTION_VIEW, uri!!))
                    }
                }
            }

            //取得當前時間 (小時)
            val Datum = DateFormat.format("HH", Calendar.getInstance().time).toString().toInt()
            //選用時段推薦影像組
            var periodCameras : List<CameraUrls> = listOf()
            when(Datum){
                in  0.. 6 -> periodCameras = PeriodRecommend.CamerasPeriodUrl[0] //AM.12 - AM.07
                in  7.. 9 -> periodCameras = PeriodRecommend.CamerasPeriodUrl[1] //AM.07 - AM.10
                in 10..14 -> periodCameras = PeriodRecommend.CamerasPeriodUrl[2] //AM.10 - PM.03
                in 15..18 -> periodCameras = PeriodRecommend.CamerasPeriodUrl[3] //PM.03 - PM.07
                in 19..23 -> periodCameras = PeriodRecommend.CamerasPeriodUrl[4] //PM.07 - AM.12
            }

            //更新即時影像
            for(i in periodCameras.indices) {
                //如果影像還沒初始化 -> 執行初始化
                if( i+1 > cameraItem.size)
                    cameraItem.add(initCameraItem())
                //更新即時影像內容
                loadCameraItem(periodCameras, i)
            }
        }

        /**設置更多即時影像按鈕功能
         * @param periodCameras List<[CameraUrls]> 鏡頭連結資訊表
         * @param i [Int] 項目編號
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setMoreLive_Image(){
            val moreLive_ImageButton = view.findViewById<Button>(R.id.moreLive_Image)
            moreLive_ImageButton.setOnClickListener{
                //轉換當前的頁面 至 公告內文頁面
                Navigation.findNavController(view).navigate(R.id.navigation_live_image_page)
            }
        }

        //初始化 氣溫適配器
        val weatherAdapter = WeatherAdapter()
        //初始化 首頁影片
        val CameraItem : ArrayList<CameraItem> = arrayListOf()

        /**更新內容用計時器 60s/次
         * @author KILNETA
         * @since Alpha_1.0
         */
        class CountdownTimerTask : TimerTask() {
            var i = 0 //計數

            /**更新主頁內容
             * @author KILNETA
             * @since Alpha_1.0
             */
            fun upData(){
                weatherAdapter.upDatas()
                setCameras(CameraItem)
            }

            /**重構 計時器運作內容
             * @author KILNETA
             * @since Alpha_1.0
             */
            @RequiresApi(Build.VERSION_CODES.N)
            override fun run() {
                //增加計時緩衝條數值
                WeatherUpdataProgressBar.setProgress(++i, false)
                when(i){
                    59-> upData()
                    60-> i = 0
                }
            }
        }
        //-----------------------------Main Function Start------------------------------------------
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //初始化計時器
        CountdownTimer = Timer()
        //套用計時器設定
        CountdownTimer!!.schedule(CountdownTimerTask(), 500, 500)

        //設置即時影像
        setCameras(CameraItem)
        //設置溫度列表
        setWeather(weatherAdapter)
        //日曆-行事曆設置
        setCalendar()
        //設置快速連結列表按鈕功能
        setFastLinks()
        //設置更多即時影像按鈕功能
        setMoreLive_Image()

        //------------------------------Main Function End-------------------------------------------

        //中央氣象局 雷達迴波圖 測試用
        /*
        val mIv = view.findViewById<ImageView>(R.id.CwbRadarEcho)
        GlobalScope.launch {
            val msg =
                HttpRetrofit.create_Image("https://www.cwb.gov.tw/Data/radar/CV1_TW_3600.png")!!
            withContext(Dispatchers.Main) {
                mIv.setImageBitmap(msg)
            }
        }*/

        /* 影片視圖控件實作 測試用
        val videoView = view.findViewById<VideoView>(R.id.PccuCamera)
        val uri = Uri.parse("https://camera.pccu.edu.tw/camera123/player.html?autoplay=1&mute=1")
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
        */
    }

    /**Weather列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class WeatherAdapter: RecyclerView.Adapter<MyViewHolder>(){
        /**氣溫API數據組 [Weather_Data]*/
        private val Weather_Data = mutableListOf<Weather_Data>()
        //計算滑動定位 座標變數
        /** 水平座標 */
        private var Dx = 0
        /** 當前項目座標*/
        private var NowPosition = 0

        /**更新資料
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        fun upDatas(){
            //協程調用氣溫API
            GlobalScope.launch ( Dispatchers.Main ){
                val Weather = withContext(Dispatchers.IO) {
                    Weather_API().Get()
                }
                //呼叫 重置氣溫資料
                reSetData(Weather!!)
            }
        }

        /**重製列表並導入新數據
         * @author KILNETA
         * @since Alpha_1.0
         */
        fun reSetData(Weather_Data: List<Weather_Data>){
            Log.d("upDatas", Weather_Data.toString())
            //導入數據資料
            this.Weather_Data.addAll(Weather_Data)
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**重構 創建視圖持有者 (連結weather_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false)
            return MyViewHolder(view)
        }

        /**重構 獲取展示物件數量 (天氣數值量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            return if(Weather_Data.size == 0) 1 else Weather_Data.size
        }

        /**重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            if(Weather_Data != mutableListOf<Weather_Data>()){

                val Weather = Weather_Data[position] //資料組( 大義館7F, 台北Cwb, 竹子湖Cwb )
                holder.itemView.weatherDesciption.text =
                    if(Weather.WeatherDesciption=="-99") "null"                             //天氣描述
                    else Weather.WeatherDesciption                                          //
                holder.itemView.temperature.text = Weather.Tempature                        //溫度
                holder.itemView.humidity.text = Weather.Humidity                            //濕度
                holder.itemView.windSpeed.text = Weather.WindSpeed                          //風速
                holder.itemView.rainFall.text = Weather.RainFall                            //雨量
                holder.itemView.location.text =Weather.Location                             //資料來源
                if(Weather.WeatherDesciption.contains("晴", ignoreCase = true))              //天氣狀態圖
                    holder.itemView.weather_image.setImageResource(R.drawable.sunny)        //
                else if(Weather.WeatherDesciption.contains("雨", ignoreCase = true))         //
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain)  //
            }


            val dm = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(dm)
            val vWidth = dm.widthPixels
            val vHeight = dm.heightPixels
            Log.d("WindowSize", "寬:${vWidth} 高:$vHeight") //後台報錯

            //if(vWidth>1080)

            val layoutParams: ViewGroup.LayoutParams = holder.itemView.getLayoutParams()
            layoutParams.width = vWidth.toInt()


            weather_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                //dx是水平滚动的距离，dy是垂直滚动距离，向上滚动的时候为正，向下滚动的时候为负
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    //super.onScrolled(recyclerView, dx, dy)
                    // 滑動率要超過50才會移動到下一個
                    if (dx < -50 || dx > 50) Dx = dx
                }

                /**重構 偵測滾動狀態的改變
                 * @param recyclerView [RecyclerView] 回收視圖
                 * @param newState [Int] 新狀態
                 *
                 * @author KILNETA
                 * @since Alpha_1.0
                 */
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    /** newState: Int
                        value(0) SCROLL_STATE_IDLE -當前未滾動。
                        value(1) SCROLL_STATE_DRAGGING -正被外部控制（如使用者觸摸）拖動。
                        value(2) SCROLL_STATE_SETTLING -當前正在滑動中，且不受外部控制。
                     **/
                    //super.onScrollStateChanged(recyclerView, newState)
                    // 如果有滑動率+任何滾動中+未超過項目總數 -跳轉到下一個項目
                    if (Dx < 0 && newState!=0 && NowPosition > 0)
                        weather_list.smoothScrollToPosition( --NowPosition )
                    if (Dx > 0 && newState!=0 && NowPosition < holder.getAdapterPosition())
                        weather_list.smoothScrollToPosition( ++NowPosition )
                    // 當前未滾動 //滑動率過小+非被使用者拖動 -回到當前的項目
                    if (Dx == 0 && newState!=1 || newState==0) {weather_list.smoothScrollToPosition( NowPosition )}
                    // 滑動率重置
                    Dx=0
                }
            })
        }
    }

    /**Calendar列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_2.0
     */
    inner class CalendarAdapter: RecyclerView.Adapter<MyViewHolder>(){
        /**行事曆活動事項 List<[String]>*/
        private val Calendar_Data = mutableListOf<String>()

        /**更新資料
         * @author KILNETA
         * @param Datum [String] 當天日期 (YYYY-MM-DD)
         *
         * @since Alpha_2.0
         */
        @DelicateCoroutinesApi
        fun upDatas(Datum:String){
            //取用APP中儲存的當日活動
            val lestCalendar = MySharedPreferences.Read(
                "TodayCalendar", //存儲區
                "Calendar", //存儲節點
                context!!)!!
                .split("%20") //依據分割標示字 拆分行事曆活動內容
            //如果APP中儲存的當日活動 日期與今日相符
            if(lestCalendar[0]==Datum) {
                //使用APP儲存的當日活動 不再連線網路
                reSetData(lestCalendar)
            }
            //日期與今日不相符
            else {
                //取用協程
                GlobalScope.launch {
                    //調用行事曆API (Google-Calendar) 取得當日活動
                    val Calendar = Calendar_API().Get(Datum)
                    //開始編寫存儲格式 (TodayCalendar)
                    //ex: (YYYY-MM-DD %20 活動一 %20 活動二 ...)
                    var TodayCalendar = Datum //以當天日期作為索引判斷依據 Datum(YYYY-MM-DD)
                    for (i in 0 until Calendar!!.items.size)
                        TodayCalendar +=
                            "%20" +  //分割標示字
                            Calendar.items[i].summary //一項活動標題
                    //紀錄當日活動存儲到APP中
                    MySharedPreferences.Write(
                        TodayCalendar, //當日活動數據
                        "TodayCalendar", //存儲區
                        "Calendar", //存儲節點
                        context!!
                    )

                    //返回主線程
                    withContext(Dispatchers.Main) {
                        //重新設置公告的數據資料
                        reSetData(TodayCalendar.split("%20"))
                    }
                }
            }
        }

        /**
         * 重製列表並導入新數據
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun reSetData(Calendar_Data:List<String>){
            //Log.d("upDatas", Calendar_Data.toString())
            //導入數據資料
            for( i in 1 until Calendar_Data.size)
                this.Calendar_Data.add(Calendar_Data[i])
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者 (連結Calendar_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.calendar_item,parent,false)
            return MyViewHolder(view)
        }

        /**重構 獲取展示物件數量 (行事曆活動量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun getItemCount(): Int {
            //如果沒有活動 特別保留一個用於顯示"今天沒有行事事項"
            return if(Calendar_Data.size == 0) 1 else Calendar_Data.size
        }

        /**重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if(Calendar_Data.size==0)// 如果當天沒有活動
                holder.itemView.calendar_text.text = "今天沒有行事事項"
            else                     // 當天有活動
                holder.itemView.calendar_text.text = Calendar_Data[position]
        }
    }

    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){}
}