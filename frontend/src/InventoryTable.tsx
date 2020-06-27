import SortableTable, { Column, Renderer } from "./SortableTable";
import React from "react";
import { Item } from "./model";

export interface InventoryTableProps {
  items: Array<Item>;
}

const InventoryTable: React.FC<InventoryTableProps> = (props) => (
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

const renderBrewery: Renderer<Item> = item => item.brewery;

const renderThumbnail: Renderer<Item> = item => (
  <a href={item.url}>
    <img alt="" src={item.thumbnailUrl} width="100px" height="100px" />
    {item.available || <div className="sold-out">Sold out</div>}
  </a>
);

const renderName: Renderer<Item> = item => (
  <>
    <a href={item.url}>{item.name}</a>
    <p className="summary">
      {item.summary && item.summary}
    </p>
    <p className="summary">
      {item.keg && <span className="pill violet">Minikeg</span>}
      {item.mixed && <span className="pill magenta">Mixed case</span>}
    </p>
  </>
);

const renderAbv: Renderer<Item> = item => item.abv ? `${item.abv.toFixed(1)}%` : "?";

const renderSize: Renderer<Item> = item => !item.sizeMl ? "?"
  : (item.sizeMl < 1000) ? `${item.sizeMl} ml`
  : `${item.sizeMl / 1000} litres`;

const renderPrice: Renderer<Item> = item => `£${item.perItemPrice.toFixed(2)}`;

export default InventoryTable;