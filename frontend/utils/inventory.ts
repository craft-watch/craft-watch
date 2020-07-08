import moment from "moment";
import _inventory from "../data/inventory.json";
import { Inventory, Item } from "./model";
import _ from "underscore";

const inventory = (_inventory as Inventory);

export const capturedAt = moment(inventory.metadata.capturedAt);
export const breweries = inventory.breweries;
export const items = _.map(inventory.items, item => ({
  ...item,
  brewery: _.find(breweries, b => b.shortName == item.brewery),
}) as Item);
export const categories = inventory.categories;
