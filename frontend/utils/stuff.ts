import { Item, Offer } from "./model";
import _ from "lodash";

export const headlineOffer = (item: Item): Offer => {
  const offer = _.first(item.offers);
  if (offer === undefined) {
    throw new Error("No offers for item");
  }
  return offer;
}
