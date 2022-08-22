package com.example.pccu.Internet

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

/**
 * PCCU公告內容HTML解析器
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class ContentParser{

    /**
     * 取得PCCU公告內容資料
     * @param Html [String] 來源網址
     * @return 公告內文資料 : [Announcement_Content]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun getContent(Html: String): Announcement_Content? {
        //初始化 公告資料結構
        val Content = Announcement_Content()
        //擷取至公告主要資料內容區
        val document = Jsoup.parse(Html).select("tbody tbody tbody td[valign=top] tbody")//.outerHtml()
        //計算公告的tr總數來判斷 公告特定元素的位置 正常=9
        //      有些特殊公告內文含有<tr> 不另外計數會出現資料爬取溢位
        //      一般來說 主旨、內文 <tr>位置不會被影響
        //      相關連結 永遠是最後一個<tr>
        var tr_quantity = document.select("tr").size-1
        // vvv --- 曾有特例會發生越界偵測<tr>產生錯誤數據 解法 --- vvv
        if(document.select("tr")[tr_quantity].html() == "<td height=\"12\">&nbsp;</td>")
            tr_quantity--


        /*---------vvv 擷取公告資料 放入 公告資料結構 function() vvv---------*/
        Log.e("testttt" , document.select("tr")[tr_quantity].outerHtml())
        //主旨
        pushSubject(document.select("tr")[0].select("td")[1],Content)
        //內文
        pushText(document.select("tr")[1].select("td")[1],Content)
        //公告起訖
        pushAnnouncementTimes(document.select("tr")[tr_quantity-7].select("td")[1],Content)
        //活動起迄
        pushActiveTimes(document.select("tr")[tr_quantity-6].select("td")[1],Content)
        //公告分類
        pushClassification(document.select("tr")[tr_quantity-5].select("td")[1],Content)
        //公告單位
        pushUnit(document.select("tr")[tr_quantity-4].select("td")[1],Content)
        //點閱率
        pushCTR(document.select("tr")[tr_quantity-3].select("td")[1],Content)
        //附件
        pushAppendix(document.select("tr")[tr_quantity-2].select("td")[1],Content)
        //活動資訊
        pushActivityInformation(document.select("tr")[tr_quantity-1].select("td")[1],Content)
        //相關連結
        pushRelatedLinks(document.select("tr")[tr_quantity].select("td")[1],Content)

        /*---------^^^ 擷取公告資料 放入 公告資料結構 function() ^^^---------*/

        //圖片連結讀取 VVV
        //td.oneTitleContent
        //Log.d("upDatas", document.select("img[alt][src!=images/SubMenu12un.jpg]").outerHtml())

        // 返回PCCU公告內容資料
        return Content
    }
}

/**
 * 取得PCCU公告內容資料 (主旨)
 * @param subjectText [Element] 主旨HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushSubject(subjectText: Element , Content: Announcement_Content){
    /*  vvv subjectText 範例 vvv
    <td colspan="3" valign="top" class="oneTitleContent">
        公告本校111學年度碩士班考試入學招生備取生可遞補名單
    </td>
    */

    //匯入公告資料結構
    Content.SetSubject(subjectText.text())

    //後台測試
    Log.d("主旨", "${Content.Subject}") //主旨
}

/**
 * 取得PCCU公告內容資料 (內文)
 * @param contentHTML [Element] 內文HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushText(contentHTML: Element , Content: Announcement_Content){
    /*  vvv subjectText 範例 vvv
    <td valign="top" class="oneContent">
        <p>因應遠距教學(5/2-5/8)實施,交通車班次調整時刻發車,</p>
        <p>教職員專車詳細發車時刻表請點選相關網址查閱.</p>
        <p>遠距教學期間, 學生專車、通勤專車(通勤18、通勤19、通勤25)停開, 5/9後恢復行駛</p>
    </td>
    */
    for (i in 0 until contentHTML.childrenSize()) {
        Content.SetText(contentHTML.child(i))
        //後台測試
        //Log.d("內文", "${contentHTML.child(i).outerHtml()}") //內文
    }

    //後台測試
    //for (i in 0 until contentHTML.childrenSize())
    //    Log.d("內文${i}", "${Content.GetText()!![i]}")

}

/**
 * 取得PCCU公告內容資料 (公告起訖)
 * @param announcementTimes [Element] 公告起訖HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushAnnouncementTimes(announcementTimes: Element , Content: Announcement_Content){
    /*  vvv subjectText 範例 vvv
    <td nowrap="" class="oneContentLeft">
        <span class="postDate">
            起 - 2022/4/28 16:00
            <br>                        <---需要分段
            迄 - 2022/7/31 17:00
        </span>
    </td>
    */

    //從<br>分段 "起"、"迄" 時間
    val parts = announcementTimes.select("span").html().split(" <br>")
    //匯入公告資料結構
    Content.SetAnnouncementTimes(parts[0],parts[1])

    //後台測試
    //Log.d("公告起訖", "${Content.GetAnnouncementTimes().startDate}~${Content.GetAnnouncementTimes().endDate}") //公告起訖
}

/**
 * 取得PCCU公告內容資料 (活動起迄)
 * @param activeTimes [Element] 活動起迄HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushActiveTimes(activeTimes: Element , Content: Announcement_Content){
    /*  vvv subjectText 範例 vvv
    <td nowrap="" class="oneContentLeft">
		<span class="startDate">
		    起 - 2022/4/27 09:00
		</span>
		<br>
		<span class="endDate">
		    迄 - 2022/7/20 17:00
		</span>
	</td>
    */

    //匯入公告資料結構
    Content.SetActiveTimes( activeTimes.select("span[class=startDate]").text() ,
                            activeTimes.select("span[class=endDate]").text())

    //後台測試
    //Log.d("活動起迄", "${Content.GetActiveTimes().startDate}~${Content.GetActiveTimes().endDate}") //活動起迄
}

/**
 * 取得PCCU公告內容資料 (公告分類)
 * @param classification [Element] 公告分類HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushClassification(classification: Element , Content: Announcement_Content){
    /*  vvv subjectText 範例 vvv
     <td nowrap="" class="oneContentLeft">
        <span class="catoText">
            活動及競賽
        </span>
    </td>
    */

    //匯入公告資料結構
    Content.SetClassification(classification.text())

    //後台測試
    //Log.d("公告分類", "${Content.GetClassification()}") //公告分類
}

/**
 * 取得PCCU公告內容資料 (公告單位)
 * @param unitText [Element] 公告單位HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushUnit(unitText: Element , Content: Announcement_Content){
    /*  vvv unitText 範例 vvv
    <td nowrap="" class="oneContentLeft">
        <span class="unitText">
		    教學資源中心-學資組
		</span>　
	    <span class="catoText">
	        詹詠筑
	    </span>
	</td>
    */

    //匯入公告資料結構
    Content.SetUnit( unitText.select("span[class=unitText]").text(),
                     unitText.select("span[class=catoText]").text())

    //後台測試
    //Log.d("公告單位", "Unit = ${Content.GetUnit().GetUnit()} , Cato = ${Content.GetUnit().GetCato()}") //公告單位
}

/**
 * 取得PCCU公告內容資料 (點閱率)
 * @param CTRText [Element] 點閱率HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushCTR(CTRText: Element , Content: Announcement_Content){
    /*  vvv CTRText 範例 vvv
    <td colspan="3" valign="top" class="oneTitleContent">
        公告本校111學年度碩士班考試入學招生備取生可遞補名單
    </td>
    */

    //匯入公告資料結構
    Content.SetCTR(CTRText.text())

    //後台測試
    //Log.d("點閱率", "${Content.GetCTR()}") //點閱率
}

/**
 * 取得PCCU公告內容資料 (附件)
 * @param appendixText [Element] 附件HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushAppendix(appendixText: Element , Content: Announcement_Content){
    /*  vvv appendixText 範例 vvv
    <span id="file_{0C51654F-6043-4936-B165-76662F430581}">
        <img src="images/att.gif" width="16" height="16">
        <a href="https://ap2.pccu.edu.tw/postSystem/down.ashx?fid2=%7B0C51654F%2D6043%2D4936%2DB165%2D76662F430581%7D" class="css_file">
            111年暑假實習生需求總表.pdf
        </a>
    </span>
     */
    for(i in 0 until (appendixText.outerHtml().split("<span id").size - 1)){
        //分別記錄 Link、Text
        val LinkText = appendixText.select("span")[i].select("a").outerHtml()
        //匯入公告資料結構
        Content.SetAppendix(LinkText)
    }
    //後台測試
    /*
    for(i in 0 until Content.GetAppendix().size){
        Log.d("附件${i+1}", "Link = ${Content.GetAppendix()[i].GetLink()} , Text = ${Content.GetAppendix()[i].GetText()}") //附件
    }
    */
}

/**
 * 取得PCCU公告內容資料 (活動資訊)
 * @param activityInformationText [Element] 活動資訊HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushActivityInformation(activityInformationText: Element , Content: Announcement_Content){
    /*  vvv activityInformationText 範例 vvv
    <span id="act_17696">
        <a href="http://event.pccu.edu.tw/?dataAttr=1&amp;actSno=17696" target="_blank">
            1.0425企業徵才說明會-褒綠美股份有限公司&薩摩亞商新茂環球有限公司台灣分公司（oppo） ／
        </a> <br>
    </span>
    */

    for(i in 0 until (activityInformationText.outerHtml().split("<span id").size - 1)){
        //分別記錄 Link、Text
        val LinkText = activityInformationText.select("span")[i].select("a").outerHtml()
        //匯入公告資料結構
        Content.SetActivityInformation(LinkText)
    }
    //後台測試
    /*
    for(i in 0 until Content.GetActivityInformation().size){
        Log.d("活動資訊${i+1}", "Link = ${Content.GetActivityInformation()[i].GetLink()} , Text = ${Content.GetActivityInformation()[i].GetText()}") //活動資訊
    }
    */
}

/**
 * 取得PCCU公告內容資料 (相關連結)
 * @param relatedLinksText [Element] 相關連結HTML節點
 * @param Content [Announcement_Content] 公告內文資料
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
fun pushRelatedLinks(relatedLinksText: Element , Content: Announcement_Content){
    /*  vvv relatedLinksText 範例 vvv
    <img src="images/link.gif" width="16" height="16">
    <a href="https://ap2.pccu.edu.tw/freshmanwish/fwish000stu.aspx?kind=01" target="_blank">
        https://ap2.pccu.edu.tw/freshmanwish/fwish000stu.aspx?kind=01
    </a>&nbsp;
    */

    for(i in 0 until (relatedLinksText.outerHtml().split("<a href").size - 1)) {
        //分別記錄 Link、Text
        val LinkText = relatedLinksText.select("a").outerHtml()
        //匯入公告資料結構
        Content.SetRelatedLinks(LinkText)
    }
    //後台測試
    /*
    for(i in 0 until Content.GetRelatedLinks().size){
        Log.d("相關連結${i+1}", "Link = ${Content.GetRelatedLinks()[i].GetLink()} , Text = ${Content.GetRelatedLinks()[i].GetText()}") //相關連結
    }
    */
}

/**
 * 時間起迄 -數據結構
 * @param startDate     [String] 開始時間
 * @param endDate       [String] 結束時間
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class Time_Start_End(startDate: String, endDate: String) {
    var startDate: String? = startDate
    var endDate: String? = endDate
}

/**
 * 公告單位、負責人 -數據結構
 * @param Unit     [String] 單位
 * @param Cato     [String] 負責人
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class UnitText(Unit: String, Cato: String) {
    var Unit: String? = Unit
    var Cato: String? = Cato
}

/**
 * 公告資料 -數據結構
 * @param Subject               [String] 主旨
 * @param Text                  Vector<[Element]> 內文節點表
 * @param Appendix              Vector<[String]> 附件表
 * @param ActivityInformation   Vector<[String]> 活動資訊表
 * @param RelatedLinks          Vector<[String]> 相關連結表
 * @param AnnouncementTimes     [Time_Start_End] 公告起訖
 * @param ActiveTimes           [Time_Start_End] 活動起迄
 * @param Classification        [String] 公告分類
 * @param Unit                  [UnitText] 公告單位
 * @param CTR                   [String] 點閱率
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class Announcement_Content (
    var Subject: String? = null,                                    //主旨
    var Text: Vector<Element> = Vector<Element>(),                  //內文
    var Appendix: Vector<String> = Vector<String>(),                //附件
    var ActivityInformation: Vector<String> = Vector<String>(),     //活動資訊
    var RelatedLinks: Vector<String> = Vector<String>(),            //相關連結
    var AnnouncementTimes: Time_Start_End = Time_Start_End("", ""), //公告起訖
    var ActiveTimes: Time_Start_End = Time_Start_End("", ""),       //活動起迄
    var Classification: String? = null,                             //公告分類
    var Unit: UnitText = UnitText("", ""),                //公告單位
    var CTR: String? = null                                         //點閱率
){

    /**存儲主旨
     * @param Subject    [String] 主旨
     */
    fun SetSubject(Subject: String){
        this.Subject = Subject
    }

    /**存儲內文
     * @param Text    [Element] 內文節點
     */
    fun SetText(Text: Element){
        this.Text.add(Text)
    }

    /**存儲附件
     * @param Appendix    [String] 附件
     */
    fun SetAppendix(Appendix: String){
        this.Appendix.add(Appendix)
    }

    /**存儲活動資訊
     * @param ActivityInformation    [String] 活動資訊
     */
    fun SetActivityInformation(ActivityInformation: String){
        this.ActivityInformation.add(ActivityInformation)
    }

    /**存儲活動資訊
     * @param ActivityInformation    [String] 活動資訊
     */
    fun SetRelatedLinks(RelatedLinks: String){
        this.RelatedLinks.add(RelatedLinks)
    }

    /**存儲公告起訖
     * @param startDate  [String] 開始時間
     * @param endDate    [String] 結束時間
     */
    fun SetAnnouncementTimes(startDate: String, endDate: String){
        this.AnnouncementTimes = Time_Start_End(startDate,endDate)
    }

    /**存儲活動起迄
     * @param startDate  [String] 開始時間
     * @param endDate    [String] 結束時間
     */
    fun SetActiveTimes(startDate: String, endDate: String){
        this.ActiveTimes = Time_Start_End(startDate,endDate)
    }

    /**存儲公告分類
     * @param Classification  [String] 公告分類
     */
    fun SetClassification(Classification: String){
        this.Classification = Classification
    }

    /**存儲公告單位
     * @param Unit  [String] 公告單位
     * @param Cato  [String] 負責人
     */
    fun SetUnit(Unit: String, Cato: String){
        this.Unit = UnitText(Unit, Cato)
    }

    /**存儲點閱率
     * @param CTR  [String] 點閱率
     */
    fun SetCTR(CTR: String){
        this.CTR = CTR
    }
}