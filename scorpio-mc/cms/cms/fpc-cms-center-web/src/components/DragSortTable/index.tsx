import {
  DEFAULT_PAGE_SIZE_KEY,
  getTablePaginationDefaultSettings,
  proTableSerchConfig,
} from '@/common/app';
import { MenuOutlined } from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import type { AlertRenderType } from '@ant-design/pro-table/lib/components/Alert';
import { arrayMoveImmutable, useRefFunction } from '@ant-design/pro-utils';
import type { TableRowSelection } from 'antd/lib/table/interface';
import storage from '@/utils/frame/storage';
import { SortableContainer, SortableElement, SortableHandle } from 'react-sortable-hoc';
import './drag.less';
import numeral from 'numeral';

/** 可拖拽排序的受控ProTable */
export interface IDragSortTableProps<T> {
  /** 渲染列 */
  columns: ProColumns<T>[];
  /** 数据 */
  data: T[];
  /** data设置 */
  setData?: (data: T[]) => void;
  /** 行选择 */
  rowSelection?: TableRowSelection<T>;
  /** 是否可拖拽 */
  shouldRowDragged?: (data: T) => boolean;
  /** 渲染option */
  optionRender?: any;
  /** 渲染勾选后弹出的alert */
  tableAlertRender?: AlertRenderType<T>;
  /** 渲染勾选后弹出的alert的option */
  tableAlertOptionRender?: AlertRenderType<T>;
  /** 拖动结束回调 */
  onGragEnd?: (oldIndex: number, newIndex: number, newData?: T[], oldData?: T[]) => void;
  /** page设置 */
  setPages?: ({ page, pageSize }: { page: number; pageSize: number }) => void;
  /** totalItems */
  totalElements?: number;
  /** 小工具渲染 */
  tableExtraRender?: any;
  /** 是否分页 */
  pagination?: boolean;
}

export const DragHandle = SortableHandle(() => (
  <MenuOutlined style={{ cursor: 'grab', color: '#999' }} />
));

export default function DragSortTable<T extends Record<string, any>>({
  data,
  setData,
  columns,
  rowSelection,
  shouldRowDragged,
  optionRender,
  tableAlertRender,
  tableAlertOptionRender,
  onGragEnd,
  setPages,
  totalElements,
  tableExtraRender,
  pagination = true,
}: IDragSortTableProps<T>) {
  const SortableItem = SortableElement((props: any) => <tr {...props} />);
  const SortContainer = SortableContainer((props: any) => <tbody {...props} />);

  /** ============= 处理拖拽 ============= */
  const onSortEnd = useRefFunction(
    ({ oldIndex, newIndex }: { oldIndex: number; newIndex: number }) => {
      const oldData = [...data];
      if (oldIndex !== newIndex) {
        const newData = arrayMoveImmutable([...data], oldIndex, newIndex).filter((el) => !!el);
        if (setData) {
          setData([...newData]);
        }
        if (onGragEnd) {
          onGragEnd(oldIndex, newIndex, [...newData], [...oldData]);
        }
      }
    },
  );

  const DraggableContainer = (props: any) => (
    <SortContainer
      useDragHandle
      disableAutoscroll
      helperClass="row-dragging"
      onSortEnd={onSortEnd}
      {...props}
    />
  );

  const DraggableBodyRow = (props: any) => {
    const { className, style, ...restProps } = props;
    // function findIndex base on Table rowKey props and should always be a right array index
    const index = data.findIndex((x) => x.id === restProps['data-row-key']);
    if (!shouldRowDragged) {
      return <SortableItem index={index} {...restProps} />;
    }
    return shouldRowDragged!(data[index]) ? (
      <SortableItem index={index} {...restProps} />
    ) : (
      <SortableItem index={undefined} {...restProps} />
    );
  };
  /** =============      ============= */

  return (
    <>
      <ProTable<T>
        columns={columns}
        rowKey="id"
        bordered
        dataSource={[...(data as any)]}
        rowSelection={rowSelection}
        toolBarRender={false}
        search={{
          ...proTableSerchConfig,
          resetText: undefined,
          optionRender: optionRender,
        }}
        tableExtraRender={tableExtraRender}
        tableAlertRender={tableAlertRender}
        tableAlertOptionRender={tableAlertOptionRender}
        components={{
          body: {
            wrapper: DraggableContainer,
            row: DraggableBodyRow,
          },
        }}
        pagination={
          pagination
            ? {
                ...getTablePaginationDefaultSettings(),
                onChange: (page, pageSize) => {
                  storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
                  if (setPages) {
                    setPages({
                      page: page - 1,
                      pageSize,
                    });
                  }
                },
                showTotal: (total) => {
                  return `共 ${numeral(total).format('0,0')} 条`;
                },
                total: totalElements,
              }
            : false
        }
      />
    </>
  );
}
