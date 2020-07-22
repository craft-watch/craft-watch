import React, { ReactElement, useState } from "react";
import _ from "lodash";
import { Moment } from "moment";
import styles from "./Menu.module.css";
import classNames from "classnames";


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
    <div className={styles.collapsed}>
      <div className={styles.clickable} onClick={() => setExpanded(true)}>Refine search ...</div>
    </div>
  );

  const renderExpanded = (): JSX.Element => (
    <div className={styles.expanded}>
      <div className={classNames(styles.button, styles.clickable)}>
        <span onClick={() => setExpanded(false)}>&times;</span>
      </div>
      <div className={styles.content}>
        {props.children}
      </div>
      <div className={styles.info}>
        Data captured: {props.capturedAt.local().format("lll")}.
      </div>
    </div>
  );

  return (
    <div className={styles.menu}>
      {expanded ? renderExpanded() : renderCollapsed()}
    </div>
  );
};

export const Section: React.FC<SectionProps> = (props) => (
  <div className={styles.section}>
    <h4>{props.title}</h4>
    {
      _.map(props.selections.selections, (selected, key) => (
        <label key={key} className={classNames(styles.selectable, styles.clickable)}>
          {key}
          <input
            type="checkbox"
            checked={selected}
            onChange={() => props.selections.toggle(key)}
          />
          <span className={styles.checkmark}>{selected ? "✓" : ""}</span>
        </label>
      ))
    }
    {
      (_.size(props.selections.selections) > 1) && (
        <div>
          <span
            className={classNames(styles.option, styles.clickable)}
            onClick={() => props.selections.setGlobal(true)}
          >
            All
          </span>
          <span
            className={classNames(styles.option, styles.clickable)}
            onClick={() => props.selections.setGlobal(false)}
          >
            None
          </span>
        </div>
      )
    }

  </div>
);

export default Menu;
