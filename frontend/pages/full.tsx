import React from "react";
import App from "../components/App";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - Full list of beer prices from UK breweries"
    description = "Daily updates of beer prices from across UK brewery online shops"
  >
    <App
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
      inventory={inventory}
    />
  </Page>
);

export default ThisPage;
