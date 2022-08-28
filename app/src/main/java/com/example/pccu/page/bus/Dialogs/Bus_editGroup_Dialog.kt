package com.example.pccu.page.bus.Dialogs

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.internet.CollectGroup
import com.example.pccu.R
import com.example.pccu.sharedFunctions.Object_SharedPreferences
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.android.synthetic.main.bus_dialog_group_addition_item.view.*
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.*
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.GroupName
import kotlinx.android.synthetic.main.bus_dialog_group_edit_item.view.bar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class Bus_editGroup_Dialog(
    val listener: PriorityListener
) : DialogFragment(R.layout.bus_dialog) {

    var CollectList : ArrayList<CollectGroup>? = null
    var sequenced = false
    var added = false

    //初始化 列表適配器
    var adapter = Adapter()
    val mItemTouchHelper  = ItemTouchHelper(MyItemTouchHelperCallback(adapter))

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

    }

    /**
     * 自定义Dialog监听器
     */
    interface PriorityListener {
        /**
         * 回调函数，用于在Dialog的监听事件触发后刷新Activity的UI显示
         */
        fun respond(respond: Boolean?)
    }

    private fun getScreenSize(): Point {
        val size = Point()
        val activity = requireActivity()
        val windowManager = activity.windowManager
        val display: Display = windowManager.defaultDisplay
        display.getSize(size)
        return size
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = dialog ?: return
        val window = dialog.window ?: return
        val size = getScreenSize()
        val attributes = window.attributes
        attributes.width = (size.x * 0.8).toInt()
        window.attributes = attributes

        CollectList = Object_SharedPreferences.get(
            "Bus",
            "Collects",
            context!!) as ArrayList<CollectGroup>

        dialogName.text = "編輯群組"

        val recycler = RecyclerView(context!!)
        recycler.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        //消除頂部底部動畫
        recycler.overScrollMode = View.OVER_SCROLL_NEVER
        //創建Bus列表
        recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //掛載 列表適配器
        recycler.adapter = adapter
        mItemTouchHelper.attachToRecyclerView(recycler)
        listView.addView(recycler)

        val buttonCancel = Button(context!!)
        buttonCancel.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        buttonCancel.text = "取消"
        buttonCancel.setOnClickListener {
            if(sequenced || added)
                listener.respond(true)
            dismiss()
        }
        buttonView.addView(buttonCancel)

        val buttonOk = Button(context!!)
        buttonOk.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        buttonOk.text = "確認"
        buttonOk.setOnClickListener {

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    Object_SharedPreferences.save(
                        "Bus",
                        "Collects",
                        CollectList!!,
                        context!!
                    )
                }
                val toast: Toast =
                    Toast.makeText(parentFragment!!.context!!, "群組編輯完成", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()

                if(sequenced || added)
                    listener.respond(true)
                dismiss()
            }
        }
        buttonView.addView(buttonOk)
    }



    /**
     * Bus列表控件的適配器 "內部類"
     * @param Station List<[Bus_Data_Station]>              站點資訊
     * @param EstimateTime Array<[Bus_Data_EstimateTime]>   到站時間
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class Adapter : RecyclerView.Adapter<MyViewHolder>(),IItemTouchHelperAdapter {

        override fun getItemViewType(position: Int): Int {
            return if(position < CollectList!!.size) 0 else 1
        }

        /**
         * 重構 創建視圖持有者 (連結bus_item顯示物件)
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //加載布局於當前的context 列表控件元素bus_item
            val view =
                if(viewType == 0)
                    LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_edit_item, parent, false)
                else
                    LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_addition_item, parent, false)
            return MyViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量 (站點數量)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            return CollectList!!.size + 1
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

            if(position < CollectList!!.size) {
                holder.itemView.GroupName.text = CollectList!![position].GroupName

                if(CollectList!![position].canLost) {
                    holder.itemView.bar.setOnTouchListener { view, motionEvent ->
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            //通知ItemTouchHelper開始拖拽
                            object : Bus_sequenceStation_Dialog.OnStartDragListener {
                                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                                    //通知ItemTouchHelper開始拖拽
                                    mItemTouchHelper.startDrag(holder)
                                }
                            }.onStartDrag(holder)
                        }
                        return@setOnTouchListener false
                    }
                    holder.itemView.edit.setOnClickListener {
                        val resetGroupName = Bus_addGroup_Dialog(
                            "更改群組名",
                            object : Bus_addGroup_Dialog.PriorityListener {
                                override fun setActivityText(string: String?) {
                                    CollectList!![position].GroupName = string!!
                                    GlobalScope.launch(Dispatchers.Main) {
                                        withContext(Dispatchers.IO) {
                                            Object_SharedPreferences.save(
                                                "Bus",
                                                "Collects",
                                                CollectList!!,
                                                context!!
                                            )
                                        }
                                        val toast: Toast =
                                            Toast.makeText(parentFragment!!.context!!, "群組名稱 $string 更改成功", Toast.LENGTH_SHORT)
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                    }
                                    notifyDataSetChanged()
                                }
                            },
                            CollectList!![position].GroupName
                        )
                        val args = Bundle()
                        resetGroupName.setArguments(args)
                        resetGroupName.show(childFragmentManager,"resetGroupName")
                    }
                }else{
                    holder.itemView.GroupName.setTextColor(Color.parseColor("#BABABA"))
                    holder.itemView.bar.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    holder.itemView.edit.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                }
            }
            else{
                holder.itemView.bus_dialog_group_addition_item.setOnClickListener {
                    val addGroup = Bus_addGroup_Dialog(
                        "新建群組",
                        object : Bus_addGroup_Dialog.PriorityListener {
                            override fun setActivityText(string: String?) {
                                CollectList!!.add(
                                    CollectGroup(
                                        string!!,
                                        true,
                                        arrayListOf()
                                    )
                                )
                                GlobalScope.launch(Dispatchers.Main) {
                                    withContext(Dispatchers.IO) {
                                        Object_SharedPreferences.save(
                                            "Bus",
                                            "Collects",
                                            CollectList!!,
                                            context!!
                                        )
                                    }
                                    val toast: Toast =
                                        Toast.makeText(parentFragment!!.context!!, "群組 $string 創建成功", Toast.LENGTH_SHORT)
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()
                                }
                                if(!added)
                                    added = true
                                notifyDataSetChanged()
                            }
                        }
                    )
                    val args = Bundle()
                    addGroup.setArguments(args)
                    addGroup.show(childFragmentManager,"addGroup")
                }
            }
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            if(toPosition < CollectList!!.size && CollectList!![toPosition].canLost) {
                if(!sequenced)
                    sequenced = true
                Collections.swap(CollectList!!, fromPosition, toPosition)
                notifyItemMoved(fromPosition, toPosition)
            }
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
    inner class MyViewHolder : RecyclerView.ViewHolder,IItemTouchHelperViewHolder {

        private var bus_dialog_edit_item: LinearLayout? = null

        constructor(itemView: View) : super(itemView) {
            bus_dialog_edit_item = itemView.bus_dialog_edit_item
        }

        override fun onItemSelected() {
            bus_dialog_edit_item!!.setBackgroundColor(
                Color.parseColor("#f5f5f5"))
        }

        override fun onItemClear() {
            bus_dialog_edit_item!!.setBackgroundColor(
                Color.parseColor("#FFFFFF"))
        }
    }

    interface IItemTouchHelperAdapter {
        /**
         * 當item被移動時調用
         *
         * @param fromPosition 被操作的item的起點
         * @param toPosition   被操作的item的終點
         */
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    inner class MyItemTouchHelperCallback(private val mAdapter: IItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            //上下拖拽，若有其他需求同理
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            //向右側滑，若有其他需求同理
            val swipeFlags = ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            //通知Adapter更新數據和視圖
            mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            //若返回false則表示不支持上下拖拽
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            //是否可以左右側滑，默認返回true
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun isLongPressDragEnabled(): Boolean {
            //禁止長按item可以上下拖拽，因為我們要自定義開啟拖拽的時機
            return false
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                //不為空閒狀態，即為拖拽或側滑狀態
                if (viewHolder is IItemTouchHelperViewHolder) {
                    val itemTouchHelperViewHolder = viewHolder as IItemTouchHelperViewHolder
                    itemTouchHelperViewHolder.onItemSelected()
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is IItemTouchHelperViewHolder) {
                val itemTouchHelperViewHolder = viewHolder as IItemTouchHelperViewHolder
                itemTouchHelperViewHolder.onItemClear()
            }
        }
    }

    interface OnStartDragListener {
        /**
         * 當View需要拖拽時回調
         *
         * @param viewHolder The holder of view to drag
         */
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder?)
    }

    interface IItemTouchHelperViewHolder {
        /**
         * item被選中，在側滑或拖拽過程中更新狀態
         */
        fun onItemSelected()

        /**
         * item的拖拽或側滑結束，恢復默認的狀態
         */
        fun onItemClear()
    }
}