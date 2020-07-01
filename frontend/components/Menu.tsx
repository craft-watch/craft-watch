import React, { ReactElement } from "react";
import _ from "underscore";

export type Selections = { [key: string]: boolean };

interface MenuProps {
  children: ReactElement<SectionProps> | Array<ReactElement<SectionProps> | boolean>;
}

interface SectionProps {
  title: string;
  selections: Selections;
  onToggleSelection: (key: string) => void;
  onGlobalSelection: (selection: boolean) => void;
}

interface State {
  expanded: boolean;
}

export default class Menu extends React.Component<MenuProps, State> {
  constructor(props: MenuProps) {
    super(props);
    this.state = {
      expanded: false,
    };
  }

  render(): JSX.Element {
    return this.state.expanded ? this.renderExpanded() : this.renderCollapsed();
  }

  private renderCollapsed(): JSX.Element {
    return (
      <div className="menu-hamburger">
        <div onClick={() => this.setState({ expanded: true })}>Refine search ...</div>
      </div>
    );
  }

  private renderExpanded(): JSX.Element {
    return (
      <div className="menu">
        <div className="menu-button">
          <span onClick={() => this.setState({ expanded: false })}>&times;</span>
        </div>
        <div className="content">
          {this.props.children}
        </div>
        <div className="copyright">© <a href="https://github.com/oliver-charlesworth">Oliver Charlesworth</a> 2020</div>
      </div>
    );
  }
}

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
