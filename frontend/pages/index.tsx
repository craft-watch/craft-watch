import React from "react";
import App from "../components/App";
import Page from "../components/Page";
import { items, capturedAt, categories } from "../utils/inventory";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Craft Watch - beer prices from UK breweries"
    description = "Daily updates of beer prices from across UK brewery online shops"
  >
    <App capturedAt={capturedAt} items={items} categories={categories} />
  </Page>
);

export default ThisPage;
