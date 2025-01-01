package com.pccu.pccu.page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.pccu.pccu.R
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.pccu.pccu.internet.*
import com.pccu.pccu.page.bus.BusCollectFragment
import com.pccu.pccu.page.bus.dialogs.*
import com.pccu.pccu.page.bus.search.BusSearchActivity
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.PToast
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.collections.ArrayList

/**
 * 公車系統 主頁面 頁面建構類 : "Fragment(bus_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class BusPage : Fragment(R.layout.bus_page){

    /**頁面適配器*/
    private var pageAdapter : PageAdapter? = null
    /**最後瀏覽的頁面*/
    private var lastPageNum = 0
    /**收藏群組 站牌顯示頁面表*/
    private var collectList : ArrayList<CollectGroup> = arrayListOf()
    /**站牌收藏頁面*/
    @OptIn(DelicateCoroutinesApi::class)
    private val collectFragment = arrayListOf<BusCollectFragment>()

    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    private val itFilter = IntentFilter()
    init {
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
    }

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initInternetReceiver(){
        val noNetWork = this.view?.findViewById<TextView>(R.id.noNetWork)
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    noNetWork?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    noNetWork?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    collectFragment.forEach {
                        it.timerI = 18
                    }
                }
            },
            requireContext()
        )
    }

    /**
     * 編輯群組
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun editGroup(){
        val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
        /**編輯群組介面 (Dialog)*/
        val editGroup = BusEditGroupDialog (
            object : PToast.Listener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    busFragment?.let {
                        if (respond!!) {
                            //設置最後瀏覽的頁面位置 (最終瀏覽頁面的名稱)
                            resetCollectPage(collectList[busFragment.currentItem].GroupName)
                        } else if (!respond) {
                            //設置最後瀏覽的頁面位置
                            lastPageNum = busFragment.currentItem
                            resetCollectPage()
                        }
                    }
                }
            }
        )
        /**傳入介面的資料包*/
        val args = Bundle()
        //傳入收藏列表
        args.putSerializable("CollectList", collectList)
        //將資料包傳入介面
        editGroup.arguments = args
        //顯示編輯群組介面
        editGroup.show(childFragmentManager,"editGroup")
    }

    /**
     * 刪除群組
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun removeGroup() {
        /**刪除群組介面 (Dialog)*/
        val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
        val removeGroup = BusRemoveGroupDialog(
            object : PToast.Listener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    if(respond!!) {
                        busFragment?.let {
                            //設置最後瀏覽的頁面位置 (最終瀏覽頁面的名稱)
                            resetCollectPage(collectList[busFragment.currentItem].GroupName)
                        }
            }   }   }
        )
        //顯示刪除群組介面
        removeGroup.show(childFragmentManager,"removeGroup")
    }

    /**
     * 排序站牌
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun sequenceStation() {
        /**排序站牌介面 (Dialog)*/
        val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
        busFragment?.let {
            val sequenceStation = BusSequenceStationDialog(
                collectList[busFragment.currentItem].GroupName,
                object : PToast.Listener {
                    //回應是否需要重置站牌頁面+列表
                    override fun respond(respond: Boolean?) {
                        if (respond!!) {
                            //設置最後瀏覽的頁面位置
                            lastPageNum = busFragment.currentItem
                            resetCollectPage()
                        }
                    }
                }
            )
            //顯示編輯群組介面
            sequenceStation.show(childFragmentManager, "sequenceStation")
        }
    }

    /**
     * 刪除站牌
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun removeStation(){
        /**刪除站牌介面 (Dialog)*/
        val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
        busFragment?.let {
            val removeStation = BusRemoveStationDialog(
                collectList[busFragment.currentItem].GroupName,
                object : PToast.Listener {
                    //回應是否需要重置站牌頁面+列表
                    override fun respond(respond: Boolean?) {
                        if(respond!!){
                            //設置最後瀏覽的頁面位置
                            lastPageNum = busFragment.currentItem
                            resetCollectPage()
                }   }   }
            )
            //顯示編輯群組介面
            removeStation.show(childFragmentManager, "removeStation")
        }
    }

    /**
     * 關於介面 底部彈窗
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun aboutSheetFragment(){
        /**關於介面 內文*/
        val content = arrayOf(
            "提醒：",
            "　　公車資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
            "",
            "聲明：",
            "　　本程式之公車系統僅是提供便捷查詢，無法保證公車班次的展示之正確性，若認為是重要資訊，" +
                    "請務必與運營方資料校驗，以確保內容正確，若造成損失本程式一概不負責。",
            "",
            "資料來源：",
            "　　TDX運輸資料流通服務"
        )
        /**關於介面 底部彈窗*/
        val aboutSheetFragment = com.pccu.pccu.about.AboutBottomSheet(content)
        aboutSheetFragment.show(parentFragmentManager, aboutSheetFragment.tag)
    }

    /**
     * 沒有站牌存在群組中 提示彈窗
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun noStationInformation(){
        /**沒有站牌存在群組中 提示彈窗*/
        val toast =
            Toast.makeText(requireParentFragment().requireContext(), "此群組沒有任何站牌", Toast.LENGTH_SHORT)
        //設定提示彈窗位置
        toast.setGravity(Gravity.CENTER, 0, 0)
        //顯示提示彈窗
        toast.show()
    }

    /**
     * 初始化 更多功能按鈕 功能
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initMoreMenuButton(){
        //當 更多功能按鈕 被按下
        val moreButton = this.view?.findViewById<MaterialButton>(R.id.moreButton)
        moreButton?.setOnClickListener{
            /**PopupMenu菜單窗口*/
            val popupMenu = PopupMenu(context, moreButton)
            //設置PopupMenu對象的佈局 與帶入menu菜單
            popupMenu.menuInflater.inflate(R.menu.bus_menu, popupMenu.menu)
            //設置menu列表功能
            popupMenu.setOnMenuItemClickListener { item ->
                val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
                busFragment?.let {
                    when (item.itemId) {
                        /**編輯群組*/
                        R.id.bus_editGroup -> {
                            editGroup()
                        }
                        /**刪除群組*/
                        R.id.bus_removeGroup -> {
                            removeGroup()
                        }
                        /**排序站牌*/
                        R.id.bus_sequenceStation -> {
                            if (collectList[busFragment.currentItem].SaveStationList.isNotEmpty())
                                sequenceStation()
                            else
                                noStationInformation()
                        }
                        /**刪除站牌*/
                        R.id.bus_removeStation -> {
                            if (collectList[busFragment.currentItem].SaveStationList.isNotEmpty())
                                removeStation()
                            else
                                noStationInformation()
                        }
                        /**關於介面*/
                        R.id.bus_about -> {
                            aboutSheetFragment()
                        }
                    }
                }
                //關閉列表
                false
            }
            //顯示菜單
            popupMenu.show()
        }
    }

    /**
     * 初始化 搜索框 功能
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initSearchBox(){
        //當搜索框被按下
        this.view?.findViewById<MaterialButton>(R.id.searchBox)?.setOnClickListener{
            //轉換當前的頁面 至 搜索頁面
            /**新介面Activity目標*/
            val intentObj = Intent()
            intentObj.setClass(requireContext(), BusSearchActivity::class.java )
            startActivity(intentObj)
        }
    }

    /**
     * 取得儲存的 站牌群組收藏資料
     * @return 站牌群組收藏資料 : ArrayList<[CollectGroup]>
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun getCollectBuses(): ArrayList<CollectGroup> {

        //取得用戶收藏資料
        /**用戶收藏資料*/
        var saveBusList = Object_SharedPreferences["Bus", "Collects", requireContext()]

        //當收藏資料不存在 重置預設資料
        if( saveBusList == null) {
            //預設收藏資料
            saveBusList = arrayListOf(CollectGroup("最愛", false))
            //存入預設資料
            Object_SharedPreferences.save(
                "Bus",
                "Collects",
                saveBusList,
                requireContext()
            )
        }

        //回傳收藏資料
        @Suppress("UNCHECKED_CAST")
        return saveBusList as ArrayList<CollectGroup>
    }

    /**
     * 創建 收藏群組站牌頁面
     * @return 收藏群組站牌顯示頁面表 : ArrayList<[Fragment]>
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun createFragment(): ArrayList<BusCollectFragment>{
        collectFragment.clear()
        for(i in collectList.indices){
            //添加 站牌顯示頁面
            collectFragment.add(BusCollectFragment())
        }
        //回傳 收藏群組 站牌顯示頁面表
        return collectFragment
    }

    /**
     * 重置所有收藏群組頁面
     * @param lastGroupName [String] 最後瀏覽群組名(部分編輯操作不能使用數字位置)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun resetCollectPage(lastGroupName:String? = null){
        val busFragment = this.view?.findViewById<ViewPager2>(R.id.bus_fragment)
        //重新取得 群組站牌收藏資料
        collectList = getCollectBuses()

        //如果有提供 最後瀏覽群組名 (部分編輯操作不能使用數字位置)
        lastGroupName?.let{
            lastPageNum = collectList.indexOfFirst { it.GroupName == lastGroupName }
        }

        //創建 收藏群組 頁面
        pageAdapter = PageAdapter(
            childFragmentManager,   //子片段管理器
            lifecycle,              //生命週期
            createFragment()        //收藏群組站牌頁面視圖
        )
        //套用 收藏群組頁面 適配器
        busFragment?.adapter = pageAdapter

        //銜接 收藏群組頁面 至 控制Tab
        val busTabs = this.view?.findViewById<TabLayout>(R.id.bus_tabs)
        if (busFragment != null && busTabs != null) {
            TabLayoutMediator(busTabs, busFragment) { tab, position ->
                /**Tab文字控件*/
                val textView = TextView(context)

                //設置文字大小
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 16f, resources.displayMetrics))
                //設置文字顏色
                textView.setTextColor(Color.parseColor("#FFFFFF"))
                //設置文字布局 (置中)
                textView.gravity = Gravity.CENTER
                //設置tab自定義視圖 為 Tab文字控件
                tab.customView = textView
                //設置文字內容 (收藏群組名稱)
                textView.text = collectList[position].GroupName

                /**外部布局適配*/
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0f)
                //套用 外部布局適配 至 Tab & Tab文字控件
                textView.layoutParams = params
                tab.view.layoutParams = params
            }.attach()
        }

        //當最後瀏覽的頁面 超過於總頁數 則歸0
        if(lastPageNum >= collectList.size)
            lastPageNum = 0
        //前往最後瀏覽的頁面
        busFragment?.currentItem = lastPageNum
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
        //關閉左右托拽時的Android預設動畫
        this.view?.findViewById<ViewPager2>(R.id.bus_fragment)?.getChildAt(0)?.overScrollMode =
            View.OVER_SCROLL_NEVER

        //網路接收器初始化
        initInternetReceiver()
        //更多功能菜單
        initMoreMenuButton()
        //搜索介面
        initSearchBox()
        //重置所有收藏群組頁面
        resetCollectPage()
    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStart() {
        super.onStart()
        //初始化網路接收器
        activity?.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onStop(){
        super.onStop()
        activity?.unregisterReceiver(internetReceiver)
    }

    /**
     * bus_page頁面控件適配器
     * @param fragmentManager [FragmentManager]    子片段管理器
     * @param lifecycle [Lifecycle]                生命週期
     * @param groupFragments ArrayList<[Fragment]> 收藏群組站牌頁面視圖
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class PageAdapter @OptIn(DelicateCoroutinesApi::class) constructor(
        fragmentManager: FragmentManager,           // 子片段管理器
        lifecycle: Lifecycle,                       // 生命週期
        /**收藏群組站牌頁面視圖*/
        private val groupFragments: ArrayList<BusCollectFragment>
    ):  FragmentStateAdapter(                       // 片段狀態適配器
        fragmentManager,                            // 片段管理器
        lifecycle                                   // 生命週期
    ){

        /**頁面數量
         * @return 頁面數量 : [Int]
         */
        @OptIn(DelicateCoroutinesApi::class)
        override fun getItemCount(): Int {
            return groupFragments.size
        }

        /**創建頁面
         * @param position [Int] 頁面數量
         * @return 頁面 : [Fragment]
         */
        @OptIn(DelicateCoroutinesApi::class)
        override fun createFragment(position: Int): Fragment {
            /**傳入介面的資料包*/
            val args = Bundle()
            //傳入該群組站牌列表
            args.putString("CollectListGroupName", collectList[position].GroupName)
            //將資料包傳入介面
            groupFragments[position].arguments = args
            //顯示編輯群組介面
            return groupFragments[position]
        }
    }
}