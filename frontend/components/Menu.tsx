import React, { ReactElement, useState } from "react";
import _ from "underscore";
import { Moment } from "moment";

export type Selections = { [key: string]: boolean };

interface Props {
  children: ReactElement<SectionProps> | Array<ReactElement<SectionProps> | boolean>;
  capturedAt: Moment;
}

interface SectionProps {
  title: string;
  selections: Selections;
  onToggleSelection: (key: string) => void;
  onGlobalSelection: (selection: boolean) => void;
}

const Menu = (props: Props): JSX.Element => {
  const [expanded, setExpanded] = useState<boolean>(false);

  const renderCollapsed = (): JSX.Element => (
    <div className="menu-hamburger">
      <div onClick={() => setExpanded(true)}>Refine search ...</div>
    </div>
  );

  const renderExpanded = (): JSX.Element => (
    <div className="menu">
      <div className="menu-button">
        <span onClick={() => setExpanded(false)}>&times;</span>
      </div>
      <div className="content">
        {props.children}
      </div>
      <div className="info">
        Data captured: {props.capturedAt.local().format("lll")}.
        <div className="copyright">
          © <a href="https://github.com/oliver-charlesworth">Oliver Charlesworth</a> 2020
        </div>
      </div>
    </div>
  );

  return expanded ? renderExpanded() : renderCollapsed();
};

export const Section: React.FC<SectionProps> = (props) => (
  <div className="section">
    <h4>{props.title}</h4>
    {
      _.map(props.selections, (selected, key) => (
        <label key={key} className="selectable">
          {key}
          <input
            type="checkbox"
            checked={selected}
            onChange={() => props.onToggleSelection(key)}
          />
          <span className="checkmark">{selected ? "✓" : ""}</span>
        </label>
      ))
    }
    {
      (_.size(props.selections) > 1) && (
        <div>
          <span className="all-or-none" onClick={() => props.onGlobalSelection(true)}>All</span>
          <span className="all-or-none" onClick={() => props.onGlobalSelection(false)}>None</span>
        </div>
      )
    }

  </div>
);

export default Menu;
