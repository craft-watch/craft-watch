import React from "react";
import _ from "lodash";
import { Inventory, Brewery } from "../utils/model";
import SortableTable, { Column, Section, CellProps } from "./SortableTable";
import FavouriteIcon from "./FavouriteIcon";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLink } from "@fortawesome/free-solid-svg-icons";
import { faTwitter } from "@fortawesome/free-brands-svg-icons";
import { BreweryLink } from "./BreweryLink";

interface Props {
  inventory: Inventory;
}

const BreweriesApp = ({ inventory }: Props): JSX.Element => {
  const counts = _.countBy(inventory.items, item => item.brewery.id);

  const BreweryInfo = ({ datum }: CellProps<Brewery>) => (
    <>
      <BreweryLink id={datum.id}>
        {datum.shortName}
      </BreweryLink>
    </>
  );

  const LocationInfo = ({ datum }: CellProps<Brewery>) => (
    <>
      {datum.location}
    </>
  );

  const ItemsInfo = ({ datum }: CellProps<Brewery>) => (
    <>
      {counts[datum.shortName]}
    </>
  );

  const WebLink = ({ datum }: CellProps<Brewery>) => (
    <>
      <a href={datum.websiteUrl}><FontAwesomeIcon icon={faLink} /></a>
    </>
  );

  const TwitterLink = ({ datum }: CellProps<Brewery>) => (
    <>
      {
        (datum.twitterHandle !== undefined) && (
          <a href={`https://twitter.com/${datum.twitterHandle}`}><FontAwesomeIcon icon={faTwitter} /></a>
        )
      }
    </>
  );

  const FvouriteInfo = ({ datum }: CellProps<Brewery>) => (
    <>
      <FavouriteIcon breweryId={datum.id} />
    </>
  );

  return (
    <>
      <div className="how-to-use">
        Click on an image to go to the brewery page!
      </div>

      <main>
        <SortableTable sections={partition(inventory.breweries)}>
          <Column render={FvouriteInfo} />
          <Column render={BreweryInfo} name="Brewery" />
          <Column render={LocationInfo} name="Location" className="brewery-info" />
          <Column render={WebLink} className="brewery-info" />
          <Column render={TwitterLink} className="brewery-info" />
          <Column render={ItemsInfo} name="Items" className="brewery-info" />
        </SortableTable>
      </main>
    </>
  );
};

const partition = (breweries: Array<Brewery>): Array<Section<Brewery>> => {
  const partitioned = _.groupBy(breweries, b => b.id[0]);
  return _.sortBy(
    _.map(partitioned, (v, k) => ({ name: k, data: v })),
    s => s.name,
  );
};



export default BreweriesApp;
