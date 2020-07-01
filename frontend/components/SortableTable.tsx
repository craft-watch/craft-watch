import React, { ReactElement, useState } from "react";
import _ from "underscore";

export type Renderer<T> = (datum: T) => JSX.Element | string | null;

interface ColumnProps<T> {
  name?: JSX.Element | string;
  className?: string;
  render: Renderer<T>;
  selector?: (datum: T) => any; // eslint-disable-line @typescript-eslint/no-explicit-any
}

interface Props<T> {
  children: ReactElement<ColumnProps<T>> | Array<ReactElement<ColumnProps<T>>>;
  data: Array<T>;
}

export class Column<T> extends React.PureComponent<ColumnProps<T>> {}

const SortableTable = <T extends {}>(props: Props<T>): JSX.Element => {
  const [sortColIdx, setSortColIdx] = useState<number | null>(null);
  const [sortDescending, setSortDescending] = useState<boolean>(false);

  // TODO - useCallback?
  const handleHeaderClick = (idx: number) => {
    setSortColIdx(idx);
    setSortDescending((sortColIdx === idx) ? !sortDescending : false);
  };

  const columns = React.Children.toArray(props.children) as Array<ReactElement<ColumnProps<T>>>;

  const selector = (sortColIdx === null) ? null : columns[sortColIdx].props.selector;
  const sortedData = selector ? _.sortBy(props.data, selector) : props.data;
  const sortedData2 = sortDescending ? sortedData.reverse() : sortedData;

  return (
    <table>
      <thead>
        <tr>
          {
            columns.map((col, idx) => {
              const className = (sortColIdx !== idx) ? "sort-none" : sortDescending ? "sort-desc" : "sort-asc";

              return (col.props.selector)
                ? (
                  <th
                    key={idx}
                    className={[className, col.props.className].join(" ")}
                    onClick={() => handleHeaderClick(idx)}
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
          sortedData2.map((datum, idx) => (
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
};

export default SortableTable;
