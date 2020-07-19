import moment from "moment";
import _inventory from "../data/inventory.json";
import { Inventory, RawInventory } from "./model";
import _ from "lodash";

const raw = (_inventory as RawInventory);

export const inventory = {
  ...raw,
  capturedAt: moment(raw.metadata.capturedAt),
  items: _.map(raw.items, item => ({
    ...item,
    brewery: _.find(raw.breweries, b => b.id == item.breweryId),
  })),
} as Inventory;
