package com.example.pccu.Page.Bus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.R
import kotlinx.android.synthetic.main.bus_item.view.*
import kotlinx.android.synthetic.main.bus_list_page.*
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.android.synthetic.main.weather_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification.EXTRA_TITLE
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.fragment.app.FragmentFactory
import android.provider.AlarmClock.EXTRA_MESSAGE
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import android.content.Intent.getIntent
import android.content.Intent.getIntent
import com.example.pccu.Internet.*
import java.util.*

class Data(Station: List<Bus_Data_Station>, EstimateTime: Vector<Bus_Data_EstimateTime>) : Serializable {
    val Station: List<Bus_Data_Station> = Station
    val EstimateTime: Vector<Bus_Data_EstimateTime> = EstimateTime

    fun GetStation(): List<Bus_Data_Station>{
        return Station
    }
    fun GetEstimateTime(): Vector<Bus_Data_EstimateTime>{
        return EstimateTime
    }
}

class  Bus_ListPage : Fragment(R.layout.bus_list_page){

    var Direction = -1 // 0:去程 1:返程 2:迴圈 255:未知
    var Zh_tw: String? = null
    var Station: List<Bus_Data_Station>? = null
    var EstimateTime: Vector<Bus_Data_EstimateTime>? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
    }

    companion object {
        fun newInstance(Direction: Int, BusName: String, Station: List<Bus_Data_Station>, EstimateTime: Vector<Bus_Data_EstimateTime>): Bus_ListPage {
            val Bundle = Bundle()
            val NStation = Data(Station!!,EstimateTime)
            Bundle.putInt("Direction", Direction)
            Bundle.putString("BusName", BusName)
            Bundle.putSerializable("Station", NStation)
            return Bus_ListPage().apply{
                arguments = Bundle
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            Direction = args.getInt("Direction")
            Zh_tw = args.getString("BusName")
            val NStation = args.getSerializable("Station") as Data
            Station = NStation.GetStation()
            EstimateTime = NStation.GetEstimateTime()

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        bus_list.layoutManager = LinearLayoutManager( context, LinearLayoutManager.VERTICAL, false)
        val adapter = Adapter(Station!!, EstimateTime!!)
        bus_list.adapter = adapter
    }

    inner class Adapter(Station: List<Bus_Data_Station>,EstimateTime: Vector<Bus_Data_EstimateTime>) : RecyclerView.Adapter<MyViewHolder>() {
        private val Station_Data: Bus_Data_Station = Station[Direction]
        private val EstimateTime: Vector<Bus_Data_EstimateTime> = EstimateTime

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_item, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return if(Station_Data==null) 0 else Station_Data!!.Stops.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val Station = Station_Data!!.Stops[position]
            holder.itemView.StationName.text = "${Station.StopName.Zh_tw}"

            for(i in 0..EstimateTime.size-1){
                if(EstimateTime[i].StopID == Station.StopID) {
                    if(EstimateTime[i].EstimateTime>180) {
                        holder.itemView.TimeState.textSize = 20f
                        holder.itemView.EstimateTime.text = (EstimateTime[i].EstimateTime / 60).toString()
                        holder.itemView.TimeMin.text = "分"
                        holder.itemView.TimeState.text = ""
                    }else{
                        holder.itemView.TimeMin.text = ""
                        holder.itemView.EstimateTime.text = ""
                        if(EstimateTime[i].EstimateTime in 0..4 && EstimateTime[i].StopStatus == 0) {
                            holder.itemView.TimeState.textSize = 20f
                            holder.itemView.TimeState.text = "進站中"
                        }else if(EstimateTime[i].EstimateTime in 5..180 && EstimateTime[i].StopStatus == 0) {
                            holder.itemView.TimeState.textSize = 20f
                            holder.itemView.TimeState.text = "將進站"
                        }
                        else if(EstimateTime[i].EstimateTime<5 && EstimateTime[i].StopStatus!=0) when(EstimateTime[i].StopStatus){
                            1->{holder.itemView.TimeState.textSize = 20f
                                holder.itemView.TimeState.text = "未發車"}
                            2->{holder.itemView.TimeState.textSize = 19f
                                holder.itemView.TimeState.text = "交管不停"}
                            3->{holder.itemView.TimeState.textSize = 19f
                                holder.itemView.TimeState.text = "末班已過"}
                            4->{holder.itemView.TimeState.textSize = 17f
                                holder.itemView.TimeState.text = "今日未營運"}
                        }
                    }
                    break
                }
            }
        }

    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}