import React, { Fragment, memo, useEffect } from 'react';
import _ from 'lodash';
import VirtualList from 'react-tiny-virtual-list';
import { Spin, message } from 'antd';

import styles from './index.less';

const packetColumns = ['No.', 'Time', 'Source', 'Destination', 'Protocol', 'Length', 'Info'];

const FrameList = ({
  frameList,
  selectedPacket,
  onRowClick,
  height = 270,
  loading,
  detailLoading,
}) => {
  const currentIndex = _.findIndex(frameList, o => o.num === selectedPacket.num);

  const handleEnterKey = e => {
    if (loading) {
      return;
    }
    if (detailLoading) {
      message.warning('请等待加载完成');
      return;
    }

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

  useEffect(
    () => {
      document.addEventListener('keydown', handleEnterKey);
      return () => document.removeEventListener('keydown', handleEnterKey);
    },
    [currentIndex, detailLoading],
  );

  const renderContent = () => {
    // 空
    if (!loading && frameList.length === 0) {
      return <div className={styles.emptyText}>暂无数据</div>;
    }

    return (
      <Fragment>
        <VirtualList
          height={height - 30} // Table表头大约30px
          itemCount={frameList.length}
          itemSize={24}
          overscanCount={50}
          renderItem={({ index, style }) => {
            const row = frameList[index];
            return (
              <ul
                key={row.num}
                onClick={() => (selectedPacket.num === row.num ? null : onRowClick(row))}
                className={`${styles.row} ${styles.dataRow} ${
                  selectedPacket.num === row.num ? styles.selected : ''
                }`}
                style={{ ...style, backgroundColor: `#${row.bg}`, color: `#${row.fg}` }}
              >
                {row.c.map(value => (
                  <li title={value}>{value}</li>
                ))}
              </ul>
            );
          }}
        />
      </Fragment>
    );
  };

  return (
    <div className={styles.packetList}>
      <div className={styles.header}>
        <ul className={styles.row}>
          {packetColumns.map(item => (
            <li>{item}</li>
          ))}
        </ul>
      </div>
      <Spin spinning={loading}>{renderContent()}</Spin>
    </div>
  );
};

export default memo(FrameList);
