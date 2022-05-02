package com.example.pccu.Page

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.Internet.AnnouncementByPULL
import com.example.pccu.Internet.Announcement_Data
import com.example.pccu.Internet.PccuAnnouncement_Xml
import com.example.pccu.R
import kotlinx.android.synthetic.main.announcement_item.view.*
import kotlinx.android.synthetic.main.announcement_page.*
import kotlinx.coroutines.*
import java.util.*

/**
 * PCCU公告系統 公告列表 頁面建構類 : "Fragment(announcement_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class  Announcement_Page : Fragment(R.layout.announcement_page){

    /**
     * aannouncement_page頁面建構
     * @param view [View] 該頁面的父類
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //呼叫頁面建置
        super.onViewCreated(view, savedInstanceState)

        //列表控件announcement_list的設置佈局管理器 (列表)
        announcement_list.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        //設定列表控件的適配器
        val adapter = Adapter()
        announcement_list.adapter = adapter
        //呼叫適配器CallPccuAnnouncementAPI取得資料與建構列表
        adapter.CallPccuAnnouncementAPI()

    }

    /**
     * 公告列表控件的適配器 "內部類"
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class Adapter:RecyclerView.Adapter<MyViewHolder>(){
        // 公告列表資料
        var AnnouncementList = Vector<Announcement_Data>()

        /**
         * 網路調取PCCU公告XML
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        @DelicateCoroutinesApi
        fun CallPccuAnnouncementAPI(){
            //協程調用
            GlobalScope.launch {
                //網路調取PCCU公告XML
                val announcementList =
                    AnnouncementByPULL.getAnnouncements(PccuAnnouncement_Xml().Get())!!

                //返回主線程
                withContext(Dispatchers.Main) {
                    //重新讀入公告XML 並重設公告列表
                    reSetData(announcementList)
                }
            }
        }

        /**
         * 重製公告列表並導入新數據
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        fun reSetData(AnnouncementList: Vector<Announcement_Data>){
            //導入數據資料
            this.AnnouncementList.addAll(AnnouncementList)
            //刷新視圖列表
            notifyDataSetChanged()

            //Log.d("upDatas", "${this.AnnouncementList.size}") //測試公告量
        }

        /**
         * 重構 創建視圖持有者
         * @param parent [ViewGroup] 視圖組
         * @param viewType [Int] 視圖類型
         * @return 當前持有者 : [MyViewHolder]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //加載布局於當前的context 列表控件元素announcement_item
            val view =
                LayoutInflater.from(context).inflate(R.layout.announcement_item,parent,false)
            //回傳當前持有者
            return MyViewHolder(view)
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
            return AnnouncementList.size
        }

        /**
         * 重構 綁定視圖持有者
         * @param holder [MyViewHolder] 當前持有者
         * @param position [Int] 元素位置(第幾項)
         * @return 列表元素數量 : [Int]
         *
         * @author KILNETA
         * @since Alpha_1.0
         */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            //取得該元素對應的公告列表資料
            val PccuList = AnnouncementList[position]
            //設置公告縮圖
            holder.itemView.item_announcement_image.setImageResource(changeAuthorImage(cutAuthor(PccuList.author))!!)
            //設置公告單位
            holder.itemView.item_announcement_unit.text = cutAuthor(PccuList.author)
            //設置公告單位顏色
            holder.itemView.item_announcement_unit.setBackgroundColor(changeAuthorColor(cutAuthor(PccuList.author)))
            //設置公告標題
            holder.itemView.item_announcement_title.text = PccuList.title
            //設置公告發布時間
            holder.itemView.item_announcement_pubDate.text = cutTimeStering(PccuList.pubDate)

            //設置元素子控件的點擊功能
            holder.itemView.setOnClickListener {
                //Log.d("upDatas", "${position}") //測試是第幾個控件被點擊

                //設置資料傳遞者
                val bundle = Bundle()
                //傳遞 公告連結 -> 公告內文顯示頁面
                bundle.putString("Url", PccuList.link)
                //轉換當前的頁面 至 公告內文頁面
                Navigation.findNavController(view!!).navigate(R.id.navigation_announcement_context_page, bundle)

                // 棄置方案 vvv
                //val intent = Intent()
                //intent.setClass(getContext()!!, Announcement_Content_Page::class.java)
                //startActivity(intent)
            }
        }
    }

    /**
     * 當前持有者 "內部類"
     * @param view [View] 該頁面的父類
     * @return 回收視圖持有者 : [RecyclerView.ViewHolder]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}

/**
 * 調整不同發布單位的顏色標示
 * @param author [String] 發布單位
 * @return 顏色代碼 : [Int]
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun changeAuthorColor(author:String?): Int {
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
fun changeAuthorImage(author:String?): Int {
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
    return R.color.black
}

/**
 * 微調發布單位職稱
 * @param author [String] 修改前的發布單位
 * @return 修改後的發布單位 : [String]
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun cutAuthor (author:String?) : String {
    //main function
    //"學生事務處　職發組" -> "學生事務處"
    val AuthorList = author!!.split('　')
    val Author = AuthorList[0]

    return Author
}

/**
 * 微調公告發布時間
 * @param pubDate [String] 修改前的發布時間
 * @return 修改後的發布時間 : [String]
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun cutTimeStering (pubDate:String?) : String {

    /**微調公告發布時間(月份)
     * @param month [String] 修改前的月份
     * @return 修改後的月份: [String]
     */
    fun monthConversion (month:String) : String{
        when(month){
            "Jan"-> return "01"
            "Feb"-> return "02"
            "Mar"-> return "03"
            "Apr"-> return "04"
            "May"-> return "05"
            "Jun"-> return "06"
            "Jul"-> return "07"
            "Aug"-> return "08"
            "Sep"-> return "09"
            "Oct"-> return "10"
            "Nov"-> return "11"
            "Dec"-> return "12"
        }
        return "--"
    }

    /**微調公告發布時間(星期)
     * @param month [String] 修改前的星期
     * @return 修改後的星期: [String]
     */
    fun WeekNumberConversion (month:String) : String{
        when(month){
            "Mon"-> return "一"
            "Tue"-> return "二"
            "Wed"-> return "三"
            "Thu"-> return "四"
            "Fri"-> return "五"
            "Sat"-> return "六"
            "Sun"-> return "日"
        }
        return "--"
    }

    //main function
    //"Fri, 01 Apr 2022 15:00:00 GMT" -> "2022/04/01(五)"
    val PubDateList = pubDate!!.split(' ')
    val PubDate = "${PubDateList[3]}/${monthConversion(PubDateList[2])}/${PubDateList[1]}" +
              "(${WeekNumberConversion(PubDateList[0].substring(0,3))}) "

    return PubDate
}
