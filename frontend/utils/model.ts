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
  summary: string | null;
  desc: string | null;
  keg: boolean;
  mixed: boolean;
  sizeMl: number | null;
  abv: number | null;
  numItems: number;
  perItemPrice: number;
  available: boolean;
  categories: Array<string>;
  new: boolean;
  thumbnailUrl: string;
  url: string;
}

export type RawItem = BaseItem & { brewery: string; }
export type Item = BaseItem & { brewery: Brewery }

