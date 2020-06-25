import React from "react";
import _ from "underscore";

export interface MenuProps {
  breweryVisibility: { [key: string]: boolean };
  onToggleVisibility: (brewery: string) => void;
  onGlobalVisibility: (visible: boolean) => void;
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
        <h4>Select breweries</h4>
        {
          _.map(this.props.breweryVisibility, (visible, brewery) => (
            <label key={brewery} className="selectable">
              {brewery}
              <input
                type="checkbox"
                checked={visible}
                onClick={() => this.props.onToggleVisibility(brewery)}
              />
              <span className="checkmark">{visible ? "✓" : ""}</span>
            </label>
          ))
        }
        <div>
          <span className="allOrNone" onClick={() => this.props.onGlobalVisibility(true)}>All</span>
          <span className="allOrNone" onClick={() => this.props.onGlobalVisibility(false)}>None</span>
        </div>
      </div>
    );
  }
}