import React, { useState, useEffect } from "react";
import _ from "underscore";

export interface FavouritesProps {
  favourites: Favourites;
  onToggle: (name: string) => void;
}

export interface Favourites {
  breweries: Array<string>;
}

export const FavouritesContext = React.createContext<FavouritesProps>({
  favourites: { breweries: [] },
  onToggle: () => {},
});

export const FavouritesProvider: React.FC<unknown> = (props) => {
  const key = "favourites";
  const [favourites, setFavourites] = useState<Favourites>({ breweries: [] });

  const readFromLocalStorage = () => {
    // TODO - error-handling
    const raw = window.localStorage.getItem(key);
    if (raw !== null) {
      setFavourites(JSON.parse(raw));
    }
  };

  useEffect(() => {
    window.addEventListener("storage", readFromLocalStorage);
    readFromLocalStorage();  // Acquire initial state
  }, []);

  const onToggle = (shortName: string) => {
    const breweries = new Set<string>(favourites.breweries);
    if (breweries.has(shortName)) {
      breweries.delete(shortName);
    } else {
      breweries.add(shortName);
    }
    const next = { breweries: _.sortBy(Array.from(breweries), s => s) } as Favourites;

    window.localStorage.setItem(key, JSON.stringify(next));
    readFromLocalStorage();   // Don't set state directly, to avoid race with external modification to local storage
  };

  return (
    <FavouritesContext.Provider value={{ favourites, onToggle }}>
      {props.children}
    </FavouritesContext.Provider>
  )
};

export const withFavourites = <P extends unknown>(Component: React.ComponentType<P & FavouritesProps>) =>
  (props: P) => (
    <FavouritesContext.Consumer>
      {
        contextProps => <Component {...props} {...contextProps} />
      }
    </FavouritesContext.Consumer>
  );
