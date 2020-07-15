import React from "react";
import _ from "underscore";
import { Inventory, BreweryStats } from "../utils/model";
import SortableTable, { Column, Section, CellProps } from "./SortableTable";
import Link from "next/link";
import { toSafePathPart } from "../utils/stuff";

interface Props {
  inventory: Inventory;
}

const StatsApp = ({ inventory }: Props): JSX.Element => {
  const render = (extract: (stats: BreweryStats) => number) => ({ datum }: CellProps<BreweryStats>) => (
    <>{asString(extract(datum))}</>
  );

  return (
    <>
      <main className="stats">
        <SortableTable sections={partition(inventory.stats.breweries)}>
          <Column
            name="Brewery"
            className="brewery"
            render={({ datum }: CellProps<BreweryStats>) => (
              <Link href={`/${toSafePathPart(datum.name)}`}>
                <a>{datum.name}</a>
              </Link>
            )}
          />
          <Column name="Raw" render={render(stats => stats.numRawItems)} />
          <Column name="Skipped" render={render(stats => stats.numSkipped)} />
          <Column name="Malformed" render={render(stats => stats.numMalformed)} />
          <Column name="Invalid" render={render(stats => stats.numInvalid)} />
          <Column name="Errors" render={render(stats => stats.numErrors)} />
          <Column name="Merged" render={render(stats => stats.numMerged)} />
        </SortableTable>
      </main>
    </>
  );
};

const asString = (n: number): string => (n > 0) ? n.toString() : "";

const partition = (breweries: Array<BreweryStats>): Array<Section<BreweryStats>> => [
  {
    name: "All",
    data: _.sortBy(breweries, b => b.name),
  }
];

export default StatsApp;
