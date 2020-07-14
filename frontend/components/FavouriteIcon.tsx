import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHeart as enabledIcon } from "@fortawesome/free-solid-svg-icons";
import { faHeart as disabledIcon } from "@fortawesome/free-regular-svg-icons";
import { FavouritesProps, withFavourites } from "../utils/favourites";

interface Props {
  breweryShortName: string;
}

const FavouriteIcon = (props: Props & FavouritesProps) => {
  const enabled = props.favourites.breweries.includes(props.breweryShortName);
  return (
    <span
      className="favourite-icon"
      onClick={() => props.onToggle(props.breweryShortName)}
      title={enabled ? "Click to remove from favourites" : "Click to add to favourites"}>
      <FontAwesomeIcon icon={enabled ? enabledIcon : disabledIcon} />
    </span>
  );
};

export default withFavourites(FavouriteIcon);
