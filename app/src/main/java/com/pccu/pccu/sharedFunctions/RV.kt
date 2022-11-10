package com.pccu.pccu.sharedFunctions

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
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
    open class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    /**
     * OnStartDragListener (啟動拖動監聽器)
     * @author KILNETA
     * @since Alpha_5.0
     */
    interface OnSDL {
        /**
         * 當View需要拖拽時回調
         * @param viewHolder The holder of view to drag
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder?)
    }

    /**
     * ItemTouchHelperAdapter (項目觸摸助手適配器)
     * @author KILNETA
     * @since Alpha_5.0
     */
    interface ITHA {
        /**
         * 當item被移動時調用
         * @param fromPosition 被操作的item的起點
         * @param toPosition   被操作的item的終點
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    /**
     * ItemTouchHelperViewHolder (項目觸控助手視圖支架)
     * @author KILNETA
     * @since Alpha_5.0
     */
    interface ITHVH {
        /**
         * item被選中，在側滑或拖拽過程中更新狀態
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun onItemSelected()
        /**
         * item的拖拽或側滑結束，恢復默認的狀態
         * @author KILNETA
         * @since Alpha_5.0
         */
        fun onItemClear()
    }

    /**
     * ItemTouchHelperCallback (項目觸摸助手回調)
     * @author KILNETA
     * @since Alpha_5.0
     */
    class ITHC(private val mAdapter: ITHA) : ItemTouchHelper.Callback() {

        /**
         * 獲取移動標誌
         * @param recyclerView [RecyclerView]           列表控件
         * @param viewHolder [RecyclerView.ViewHolder]  列表控件.視圖控制者
         * @return 獲取移動標誌 : [Int]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            //上下拖拽，若有其他需求同理
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            //向右側滑，若有其他需求同理
            val swipeFlags = ItemTouchHelper.RIGHT
            //回傳 獲取的移動標誌
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        /**
         * 移動(上下)
         * @param recyclerView [RecyclerView]           列表控件
         * @param viewHolder [RecyclerView.ViewHolder]  列表控件.視圖控制者
         * @param target [RecyclerView.ViewHolder]      列表控件.目標
         * @return 許可移動 : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            //通知Adapter更新數據和視圖
            mAdapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            //若返回false則表示不支持上下拖拽
            return true
        }

        /**
         * 是否啟用了項目視圖滑動
         * @return 許可移動 : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun isItemViewSwipeEnabled(): Boolean {
            //是否可以左右側滑，默認返回true
            return false
        }

        /**
         * 刷動(左右)
         * @param viewHolder [RecyclerView.ViewHolder]  列表控件.視圖控制者
         * @param direction [Int]                       方向
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        /**
         * 是否啟用長按拖動
         * @return 許可長按拖動 : [Boolean]
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun isLongPressDragEnabled(): Boolean {
            //禁止長按item可以上下拖拽，因為我們要自定義開啟拖拽的時機
            return false
        }

        /**
         * 在選定的更改
         * @param viewHolder [RecyclerView.ViewHolder]  列表控件.視圖控制者
         * @param actionState [Int]                     動作狀態
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                //不為空閒狀態，即為拖拽或側滑狀態
                if (viewHolder is ITHVH) {
                    val itemTouchHelperViewHolder = viewHolder as ITHVH
                    itemTouchHelperViewHolder.onItemSelected()
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        /**
         * 清除視圖
         * @param recyclerView [RecyclerView]           列表控件
         * @param viewHolder [RecyclerView.ViewHolder]  列表控件.視圖控制者
         *
         * @author KILNETA
         * @since Alpha_5.0
         */
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is ITHVH) {
                val itemTouchHelperViewHolder = viewHolder as ITHVH
                itemTouchHelperViewHolder.onItemClear()
            }
        }
    }
}