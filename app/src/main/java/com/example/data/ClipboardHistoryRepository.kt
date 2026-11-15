package com.example.data

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
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
    val LAST_SYSTEM_CLIP_SIG = stringPreferencesKey("last_system_clip_sig")
  }

  val clipsFlow: Flow<List<ClipboardItem>> =
    context.clipboardDataStore.data
      .catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
      }
      .map { prefs ->
        decodeAndSort(prefs[Keys.CLIPS_JSON].orEmpty())
      }

  suspend fun syncFromSystemClipboard(
    fromChangeListener: Boolean = false,
    force: Boolean = false,
  ): ClipboardItem? {
    val (normalized, signature) = readSystemClipboard() ?: return null
    var savedItem: ClipboardItem? = null

    context.clipboardDataStore.edit { prefs ->
      val lastSig = prefs[Keys.LAST_SYSTEM_CLIP_SIG]
      val isDuplicateSystemClip = lastSig == signature
      val allowApi24Recopy = fromChangeListener && signature.startsWith("0|")
      if (isDuplicateSystemClip && !force && !allowApi24Recopy) {
        return@edit
      }

      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty()).toMutableList()
      val updatedItem = upsertClipInList(current, normalized, pinned = null)
      savedItem = updatedItem
      prefs[Keys.CLIPS_JSON] = encodeList(trimHistory(current))
      prefs[Keys.LAST_SYSTEM_CLIP_SIG] = signature
    }

    return savedItem
  }

  suspend fun addOrUpdateClip(
    rawText: String,
    pinned: Boolean? = null,
  ): ClipboardItem? {
    val normalized = rawText.trim()
    if (normalized.isEmpty() || normalized.length > MAX_CLIP_LENGTH) return null

    var savedItem: ClipboardItem? = null
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty()).toMutableList()
      val updatedItem = upsertClipInList(current, normalized, pinned = pinned)
      savedItem = updatedItem
      prefs[Keys.CLIPS_JSON] = encodeList(trimHistory(current))
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
    val systemClip = readSystemClipboard()
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty())
      val deletedItem = current.firstOrNull { it.id == id }
      val updated = current.filterNot { it.id == id }
      prefs[Keys.CLIPS_JSON] = encodeList(updated)
      if (deletedItem != null && systemClip != null && deletedItem.text == systemClip.first) {
        prefs[Keys.LAST_SYSTEM_CLIP_SIG] = systemClip.second
      }
    }
  }

  suspend fun clearUnpinned() {
    val systemClip = readSystemClipboard()
    context.clipboardDataStore.edit { prefs ->
      val current = decodeList(prefs[Keys.CLIPS_JSON].orEmpty())
      val removedUnpinned = current.filterNot { it.isPinned }
      val updated = current.filter { it.isPinned }
      prefs[Keys.CLIPS_JSON] = encodeList(updated)
      if (systemClip != null && removedUnpinned.any { it.text == systemClip.first }) {
        prefs[Keys.LAST_SYSTEM_CLIP_SIG] = systemClip.second
      }
    }
  }

  private fun readSystemClipboard(): Pair<String, String>? {
    val clip =
      runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.primaryClip
      }.getOrNull() ?: return null

    if (clip.itemCount <= 0) return null
    val rawText =
      runCatching {
        clip.getItemAt(0)?.coerceToText(context)?.toString()
      }.getOrNull() ?: return null

    val normalized = rawText.trim()
    if (normalized.isEmpty() || normalized.length > MAX_CLIP_LENGTH) return null

    val timestamp =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clip.description?.timestamp ?: 0L
      } else {
        0L
      }
    return normalized to "$timestamp|$normalized"
  }

  private fun upsertClipInList(
    current: MutableList<ClipboardItem>,
    normalized: String,
    pinned: Boolean?,
  ): ClipboardItem {
    val (category, smartExtract) = SmartClipboardAnalyzer.analyze(normalized)
    val now = System.currentTimeMillis()
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
    return updatedItem
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
