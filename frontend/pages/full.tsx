import React from "react";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import InventoryApp from "../components/InventoryApp";

const ThisPage = (): JSX.Element => (
  <Page
    title="Full menu"
    desc="Daily updates of beer prices from across UK brewery online shops"
    longDesc={
      (
        <>
          <p>
            This is the full selection of beer prices from across UK breweries that sell direct to your doorstep.
          </p>
          <p>
            Updated daily!
          </p>
        </>
      )
    }
    breweries={inventory.breweries}
  >
    <InventoryApp inventory={inventory} />
  </Page>
);

export default ThisPage;
