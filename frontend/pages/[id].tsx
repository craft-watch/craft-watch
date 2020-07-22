import React from "react";
import _ from "lodash";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../components/Page";
import { Brewery, Item } from "../utils/model";
import { inventory } from "../utils/inventory";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLink } from "@fortawesome/free-solid-svg-icons";
import { faTwitter } from "@fortawesome/free-brands-svg-icons";
import InventoryApp from "../components/InventoryApp";
import FavouriteIcon from "../components/FavouriteIcon";
import styles from "./[id].module.css";

interface Props {
  brewery: Brewery;
  items: Array<Item>;
}

const ThisPage = ({ brewery, items }: Props): JSX.Element => {
  return (
    <Page
      title={brewery.name}
      titleSuffix={<FavouriteIcon breweryId={brewery.id} />}
      desc={`Daily updates of beer prices from ${brewery.name}`}
      longDesc={
        (
          <>
            <p>
              Daily updates of beer prices from {brewery.name}, a brewery based in {brewery.location}.
            </p>
            <p>
              Every item here can be delivered directly to your doorstep from their online shop.
            </p>
            <p className={styles.contact}>
              <a href={brewery.websiteUrl}><FontAwesomeIcon icon={faLink} /> {brewery.websiteUrl}</a>
            </p>
            {
              (brewery.twitterHandle !== undefined) && (
                <p className={styles.contact}>
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

export default ThisPage;

export const getStaticPaths: GetStaticPaths = async () => ({
  paths: _.map(_.keys(groupedById), id => ({ params: { id } })),
  fallback: false,
});

export const getStaticProps: GetStaticProps<Props> = async ({ params }) => {
  const id = params?.id as string;
  const items = groupedById[id];
  return {
    props: {
      brewery: items[0].brewery,
      items
    }
  };
};

const groupedById = _.groupBy(inventory.items, item => item.brewery.id);
