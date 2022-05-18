package com.example.pccu.Page.Live_Image

import android.os.Bundle
import android.view.View
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


/**
 * 即時影像 主頁面 頁面建構類 : "Fragment(live_image_page)"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
class LiveImage_Page : Fragment(R.layout.live_image_page) {

    /**頁面適配器*/
    var pageAdapter : PageAdapter? = null

    /**
     * live_image_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState) //創建頁面

        /**顯示頁面控件 增加指定頁面*/
        var fragments: ArrayList<Fragment> = arrayListOf(
            LiveImage_Fragment.newInstance(MoreCameras.CameraSource[0]),   //文化
            LiveImage_Fragment.newInstance(MoreCameras.CameraSource[1]),   //仰德
            LiveImage_Fragment.newInstance(MoreCameras.CameraSource[2]),   //劍潭
            LiveImage_Fragment.newInstance(MoreCameras.CameraSource[3]),   //後山
        )

        //創建Bus頁面資料
        pageAdapter = PageAdapter(childFragmentManager, lifecycle, fragments)
        //Bus頁面 是配器
        LiveCameras_fragment.adapter = pageAdapter

        //頁面標題配置
        val title: ArrayList<String> = arrayListOf("文化", "仰德", "劍潭", "後山")
        //套用至Bus頁面的標題
        TabLayoutMediator(camera_tabs, LiveCameras_fragment) { tab, position ->
            tab.text = title[position]
        }.attach()

        val tabLayout = view.findViewById<ViewPager2>(R.id.LiveCameras_fragment)

        //重構 ViewPage2 控件的部分功能
        tabLayout.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            /**
             * 頁面被選中顯示
             * @param position [Int] 當前位置
             *
             * @author KILNETA
             * @since Alpha_2.0
             */
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val view: View? = fragments.get(position).getView()
                //如果視圖還沒被加載
                //(避免如果使用TabLayout切換頁面時APP崩潰)
                if (view != null) {
                    //重置ViewPage2控件的高度適配子視圖
                    updatePagerHeightForChild(view, tabLayout)
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
                    //測量規格 寬度
                    val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                        view.width,
                        View.MeasureSpec.EXACTLY
                    )
                    //測量規格 高度
                    val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    )
                    //取得視圖尺寸
                    view.measure(wMeasureSpec, hMeasureSpec)
                    //如果 控件高度 與 視圖高度 不符合 (重置控件高度)
                    if (pager.layoutParams.height != view.measuredHeight) {
                        val layoutParams = pager.layoutParams
                        layoutParams.height = view.measuredHeight
                        pager.layoutParams = layoutParams
                    }
                }
            }
        })
    }

    /**
     * LiveImage_Page頁面控件適配器
     * @param fragmentManager [FragmentManager] 子片段管理器
     * @param lifecycle [Lifecycle] 生命週期
     * @param fragments ArrayList<[Fragment]> 欲展視的片段視圖
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    class PageAdapter(
        fragmentManager: FragmentManager, // 子片段管理器
        lifecycle: Lifecycle, // 生命週期
        fragments: ArrayList<Fragment>
    ):  FragmentStateAdapter( // 片段狀態適配器
        fragmentManager, // 片段管理器
        lifecycle // 生命週期
    ){

        val fragments = fragments

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