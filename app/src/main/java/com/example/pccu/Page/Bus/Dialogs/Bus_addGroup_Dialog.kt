package com.example.pccu.Page.Bus.Dialogs

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.pccu.Internet.SaveBusList
import com.example.pccu.R
import com.example.pccu.Shared_Functions.Object_SharedPreferences
import kotlinx.android.synthetic.main.bus_dialog.*

class Bus_addGroup_Dialog (
    val showContext : String,
    val listener : PriorityListener,
    val originalGroupName : String? = null
) : DialogFragment(R.layout.bus_dialog) {

    var CollectList : ArrayList<SaveBusList>? = null

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


    /**
     * 自定义Dialog监听器
     */
    interface PriorityListener {
        /**
         * 回调函数，用于在Dialog的监听事件触发后刷新Activity的UI显示
         */
        fun setActivityText(string: String?)
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

        dialogName.text = showContext

        val editText = EditText(context!!)
        editText.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        editText.isSingleLine = true
        editText.inputType = InputType.TYPE_CLASS_TEXT
        if(originalGroupName!=null){
            editText.setText(originalGroupName)
        }
        listView.addView(editText)


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
            if(CollectList!!.any { it.ListName == editText.text.toString() }) {
                val toast: Toast =
                    Toast.makeText(parentFragment!!.context!!, "已有相同群組存在", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
            else {
                if (editText.text != null) {
                    listener.setActivityText(editText.text.toString())
                    dismiss()
                }
            }
        }
        buttonView.addView(buttonOk)
    }
}