import React from "react";
import Link from "next/link";
import { Brewery } from "../utils/model";
import _ from "underscore";
import { toSafePathPart } from "../utils/stuff";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTwitterSquare, faGithubSquare } from "@fortawesome/free-brands-svg-icons";


interface Props {
  title: string;
  desc?: JSX.Element | string;
  allBreweries: Array<Brewery>;
}

const Sidebar = (props: Props): JSX.Element => {

  return (
    <div className="sidebar">
      <nav>
        <h2 className="hide-medium">Explore ...</h2>
        <ul>
          <li><Link href="/"><a>New beers</a></Link></li>
          <li><Link href="/taster"><a>Taster menu</a></Link></li>
          <li><Link href="/full"><a>Full menu</a></Link></li>
          <li className="hide-medium">
            <b>Just added ...</b>
            <ul>
              {
                _.map(_.filter(props.allBreweries, b => b.new), b => (
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

      <div className="info hide-medium">
        <h1>{props.title}</h1>
        <div className="desc">
          {props.desc}
        </div>
      </div>

      <address>
        <div className="social">
          <a href="https://twitter.com/craft_watch">
            <FontAwesomeIcon icon={faTwitterSquare} />
          </a>
          <a href="https://github.com/craft-watch">
            <FontAwesomeIcon icon={faGithubSquare} />
          </a>
        </div>
        <div className="copyright">
          © <a href="https://github.com/oliver-charlesworth">Oliver Charlesworth</a> 2020
        </div>
      </address>
    </div>
  );
};

export default Sidebar;
