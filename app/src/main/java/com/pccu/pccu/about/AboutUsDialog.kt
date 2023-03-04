package com.pccu.pccu.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.pccu.pccu.R
import kotlinx.android.synthetic.main.about_us_dialog.*
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * App關於彈窗介面 建構類 : "DialogFragment"
 * @author KILNETA
 * @since Beta_1.3.0
 */
class AboutUsDialog : DialogFragment(R.layout.about_us_dialog) {

    /**
     * 初始化關閉按鈕
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    private fun initCloseButton(){
        //當按鈕被按下
        close_Button.setOnClickListener {
            //關閉彈窗
            dismiss()
        }
    }

    /**
     * 建構頁面
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //初始化關閉按鈕
        initCloseButton()
        //開發者Github
        developerGithub.setOnClickListener {
            startActivity(Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://github.com/KILNETA"
                )
            ))
        }
        //App官網
        officialWebsite.setOnClickListener {
            startActivity( Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://kilneta.github.io/PccuLive-AndroidApp/Introduction/index.html"
                )
            ))
        }
        //App Github開源
        GithubWebsite.setOnClickListener {
            startActivity( Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://github.com/KILNETA/PccuLive-AndroidApp"
                )
            ))
        }
        //App 隱私權保護政策
        privacyPolicyWebsite.setOnClickListener {
            startActivity( Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://kilneta.github.io/PccuLive-AndroidApp/" +
                            "%E9%9A%B1%E7%A7%81%E6%AC%8A%E4%BF%9D%E8%AD%B7%E6%94%BF%E7%AD%96/"
                )
            ))
        }

    }
}