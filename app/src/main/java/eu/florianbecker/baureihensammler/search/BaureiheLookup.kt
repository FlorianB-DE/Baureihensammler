package eu.florianbecker.baureihensammler.search

import eu.florianbecker.baureihensammler.data.AlphaTrainSeriesRepository
import eu.florianbecker.baureihensammler.data.TrainSeries
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin

fun normalizedBaureiheQuery(raw: String): String =
    raw.trim().removePrefix("BR ").removePrefix("br ").trim()

/**
 * Normalisiert die Nutzereingabe für Baureihe/Alias (z. B. führende Nullen bei rein numerisch: `0445` → `445`).
 */
fun normalizeBrLookup(raw: String): String {
    val trimmed = normalizedBaureiheQuery(raw)
    if (trimmed.matches(Regex("""^0+\d+$"""))) {
        return trimmed.trimStart('0').ifEmpty { "0" }
    }
    return trimmed
}

fun catalogForOrigin(origin: TrainSeriesOrigin): List<TrainSeries> =
    AlphaTrainSeriesRepository.items.filter { it.origin == origin }

private fun seriesMatchesBrToken(series: TrainSeries, normalizedToken: String): Boolean {
    if (normalizeBrLookup(series.baureihe).equals(normalizedToken, ignoreCase = true)) return true
    if (series.aliases.any { normalizeBrLookup(it).equals(normalizedToken, ignoreCase = true) }) {
        return true
    }
    if (series.overlapGroupKey?.equals(normalizedToken, ignoreCase = true) == true) return true
    return false
}

fun brCandidatesForToken(normalizedToken: String, origin: TrainSeriesOrigin): List<TrainSeries> {
    if (normalizedToken.isEmpty()) return emptyList()
    return catalogForOrigin(origin).filter { seriesMatchesBrToken(it, normalizedToken) }
}

fun needsOverlapVehicleField(brQuery: String, origin: TrainSeriesOrigin): Boolean =
    brCandidatesForToken(normalizeBrLookup(brQuery), origin).size > 1

fun ghostBaureiheSuffix(raw: String, origin: TrainSeriesOrigin): String {
    val normalized = normalizeBrLookup(raw)
    if (normalized.isEmpty()) return ""
    val candidates =
        catalogForOrigin(origin).filter { series ->
            series.baureihe.contains('.') &&
                series.baureihe.startsWith(normalized, ignoreCase = true) &&
                !normalizeBrLookup(series.baureihe).equals(normalized, ignoreCase = true)
        }
    if (candidates.size != 1) return ""
    val full = candidates.first().baureihe
    return full.substring(normalized.length)
}

fun findSeries(
    brQuery: String,
    vehicleQuery: String?,
    origin: TrainSeriesOrigin,
): TrainSeries? {
    val brNorm = normalizeBrLookup(brQuery)
    if (brNorm.isEmpty()) return null

    val candidates = brCandidatesForToken(brNorm, origin).distinct()
    if (candidates.isEmpty()) return null

    if (candidates.size == 1) {
        val s = candidates.single()
        val v = vehicleQuery?.trim()?.toIntOrNull()
        if (s.overlapVehicleRanges.isNotEmpty() && v != null && !s.matchesVehicleNumber(v)) {
            return null
        }
        return s
    }

    val vehicleToken = vehicleQuery?.trim() ?: return null
    val v = vehicleToken.toIntOrNull() ?: return null
    val vehicleDigits = vehicleToken.takeIf { it.matches(Regex("""^\d+$""")) }?.length

    val rangeMatches = candidates.filter { series -> series.matchesVehicleNumber(v) }

    if (rangeMatches.size <= 1) return rangeMatches.singleOrNull()

    if (vehicleDigits != null) {
        val digitMatches =
            rangeMatches.filter { series ->
                series.overlapVehicleDigits == vehicleDigits
            }
        if (digitMatches.size == 1) return digitMatches.single()
    }

    return rangeMatches.singleOrNull()
}

private fun TrainSeries.matchesVehicleNumber(vehicleNumber: Int): Boolean =
    overlapVehicleRanges.any { vehicleNumber in it }

fun calculatePoints(fleetEstimate: Int): Int =
    (1200 / fleetEstimate.coerceAtLeast(20)).coerceAtLeast(1)

/** Sammlung kann noch unter [TrainSeries.aliases] gespeicherte Baureihen-Strings haben (z. B. `4462` → 1462). */
fun TrainSeries.matchesStoredBaureihe(storedBaureihe: String): Boolean {
    val n = normalizeBrLookup(storedBaureihe)
    if (n.isEmpty()) return false
    if (normalizeBrLookup(baureihe).equals(n, ignoreCase = true)) return true
    return aliases.any { normalizeBrLookup(it).equals(n, ignoreCase = true) }
}
