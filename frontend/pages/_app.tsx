/* eslint-disable */
import "../components/index.css";

export default function MyApp(props: any) {
  const { Component, pageProps } = props;

  return <Component {...pageProps} />
}

// TODO - move to CSS modules to avoid the need for this