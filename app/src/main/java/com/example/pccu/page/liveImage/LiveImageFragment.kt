package com.example.pccu.page.liveImage

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pccu.R
import com.example.pccu.internet.CameraItem
import com.example.pccu.internet.CameraSourceData
import com.example.pccu.internet.CameraAPI
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.live_image_fragment.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * 即時影像 影像分頁-子頁面 頁面建構類 : "Fragment(live_image_fragment)"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class LiveImageFragment (
    private val liveImageSources: CameraSourceData
)  : Fragment(R.layout.live_image_fragment) {

    /**首頁影片視圖表*/
    private val cameraList: ArrayList<CameraItem> = arrayListOf()

    /**初始化即時影像視圖
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun initCameraItem() {
        /**顯示指標*/
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity!!.display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            activity!!.windowManager.defaultDisplay.getMetrics(outMetrics)
        }
        /**螢幕寬度 (用於計算影像長寬比)*/
        val vWidth = outMetrics.widthPixels

        for(i in liveImageSources.Data.indices) {

            /**即時影像控件初始化 -編號資料-*/
            val cameraItem = CameraItem(
                TextView(this.context!!),
                ImageView(this.context!!)
            )

            //標題控件 設定
            cameraItem.title.setPadding(70, 10, 0, 10)
            cameraItem.title.textSize = 18F
            cameraItem.title.setBackgroundColor(Color.parseColor("#7EBCA8"))

            //影像控件 設定
            cameraItem.imageView.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    vWidth / 16 * 9
                )

            /**即時影像視圖組*/
            val cameraView = LinearLayout(this.context!!)
            cameraView.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            //垂直布局
            cameraView.orientation = LinearLayout.VERTICAL

            //將控件綁定於視圖組上
            cameraView.addView(cameraItem.title)
            cameraView.addView(cameraItem.imageView)

            //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
            LiveImage_view.addView(cameraView)

            //保存 影像控件 -編號資料-
            cameraList.add(cameraItem)
            //加載影像
            loadCameraItem(liveImageSources, i)
        }
    }

    /**更新即時影像內容
     * @param periodCameras List<[CameraUrls]> 鏡頭連結資訊表
     * @param i [Int] 項目編號
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun loadCameraItem(CameraDatas : CameraSourceData, i:Int){
        //返回主線程
        GlobalScope.launch ( Dispatchers.Main ){
            //即時影像元件組 -標題-
            cameraList[i].title.text = CameraDatas.Data[i].Name //設定鏡頭名稱

            //即時影像元件組 -影像-
            Picasso.get() //Picasso影像取得插件
                .load( CameraAPI.getClassificationUrl( CameraDatas, i,0))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
                // vvv 沒有使用更新功能
                //.placeholder(cameraItem[i].imageView.drawable) //加載中顯示圖片 (上一張即時影像)
                .placeholder(R.drawable.loding_black) //loading
                .error(R.drawable.no_image) //連線錯誤顯示圖片
                .into(cameraList[i].imageView) //匯入視圖

            //設定影像點擊 連結 瀏覽器直播影像
            cameraList[i].imageView.setOnClickListener{
                val uri = Uri.parse( CameraAPI.getClassificationUrl( CameraDatas, i,1))
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        }
    }

    /**
     * LiveImage_Fragment建構頁面
     * @param view [View] 該頁面的父類
     * @param ImageSources [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, ImageSources: Bundle?) {
        //創建頁面
        super.onViewCreated(view, ImageSources)
        //初始化即時影像控件
        initCameraItem()
    }

}