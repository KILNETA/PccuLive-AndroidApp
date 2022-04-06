package com.example.pccu.Page.Bus

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.R
import kotlinx.android.synthetic.main.bus_item.view.*
import kotlinx.android.synthetic.main.bus_list_page.*
import java.io.Serializable
import com.example.pccu.Internet.*
import java.util.*








//用於調用的資料組 (數據包) : (Serializable 可序列化)
class Data( Station: List<Bus_Data_Station>,
            EstimateTime: Vector<Bus_Data_EstimateTime>
) : Serializable {
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
    var Zh_tw: String? = null //Bus名
    var Station: List<Bus_Data_Station>? = null //站點資料
    var EstimateTime: Vector<Bus_Data_EstimateTime>? = null //到站時間

    //設置參數 (導入頁面已存儲實例狀態)
    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
    }

    companion object {
        fun newInstance( Direction: Int,
                         BusName: String,
                         Station: List<Bus_Data_Station>,
                         EstimateTime: Vector<Bus_Data_EstimateTime>
        ): Bus_ListPage {
            val Bundle = Bundle()
            val NStation = Data(Station!!,EstimateTime) //打包資料組 (以備調用) { 站點資料, 到站時間 }
            Bundle.putInt("Direction", Direction) //去回程
            Bundle.putString("BusName", BusName) //Bus名
            Bundle.putSerializable("Station", NStation) //站點資料
            return Bus_ListPage().apply{
                arguments = Bundle //回傳已建置的資料組 (數據包)
            }
        }
    }

    //關閉頁面
    override fun onDestroyView() {
        super.onDestroyView()
    }

    //建置頁面資料
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //savedInstanceState 已保存實例狀態
        val args = getArguments() //導入 已建置的資料組 (數據包)
        if (args != null) {
            Direction = args.getInt("Direction")                    //導入 去回程
            Zh_tw = args.getString("BusName")                       //導入 Bus名
            val NStation = args.getSerializable("Station") as Data //反序列化 資料組
            Station = NStation.GetStation()                             //導入 站點資料
            EstimateTime = NStation.GetEstimateTime()                   //導入 到站時間

        }
    }

    //顯示頁面
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //創建Bus列表
        bus_list.layoutManager = LinearLayoutManager( context, LinearLayoutManager.VERTICAL, false)
        //初始化 列表適配器
        val adapter = Adapter(Station!!, EstimateTime!!)
        //掛載 列表適配器
        bus_list.adapter = adapter
    }

    //Bus列表適配器
    inner class Adapter(Station: List<Bus_Data_Station>,EstimateTime: Vector<Bus_Data_EstimateTime>) : RecyclerView.Adapter<MyViewHolder>() {
        //站點資訊 資料陣列[]
        private val Station_Data: Bus_Data_Station = Station[Direction]
        //到站時間 資料陣列[]
        private val EstimateTime: Vector<Bus_Data_EstimateTime> = EstimateTime

        //創建視圖持有者 (連結bus_item顯示物件)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_item, parent, false)
            return MyViewHolder(view)
        }

        //設置展示物件數量 (站點數量)
        override fun getItemCount(): Int {
            return if(Station_Data==null) 0 else Station_Data!!.Stops.size
        }

        //物件建構者
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val Station = Station_Data!!.Stops[position]
            holder.itemView.StationName.text = "${Station.StopName.Zh_tw}"

            for(i in 0..EstimateTime.size-1){
                //找到匹配的站點資訊
                if(EstimateTime[i].StopID == Station.StopID) {

                        //如果剩餘時間 > 3min  {顯示剩餘時間}
                    if(EstimateTime[i].EstimateTime>180) {
                        holder.itemView.TimeState.textSize = 22f
                        holder.itemView.EstimateTime.text = (EstimateTime[i].EstimateTime / 60).toString()
                        holder.itemView.TimeMin.text = "分"
                        holder.itemView.TimeState.text = ""

                        //  剩餘時間 < 3min {剩餘時間欄位清空}
                    }else{
                        holder.itemView.TimeMin.text = ""
                        holder.itemView.EstimateTime.text = ""

                            //如果剩餘時間 < 3min && 正在 0:運營中 {即將進站}
                        if(EstimateTime[i].EstimateTime in 5..180 && EstimateTime[i].StopStatus == 0) {
                            holder.itemView.TimeState.setTextColor(Color.parseColor("#F5B939"))
                            holder.itemView.TimeState.textSize = 22f
                            holder.itemView.TimeState.text = "將進站"
                        }
                            //如果剩餘時間 < 5s && 正在 0:運營中 {進站中}
                        else if(EstimateTime[i].EstimateTime in 0..4 && EstimateTime[i].StopStatus == 0) {
                            holder.itemView.TimeState.setTextColor(Color.parseColor("#ff6363"))
                            holder.itemView.TimeState.textSize = 22f
                            holder.itemView.TimeState.text = "進站中"
                        }
                            //如果剩餘時間 < 5s && 不是 0:運營中 {未運營狀況}
                        else if(EstimateTime[i].EstimateTime<5 && EstimateTime[i].StopStatus!=0)
                            holder.itemView.TimeState.setTextColor(Color.parseColor("#7d7d7d"))
                            //當前交通狀況 1:未發車 2:交管不停 3:末班已過 4:今日未營運
                        when(EstimateTime[i].StopStatus){
                            1->{holder.itemView.TimeState.textSize = 22f
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