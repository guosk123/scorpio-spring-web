import React, { useEffect, useMemo } from 'react';
import type { ColumnOriginType } from '..';
import { useGetDataIndexColumns } from './useGetDataIndexColumns';
import { useMemoizedFn } from 'ahooks';
import storage from '@/utils/frame/storage';

interface LocalColumnsProp<T> {
  persistenceKey?: string;
  resizableColumns?: T[];
  columns?: T[];
}

function useLocalColumns<T extends ColumnOriginType<T>>({
  persistenceKey,
  resizableColumns,
  columns,
}: LocalColumnsProp<T>) {
  // 列设置需要每一个column都有dataIndex或key
  const columnsProp = useGetDataIndexColumns(columns);

  // 初始化本地columns
  const initLocalColumns = useMemoizedFn(() => {
    if (!persistenceKey) {
      return columnsProp;
    }
    if (typeof window === 'undefined') return columnsProp;

    try {
      const localResizableColumns = JSON.parse(
        storage?.get(persistenceKey) || '{}',
      )?.resizableColumns;
      const c = columnsProp?.map((col, i) => ({
        ...col,
        width:
          (localResizableColumns as T[])?.find((item, j) => {
            if (col.fixed === 'right') {
              return false;
            }
            if (item.dataIndex && col.dataIndex && item.dataIndex === col.dataIndex) {
              return true;
            }
            if (item.key && col.key && item.key === col.key) {
              return true;
            }
            if (i === j && !col.dataIndex && !col.key) {
              return true;
            }
            return false;
          })?.width || col.width,
      }));
      return c;
    } catch (error) {
      console.error(error);
    }
  });

  const [localColumns, setLocalColumns] = React.useState<T[] | undefined>(initLocalColumns);

  useEffect(() => {
    setLocalColumns(initLocalColumns());
  }, [columnsProp]);

  /**
   * 把resizableColumns存储在本地
   */
  React.useEffect(() => {
    if (!persistenceKey || !resizableColumns?.length) {
      return;
    }
    if (typeof window === 'undefined') return;
    /** 给持久化中设置数据 */

    try {
      storage.put(
        persistenceKey,
        JSON.stringify({
          ...JSON.parse(storage?.get(persistenceKey) || '{}'),
          resizableColumns: resizableColumns.map((col) => ({
            dataIndex: col.dataIndex,
            key: col.key,
            title: col.title,
            width: col.width,
          })),
        }),
      );
    } catch (error) {
      console.error(error);
    }
  }, [resizableColumns]);

  /**
   * reset
   */
  const resetLocalColumns = useMemoizedFn(() => {
    setLocalColumns([...(columnsProp || [])]);
  });

  return {
    localColumns: useMemo(() => localColumns, [localColumns]),
    resetLocalColumns,
  };
}

export { useLocalColumns };
