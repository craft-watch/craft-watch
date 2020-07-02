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
  summary?: string;
  desc?: string;
  keg: boolean;
  mixed: boolean;
  sizeMl?: number;
  abv?: number;
  numItems: number;
  perItemPrice: number;
  available: boolean;
  categories: Array<string>;
  thumbnailUrl: string;
  url: string;
}
