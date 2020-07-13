import React from "react";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import Sidebar from "../components/Sidebar";
import InventoryTableAndMenu from "../components/InventoryTableAndMenu";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - Full list of beer prices from UK breweries"
    description = "Daily updates of beer prices from across UK brewery online shops"
  >
    <Sidebar
      title="Full menu"
      desc={
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
      allBreweries={inventory.breweries}
    />

    <InventoryTableAndMenu inventory={inventory} />
  </Page>
);

export default ThisPage;
