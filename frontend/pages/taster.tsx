import React from "react";
import _ from "underscore";
import { GetServerSideProps } from "next";
import Page from "../components/Page";
import App from "../components/App";
import { Item } from "../utils/model";
import { items } from "../utils/inventory";

interface Props {
  items: Array<Item>;
}

const IndexPage = (props: Props): JSX.Element => (
  <Page
    title = {"Craft Watch - taster menu"}
    description = {"Taster menu of beer prices from across the UK"}
  >
    <App items={props.items} />
  </Page>
);

export default IndexPage;

export const getServerSideProps: GetServerSideProps<Props> = async () => ({
  props: {
    items: _.sample(items, 30)
  }
});
