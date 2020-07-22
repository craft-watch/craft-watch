import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub, faTwitter } from "@fortawesome/free-brands-svg-icons";
import styles from "./SidebarLeft.module.css";
import classNames from "classnames";


interface Props {
  title: string;
  titleSuffix?: JSX.Element;
  desc?: JSX.Element;
}

const SidebarLeft = (props: Props): JSX.Element => (
  <>
    <div className={classNames(styles.sidebar, "hide-medium")}>
      <h1>{props.title} {props.titleSuffix}</h1>
      {props.desc}

      <address className={styles.address}>
        <div className={styles.social}>
          <TwitterLink />
          <GitHubLink />
        </div>
        <div className={styles.lol}>
          Craft Watch is brewed in Crofton Park.
        </div>
      </address>
    </div>
  </>
);

const TwitterLink = () => (
  <a href="https://twitter.com/craft_watch">
    <FontAwesomeIcon icon={faTwitter} />
  </a>
);

const GitHubLink = () => (
  <a href="https://github.com/craft-watch">
    <FontAwesomeIcon icon={faGithub} />
  </a>
);

export default SidebarLeft;
