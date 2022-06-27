package com.example.pccu.Page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pccu.R

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.Internet.Bus_API
import com.example.pccu.Internet.Bus_Data_EstimateTime
import com.example.pccu.Internet.Bus_Data_Station
import com.example.pccu.Page.Bus.Bus_ListPage
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.bus_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

/**
 * 公車系統 主頁面 頁面建構類 : "Fragment(bus_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Bus_Page : Fragment(R.layout.bus_page) {

    /**頁面適配器*/
    var pageAdapter : PageAdapter? = null

    /**
     * bus_page頁面被關閉
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面
        //預設紅五路線
        val T_OutBound = "往陽明山"
        val T_ReturnTrip = "往劍潭捷運站"
        val Zh_tw = "紅5"

        //協程 (取得 站務資料、到站時間)
        GlobalScope.launch ( Dispatchers.Main ){
            //站務資料 連接BusAPI
            val Station = withContext(Dispatchers.IO) {
                Bus_API().GetBusStation(Zh_tw)
            }
            //到站時間 連接BusAPI
            val Data_EstimateTime = withContext(Dispatchers.IO) {
                Bus_API().GetBusEstimateTime(Zh_tw)
            }

            //估計時間
            val EstimateTime: List<Vector<Bus_Data_EstimateTime>> = mutableListOf(Vector(),Vector())
            //如果 到站時間 不是空的
            if(Data_EstimateTime!=null)
                //分類資料 去程、返程
                for (i in 0..Data_EstimateTime!!.size-1){
                    if(Data_EstimateTime[i].Direction==0)
                        EstimateTime!![0].add(Data_EstimateTime[i]) //去程 到站時間
                    else
                        EstimateTime!![1].add(Data_EstimateTime[i]) //返程 到站時間
                }
            //創建Bus頁面資料
            pageAdapter = PageAdapter(
                childFragmentManager,
                lifecycle,
                "紅5",
                Station!!,
                EstimateTime!!)
            //Bus頁面 是配器
            bus_fragment.adapter = pageAdapter

            //頁面標題配置
            val title: ArrayList<String> = arrayListOf(T_OutBound, T_ReturnTrip)
            //套用至Bus頁面的標題
            TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->
                tab.text = title[position]
            }.attach()
        }
    }

    /* 未用到的function
    fun loadBusList(T_OutBound: String, T_ReturnTrip: String, Station: List<Bus_Data_Station>, EstimateTime: List<Vector<Bus_Data_EstimateTime>>){
        pageAdapter = PageAdapter(childFragmentManager, lifecycle , "紅5", Station, EstimateTime)
        bus_fragment.adapter = pageAdapter

        val title: ArrayList<String> = arrayListOf(T_OutBound,T_ReturnTrip )
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->
            tab.text = title[position]
        }.attach()
    }*/

    /**
     * bus_page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param BusName [String] 公車名
     * @param Station List<[Bus_Data_Station]> 站點資料表
     * @param EstimateTime List<Vector<[Bus_Data_EstimateTime]>> 進站時間資料表
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    class PageAdapter(
        fragmentManager: FragmentManager, // 子片段管理器
        lifecycle: Lifecycle, // 生命週期
        BusName: String, // 公車名
        Station: List<Bus_Data_Station>, // 站點資料表
        EstimateTime: List<Vector<Bus_Data_EstimateTime>> // 進站時間資料表
    ):  FragmentStateAdapter( // 片段狀態適配器
        fragmentManager, // 片段管理器
        lifecycle // 生命週期
    ){

        /**顯示頁面控件 增加指定頁面*/
        var fragments: ArrayList<Fragment> = arrayListOf(
            Bus_ListPage.newInstance(0, BusName, Station!!, EstimateTime[0]),   //去程
            Bus_ListPage.newInstance(1, BusName, Station!!, EstimateTime[1])    //返程
        )

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