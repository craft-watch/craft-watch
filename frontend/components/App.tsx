import React from "react";
import _ from "underscore";
import { Item } from "../utils/model";
import Menu, { Selections } from "./Menu";
import InventoryTable from "./InventoryTable";

interface Props {
  items: Array<Item>;
}

interface State {
  brewerySelections: Selections;
  formatSelections: Selections;
}

class App extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      brewerySelections: _.object(_.uniq(_.map(props.items, item => [item.brewery, true]), true, p => p[0])),
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
        <div className="how-to-use">
          Click on an image to go to the brewery shop!
        </div>

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

  private filterItems = (): Array<Item> => this.props.items.filter(item =>
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
    });
  };

  private handleGlobalBrewerySelection = (selected: boolean): void => {
    this.setState(state => {
      const brewerySelections = { ...state.brewerySelections };
      _.each(brewerySelections, (_, b) => brewerySelections[b] = selected);
      return { brewerySelections };
    });
  };

  private handleToggleFormatSelection = (key: string): void => {
    this.setState(state => {
      const formatSelections = { ...state.formatSelections };
      formatSelections[key] = !formatSelections[key];
      return { formatSelections };
    });
  };

  private handleGlobalFormatSelection = (selected: boolean): void => {
    this.setState(state => {
      const formatSelections = { ...state.formatSelections };
      _.each(formatSelections, (_, b) => formatSelections[b] = selected);
      return { formatSelections };
    });
  };
}

export default App;
