import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import Sidebar from "../components/Sidebar";
import BreweriesApp from "../components/BreweriesApp";

// TODO - update title and description
const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - Breweries A-Z"
    description = "Daily updates and prices of new beers from across UK brewery online shops"
  >
    <Sidebar
      title="Breweries A-Z"
      desc={
        (
          <>
            <p>
              Blah blah blah.
            </p>
          </>
        )
      }
      allBreweries={inventory.breweries}
    />

    <BreweriesApp inventory={inventory} />
  </Page>
);

export default ThisPage;
