package com.pccu.pccu.internet

import android.widget.ImageView
import android.widget.TextView
import java.io.Serializable

/**
 * 即時影像API "class"
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
object CameraAPI {
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
     * @param [index] [0] 後山風景
     * @param [index] [1] 曉峰大成館方向
     * @param [index] [2] 計程車停車場
     * @param [index] [3] 百花池方向
     * @param [index] [4] 大賢大倫櫻花步道
     * @param [index] [5] 校車公車亭
     * @param [index] [6] 大仁球場
     * @param [index] [7] 曉峰廣場
     * @param [index] [8] 大義方向
     * @param [index] [9] 仇人坡
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private val PccuCameras = CameraSourceData(
        "文化",
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
     * @param [index] [0] 文化附近
     * @param [index] [1] 華興附近
     * @param [index] [2] 山下橋頭
     * @param [index] [3] 士林官邸
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private val YangdeAvenue= CameraSourceData(
        "仰德",
        "TaipeiCity",
        listOf(
            CameraUrls("文化附近", "373"),
            CameraUrls("華興附近", "282"),
            CameraUrls("山下橋頭", "234"),
            CameraUrls("士林官邸", "235")
        )
    )

    /**劍潭即時影像連結
     * @param [index] [0] 中山劍潭(東側)
     * @param [index] [1] 劍潭路-中山北路口
     * @param [index] [2] 中山北路5段與文林路口
     * @param [index] [3] 劍潭路-承德路口
     * @param [index] [4] 中山北路-通河街口
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private val Jiantan= CameraSourceData(
        "劍潭",
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
     * @param [index] [0] 芝山站附近
     * @param [index] [1] 忠誠士東
     * @param [index] [2] 至誠雙溪
     * @param [index] [3] 中山福國
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private val BackHill= CameraSourceData(
        "後山",
        "TaipeiCity",
        listOf(
            CameraUrls("芝山站附近", "307"),
            CameraUrls("忠誠士東", "297"),
            CameraUrls("至誠雙溪", "327"),
            CameraUrls("中山福國", "231")
        )
    )

    /**文大即時影像連結 (縮圖)
     * @param Source [CameraUrls] 相機編號
     * @return 文大即時影像(縮圖) : [String]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun pccuImageLink(Source: CameraUrls) : String {
        return "https://camera.pccu.edu.tw/" +      //根網域
                "camera" + Source.Url +             //相機編號
                "/images/live.jpg"                  //圖片來源
    }

    /**文大即時影像連結 (影片)
     * @param Source [CameraUrls] 相機編號
     * @return 文大即時影像(影片) : [String]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun pccuExternalLink(Source: CameraUrls) : String {
        return "https://mobi.pccu.edu.tw/campusview/" +   //根網域
                "View.aspx?ip=140.137.99." + Source.Url + //相機編號
                "&name=" + Source.Name                    //網站標題
    }

    /**北市交通即時影像連結 (縮圖)
     * @param Source [CameraUrls] 相機編號
     * @return 北市交通即時影像(縮圖) : [String]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun taipeiCityImageLink(Source: CameraUrls) : String {
        return "https://cctv.bote.gov.taipei:8502/jpg/" + Source.Url //相機編號
    }

    /**北市交通即時影像連結 (影片)
     * @param Source [CameraUrls] 相機編號
     * @return 北市交通即時影像(影片) : [String]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private fun taipeiCityExternalLink(Source: CameraUrls) : String {
        return "https://cctv.bote.gov.taipei:8501/mjpeg/" + Source.Url //相機編號
    }

    fun getClassificationUrl(source: CameraSourceData, index:Int, getType:Int) : String {
        when(source.useClassification){
            "PCCU"->
                when(getType){
                    0 -> return pccuImageLink(source.Data[index])
                    1 -> return pccuExternalLink(source.Data[index])
                }
            "TaipeiCity"->
                when(getType){
                    0 -> return taipeiCityImageLink(source.Data[index])
                    1 -> return taipeiCityExternalLink(source.Data[index])
                }
        }
        return ""
    }

    /**首頁推薦輪播最大影像數*/
    const val PeriodSize = 3

    /**首頁用時段推播文化大學即時影像截圖連結
     * @param [index] [0] AM.12 - AM.07
     * @param [index] [1] AM.07 - AM.10
     * @param [index] [2] AM.10 - PM.03
     * @param [index] [3] PM.03 - PM.07
     * @param [index] [4] PM.07 - AM.12
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    private val CamerasPeriodUrl : List<List<CameraUrls>> =
        listOf(
            listOf( // AM.12 - AM.7
                PccuCameras.Data[4],
                PccuCameras.Data[2],
                PccuCameras.Data[0]
            ),
            listOf( // AM.7 - AM.10
                PccuCameras.Data[9],
                PccuCameras.Data[4],
                PccuCameras.Data[7]
            ),
            listOf( // AM.10 - PM.3
                PccuCameras.Data[9],
                PccuCameras.Data[3],
                PccuCameras.Data[7]
            ),
            listOf( // PM.3 - PM.7
                PccuCameras.Data[6],
                PccuCameras.Data[2],
                PccuCameras.Data[5]
            ),
            listOf( // PM.7 - AM.12
                PccuCameras.Data[6],
                PccuCameras.Data[8],
                PccuCameras.Data[7]
            )
        )

    fun periodRecommendCameras( Time_H:  Int): List<CameraUrls>{
        return when(Time_H){
            in  0.. 6 -> CamerasPeriodUrl[0] //AM.12 - AM.07
            in  7.. 9 -> CamerasPeriodUrl[1] //AM.07 - AM.10
            in 10..14 -> CamerasPeriodUrl[2] //AM.10 - PM.03
            in 15..18 -> CamerasPeriodUrl[3] //PM.03 - PM.07
            else      -> CamerasPeriodUrl[4] //PM.07 - AM.12
        }
    }

    /**首頁用時段推播文化大學即時影像截圖連結
     * @param [index] [0] 文化
     * @param [index] [1] 仰德
     * @param [index] [2] 劍潭
     * @param [index] [3] 後山
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    val CameraSource : List<CameraSourceData> = listOf(
        PccuCameras,
        YangdeAvenue,
        Jiantan,
        BackHill
    )
}

/**即時影像基礎資料 -資料結構
 * @param Name  [String] 影像名稱
 * @param Url   [String] 影像來源
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraUrls(
    val Name:String,
    val Url:String
) : Serializable

/**即時影像來源資料 -資料結構
 * @param useClassification [String] 使用分類
 * @param Data List<[CameraUrls]> 影像來源表
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraSourceData(
    val locationName : String,
    val useClassification : String,
    val Data : List<CameraUrls>
) : Serializable

/**影像控件 -數據結構
 * @param title [TextView] 影像標題控件編號
 * @param imageView [ImageView] 影像圖片控件編號
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
data class CameraItem(
    val title: TextView,
    val imageView: ImageView
)
