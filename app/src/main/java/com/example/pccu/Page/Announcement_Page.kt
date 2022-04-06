package com.example.pccu.Page

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.Internet.AnnouncementByPULL
import com.example.pccu.Internet.Announcement_Data
import com.example.pccu.Internet.PccuAnnouncement_Xml_API
import com.example.pccu.Internet.Weather_Data
import com.example.pccu.R
import kotlinx.android.synthetic.main.announcement_item.view.*
import kotlinx.android.synthetic.main.announcement_page.*
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.*

class  Announcement_Page : Fragment(R.layout.announcement_page){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        announcement_list.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        val adapter = Adapter()
        announcement_list.adapter = adapter
        adapter.CallPccuAnnouncementAPI()

    }
    inner class  Adapter:RecyclerView.Adapter<MyViewHolder>(){
        var AnnouncementList = Vector<Announcement_Data>()

        @DelicateCoroutinesApi
        fun CallPccuAnnouncementAPI(){
            //協程調用
            GlobalScope.launch {
                val announcementList =
                    AnnouncementByPULL.getAnnouncements(PccuAnnouncement_Xml_API().Get())!!

                withContext(Dispatchers.Main) {
                    reSetData(announcementList)
                }
            }
        }

        //重置氣溫資料
        fun reSetData(AnnouncementList: Vector<Announcement_Data>){
            //導入數據資料
            this.AnnouncementList.addAll(AnnouncementList)
            Log.d("upDatas", "${this.AnnouncementList.size}")
            //刷新視圖列表
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.announcement_item,parent,false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return AnnouncementList.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            val PccuList = AnnouncementList[position]
            holder.itemView.item_announcement_image.setImageResource(changeAuthorImage(cutAuthor(PccuList.GetAuthor()))!!)
            holder.itemView.item_announcement_unit.text = cutAuthor(PccuList.GetAuthor())
            holder.itemView.item_announcement_unit.setBackgroundColor(changeAuthorColor(cutAuthor(PccuList.GetAuthor())))
            holder.itemView.item_announcement_title.text = PccuList.GetTitle()
            holder.itemView.item_announcement_pubDate.text = cutTimeStering(PccuList.GetPubDate())

        }

    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){}
}

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

fun changeAuthorImage(author:String?): Int? {
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

fun cutAuthor (author:String?) : String? {
    //main function
    //"學生事務處　職發組" -> "學生事務處"
    val AuthorList = author!!.split('　')
    val Author = AuthorList[0]

    return Author
}

fun cutTimeStering (pubDate:String?) : String? {
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
