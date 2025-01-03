package com.pccu.pccu.appStart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pccu.pccu.R
import com.pccu.pccu.about.UpdateNewsDialog
import com.pccu.pccu.sharedFunctions.Object_SharedPreferences

/**
 * PCCU_APP主頁建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Alpha_1.0
 */
class MainActivity : AppCompatActivity(R.layout.activity_main){

    /**
     * PCCU_APP主框架建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //建構主框架
        super.onCreate(savedInstanceState)

        /**底部導航視圖*/ // -> bottom_navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        /**設置導航控制器*/ // -> nav_fragment
        val navController = findNavController(R.id.nav_fragment)
        //底部導航視圖(bottom_navigation) 連結 導航控制器(nav_fragment)
        bottomNavigationView.setupWithNavController(navController)

        //判斷公告是否已被已讀
        val updateNews = Object_SharedPreferences["News", "update", baseContext].toString()
        //若未被已讀 顯示公告介面
        if(updateNews != UPDATE_NEWAS_STR) {
            /**公告介面*/
            val news = UpdateNewsDialog()
            news.setStyle(
                DialogFragment.STYLE_NO_TITLE,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth
            )
            //顯示介面
            news.show(supportFragmentManager, "UpdateNewsDialog")
        }
    }

    companion object {
        const val UPDATE_NEWAS_STR = "true_1.3.4"
    }
}