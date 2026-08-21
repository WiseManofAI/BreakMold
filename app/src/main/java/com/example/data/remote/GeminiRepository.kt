package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiRepository(
    private val apiService: GeminiApiService = GeminiApiClient.service
) {
    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    fun hasApiKey(): Boolean = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    suspend fun optimizeScheduleAndAlarms(
        userRoutinePrompt: String,
        currentWakeUpTime: String = "06:30 AM",
        firstKeyMeeting: String = "08:00 AM"
    ): Result<AIScheduleResult> = withContext(Dispatchers.IO) {
        if (!hasApiKey()) {
            Log.d("GeminiRepository", "Using local smart heuristic AI engine (no API key set)")
            return@withContext Result.success(generateHeuristicSchedule(userRoutinePrompt))
        }

        try {
            val systemPrompt = """
                You are BreakMold AI, an elite productivity architect.
                Analyze the user's daily intentions, time-block them into a hyper-efficient schedule, and compute the optimal Smart Adaptive Alarm wake-up time with buffer for hydration and preparation.
                Return ONLY valid JSON matching this exact structure (no markdown fences, no extra text):
                {
                  "summary": "Optimized high-output routine with 90m deep work blocks and dynamic recovery",
                  "suggestedWakeUpTime": "06:30 AM",
                  "motivationalQuote": "Mold is broken in the first hour of discipline.",
                  "totalEstimatedXp": 120,
                  "timeBlocks": [
                    {
                      "title": "Wake Up & Protocol",
                      "timeString": "06:30 AM",
                      "durationMinutes": 30,
                      "tag": "ROUTINE",
                      "prepWarningMinutes": 0,
                      "isAutoSyncedCalendar": false,
                      "xpReward": 15
                    },
                    {
                      "title": "Deep Work Block",
                      "timeString": "08:00 AM",
                      "durationMinutes": 90,
                      "tag": "WORK",
                      "prepWarningMinutes": 15,
                      "isAutoSyncedCalendar": true,
                      "xpReward": 50
                    }
                  ]
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = "User Intentions & Schedule Request: $userRoutinePrompt\nTarget First Meeting: $firstKeyMeeting\nCurrent alarm: $currentWakeUpTime"
                            )
                        )
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4,
                    responseMimeType = "application/json"
                )
            )

            val response = apiService.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response from Gemini API")

            val parsed = parseJsonResponse(rawText)
            Result.success(parsed)
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Gemini API error, falling back to heuristic engine", e)
            Result.success(generateHeuristicSchedule(userRoutinePrompt))
        }
    }

    private fun parseJsonResponse(rawJson: String): AIScheduleResult {
        val clean = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(clean)
        val summary = json.optString("summary", "AI Optimized Daily Schedule")
        val suggestedWakeUp = json.optString("suggestedWakeUpTime", "06:30 AM")
        val quote = json.optString("motivationalQuote", "Break the mold today.")
        val totalXp = json.optInt("totalEstimatedXp", 100)

        val blocksArray = json.optJSONArray("timeBlocks") ?: JSONArray()
        val blocks = mutableListOf<AITimeBlock>()
        for (i in 0 until blocksArray.length()) {
            val item = blocksArray.getJSONObject(i)
            blocks.add(
                AITimeBlock(
                    title = item.optString("title", "Focus Session"),
                    timeString = item.optString("timeString", "09:00 AM"),
                    durationMinutes = item.optInt("durationMinutes", 45),
                    tag = item.optString("tag", "WORK"),
                    prepWarningMinutes = item.optInt("prepWarningMinutes", 15),
                    isAutoSyncedCalendar = item.optBoolean("isAutoSyncedCalendar", true),
                    xpReward = item.optInt("xpReward", 20)
                )
            )
        }

        if (blocks.isEmpty()) {
            return generateHeuristicSchedule("Standard Schedule")
        }

        return AIScheduleResult(
            summary = summary,
            suggestedWakeUpTime = suggestedWakeUp,
            timeBlocks = blocks,
            motivationalQuote = quote,
            totalEstimatedXp = totalXp
        )
    }

    private fun generateHeuristicSchedule(prompt: String): AIScheduleResult {
        val blocks = mutableListOf<AITimeBlock>()
        blocks.add(
            AITimeBlock(
                title = "Wake Up & Hydration Protocol",
                timeString = "06:30 AM",
                durationMinutes = 30,
                tag = "ROUTINE",
                prepWarningMinutes = 0,
                isAutoSyncedCalendar = false,
                xpReward = 15
            )
        )
        blocks.add(
            AITimeBlock(
                title = "Deep Work Block #1",
                timeString = "08:00 AM",
                durationMinutes = 90,
                tag = "WORK",
                prepWarningMinutes = 15,
                isAutoSyncedCalendar = true,
                xpReward = 50
            )
        )
        blocks.add(
            AITimeBlock(
                title = "Lunch & Active Recovery Walk",
                timeString = "12:30 PM",
                durationMinutes = 45,
                tag = "HEALTH",
                prepWarningMinutes = 5,
                isAutoSyncedCalendar = true,
                xpReward = 25
            )
        )
        blocks.add(
            AITimeBlock(
                title = "Deep Work Block #2 (Sprint & Architecture)",
                timeString = "02:30 PM",
                durationMinutes = 90,
                tag = "FOCUS",
                prepWarningMinutes = 10,
                isAutoSyncedCalendar = true,
                xpReward = 45
            )
        )
        blocks.add(
            AITimeBlock(
                title = "Day Review & Habit Check",
                timeString = "06:00 PM",
                durationMinutes = 30,
                tag = "ROUTINE",
                prepWarningMinutes = 5,
                isAutoSyncedCalendar = false,
                xpReward = 20
            )
        )

        val quote = if (prompt.contains("gym", ignoreCase = true) || prompt.contains("workout", ignoreCase = true)) {
            "Iron sharpens iron. Your physical limits define your mental capacity."
        } else if (prompt.contains("study", ignoreCase = true) || prompt.contains("exam", ignoreCase = true)) {
            "Deep comprehension is forged in uninterrupted silence."
        } else {
            "Today, Break The Mold. Execute with ruthless precision."
        }

        return AIScheduleResult(
            summary = "Smart adaptive 5-stage time-blocking schedule with +155 Total XP potential",
            suggestedWakeUpTime = "06:30 AM",
            timeBlocks = blocks,
            motivationalQuote = quote,
            totalEstimatedXp = 155
        )
    }
}
