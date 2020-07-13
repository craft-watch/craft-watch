export interface Inventory {
  metadata: Metadata;
  categories: Array<string>;
  breweries: Array<Brewery>;
  items: Array<RawItem>;
}

export interface Metadata {
  capturedAt: string;   // TODO - can we specify this as Date directly?
}

export interface Brewery {
  shortName: string;
  name: string;
  location: string;
  websiteUrl: string;
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

