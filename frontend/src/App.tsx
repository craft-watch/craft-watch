import React from "react";
import "./index.css";
import inventory from "./inventory.json";

const App = () => (
  <div>
    <table>
      <thead>
        <tr>
          <th>Brewery</th>
          <th></th>
          <th className="name">Name</th>
          <th>ABV</th>
          <th>Size</th>
          <th>Price per item</th>
        </tr>
      </thead>
      <tbody>
        {inventory.items.map(item => renderRow(item))}
      </tbody>
    </table>
  </div>
);

const renderRow = (item: any) => (
  <tr key={`${item.name}/${item.summary}`}>
    <td>{item.brewery}</td>
    <td className="thumbnail">
      {
        item.thumbnailUrl && (
          <>
            <a href={item.url}>
              <img src={item.thumbnailUrl} width="100px" height="100px" />
              {item.available || <div className="sold-out">Sold out</div>}
            </a>
          </>
        )
      }
    </td>
    <td className="name">
      <a href={item.url}>{item.name}</a>
      {item.summary && <p className="summary">{item.summary}</p>}
    </td>
    <td>{item.abv ? `${item.abv.toFixed(1)}%` : "?"}</td>
    <td>
      {
        !item.sizeMl ? null
        : (item.sizeMl < 1000) ? `${item.sizeMl} ml`
        : `${item.sizeMl / 1000} litres`
      }
    </td>
    <td>{`£${item.perItemPrice.toFixed(2)}`}</td>
  </tr>
);

export default App;
