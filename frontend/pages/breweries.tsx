import React from "react";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import BreweriesApp from "../components/BreweriesApp";
import Link from "next/link";

const ThisPage = (): JSX.Element => (
  <Page
    title = "Breweries A-Z"
    desc = "All the UK breweries covered by Craft Watch"
    longDesc={
      (
        <>
          <p>
            These are all the UK breweries that we monitor for daily price and product updates.
            Every brewery here has an online shop, and delivers directly to your doorstep.
          </p>
          <p>
            Click the hearts to add breweries to your
            personalised <Link href="/favourites"><a>favourites page</a></Link>!
          </p>
        </>
      )
    }
    breweries={inventory.breweries}
  >
    <BreweriesApp inventory={inventory} />
  </Page>
);

export default ThisPage;
