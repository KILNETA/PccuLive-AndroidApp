package com.pccu.pccu.page.bus.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pccu.pccu.internet.CollectGroup
import com.pccu.pccu.R
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.PToast
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.android.synthetic.main.bus_dialog_group_remove_item.view.*
import kotlinx.coroutines.*

/**
 * 站牌收藏群組 添加站牌
 * @param listener      [PToast.Listener] 回傳函式
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusRemoveGroupDialog(
    /**回傳函式*/
    private val listener : PToast.Listener
): DialogFragment(R.layout.bus_dialog) {
    /**所有收藏群組*/
    private var allCollectList : ArrayList<CollectGroup>? = null
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
    private fun initGroupRV(){
        //初始化 勾選數據列表
        for(i in allCollectList!!.iterator()) {
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
     * 儲存修改後的收藏群組
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    private fun saveCollectGroup( respond:Boolean){
        if(respond) {
            /**計算刪除的群組數*/
            var removeGroupNum = 0
            //刪除選定的站牌(倒敘刪除)
            for (i in allCollectList!!.indices.reversed()) {
                if (onChecks[i] && allCollectList!![i].canLost) {
                    allCollectList!!.removeAt(i)
                    removeGroupNum++
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
                PToast.popLongHint(
                    requireActivity().baseContext,
                    "已刪除 $removeGroupNum 個群組"
                )
                //回應父輩
                listener.respond(true)
                //關閉彈窗
                dismiss()
            }
        }
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
                /**刪除群組 再次確認框*/
                val respond = BusRemoveGroupCheckDialog(
                    object : PToast.Listener {
                        override fun respond(respond: Boolean?) {
                            respond?.let{ saveCollectGroup(it) }
                        }
                    }
                )
                respond.show(childFragmentManager,"respond")
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
        dialogName.text = "選擇移除的群組"
        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        allCollectList = Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>

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
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_remove_item, parent, false)
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
            //設置群組名稱
            holder.itemView.GroupName.text = allCollectList!![position].GroupName
            //群組不能被刪除
            if (!allCollectList!![position].canLost){
                holder.itemView.GroupName.setTextColor(Color.parseColor("#BABABA"))
                holder.itemView.check.buttonTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            }
            else{
                holder.itemView.bus_dialog_group_remove_item.setOnClickListener {
                    //設置是否勾選
                    if(!holder.itemView.check.isChecked){
                        holder.itemView.check.isChecked = true
                        onChecks[position] = holder.itemView.check.isChecked
                    }else{
                        holder.itemView.check.isChecked = false
                        onChecks[position] = holder.itemView.check.isChecked
}   }   }   }   }   }