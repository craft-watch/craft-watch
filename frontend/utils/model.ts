export interface Inventory {
  metadata: Metadata;
  categories: Array<string>;
  items: Array<Item>;
}

export interface Metadata {
  capturedAt: string;   // TODO - can we specify this as Date directly?
}

export interface Item {
  brewery: string;
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
