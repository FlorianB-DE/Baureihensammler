package eu.florianbecker.baureihensammler.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.hjson.JsonValue
import java.lang.reflect.Type
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Bahngesellschaft / Datenherkunft der Baureihe.
 * Aktuell nur [DB] in den Stammdaten; weitere Werte für geplante Kataloge (z. B. SBB, ÖBB).
 */
enum class TrainSeriesOrigin {
    /** Deutsche Bahn (inkl. historischer DR-/DB-Baureihen in diesem Katalog). */
    DB,

    /** Schweizerische Bundesbahnen — reserviert für zukünftige Erweiterungen. */
    SBB,

    /** Österreichische Bundesbahnen — reserviert für zukünftige Erweiterungen. */
    OBB,
    ;

    companion object {
        fun fromName(raw: String?): TrainSeriesOrigin =
            entries.find { it.name == raw } ?: DB
    }
}

/** Kurztext im Kennzeichenfeld (links neben „BR“). */
fun TrainSeriesOrigin.plateAbbrev(): String =
    when (this) {
        TrainSeriesOrigin.DB -> "DB"
        TrainSeriesOrigin.SBB -> "SBB"
        TrainSeriesOrigin.OBB -> "ÖBB"
    }

/** Bezeichnung im Auswahlmenü. */
fun TrainSeriesOrigin.menuLabel(): String =
    when (this) {
        TrainSeriesOrigin.DB -> "Deutsche Bahn (DB)"
        TrainSeriesOrigin.SBB -> "Schweizerische Bundesbahnen (SBB)"
        TrainSeriesOrigin.OBB -> "Österreichische Bundesbahnen (ÖBB)"
    }

data class TrainSeries(
    val baureihe: String,
    val name: String,
    val category: String,
    val vmaxKmh: Int,
    val fleetEstimate: Int,
    val wikiArticleTitle: String,
    val origin: TrainSeriesOrigin = TrainSeriesOrigin.DB,
    /** Weitere Eingaben, die dieselbe Baureihe finden (z. B. `0445` → wie `445`). */
    val aliases: List<String> = emptyList(),
    /**
     * Wenn mehrere Züge dieselbe Grund-BR teilen: gemeinsamer Schlüssel (z. B. `445`).
     * Dann sind [overlapVehicleRanges] die möglichen Wagennummern-Bereiche
     * (üblicherweise Ordnungsnummern im Verband).
     */
    val overlapGroupKey: String? = null,
    val overlapVehicleRanges: List<IntRange> = emptyList(),
    /** Erwartete Stellenanzahl der Wagennummer (z. B. 2 für `01`, 3 für `001`). */
    val overlapVehicleDigits: Int? = null,
) {
    val wikiArticleUrl: String
        get() = "https://de.wikipedia.org/wiki/${encodeForWiki(wikiArticleTitle)}"

    val wikiSummaryApiUrl: String
        get() = "https://de.wikipedia.org/api/rest_v1/page/summary/${encodeForWiki(wikiArticleTitle)}"
}

private fun encodeForWiki(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}

/**
 * Custom deserializer for IntRange since Gson doesn't handle it by default.
 * Expects JSON format: [start, end]
 */
class IntRangeDeserializer : JsonDeserializer<IntRange> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): IntRange {
        val array = json.asJsonArray
        val start = array[0].asInt
        val end = array[1].asInt
        return start..end
    }
}

object AlphaTrainSeriesRepository {
    private var _items: List<TrainSeries>? = null
    
    /**
     * Initializes the repository by loading train series data from the JSON file.
     * Must be called before accessing [items].
     */
    fun initialize(context: Context) {
        if (_items == null) {
            _items = loadTrainSeriesFromJson(context)
        }
    }
    
    val items: List<TrainSeries>
        get() = _items ?: throw IllegalStateException(
            "AlphaTrainSeriesRepository not initialized. Call initialize(context) first."
        )
    
    private fun loadTrainSeriesFromJson(context: Context): List<TrainSeries> {
        val gson =
            GsonBuilder()
                .registerTypeAdapter(IntRange::class.java, IntRangeDeserializer())
                .create()

        val hjsonString = context.assets.open("train_series.hjson").bufferedReader().use {
            it.readText()
        }

        // Parse lenient Hjson and convert to strict JSON for Gson mapping.
        val jsonString = JsonValue.readHjson(hjsonString).toString()

        val type = object : TypeToken<List<TrainSeries>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}
