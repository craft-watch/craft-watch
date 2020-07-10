package watch.craft

import watch.craft.enrichers.Categoriser.Component
import watch.craft.enrichers.Categoriser.Component.NAME
import watch.craft.enrichers.Categoriser.Component.SUMMARY
import watch.craft.enrichers.Categoriser.Synonym
import java.io.File

const val GCS_BUCKET = "backend.craft.watch"
val LOCAL_STORAGE_DIR = File("storage")
const val INVENTORY_FILENAME = "inventory.json"
val CANONICAL_INVENTORY_PATH = File("frontend/data/${INVENTORY_FILENAME}")

const val DEFAULT_SIZE_ML = 330

// Key order here is respected in frontend
val CATEGORY_KEYWORDS = mapOf(
  "Pale" to listOf(
    "Pale".anywhere(),
    "Pale Ale".anywhere(),
    "Blonde".anywhere(),
    "Blonde Ale".anywhere(),
    "Saison".anywhere(),
    "Amber Ale".anywhere()
  ),
  "IPA" to listOf(
    "IPA".anywhere(),
    "DIPA".anywhere(),
    "DDH".anywhere(),
    "New England IPA".anywhere(),
    "NEIPA".anywhere(),
    "India Pale Ale".anywhere(),
    "XPA".anywhere(),
    "Extra Pale Ale".anywhere()
  ),
  "Dark" to listOf(
    "Porter".anywhere(),
    "Stout".anywhere(),
    "Red Ale".anywhere(),
    "Dark".anywhere()
  ),
  "Bitter" to listOf(
    "Bitter".anywhere(),
    "Copper Ale".anywhere(),
    "Golden Ale".anywhere()
  ),
  "Pils / Lager" to listOf(
    "Pils".anywhere(),
    "Pilsner".anywhere(),
    "Lager".anywhere(),
    "India Pale Lager".anywhere()
  ),
  "Weisse" to listOf(
    "Wheat".only(NAME, SUMMARY),  // This is too general a term to match against the description
    "Weiss".anywhere(),
    "Weisse".anywhere(),
    "Weizen".anywhere(),
    "Weizenbier".anywhere(),
    "Hefeweizen".anywhere(),
    "Witbier".anywhere(),
    "Weißbier".anywhere()
  ),
  "Sours / Gose" to listOf(
    "Sour".anywhere(),
    "Gose".anywhere()
  ),
  "Cider" to listOf(
    "Cider".anywhere()
  )
)

private fun String.anywhere() = Synonym(this)
private fun String.only(vararg components: Component) = Synonym(this, components.toSet())
