package com.example.pccu.Page

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_page.*

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pccu.Internet.*
import com.example.pccu.Menu.FastLinks_BottomMenu

import com.example.pccu.R
import kotlinx.coroutines.*
import java.util.*
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.format.DateFormat
import android.widget.*
import com.example.pccu.Page.Live_Image.*
import com.example.pccu.Shared_Functions.MySharedPreferences
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.calendar_item.view.*
import java.util.Calendar
import com.example.pccu.Page.Live_Image.UrlSource.PCCU_ExternalLink
import com.example.pccu.Page.Live_Image.UrlSource.PCCU_ImageLink
import com.squareup.picasso.MemoryPolicy
import kotlinx.android.synthetic.main.live_image_page.*
import java.text.SimpleDateFormat
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.weather_item.view.*
import kotlinx.android.synthetic.main.about_view.*





/**
 * 程式主頁面 : "Fragment(home_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Home_Page : Fragment(R.layout.home_page){

    //倒計時器
    private var CountdownTimer:Timer? = null

    /**
     * home_page頁面被關閉
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * 日曆視窗設置
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
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
        view!!.findViewById<LinearLayout>(R.id.CalendarView)
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

        //當前日期
        val Datum = DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time).toString()
        //拆分成日期陣列
        val Dates = Datum.split("-")

        //設置小日曆圖示內部數據(年月份)
        val CalendarYear = view!!.findViewById<TextView>(R.id.CalendarYear)
        val CalendarMoon = view!!.findViewById<TextView>(R.id.CalendarMoon)
        val CalendarDate = view!!.findViewById<TextView>(R.id.CalendarDate)
        CalendarYear.setText(Dates[0])
        CalendarMoon.setText(monthConversion(Dates[1]))
        CalendarDate.setText(Dates[2])

        //行事曆列表呼叫
        calendar_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        //行事曆列表 關閉滑動
        calendar_list.isNestedScrollingEnabled = false
        //初始化 行事曆適配器
        val CalendarAdapter = CalendarAdapter()
        //行事曆列表 連接適配器
        calendar_list.adapter = CalendarAdapter
        //首次更新數據
        CalendarAdapter.upDatas(Datum)
    }
    //創建weather頁面資料
    val WeatherAdapter = weatherAdapter()

    /**
     * 氣溫列表設置
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    @DelicateCoroutinesApi
    fun setWeather() {
        //行事曆列表呼叫
        weather_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(weather_list)
        //weather頁面 是配器
        weather_list.adapter = WeatherAdapter
    }

    /**
     * 設置快速連結列表按鈕功能
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun setFastLinks(){
        val FastLinkButton = view!!.findViewById<Button>(R.id.FastLink)
        FastLinkButton.setOnClickListener{
            val FastLinkSheetFragment = FastLinks_BottomMenu()
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
            view!!.findViewById<LinearLayout>(R.id.Camera)
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
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun setMoreLive_Image(){
        val moreLive_ImageButton = view!!.findViewById<Button>(R.id.moreLive_Image)
        moreLive_ImageButton.setOnClickListener{
            //轉換當前的頁面 至 公告內文頁面
            //新方案 (新建Activity介面展示)
            val IntentObj = Intent()
            IntentObj.setClass(context!!, LiveImage_Page::class.java )
            startActivity(IntentObj)

            // 棄用方案 (直接更換當前View)
            // Navigation.findNavController(view!!).navigate(R.id.navigation_live_image_page)
        }
    }

    //初始化 首頁影片
    val CameraItem : ArrayList<CameraItem> = arrayListOf()

    //初始化 計時器計數
    var Timer_i = 58 //計數

    /**更新主頁內容
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    fun upData(){
        GlobalScope.launch ( Dispatchers.Main ){
            WeatherAdapter.upDatas()
            setCameras(CameraItem)
        }
    }

    /**更新內容用計時器 60s/次
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class CountdownTimerTask : TimerTask() {
        /**重構 計時器運作內容
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        @RequiresApi(Build.VERSION_CODES.N)
        override fun run() {
            //增加計時緩衝條數值
            WeatherUpdataProgressBar.setProgress(++Timer_i, false)
            when(Timer_i){
                59-> upData()
                60-> Timer_i = 0
            }
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

        //-----------------------------Main Function Start------------------------------------------
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //設定氣溫列表
        setWeather()
        //刪除影像首頁影片物件資料
        CameraItem.clear()
        //初始化 計時器計數
        Timer_i = 58
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

    override fun onStart() {
        super.onStart()
        Timer_i = 58
        //初始化計時器
        CountdownTimer = Timer()
        //套用計時器設定
        CountdownTimer!!.schedule(CountdownTimerTask(), 500, 500)
    }

    /**
     * home_page當頁面不可見時
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        //關閉計時器 (避免持續計時導致APP崩潰)
        CountdownTimer!!.cancel()
        CountdownTimer = null //如果不重新new，会报异常
    }

    /**Calendar列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_3.0
     */
    inner class weatherAdapter: RecyclerView.Adapter<MyViewHolder>(){
        /**行事曆活動事項 List<[String]>*/
        private var Weather_Data = mutableListOf<Weather_Data>()

        /**更新資料
         * @author KILNETA
         * @param Datum [String] 當天日期 (YYYY-MM-DD)
         *
         * @since Alpha_3.0
         */
        @DelicateCoroutinesApi
        fun upDatas(){
            GlobalScope.launch(Dispatchers.Main) {
                //取得氣溫資料
                val weather = withContext(Dispatchers.IO) {
                    Weather_API().Get()
                }
                //更新氣溫數據
                reSetData(weather!!)
            }
        }

        /**
         * 重製列表並導入新數據
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        fun reSetData(weather:List<Weather_Data>?){
            Weather_Data = (weather as MutableList<Weather_Data>?)!!
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者 (連結Weather_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false)
            return MyViewHolder(view)
        }

        /**重構 獲取展示物件數量 (行事曆活動量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun getItemCount(): Int {
            //如果沒有活動 特別保留一個用於顯示"今天沒有行事事項"
            return if(Weather_Data.size == 0) 1 else Weather_Data.size
        }

        /**重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_3.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            if(Weather_Data.isNotEmpty()) {

                val weather = Weather_Data[position]

                holder.itemView.weatherDesciption.text =
                    if (weather.WeatherDesciption == "-99") "null"                        //天氣描述
                    else weather.WeatherDesciption                                        //
                holder.itemView.temperature.text = weather.Tempature                      //溫度
                holder.itemView.humidity.text = weather.Humidity                          //濕度
                holder.itemView.windSpeed.text = weather.WindSpeed                        //風速
                holder.itemView.rainFall.text = weather.RainFall                          //雨量
                holder.itemView.location.text = weather.Location                          //資料來源

                if (weather.WeatherDesciption.contains("雨", ignoreCase = true)){
                    if (     weather.WeatherDesciption.contains("雷", ignoreCase = true)
                         &&  weather.WeatherDesciption.contains("霧", ignoreCase = true))
                        holder.itemView.weather_image.setImageResource(R.drawable.fog_thunder)
                    else if (weather.WeatherDesciption.contains("雷", ignoreCase = true))
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain_thunder)
                    else if (weather.WeatherDesciption.contains("霧", ignoreCase = true))
                        holder.itemView.weather_image.setImageResource(R.drawable.fog_rain)
                    else if (weather.WeatherDesciption.contains("晴", ignoreCase = true))
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_cloudy_rain)
                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain)
                }
                else if(     weather.WeatherDesciption.contains("霧", ignoreCase = true)) {
                    if (     weather.WeatherDesciption.contains("晴", ignoreCase = true)) {
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_fog)
                    }
                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.cloudy_fog)
                }
                else if (    weather.WeatherDesciption.contains("晴", ignoreCase = true)) {
                    if (     weather.WeatherDesciption.contains("雲", ignoreCase = true))
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny_cloudy)
                    else
                        holder.itemView.weather_image.setImageResource(R.drawable.sunny)
                }
                else if (    weather.WeatherDesciption.contains("多雲", ignoreCase = true))
                    holder.itemView.weather_image.setImageResource(R.drawable.muti_cloudy)

                else if (    weather.WeatherDesciption.contains("雪", ignoreCase = true)) {
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy_snow)
                }
                else
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy)
            }
        }
    }
    /*
    /**
     * 氣溫列表設置
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun setWeather( /*weatherAdapter:WeatherAdapter*/) {

        if(pageAdapter == null) {
            GlobalScope.launch(Dispatchers.Main) {
                val weather = withContext(Dispatchers.IO) {
                    Weather_API().Get()
                }

                /**頁面適配器*/
                //創建weather頁面資料
                pageAdapter = weatherAdapter(childFragmentManager, lifecycle, weather!!)
                //weather頁面 是配器
                weather_list.adapter = pageAdapter
            }
        }
        else{
            GlobalScope.launch(Dispatchers.Main) {
                //取得氣溫資料
                val weather = withContext(Dispatchers.IO) {
                    Weather_API().Get()
                }
                //更新氣溫數據
                pageAdapter!!.upData(weather!!)
            }
        }

        /*
        //氣溫列表呼叫
        weather_list.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //氣溫列表 連接適配器
        weather_list.adapter = weatherAdapter
        //首次更新數據
        weatherAdapter.upDatas()
        */
    }
    */
    /*
    /**
     * 天氣頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param fragments ArrayList<[Fragment]> 欲展視的片段視圖
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    class weatherAdapter(
        fragmentManager: FragmentManager, // 子片段管理器
        lifecycle: Lifecycle, // 生命週期
        weather: List<Weather_Data>
    ):  FragmentStateAdapter( // 片段狀態適配器
        fragmentManager, // 片段管理器
        lifecycle // 生命週期
    ){

        /**
         * LiveImage_Page頁面控件適配器
         * @param weather List<[Weather_Data]> 氣象資料表
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setFragments(weather: List<Weather_Data>): ArrayList<Weather_Fragment> {
            val Fragments: ArrayList<Weather_Fragment> = arrayListOf()
            for (i in weather.indices)
                Fragments.add(Weather_Fragment.newInstance(weather[i]))
            return Fragments
        }

        val fragments: ArrayList<Weather_Fragment> = setFragments(weather)


        fun upData(weather: List<Weather_Data>){
            for (j in fragments.indices){
                for (i in weather.indices){
                    if(fragments[j].getView() != null && fragments[j].location.text == weather[i].Location) {
                        fragments[j].reUpData(weather[i])
                        break
                    }
                }
            }
        }

        /**頁面數量
         * @return 頁面數量 : [Int]
         */
        override fun getItemCount(): Int {
            return fragments.size
        }

        /**創建頁面
         * @param position [Int] 頁面數量
         * @return 頁面 : [Fragment]
         */
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
    */

    /**Calendar列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_2.0
     */
    inner class CalendarAdapter: RecyclerView.Adapter<MyViewHolder>(){
        /**行事曆活動事項 List<[String]>*/
        private val Calendar_Data = mutableListOf<String>()
        private var currentPosition = 0

        /**檢查行事曆內容是否為今天的行程
         * @author KILNETA
         * @param startDate [String] 行程開始日期 (YYYY-MM-DD)
         * @param endDate [String] 行程結束日期 (YYYY-MM-DD)
         * @param Datum [String] 當天日期 (YYYY-MM-DD)
         *
         * @since Alpha_3.0
         */
        fun check_Calendar_StartEnd_Date(startDate:String, endDate:String, Datum:String): Boolean{
            //換算為 Date 單位 用於比較日期先後關係
            val startDates = SimpleDateFormat("yyyy-MM-dd").parse(startDate)
            val endDates = SimpleDateFormat("yyyy-MM-dd").parse(endDate)
            val Datums = SimpleDateFormat("yyyy-MM-dd").parse(Datum)

            //行事曆內容 是 今天的行程
            if(startDates <= Datums && Datums < endDates)
                return true
            //行事曆內容 不是 今天的行程
            return false
        }

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
                        if(check_Calendar_StartEnd_Date(
                                Calendar.items[i].start.date,
                                Calendar.items[i].end.date,
                                Datum )
                        ) {
                            TodayCalendar +=
                                "%20" +  //分割標示字
                                Calendar.items[i].summary //一項活動標題
                        }
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

            //使用點擊跳轉行事曆列表物件
            holder.itemView.setOnClickListener {
                //Log.d("upDatas", "${currentPosition}") //測試是第幾個控件被點擊

                //回到首部
                if(++currentPosition >= itemCount)
                    currentPosition = 0

                //跳轉列表物件
                calendar_list.smoothScrollToPosition(
                    currentPosition
                )
            }
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