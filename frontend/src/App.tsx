import React from "react";
import inventory from "./inventory.json";
import { SortableTable, Column } from "./SortableTable";
import { Inventory, Item } from "./model";
import "./index.css";

const App = () => (
  <div style={{ display: "flex" }}>
    <div id="left-gutter">
      <Settings inventory={inventory as Inventory} />
    </div>

    <InventoryTable inventory={inventory as Inventory} />

    <div id="right-gutter" style={{ margin: "20px", border: "1px" }}>
    </div>
  </div>
);

interface SettingsTableProps {
  inventory: Inventory;
}


const Settings = (props: SettingsTableProps) => {
  const breweries = Array.from(new Set(props.inventory.items.map(item => item.brewery)));

  return (
    <div className="settings">
      <h4>Filter breweries</h4>
      {
        breweries.map((brewery, idx) => (
          <label key={idx} className="selectable">
            {brewery}
            <input type="checkbox" checked={true} />
            <span className="checkmark"></span>
          </label>
        ))
      } 
    </div>
  );
};


interface InventoryTableProps {
  inventory: Inventory;
}

const InventoryTable = (props: InventoryTableProps) => (
  <SortableTable data={inventory.items}>
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
