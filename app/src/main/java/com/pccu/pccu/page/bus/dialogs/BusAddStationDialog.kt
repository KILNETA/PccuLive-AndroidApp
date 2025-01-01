package com.pccu.pccu.page.bus.dialogs

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.pccu.pccu.internet.CollectGroup
import com.pccu.pccu.internet.CollectStation
import com.pccu.pccu.R
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pccu.pccu.sharedFunctions.PToast
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.coroutines.*

/**
 * 站牌收藏群組 添加站牌
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusAddStationDialog : DialogFragment(R.layout.bus_dialog)  {
    /**所有收藏群組*/
    private var allCollectList : ArrayList<CollectGroup>? = null
    /**調整的站牌群組*/
    private var stationData : CollectStation? = null
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
     * 初始化輸入框
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initGroupRV(){
        //初始化 勾選數據列表
        for(i in allCollectList!!.indices) {
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
        this.view?.findViewById<LinearLayout>(R.id.listView)?.addView(recycler)
    }

    /**
     * 初始化取消按鈕
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initCancelButton(){
        /**取消按鈕控件*/
        val buttonCancel = Button(requireContext())
        buttonCancel.layoutParams = LinearLayout.LayoutParams(
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
        this.view?.findViewById<LinearLayout>(R.id.buttonView)?.addView(buttonCancel)
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
            onChecks.forEachIndexed { index, element ->
                GlobalScope.launch ( Dispatchers.Main ) {
                    if(element) {
                        if(allCollectList!![index].SaveStationList.any {
                                it.StationUID == stationData!!.StationUID &&
                                it.Direction == stationData!!.Direction
                        }){ /**已存在此站牌*/
                            //提示彈窗
                            PToast.popLongHint(
                                requireActivity().baseContext,
                                "${allCollectList!![index].GroupName} 已存在此站牌"
                            )
                        }else {
                            /**成功添加站牌*/
                            allCollectList!![index].SaveStationList.add(stationData!!)
                            //提示彈窗
                            PToast.popLongHint(
                                requireActivity().baseContext,
                                "成功添加至 ${allCollectList!![index].GroupName}"
                            )
                        }
                    }
                    //異步保存修改的數據
                    withContext(Dispatchers.IO) {
                        Object_SharedPreferences.save(
                            "Bus",
                            "Collects",
                            allCollectList!!,
                            requireContext()
                        )
                    }
                    //關閉彈窗
                    dismiss()
                }
            }
        }
        this.view?.findViewById<LinearLayout>(R.id.buttonView)?.addView(buttonConfirm)
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
        this.view?.findViewById<TextView>(R.id.dialogName)?.text = "選擇站牌收藏群組"
        //初始化彈窗大小
        initWindowsSize()

        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        allCollectList =
            Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>
        //取得欲收藏的站牌資料
        stationData =
            requireArguments().getSerializable("saveStation") as CollectStation


        //初始化彈窗大小
        initWindowsSize()
        //初始化群組列表
        initGroupRV()
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
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_station_addition_item, parent, false)
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
            return allCollectList!!.size
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
            /**指定位置的群組資料*/
            val group = allCollectList!![position]
            //設置群組名稱
            holder.itemView.findViewById<TextView>(R.id.GroupName)?.text = group.GroupName

            //當Item被按下
            holder.itemView.findViewById<LinearLayout>(R.id.bus_dialog_station_addition_item)?.setOnClickListener {
                val check = holder.itemView.findViewById<CheckBox>(R.id.check)
                //設置是否勾選
                if(!check.isChecked){
                    check.isChecked = true
                    onChecks[position] = check.isChecked
                }else{
                    check.isChecked = false
                    onChecks[position] = check.isChecked
                }
            }
        }
    }
}