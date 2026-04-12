package eu.florianbecker.baureihensammler.search

import eu.florianbecker.baureihensammler.data.AlphaTrainSeriesRepository
import eu.florianbecker.baureihensammler.data.TrainSeries
import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin

fun normalizedBaureiheQuery(raw: String): String =
    raw.trim().removePrefix("BR ").removePrefix("br ")

fun catalogForOrigin(origin: TrainSeriesOrigin): List<TrainSeries> =
    AlphaTrainSeriesRepository.items.filter { it.origin == origin }

fun ghostBaureiheSuffix(raw: String, origin: TrainSeriesOrigin): String {
    val normalized = normalizedBaureiheQuery(raw)
    if (normalized.isEmpty()) return ""
    val candidates =
        catalogForOrigin(origin).filter { series ->
            series.baureihe.contains('.') &&
                series.baureihe.startsWith(normalized, ignoreCase = true) &&
                !series.baureihe.equals(normalized, ignoreCase = true)
        }
    if (candidates.size != 1) return ""
    val full = candidates.first().baureihe
    return full.substring(normalized.length)
}

fun findSeries(query: String, origin: TrainSeriesOrigin): TrainSeries? {
    val cleanedQuery = normalizedBaureiheQuery(query)
    return catalogForOrigin(origin).firstOrNull { series ->
        series.baureihe.equals(cleanedQuery, ignoreCase = true)
    }
}

fun calculatePoints(fleetEstimate: Int): Int =
    (1200 / fleetEstimate.coerceAtLeast(20)).coerceAtLeast(1)
