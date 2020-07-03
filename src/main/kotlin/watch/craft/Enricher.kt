package watch.craft

interface Enricher {
  fun enrich(item: Item): Item
}
