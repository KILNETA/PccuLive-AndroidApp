package com.pccu.pccu.internet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

/**
 * 網絡更改接收器 -繼承 [BroadcastReceiver]
 * @param respond [RespondNetWork] 網絡響應器
 * @param context [Context] 上下文
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class NetWorkChangeReceiver(
    /**網絡響應器*/
    val respond:RespondNetWork,
    /**上下文*/
    context: Context
) : BroadcastReceiver() {

    /**是否連接網路*/
    var isConnect = false

    /**初始化參數*/
    init{
        /**連接管理器*/
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        /**當前網路狀態*/
        val network =
            connectivityManager.activeNetwork

        //判斷網路狀態
        if (network == null) {
            //網路中斷
            isConnect = false
            //觸發斷網函式 (一般來說只有控制提示方塊彈出)
            //而初始化階段不可呼叫 連網函式(可能導致錯誤操作 如:數據重複加載 而崩潰)
            respond.interruptInternet()
        } else {
            //網路正常
            isConnect = true
        }
    }

    /**
     * 接收情況
     * @param context [Context] 上下文
     * @param intent [Intent] 目標
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onReceive(context: Context, intent: Intent)  {
        /**連接管理器*/
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        /**當前網路狀態*/
        val network =
            connectivityManager.activeNetwork

        //判斷網路狀態
        if (network == null && isConnect) {
            //網路中斷
            respond.interruptInternet()
            isConnect = false
        } else if ( network != null && !isConnect ) {
            //網路正常
            respond.connectedInternet()
            isConnect = true
        }
    }

    /**
     * ItemTouchHelperViewHolder (項目觸控助手視圖支架)
     * @author KILNETA
     * @since Alpha_5.0
     */
    interface RespondNetWork {
        /**
         * 中斷網路
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun interruptInternet()
        /**
         * 連接到網路
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun connectedInternet()
    }
}