import React from "react";
import _ from "underscore";
import _inventory from "./inventory.json";
import { Inventory, Item } from "./model";
import Menu, { Selections } from "./Menu";
import InventoryTable from "./InventoryTable";

const items = (_inventory as Inventory).items;
const shuffledItems = _.flatten(_.shuffle(_.groupBy(items, item => item.brewery)));

interface AppState {
  brewerySelections: Selections; 
  formatSelections: Selections;
}

class App extends React.Component<{}, AppState> {
  constructor(props: {}) {
    super(props);
    this.state = {
      brewerySelections: _.object(_.uniq(_.map(items, item => [item.brewery, true]), true, p => p[0])),
      formatSelections: {
        "Regular": true,
        "Mixed case": true,
        "Minikeg": true,
      },
    };
  }

  render(): JSX.Element {
    return (
      <div>
        <Menu
          brewerySelections={this.state.brewerySelections}
          onToggleBrewerySelection={(key) => this.handleToggleBrewerySelection(key)}
          onGlobalBrewerySelection={(selected) => this.handleGlobalBrewerySelection(selected)}
          formatSelections={this.state.formatSelections}
          onToggleFormatSelection={(key) => this.handleToggleFormatSelection(key)}
          onGlobalFormatSelection={(selected) => this.handleGlobalFormatSelection(selected)}
        />

        <InventoryTable items={this.filterItems()} />
      </div>
    );
  }

  private filterItems = (): Array<Item> => shuffledItems.filter(item =>
    this.state.brewerySelections[item.brewery] && this.formatSelected(item)
  );

  private formatSelected = (item: Item): boolean =>
    (this.state.formatSelections["Regular"] && !item.keg && !item.mixed) ||
    (this.state.formatSelections["Mixed case"] && item.mixed) ||
    (this.state.formatSelections["Minikeg"] && item.keg);

  private handleToggleBrewerySelection = (key: string): void => {
    this.setState(state => {
      const brewerySelections = { ...state.brewerySelections };
      brewerySelections[key] = !brewerySelections[key];
      return { brewerySelections };
    })
  }

  private handleGlobalBrewerySelection = (selected: boolean): void => {
    this.setState(state => {
      const brewerySelections = { ...state.brewerySelections };
      _.each(brewerySelections, (_, b) => brewerySelections[b] = selected);
      return { brewerySelections };
    });
  }

  private handleToggleFormatSelection = (key: string): void => {
    this.setState(state => {
      const formatSelections = { ...state.formatSelections };
      formatSelections[key] = !formatSelections[key];
      return { formatSelections };
    });
  }

  private handleGlobalFormatSelection = (selected: boolean): void => {
    this.setState(state => {
      const formatSelections = { ...state.formatSelections };
      _.each(formatSelections, (_, b) => formatSelections[b] = selected);
      return { formatSelections };
    });
  }
}

export default App;
