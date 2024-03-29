package com.pccu.pccu.internet

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
class PccuAnnouncementXml {

    /**
     * 連線取得PCCU公告列表XML檔
     * @return PCCU公告XML字串流 : [InputStream]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun get(): InputStream? {
        val url = "https://ap2.pccu.edu.tw/postrss/createrss.aspx"
        return HttpRetrofit.createXML(url)
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
     * @return 公告列表 : Vector<[AnnouncementData]>
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    @Throws(Throwable::class)
    fun getAnnouncements(inputStream: InputStream?): Vector<AnnouncementData>? {
        //最終輸出的公告資訊列表
        val announcementDatas: Vector<AnnouncementData> = Vector<AnnouncementData>()
        //擷取單個公告資訊
        var mAnnouncementData: AnnouncementData? = null

        if(inputStream==null) return null

        //創建XmlPullParser
        val xmlPullParser = Xml.newPullParser()
        //解析文件輸入流
        xmlPullParser.setInput(inputStream, "UTF-8")

        //得到當前的解析對象
        var eventType = xmlPullParser.eventType
        //當解析工作還沒完成時，調用next（）方法得到下一個解析事件

        while( "channel" == xmlPullParser.name){
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
                        mAnnouncementData = AnnouncementData()
                    }
                    //如過 mAnnouncement_Data 已經重製過 開始解析公告列表
                    if (mAnnouncementData != null) {
                        when(xmlPullParser.name) {
                            "title"->      //公告標題
                                mAnnouncementData.setupTitle(xmlPullParser.nextText())
                            "link"->       //公告連結
                                mAnnouncementData.setupLink(xmlPullParser.nextText())
                            "source"->     //外部資源
                                mAnnouncementData.setupSource(xmlPullParser.nextText())
                            "enclosure"->  //顯示資源
                                mAnnouncementData.setupEnclosure(xmlPullParser.nextText())
                            "author"->     //公告處室
                                mAnnouncementData.setupAuthor(xmlPullParser.nextText())
                            "pubDate"->    //公告時間
                                mAnnouncementData.setupPubDate(xmlPullParser.nextText())
                        }
                    }
                }
                //如果遇到結束標籤 -> 存儲擷取的資料到 回傳用的Vector<Announcement_Data>中
                XmlPullParser.END_TAG -> if ("item" == xmlPullParser.name) {
                    announcementDatas.add(mAnnouncementData!!)
                    mAnnouncementData = null
                }
                //其他操作 -> NULL
                else -> {
                }
            }
            //到下一個節點
            eventType = xmlPullParser.next()
        }
        //回傳 資料列表Vector<Announcement_Data>
        return announcementDatas
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
data class AnnouncementData (
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
    fun setupTitle(title: String?) {
        this.title = title
    }

    /**存儲公告連結
     * @param link    [String] 公告連結
     */
    fun setupLink(link: String?) {
        this.link = link
    }

    /**存儲外部資源
     * @param source    [String] 外部資源
     */
    fun setupSource(source: String?) {
        this.source = source
    }

    /**存儲顯示資源
     * @param enclosure    [String] 顯示資源
     */
    fun setupEnclosure(enclosure: String?) {
        if( enclosure!=null )
            this.enclosure = true
    }

    /**存儲公告處室
     * @param author    [String] 公告處室
     */
    fun setupAuthor(author: String?) {
        this.author = author
    }

    /**存儲公告時間
     * @param pubDate    [String] 公告時間
     */
    fun setupPubDate(pubDate: String?) {
        this.pubDate = pubDate
    }
}