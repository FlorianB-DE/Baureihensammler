package eu.florianbecker.baureihensammler.collection

data class CollectionEntry(
    val baureihe: String,
    val name: String,
    val category: String,
    val vmaxKmh: Int,
    val fleetEstimate: Int,
    val seenAt: String,
    val totalPoints: Int,
    val imagePath: String?
)
