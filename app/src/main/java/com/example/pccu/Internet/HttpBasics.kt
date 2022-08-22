package com.example.pccu.Internet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.pccu.BuildConfig.TDX_Id_API_KEY
import com.example.pccu.BuildConfig.TDX_Secret_API_KEY
import com.google.gson.Gson

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.io.InputStream
import java.util.*
import okhttp3.FormBody

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.http.*
import retrofit2.http.Headers
import java.lang.reflect.Type


/**
 * 更好的網路數據解析 "object"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
object  HttpRetrofit{

    /**
     * 連線限制 (連接超時、讀取超時、請求超時...等) : OkHttpClient
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private val client: OkHttpClient = OkHttpClient.Builder() //builder建構者
        .connectTimeout(1000, TimeUnit.SECONDS) //連接超時
        .readTimeout(1000, TimeUnit.SECONDS) //讀取超時
        .writeTimeout(1000, TimeUnit.SECONDS) //請求超時
        .addInterceptor(HttpLoggingInterceptor()) //添加 Http日誌攔截器.setLenient()
        .build() //建構

    /**
     * 連線 Json_API (無偽裝瀏覽器)
     *
     * @param clazz [Class] -> <Json接周資料結構>
     * @param Url [String] 目標網址
     * @return Json文字 : [接收Json資料結構]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun<T> create_Json(clazz: Class<T>,Url:String): T{

        val retrofit = Retrofit.Builder() //builder建構者
            .client(client) //獲取 :OkHttpClient 連接函數
            .baseUrl(Url) //API根網域
            .addConverterFactory(GsonConverterFactory.create()) //Gson數據解析
            .build() //建構

        return retrofit.create(clazz) //回傳 獲取的 :Retrofit 函數
    }

    /**
     * 連線 Json_API (目前供給Bus_API用)
     *
     * @param clazz [Class] -> <Json接周資料結構>
     * @param Url [String] 目標網址
     * @return Json文字 : [接收Json資料結構]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun<T> create_Json_Bus(clazz: Class<T>,Url:String): T{

        val retrofit = Retrofit.Builder() //builder建構者
            .client(client) //獲取 :OkHttpClient 連接函數
            .client(getOkHttpClient()) // ->偽裝成瀏覽器 (僅用於交通部BUS資料爬取)
            .baseUrl(Url) //API根網域
            .addConverterFactory(GsonConverterFactory.create()) //Gson數據解析
            .build() //建構

        return retrofit.create(clazz) //回傳 獲取的 :Retrofit 函數
    }

    /**
     * 連線 取出網頁文字檔(XML)
     *
     * @param Url [String] 目標網址
     * @return XML文字 : [InputStream]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun create_XML(Url:String): InputStream{
        //建立Request，設置連線資訊
        val response = client.newCall(
            Request.Builder() //連線資訊建構
                .url(Url) //輸入網址
                .build() //建構連線
        ).execute() //執行

        return response.body!!.byteStream()
    }

    fun create_String(Url:String): String{
        //建立Request，設置連線資訊
        val response = client.newCall(
            Request.Builder() //連線資訊建構
                .url(Url) //輸入網址
                .build() //建構連線
        ).execute() //執行

        return response.body!!.string()
    }

    /**
     * 連線 取出網址文字(HTML)
     *
     * @param Url       [String] 目標網址
     * @param charset   [String] 字符集
     * @return HTML文字 : [String]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun create_Content(Url:String,charset:String): String {
        //建立Request，設置連線資訊
        val response = client.newCall(
            Request.Builder()           //連線資訊建構
                .url(Url)               //輸入網址
                .build()                //建構連線
        ).execute()                     //執行

        //返還修復特定字元集後的HTML文字檔
        return String(response.body!!.bytes(), charset(charset))
    }

    /**
     * 連線 取出網址圖片(Bitmap)
     * @param Url   [String]  目標網址
     * @return 圖片 : [Bitmap]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun create_Image(Url:String): Bitmap {
        //建立Request，設置連線資訊
        val response = client.newCall(
            Request.Builder()           //連線資訊建構
                .url(Url)               //輸入網址
                .build()                //建構連線
        ).execute()                     //執行

        //byte轉檔Bitmap圖片
        val bitmap = BitmapFactory.decodeStream(response.body!!.byteStream())

        //回傳取得的圖片(Bitmap)
        return bitmap
    }

    /**
     * 重構okhttp頭部 (暫時用於Bus的連結)
     *
     * @return http協議頭部資訊 : [OkHttpClient]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor( //回傳 新的建構者
            Interceptor { chain ->  //添加攔截器 chain(連接)
                val request = chain.request()       //建立新的連接request(請求)
                    .newBuilder()                   //新建構子
                    .removeHeader("User-Agent") //移除舊的頭部資訊
                    .addHeader(                     //添加偽裝瀏覽器的頭部資訊
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) " +
                              "AppleWebKit/537.36 (KHTML, like Gecko) " +
                              "Chrome/70.0.3538.77 " +
                              "Safari/537.36"
                    )
                    .build()                        //建構
                chain.proceed(request)
            }
        ).build()
    }

    /**
     * 重構okhttp頭部 (暫時用於Bus的連結)
     *
     * @return http協議頭部資訊 : [OkHttpClient]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun create_TDX(){
        val formBody: RequestBody = FormBody.Builder()
            .add("content-type","application/x-www-form-urlencoded")
            .add("grant_type", "client_credentials") //傳遞鍵值對參數
            .add("client_id", TDX_Id_API_KEY) //傳遞鍵值對參數
            .add("client_secret", TDX_Secret_API_KEY) //傳遞鍵值對參數
            .build()
        val request: Request = Request.Builder()
            .url("https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token")
            .post(formBody)
            .build()

        val response = client.newCall(request)

        Log.e("TDX_TEST",response.execute().body!!.string())
    }
}

/**
 * Api資料節點
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
interface ApiServce{

    /** 取得Api資訊的子節點
     *  @return [Weather_Data]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("public/weather.json") //取得Api資訊的子節點
    fun getWeather(): Call<List<Weather_Data>> //得到天氣資料

    /** 取得Google_Calendar_Api資訊的子節點
     *  @return [ToDayCalendar]
     *  @author KILNETA
     *  @since  Alpha_2.0
     */
    @GET("pccu.edu.tw_blcke48f8jv7rd96hs8oeb06ro%40group.calendar.google.com/events?")
    fun getCalendar(
        @Query("key") key:String,
        @Query("timeMax") timeMax:String,
        @Query("timeMin") timeMin:String
    ): Call<CalendarSourse>

    /** 取得CWB_Api資訊的子節點
     *  @return [CwbWeatherSource]
     *  @author KILNETA
     *  @since  Alpha_4.0
     */
    @Headers("accept: application/json")
    @GET("api/v1/rest/datastore/F-C0032-001?")
    fun getCWB(
        @Query("Authorization") key:String,
    ): Call<CwbWeatherSource>

    /** 取得TDX_token
     *  @return [CwbWeatherSource]
     *  @author KILNETA
     *  @since  Alpha_4.0
     */
    @POST("auth/realms/TDXConnect/protocol/openid-connect/token")
    @FormUrlEncoded
    fun getTDX_token(
        @Header("content-type") `content-type`:String,
        @Field("grant_type") grant_type:String,
        @Field("client_id") client_id:String,
        @Field("client_secret") client_secret:String
    ): Call<TDX_token>
    /** 市區公車之路線資料
     *  @return [CwbWeatherSource]
     *  @author KILNETA
     *  @since  Alpha_4.0
     */
    @GET("api/basic/v2/Bus/{activity}/City/{city}{routeName}?")
    fun getTDX_CR(
        @Header("authorization") authorization:String,
        @Path("activity") activity:String,
        @Path("city") city:String,
        @Path("routeName") routeName:String?,
        @Query("\$filter") filter:String?,
        @Query("\$format") format:String,
    ): Call<ResponseBody>

    /** 取得指定"縣市","路線名稱"的路線資料
     *  @return 路線資料 : [Bus_Data_Station]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("api/basic/v2/Bus/DisplayStopOfRoute/City/{city}/{Zh_tw}?") //取得BusStation資訊的子節點
    fun getBusStation(
        @Header("authorization") authorization:String,
        @Path("city") city:String,
        @Path("Zh_tw") Zh_tw:String,
        @Query("\$filter") filter:String,
        @Query("\$format") format:String
    ): Call<List<Bus_Data_Station>> //得到路線資料

    /** 取得指定"縣市","路線名稱"的公車預估到站資料
     *  @return 預估到站資料 : [Bus_Data_EstimateTime]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("MOTC/v2/Bus/EstimatedTimeOfArrival/City/Taipei/{Zh_tw}?") //取得BusEstimateTime資訊的子節點
    fun getBusEstimateTime(
        @Path("Zh_tw") Zh_tw:String,
        @Query("\$filter") filter:String,
        @Query("\$format") format:String
    ): Call<Vector<Bus_Data_EstimateTime>> //取得公車預估到站資料
}
