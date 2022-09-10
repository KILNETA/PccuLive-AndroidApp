package com.example.pccu.page.liveImage

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.pccu.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.live_image_page.*
import java.util.*
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.pccu.about.AboutBottomSheet
import com.example.pccu.internet.CameraAPI
import com.example.pccu.internet.NetWorkChangeReceiver
import kotlinx.android.synthetic.main.live_image_page.aboutButton
import kotlinx.android.synthetic.main.live_image_page.noNetWork

/**
 * 即時影像 主頁面 頁面建構類 : "AppCompatActivity(live_image_page)"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class LiveImagePage : AppCompatActivity(R.layout.live_image_page) {

    /**頁面適配器*/
    private var pageAdapter : PageAdapter? = null
    /**顯示頁面控件 增加指定頁面*/
    private val fragments: ArrayList<LiveImageFragment> = arrayListOf(
        LiveImageFragment(CameraAPI.CameraSource[0]),   //文化
        LiveImageFragment(CameraAPI.CameraSource[1]),   //仰德
        LiveImageFragment(CameraAPI.CameraSource[2]),   //劍潭
        LiveImageFragment(CameraAPI.CameraSource[3]),   //後山
    )
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

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
            baseContext
        )
        val itFilter = IntentFilter()
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 設置關於按鈕功能
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    private fun initAboutButton(){
        /**關於介面 內文*/
        val content = arrayOf(
            "提醒：",
            "　　由於文大即時影像改版，以及北市即時影像過於卡頓且僅能播放片段；且考量用戶流量問題，故不採用播放影片的方式，" +
                    "改採用即時影像圖片，並且定時刷新。",
            "　　部分文大即時影像附有互動功能，因此可能不會每次都是同一個影像角度，若須使用互動功能請至該影像之網站。",
            "　　點擊影像圖片即可連接至該影像之網站。",
            "",
            "　　即時影像若出現部分無法顯示，可能是影像來源有問題；若出現整頁無法顯示，且點擊影像區後仍無法正確連線，可聯繫程式負責方協助修正。",
            "",
            "即時影像來源：",
            "　　中國文化大學、臺北市即時交通資訊網。"
        )
        //當關於按鈕被按下 開啟關於介面 底部彈窗
        aboutButton.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = AboutBottomSheet(content)
            aboutSheetFragment.show(supportFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**
     * 重新加載 ViewPage2 控件的高度 適配子視圖
     * @param view [View] 2; 當前視圖
     * @param pager [ViewPager2] ViewPager2控件
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
        view.post {
            /**測量規格 寬度*/
            val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                view.width,
                View.MeasureSpec.EXACTLY
            )
            /**測量規格 高度*/
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            )
            //取得視圖尺寸
            view.measure(wMeasureSpec, hMeasureSpec)
            //如果 控件高度 與 視圖高度 不符合 (重置控件高度)
            if (pager.layoutParams.height != view.measuredHeight) {
                //ViewPager2佈局參數高度 = 視圖的測量高度
                val layoutParams = pager.layoutParams
                layoutParams.height = view.measuredHeight
                pager.layoutParams = layoutParams

                //不能縮寫成下面 會失效
                //pager.layoutParams.height = view.measuredHeight
            }
        }
    }

    /**
     * live_image_page頁面建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //創建頁面
        //初始化網路接收器
        initInternetReceiver()
        //初始化關於按鈕
        initAboutButton()

        //允許您定義在螢幕外呈現多少頁。 (全部頁數顯示)
        LiveCameras_fragment.offscreenPageLimit = fragments.size

        //創建Bus頁面資料
        pageAdapter = PageAdapter(supportFragmentManager, lifecycle)
        //LiveCameras頁面 適配器
        LiveCameras_fragment.adapter = pageAdapter


        //套用至LiveCameras頁面的標題
        TabLayoutMediator(camera_tabs, LiveCameras_fragment) { tab, position ->
            //Tab文字 = 即時影像分類地區
            tab.text = CameraAPI.CameraSource[position].locationName
        }.attach()
        //設置View覆蓋子類控件而直接獲得焦點 (避免ViewPage2跳轉頁面位置)
        LiveCameras_fragment.descendantFocusability = FOCUS_BLOCK_DESCENDANTS

        /** vvv 由於各頁面高度不一 功能使切換頁面會重新計算頁面高度 避免頁面空白 vvv */
        //重構 ViewPage2 控件的部分功能
        LiveCameras_fragment.registerOnPageChangeCallback(
            object : OnPageChangeCallback() {
                /**
                 * 頁面被選中 (頁面改變)
                 * @param position [Int] 當前位置
                 *
                 * @author KILNETA
                 * @since Alpha_2.0
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val view: View? = fragments[position].view
                    //如果視圖還沒被加載
                    //(避免如果使用TabLayout切換頁面時APP崩潰)
                    if (view != null) {
                        //重置ViewPage2控件的高度適配子視圖
                        updatePagerHeightForChild(view, LiveCameras_fragment)
                        Log.e("","nice")
                    }
                }
            }
        )
    }

    /**
     * 當頁面刪除時(刪除)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * LiveImage_Page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    inner class PageAdapter(
        fragmentManager: FragmentManager, // 子片段管理器
        lifecycle: Lifecycle, // 生命週期
    ):  FragmentStateAdapter( // 片段狀態適配器
        fragmentManager, // 片段管理器
        lifecycle // 生命週期
    ){
        /**頁面數量
         * @return 頁面數量 : [Int]
         */
        override fun getItemCount(): Int {
            return fragments.size
        }

        /**創建頁面
         * @param position [Int] 頁面數量
         * @return 頁面 : [Fragment]
         */
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}