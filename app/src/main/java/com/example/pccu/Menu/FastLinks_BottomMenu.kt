package com.example.pccu.Menu
import com.example.pccu.R
import android.os.Bundle

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout

/**
 *  首頁快速連結彈窗介面 建構類 : "BottomSheetDialogFragment"
 *
 * @author KILNETA
 * @since Alpha_3.0
 */
class FastLinks_BottomMenu : BottomSheetDialogFragment() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url:List<String> = listOf(
            "https://icas.pccu.edu.tw/cfp/#my",
            "https://ecampus.pccu.edu.tw/ecampus/default.aspx?usertype=student",
            "https://www.pccu.edu.tw/",
            "https://mycourse.pccu.edu.tw/",
            "https://www.pccu.edu.tw/fever/fever.html"
        )
        var uri: Uri

        view.findViewById<LinearLayout>(R.id.FastLink_Course_assistance_system)
            .setOnClickListener{
                uri = Uri.parse(url[0])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Student_area)
            .setOnClickListener{
                uri = Uri.parse(url[1])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Pccu_page)
            .setOnClickListener{
                uri = Uri.parse(url[2])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Course_selection)
            .setOnClickListener{
                uri = Uri.parse(url[3])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Epidemic_Prevention_Zone)
            .setOnClickListener{
                uri = Uri.parse(url[4])
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
    }
}