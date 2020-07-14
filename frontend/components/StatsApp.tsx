import React from "react";
import _ from "underscore";
import { Inventory, BreweryStats } from "../utils/model";
import SortableTable, { Column, Section } from "./SortableTable";
import Link from "next/link";
import { toSafePathPart } from "../utils/stuff";

interface Props {
  inventory: Inventory;
}

const StatsApp = ({ inventory }: Props): JSX.Element => {
  return (
    <>
      <main className="stats">
        <SortableTable sections={partition(inventory.stats.breweries)}>
          <Column
            name="Brewery"
            className="brewery"
            render={(brewery: BreweryStats) => (
              <Link href={`/${toSafePathPart(brewery.name)}`}>
                <a>{brewery.name}</a>
              </Link>
            )}
          />
          <Column
            name="Raw"
            render={(brewery: BreweryStats) => asString(brewery.numRawItems)}
          />
          <Column
            name="Skipped"
            render={(brewery: BreweryStats) => asString(brewery.numSkipped)}
          />
          <Column
            name="Malformed"
            render={(brewery: BreweryStats) => asString(brewery.numMalformed)}
          />
          <Column
            name="Invalid"
            render={(brewery: BreweryStats) => asString(brewery.numInvalid)}
          />
          <Column
            name="Errors"
            render={(brewery: BreweryStats) => asString(brewery.numErrors)}
          />
          <Column
            name="Merged"
            render={(brewery: BreweryStats) => asString(brewery.numMerged)}
          />
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
