import type { ColumnsType } from 'antd/lib/table';
import type { TableProps } from 'antd';
import { Table } from 'antd';
import styles from '../../index.less';
import { TableEmpty } from '@/components/EnhancedTable';
// import storage from '@/utils/frame/storage';
// import type { PaginationProps } from 'antd';
import numeral from 'numeral';
import { pageSizeOptions } from '@/common/app';
import { useMemo, useState } from 'react';
import AutoHeightContainer from '@/components/AutoHeightContainer';
interface Props {
  columns: ColumnsType<any>;
  dataSource: [];
  onChange?: TableProps<any>['onChange'];
  totalNumber: number;
  // changeTop?: any;
  isfull?: boolean;
  page: number;
  pageSize: number;
}

export default function SmallTable(props: Props) {
  const { columns, dataSource, onChange, isfull = false, totalNumber, page, pageSize } = props;
  const [tableHeight, setTableHeight] = useState(0);
  const handleHeightChange = (height: number) => {
    console.log('height', height);
    setTableHeight(height);
  };
  const currentScroll = useMemo(() => {
    if (isfull) {
      return { x: 'max-content', scrollToFirstRowOnChange: true, y: tableHeight - 100 };
    }
    return { x: 'max-content', scrollToFirstRowOnChange: true };
  }, [isfull, tableHeight]);

  const table = useMemo(() => {
    return (
      <Table
        className={styles.smallTable}
        size="small"
        style={{ height: '100%' }}
        columns={columns}
        dataSource={dataSource}
        onChange={onChange}
        showSorterTooltip={false}
        sortDirections={['ascend', 'descend', 'ascend']}
        pagination={
          isfull
            ? {
                pageSizeOptions,
                current: page,
                pageSize: pageSize,
                showSizeChanger: true,
                total: totalNumber,
                showTotal: (total) => {
                  return `共 ${numeral(total).format('0,0')} 条`;
                },
              }
            : false
        }
        scroll={currentScroll}
        locale={{
          emptyText: <TableEmpty componentName="smallTable" height={380 as number} />,
        }}
      />
    );
  }, [columns, currentScroll, dataSource, isfull, onChange, page, pageSize, totalNumber]);
  return (
    <>
      {isfull ? (
        <AutoHeightContainer autoHeight={true} onHeightChange={handleHeightChange}>
          {table}
        </AutoHeightContainer>
      ) : (
        table
      )}
    </>
  );
}
