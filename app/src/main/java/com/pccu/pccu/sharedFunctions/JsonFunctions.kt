package com.pccu.pccu.sharedFunctions

import com.google.gson.Gson
import okhttp3.ResponseBody
import java.lang.reflect.Type

/**
 * Json 輔助工具
 * @author KILNETA
 * @since Alpha_5.0
 */
object JsonFunctions {

    /**
     * 將Json轉化為指定結構 (配合動態型API使用)
     * @param json [String] json文檔
     * @param type [Type] 套用的結構
     * @return 對應結構的json檔案 : [T]
     *
     * @author KILNETA
     * @since Alpha_5.0
     */
    fun <T> fromJson(json: ResponseBody, type: Type): T {
        return Gson().fromJson(json.string(), type)
    }
}