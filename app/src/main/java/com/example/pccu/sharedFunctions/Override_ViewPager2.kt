package com.example.pccu.sharedFunctions

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * 設置 CWB氣象展示頁面偏移量 適配器
 * @param offsetPx [Int] 偏移量
 * @param pageMarginPx [Int] 頁面邊長
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class OffsetPageTransformer(
    @Px private val offsetPx: Int,
    @Px private val pageMarginPx: Int
) : ViewPager2.PageTransformer {

    /**
     * (override) 轉換頁面
     * @param page [View] 頁面
     * @param position [Float] 座標
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    override fun transformPage(page: View, position: Float) {
        val viewPager = requireViewPager(page)
        val offset = position * -(2 * offsetPx + pageMarginPx)
        val totalMargin = offsetPx + pageMarginPx

        //viewPager 是水平滑動展示
        if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            //更新佈局參數
            page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = totalMargin   //左邊
                marginEnd = totalMargin     //右邊
            }
            //設置偏移方向 (正or負)
            page.translationX = if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                -offset
            } else {
                offset
            }
        //viewPager 是垂直滑動展示
        } else {
            page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = totalMargin     //上邊
                bottomMargin = totalMargin  //下邊
            }
            //設置偏移方向 (正)
            page.translationY = offset
        }

        //調整比例 的加權算法(當頁面非在第一位時要縮小)
        page.scaleY = 1 - (0.2f * abs(position))
        page.scaleX = 1 - (0.2f * abs(position))

        //設置視圖 (當頁面非在第一位時變暗) <未使用>
        //val startColor = ContextCompat.getColor(this, R.color.light_blue)
        //val endColor = ContextCompat.getColor(this, R.color.dark_blue)
        //val color = ColorUtils.blendARGB(startColor, endColor, abs(position))
        //val view = page.findViewById<LinearLayout>(R.id.page_root) //  這是頁面片段根視圖（確保設置其 id）
        //view?.setBackgroundColor(color)
    }

    /**
     * 檢查ViewPager
     * @param page [View] 頁面
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    private fun requireViewPager(page: View): ViewPager2 {
        val parent = page.parent
        val parentParent = parent.parent

        // 頁面視圖 == RecyclerView && 頁面視圖父類 == ViewPager2
        if (parent is RecyclerView && parentParent is ViewPager2) {
            return parentParent
        }//非法狀態 異常回報
        throw IllegalStateException(
            "Expected the page view to be managed by a ViewPager2 instance.\n" +
            "期望頁面視圖由 ViewPager2 實例管理。"
        )
    }
}