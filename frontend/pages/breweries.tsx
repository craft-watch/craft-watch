import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { inventory } from "../utils/inventory";

// TODO - update title and description
const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - Breweries A-Z"
    description = "Daily updates and prices of new beers from across UK brewery online shops"
  >
    <App
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
      inventory={inventory}
    />
  </Page>
);

export default ThisPage;
