package com.example.pccu.page.announcement

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import org.jsoup.nodes.Element
import java.util.*
import com.example.pccu.R
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.pccu.internet.AnnouncementContent
import com.example.pccu.internet.ContentParser
import com.example.pccu.internet.HttpRetrofit
import com.example.pccu.internet.NetWorkChangeReceiver
import kotlinx.android.synthetic.main.announcement_page.*
import kotlinx.coroutines.*

/**
 * PCCU公告系統 公告內容 頁面建構類 : "AppCompatActivity(announcement_content_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class AnnouncementContentPage : AppCompatActivity(R.layout.announcement_content_page){
    /**取出數據傳遞中的公告連結 來源:Announcement_Page > List.Adapter > onBindViewHolder*/
    private var contentUrl: String? = null
    /**網路接收器*/
    private var internetReceiver: NetWorkChangeReceiver? = null
    /**第一次加載數據*/
    private var init = true

    /**
     * 網路接收器初始化
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    @DelicateCoroutinesApi
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
                @RequiresApi(Build.VERSION_CODES.N)
                override fun connectedInternet() {
                    noNetWork.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                        )
                    if(init){
                        callPccuAnnouncementContent(contentUrl!!)
                    }

                }
            },
            baseContext!!
        )
        val itFilter = IntentFilter()
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(internetReceiver, itFilter)
    }

    /**
     * announcement_content_page頁面建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        //呼叫頁面建置
        super.onCreate(savedInstanceState)
        //初始化網路接收器
        initInternetReceiver()

        //取出數據傳遞中的公告連結 來源:Announcement_Page > List.Adapter > onBindViewHolder
        contentUrl = intent.extras!!.getString("Url")
        if(internetReceiver!!.isConnect)
            //取得公告內文
            callPccuAnnouncementContent(contentUrl!!)

    }

    /**
     * 當頁面刪除時(刪除)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(internetReceiver)
    }

    /**
     * 重新設置公告的數據資料
     * @param ContentData [AnnouncementContent] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun reSetData(ContentData: AnnouncementContent? ){

        ContentData?.let {
            // 設置主旨(標題)
            changeTitle(ContentData.Subject!!)
            // 設置內文
            contentSetter(ContentData.Text)
            // 設置附件
            addLinkText(ContentData.Appendix, R.id.Appendix)
            // 設置活動資訊
            addLinkText(ContentData.ActivityInformation, R.id.ActivityInformation)
            // 設置相關連結
            addLinkText(ContentData.RelatedLinks, R.id.RelatedLinks)
            // 設置公告、活動起迄時間
            datesAnnouncement(ContentData)
            // 設置雜項內容(公告分類、公告單位、點閱率)
            miscellaneousDatas(ContentData)
        }
    }

    /**
     * 設置主旨(標題)
     * @param Subject [String] 主旨
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private fun changeTitle(Subject: String){
        //取得主旨的控件
        val title = findViewById<TextView>(R.id.title)
        //輸入資料到控件中
        title.text = Subject
    }

    /**
     * 設置內文(標題)
     * @param ContentData Vector<[Element]> 內文表<元件(HTML)>
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun contentSetter(ContentData: Vector<Element>) {
        //逐一讀取內文表
        for (i in 0 until ContentData.size) {
            //添加內文
            addContentText(ContentData[i])
        }
    }

    /**
     * 添加內文
     * @param ContentData [Element] 內文元件(HTML)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addContentText(ContentData: Element){
        //取得內文的控件
        val lin = findViewById<LinearLayout>(R.id.Content)
        //新建WebView控件
        val webView = WebView(this)

        //已啟用 JavaScript
        webView.settings.javaScriptEnabled = true
        //載入 內文HTML 於WebView控件中顯示
        webView.loadData(
            "<html><body>" + ContentData.outerHtml() + "</body></html>",
            "text/html; charset=utf-8",
            "UTF-8"
        )

        //添加 "新建的WebView控件" -> "內文控件"中
        lin.addView(webView)

    }

    /**
     * 添加公告、活動起迄時間
     * @param ContentData [AnnouncementContent] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private fun datesAnnouncement(ContentData: AnnouncementContent){
        // 取得 "公告、活動起迄時間" 的控件
        val timesControls: Array<TextView> = arrayOf(
            findViewById(R.id.AnnouncementTimes_Start),
            findViewById(R.id.AnnouncementTimes_End),
            findViewById(R.id.ActiveTimes_Start),
            findViewById(R.id.ActiveTimes_End)
        )
        //內文資料表格化
        val dates: Array<String> = arrayOf(
            ContentData.AnnouncementTimes.startDate!!,
            ContentData.AnnouncementTimes.endDate!!,
            ContentData.ActiveTimes.startDate!!,
            ContentData.ActiveTimes.endDate!!
        )

        //匯入所有資料
        for(i in timesControls.indices){
            //微調部分格式
            if(dates[i]==" ") dates[i] = "--"
            else { dates[i] = dates[i].replace(" - ","\n")
                .replace(" ","\n")}
            //輸入資料到控件中
            timesControls[i].text = dates[i]
        }
    }

    /**
     * 添加公告、活動起迄時間
     * @param ContentData [AnnouncementContent] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private fun miscellaneousDatas(ContentData: AnnouncementContent){
        //取得 "公告分類、公告單位、點閱率" 的控件
        val miscellaneousControls: Array<TextView> = arrayOf(
            findViewById(R.id.Classification),
            findViewById(R.id.cato),
            findViewById(R.id.CTR)
        )
        //內文資料表格化
        val miscellaneousData: Array<String> = arrayOf(
            ContentData.Classification!!,
            ContentData.Unit.Unit + "\n" + ContentData.Unit.Cato,
            ContentData.CTR!!
        )

        //匯入所有資料
        for(i in miscellaneousControls.indices){
            //微調部分格式
            if(miscellaneousData[i]==" ") miscellaneousData[i] = "--"
            //輸入資料到控件中
            miscellaneousControls[i].text = miscellaneousData[i]
        }
    }

    /**
     * 添加連結資料
     * @param linkList Vector<[String]> 連結列表
     * @param linkLayout [Int] 欲擺放資料的控件
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun addLinkText(linkList: Vector<String>, linkLayout: Int){
        //取得 放連結的控件
        val link = findViewById<LinearLayout>(linkLayout)

        if(linkList.size == 0) {
            //沒有連結就印 灰黑色"--"
            val textView = TextView(this)
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.text = "--"
            textView.setLinkTextColor(Color.parseColor("#7d7d7d"))
            link.addView(textView)
        }
        else
            //否則印出所有列表中的連結
            for(i in 0 until linkList.size){
                val textView = TextView(this)
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.text = Html.fromHtml(linkList[i],Html.FROM_HTML_MODE_LEGACY)
                link.addView(textView)
            }
    }

    /**
     * 重新設置公告的數據資料
     * @param Content_Url [String] 公告內文的連結
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.N)
    fun callPccuAnnouncementContent(Content_Url: String){

        //取用協程
        GlobalScope.launch (Dispatchers.Main) {
            //異地連接存取公告內文資料
            val announcementList = withContext(Dispatchers.IO) {
                HttpRetrofit.createHTML(Content_Url, "big5")
            }
            //重新設置公告的數據資料
            announcementList?.let {
                reSetData(ContentParser().getContent(announcementList))
                init = false
            }
        }
    }
}