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

    fun gethun(){

    }

    var pageAdapter : PageAdapter? = null

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面
        val T_OutBound = "往陽明山"
        val T_ReturnTrip = "往劍潭捷運站"
        val Zh_tw = "紅5"

        GlobalScope.launch ( Dispatchers.Main ){
            val Station = withContext(Dispatchers.IO) {
                Bus_API().GetBusStation(Zh_tw)
            }
            val Data_EstimateTime = withContext(Dispatchers.IO) {
                Bus_API().GetBusEstimateTime(Zh_tw)
            }

            val EstimateTime: List<Vector<Bus_Data_EstimateTime>> = mutableListOf(Vector(),Vector())
            if(Data_EstimateTime!=null)
                for (i in 0..Data_EstimateTime!!.size-1){
                    if(Data_EstimateTime[i].Direction==0)
                        EstimateTime!![0].add(Data_EstimateTime[i])
                    else
                        EstimateTime!![1].add(Data_EstimateTime[i])
                }
            pageAdapter =PageAdapter(childFragmentManager, lifecycle, "紅5", Station!!, EstimateTime!!)
            bus_fragment.adapter = pageAdapter

            val title: ArrayList<String> = arrayListOf(T_OutBound, T_ReturnTrip)
            TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->
                tab.text = title[position]
            }.attach()
        }
    }

    fun loadBusList(T_OutBound: String, T_ReturnTrip: String, Station: List<Bus_Data_Station>, EstimateTime: List<Vector<Bus_Data_EstimateTime>>){
        pageAdapter = PageAdapter(childFragmentManager, lifecycle , "紅5", Station, EstimateTime)
        bus_fragment.adapter = pageAdapter

        val title: ArrayList<String> = arrayListOf(T_OutBound,T_ReturnTrip )
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->
            tab.text = title[position]
        }.attach()
    }

    class PageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, BusName: String, Station: List<Bus_Data_Station>, EstimateTime: List<Vector<Bus_Data_EstimateTime>>) :
        FragmentStateAdapter(fragmentManager, lifecycle ) {

        var fragments: ArrayList<Fragment> = arrayListOf(
            Bus_ListPage.newInstance(0, BusName, Station!!, EstimateTime[0]),
            Bus_ListPage.newInstance(1, BusName, Station!!, EstimateTime[1])
        )

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}