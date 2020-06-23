export interface Inventory {
  items: Array<Item>;
}

export interface Item {
  brewery: string;
  name: string;
  summary?: string;
  sizeMl?: number;
  abv?: number;
  perItemPrice: number;
  available: boolean;
  thumbnailUrl: string;
  url: string;
}

  