import React from "react";
import Link from "next/link";
import { Brewery } from "../utils/model";
import _ from "underscore";
import { toSafePathPart } from "../utils/stuff";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub, faTwitter } from "@fortawesome/free-brands-svg-icons";


interface Props {
  title: string;
  desc?: JSX.Element | string;
  breweries: Array<Brewery>;
}

const Sidebars = (props: Props): JSX.Element => {
  return (
    <>
      <div className="sidebar right">
        <nav>
          <h2 className="hide-medium">Explore ...</h2>
          <ul>
            <li><Link href="/"><a>New <span className="hide-tiny">beers</span></a></Link></li>
            <li><Link href="/taster"><a>Taster <span className="hide-tiny">menu</span></a></Link></li>
            <li><Link href="/full"><a>Full <span className="hide-tiny">menu</span></a></Link></li>
            <li><Link href="/breweries"><a>Breweries <span className="hide-tiny">A-Z</span></a></Link></li>
            <li className="show-medium"><TwitterLink /></li>
            <li className="show-medium"><GitHubLink /></li>
            <li className="hide-medium">
              <b>Just added ...</b>
              <ul>
                {
                  _.map(_.filter(props.breweries, b => b.new), b => (
                    <li key={b.shortName}>
                      <Link href={`/${toSafePathPart(b.shortName)}`}>
                        <a>{b.shortName}</a>
                      </Link>
                    </li>
                  ))
                }
              </ul>
            </li>
          </ul>
        </nav>
      </div>

      <div className="sidebar left">
        <div className="info hide-medium">
          <h1>{props.title}</h1>
          <div className="desc">
            {props.desc}
          </div>
        </div>

        <address className="hide-medium">
          <div className="social">
            <TwitterLink />
            <GitHubLink />
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

export default Sidebars;
