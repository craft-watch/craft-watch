import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub, faTwitter } from "@fortawesome/free-brands-svg-icons";


interface Props {
  title: string;
  titleSuffix?: JSX.Element;
  desc?: JSX.Element;
}

const SidebarLeft = (props: Props): JSX.Element => {
  return (
    <>
      <div className="sidebar-left">
        <div className="hide-medium">
          <h1>{props.title} {props.titleSuffix}</h1>
          {props.desc}
        </div>

        <address className="hide-medium">
          <div className="social">
            <TwitterLink />
            <GitHubLink />
          </div>
          <div className="lol">
            Craft Watch is brewed in Crofton Park.
          </div>
        </address>
      </div>
    </>
  );
};

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
