package com.example.data

import android.content.ClipboardManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ClipCategory
import com.example.model.ClipboardItem
import com.example.model.SmartClipboardAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

private val Context.clipboardDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "tonekey_clipboard"
)

class ClipboardHistoryRepository(private val context: Context) {

  private object Keys {
    val CLIPS_JSON = stringPreferencesKey("clips_json")
  }

  val clipsFlow: Flow<List<ClipboardItem>> =
    context.clipboardDataStore.data
      .catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
      }
      .map { prefs ->
        decodeAndSort(prefs[Keys.CLIPS_JSON].orEmpty())
      }

  suspend fun syncFromSystemClipboard(): ClipboardItem? {
    val text =
      runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.primaryClip
          ?.takeIf { it.itemCount > 0 }
          ?.getItemAt(0)
          ?.coerceToText(context)
          ?.toString()
      }.getOrNull() ?: return null

    return addOrUpdateClip(text)
  }

  suspend fun addOrUpdateClip(
    rawText: String,
    pinned: Boolean? = null,
  ): ClipboardItem? {
    val normalized = rawText.trim()
    if (normalized.isEmpty() || normalized.length > MAX_CLIP_LENGTH) return null

    val (category, smartExtract) = SmartClipboardAnalyzer.analyze(normalized)
    val now = System.currentTimeMillis()
    var savedItem: ClipboardItem? = null

    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty()).toMutableList()
      val existingIndex = current.indexOfFirst { it.text == normalized }

      val updatedItem =
        if (existingIndex >= 0) {
          val existing = current.removeAt(existingIndex)
          existing.copy(
            timestamp = now,
            isPinned = pinned ?: existing.isPinned,
            category = category,
            smartExtract = smartExtract,
          )
        } else {
          ClipboardItem(
            id = UUID.randomUUID().toString(),
            text = normalized,
            timestamp = now,
            isPinned = pinned ?: false,
            category = category,
            smartExtract = smartExtract,
          )
        }

      current.add(0, updatedItem)
      savedItem = updatedItem

      val trimmed = trimHistory(current)
      prefs[Keys.CLIPS_JSON] = encodeList(trimmed)
    }

    return savedItem
  }

  suspend fun togglePin(id: String) {
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty())
      val updated = current.map { item ->
        if (item.id == id) item.copy(isPinned = !item.isPinned) else item
      }
      prefs[Keys.CLIPS_JSON] = encodeList(trimHistory(updated))
    }
  }

  suspend fun deleteClip(id: String) {
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty())
      val updated = current.filterNot { it.id == id }
      prefs[Keys.CLIPS_JSON] = encodeList(updated)
    }
  }

  suspend fun clearUnpinned() {
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty())
      val updated = current.filter { it.isPinned }
      prefs[Keys.CLIPS_JSON] = encodeList(updated)
    }
  }

  private fun trimHistory(items: List<ClipboardItem>): List<ClipboardItem> {
    val pinned = items.filter { it.isPinned }.sortedByDescending { it.timestamp }
    val unpinned = items.filterNot { it.isPinned }.sortedByDescending { it.timestamp }.take(MAX_UNPINNED_ITEMS)
    return pinned + unpinned
  }

  private fun decodeAndSort(rawJson: String): List<ClipboardItem> {
    val list = decodeList(rawJson)
    return list.sortedWith(
      compareByDescending<ClipboardItem> { it.isPinned }
        .thenByDescending { it.timestamp },
    )
  }

  private fun decodeList(rawJson: String): List<ClipboardItem> {
    if (rawJson.isBlank()) return emptyList()
    return runCatching {
      val array = JSONArray(rawJson)
      buildList(array.length()) {
        for (i in 0 until array.length()) {
          val obj = array.optJSONObject(i) ?: continue
          val text = obj.optString("text", "")
          if (text.isEmpty()) continue
          val categoryName = obj.optString("category", ClipCategory.TEXT.name)
          val category = runCatching { ClipCategory.valueOf(categoryName) }.getOrDefault(ClipCategory.TEXT)
          val smart = if (obj.isNull("smartExtract")) null else obj.optString("smartExtract", "").ifBlank { null }
          add(
            ClipboardItem(
              id = obj.optString("id", UUID.randomUUID().toString()),
              text = text,
              timestamp = obj.optLong("timestamp", 0L),
              isPinned = obj.optBoolean("isPinned", false),
              category = category,
              smartExtract = smart,
            ),
          )
        }
      }
    }.getOrDefault(emptyList())
  }

  private fun encodeList(items: List<ClipboardItem>): String {
    val array = JSONArray()
    items.forEach { item ->
      val obj = JSONObject().apply {
        put("id", item.id)
        put("text", item.text)
        put("timestamp", item.timestamp)
        put("isPinned", item.isPinned)
        put("category", item.category.name)
        if (item.smartExtract != null) {
          put("smartExtract", item.smartExtract)
        } else {
          put("smartExtract", JSONObject.NULL)
        }
      }
      array.put(obj)
    }
    return array.toString()
  }

  companion object {
    private const val MAX_UNPINNED_ITEMS = 24
    private const val MAX_CLIP_LENGTH = 2000
  }
}
