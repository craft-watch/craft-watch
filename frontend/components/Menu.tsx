import React from "react";
import _ from "underscore";

export type Selections = { [key: string]: boolean };

export interface MenuProps {
  brewerySelections: Selections;
  onToggleBrewerySelection: (key: string) => void;
  onGlobalBrewerySelection: (selected: boolean) => void;
  formatSelections: Selections;
  onToggleFormatSelection: (key: string) => void;
  onGlobalFormatSelection: (selected: boolean) => void;
}

export interface SectionProps {
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
          <Section
            title="Formats"
            selections={this.props.formatSelections}
            onToggleSelection={this.props.onToggleFormatSelection}
            onGlobalSelection={this.props.onGlobalFormatSelection}
          />
          {
            (_.size(this.props.brewerySelections) > 1) && (
              <Section
                title="Breweries"
                selections={this.props.brewerySelections}
                onToggleSelection={this.props.onToggleBrewerySelection}
                onGlobalSelection={this.props.onGlobalBrewerySelection}
              />
            )
          }
        </div>
        <div className="copyright">© <a href="https://github.com/oliver-charlesworth">Oliver Charlesworth</a> 2020</div>
      </div>
    );
  }
}

const Section: React.FC<SectionProps> = (props) => (
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
    <div>
      <span className="all-or-none" onClick={() => props.onGlobalSelection(true)}>All</span>
      <span className="all-or-none" onClick={() => props.onGlobalSelection(false)}>None</span>
    </div>
  </div>
);
