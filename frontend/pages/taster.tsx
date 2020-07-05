import React, { useEffect, useState } from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { Item } from "../utils/model";
import { items as inventoryItems, capturedAt, categories } from "../utils/inventory";

const TASTER_MENU_SIZE = 20;

const ThisPage = (): JSX.Element => {
  const [items, setItems] = useState<Array<Item>>([]);

  // TODO - is there a better way to avoid this being captured by SSG?
  useEffect(() => {
    const sample = generateFairTasterMenu(inventoryItems);
    setItems(_.sortBy(_.sortBy(sample, item => item.name), item => item.brewery));
  }, []);

  return (
    <Page
      title = "Craft Watch - Taster menu"
      description = "Taster menu of beer prices from across the UK"
    >
      <App
        title="Taster menu"
        desc={
          (
            <>
              <p>
                We&apos;ve put together a randomly curated selection of {TASTER_MENU_SIZE} beers to inspire you.
              </p>
              <p>
                Refresh the page to get another selection!
              </p>
            </>
          )
        }
        capturedAt={capturedAt}
        items={items}
        categories={categories}
      />
    </Page>
  );
};

// Avoid over-representing breweries that have a ton of beers.
const generateFairTasterMenu = (items: Array<Item>): Array<Item> => {
  // Remove inappropriate items for a taster menu
  const relevant = _.filter(items, item => !item.keg && !item.mixed && item.available);

  const byBrewery = _.groupBy(relevant, item => item.brewery);

  // Pick breweries with *almost* uniform distribution.
  // We allow breweries with lots of beers to feature *slightly* more.
  const breweryPicks = _.shuffle(
    _.flatten(
      _.map(byBrewery, (items, brewery) => {
        const count = _.size(items);
        const rep =
          (count >= 10) ? 6 :
          (count >= 5) ? 5 :
          4;
        return _.times(rep, () => brewery);
      })
    )
  );

  // TODO - what if TASTER_MENU_SIZE < num items?
  const picked = new Set<Item>();
  let idx = 0;
  while (picked.size < TASTER_MENU_SIZE) {
    picked.add(_.sample(byBrewery[breweryPicks[idx++]]));
  }
  return Array.from(picked);
};

export default ThisPage;
