package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>?
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String?
)

@JsonClass(generateAdapter = true)
data class MonsterAnalysisResult(
    val isMonster: Boolean,
    val flavorId: String,
    val flavorName: String,
    val caffeineMg: Int,
    val volumeMl: Int,
    val notes: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeMonsterCan(bitmap: Bitmap): MonsterAnalysisResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured.")
            return@withContext null
        }

        val base64Image = bitmap.toBase64()
        
        val prompt = """
            Analyze the image of this energy drink can. Determine if it is a Monster Energy flavor.
            Provide your response strictly as a JSON object with the following fields:
            {
              "isMonster": true/false,
              "flavorId": "original", "ultra_white", "mango_loco", "pipeline_punch", "aussie_lemonade", "ultra_violet", "ultra_paradise", "khaotic", "pacific_punch", or "custom" (select most matching, use "custom" if it's another Monster flavor not listed),
              "flavorName": "The name of the flavor",
              "caffeineMg": integer (estimated caffeine in mg, usually 160),
              "volumeMl": integer (estimated volume in ml, usually 500),
              "notes": "Short description or AI observation about this drink"
            }
            Do not wrap in markdown tags. Provide ONLY the raw JSON.
        """.trimIndent()

        val requestJson = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": ${escapeJsonString(prompt)} },
                    {
                      "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": "$base64Image"
                      }
                    }
                  ]
                }
              ],
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed with code: ${response.code}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Response: ${responseBody.take(100)}...")
                
                val geminiResponse = moshi.adapter(GeminiResponse::class.java).fromJson(responseBody)
                val rawText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (rawText != null) {
                    Log.d(TAG, "Raw text result: $rawText")
                    return@withContext moshi.adapter(MonsterAnalysisResult::class.java).fromJson(rawText)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
        }
        
        return@withContext null
    }

    private fun escapeJsonString(string: String): String {
        val escaped = string
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
