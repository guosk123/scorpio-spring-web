import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Space } from 'antd';
import { useRef } from 'react';
import { history } from 'umi';
import { deleteForwardRule, queryForwardRules } from '../service';
import type { IForwardRule } from '../typings';

const RuleList = () => {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<IForwardRule>[] = [
    {
      dataIndex: 'name',
      align: 'center',
      title: '规则名称',
      search: false,
    },
    {
      dataIndex: 'referenceCount',
      align: 'center',
      title: '策略应用次数',
      search: false,
    },
    {
      dataIndex: 'description',
      align: 'center',
      title: '描述',
      search: false,
    },
    {
      dataIndex: 'operation',
      align: 'center',
      title: '操作',
      search: false,
      render: (_, record) => {
        const { id } = record;
        return (
          <Space>
            <span
              className="link"
              onClick={() => {
                history.push(`/configuration/netflow/forward-rule/update/${id}`);
              }}
            >
              编辑
            </span>
            <span
              className="link"
              onClick={() => {
                deleteForwardRule(id).then(() => {
                  actionRef.current?.reload();
                });
              }}
            >
              删除
            </span>
          </Space>
        );
      },
    },
  ];
  return (
    <ProTable
      size="small"
      columns={columns}
      bordered
      rowKey={'id'}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;

        const { success, result } = await queryForwardRules({
          ...rest,
          page: current! - 1,
          pageSize: pageSize,
        });
        if (!success) {
          return {
            total: 0,
            data: [],
            success: true,
          };
        }
        const { content, totalElements } = result;

        return {
          total: totalElements,
          data: content,
          success,
        };
      }}
      actionRef={actionRef}
      search={{
        ...proTableSerchConfig,
        resetText: undefined,
        optionRender: () => [
          <Button
            type="primary"
            key="create"
            onClick={() => {
              history.push('/configuration/netflow/forward-rule/create');
            }}
          >
            新建
          </Button>,
        ],
      }}
      toolBarRender={false}
      pagination={getTablePaginationDefaultSettings()}
    />
  );
};

export default RuleList;
