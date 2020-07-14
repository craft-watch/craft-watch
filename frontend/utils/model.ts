import { Moment } from "moment"

// This is our preprocessed format
export interface Inventory {
  capturedAt: Moment;
  stats: Stats;
  categories: Array<string>;
  breweries: Array<Brewery>;
  items: Array<Item>;
}

// This is the wire model
export interface RawInventory {
  metadata: Metadata;
  stats: Stats;
  categories: Array<string>;
  breweries: Array<Brewery>;
  items: Array<RawItem>;
}

export interface Metadata {
  capturedAt: string;   // TODO - can we specify this as Date directly?
}

export interface Stats {
  breweries: Array<BreweryStats>;
}

export interface BreweryStats {
  name: string;
  numRawItems: number;
  numSkipped: number;
  numMalformed: number;
  numInvalid: number;
  numErrors: number;
  numMerged: number;
}

export interface Brewery {
  shortName: string;
  name: string;
  location: string;
  websiteUrl: string;
  twitterHandle?: string;
  new: boolean;
}

export interface BaseItem {
  name: string;
  summary?: string;
  desc?: string;
  mixed: boolean;
  abv?: number;
  offers: Array<Offer>;
  available: boolean;
  categories: Array<string>;
  new: boolean;
  thumbnailUrl: string;
  url: string;
}

export interface Offer {
  quantity: number;
  totalPrice: number;
  sizeMl?: number;
  format?: Format;
}

export enum Format {
  Bottle = "BOTTLE",
  Can = "CAN",
  Keg = "KEG",
}

export type RawItem = BaseItem & { brewery: string; }
export type Item = BaseItem & { brewery: Brewery }

export interface Favourites {
  breweries: Array<string>;
}

