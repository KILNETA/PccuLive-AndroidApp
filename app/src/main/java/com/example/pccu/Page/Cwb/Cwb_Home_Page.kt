package com.example.pccu.Page.Cwb

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.pccu.R

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.About.About_BottomSheet
import kotlinx.android.synthetic.main.cwb_home_page.*
import java.util.*
import com.example.pccu.Internet.*
import com.example.pccu.Menu.MoreLocation_BottomMenu
import com.example.pccu.Shared_Functions.Object_SharedPreferences
import com.example.pccu.Shared_Functions.OffsetPageTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList


/**
 * CWB氣象資料 主頁面 頁面建構類 : "Fragment(cwb_home_page)"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class Cwb_Home_Page : Fragment(R.layout.cwb_home_page) {

    //欲展示天氣預報資料之 縣市(預設為臺北市 同時適用"未自訂縣市")
    var TargetLocation = "臺北市"
    //暫存氣象預報資料(全縣市) 減少反覆讀取時間
    var WeatherForecast : CwbForecast_Save? = null
    //
    var airQualityData : EPA_airQuality? = null

    /**
     * 設置選擇縣市列表按鈕功能
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun setMoreLocation(){
        Cwb_moreLocation.setOnClickListener{
            val FastLinkSheetFragment = MoreLocation_BottomMenu(view!!,this)
            FastLinkSheetFragment.show(parentFragmentManager, FastLinkSheetFragment.tag)
        }
    }

    /**
     * 尋找相對應縣市的預報資料
     * @param cwbForecast ArrayList<[CwbForecast]> 氣象預報資料(全縣市)
     * @param findLocation [String] 目標縣市
     * @return 目標預報資料(該縣市) : ArrayList<[TimePeriodForecast]>
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun Cwb_filterLocation(cwbForecast : ArrayList<CwbForecast>, findLocation : String ) : ArrayList<TimePeriodForecast>? {
        for( i in cwbForecast.indices){
            if(cwbForecast[i].Location == findLocation)
                //找到回傳該縣市氣象預報資料
                return cwbForecast[i].timePeriod
        }
        //沒找到回傳 null
        return null
    }

    fun Epa_filterLocation(epaForecast : EPA_airQuality, findLocation : String ) : EPA_airQualityData? {
        var index = -1

        for( i in CwbSource.EPA_locations.indices){
            if(TargetLocation in CwbSource.EPA_locations[i])
                index = i
        }

        if(index != -1)
            for( i in epaForecast.EPA_airQualityData.indices){
                if(epaForecast.EPA_airQualityData[i].locationArea == CwbSource.EPA_locationArea[index])
                //找到回傳該縣市氣象預報資料
                    return epaForecast.EPA_airQualityData[i]
            }
        //沒找到回傳 null
        return null
    }

    /**
     * 創建預報展示頁面
     * @param weatherForecast ArrayList<[TimePeriodForecast]> 欲展示氣象預報資料(該縣市)
     * @return 預報展示頁面 : ArrayList<[Fragment]>
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun upData_CWBForecast( weatherForecast : ArrayList<TimePeriodForecast>? ) : ArrayList<Fragment> {
        val fragments = arrayListOf<Fragment>()

        //如果沒有預報資料 -> 顯示 NoDataSource
        if(weatherForecast==null) {
            fragments.add(Cwb_Weather_Fragment(null))
        }
        //有預報資料 -> 顯示 預報資料
        else {
            for (i in weatherForecast.indices) {
                fragments.add(Cwb_Weather_Fragment(weatherForecast[i]))
            }
        }
        //回傳已創建的預報資料頁面
        return fragments
    }

    /**
     * 設置 CWBForecast頁面 適配器 to (Cwb_value_page)
     * @param fragments ArrayList<[Fragment]> 欲展示氣象預報資料頁面
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun setCWBForecastPageAdapter(fragments : ArrayList<Fragment>){
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
    fun updataForCWBForecast(){
        //使用協程調用CWB_API天氣預報資料
        GlobalScope.launch {
            //調用CWB_API取得天氣預報資料 並經過制式重構資料->( CwbForecast_Save 存儲用氣象預報資料)
            WeatherForecast = CWB_API().refactorCwbSource(
                CWB_API().GetWeatherForecast()!!
            )
            //天氣預報資料存儲到APP中
            Object_SharedPreferences.save("Cwb_Forecast","All_LocationForecast",WeatherForecast!!,context!!)
            //返回主線程
            withContext(Dispatchers.Main) {
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
        //選出用戶選擇之地區天氣預報
        val forecastOfLocation = Cwb_filterLocation(WeatherForecast!!.CwbForecast,TargetLocation)
        //重設展示頁面 顯示該地區之天氣預報
        setCWBForecastPageAdapter(upData_CWBForecast(forecastOfLocation))
    }

    /**
     * 首次調用展示天氣預報資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun firstSetCwbForecast_PageView(){
        val fragments = arrayListOf<Fragment>()
        fragments.add(Cwb_Weather_Fragment(null))
        setCWBForecastPageAdapter(fragments)

        //測試用-----VVV-----
        Object_SharedPreferences.clear("Cwb_Forecast", context!!)

        //嘗試取得存儲的天氣預報資料
        val WF = Object_SharedPreferences.get("Cwb_Forecast","All_LocationForecast", context!!)
        //存儲天氣預報資料 最早結束時間 (預設為最小確保無資料時一定會更新天氣預報)
        var saveEndDate = Date(0,0,0,0,0,0)
        //當前時間
        val Datum = Calendar.getInstance().time

        //確認取得存儲的天氣預報資料 並非為空
        if(WF!=null) {
            //將取得的Any類別套用至CwbForecast_Save資料結構中使用
            WeatherForecast = WF as CwbForecast_Save
            //將比較時間設置為 存儲的天氣預報資料中 最早結束時間
            saveEndDate = WF.endTime
        }

        //比較存儲預報時間是否 < 當前時間
        if( saveEndDate < Datum )//(是則表示資料過期 需重新請求預報資料)
            updataForCWBForecast()
        else                     //(否則表示資料未過期 能直接展示存儲的預報資料)
            resetCwbForecast()
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun setEPA_airQuality(airQuality : EPA_airQuality){
        airQualityData = airQuality
        resetEPA_View()
    }



    @RequiresApi(Build.VERSION_CODES.N)
    fun resetEPA_View(){
        if(view != null) {
            val airQuality = Epa_filterLocation(airQualityData!!, TargetLocation)!!
            // 發布日期：－/－/－ －:－
            EPA_UpDate.text = "發布日期：" +
                    DateFormat.format("yyyy/MM/dd HH:mm", airQualityData!!.upDate)
                        .toString()

            EPA_Loction.text = airQuality.locationArea

            EPA_AirQuality_context.text = airQuality.airQualityName!!

            if (airQuality.airQualityValue == null) {
                EPA_AirQuality_value.text = "－"
                EPA_AirQuality_progressbar.setProgress(0, false)
            } else {
                EPA_AirQuality_value.text = airQuality.airQualityValue.toString()
                EPA_AirQuality_progressbar.setProgress(airQuality.airQualityValue, false)
            }

            when (airQuality.airQualityValue) {
                in 0..50 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#009865"))
                    )//良好 0~50
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_1)
                }
                in 51..100 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#fffb26"))
                    )//普通 51~100
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_2)
                }
                in 101..200 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#ff9734"))
                    )//對敏感族群不健康 101~150
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_3)
                }
                in 201..300 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#ca0034"))
                    )//對所有族群不健康 151~200
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_4)
                }
                in 301..400 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#670099"))
                    )//非常不健康 201~300
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_5)
                }
                in 401..500 -> {
                    EPA_AirQuality_progressbar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#7e0123"))
                    )//危害 301~500
                    EPA_AirQuality_icon.setImageResource(R.drawable.air_quality_6)
                }
                else -> EPA_AirQuality_icon.setImageResource(R.drawable.question1)
            }
        }

    }

    /**
     * 取得存儲中用戶選擇 顯示縣市
     * @param newLocation [String] 新縣市
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun getTargetLocation(){
        var newLocation : String? = null
        val SP = Object_SharedPreferences.get(
            "Cwb",
            "TargetLocation",
            context!!)

        if(SP != null)
            newLocation = SP as String

        //確保存儲中的縣市資料沒被竄改
        for(element in CwbSource.CWB_locations)
            if(newLocation != null && newLocation in element)
                TargetLocation = newLocation

        //更改目標縣市 = 存儲中的目標縣市
        Cwb_Location.text = TargetLocation
    }

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /**
         * 設置關於按鈕功能
         * @author KILNETA
         * @since Alpha_4.0
         */
        fun setAboutButton(){
            val context = arrayOf(
                "提醒：",
                "　　氣象資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
                "",
                "資料來源：",
                "　　交通部中央氣象局、行政院環境保護署"
            )

            val Button = view.findViewById<Button>(R.id.aboutButton)
            Button.setOnClickListener{
                val FastLinkSheetFragment = About_BottomSheet(context)
                FastLinkSheetFragment.show(parentFragmentManager, FastLinkSheetFragment.tag)
            }
        }

        super.onViewCreated(view, savedInstanceState) //創建頁面

        //設置關於按鈕功能
        setAboutButton()
        //取得存儲中的目標縣市
        getTargetLocation()
        //設置選擇縣市列表按鈕功能
        setMoreLocation()
        //首次調用展示天氣預報資料
        firstSetCwbForecast_PageView()

        EPA_API(this).Get()

        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#009865")))//良好 0~50
        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#fffb26")))//普通 51~100
        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#ff9734")))//對敏感族群不健康 101~150
        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#ca0034")))//對所有族群不健康 151~200
        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#670099")))//非常不健康 201~300
        EPA_AirQuality_progressbar.setProgressTintList(
            ColorStateList.valueOf(Color.parseColor("#7e0123")))//危害 301~500
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
    class PageAdapter(
        fragmentManager: FragmentManager, // 子片段管理器
        lifecycle: Lifecycle, // 生命週期
        val fragments: ArrayList<Fragment>
    ):  FragmentStateAdapter( // 片段狀態適配器
        fragmentManager, // 片段管理器
        lifecycle // 生命週期
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