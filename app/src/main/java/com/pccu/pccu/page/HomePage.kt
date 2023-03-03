package com.pccu.pccu.page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_page.*
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.pccu.pccu.internet.*
import com.pccu.pccu.menu.FastLinksBottomMenu
import com.pccu.pccu.R
import kotlinx.coroutines.*
import java.util.*
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.text.format.DateFormat
import android.widget.*
import com.pccu.pccu.page.liveImage.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.calendar_item.view.*
import java.util.Calendar
import com.squareup.picasso.MemoryPolicy
import java.text.SimpleDateFormat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.PagerSnapHelper
import com.pccu.pccu.sharedFunctions.DateConvert
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.appStart.CwbMainActivity
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.weather_item.view.*

/**
 * 程式主頁面 : "Fragment(home_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class HomePage : Fragment(R.layout.home_page){

    /**倒計時器*/
    private var countdownTimer:Timer? = null
    /**計時器計數*/
    private var timer = 58
    /**weather適配器*/
    private val weatherAdapter = WeatherAdapter()
    /**行事曆適配器*/
    private val calendarAdapter = CalendarAdapter()
    /**首頁影片視圖表*/
    private val cameraList : ArrayList<CameraItem> = arrayListOf()
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null
    /**初始化行事曆*/
    private var initCalendar = true
    /**活動濾波器(監測斷線問題)*/
    private val itFilter = IntentFilter()

    init{
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
    }

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    timer = 58

                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )

                    if(initCalendar) {
                        /**當前日期*/
                        val presentDate = DateFormat.format(
                            "yyyy-MM-dd",
                            Calendar.getInstance().time
                        ).toString()

                        calendarAdapter.upDates(presentDate)
                    }
                }
            },
            requireContext()
        )
    }

    /**
     * 日曆圖示初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun initCalendar(){
        //點擊日曆圖示 開啟整個GOOGLE行事曆頁面
        CalendarView.setOnClickListener{
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(CalendarAPI.PccuGoogleCalendarUrl)
                )
            )
        }

        /**當前日期*/
        val presentDate = DateFormat.format(
            "yyyy-MM-dd",
            Calendar.getInstance().time
        ).toString()
        /**當前日期陣列*/
        val presentDates = presentDate.split("-")

        //設置小日曆圖示內部數據(年/月/日)
        calendarYear.text = presentDates[0]
        calendarMoon.text = DateConvert.monthNumToStr3(presentDates[1])
        calendarDay. text = presentDates[2]

        //行事曆列表控件 calendar_list 的設置佈局管理器 (列表)
        calendar_list.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        //行事曆列表 關閉滑動
        calendar_list.isNestedScrollingEnabled = false
        //行事曆列表 連接適配器
        calendar_list.adapter = calendarAdapter
        //首次更新數據
        calendarAdapter.upDates(presentDate)
    }

    /**
     * 初始化氣溫列表設置
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    @DelicateCoroutinesApi
    private fun initWeather() {
        //氣溫列表控件 weather_list 的設置佈局管理器 (列表)
        weather_list.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        //輔助RecyclerView在滾動結束時將Item對齊到某個位置
        PagerSnapHelper().attachToRecyclerView(weather_list)
        //weather頁面 適配器
        weather_list.adapter = weatherAdapter
    }

    /**
     * 初始化快速連結列表按鈕功能
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initFastLinks(){
       //快速連結按鈕被點下 開啟快速連結列表 底部彈窗
        FastLink.setOnClickListener{
            /**快速連結底部彈窗*/
            val fastLinkSheetFragment = FastLinksBottomMenu()
            fastLinkSheetFragment.show(parentFragmentManager, fastLinkSheetFragment.tag)
        }
    }

    /**初始化即時影像視圖
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initCameras(){
        //清空曾創建的即時影像視圖
        cameraList.clear()
        for(i in 0 until CameraAPI.PeriodSize) {
            /**螢幕寬度 (用於計算影像長寬比)*/
            val vWidth = ViewGauge.getDisplayWidth(requireActivity())

            /**即時影像控件初始化 -編號資料-*/
            val cameraItem = CameraItem(
                TextView(this.requireContext()),
                ImageView(this.requireContext())
            )

            //標題控件 設定
            cameraItem.title.setPadding(70, 10, 0, 10)
            cameraItem.title.textSize = 18F
            cameraItem.title.setBackgroundColor(Color.parseColor("#7EBCA8"))

            //影像比例 設定
            cameraItem.imageView.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    vWidth / 16 * 9
                )

            /**即時影像視圖組*/
            val cameraView = LinearLayout(this.requireContext())
            cameraView.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            //垂直布局
            cameraView.orientation = LinearLayout.VERTICAL

            //將控件綁定於視圖組上
            cameraView.addView(cameraItem.title)
            cameraView.addView(cameraItem.imageView)

            //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
            Camera.addView(cameraView)

            //保存 影像控件 -編號資料-
            cameraList.add(cameraItem)
        }
    }

    /**加載即時影像內容
     * @param periodCameras List<[CameraUrls]> 鏡頭連結資訊表
     * @param i [Int] 項目編號
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun loadCameraImage(periodCameras : List<CameraUrls>, i:Int){
        //返回主線程
        GlobalScope.launch ( Dispatchers.Main ){
            //即時影像元件組 -標題-
            cameraList[i].title.text = periodCameras[i].Name //設定鏡頭名稱

            //即時影像元件組 -影像-
            Picasso.get() //Picasso影像取得插件
                .load( CameraAPI.pccuImageLink( periodCameras[i] ))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
                .placeholder(cameraList[i].imageView.drawable) //加載中顯示圖片 (上一張即時影像)
                .error(R.drawable.no_image) //連線錯誤顯示圖片
                .into(cameraList[i].imageView) //匯入視圖

            //設定影像點擊 連結 瀏覽器直播影像
            cameraList[i].imageView.setOnClickListener{
                /**影像連結*/
                val uri = Uri.parse( CameraAPI.pccuExternalLink( periodCameras[i] ))
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        }
    }

    /**
     * 更新即時影像
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun updatedCameras(){
        /**當前時間 (小時)*/
        val presentHour =
            DateFormat.format("HH", Calendar.getInstance().time).toString().toInt()
        /**選用時段推薦影像組*/
        val periodCameras = CameraAPI.periodRecommendCameras(presentHour)
        //加載即時影像內容
        for(i in 0 until CameraAPI.PeriodSize) {
            loadCameraImage(periodCameras, i)
        }
    }

    /**
     * 初始化更多即時影像按鈕功能
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initMoreLiveImage(){
        moreLiveImage_Button.setOnClickListener{
            //轉換當前的頁面 至 公告內文頁面
            startActivity(Intent().setClass(requireContext(), LiveImagePage::class.java))
        }
    }

    /**
     * 更新主頁內容
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    fun updatedHomePage(){
        GlobalScope.launch ( Dispatchers.Main ){
            //更新氣溫資料
            weatherAdapter.upDates()
            //更新即時影像內容
            updatedCameras()
        }
    }

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
        override fun run() {
            //增加計時緩衝條數值
            WeatherUpdataProgressBar.setProgress(++timer, false)
            when(timer){
                59-> updatedHomePage()
                60-> timer = 0
            }
        }
    }

    /**
     * 根據時間初始化歡迎語
     * @author KILNETA
     * @since Beta_1.1.1
     */
    private fun initWelcomeMessage(){
        val currentTime = Calendar.getInstance().time
        val currentHour = DateFormat.format("HH",currentTime).toString().toInt()

        homeTitle.text =
            when(currentHour){
            in 5..7->"早安！祝你有美好的一天！"
            in 8..10->"早安！(๑•̀ㅂ•́)و✧"
            in 11..12->"午安！午餐要吃什麼好呢？"
            in 13..17->"午安！o(￣▽￣)ｄ"
            in 18..19->"吃晚餐囉！ヽ(✿ﾟ▽ﾟ)ノ"
            in 20..23->"夜生活開始囉！把握時間～♪(^∇^*)"
            else ->"夜深了喔！注意休息～"
        }
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
        super.onViewCreated(view, savedInstanceState) //創建頁面
        //根據時間初始化歡迎語
        initWelcomeMessage()
        //網路接收器初始化
        initInternetReceiver()
        //設定氣溫列表
        initWeather()
        //初始化即時影像控件
        initCameras()
        //日曆-行事曆設置
        initCalendar()
        //設置快速連結列表按鈕功能
        initFastLinks()
        //設置更多即時影像按鈕功能
        initMoreLiveImage()
    }

    /**
     * home_page頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStart() {
        super.onStart()

        //初始化網路接收器
        activity?.registerReceiver(internetReceiver, itFilter)

        //重新計數
        timer = 58
        //初始化計時器
        countdownTimer = Timer()
        //套用計時器設定
        countdownTimer!!.schedule(
            CountdownTimerTask(),
            500,
            500
        )
    }

    /**
     * home_page當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        activity?.unregisterReceiver(internetReceiver)
        //關閉計時器 (避免持續計時導致APP崩潰)
        //關閉計時器 (避免持續計時導致APP崩潰)
        countdownTimer!!.cancel()
        countdownTimer = null //如果不重新new，會報異常
    }

    /**
     * Weather列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    inner class WeatherAdapter: RecyclerView.Adapter<RV.ViewHolder>(){
        /**行事曆活動事項*/
        private var weatherData = mutableListOf<WeatherData>()

        /**更新資料
         * @author KILNETA
         *
         * @since Alpha_3.0
         */
        @DelicateCoroutinesApi
        fun upDates(){
            //判斷網路能否使用
            if(!internetReceiver!!.isConnect) return

            GlobalScope.launch(Dispatchers.Main) {
                /**取得氣溫資料*/
                val weather = withContext(Dispatchers.IO) {
                    WeatherAPI().get()
                }
                //更新氣溫數據
                weather?.let { resetData(it) }
            }
        }

        /**
         * 重製列表並導入新數據
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        private fun resetData(weather:List<WeatherData>?){
            weatherData = (weather as MutableList<WeatherData>?)!!
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者 (連結Weather_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**視圖綁定XML布局 (佈局膨脹)*/
            val view =
                LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false)
            return RV.ViewHolder(view)
        }

        /**重構 獲取展示物件數量 (氣溫資料數)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun getItemCount(): Int {
            //如果沒有資料來源 特別保留一個用於顯示"NoDataSource"
            return if(weatherData.size == 0) 1 else weatherData.size
        }

        /**重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {

            //氣溫資料非為空
            if(weatherData.isNotEmpty()) {
                /**取出相對應位置的元素*/
                val weather = weatherData[position]

                holder.itemView.weatherDesciption.text =
                    if (weather.WeatherDesciption == "-99") "null"                        //天氣描述
                    else weather.WeatherDesciption                                        //
                holder.itemView.temperature.text = weather.Tempature                      //溫度
                holder.itemView.humidity.text = weather.Humidity                          //濕度
                holder.itemView.windSpeed.text = weather.WindSpeed                        //風速
                holder.itemView.rainFall.text = weather.RainFall                          //雨量
                holder.itemView.location.text = weather.Location                          //資料來源

                //氣象描述縮圖判斷式 (同文化大學官網判斷公式)
                if (weather.WeatherDesciption.contains("雨")){
                    if (     weather.WeatherDesciption.contains("雷")
                         &&  weather.WeatherDesciption.contains("霧"))
                        holder.itemView.weather_image.setImageResource(R.drawable.fog_thunder)
                        //雨雷霧

                    else if (weather.WeatherDesciption.contains("雷"))
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain_thunder)
                        //雨雷

                    else if (weather.WeatherDesciption.contains("霧"))
                        holder.itemView.weather_image.setImageResource(R.drawable.fog_rain)
                        //雨霧

                    else if (weather.WeatherDesciption.contains("晴"))
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_cloudy_rain)
                        //雨晴

                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain)
                        //雨
                }

                else if(     weather.WeatherDesciption.contains("霧")) {
                    if (     weather.WeatherDesciption.contains("晴")) {
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_fog)
                        //霧晴
                    }

                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_fog)
                        //霧
                }

                else if (    weather.WeatherDesciption.contains("晴")) {
                    if (     weather.WeatherDesciption.contains("雲"))
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_cloudy)
                        //晴雲

                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny)
                        //晴
                }

                else if (    weather.WeatherDesciption.contains("多雲"))
                    holder.itemView.weather_image.setImageResource(R.drawable.muti_cloudy)
                    //多雲

                else if (    weather.WeatherDesciption.contains("雪")) {
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy_snow)
                    //雪
                }

                else
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy)
                    //陰
            }

            //使用點擊跳轉 CWB & EPA 頁面
            holder.itemView.setOnClickListener {
                //轉換當前的頁面 至  CWB & EPA 頁面
                //新方案 (新建Activity介面展示)
                startActivity(Intent().setClass(context!!, CwbMainActivity::class.java ))
            }
        }
    }

    /**Calendar列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_2.0
     */
    inner class CalendarAdapter: RecyclerView.Adapter<RV.ViewHolder>(){
        /**行事曆活動事項*/
        private val calendarData = mutableListOf<String>()
        /**當前位置*/
        private var currentPosition = 0

        /**檢查行事曆內容是否為今天的行程
         * @param startDate [String] 行程開始日期 (YYYY-MM-DD)
         * @param endDate [String] 行程結束日期 (YYYY-MM-DD)
         * @param presentDate [String] 當天日期 (YYYY-MM-DD)
         * @return 是否為今天的行程 : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        private fun checkCalendarDate(startDate:String, endDate:String, presentDate:String): Boolean{
            //換算為 Date 單位 用於比較日期先後關係
            /**公告 起始時間*/
            val startDates  = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).parse(startDate)
            /**公告 結束時間*/
            val endDates    = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).parse(endDate)
            /**當前時間*/
            val presentDates= SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).parse(presentDate)

            //行事曆內容 是 今天的行程
            return (startDates!! <= presentDates) && (presentDates!! < endDates)
        }

        /**更新資料
         * @author KILNETA
         * @param Datum [String] 當天日期 (YYYY-MM-DD)
         *
         * @since Alpha_2.0
         */
        @DelicateCoroutinesApi
        fun upDates(Datum:String){

            /**取用APP中儲存的當日活動*/
            var lestCalendar : ToDayCalendar? = null
            /**嘗試取得存儲的天氣預報資料*/
            val sP = Object_SharedPreferences["Calendar", "TodayCalendar", context!!]

            //確認取得存儲的天氣預報資料 並非為空
            if(sP!=null) {
                //將取得的Any類別套用至CwbForecast_Save資料結構中使用
                lestCalendar = sP as ToDayCalendar
            }

            //如果APP中儲存的當日活動 日期與今日相符
            if(lestCalendar!=null && lestCalendar.updated == Datum) {
                //使用APP儲存的當日活動 不再連線網路
                resetData(lestCalendar)
            }
            //日期與今日不相符
            else {
                //判斷網路能否使用
                if(!internetReceiver!!.isConnect) return
                //取用協程
                GlobalScope.launch {
                    /**調用行事曆API (Google-Calendar) 取得當日活動*/
                    val calendarSource = CalendarAPI.get(Datum)
                    /**存儲用檔案格式宣告*/
                    val calendarData = ToDayCalendar(Datum)
                    calendarSource?.let {
                        //開始編寫存儲格式 (TodayCalendar)
                        //使用資料結構方式存儲
                        for (i in calendarSource.items.indices) {
                            //確認公告時間是本日 (API連接設置條件限制有時會失效)
                            if (checkCalendarDate(
                                    calendarSource.items[i].start.date,
                                    calendarSource.items[i].end.date,
                                    Datum)
                            ) {
                                calendarData.items.add(calendarSource.items[i])
                            }
                        }
                        //紀錄當日活動存儲到APP中
                        Object_SharedPreferences.save(
                            "Calendar",
                            "TodayCalendar",
                            calendarData,
                            context!!)

                        //返回主線程
                        withContext(Dispatchers.Main) {
                            //重新設置公告的數據資料
                            resetData(calendarData)
        }   }   }   }   }

        /**
         * 重製列表並導入新數據
         * @param calendarData [ToDayCalendar] 行事曆資料
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        private fun resetData(calendarData: ToDayCalendar){
            //清空數據資料
            this.calendarData.clear()
            //重新導入數據資料
            for( i in 0 until calendarData.items.size)
                this.calendarData.add(calendarData.items[i].summary)
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()

            initCalendar = false
        }

        /**
         * 重構 創建視圖持有者 (連結Calendar_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**視圖綁定XML布局 (佈局膨脹)*/
            val view =
                LayoutInflater.from(context).inflate(R.layout.calendar_item,parent,false)
            return RV.ViewHolder(view)
        }

        /**重構 獲取展示物件數量 (行事曆活動量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun getItemCount(): Int {
            //如果沒有活動 特別保留一個用於顯示"今天沒有行事事項"
            return if(calendarData.size == 0) 1 else calendarData.size
        }

        /**重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {

            if(calendarData.size==0)// 如果當天沒有活動
                holder.itemView.calendar_text.text = "今天沒有行事事項"
            else                     // 當天有活動
                holder.itemView.calendar_text.text = calendarData[position]

            //使用點擊跳轉行事曆列表物件
            holder.itemView.setOnClickListener {
                //回到首部
                if(++currentPosition >= itemCount)
                    currentPosition = 0

                //跳轉列表物件
                calendar_list.smoothScrollToPosition(
                    currentPosition
                )
}   }   }   }