import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHeart as enabledIcon } from "@fortawesome/free-solid-svg-icons";
import { faHeart as disabledIcon } from "@fortawesome/free-regular-svg-icons";
import { FavouritesProps, withFavourites } from "../../utils/favourites";
import styles from "./styles.module.css";


interface Props {
  breweryId: string;
}

const FavouriteIcon = (props: Props & FavouritesProps) => {
  const enabled = props.favourites.breweryIds.includes(props.breweryId);
  return (
    <span
      className={styles.icon}
      onClick={() => props.onToggle(props.breweryId)}
      title={enabled ? "Click to remove from favourites" : "Click to add to favourites"}>
      <FontAwesomeIcon icon={enabled ? enabledIcon : disabledIcon} />
    </span>
  );
};

export default withFavourites(FavouriteIcon);
