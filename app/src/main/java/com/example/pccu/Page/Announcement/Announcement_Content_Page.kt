package com.example.pccu.Page.Announcement

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.pccu.Internet.*
import org.jsoup.nodes.Element
import java.util.*
import com.example.pccu.R
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

/**
 * PCCU公告系統 公告內容 頁面建構類 : "AppCompatActivity(announcement_content_page)"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class  Announcement_Content_Page : AppCompatActivity(R.layout.announcement_content_page){

    /**
     * announcement_content_page頁面建構
     * @param view [View] 該頁面的父類
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

        //取出數據傳遞中的公告連結 來源:Announcement_Page > List.Adapter > onBindViewHolder
        val bundle = intent.extras!!
        val Content_Url = bundle.getString("Url")
        //取得公告內文
        CallPccuAnnouncement_Content(Content_Url!!)
    }

    /**
     * announcement_content_page頁面建構
     * @param view [View] 該頁面的父類
     * @param ContentData [Announcement_Content] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun reExhibit(ContentData: Announcement_Content ){

        /*
        val lin = view.findViewById<LinearLayout>(R.id.Content)

        val imageView = ImageView(this.context)
        imageView.setBackgroundResource(R.drawable.sunny);
        val PARA = LinearLayout.LayoutParams(500, 500) //

        imageView.setLayoutParams(PARA)
        lin.addView(imageView)*/

        ///vvv正式 ^^^測試新增圖片

        // 設置主旨(標題)
        ChangeTitle(ContentData.Subject!!)
        // 設置內文
        ContentSetter(ContentData.Text)
        // 設置附件
        addLinkText(ContentData.Appendix,R.id.Appendix)
        // 設置活動資訊
        addLinkText(ContentData.ActivityInformation,R.id.ActivityInformation)
        // 設置相關連結
        addLinkText(ContentData.RelatedLinks,R.id.RelatedLinks)
        // 設置公告、活動起迄時間
        Time_Announcement(ContentData)
        // 設置雜項內容(公告分類、公告單位、點閱率)
        MiscellaneousDatas(ContentData)

    }

    /**
     * 設置主旨(標題)
     * @param view [View] 該頁面的父類
     * @param Subject [String] 主旨
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun ChangeTitle(Subject: String){
        //取得主旨的控件
        val Titel = findViewById<TextView>(R.id.title)
        //輸入資料到控件中
        Titel.setText(Subject)
    }

    /**
     * 設置內文(標題)
     * @param view [View] 該頁面的父類
     * @param ContentData Vector<[Element]> 內文表<元件(HTML)>
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun ContentSetter(ContentData: Vector<Element>) {
        //逐一讀取內文表
        for (i in 0 until ContentData.size) {
            //添加內文
            addContentText(ContentData[i])
        }
    }

    /**
     * 添加內文
     * @param view [View] 該頁面的父類
     * @param ContentData [Element] 內文元件(HTML)
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.N)
    fun addContentText(ContentData: Element){
        //取得內文的控件
        val lin = findViewById<LinearLayout>(R.id.Content)
        //新建WebView控件
        val webView = WebView(this)

        //已啟用 JavaScript
        webView.settings.javaScriptEnabled = true
        //載入 內文HTML 於WebView控件中顯示
        webView.loadData("<html><body>" + ContentData.outerHtml() + "</body></html>", "text/html; charset=utf-8", "UTF-8")

        //添加 "新建的WebView控件" -> "內文控件"中
        lin.addView(webView)

    }

    /**
     * 添加公告、活動起迄時間
     * @param view [View] 該頁面的父類
     * @param ContentData [Announcement_Content] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun Time_Announcement(ContentData: Announcement_Content){
        // 取得 "公告、活動起迄時間" 的控件
        val TimesControls: Array<TextView> = arrayOf(
            findViewById(R.id.AnnouncementTimes_Start),
            findViewById(R.id.AnnouncementTimes_End),
            findViewById(R.id.ActiveTimes_Start),
            findViewById(R.id.ActiveTimes_End)
        )
        //內文資料表格化
        val TimeDatas: Array<String> = arrayOf(
            ContentData.AnnouncementTimes.startDate!!,
            ContentData.AnnouncementTimes.endDate!!,
            ContentData.ActiveTimes.startDate!!,
            ContentData.ActiveTimes.endDate!!
        )

        //匯入所有資料
        for(i in 0 until TimesControls.size){
            //微調部分格式
            if(TimeDatas[i]==" ") TimeDatas[i] = "--"
            else { TimeDatas[i] = TimeDatas[i].replace(" - ","\n")
                .replace(" ","\n")}
            //輸入資料到控件中
            TimesControls[i].setText(TimeDatas[i])
        }
    }

    /**
     * 添加公告、活動起迄時間
     * @param view [View] 該頁面的父類
     * @param ContentData [Announcement_Content] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun MiscellaneousDatas(ContentData: Announcement_Content){
        //取得 "公告分類、公告單位、點閱率" 的控件
        val MiscellaneousControls: Array<TextView> = arrayOf(
            findViewById(R.id.Classification),
            findViewById(R.id.cato),
            findViewById(R.id.CTR)
        )
        //內文資料表格化
        val MiscellaneousData: Array<String> = arrayOf(
            ContentData.Classification!!,
            ContentData.Unit.Unit + "\n" + ContentData.Unit.Cato,
            ContentData.CTR!!
        )

        //匯入所有資料
        for(i in 0 until MiscellaneousControls.size){
            //微調部分格式
            if(MiscellaneousData[i]==" ") MiscellaneousData[i] = "--"
            //輸入資料到控件中
            MiscellaneousControls[i].setText(MiscellaneousData[i])
        }
    }

    /**
     * 添加連結資料
     * @param view [View] 該頁面的父類
     * @param linkList Vector<[String]> 連結列表
     * @param linLayout [Int] 欲擺放資料的控件
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun addLinkText(linkList: Vector<String>, linLayout: Int){
        //取得 放連結的控件
        val lin = findViewById<LinearLayout>(linLayout)

        if(linkList.size == 0) { //沒有連結就印 灰黑色"--"
            val textView = TextView(this)
            textView.setMovementMethod(LinkMovementMethod.getInstance())
            textView.setText("--")
            textView.setLinkTextColor(Color.parseColor("#7d7d7d"))
            lin.addView(textView)
        }
        else //否則印出所有列表中的連結
            for(i in 0 until linkList.size){
                val textView = TextView(this)
                textView.setMovementMethod(LinkMovementMethod.getInstance())
                textView.setText(Html.fromHtml(linkList[i],Html.FROM_HTML_MODE_LEGACY))
                lin.addView(textView)
            }
    }

    /**
     * 重新設置公告的數據資料
     * @param view [View] 該頁面的父類
     * @param ContentData [Announcement_Content] 公告內文資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun reSetData(ContentData: Announcement_Content?){
        //導入數據資料
        reExhibit(ContentData!!)
    }

    /**
     * 重新設置公告的數據資料
     * @param view [View] 該頁面的父類
     * @param Content_Url [String] 公告內文的連結
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.N)
    fun CallPccuAnnouncement_Content(Content_Url: String){

        //取用協程
        GlobalScope.launch {
            //Log.i("mytag", HttpRetrofit.create_Content(Content_Url))

            //異地連接存取公告內文資料
            val announcementList =
                HttpRetrofit.create_Content(Content_Url,"big5")

            //返回主線程
            withContext(Dispatchers.Main) {
                //重新設置公告的數據資料
                reSetData(ContentParser().getContent(announcementList))
            }
        }
    }
}

// ----vvv----  <TextView>公告內文的圖片顯示重構functions 目前已用 <WebView>  ----vvv----

/*
class URLImageGetter(contxt: Context, textView: TextView) : ImageGetter {
    var textView: TextView
    var context: Context
    override fun getDrawable(paramString: String): Drawable {
        val urlDrawable = URLDrawable(context)
        val getterTask: ImageGetterAsyncTask = ImageGetterAsyncTask(urlDrawable)
        getterTask.execute(paramString)
        return urlDrawable
    }

    inner class ImageGetterAsyncTask(drawable: URLDrawable) :
        AsyncTask<String?, Void?, Drawable?>() {
        var urlDrawable: URLDrawable
        override fun onPostExecute(result: Drawable?) {
            if (result != null) {
                urlDrawable.drawable = result
                textView.requestLayout()
            }
        }

        protected override fun doInBackground(vararg p0: String?): Drawable? {
            val source = p0[0]
            Log.e("test3","${source}")
            return fetchDrawable(source)
        }

        fun fetchDrawable(url: String?): Drawable? {
            var drawable: Drawable? = null
            val Url: URL
            try {
                Url = URL(url)
                drawable = Drawable.createFromStream(Url.openStream(), "")
            } catch (e: Exception) {
                return null
            }
            // 按比例缩放图片
            val scale = context.getResources().getDisplayMetrics().density.toDouble()
            val width: Double = drawable.intrinsicWidth * scale
            val height: Double = drawable.intrinsicHeight * scale

            Log.e("test1","${width}-${height}")
            drawable.setBounds(0, 0, width.toInt(), height.toInt())
            return drawable
        }

        init {
            urlDrawable = drawable
        }
    }

    // 预定图片宽高比例为 4:3
    fun getDefaultImageBounds(drawable: Drawable?): Rect {
        return Rect(0, 0, drawable!!.getIntrinsicHeight(), drawable!!.getIntrinsicWidth())
    }

    init {
        context = contxt
        this.textView = textView
    }
}

class URLDrawable(context: Context) : BitmapDrawable() {
    var drawable: Drawable?
    override fun draw(canvas: Canvas) {
        if (drawable != null) {
            drawable!!.draw(canvas)
        }
    }

    fun getDefaultImageBounds(context: Context): Rect {
        //val Width = drawable!!.getIntrinsicWidth()
        //val Height = drawable!!.getIntrinsicHeight()
        return Rect(0, 0, 70, 70)
    }

    init {
        this.bounds = getDefaultImageBounds(context)
        drawable = context.resources.getDrawable(R.mipmap.ic_launcher)
        drawable!!.setBounds(getDefaultImageBounds(context))
    }
}
*/

// ----^^^----  <TextView>公告內文的圖片顯示重構functions 目前已用 <WebView>  ----^^^----