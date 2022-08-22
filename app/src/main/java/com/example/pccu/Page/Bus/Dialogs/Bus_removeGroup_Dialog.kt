package com.example.pccu.Page.Bus.Dialogs

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.Internet.SaveBusList
import com.example.pccu.R
import com.example.pccu.Shared_Functions.Object_SharedPreferences
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.android.synthetic.main.bus_dialog_group_remove_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Bus_removeGroup_Dialog (
    val listener : PriorityListener
): DialogFragment(R.layout.bus_dialog) {

    var CollectList : ArrayList<SaveBusList>? = null
    val onChecks = arrayListOf<Boolean>()

    //初始化 列表適配器
    var adapter = Adapter()

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
            context!!) as ArrayList<SaveBusList>

        dialogName.text = "選擇移除的群組"

        for(i in CollectList!!.iterator()) {
            onChecks.add(false)
        }

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
            if(onChecks.any{it}) {
                val respond = Bus_removeGroup_CheckDialog(
                    object : Bus_removeGroup_CheckDialog.PriorityListener {
                        override fun respond(respond: Boolean?) {
                            var removeGroupNum = 0
                            if(respond == true){
                                for (i in CollectList!!.indices.reversed()) {
                                    if (onChecks[i] && CollectList!![i].canLost) {
                                        CollectList!!.removeAt(i)
                                        removeGroupNum++
                                    }
                                }
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
                                        Toast.makeText(parentFragment!!.context!!, "已刪除 $removeGroupNum 個群組", Toast.LENGTH_SHORT)
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()

                                    listener.respond(true)

                                    dismiss()
                                }
                            }
                        }
                    }
                )
                val args = Bundle()
                respond.setArguments(args)
                respond.show(childFragmentManager,"respond")
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
    inner class Adapter : RecyclerView.Adapter<MyViewHolder>() {

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
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_group_remove_item, parent, false)
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
            return CollectList!!.size
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

            holder.itemView.GroupName.text = CollectList!![position].ListName
            if (!CollectList!![position].canLost){
                holder.itemView.GroupName.setTextColor(Color.parseColor("#BABABA"))
                holder.itemView.check.buttonTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            }
            else{
                holder.itemView.bus_dialog_group_remove_item.setOnClickListener {
                    if( holder.itemView.check.isChecked == false){
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

    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
}