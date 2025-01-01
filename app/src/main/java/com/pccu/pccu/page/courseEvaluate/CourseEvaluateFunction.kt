package com.pccu.pccu.page.courseEvaluate

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.coroutines.*

/**
 * 課程評鑑內文列表控件的適配器 "內部類"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
class CourseEvaluateContentListAdapter(
    /**上下文*/
    val context : Context,
    /**課程評鑑 評價*/
    val CE : ArrayList<CourseEvaluation>,
    /**課程評鑑 簡介*/
    val CI : CourseIntroduction
): RecyclerView.Adapter<RV.ViewHolder>() {

    /**推薦指數轉換
     * @param rating [Int] 推薦指數(-3 ~ 3)
     * @return [String] : 推薦評語
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun ratingText(rating: Int): String {
        return when (rating) {
            3 -> "推爆 / 👍👍👍"
            2 -> "頗推 / 👍👍"
            1 -> "略推 / 👍"
            0 -> "普通 / 👀"
            -1 -> "略雷 / ⚠️"
            -2 -> "頗雷 / ⚠️⚠️"
            -3 -> "雷爆 / ⚠️⚠️⚠️"
            else -> "null"
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
            LayoutInflater.from(context)
                .inflate(R.layout.course_evaluate_courses_evaluation_list_item, parent, false)
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
        return CE.size
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
        /**對應 position 課程評價內文*/
        val _CE = CE[position]

        if(position % 2 == 1)
            holder.itemView.findViewById<LinearLayout>(R.id.course_evaluate_courses_evaluation_list_item)
                .setBackgroundColor(Color.parseColor("#F9F9F9"))

        holder.itemView.findViewById<TextView>(R.id.assigmentText).text =
            _CE.assigment
        holder.itemView.findViewById<TextView>(R.id.commentText).text =
            _CE.comment
        holder.itemView.findViewById<TextView>(R.id.gradingText).text =
            _CE.grading
        holder.itemView.findViewById<TextView>(R.id.ratingText).text =
            ratingText(_CE.rating)
        holder.itemView.findViewById<TextView>(R.id.semesterText).text =
            _CE.semester
        holder.itemView.findViewById<TextView>(R.id.teachingText).text =
            _CE.teaching
        holder.itemView.findViewById<TextView>(R.id.timestampText).text =
            _CE.timestamp
    }
}

/**
 * 課程評鑑列表控件的適配器 "內部類"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
open class CourseEvaluateListAdapter(
    /**上下文*/
    private val context : Context,
    /**網路狀態檢測器*/
    private val internetReceiver : NetWorkChangeReceiver,
    /**父類列表控件*/
    private val parentRecyclerView : RecyclerView
): RecyclerView.Adapter<RV.ViewHolder>() {
    /**課程評價 控件展示數據*/
    val courseEvaluationItem: ArrayList<CourseEvaluationItem> = arrayListOf()
    /**調取課程評鑑網資料 異線程*/
    var mHT_ = HandlerThread("handlerThread")
    /**課程資料取得完成*/
    var courseFinish = false

    /**
     * 網路調取 課程評價資料
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    fun closeThread() {
        //關閉mHT_
        mHT_.quit()
        //若"學院"資料未被正確匯入 嘗試清空重取
        if(courseEvaluationItem.isNotEmpty() && !courseFinish)
            courseEvaluationItem.clear()
        //重構線程
        mHT_ = HandlerThread("handlerThread")
    }

    /**
     * 取得課程評價資料
     * @author KILNETA
     * @since Beta_1.3.0
     */
    suspend fun getCourseEvaluateData(loadDone:()->Unit) {
        withContext(Dispatchers.IO) {
            /**HandlerThread實例化 線程名稱為(handlerThread)*/
            val mHandlerThread = HandlerThread("handlerThread")

            val mThreadCorrect = arrayListOf<Boolean>()
            //多線程處理 評價資料取得
            getCourseEvaluation_Thread(mHandlerThread,mThreadCorrect)
            //嘗試取得各課程 評價資料
            do {
                /**各課程評價 資料取得 是否完成*/
                var correct = true

                mThreadCorrect.forEach {
                    //判斷資料取得是否成功 (結束迴圈)
                    if (!it)
                        correct = false
                }
                //如果網路中斷則強制結束迴圈
                if (!internetReceiver.isConnect) {
                    mHandlerThread.quit()
                    return@withContext
                }
            } while(!correct)

            //更新顯示列表資料
            withContext(Dispatchers.Main) {
                loadDone()
            }
        }
    }

    /**
     * 多線程處理 學系資料取得
     * @author mainHandler      [Handler] 主線程聯繫Handler
     * @author mHandlerThread   [HandlerThread] 實例化HandlerThread
     * @author threadCorrect    ArrayList<[Boolean]> 線程完成判斷
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun getCourseEvaluation_Thread(
        mHandlerThread : HandlerThread,
        mThreadCorrect : ArrayList<Boolean>
    ){
        //啟用HandlerThread
        mHandlerThread.start()
        //(須確保啟用HandlerThread後才可以執行mHandlerThread.looper的handleMessage定義)
        //workHandler定義
        /**線程任務Handler*/
        val workHandler : Handler = object : Handler(mHandlerThread.looper) {
            /**線程任務訊息處理*/
            override fun handleMessage(msg: Message) {
                /**站牌Index*/
                val evaluationIndex = msg.what
                //需要搭配 Main/IO 才能執行多線程
                GlobalScope.launch(Dispatchers.Main) {
                    //IO線程獲取到站時間資料原始檔
                    courseEvaluationItem[evaluationIndex].courseEvaluation = getCourseEvaluation(
                        courseEvaluationItem[evaluationIndex].courseIntroduction.department,
                        courseEvaluationItem[evaluationIndex].courseIntroduction.name,
                        courseEvaluationItem[evaluationIndex].courseIntroduction.teacher
                    )?: arrayListOf()
                    mThreadCorrect[evaluationIndex] = true
                }
            }
        }

        //逐個使用異線程 取得對應評價內容
        for(i in 0 until courseEvaluationItem.size) {
            mThreadCorrect.add(false)
            //加入異線程執行序列
            workHandler.sendMessage(
                Message.obtain().apply { what = i }
            )
        }
    }

    /**
     * 開啟調取課程評鑑資料 異線程
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    fun callCourseEvaluateDataThread(
        callCourseIntroduction :suspend()-> Unit,
        getCourseEvaluateData_Done :()-> Unit,
        Search :(()-> Unit)?
    ){
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
                    if(courseEvaluationItem.isEmpty() && !courseFinish)
                        callCourseIntroduction()
                    //協程調用
                    if (courseEvaluationItem.isNotEmpty())
                        getCourseEvaluateData { getCourseEvaluateData_Done() }

                    else if(Search!=null){
                        Search()
                    }
                }
            }
        }
        //觸發線程處理
        workHandler.sendMessage(Message.obtain())
    }

    /**
     * 取得課程簡介資料
     * @param college       [String] 學院
     * @param department    [String] 學系
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    suspend fun callCourseIntroduction(
        college: String,
        department: String,
    ){
        //嘗試取得 課程簡介
        withContext(Dispatchers.IO) {
            CourseEvaluationAPI.getCourseIntroduction(college, department)
        }?.forEach {
            //取得課程簡介
            courseEvaluationItem.add(
                CourseEvaluationItem(
                    it,
                    arrayListOf(),
                    true
                )
            ).run {
                //確認資料取得成功
                if (courseEvaluationItem.isNotEmpty())
                    courseFinish = true
            }
        }
    }

    /**
     * 取得課程簡介資料
     * @param keyword [String] 關鍵字(課名、教授)
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    suspend fun callCourseIntroduction(
        keyword: String
    ){
        //嘗試取得 課程簡介
        withContext(Dispatchers.IO) {
            CourseEvaluationAPI.getCourseIntroduction_useKeyword(keyword)
        }?.forEach {
            //取得課程簡介
            courseEvaluationItem.add(
                CourseEvaluationItem(
                    it,
                    arrayListOf(),
                    true
                )
            ).run {
                //確認資料取得成功
                if (courseEvaluationItem.isNotEmpty())
                    courseFinish = true
            }
        }
    }

    /**
     * 取得課程評價內文資料
     * @param department    [String] 學系
     * @param course        [String] 課名
     * @param teacher       [String] 教授
     * @return 課程簡介資料表 : List<[CourseIntroduction]>
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    suspend fun getCourseEvaluation(
        department: String,
        course: String,
        teacher: String
    ): ArrayList<CourseEvaluation>? {
        /**評價內文列表*/
        return withContext(Dispatchers.IO) {
            CourseEvaluationAPI.getCourseEvaluation(
                department,
                course,
                teacher,
            )
        }
    }

    /**
     * 重製列表並導入新數據
     * @return [Boolean] : 導入完成
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    fun resetData() : Boolean {
        //刷新視圖列表
        @Suppress("NotifyDataSetChanged")
        notifyDataSetChanged()
        return false
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
            LayoutInflater.from(context)
                .inflate(R.layout.course_evaluate_courses_list_item, parent, false)
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
        return courseEvaluationItem.size
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
        val adapter = CourseEvaluateContentListAdapter(
            context,
            courseEvaluationItem[position].courseEvaluation,
            courseEvaluationItem[position].courseIntroduction
        )
        //掛載 evaluationView 列表適配器
        holder.itemView.findViewById<RecyclerView>(R.id.evaluationView).adapter = adapter
        //evaluationView列表 禁止滑動
        holder.itemView.findViewById<RecyclerView>(R.id.evaluationView).layoutManager =
            object : LinearLayoutManager(context) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        //evaluationView列表 方向(垂直)
        holder.itemView.findViewById<RecyclerView>(R.id.evaluationView).layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )

        /**課程評鑑 指定position*/
        val CEI = courseEvaluationItem[position]
        /**課程評鑑 簡介 指定position*/
        val CI = courseEvaluationItem[position].courseIntroduction

        holder.itemView.findViewById<TextView>(R.id.CollegeName).text =
            CI.department
        holder.itemView.findViewById<TextView>(R.id.CourseName).text =
            "${CI.name} - ${CI.teacher}"

        //設置當前布局(收合、展開)
        holder.itemView.findViewById<RecyclerView>(R.id.evaluationView).layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if(!CEI.isCollapse)
                    ViewGroup.LayoutParams.WRAP_CONTENT
                else
                    0
            )
        //設置按紐
        holder.itemView.findViewById<ImageView>(R.id.collapseButton).setImageDrawable(
            if(!CEI.isCollapse)
                ContextCompat.getDrawable(context, R.drawable.collapse)
            else
                ContextCompat.getDrawable(context, R.drawable.expand)
        )
        //按下標題區塊 操作(收合、展開)
        holder.itemView.findViewById<LinearLayout>(R.id.courseBar).setOnClickListener {
            //判斷是否收合 並操作、賦值
            CEI.isCollapse = run {
                //收合操作
                ViewGauge.changeExpandCollapse(
                    CEI.isCollapse,
                    parentRecyclerView,
                    holder.itemView.findViewById<RecyclerView>(R.id.evaluationView),
                    500,
                    null,
                    null
                )
                //更改按鈕圖片
                holder.itemView.findViewById<ImageView>(R.id.collapseButton).setImageDrawable(
                    if(CEI.isCollapse)
                        ContextCompat.getDrawable(context, R.drawable.collapse)
                    else
                        ContextCompat.getDrawable(context, R.drawable.expand)
                )
                return@run !CEI.isCollapse
            }
        }
    }
}