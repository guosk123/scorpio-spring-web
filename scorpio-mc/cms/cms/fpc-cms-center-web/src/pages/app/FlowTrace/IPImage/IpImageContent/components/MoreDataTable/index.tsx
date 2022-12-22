import { useMemo, useState } from 'react';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import { Table } from 'antd';
import { TableEmpty } from '@/components/EnhancedTable';
import { pageSizeOptions } from '@/common/app';
import type { TableProps } from 'antd';
// import { ESortDirection } from '@/pages/app/Home/typings';
// import { IShowCategory, NetworkLocationType } from '../../../typings';
// import useIpDataTable from '../hooks/useIpdataTable';
import numeral from 'numeral';
import type { ColumnsType } from 'antd/lib/table';

interface IMoreDataTableProps {
  isLoading: boolean;
  columns: ColumnsType<any>;
  dataSource: [];
  onChange?: TableProps<any>['onChange'];
  totalNumber: number;
  page: number;
  pageSize: number;
}

export default function MoreDataTable(props: IMoreDataTableProps) {
  const { isLoading, columns, onChange, dataSource, page, pageSize, totalNumber } = props;
  const [tableHeight, setTableHeight] = useState(0);
  const handleHeightChange = (height: number) => {
    setTableHeight(height);
  };

  const table = useMemo(() => {
    return (
      <Table
        size="small"
        style={{ height: '100%' }}
        loading={isLoading}
        columns={columns}
        dataSource={dataSource}
        onChange={onChange}
        showSorterTooltip={false}
        sortDirections={['ascend', 'descend', 'ascend']}
        pagination={{
          pageSizeOptions,
          current: page,
          pageSize: pageSize,
          showSizeChanger: true,
          total: totalNumber,
          showTotal: (total) => {
            return `共 ${numeral(total).format('0,0')} 条`;
          },
        }}
        scroll={{ x: 'max-content', scrollToFirstRowOnChange: true, y: tableHeight - 100 }}
        locale={{
          emptyText: <TableEmpty componentName="EnhancedTable" height={400 as number} />,
        }}
      />
    );
  }, [columns, dataSource, isLoading, onChange, page, pageSize, tableHeight, totalNumber]);

  return (
    <AutoHeightContainer autoHeight={true} onHeightChange={handleHeightChange} fixHeight={100}>
      {table}
    </AutoHeightContainer>
  );
}
