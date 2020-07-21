import React from "react";
import _ from "lodash";
import Page from "../components/Page";
import { inventory } from "../utils/inventory";
import InventoryApp from "../components/InventoryApp";
import { FavouritesProps, withFavourites } from "../utils/favourites";
import Link from "next/link";

const ThisPage = (props: FavouritesProps): JSX.Element => {
  return (
    <Page
      title="My favourites"
      desc="Daily updates from my favourite breweries"
      longDesc={
        (
          _.isEmpty(props.favourites.breweryIds)
            ? (
              <p>
                It looks like you haven&apos;t set any favourite breweries yet.  Visit
                the <Link href="/breweries"><a>A-Z page</a></Link> to add some.
              </p>
            ) : (
              <p>
                This is a personalised view of your favourite breweries.  Add or remove breweries from
                the <Link href="/breweries"><a>A-Z page</a></Link>.
              </p>
            )
        )
      }
      breweries={inventory.breweries}
    >
      <InventoryApp
        inventory={{
          ...inventory,
          items: _.filter(inventory.items, item => props.favourites.breweryIds.includes(item.brewery.id)),
        }}
      />
    </Page>
  );
};

export default withFavourites(ThisPage);
