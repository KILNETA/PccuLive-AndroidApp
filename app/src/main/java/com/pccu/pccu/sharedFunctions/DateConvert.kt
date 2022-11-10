package com.pccu.pccu.sharedFunctions



/**
 * 時間換算
 * @author KILNETA
 * @since Alpha_5.0
 */
object DateConvert{

    /**
     * 轉換月份 Int(2) -> En_Str(3)
     * @param month [String] 數字月份 ex:(01)
     * @return 英文月份縮寫 ex:(Jan) : [String]
     *
     * @author KILNETA
     * @since Alpha_2.0
     */
    fun monthNumToStr3 (month:String) : String{
        when(month){
            "01"-> return "Jan"
            "02"-> return "Feb"
            "03"-> return "Mar"
            "04"-> return "Apr"
            "05"-> return "May"
            "06"-> return "Jun"
            "07"-> return "Jul"
            "08"-> return "Aug"
            "09"-> return "Sep"
            "10"-> return "Oct"
            "11"-> return "Nov"
            "12"-> return "Dec"
        }
        return "--"
    }

    /**轉換月份 En_Str(3) -> Int(2)
     * @param month [String] 英文月份縮寫 ex:(Jan)
     * @return 數字月份 ex:(01): [String]
     */
    fun monthStr3ToNum (month:String) : String{
        when(month){
            "Jan"-> return "01"
            "Feb"-> return "02"
            "Mar"-> return "03"
            "Apr"-> return "04"
            "May"-> return "05"
            "Jun"-> return "06"
            "Jul"-> return "07"
            "Aug"-> return "08"
            "Sep"-> return "09"
            "Oct"-> return "10"
            "Nov"-> return "11"
            "Dec"-> return "12"
        }
        return "--"
    }

    /**微調公告發布時間(星期)
     * @param month [String] 修改前的英文星期
     * @return 修改後的中文星期: [String]
     */
    fun weekEnToZhTw (month:String) : String{
        when(month){
            "Mon"-> return "一"
            "Tue"-> return "二"
            "Wed"-> return "三"
            "Thu"-> return "四"
            "Fri"-> return "五"
            "Sat"-> return "六"
            "Sun"-> return "日"
        }
        return "--"
    }

}