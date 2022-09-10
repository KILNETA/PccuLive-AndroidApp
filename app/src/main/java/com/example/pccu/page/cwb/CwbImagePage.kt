package com.example.pccu.page.cwb

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pccu.about.AboutBottomSheet
import com.example.pccu.page.cwb.CwbSource.CWB_ImageSource_Url
import com.example.pccu.R
import com.example.pccu.internet.NetWorkChangeReceiver
import com.example.pccu.sharedFunctions.ViewGauge
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cwb_image_page.*
import kotlinx.android.synthetic.main.cwb_image_page.aboutButton
import kotlinx.android.synthetic.main.cwb_image_page.noNetWork
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * CWB氣象資料 圖資頁面 頁面建構類 : "Fragment(cwb_image_page)"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class CwbImagePage: Fragment(R.layout.cwb_image_page) {
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    private val itFilter = IntentFilter()
    init {
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
    }

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                }
            },
            requireContext()
        )
    }

    /**
     * 設置關於按鈕功能
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun setAboutButton(){
        /**關於介面 內文*/
        val context = arrayOf(
            "提醒：",
            "　　點擊影像圖片即可連接至該影像之網站。",
            "　　氣象資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
            "",
            "資料來源：",
            "　　交通部中央氣象局"
        )

        //當 關於按鈕 被按下
        aboutButton.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = AboutBottomSheet(context)
            aboutSheetFragment.show(parentFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**初始化氣象圖資視圖
     * @param baseUrl [CwbImageUrl] 影像來源
     * @param vWidth [Int] 螢幕寬度
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun initCameraItem(baseUrl:CwbImageUrl, vWidth:Int){
        /**即時影像控件*/ //初始化 -編號資料-
        val pictureItem = CwbImageItem(
            TextView(this.requireContext()),
            ImageView(this.requireContext())
        )

        //標題控件 設定
        pictureItem.title.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100
            )
        //設置文字顏色
        pictureItem.title.setTextColor(Color.parseColor("#D7FFFFFF"))
        //設置文字背景顏色
        pictureItem.title.setBackgroundColor(Color.parseColor("#4FB6D5"))
        //設置內部間距
        pictureItem.title.setPadding(70, 10, 0, 10)
        //設置文字大小
        pictureItem.title.textSize = 18F
        //設置文字內容
        pictureItem.title.text = baseUrl.title

        //影像比例 設定
        pictureItem.imageView.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                vWidth
            )

        //即時影像元件組 -影像-
        Picasso.get() //Picasso影像取得插件
            .load(baseUrl.imageUrl)
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
            .placeholder(R.drawable.loding_white) //加載中顯示圖片 (上一張即時影像)
            .error(R.drawable.no_picture) //連線錯誤顯示圖片
            .into(pictureItem.imageView) //匯入視圖

        /**即時影像視圖組*/ // 設定
        val pictureView = LinearLayout(this.requireContext())
        pictureView.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        //垂直布局
        pictureView.orientation = LinearLayout.VERTICAL

        //將控件綁定於視圖組上
        pictureView.addView(pictureItem.title)
        pictureView.addView(pictureItem.imageView)

        //當控件被按下
        pictureView.setOnClickListener{
            //連接外部瀏覽器開啟圖片
            /**影像連結*/
            val uri = Uri.parse(baseUrl.imageUrl)
            startActivity(Intent(Intent.ACTION_VIEW, uri!!))
        }

        //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
        Cwb_Images.addView(pictureView)
    }

    /**
     * 初始化圖資視圖
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun initImages(){
        /**螢幕寬度 (用於計算影像長寬比)*/
        val vWidth = ViewGauge.getDisplayWidth(requireActivity())

        //將所有影像來源具象化
        CWB_ImageSource_Url.forEach {
            initCameraItem(it, vWidth)
        }
    }

    /**
     * CWB氣象資料 圖資頁面 建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //創建頁面

        //初始化網路接收器
        initInternetReceiver()
        //設置關於按鈕功能
        setAboutButton()
        //初始化圖資視圖
        initImages()
    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        activity?.unregisterReceiver(internetReceiver)
    }
}