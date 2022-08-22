package com.example.pccu.Menu

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pccu.Page.Cwb.Cwb_Home_Page
import com.example.pccu.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.cwb_home_page.view.*
import kotlinx.android.synthetic.main.cwb_locations_list.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.example.pccu.Page.Cwb.CwbSource
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 *  CWB頁面 選擇氣象預報地區_彈窗介面 建構類 : "BottomSheetDialogFragment"
 *  @param parentView [View] 父視圖
 *  @param parent [Cwb_Home_Page] 父類別(氣象主頁)
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class MoreLocation_BottomMenu(val parentView:View, val parent: Cwb_Home_Page) : BottomSheetDialogFragment() {

    /**確保底部彈窗無法被滑動 使用之變數*/ //底頁行為
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    /**確保底部彈窗無法被滑動 使用之變數*/ //重構 底頁回調 中的函式
    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        //狀態改變
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            //禁止拖拽
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                //設置為收縮狀態
                mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        //投影片
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    //當視圖可見
    override fun onStart() {
        super.onStart()
        /**--vvv--確保底部彈窗無法被滑動--vvv--*/
        //調整當前視圖高度 = 契合全局
        view!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        //調整視圖post
        view!!.post {
            //取得父視圖
            val parent = view!!.parent as View
            //取得父視圖的 佈局參數
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            //取的父視圖佈局參數中的 狀態
            val behavior = params.behavior
            //設置底頁行為 = 父視圖.佈局參數.狀態
            mBottomSheetBehavior = behavior as BottomSheetBehavior<View>?
            //重設mBottomSheetBehavior的回調活動函式
            mBottomSheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        /**--^^^--確保底部彈窗無法被滑動--^^^--*/
    }

    /**
     * 重構 創建視圖
     * @param inflater [LayoutInflater] 打氣機
     * @param container [ViewGroup] 容器
     * @param savedInstanceState [Bundle] 已保存實例狀態
     * @return 視圖 : [View]
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**--vvv--確保底部彈窗高度等同頁面--vvv--*/
        // 膨脹這個片段的佈局
        dialog!!.setOnShowListener { dialog ->
            //取得改為BottomSheetDialog格式的對話
            val Dialog = dialog as BottomSheetDialog
            //取得底頁
            val bottomSheet = Dialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            //取得底頁的父輩 改為CoordinatorLayout格式
            val coordinatorLayout = bottomSheet!!.parent as CoordinatorLayout
            //取得底頁行為 來自(底頁)
            val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(bottomSheet)
            //底頁行為的可見高度 = 底頁高度
            bottomSheetBehavior.peekHeight = bottomSheet.height
            //底頁父輩的父輩 請求佈局
            coordinatorLayout.parent.requestLayout()
        }
        /**--^^^--確保底部彈窗高度等同頁面--^^^--*/
        // 膨脹這個片段的佈局
        return inflater.inflate(R.layout.cwb_locations_list, container, false)
    }

    /**
     * 重構 建構視圖
     * @param view [View] 視圖
     * @param savedInstanceState [Bundle] 已保存實例狀態
     *
     * @author KILNETA
     * @since Alpha_4.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //依照地區分區 依特定格式建立 選擇縣市控件
        for( j in CwbSource.CWB_locationArea.indices) {
            //區域分隔線 控件
            val textArea = TextView(this.context!!)
            textArea.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            //內邊間格
            textArea.setPadding(0, 3, 0, 3)
            //文字大小
            textArea.textSize = 16F
            //文字置中
            textArea.gravity = 1
            //文字 = 分區名
            textArea.text = CwbSource.CWB_locationArea[j]
            textArea.setBackgroundColor(Color.parseColor("#cedade"))
            Cwb_Locations_list.addView(textArea)

            //打印該分區所有縣市選項
            for (i in CwbSource.CWB_locations[j].indices) {
                //縣市選項 控件
                val textLocation = TextView(this.context!!)
                textLocation.setLayoutParams(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                //內邊間格
                textLocation.setPadding(0, 6, 0, 6)
                //文字大小
                textLocation.textSize = 20F
                //文字置中
                textLocation.gravity = 1
                //文字 = 縣市名
                textLocation.text = CwbSource.CWB_locations[j][i]

                //設置點擊觸發工作
                textLocation.setOnClickListener {
                    //存儲用戶選擇的地區
                    parent.saveTargetLocation(CwbSource.CWB_locations[j][i])
                    //更改父視圖當前縣市 //給用戶看
                    parentView.Cwb_Location.text = CwbSource.CWB_locations[j][i]
                    //更改父類別(氣象主頁)目標縣市 //給資料看
                    parent.TargetLocation = CwbSource.CWB_locations[j][i]
                    //重新設置 氣象預報 展示之數據
                    parent.resetCwbForecast()
                    //重新設置 空汙預報 展示之數據
                    parent.resetEPA_View()
                    //點選後徹底關閉這個彈窗頁面
                    super.onDismiss(dialog!!)
                }
                //新增到列表中
                Cwb_Locations_list.addView(textLocation)
            }
        }
    }
}