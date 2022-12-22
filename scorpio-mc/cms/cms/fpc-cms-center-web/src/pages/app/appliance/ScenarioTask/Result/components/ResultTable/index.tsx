import { getTablePaginationDefaultSettings } from '@/common/app';
import type { PaginationProps, TableColumnProps, TableProps } from 'antd';
import { Table } from 'antd';
import styles from './index.less';

interface Props<T> extends TableColumnProps<T> {
  loading: boolean | undefined;
  columns: TableColumnProps<T>[];
  dataSource: T[];
  pagination: PaginationProps;
  onTableChange: TableProps<T>['onChange'];
}

function ResultTable<RecordType extends Record<string, any>>(props: Props<RecordType>) {
  const { loading, columns, dataSource, pagination, onTableChange } = props;
  // 处理分页

  const pageProps = {
    ...pagination,
    ...getTablePaginationDefaultSettings(),
  };

  return (
    <Table<RecordType>
      className={styles.resultTable}
      size="small"
      // sortDirections={['ascend', 'descend', 'ascend']}
      bordered
      rowKey={(record) => `${record.task_id}_${record.id}`}
      loading={loading}
      columns={columns}
      dataSource={dataSource}
      pagination={pageProps}
      onChange={onTableChange}
    />
  );
}

export default ResultTable;
