import { Table, Typography } from 'antd';
import type { ColumnProps } from 'antd/lib/table';

const { Paragraph, Text } = Typography;

const intelligenceRuleColumns: ColumnProps<any>[] = [
  {
    title: '情报类型',
    dataIndex: 'type',
    key: 'type',
    align: 'center',
  },
  {
    title: '情报信息',
    dataIndex: 'content',
    key: 'content',
    align: 'center',
  },
  {
    title: '威胁类型',
    dataIndex: 'threatCategory',
    key: 'threatCategory',
    align: 'center',
  },
  {
    title: '更新时间',
    dataIndex: 'timestamp',
    key: 'timestamp',
    align: 'center',
  },
];

/**
 * 威胁情报导入规则说明
 */
const intelligenceRuleDesc = (
  <>
    <Paragraph style={{ marginTop: 10 }}>
      <Text strong>1. 导入规则：</Text>
      <div>情报类型和威胁类型必须一一对应：</div>
      <ul>
        <li>domain: dynamic domain</li>
        <li>ip: suspicious</li>
        <li>ja3: whitelist</li>
      </ul>
      <Text strong>2. 示例：</Text>
      <Table
        rowKey="type"
        bordered
        size="small"
        columns={intelligenceRuleColumns}
        pagination={false}
        dataSource={[
          {
            type: 'domain',
            content: 'www.abcd.com',
            threatCategory: 'dynamic domain',
            timestamp: '2020/6/5',
          },
          {
            type: 'ip',
            content: '123.123.123.123',
            threatCategory: 'suspicious',
            timestamp: '2020/6/5',
          },
          {
            type: 'ja3',
            content: '6b5e0cfe988c723ee71faf54f8460684',
            threatCategory: 'whitelist',
            timestamp: '2020/6/5',
          },
        ]}
      />
    </Paragraph>
  </>
);

export default intelligenceRuleDesc;
