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

    // Sorted, but then brewery order is randomised
    setItems(
      _.flatten(
        _.values(
          _.shuffle(
            _.groupBy(
              _.sortBy(sample, item => item.name),
              item => item.brewery
            )
          )
        )
      )
    );
  }, []);

  return (
    <Page
      title = "Craft Watch - taster menu"
      description = "Taster menu of beer prices from across the UK"
    >
      <App capturedAt={capturedAt} items={items} categories={categories} />
    </Page>
  );
};

// Avoid over-representing breweries that have a ton of beers.
const generateFairTasterMenu = (items: Array<Item>): Array<Item> => {
  // Remove inappropriate items for a taster menu
  const relevant = _.filter(items, item => !item.keg && !item.mixed && item.available);

  const byBrewery = _.groupBy(relevant, item => item.brewery);

  // Generate a weighted array of breweries to sample from.
  // We allow breweries with lots of beers to feature *slightly* more.
  const weightedRepeats = _.flatten(
    _.map(byBrewery, (items, brewery) => {
      const count = _.size(items);
      const rep =
        (count >= 10) ? 6 :
        (count >= 5) ? 5 :
        4;
      return _.times(rep, () => brewery);
    })
  );

  // TODO - what if TASTER_MENU_SIZE < num items?
  const picked = new Set<Item>();
  while (picked.size < TASTER_MENU_SIZE) {
    const brewery = _.sample(weightedRepeats);
    const candidate = _.sample(byBrewery[brewery]);
    picked.add(candidate);
  }
  return Array.from(picked);
};

export default ThisPage;
