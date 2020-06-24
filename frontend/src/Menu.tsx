import React from "react";

export interface MenuProps {
  breweryVisibility: { [key: string]: boolean; };
  onChange: (brewery: string) => void;
}
  
interface State {
  expanded: boolean;
}
  
export class Menu extends React.Component<MenuProps, State> {
  constructor(props: MenuProps) {
    super(props);
    this.state = {
      expanded: false,
    };
  }
  
  render() {
    return (
      <div id="menu">
        {this.state.expanded ? this.renderExpanded() : this.renderCollapsed()}
      </div>
    );
  }

  private renderCollapsed() {
    return (
      <div className="collapsed">
        <div className="button" onClick={() => this.setState({ expanded: true })}>&#9776;</div>
      </div>
    );
  }

  private renderExpanded() {
    return (
      <div className="expanded">
        <div className="button" onClick={() => this.setState({ expanded: false })}>&times;</div>
        <h4>Select breweries</h4>
        {
          Object.entries(this.props.breweryVisibility).map(([brewery, visible]) => (
            <label key={brewery} className="selectable">
              {brewery}
              <input
                type="checkbox"
                checked={visible}
                onChange={() => this.props.onChange(brewery)}
              />
              <span className="checkmark"></span>
            </label>
          ))
        }
      </div>
    );
  }
}