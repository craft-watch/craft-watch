import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { items, capturedAt, categories } from "../utils/inventory";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - New beers"
    description = "Newly released beers from across UK brewery online shops"
  >
    <App
      title="New beers"
      desc={
        (
          <>
            <p>
              These are the latest releases from breweries, along with new additions to Craft Watch itself.
            </p>
            <p>
              Updated daily!
            </p>
          </>
        )
      }
      capturedAt={capturedAt}
      items={_.filter(items, item => item.newFromBrewer || item.newToUs) }
      categories={categories}
    />
  </Page>
);

export default ThisPage;
