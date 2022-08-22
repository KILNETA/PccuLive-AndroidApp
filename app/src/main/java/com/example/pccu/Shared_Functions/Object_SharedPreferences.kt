package com.example.pccu.Shared_Functions

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.*
import java.lang.Exception

/**
 * SharedPreferences工具類，可以保存object對象
 * 存儲時以object存儲到本地，獲取時返回的也是object對象，需要自己進行強制轉換
 * 也就是說，存的人和取的人要是同一個人才知道取出來的東西到底是個啥
 * 參考來源 : https://www.jianshu.com/p/ae0ca6c2d926
 *
 * @since Alpha_4.0
 */
object Object_SharedPreferences {
    /**
     * writeObject 方法負責寫入特定類的對象的狀態，以便相應的 readObject 方法可以還原它
     * 最後，用Base64.encode將字節文件轉換成Base64編碼保存在String中
     * @param object 待加密的轉換為String的對象
     * @return String   加密後的String
     *
     * @since Alpha_4.0
     */
    private fun Object2String(`object`: Any): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null
        return try {
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(`object`)
            val string = String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
            objectOutputStream.close()
            string
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 使用Base64解密String，返回Object對象
     * @param objectString 待解密的String
     * @return object      解密後的object
     *
     * @since Alpha_4.0
     */
    private fun String2Object(objectString: String): Any? {
        val mobileBytes = Base64.decode(objectString.toByteArray(), Base64.DEFAULT)
        val byteArrayInputStream = ByteArrayInputStream(mobileBytes)
        var objectInputStream: ObjectInputStream? = null
        return try {
            objectInputStream = ObjectInputStream(byteArrayInputStream)
            val `object` = objectInputStream.readObject()
            objectInputStream.close()
            `object`
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 使用SharedPreference保存對象
     * @param fileKey    儲存文件的key
     * @param key        儲存對象的key
     * @param saveObject 儲存的對象
     * @param context [Context]     上下文
     *
     * @since Alpha_4.0
     */
    fun save(fileKey: String?, key: String?, saveObject: Any, context: Context) {
        //創建SharedPreferences
        val sharedPreferences: SharedPreferences =
            context.getApplicationContext().getSharedPreferences(fileKey, Activity.MODE_PRIVATE)
        //取得SharedPreferences.Editor編輯內容
        val editor = sharedPreferences.edit()
        //用Base64.encode將字節文件轉換成Base64編碼保存在String中
        val string = Object2String(saveObject)
        //放入字串，並定義索引為"Saved"
        editor.putString(key, string)
        //不返回結果的提交
        editor.apply()
    }

    /**
     * 獲取SharedPreference保存的對象
     * @param fileKey [String] 儲存文件的key
     * @param key [String] 儲存對象的key
     * @param context [Context] 上下文
     * @return object : [Any] 返回根據key得到的對象
     *
     * @since Alpha_4.0
     */
    operator fun get(fileKey: String?, key: String?, context: Context): Any? {
        //創建SharedPreferences
        val sharedPreferences: SharedPreferences =
            context.getApplicationContext().getSharedPreferences(fileKey, Activity.MODE_PRIVATE)

        //回傳在"Saved"索引之下的資料；若無儲存則回傳"未存任何資料"
        val string = sharedPreferences.getString(key, null)
        //回傳 使用Base64解密String後的Object對象
        return string?.let { String2Object(it) }
    }

    /**
     * 刪除SharedPreference保存的對象
     * @param fileKey 儲存文件的key
     * @param context [Context] 上下文
     *
     * @since Alpha_4.0
     */
    fun clear(fileKey: String, context: Context) {
        //創建SharedPreferences
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(fileKey, Context.MODE_PRIVATE)

        //取得SharedPreferences.Editor
        val editor = sharedPreferences.edit()
        //利用clear清除掉所有資料
        editor.clear()

        //不返回結果的提交
        //若需要提交結果，則可使用.commit()
        editor.apply()
    }
}











/**
 * SharedPreferences 資料本地存儲類別 (已棄用僅能存儲String格式 備存用)
 *
 * @author KILNETA
 * @since Alpha_2.0
 */
object String_SharedPreferences {

    /**
     * 寫入SharedPreferences存儲資料
     * @param data  [String] 存儲的資料
     * @param storageArea  [String] 資料區塊
     * @param storagePoint [String] 資料節點
     * @param context [Context] 上下文
     * @return success : [Boolean] 是否成功
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun Write(data: String, storageArea: String, storagePoint: String, context: Context): Boolean {
        if (data.isEmpty()) return false
        //創建SharedPreferences，索引為"Data"
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)

        //取得SharedPreferences.Editor編輯內容
        val editor = sharedPreferences.edit()
        //放入字串，並定義索引為"Saved"
        editor.putString(storagePoint, data)
        //提交；提交結果將會回傳一布林值
        //若不需要提交結果，則可使用.apply()
        return editor.commit()
    }

    /**
     * 讀取SharedPreferences存儲資料
     * @param storageArea  [String] 資料區塊
     * @param storagePoint [String] 資料節點
     * @param context [Context] 上下文
     * @return data : [String] 存儲的資料
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun Read(storageArea: String, storagePoint: String, context: Context): String? {
        //創建SharedPreferences，索引為"Data"
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)
        //回傳在"Saved"索引之下的資料；若無儲存則回傳"未存任何資料"
        return sharedPreferences.getString(storagePoint, "NULL")
    }

    /**
     * 清除SharedPreferences存儲的資料
     * @param storageArea  [String] 資料區塊
     * @param context [Context] 上下文
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun Clear(storageArea: String, context: Context) {
        //創建SharedPreferences
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(storageArea, Context.MODE_PRIVATE)

        //取得SharedPreferences.Editor
        val editor = sharedPreferences.edit()
        //利用clear清除掉所有資料
        editor.clear()
        //不返回結果的提交
        //若需要提交結果，則可使用.commit()
        editor.apply()
    }
}