package com.example.pccu.Internet

import android.util.Log
import org.xmlpull.v1.XmlPullParser

import android.util.Xml
import java.io.InputStream
import java.util.*

class PccuAnnouncement_Xml_API {

    fun Get(): InputStream? {
        val Url = "https://ap2.pccu.edu.tw/postrss/createrss.aspx"
        return HttpRetrofit.create(Url)
    }
}
object AnnouncementByPULL {

    //採用XmlPullParser來解析文件
    @Throws(Throwable::class)
    fun getAnnouncements(inputStream: InputStream?): Vector<Announcement_Data>? {
        var announcement_Datas: Vector<Announcement_Data>? = Vector<Announcement_Data>()
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
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    //獲得解析器當前指向的元素的名字
                    //當指向元素的名字和id,name,sex這些屬性重合時可以返回他們的值
                    if ("item" == xmlPullParser.name) {
                        //通過解析器獲取Title的元素值，並設置一個新的Announcement_Data對象的Title
                        mAnnouncement_Data = Announcement_Data()
                    }
                    if (mAnnouncement_Data != null) {
                        if ("title" == xmlPullParser.name)
                            mAnnouncement_Data.SetTitle(xmlPullParser.nextText())
                        if ("link" == xmlPullParser.name)
                            mAnnouncement_Data.SetLink(xmlPullParser.nextText())
                        if ("source" == xmlPullParser.name)
                            mAnnouncement_Data.SetSource(xmlPullParser.nextText())
                        if ("enclosure" == xmlPullParser.name)
                            mAnnouncement_Data.SetEnclosure(xmlPullParser.nextText())
                        if ("author" == xmlPullParser.name)
                            mAnnouncement_Data.SetAuthor(xmlPullParser.nextText())
                        if ("pubDate" == xmlPullParser.name)
                            mAnnouncement_Data.SetPubDate(xmlPullParser.nextText())
                    }
                }
                XmlPullParser.END_TAG -> if ("item".equals(xmlPullParser.name)) {
                    announcement_Datas!!.add(mAnnouncement_Data!!)
                    mAnnouncement_Data = null
                }
                else -> {
                }
            }
            eventType = xmlPullParser.next()
        }
        return announcement_Datas
    }
}

class Announcement_Data() {

    var title: String? = null      //公告標題
    var link: String? = null       //公告連結
    var source: String? = null     //外部資源 (一般是連結)
    var enclosure: Boolean? = null //顯示資源 (一般是公告附件)(僅顯示有附件)
    var author: String? = null     //公告處室
    var pubDate: String? = null     //公告時間

    fun SetTitle(title: String?) {
        this.title = title
    }
    fun GetTitle(): String? {
        return this.title
    }

    fun SetLink(link: String?) {
        this.link = link
    }
    fun GetLink(): String? {
        return this.link
    }

    fun SetSource(source: String?) {
        this.source = source
    }
    fun GetSource(): String? {
        return this.source
    }

    fun SetEnclosure(title: String?) {
        if( title!=null )
            this.enclosure = true
    }
    fun GetEnclosure(): Boolean? {
        return this.enclosure
    }

    fun SetAuthor(author: String?) {
        this.author = author
    }
    fun GetAuthor(): String? {
        return this.author
    }

    fun SetPubDate(pubDate: String?) {
        this.pubDate = pubDate
    }
    fun GetPubDate(): String? {
        return this.pubDate
    }
}