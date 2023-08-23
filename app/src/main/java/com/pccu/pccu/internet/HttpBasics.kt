package com.pccu.pccu.internet

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.io.InputStream
import okhttp3.OkHttpClient
import retrofit2.http.*
import retrofit2.http.Headers

/**
 * 更好的網路數據解析 "object"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
object HttpRetrofit{

    /**
     * 連線限制 (連接超時、讀取超時、請求超時...等) : OkHttpClient
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    private val client: OkHttpClient = OkHttpClient.Builder()   //builder建構者
        .connectTimeout(1000, TimeUnit.SECONDS)         //連接超時
        .readTimeout(1000, TimeUnit.SECONDS)            //讀取超時
        .writeTimeout(1000, TimeUnit.SECONDS)           //請求超時
        .addInterceptor(HttpLoggingInterceptor())               //添加 Http日誌攔截器.setLenient()
        .build()                                                //建構

    /**
     * 連線 Json_API (無偽裝瀏覽器)
     *
     * @param clazz [Class]<[T]> -> <Json接口資料結構>
     * @param Url [String] 目標網址
     * @return Json文字 : [T]
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun<T> createJson(clazz: Class<T>,Url:String): T{

        val retrofit = Retrofit.Builder() //builder建構者
            .client(client)                                     //獲取 :OkHttpClient 連接函數
            .baseUrl(Url)                                       //API根網域
            .addConverterFactory(GsonConverterFactory.create()) //Gson數據解析
            .build()                                            //建構

        return retrofit.create(clazz) //回傳 獲取的 :Retrofit 函數
    }
    /*
    /**
     * 連線 取出網頁文字檔(XML)
     * @param Url [String] 目標網址
     * @param R [IInputStream] 回調函數
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    fun createXML(Url:String ,R:IInputStream){

        /**建立Request，設置連線資訊*/
        val call = client.newCall(
            Request.Builder()       //連線資訊建構
                .url(Url)           //輸入網址
                .build()            //建構連線
        )
        //連線 (連線失敗不會閃退 單純報錯)
        call.enqueue(object : Callback {
            //連線失敗
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("連線錯誤", "onFailure: $e")
                R.respond(null)
            }
            //連線成功
            override fun onResponse(call: okhttp3.Call, response: Response) {
                R.respond(response.body?.byteStream())
            }
        })
    }*/
    fun createXML(Url:String): InputStream? {
        return try {
            //建立Request，設置連線資訊
            val response = client.newCall(
                Request.Builder()       //連線資訊建構
                    .url(Url)           //輸入網址
                    .build()            //建構連線
            ).execute()                 //執行

            response.body!!.byteStream()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
    fun createHTML(Url:String,charset:String): String? {
        //建立Request，設置連線資訊
        return try {
            val response = client.newCall(
                Request.Builder()           //連線資訊建構
                    .url(Url)               //輸入網址
                    .build()                //建構連線
            ).execute()                     //執行

            String(response.body!!.bytes(), charset(charset))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Api資料節點
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    interface ApiService{

        /**取得文化天氣Api資訊的子節點
         * @return [WeatherData]
         * @author KILNETA
         * @since  Alpha_1.0
         */
        @GET("public/weather.json") //取得Api資訊的子節點
        fun getWeather(): Call<List<WeatherData>> //得到天氣資料

        /***************************** Google行事曆 *****************************/

        /**取得Google_Calendar_Api資訊的子節點
         * @param key       [String] 密鑰
         * @param timeMax   [String] 結束時間
         * @param timeMin   [String] 起始時間
         * @return 行事曆活動資料 : [CalendarSource]
         *
         * @author KILNETA
         * @since  Alpha_2.0
         */
        @GET("pccu.edu.tw_blcke48f8jv7rd96hs8oeb06ro%40group.calendar.google.com/events?")
        fun getCalendar(
            @Query("key") key:String,
            @Query("timeMax") timeMax:String,
            @Query("timeMin") timeMin:String
        ): Call<CalendarSource>

        /***************************** 中央氣象局 *****************************/

        /**
         * 取得CWB_Api資訊的子節點
         * @param key   [String] 密鑰
         * @return 氣象預報資料 : [CwbWeatherSource]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @Headers("accept: application/json")
        @GET("api/v1/rest/datastore/F-C0032-001?")
        fun getCWB(
            @Query("Authorization") key:String,
        ): Call<CwbWeatherSource>

        /***************************** TDX_BUS *****************************/

        /**
         * 取得TDX_token
         * @param contentType   [String] 內容類型
         * @param grant_type    [String] 授權類型
         * @param client_id     [String] 用戶ID
         * @param client_secret [String] 用戶協定
         * @return TdxToken : [TdxToken]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @POST("auth/realms/TDXConnect/protocol/openid-connect/token")
        @FormUrlEncoded
        fun getTdxToken(
            @Header("content-type") contentType:String,
            @Field("grant_type") grant_type:String,
            @Field("client_id") client_id:String,
            @Field("client_secret") client_secret:String
        ): Call<TdxToken>

        /** 市區公車API
         * @param authorization [String] 授權
         * @param activity      [String] API功能名稱
         * @param city          [String] 縣市地區
         * @param routeName     [String] 路線名稱
         * @param filter        [String] 篩選條件
         * @param format        [String] 檔案格式
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("api/basic/v2/Bus/{activity}/City/{city}{routeName}?")
        fun getTdxBus(
            @Header("authorization") authorization:String,
            @Path("activity") activity:String,
            @Path("city") city:String,
            @Path("routeName") routeName:String?,
            @Query("\$filter") filter:String?,
            @Query("\$format") format:String,
        ): Call<ResponseBody>

        /***************************** 課程評價 *****************************/

        /** 課程評鑑API
         * @param keyword     [String] 關鍵字
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("api/courses/search?")
        fun getCourseEvaluation(
            @Query("keyword") keyword:String
        ): Call<ArrayList<CourseIntroduction>>

        /** 課程評鑑API 學院
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("/api/colleges")
        fun getCourseEvaluationColleges(
        ): Call<ArrayList<String>>

        /** 課程評鑑API 依系組
         * @param college     [String] 學院
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("/api/departments?")
        fun getCourseEvaluationDepartments(
            @Query("college") college:String,
        ): Call<ArrayList<Department>>

        /** 課程評鑑API 評價數
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("/api/ratings/count")
        fun getCourseEvaluationCount(
        ): Call<EvaluationCount>

        /** 課程評鑑API 評價數
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("api/courses?")
        fun getCourseEvaluationIntroduction(
            @Query("college") college:String,
            @Query("department") department:String
        ): Call<ArrayList<CourseIntroduction>>

        /** 課程評鑑API 評價數
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.0
         */
        @GET("api/ratings?")
        fun getCourseEvaluationEvaluates(
            @Query("department") department:String,
            @Query("course") course:String,
            @Query("teacher") teacher:String
        ): Call<ArrayList<CourseEvaluation>>

        /** Moenv空氣品質預報資料
         * @param api_key  [String] ApiKey
         * @param sort     [String] 以為排序基準
         * @param limit    [String] 查多少筆
         * @return API原始文檔 : [ResponseBody]
         *
         * @author KILNETA
         * @since  Alpha_4.2
         */
        @GET("api/v2/aqf_p_01?")
        fun getAirQualityForecast(
            @Query("api_key") api_key:String,
            @Query("sort") sort:String,
            @Query("limit") limit:String
        ): Call<MoenvJson>
    }
}