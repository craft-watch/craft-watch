import React from "react";
import _ from "underscore";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import InventoryApp from "../components/InventoryApp";
import { FavouritesProps, withFavourites } from "../utils/favourites";

const ThisPage = (props: FavouritesProps): JSX.Element => {
  return (
    <Page
      title="My favourites"
      desc="Daily updates from my favourite breweries"
      longDesc={
        (
          <>
            <p>
              This is your personalised view of your favourite breweries.  TODO
            </p>
          </>
        )
      }
      breweries={inventory.breweries}
    >
      <InventoryApp
        inventory={{
          ...inventory,
          items: _.filter(inventory.items, item => props.favourites.breweries.includes(item.brewery.shortName)),
        }}
      />
    </Page>
  );
};

export default withFavourites(ThisPage);
