import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as enabledStar } from "@fortawesome/free-solid-svg-icons";
import { faStar as disabledStar } from "@fortawesome/free-regular-svg-icons";
import { FavouritesProps, withFavourites } from "../utils/favourites";

interface Props {
  breweryShortName: string;
}

const FavouriteStar = (props: Props & FavouritesProps) => {
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

export default withFavourites(FavouriteStar);
