package com.example.pccu.Shared_Functions

import android.content.Context
import android.content.SharedPreferences

object MySharedPreferences {
    fun Write(data: String, storageArea: String, storagePoint: String, context: Context): Boolean {
        if (data.length == 0) return false
        /**創建SharedPreferences，索引為"Data" */
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)

        /**取得SharedPreferences.Editor編輯內容 */
        val editor = sharedPreferences.edit()
        /**放入字串，並定義索引為"Saved" */
        editor.putString(storagePoint, data)
        /**提交；提交結果將會回傳一布林值 */
        /**若不需要提交結果，則可使用.apply() */
        return editor.commit()
    }

    fun Read(storageArea: String, storagePoint: String, context: Context): String? {
        /**創建SharedPreferences，索引為"Data" */
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)
        /**回傳在"Saved"索引之下的資料；若無儲存則回傳"未存任何資料" */
        return sharedPreferences.getString(storagePoint, "NULL")
    }

    fun Clear(storageArea: String, context: Context) {
        /**創建SharedPreferences */
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)

        /**取得SharedPreferences.Editor */
        val editor = sharedPreferences.edit()
        /**利用clear清除掉所有資料 */
        editor.clear()
        /**不返回結果的提交 */
        /**若需要提交結果，則可使用.commit() */
        editor.apply()
    }
}