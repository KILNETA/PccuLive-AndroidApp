package com.example.pccu.Page.Live_Image

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pccu.Page.Live_Image.UrlSource.getClassificationUrl
import com.example.pccu.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*

/**
 * 用於調用的資料組 (數據包) : (Serializable 可序列化)
 * @param LiveImageSources [CameraSourceData] 即時影像來源表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class ImageSources_Data(LiveImageSources: CameraSourceData) : Serializable {
    //轉存引入的資料至本地
    val LiveImageSources: CameraSourceData = LiveImageSources //即時影像來源表

    /**調取即時影像來源表*/
    fun GetLiveImageSource() : CameraSourceData{
        return LiveImageSources
    }
}

/**
 * 即時影像 影像分頁-子頁面 頁面建構類 : "Fragment(live_image_fragment)"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class LiveImage_Fragment  : Fragment(R.layout.live_image_fragment) {

    var LiveImageSources: CameraSourceData? = null //即時影像來源

    /**
     * 設置參數 (導入頁面已存儲實例狀態)
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
    }

    /**
     * 設置新實例 "伴生對象" 用於轉存欲傳入的資料
     * @author KILNETA
     * @since Alpha_2.0
     */
    companion object {
        fun newInstance( LiveImageSources: CameraSourceData): LiveImage_Fragment {
            val Bundle = Bundle()
            val Sources = ImageSources_Data(LiveImageSources) //打包資料組 (以備調用) { 即時影像來源 }
            Bundle.putSerializable("LiveImageSources", Sources) //即時影像來源
            return LiveImage_Fragment().apply{
                arguments = Bundle //回傳已建置的資料組 (數據包)
            }
        }
    }

    /**
     * LiveImage_Fragment頁面預建構 "先導入資料"
     * @param ImageSources [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun onCreate(ImageSources: Bundle?) {
        super.onCreate(ImageSources) //ImageSources 已保存實例狀態
        val args = getArguments() //導入 已建置的資料組 (數據包)
        if (args != null) {
            val Sources =
                args.getSerializable("LiveImageSources") as ImageSources_Data //反序列化 資料組
            LiveImageSources = Sources.GetLiveImageSource()
        }
    }

    /**
     * bus_list_page建構頁面
     * @param view [View] 該頁面的父類
     * @param ImageSources [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onViewCreated(view: View, ImageSources: Bundle?) {
        /**
         * 設置即時影像
         *
         * @author KILNETA
         * @since Alpha_2.0
         */
        fun setCameras(cameraItem: ArrayList<CameraItem>){
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
            fun initCameraItem(): CameraItem {
                //即時影像控件初始化 -編號資料-
                val Item = CameraItem(
                    TextView(this.context!!),
                    ImageView(this.context!!)
                )
                Item.imageView.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                Item.titel.setPadding(70, 10, 0, 10)
                Item.titel.textSize = 18F
                Item.titel.setBackgroundColor(Color.parseColor("#7EBCA8"))

                //影像控件 設定
                Item.imageView.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        vWidth / 16 * 9
                    )
                )

                //即時影像視圖組 設定
                val CameraItem = LinearLayout(this.context!!)
                CameraItem.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                CameraItem.orientation = LinearLayout.VERTICAL

                //將控件綁定於視圖組上
                CameraItem.addView(Item.titel)
                CameraItem.addView(Item.imageView)

                //將視圖組綁定於即時影像區上 (顯示於Home_Page上)
                view.findViewById<LinearLayout>(R.id.LiveImage_view)
                    .addView(CameraItem)

                //回傳 影像控件 -編號資料-
                return Item
            }
            /**更新即時影像內容
             * @param periodCameras List<[CameraUrls]> 鏡頭連結資訊表
             * @param i [Int] 項目編號
             *
             * @author KILNETA
             * @since Alpha_2.0
             */
            fun loadCameraItem(periodCameras : CameraSourceData , i:Int){
                //返回主線程
                GlobalScope.launch ( Dispatchers.Main ){
                    //即時影像元件組 -標題-
                    cameraItem[i].titel.text = periodCameras.Data[i].Name //設定鏡頭名稱

                    //即時影像元件組 -影像-
                    Picasso.get() //Picasso影像取得插件
                        .load( getClassificationUrl( periodCameras, i,0))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不使用暫存影像
                        .into(cameraItem[i].imageView) //匯入視圖

                    //設定影像點擊 連結 瀏覽器直播影像
                    cameraItem[i].imageView.setOnClickListener{
                        val uri = Uri.parse( getClassificationUrl( periodCameras, i,1))
                        startActivity(Intent(Intent.ACTION_VIEW, uri!!))
                    }
                }
            }

            //更新即時影像
            for(i in 0 until LiveImageSources!!.Data.size) {
                //如果影像還沒初始化 -> 執行初始化
                if( i+1 > cameraItem.size)
                    cameraItem.add(initCameraItem())
                //更新即時影像內容
                loadCameraItem(LiveImageSources!!, i)
            }
        }

        //創建頁面
        super.onViewCreated(view, ImageSources)

        //初始化 首頁影片
        val CameraItem : ArrayList<CameraItem> = arrayListOf()

        //設置即時影像
        setCameras(CameraItem)
    }

}