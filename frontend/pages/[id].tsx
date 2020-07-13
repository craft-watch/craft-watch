import React from "react";
import _ from "underscore";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../components/Page";
import App from "../components/App";
import { Brewery, Item } from "../utils/model";
import { items, capturedAt, categories, breweries } from "../utils/inventory";
import { toSafePathPart } from "../utils/stuff";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGlobeEurope } from "@fortawesome/free-solid-svg-icons";
import { faTwitter } from "@fortawesome/free-brands-svg-icons";


interface Props {
  brewery: Brewery;
  items: Array<Item>;
}

const ThisPage = ({ brewery, items }: Props): JSX.Element => {
  return (
    <Page
      title = {`Craft Watch - ${brewery.name}`}
      description = {`Daily updates of beer prices from ${brewery.name}`}
    >
      <App
        title={brewery.name}
        desc={
          (
            <>
              <p>
                Daily updates of beer prices from {brewery.name}, a brewery based in {brewery.location}.
              </p>
              <p>
                Every item here can be delivered directly to your doorstep from their online shop.
              </p>
              <p>
                <a href={brewery.websiteUrl}><FontAwesomeIcon icon={faGlobeEurope} /> {brewery.websiteUrl}</a>
              </p>
              {
                (brewery.twitterHandle !== undefined) && (
                  <p>
                    <a href={`https://twitter.com/${brewery.twitterHandle}`}>
                      <FontAwesomeIcon icon={faTwitter} /> @{brewery.twitterHandle}
                    </a>
                  </p>
                )
              }

            </>
          )
        }
        capturedAt={capturedAt}
        items={items}
        allBreweries={breweries}
        categories={categories}
      />
    </Page>
  );
};

export default ThisPage;

export const getStaticPaths: GetStaticPaths = async () => ({
  paths: _.map(_.keys(safeNamesToItems), name => ({ params: { id: name } })),
  fallback: false,
});

export const getStaticProps: GetStaticProps<Props> = async ({ params }) => {
  const brewery = params?.id as string;
  const items = safeNamesToItems[brewery];
  return {
    props: {
      brewery: items[0].brewery,
      items
    }
  };
};

const safeNamesToItems = _.groupBy(items, item => toSafePathPart(item.brewery.shortName));
