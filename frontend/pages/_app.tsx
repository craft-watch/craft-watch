/* eslint-disable */
import { config } from '@fortawesome/fontawesome-svg-core'
import "@fortawesome/fontawesome-svg-core/styles.css";
import "../components/index.css";
import { FavouritesProvider } from '../utils/favourites';

config.autoAddCss = false; // See https://github.com/FortAwesome/react-fontawesome#nextjs

export default function MyApp(props: any) {
  const { Component, pageProps } = props;

  return (
    <FavouritesProvider>
      <Component {...pageProps} />
    </FavouritesProvider>
  );
}

// TODO - move to CSS modules to avoid the need for this
