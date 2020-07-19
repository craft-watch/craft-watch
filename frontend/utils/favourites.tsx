import React, { useState, useEffect } from "react";
import _ from "lodash";

export interface FavouritesProps {
  favourites: Favourites;
  onToggle: (name: string) => void;
}

export interface Favourites {
  breweryIds: Array<string>;
}

const FavouritesContext = React.createContext<FavouritesProps>({
  favourites: { breweryIds: [] },
  onToggle: () => {},
});

const KEY = "favourites";

export const FavouritesProvider: React.FC<unknown> = (props) => {
  const [favourites, setFavourites] = useState<Favourites>({ breweryIds: [] });

  const readFromLocalStorage = () => {
    // TODO - error-handling
    const raw = window.localStorage.getItem(KEY);
    if (raw !== null) {
      setFavourites(JSON.parse(raw));
    }
  };

  useEffect(() => {
    readFromLocalStorage();  // Acquire initial state
    window.addEventListener("storage", readFromLocalStorage);
  }, []);

  const onToggle = (id: string) => {
    const breweries = new Set<string>(favourites.breweryIds);
    if (breweries.has(id)) {
      breweries.delete(id);
    } else {
      breweries.add(id);
    }
    const next = { breweryIds: _.sortBy(Array.from(breweries), s => s) } as Favourites;

    window.localStorage.setItem(KEY, JSON.stringify(next));
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
