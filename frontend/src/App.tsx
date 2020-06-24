import React from "react";
import _inventory from "./inventory.json";
import { SortableTable, Column } from "./SortableTable";
import { Inventory, Item } from "./model";
import "./index.css";

const inventory = _inventory as Inventory;

interface AppState {
  breweryVisibility: { [key: string]: boolean; }; 
}

class App extends React.Component<{}, AppState> {
  constructor(props: {}) {
    super(props);
    
    const breweryVisibility: { [key:string]:boolean; } = {};
    new Set(inventory.items.map(item => item.brewery))
      .forEach(b => breweryVisibility[b] = true);

    this.state = {
      breweryVisibility: breweryVisibility,
    };
  }

  render() {
    return (
      <div style={{ display: "flex" }}>
        <div id="left-gutter">
          <Settings
            breweryVisibility={this.state.breweryVisibility}
            onChange={(brewery) => this.handleVisibilityChange(brewery)}
          />
        </div>

        <InventoryTable
          items={inventory.items.filter(item => this.state.breweryVisibility[item.brewery])}
        />

        <div id="right-gutter">
        </div>
      </div>
    );
  }

  handleVisibilityChange(brewery: string) {
    this.setState(state => {
      const breweryVisibility = { ...state.breweryVisibility };
      breweryVisibility[brewery] = !breweryVisibility[brewery];
      return { breweryVisibility };
    });
  }
}

interface SettingsTableProps {
  breweryVisibility: { [key: string]: boolean; };
  onChange: (brewery: string) => void;
}

const Settings = (props: SettingsTableProps) => {
  return (
    <div className="settings">
      <h4>Select breweries</h4>
      {
        Object.entries(props.breweryVisibility).map(([brewery, visible]) => (
          <label key={brewery} className="selectable">
            {brewery}
            <input
              type="checkbox"
              checked={visible}
              onChange={() => props.onChange(brewery)}
            />
            <span className="checkmark"></span>
          </label>
        ))
      } 
    </div>
  );
};


interface InventoryTableProps {
  items: Array<Item>;
}

const InventoryTable = (props: InventoryTableProps) => (
  <SortableTable data={props.items}>
    <Column
      name="Brewery"
      render={renderBrewery}
      selector={(item) => item.brewery}
    />
    <Column
      className="thumbnail"
      render={renderThumbnail}
    />
    <Column
      name="Name"
      className="name"
      render={renderName}
      selector={(item) => item.name}
    />
    <Column
      name="ABV"
      render={renderAbv}
      selector={(item) => item.abv}
    />
    <Column
      name="Size"
      className="size"
      render={renderSize}
      selector={(item) => item.sizeMl}
    />
    <Column
      name="Price per item"
      render={renderPrice}
      selector={(item) => item.perItemPrice}
    />
  </SortableTable>
);

const renderBrewery = (item: Item) => item.brewery;

const renderThumbnail = (item: Item) => (
  <a href={item.url}>
    <img alt="" src={item.thumbnailUrl} width="100px" height="100px" />
    {item.available || <div className="sold-out">Sold out</div>}
  </a>
);

const renderName = (item: Item) => (
  <>
    <a href={item.url}>{item.name}</a>
    {item.summary && <p className="summary">{item.summary}</p>}
  </>
);

const renderAbv = (item: Item) => item.abv ? `${item.abv.toFixed(1)}%` : "?";

const renderSize = (item: Item) => !item.sizeMl ? "?"
  : (item.sizeMl < 1000) ? `${item.sizeMl} ml`
  : `${item.sizeMl / 1000} litres`;

const renderPrice = (item: Item) => `£${item.perItemPrice.toFixed(2)}`;

export default App;
