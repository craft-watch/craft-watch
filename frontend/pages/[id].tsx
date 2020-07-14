import React, { useState, useEffect } from "react";
import _ from "underscore";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../components/Page";
import { Brewery, Item, Favourites } from "../utils/model";
import { inventory } from "../utils/inventory";
import { toSafePathPart } from "../utils/stuff";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGlobeEurope } from "@fortawesome/free-solid-svg-icons";
import { faTwitter } from "@fortawesome/free-brands-svg-icons";
import InventoryApp from "../components/InventoryApp";
import FavouriteStar from "../components/FavouriteStar";

interface Props {
  brewery: Brewery;
  items: Array<Item>;
}

const ThisPage = ({ brewery, items }: Props): JSX.Element => {
  const [favourites, toggleFavourite] = useFavourites();

  return (
    <Page
      title={brewery.name}
      desc={`Daily updates of beer prices from ${brewery.name}`}
      longDesc={
        (
          <>
            <p>
              <FavouriteStar
                breweryShortName={brewery.shortName}
                favourites={favourites}
                onToggle={toggleFavourite}
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

export default ThisPage;

const useFavourites = (): [Favourites, (shortName: string) => void] => {
  const key = "favourites";
  const [favourites, setFavourites] = useState<Favourites>({ breweries: [] });

  const readFromLocalStorage = () => {
    // TODO - error-handling
    const raw = window.localStorage.getItem(key);
    if (raw !== null) {
      setFavourites(JSON.parse(raw));
    }
  };

  useEffect(() => {
    window.addEventListener("storage", readFromLocalStorage);
    readFromLocalStorage();  // Acquire initial state
  }, []);

  const toggle = (shortName: string) => {
    const breweries = new Set<string>(favourites.breweries);
    if (breweries.has(shortName)) {
      breweries.delete(shortName);
    } else {
      breweries.add(shortName);
    }
    const next = { breweries: _.sortBy(Array.from(breweries), s => s) } as Favourites;

    window.localStorage.setItem(key, JSON.stringify(next));
    readFromLocalStorage();   // Don't set state directly, to avoid race with external modification to local storage
  };

  return [favourites, toggle];
}


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
