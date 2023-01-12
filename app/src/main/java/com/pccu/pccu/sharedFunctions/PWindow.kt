package com.pccu.pccu.sharedFunctions

import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

/**
 * 自定義 PopupWindow 建構 Class
 * 繼承 PopupWindow
 * @param popupWindowView [View] popupWindow樣式佈局
 *
 * @author KILNETA
 * @since Beta_1.2.0
 */
class PWindow(popupWindowView: View) : PopupWindow() {

    /**定位點 偏移座標*/
    private val positionOffset = Offset(0,0)

    init {
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        //加載的PopupWindow 的樣式佈局
        contentView = popupWindowView
    }

    /**
     * 測量視圖規格
     * @param measureSpec [Int] 被測量規格
     * @return 規格長度 : [Int]
     *
     * @author KILNETA
     * @since Beta_1.2.0
     */
    private fun measureSpec(measureSpec: Int): Int {
        /**測量模式*/
        val mode: Int =
            if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
                View.MeasureSpec.UNSPECIFIED
            } else {
                View.MeasureSpec.EXACTLY
            }
        //返回測量值
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }

    /**
     * 顯示 PopupWindow
     * @param positionView      [View] 定位的視圖
     * @param positionMode      [Int] 定位點 (0:左下 1:右下 2:左上 3:右上)
     * @param horizontalMode    [Int] 水平朝向 (0:右 1:左)
     * @param verticalMode      [Int] 垂直朝向 (0:下 1:上)
     *
     * @author KILNETA
     * @since Beta_1.2.0
     */
    fun showDropDown(positionView:View,positionMode:Int,horizontalMode:Int,verticalMode:Int){
        //測量PopupWindow長、寬
        this.contentView.measure(
            measureSpec(width),
            measureSpec(height)
        )

        //計算顯示方位與朝向
        positionPoint(positionView,positionMode)
        itemToward(horizontalMode,verticalMode)

        //依據 positionView 顯示 PopupWindow
        this.showAsDropDown(
            positionView,
            positionOffset.offsetX,
            positionOffset.offsetY
        )
    }

    /**
     * 計算定位點座標
     * @param positionView 定位的視圖
     * @param positionMode [Int] 定位點 (0:左下 1:右下 2:左上 3:右上)
     *
     * @author KILNETA
     * @since Beta_1.2.0
     */
    private fun positionPoint(positionView:View,positionMode:Int){
        when(positionMode){
        //  0-> 不需要更改
            1->
                positionOffset.offsetX +=
                    positionView.width
            2->
                positionOffset.offsetY +=
                    -(positionView.height + this.contentView.height)
            3-> {
                positionOffset.offsetX +=
                    positionView.width
                positionOffset.offsetY +=
                    -(positionView.height + this.contentView.height)
            }
        }
    }

    /**
     * 計算朝向座標
     * @param horizontalMode    [Int] 水平朝向 (0:右 1:左)
     * @param verticalMode      [Int] 垂直朝向 (0:下 1:上)
     *
     * @author KILNETA
     * @since Beta_1.2.0
     */
    private fun itemToward(horizontalMode:Int,verticalMode:Int){
        when(horizontalMode) {
        //  0-> 不需要更改
            1->
                positionOffset.offsetX +=
                    -this.contentView.measuredWidth
        }
        when(verticalMode) {
        //  0-> 不需要更改
            1->
                positionOffset.offsetY +=
                    -this.contentView.measuredHeight
        }
    }

    /**
     * 偏移座標 資料結構
     * @author KILNETA
     * @since Beta_1.2.0
     */
    data class Offset (
        var offsetX :Int,
        var offsetY :Int
    )
}