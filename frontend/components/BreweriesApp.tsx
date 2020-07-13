import React, {  } from "react";
import _ from "underscore";
import { Inventory, Brewery } from "../utils/model";
import SortableTable, { Column, Section } from "./SortableTable";
import { toSafePathPart } from "../utils/stuff";
import Link from "next/link";

interface Props {
  inventory: Inventory;
}

interface CellProps {
  brewery: Brewery;
}

const BreweriesApp = ({ inventory }: Props): JSX.Element => {
  const counts = _.countBy(inventory.items, item => item.brewery.shortName);

  return (
    <>
      <div className="how-to-use">
        Click on an image to go to the brewery page!
      </div>

      <main>
        <SortableTable sections={partition(inventory.breweries)}>
          <Column
            name="Brewery"
            className="brewery"
            render={(brewery: Brewery) => <BreweryInfo brewery={brewery} />}
          />
          <Column
            name="Location"
            render={(brewery: Brewery) => <LocationInfo brewery={brewery} />}
          />
          <Column
            name="# items"
            render={(brewery: Brewery) => <ItemsInfo count={counts[brewery.shortName]} />}
          />
        </SortableTable>
      </main>
    </>
  );
};

const BreweryInfo = ({ brewery }: CellProps) => (
  <Link href={`/${toSafePathPart(brewery.shortName)}`}>
    <a>{brewery.shortName}</a>
  </Link>
);

const LocationInfo = ({ brewery }: CellProps) => (
  <>
    {brewery.location}
  </>
);

const ItemsInfo = ({ count }: { count: number }) => (
  <>
    {count}
  </>
);

const partition = (breweries: Array<Brewery>): Array<Section<Brewery>> => {
  const partitioned = _.groupBy(breweries, b => b.name[0]);
  return _.sortBy(
    _.map(partitioned, (v, k) => ({ name: k, data: v })),
    s => s.name,
  );
};



export default BreweriesApp;
