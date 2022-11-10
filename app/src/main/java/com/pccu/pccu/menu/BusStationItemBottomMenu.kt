package com.pccu.pccu.menu

import android.os.Bundle
import android.view.*
import com.pccu.pccu.internet.BusRoute
import com.pccu.pccu.internet.NameType
import com.pccu.pccu.internet.CollectStation
import com.pccu.pccu.page.bus.dialogs.BusAddStationDialog
import com.pccu.pccu.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bus_route_item_menu.*

/**
 *  站牌Item底部彈窗介面 建構類 : "BottomSheetDialogFragment"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusStationItemBottomMenu (
    /**路線資料*/
    private val RouteData : BusRoute,
    /**站牌唯一編號*/
    private val StationUID: String,
    /**站牌名稱*/
    private val StationName: NameType,
    /**站牌方向*/
    private val Direction: Int
): BottomSheetDialogFragment() {

    /**
     * 重構 創建視圖
     * @param inflater [LayoutInflater] 打氣機
     * @param container [ViewGroup] 容器
     * @param savedInstanceState [Bundle] 已保存實例狀態
     * @return 視圖 : [View]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    // inflater 類似於findViewById()。
    // 不同點是LayoutInflater是用來找res/layout/下的xml佈局文件，並且實例化；
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 膨脹這個片段的佈局
        return inflater.inflate(R.layout.bus_route_item_menu, container, false)
    }

    /**
     * 頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //標示選中的站牌名稱
        BusStationName.text = StationName.Zh_tw
        //當收藏站牌選項被選中
        BusStationItem_Menu_Collect.setOnClickListener{
            /**製作存儲用站牌資料*/
            val saveStation = CollectStation(
                RouteData,
                if(Direction==0) RouteData.DestinationStopNameZh else RouteData.DepartureStopNameZh,
                StationUID,
                StationName,
                Direction
            )

            /**傳入介面的資料包*/
            val args = Bundle()
            val addStationDialog = BusAddStationDialog()
            //傳入存儲用站牌資料
            args.putSerializable("saveStation", saveStation)
            //將資料包傳入介面
            addStationDialog.arguments = args
            //顯示添加站牌介面
            addStationDialog.show(parentFragmentManager,"addStationDialog")
            //關閉底部彈窗
            super.onDismiss(dialog!!)
        }
    }
}