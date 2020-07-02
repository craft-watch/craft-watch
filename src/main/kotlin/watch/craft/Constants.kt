package watch.craft

import java.io.File

const val GCS_BUCKET = "backend.craft.watch"
val CACHE_DIR = File("cache")
val INVENTORY_JSON_FILE = File("frontend/data/inventory.json")

// Key order here is respected in frontend
val CATEGORY_KEYWORDS = mapOf(
  "Pale" to listOf(
    "Pale",
    "Pale Ale",
    "Blonde",
    "Blonde Ale"
  ),
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
  "Dark" to listOf(
    "Porter",
    "Stout",
    "Red Ale",
    "Dark"
  ),
  "Bitter" to listOf(
    "Bitter",
    "Copper Ale",
    "Golden Ale"
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
  "Cider" to listOf(
    "Cider"
  )
)
