import React from "react";
import _ from "underscore";
import _inventory from "./inventory.json";
import { Inventory } from "./model";
import Menu from "./Menu";
import InventoryTable from "./InventoryTable";
import "./index.css";

const items = (_inventory as Inventory).items;
const shuffledItems = _.flatten(_.shuffle(_.groupBy(items, item => item.brewery)));

interface AppState {
  breweryVisibility: { [key: string]: boolean; }; 
}

class App extends React.Component<{}, AppState> {
  constructor(props: {}) {
    super(props);
    this.state = {
      breweryVisibility: _.object(_.uniq(_.map(items, item => [item.brewery, true]), true, p => p[0])),
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
          items={shuffledItems.filter(item => this.state.breweryVisibility[item.brewery])}
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
