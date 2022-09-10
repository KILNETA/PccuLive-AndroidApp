package com.example.pccu.page.bus.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.pccu.R
import com.example.pccu.sharedFunctions.Object_SharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.internet.*
import java.util.*
import android.view.MotionEvent
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.pccu.sharedFunctions.PopWindows
import com.example.pccu.sharedFunctions.RV
import com.example.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.android.synthetic.main.bus_dialog_station_sequence_item.view.*
import kotlinx.coroutines.*

/**
 * 站牌收藏群組 排序站牌
 * @param editGroupName [String] 要調整的收藏群組名
 * @param listener      [PopWindows.Listener] 回傳函式
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusSequenceStationDialog (
    /**要調整的收藏群組名*/
    private val editGroupName : String,
    /**回傳函式*/
    private val listener: PopWindows.Listener
) : DialogFragment(R.layout.bus_dialog) {
    /**所有收藏群組*/
    private var allCollectList : ArrayList<CollectGroup>? = null
    /**調整的站牌群組*/
    private var collectGroup : CollectGroup? = null
    /**列表適配器*/
    private var adapter = Adapter()
    /**列表物品觸控助手*/
    private val mITH = ItemTouchHelper(RV.ITHC(adapter))
    /**是否需要回傳重整列表 (有排列動作)*/
    private var sequenced = false

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
                        allCollectList!!,
                        requireContext()
                    )
                }
                //提示彈窗
                PopWindows.popLongHint(
                    requireActivity().baseContext,
                    "排序站牌成功"
                )
                //回應父輩
                listener.respond(sequenced)
                //關閉彈窗
                dismiss()
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
        dialogName.text = "排序站牌"
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
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    inner class Adapter : RecyclerView.Adapter<ViewHolder>(),RV.ITHA {

        /**
         * 重構 創建視圖持有者 (連結bus_dialog_station_sequence_item顯示物件)
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
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_station_sequence_item, parent, false)
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
            return collectGroup!!.SaveStationList.size
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
        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //取得指定站牌資料
            val station = collectGroup!!.SaveStationList[position]
            //設置路線名
            holder.itemView.BusName.text = station.RouteData.RouteName.Zh_tw
            //設置終點站名
            @Suppress("SetTextI18n")
            holder.itemView.DestinationStopName.text = "往${station.DestinationStopName}"
            //設置站牌名稱
            holder.itemView.StationName.text = station.StationName.Zh_tw

            //當控制桿被按下
            holder.itemView.bar.setOnTouchListener { v, event ->
                //視圖執行點擊
                v.performClick()
                //設置觸摸事件
                v.onTouchEvent(event)
                //按下行為 == 行動完成
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
         * 重構 項目移動時
         * @param fromPosition  [Int] 來源元素位置(第幾項)
         * @param toPosition    [Int] 到達元素位置(第幾項)
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            //修改回傳許可
            if(!sequenced)
                sequenced = true
            //資料交換位置
            Collections.swap(collectGroup!!.SaveStationList, fromPosition, toPosition)
            //展示內容互換
            notifyItemMoved(fromPosition, toPosition)
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