import { FRAME_LIMIT } from '@/models/app/pktAnalysis';
import { Pagination, Spin } from 'antd';
import React from 'react';
import type { IIntervalData } from '../typings';
import numeral from 'numeral';
import styles from './index.less';

interface PageProps {
  intervals?: IIntervalData;
  filterIntervals?: IIntervalData;
  loading: boolean;
  currentPage: number;
  onPageChange?: (skip: number) => void;
}

const Page = React.memo(({ filterIntervals, currentPage, loading, onPageChange }: PageProps) => {
  const handleChange = (current: number) => {
    if (onPageChange) {
      onPageChange((current - 1) * FRAME_LIMIT);
    }
  };

  return (
    <div className={styles.pageWrap}>
      <Spin spinning={loading}>
        <Pagination
          size="small"
          total={filterIntervals ? filterIntervals.frames : 0}
          onChange={handleChange}
          pageSize={FRAME_LIMIT}
          current={currentPage}
          showSizeChanger={false}
          showTotal={(total) => `共 ${numeral(total).format('0,0')} 条`}
        />
      </Spin>
    </div>
  );
});

export default Page;
