import { DEFAULT_PAGE_SIZE_KEY, pageSizeOptions } from '@/common/app';
import storage from '@/utils/frame/storage';
import { LeftOutlined, RightOutlined, StepBackwardOutlined } from '@ant-design/icons';
import { Select } from 'antd';
import numeral from 'numeral';
import React, { memo, useEffect, useState } from 'react';
import styles from './index.less';

interface CustomPaginationProps {
  currentPage: number;
  pageSize: number;
  total: number;
  /**
   * 当前页的记录数
   */
  onChange: (currentPage: number, pageSize: number) => void;
  loading?: boolean;
}

const CustomPagination: React.FC<CustomPaginationProps> = ({
  currentPage = 1,
  pageSize,
  total = 0,
  onChange,
  loading = false,
}) => {
  // 存在总页数，就以页数来计算是否可以进行翻页
  // 不存在总页数时，就以当前页的记录数 < 每页的最大的记录数来判断
  const nextDisabled = currentPage >= total / pageSize;
  const prevDisabled = currentPage === 1;

  const [currentPageSize, setCurrentPageSize] = useState<number>(pageSize);

  useEffect(() => {
    setCurrentPageSize(pageSize);
  }, [pageSize]);

  const onPageSizeChange = (selectPageSize: number) => {
    setCurrentPageSize(selectPageSize);
    onChange(1, selectPageSize);
    storage.put(DEFAULT_PAGE_SIZE_KEY, selectPageSize);
  };

  // return (
  //   <Pagination
  //     size="small"
  //     hideOnSinglePage={false}
  //     showSizeChanger={true}
  //     showQuickJumper={false}
  //     current={currentPage}
  //     total={total}
  //     showLessItems
  //     showTotal={(total) => `共 ${total} 条`}
  //     pageSize={pageSize}
  //     itemRender={(current, type, originalElement) => {
  //       if (type === 'prev') {
  //         return originalElement;
  //       }
  //       if (type === 'next') {
  //         return originalElement;
  //       }
  //       if (current === currentPage) {
  //         return originalElement;
  //       }
  //       return null;
  //     }}
  //   />
  // );

  return (
    <div>
      <ul className={styles.pagination}>
        {(!loading || total > 0) && <li>共 {numeral(total).format('0,0')} 条</li>}
        <li
          onClick={() => {
            if (!prevDisabled) {
              onChange(1, pageSize);
            }
          }}
          className={`${styles.firstPage} ${prevDisabled ? styles.disabled : ''}`}
        >
          <StepBackwardOutlined />
          返回首页
        </li>
        <li
          onClick={() => {
            if (!prevDisabled) {
              onChange(currentPage - 1, pageSize);
            }
          }}
          className={`${styles.prev} ${prevDisabled ? styles.disabled : ''}`}
        >
          <LeftOutlined />
          {/* &nbsp;上一页 */}
        </li>
        <li className={styles.current}>{currentPage}</li>
        <li
          className={`${styles.next} ${nextDisabled ? styles.disabled : ''}`}
          onClick={() => {
            if (!nextDisabled) {
              onChange(currentPage + 1, pageSize);
            }
          }}
        >
          {/* 下一页&nbsp; */}
          <RightOutlined />
        </li>
        <li>
          <Select size="small" value={currentPageSize} onChange={onPageSizeChange}>
            {pageSizeOptions.map((size) => (
              <Select.Option key={size} value={+size}>
                {size}条/页
              </Select.Option>
            ))}
          </Select>
          {/* 共 {numeral(totalPages).format('0,0')} 页 */}
        </li>
      </ul>
    </div>
  );
};

export default memo(CustomPagination);
