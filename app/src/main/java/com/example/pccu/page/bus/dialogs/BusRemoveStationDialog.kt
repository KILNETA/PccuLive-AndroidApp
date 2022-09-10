package com.example.pccu.page.bus.dialogs

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.example.pccu.internet.CollectGroup
import com.example.pccu.R
import com.example.pccu.sharedFunctions.Object_SharedPreferences
import kotlinx.android.synthetic.main.bus_dialog.*
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.sharedFunctions.PopWindows
import com.example.pccu.sharedFunctions.RV
import com.example.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.bus_dialog_station_remove_item.view.*
import kotlinx.coroutines.*

/**
 * 站牌收藏群組 刪除站牌
 * @param editGroupName [String] 要調整的收藏群組名
 * @param listener      [PopWindows.Listener] 回傳函式
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusRemoveStationDialog (
    /**要調整的收藏群組名*/
    private val editGroupName : String,
    /**回傳函式*/
    private val listener: PopWindows.Listener
): DialogFragment(R.layout.bus_dialog) {
    /**所有收藏群組*/
    private var allCollectList : ArrayList<CollectGroup>? = null
    /**調整的站牌群組*/
    private var collectGroup : CollectGroup? = null
    /**勾選數據列表*/
    private val onChecks = arrayListOf<Boolean>()
    /**列表適配器*/
    private var adapter = Adapter()

    /**
     * 初始化彈窗大小
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initWindowsSize(){
        /**對話框*/
        val dialog = dialog ?: return
        /**對話框視窗*/
        val window = dialog.window ?: return
        /**屏幕大小*/
        val size = ViewGauge.getScreenSize(requireActivity())
        /**對話框視窗屬性*/
        val attributes = window.attributes
        //彈窗大小寬度為屏幕的80%
        attributes.width = (size.x * 0.8).toInt()
        //套用設定
        window.attributes = attributes
    }

    /**
     * 初始化站牌列表
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initStationRV(){
        //初始化 勾選數據列表
        for(i in collectGroup!!.SaveStationList.indices) {
            onChecks.add(false)
        }
        /**列表控件*/
        val recycler = RecyclerView(requireContext())
        recycler.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        //消除頂部底部動畫
        recycler.overScrollMode = View.OVER_SCROLL_NEVER
        //創建Bus列表
        recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器
        recycler.adapter = adapter
        listView.addView(recycler)
    }

    /**
     * 初始化取消按鈕
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initCancelButton(){
        /**取消按鈕控件*/
        val buttonCancel = Button(requireContext())
        buttonCancel.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        //設置按鈕文字
        buttonCancel.text = "取消"
        //當按鈕被按下
        buttonCancel.setOnClickListener {
            //關閉彈窗
            dismiss()
        }
        //載入視圖
        buttonView.addView(buttonCancel)
    }

    /**
     * 初始化確認按鈕
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    private fun initConfirmButton(){
        /**確認按鈕控件*/
        val buttonConfirm = Button(requireContext())
        buttonConfirm.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        //設置按鈕文字
        buttonConfirm.text = "確認"
        //當按鈕被按下
        buttonConfirm.setOnClickListener {
            if(onChecks.any{it}) {
                /**計算刪除的站牌數*/
                var removeGroupNum = 0
                //刪除選定的站牌(倒敘刪除)
                for (i in collectGroup!!.SaveStationList.indices.reversed()) {
                    if (onChecks[i]) {
                        removeGroupNum++
                        collectGroup!!.SaveStationList.removeAt(i)
                    }
                }
                GlobalScope.launch(Dispatchers.Main) {
                    //異步保存修改的數據
                    withContext(Dispatchers.IO) {
                        Object_SharedPreferences.save(
                            "Bus",
                            "Collects",
                            allCollectList!!,
                            requireContext()
                        )
                    }
                    //提示彈窗
                    PopWindows.popLongHint(
                        requireActivity().baseContext,
                        "已刪除 $removeGroupNum 個站牌"
                    )
                    //回應父輩
                    listener.respond(true)
                    //關閉彈窗
                    dismiss()
                }
            }
        }
        //載入視圖
        buttonView.addView(buttonConfirm)
    }

    /**
     * 建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //設置對話框功能標題
        dialogName.text = "選擇移除的站牌"

        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        allCollectList = Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>
        //取得要修改的群組
        collectGroup = allCollectList!!.first { it.GroupName == editGroupName }

        //初始化彈窗大小
        initWindowsSize()
        //初始化站牌列表
        initStationRV()
        //初始化取消按鈕
        initCancelButton()
        //初始化確認按鈕
        initConfirmButton()
    }



    /**
     * 列表控件的適配器 "內部類"
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class Adapter : RecyclerView.Adapter<RV.ViewHolder>() {

        /**
         * 重構 創建視圖持有者 (連結bus_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            //加載布局於當前的context 列表控件元素bus_item
            val view =
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_station_remove_item, parent, false)
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemCount(): Int {
            return collectGroup!!.SaveStationList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {
            /**指定位置的站牌資料*/
            val station = collectGroup!!.SaveStationList[position]
            //設置路線名稱
            holder.itemView.BusName.text = station.RouteData.RouteName.Zh_tw
            //設置終點站名稱
            @Suppress("SetTextI18n")
            holder.itemView.DestinationStopName.text = "往${station.DestinationStopName}"
            //設置站牌名稱
            holder.itemView.StationName.text = station.StationName.Zh_tw

            //當Item被按下
            holder.itemView.setOnClickListener {
                //設置是否勾選
                if(!holder.itemView.check.isChecked){
                    holder.itemView.check.isChecked = true
                    onChecks[position] = holder.itemView.check.isChecked
                }else{
                    holder.itemView.check.isChecked = false
                    onChecks[position] = holder.itemView.check.isChecked
                }
            }
        }
    }
}