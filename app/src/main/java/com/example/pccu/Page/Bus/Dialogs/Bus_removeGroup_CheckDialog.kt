package com.example.pccu.Page.Bus.Dialogs

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.example.pccu.R
import kotlinx.android.synthetic.main.bus_dialog.*

class Bus_removeGroup_CheckDialog(
    val listener : PriorityListener
) : DialogFragment(R.layout.bus_dialog) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogName.text = "群組中的站牌也將一併刪除"

        val buttonCancel = Button(context!!)
        buttonCancel.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        buttonCancel.text = "取消"
        buttonCancel.setOnClickListener {
            listener.respond(false)
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
            listener.respond(true)
            dismiss()
        }

        buttonView.addView(buttonOk)
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
}