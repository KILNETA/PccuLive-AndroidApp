package com.example.pccu.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.pccu.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 提示關於彈窗介面 建構類 : "BottomSheetDialogFragment"
 *
 * @author KILNETA
 * @since Alpha_3.0
 */
class AboutBottomSheet(
    private val aboutContext: Array<String>
) : BottomSheetDialogFragment() {

    /**
     * 重構 創建視圖
     * @param inflater [LayoutInflater] 打氣機
     * @param container [ViewGroup] 容器
     * @param savedInstanceState [Bundle] 已保存實例狀態
     * @return 視圖 : [View]
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    // inflater 類似於findViewById()。
    // 不同點是LayoutInflater是用來找res/layout/下的xml佈局文件，並且實例化；
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 膨脹這個片段的佈局
        return inflater.inflate(R.layout.about_view, container, false)
    }

    /**
     * 關於彈窗介面 頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //取得關於內容區塊
        val aboutView = view.findViewById<LinearLayout>(R.id.about_context)

        //依序輸入關於文字內容
        for(i in aboutContext.indices){
            //創立文字控件
            val textView = TextView(this.context)
            //字體大小16
            textView.textSize = 16f
            //輸入文字內容
            textView.text = aboutContext[i]
            //放入關於內容區塊
            aboutView.addView(textView)
        }
    }
}