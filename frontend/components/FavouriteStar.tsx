import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as enabledStar } from "@fortawesome/free-solid-svg-icons";
import { faStar as disabledStar } from "@fortawesome/free-regular-svg-icons";
import { Favourites } from "../utils/model";

interface Props {
  breweryShortName: string;
  // TODO - how can we do this behind the scenes?  i.e. via a context?
  favourites: Favourites;
  onToggle: (name: string) => void;
}

const FavouriteStar = (props: Props) => {
  const enabled = props.favourites.breweries.includes(props.breweryShortName);

  return (
    <span
      className="favourite-star"
      onClick={() => props.onToggle(props.breweryShortName)}
      title={enabled ? "Click to remove from favourites" : "Click to add to favourites"}>
      <FontAwesomeIcon icon={enabled ? enabledStar : disabledStar} />
    </span>
  );
};

export default FavouriteStar;
