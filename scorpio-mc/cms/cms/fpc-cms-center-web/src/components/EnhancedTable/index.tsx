import { getTablePaginationDefaultSettings } from '@/common/app';
import storage from '@/utils/frame/storage';
import { parseArrayJson } from '@/utils/utils';
import { useLatest, useSafeState, useSize } from 'ahooks';
import type { TablePaginationConfig, TableProps } from 'antd';
import { Space, Spin, Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import classNames from 'classnames';
import type { ReactNode } from 'react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import ReactDragListView from 'react-drag-listview';
import AutoHeightContainer from '../AutoHeightContainer';
import type { ICheckboxOption } from './components/CheckboxMenu';
import CheckboxMenu from './components/CheckboxMenu';
import { TableEmpty } from './components/TableEmpty';
import type { ColumnOriginType } from './hooks';
import { useResizableHeader } from './hooks';
import styles from './index.less';

export { TableEmpty };

export const EMPTY_DATAINDEX = '__empty';
export interface IEnhancedTableProps<RecordType = unknown> extends TableProps<RecordType> {
  tableKey: string;
  /** 是否允许调整列宽 */
  resizable?: boolean;
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
  columns: ColumnProps<RecordType>[];
  // 默认显示的列
  defaultShowColumns?: string[];
  // 改变列的回调
  onColumnChange?: (columns: string[]) => void;

  [key: string]: any;

  fixHeight?: number;
}

type ColumnOrderCache = {
  dataIndex: string;
  hideInTable: boolean;
};

type ColumnType<RecordType> = ColumnOriginType<RecordType> & {
  fixed?: true | 'left' | 'right';
};

/** 根据列名字动态计算列的宽度 */
const computedWidth = (title: string) => {
  const DEFAULT_WIDTH = 160;
  if (!title) {
    return DEFAULT_WIDTH;
  }
  const width = title.length * 18 + 20;
  return width > DEFAULT_WIDTH ? width : DEFAULT_WIDTH;
};

function EnhancedTable<RecordType extends Record<string, any>>(
  props: IEnhancedTableProps<RecordType>,
) {
  const {
    columns: columnsProp,
    tableKey,
    autoHeight = true,
    showColumnTool = true,
    extraTool,
    resizable = true,
    draggable = true,
    pagination,
    loading,
    dataSource,
    extraFooter,
    buttomHeight = 40,
    defaultShowColumns,
    onColumnChange,
    scroll,
    fixHeight,
    ...restProps
  } = props;
  const [tableWrapHeight, setTableWrapperHeight] = useState(300);
  const [ready, setReady] = useState(false);

  const defaultColsRef = useLatest(defaultShowColumns);

  const [tableWidth, setTableWidth] = useSafeState(0);

  const tableContainerRef = useRef<HTMLDivElement>(null);
  const containerSize = useSize(tableContainerRef.current);

  const initColumns = useMemo(() => {
    const nextColumns: ColumnType<RecordType>[] = [];
    nextColumns.push(
      ...(columnsProp.filter(
        (col) => col.fixed === 'left' || col.fixed === true,
      ) as ColumnType<RecordType>[]),
    );
    nextColumns.push(...(columnsProp.filter((col) => !col.fixed) as ColumnType<RecordType>[]));
    nextColumns.push(
      ...(columnsProp.filter((col) => col.fixed === 'right') as ColumnType<RecordType>[]),
    );

    // 空列不计算列宽
    nextColumns.forEach((col, index) => {
      if (col.fixed) return;
      nextColumns[index].width =
        col.width || (typeof col.title === 'string' ? computedWidth(col.title) : 200);

      nextColumns[index].hideInTable = false;
    });

    const localColumns = parseArrayJson(
      storage.get(`${tableKey}-order`) || '[]',
    ) as ColumnOrderCache[];

    const localOrderColumns: ColumnType<RecordType>[] = [];

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
                hideInTable: col.hideInTable,
              };
            }),
          );
        }
      }
    } else if (defaultColsRef.current && defaultColsRef.current.length > 0) {
      localOrderColumns.push(
        ...nextColumns.map((item) => {
          if (defaultColsRef.current?.includes(item.dataIndex as string)) {
            return item;
          }
          return {
            ...item,
            hideInTable: true,
          };
        }),
      );
      storage.put(
        `${tableKey}-order`,
        JSON.stringify(
          localOrderColumns.map((item) => {
            return {
              dataIndex: item.dataIndex,
              hideInTable: item.hideInTable,
            };
          }),
        ),
      );
    } else {
      localOrderColumns.push(...nextColumns);
      storage.put(
        `${tableKey}-order`,
        JSON.stringify(
          localOrderColumns.map((item) => {
            return {
              dataIndex: item.dataIndex,
              hideInTable: item.hideInTable,
            };
          }),
        ),
      );
    }

    return localOrderColumns;
    // count：当用户显隐某些列时，重新计算列
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [columnsProp, tableKey]);

  const { components, resizableColumns } = useResizableHeader({
    persistenceKey: `${tableKey}-width`,
    columns: initColumns,
    minConstraints: 30,
  });

  // 这里不建议使用空数组初始化，
  // 因为allColumns的值为undefined代表初始状态，这里那resizableColumns初始化是没有用的，应为resizableColumns是一个state,初始值为[],
  // allColumns值为undefined，还代表了尚未使用初始化好的resizableColumns初始化allColumns
  // 同时debug时也能更加方便调试
  const [allColumns, setAllColumns] = useState<ColumnType<RecordType>[]>();

  useEffect(() => {
    if (resizableColumns.length < 1) {
      return;
    }
    const resizableCols = resizableColumns.map((col) => {
      return {
        ...col,
        align: col.dataIndex !== 'action' ? 'left' : 'center',
        title:
          draggable && !col.fixed ? (
            <span className="react-draggable-handle">{col.title}</span>
          ) : (
            col.title
          ),
      };
    });
    // 更新时，使用allColumns中的hideInTable,否则直接使用resizableCols更新会导致有些列被屏蔽，出现问题
    setAllColumns((prev) => {
      if (prev === undefined) return resizableCols;
      // 如果更新之前有数据
      // 则同一个dataIndex的hideInTable字段使用allColumns
      if (prev.length > 0) {
        // 找出不在pre中的列
        const colsNotInPre = resizableCols.filter(
          (item) => !prev.find((prevCol) => prevCol.dataIndex === item.dataIndex),
        );

        // 返回时，携带不在当前cols中的列
        return [
          ...prev
            .filter((item) => resizableCols.find((col) => col.dataIndex === item.dataIndex))
            .map((item) => {
              const resizableCol = resizableCols.find((col) => col.dataIndex === item.dataIndex);

              return {
                ...resizableCol,
                hideInTable: item.hideInTable,
              };
            }),
          ...colsNotInPre,
        ];
      }
      return resizableCols;
    });
  }, [draggable, resizableColumns]);

  // 计算需要的表格宽度
  useEffect(() => {
    let width = 0;

    (function loop(cls: ColumnType<RecordType>[]) {
      for (let i = 0; i < cls.length; i++) {
        if (!cls[i].hideInTable && cls[i].dataIndex !== EMPTY_DATAINDEX) {
          width += Number(cls[i].width) || 120;
        }
      }
    })(allColumns || []);

    setTableWidth(width);
  }, [allColumns, setTableWidth]);

  const showColumns = useMemo(() => {
    if (allColumns === undefined) return [];

    const result = allColumns.filter((item) => {
      return !item.hideInTable;
    });

    //  当列数较多的时候，不显示空列
    if (tableWidth && containerSize?.width && tableWidth < containerSize.width) {
      const lastScrollColumnIndex = result.findIndex((item) => item.fixed === 'right');
      const emptyColumn = {
        dataIndex: EMPTY_DATAINDEX,
        title: '',
      };
      if (lastScrollColumnIndex === -1) {
        result.splice(result.length, 0, emptyColumn);
      } else {
        result.splice(lastScrollColumnIndex, 0, emptyColumn);
      }
    }

    return result;
  }, [allColumns, containerSize?.width, tableWidth]);

  /** 存在浏览器中的数据 */
  const localCacheSrouce = useMemo(() => {
    if (showColumnTool && allColumns) {
      return allColumns.map((arg) => ({
        dataIndex: arg.dataIndex,
        hideInTable: arg.hideInTable,
      }));
    }
    return [];
  }, [allColumns, showColumnTool]);

  useEffect(() => {
    if (showColumnTool && localCacheSrouce.length > 0) {
      storage.put(`${tableKey}-order`, JSON.stringify(localCacheSrouce));
    }
  }, [tableKey, localCacheSrouce, showColumnTool]);

  /** checkBox中的选项 */
  const checkboxGroupOptions = useMemo<ICheckboxOption[]>(() => {
    return columnsProp.map((col) => {
      return {
        label: (col.title?.toString() as string) || (col.dataIndex as string),
        value: col.dataIndex as string,
        disabled: !!col.fixed,
      };
    });
  }, [columnsProp]);

  /** 自定义显示列中被选中的列 */
  const checked = useMemo(() => {
    if (showColumnTool && allColumns) {
      return allColumns
        .filter((col) => {
          return !col.hideInTable && col.dataIndex !== EMPTY_DATAINDEX;
        })
        .map((col) => {
          return col.dataIndex as string;
        });
    }
    return [];
  }, [showColumnTool, allColumns]);

  // 左侧固定列的数量
  // 这个缓存的目的是为了支持拖拽时改变列序的计算。
  const fixedLeftColumnsCount = useMemo(() => {
    return columnsProp.filter((col) => col.fixed === 'left').length;
  }, [columnsProp]);

  const fixedRightColumnCount = useMemo(() => {
    return columnsProp.filter((col) => col.fixed === 'right').length;
  }, [columnsProp]);

  const dragProps = useMemo(
    () => ({
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
          const copy = [...prev!];
          const fromColIndex = copy.findIndex((col) => col.dataIndex === dragFrom.dataIndex);
          const toColIndex = copy.findIndex((col) => col.dataIndex === dragTo.dataIndex);
          const fromCol = copy.splice(fromColIndex, 1);
          if (fromCol) {
            copy.splice(toColIndex, 0, ...fromCol);
          }

          // const tmp = copy[fromColIndex];
          // copy[fromColIndex] = copy[toColIndex];
          // copy[toColIndex] = tmp;
          return copy;
        });
      },
      nodeSelector: 'th.ant-table-cell',
      handleSelector: 'span.react-draggable-handle',
      // ignoreSelector: '.ant-table-cell-fix-left',
    }),
    [fixedLeftColumnsCount, fixedRightColumnCount, showColumns],
  );

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

  const tableScroll = useMemo(() => {
    const temp = {
      scrollToFirstRowOnChange: true,
      x:
        tableWidth && containerSize?.width && tableWidth > containerSize.width
          ? tableWidth
          : undefined,
    } as TableProps<RecordType>['scroll'] as any;

    if (autoHeight) {
      const bodyMaxHeight =
        tableWrapHeight -
        // 表格头部高度（40）
        40 -
        // 没有分页时就不再预留分页的空间
        ((pagination === false || !dataSource || dataSource.length === 0) && !extraFooter
          ? 0
          : buttomHeight);
      // 内容高度 - 表格头部高度（40） - 底部内容区域（分页区域高度）(40)
      if ((dataSource?.length || 0) * 39 > bodyMaxHeight) {
        temp.y = bodyMaxHeight;
      }
    }
    if (scroll) {
      return {
        ...temp,
        ...scroll,
      };
    }

    return temp;
  }, [
    tableWidth,
    containerSize?.width,
    autoHeight,
    scroll,
    tableWrapHeight,
    pagination,
    dataSource,
    extraFooter,
    buttomHeight,
  ]);

  // 根据选项更新显示的列
  const checkedUpdate = useCallback(
    (values: string[]) => {
      const tmpCols: any[] = [];
      setAllColumns((prevCols) => {
        const nextCols = prevCols!.map((col) => {
          if (col.dataIndex === EMPTY_DATAINDEX) {
            return {
              ...col,
              hideInTable: false,
            };
          }
          return {
            ...col,
            hideInTable: !values.includes(col.dataIndex as string),
          };
        });

        tmpCols.push(...nextCols);
        return nextCols;
      });

      onColumnChange?.(
        tmpCols.filter((item) => !item.hideInTable).map((item) => item.dataIndex as string),
      );
    },
    [onColumnChange],
  );

  useEffect(() => {
    if (allColumns !== undefined && ready === false) {
      setReady(true);
      onColumnChange?.(checked);
    }
  }, [allColumns, checked, onColumnChange, ready]);

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
                resetValues={defaultColsRef}
              />
            )}
          </Space>
        </>
      );
    }
    return null;
  }, [showColumnTool, checkboxGroupOptions, checked, checkedUpdate, defaultColsRef]);

  if (!ready || resizableColumns.length < 1) {
    return (
      <div className="center">
        <Spin />
      </div>
    );
  }

  return (
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
      fixHeight={fixHeight ?? 0}
    >
      <div className={classNames({ [styles.enhancedTable]: autoHeight })} ref={tableContainerRef}>
        <ReactDragListView.DragColumn {...dragProps}>
          <Table<RecordType>
            {...restProps}
            components={resizable ? components : {}}
            pagination={tablePagination}
            dataSource={loading ? [] : dataSource}
            loading={loading}
            size="small"
            bordered={true}
            columns={showColumns}
            showSorterTooltip={false}
            sortDirections={['ascend', 'descend', 'ascend']}
            scroll={tableScroll}
            locale={{
              emptyText: <TableEmpty height={'100%'} />,
            }}
          />
        </ReactDragListView.DragColumn>
        {extraFooter && <div className={styles.footer}>{extraFooter}</div>}
      </div>
    </AutoHeightContainer>
  );
}

export default EnhancedTable;
