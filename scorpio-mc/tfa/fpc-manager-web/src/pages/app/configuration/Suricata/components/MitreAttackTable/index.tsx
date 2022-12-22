import { querySuricataMitreAttack } from '@/pages/app/security/service';
import type { IMitreAttack } from '@/pages/app/security/typings';
import { Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { useEffect, useState } from 'react';

const columns: ColumnProps<IMitreAttack>[] = [
  {
    title: '名称',
    dataIndex: 'name',
    align: 'center',
    width: 100,
  },
  {
    title: 'ID',
    dataIndex: 'id',
    align: 'center',
    width: 100,
  },
  {
    title: '规则数量',
    dataIndex: 'ruleSize',
    align: 'center',
    width: 100,
  },
  {
    title: '父规则ID',
    dataIndex: 'parentId',
    width: 100,
    align: 'center',
  },
];

const MitreAttackTable = () => {
  const [tableData, setTableData] = useState<IMitreAttack[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    querySuricataMitreAttack().then(({ success, result }) => {
      if (success) {
        setTableData(result);
      }

      setLoading(false);
    });
  }, []);

  return (
    <Table<IMitreAttack>
      bordered={true}
      size="small"
      columns={columns}
      dataSource={tableData}
      rowKey={'id'}
      loading={loading}
      scroll={{ x: 'max-content', y: 500 }}
      pagination={false}
    />
  );
};

export default MitreAttackTable;
