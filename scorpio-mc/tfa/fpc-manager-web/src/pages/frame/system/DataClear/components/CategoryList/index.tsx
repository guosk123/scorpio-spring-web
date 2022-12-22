import useFetchData from '@/utils/hooks/useFetchData';
import { useSafeState } from 'ahooks';
import { Button, Col, Popconfirm, Row, Select, Spin } from 'antd';
import { memo, useMemo, useState } from 'react';
import { clearData, queryDataClearCategory } from '../../service';

interface Props {
  onClear: () => void;
}

const CategoryList = ({ onClear }: Props) => {
  const [selectedAll, setSelectedAll] = useState(false);
  const [selectedValue, setSelectedValue] = useState<string[]>([]);
  const { data } = useFetchData<Record<string, string>>(queryDataClearCategory);
  const [clearLoading, setClearLoading] = useSafeState(false);

  const category = useMemo(() => {
    if (data === undefined) {
      return [];
    }
    return Object.keys(data).map((item) => {
      return {
        label: data[item],
        value: item,
      };
    });
  }, [data]);

  const handleSelectChange = (values: string[]) => {
    if (values.includes('all')) {
      setSelectedValue(['all']);
      setSelectedAll(true);
    } else {
      setSelectedValue(values);
      setSelectedAll(false);
    }
  };

  const handleClear = () => {
    setClearLoading(true);
    clearData(selectedAll ? ['all'] : selectedValue).then(() => {
      setClearLoading(false);
      onClear();
    });
  };

  if (data === undefined) {
    return (
      <div className="center">
        <Spin />
      </div>
    );
  }

  return (
    <Row style={{ marginBottom: 12 }} gutter={12}>
      <Col>
        <div className="center" style={{ height: '100%' }}>
          清理数据类型
        </div>
      </Col>
      <Col span={18}>
        <Select
          placeholder={"请选择要清理的数据"}
          style={{ width: '100%' }}
          value={selectedValue}
          onChange={handleSelectChange}
          mode="multiple"
          maxTagCount={'responsive'}
        >
          {category.map((item) => {
            return (
              <Select.Option
                key={item.value}
                value={item.value}
                disabled={selectedAll && item.value !== 'all'}
              >
                {item.label}
              </Select.Option>
            );
          })}
        </Select>
      </Col>
      <Col span={3}>
        <Popconfirm title={'是否确认清理'} onConfirm={handleClear} okText="确认" cancelText="取消">
          <Button htmlType={'submit'} type="primary" danger loading={clearLoading}>
            {`清理${clearLoading ? '中' : ''}`}
          </Button>
        </Popconfirm>
      </Col>
    </Row>
  );
};

export default memo(CategoryList);
