package com.example.pccu.Page.Cwb

import android.widget.ImageView
import android.widget.TextView

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

    val EPA_locationArea: List<String> = listOf(
        "北部",
        "竹苗",
        "中部",
        "雲嘉南",
        "高屏",
        "宜蘭",
        "花東",
        "澎湖",
        "金門",
        "馬祖"
    )

    /**縣市名*/
    val EPA_locations: List<List<String>> = listOf(
        listOf("基隆市", "臺北市", "新北市"),
        listOf("桃園市", "新竹市", "新竹縣", "苗栗縣"),
        listOf("臺中市", "彰化縣", "南投縣"),
        listOf("雲林縣", "嘉義市", "嘉義縣","臺南市"),
        listOf("高雄市", "屏東縣"),
        listOf("宜蘭縣"),
        listOf("花蓮縣", "臺東縣"),
        listOf("澎湖縣"),
        listOf("金門縣"),
        listOf("連江縣")
    )

    /**CWB圖資來源*/
    val CWB_ImageSource_Url: List<CWB_ImageUrl> = listOf(
        CWB_ImageUrl("雷達合成回波圖","https://www.cwb.gov.tw/Data/radar/CV1_TW_3600.png"),
        CWB_ImageUrl("衛星雲圖","https://www.cwb.gov.tw/Data/satellite/TWI_VIS_TRGB_1375/TWI_VIS_TRGB_1375.jpg"),
        CWB_ImageUrl("衛星紅外線雲圖","https://www.cwb.gov.tw/Data/satellite/TWI_IR1_CR_800/TWI_IR1_CR_800.jpg"),
        CWB_ImageUrl("累計雨量圖","https://www.cwb.gov.tw/Data/rainfall/QZJ.jpg"),
        CWB_ImageUrl("氣溫分布圖","https://www.cwb.gov.tw/Data/temperature/temp.jpg"),
        CWB_ImageUrl("健康氣象資料","https://www.cwb.gov.tw/Data/health/health_forPreview.png"),
        CWB_ImageUrl("紫外線資料圖","https://www.cwb.gov.tw/Data/UVI/UVI.png")
    )
}

/**CWB圖資來源 -數據結構
 * @param title [String] 圖資標題
 * @param imageUrl [String] 圖資來源連結
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
data class CWB_ImageUrl(
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
data class CWB_ImageItem(
    val title: TextView,
    val imageView: ImageView
)