import React from "react";
import Link from "next/link";
import { Brewery } from "../utils/model";
import _ from "lodash";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub, faTwitter } from "@fortawesome/free-brands-svg-icons";
import { faHeart, faQuestion, faSun, faListAlt } from "@fortawesome/free-solid-svg-icons";
import { BreweryLink } from "./BreweryLink";


interface Props {
  title: string;
  titleSuffix?: JSX.Element;
  desc?: JSX.Element;
  breweries: Array<Brewery>;
}

const Sidebars = (props: Props): JSX.Element => {
  return (
    <>
      <div className="sidebar right">
        <nav>
          <h2 className="hide-medium">Explore</h2>
          <ul>
            <li>
              <Link href="/">
                <a>
                  <FontAwesomeIcon icon={faSun} /> New <span className="hide-tiny">beers</span>
                </a>
              </Link>
            </li>
            <li>
              <Link href="/taster">
                <a>
                  <FontAwesomeIcon icon={faQuestion} /> Taster <span className="hide-tiny">menu</span>
                </a>
              </Link>
            </li>
            <li>
              <Link href="/favourites">
                <a>
                  <FontAwesomeIcon icon={faHeart} /> Favourites
                </a>
              </Link>
            </li>
            <li>
              <Link href="/breweries">
                <a>
                  <FontAwesomeIcon icon={faListAlt} /> Breweries <span className="hide-tiny">A-Z</span>
                </a>
              </Link>
            </li>
            <li className="show-medium"><TwitterLink /></li>
            <li className="show-medium"><GitHubLink /></li>
            <li className="hide-medium">
              <b>Just added</b>
              <ul>
                {
                  _.map(_.filter(props.breweries, b => b.new), b => (
                    <li key={b.id}>
                      <BreweryLink id={b.id}>
                        {b.shortName}
                      </BreweryLink>
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
          <h1>{props.title} {props.titleSuffix}</h1>
          <div className="desc">
            {props.desc}
          </div>
        </div>

        <address className="hide-medium">
          <div className="social">
            <TwitterLink />
            <GitHubLink />
          </div>
          <div className="copyright">
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

export default Sidebars;
