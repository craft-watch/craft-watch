import _ from "underscore";
import React from "react";
import Link from "next/link";
import { Item } from "../utils/model";
import SortableTable, { Column, Renderer, Section } from "./SortableTable";
import { toSafePathPart } from "../utils/stuff";
import { OUT_OF_STOCK, MINIKEG, MIXED_CASE } from "../utils/strings";

interface Props {
  items: Array<Item>;
  categories: Array<string>;
}

const InventoryTable: React.FC<Props> = (props) => (
  <SortableTable sections={partitionItems(props.items, props.categories)}>
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
      className="hide-on-mobile"
      render={renderAbv}
      selector={(item) => item.abv}
    />
    <Column
      name="Size"
      className="size hide-on-mobile"
      render={renderSize}
      selector={(item) => item.sizeMl}
    />
    <Column
      name="Price"
      render={renderPrice}
      selector={(item) => item.perItemPrice}
    />
  </SortableTable>
);

// TODO - may want to randomise selection for items with more than one category
const partitionItems = (items: Array<Item>, categories: Array<string>): Array<Section<Item>> => {
  const other = "Other";
  const partitioned = _.groupBy(items, item => item.categories[0] || other);
  // Ensure we uphold preferred display order
  return _.map(categories.concat(other), c => ({ name: c, data: partitioned[c] }));
};

const renderBrewery: Renderer<Item> = item => (
  <Link href={`/${toSafePathPart(item.brewery)}`}>
    <a>{item.brewery}</a>
  </Link>
);

const renderThumbnail: Renderer<Item> = item => (
  <a href={item.url}>
    <img alt="" src={item.thumbnailUrl} width="100px" height="100px" />
    {item.available || <div className="sold-out">{OUT_OF_STOCK}</div>}
  </a>
);

const renderName: Renderer<Item> = item => (
  <div className="tooltip">
    <a className="item-link" href={item.url}>{item.name}</a>
    <p className="summary">
      {item.summary && item.summary}
    </p>
    <p className="summary">
      {item.keg && <span className="pill violet">{MINIKEG}</span>}
      {item.mixed && <span className="pill magenta">{MIXED_CASE}</span>}
    </p>
    {item.desc && renderTooltipText(item)}
  </div>
);

// TODO - collapse successive newlines
const renderTooltipText = (item: Item): JSX.Element => (
  <span className="tooltip-text" style={{ display: "hidden" }}>
    {item.desc && _.map(item.desc.split("\n"), (para, idx) => <p key={idx}>{para}</p>)}
    <div className="disclaimer">© {item.brewery}</div>
  </span>
);

const renderAbv: Renderer<Item> = item => item.abv ? `${item.abv.toFixed(1)}%` : "?";

const renderSize: Renderer<Item> = item =>
  !item.sizeMl ? "?" :
  (item.sizeMl < 1000) ? `${item.sizeMl} ml` :
  `${item.sizeMl / 1000} litres`;

const renderPrice: Renderer<Item> = item => (
  <div>
    £{item.perItemPrice.toFixed(2)}
    {
      (item.numItems > 1) && (
        <p className="summary">
          &times; {item.numItems} items
        </p>
      )
    }
  </div>
);

export default InventoryTable;
