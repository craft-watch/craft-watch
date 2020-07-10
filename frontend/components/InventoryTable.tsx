import _ from "underscore";
import React from "react";
import Link from "next/link";
import { Item, Offer } from "../utils/model";
import SortableTable, { Column, Section } from "./SortableTable";
import { toSafePathPart, headlineOffer } from "../utils/stuff";
import { OUT_OF_STOCK, MINIKEG, MIXED_CASE } from "../utils/strings";
import { splitToParagraphs } from "../utils/reactUtils";

interface Props {
  items: Array<Item>;
  categories: Array<string>;
}

interface CellProps {
  item: Item;
}

const InventoryTable: React.FC<Props> = (props) => (
  <SortableTable sections={partitionItems(props.items, props.categories)}>
    <Column
      name="Brewery"
      className="brewery"
      render={(item: Item) => <BreweryInfo item={item} />}
      selector={item => item.brewery}
    />
    <Column
      className="thumbnail"
      render={(item: Item) => <Thumbnail item={item} />}
    />
    <Column
      name="Name"
      className="name"
      render={(item: Item) => <NameInfo item={item} />}
      selector={item => item.name}
    />
    <Column
      name="ABV"
      className="hide-tiny"
      render={(item: Item) => <AbvInfo item={item} />}
      selector={item => item.abv}
    />
    <Column
      name="Price"
      className="price"
      render={(item: Item) => <PriceInfo item={item} />}
      selector={item => perItemPrice(headlineOffer(item))}
    />
  </SortableTable>
);

const BreweryInfo = ({ item }: CellProps) => (
  <Link href={`/${toSafePathPart(item.brewery.shortName)}`}>
    <a>{item.brewery.shortName}</a>
  </Link>
);

const Thumbnail = ({ item }: CellProps) => (
  <a href={item.url}>
    <img alt="" src={item.thumbnailUrl} width="100px" height="100px" />
    {item.available || <div className="sold-out">{OUT_OF_STOCK}</div>}
  </a>
);

const NameInfo = ({ item }: CellProps) => {
  const newItem = item.new && !item.brewery.new;
  const justAdded = item.new && item.brewery.new;
  const keg = headlineOffer(item).keg;
  const kegAvailable = !keg && _.any(_.rest(item.offers), offer => offer.keg);
  const mixed = item.mixed;

  return (
    <div className="tooltip">
      <a className="item-link" href={item.url}>{item.name}</a>
      <p className="summary">
        {item.summary}
      </p>
      <p className="summary">
        {newItem && <span className="pill new">NEW !!!</span>}
        {justAdded && <span className="pill just-added">Just added</span>}
        {keg && <span className="pill keg">{MINIKEG}</span>}
        {kegAvailable && <span className="pill keg">Minikeg available</span>}
        {mixed && <span className="pill mixed">{MIXED_CASE}</span>}
      </p>
      {(item.desc !== undefined) && <TooltipBody item={item} />}
    </div>
  );
};

const AbvInfo = ({ item }: CellProps) => (
  <>
    {(item.abv !== undefined) ? `${item.abv.toFixed(1)}%` : "?"}
  </>
);

const PriceInfo = ({ item }: CellProps) => (
  <>
    <OfferInfo offer={headlineOffer(item)} />
    {
      (_.size(item.offers) > 1) && (
        <details>
          <summary>More</summary>
          {
            _.map(_.rest(item.offers), (offer, idx) => <OfferInfo key={idx} offer={offer} />)
          }
        </details>
      )
    }
  </>
);

const OfferInfo = ({ offer }: { offer: Offer }) => {
  const sizeString = sizeForDisplay(offer);
  return (
    <div className="offer">
      £{perItemPrice(offer).toFixed(2)} <span className="summary hide-small">/ item</span>
      <p className="summary">
        {
          (offer.quantity > 1) ? `${offer.quantity} × ${sizeString ?? "items"}` : sizeString
        }
        {
          offer.keg && " (keg)"
        }
      </p>
    </div>
  );
};

// These are positioned all wrong on mobile, so disable when things get small
const TooltipBody = ({ item }: CellProps) => (
  <span className="tooltip-text hide-small" style={{ display: "hidden" }}>
    {(item.desc !== undefined) && splitToParagraphs(item.desc)}
    <div className="disclaimer">© {item.brewery.shortName}</div>
  </span>
);

// TODO - may want to randomise selection for items with more than one category
const partitionItems = (items: Array<Item>, categories: Array<string>): Array<Section<Item>> => {
  const other = "Other";
  const partitioned = _.groupBy(items, item => item.categories[0] || other);
  // Ensure we uphold preferred display order
  return _.map(categories.concat(other), c => ({ name: c, data: partitioned[c] }));
};

const sizeForDisplay = (offer: Offer): string | undefined =>
  (offer.sizeMl === undefined) ? undefined :
  (offer.sizeMl < 1000) ? `${offer.sizeMl} ml` :
  `${offer.sizeMl / 1000} litres`;

const perItemPrice = (offer: Offer): number => offer.totalPrice / offer.quantity;

export default InventoryTable;
