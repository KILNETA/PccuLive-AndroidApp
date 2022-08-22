package com.example.pccu.Page

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
import com.example.pccu.About.About_BottomSheet
import com.example.pccu.Internet.*
import com.example.pccu.Page.Bus.Bus_CollectFragment
import com.example.pccu.Page.Bus.Dialogs.*
import com.example.pccu.Page.Bus.Search.Search_Activity
import com.example.pccu.Shared_Functions.Object_SharedPreferences
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.bus_page.bus_fragment
import kotlinx.android.synthetic.main.bus_page.bus_tabs
import kotlinx.android.synthetic.main.bus_page.moreButton
import kotlinx.android.synthetic.main.bus_route_page.*


/**
 * 公車系統 主頁面 頁面建構類 : "Fragment(bus_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Bus_Page : Fragment(R.layout.bus_page) {

    /**頁面適配器*/
    var pageAdapter : PageAdapter? = null

    var LastPageNum = 0

    var CollectList : ArrayList<SaveBusList> = arrayListOf()

    fun setMore_MenuButton(){
        moreButton.setOnClickListener{
            //定義PopupMenu對象
            val popupMenu = PopupMenu(context, moreButton)
            //設置PopupMenu對象的佈局
            popupMenu.getMenuInflater().inflate(R.menu.bus_menu, popupMenu.getMenu())
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.bus_editGroup -> {
                        val editGroup = Bus_editGroup_Dialog(
                            object : Bus_editGroup_Dialog.PriorityListener {
                                override fun respond(respond: Boolean?) {
                                    if (respond!!) setCollectPage()
                                }
                            }
                        )
                        val args = Bundle()
                        args.putSerializable("CollectList", CollectList)
                        LastPageNum = 0
                        editGroup.setArguments(args)
                        editGroup.show(childFragmentManager,"editGroup")
                    }
                    R.id.bus_removeGroup ->{
                        val removeGroup = Bus_removeGroup_Dialog(
                            object : Bus_removeGroup_Dialog.PriorityListener {
                                override fun respond(respond: Boolean?) {
                                    if(respond!!) setCollectPage()
                                }
                            }
                        )
                        val args = Bundle()
                        LastPageNum = 0
                        removeGroup.setArguments(args)
                        removeGroup.show(childFragmentManager,"removeGroup")
                    }
                    R.id.bus_sequenceStation -> {
                        if(CollectList[bus_fragment.currentItem].SaveStationList.isNotEmpty()) {
                            val sequenceStation = Bus_sequenceStation_Dialog(
                                object : Bus_sequenceStation_Dialog.PriorityListener {
                                    override fun respond(respond: Boolean?) {
                                        if (respond!!) setCollectPage()
                                    }
                                }
                            )
                            val args = Bundle()
                            args.putSerializable(
                                "CollectStation",
                                CollectList[bus_fragment.currentItem]
                            )
                            LastPageNum = bus_fragment.currentItem
                            sequenceStation.setArguments(args)
                            sequenceStation.show(childFragmentManager, "sequenceStation")
                        }
                        else{
                            val toast: Toast =
                                Toast.makeText(parentFragment!!.context!!, "此群組沒有任何站牌", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }
                    }
                    R.id.bus_removeStation -> {
                        if(CollectList[bus_fragment.currentItem].SaveStationList.isNotEmpty()) {
                            val removeStation = Bus_removeStation_Dialog(
                                    object : Bus_removeStation_Dialog.PriorityListener {
                                    override fun respond(respond: Boolean?) {
                                        if(respond!!) setCollectPage()
                                    }
                                }
                            )
                            val args = Bundle()
                            args.putSerializable(
                                "CollectStation",
                                CollectList[bus_fragment.currentItem]
                            )
                            LastPageNum = bus_fragment.currentItem
                            removeStation.setArguments(args)
                            removeStation.show(childFragmentManager, "removeStation")
                        }
                        else{
                            val toast: Toast =
                                Toast.makeText(parentFragment!!.context!!, "此群組沒有任何站牌", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }
                    }
                    R.id.bus_bout -> {
                        val context = arrayOf(
                            "提醒：",
                            "　　氣象資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
                            "",
                            "資料來源：",
                            "　　交通部中央氣象局、行政院環境保護署"
                        )

                        val FastLinkSheetFragment = About_BottomSheet(context)
                        FastLinkSheetFragment.show(parentFragmentManager, FastLinkSheetFragment.tag)
                    }
                }
                false
            }
            //顯示菜單
            popupMenu.show()

        }
    }

    fun setSearchBox(){
        searchBox.setOnClickListener{
            //轉換當前的頁面 至 公告內文頁面
            //新方案 (新建Activity介面展示)
            val IntentObj = Intent()
            IntentObj.setClass(context!!, Search_Activity::class.java )
            startActivity(IntentObj)
        }
    }

    fun getCollectBuses(): ArrayList<SaveBusList> {

        var Return = Object_SharedPreferences.get(
            "Bus",
            "Collects",
            context!!)

        if( Return == null) {
            Return = arrayListOf<SaveBusList>(
                SaveBusList("最愛", false)
            )
            Object_SharedPreferences.save(
                "Bus",
                "Collects",
                Return,
                context!!
            )
        }

       return if(Return!=null) Return as ArrayList<SaveBusList> else arrayListOf()
    }

    fun createFragment(): ArrayList<Fragment>{
        val fragment = arrayListOf<Fragment>()
        for(i in CollectList.indices){
            fragment.add(Bus_CollectFragment())
        }
        return fragment
    }

    fun setCollectPage(){

        CollectList = getCollectBuses()!!
        //創建Bus頁面資料
        pageAdapter = PageAdapter(
            childFragmentManager,
            lifecycle,
            createFragment()
        )
        //Bus頁面 是配器
        bus_fragment.adapter = pageAdapter

        //套用至Bus頁面的標題
        bus_tabs.scrollBarSize
        TabLayoutMediator(bus_tabs, bus_fragment) { tab, position ->

            val textView = TextView(context)
            val selectedSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 16f, resources.displayMetrics)

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
            textView.setTextColor(resources.getColor(R.color.white))
            textView.text = tab.text
            textView.gravity = Gravity.CENTER
            tab.customView = textView
            textView.text = CollectList[position].ListName

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f)
            textView.setLayoutParams(params)
            tab.view.setLayoutParams(params)
        }.attach()

        if(LastPageNum >= CollectList.size)
            LastPageNum = 0
        bus_fragment.setCurrentItem(LastPageNum)
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
        setMore_MenuButton()
        //搜索介面
        setSearchBox()

        getCollectBuses()

        setCollectPage()
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
            val args = Bundle()
            args.putSerializable("CollectList", CollectList[position]);
            fragments[position].setArguments(args)
            return fragments[position]
        }
    }
}