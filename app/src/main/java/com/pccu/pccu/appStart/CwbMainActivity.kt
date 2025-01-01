package com.pccu.pccu.appStart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pccu.pccu.R

/**
 * Cwb主框架建構類 : "AppCompatActivity"
 *
 * @author KILNETA
 * @since Alpha_4.0
 */
class CwbMainActivity: AppCompatActivity(R.layout.cwb_activity_main) {

    /**
     * Cwb主框架建構
     * @param savedInstanceState [Bundle] 傳遞的資料
     *
     * @author KILNETA
     * @since Alpha_1.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //建構主框架
        super.onCreate(savedInstanceState)

        /**設置導航控制器*/ // -> cwb_nav_fragment
        val navController = findNavController(R.id.cwb_nav_fragment)
        //底部導航視圖(cwb_bottom_navigation) 連結 導航控制器(nav_fragment)
        findViewById<BottomNavigationView>(R.id.cwb_bottom_navigation)?.setupWithNavController(navController)
    }
}