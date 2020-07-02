package watch.craft

import java.io.File

const val GCS_BUCKET = "backend.craft.watch"
val CACHE_DIR = File("cache")
val INVENTORY_JSON_FILE = File("frontend/data/inventory.json")

val KEYWORDS = mapOf(
  "IPA" to listOf(
    "IPA",
    "DIPA",
    "DDH",
    "New England IPA",
    "NEIPA",
    "India Pale Ale",
    "XPA",
    "Extra Pale Ale"
  ),
  "Pale" to listOf(
    "Pale",
    "Pale Ale",
    "Blonde",
    "Blonde Ale"
  ),
  "Dark" to listOf(
    "Porter",
    "Stout",
    "Red Ale",
    "Dark"
  ),
  "Pils / Lager" to listOf(
    "Pils",
    "Pilsner",
    "Lager",
    "India Pale Lager"
  ),
  "Sours / Gose" to listOf(
    "Sour",
    "Gose"
  ),
  "Bitter" to listOf(
    "Bitter",
    "Copper Ale",
    "Golden Ale"
  ),
  "Cider" to listOf(
    "Cider"
  )
)
