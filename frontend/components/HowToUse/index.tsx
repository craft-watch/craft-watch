import React from "react";
import styles from "./styles.module.css";
import classNames from "classnames";


interface Props {
  text: string;
}

const HowToUse = (props: Props): JSX.Element => (
  <div className={classNames(styles.htu, "show-medium")}>
    {props.text}
  </div>
);

export default HowToUse;
