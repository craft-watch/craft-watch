export interface Inventory {
  items: Array<Item>;
}

export interface Item {
  brewery: string;
  name: string;
  summary?: string;
  keg: boolean;
  mixed: boolean;
  sizeMl?: number;
  abv?: number;
  perItemPrice: number;
  available: boolean;
  thumbnailUrl: string;
  url: string;
}
