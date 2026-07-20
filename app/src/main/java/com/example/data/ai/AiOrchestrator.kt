package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProviderId {
    GEMINI, GROQ, ZAI, OPENROUTER, OPENAI, HUGGINGFACE, DEEPSEEK, OLLAMA
}

data class AiProviderConfig(
    val id: AiProviderId,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val isEnabled: Boolean,
    val priority: Int, // lower is higher priority
    val costMultiplier: Double = 1.0,
    val capabilityScore: Int = 8
)

data class ProviderMetrics(
    val id: AiProviderId,
    var totalRequests: Int = 0,
    var totalSuccesses: Int = 0,
    var totalFailures: Int = 0,
    var averageLatencyMs: Long = 0L,
    var lastLatencyMs: Long = 0L,
    var lastSuccessfulRequestTime: Long = 0L,
    var isDisabled: Boolean = false,
    var disabledUntil: Long = 0L,
    var consecutiveFailures: Int = 0
)

data class AiOrchestratorLog(
    val timestamp: Long = System.currentTimeMillis(),
    val promptLength: Int,
    val selectedProvider: AiProviderId,
    val fallbackProviders: List<AiProviderId>,
    val finalStatus: String,
    val processingTimeMs: Long,
    val errors: List<String>,
    val retryCount: Int
)

object AiOrchestrator {
    private const val TAG = "AiOrchestrator"
    
    // In-memory list to store recent orchestration logs
    val logs = mutableListOf<AiOrchestratorLog>()
    
    // Configurable list of providers
    private val providers = mutableListOf<AiProviderConfig>()
    
    // Health metrics
    private val healthMetrics = mutableMapOf<AiProviderId, ProviderMetrics>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        initializeProviders()
    }

    @Synchronized
    fun initializeProviders() {
        providers.clear()
        
        // 1. Gemini (Default and primary)
        providers.add(
            AiProviderConfig(
                id = AiProviderId.GEMINI,
                name = "Google Gemini",
                apiKey = "",
                baseUrl = "https://generativelanguage.googleapis.com",
                model = "gemini-3.5-flash",
                isEnabled = true,
                priority = 1,
                costMultiplier = 0.1,
                capabilityScore = 9
            )
        )

        // 2. Groq
        providers.add(
            AiProviderConfig(
                id = AiProviderId.GROQ,
                name = "Groq Cloud",
                apiKey = "",
                baseUrl = "https://api.groq.com/openai/v1",
                model = "llama-3.3-70b-versatile",
                isEnabled = true,
                priority = 2,
                costMultiplier = 0.2,
                capabilityScore = 8
            )
        )

        // 3. Z.ai
        providers.add(
            AiProviderConfig(
                id = AiProviderId.ZAI,
                name = "Z.ai API",
                apiKey = "",
                baseUrl = "https://api.z.ai/api/paas/v4",
                model = "glm-4.7",
                isEnabled = true,
                priority = 3,
                costMultiplier = 0.3,
                capabilityScore = 8
            )
        )

        // 4. OpenRouter
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OPENROUTER,
                name = "OpenRouter",
                apiKey = "",
                baseUrl = "https://openrouter.ai/api/v1",
                model = "meta-llama/llama-3-8b-instruct:free",
                isEnabled = true,
                priority = 4,
                costMultiplier = 0.1,
                capabilityScore = 7
            )
        )

        // 5. OpenAI
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OPENAI,
                name = "OpenAI",
                apiKey = "",
                baseUrl = "https://api.openai.com/v1",
                model = "gpt-4o-mini",
                isEnabled = true,
                priority = 5,
                costMultiplier = 0.5,
                capabilityScore = 9
            )
        )

        // 6. Hugging Face
        providers.add(
            AiProviderConfig(
                id = AiProviderId.HUGGINGFACE,
                name = "Hugging Face Hub",
                apiKey = "",
                baseUrl = "https://router.huggingface.co/v1",
                model = "meta-llama/Llama-3.3-70B-Instruct",
                isEnabled = true,
                priority = 6,
                costMultiplier = 0.0,
                capabilityScore = 8
            )
        )

        // 7. DeepSeek
        providers.add(
            AiProviderConfig(
                id = AiProviderId.DEEPSEEK,
                name = "DeepSeek AI",
                apiKey = "",
                baseUrl = "https://api.deepseek.com",
                model = "deepseek-chat",
                isEnabled = true,
                priority = 7,
                costMultiplier = 0.15,
                capabilityScore = 8
            )
        )

        // 8. Ollama (Local)
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OLLAMA,
                name = "Ollama Local",
                apiKey = "",
                baseUrl = "http://localhost:11434",
                model = "llama3",
                isEnabled = false,
                priority = 8,
                costMultiplier = 0.0,
                capabilityScore = 6
            )
        )

        // Initialize metrics
        AiProviderId.values().forEach { id ->
            if (!healthMetrics.containsKey(id)) {
                healthMetrics[id] = ProviderMetrics(id = id)
            }
        }
    }

    @Synchronized
    fun getActiveProviders(): List<AiProviderConfig> {
        return providers.filter { it.isEnabled }
    }

    @Synchronized
    fun getMetrics(id: AiProviderId): ProviderMetrics {
        return healthMetrics[id] ?: ProviderMetrics(id = id)
    }

    @Synchronized
    fun updateMetricsSuccess(id: AiProviderId, latencyMs: Long) {
        val metrics = healthMetrics[id] ?: return
        metrics.totalRequests++
        metrics.totalSuccesses++
        metrics.consecutiveFailures = 0
        metrics.lastLatencyMs = latencyMs
        metrics.averageLatencyMs = if (metrics.averageLatencyMs == 0L) latencyMs else (metrics.averageLatencyMs * 4 + latencyMs) / 5
        metrics.lastSuccessfulRequestTime = System.currentTimeMillis()
        metrics.isDisabled = false
    }

    @Synchronized
    fun updateMetricsFailure(id: AiProviderId) {
        val metrics = healthMetrics[id] ?: return
        metrics.totalRequests++
        metrics.totalFailures++
        metrics.consecutiveFailures++
        
        // If repeatedly failing (e.g. 3 consecutive failures), disable temporarily for 30 seconds
        if (metrics.consecutiveFailures >= 3) {
            metrics.isDisabled = true
            metrics.disabledUntil = System.currentTimeMillis() + 30_000L
            Log.w(TAG, "Provider $id disabled temporarily due to consecutive failures")
        }
    }

    @Synchronized
    fun routeProviders(): List<AiProviderConfig> {
        val now = System.currentTimeMillis()
        val activeConfigs = providers.filter { it.isEnabled }
        
        val availableConfigs = activeConfigs.filter { config ->
            val metrics = healthMetrics[config.id] ?: return@filter true
            if (metrics.isDisabled) {
                if (now > metrics.disabledUntil) {
                    metrics.isDisabled = false
                    metrics.consecutiveFailures = 0
                    true
                } else {
                    false
                }
            } else {
                true
            }
        }

        val configsToUse = if (availableConfigs.isEmpty()) activeConfigs else availableConfigs

        val finalConfigs = if (configsToUse.isEmpty()) {
            listOf(
                AiProviderConfig(
                    id = AiProviderId.GEMINI,
                    name = "Google Gemini",
                    apiKey = "",
                    baseUrl = "https://generativelanguage.googleapis.com",
                    model = "gemini-3.5-flash",
                    isEnabled = true,
                    priority = 1
                )
            )
        } else {
            configsToUse
        }

        return finalConfigs.sortedWith { a, b ->
            val metricsA = healthMetrics[a.id] ?: ProviderMetrics(a.id)
            val metricsB = healthMetrics[b.id] ?: ProviderMetrics(b.id)

            val scoreA = a.priority * 10 + (if (metricsA.averageLatencyMs > 0) metricsA.averageLatencyMs / 500.0 else 5.0)
            val scoreB = b.priority * 10 + (if (metricsB.averageLatencyMs > 0) metricsB.averageLatencyMs / 500.0 else 5.0)
            scoreA.compareTo(scoreB)
        }
    }

    suspend fun executePrompt(prompt: String, temperature: Double = 0.7): String = withContext(Dispatchers.IO) {
        val sortedProviders = routeProviders()
        val fallbackChain = mutableListOf<AiProviderId>()
        val errors = mutableListOf<String>()
        var successResponse: String? = null
        var chosenProvider: AiProviderId = AiProviderId.GEMINI
        var retryCount = 0
        val startTime = System.currentTimeMillis()

        for (i in sortedProviders.indices) {
            val provider = sortedProviders[i]
            if (i > 0) {
                fallbackChain.add(provider.id)
            }
            if (i == 0) {
                chosenProvider = provider.id
            }

            Log.d(TAG, "Trying AI request with provider: ${provider.name} (${provider.id}) using model ${provider.model}")
            val startCallTime = System.currentTimeMillis()
            try {
                val responseText = makeApiCall(provider, prompt, temperature)
                if (responseText.isNotBlank()) {
                    val duration = System.currentTimeMillis() - startCallTime
                    updateMetricsSuccess(provider.id, duration)
                    successResponse = responseText
                    break
                } else {
                    throw Exception("Empty response from provider ${provider.id}")
                }
            } catch (e: Exception) {
                retryCount++
                val errMessage = e.localizedMessage ?: "Unknown error"
                Log.e(TAG, "Provider ${provider.id} failed: $errMessage")
                errors.add("${provider.id}: $errMessage")
                updateMetricsFailure(provider.id)
            }
        }

        val totalDuration = System.currentTimeMillis() - startTime
        val logEntry = AiOrchestratorLog(
            promptLength = prompt.length,
            selectedProvider = chosenProvider,
            fallbackProviders = fallbackChain,
            finalStatus = if (successResponse != null) "SUCCESS" else "FAILED",
            processingTimeMs = totalDuration,
            errors = errors,
            retryCount = retryCount
        )
        
        synchronized(logs) {
            logs.add(logEntry)
            if (logs.size > 100) {
                logs.removeAt(0)
            }
        }

        if (successResponse != null) {
            return@withContext successResponse
        }

        Log.e(TAG, "All AI providers failed. Fallback error message returned.")
        "எடப்பாடி மக்களே! 🌾 சிறிய நெட்வொர்க் சுணக்கம் ஏற்பட்டுள்ளதால், நீங்கள் கேட்ட வசதிகளுக்கு உடனடியாக எங்களை Coscoom Creative Tech Solutions (8778148899) என்ற எண்ணில் கால் அல்லது வாட்ஸ்அப் மூலம் தொடர்பு கொள்ள அன்புடன் கேட்டுக்கொள்கிறோம்!"
    }

    suspend fun makeApiCall(config: AiProviderConfig, prompt: String, temperature: Double): String = withContext(Dispatchers.IO) {
        if (config.id == AiProviderId.OLLAMA) {
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBodyJson = JSONObject()
                .put("model", config.model)
                .put("prompt", prompt)
                .put("stream", false)
                .toString()
            val requestBody = requestBodyJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${config.baseUrl}/api/generate")
                .post(requestBody)
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}: ${response.body?.string() ?: ""}")
                }
                val responseString = response.body?.string() ?: throw Exception("Null response body")
                return@withContext parseResponseText(config.id, responseString)
            }
        } else {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val data = mapOf(
                "provider" to config.id.name,
                "prompt" to prompt,
                "model" to config.model
            )
            val result = functions.getHttpsCallable("callAiProvider")
                .call(data)
                .await()
            val resMap = result.data as? Map<*, *>
            val responseText = resMap?.get("response") as? String
            if (responseText != null) {
                return@withContext responseText
            } else {
                throw Exception("Cloud function returned empty or invalid response")
            }
        }
    }

    private fun parseResponseText(id: AiProviderId, rawJson: String): String {
        val root = JSONObject(rawJson)
        return when (id) {
            AiProviderId.GEMINI -> {
                val candidates = root.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                firstPart?.optString("text", "") ?: ""
            }

            AiProviderId.OLLAMA -> {
                root.optString("response", "")
            }

            else -> {
                val choices = root.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val message = firstChoice?.optJSONObject("message")
                message?.optString("content", "") ?: ""
            }
        }
    }
    
    fun validateMenuJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val hasRestaurantId = root.has("restaurant_id")
            val hasStoreInfo = root.has("store_info")
            val hasMenuData = root.has("menu_data")
            hasRestaurantId && hasStoreInfo && hasMenuData
        } catch (e: Exception) {
            false
        }
    }
}
