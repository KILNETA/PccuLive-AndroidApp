package com.pccu.pccu.sharedFunctions

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * 螢幕中間的提示用(Toast)彈窗 Object
 * @author KILNETA
 * @since Alpha_5.0
 */
object PToast {
    /**
     * 小提示彈窗 持續3s
     * @param context [Context] 上下文
     * @param title [String]    顯示內容
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun popLongHint(context: Context, title: String){
        /**沒有站牌存在群組中 提示彈窗*/
        val toast = Toast.makeText(context, title, Toast.LENGTH_LONG)
        //設定提示彈窗位置
        toast.setGravity(Gravity.CENTER, 0, 0)
        //顯示提示彈窗
        toast.show()
    }

    /**
     * 小提示彈窗 持續2.5s
     * @param context [Context] 上下文
     * @param title [String]    顯示內容
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun popShortHint(context: Context, title: String){
        /**沒有站牌存在群組中 提示彈窗*/
        val toast = Toast.makeText(context, title, Toast.LENGTH_SHORT)
        //設定提示彈窗位置
        toast.setGravity(Gravity.CENTER, 0, 0)
        //顯示提示彈窗
        toast.show()
    }

    /**
     * 自定義Dialog監聽器
     * @author KILNETA
     * @since Alpha_5.0
     */
    interface Listener {
        /**回調函數，用於在Dialog的監聽事件觸發後刷新Activity的UI顯示*/
        fun respond(respond: Boolean?)
    }
}