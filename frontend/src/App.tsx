import React from "react";
import _ from "underscore";
import _inventory from "./inventory.json";
import { Inventory } from "./model";
import Menu from "./Menu";
import InventoryTable from "./InventoryTable";
import "./index.css";

const inventory = _inventory as Inventory;

const extractBreweries = (inventory: Inventory) => _.map(inventory.items, item => item.brewery);

interface AppState {
  breweryVisibility: { [key: string]: boolean; }; 
}

class App extends React.Component<{}, AppState> {
  constructor(props: {}) {
    super(props);
    
    const breweryVisibility: { [key:string]:boolean; } = {};
    _.each(extractBreweries(inventory), b => breweryVisibility[b] = true);

    this.state = {
      breweryVisibility: breweryVisibility,
    };
  }

  render() {
    return (
      <div>
        <Menu
          breweryVisibility={this.state.breweryVisibility}
          onToggleVisibility={(brewery) => this.handleToggleVisibility(brewery)}
          onGlobalVisibility={(visible) => this.handleGlobalVisibility(visible)}
        />

        <InventoryTable
          items={inventory.items.filter(item => this.state.breweryVisibility[item.brewery])}
        />
      </div>
    );
  }

  private handleToggleVisibility(brewery: string) {
    this.setState(state => {
      const breweryVisibility = { ...state.breweryVisibility };
      breweryVisibility[brewery] = !breweryVisibility[brewery];
      return { breweryVisibility };
    });
  }

  private handleGlobalVisibility(visible: boolean) {
    this.setState(state => {
      const breweryVisibility = { ...state.breweryVisibility };
      _.each(breweryVisibility, (_, b) => breweryVisibility[b] = visible);
      return { breweryVisibility };
    });
  }
}

export default App;
