package com.pccu.pccu.page.cwb

import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pccu.pccu.R
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.pccu.pccu.about.AboutBottomSheet
import java.util.*
import com.pccu.pccu.internet.*
import com.pccu.pccu.menu.MoreLocationBottomMenu
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.OffsetPageTransformer
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

/**
 * CWB氣象資料 主頁面 頁面建構類 : "Fragment(cwb_home_page)"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class CwbHomePage : Fragment(R.layout.cwb_home_page) {

    /**展示天氣預報資料之 縣市*/ //(預設為臺北市 同時適用"未自訂縣市")
    var targetLocation = "臺北市"
    /**暫存氣象預報資料(全縣市)*/ //減少反覆讀取時間
    private var weatherForecast : CwbForecastSave? = null
    /**暫存空汙指標(全縣市)*/
    private var airQualityData = MoenvAirQuality()
    /**CWB第一次加載數據*/
    private var initCwb = true
    /**CWB第一次加載數據*/
    private var initEpa = true
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    private val itFilter = IntentFilter()
    init {
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
        val pageView = this.view
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    pageView?.findViewById<TextView>(R.id.noNetWork)?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    pageView?.findViewById<TextView>(R.id.noNetWork)?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    if(initCwb)
                        //首次調用展示天氣預報資料
                        initCwbForecastPageView()
                    if(initEpa)initEpa
                        //取得空污預報資料
                        initEpaAirQuality()
                }
            },
            requireContext()
        )
    }

    /**
     * 設置選擇縣市列表按鈕功能
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun setMoreLocation(){
        //當 選擇縣市列表按鈕 被按下
        this.view?.findViewById<MaterialButton>(R.id.Cwb_moreLocation)?.setOnClickListener{
            /**選擇縣市列表 底部彈窗*/
            val moreLocationSheetFragment = MoreLocationBottomMenu(requireView(),this)
            moreLocationSheetFragment.show(parentFragmentManager, moreLocationSheetFragment.tag)
        }
    }

    /**
     * 設置關於按鈕功能
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun setAboutButton(){
        /**關於介面 內文*/
        val content = arrayOf(
            "提醒：",
            "　　氣象資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
            "　　各預報資料會在下次瀏覽時優先顯示，最後一次劉覽之地區預報",
            "",
            "資料來源：",
            "　　交通部中央氣象局、行政院環境保護署"
        )

        //當 關於按鈕 被按下
        this.view?.findViewById<MaterialButton>(R.id.aboutButton)?.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = AboutBottomSheet(content)
            aboutSheetFragment.show(parentFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**
     * 存儲用戶選擇 顯示縣市
     * @param newLocation [String] 新縣市
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun saveTargetLocation(newLocation : String){
        Object_SharedPreferences.save(
            "Cwb",
            "TargetLocation",
            newLocation,
            requireContext()
        )
    }

    /**
     * 取得存儲中用戶選擇 顯示縣市
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun getTargetLocation(){
        /**得到用戶最後選擇的縣市*/
        val sP = Object_SharedPreferences["Cwb", "TargetLocation", requireContext()]

        //如果沒有資料 則 使用預設值(臺北市)
        if(sP != null)
            targetLocation = sP as String

        //更改目標縣市 = 存儲中的目標縣市
        this.view?.findViewById<TextView>(R.id.Cwb_Location)?.text = targetLocation
    }

    /**
     * 氣象 尋找相對應縣市的預報資料
     * @param cwbForecast ArrayList<[CwbForecast]> 氣象預報資料(全縣市)
     * @param findLocation [String] 目標縣市
     * @return 目標預報資料(該縣市) : ArrayList<[TimePeriodForecast]>
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun cwbFilterLocation(
        cwbForecast : ArrayList<CwbForecast>,
        findLocation : String )
    : ArrayList<TimePeriodForecast>? {

        //篩選對應地區的氣象預報資料
        cwbForecast.forEach {
            if(it.Location == findLocation)
                return it.timePeriod
        }
        //沒找到回傳 null
        return null
    }

    /**
     * 空汙 尋找相對應縣市的預報資料
     * @param epaForecast [EpaAirQuality] 空汙預報資料(指定地區)
     * @return 目標預報資料(指定地區) : ArrayList<[EpaAirQualityData]>
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun epaFilterLocation(epaForecast : MoenvAirQuality): MoenvAirQualityData? {
        /**EPA地區*/
        var epaArea: String? = null
        //換算Cwb縣市 對應之 EPA地區
        CwbSource.EPA_locationArea.forEach {
            if(it.CwbLocations.contains(targetLocation))
                epaArea = it.EpaLocation
        }

        if(epaArea != null) {
            epaForecast.Moenv_airQualityData.forEach {
                //找到回傳該縣市氣象預報資料
                if (it.locationArea == epaArea)
                    return it
            }
        }

        //沒找到回傳 null
        return null
    }

    /**
     * 氣象 創建預報展示頁面
     * @param weatherForecast ArrayList<[TimePeriodForecast]> 欲展示氣象預報資料(該縣市)
     * @return 預報展示頁面 : ArrayList<[Fragment]>
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun upDataCwbForecast(
        weatherForecast : ArrayList<TimePeriodForecast>?
    ) : ArrayList<Fragment> {
        /**氣象預報建構之頁面*/
        val fragments = arrayListOf<Fragment>()

        //如果沒有預報資料 -> 顯示 NoDataSource
        if(weatherForecast==null)
            fragments.add(CwbWeatherFragment(null))

        //有預報資料 -> 顯示 預報資料
        else
            weatherForecast.forEach {
                fragments.add(CwbWeatherFragment(it))
            }

        //回傳已創建的預報資料頁面
        return fragments
    }

    /**
     * 設置 CwbForecast頁面 適配器 to (Cwb_value_page)
     * @param fragments ArrayList<[Fragment]> 欲展示氣象預報資料頁面
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun setCwbForecastPageAdapter(fragments : ArrayList<Fragment>){
        /**顯示指標*/
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            requireActivity().display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().windowManager.defaultDisplay.getMetrics(outMetrics)
        }
        /**螢幕寬度 (用於計算影像長寬比)*/
        val vWidth = outMetrics.widthPixels

        //創建CWBForecast頁面資料
        val pageAdapter = PageAdapter(childFragmentManager, lifecycle, fragments)
        //CWBForecast頁面 適配器
        val cwbValuePage = this.view?.findViewById<ViewPager2>(R.id.Cwb_value_page)
        cwbValuePage?.adapter = pageAdapter
        //重新調整頁面邊界與動畫
        val scale = resources.displayMetrics.density
        val x = (vWidth-(280*scale)).toInt()
        cwbValuePage?.setPageTransformer(OffsetPageTransformer(x, -x))
        //同時加載最大頁面為3
        cwbValuePage?.offscreenPageLimit = 3
    }

    /**
     * 重新調取CWB預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    private fun updatedCwbForecast(){
        //使用協程調用CWB_API天氣預報資料
        GlobalScope.launch ( Dispatchers.Main ) {
            val wF = withContext(Dispatchers.IO) {
                CwbAPI.getWeatherForecast()
            }
            wF?.let {
                //調用CWB_API取得天氣預報資料 並經過制式重構資料->( CwbForecast_Save 存儲用氣象預報資料)
                weatherForecast =
                    CwbAPI.refactorCwbSource( wF )
                //協程調用 天氣預報資料存儲到APP中
                withContext(Dispatchers.IO) {
                    Object_SharedPreferences.save(
                        "Cwb_Forecast",
                        "All_LocationForecast",
                        weatherForecast!!,
                        requireContext()
                    )
                }
                resetCwbForecast()
            }
        }
    }

    /**
     * 重製氣象預報資料頁面
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun resetCwbForecast(){
        weatherForecast?.let {
            //選出用戶選擇之地區天氣預報
            val forecastOfLocation = cwbFilterLocation(weatherForecast!!.CwbForecast, targetLocation)
            //重設展示頁面 顯示該地區之天氣預報
            setCwbForecastPageAdapter(upDataCwbForecast(forecastOfLocation))

            initCwb = false
        }
    }

    /**
     * 首次調用展示天氣預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    private fun initCwbForecastPageView(){
        //預建構無內容視圖
        setCwbForecastPageAdapter(
            arrayListOf(CwbWeatherFragment(null))
        )

        //測試用-----VVV-----
        //Object_SharedPreferences.clear("Cwb_Forecast", context!!)

        //嘗試取得存儲的天氣預報資料
        val sP = Object_SharedPreferences["Cwb_Forecast", "All_LocationForecast", requireContext()]
        //存儲天氣預報資料 最早結束時間 (預設null確保無資料時一定會更新天氣預報)
        var saveEndDate : Date? = null
        //存儲天氣預報資料 最早開始時間 (預設null確保無資料時一定會更新天氣預報)
        var saveStartDate : Date? = null
        //當前時間
        val presentDate = Calendar.getInstance().time

        //確認取得存儲的天氣預報資料 並非為空
        if(sP!=null) {
            //將取得的Any類別套用至CwbForecast_Save資料結構中使用
            weatherForecast = sP as CwbForecastSave
            //將比較時間設置為 存儲的天氣預報資料中 最早開始時間
            saveStartDate = sP.stratTime
            //將比較時間設置為 存儲的天氣預報資料中 最早結束時間
            saveEndDate = sP.endTime
        }

        //比較存儲預報時間是否 < 當前時間
        if( saveEndDate == null ||
            saveStartDate == null ||
            saveEndDate < presentDate ||
            ( arrayListOf(6,18).contains(saveStartDate.hours) &&
                arrayListOf(12,24).contains(presentDate.hours+1) )
        )
            //(是則表示資料過期 需重新請求預報資料)
            updatedCwbForecast()
        else
            //(否則表示資料未過期 能直接展示存儲的預報資料)
            resetCwbForecast()
    }

    /**
     * 接收新的空污預報資料 & 更新空污展示
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    fun initEpaAirQuality(){
        //使用協程調用moenv天氣預報資料(API)
        GlobalScope.launch ( Dispatchers.Main ) {
            val wF = withContext(Dispatchers.IO) {
                EpaAPI.getAirQualityForecast()
            }
            wF?.records?.let { it->
                /**用於彙整資料的列表*/
                val datas = ArrayList<MoenvAirQualityData>()

                //去除非時間段的資訊
                //10:30分的預報中 會出現4天的預報 其他則為3天
                //因此其他時段的預報 需要額外判斷 4天的預報則有31條資訊 3天(24條)
                //因此需要將較舊的預測資料刪除
                //if(!it[0].publishtime.contains(" 10:30")){
                    for (i in (1..it.lastIndex).reversed()) {
                        if( SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.TAIWAN)
                                .parse(it[0].publishtime)!!
                            >
                            SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.TAIWAN)
                                .parse(it[i].publishtime)
                        ) {
                            //Log.e(it[i].area,it[i].aqi+" "+it[i].forecastdate+" "+it[i].publishtime)
                            it.removeAt(i)
                        }
                    }
                //}

                /**根據預測時間排序的列表(欲取出最近的預測日期)*/
                val records = it.sortedBy {
                    it.forecastdate.let { _it ->
                        SimpleDateFormat("yyyy-MM-dd",Locale.TAIWAN)
                            .parse(_it)
                    }
                }
                //records.forEach { _it->
                //    Log.e(_it.area,_it.aqi+" "+_it.forecastdate+" "+_it.publishtime)
                //}
                //Log.e("---","")
                //取出前十筆(全台及外島共十區的預報)
                for (i in 0 until 10){
                    datas.add(
                        MoenvAirQualityData(
                            records[i].area,
                            records[i].aqi.toInt())
                    )
                    //Log.e(records[i].area,records[i].aqi+" "+records[i].forecastdate+" "+records[i].publishtime)
                }
                //存入資料暫存列表
                airQualityData.Moenv_airQualityData = datas
                airQualityData.upDate =
                    it[0].publishtime.let { _it ->
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.TAIWAN
                        ).parse(_it)
                    }

                //Log.e("",airQualityData.toString())

                //重置空汙預報顯示
                resetEpaView()
                initEpa = false
            }
        }
        /* epa時期的官網爬蟲
        //取用協程
        GlobalScope.launch (Dispatchers.Main) {
            /**EpaHtmlString*/
            val ehs = withContext(Dispatchers.IO) {
                EpaAPI.get()
            }
            ehs?.let {
                airQualityData = EpaHtmlParser.getContent(it)
                resetEpaView()
                initEpa = false
            }
        }
        */
    }

    /**
     * 更新空污展示視圖
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun resetEpaView(){
        val airQuality = epaFilterLocation(airQualityData)
        if(view != null && airQuality != null) {

            //發布日期：－/－/－ －:－ (vvv 避免系統警告的寫法 vvv)
            /**空污預報發布日期*/
            val upDate =
                "發布日期：${DateFormat.format("yyyy/MM/dd HH:mm", airQualityData.upDate)}"
            this.view?.findViewById<TextView>(R.id.EPA_UpDate)?.text = upDate
            //空污預報地區
            this.view?.findViewById<TextView>(R.id.EPA_Loction)?.text = airQuality.locationArea

            val epaAirQualityContext = this.view?.findViewById<TextView>(R.id.EPA_AirQuality_context)
            val epaAirQualityValue = this.view?.findViewById<TextView>(R.id.EPA_AirQuality_value)
            val epaAirQualityProgressbar = this.view?.findViewById<ProgressBar>(R.id.EPA_AirQuality_progressbar)
            val epaAirQualityIcon = this.view?.findViewById<ImageView>(R.id.EPA_AirQuality_icon)
            //設置空汙數值
            if (airQuality.airQualityValue == null) {
                //數值為空
                this.view?.findViewById<TextView>(R.id.EPA_AirQuality_value)?.text = "－"
                epaAirQualityProgressbar?.progress = 0
            } else {
                //數值不為空
                epaAirQualityValue?.text = airQuality.airQualityValue.toString()
                epaAirQualityProgressbar?.progress = airQuality.airQualityValue
            }

            //設置空汙對應指標圖示
            when (airQuality.airQualityValue) {
                in 0..50 -> {       //良好 0~50
                    epaAirQualityContext?.text = "良好"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#009865"))//良好 0~50
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_1)
                }
                in 51..100 -> {     //普通 51~100
                    epaAirQualityContext?.text = "普通"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#fffb26")
                    )//普通 51~100
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_2)
                }
                in 101..150 -> {    //對敏感族群不健康 101~150
                    epaAirQualityContext?.text = "對敏感族群不健康"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#ff9734")
                    )//對敏感族群不健康 101~150
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_3)
                }
                in 151..200 -> {    //對所有族群不健康 151~200
                    epaAirQualityContext?.text = "對所有族群不健康"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#ca0034")
                    )//對所有族群不健康 151~200
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_4)
                }
                in 201..300 -> {    //非常不健康 201~300
                    epaAirQualityContext?.text = "非常不健康"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#670099")
                    )//非常不健康 201~300
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_5)
                }
                in 301..500 -> {    //危害 301~500
                    epaAirQualityContext?.text = "危害"
                    epaAirQualityProgressbar?.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#7e0123")
                    )//危害 301~500
                    epaAirQualityIcon?.setImageResource(R.drawable.air_quality_6)
                }
                else -> epaAirQualityIcon?.setImageResource(R.drawable.question1)
            }
        }

    }

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面
        //關閉左右托拽時的Android預設動畫
        this.view?.findViewById<ViewPager2>(R.id.Cwb_value_page)?.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER

        //初始化網路接收器
        initInternetReceiver()
        //取得存儲中的目標縣市
        getTargetLocation()
        //設置關於按鈕功能
        setAboutButton()
        //設置選擇縣市列表按鈕功能
        setMoreLocation()

        //首次調用展示天氣預報資料
        initCwbForecastPageView()
        //取得空污預報資料
        initEpaAirQuality()
    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        activity?.unregisterReceiver(internetReceiver)
        initCwb = true
        initEpa = true
    }

    /**
     * Cwb_value_page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param fragments ArrayList<[Fragment]> 欲展視的片段視圖
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    inner class PageAdapter(
        fragmentManager: FragmentManager,   // 子片段管理器
        lifecycle: Lifecycle,               // 生命週期
        private val fragments: ArrayList<Fragment>
    ):  FragmentStateAdapter(   // 片段狀態適配器
        fragmentManager,        // 片段管理器
        lifecycle               // 生命週期
    ){

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
}