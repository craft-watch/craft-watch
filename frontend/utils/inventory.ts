import moment from "moment";
import _inventory from "../data/inventory.json";
import { Inventory, RawInventory } from "./model";
import _ from "underscore";

const rawInventory = (_inventory as RawInventory);

export const inventory = {
  capturedAt: moment(rawInventory.metadata.capturedAt),
  breweries: rawInventory.breweries,
  categories: rawInventory.categories,
  items: _.map(rawInventory.items, item => ({
    ...item,
    brewery: _.find(rawInventory.breweries, b => b.shortName == item.brewery),
  })),
} as Inventory;
