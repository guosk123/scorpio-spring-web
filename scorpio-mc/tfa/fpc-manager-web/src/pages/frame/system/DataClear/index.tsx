import { Card } from 'antd';
import { useRef } from 'react';
import CategoryList from './components/CategoryList';
import LogTable from './components/LogTable';

const DataClear = () => {
  const tableRef = useRef<{ fresh: () => void }>(null);

  const freshTable = () => {
    tableRef.current?.fresh();
  };

  return (
    <Card bordered={false} bodyStyle={{ width: '80%', margin: '0 auto' }}>
      <CategoryList onClear={freshTable} />
      <LogTable ref={tableRef} />
    </Card>
  );
};

export default DataClear;
