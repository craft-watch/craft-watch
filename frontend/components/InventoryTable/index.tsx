import _ from "lodash";
import React from "react";
import classNames from "classnames";
import { Item, Offer, Format } from "../../utils/model";
import SortableTable, { Column, Section, CellProps } from "../SortableTable";
import { headlineOffer } from "../../utils/stuff";
import { OUT_OF_STOCK, MINIKEG, MIXED_CASE } from "../../utils/strings";
import { splitToParagraphs } from "../../utils/reactUtils";
import { BreweryLink } from "../BreweryLink";
import styles from "./styles.module.css";


interface Props {
  items: Array<Item>;
  categories: Array<string>;
}

const InventoryTable: React.FC<Props> = (props) => (
  <SortableTable sections={partitionItems(props.items, props.categories)}>
    <Column render={BreweryInfo} name="Brewery" className={styles.brewery} selector={item => item.brewery} />
    <Column render={Thumbnail} className={styles.thumbnail} />
    <Column render={NameInfo} name="Name" className={styles.name} selector={item => item.name} />
    <Column render={AbvInfo} name="ABV" className="hide-tiny" selector={item => item.abv} />
    <Column
      render={PriceInfo}
      name="Price"
      className={styles.price}
      selector={item => perItemPrice(headlineOffer(item))}
    />
  </SortableTable>
);

const BreweryInfo = ({ datum }: CellProps<Item>) => (
  <BreweryLink id={datum.brewery.id}>
    {datum.brewery.shortName}
  </BreweryLink>
);

const Thumbnail = ({ datum }: CellProps<Item>) => (
  <a href={datum.url}>
    <img alt="" src={datum.thumbnailUrl} width="100px" height="100px" />
    {datum.available || <div className={styles["sold-out"]}>{OUT_OF_STOCK}</div>}
  </a>
);

const NameInfo = ({ datum }: CellProps<Item>) => {
  const newItem = datum.new;
  const justAdded = datum.brewery.new;
  const keg = headlineOffer(datum).format === Format.Keg;
  const kegAvailable = !keg && _.some(_.tail(datum.offers), offer => offer.format === Format.Keg);
  const mixed = datum.mixed;

  return (
    <div className={styles.tooltip}>
      <a href={datum.url}>{datum.name}</a>
      <p className={styles.info}>
        {datum.summary}
      </p>
      <p className={styles.info}>
        {newItem && <span className={classNames(styles.pill, styles.new)}>NEW !!!</span>}
        {justAdded && <span className={classNames(styles.pill, styles["just-added"])}>Just added</span>}
        {keg && <span className={classNames(styles.pill, styles.keg)}>{MINIKEG}</span>}
        {kegAvailable && <span className={classNames(styles.pill, styles.keg)}>Keg available</span>}
        {mixed && <span className={classNames(styles.pill, styles.mixed)}>{MIXED_CASE}</span>}
      </p>
      {(datum.desc !== undefined) && <TooltipBody datum={datum} />}
    </div>
  );
};

const AbvInfo = ({ datum }: CellProps<Item>) => (
  <>
    {
      (datum.mixed) ? "" :
      (datum.abv !== undefined) ? `${datum.abv.toFixed(1)}%` :
      "?"
    }
  </>
);

const PriceInfo = ({ datum }: CellProps<Item>) => (
  <>
    <OfferInfo offer={headlineOffer(datum)} />
    {
      (_.size(datum.offers) > 1) && (
        <details>
          <summary>{_.size(datum.offers) - 1} more</summary>
          {
            _.map(_.tail(datum.offers), (offer, idx) => <OfferInfo key={idx} offer={offer} />)
          }
        </details>
      )
    }
  </>
);

const OfferInfo = ({ offer }: { offer: Offer }) => {
  const sizeString = sizeForDisplay(offer);
  const formatString = formatForDisplay(offer);
  return (
    <div className={styles.offer}>
      £{priceForDisplay(offer)} <span className={classNames(styles.info, "hide-small")}>/ {formatString}</span>
      <p className={styles.info}>
        {
          (offer.quantity > 1) ? `${offer.quantity} × ${sizeString ?? `${formatString}s`}` : sizeString
        }
      </p>
    </div>
  );
};

// These are positioned all wrong on mobile, so disable when things get small
const TooltipBody = ({ datum }: CellProps<Item>) => (
  <span className={classNames(styles["tooltip-text"], "hide-small")} style={{ display: "hidden" }}>
    {(datum.desc !== undefined) && splitToParagraphs(datum.desc)}
    <div className={styles.disclaimer}>© {datum.brewery.shortName}</div>
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

const formatForDisplay = (offer: Offer): string => {
  switch (offer.format) {
  case Format.Bottle:
    return "bottle";
  case Format.Can:
    return "can";
  case Format.Keg:
    return "keg";
  default:
    return "item";
  }
};

const priceForDisplay = (offer: Offer): string => perItemPrice(offer).toFixed(2).replace(/\.00/, "");

const perItemPrice = (offer: Offer): number => offer.totalPrice / offer.quantity;

export default InventoryTable;
