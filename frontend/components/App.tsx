import React, {  } from "react";
import _ from "underscore";
import { Inventory } from "../utils/model";
import InventoryTableAndMenu from "./InventoryTableAndMenu";
import Sidebar from "./Sidebar";

interface Props {
  title: string;
  desc?: JSX.Element | string;
  inventory: Inventory;
}

const App = (props: Props): JSX.Element => {
  return (
    <div>
      <Sidebar
        title={props.title}
        desc={props.desc}
        allBreweries={props.inventory.breweries}
      />

      <InventoryTableAndMenu inventory={props.inventory} />
    </div>
  );
};

export default App;
