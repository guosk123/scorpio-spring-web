import { getTablePaginationDefaultSettings } from '@/common/app';
import storage from '@/utils/frame/storage';
import { parseArrayJson } from '@/utils/utils';
import type { TableColumnProps, TablePaginationConfig, TableProps } from 'antd';
import { Space, Table } from 'antd';
import type { ColumnType } from 'antd/lib/table';
import type { Dispatch, ReactNode } from 'react';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import ReactDragListView from 'react-drag-listview';
import type { ResizeCallbackData } from 'react-resizable';
import AutoHeightContainer from '../AutoHeightContainer';
import type { ICheckboxOption } from './components/CheckboxMenu';
import CheckboxMenu from './components/CheckboxMenu';
import ResizableHeaderCell from './components/ResizableHeaderCell';
import { TableEmpty } from './components/TableEmpty';
import styles from './index.less';

interface IEnhancedTableColumnProps<RecordType> extends TableColumnProps<RecordType> {
  /** 原始的 Title 值 */
  originTitle?: string;
  show?: boolean;
}

/** 根据列名字动态计算列的宽度 */
const computedWidth = (title: string) => {
  const DEFAULT_WIDTH = 160;
  if (!title) {
    return DEFAULT_WIDTH;
  }
  const width = title.length * 18 + 20;
  return width > DEFAULT_WIDTH ? width : DEFAULT_WIDTH;
};
export interface IEnhancedTableProps<RecordType = unknown> extends TableProps<RecordType> {
  tableKey: string;
  /** 是否允许自定义列宽度 */
  resizeable?: boolean;
  /** 是否允许拖拽排序 */
  draggable?: boolean;
  /** 是否可以自定义列显示 */
  showColumnTool?: boolean;
  /**
   * 表格是否自动设置高度
   * @description 需要配合 AutoHeight 组件使用
   */
  autoHeight?: boolean;
  /**
   * 表格底部内容的高度
   * @description 主要是分页容器的高度，默认为40px
   */
  buttomHeight?: number;
  /** 额外放在工具栏中的内容 */
  extraTool?: string | ReactNode;
  /** 额外的底部内容 */
  extraFooter?: string | ReactNode;
  // table columns dataIndex[]
  columns: TableColumnProps<RecordType>[];
  // 默认显示的列
  defaultShowColumns?: string[];
  // 改变列的回调
  onColumnChange?: (columns: string[]) => void;

  [key: string]: any;
}

export const ColumnsContext = React.createContext<IEnhancedTableColumnProps<any>[]>([]);
// eslint-disable-next-line @typescript-eslint/ban-types
function EnhancedTable<RecordType extends object = any>({
  tableKey,
  columns = [],
  resizeable = true,
  draggable = true,
  showColumnTool = true,
  autoHeight = true,
  buttomHeight = 40,
  extraTool,
  extraFooter,
  pagination,
  scroll,
  defaultShowColumns,
  onColumnChange,
  ...restProp
}: IEnhancedTableProps<RecordType>) {
  const [tableWrapHeight, setTableWrapperHeight] = useState(300);

  const initAllColumns = useCallback(
    (setColumns: Dispatch<any>) => {
      // 列初始化,
      // 表格重排序
      const nextColumns: IEnhancedTableColumnProps<RecordType>[] = [];
      nextColumns.push(...columns.filter((col) => col.fixed === 'left' || col.fixed === true));
      nextColumns.push(...columns.filter((col) => !col.fixed));
      nextColumns.push(...columns.filter((col) => col.fixed === 'right'));

      // 本地存储的表格信息
      const localColumns: IEnhancedTableColumnProps<RecordType>[] = parseArrayJson(
        storage.get(tableKey) || '',
      );

      nextColumns.forEach((col, index) => {
        nextColumns[index].width = col.width || 200;
        nextColumns[index].show = true;
      });

      const localOrderColumns: IEnhancedTableColumnProps<RecordType>[] = [];

      // 如果表格存在本地缓存信息时，
      if (localColumns.length > 0) {
        // 如果本地的列 与 传入的列有出入，则清空本地缓存
        if (localColumns.length !== nextColumns.length) {
          localOrderColumns.push(...nextColumns);
        } else {
          // 如果localcolumns中不能找到所有传入的列，则重置本地缓存
          // 是否需要重置缓存的标志
          let resetCacheFlag = false;
          nextColumns.forEach((col) => {
            if (!localColumns.find((localCol) => localCol.dataIndex === col.dataIndex)) {
              resetCacheFlag = true;
            }
          });
          // flag为true时，使用传入的列刷新缓存
          if (resetCacheFlag) {
            localOrderColumns.push(...nextColumns);
          } else {
            // flag为false时，使用本地信息填充列。
            localOrderColumns.push(
              ...localColumns.map((col) => {
                // 从nextColumns找当前遍历到的列，
                const findCol = nextColumns.find((nextCol) => nextCol.dataIndex === col.dataIndex);
                return {
                  ...findCol,
                  show: col.show === undefined ? true : col.show,
                  width: col.width || findCol?.width || 200,
                };
              }),
            );
          }
        }
      } else if (defaultShowColumns && defaultShowColumns.length > 0) {
        localOrderColumns.push(
          ...nextColumns.map((item) => {
            if (defaultShowColumns.includes(item.dataIndex as string)) {
              return item;
            }
            return {
              ...item,
              show: false,
            };
          }),
        );
      } else {
        localOrderColumns.push(...nextColumns);
      }

      if (!resizeable && !draggable) {
        return nextColumns;
      }

      // Resize回调.
      // TODO:如何才能将handleResisze提出取，并且editor不会提示function is uesd before it was defined
      const handleResize =
        (dataIndex: string) =>
        (
          e: any,
          data: ResizeCallbackData,
          contextAllColumns: IEnhancedTableColumnProps<RecordType>[],
        ) => {
          const allColumnsCopy = [...contextAllColumns];
          const columnIndex = allColumnsCopy.findIndex((col) => col.dataIndex === dataIndex);
          if (columnIndex !== -1) {
            allColumnsCopy[columnIndex] = {
              ...allColumnsCopy[columnIndex],
              width: data.size.width,
            };

            setColumns(allColumnsCopy);
          }
        };

      // drag包裹层
      return localOrderColumns.map((tableColumn) => {
        const nextColumn: IEnhancedTableColumnProps<RecordType> = {
          ...tableColumn,
          title:
            draggable && !tableColumn.fixed ? (
              <span className="react-draggable-handle">{tableColumn.title}</span>
            ) : (
              tableColumn.title
            ),
          originTitle: tableColumn.title as string,
        };
        // Resize包裹层
        if (resizeable) {
          nextColumn.width = !tableColumn.width
            ? computedWidth(tableColumn.title as string)
            : tableColumn.width;
          nextColumn.onHeaderCell = (column: ColumnType<RecordType>) => {
            const res = {
              width: !column.width ? computedWidth(tableColumn.title as string) : column.width,
              dataIndex: column.dataIndex,
              onResize: handleResize(column.dataIndex as string),
            };
            return res;
          };
        }
        return nextColumn;
      });
    },
    [columns, draggable, defaultShowColumns, resizeable, tableKey],
  );

  const [allColumns, setAllColumns] = useState<IEnhancedTableColumnProps<RecordType>[]>(
    (): IEnhancedTableColumnProps<RecordType>[] => {
      return initAllColumns(setAllColumns);
    },
  );

  // 监听props.column的变化，props.column的变化需要引起state的变化
  useEffect(() => {
    setAllColumns(initAllColumns(setAllColumns));
  }, [columns, draggable, initAllColumns, resizeable, tableKey]);

  // 最终显示的列
  const showColumns = useMemo(() => {
    return allColumns.filter((col) => {
      return col.show === true;
    });
  }, [allColumns]);

  // 左侧固定列的数量
  // 这个缓存的目的是为了支持拖拽时改变列序的计算。
  const fixedLeftColumnsCount = useMemo(() => {
    return columns.filter((col) => col.fixed === 'left').length;
  }, [columns]);

  const fixedRightColumnCount = useMemo(() => {
    return columns.filter((col) => col.fixed === 'right').length;
  }, [columns]);

  /** checkBox中的选项 */
  const checkboxGroupOptions = useMemo<ICheckboxOption[]>(() => {
    return columns.map((col) => {
      return {
        label: (col.title?.toString() as string) || (col.dataIndex as string),
        value: col.dataIndex as string,
        disabled: !!col.fixed,
      };
    });
  }, [columns]);

  /** 自定义显示列中被选中的列 */
  const checked = useMemo(() => {
    if (showColumnTool) {
      return allColumns
        .filter((col) => {
          return col.show === true;
        })
        .map((col) => {
          return col.dataIndex as string;
        });
    }
    return [];
  }, [showColumnTool, allColumns]);

  /** 存在浏览器中的数据 */
  const localCacheSrouce = useMemo(() => {
    if (showColumnTool) {
      return allColumns.map((arg) => ({
        width: arg.width,
        dataIndex: arg.dataIndex,
        show: arg.show,
      }));
    }
    return [];
  }, [allColumns, showColumnTool]);

  useEffect(() => {
    if (showColumnTool) {
      storage.put(tableKey, JSON.stringify(localCacheSrouce));
    }
  }, [tableKey, localCacheSrouce, showColumnTool]);

  // 根据选项更新显示的列
  const checkedUpdate = useCallback(
    (values: string[]) => {
      setAllColumns((prevCols) => {
        const nextCols = prevCols.map((col) => {
          return {
            ...col,
            show: values.includes(col.dataIndex as string),
          };
        });
        onColumnChange?.(
          nextCols.filter((item) => item.show).map((item) => item.dataIndex as string),
        );

        return nextCols;
      });
    },
    [onColumnChange],
  );

  const dragProps = {
    onDragEnd: (fromIndex: number, toIndex: number) => {
      if (
        toIndex < fixedLeftColumnsCount ||
        toIndex > showColumns.length - fixedRightColumnCount - 1
      )
        return;
      const dragColumns = [...showColumns];
      // 拖拽的目标列
      const dragFrom = dragColumns[fromIndex];
      const dragTo = dragColumns[toIndex];
      setAllColumns((prev) => {
        const copy = [...prev];
        const fromColIndex = copy.findIndex((col) => col.dataIndex === dragFrom.dataIndex);
        const toColIndex = copy.findIndex((col) => col.dataIndex === dragTo.dataIndex);
        const indexArr = [fromColIndex, toColIndex].sort();
        const colFromItem = copy[indexArr[1]];
        const res: IEnhancedTableColumnProps<RecordType>[] = [];
        copy.forEach((colItem, index) => {
          if (index === indexArr[0]) {
            res.push(colFromItem);
          }
          if (index === indexArr[1]) {
            return;
          }
          res.push(colItem);
        });
        return res;
      });
    },
    nodeSelector: 'th.ant-table-cell',
    handleSelector: 'span.react-draggable-handle',
    // ignoreSelector: '.ant-table-cell-fix-left',
  };

  const components = {
    header: {
      cell: ResizableHeaderCell,
    },
  };

  const tableScroll = useMemo(() => {
    let temp = {
      scrollToFirstRowOnChange: true,
      x: 'max-content',
    } as TableProps<RecordType>['scroll'] as any;

    if (autoHeight) {
      // 内容高度 - 表格头部高度（40） - 底部内容区域（分页区域高度）(40)
      temp.y =
        tableWrapHeight -
        // 表格头部高度（40）
        40 -
        // 没有分页时就不再预留分页的空间
        ((pagination === false || !restProp.dataSource || restProp.dataSource.length === 0) &&
        !extraFooter
          ? 0
          : buttomHeight);
    }
    if (scroll) {
      temp = {
        ...temp,
        ...scroll,
      };
    }

    return temp;
  }, [
    autoHeight,
    scroll,
    tableWrapHeight,
    pagination,
    restProp.dataSource,
    extraFooter,
    buttomHeight,
  ]);

  const EnhancedTableTools = useMemo(() => {
    if (showColumnTool) {
      return (
        <>
          <Space>
            {showColumnTool && (
              <CheckboxMenu
                options={checkboxGroupOptions}
                value={checked}
                onChange={checkedUpdate}
              />
            )}
          </Space>
        </>
      );
    }
    return null;
  }, [showColumnTool, checkboxGroupOptions, checked, checkedUpdate]);

  const tablePagination: false | TablePaginationConfig | undefined = useMemo(() => {
    if (pagination === undefined) {
      return getTablePaginationDefaultSettings();
    }
    if (pagination === false) {
      return false;
    }
    return {
      ...getTablePaginationDefaultSettings(),
      ...pagination,
    };
  }, [pagination]);

  return (
    <ColumnsContext.Provider value={allColumns}>
      <div className={styles.enhancedTable}>
        <AutoHeightContainer
          autoHeight={autoHeight}
          headerRender={
            <div className={styles.toolWrap} style={{ marginTop: 3 }}>
              {/* 存在外部的元素 */}
              <div className={styles.extra}>{extraTool}</div>
              {/* 默认的内容 */}
              <div>{EnhancedTableTools}</div>
            </div>
          }
          onHeightChange={(h) => {
            setTableWrapperHeight(h);
          }}
        >
          <ReactDragListView.DragColumn {...dragProps}>
            <Table<RecordType>
              components={resizeable ? components : {}}
              pagination={tablePagination}
              {...restProp}
              dataSource={restProp.loading ? [] : restProp.dataSource}
              size="small"
              bordered={true}
              columns={showColumns}
              showSorterTooltip={false}
              sortDirections={['ascend', 'descend', 'ascend']}
              scroll={tableScroll}
              locale={{
                emptyText: <TableEmpty height={tableScroll.y as number} />,
              }}
            />
          </ReactDragListView.DragColumn>
          {extraFooter}
        </AutoHeightContainer>
      </div>
    </ColumnsContext.Provider>
  );
}

export default EnhancedTable;
