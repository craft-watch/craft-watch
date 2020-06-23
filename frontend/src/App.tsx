import React from "react";
import inventory from "./inventory.json";
import { SortableTable, Column } from "./SortableTable";
import { Inventory, Item } from "./model";
import "./index.css";

const App = () => (
  <div>
    <SortableTable data={(inventory as Inventory).items}>
      <Column name="Brewery" render={renderBrewery} />
      <Column className="thumbnail" render={renderThumbnail} />
      <Column name="Name" className="name" render={renderName} />
      <Column name="ABV" render={renderAbv} />
      <Column name="Size" className="size" render={renderSize} />
      <Column name="Price per item" render={renderPrice} />
    </SortableTable>
  </div>
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
