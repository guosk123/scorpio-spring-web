import { useDebounceFn, useLatest, useMemoizedFn, useSafeState, useThrottleEffect } from 'ahooks';
import { isEmpty } from 'lodash';
import React from 'react';
import { option } from './config';
import ResizableHeader from './ResizableHeader';
import { depthFirstSearch } from './utils';
import { GETKEY } from './utils/useGetDataIndexColumns';
import { useLocalColumns } from './utils/useLocalColumns';

export interface ColumnsState {
  width: number;
}

export interface useTableResizableHeaderProps<
  ColumnType extends ColumnOriginType<ColumnType> = Record<string, any>,
> {
  columns: ColumnType[] | undefined;
  /** @description 最后一列不能拖动，设置最后一列的最小展示宽度，默认120 */
  defaultWidth?: number;
  /** @description 拖动最小宽度 默认0 */
  minConstraints?: number;
  /** @description 拖动最大宽度 默认无穷 */
  maxConstraints?: number;
  /** @description 是否缓存宽度 */
  cache?: boolean;
  /** @description 列状态的配置，可以用来操作列拖拽宽度 */
  persistenceKey?: string;
  /** @description 开始拖拽时触发 */
  onResizeStart?: (col: ColumnType & { resizableColumns: ColumnType[] }) => void;
  /** @description 结束拖拽时触发 */
  onResizeEnd?: (col: ColumnType & { resizableColumns: ColumnType[] }) => void;
}

type Width = number | string;

export interface ColumnOriginType<T> {
  width?: Width;
  dataIndex?: React.Key;
  key?: React.Key;
  title?: React.ReactNode | string;
  children?: T[];
  resizable?: boolean;
  ellipsis?: any;
  hideInTable?: boolean;
  fixed?: 'left' | 'right' | false;
}

interface CacheType {
  width?: Width;
  index: number;
}

const WIDTH = 120;

function useResizableHeader<ColumnType extends ColumnOriginType<ColumnType>>(
  props: useTableResizableHeaderProps<ColumnType>,
) {
  const {
    columns: columnsProp,
    // defaultWidth = WIDTH,
    minConstraints = WIDTH / 2,
    maxConstraints = Infinity,
    cache = true,
    persistenceKey,
    onResizeStart: onResizeStartProp,
    onResizeEnd: onResizeEndProp,
  } = props;

  // column的宽度缓存，避免render导致columns宽度重置
  // add column width cache to avoid column's width reset after render
  const widthCache = React.useRef<Map<React.Key, CacheType>>(new Map());

  const [resizableColumns, setResizableColumns] = useSafeState<ColumnType[]>([]);

  const lastestColumns = useLatest(resizableColumns);

  const { localColumns: columns, resetLocalColumns } = useLocalColumns({
    persistenceKey,
    columns: columnsProp,
    resizableColumns,
  });

  // const [tableWidth, setTableWidth] = useSafeState<number>();

  const [triggerRender, forceRender] = React.useReducer((s) => s + 1, 0);

  const resetColumns = useMemoizedFn(() => {
    widthCache.current = new Map();
    resetLocalColumns();
  });

  const onMount = React.useCallback(
    (id: React.Key | undefined) => (width?: number) => {
      if (width) {
        setResizableColumns((t) => {
          const nextColumns = depthFirstSearch(t, (col) => col[GETKEY] === id, width);

          const kvMap = new Map<React.Key, CacheType>();

          function dig(cols: ColumnType[]) {
            cols.forEach((col, i) => {
              const key = col[GETKEY];
              kvMap.set(key ?? '', { width: col?.width, index: i });
              if (col?.children) {
                dig(col.children);
              }
            });
          }

          dig(nextColumns);

          widthCache.current = kvMap;

          return nextColumns;
        });
      }
    },
    [setResizableColumns],
  );

  const onResize = React.useMemo(() => onMount, [onMount]);

  const onResizeStart = (col: ColumnType) => (width: number) => {
    onResizeStartProp?.({
      ...col,
      width,
      resizableColumns: lastestColumns.current,
    });
  };

  const onResizeEnd = (col: ColumnType) => (width: number) => {
    onResizeEndProp?.({
      ...col,
      width,
      resizableColumns: lastestColumns.current,
    });
  };

  const getColumns = useMemoizedFn((list: ColumnType[]) => {
    const trulyColumns = list?.filter((item) => !isEmpty(item));
    const c = trulyColumns.map((col) => {
      return {
        ...col,
        children: col?.children?.length ? getColumns(col.children) : undefined,
        onHeaderCell: (column: ColumnType) => {
          return {
            title: typeof col?.title === 'string' ? col?.title : '',
            width: cache
              ? widthCache.current?.get(column[GETKEY] ?? '')?.width || column?.width
              : column?.width,
            resizable: column.resizable,
            onMount: onMount(column?.[GETKEY]),
            onResize: onResize(column?.[GETKEY]),
            onResizeStart: onResizeStart(column),
            onResizeEnd: onResizeEnd(column),
            minWidth: minConstraints,
            maxWidth: maxConstraints,
            triggerRender,
          };
        },
        width: cache ? widthCache.current?.get(col[GETKEY] ?? '')?.width || col?.width : col?.width,
        ellipsis: typeof col.ellipsis !== 'undefined' ? col.ellipsis : true,
        [GETKEY]: col[GETKEY] || col.key,
      };
    }) as ColumnType[];

    return c;
  });

  React.useEffect(() => {
    if (columns) {
      const c = getColumns(columns);
      setResizableColumns(c);
    }
  }, [columns, getColumns, setResizableColumns]);

  useThrottleEffect(
    () => {
      const t = getColumns(resizableColumns);
      setResizableColumns(t);
    },
    [triggerRender],
    option,
  );

  // React.useEffect(() => {
  //   let width = 0;

  //   (function loop(cls: ColumnType[]) {
  //     for (let i = 0; i < cls.length; i++) {
  //       if (cls[i].children) {
  //         loop(cls[i].children as ColumnType[]);
  //       } else {
  //         if (!cls[i].hideInTable && cls[i].dataIndex !== EMPTY_DATAINDEX) {
  //           width +=
  //             Number(cls[i].width) || Number(columns?.[columns.length - 1].width) || defaultWidth;
  //         }
  //       }
  //     }
  //   })(resizableColumns);

  //   setTableWidth(width);
  // }, [columns, defaultWidth, resizableColumns, setTableWidth]);

  const { run: debounceRender } = useDebounceFn(forceRender);

  React.useEffect(() => {
    window.addEventListener('resize', debounceRender);
    return () => {
      window.removeEventListener('resize', debounceRender);
    };
  }, [debounceRender]);

  const components = React.useMemo(() => {
    return {
      header: {
        cell: ResizableHeader,
      },
    };
  }, []);

  return {
    resizableColumns,
    components,
    // tableWidth,
    resetColumns,
  };
}

export { useResizableHeader };
