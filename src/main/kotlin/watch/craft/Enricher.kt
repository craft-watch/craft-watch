package watch.craft

interface Enricher {
  fun enrich(item: Item): Item = item
  fun enrich(brewery: Brewery): Brewery = brewery
}
