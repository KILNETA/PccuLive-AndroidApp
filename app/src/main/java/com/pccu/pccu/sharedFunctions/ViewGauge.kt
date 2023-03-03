package com.pccu.pccu.sharedFunctions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.course_evaluate_courses_list_item.view.*

/**
 * 視圖計算
 * @author KILNETA
 * @since Alpha_5.0
 */
object ViewGauge {

    /**
     * Px 轉 Dp
     * @param context [Context]
     * @param px [Float] Px長度
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun PX_DP(context:Context, px:Float) : Float {
        val density = context.resources.displayMetrics.density
        return px / density
    }

    /**
     * Dp 轉 Px
     * @param context [Context]
     * @param dp [Float] Dp長度
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun DP_PX(context:Context, dp:Float) : Float {
        val density = context.resources.displayMetrics.density
        return dp * density
    }

    /**
     * 取得螢幕寬度
     * @param activity [Activity] 活動
     * @return 螢幕寬度 : [Point]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun getScreenSize( activity:Activity ): Point {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width: Int
        val height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val windowInsets: WindowInsets = windowMetrics.windowInsets

            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom

            val b = windowMetrics.bounds
            width = b.width() - insetsWidth
            height = b.height() - insetsHeight
        } else {
            val size = Point()
            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay // deprecated in API 30
            @Suppress("DEPRECATION")
            display?.getSize(size) // deprecated in API 30
            width = size.x
            height = size.y
        }
        return Point(width,height)
    }

    /**
     * 取得螢幕寬度
     * @param activity [Activity] 活動
     * @return 螢幕寬度 : [Int]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun getDisplayWidth( activity:Activity ): Int {
        /**顯示指標*/
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            activity.display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
        }
        /**螢幕寬度 (用於計算影像長寬比)*/
        return outMetrics.widthPixels
    }

    /**
     * 選單 收合、展開動畫(上下)
     * @param action            [Boolean] 0:收合 1:展開
     * @param parentView        [View] 父類視圖
     * @param view              [View] 目標視圖
     * @param time              [Int] 動畫時間
     * @param startListener     [function] 動畫開始觸發函式
     * @param endListener       [function] 動畫結束觸發函式
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun changeExpandCollapse(
        action:Boolean,
        parentView: View,
        view: View,
        time: Long,
        startListener: (() -> Unit)?,
        endListener: (() -> Unit)?
    ) {
        //使用measure對控件進行預建構 用以取得布局大小(取長寬)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(parentView.width, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST),
        )
        /**動畫器 (輸入數值)*/
        val animator = ValueAnimator.ofInt(
            if(action) 0 else view.measuredHeight,
            if(action) view.measuredHeight else 0
        )
        //設置目標
        animator.setTarget(view)
        //動作設置
        animator.addUpdateListener { animation ->
            //依照數值變化 進行垂直方向高度伸縮
            view.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    animation.animatedValue as Int
                )
        }

        //動畫開始觸發函式
        startListener?.let{
            //設置事件監聽器
            animator.addListener(object : AnimatorListenerAdapter() {
                /**當動畫結束*/
                override fun onAnimationEnd(animation: Animator) {
                    //還原頁面滑動
                    it()
                }
            })
        }
        //動畫結束觸發函式
        endListener?.let{
            //設置事件監聽器
            animator.addListener(object : AnimatorListenerAdapter() {
                /**當動畫結束*/
                override fun onAnimationEnd(animation: Animator) {
                    //還原頁面滑動
                    it()
                }
            })
        }

        //開始動畫 時間
        animator.setDuration(time).start()
    }
}