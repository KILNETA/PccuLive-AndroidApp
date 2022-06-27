package com.example.pccu.Internet

import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import java.io.InputStream
import java.util.*

/**
 * 連線取得PCCU公告列表XML檔
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class PccuAnnouncement_Xml {

    /**
     * 連線取得PCCU公告列表XML檔
     * @return PCCU公告XML字串流 : [InputStream]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun Get(): InputStream {
        val Url = "https://ap2.pccu.edu.tw/postrss/createrss.aspx"
        return HttpRetrofit.create_XML(Url)
    }
}

/**
 * 使用XmlPullParser解析並擷取PCCU_Xml公告 "object"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
object AnnouncementByPULL {

    /**
     * 使用XmlPullParser解析 取得PCCU_XML公告列表
     * @param inputStream [InputStream] 公告XML字串流
     * @return 公告列表 : Vector<[Announcement_Data]>
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @Throws(Throwable::class)
    fun getAnnouncements(inputStream: InputStream?): Vector<Announcement_Data> {
        //最終輸出的公告資訊列表
        val announcement_Datas: Vector<Announcement_Data> = Vector<Announcement_Data>()
        //擷取單個公告資訊
        var mAnnouncement_Data: Announcement_Data? = null

        //創建XmlPullParser
        val xmlPullParser = Xml.newPullParser()
        //解析文件輸入流
        xmlPullParser.setInput(inputStream, "UTF-8")

        //得到當前的解析對象
        var eventType = xmlPullParser.eventType
        //當解析工作還沒完成時，調用next（）方法得到下一個解析事件

        while( "channel" == xmlPullParser.getName() ){
            xmlPullParser.nextTag()
        }
        //直到解析結束
        while (eventType != XmlPullParser.END_DOCUMENT) {
            //判斷eventType選擇適配的標籤
            when (eventType) {
                //如果遇到開始標籤 -> 逐行解剖
                XmlPullParser.START_TAG -> {
                    //獲得解析器當前指向的元素的名字
                    //當指向元素的名字和id,name,sex這些屬性重合時可以返回他們的值
                    if ("item" == xmlPullParser.name) {
                        //通過解析器獲取Title的元素值，並設置一個新的Announcement_Data對象的Title
                        mAnnouncement_Data = Announcement_Data()
                    }
                    //如過 mAnnouncement_Data 已經重製過 開始解析公告列表
                    if (mAnnouncement_Data != null) {
                        if (xmlPullParser.name == "title")      //公告標題
                            mAnnouncement_Data.SetTitle(xmlPullParser.nextText())
                        if (xmlPullParser.name == "link")       //公告連結
                            mAnnouncement_Data.SetLink(xmlPullParser.nextText())
                        if (xmlPullParser.name == "source")     //外部資源
                            mAnnouncement_Data.SetSource(xmlPullParser.nextText())
                        if (xmlPullParser.name == "enclosure")  //顯示資源
                            mAnnouncement_Data.SetEnclosure(xmlPullParser.nextText())
                        if (xmlPullParser.name == "author")     //公告處室
                            mAnnouncement_Data.SetAuthor(xmlPullParser.nextText())
                        if (xmlPullParser.name == "pubDate")    //公告時間
                            mAnnouncement_Data.SetPubDate(xmlPullParser.nextText())
                    }
                }
                //如果遇到結束標籤 -> 存儲擷取的資料到 回傳用的Vector<Announcement_Data>中
                XmlPullParser.END_TAG -> if ("item".equals(xmlPullParser.name)) {
                    announcement_Datas!!.add(mAnnouncement_Data!!)
                    mAnnouncement_Data = null
                }
                //其他操作 -> NULL
                else -> {
                }
            }
            //到下一個節點
            eventType = xmlPullParser.next()
        }
        //回傳 資料列表Vector<Announcement_Data>
        return announcement_Datas
    }
}

/**
 * 公告資料 -數據結構
 * @param title     [String] 公告標題
 * @param link      [String] 公告連結
 * @param source    [String] 外部資源
 * @param enclosure [String] 顯示資源
 * @param author    [String] 公告處室
 * @param pubDate   [String] 公告時間
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class Announcement_Data (

    var title: String? = null,      //公告標題
    var link: String? = null,       //公告連結
    var source: String? = null,     //外部資源 (一般是連結)
    var enclosure: Boolean? = null, //顯示資源 (一般是公告附件)(僅顯示有附件)
    var author: String? = null,     //公告處室
    var pubDate: String? = null     //公告時間
){

    /**存儲公告標題
     * @param title    [String] 公告標題
     */
    fun SetTitle(title: String?) {
        this.title = title
    }

    /**存儲公告連結
     * @param link    [String] 公告連結
     */
    fun SetLink(link: String?) {
        this.link = link
    }

    /**存儲外部資源
     * @param source    [String] 外部資源
     */
    fun SetSource(source: String?) {
        this.source = source
    }

    /**存儲顯示資源
     * @param enclosure    [String] 顯示資源
     */
    fun SetEnclosure(enclosure: String?) {
        if( enclosure!=null )
            this.enclosure = true
    }

    /**存儲公告處室
     * @param author    [String] 公告處室
     */
    fun SetAuthor(author: String?) {
        this.author = author
    }

    /**存儲公告時間
     * @param pubDate    [String] 公告時間
     */
    fun SetPubDate(pubDate: String?) {
        this.pubDate = pubDate
    }
}