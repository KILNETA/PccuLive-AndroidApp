package com.example.pccu.sharedFunctions

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager

/**
 * 視圖計算
 * @author KILNETA
 * @since Alpha_5.0
 */
object ViewGauge {

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
}