package com.pccu.pccu.about

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.pccu.pccu.R
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences
import kotlinx.android.synthetic.main.update_news_dialog.*
import kotlinx.coroutines.DelicateCoroutinesApi


/**
 * App更新公告彈窗介面 建構類 : "DialogFragment"
 * @author KILNETA
 * @since Beta_1.3.0
 */
class UpdateNewsDialog : DialogFragment(R.layout.update_news_dialog) {

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

        //開啟PlayStore 評價介面
        PlayStore_evaluate.setOnClickListener {
            val manager = ReviewManagerFactory.create(requireContext())
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                } else {
                    Toast.makeText(
                        context,
                        "無法連接到 PlayStore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 頁面關閉
     * @author KILNETA
     * @since Beta_1.3.0
     */
    override fun dismiss() {
        super.dismiss()
        //確認公告已被已讀
        Object_SharedPreferences.save("News","update","true_1.3.2",requireContext())
    }
}