package com.example.pccu.page.bus.dialogs

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.example.pccu.R
import com.example.pccu.sharedFunctions.PopWindows
import kotlinx.android.synthetic.main.bus_dialog.*
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * 站牌收藏群組 刪除群組
 * @param listener      [PopWindows.Listener] 回傳函式
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusRemoveGroupCheckDialog(
    /**回傳函式*/
    private val listener : PopWindows.Listener
) : DialogFragment(R.layout.bus_dialog) {

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
            //回傳取消
            listener.respond(false)
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
            //回傳確認
            listener.respond(true)
            //關閉彈窗
            dismiss()
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
        dialogName.text = "群組中的站牌也將一併刪除"

        //初始化取消按鈕
        initCancelButton()
        //初始化確認按鈕
        initConfirmButton()
    }
}