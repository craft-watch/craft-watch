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
    // Use stable-sort property to achieve lexicographical sort
    setItems(
      _.sortBy(
        _.sortBy(
          _.sample(_.filter(inventoryItems, item => !item.keg && !item.mixed && item.available), 20),
          item => item.name
        ),
        item => item.brewery
      )
    );
  }, []);

  return (
    <Page
      title = "Craft Watch - taster menu"
      description = "Taster menu of beer prices from across the UK"
    >
      <App items={items} />
    </Page>
  );
};

export default ThisPage;
