import React from "react";
import _ from "lodash";
import { Inventory, BreweryStats } from "../../utils/model";
import SortableTable, { Column, CellProps, Section } from "../SortableTable";
import { BreweryLink } from "../BreweryLink";
import styles from "./styles.module.css";
import classNames from "classnames";


interface Props {
  inventory: Inventory;
}

interface NewAndBaseline {
  now: BreweryStats;
  baseline: BreweryStats;
}

type Extract = (stats: BreweryStats) => number | undefined;

const StatsApp = ({ inventory }: Props): JSX.Element => (
  <>
    <main className={styles.stats}>
      <SortableTable sections={createSections(inventory)}>
        <Column
          name="Brewery"
          className={styles.brewery}
          render={({ datum }: CellProps<NewAndBaseline>) => (
            <BreweryLink id={datum.now.breweryId}>
              {
                _.find(inventory.breweries, b => b.id === datum.now.breweryId)?.shortName
              }
            </BreweryLink>
          )}
        />
        <Column name="Raw" render={field(stats => stats.numRawItems, true)} />
        <Column name="Skipped" render={field(stats => stats.numSkipped)} />
        <Column name="Unretrievable" render={field(stats => stats.numUnretrievable)} />
        <Column name="Malformed" render={field(stats => stats.numMalformed)} />
        <Column name="Invalid" render={field(stats => stats.numInvalid)} />
        <Column name="Errors" render={field(stats => stats.numErrors)} />
        <Column name="Merged" render={field(stats => stats.numMerged, true)} />
      </SortableTable>
    </main>
  </>
);

const createSections = (inventory: Inventory): Array<Section<NewAndBaseline>> => {
  const zipped = _.map(inventory.stats.breweries, s => ({
    now: s,
    baseline: _.find(inventory.incubating.baselineStats.breweries, b => b.breweryId === s.breweryId)
  } as NewAndBaseline));

  return [
    {
      name: "All",
      data: _.sortBy(zipped, z => z.now.breweryId),
    }
  ];
};

const field = (extract: Extract, nonplussed = false) => ({ datum }: CellProps<NewAndBaseline>) => {
  const now = extract(datum.now) ?? 0;
  const baseline = extract(datum.baseline) ?? 0;
  const delta = now - baseline;

  const extraClassName = nonplussed ? undefined :
    (delta > 0) ? styles.worse :
    (delta < 0) ? styles.better :
    undefined;

  return (
    <>
      {(now !== 0) ? now : ""}
      {
        (delta !== 0) && (
          <span className={classNames(styles.delta, extraClassName)}>
            ({(delta < 0 ? "" : "+") + delta})
          </span>
        )
      }
    </>
  );
};

export default StatsApp;
