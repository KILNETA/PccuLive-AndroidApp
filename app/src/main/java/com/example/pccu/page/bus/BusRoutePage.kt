package com.example.pccu.page.bus

import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.internet.*
import com.example.pccu.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.bus_route_page.*
import kotlinx.coroutines.*
import kotlin.collections.ArrayList
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.pccu.about.AboutBottomSheet
import kotlinx.android.synthetic.main.bus_route_page.aboutButton
import kotlinx.android.synthetic.main.bus_route_page.noNetWork

/**
 * Cwb主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusRoutePage : AppCompatActivity(R.layout.bus_route_page) {

    /**路線資料*/
    private var routeData : BusRoute? = null
    /**頁面適配器*/
    private var pageAdapter : PageAdapter? = null
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null
    /**路線站牌頁面*/
    private var busRouteFragment = arrayListOf<BusRouteFragment>()

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    busRouteFragment.forEach {
                        it.timerI = 18
                    }
                }
            },
            baseContext
        )
        val itFilter = IntentFilter()
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 設置關於按鈕功能
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun initAboutButton(){
        /**關於介面 內文*/
        val content = arrayOf(
            "提醒：",
            "　　公車資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
            "",
            "聲明：",
            "　　本程式之公車系統僅是提供便捷查詢，無法保證公車班次的展示之正確性，若認為是重要資訊，" +
                    "請務必校驗官方，以確保內容正確，若造成損失本程式一概不負責。",
            "",
            "資料來源：",
            "　　TDX運輸資料流通服務"
        )

        //當 關於按鈕 被按下
        aboutButton.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = AboutBottomSheet(content)
            aboutSheetFragment.show(supportFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**
     * Cwb主框架建構類 : "AppCompatActivity"
     * @param goalDirection [Int] 目標去返程方向 (定位用)
     * @param goalStation [String] 目標站牌 (定位用)
     * @return 欲展示頁面 : ArrayList<[BusRouteFragment]>
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun createFragment(goalDirection:Int?, goalStation:String?) : ArrayList<BusRouteFragment>{
        busRouteFragment = if(routeData!!.HasSubRoutes && routeData!!.SubRoutes.any{it.Direction==1}) {
            //擁有其他子路線 且 存在回程路線 (創建雙頁面 去返程)
            arrayListOf(
                BusRouteFragment(0, routeData!!,
                    if(goalDirection==0) goalStation else null),
                BusRouteFragment(1, routeData!!,
                    if(goalDirection==1) goalStation else null)
            )
        } else {
            //其他 (創建單頁面)
            arrayListOf(
                BusRouteFragment(0, routeData!!,
                    if(goalDirection==0) goalStation else null)
            )
        }
        return busRouteFragment
    }

    /**
     * 設置返回按鈕
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initBackButton(){
        backButton.setOnClickListener {
            //關閉視窗
            finish()
        }
    }

    /**
     * 公車路線展示框架建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        //建構主框架
        super.onCreate(savedInstanceState)
        //初始化網路接收器
        initInternetReceiver()
        //初始化關於按鈕
        initAboutButton()
        //設置返回按鈕
        initBackButton()

        /**拿包裹*/
        val bundle = this.intent.extras!!
        //取得包裹中 RouteData 路線資訊
        routeData = bundle.getSerializable("RouteData") as BusRoute
        /**目標去返程方向 (定位用)*/
        val goalStation =  bundle.getString("StationUID")
        /**目標站牌 (定位用)*/
        val goalDirection =  bundle.getInt("Direction")

        //設置路線名
        if(!routeData!!.HasSubRoutes && routeData!!.SubRoutes[0].Headsign!=null) {
            // 沒有附屬路線 && 有車頭描述
            /**控件展示之路線名*/ //此寫法不會出現警告 vvv
            val text = "${routeData!!.RouteName.Zh_tw} ${routeData!!.SubRoutes[0].Headsign}"
            routeName.text = text
        }
        else
            routeName.text = routeData!!.RouteName.Zh_tw

        //創建Bus頁面資料
        pageAdapter = PageAdapter(
            supportFragmentManager,
            lifecycle,
            createFragment( goalDirection, goalStation )
        )

        //Bus頁面 是配器
        bus_fragment.adapter = pageAdapter
        //最大同時顯示兩頁
        bus_fragment.offscreenPageLimit = 2
        //翻至指定頁面
        bus_fragment.currentItem = goalDirection

        //頁面標題配置
        val title: ArrayList<String> =
            arrayListOf("往${routeData!!.DestinationStopNameZh}", "往${routeData!!.DepartureStopNameZh}")
        //套用至Bus頁面的標題
        bus_tabs.scrollBarSize
        //設置路線站牌頁面控件 綁定 Tab控件
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->

            /**Tab文字控件*/
            val textView = TextView(this)
            /**控件字體大小*/
            val selectedSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 16f, resources.displayMetrics)
            //設置字體大小
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
            //設置文字顏色
            textView.setTextColor(Color.parseColor("#FFFFFF"))
            //設置文字布局 (置中)
            textView.gravity = Gravity.CENTER
            //設置文字內容
            textView.text = title[position]
            //將Tab文字控件 放入Tab控件中
            tab.customView = textView

            /**布局格式*/
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f)
            textView.layoutParams = params
            tab.view.layoutParams = params
        }.attach()
    }

    /**
     * 當頁面刪除時(刪除)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * bus_page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param fragments ArrayList<[BusRouteFragment]> 欲展視的片段視圖
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class PageAdapter(
        fragmentManager: FragmentManager,   // 子片段管理器
        lifecycle: Lifecycle,               // 生命週期
        private val fragments: ArrayList<BusRouteFragment>

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