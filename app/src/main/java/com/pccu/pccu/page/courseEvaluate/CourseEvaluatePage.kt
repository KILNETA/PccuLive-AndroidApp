package com.pccu.pccu.page.courseEvaluate

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.page.courseEvaluate.search.CourseEvaluateSearchActivity
import com.pccu.pccu.sharedFunctions.JsonFunctions
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.android.synthetic.main.course_evaluate_college_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_college_item.view.CollegeName
import kotlinx.android.synthetic.main.course_evaluate_college_item.view.collapseButton
import kotlinx.android.synthetic.main.course_evaluate_courses_list_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_department_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_page.*
import kotlinx.android.synthetic.main.course_evaluate_page.aboutButton
import kotlinx.android.synthetic.main.course_evaluate_page.loading
import kotlinx.android.synthetic.main.course_evaluate_page.noNetWork
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * 課程評價 主頁面 主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
class CourseEvaluatePage : AppCompatActivity(R.layout.course_evaluate_page){

    /** courseEvaluateCollege_list 列表控件的適配器*/
    private val adapter = Adapter()
    /**第一次加載數據*/
    private var init = true
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null

    private val itFilter = IntentFilter()
    init {
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
    }

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    private fun initInternetReceiver(){
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                /**中斷網路*/
                override fun interruptInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    if(init){
                        //關閉 調取課程評鑑資料 異線程
                        adapter.closeCollegeDepartmentDataThread()
                    }
                }
                /**網路連接*/
                override fun connectedInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    if(init){
                        //啟用 調取課程評鑑資料 異線程
                        adapter.callCollegeDepartmentDataThread()
                    }
                }
            },
            baseContext!!
        )
    }

    /**
     * 設置關於按鈕功能
     * @author KILNETA
     * @since Alpha_3.0
     */
    private fun setAboutButton(){
        /**關於內文*/
        val content = arrayOf(
            "聲明：",
            "　　本程式之課程評價系統僅是提供便捷閱讀，無法保證評價之正確性，參考時請謹慎評估；" +
                    "本程式亦無編輯課程評價數據之權限，數據由\"中國文化大學選課評價系統\"提供與管理。",
            "　　(長按評價項可使用瀏覽器開啟)",
            "",
            "　　評價條目、內文若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正。",
            "",
            "評價來源：",
            "　　中國文化大學選課評價系統"
        )
        //點擊關於按鈕 開啟關於頁面
        aboutButton.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = com.pccu.pccu.about.AboutBottomSheet(content)
            aboutSheetFragment.show(supportFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**
     * 停止載入動畫
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun stopLoading(){
        //套用設定至載入動畫物件
        loading.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            )
    }

    /**
     * 初始化 搜索框 功能
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun initSearchBox(){
        //當搜索框被按下
        searchBox.setOnClickListener{
            //轉換當前的頁面 至 搜索頁面
            /**新介面Activity目標*/
            val intentObj = Intent()
            intentObj.setClass(baseContext, CourseEvaluateSearchActivity::class.java )
            startActivity(intentObj)
        }
    }

    /**
     * 初始化 撰寫課程評價按鈕
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun initWriteEvaluate(){
        //撰寫課程評價按鈕被按下
        writeEvaluate.setOnClickListener{
            //使用外部瀏覽器開啟撰寫課程評價網頁
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://course.pccu.app/submit")
                )
            )
        }
    }

    /**
     * 初始化 顯示目前總計評價數
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initEvaluateCount(){
        GlobalScope.launch(Dispatchers.Main) {
            evaluationCount.text =
                callEvaluateCount()?.count?.toString()
                    ?: run { "－" }
        }
    }

    /**
     * 連接API 取得目前總計評價數
     * @return [EvaluationCount] : 目前總計評價數
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private suspend fun callEvaluateCount() : EvaluationCount? {
        return withContext(Dispatchers.IO) {
            CourseEvaluationAPI.getCount()
        }
    }


    /**
     * 課程評價 主頁面 主框架建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //創建頁面

        //網路接收器初始化
        initInternetReceiver()
        //設置關於按鈕功能
        setAboutButton()
        //初始化 搜索框
        initSearchBox()
        //初始化 撰寫課程評價按鈕
        initWriteEvaluate()

        //掛載 courseEvaluateCollege_list 列表適配器
        courseEvaluateCollege_list.adapter = adapter
        //列表控件 courseEvaluateCollege_list 的設置佈局管理器 (列表)
        courseEvaluateCollege_list.layoutManager =
            LinearLayoutManager(
                baseContext,
                LinearLayoutManager.VERTICAL,
                false
            )
        //呼叫適配器callPccuAnnouncementAPI取得資料與建構列表
        if(internetReceiver!!.isConnect) {
            adapter.callCollegeDepartmentDataThread()
        }
    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    override fun onStart() {
        super.onStart()
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * 學院列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    inner class Adapter: RecyclerView.Adapter<RV.ViewHolder>(){
        /**學院與學系控件管理列表*/
        private val collegeDepartment : ArrayList<CollegeDepartmentsItem> = arrayListOf()
        /**調取課程評鑑網資料 異線程*/
        var mHT_ = HandlerThread("handlerThread")
        /**學院資料取得完成*/
        var collegeFinish = false

        /**
         * 關閉調取課程評鑑資料 異線程
         * @author KILNETA
         * @since Beta_1.3.0
         */
        @DelicateCoroutinesApi
        fun closeCollegeDepartmentDataThread(){
            //關閉mHT_
            mHT_.quit()
            //若"學院"資料未被正確匯入 嘗試清空重取
            if(collegeDepartment.isNotEmpty() && !collegeFinish)
                collegeDepartment.clear()
            //重構線程
            mHT_ = HandlerThread("handlerThread")
        }

        /**
         * 開啟調取課程評鑑資料 異線程
         * @author KILNETA
         * @since Beta_1.3.0
         */
        @DelicateCoroutinesApi
        fun callCollegeDepartmentDataThread(){
            //啟用mHT_
            mHT_.start()
            //(須確保啟用HandlerThread後才可以執行mHandlerThread.looper的handleMessage定義)
            //workHandler定義
            /**線程任務Handler*/
            val workHandler : Handler = object : Handler(mHT_.looper) {
                /**線程任務訊息處理*/
                override fun handleMessage(msg: Message) {
                    //需要搭配 Main/IO 才能執行多線程
                    GlobalScope.launch(Dispatchers.Main) {
                        //嘗試取得 學院資料
                        if(collegeDepartment.isEmpty() && !collegeFinish)
                            getColleges()
                        //協程調用
                        if (collegeDepartment.isNotEmpty())
                            getCollegeDepartment()
                    }
                }
            }
            //觸發線程處理
            workHandler.sendMessage(Message.obtain())
        }

        /**
         * 重製列表並導入新數據
         * @author KILNETA
         * @since Beta_1.3.0
         */
        @DelicateCoroutinesApi
        private fun resetData(){
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
            initEvaluateCount()
            //關閉loading動畫
            stopLoading()
            init = false
        }

        /**
         * 取得課程評價 學院資料
         * @author KILNETA
         * @since Alpha_5.0
         */
        private suspend fun getCollegeDepartment() {
            withContext(Dispatchers.IO) {
                /**HandlerThread實例化 線程名稱為(handlerThread)*/
                val mHandlerThread = HandlerThread("handlerThread")
                //多線程處理 學系資料取得
                getDepartment_Thread(mHandlerThread)
                //嘗試取得各課程 評價資料
                do {
                    /**各課程評價 資料取得 是否完成*/
                    var correct = true

                    collegeDepartment.forEach {
                        //判斷資料取得是否成功 (結束迴圈)
                        if (it.departments.isEmpty())
                            correct = false
                    }
                    //如果網路中斷則強制結束迴圈
                    if(!internetReceiver!!.isConnect) {
                        mHandlerThread.quit()
                        return@withContext
                    }
                } while(!correct)

                //更新顯示列表資料
                withContext(Dispatchers.Main) {
                    //更新搜索到的數據
                    resetData()
                }
            }
        }

        /**
         * 多線程處理 學系資料取得
         * @author mHandlerThread   [HandlerThread] 實例化HandlerThread
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        private fun getDepartment_Thread(
            mHandlerThread : HandlerThread
        ) : Boolean{
            //啟用HandlerThread
            mHandlerThread.start()
            //(須確保啟用HandlerThread後才可以執行mHandlerThread.looper的handleMessage定義)
            //workHandler定義
            /**線程任務Handler*/
            val workHandler : Handler = object : Handler(mHandlerThread.looper) {
                /**線程任務訊息處理*/
                override fun handleMessage(msg: Message) {
                    /**學院Index*/
                    val collegeIndex = msg.what
                    //需要搭配 Main/IO 才能執行多線程
                    GlobalScope.launch(Dispatchers.Main) {
                        //重複直到取得資料
                        while (collegeDepartment[collegeIndex].departments.isEmpty()) {
                            //獲取到站時間資料原始檔
                            collegeDepartment[collegeIndex].departments = getDepartment(
                                collegeDepartment[collegeIndex].college
                            )?: arrayListOf()
                        }
                    }
                }
            }

            //逐個使用異線程 取得對應評價內容
            for(i in 0 until collegeDepartment.size) {
                //已匯入資料則不再重新取得
                if (collegeDepartment[i].departments.isEmpty()) {
                    //加入異線程執行序列
                    workHandler.sendMessage(
                        Message.obtain().apply { what = i }
                    )
                }
            }
            return true
        }

        /**
         * 取得 學系資料
         * @param college [String] 學院
         * @return List<[Department]> : 學系資料列表
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        private suspend fun getDepartment(college:String):ArrayList<Department>? {
            return withContext(Dispatchers.IO) {
                CourseEvaluationAPI.getDepartments(college)
            }
        }

        /**
         * 取得 學院資料
         * @author KILNETA
         * @since Beta_1.3.0
         */
        private suspend fun getColleges() {
            //嘗試取得 學院資料
            withContext(Dispatchers.IO) {
                CourseEvaluationAPI.getColleges()
            }?.forEach {
                //取得學院資料
                collegeDepartment.add(
                    CollegeDepartmentsItem(
                        it,
                        arrayListOf(),
                        true
                    )
                )
            }.run {
                //確認資料取得成功
                if(collegeDepartment.isNotEmpty())
                    collegeFinish = true
            }
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載布局於當前的context 列表控件元素announcement_item*/
            val view =
                LayoutInflater.from(baseContext)
                    .inflate(R.layout.course_evaluate_college_item,parent,false)
            //回傳當前持有者
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        override fun getItemCount(): Int {
            //列表元素數量 = 校務功能按鈕數量
            return collegeDepartment.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {
            /** DepartmentList 列表控件的適配器*/
            val adapter = DepartmentAdapter(
                collegeDepartment[position].college,
                collegeDepartment[position].departments
            )
            holder.itemView.CollegeName.text = collegeDepartment[position].college
            //掛載 DepartmentList 列表適配器
            holder.itemView.DepartmentList.adapter = adapter
            //DepartmentList列表 禁止滑動
            holder.itemView.DepartmentList.layoutManager =
                object : LinearLayoutManager(baseContext) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
            //DepartmentList列表 方向(垂直)
            holder.itemView.DepartmentList.layoutManager =
                LinearLayoutManager(
                    baseContext,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            //設置當前布局(收合、展開)
            holder.itemView.DepartmentList.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    if(!collegeDepartment[position].isCollapse)
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    else
                        0
                )
            //設置按紐
            holder.itemView.collapseButton.setImageDrawable(
                if(!collegeDepartment[position].isCollapse)
                    ContextCompat.getDrawable(baseContext, R.drawable.collapse)
                else
                    ContextCompat.getDrawable(baseContext, R.drawable.expand)
            )
            //按下標題區塊 操作(收合、展開)
            holder.itemView.CollegeBar.setOnClickListener {
                collegeDepartment[position].isCollapse = run {
                    //收合操作
                    ViewGauge.changeExpandCollapse(
                        collegeDepartment[position].isCollapse,
                        courseEvaluateCollege_list,
                        holder.itemView.DepartmentList,
                        500,
                        null,
                        null
                    )
                    //更改按鈕圖片
                    holder.itemView.collapseButton.setImageDrawable(
                        if(collegeDepartment[position].isCollapse)
                            ContextCompat.getDrawable(baseContext, R.drawable.collapse)
                        else
                            ContextCompat.getDrawable(baseContext, R.drawable.expand)
                    )
                    return@run !collegeDepartment[position].isCollapse
                }
            }
        }

        /**
         * 學系列表控件的適配器 "內部類"
         *
         * @author KILNETA
         * @since Beta_1.3.0
         */
        inner class DepartmentAdapter(
            /**學院名稱*/
            private val college : String ,
            /**學院的學系列表*/
            private val collegeDepartment : ArrayList<Department>
        ): RecyclerView.Adapter<RV.ViewHolder>(){

            /**
             * 重製列表並導入新數據
             * @author KILNETA
             * @since Beta_1.3.0
             */
            @DelicateCoroutinesApi
            private fun resetData(){
                //刷新視圖列表
                @Suppress("NotifyDataSetChanged")
                notifyDataSetChanged()
                //關閉loading動畫
                stopLoading()
            }

            /**
             * 重構 創建視圖持有者
             * @param parent [ViewGroup] 視圖組
             * @param viewType [Int] 視圖類型
             * @return 當前持有者 : [RV.ViewHolder]
             *
             * @author KILNETA
             * @since Beta_1.3.0
             */
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
                /**加載布局於當前的context 列表控件元素announcement_item*/
                val view =
                    LayoutInflater.from(baseContext)
                        .inflate(R.layout.course_evaluate_department_item,parent,false)
                //回傳當前持有者
                return RV.ViewHolder(view)
            }

            /**
             * 重構 獲取展示物件數量
             * @return 列表元素數量 : [Int]
             *
             * @author KILNETA
             * @since Beta_1.3.0
             */
            override fun getItemCount(): Int {
                //列表元素數量 = 校務功能按鈕數量
                return collegeDepartment.size
            }

            /**
             * 重構 綁定視圖持有者
             * @param holder [RV.ViewHolder] 當前持有者
             * @param position [Int] 元素位置(第幾項)
             * @return 列表元素數量 : [Int]
             *
             * @author KILNETA
             * @since Beta_1.3.0
             */
            override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {
                holder.itemView.DepartmentName.text =
                    collegeDepartment[position].name
                holder.itemView.CourseNum.text =
                    collegeDepartment[position].course_count.toString()
                holder.itemView.setOnClickListener {
                    /**新方案 (新建Activity介面展示)*/
                    val intentObj = Intent()
                    //轉換當前的頁面 至 公車路線頁面
                    /**傳遞資料包*/
                    val bundle = Bundle()
                    //傳遞站點資料
                    bundle.putString("College", college)
                    bundle.putString("Department", collegeDepartment[position].name)
                    bundle.putInt("CourseCount", collegeDepartment[position].course_count)
                    intentObj.putExtras(bundle)
                    intentObj.setClass(
                        applicationContext,
                        CourseEvaluateDepartmentCoursesPage::class.java
                    )
                    //連接Activity
                    startActivity(intentObj)
                }
            }
        }
    }

    /**
     * 學院的學系列表 -數據結構
     * @param college           [String] 學院
     * @param departments       ArrayList<[Department]> 學院的學系列表
     * @param isCollapse        [Boolean] 是否折疊
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private data class CollegeDepartmentsItem(
        val college : String,                       //學院
        var departments : ArrayList<Department>,    //學院的學系列表
        var isCollapse : Boolean = true
    ) : Serializable
}