import React, { ReactElement } from "react";

export interface ColumnProps<T> {
  name?: string;
  className?: string;
  render: (datum: T) => ReactElement | string | null;
}

export interface SortableTableProps<T> {
  children: ReactElement<ColumnProps<T>> | Array<ReactElement<ColumnProps<T>>>;
  data: Array<T>;
}

export class Column<T> extends React.PureComponent<ColumnProps<T>> {};

export const SortableTable = <T extends {}>(props: SortableTableProps<T>) => {
  const children = React.Children.toArray(props.children) as Array<ReactElement<ColumnProps<T>>>;
  return (
    <table>
      <thead>
        <tr>
          {children.map((col, idx) => <th key={idx} className={col.props.className}>{col.props.name}</th>)}
        </tr>
      </thead>
      <tbody>
        {
          props.data.map((datum, idx) => (
            <tr key={idx}>
              {children.map((col, idx) => <td key={idx} className={col.props.className}>{col.props.render(datum)}</td>)}
            </tr>
          ))
        }
      </tbody>
    </table>
  );
}