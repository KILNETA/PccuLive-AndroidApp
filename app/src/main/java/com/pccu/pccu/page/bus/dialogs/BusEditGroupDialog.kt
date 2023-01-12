package com.pccu.pccu.page.bus.dialogs

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pccu.pccu.internet.CollectGroup
import com.pccu.pccu.R
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import android.view.MotionEvent
import com.pccu.pccu.sharedFunctions.PToast
import kotlinx.android.synthetic.main.bus_dialog_group_addition_item.view.*
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.GroupName
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.bar

class BusEditGroupDialog(
    /**回傳函式*/
    private val listener: PToast.Listener
) : DialogFragment(R.layout.bus_dialog) {
    /**要調整的收藏群組名*/
    private var collectList : ArrayList<CollectGroup>? = null
    /**列表適配器*/
    private var adapter = Adapter()
    /**列表物品觸控助手*/
    private val mITH = ItemTouchHelper(RV.ITHC(adapter))
    /**是否需要回傳重整列表 (有排列動作)*/
    private var sequenced = false
    /**是否需要回傳重整列表 (添加群組動作)*/
    private var added = false

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
        //列表物品觸控助手 套用 列表 (不能少 少了會讓Item拖移出Bug 且很難排查)
        mITH.attachToRecyclerView(recycler)
        //加載視圖
        listView.addView(recycler)
    }

    /**
     * 初始化取消按鈕
     * @author KILNETA
     * @since Alpha_5.0
     */
    private fun initCancelButton(){

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
            //
            if(sequenced || added)
                listener.respond(true)
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
            GlobalScope.launch(Dispatchers.Main) {
                //異步保存修改的數據
                withContext(Dispatchers.IO) {
                    Object_SharedPreferences.save(
                        "Bus",
                        "Collects",
                        collectList!!,
                        requireContext()
                    )
                }
                //提示彈窗
                PToast.popLongHint(
                    requireActivity().baseContext,
                    "群組編輯完成"
                )
                //回應父輩
                if(sequenced || added)
                    listener.respond(true)
                //關閉彈窗
                dismiss()
            }
        }
        //載入視圖
        buttonView.addView(buttonConfirm)
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //設置對話框功能標題
        dialogName.text = "編輯群組"
        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        collectList = Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>

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
    inner class Adapter : RecyclerView.Adapter<ViewHolder>(),RV.ITHA {

        /**
         * 列表控件的適配器 "內部類"
         * @param position [Int] Item座標
         * @return 視圖標籤 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemViewType(position: Int): Int {
            return if(position < collectList!!.size) 0 else 1
        }

        /**
         * 重構 創建視圖持有者 (連結bus_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            //加載布局於當前的context 列表控件元素bus_item
            val view =
                //判斷視圖標籤
                if(viewType == 0)
                    //編輯群組Item
                    LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_edit_item, parent, false)
                else
                    //添加群組Item
                    LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_addition_item, parent, false)
            return ViewHolder(view as LinearLayout)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getItemCount(): Int {
            return collectList!!.size + 1
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        @DelicateCoroutinesApi
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            if(position < collectList!!.size) {
                //Item座標 為 編輯群組Item

                holder.itemView.GroupName.text = collectList!![position].GroupName

                //可編輯的群組
                if(collectList!![position].canLost) {
                    //當控制桿被按下
                    setBarTouchListener(holder)
                    //當修改群組名稱被按下
                    setEditTouchListener(holder,position)
                } else {
                    //不可編輯的群組
                    initCantLostItem(holder)
                }
            } else{
                //Item座標 為 添加群組Item
                setAddTouchListener(holder)
            }
        }

        /**
         * 設置控制桿觸控
         * @param holder [ViewHolder] 當前持有者
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        @SuppressLint("ClickableViewAccessibility")
        private fun setBarTouchListener(holder: ViewHolder){
            holder.itemView.bar.setOnTouchListener { v, event ->
                v.performClick()
                v.onTouchEvent(event)

                if (event.action == MotionEvent.ACTION_DOWN) {
                    //通知ItemTouchHelper開始拖拽
                    object : RV.OnSDL {
                        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                            //通知ItemTouchHelper開始拖拽
                            mITH.startDrag(holder)
                        }
                    }.onStartDrag(holder)
                }
                return@setOnTouchListener false
            }
        }

        /**
         * 設置編輯紐觸控
         * @param holder [ViewHolder] 當前持有者
         * @param position [Int] 控件座標
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setEditTouchListener(holder: ViewHolder,position: Int){
            holder.itemView.edit.setOnClickListener {
                BusEditGroupNameDialog(
                    0,
                    listener,
                    collectList!![position].GroupName
                ).show(parentFragment!!.childFragmentManager,"resetGroupName")
                dismiss()
            }
        }

        /**
         * 設置控添加群組Item觸控
         * @param holder [ViewHolder] 當前持有者
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun setAddTouchListener(holder: ViewHolder){
            holder.itemView.bus_dialog_group_addition_item.setOnClickListener {
                BusEditGroupNameDialog(
                    1,
                    listener
                ).show(parentFragment!!.childFragmentManager,"addGroup")
                dismiss()
            }
        }

        /**
         * 初始化 不可編輯群組選項 控件
         * @param holder [ViewHolder] 當前持有者
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun initCantLostItem(holder: ViewHolder){
            holder.itemView.GroupName.setTextColor(Color.parseColor("#BABABA"))
            holder.itemView.bar.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            holder.itemView.edit.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }

        /**
         * 重構 項目移動時
         * @param fromPosition  [Int] 來源元素位置(第幾項)
         * @param toPosition    [Int] 到達元素位置(第幾項)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            if(toPosition < collectList!!.size && collectList!![toPosition].canLost) {
                if(!sequenced)
                    sequenced = true
                Collections.swap(collectList!!, fromPosition, toPosition)
                notifyItemMoved(fromPosition, toPosition)
            }
        }
    }

    /**
     * 當前持有者 "內部類"
     * @param itemView [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class ViewHolder(
        itemView: LinearLayout ) : RV.ViewHolder(itemView), RV.ITHVH {

        /**item被選中，在側滑或拖拽過程中更新狀態*/
        override fun onItemSelected() {
            itemView.setBackgroundColor(
                Color.parseColor("#f5f5f5"))
        }

        /**item的拖拽或側滑結束，恢復默認的狀態*/
        override fun onItemClear() {
            itemView.setBackgroundColor(
                Color.parseColor("#FFFFFF"))
        }
    }
}