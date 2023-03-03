package com.pccu.pccu.page.courseEvaluate.search

import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.page.courseEvaluate.*
import kotlinx.android.synthetic.main.course_evaluation_search_main.*
import kotlinx.android.synthetic.main.course_evaluation_search_main.loading
import kotlinx.android.synthetic.main.course_evaluation_search_main.noNetWork
import kotlinx.coroutines.*

/**
 * 課程評價 搜索頁面 主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
class CourseEvaluateSearchActivity : AppCompatActivity(R.layout.course_evaluation_search_main) {

    /**courseEvaluateList 列表適配器*/
    private var adapter:Adapter? = null
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                override fun interruptInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                override fun connectedInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                }
            },
            baseContext
        )
        val itFilter = IntentFilter()
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 課程評價 搜索頁面 主框架建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        //建構主框架
        super.onCreate(savedInstanceState)
        //初始化網路接收器
        initInternetReceiver()
        //courseEvaluateList 列表適配器初始化
        adapter = Adapter()

        //掛載 courseEvaluateList 列表適配器
        courseEvaluateList.adapter = adapter
        //列表控件 courseEvaluateList 的設置佈局管理器 (列表)
        courseEvaluateList.layoutManager =
            LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)

        //開啟搜索框焦點 (順帶啟用軟鍵盤)
        _searchView.requestFocusFromTouch()
        //設置搜索文本偵聽器
        _searchView.setOnQueryTextListener(searchViewOnQueryTextListener)
    }

    /**
     * 當頁面刪除時(刪除)
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * SearchView 搜索控件 事件重構
     * @author KILNETA
     * @since Alpha_5.0
     */
    @DelicateCoroutinesApi
    private val searchViewOnQueryTextListener = object : SearchView.OnQueryTextListener {

        /**上一次搜索目標*/
        private var lastQuery = ""
        //當 點擊搜索按鈕 時觸發該方法
        override fun onQueryTextSubmit(query: String): Boolean {
            if(!internetReceiver!!.isConnect) return false
            //如果輸入目標跟上次搜索目標一樣 則不執行搜索
            if(lastQuery == query)  return false
            //清除上次搜索結果
            clearSearchResult()

            //主線程
            GlobalScope.launch(Dispatchers.Main) {
                //啟用Loading動畫
                startLoading()
                //設定重取許可
                adapter!!.courseFinish = false
                //關閉線程
                adapter!!.closeThread()
                //重新讀取資料並展示
                adapter!!.callThread(query)
                //修改為上次搜索 避免重複搜索
                lastQuery = query
            }

            //清除焦點，收軟鍵盤
            _searchView.clearFocus()
            return false
        }

        /**
         * 清除上次搜索結果
         * @author KILNETA
         * @since Alpha_5.0
         */
        private fun clearSearchResult(){

            //清空上次搜索的結果
            if (courseEvaluateList.childCount > 0 ) {
                courseEvaluateList.removeAllViews()
                adapter!!.clearItems()
            }
            //清空上次搜索的結果數
            resultNum.text = "－"
        }

        //當搜索內容改變時觸發該方法
        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }
    }

    /**
     * 課程評價列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    inner class Adapter: CourseEvaluateListAdapter(
        baseContext,
        internetReceiver!!,
        courseEvaluateList
    ) {

        /**
         * 清空上次搜索的資料
         * @author KILNETA
         * @since Beta_1.3.0
         */
        fun clearItems(){
            courseEvaluationItem.clear()
        }

        /**
         * 開啟調取課程評鑑資料 異線程
         * @author KILNETA
         * @since Beta_1.3.0
         */
        @DelicateCoroutinesApi
        fun callThread(keyword : String){
            GlobalScope.launch(Dispatchers.Main) {
                callCourseEvaluateDataThread(
                    { callCourseIntroduction(keyword) },
                    { getCourseEvaluateData_Done() },
                    { getCourseEvaluateData_Done() }
                )
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun getCourseEvaluateData_Done(){
            //更新搜索到的數據
            resetData()
            resultNum.text = courseEvaluationItem.size.toString()
            //停止加載動畫
            stopLoading()
        }
    }

    /**
     * 載入動畫
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun startLoading(){
        //修改版面 (顯示)
        loading.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
    }

    /**
     * 停止載入動畫
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun stopLoading(){
        //修改版面 (隱藏)
        loading.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            )
    }
}