package com.malla.mvp.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

data class ConversationPrefs(
    val bubbleStyle: String = BubbleStyle.ROUNDED.name,
    val ownBubbleColor: Int? = null,
    val otherBubbleColor: Int? = null,
    val fontSize: Float = 14f,
    val bubbleOpacity: Float = 1f,
    val ownTextColor: Int? = null,
    val otherTextColor: Int? = null,
    val incomingSound: String = "system",
    val outgoingSound: String = "system",
    val chatBackgroundColor: Int? = null
)

object ConversationPreferences {
    private const val PREFIX = "conversation_prefs_"
    val changes = MutableStateFlow(0L)

    fun load(context: Context, conversationId: String): ConversationPrefs {
        val prefs = context.getSharedPreferences("malla_conversation_prefs", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREFIX + conversationId, null) ?: return ConversationPrefs()
        return try {
            val json = JSONObject(jsonStr)
            ConversationPrefs(
                bubbleStyle = json.optString("bubbleStyle", BubbleStyle.ROUNDED.name),
                ownBubbleColor = if (json.has("ownBubbleColor")) json.getInt("ownBubbleColor") else null,
                otherBubbleColor = if (json.has("otherBubbleColor")) json.getInt("otherBubbleColor") else null,
                fontSize = json.optDouble("fontSize", 14.0).toFloat(),
                bubbleOpacity = json.optDouble("bubbleOpacity", 1.0).toFloat(),
                ownTextColor = if (json.has("ownTextColor")) json.getInt("ownTextColor") else null,
                otherTextColor = if (json.has("otherTextColor")) json.getInt("otherTextColor") else null,
                incomingSound = json.optString("incomingSound", "system"),
                outgoingSound = json.optString("outgoingSound", "system"),
                chatBackgroundColor = if (json.has("chatBackgroundColor")) json.getInt("chatBackgroundColor") else null
            )
        } catch (e: Exception) {
            ConversationPrefs()
        }
    }

    fun save(context: Context, conversationId: String, prefs: ConversationPrefs) {
        val json = JSONObject().apply {
            put("bubbleStyle", prefs.bubbleStyle)
            put("fontSize", prefs.fontSize.toDouble())
            put("bubbleOpacity", prefs.bubbleOpacity.toDouble())
            prefs.ownBubbleColor?.let { put("ownBubbleColor", it) }
            prefs.otherBubbleColor?.let { put("otherBubbleColor", it) }
            prefs.ownTextColor?.let { put("ownTextColor", it) }
            prefs.otherTextColor?.let { put("otherTextColor", it) }
            put("incomingSound", prefs.incomingSound)
            put("outgoingSound", prefs.outgoingSound)
            prefs.chatBackgroundColor?.let { put("chatBackgroundColor", it) }
        }
        context.getSharedPreferences("malla_conversation_prefs", Context.MODE_PRIVATE)
            .edit().putString(PREFIX + conversationId, json.toString()).apply()
        changes.value++
    }

    fun delete(context: Context, conversationId: String) {
        context.getSharedPreferences("malla_conversation_prefs", Context.MODE_PRIVATE)
            .edit().remove(PREFIX + conversationId).apply()
        changes.value++
    }
}
