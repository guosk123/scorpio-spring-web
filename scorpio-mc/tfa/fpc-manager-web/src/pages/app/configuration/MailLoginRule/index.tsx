import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Popconfirm, Space } from 'antd';
import { useRef } from 'react';
import type { GeolocationModelState } from 'umi';
import { history, useSelector } from 'umi';
import { deleteMailLoginRule, queryAllMailLoginRule, updateMailLoginRuleState } from './service';
import type { IMailLoginRule } from './typings';
import { RuleActionLabel } from './typings';
import { EMailRuleStatus, WeekLabel } from './typings';

const MailLoginRule = () => {
  const actionRef = useRef<ActionType>();

  const { allCountryMap, allCityMap, allProvinceMap } = useSelector<
    ConnectState,
    GeolocationModelState
  >((state) => state.geolocationModel);

  const columns: ProColumns<IMailLoginRule>[] = [
    {
      dataIndex: 'index',
      title: '#',
      search: false,
      align: 'center',
    },
    {
      dataIndex: 'mailAddress',
      title: '邮箱',
      align: 'center',
    },
    {
      dataIndex: 'countryId',
      title: '国家',
      search: false,
      align: 'center',
      render(countryId) {
        return allCountryMap[countryId as string]?.nameText;
      },
    },
    {
      dataIndex: 'provinceId',
      title: '省份',
      search: false,
      align: 'center',
      render(dom, record) {
        const { provinceId } = record;
        if (!provinceId) return '-';
        return allProvinceMap[provinceId as string]?.nameText;
      },
    },
    {
      dataIndex: 'cityId',
      title: '城市',
      search: false,
      align: 'center',
      render(dom, record) {
        const { cityId } = record;
        if (!cityId) return '-';
        return allCityMap[cityId as string]?.nameText;
      },
    },
    {
      title: '登录起始时间',
      dataIndex: 'startTime',
      search: false,
      align: 'center',
    },
    {
      title: '登录结束时间',
      dataIndex: 'endTime',
      search: false,
      align: 'center',
    },
    {
      title: '动作',
      dataIndex: 'action',
      align: 'center',
      valueType: 'select',
      valueEnum: RuleActionLabel,
      render(dom, record) {
        const { action } = record;
        return RuleActionLabel[action];
      },
    },
    {
      title: '每周生效时间',
      dataIndex: 'period',
      search: false,
      align: 'center',
      render(dom, record) {
        const { period } = record;
        return period
          ?.split(',')
          .map((item) => WeekLabel[item])
          .join(',');
      },
    },
    {
      title: '操作',
      dataIndex: 'operation',
      search: false,
      align: 'center',
      render: (dom, record) => {
        const { id, state } = record;
        return (
          <Space>
            <span
              className="link"
              onClick={() => {
                history.push(`/configuration/safety-analysis/mail-login/${id}/update`);
              }}
            >
              编辑
            </span>
            <span
              className={state === EMailRuleStatus.disable ? 'link' : 'disabled'}
              onClick={() => {
                updateMailLoginRuleState(id, EMailRuleStatus.enable).then(() => {
                  actionRef.current?.reload();
                });
              }}
            >
              启用
            </span>
            <span
              className={state === EMailRuleStatus.enable ? 'link' : 'disabled'}
              onClick={() => {
                updateMailLoginRuleState(id, EMailRuleStatus.disable).then(() => {
                  actionRef.current?.reload();
                });
              }}
            >
              停用
            </span>
            <Popconfirm
              title="是否确认删除规则"
              onConfirm={() => {
                deleteMailLoginRule(id).then(() => {
                  actionRef.current?.reload();
                });
              }}
            >
              <span style={{ color: 'red', cursor: 'pointer' }}>删除</span>
            </Popconfirm>
          </Space>
        );
      },
    },
  ];

  return (
    <ProTable
      columns={columns}
      bordered
      toolBarRender={false}
      size={'small'}
      actionRef={actionRef}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;
        const { success, result } = await queryAllMailLoginRule({
          ...rest,
          pageNumber: current || 0,
          pageSize: pageSize || 20,
        });
        if (!success) {
          return {
            total: 0,
            data: [],
          };
        }
        const { content, totalElements } = result;
        return {
          total: totalElements,
          data: content,
          success,
        };
      }}
      search={{
        ...proTableSerchConfig,
        optionRender: (config, _, dom) => {
          return [
            ...dom,
            <Button
              key="create"
              type="primary"
              onClick={() => {
                history.push('/configuration/safety-analysis/mail-login/create');
              }}
            >
              新建
            </Button>,
          ];
        },
      }}
      pagination={{ ...getTablePaginationDefaultSettings() }}
    />
  );
};

export default MailLoginRule;
