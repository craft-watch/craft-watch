import React from "react";
import _ from "lodash";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import InventoryApp from "../components/InventoryApp";

const ThisPage = (): JSX.Element => (
  <Page
    title = "New beers"
    desc = "Daily updates and prices of new beers from across UK brewery online shops"
    longDesc={
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
    breweries = {inventory.breweries}
  >
    <InventoryApp
      inventory={{
        ...inventory,
        items: _.filter(inventory.items, item => item.new),
      }}
    />
  </Page>
);

export default ThisPage;
