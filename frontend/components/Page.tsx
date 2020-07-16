import React, { ReactNode } from "react";
import Head from "next/head";
import { Brewery } from "../utils/model";
import Sidebars from "./Sidebars";

interface Props {
  title: string;
  titleSuffix?: JSX.Element;
  desc: string;
  longDesc: JSX.Element;
  breweries: Array<Brewery>;
  children?: ReactNode;
}

const Page: React.FC<Props> = (props) => (
  <>
    <Head>
      <meta charSet="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <meta name="description" content={props.desc} />

      <title>Craft Watch - {props.title}</title>

      <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png" />
      <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png" />
      <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png" />
      <link rel="manifest" href="/site.webmanifest" />
      <link rel="mask-icon" href="/safari-pinned-tab.svg" color="#5bbad5" />
      <link rel="shortcut icon" href="/favicon.ico" />
      <meta name="msapplication-TileColor" content="#da532c" />
      <meta name="msapplication-config" content="/browserconfig.xml" />
      <meta name="theme-color" content="#ffffff" />

      <meta name="twitter:card" content="summary" />
      <meta name="twitter:site" content="@craft_watch" />
      <meta property="og:url" content="https://craft.watch/" />
      <meta property="og:title" content={props.title} />
      <meta property="og:description" content={props.desc} />
      <meta property="og:image" content="https://craft.watch/craft-watch.jpg" />
      <meta property="og:type" content="website" />

      <script async defer src="https://alysis.alexsparrow.dev/tracker.js" data-alysis-domain="craft.watch" />

      {
        // See https://github.com/vercel/next.js/issues/9070#issuecomment-552981178
        // (and https://docs.plausible.io/spa-support).
        // Unclear why this hack works, but it does.
        process.browser && <script async defer data-domain="craft.watch" src="https://plausible.io/js/plausible.js" />
      }
    </Head>

    <Sidebars
      title={props.title}
      titleSuffix={props.titleSuffix}
      desc={props.longDesc}
      breweries={props.breweries}
    />

    {props.children}
  </>
);

export default Page;
