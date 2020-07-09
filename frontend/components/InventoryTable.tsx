import _ from "underscore";
import React from "react";
import Link from "next/link";
import { Item, Offer } from "../utils/model";
import SortableTable, { Column, Renderer, Section } from "./SortableTable";
import { toSafePathPart } from "../utils/stuff";
import { OUT_OF_STOCK, MINIKEG, MIXED_CASE } from "../utils/strings";
import { splitToParagraphs } from "../utils/reactUtils";

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
      className="hide-tiny"
      render={renderAbv}
      selector={(item) => item.abv}
    />
    <Column
      name="Size"
      className="size hide-small"
      render={renderSize}
      selector={(item) => item.sizeMl}
    />
    <Column
      name="Price"
      render={renderPrice}
      selector={(item) => {
        const offer = extractOffer(item);
        return offer ? perItemPrice(offer) : undefined;
      }}
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
  <Link href={`/${toSafePathPart(item.brewery.shortName)}`}>
    <a>{item.brewery.shortName}</a>
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
      {item.summary}
    </p>
    <p className="summary">
      {item.new && !item.brewery.new && <span className="pill new">NEW !!!</span>}
      {item.new && item.brewery.new && <span className="pill just-added">Just added</span>}
      {item.keg && <span className="pill keg">{MINIKEG}</span>}
      {item.mixed && <span className="pill mixed">{MIXED_CASE}</span>}
    </p>
    {(item.desc !== null) && renderTooltipText(item)}
  </div>
);

// These are positioned all wrong on mobile, so disable when things get small
const renderTooltipText = (item: Item): JSX.Element => (
  <span className="tooltip-text hide-small" style={{ display: "hidden" }}>
    {(item.desc !== null) && splitToParagraphs(item.desc)}
    <div className="disclaimer">© {item.brewery.shortName}</div>
  </span>
);

const renderAbv: Renderer<Item> = item => (item.abv !== null) ? `${item.abv.toFixed(1)}%` : "?";

const renderSize: Renderer<Item> = item =>
  (item.sizeMl === null) ? "?" :
  (item.sizeMl < 1000) ? `${item.sizeMl} ml` :
  `${item.sizeMl / 1000} litres`;

const renderPrice: Renderer<Item> = item => {
  const offer = extractOffer(item);

  return (!offer)
    ? "?"
    : (
      <div>
        £{perItemPrice(offer).toFixed(2)}
        {
          (offer.quantity > 1) && (
            <p className="summary">
              &times; {offer.quantity} items
            </p>
          )
        }
      </div>
    );
};


const extractOffer = (item: Item): Offer | undefined => _.first(item.offers);

const perItemPrice = (offer: Offer): number => offer.totalPrice / offer.quantity;

export default InventoryTable;
