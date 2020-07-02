import React from "react";
import _ from "underscore";
import { Item } from "../utils/model";
import Menu, { Selections, Section as MenuSection } from "./Menu";
import InventoryTable from "./InventoryTable";
import { MIXED_CASE, MINIKEG, REGULAR, OUT_OF_STOCK } from "../utils/strings";
import { Moment } from "moment";

interface Props {
  capturedAt: Moment;
  items: Array<Item>;
}

interface State {
  availabilitySelections: Selections;
  brewerySelections: Selections;
  formatSelections: Selections;
}

class App extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      availabilitySelections: this.initialSelections([OUT_OF_STOCK]),
      brewerySelections: this.initialSelections(this.uniqueBreweries(props.items)),
      formatSelections: this.initialSelections([REGULAR, MIXED_CASE, MINIKEG]),
    };
  }

  componentDidUpdate(prevProps: Props): void {
    const breweries = this.uniqueBreweries(this.props.items);
    const prevBreweries = this.uniqueBreweries(prevProps.items);
    if (!_(breweries).isEqual(prevBreweries)) {
      // TODO - retain selection for retained item
      this.setState({ brewerySelections: this.initialSelections(breweries) });
    }
  }

  render(): JSX.Element {
    return (
      <div>
        <div className="how-to-use">
          Click on an image to go to the brewery shop!
        </div>

        <Menu capturedAt={this.props.capturedAt}>
          <MenuSection
            title="Formats"
            selections={this.state.formatSelections}
            onToggleSelection={(key) => this.handleToggleFormatSelection(key)}
            onGlobalSelection={(selected) => this.handleGlobalFormatSelection(selected)}
          />
          <MenuSection
            title="Availability"
            selections={this.state.availabilitySelections}
            onToggleSelection={(key) => this.handleToggleAvailabilitySelection(key)}
            onGlobalSelection={(selected) => this.handleGlobalAvailabilitySelection(selected)}
          />
          {
            (_.size(this.state.brewerySelections) > 1) && (
              <MenuSection
                title="Breweries"
                selections={this.state.brewerySelections}
                onToggleSelection={(key) => this.handleToggleBrewerySelection(key)}
                onGlobalSelection={(selected) => this.handleGlobalBrewerySelection(selected)}
              />
            )
          }
        </Menu>

        <InventoryTable items={this.filterItems()} />
      </div>
    );
  }

  private filterItems = (): Array<Item> => this.props.items.filter(item =>
    this.brewerySelected(item) && this.formatSelected(item) && this.availabilitySelected(item)
  );

  private brewerySelected = (item: Item): boolean =>
    this.state.brewerySelections[item.brewery];

  private formatSelected = (item: Item): boolean =>
    (this.state.formatSelections[REGULAR] && !item.keg && !item.mixed) ||
    (this.state.formatSelections[MIXED_CASE] && item.mixed) ||
    (this.state.formatSelections[MINIKEG] && item.keg);

  private availabilitySelected = (item: Item): boolean =>
    (this.state.availabilitySelections[OUT_OF_STOCK] || item.available);

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

  private handleToggleAvailabilitySelection = (key: string): void => {
    this.setState(state => {
      const availabilitySelections = { ...state.availabilitySelections };
      availabilitySelections[key] = !availabilitySelections[key];
      return { availabilitySelections };
    });
  };

  private handleGlobalAvailabilitySelection = (selected: boolean): void => {
    this.setState(state => {
      const availabilitySelections = { ...state.formatSelections };
      _.each(availabilitySelections, (_, b) => availabilitySelections[b] = selected);
      return { availabilitySelections };
    });
  };

  private initialSelections = (keys: Array<string>): Selections => _.object(_.map(keys, b => [b, true]));

  private uniqueBreweries = (items: Array<Item>): Array<string> => _.uniq(_.map(items, item => item.brewery));
}

export default App;
