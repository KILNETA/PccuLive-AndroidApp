package com.pccu.pccu.sharedFunctions

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

/**
 * ScrollView控件重載
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
class SV : ScrollView {
    /**可否滑動*/
    var scroll = true

    /**建構者*/
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    /**
     * ScrollView onTouchEvent 重載
     * @param ev [MotionEvent] 動作事件
     * @return  事件值 : [Boolean]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        //是否可以滑動
        return if (scroll) {
            super.onTouchEvent(ev)
        } else {
            true
        }
    }
}