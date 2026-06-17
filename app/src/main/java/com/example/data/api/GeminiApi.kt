package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

suspend fun generateBio(currentBio: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Please configure your Gemini API Key in the Secrets panel."
    }
    
    val prompt = "Generate a professional and catchy bio for a link-in-bio page. It should be 1-2 sentences. Here is the user's current bio or info: $currentBio. Do not enclose it in quotes."
    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt))))
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate bio."
    } catch (e: Exception) {
        "AI Error: ${e.message}"
    }
}

suspend fun suggestLinkInfo(url: String): Pair<String, String> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Pair("API Key Required", "API Key Required")
    }
    
    val prompt = "Based on this URL: $url, suggest a catchy, professional title (1-3 words) and a short description (1 sentence). Format exactly as follows:\nTITLE: <your title>\nDESC: <your description>"
    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt))))
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        
        var parsedTitle = "My Link"
        var parsedDesc = ""
        
        text.lines().forEach { line ->
            if (line.uppercase().startsWith("TITLE:")) {
                parsedTitle = line.substringAfter("TITLE:").trim('\"', ' ', '*')
            } else if (line.uppercase().startsWith("DESC:")) {
                parsedDesc = line.substringAfter("DESC:").trim('\"', ' ', '*')
            }
        }
        Pair(parsedTitle, parsedDesc)
    } catch (e: Exception) {
        Pair("My Link", "AI Error: ${e.message}")
    }
}
