package com.pccu.pccu.page.bus.dialogs

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.pccu.pccu.internet.CollectGroup
import com.pccu.pccu.R
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import com.pccu.pccu.sharedFunctions.PToast
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.coroutines.*

/**
 * 站牌收藏群組 名稱輸入框
 * @param mode              [Int] 對話框功能 (0:更改群組名 1:新建群組)
 * @param listener          [PToast.Listener] 回傳函式
 * @param originalGroupName [String] 要改變的群組名 (可為空)
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusEditGroupNameDialog(
    /**對話框功能標題*/
    private val mode : Int,
    private val listener: PToast.Listener,
    /**要改變的群組名 (可為空)*/
    private val originalGroupName : String? = null
) : DialogFragment(R.layout.bus_dialog) {

    /**站牌收藏群組*/
    private var collectList : ArrayList<CollectGroup>? = null
    /**輸入框控件*/
    private var editText : EditText? = null

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
    private fun initEditText(){
        editText = EditText(requireContext())
        editText!!.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //設置為單行
        editText!!.isSingleLine = true
        //設置輸入模式 (類文本)
        editText!!.inputType = InputType.TYPE_CLASS_TEXT
        //若為修改群組名稱 則讓輸入框顯示原先群組名稱
        originalGroupName?.let{
            editText!!.setText(it)
        }
        //載入視圖
        this.view?.findViewById<LinearLayout>(R.id.listView)?.addView(editText)
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
        buttonConfirm.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //設置按鈕文字
        buttonConfirm.text = "確認"
        //當按鈕被按下
        buttonConfirm.setOnClickListener {
            when {
                editText?.text == null ->
                    //輸入為空
                    PToast.popShortHint(requireParentFragment().requireContext(),"輸入不能為空")

                collectList!!.any { it.GroupName == editText?.text.toString() } ->
                    //已有相同群組存在
                    PToast.popShortHint(requireParentFragment().requireContext(),"已有相同群組存在")

                else -> {
                    val newGroupName = editText?.text.toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        when(mode){
                            //更改群組名
                            0 -> collectList!!.first{ it.GroupName == originalGroupName }
                                .GroupName = newGroupName
                            //新建群組
                            1 -> collectList!!.add(
                                CollectGroup(newGroupName, true, arrayListOf()))
                        }
                        //協程存儲修改的資料
                        withContext(Dispatchers.IO) {
                            Object_SharedPreferences.save(
                                "Bus",
                                "Collects",
                                collectList!!,
                                requireContext()
                            )
                        }

                        //提示彈窗
                        PToast.popShortHint(
                            requireActivity().baseContext,
                            when(mode){
                                0 -> "群組名稱 ${editText?.text} 更改成功"
                                1 -> "新建群組 ${editText?.text}"
                                else -> ""
                            }
                        )
                        //回應父輩
                        listener.respond(when(mode){
                            0 -> true
                            1 -> false
                            else -> false
                        })
                        //關閉彈窗
                        dismiss()
        }   }   }   }
        //載入視圖
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
        this.view?.findViewById<TextView>(R.id.dialogName)?.text = when(mode){
            0 -> "更改群組名"
            1 -> "新建群組"
            else -> null
        }

        //取得收藏群組列表
        @Suppress("UNCHECKED_CAST")
        collectList = Object_SharedPreferences["Bus", "Collects", requireContext()] as ArrayList<CollectGroup>

        //初始化彈窗大小
        initWindowsSize()
        //初始化輸入框
        initEditText()
        //初始化取消按鈕
        initCancelButton()
        //初始化確認按鈕
        initConfirmButton()
    }
}