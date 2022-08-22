package com.example.pccu.Page.Cwb

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.example.pccu.Internet.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EPA_API(val parent: Cwb_Home_Page) {

    val EPA_Url = "https://www.epa.gov.tw/Index"

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
    fun Get(){

        //取用協程
        GlobalScope.launch {
            //異地連接存取公告內文資料
            val announcementList =
                HttpRetrofit.create_Content(EPA_Url,"utf-8")

            //返回主線程
            withContext(Dispatchers.Main) {
                //重新設置公告的數據資料
                parent.setEPA_airQuality(EPA_HTMLParser.getContent(announcementList))
            }
        }
    }
}

/**
 * PCCU公告內容HTML解析器
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
object EPA_HTMLParser{

    val TimeFormat = "yyy-MM-dd HH:mm"

    /**
     * 取得PCCU公告內容資料
     * @param Html [String] 來源網址
     * @return 公告內文資料 : [Announcement_Content]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun getContent(Html: String): EPA_airQuality {
        //初始化 空氣品質結構
        val airQuality = EPA_airQuality()
        //擷取至空氣品質主要資料內容區
        val document = Jsoup.parse(Html).select("body section div[class=weather_main]")//.outerHtml()
        val document_UpDate = Jsoup.parse(Html).select("body section div[class=Mainnews4] blockquote")[2]//.outerHtml()
        //計算資料的li總數來判斷 空氣品質的資料數 正常=10
        val tr_quantity = document.outerHtml().split("<li").size-1

        airQuality.upDate = SimpleDateFormat(TimeFormat).parse(document_UpDate.text())
        airQuality.upDate.year += 1911
        Log.e("EPA_HTMLParser","${airQuality.upDate}")

        for( i in 0 until tr_quantity)
            airQuality.EPA_airQualityData.add(pushData(document.select("li[class=item]")[i]))

        return airQuality
    }

    fun pushData(classification: Element) : EPA_airQualityData{
        //Log.e("EPA_HTMLParser","${classification.text()}")

        val data = classification.text().split(' ')
        return EPA_airQualityData( data[0], data[1], data[2].toInt() )
    }
}

data class EPA_airQuality(
    var upDate : Date = Date(),
    val EPA_airQualityData : ArrayList<EPA_airQualityData> = ArrayList()
)

data class EPA_airQualityData(
    val locationArea : String? = null ,
    val airQualityName : String? = null ,
    val airQualityValue : Int? = null
)