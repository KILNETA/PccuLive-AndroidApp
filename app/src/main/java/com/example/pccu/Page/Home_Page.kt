package com.example.pccu.Page

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_page.*

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pccu.Internet.*
import com.example.pccu.Page.Bus.Bus_ListPage.MyViewHolder

import kotlinx.android.synthetic.main.weather_item.view.*

import com.example.pccu.R
import kotlinx.coroutines.*
import java.util.*

/**
 * 程式主頁面 : "Fragment(home_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Home_Page : Fragment(R.layout.home_page){

    private var CountdownTimer:Timer? = null

    /**
     * home_page頁面被關閉
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onDestroyView() {
        CountdownTimer!!.cancel()
        CountdownTimer!!.purge()
        super.onDestroyView()
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
        super.onViewCreated(view, savedInstanceState) //創建頁面


        //中央氣象局 雷達迴波圖 測試用
        val mIv = view.findViewById<ImageView>(R.id.CwbRadarEcho);
        GlobalScope.launch {
            val msg =
                HttpRetrofit.create_Image("https://www.cwb.gov.tw/Data/radar/CV1_TW_3600.png")!!
            withContext(Dispatchers.Main) {
                mIv.setImageBitmap(msg)
            }
        }


        //氣溫列表呼叫
        weather_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        //初始化 氣溫適配器
        val adapter = adapter()
        //氣溫列表 連接適配器
        weather_list.adapter = adapter
        //首次更新數據
        adapter.upDatas()

        //初始化計時器
        CountdownTimer = Timer()
        //氣溫計數值
        var i=0

        //更新氣溫用計時器 60s 更新一次
        class CountdownTimerTask : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun run() {
                WeatherUpdataProgressBar.setProgress(i,false)
                i+=1
                if(i==59){ adapter.upDatas() }
                else if( i==60 ){ i=0 }
            }
        }
        //套用計時器設定
        CountdownTimer!!.schedule(CountdownTimerTask(), 500, 500)
    }

    /**
     * Weather列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class adapter:RecyclerView.Adapter<MyViewHolder>(){
        /**氣溫API數據組 [Weather_Data]*/
        private val Weather_Data = mutableListOf<Weather_Data>()
        //計算滑動定位 座標變數
        /** 水平座標 */
        private var Dx = 0
        /** 當前項目座標*/
        private var NowPosition = 0

        /**
         * 更新資料
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        fun upDatas(){
            //協程調用氣溫API
            GlobalScope.launch ( Dispatchers.Main ){
                val Weather = withContext(Dispatchers.IO) {
                    Weather_API().Get()
                }
                //呼叫 重置氣溫資料
                reSetData(Weather!!)
            }
        }

        /**
         * 重製列表並導入新數據
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        fun reSetData(Weather_Data: List<Weather_Data>){
            Log.d("upDatas", Weather_Data.toString())
            //導入數據資料
            this.Weather_Data.addAll(Weather_Data)
            //刷新視圖列表
            notifyDataSetChanged()
        }

        /**
         * 重構 創建視圖持有者 (連結weather_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false)
            return MyViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (天氣數值量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            return if(Weather_Data.size == 0) 1 else Weather_Data.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            if(Weather_Data != mutableListOf<Weather_Data>()){

                val Weather = Weather_Data[position] //資料組( 大義館7F, 台北Cwb, 竹子湖Cwb )
                holder.itemView.weatherDesciption.text =  if(Weather.WeatherDesciption=="-99") "null"   //天氣描述
                                                        else Weather.WeatherDesciption                  //
                holder.itemView.temperature.text = "${Weather.Tempature} 度"                            //溫度
                holder.itemView.humidity.text = "${Weather.Humidity} %"                                 //濕度
                holder.itemView.windSpeed.text = "${Weather.WindSpeed} m/s"                             //風速
                holder.itemView.rainFall.text = "${Weather.RainFall} mm"                                //雨量
                holder.itemView.location.text = "${Weather.Location}"                                   //資料來源
                if(Weather.WeatherDesciption.contains("晴", ignoreCase = true))                    //天氣狀態圖
                    holder.itemView.weather_image.setImageResource(R.drawable.sunny)                    //
                else if(Weather.WeatherDesciption.contains("雨", ignoreCase = true))               //
                    holder.itemView.weather_image.setImageResource(R.drawable.cloudy_rain)              //
            }


            val dm = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(dm)
            val vWidth = dm.widthPixels
            val vHeight = dm.heightPixels
            Log.d("WindowSize", "寬:${vWidth} 高:$vHeight") //後台報錯

            //if(vWidth>1080)

            val layoutParams: ViewGroup.LayoutParams = holder.itemView.getLayoutParams()
            layoutParams.width = vWidth.toInt()


            weather_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                //dx是水平滚动的距离，dy是垂直滚动距离，向上滚动的时候为正，向下滚动的时候为负
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    //super.onScrolled(recyclerView, dx, dy)
                    // 滑動率要超過50才會移動到下一個
                    if (dx < -50 || dx > 50) Dx = dx
                }

                /**
                 * 重構 偵測滾動狀態的改變
                 * @param recyclerView [RecyclerView] 回收視圖
                 * @param newState [Int] 新狀態
                 *
                 * @author KILNETA
                 * @since Alpha_1.0
                 */
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    /** newState: Int
                        value(0) SCROLL_STATE_IDLE -當前未滾動。
                        value(1) SCROLL_STATE_DRAGGING -正被外部控制（如使用者觸摸）拖動。
                        value(2) SCROLL_STATE_SETTLING -當前正在滑動中，且不受外部控制。
                     **/
                    //super.onScrollStateChanged(recyclerView, newState)
                    // 如果有滑動率+任何滾動中+未超過項目總數 -跳轉到下一個項目
                    if (Dx < 0 && newState!=0 && NowPosition > 0)
                        weather_list.smoothScrollToPosition( --NowPosition )
                    if (Dx > 0 && newState!=0 && NowPosition < holder.getAdapterPosition())
                        weather_list.smoothScrollToPosition( ++NowPosition )
                    // 當前未滾動 //滑動率過小+非被使用者拖動 -回到當前的項目
                    if (Dx == 0 && newState!=1 || newState==0) {weather_list.smoothScrollToPosition( NowPosition )}
                    // 滑動率重置
                    Dx=0
                }
            })
        }
    }

    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){}
}
