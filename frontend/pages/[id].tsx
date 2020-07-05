import React from "react";
import _ from "underscore";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../components/Page";
import App from "../components/App";
import { Item } from "../utils/model";
import { items, capturedAt, categories, breweries } from "../utils/inventory";
import { toSafePathPart } from "../utils/stuff";

interface Props {
  brewery: string;
  items: Array<Item>;
}

const ThisPage = (props: Props): JSX.Element => {
  const brewery = _.find(breweries, brewery => brewery.shortName === props.brewery);
  if (!brewery) {
    throw new Error("Unknown brewery");
  }

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
                <a href={brewery.websiteUrl}>{brewery.websiteUrl}</a>
              </p>
            </>
          )
        }
        capturedAt={capturedAt}
        items={props.items}
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
      brewery: items[0].brewery,  // Hack to get back to human-readable name
      items
    }
  };
};

const safeNamesToItems = _.groupBy(items, item => toSafePathPart(item.brewery));
