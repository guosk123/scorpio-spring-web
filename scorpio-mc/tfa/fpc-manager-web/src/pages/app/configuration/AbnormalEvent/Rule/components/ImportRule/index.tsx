import { Table, Typography } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { ABNORMAL_EVENT_TYPE_ENUM } from '../../../typings';
import { PRIVATE_RULE_NUMBER_MIN } from '../RuleForm';

const { Paragraph, Text } = Typography;

const eventColumns: ColumnProps<any>[] = [
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    align: 'center',
  },
  {
    title: '内容',
    dataIndex: 'content',
    key: 'content',
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
 * 自定义异常事件导入规则说明
 */
export const customEventRuleDesc = (
  <>
    <Paragraph style={{ marginTop: 10 }}>
      <Text strong>1. 导入示例：</Text>
      <Table
        rowKey="type"
        bordered
        size="small"
        columns={eventColumns}
        pagination={false}
        dataSource={[
          {
            type: '违规域名访问',
            content: '违规域名访问内容',
            timestamp: '2020/6/5',
          },
          {
            type: '可疑域名',
            content: '可疑域名内容',
            timestamp: '2020/6/5',
          },
        ]}
      />
      <Text strong>2. 类型枚举值</Text>
      <Table
        size={'small'}
        bordered
        showHeader={false}
        pagination={false}
        columns={[{ title: '标签', dataIndex: 'title' }]}
        dataSource={(() => {
          return Object.keys(ABNORMAL_EVENT_TYPE_ENUM)
            .filter((ele) => +ele < PRIVATE_RULE_NUMBER_MIN)
            .map((item) => ({
              title: ABNORMAL_EVENT_TYPE_ENUM[item]?.label,
            }));
        })()}
      />
    </Paragraph>
  </>
);

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
export const intelligenceRuleDesc = (
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
