package com.pccu.pccu.page.courseEvaluate

import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import kotlinx.android.synthetic.main.bus_route_page.*
import kotlinx.android.synthetic.main.course_evaluate_college_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_courses_evaluation_list_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_courses_list_item.view.*
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.*
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.aboutButton
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.backButton
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.courseEvaluateList
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.loading
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.noNetWork
import kotlinx.android.synthetic.main.course_evaluate_department_courses_page.view.*
import kotlinx.android.synthetic.main.course_evaluation_search_main.*
import kotlinx.coroutines.*
import java.util.*

/**
 * 課程評價 學系課程評價頁面 主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
class CourseEvaluateDepartmentCoursesPage : AppCompatActivity(
    R.layout.course_evaluate_department_courses_page
){
    /**courseEvaluateList 列表適配器*/
    private var adapter: Adapter? = null
    /**第一次加載數據*/
    private var init = true
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null
    /**頁面顯示的學院*/
    private var college : String = ""
    /**頁面顯示的學系*/
    private var department : String = ""
    /**頁面顯示的學系 -課程評價總數*/
    private var courseCount : Int = -1

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
                        adapter?.closeThread()
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
                        adapter?.callThread()
                    }
                }
            },
            baseContext!!
        )
    }

    /**
     * 設置返回按鈕
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun initBackButton(){
        backButton.setOnClickListener {
            //關閉視窗
            finish()
        }
    }

    /**
     * 設置關於按鈕功能
     *
     * @author KILNETA
     * @since Beta_1.3.0
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
     *
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
     * 停止載入動畫
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun noDatasView(){
        //套用設定至載入動畫物件
        noDataView.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
    }

    /**
     * 課程評價 學系課程評價頁面 主框架建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //創建頁面
        //初始化網路接收器
        initInternetReceiver()
        /**route_list 列表適配器*/
        adapter = Adapter()
        /**拿包裹*/
        val bundle = this.intent.extras!!
        college = bundle.getString("College").toString()
        department = bundle.getString("Department").toString()
        courseCount =  bundle.getInt("CourseCount")
        //設置頁面 學院、學系展示
        CollegeDepartmentName.text = "${college} - ${department}"
        evaluationCount.text = courseCount.toString()

        //設置返回按鈕
        initBackButton()
        //設置關於按鈕功能
        setAboutButton()

        //沒有任何評價
        if(courseCount<=0){
            stopLoading()
            noDatasView()
        }

        //掛載 courseEvaluateList 列表適配器
        courseEvaluateList.adapter = adapter
        //列表控件courseEvaluateList的設置佈局管理器 (列表)
        courseEvaluateList.layoutManager =
            LinearLayoutManager(
                baseContext,
                LinearLayoutManager.VERTICAL,
                false
            )

        //呼叫適配器callPccuAnnouncementAPI取得資料與建構列表
        if(internetReceiver!!.isConnect)
            adapter!!.callThread()


    }

    /**
     * 頁面被啟用
     *
     * @author KILNETA
     * @since Beta_1.3.0
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
     * @since Beta_1.3.0
     */
    override fun onStop(){
        super.onStop()
        this.unregisterReceiver(internetReceiver)
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
         * 開啟調取課程評鑑資料 異線程
         * @author KILNETA
         * @since Beta_1.3.0
         */
        @DelicateCoroutinesApi
        fun callThread(){
            GlobalScope.launch(Dispatchers.Main) {
                callCourseEvaluateDataThread(
                    { callCourseIntroduction(college, department) },
                    { getCourseEvaluateData_Done() },
                    null
                )
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun getCourseEvaluateData_Done(){
            //關閉loading動畫
            stopLoading()
            init = resetData()
        }
    }
}