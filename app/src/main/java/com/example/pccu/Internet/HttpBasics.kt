package com.example.pccu.Internet

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import android.webkit.WebSettings
import okhttp3.Request
import okhttp3.Interceptor
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*


object  HttpRetrofit{ //更好的網路數據解析

    private  val client: OkHttpClient = OkHttpClient.Builder() //builder建構者
        .connectTimeout(10, TimeUnit.SECONDS) //連接超時
        .readTimeout(10, TimeUnit.SECONDS) //讀取超時
        .writeTimeout(10, TimeUnit.SECONDS) //請求超時
        .addInterceptor(HttpLoggingInterceptor()) //添加 Http日誌攔截器
        .build() //建構

    fun<T> create(clazz: Class<T>,Url:String): T{

        val retrofit = Retrofit.Builder() //builder建構者
            .client(client) //獲取 :OkHttpClient 連接函數
            .client(getOkHttpClient())
            .baseUrl(Url) //API根網域
            .addConverterFactory(GsonConverterFactory.create()) //Gson數據解析
            .build() //建構

        return retrofit.create(clazz) //回傳 獲取的 :Retrofit 函數
    }

    /**
     * 构造okhttp头部
     *
     */
    private fun getOkHttpClient(): OkHttpClient? {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .removeHeader("User-Agent") //移除旧的
                    .addHeader(
                        "User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
                    ) //添加真正的头部
                    .build()
                chain.proceed(request)
            }).build()
    }
}

interface ApiServce{ //Api資料節點
    @GET("public/weather.json") //取得Api資訊的子節點
    fun getWeather(): Call<List<Weather_Data>> //得到天氣資料

    @GET("MOTC/v2/Bus/Route/City/Taipei/紅5?\$format=JSON") //取得Api資訊的子節點
    fun getBusA1(): Call<List<Bus_Data_A1>> //得到天氣資料

    @GET("MOTC/v2/Bus/RealTimeNearStop/City/Taipei/紅5?\$format=JSON") //取得Api資訊的子節點
    fun getBusA2(): Call<List<Bus_Data_A2>> //得到天氣資料

    @GET("MOTC/v2/Bus/DisplayStopOfRoute/City/Taipei/{Zh_tw}?") //取得指定[縣市],[路線名稱]的路線資料
    fun getBusStation(
        @Path("Zh_tw") Zh_tw:String,
        @Query("\$filter") filter:String,
        @Query("\$format") format:String
    ): Call<List<Bus_Data_Station>> //得到路線資料

    @GET("MOTC/v2/Bus/EstimatedTimeOfArrival/City/Taipei/{Zh_tw}?") //取得指定[縣市],[路線名稱]的公車預估到站資料
    fun getBusEstimateTime(
        @Path("Zh_tw") Zh_tw:String,
        @Query("\$filter") filter:String,
        @Query("\$format") format:String
    ): Call<Vector<Bus_Data_EstimateTime>> //取得公車預估到站資料
}
