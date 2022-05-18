package com.example.pccu.Page.Live_Image

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.example.pccu.Internet.Estimate
import com.example.pccu.Internet.NameType
import java.io.Serializable

/**即時影像基礎資料 -資料結構
 * @param Name [String] 影像名稱
 * @param Url [String] 影像來源
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraUrls(
    val Name:String,
    val Url:String
) : Serializable

/**即時影像基礎資料 -資料結構
 * @param Name [String] 影像名稱
 * @param Url [String] 影像來源
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraSourceData(
    val useClassification : String,
    val Data : List<CameraUrls>
) : Serializable

/**即時影像來源 (URL)
 * @author KILNETA
 * @since Alpha_2.0
 */
object UrlSource{

    /** ！！！ 使用提示 ！！！
     *
     * 文化即時影像
     * 影片 - https://mobi.pccu.edu.tw/campusview/View.aspx?ip=140.137.99.{相機編號} &name={網站標題}
     * 圖片 - https://camera.pccu.edu.tw/camera{相機編號}/images/live.jpg
     *
     * 北市即時影像
     * 影片 - https://cctv.bote.gov.taipei:8501/mjpeg/{相機編號}
     * 圖片 - https://cctv.bote.gov.taipei:8502/jpg/{相機編號}
     */

    /**文化大學即時影像截圖連結
     * @param [String] [0] 後山風景
     * @param [String] [1] 曉峰大成館方向
     * @param [String] [2] 計程車停車場
     * @param [String] [3] 百花池方向
     * @param [String] [4] 大賢大倫櫻花步道
     * @param [String] [5] 校車公車亭
     * @param [String] [6] 大仁球場
     * @param [String] [7] 曉峰廣場
     * @param [String] [8] 大義方向
     * @param [String] [9] 仇人坡
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val PccuCameras = CameraSourceData(
        "PCCU",
        listOf(
            CameraUrls("後山風景",      "122"),
            CameraUrls("曉峰大成館方向" ,"124"),
            CameraUrls("計程車停車場",   "95"),
            CameraUrls("百花池方向",     "98"),
            CameraUrls("大賢大倫櫻花步道","96"),
            CameraUrls("校車公車亭",     "93"),
            CameraUrls("大仁球場",       "92"),
            CameraUrls("曉峰廣場",       "91"),
            CameraUrls("大義方向",       "123"),
            CameraUrls("仇人坡",         "94")
        )
    )

    /**仰德大道即時影像連結
     * @param [String] [0] 文化附近
     * @param [String] [1] 華興附近
     * @param [String] [2] 山下橋頭
     * @param [String] [3] 士林官邸
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val YangdeAvenue= CameraSourceData(
        "TaipeiCity",
        listOf(
            CameraUrls("文化附近", "373"),
            CameraUrls("華興附近", "282"),
            CameraUrls("山下橋頭", "234"),
            CameraUrls("士林官邸", "235")
        )
    )

    /**劍潭即時影像連結
     * @param [String] [0] 中山劍潭(東側)
     * @param [String] [1] 劍潭路-中山北路口
     * @param [String] [2] 中山北路5段與文林路口
     * @param [String] [3] 劍潭路-承德路口
     * @param [String] [4] 中山北路-通河街口
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val Jiantan= CameraSourceData(
        "TaipeiCity",
        listOf(
            CameraUrls("中山劍潭(東側)", "157"),
            CameraUrls("劍潭路-中山北路口", "250"),
            CameraUrls("中山北路5段與文林路口", "326"),
            CameraUrls("劍潭路-承德路口", "251"),
            CameraUrls("中山北路-通河街口", "232")
        )
    )

    /**後山即時影像連結
     * @param [String] [0] 芝山站附近
     * @param [String] [1] 忠誠士東
     * @param [String] [2] 至誠雙溪
     * @param [String] [3] 中山福國
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val BackHill= CameraSourceData(
        "TaipeiCity",
        listOf(
            CameraUrls("芝山站附近", "231"),
            CameraUrls("忠誠士東", "297"),
            CameraUrls("至誠雙溪", "327"),
            CameraUrls("中山福國", "231")
        )
    )

    fun PCCU_ImageLink(Source:CameraUrls) : String {
        return "https://camera.pccu.edu.tw/" +      //根網域
                "camera" + Source.Url +             //相機編號
                "/images/live.jpg"                  //圖片來源
    }

    fun PCCU_ExternalLink(Source:CameraUrls) : String {
        return "https://mobi.pccu.edu.tw/campusview/" +   //根網域
                "View.aspx?ip=140.137.99." + Source.Url + //相機編號
                "&name=" + Source.Name                    //網站標題
    }

    fun TaipeiCity_ImageLink(Source:CameraUrls) : String {
        return "https://cctv.bote.gov.taipei:8502/jpg/" + Source.Url //相機編號
    }

    fun TaipeiCity_ExternalLink(Source:CameraUrls) : String {
        return "https://cctv.bote.gov.taipei:8501/mjpeg/" + Source.Url //相機編號
    }

    fun getClassificationUrl( source:CameraSourceData , index:Int , getType:Int) : String {
        when(source.useClassification){
            "PCCU"->
                when(getType){
                    0 -> return PCCU_ImageLink(source.Data[index])
                    1 -> return PCCU_ExternalLink(source.Data[index])
                }
            "TaipeiCity"->
                when(getType){
                    0 -> return TaipeiCity_ImageLink(source.Data[index])
                    1 -> return TaipeiCity_ExternalLink(source.Data[index])
                }
        }
        return ""
    }
}

/**首頁用時段推播即時影像 (URL) 三個影像一組
 * @author KILNETA
 * @since Alpha_2.0
 */
object PeriodRecommend{

    /**首頁用時段推播文化大學即時影像截圖連結
     * @param [String] [0] AM.12 - AM.07
     * @param [String] [1] AM.07 - AM.10
     * @param [String] [2] AM.10 - PM.03
     * @param [String] [3] PM.03 - PM.07
     * @param [String] [4] PM.07 - AM.12
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val CamerasPeriodUrl : List<List<CameraUrls>> = listOf(
        listOf( // AM.12 - AM.7
            UrlSource.PccuCameras.Data[4],
            UrlSource.PccuCameras.Data[2],
            UrlSource.PccuCameras.Data[0]
        ),
        listOf( // AM.7 - AM.10
            UrlSource.PccuCameras.Data[9],
            UrlSource.PccuCameras.Data[4],
            UrlSource.PccuCameras.Data[7]
        ),
        listOf( // AM.10 - PM.3
            UrlSource.PccuCameras.Data[9],
            UrlSource.PccuCameras.Data[3],
            UrlSource.PccuCameras.Data[7]
        ),
        listOf( // PM.3 - PM.7
            UrlSource.PccuCameras.Data[6],
            UrlSource.PccuCameras.Data[2],
            UrlSource.PccuCameras.Data[5]
        ),
        listOf( // PM.7 - AM.12
            UrlSource.PccuCameras.Data[6],
            UrlSource.PccuCameras.Data[8],
            UrlSource.PccuCameras.Data[7]
        )
    )
}

/**首頁用時段推播即時影像 (URL) 三個影像一組
 * @author KILNETA
 * @since Alpha_2.0
 */
object MoreCameras{

    /**首頁用時段推播文化大學即時影像截圖連結
     * @param [CameraSourceData] [0] 文化
     * @param [CameraSourceData] [1] 仰德
     * @param [CameraSourceData] [2] 劍潭
     * @param [CameraSourceData] [3] 後山
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val CameraSource : List<CameraSourceData> = listOf(
        UrlSource.PccuCameras,
        UrlSource.YangdeAvenue,
        UrlSource.Jiantan,
        UrlSource.BackHill
    )
}



/**影像控件 -數據結構
 * @param titel [TextView] 影像標題控件編號
 * @param imageView [ImageView] 影像圖片控件編號
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraItem(
    val titel: TextView,
    val imageView: ImageView,
)
