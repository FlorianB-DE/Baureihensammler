package eu.florianbecker.baureihensammler.search

import eu.florianbecker.baureihensammler.data.AlphaTrainSeriesRepository
import eu.florianbecker.baureihensammler.data.TrainSeries

fun normalizedBaureiheQuery(raw: String): String =
    raw.trim().removePrefix("BR ").removePrefix("br ")

fun ghostBaureiheSuffix(raw: String): String {
    val normalized = normalizedBaureiheQuery(raw)
    if (normalized.isEmpty()) return ""
    val candidates =
        AlphaTrainSeriesRepository.items.filter { series ->
            series.baureihe.contains('.') &&
                series.baureihe.startsWith(normalized, ignoreCase = true) &&
                !series.baureihe.equals(normalized, ignoreCase = true)
        }
    if (candidates.size != 1) return ""
    val full = candidates.first().baureihe
    return full.substring(normalized.length)
}

fun findSeries(query: String): TrainSeries? {
    val cleanedQuery = normalizedBaureiheQuery(query)
    return AlphaTrainSeriesRepository.items.firstOrNull { series ->
        series.baureihe.equals(cleanedQuery, ignoreCase = true)
    }
}

fun calculatePoints(fleetEstimate: Int): Int =
    (1200 / fleetEstimate.coerceAtLeast(20)).coerceAtLeast(1)
