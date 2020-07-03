package watch.craft

import java.io.File

const val GCS_BUCKET = "backend.craft.watch"
val CACHE_DIR = File("cache")
const val INVENTORY_FILENAME = "inventory.json"
val CANONICAL_INVENTORY_PATH = File("frontend/data/${INVENTORY_FILENAME}")

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
  "Weisse" to listOf(
    "Weiss",
    "Weisse",
    "Weizen",
    "Weizenbier",
    "Hefeweizen",
    "Witbier",
    "Weißbier"
  ),
  "Sours / Gose" to listOf(
    "Sour",
    "Gose"
  ),
  "Cider" to listOf(
    "Cider"
  )
)
