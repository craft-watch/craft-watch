import React, { useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as enabledStar } from "@fortawesome/free-solid-svg-icons";
import { faStar as disabledStar } from "@fortawesome/free-regular-svg-icons";
import { toSafePathPart } from "../utils/stuff";
import { Favourites } from "../utils/model";

interface Props {
  breweryShortName: string;
  // favourites: Favourites;
}

const FavouriteStar = (props: Props) => {
  const key = `favourites.${toSafePathPart(props.breweryShortName)}`;
  const [enabled, setEnabled] = useBooleanLocalStorage(key);

  return (
    <span
      className="favourite-star"
      onClick={() => setEnabled(!enabled)}
      title={enabled ? "Click to remove from favourites" : "Click to add to favourites"}>
      <FontAwesomeIcon icon={enabled ? enabledStar : disabledStar} />
    </span>
  );
};

// TODO - we'll need to centralise this - how do we keep multiple stars for same brewery in sync?
const useBooleanLocalStorage = (key: string): [boolean, (value: boolean) => void] => {
  const [value, setValue] = useState<boolean>(false);

  const handleStorageChange = () => {
    console.log("Storage changed")
  };

  useEffect(() => {
    window.addEventListener("storage", handleStorageChange);
  }, []);

  useEffect(() => {
    const fromStorage = window.localStorage.getItem(key) !== null;
    console.log("Reading localStorage:", fromStorage);
    setValue(fromStorage);
  }, [key]);

  const update = (value: boolean) => {
    console.log("Writing localStorage:", value);
    setValue(value);
    if (value) {
      window.localStorage.setItem(key, "true");
    } else {
      window.localStorage.removeItem(key);
    }
  };

  return [value, update];
}

export default FavouriteStar;
