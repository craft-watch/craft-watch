import React, { useState } from "react";
import _ from "underscore";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../components/Page";
import { Brewery, Item } from "../utils/model";
import { inventory } from "../utils/inventory";
import { toSafePathPart } from "../utils/stuff";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGlobeEurope, faStar as enabledStar } from "@fortawesome/free-solid-svg-icons";
import { faStar as disabledStar } from "@fortawesome/free-regular-svg-icons";
import { faTwitter } from "@fortawesome/free-brands-svg-icons";
import InventoryApp from "../components/InventoryApp";

interface Props {
  brewery: Brewery;
  items: Array<Item>;
}

interface ClickyStarProps {
  enabled: boolean;
  onClick: () => void;
}


const ThisPage = ({ brewery, items }: Props): JSX.Element => {
  const [enabled, setEnabled] = useState<boolean>(false);

  return (
    <Page
      title={brewery.name}
      desc={`Daily updates of beer prices from ${brewery.name}`}
      longDesc={
        (
          <>
            <p>
              <ClickyStar
                enabled={enabled}
                onClick={() => setEnabled(!enabled)}
              />
              Add to favourites.
            </p>
            <p>
              Daily updates of beer prices from {brewery.name}, a brewery based in {brewery.location}.
            </p>
            <p>
              Every item here can be delivered directly to your doorstep from their online shop.
            </p>
            <p className="contact">
              <a href={brewery.websiteUrl}><FontAwesomeIcon icon={faGlobeEurope} /> {brewery.websiteUrl}</a>
            </p>
            {
              (brewery.twitterHandle !== undefined) && (
                <p className="contact">
                  <a href={`https://twitter.com/${brewery.twitterHandle}`}>
                    <FontAwesomeIcon icon={faTwitter} /> @{brewery.twitterHandle}
                  </a>
                </p>
              )
            }
          </>
        )
      }
      breweries={inventory.breweries}
    >
      <InventoryApp inventory={{ ...inventory, items }} />
    </Page>
  );
};

const ClickyStar = (props: ClickyStarProps) => (
  <span
    className="clicky-star"
    onClick={props.onClick}
    title={props.enabled ? "Click to remove from favourites" : "Click to add to favourites"}>
    <FontAwesomeIcon icon={props.enabled ? enabledStar : disabledStar} />
  </span>
);


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

const safeNamesToItems = _.groupBy(inventory.items, item => toSafePathPart(item.brewery.shortName));
