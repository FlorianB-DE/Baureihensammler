package eu.florianbecker.baureihensammler.util

import eu.florianbecker.baureihensammler.collection.CollectionEntry
import java.io.File

fun deleteSnapshotFile(path: String?) {
    if (path.isNullOrBlank()) return
    val file = File(path)
    if (file.exists()) {
        file.delete()
    }
}

fun clearAllSnapshots(collection: List<CollectionEntry>) {
    collection.forEach { entry -> deleteSnapshotFile(entry.imagePath) }
}
