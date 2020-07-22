import React from "react";
import Link from "next/link";
import { Brewery } from "../../utils/model";
import _ from "lodash";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub, faTwitter } from "@fortawesome/free-brands-svg-icons";
import { faHeart, faQuestion, faSun, faListAlt } from "@fortawesome/free-solid-svg-icons";
import { BreweryLink } from "../BreweryLink";
import styles from "./styles.module.css";


interface Props {
  breweries: Array<Brewery>;
}

const SidebarRight = (props: Props): JSX.Element => (
  <>
    <div className={styles.sidebar}>
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
        </ul>

        <div className="hide-medium">
          <h2>Just added</h2>
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
        </div>
      </nav>
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

export default SidebarRight;
