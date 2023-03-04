package com.pccu.pccu.menu

import com.pccu.pccu.R
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.pccu.pccu.about.AboutUsDialog

/**
 *  首頁快速連結彈窗介面 建構類 : "BottomSheetDialogFragment"
 *
 * @author KILNETA
 * @since Alpha_3.0
 */
class FastLinksBottomMenu : BottomSheetDialogFragment() {

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
        return inflater.inflate(R.layout.fast_links, container, false)
    }

    /**
     * 頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**快速連結Url*/
        val url:List<String> = listOf(
            "https://docs.google.com/forms/d/e/1FAIpQLScAOJnqIZOspwZCGv0hq2zSoucugq2kiMp3NzlIZ8npGfymgg/viewform"
        )
        /**Uri*/
        var uri: Uri

        /**關 於 我 們*/
        view.findViewById<LinearLayout>(R.id.aboutUs)
            .setOnClickListener{
                val aboutUs = AboutUsDialog()
                aboutUs.setStyle(
                    DialogFragment.STYLE_NO_TITLE,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth
                )
                //顯示介面
                aboutUs.show(parentFragmentManager,"aboutUs")
                //點選後徹底關閉這個彈窗頁面
                super.onDismiss(dialog!!)
            }

        /**反 饋 問 卷*/
        view.findViewById<LinearLayout>(R.id.FastLink_Feedback_Questionnaire)
            .setOnClickListener{
                uri = Uri.parse(url[0])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                //點選後徹底關閉這個彈窗頁面
                super.onDismiss(dialog!!)
            }
    }
}