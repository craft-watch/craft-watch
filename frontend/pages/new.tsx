import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { items, capturedAt, categories } from "../utils/inventory";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - new beers"
    description = "Newly released beers from across UK brewery online shops"
  >
    <App
      capturedAt={capturedAt}
      items={_.filter(items, item => item.newFromBrewer || item.newToUs) }
      categories={categories}
    />
  </Page>
);

export default ThisPage;
