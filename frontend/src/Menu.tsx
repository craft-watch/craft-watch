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
    return (
      <div id="menu">
        {this.state.expanded ? this.renderExpanded() : this.renderCollapsed()}
      </div>
    );
  }

  private renderCollapsed(): JSX.Element {
    return (
      <div className="collapsed">
        <div className="button" onClick={() => this.setState({ expanded: true })}>&#9776;</div>
      </div>
    );
  }

  private renderExpanded(): JSX.Element {
    return (
      <div className="expanded">
        <div className="button" onClick={() => this.setState({ expanded: false })}>&times;</div>
        <Section
          title="Formats"
          selections={this.props.formatSelections}
          onToggleSelection={this.props.onToggleFormatSelection}
          onGlobalSelection={this.props.onGlobalFormatSelection}
        />
        <Section
          title="Breweries"
          selections={this.props.brewerySelections}
          onToggleSelection={this.props.onToggleBrewerySelection}
          onGlobalSelection={this.props.onGlobalBrewerySelection}
        />
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
            onClick={() => props.onToggleSelection(key)}
          />
          <span className="checkmark">{selected ? "✓" : ""}</span>
        </label>
      ))
    }
    <div>
      <span className="allOrNone" onClick={() => props.onGlobalSelection(true)}>All</span>
      <span className="allOrNone" onClick={() => props.onGlobalSelection(false)}>None</span>
    </div>
  </div>
);
