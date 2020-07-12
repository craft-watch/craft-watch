import React, { ReactElement, useState } from "react";
import _ from "underscore";
import { Moment } from "moment";

export interface Selections {
  selections: { [key: string]: boolean };
  toggle: (key: string) => void;
  setGlobal: (selected: boolean) => void;
  setKeys: (keys: Array<string>) => void;
}

interface Props {
  children: ReactElement<SectionProps> | Array<ReactElement<SectionProps> | boolean>;
  capturedAt: Moment;
}

interface SectionProps {
  title: string;
  selections: Selections;
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
      </div>
    </div>
  );

  return expanded ? renderExpanded() : renderCollapsed();
};

export const Section: React.FC<SectionProps> = (props) => (
  <div className="section">
    <h4>{props.title}</h4>
    {
      _.map(props.selections.selections, (selected, key) => (
        <label key={key} className="selectable">
          {key}
          <input
            type="checkbox"
            checked={selected}
            onChange={() => props.selections.toggle(key)}
          />
          <span className="checkmark">{selected ? "✓" : ""}</span>
        </label>
      ))
    }
    {
      (_.size(props.selections.selections) > 1) && (
        <div>
          <span className="all-or-none" onClick={() => props.selections.setGlobal(true)}>All</span>
          <span className="all-or-none" onClick={() => props.selections.setGlobal(false)}>None</span>
        </div>
      )
    }

  </div>
);

export default Menu;
