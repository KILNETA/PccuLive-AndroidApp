package com.example.pccu.Internet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.http.GET
import java.util.*

class Bus_API{

    fun GetA1(): List<Bus_Data_A1>? {
        val Url = "https://ptx.transportdata.tw/"
        return HttpRetrofit.create(ApiServce::class.java,Url).getBusA1().execute().body()
    }

    fun GetA2(): List<Bus_Data_A2>? {
        val Url = "https://ptx.transportdata.tw/"
        return HttpRetrofit.create(ApiServce::class.java,Url).getBusA2().execute().body()
    }

    fun GetBusStation(Zh_tw:String): List<Bus_Data_Station>? {
        //根網域
        val Url = "https://ptx.transportdata.tw/"
        //過濾條件
        val filter = "RouteName/Zh_tw eq '${Zh_tw}'"
        //檔案格式
        val format = "JSON"

        val a = HttpRetrofit.create(ApiServce::class.java,Url).getBusStation(Zh_tw,filter,format).execute().body()
        return a
    }

    fun GetBusEstimateTime(Zh_tw:String): Vector<Bus_Data_EstimateTime>? {
        //根網域
        val Url = "https://ptx.transportdata.tw/"
        //過濾條件
        val filter = "RouteName/Zh_tw eq '${Zh_tw}'"
        //檔案格式
        val format = "JSON"
        return HttpRetrofit.create(ApiServce::class.java,Url).getBusEstimateTime(Zh_tw,filter,format).execute().body()
    }
}

// 名字 -數據結構
data class NameType(
    val Zh_tw:String,                //中文繁體名稱
    val En:String                   //英文名稱
)

// 經緯度  -數據結構
data class PointType(
    val PositionLon: Double,         //位置經度(WGS84)
    val PositionLat: Double,         //位置緯度(WGS84)
    val GeoHash: String              //地理空間編碼

)

// 取得指定[縣市],[路線名稱]的公車動態定時資料(A1)[批次更新] -數據結構
data class Bus_Data_A1( // Bus A1 Data
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
)

// 取得指定[縣市],[路線名稱]的公車動態定點資料(A2)[批次更新] -數據結構
data class Bus_Data_A2( // Bus A2 Data
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
    val StopUID: String,             //站牌唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {StopID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,              //地區既用中之站牌代號(為原資料內碼)
    val StopName: List<NameType>,    //站牌名字 (List)
    val StopSequence: Int,       //路線經過站牌之順序
    val MessageType: Int,        //資料型態種類 {
                                            // 0:未知 1:定期 2:非定期 }
    val DutyStatus: Int,         //勤務狀態 {
                                            // 0:正常 1:開始 2:結束 }
    val BusStatus: Int,          //行車狀況 {
                                            // 0:正常
                                            // 1:車禍 2:故障 4:緊急求援
                                            // 3:塞車
                                            // 90:不明 91:去回不明 98:偏移路線 99:非營運狀態 255:未知
                                            // 5:加油 100:客滿 101:包車出租 }
    val A2EventType: Int,        //進站離站
                                            // 0:離站 1:進站 }
    val GPSTime: String,             //車機時間              (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val TransTime: String,           //車機資料傳輸時間       (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [多數單位沒有提供此欄位資訊]
    val SrcRecTime: String,          //來源端平台接收時間     (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val SrcTransTime: String,        //來源端平台資料傳出時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故有提供此欄位, 而非公總系統因使用整包資料更新, 故沒有提供此欄位]
    val SrcUpdateTime: String,       //來源端平台資料更新時間  (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
                                            // [公總使用動態即時推播故沒有提供此欄位, 而非公總系統因提供整包資料更新, 故有提供此欄]
    val UpdateTime: String           //本平台資料更新時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
)

// 站點 -數據結構
data class Stop(
    val StopUID: String,             //站牌唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {StopID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,              //地區既用中之站牌代碼(為原資料內碼)
    val StopName: NameType,    //站牌名稱 (List)
    val StopBoarding: Int,       //上下車站別 {
                                            // -1:可下車 0:可上下車 1:可上車 }
    val StopSequence: Int,       //路線經過站牌之順序
    val StopPosition: PointType,//站牌位置
    val StationID: String,           //站牌所屬的站位ID
    val StationGroupID:String,       //站牌所屬的組站位ID
    val LocationCityCode:String,     //站牌位置縣市之代碼
                                            // (國際ISO 3166-2 三碼城市代碼)[若為公路/國道客運路線則為空值]
)

// 取得指定[縣市],[路線名稱]的市區公車顯示用路線站序資料 -數據結構
data class Bus_Data_Station( // Bus Station
    val RouteUID: String,            //路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,             //地區既用中之路線代碼(為原資料內碼)
    val RouteName: NameType,    //路線名字 (List)
    val Direction: Int,          //去返程 {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val Stops: List<Stop>,          //所有經過站牌
    val UpdateTime: String,          //本平台資料更新時間      (ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val VersionID: Int           //資料版本編號
)

// 到站時間預估 -數據結構
data class Estimate(
    val PlateNumb: String,           //車輛車牌號碼
    val EstimateTime: Int,       //車輛之到站時間預估(秒)
    val IsLastBus: Boolean,          //是否為末班車
    val VehicleStopStatus: Int,  //車輛於該站之進離站狀態 {
                                     // 0:離站 1:進站 }
)

// 取得指定[縣市],[路線名稱]的公車預估到站資料(N1)[批次更新] -數據結構
data class Bus_Data_EstimateTime( // Bus EstimateTime
    val PlateNumb: String,           //車牌號碼 [値為値為-1時，表示目前該站位無車輛行駛]
    val StopUID: String,             //站牌唯一識別代碼，規則為 {業管機關簡碼} + {StopID}
                                            // 其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val StopID: String,              //地區既用中之站牌代碼(為原資料內碼)
    val StopName: NameType,    //站牌名 (List)
    val RouteUID: String,            //路線唯一識別代碼
                                            // 規則為 {業管機關簡碼} + {RouteID}，其中 {業管機關簡碼}
                                            // 可於Authority API中的AuthorityCode欄位查詢
    val RouteID: String,             //地區既用中之路線代碼(為原資料內碼)
    val RouteName: NameType,   //路線名稱 (List)
    val SubRouteUID: String,         //子路線唯一識別代碼，規則為 {業管機關簡碼} + {SubRouteID}
                                            // 其中 {業管機關簡碼} 可於Authority API中的AuthorityCode欄位查詢
    val SubRouteID: String,          //地區既用中之子路線代碼(為原資料內碼)
    val SubRouteName: String,        //子路線名稱
    val Direction: Int,          //去返程(該方向指的是此車牌車輛目前所在路線的去返程方向，非指站站牌所在路線的去返程方向，使用時請加值業者多加注意) {
                                            // 0:去程 1:返程 2:迴圈 255:未知 }
    val EstimateTime: Int,       //到站時間預估(秒) {
                                            // 當StopStatus ->2~4 ||PlateNumb = -1時，EstimateTime値為null;
                                            // 當StopStatus値為1時， EstimateTime値多數為null
                                            // 僅部分路線因有固定發車時間，故EstimateTime有値;
                                            // 當StopStatus値為0時，EstimateTime有値。 }
    val StopCountDown: Int,      //車輛距離本站站數
    val CurrentStop: String,         //車輛目前所在站牌代碼
    val DestinationStop: String,     //車輛目的站牌代碼
    val StopSequence: Int,       //路線經過站牌之順序
    val StopStatus: Int,         //車輛狀態備註 {
                                            // 0:正常 1:尚未發車 2:交管不停靠 3:末班車已過 4:今日未營運 }
    val MessageType: Int,        //資料型態種類 {
                                            // 0:未知 1:定期 2:非定期 }
    val NextBusTime: String,         //下一班公車到達時間(ISO8601格式:yyyy-MM-ddTHH:mm:sszzz)
    val IsLastBus: Boolean,          //是否為末班車
    val Estimates: List<Estimate>,  //到站時間預估
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
)
