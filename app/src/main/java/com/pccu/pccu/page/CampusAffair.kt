package com.pccu.pccu.page

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.pccu.pccu.R
import com.pccu.pccu.internet.*
import com.pccu.pccu.menu.AnnouncementListItemBottomMenu
import com.pccu.pccu.page.announcement.AnnouncementContentPage
import com.pccu.pccu.page.courseEvaluate.CourseEvaluatePage
import com.pccu.pccu.sharedFunctions.DateConvert
import com.pccu.pccu.sharedFunctions.RV
import com.pccu.pccu.sharedFunctions.SV
import com.pccu.pccu.sharedFunctions.ViewGauge
import kotlinx.coroutines.*
import java.util.*

/**
 * PCCU校務系統 頁面建構類 : "Fragment(campus_affair_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class CampusAffair : Fragment(R.layout.campus_affair_page){

    /**announcement_list列表控件的適配器*/
    private val adapterAnnouncement = AdapterAnnouncement()
    /**campus_affair_list列表控件的適配器*/
    private val adapterCampusAffair = AdapterCampusAffair()
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null
    /**第一次加載數據*/
    private var init = true
    /**狀態過濾器*/
    private val itFilter = IntentFilter()
    /**CampusAffairServe (CAS) 校務選單是否收合*/
    private var cas_IsCollapse = false

    private var announcementListView : RecyclerView? = null
    private var announcementTitleView : LinearLayout? = null

    init {
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
    }

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
    private fun initInternetReceiver(){
        val noNetWork = this.view?.findViewById<TextView>(R.id.noNetWork)
        internetReceiver = NetWorkChangeReceiver(
            object : NetWorkChangeReceiver.RespondNetWork{
                /**中斷網路*/
                override fun interruptInternet() {
                    noNetWork?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
                /**網路連接*/
                override fun connectedInternet() {
                    noNetWork?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    if(init){
                        adapterAnnouncement.callPccuAnnouncementRSS()
                    }
                }
            },
            requireContext()
        )
    }

    /**
     * 停止載入動畫
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun stopLoading(){
        //停止載入動畫前判斷視圖是否還存在 避免APP發生崩潰
        if(view != null) {
            //套用設定至載入動畫物件
            this.view?.findViewById<LinearLayout>(R.id.loading)?.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0
                )
        }
    }

    /**
     * 設置關於按鈕功能
     *
     * @author KILNETA
     * @since Alpha_3.0
     */
    private fun setAboutButton(){
        /**關於內文*/
        val content = arrayOf(
            "提醒：",
            "　　有時文大公告系統的連線狀況不佳，則需要等待較長加載時間。",
            "",
            "聲明：",
            "　　本程式之公告系統僅是提供便捷閱讀，無法保證公告的展示之正確性，若認為是重要訊息通知，" +
                    "請務必返回文大官方公告頁面校驗，以確保內容正確，若造成損失本程式一概不負責。",
            "　　(長按公告項可開啟文大公告頁面)",
            "",
            "　　公告條目、內文若出現部分無法顯示，或是內容出現問題，可聯繫程式負責方協助修正，並附上該則公告連結。",
            "",
            "公告來源：",
            "　　中國文化大學"
        )
        //點擊關於按鈕 開啟關於頁面
        this.view?.findViewById<MaterialButton>(R.id.aboutButton)?.setOnClickListener{
            /**關於介面 底部彈窗*/
            val aboutSheetFragment = com.pccu.pccu.about.AboutBottomSheet(content)
            aboutSheetFragment.show(parentFragmentManager, aboutSheetFragment.tag)
        }
    }

    /**
     * 初始化 校務選單收合按鈕
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private fun setCollapseButton(){
        this.view?.findViewById<MaterialButton>(R.id.collapseButton)?.setOnClickListener {
            //禁止畫面滑動
            val campusAffairScrollView = this.view?.findViewById<SV>(R.id.campusAffair_ScrollView)
            val campusAffairServeView = this.view?.findViewById<LinearLayout>(R.id.campusAffairServe_view)

            campusAffairScrollView?.scroll = false
            campusAffairServeView?.let {
                //判斷是否收合 並操作、賦值
                cas_IsCollapse = let {
                    //操作收合、展開
                    ViewGauge.changeExpandCollapse(
                        cas_IsCollapse,
                        campusAffairServeView.rootView,
                        campusAffairServeView,
                        600,
                        null,
                        fun() { campusAffairScrollView?.scroll = true }
                    )
                    //更改按鈕圖片
                    this.view?.findViewById<MaterialButton>(R.id.collapseButton)?.setIconResource(
                        if (cas_IsCollapse)
                            R.drawable.collapse
                        else
                            R.drawable.expand
                    )
                    return@let !cas_IsCollapse
                }
            }
        }
    }

    /**
     * announcement_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //呼叫頁面建置
        super.onViewCreated(view, savedInstanceState)

        announcementListView = this.view?.findViewById(R.id.announcement_list)
        announcementTitleView = this.view?.findViewById(R.id.announcementTitle)

        //初始化網路接收器
        initInternetReceiver()
        //設置關於按鈕功能
        setAboutButton()
        //設置校務選單收合按鈕
        setCollapseButton()

        val campusAffairServeList = this.view?.findViewById<RecyclerView>(R.id.campusAffairServe_list)

        //列表控件campus_affair_list的設置佈局管理器 (列表)
        campusAffairServeList?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        //掛載 campusAffairServe_list 列表適配器
        campusAffairServeList?.adapter = adapterCampusAffair

        //列表控件announcement_list的設置佈局管理器 (列表)
        announcementListView?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        announcementListView?.adapter = adapterAnnouncement

        //呼叫適配器callPccuAnnouncementAPI取得資料與建構列表
        if(internetReceiver!!.isConnect)
            adapterAnnouncement.callPccuAnnouncementRSS()
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
        activity?.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * 當頁面停用時(不可見)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onStop(){
        super.onStop()
        activity?.unregisterReceiver(internetReceiver)
    }

    /**
     * 公告列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class AdapterAnnouncement:RecyclerView.Adapter<RV.ViewHolder>(){
        /**公告列表資料*/
        private var announcementList = Vector<AnnouncementData>()

        /**
         * 網路調取PCCU公告XML
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        fun callPccuAnnouncementRSS(){
            //協程調用
            GlobalScope.launch ( Dispatchers.Main ) {
                /**網路調取PCCU公告XML*/
                val announcementList = withContext(Dispatchers.IO) {
                    AnnouncementByPULL.getAnnouncements(PccuAnnouncementXml().get())
                }
                //重新讀入公告XML 並重設公告列表
                announcementList?.let {
                    resetData(announcementList)
                    init = !init
                }
            }
        }

        /**
         * 設置公告列表高度
         * @author KILNETA
         * @since Beta_1.3.0
         */
        private fun setAnnouncementListHeight(){
            //停止載入動畫前判斷視圖是否還存在 避免APP發生崩潰
            if(view != null) {
                /**裝置顯示指標*/
                val displayMetrics = context!!.resources.displayMetrics

                /**取得裝置高度(px)*/
                var height = displayMetrics.heightPixels

                /**取得底部導航*/
                val navigationBar = resources.getIdentifier(
                    "navigation_bar_height", "dimen", "android"
                )

                if(announcementTitleView != null){
                    /**底部導航高度*/
                    val navigationBarHeight = resources.getDimensionPixelSize(navigationBar)

                    //預留空間顯示 公告標頭、底部導航
                    height -= announcementTitleView!!.measuredHeight + 15
                    height -= navigationBarHeight

                    //導入布局
                    announcementListView?.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            height
                        )
                }
            }
        }

        /**
         * 重製公告列表並導入新數據
         * @param AnnouncementList Vector<[AnnouncementData]> 網路調取PCCU公告XML
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        private fun resetData(AnnouncementList: Vector<AnnouncementData>){
            //導入數據資料
            this.announcementList.addAll(AnnouncementList)
            //刷新視圖列表
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
            //關閉loading動畫
            stopLoading()
            //設置公告列表高度
            setAnnouncementListHeight()
        }

        /**
         * 調整不同發布單位的顏色標示
         * @param author [String] 發布單位
         * @return 顏色代碼 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        private fun changeAuthorColor(author:String?): Int {
            when(author){
                "文學院","國際暨外語學院","理學院","法學院","社科學院","農學院","工學院","商學院",
                "新聞暨傳播學院","藝術學院","環境設計學院","教育學院","體育運動健康學院"
                ->
                    return Color.parseColor("#7e75e0")

                "董事會","校長室","副校長室","秘書處","人事室","校務研究辦公室","環境保護暨職業安全衛生中心",
                ->
                    return Color.parseColor("#009cf0")
                "教務處","學務處","總務處","推廣教育部","研究發展處","國際暨兩岸事務處","資訊處",
                "圖書館","會計室","體育室","教學資源中心","共同科目與通識教育中心","華岡博物館",
                "學生事務處",
                ->
                    return Color.parseColor("#FF5887")
            }
            return Color.parseColor("#000000")
        }

        /**
         * 調整不同發布單位的縮圖
         * @param author [String] 發布單位
         * @return 縮圖編號 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        private fun changeAuthorImage(author:String?): Int {
            when(author){
                "文學院"-> return R.drawable.college_literature
                "國際暨外語學院"-> return R.drawable.college_internationality
                "理學院"-> return R.drawable.college_science
                "法學院"-> return R.drawable.college_law
                "社科學院"-> return R.drawable.college_social
                "農學院"-> return R.drawable.college_agriculture
                "工學院"-> return R.drawable.college_engineering
                "商學院"-> return R.drawable.college_business
                "新聞暨傳播學院"-> return R.drawable.college_media
                "藝術學院"-> return R.drawable.college_art
                "環境設計學院"-> return R.drawable.college_environmental
                "教育學院"-> return R.drawable.college_educate
                "體育運動健康學院"-> return R.drawable.college_socialpe

                "董事會","校長室","副校長室","秘書處","人事室","校務研究辦公室","環境保護暨職業安全衛生中心",
                ->
                    return R.drawable.administrative_1
                "教務處","學務處","總務處","推廣教育部","研究發展處","國際暨兩岸事務處","資訊處",
                "圖書館","會計室","體育室","教學資源中心","共同科目與通識教育中心","華岡博物館",
                "學生事務處",
                ->
                    return R.drawable.administrative_2
            }
            return R.drawable.question1
        }

        /**
         * 微調發布單位職稱
         * @param author [String] 修改前的發布單位
         * @return 修改後的發布單位 : [String]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        private fun cutAuthor (author:String?) : String {
            //"學生事務處　職發組" -> "學生事務處"
            /**字串格式拆分*/
            val authorList = author!!.split('　')
            return authorList[0]
        }

        /**
         * 微調公告發布時間
         * @param pubDate [String] 修改前的發布時間
         * @return 修改後的發布時間 : [String]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        private fun cutTimeString (pubDate:String?) : String {
            //"Fri, 01 Apr 2022 15:00:00 GMT" -> "2022/04/01(五)"
            /**字串格式拆分*/
            val pubDateList = pubDate!!.split(' ')
            return  pubDateList[3] +
                    "/" +
                    DateConvert.monthStr3ToNum(pubDateList[2]) +
                    "/" +
                    pubDateList[1] +
                    "(${DateConvert.weekEnToZhTw(pubDateList[0].substring(0,3))}) "
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [RV.ViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RV.ViewHolder {
            /**加載布局於當前的context 列表控件元素announcement_item*/
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.announcement_item,parent,false)
            //回傳當前持有者
            return RV.ViewHolder(view)
        }

        /**
         * 重構 獲取展示物件數量
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun getItemCount(): Int {
            //列表元素數量 = 公告列表資料(AnnouncementList)的大小
            return announcementList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [RV.ViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: RV.ViewHolder, position: Int) {

            /**取得該元素對應的公告列表資料*/
            val pccuList = announcementList[position]
            //設置公告縮圖
            holder.itemView.findViewById<ImageView>(R.id.item_announcement_image).setImageResource(
                changeAuthorImage(cutAuthor(pccuList.author))
            )
            //設置公告單位
            holder.itemView.findViewById<TextView>(R.id.item_announcement_unit)?.text =
                cutAuthor(pccuList.author)
            //設置公告單位顏色
            holder.itemView.findViewById<TextView>(R.id.item_announcement_unit).setBackgroundColor(
                changeAuthorColor(cutAuthor(pccuList.author))
            )
            //設置公告標題
            holder.itemView.findViewById<TextView>(R.id.item_announcement_title)?.text =
                pccuList.title
            //設置公告發布時間
            holder.itemView.findViewById<TextView>(R.id.item_announcement_pubDate)?.text =
                cutTimeString(pccuList.pubDate)

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                /**設置資料傳遞者*/
                val bundle = Bundle()
                //傳遞 公告連結 -> 公告內文顯示頁面
                bundle.putString("Url", pccuList.link)
                //轉換當前的頁面 至 公告內文頁面

                /**新方案 (新建Activity介面展示)*/
                val intentObj = Intent()
                intentObj.setClass(context!!, AnnouncementContentPage::class.java)
                //載入資料傳遞者
                intentObj.putExtras(bundle)
                //連接Activity
                startActivity(intentObj)
            }

            //設置元素子控件的長按功能
            holder.itemView.setOnLongClickListener {
                /**顯示底部彈窗列表*/
                val sheetFragment = AnnouncementListItemBottomMenu(pccuList.link!!)
                sheetFragment.show(parentFragmentManager, sheetFragment.tag)
                return@setOnLongClickListener true
            }

        }
    }

    /**
     * 校務功能列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    inner class AdapterCampusAffair:RecyclerView.Adapter<RV.ViewHolder>(){
        /**校務功能列表*/
        private val buttonDate:List<CampusAffairServe> = listOf(
            CampusAffairServe(
                "課程評價",
                "null",
                R.drawable.note
            ),
            CampusAffairServe(
                "課輔系統",
                "https://icas.pccu.edu.tw/cfp/#my",
                R.drawable.homework
            ),
            CampusAffairServe(
                "學生專區",
                "https://ecampus.pccu.edu.tw/ecampus/default.aspx?usertype=student",
                R.drawable.bookbag
            ),
            CampusAffairServe(
                "文化校網",
                "https://www.pccu.edu.tw/",
                R.drawable.pccupage
            ),
            CampusAffairServe(
                "選課專區",
                "https://mycourse.pccu.edu.tw/",
                R.drawable.classbook
            ),
            CampusAffairServe(
                "防疫專區",
                "https://www.pccu.edu.tw/fever/fever.html",
                R.drawable.needle
            )
        )

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
                    .inflate(R.layout.campus_affair_serve_list_item,parent,false)
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
            return buttonDate.size
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
            //設置選單項目圖片
            holder.itemView.findViewById<ImageView>(R.id.icon)?.setImageDrawable(
                ContextCompat.getDrawable( context!! , buttonDate[position].icon )
            )
            //設置選單項目名稱
            holder.itemView.findViewById<TextView>(R.id.name)?.text = buttonDate[position].name
            //設置選單項目典籍功能
            holder.itemView.setOnClickListener{
                when(buttonDate[position].name) {
                    "課程評價"-> {
                        /**新方案 (新建Activity介面展示)*/
                        val intentObj = Intent()
                        intentObj.setClass(requireContext(), CourseEvaluatePage::class.java)
                        //連接Activity
                        startActivity(intentObj)
                    }
                    else-> { //連接外部連結
                        startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(buttonDate[position].url))
                        )
                    }
                }
            }
        }
    }

    /**
     * 校務服務功能 -數據結構
     * @param name      [String] 功能名稱
     * @param url       [String] 功能連結
     * @param icon      [Int] 功能圖示
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    private data class CampusAffairServe(
        val name : String,
        val url : String,
        val icon : Int
    )
}
