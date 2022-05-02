package com.example.pccu.Internet

import android.graphics.Bitmap
import android.graphics.BitmapFactory

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.InputStream
import java.util.*

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
        .connectTimeout(100, TimeUnit.SECONDS) //連接超時
        .readTimeout(100, TimeUnit.SECONDS) //讀取超時
        .writeTimeout(100, TimeUnit.SECONDS) //請求超時
        .addInterceptor(HttpLoggingInterceptor()) //添加 Http日誌攔截器.setLenient()
        .build() //建構

    /**
     * 連線 API (Json專用)
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

    /** 取得指定"縣市","路線名稱"的公車動態定時資料(A1)"批次更新"
     *  @return [Bus_Data_A1]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("MOTC/v2/Bus/Route/City/Taipei/紅5?\$format=JSON") //取得BusA1資訊的子節點
    fun getBusA1(): Call<List<Bus_Data_A1>> //得到動態定時資料

    /** 取得指定"縣市","路線名稱"的公車動態定點資料(A2)"批次更新"
     *  @return [Bus_Data_A2]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("MOTC/v2/Bus/RealTimeNearStop/City/Taipei/紅5?\$format=JSON") //取得BusA2資訊的子節點
    fun getBusA2(): Call<List<Bus_Data_A2>> //得到動態定點資料

    /** 取得指定"縣市","路線名稱"的路線資料
     *  @return 路線資料 : [Bus_Data_Station]
     *  @author KILNETA
     *  @since  Alpha_1.0
     */
    @GET("MOTC/v2/Bus/DisplayStopOfRoute/City/Taipei/{Zh_tw}?") //取得BusStation資訊的子節點
    fun getBusStation(
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
