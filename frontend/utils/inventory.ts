import _inventory from "../data/inventory.json";
import { Inventory } from "./model";

export const items = (_inventory as Inventory).items;
