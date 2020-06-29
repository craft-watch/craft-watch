import "../components/index.css"

// This default export is required in a new `pages/_app.js` file.
export default function MyApp({ Component, pageProps }) {
  return <Component {...pageProps} />
}

// TODO - move to CSS modules to avoid the need for this
