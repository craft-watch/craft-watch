import React, { ReactElement, useState, ComponentType } from "react";
import _ from "lodash";
import styles from "./styles.module.css";
import classNames from "classnames";


interface ColumnProps<T> {
  name?: JSX.Element | string;
  className?: string;
  render: ComponentType<CellProps<T>>;
  selector?: (datum: T) => any; // eslint-disable-line @typescript-eslint/no-explicit-any
}

export interface CellProps<T> {
  datum: T;
}

export interface Section<T> {
  name: string;
  data: Array<T>;
}

interface Props<T> {
  children: ReactElement<ColumnProps<T>> | Array<ReactElement<ColumnProps<T>>>;
  sections: Array<Section<T>>;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const Column = <T extends unknown>(_: ColumnProps<T>): null => null;

const SortableTable = <T extends unknown>(props: Props<T>): JSX.Element => {
  const [sortColIdx, setSortColIdx] = useState<number | null>(null);
  const [sortDescending, setSortDescending] = useState<boolean>(false);

  // TODO - useCallback?
  const handleHeaderClick = (idx: number) => {
    setSortColIdx(idx);
    setSortDescending(b => (sortColIdx === idx) ? !b : false);
  };

  const columns = React.Children.toArray(props.children) as Array<ReactElement<ColumnProps<T>>>;
  const selector = (sortColIdx === null) ? null : columns[sortColIdx].props.selector;

  const showHeader = _.size(props.sections) > 1;

  return (
    <table className={styles.sortable}>
      <thead>
        <tr>
          {
            columns.map((col, idx) => {
              const className = (sortColIdx !== idx) ? undefined : sortDescending ? styles.desc : styles.asc;

              return (col.props.selector)
                ? (
                  <th
                    key={idx}
                    className={classNames(styles.sort, className, col.props.className)}
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
      {
        _.map(props.sections, section => {
          const sorted = selector ? _.sortBy(section.data, selector) : section.data;
          const maybeReversed = sortDescending ? sorted.reverse() : sorted;

          return _.isEmpty(section.data) || (
            <tbody key={section.name}>
              {
                showHeader && (
                  <tr>
                    <th colSpan={_.size(columns)}>
                      <div className={styles.section}>{section.name}</div>
                    </th>
                  </tr>
                )
              }
              {
                maybeReversed.map((datum, idx) => (
                  <tr key={idx}>
                    {
                      columns.map((col, idx) => (
                        <td key={idx} className={col.props.className}>
                          <col.props.render datum={datum} />
                        </td>
                      ))
                    }
                  </tr>
                ))
              }
            </tbody>
          );
        })
      }
    </table>
  );
};

export default SortableTable;
