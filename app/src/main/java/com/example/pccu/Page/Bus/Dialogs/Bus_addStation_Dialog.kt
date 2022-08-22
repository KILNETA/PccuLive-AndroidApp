package com.example.pccu.Page.Bus.Dialogs

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.pccu.Internet.SaveBusList
import com.example.pccu.Internet.SaveStation
import com.example.pccu.R
import com.example.pccu.Shared_Functions.Object_SharedPreferences
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bus_dialog_station_addition_item.view.*


class Bus_addStation_Dialog : DialogFragment(R.layout.bus_dialog) {

    var CollectList : ArrayList<SaveBusList>? = null
    var saveStation : SaveStation? = null
    val onChecks = arrayListOf<Boolean>()

    //初始化 列表適配器
    var adapter = Adapter()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

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

        saveStation = getArguments()!!.getSerializable("saveStation") as SaveStation  //拆包裹

        dialogName.text = "選擇站牌收藏群組"

        for(i in CollectList!!.indices) {
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

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT)


        val buttonCancel = Button(context!!)
        buttonCancel.setLayoutParams(params)
        buttonCancel.text = "取消"
        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonView.addView(buttonCancel)

        val buttonOk = Button(context!!)
        buttonOk.setLayoutParams(params)
        buttonOk.text = "確認"
        buttonOk.setOnClickListener {
            onChecks.forEachIndexed  { index, it ->
                GlobalScope.launch ( Dispatchers.Main ) {
                    if(it) {
                        if(CollectList!![index].SaveStationList.any { it.StationUID == saveStation!!.StationUID && it.Direction == saveStation!!.Direction }){
                            val toast: Toast =
                                Toast.makeText(context!!, "${CollectList!![index].ListName} 已存在此站牌", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }
                        else {
                            CollectList!![index].SaveStationList.add(saveStation!!)
                            val toast: Toast =
                                Toast.makeText(context!!, "成功添加至 ${CollectList!![index].ListName}", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }
                    }
                    withContext(Dispatchers.IO) {
                        Object_SharedPreferences.save(
                            "Bus",
                            "Collects",
                            CollectList!!,
                            context!!
                        )
                    }
                    dismiss()
                }
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
                LayoutInflater.from(context).inflate(R.layout.bus_dialog_station_addition_item, parent, false)
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
            val Group = CollectList!![position]
            holder.itemView.GroupName.text = Group.ListName

            holder.itemView.bus_dialog_station_addition_item.setOnClickListener {
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