import React, {  } from "react";
import _ from "underscore";
import { Moment } from "moment";
import { Item, Brewery } from "../utils/model";
import InventoryTableAndMenu from "./InventoryTableAndMenu";
import Sidebar from "./Sidebar";

interface Props {
  title: string;
  desc?: JSX.Element | string;
  capturedAt: Moment;
  items: Array<Item>;
  allBreweries: Array<Brewery>;
  categories: Array<string>;
}

const App = (props: Props): JSX.Element => {
  return (
    <div>
      <Sidebar
        title={props.title}
        desc={props.desc}
        allBreweries={props.allBreweries}
      />

      <InventoryTableAndMenu
        capturedAt={props.capturedAt}
        items={props.items}
        categories={props.categories}
      />
    </div>
  );
};

export default App;
