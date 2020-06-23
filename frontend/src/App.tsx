import React, { ReactElement } from "react";
import "./index.css";
import inventory from "./inventory.json";

interface Inventory {
  items: Array<Item>;
}

interface Item {
  brewery: string;
  name: string;
  summary?: string;
  sizeMl?: number;
  abv?: number;
  perItemPrice: number;
  available: boolean;
  thumbnailUrl: string;
  url: string;
}

const App = () => (
  <div>
    {renderTable(COLUMNS, (inventory as Inventory).items)}
  </div>
);

const COLUMNS = [
  {
    name: "Brewery",
    render: (item) => item.brewery,
  },
  {
    className: "thumbnail",
    render: (item) => (
      <a href={item.url}>
        <img alt="" src={item.thumbnailUrl} width="100px" height="100px" />
        {item.available || <div className="sold-out">Sold out</div>}
      </a>
    ),
  },
  {
    name: "Name",
    className: "name",
    render: (item) => (
      <>
        <a href={item.url}>{item.name}</a>
        {item.summary && <p className="summary">{item.summary}</p>}
      </>
    ),
  },
  {
    name: "ABV",
    render: (item) => item.abv ? `${item.abv.toFixed(1)}%` : "?",
  },
  {
    name: "Size",
    className: "size",
    render: (item) => !item.sizeMl ? "?"
      : (item.sizeMl < 1000) ? `${item.sizeMl} ml`
      : `${item.sizeMl / 1000} litres`,
  },
  {
    name: "Price per item",
    render: (item) => `£${item.perItemPrice.toFixed(2)}`,
  },
] as Array<Column<Item>>;

interface Column<T> {
  name?: string;
  className?: string;
  render: (item: T) => ReactElement | string | null;
}

// TODO - switch to component definition
const renderTable = <T extends {}>(columns: Array<Column<T>>, items: Array<T>) => (
  <table>
    <thead>
      <tr>
        {columns.map((col, idx) => <th key={idx} className={col.className}>{col.name}</th>)}
      </tr>
    </thead>
    <tbody>
      {
        items.map((item, idx) => (
          <tr key={idx}>
            {columns.map((col, idx) => <td key={idx} className={col.className}>{col.render(item)}</td>)}
          </tr>
        ))
      }
    </tbody>
  </table>
);

export default App;
