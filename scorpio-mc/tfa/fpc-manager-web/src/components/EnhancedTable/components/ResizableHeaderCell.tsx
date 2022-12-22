import type { TableColumnProps } from 'antd';
import classNames from 'classnames';
import type { ReactNode, SyntheticEvent } from 'react';
import { useContext, useState } from 'react';
import type { ResizeCallbackData } from 'react-resizable';
import { Resizable } from 'react-resizable';
import { ColumnsContext } from '../index.backup';
import styles from '../index.less';

interface IResizableHeaderCellProps {
  width: number;
  dataIndex: string;
  onResize?: (
    e: SyntheticEvent,
    data: ResizeCallbackData,
    columns: TableColumnProps<any>[],
  ) => void;
  children?: ReactNode;
}

const ResizableHeaderCell: React.FC<IResizableHeaderCellProps> = (props) => {
  const { onResize, width, dataIndex, children, ...restProps } = props;
  const columns = useContext(ColumnsContext);
  const [offset, setOffset] = useState<number>(0);

  if (!width) {
    return <th {...restProps}>{children}</th>;
  }

  const handle = (
    <span
      className={classNames(['react-resizable-handle', styles.resizableHandle, offset && 'active'])}
      style={{ transform: `translateX(${offset}px)` }}
      onClick={(e) => {
        e.stopPropagation();
      }}
    />
  );

  const handleResize = (e: SyntheticEvent, data: ResizeCallbackData) => {
    setOffset(data.size.width - width);
  };

  const onResizeStop = (e: SyntheticEvent, data: ResizeCallbackData) => {
    setOffset(0);
    if (onResize) {
      onResize(e, data, columns);
    }
  };

  return (
    <Resizable
      width={width + offset}
      height={0}
      handle={handle}
      draggableOpts={{ enableUserSelectHack: false }}
      onResize={handleResize}
      onResizeStop={onResizeStop}
    >
      <th {...restProps}>{children}</th>
    </Resizable>
  );
};

export default ResizableHeaderCell;
