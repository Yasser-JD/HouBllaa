package com.jdcoding.houbllaa.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * API client for fetching pregnancy data
 */
object PregnancyApiClient {
    private const val TAG = "PregnancyApiClient"
    private const val BASE_URL = "https://pregnancy-tracking-api-production.up.railway.app/api"
    
    /**
     * Data class representing weekly pregnancy data from the API
     */
    data class WeeklyPregnancyData(
        val week: Int,
        val babyDevelopment: String,
        val bodyChanges: String,
        val tips: List<String>,
        val imageUrl: String
    )
    
    /**
     * Fetch all pregnancy data
     * @return List of WeeklyPregnancyData or null if failed
     */
    suspend fun fetchAllPregnancyData(): List<WeeklyPregnancyData>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/pregnancy")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    
                    parsePregnancyDataArray(response.toString())
                } else {
                    Log.e(TAG, "Error fetching all pregnancy data: HTTP $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching all pregnancy data", e)
                null
            }
        }
    }
    
    /**
     * Fetch pregnancy data for a specific week
     * @param week The week of pregnancy (1-42)
     * @return WeeklyPregnancyData or null if failed
     */
    suspend fun fetchWeekData(week: Int): WeeklyPregnancyData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/pregnancy/week/$week")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    
                    parseWeekData(response.toString())
                } else {
                    Log.e(TAG, "Error fetching week $week data: HTTP $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching week $week data", e)
                null
            }
        }
    }
    
    private fun parsePregnancyDataArray(jsonString: String): List<WeeklyPregnancyData>? {
        return try {
            val weeklyDataList = mutableListOf<WeeklyPregnancyData>()
            val jsonArray = JSONObject(jsonString).getJSONArray("data")
            
            for (i in 0 until jsonArray.length()) {
                val weekData = jsonArray.getJSONObject(i)
                weeklyDataList.add(parseWeeklyData(weekData))
            }
            
            weeklyDataList
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pregnancy data array", e)
            null
        }
    }
    
    private fun parseWeekData(jsonString: String): WeeklyPregnancyData? {
        return try {
            val jsonObject = JSONObject(jsonString)
            parseWeeklyData(jsonObject)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing week data", e)
            null
        }
    }
    
    private fun parseWeeklyData(jsonObject: JSONObject): WeeklyPregnancyData {
        val week = jsonObject.getInt("week")
        val babyDevelopment = jsonObject.getString("baby_development")
        val bodyChanges = jsonObject.getString("body_changes")
        
        val tipsArray = jsonObject.getJSONArray("tips")
        val tips = mutableListOf<String>()
        for (i in 0 until tipsArray.length()) {
            tips.add(tipsArray.getString(i))
        }
        
        val imageUrl = jsonObject.getString("image")
        
        return WeeklyPregnancyData(
            week = week,
            babyDevelopment = babyDevelopment,
            bodyChanges = bodyChanges,
            tips = tips,
            imageUrl = imageUrl
        )
    }
}
