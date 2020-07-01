import React from "react";
import _ from "underscore";
import { GetStaticProps, GetStaticPaths } from "next";
import Page from "../../components/Page";
import App from "../../components/App";
import { Item } from "../../utils/model";
import { items } from "../../utils/inventory";

interface Props {
  brewery: string;
  items: Array<Item>;
}

const IndexPage = (props: Props): JSX.Element => (
  <Page
    title = {`Craft Watch - ${props.brewery}`}
    description = {`Daily updates of beer prices from ${props.brewery}`}
  >
    <App items={props.items} />
  </Page>
);

export default IndexPage;

export const getStaticPaths: GetStaticPaths = async () => ({
  paths: _.map(_.keys(safeNamesToItems), name => ({ params: { id: name } })),
  fallback: false,
});

export const getStaticProps: GetStaticProps<Props> = async ({ params }) => {
  const brewery = params?.id as string;
  const items = safeNamesToItems[brewery];
  return {
    props: {
      brewery: items[0].brewery,
      items
    }
  };
};

const safeNamesToItems = _.groupBy(items, item => item.brewery.toLowerCase().replace(/[^0-9a-z]/g, "-"));
