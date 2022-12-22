import { message, Spin } from 'antd';
import _ from 'lodash';
import React, { memo, useEffect } from 'react';
import BaseTable, { AutoResizer, unflatten } from 'react-base-table';
import 'react-base-table/styles.css';
import type { IFrameData } from '../typings';
import styles from './index.less';

export const tableColumns = [
  {
    key: 'No.',
    title: 'No.',
    dataKey: 'No.',
    width: 70,
    minWidth: 70,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[0],
  },
  {
    key: 'Time',
    title: 'Time',
    dataKey: 'Time',
    width: 250,
    minWidth: 120,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[1],
  },
  {
    key: 'Source',
    title: 'Source',
    dataKey: 'Source',
    width: 150,
    minWidth: 150,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[2],
  },
  {
    key: 'Destination',
    title: 'Destination',
    dataKey: 'Destination',
    width: 150,
    minWidth: 150,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[3],
  },
  {
    key: 'Protocol',
    title: 'Protocol',
    dataKey: 'Protocol',
    width: 100,
    minWidth: 100,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[4],
  },
  {
    key: 'Length',
    title: 'Length',
    dataKey: 'Length',
    width: 100,
    minWidth: 100,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[5],
  },
  {
    key: 'Info',
    title: 'Info',
    dataKey: 'Info',
    minWidth: 200,
    width: 3000,
    resizable: true,
    cellRenderer: ({ rowData }: { rowData: IFrameData }) => rowData.c[6],
  },
];

const FrameList = ({
  frameList,
  selectedPacket,
  onRowClick,
  height = 270,
  loading,
  detailLoading,
}: {
  frameList: IFrameData[];
  selectedPacket: Partial<IFrameData>;
  onRowClick: (packet: IFrameData) => void;
  height: number;
  detailLoading: boolean;
  loading: boolean;
}) => {
  const currentIndex = _.findIndex(frameList, (o) => o.num === selectedPacket.num);

  const handleEnterKey = (e: KeyboardEvent) => {
    if (loading) {
      return;
    }
    if (detailLoading) {
      message.warning('请等待加载完成');
      return;
    }

    // TODO: 已被启用，未来某个时间要调整成 KeyboardEvent.code
    // @see: https://developer.mozilla.org/zh-CN/docs/Web/API/KeyboardEvent/code
    switch (e.keyCode) {
      case 38: // 向上键
        if (currentIndex > 0) {
          onRowClick(frameList[currentIndex - 1]);
        }
        break;
      case 40: // 向下键
        if (currentIndex < frameList.length - 1) {
          onRowClick(frameList[currentIndex + 1]);
        }
        break;
      default:
        break;
    }
  };

  useEffect(() => {
    document.addEventListener('keydown', handleEnterKey);
    return () => document.removeEventListener('keydown', handleEnterKey);
  }, [currentIndex, detailLoading]);

  return (
    <div className={styles.tableWrap} style={{ minHeight: 500 }}>
      <AutoResizer>
        {({ width }) => (
          <BaseTable
            headerHeight={26}
            rowHeight={26}
            width={width}
            height={height}
            fixed
            columns={tableColumns}
            rowRenderer={({ rowData, cells }) => {
              return cells.map((cell) =>
                // @ts-ignore
                React.cloneElement(cell, {
                  style: {
                    // @ts-ignore
                    ...cell.props.style,
                    backgroundColor: `#${rowData.bg}`,
                    color: `#${rowData.fg}`,
                  },
                }),
              );
            }}
            rowEventHandlers={{
              onClick: ({ rowData }: { rowData: IFrameData }) =>
                selectedPacket.num === rowData.num ? null : onRowClick(rowData),
            }}
            rowClassName={({ rowData }: { rowData: IFrameData }) =>
              `${styles.row} ${selectedPacket.num === rowData.num ? styles.selected : ''}`
            }
            rowProps={({ rowData }) => {
              return {
                // @ts-ignore
                ...rowData.props,
                style: {
                  backgroundColor: `#${rowData.bg}`,
                  color: `#${rowData.fg}`,
                },
              };
            }}
            data={unflatten(frameList)}
            overlayRenderer={() => {
              if (loading) {
                return (
                  <div className={styles.loading}>
                    <Spin spinning={loading} />
                  </div>
                );
              }
              return null;
            }}
            emptyRenderer={() => {
              if (loading) {
                return null;
              }
              return <div className={styles.emptey}>暂无数据</div>;
            }}
          />
        )}
      </AutoResizer>
    </div>
  );
};

export default memo(FrameList);
