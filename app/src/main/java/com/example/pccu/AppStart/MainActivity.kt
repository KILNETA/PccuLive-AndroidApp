package com.example.pccu.AppStart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

import java.util.concurrent.TimeUnit

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

import okhttp3.*

import com.example.pccu.R


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_fragment)
        bottomNavigationView.setupWithNavController(navController)
    }
}

object Http {
    val client  = OkHttpClient.Builder()    //builder建構者
        .connectTimeout(10, TimeUnit.SECONDS) //連接超時
        .readTimeout(10, TimeUnit.SECONDS)    //讀取超時
        .writeTimeout(10, TimeUnit.SECONDS)  //請求超時
        .build();

    fun getWeather_Async(){ //取得氣溫資料
        val request: Request = Request.Builder()
            .url("https://api.pccu.edu.tw/public/weather.json").build() //連接PccuAPI -氣溫
        val call = client.newCall(request) //建立連線

        call.enqueue(object :Callback{
            private val MsutableLit = mutableListOf<JSONObject>()
            fun setData(datas : MutableList<JSONObject>){
                if(datas.isNotEmpty()) //如果資料不為空
                    MsutableLit.addAll(datas) //複製整組已獲得的資料
            }

            //失敗
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Weather", "${e.message}")
            }
            //成功
            override fun onResponse(call: Call, response: Response) {
                val HttpDatas = response.body?.string() //取得網路資料
                val WeatherArray = JSONArray(HttpDatas) //建構Json檔

                var WeatherDataList = mutableListOf<JSONObject>() //
                for (i in 0 until WeatherArray.length()) { //拆分取得的Json所有類
                    val properties = WeatherArray.getJSONObject(i).toString(i) //
                    WeatherDataList.add(JSONObject(properties)) //
                    Log.d("Weather", "${WeatherDataList[i].getString("Location")}")
                }
            }
        })//執行連線
    }

}