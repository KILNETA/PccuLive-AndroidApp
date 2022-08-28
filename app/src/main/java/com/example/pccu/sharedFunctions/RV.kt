package com.example.pccu.sharedFunctions

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView控件重載
 * @author KILNETA
 * @since Alpha_5.0
 */
object RV{

    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

}