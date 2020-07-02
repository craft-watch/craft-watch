import moment from "moment";
import _inventory from "../data/inventory.json";
import { Inventory } from "./model";

const inventory = (_inventory as Inventory);

export const capturedAt = moment(inventory.metadata.capturedAt);
export const items = inventory.items;
export const categories = inventory.categories;
