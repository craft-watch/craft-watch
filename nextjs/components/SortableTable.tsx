import React, { ReactElement } from "react";
import _ from "underscore";

export type Renderer<T> = (datum: T) => JSX.Element | string | null;

export interface ColumnProps<T> {
  name?: JSX.Element | string;
  className?: string;
  render: Renderer<T>;
  selector?: (datum: T) => any; // eslint-disable-line @typescript-eslint/no-explicit-any
}

export interface SortableTableProps<T> {
  children: ReactElement<ColumnProps<T>> | Array<ReactElement<ColumnProps<T>>>;
  data: Array<T>;
}

interface State {
  sortColIdx: number | null;
  sortDescending: boolean;
}

export class Column<T> extends React.PureComponent<ColumnProps<T>> {}

export default class SortableTable<T> extends React.Component<SortableTableProps<T>, State> {
  constructor(props: SortableTableProps<T>) {
    super(props);
    this.state = {
      sortColIdx: null,
      sortDescending: false,
    };
  }

  // TODO - memoize sorted data
  // TODO - reset state if column config changes

  render(): JSX.Element {
    const columns = React.Children.toArray(this.props.children) as Array<ReactElement<ColumnProps<T>>>;
    return (
      <table>
        <thead>
          <tr>
            {
              columns.map((col, idx) => {
                const className = (this.state.sortColIdx !== idx) ? "sort-none"
                  : this.state.sortDescending ? "sort-desc" : "sort-asc";

                return (col.props.selector)
                  ? (
                    <th
                      key={idx}
                      className={[className, col.props.className].join(" ")}
                      onClick={() => this.handleHeaderClick(idx)}
                    >
                      {col.props.name}
                    </th>
                  )
                  : <th key={idx} className={col.props.className}>{col.props.name}</th>;
              })
            }
          </tr>
        </thead>
        <tbody>
          {
            this.getSortedData(columns).map((datum, idx) => (
              <tr key={idx}>
                {
                  columns.map((col, idx) => (
                    <td key={idx} className={col.props.className}>{col.props.render(datum)}</td>
                  ))
                }
              </tr>
            ))
          }
        </tbody>
      </table>
    );
  }

  private handleHeaderClick(idx: number): void {
    this.setState(state => ({
      sortColIdx: idx,
      sortDescending: (state.sortColIdx === idx) ? !state.sortDescending : false,
    }));
  }

  private getSortedData(columns: Array<ReactElement<ColumnProps<T>>>): Array<T> {
    const selector = (this.state.sortColIdx === null) ? null : columns[this.state.sortColIdx].props.selector;
    const sortedData = selector ? _.sortBy(this.props.data, selector) : this.props.data;
    return this.state.sortDescending ? sortedData.reverse() : sortedData;
  }
}