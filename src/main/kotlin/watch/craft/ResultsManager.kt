package watch.craft

class ResultsManager(private val setup: Setup) {
  private val mapper = mapper()

  fun write(inventory: Inventory) {
    setup.structure.results.write("inventory.json", mapper.writeValueAsBytes(inventory))
    INVENTORY_JSON_FILE.outputStream().use { mapper.writeValue(it, inventory) }
    // TODO - log
  }
}
