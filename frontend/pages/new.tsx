import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { items, capturedAt, categories, breweries } from "../utils/inventory";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - New beers from UK breweries"
    description = "Daily updates and prices of new beers from across UK brewery online shops"
  >
    <App
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
      capturedAt={capturedAt}
      items={_.filter(items, item => item.new && !item.brewery.new)}
      allBreweries={breweries}
      categories={categories}
    />
  </Page>
);

export default ThisPage;
