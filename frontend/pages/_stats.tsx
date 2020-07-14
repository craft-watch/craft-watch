import React from "react";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import StatsApp from "../components/StatsApp";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Pipeline stats"
    desc = "Pipeline stats"
    longDesc = ""
    breweries = {inventory.breweries}
  >
    <StatsApp inventory={inventory} />
  </Page>
);

export default ThisPage;
