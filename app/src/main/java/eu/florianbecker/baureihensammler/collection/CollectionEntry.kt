package eu.florianbecker.baureihensammler.collection

import eu.florianbecker.baureihensammler.data.TrainSeriesOrigin

data class CollectionEntry(
    val baureihe: String,
    val name: String,
    val category: String,
    val vmaxKmh: Int,
    val fleetEstimate: Int,
    val seenAt: String,
    val totalPoints: Int,
    val imagePath: String?,
    val origin: TrainSeriesOrigin = TrainSeriesOrigin.DB,
)
