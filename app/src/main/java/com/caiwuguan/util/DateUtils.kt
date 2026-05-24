package com.caiwuguan.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val DATETIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.CHINA)

    /**
     * 获取某一月的第一天时间戳
     */
    fun getMonthStart(year: Int = Calendar.getInstance().get(Calendar.YEAR),
                      month: Int = Calendar.getInstance().get(Calendar.MONTH)): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 获取某一月的最后一天时间戳
     */
    fun getMonthEnd(year: Int = Calendar.getInstance().get(Calendar.YEAR),
                    month: Int = Calendar.getInstance().get(Calendar.MONTH)): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month + 1, 0, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /**
     * 获取今天的开始时间戳
     */
    fun getDayStart(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 获取今天的结束时间戳
     */
    fun getDayEnd(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /**
     * 判断是否是今天
     */
    fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 判断是否是本月
     */
    fun isThisMonth(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val now = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    /**
     * 格式化日期为字符串
     */
    fun formatDate(timestamp: Long): String {
        return if (isToday(timestamp)) {
            "今天"
        } else {
            DATE_FORMAT.format(Date(timestamp))
        }
    }

    /**
     * 格式化时间为字符串
     */
    fun formatTime(timestamp: Long): String {
        return TIME_FORMAT.format(Date(timestamp))
    }

    /**
     * 格式化日期时间为字符串
     */
    fun formatDateTime(timestamp: Long): String {
        return DATETIME_FORMAT.format(Date(timestamp))
    }

    /**
     * 获取月份名称
     */
    fun getMonthName(month: Int): String {
        val months = arrayOf("一月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "十二月")
        return months.getOrElse(month) { "未知" }
    }

    /**
     * 获取两个日期之间的月份列表
     */
    fun getMonthsBetween(startTimestamp: Long, endTimestamp: Long): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val startCal = Calendar.getInstance().apply { timeInMillis = startTimestamp }
        val endCal = Calendar.getInstance().apply { timeInMillis = endTimestamp }

        while (startCal.get(Calendar.YEAR) < endCal.get(Calendar.YEAR) ||
               startCal.get(Calendar.MONTH) <= endCal.get(Calendar.MONTH)) {
            result.add(Pair(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH)))
            startCal.add(Calendar.MONTH, 1)
            if (startCal.get(Calendar.YEAR) > endCal.get(Calendar.YEAR)) break
        }
        return result
    }
}
