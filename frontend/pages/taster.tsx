import React, { useEffect, useState } from "react";
import _ from "underscore";
import Page from "../components/Page";
import App from "../components/App";
import { Item } from "../utils/model";
import { items as inventoryItems } from "../utils/inventory";

const ThisPage = (): JSX.Element => {
  const [items, setItems] = useState<Array<Item>>([]);

  // TODO - is there a better way to avoid this being captured by SSG?
  useEffect(() => {
    setItems(_.sample(_.filter(inventoryItems, item => !item.keg && !item.mixed && item.available), 30));
  }, []);

  return (
    <Page
      title = {"Craft Watch - taster menu"}
      description = {"Taster menu of beer prices from across the UK"}
    >
      <App items={items} />
    </Page>
  );
};

export default ThisPage;
