package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val geminiKey = BuildConfig.GEMINI_API_KEY
        val hasGemini = geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.GEMINI,
                name = "Google Gemini",
                apiKey = geminiKey,
                baseUrl = "https://generativelanguage.googleapis.com",
                model = "gemini-3.5-flash",
                isEnabled = hasGemini,
                priority = 1,
                costMultiplier = 0.1,
                capabilityScore = 9
            )
        )

        // 2. Groq
        val groqKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        val hasGroq = groqKey.isNotBlank() && groqKey != "MY_GROQ_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.GROQ,
                name = "Groq Cloud",
                apiKey = groqKey,
                baseUrl = "https://api.groq.com/openai/v1",
                model = "llama-3.3-70b-versatile",
                isEnabled = hasGroq,
                priority = 2,
                costMultiplier = 0.2,
                capabilityScore = 8
            )
        )

        // 3. Z.ai
        val zaiKey = try { BuildConfig.ZAI_API_KEY } catch (e: Exception) { "" }
        val hasZai = zaiKey.isNotBlank() && zaiKey != "MY_ZAI_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.ZAI,
                name = "Z.ai API",
                apiKey = zaiKey,
                baseUrl = "https://api.z.ai/api/paas/v4",
                model = "glm-4.7",
                isEnabled = hasZai,
                priority = 3,
                costMultiplier = 0.3,
                capabilityScore = 8
            )
        )

        // 4. OpenRouter
        val openRouterKey = try { BuildConfig.OPENROUTER_API_KEY } catch (e: Exception) { "" }
        val hasOpenRouter = openRouterKey.isNotBlank() && openRouterKey != "MY_OPENROUTER_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OPENROUTER,
                name = "OpenRouter",
                apiKey = openRouterKey,
                baseUrl = "https://openrouter.ai/api/v1",
                model = "meta-llama/llama-3-8b-instruct:free",
                isEnabled = hasOpenRouter,
                priority = 4,
                costMultiplier = 0.1,
                capabilityScore = 7
            )
        )

        // 5. OpenAI
        val openaiKey = try { BuildConfig.OPENAI_API_KEY } catch (e: Exception) { "" }
        val hasOpenai = openaiKey.isNotBlank() && openaiKey != "MY_OPENAI_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OPENAI,
                name = "OpenAI",
                apiKey = openaiKey,
                baseUrl = "https://api.openai.com/v1",
                model = "gpt-4o-mini",
                isEnabled = hasOpenai,
                priority = 5,
                costMultiplier = 0.5,
                capabilityScore = 9
            )
        )

        // 6. Hugging Face
        val hfKey = try { BuildConfig.HUGGINGFACE_API_KEY } catch (e: Exception) { "" }
        val hasHf = hfKey.isNotBlank() && hfKey != "MY_HUGGINGFACE_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.HUGGINGFACE,
                name = "Hugging Face Hub",
                apiKey = hfKey,
                baseUrl = "https://router.huggingface.co/v1",
                model = "meta-llama/Llama-3.3-70B-Instruct",
                isEnabled = hasHf,
                priority = 6,
                costMultiplier = 0.0,
                capabilityScore = 8
            )
        )

        // 7. DeepSeek
        val deepseekKey = try { BuildConfig.DEEPSEEK_API_KEY } catch (e: Exception) { "" }
        val hasDeepseek = deepseekKey.isNotBlank() && deepseekKey != "MY_DEEPSEEK_API_KEY"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.DEEPSEEK,
                name = "DeepSeek AI",
                apiKey = deepseekKey,
                baseUrl = "https://api.deepseek.com",
                model = "deepseek-chat",
                isEnabled = hasDeepseek,
                priority = 7,
                costMultiplier = 0.15,
                capabilityScore = 8
            )
        )

        // 8. Ollama (Local)
        val ollamaUrl = try { BuildConfig.OLLAMA_API_URL } catch (e: Exception) { "" }
        val hasOllama = ollamaUrl.isNotBlank() && ollamaUrl != "http://localhost:11434"
        providers.add(
            AiProviderConfig(
                id = AiProviderId.OLLAMA,
                name = "Ollama Local",
                apiKey = "",
                baseUrl = if (hasOllama) ollamaUrl else "http://localhost:11434",
                model = "llama3",
                isEnabled = hasOllama,
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
                    apiKey = BuildConfig.GEMINI_API_KEY,
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

    private suspend fun makeApiCall(config: AiProviderConfig, prompt: String, temperature: Double): String {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBuilder = Request.Builder()

        val url: String
        val requestBodyJson: String

        when (config.id) {
            AiProviderId.GEMINI -> {
                url = "${config.baseUrl}/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
                requestBodyJson = JSONObject()
                    .put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                    .put("generationConfig", JSONObject().put("temperature", temperature))
                    .toString()
            }

            AiProviderId.OLLAMA -> {
                url = "${config.baseUrl}/api/generate"
                requestBodyJson = JSONObject()
                    .put("model", config.model)
                    .put("prompt", prompt)
                    .put("stream", false)
                    .toString()
            }

            else -> {
                url = "${config.baseUrl}/chat/completions"
                requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
                requestBodyJson = JSONObject()
                    .put("model", config.model)
                    .put("messages", org.json.JSONArray().put(
                        JSONObject().put("role", "user").put("content", prompt)
                    ))
                    .put("temperature", temperature)
                    .toString()
            }
        }

        val requestBody = requestBodyJson.toRequestBody(mediaType)
        val request = requestBuilder
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.body?.string() ?: ""}")
            }
            val responseString = response.body?.string() ?: throw Exception("Null response body")
            return parseResponseText(config.id, responseString)
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
