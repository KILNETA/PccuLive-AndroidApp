package com.example.pccu.Page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pccu.R

import android.util.Log

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


class Bus_Page : Fragment(R.layout.bus_page) {

    //頁面適配器
    var pageAdapter : PageAdapter? = null

    //頁面被關閉時
    override fun onDestroyView() {
        super.onDestroyView()
    }

    //當頁面第一次創建
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面
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
            pageAdapter =PageAdapter(childFragmentManager, lifecycle, "紅5", Station!!, EstimateTime!!)
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

    //頁面控件 class
    class PageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, BusName: String, Station: List<Bus_Data_Station>, EstimateTime: List<Vector<Bus_Data_EstimateTime>>) :
        //頁面控件 頁面適配器
        FragmentStateAdapter(fragmentManager, lifecycle ) {

        //顯示頁面控件 增加指定頁面
        var fragments: ArrayList<Fragment> = arrayListOf(
            Bus_ListPage.newInstance(0, BusName, Station!!, EstimateTime[0]),   //去程
            Bus_ListPage.newInstance(1, BusName, Station!!, EstimateTime[1])    //返程
        )

        //獲取頁面數量
        override fun getItemCount(): Int {
            return fragments.size
        }

        //創建頁面
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}