package com.example.pccu.page.cwb

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.pccu.R
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.about.AboutBottomSheet
import kotlinx.android.synthetic.main.cwb_home_page.*
import java.util.*
import com.example.pccu.internet.*
import com.example.pccu.Menu.MoreLocation_BottomMenu
import com.example.pccu.sharedFunctions.Object_SharedPreferences
import com.example.pccu.sharedFunctions.OffsetPageTransformer
import kotlinx.coroutines.*
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
    private var airQualityData : EpaAirQuality? = null

    /**
     * 設置選擇縣市列表按鈕功能
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun setMoreLocation(){
        //當 選擇縣市列表按鈕 被按下
        Cwb_moreLocation.setOnClickListener{
            /**選擇縣市列表 底部彈窗*/
            val moreLocationSheetFragment = MoreLocation_BottomMenu(view!!,this)
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
            "",
            "資料來源：",
            "　　交通部中央氣象局、行政院環境保護署"
        )

        //當 關於按鈕 被按下
        aboutButton.setOnClickListener{
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
            context!!
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
        val sP = Object_SharedPreferences["Cwb", "TargetLocation", context!!]

        //如果沒有資料 則 使用預設值(臺北市)
        if(sP != null)
            targetLocation = sP as String

        //更改目標縣市 = 存儲中的目標縣市
        Cwb_Location.text = targetLocation
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
    private fun epaFilterLocation(epaForecast : EpaAirQuality):EpaAirQualityData? {
        /**EPA地區*/
        var epaArea: String? = null
        //換算Cwb縣市 對應之 EPA地區
        CwbSource.EPA_locationArea.forEach {
            if(it.CwbLocations.contains(targetLocation))
                epaArea = it.EpaLocation
        }

        if(epaArea != null) {
            epaForecast.EPA_airQualityData.forEach {
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
        //創建CWBForecast頁面資料
        val pageAdapter = PageAdapter(childFragmentManager, lifecycle, fragments)
        //CWBForecast頁面 適配器
        Cwb_value_page.adapter = pageAdapter
        //重新調整頁面邊界與動畫
        Cwb_value_page.setPageTransformer(OffsetPageTransformer(350, -350))
        //同時加載最大頁面為3
        Cwb_value_page.offscreenPageLimit = 3
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
            //調用CWB_API取得天氣預報資料 並經過制式重構資料->( CwbForecast_Save 存儲用氣象預報資料)
            weatherForecast = withContext(Dispatchers.IO) {
                CwbAPI().refactorCwbSource(
                    CwbAPI().getWeatherForecast()!!
                )
            }
            //天氣預報資料存儲到APP中
            Object_SharedPreferences.save(
                "Cwb_Forecast",
                "All_LocationForecast",
                weatherForecast!!,
                context!!
            )
            resetCwbForecast()
        }
    }

    /**
     * 重製氣象預報資料頁面
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun resetCwbForecast(){
        //選出用戶選擇之地區天氣預報
        val forecastOfLocation = cwbFilterLocation(weatherForecast!!.CwbForecast,targetLocation)
        //重設展示頁面 顯示該地區之天氣預報
        setCwbForecastPageAdapter(upDataCwbForecast(forecastOfLocation))
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
        Object_SharedPreferences.clear("Cwb_Forecast", context!!)

        //嘗試取得存儲的天氣預報資料
        val sP = Object_SharedPreferences["Cwb_Forecast", "All_LocationForecast", context!!]
        //存儲天氣預報資料 最早結束時間 (預設null確保無資料時一定會更新天氣預報)
        var saveEndDate : Date? = null
        //當前時間
        val presentDate = Calendar.getInstance().time

        //確認取得存儲的天氣預報資料 並非為空
        if(sP!=null) {
            //將取得的Any類別套用至CwbForecast_Save資料結構中使用
            weatherForecast = sP as CwbForecastSave
            //將比較時間設置為 存儲的天氣預報資料中 最早結束時間
            saveEndDate = sP.endTime
        }

        //比較存儲預報時間是否 < 當前時間
        if( saveEndDate == null || saveEndDate < presentDate )
            //(是則表示資料過期 需重新請求預報資料)
            updatedCwbForecast()
        else
            //(否則表示資料未過期 能直接展示存儲的預報資料)
            resetCwbForecast()
    }

    /**
     * 接收新的空污預報資料 & 更新空污展示
     * @author airQuality [EpaAirQuality] 新的空污預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun updatedEpaAirQuality(airQuality : EpaAirQuality){
        airQualityData = airQuality
        resetEpaView()
    }

    /**
     * 更新空污展示視圖
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun resetEpaView(){
        if(view != null) {
            val airQuality = epaFilterLocation(airQualityData!!)!!

            //發布日期：－/－/－ －:－ (vvv 避免系統警告的寫法 vvv)
            /**空污預報發布日期*/
            val upDate =
                "發布日期：${DateFormat.format("yyyy/MM/dd HH:mm", airQualityData!!.upDate)}"
            EPA_UpDate.text = upDate
            //空污預報地區
            EPA_Loction.text = airQuality.locationArea
            //空污指標描述
            EPA_AirQuality_context.text = airQuality.airQualityName!!

            //設置空汙數值
            if (airQuality.airQualityValue == null) {
                //數值為空
                EPA_AirQuality_value.text = "－"
                EPA_AirQuality_progressbar.setProgress(0, false)
            } else {
                //數值不為空
                EPA_AirQuality_value.text = airQuality.airQualityValue.toString()
                EPA_AirQuality_progressbar.setProgress(airQuality.airQualityValue, false)
            }

            //設置空汙對應指標圖示
            when (airQuality.airQualityValue) {
                in 0..50 -> {       //良好 0~50
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#009865"))//良好 0~50
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_1)
                }
                in 51..100 -> {     //普通 51~100
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#fffb26")
                    )//普通 51~100
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_2)
                }
                in 101..200 -> {    //對敏感族群不健康 101~150
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#ff9734")
                    )//對敏感族群不健康 101~150
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_3)
                }
                in 201..300 -> {    //對所有族群不健康 151~200
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#ca0034")
                    )//對所有族群不健康 151~200
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_4)
                }
                in 301..400 -> {    //非常不健康 201~300
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#670099")
                    )//非常不健康 201~300
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_5)
                }
                in 401..500 -> {    //危害 301~500
                    EPA_AirQuality_progressbar.progressTintList =
                        ColorStateList.valueOf(Color.parseColor("#7e0123")
                    )//危害 301~500
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_6)
                }
                else -> EPA_AirQuality_icon.setImageResource(R.drawable.question1)
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
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //取得存儲中的目標縣市
        getTargetLocation()
        //設置關於按鈕功能
        setAboutButton()
        //設置選擇縣市列表按鈕功能
        setMoreLocation()
        //首次調用展示天氣預報資料
        initCwbForecastPageView()
        //取得空污預報資料
        EpaAPI(this).get()
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