import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import Sidebar from "../components/Sidebar";
import InventoryTableAndMenu from "../components/InventoryTableAndMenu";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - New beers from UK breweries"
    description = "Daily updates and prices of new beers from across UK brewery online shops"
  >
    <Sidebar
      title="New beers"
      desc={
        (
          <>
            <p>
              These are the latest beer and cider releases from breweries, updated daily!
            </p>
            <p>
              Every item here can be delivered directly to your doorstep from the brewery&apos;s online shop.
            </p>
          </>
        )
      }
      allBreweries={inventory.breweries}
    />

    <InventoryTableAndMenu
      inventory={{
        ...inventory,
        items: _.filter(inventory.items, item => item.new && !item.brewery.new),
      }}
    />
  </Page>
);

export default ThisPage;
