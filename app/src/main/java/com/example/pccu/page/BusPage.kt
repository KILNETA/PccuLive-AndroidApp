package com.example.pccu.page

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pccu.R
import kotlinx.android.synthetic.main.bus_page.*
import androidx.annotation.RequiresApi
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pccu.about.AboutBottomSheet
import com.example.pccu.internet.*
import com.example.pccu.page.bus.BusCollectFragment
import com.example.pccu.page.bus.Dialogs.*
import com.example.pccu.page.bus.search.SearchActivity
import com.example.pccu.sharedFunctions.Object_SharedPreferences
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.bus_page.bus_fragment
import kotlinx.android.synthetic.main.bus_page.bus_tabs
import kotlinx.android.synthetic.main.bus_page.moreButton

/**
 * 公車系統 主頁面 頁面建構類 : "Fragment(bus_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class BusPage : Fragment(R.layout.bus_page) {

    /**頁面適配器*/
    private var pageAdapter : PageAdapter? = null
    /**最後瀏覽的頁面*/
    private var lastPageNum = 0
    /**站牌收藏列表*/
    private var collectList : ArrayList<CollectGroup> = arrayListOf()

    /**
     * 編輯群組
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun editGroup(){
        /**編輯群組介面 (Dialog)*/
        val editGroup = Bus_editGroup_Dialog(
            object : Bus_editGroup_Dialog.PriorityListener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    if (respond!!) {
                        //設置最後瀏覽的頁面位置
                        lastPageNum = 0
                        resetCollectPage()
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
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun removeGroup() {
        /**刪除群組介面 (Dialog)*/
        val removeGroup = Bus_removeGroup_Dialog(
            object : Bus_removeGroup_Dialog.PriorityListener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    if(respond!!) {
                        //設置最後瀏覽的頁面位置
                        lastPageNum = 0
                        resetCollectPage()
                    }
                }
            }
        )
        //顯示刪除群組介面
        removeGroup.show(childFragmentManager,"removeGroup")
    }

    /**
     * 排序站牌
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun sequenceStation() {
        /**排序站牌介面 (Dialog)*/
        val sequenceStation = Bus_sequenceStation_Dialog(
            object : Bus_sequenceStation_Dialog.PriorityListener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    if (respond!!) {
                        //設置最後瀏覽的頁面位置
                        lastPageNum = bus_fragment.currentItem
                        resetCollectPage()
                    }
                }
            }
        )
        /**傳入介面的資料包*/
        val args = Bundle()
        //傳入當前站牌列表
        args.putSerializable("CollectStation", collectList[bus_fragment.currentItem])
        //將資料包傳入介面
        sequenceStation.arguments = args
        //顯示編輯群組介面
        sequenceStation.show(childFragmentManager, "sequenceStation")
    }

    /**
     * 刪除站牌
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun removeStation(){
        /**刪除站牌介面 (Dialog)*/
        val removeStation = Bus_removeStation_Dialog(
            object : Bus_removeStation_Dialog.PriorityListener {
                //回應是否需要重置站牌頁面+列表
                override fun respond(respond: Boolean?) {
                    if(respond!!){
                        //設置最後瀏覽的頁面位置
                        lastPageNum = bus_fragment.currentItem
                        resetCollectPage()
                    }
                }
            }
        )
        /**傳入介面的資料包*/
        val args = Bundle()
        //傳入當前站牌列表
        args.putSerializable("CollectStation", collectList[bus_fragment.currentItem])
        //將資料包傳入介面
        removeStation.arguments = args
        //顯示編輯群組介面
        removeStation.show(childFragmentManager, "removeStation")
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
            "　　本程式之公告系統僅是提供便捷查詢，無法保證公車班次的展示之正確性，若認為是重要訊息通知，" +
                    "請務必校驗官方，以確保內容正確，若造成損失本程式一概不負責。",
            "",
            "資料來源：",
            "　　TDX運輸資料流通服務"
        )
        /**關於介面 底部彈窗*/
        val aboutSheetFragment = AboutBottomSheet(content)
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
            Toast.makeText(parentFragment!!.context!!, "此群組沒有任何站牌", Toast.LENGTH_SHORT)
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
        moreButton.setOnClickListener{
            /**PopupMenu菜單窗口*/
            val popupMenu = PopupMenu(context, moreButton)
            //設置PopupMenu對象的佈局 與帶入menu菜單
            popupMenu.menuInflater.inflate(R.menu.bus_menu, popupMenu.menu)
            //設置menu列表功能
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
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
                        if (collectList[bus_fragment.currentItem].SaveStationList.isNotEmpty())
                            sequenceStation()
                        else
                            noStationInformation()
                    }
                    /**刪除站牌*/
                    R.id.bus_removeStation -> {
                        if (collectList[bus_fragment.currentItem].SaveStationList.isNotEmpty())
                            removeStation()
                        else
                            noStationInformation()
                    }
                    /**關於介面*/
                    R.id.bus_about -> {
                        aboutSheetFragment()
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
        searchBox.setOnClickListener{
            //轉換當前的頁面 至 搜索頁面
            /**新介面Activity目標*/
            val intentObj = Intent()
            intentObj.setClass(context!!, SearchActivity::class.java )
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
        var saveBusList = Object_SharedPreferences["Bus", "Collects", context!!]

        //當收藏資料不存在 重置預設資料
        if( saveBusList == null) {
            //預設收藏資料
            saveBusList = arrayListOf(CollectGroup("最愛", false))
            //存入預設資料
            Object_SharedPreferences.save(
                "Bus",
                "Collects",
                saveBusList,
                context!!
            )
        }

        //回傳收藏資料
        return saveBusList as ArrayList<CollectGroup>
    }

    /**
     * 創建 收藏群組站牌頁面
     * @return 收藏群組站牌顯示頁面表 : ArrayList<[Fragment]>
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun createFragment(): ArrayList<Fragment>{
        /**收藏群組 站牌顯示頁面表*/
        val fragment = arrayListOf<Fragment>()
        for(i in collectList.indices){
            //添加 站牌顯示頁面
            fragment.add(BusCollectFragment())
        }
        //回傳 收藏群組 站牌顯示頁面表
        return fragment
    }

    /**
     * 重置所有收藏群組頁面
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun resetCollectPage(){

        //重新取得 群組站牌收藏資料
        collectList = getCollectBuses()
        //創建 收藏群組 頁面
        pageAdapter = PageAdapter(
            childFragmentManager,   //子片段管理器
            lifecycle,              //生命週期
            createFragment()        //收藏群組站牌頁面視圖
        )
        //套用 收藏群組頁面 適配器
        bus_fragment.adapter = pageAdapter

        //銜接 收藏群組頁面 至 控制Tab
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->
            /**Tab文字控件*/
            val textView = TextView(context)

            //設置文字大小
            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 16f, resources.displayMetrics))
            //設置文字顏色
            textView.setTextColor(resources.getColor(R.color.white))
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

        //當最後瀏覽的頁面 超過於總頁數 則歸0
        if(lastPageNum >= collectList.size)
            lastPageNum = 0
        //前往最後瀏覽的頁面
        bus_fragment.currentItem = lastPageNum
    }

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //更多功能菜單
        initMoreMenuButton()
        //搜索介面
        initSearchBox()
        //重置所有收藏群組頁面
        resetCollectPage()
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
    inner class PageAdapter(
        fragmentManager: FragmentManager,           // 子片段管理器
        lifecycle: Lifecycle,                       // 生命週期
        /**收藏群組站牌頁面視圖*/
        private val groupFragments: ArrayList<Fragment>
    ):  FragmentStateAdapter(                       // 片段狀態適配器
        fragmentManager,                            // 片段管理器
        lifecycle                                   // 生命週期
    ){

        /**頁面數量
         * @return 頁面數量 : [Int]
         */
        override fun getItemCount(): Int {
            return groupFragments.size
        }

        /**創建頁面
         * @param position [Int] 頁面數量
         * @return 頁面 : [Fragment]
         */
        override fun createFragment(position: Int): Fragment {
            /**傳入介面的資料包*/
            val args = Bundle()
            //傳入該群組站牌列表
            args.putSerializable("CollectList", collectList[position]);
            //將資料包傳入介面
            groupFragments[position].arguments = args
            //顯示編輯群組介面
            return groupFragments[position]
        }
    }
}