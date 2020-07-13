import React, { useState, useEffect } from "react";
import _ from "underscore";
import { Moment } from "moment";
import { Item, Brewery, Format } from "../utils/model";
import Menu, { Selections, Section as MenuSection } from "./Menu";
import InventoryTable from "./InventoryTable";
import Sidebar from "./Sidebar";
import { MIXED_CASE, MINIKEG, REGULAR, OUT_OF_STOCK } from "../utils/strings";
import { headlineOffer } from "../utils/stuff";

interface Props {
  title: string;
  desc?: JSX.Element | string;
  capturedAt: Moment;
  items: Array<Item>;
  allBreweries: Array<Brewery>;
  categories: Array<string>;
}

const App = (props: Props): JSX.Element => {
  const availability = useSelections([OUT_OF_STOCK]);
  const brewery = useSelections(uniqueBreweries(props.items));
  const format = useSelections([REGULAR, MIXED_CASE, MINIKEG]);

  useEffect(() => brewery.setKeys(uniqueBreweries(props.items)), [props.items]);

  const filterItems = (): Array<Item> => props.items.filter(item =>
    brewerySelected(item) && formatSelected(item) && availabilitySelected(item)
  );

  const brewerySelected = (item: Item): boolean => brewery.selections[item.brewery.shortName];

  const formatSelected = (item: Item): boolean => {
    const keg = (headlineOffer(item).format === Format.Keg);
    return (format.selections[REGULAR] && !keg && !item.mixed) ||
      (format.selections[MIXED_CASE] && item.mixed) ||
      (format.selections[MINIKEG] && keg);
  };

  const availabilitySelected = (item: Item): boolean => (availability.selections[OUT_OF_STOCK] || item.available);

  return (
    <div>
      <Sidebar
        title={props.title}
        desc={props.desc}
        allBreweries={props.allBreweries}
      />

      <div className="how-to-use">
        Click on an image to go to the brewery shop!
      </div>

      <Menu capturedAt={props.capturedAt}>
        <MenuSection title="Formats" selections={format} />
        <MenuSection title="Availability" selections={availability} />
        {
          (_.size(brewery.selections) > 1) && (
            <MenuSection title="Breweries" selections={brewery} />
          )
        }
      </Menu>

      <main>
        <InventoryTable items={filterItems()} categories={props.categories} />
      </main>
    </div>
  );
};

const uniqueBreweries = (items: Array<Item>): Array<string> => _.uniq(_.map(items, item => item.brewery.shortName));

const useSelections = (keys: Array<string>): Selections => {
  const toMap = (keys: Array<string>, selected: boolean) => _.object(_.map(keys, b => [b, selected]));

  const [selections, setSelections] = useState<{ [key: string]: boolean }>(toMap(keys, true));

  const toggle = (key: string): void => {
    const copy = { ...selections };
    copy[key] = !copy[key];
    setSelections(copy);
  };

  const setGlobal = (selected: boolean): void => {
    setSelections(toMap(_.keys(selections), selected));
  };

  const setKeys = (keys: Array<string>): void => {
    setSelections(toMap(keys, true));
  };

  return { selections, toggle, setGlobal, setKeys };
};

export default App;
