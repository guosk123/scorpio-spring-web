import { globalTimeFormatText } from '@/components/GlobalTimeSelector';
import {
  deleteSuricataRuleClasstype,
  querySuricataRuleClasstype,
} from '@/pages/app/security/service';
import type { IRuleClasstype } from '@/pages/app/security/typings';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { Popconfirm, Space, Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import { useEffect, useState } from 'react';
import { history } from 'umi';

const ClasstypeTable = ({ actionDisable }: { actionDisable?: boolean }) => {
  const [tableData, setTableData] = useState<IRuleClasstype[]>([]);
  const [loading, setLoading] = useState(false);
  const [fresh, setFresh] = useState(0);

  const columns: ColumnProps<IRuleClasstype>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
      width: 150,
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
      title: '创建时间',
      dataIndex: 'createTime',
      width: 200,
      align: 'center',
      render: (time) => {
        return moment(time as string).format(globalTimeFormatText);
      },
    },
    ...(actionDisable
      ? []
      : [
          {
            title: '操作',
            dataIndex: 'option',
            width: 100,
            render: (_, record) => {
              const { id } = record;
              return (
                <Space>
                  <span
                    onClick={() => {
                      history.push(
                        `/configuration/safety-analysis/suricata/rule/classtype/${id}/update`,
                      );
                    }}
                  >
                    编辑
                  </span>
                  <Popconfirm
                    title="确定删除吗？"
                    onConfirm={() => {
                      deleteSuricataRuleClasstype(id).then(() => {
                        setFresh((prev) => prev + 1);
                      });
                    }}
                    icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                  >
                    <a style={{ color: 'red' }}>删除</a>
                  </Popconfirm>
                </Space>
              );
            },
          } as ColumnProps<IRuleClasstype>,
        ]),
  ];

  useEffect(() => {
    setLoading(true);
    querySuricataRuleClasstype({}).then(({ success, result }) => {
      if (success) {
        setTableData(result);
      }

      setLoading(false);
    });
  }, [fresh]);

  return (
    <Table<IRuleClasstype>
      bordered={true}
      size="small"
      columns={columns}
      dataSource={tableData}
      loading={loading}
      scroll={{ x: 'max-content', y: 500 }}
      pagination={false}
      rowKey={'id'}
    />
  );
};

export default ClasstypeTable;
