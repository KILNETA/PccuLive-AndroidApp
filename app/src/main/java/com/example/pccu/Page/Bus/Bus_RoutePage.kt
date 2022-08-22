package com.example.pccu.Page.Bus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.Internet.*
import com.example.pccu.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.bus_route_page.*
import kotlinx.coroutines.*
import kotlin.collections.ArrayList
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * Cwb主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class Bus_RoutePage : AppCompatActivity(R.layout.bus_route_page) {

    var RouteData : BusRoute? = null

    /**頁面適配器*/
    var pageAdapter : PageAdapter? = null

    fun createFragment(goalDirection:Int? , goalStation:String?) : ArrayList<Fragment>{
        if(RouteData!!.HasSubRoutes && RouteData!!.SubRoutes.any{it.Direction==1}) {
            return arrayListOf(
                Bus_RouteFragment(0, RouteData!!,
                if(goalDirection==0) goalStation else null),
                Bus_RouteFragment(1, RouteData!!,
                if(goalDirection==1) goalStation else null)
            )
        }
        else {
            return arrayListOf(
                Bus_RouteFragment(0, RouteData!!,
                if(goalDirection==0) goalStation else null)
            )
        }

    }

    fun setBackButton(){
        backButton.setOnClickListener {
            finish()
        }
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

        //設置返回按鈕
        setBackButton()

        val bundle = this.intent.extras!! //拿包裹
        RouteData = bundle.getSerializable("RouteData") as BusRoute  //拆包裹
        val goalStation =  bundle.getString("StationUID")
        val goalDirection =  bundle.getInt("Direction")

        if(!RouteData!!.HasSubRoutes && RouteData!!.SubRoutes[0].Headsign!=null)
            routeName.text = "${RouteData!!.RouteName.Zh_tw} ${RouteData!!.SubRoutes[0].Headsign}"
        else
            routeName.text = RouteData!!.RouteName.Zh_tw

        //創建Bus頁面資料
        pageAdapter = PageAdapter(
            getSupportFragmentManager(),
            lifecycle,
            createFragment( goalDirection, goalStation )
        )

        //Bus頁面 是配器
        bus_fragment.adapter = pageAdapter
        bus_fragment.offscreenPageLimit = 2

        bus_fragment.setCurrentItem(goalDirection)

        //頁面標題配置
        val title: ArrayList<String> =
            arrayListOf("往${RouteData!!.DestinationStopNameZh}", "往${RouteData!!.DepartureStopNameZh}")
        //套用至Bus頁面的標題
        bus_tabs.scrollBarSize
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->

            val textView = TextView(this)
            val selectedSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 16f, resources.displayMetrics)

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
            textView.setTextColor(resources.getColor(R.color.white))
            textView.text = tab.text
            textView.gravity = Gravity.CENTER
            tab.customView = textView
            textView.text = title[position]

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f)
            textView.setLayoutParams(params)
            tab.view.setLayoutParams(params)
        }.attach()
    }

    /**
     * bus_page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param fragments ArrayList<[Fragment]> 欲展視的片段視圖
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class PageAdapter(
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

        //創建頁面
        /**創建頁面
         * @param position [Int] 頁面數量
         * @return 頁面 : [Fragment]
         */
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}
/*
//協程 (取得 站務資料、到站時間)
GlobalScope.launch ( Dispatchers.Main ){

    //TDX test
    val test = withContext(Dispatchers.IO) {
        Bus_API.Get_token()
    }

    //TDX test
    val busRoute = withContext(Dispatchers.IO) {
        Bus_API.Get(
            test!!,
            "Route",
            BUS_ApiRequest(
                RouteData!!.City,
                routeName)
        )
    }
    /*******************/
    fun <T> fromJson(json: String, type: Type): T {
        Log.e("TDX test", json)
        return Gson().fromJson(json, type)
    }
    //Log.e("TDX test", busRoute.string())
    val json = fromJson<Array<BusRoute>>(
        busRoute.string(),
        object : TypeToken<Array<BusRoute>>() {}.type
    )
    Log.e("TDX test", json[0].toString())
    /*******************/

    //站務資料 連接BusAPI
    val Station = withContext(Dispatchers.IO) {
        fromJson<List<Bus_Data_Station>>(
            Bus_API.Get(
                test!!,
                "StopOfRoute",
                BUS_ApiRequest(
                    RouteData!!.City,
                    routeName,
                    "RouteName/Zh_tw eq '${routeName}'"
                )
            ).string(),
            object : TypeToken<List<Bus_Data_Station>>() {}.type
        )
    }
    //到站時間 連接BusAPI
    val Data_EstimateTime = withContext(Dispatchers.IO) {
        fromJson<Array<Bus_Data_EstimateTime>>(
            Bus_API.Get(
                test!!,
                "EstimatedTimeOfArrival",
                BUS_ApiRequest(
                    RouteData!!.City,
                    routeName,
                    "RouteName/Zh_tw eq '${routeName}'"
                )
            ).string(),
            object : TypeToken<Array<Bus_Data_EstimateTime>>() {}.type
        )
    }

    //估計時間
    val EstimateTime: List<Vector<Bus_Data_EstimateTime>> = mutableListOf(Vector(),Vector())
    //如果 到站時間 不是空的
    for (i in 0..Data_EstimateTime.size-1){
        if(Data_EstimateTime[i].Direction==0)
            EstimateTime[0].add(Data_EstimateTime[i]) //去程 到站時間
        else
            EstimateTime[1].add(Data_EstimateTime[i]) //返程 到站時間
    }
}
*/