package com.example.pccu.Page.Cwb

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pccu.About.About_BottomSheet
import com.example.pccu.Page.Cwb.CwbSource.CWB_ImageSource_Url
import com.example.pccu.Page.Live_Image.CameraItem
import com.example.pccu.Page.Live_Image.CameraUrls
import com.example.pccu.Page.Live_Image.UrlSource
import com.example.pccu.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cwb_image_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * CWB氣象資料 圖資頁面 頁面建構類 : "Fragment(cwb_image_page)"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class Cwb_Image_Page: Fragment(R.layout.cwb_image_page) {

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    fun CreateImages(){
        //取得螢幕寬度
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)
        val vWidth = dm.widthPixels

        /**初始化即時影像視圖
         * @return [CameraItem] : 即時影像控件編號資料
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun initCameraItem(urlIndex: Int): CWB_ImageItem{
            //即時影像控件初始化 -編號資料-
            val Item = CWB_ImageItem(
                TextView(this.context!!),
                ImageView(this.context!!)
            )

            //標題控件 設定
            Item.title.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    100
                )
            )
            Item.title.setTextColor(Color.parseColor("#D7FFFFFF"))
            Item.title.setBackgroundColor(Color.parseColor("#4FB6D5"))
            Item.title.setPadding(70, 10, 0, 10)
            Item.title.textSize = 18F
            Item.title.text = CWB_ImageSource_Url[urlIndex].title

            //影像比例 設定
            Item.imageView.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    vWidth
                )
            )

            //即時影像元件組 -影像-
            Picasso.get() //Picasso影像取得插件
                .load(CWB_ImageSource_Url[urlIndex].imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
                .placeholder(R.drawable.loding_white) //加載中顯示圖片 (上一張即時影像)
                .error(R.drawable.no_picture) //連線錯誤顯示圖片
                .into(Item.imageView) //匯入視圖

            //即時影像視圖組 設定
            val ImageItem = LinearLayout(this.context!!)
            ImageItem.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            ImageItem.orientation = LinearLayout.VERTICAL

            //將控件綁定於視圖組上
            ImageItem.addView(Item.title)
            ImageItem.addView(Item.imageView)

            ImageItem.setOnClickListener{
                val uri = Uri.parse(CWB_ImageSource_Url[urlIndex].imageUrl)
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }

            //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
            view!!.findViewById<LinearLayout>(R.id.Cwb_Images)
                .addView(ImageItem)

            //回傳 影像控件 -編號資料-
            return Item
        }

        for( i in CWB_ImageSource_Url.indices){
            initCameraItem(i)
        }
    }

    /**
     * bus_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * 設置關於按鈕功能
         * @author KILNETA
         * @since Alpha_4.0
         */
        fun setAboutButton(){
            val context = arrayOf(
                "提醒：",
                "　　點擊影像圖片即可連接至該影像之網站。",
                "　　氣象資料若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
                "",
                "資料來源：",
                "　　交通部中央氣象局"
            )

            val Button = view.findViewById<Button>(R.id.aboutButton)
            Button.setOnClickListener{
                val FastLinkSheetFragment = About_BottomSheet(context)
                FastLinkSheetFragment.show(parentFragmentManager, FastLinkSheetFragment.tag)
            }
        }

        super.onViewCreated(view, savedInstanceState) //創建頁面
        //設置關於按鈕功能
        setAboutButton()
        //創建圖資視圖
        CreateImages()
    }


}