package com.pccu.pccu.internet

import android.util.Log
import com.pccu.pccu.BuildConfig.TDX_Id_API_KEY
import com.pccu.pccu.BuildConfig.TDX_Secret_API_KEY
import okhttp3.ResponseBody
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * 交通部公車API InterFace "class"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
object BusAPI{

    /**TDX_API根網址*/
    private const val baseUrl = "https://tdx.transportdata.tw/"

    /**
     * 縣市中英對照
     * @author KILNETA
     * @since Alpha_5.0
     */
    val Locations = listOf(
        NameType("基隆市","Keelung"),
        NameType("台北市","Taipei"),
        NameType("新北市","NewTaipei"),
        NameType("桃園市","Taoyuan"),
        NameType("新竹市","Hsinchu"),
        NameType("新竹縣","HsinchuCounty"),
        NameType("苗栗縣","MiaoliCounty"),
        NameType("台中市","Taichung"),
        NameType("彰化縣","ChanghuaCounty"),
        NameType("雲林縣","YunlinCounty"),
        NameType("嘉義市","Chiayi"),
        NameType("嘉義縣","ChiayiCounty"),
        NameType("南投縣","NantouCounty"),
        NameType("台南市","Tainan"),
        NameType("高雄市","Kaohsiung"),
        NameType("屏東縣","PingtungCounty"),
        NameType("宜蘭縣","YilanCounty"),
        NameType("花蓮縣","HualienCounty"),
        NameType("台東縣","TaitungCounty"),
        NameType("金門縣","KinmenCounty"),
        NameType("澎湖縣","PenghuCounty"),
        NameType("連江縣","LienchiangCounty"),
    )

    /**
     * 支援已整理展示路線的縣市
     * @author KILNETA
     * @since Alpha_5.0
     */
    val DisplayStopOfRoute_Locations = listOf(
        "Taipei",
        "NewTaipei",
        "Taoyuan",
        "Tainan",
        "Taichung"
    )

    /**
     * 支援查詢收費規定的縣市
     * @author KILNETA
     * @since Alpha_5.0
     */
    val RouteFare = listOf(
        "Taipei",
        "NewTaipei",
        "Taoyuan",
        "Taichung",
        "Kaohsiung",
        "Hsinchu",
        "HsinchuCounty",
        "MiaoliCounty",
        "ChanghuaCounty",
        "NantouCounty",
        "YunlinCounty",
        "ChiayiCounty",
        "Chiayi",
        "PingtungCounty",
        "YilanCounty",
        "HualienCounty",
        "TaitungCounty",
        "KinmenCounty"
    )

    /**
     * 取得TdxToken
     * @return TdxToken : [TdxToken]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun getToken(): TdxToken? {
        val url = "https://tdx.transportdata.tw/"
        return  try {
            HttpRetrofit.createJson(HttpRetrofit.ApiService::class.java, url).getTdxToken(
                "application/x-www-form-urlencoded",
                "client_credentials",
                TDX_Id_API_KEY,
                TDX_Secret_API_KEY
            ).execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("","$e")
            null
        }
    }

    /**
     * 公車API查詢
     * @param token [TdxToken] API_token許可證
     * @param activity [String] API操作目標
     * @param request [BusApiRequest] 查詢用參數
     * @return Json文檔 : [ResponseBody]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun get(token:TdxToken, activity:String , request :BusApiRequest): ResponseBody? {
        //回傳 路線站點表
        return HttpRetrofit.createJson(
            HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
            baseUrl                                                    //根網域
        ).getTdxBus(
            "${token.token_type} ${token.access_token}",    //API許可證
            activity,                                                   //功能
            request.city,                                               //縣市
            if(request.routeName==null) "" else "/${request.routeName}",//車名(中文)
            request.filter,
            "JSON",                                              //返回檔案格式
        ).execute().body()
    }
}

/**
 * TDX_token API許可證 -數據結構
 * @param access_token          [String] API_token許可證
 * @param expires_in            [Int] API_token的有效期限
 * @param refresh_expires_in    [Int]
 * @param token_type            [String] token類型，固定為“Bearer”
 * @param not_before_policy     [Int]
 * @param scope                 [String]
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class TdxToken(
    val access_token: String?,         //用於存取API服務的token，格式為JWT
    val expires_in: Int,              //token的有效期限
    val refresh_expires_in: Int,      //
    val token_type: String?,           //token類型，固定為“Bearer”
    val `not-before-policy`: Int,     //
    val scope: String,                //
) : Serializable

/**
 * BusAPI基礎請求資料包
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusApiRequest(
    val city: String,
    val routeName: String? = null,
    val filter: String? = null,
) : Serializable

/**
 * 營運業者資料 -數據結構
 * @param OperatorID          [String] 營運業者代碼
 * @param OperatorName        [NameType] 營運業者名稱
 * @param OperatorCode        [String] 營運業者簡碼
 * @param OperatorNo          [String] 營運業者編號(交通部票證資料系統定義)
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class RouteOperator(
    val OperatorID: String,         //營運業者代碼
    val OperatorName: NameType,     //營運業者名稱
    val OperatorCode: String,       //營運業者簡碼
    val OperatorNo: String,         //營運業者編號[交通部票證資料系統定義]
) : Serializable

/**
 * 附屬路線資料 -數據結構
 * (如果原始資料並無提供附屬路線ID，而本平台基於跨來源資料之一致性，會以SubRouteID=RouteID產製一份相對應的附屬路線資料
 * (若有去返程，則會有兩筆) )
 * @param SubRouteUID           [String] 附屬路線唯一識別代碼
 * @param SubRouteID            ArrayList<[String]> 地區既用中之附屬路線代碼
 * @param OperatorIDs           [NameType] 營運業者代碼
 * @param SubRouteName          [String] 附屬路線名稱
 * @param Headsign              [String] 車頭描述
 * @param HeadsignEn            [String] 車頭英文描述
 * @param Direction             [NameType] 去返程 ( 0:去程,1:返程,2:迴圈,255:未知)
 * @param FirstBusTime          [String] 平日第一班發車時間
 * @param LastBusTime           [String] 平日返程第一班發車時間
 * @param HolidayFirstBusTime   [String] 假日去程第一班發車時間
 * @param HolidayLastBusTime    [String] 假日返程第一班發車時間
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusSubRoute(
    val SubRouteUID: String,        //附屬路線唯一識別代碼
                                    //  規則為 {業管機關簡碼} + {SubRouteID}
                                    //  其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,         //地區既用中之附屬路線代碼(為原資料內碼)
    val OperatorIDs: ArrayList<String>, //營運業者代碼
    val SubRouteName: NameType,     //附屬路線名稱
    val Headsign: String? = null,           //車頭描述
    val HeadsignEn: String,         //車頭英文描述
    val Direction: Int,             //去返程 ( 0:去程,1:返程,2:迴圈,255:未知)
    val FirstBusTime: String,       //平日第一班發車時間
    val LastBusTime: String,        //平日返程第一班發車時間
    val HolidayFirstBusTime: String,//假日去程第一班發車時間
    val HolidayLastBusTime: String, //假日返程第一班發車時間
) : Serializable

/**
 * 取得指定"縣市","路線名稱"的公車之路線資料 -數據結構
 * @param RouteUID                      [String] 路線唯一識別代碼
 * @param RouteID                       [String] 地區既用中之路線代碼
 * @param HasSubRoutes                  [Boolean] 是否有多條附屬路線
 * @param Operators                     ArrayList<[RouteOperator]> 營運業者
 * @param AuthorityID                   [String] 業管機關代碼
 * @param ProviderID                    [String] 資料提供平台代碼
 * @param SubRoutes                     ArrayList<[BusSubRoute]> 附屬路線資料
 * @param BusRouteType                  [Int] 公車路線類別 (11:市區公車,12:公路客運,13:國道客運,14:接駁車)
 * @param RouteName                     [NameType] 路線名稱
 * @param DepartureStopNameZh           [String] 起站中文名稱
 * @param DepartureStopNameEn           [String] 起站英文名稱
 * @param DestinationStopNameZh         [String] 終點站中文名稱
 * @param DestinationStopNameEn         [String] 終點站英文名稱
 * @param TicketPriceDescriptionZh      [String] 票價中文敘述
 * @param TicketPriceDescriptionEn      [String] 票價英文敘述
 * @param FareBufferZoneDescriptionZh   [String] 收費緩衝區中文敘述
 * @param FareBufferZoneDescriptionEn   [String] 收費緩衝區英文敘述
 * @param RouteMapImageUrl              [String] 路線簡圖網址
 * @param City                          [String] 路線權管所屬縣市
 * @param CityCode                      [String] 路線權管所屬縣市之代碼
 * @param UpdateTime                    [String] 資料更新日期時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
 * @param VersionID                     [Int] 資料版本編號
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusRoute(
    val RouteUID: String,                   //路線唯一識別代碼
                                            //  規則為 {業管機關簡碼} + {RouteID}
                                            //  其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,                    //地區既用中之路線代碼(為原資料內碼)
    val HasSubRoutes: Boolean,              //是否有多條附屬路線
                                            //  (此欄位值與SubRoutes結構並無強烈的絕對關聯。詳細說明請參閱swagger上方的【資料服務使用注意事項】)
    val Operators: ArrayList<RouteOperator>,    //營運業者
    val AuthorityID: String,                //業管機關代碼
    val ProviderID: String,                 //資料提供平台代碼
    val SubRoutes: ArrayList<BusSubRoute>,      //附屬路線資料
                                            //  (如果原始資料並無提供附屬路線ID，而本平台基於跨來源資料之一致性，會以SubRouteID=RouteID產製一份相對應的附屬路線資料
                                            //  (若有去返程，則會有兩筆))
    val BusRouteType: Int,                  //公車路線類別 (11:市區公車,12:公路客運,13:國道客運,14:接駁車)
    val RouteName: NameType,                //路線名稱
    val DepartureStopNameZh: String,        //起站中文名稱
    val DepartureStopNameEn: String,        //起站英文名稱
    val DestinationStopNameZh: String,      //終點站中文名稱
    val DestinationStopNameEn: String,      //終點站英文名稱
    val TicketPriceDescriptionZh: String,   //票價中文敘述
    val TicketPriceDescriptionEn: String,   //票價英文敘述
    val FareBufferZoneDescriptionZh: String,//收費緩衝區中文敘述
    val FareBufferZoneDescriptionEn: String,//收費緩衝區英文敘述
    val RouteMapImageUrl: String,           //路線簡圖網址
    val City: String,                       //路線權管所屬縣市
                                            //  (相當於市區公車API的City參數) (若為公路/國道客運路線則為空值)
    val CityCode: String,                   //路線權管所屬縣市之代碼
                                            //  (國際ISO 3166-2 三碼城市代碼) (若為公路/國道客運路線則為空值)
    val UpdateTime: String,                 //資料更新日期時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val VersionID: Int,                     //資料版本編號
) : Serializable
/**
 * 名字 -數據結構
 * @param Zh_tw     [String] 中文繁體名稱
 * @param En        [String] 英文名稱
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class NameType(
    val Zh_tw:String,               //中文繁體名稱
    val En:String                   //英文名稱
) : Serializable

/**
 * 經緯度  -數據結構
 * @param PositionLon   [Double] 位置經度(WGS84)
 * @param PositionLat   [Double] 位置緯度(WGS84)
 * @param GeoHash       [String] 地理空間編碼
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class PointType(
    val PositionLon: Double,         //位置經度(WGS84)
    val PositionLat: Double,         //位置緯度(WGS84)
    val GeoHash: String              //地理空間編碼

) : Serializable

/**
 * 取得指定"縣市","路線名稱"的公車動態定時資料(A1)"批次更新" -數據結構
 * @param PlateNumb     [String] 車牌號碼
 * @param OperatorID    [String] 營運業者代碼
 * @param RouteUID      [String] 路線唯一識別代碼
 * @param RouteID       List<[NameType]> 路線名字
 * @param SubRouteUID   [String] 子路線唯一識別代碼
 * @param SubRouteID    [String] 地區既用中之子路線代碼
 * @param SubRouteName  List<[NameType]> 路線名字
 * @param Direction     [Int] 去返程 - ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param BusPosition   List<[PointType]> 車輛位置經度
 * @param Speed         [Double] 行駛速度(kph)
 * @param Azimuth       [Double] 方位角
 * @param DutyStatus    [Int] 路線經過站牌之順序
 * @param BusStatus     [Int] 行車狀況 - ( 0:正常 1:車禍 2:故障 4:緊急求援 3:塞車
 *                                     90:不明 91:去回不明 98:偏移路線 99:非營運狀態
 *                                     255:未知 5:加油 100:客滿 101:包車出租 )
 * @param MessageType   [Int] 資料型態種類 - ( 0:未知 1:定期 2:非定期 )
 * @param GPSTime       [String] 車機時間
 * @param TransTime     [String] 車機資料傳輸時間
 * @param SrcRecTime    [String] 來源端平台接收時間
 * @param SrcTransTime  [String] 來源端平台資料傳出時間
 * @param SrcUpdateTime [String] 來源端平台資料更新時間
 * @param UpdateTime    [String] 本平台資料更新時間
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class BusA1(
    // Bus A1 Data
    val PlateNumb: String,           //車牌號碼
    val OperatorID: String,          //營運業者代碼
    val RouteUID: String,            //路線唯一識別代碼
    // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
    // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: List<NameType>,     //路線名字 (List)
    val SubRouteUID: String,         //子路線唯一識別代碼
    // 規則為 {業管機關簡碼} + {SubRouteID}，其中 {業管機關簡碼}
    // 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,          //地區既用中之子路線代碼(為原資料內碼)
    val SubRouteName: List<NameType>,//路線名字 (List)
    val Direction: Int,          //去返程 {
    // 0:去程 1:返程 2:迴圈 255:未知 }
    val BusPosition: List<PointType>,//車輛位置經度
    val Speed: Double,               //行駛速度(kph)
    val Azimuth: Double,             //方位角
    val DutyStatus: Int,         //勤務狀態 {
    // 0:正常 1:開始 2:結束 }
    val BusStatus: Int,          //行車狀況 {
    // 0:正常
    // 1:車禍 2:故障 4:緊急求援
    // 3:塞車
    // 90:不明 91:去回不明 98:偏移路線 99:非營運狀態 255:未知
    // 5:加油 100:客滿 101:包車出租 }
    val MessageType: Int,        //資料型態種類 {
    // 0:未知 1:定期 2:非定期 }
    val GPSTime: String,             //車機時間              (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val TransTime: String,           //車機資料傳輸時間       (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    // [多數單位沒有提供此欄位資訊]
    val SrcRecTime: String,          //來源端平台接收時間     (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val SrcTransTime: String,        //來源端平台資料傳出時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    // [公總使用動態即時推播故有提供此欄位, 而非公總系統因使用整包資料更新, 故沒有提供此欄位]
    val SrcUpdateTime: String,       //來源端平台資料更新時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    // [公總使用動態即時推播故沒有提供此欄位, 而非公總系統因提供整包資料更新, 故有提供此欄]
    val UpdateTime: String,          //本平台資料更新時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
) : Serializable

/**
 * 取得指定"縣市","路線名稱"的公車動態定點資料(A2)"批次更新" -數據結構
 * @param PlateNumb     [String] 車牌號碼
 * @param OperatorID    [String] 營運業者代碼
 * @param RouteUID      [String] 路線唯一識別代碼
 * @param RouteID       [String] 路線名字
 * @param SubRouteUID   [String] 子路線唯一識別代碼
 * @param SubRouteID    [String] 地區既用中之子路線代碼
 * @param SubRouteName  [NameType]> 路線名字
 * @param Direction     [Int] 去返程 - ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param StopUID       [String] 站牌唯一識別代碼
 * @param StopID        [String] 地區既用中之站牌代號
 * @param StopName      [NameType] 站牌名字
 * @param StopSequence  [Int] 路線經過站牌之順序
 * @param MessageType   [Int] 資料型態種類 - ( 0:未知 1:定期 2:非定期 )
 * @param DutyStatus    [Int] 勤務狀態 - ( 0:正常 1:開始 2:結束 )
 * @param BusStatus     [Int] 行車狀況 - ( 0:正常 1:車禍 2:故障 4:緊急求援 3:塞車
 *                                     90:不明 91:去回不明 98:偏移路線 99:非營運狀態
 *                                     255:未知 5:加油 100:客滿 101:包車出租 )
 * @param A2EventType   [Int] 進站離站 - ( 0:離站 1:進站 )
 * @param GPSTime       [String] 車機時間
 * @param TransTime     [String] 車機資料傳輸時間
 * @param SrcRecTime    [String] 來源端平台接收時間
 * @param SrcTransTime  [String] 來源端平台資料傳出時間
 * @param SrcUpdateTime [String] 來源端平台資料更新時間
 * @param UpdateTime    [String] 本平台資料更新時間
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class BusA2( // Bus A2 Data
    val PlateNumb: String,          //車牌號碼
    val OperatorID: String,         //營運業者代碼
    val RouteUID: String,           //路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,            //路線名字 (List)
    val SubRouteUID: String,        //子路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {SubRouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,         //地區既用中之子路線代碼(為原資料內碼)
    val SubRouteName: NameType,     //路線名字 (List)
    val Direction: Int,             //去返程 {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val StopUID: String,            //站牌唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {StopID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,             //地區既用中之站牌代號(為原資料內碼)
    val StopName: NameType,         //站牌名字 (List)
    val StopSequence: Int,          //路線經過站牌之順序
    val MessageType: Int,           //資料型態種類 {
                                            // 0:未知 1:定期 2:非定期 }
    val DutyStatus: Int,            //勤務狀態 {
                                            // 0:正常 1:開始 2:結束 }
    val BusStatus: Int,             //行車狀況 {
                                            // 0:正常
                                            // 1:車禍 2:故障 4:緊急求援
                                            // 3:塞車
                                            // 90:不明 91:去回不明 98:偏移路線 99:非營運狀態 255:未知
                                            // 5:加油 100:客滿 101:包車出租 }
    val A2EventType: Int,           //進站離站
                                            // 0:離站 1:進站 }
    val GPSTime: String,            //車機時間              (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val TransTime: String,          //車機資料傳輸時間       (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [多數單位沒有提供此欄位資訊]
    val SrcRecTime: String,         //來源端平台接收時間     (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val SrcTransTime: String,       //來源端平台資料傳出時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故有提供此欄位, 而非公總系統因使用整包資料更新, 故沒有提供此欄位]
    val SrcUpdateTime: String,      //來源端平台資料更新時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故沒有提供此欄位, 而非公總系統因提供整包資料更新, 故有提供此欄]
    val UpdateTime: String          //本平台資料更新時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
) : Serializable

/**
 * 站點 -數據結構
 * @param StopUID           [String] 站牌唯一識別代碼
 * @param StopID            [String] 地區既用中之站牌代碼
 * @param StopName          [NameType] 站牌名稱
 * @param StopBoarding      [Int] 上下車站別 - ( -1:可下車 0:可上下車 1:可上車 )
 * @param StopSequence      [Int] 路線經過站牌之順序
 * @param StopPosition      [PointType] 站牌位置
 * @param StationID         [String] 站牌所屬的站位ID
 * @param StationGroupID    [String] 站牌所屬的組站位ID
 * @param LocationCityCode  [String] 站牌位置縣市之代碼
 *
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class Stop(
    val StopUID: String,             //站牌唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {StopID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,              //地區既用中之站牌代碼(為原資料內碼)
    val StopName: NameType,          //站牌名稱 (List)
    val StopBoarding: Int,           //上下車站別 {
                                            // -1:可下車 0:可上下車 1:可上車 }
    val StopSequence: Int,           //路線經過站牌之順序
    val StopPosition: PointType,     //站牌位置
    val StationID: String,           //站牌所屬的站位ID
    val StationGroupID:String,       //站牌所屬的組站位ID
    val LocationCityCode:String,     //站牌位置縣市之代碼
                                            // (國際ISO 3166-2 三碼城市代碼)[若為公路/國道客運路線則為空值]
) : Serializable

/**
 * 取得指定"縣市","路線名稱"的市區公車顯示用路線站序資料 -數據結構
 * @param RouteUID      [String] 路線唯一識別代碼
 * @param RouteID       [String] 地區既用中之路線代碼
 * @param RouteName     [NameType] 路線名字
 * @param Operators     Array<[RouteOperator]> 營運業者
 * @param SubRouteUID   [String] 附屬路線唯一識別代碼
 * @param SubRouteID    [String] 地區既用中之附屬路線代碼
 * @param SubRouteName [NameType] 附屬路線名稱
 * @param Direction     [Int] 去返程 - ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param City          List<[String]> 站牌權管所屬縣市
 * @param CityCode      List<[String]> 站牌權管所屬縣市之代碼
 * @param Stops         List<[Stop]> 所有經過站牌
 * @param UpdateTime    [String] 本平台資料更新時間
 * @param VersionID     [Int] 資料版本編號
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class RouteStation( // Bus Station
    val RouteUID: String,            //路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,             //地區既用中之路線代碼(為原資料內碼)
    val RouteName: NameType,         //路線名字 (List)
    val Operators: ArrayList<RouteOperator>,    //營運業者
    val SubRouteUID: String,         //附屬路線唯一識別代碼，規則為 {業管機關簡碼} + {SubRouteID}
                                            // 其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,          //地區既用中之附屬路線代碼(為原資料內碼)
    val SubRouteName: NameType,      //附屬路線名稱
    val Direction: Int,              //去返程 {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val City: String,                //站牌權管所屬縣市(相當於市區公車API的City參數)
                                            // [若為公路/國道客運路線則為空值]
    val CityCode: String,            //站牌權管所屬縣市之代碼(國際ISO 3166-2 三碼城市代碼)
                                            // [若為公路/國道客運路線則為空值]
    val Stops: ArrayList<Stop>,           //所有經過站牌
    val UpdateTime: String,          //本平台資料更新時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val VersionID: Int               //資料版本編號
) : Serializable

/**
 * 到站時間預估 -數據結構
 * @param PlateNumb         [String] 車牌號碼
 * @param EstimateTime      [Int] 車輛之到站時間預估(秒)
 * @param IsLastBus         [Boolean] 是否為末班車
 * @param VehicleStopStatus [Int] 車輛於該站之進離站狀態 - ( 0:離站 1:進站 )
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class Estimate(
    val PlateNumb: String,           //車輛車牌號碼
    val EstimateTime: Int,       //車輛之到站時間預估(秒)
    val IsLastBus: Boolean,          //是否為末班車
    val VehicleStopStatus: Int,  //車輛於該站之進離站狀態 {
                                     // 0:離站 1:進站 }
) : Serializable

/**
 * 取得指定"縣市","路線名稱"的公車預估到站資料(N1)"批次更新" -數據結構
 * @param PlateNumb     [String] 車牌號碼
 * @param StopUID       [String] 站牌唯一識別代碼
 * @param StopID        [String] 地區既用中之站牌代碼
 * @param StopName      [NameType] 站牌名
 * @param RouteUID      [String] 路線唯一識別代碼
 * @param RouteID       [String] 地區既用中之路線代碼
 * @param RouteName     [NameType] 路線名稱
 * @param SubRouteUID   [String] 子路線唯一識別代碼
 * @param SubRouteID    [String] 地區既用中之子路線代碼
 * @param SubRouteName  [NameType] 子路線名稱
 * @param Direction     [Int] 去返程 - ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param EstimateTime  [Int] 到站時間預估
 * @param StopCountDown [Int] 車輛距離本站站數
 * @param CurrentStop   [String] 車輛目前所在站牌代碼
 * @param DestinationStop [String] 車輛目的站牌代碼
 * @param StopSequence  [Int] 路線經過站牌之順序
 * @param StopStatus    [Int] 車輛狀態備註 - ( 0:正常 1:尚未發車 2:交管不停靠 3:末班車已過 4:今日未營運 )
 * @param MessageType   [Int] 資料型態種類 - ( 0:未知 1:定期 2:非定期 )
 * @param NextBusTime   [String] 下一班公車到達時間
 * @param IsLastBus     [Boolean] 是否為末班車
 * @param Estimates     List<[Estimate]> 到站時間預估
 * @param DataTime      [String] 系統演算該筆預估到站資料的時間
 * @param TransTime     [String] 車機資料傳輸時間
 * @param SrcRecTime    [String] 來源端平台接收時間
 * @param SrcTransTime  [String] 來源端平台資料傳出時間
 * @param SrcUpdateTime [String] 來源端平台資料更新時間
 * @param UpdateTime    [String] 本平台資料更新時間
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
data class EstimateTime( // Bus EstimateTime
    val PlateNumb: String,           //車牌號碼 [値為値為-1時，表示目前該站位無車輛行駛]
    val StopUID: String,             //站牌唯一識別代碼，規則為 {業管機關簡碼} + {StopID}
                                            // 其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,              //地區既用中之站牌代碼(為原資料內碼)
    val StopName: NameType,          //站牌名 (List)
    val RouteUID: String,            //路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,             //地區既用中之路線代碼(為原資料內碼)
    val RouteName: NameType,         //路線名稱 (List)
    val SubRouteUID: String,         //子路線唯一識別代碼，規則為 {業管機關簡碼} + {SubRouteID}
                                            // 其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,          //地區既用中之子路線代碼(為原資料內碼)
    val SubRouteName: NameType,        //子路線名稱
    val Direction: Int,              //去返程(該方向指的是此車牌車輛目前所在路線的去返程方向，非指站站牌所在路線的去返程方向，使用時請加值業者多加注意) {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val EstimateTime: Int,           //到站時間預估(秒) {
                                            // 當StopStatus ->2~4 ||PlateNumb = -1時，EstimateTime値為null;
                                            // 當StopStatus値為1時， EstimateTime値多數為null
                                            // 僅部分路線因有固定發車時間，故EstimateTime有値;
                                            // 當StopStatus値為0時，EstimateTime有値。 }
    val StopCountDown: Int,          //車輛距離本站站數
    val CurrentStop: String,         //車輛目前所在站牌代碼
    val DestinationStop: String,     //車輛目的站牌代碼
    val StopSequence: Int,           //路線經過站牌之順序
    val StopStatus: Int,             //車輛狀態備註 {
                                            // 0:正常 1:尚未發車 2:交管不停靠 3:末班車已過 4:今日未營運 }
    val MessageType: Int,            //資料型態種類 {
                                            // 0:未知 1:定期 2:非定期 }
    val NextBusTime: String?,         //下一班公車到達時間(ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val IsLastBus: Boolean,          //是否為末班車
    val Estimates: List<Estimate>,   //到站時間預估
    val DataTime: String,            //系統演算該筆預估到站資料的時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [目前僅公總提供此欄位資訊]
    val TransTime: String,           //車機資料傳輸時間                  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [該欄位在N1資料中無意義]
    val SrcRecTime: String,          //來源端平台接收時間                (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [該欄位在N1資料中無意義]
    val SrcTransTime: String,        //來源端平台資料傳出時間             (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故有提供此欄位, 而非公總系統因使用整包資料更新, 故沒有提供此欄位]
    val SrcUpdateTime: String,       //來源端平台資料更新時間             (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故沒有提供此欄位, 而非公總系統因提供整包資料更新, 故有提供此欄]
    val UpdateTime: String           //本平台資料更新時間                (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
) : Serializable

/**
 * 段次計費 -數據結構
 * @param BufferZones   [String] 緩衝區資訊
 * @param Fares         [String] 每段收費資訊
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BufferZones(
    val BufferZones : ArrayList<BufferZone>?,//緩衝區資訊
    val Fares : ArrayList<Fare>,            //每段收費資訊
) : Serializable

/**
 * 緩衝區資訊
 * @param ZoneID                    [String] 緩衝區代號
 * @param SectionSequence           [Int] 緩衝區順序
 * @param Direction                 [Int] 方向性描述 - ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param FareBufferZoneOrigin      [BusStage] 緩衝區起點
 * @param FareBufferZoneDestination [BusStage] 緩衝區迄點
 * @param BufferZoneDescription     [String] 收費緩衝區描述
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BufferZone(
    val ZoneID : String,                    //緩衝區代號
    val SectionSequence : Int,              //緩衝區順序
    val Direction : Int,                    //方向性描述 {
                                                    // 0:去程 1:返程 2:迴圈 255:未知 }
    val FareBufferZoneOrigin : BusStage,        //緩衝區起點
    val FareBufferZoneDestination : BusStage,   //緩衝區迄點
    val BufferZoneDescription : String,     //收費緩衝區描述
) : Serializable

/**
 * 票價內容 -數據結構
 * @param TicketType    [Int] 票種類型 ( 1:一般票 2:來回票 3:電子票證 4:回數票 5:定期票30天期
                                        6:定期票60天期 7:早鳥票 8:定期票90天期 )
 * @param FareClass     [Int] 費率等級 ( 1:成人 2:學生 3:孩童 4:敬老 5:愛心 6:愛心孩童
                                        7:愛心優待或愛心陪伴 8:團體 9:軍警 10:由各運業者自行定義的半票 )
 * @param Price         [Int] 計費價格 (新台幣)
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class Fare(
    val TicketType : Int,   //票種類型 {
                                    // 1:一般票 2:來回票 3:電子票證 4:回數票 5:定期票30天期
                                    // 6:定期票60天期 7:早鳥票 8:定期票90天期 }
    val FareClass : Int,    //費率等級 {
                                    // 1:成人 2:學生 3:孩童 4:敬老 5:愛心 6:愛心孩童
                                    // 7:愛心優待或愛心陪伴 8:團體 9:軍警 10:由各運業者自行定義的半票 }
    val Price : Int,        //計費價格(新台幣) (其中-1表示不提供售票服務)
) : Serializable

/**
 * 站牌代碼+名稱 -數據結構
 * @param StopID    [String] 站牌代碼
 * @param StopName  [String] 站牌名稱
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusStage(
    val StopID : String,    //站牌代碼
    val StopName : String,  //站牌名稱
) : Serializable

/**
 * 營運日型態 -數據結構
 * @param Sunday            [String] 星期日是否營運 ( 0:否 1:是 )
 * @param Monday            [String] 星期一是否營運 ( 0:否 1:是 )
 * @param Tuesday           [String] 星期二是否營運 ( 0:否 1:是 )
 * @param Wednesday         [String] 星期三是否營運 ( 0:否 1:是 )
 * @param Thursday          [String] 星期四是否營運 ( 0:否 1:是 )
 * @param Friday            [String] 星期五是否營運 ( 0:否 1:是 )
 * @param Saturday          [String] 星期六是否營運 ( 0:否 1:是 )
 * @param NationalHolidays  [String] 國定假日是否營運 ( 0:否 1:是 )
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class ServiceDay(
    val Sunday : Int,           //星期日是否營運 { 0:否 1:是 }
    val Monday : Int,           //星期一是否營運 { 0:否 1:是 }
    val Tuesday : Int,          //星期二是否營運 { 0:否 1:是 }
    val Wednesday : Int,        //星期三是否營運 { 0:否 1:是 }
    val Thursday : Int,         //星期四是否營運 { 0:否 1:是 }
    val Friday : Int,           //星期五是否營運 { 0:否 1:是 }
    val Saturday : Int,         //星期六是否營運 { 0:否 1:是 }
    val NationalHolidays : Int, //國定假日是否營運 { 0:否 1:是 }
) : Serializable

/**
 * 優惠時段 -數據結構
 * @param ServiceDay    [ServiceDay] 營運日型態
 * @param StartTime     [String] 開始時間(HH:mm制)
 * @param EndTime       [String] 結束時間(HH:mm制)
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusDiscountPeriods(
    val ServiceDay : ServiceDay,//營運日型態
    val StartTime : String,     //開始時間(HH:mm制)
    val EndTime : String,       //結束時間(HH:mm制)
) : Serializable

/**
 * 票價內容 -數據結構 (票票價種類及費率說明)
 * @param FareName          [String] 票價名稱
 * @param TicketType        [Int] 票種類型 ( 1:一般票 2:來回票 3:電子票證 4:回數票 5:定期票30天期
                                             6:定期票60天期 7:早鳥票 8:定期票90天期 )
 * @param FareClass         [Int] 費率等級 ( 1:成人 2:學生 3:孩童 4:敬老 5:愛心 6:愛心孩童 7:愛心優待或愛心陪伴
                                             8:團體 9:軍警 10:由各運業者自行定義的半票 )
 * @param Price             [Int] 計費價格 (新台幣)
 * @param DiscountPeriods   Array<[BusDiscountPeriods]> 優惠時段
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusFare(
    val FareName : String,  //票價名稱
    val TicketType : Int,   //票種類型 {
                                    // 1:一般票 2:來回票 3:電子票證 4:回數票 5:定期票30天期
                                    // 6:定期票60天期 7:早鳥票 8:定期票90天期 }
    val FareClass : Int,    //費率等級 {
                                    // 1:成人 2:學生 3:孩童 4:敬老 5:愛心 6:愛心孩童 7:愛心優待或愛心陪伴
                                    // 8:團體 9:軍警 10:由各運業者自行定義的半票 }
    val Price : Int,        //計費價格(新台幣) (其中-1表示不提供售票服務)
    val DiscountPeriods : ArrayList<BusDiscountPeriods>,
                            //優惠時段
) : Serializable

/**
 * 計費站區間計費 -數據結構
 * (此計費方式以一路線內所有站牌分區收費。)(公總稱之為計費站收費, Stage=計費站)
 * @param Direction         [String] 方向性描述 ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param OriginStage       [BusStage] 起點計費站
 * @param DestinationStage  [BusStage] 迄點計費站
 * @param Fares             Array<[BusFare]> 票價內容
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusStageFare(
    val Direction : Int,            //方向性描述 {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val OriginStage : BusStage,     //起點計費站
    val DestinationStage : BusStage,//迄點計費站
    val Fares : ArrayList<BusFare>,     //票價內容
) : Serializable

/**
 * 起迄站間計費 -數據結構
 * @param Direction         [Int] 方向性描述 ( 0:去程 1:返程 2:迴圈 255:未知 )
 * @param OriginStop        [BusStage] 計費起點站牌資訊
 * @param DestinationStop   [BusStage] 計費起點站牌資訊
 * @param Fares             Array<[Fare]> 票價內容
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class BusODFare(
    val Direction : Int,        //方向性描述 {
                                        // 0:去程 1:返程 2:迴圈 255:未知 }
    val OriginStop : BusStage,      //計費起點站牌資訊
    val DestinationStop : BusStage, //計費起點站牌資訊
    val Fares : ArrayList<Fare>,    //票價內容
) : Serializable

/**
 * 取得指定"縣市","路線名稱"的收費資料"批次更新" -數據結構
 * @param RouteID           [String] 機關定義路線代號
 * @param RouteName         [String] 路線名稱
 * @param OperatorID        [String] 營運業者代碼
 * @param SubRouteID        [String] 機關定義附屬路線代碼
 * @param SubRouteName      [String] 附屬路線名稱
 * @param FarePricingType   [Int] 描述該路線計費方式 ( 0:段次計費 1:起迄站間計費 2:計費站區間計費 )
 * @param IsFreeBus         [Int] 是否為免費公車 ( 0:否 1:是 )
 * @param IsForAllSubRoutes [Int] 該收費方式是否應用到所有附屬路線 ( 0:否 1:是 )
 * @param SectionFares      Array<[BufferZones]> 段次計費
 * @param StageFares        Array<[BusStageFare]> 計費站區間計費
 * @param ODFares           Array<[BusODFare]> 起迄站間計費
 * @param UpdateTime        [String] 本平台資料更新時間
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class RouteFare(
    val RouteID : String,                   //機關定義路線代號
    val RouteName : String,                 //路線名稱
    val OperatorID : String,                //營運業者代碼
    val SubRouteID : String,                //機關定義附屬路線代碼
    val SubRouteName : String,              //附屬路線名稱
    val FarePricingType : Int,              //描述該路線計費方式 {
                                                    // 0:段次計費 1:起迄站間計費 2:計費站區間計費 }
    val IsFreeBus : Int,                    //是否為免費公車 {
                                                    // 0:否 1:是 }
    val IsForAllSubRoutes : Int,            //該收費方式是否應用到所有附屬路線 {
                                                    // 0:否 1:是 }
    val SectionFares : ArrayList<BufferZones>?,  //段次計費
    val StageFares : ArrayList<BusStageFare>,   //計費站區間計費
    val ODFares : ArrayList<BusODFare>,         //起迄站間計費
    val UpdateTime : String,                //本平台資料更新時間
                                                    // ( ISO8601格式:yyyy-MM-ddTHH:mm:sszzz )
) : Serializable

/**
 * 取得指定"縣市"的市區公車車輛資料 -數據結構
 * @param PlateNumb             [String] 車牌號碼
 * @param OperatorID            [String] 營運業者代碼
 * @param OperatorCode          [String] 營運業者簡碼
 * @param VehicleClass          [Int] 車輛種類 (
                                            1:大型巴士 2:中型巴士 3:小型巴士 4:雙層巴士 5:雙節巴士 6:計程車 )
 * @param VehicleType           [Int] 車輛種類 (
                                            0:一般 1:無障礙公車 2:復康巴士 3:小型巴士 4:專車 5:其他 )
 * @param CardReaderLayout      [Int] 讀卡機配置 (
                                            0:無讀卡機配置 1:前門刷卡 2:前後門刷卡 )
 * @param IsElectric            [Boolean] 是否為電動公車
 * @param IsHybrid              [Boolean] 是否為油電混合公車
 * @param IsLowFloor            [Boolean] 是否為低地板
 * @param HasLiftOrRamp         [Boolean] 是否有活動坡道
 * @param HasWifi               [Boolean] 是否有提供Wifi服務
 * @param InBoxID               [String] 車機代號
 * @param UpdateTime            [String] 本平台資料更新時間
 *
 * @author KILNETA
 * @since Beta_1.2.0
 */
data class Vehicle(
    val PlateNumb : String,                 //車牌號碼
    val OperatorID : String,                //營運業者代碼
    val OperatorCode : String,              //營運業者簡碼
    val VehicleClass : Int,                 //車輛種類 {
                                                // 1:大型巴士 2:中型巴士 3:小型巴士 4:雙層巴士
                                                // 5:雙節巴士 6:計程車 }
    val VehicleType : Int,                  //車輛種類 {
                                                // 0:一般 1:無障礙公車 2:復康巴士 3:小型巴士
                                                // 4:專車 5:其他 }
    val CardReaderLayout : Int,             //讀卡機配置 {
                                                // 0:無讀卡機配置 1:前門刷卡 2:前後門刷卡 }
    val IsElectric : Boolean,               //是否為電動公車
    val IsHybrid : Boolean,                 //是否為油電混合公車
    val IsLowFloor : Boolean,               //是否為低地板
    val HasLiftOrRamp : Boolean,            //是否有活動坡道
    val HasWifi : Boolean,                  //是否有提供Wifi服務
    val InBoxID : String,                   //車機代號
    val UpdateTime : String,                //本平台資料更新時間
                                                // ( ISO8601格式:yyyy-MM-ddTHH:mm:sszzz )
) : Serializable

/*-----------------------存儲用資料結構-----------------------*/

/**
 * 收藏站牌 -數據結構
 * @param RouteData             [BusRoute] 路線名稱
 * @param DestinationStopName   [String] 路線終點站名稱
 * @param StationUID            [String] 站牌唯一識別代碼
 * @param StationName           [NameType] 站牌名稱
 * @param Direction             [Int] 路線往返方向 ( 0:去程,1:返程,2:迴圈,255:未知)
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class CollectStation(
    val RouteData : BusRoute,           //路線名稱
    val DestinationStopName : String,   //路線終點站名稱
    val StationUID: String,             //站牌唯一識別代碼
    val StationName: NameType,          //站牌名稱
    val Direction: Int                  //路線往返方向 ( 0:去程,1:返程,2:迴圈,255:未知)
) : Serializable

/**
 * 收藏群組 -數據結構
 * @param GroupName          [BusRoute] 群組名稱
 * @param canLost           [String] 能否移除
 * @param SaveStationList   ArrayList<[CollectStation]> 營運業者代碼
 *
 * @author KILNETA
 * @since Alpha_5.0
 */
data class CollectGroup(
    var GroupName : String,                          //群組名稱
    val canLost : Boolean,                          //能否移除
    val SaveStationList: ArrayList<CollectStation> =//營運業者代碼
        arrayListOf()
) : Serializable