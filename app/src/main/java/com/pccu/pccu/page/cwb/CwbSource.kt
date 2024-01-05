package com.pccu.pccu.page.cwb

import android.widget.ImageView
import android.widget.TextView

/**
 * 氣象資料 來源
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
object CwbSource {

    /**地區區域名*/
    val CWB_locationArea: List<String> = listOf(
        "北部",
        "中部",
        "南部",
        "東部",
        "外島"
    )

    /**縣市名*/
    val CWB_locations: List<List<String>> = listOf(
        listOf("基隆市", "臺北市", "新北市", "桃園市", "新竹市", "新竹縣", "苗栗縣"),
        listOf("臺中市", "彰化縣", "南投縣", "雲林縣", "嘉義市", "嘉義縣"),
        listOf("臺南市", "高雄市", "屏東縣"),
        listOf("宜蘭縣", "花蓮縣", "臺東縣"),
        listOf("澎湖縣", "金門縣", "連江縣")
    )

    /**空汙 縣市轉地區*/
    val EPA_locationArea: List<CwbToEpaLocation> = listOf(
        CwbToEpaLocation("北部",  listOf("基隆市", "臺北市", "新北市")),
        CwbToEpaLocation("竹苗",  listOf("桃園市", "新竹市", "新竹縣", "苗栗縣")),
        CwbToEpaLocation("中部",  listOf("臺中市", "彰化縣", "南投縣")),
        CwbToEpaLocation("雲嘉南",listOf("雲林縣", "嘉義市", "嘉義縣","臺南市")),
        CwbToEpaLocation("高屏",  listOf("高雄市", "屏東縣")),
        CwbToEpaLocation("宜蘭",  listOf("宜蘭縣")),
        CwbToEpaLocation("花東",  listOf("花蓮縣", "臺東縣")),
        CwbToEpaLocation("澎湖",  listOf("澎湖縣")),
        CwbToEpaLocation("金門",  listOf("金門縣")),
        CwbToEpaLocation("馬祖",  listOf("連江縣"))
    )

    /**CWB圖資來源*/
    val CWB_ImageSource_Url: List<CwbImageUrl> = listOf(
        CwbImageUrl("雷達合成回波圖","https://www.cwa.gov.tw/Data/radar/CV1_TW_3600.png"),
        CwbImageUrl("衛星雲圖","https://www.cwa.gov.tw/Data/satellite/TWI_VIS_TRGB_1375/TWI_VIS_TRGB_1375.jpg"),
        CwbImageUrl("衛星紅外線雲圖","https://www.cwa.gov.tw/Data/satellite/TWI_IR1_CR_800/TWI_IR1_CR_800.jpg"),
        CwbImageUrl("累計雨量圖","https://www.cwa.gov.tw/Data/rainfall/QZJ.jpg"),
        CwbImageUrl("氣溫分布圖","https://www.cwa.gov.tw/Data/temperature/temp.jpg"),
        CwbImageUrl("健康氣象資料","https://www.cwa.gov.tw/Data/health/health_forPreview.png"),
        CwbImageUrl("紫外線資料圖","https://www.cwa.gov.tw/Data/UVI/UVI.png")
    )
}

/**CWB縣市轉Epa地區 -數據結構
 * @param EpaLocation [String] 地區
 * @param CwbLocations [String] 縣市表
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbToEpaLocation(
    val EpaLocation: String,
    val CwbLocations: List<String>
)

/**CWB圖資來源 -數據結構
 * @param title [String] 圖資標題
 * @param imageUrl [String] 圖資來源連結
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbImageUrl(
    val title: String,
    val imageUrl: String
)

/**CWB圖資控件 -數據結構
 * @param title [TextView] 影像標題控件編號
 * @param imageView [ImageView] 影像圖片控件編號
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CwbImageItem(
    val title: TextView,
    val imageView: ImageView
)