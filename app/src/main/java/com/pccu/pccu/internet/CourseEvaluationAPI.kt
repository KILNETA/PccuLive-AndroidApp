package com.pccu.pccu.internet

import android.util.Log
import okhttp3.ResponseBody
import java.io.Serializable
import kotlin.collections.ArrayList

/**
 * 課程評價API "class"
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
object CourseEvaluationAPI {
    /**Pccu課程評價網 根網址*/
    private const val baseUrl = "https://course.pccu.app/"

    /**
     * 課程評鑑API 總筆數 查詢 (回傳資料結構 [EvaluationCount])
     * @return 總筆數 : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getCount(): EvaluationCount? {
        //回傳 路線站點表
        return try {
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluationCount(
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }

    /**
     * 課程評鑑API 學院 查詢 (回傳資料結構 List<[String]>)
     * @return 學院列表 : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getColleges(): ArrayList<String>? {
        //回傳 路線站點表
        return try {
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluationColleges(
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }

    /**
     * 課程評鑑API 學系 查詢 (回傳資料結構 List<[Department]>)
     * @param college [String] 學院
     * @return 學系列表 : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getDepartments(college:String): ArrayList<Department>? {
        //回傳 路線站點表
        return try{
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluationDepartments(
                college
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }

    /**
     * 課程評鑑API 課程基本資料 查詢 (回傳資料結構 List<[CourseIntroduction]>)
     * @param college [String]      學院
     * @param department [String]   學系
     * @return 課程基本資料列表 : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getCourseIntroduction(college:String,department:String): ArrayList<CourseIntroduction>? {
        //回傳 路線站點表
        return try {
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluationIntroduction(
                college,                                                    //學院
                department                                                  //學系
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }

    /**
     * 課程評鑑API 課程基本資料 查詢(使用關鍵字) (回傳資料結構 [CourseIntroduction])
     * @param keyword [String] 關鍵字(課名、教授)
     * @return 課程基本資料列表 : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getCourseIntroduction_useKeyword(keyword :String): ArrayList<CourseIntroduction>? {
        //回傳 路線站點表
        return try {
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluation(
                keyword,                                                    //關鍵字(課名、教授)
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }

    /**
     * 課程評鑑API 課程評價資料 查詢 (回傳資料結構 List<[CourseEvaluation]>)
     * @param department [String]   學系
     * @param course [String]       課名
     * @param teacher [String]      教授
     * @return 課程評價資料(可能存在多個) : [ResponseBody]
     *
     * @author KILNETA
     * @since Beta_1.3.0
     */
    fun getCourseEvaluation(department:String,course:String,teacher:String)
    : ArrayList<CourseEvaluation>? {
        //回傳 路線站點表
        return try {
            HttpRetrofit.createJson(
                HttpRetrofit.ApiService::class.java,                        //Api資料節點接口
                baseUrl                                                     //根網域
            ).getCourseEvaluationEvaluates(
                department,                                                 //學系
                course,                                                     //課名
                teacher                                                     //教授
            ).execute().body()
        } catch (e: Exception) {
            Log.e("okHttp", e.toString())
            null
        }
    }
}

/**
 * 學系(含課程評價數) -數據結構
 * @param name          [String] 學系
 * @param course_count  [Int] 所含課程評價數
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
data class Department(
    val name : String,       //學系
    val course_count : Int   //所含課程評價數
) : Serializable

/**
 * 課程簡介 -數據結構
 * @param name          [String] 課程名稱
 * @param teacher       [String] 教授
 * @param department    [String] 學系
 * @param college       [String] 學院
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
data class CourseIntroduction(
    val name : String,      //課程名稱
    val teacher : String,   //教授
    val department : String,//系所
    val college : String,   //學院

) : Serializable

/**
 * 課程評價 -數據結構
 * @param assigment [String] 報告 / 作業
 * @param comment   [String] 其他補充
 * @param grading   [String] 評分 / 考試方式
 * @param rating    [Int] 推薦程度
 * @param semester  [String] 修課學期
 * @param teaching  [String] 上課內容 / 方式 / 規定 / 點名
 * @param timestamp [String] 填寫時間
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
data class CourseEvaluation(
    val assigment : String, //報告 / 作業
    val comment : String,   //其他補充
    var grading : String,   //評分 / 考試方式
    var rating : Int,       //推薦程度
    var semester : String,  //修課學期
    var teaching : String,  //上課內容 / 方式 / 規定 / 點名
    var timestamp : String  //填寫時間
) : Serializable

/**
 * 評價網 總評價數 -數據結構
 * @param count     [Int] 總評價數
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
data class EvaluationCount(
    val count : Int, //總評價數
) : Serializable

/************************ 配合控件操作使用 資料結構 ************************/

/**
 * 課程的評價列表 -數據結構
 * @param courseIntroduction    [CourseIntroduction] 資料源ID
 * @param courseEvaluation      ArrayList<[CourseEvaluation]> 各資料的結構
 * @param isCollapse            [Boolean] 是否折疊
 *
 * @author KILNETA
 * @since Beta_1.3.0
 */
data class CourseEvaluationItem(
    val courseIntroduction : CourseIntroduction,   //各資料的結構
    var courseEvaluation : ArrayList<CourseEvaluation>,   //課程的評價列表
    var isCollapse : Boolean = true
) : Serializable