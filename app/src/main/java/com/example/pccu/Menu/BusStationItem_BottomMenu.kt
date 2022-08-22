package com.example.pccu.Menu

import android.os.Bundle
import android.view.*
import com.example.pccu.Internet.BusRoute
import com.example.pccu.Internet.NameType
import com.example.pccu.Internet.SaveStation
import com.example.pccu.Page.Bus.Dialogs.Bus_addStation_Dialog
import com.example.pccu.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bus_route_item_menu.*

/**
 *  公告底部彈窗介面 建構類 : "BottomSheetDialogFragment"
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
class BusStationItem_BottomMenu(
    val RouteData : BusRoute,
    val StationUID: String,
    val StationName: NameType,
    val Direction: Int
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BusStationName.text = StationName.Zh_tw
        BusStationItem_Menu_Collect.setOnClickListener{
            val saveStation = SaveStation(
                RouteData,
                if(Direction==0) RouteData.DestinationStopNameZh else RouteData.DepartureStopNameZh,
                StationUID,
                StationName,
                Direction
            )

            val args = Bundle()
            val addStation_Dialog = Bus_addStation_Dialog()
            args.putSerializable("saveStation", saveStation);
            addStation_Dialog.setArguments(args)
            addStation_Dialog.show(parentFragmentManager,"addStation_Dialog")

            super.onDismiss(dialog!!)
        }
    }
}