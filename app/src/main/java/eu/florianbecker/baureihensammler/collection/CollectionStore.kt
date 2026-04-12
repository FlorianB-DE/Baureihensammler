package eu.florianbecker.baureihensammler.collection

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.format.DateTimeFormatter

const val PREFS_NAME = "baureihen_prefs"
const val KEY_COLLECTION = "collection_entries"
const val KEY_PRIVACY_OFFLINE_MODE = "privacy_offline_mode"

val collectionDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun loadPrivacyOfflineMode(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_PRIVACY_OFFLINE_MODE, false)

fun savePrivacyOfflineMode(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_PRIVACY_OFFLINE_MODE, enabled)
        .apply()
}

fun saveCollection(context: Context, items: List<CollectionEntry>) {
    val array = JSONArray()
    items.forEach { item ->
        val obj =
            JSONObject()
                .put("baureihe", item.baureihe)
                .put("name", item.name)
                .put("category", item.category)
                .put("vmaxKmh", item.vmaxKmh)
                .put("fleetEstimate", item.fleetEstimate)
                .put("seenAt", item.seenAt)
                .put("totalPoints", item.totalPoints)
                .put("imagePath", item.imagePath)
        array.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_COLLECTION, array.toString())
        .apply()
}

fun loadCollection(context: Context): List<CollectionEntry> {
    val raw =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_COLLECTION, null)
            ?: return emptyList()
    val array = JSONArray(raw)
    val items = mutableListOf<CollectionEntry>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        items.add(
            CollectionEntry(
                baureihe = obj.getString("baureihe"),
                name = obj.getString("name"),
                category = obj.getString("category"),
                vmaxKmh = obj.getInt("vmaxKmh"),
                fleetEstimate = obj.getInt("fleetEstimate"),
                seenAt = obj.getString("seenAt"),
                totalPoints = obj.getInt("totalPoints"),
                imagePath = obj.optString("imagePath").ifBlank { null }
            )
        )
    }
    return items
}
