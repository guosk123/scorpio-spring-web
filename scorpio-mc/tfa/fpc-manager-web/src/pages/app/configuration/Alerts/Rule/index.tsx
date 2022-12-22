import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import '@ant-design/compatible/assets/index.css';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Badge, Button, Popconfirm, Select } from 'antd';
import { connect } from 'dva';
import { Fragment, useRef, useState } from 'react';
import type { Dispatch, INetworkTreeData } from 'umi';
import { history } from 'umi';
import ConnectCmsState from '../../components/ConnectCmsState';
import type { ILogicalSubnet, ILogicalSubnetMap } from '../../LogicalSubnet/typings';
import type { INetworkMap } from '../../Network/typings';
import type { IService } from '../../Service/typings';
import { queryAlertRules } from '../service';
import type { IAlertRule } from '../typings';
import { ALERT_CATEGORY_ENUM, ALERT_LEVEL_ENUM, EAlertRuleStatus } from '../typings';
import AlertRuleProfile from './components/AlertRuleProfile';
import { enumObj2List } from './components/RuleForm';

interface IAlertRuleProps {
  dispatch: Dispatch;
  allNetworkMap: INetworkMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  allServices: IService[];
  allLogicalSubnets: ILogicalSubnet[];
  networkTree: INetworkTreeData[];
}

const MAX_ALERTRULE_LIMIT = 100;

const AlertRule = ({
  dispatch,
  allNetworkMap,
  allLogicalSubnetMap,
  allServices,
  allLogicalSubnets,
  networkTree,
}: IAlertRuleProps) => {
  const actionRef = useRef<ActionType>();
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  // 告警规则总数
  const [total, setTotal] = useState(0);

  const handleCopy = (id: string) => {
    history.push(getLinkUrl(`/configuration/objects/alerts/rule/${id}/copy`));
  };

  // 删除
  const handleDelete = ({ id }: IAlertRule) => {
    dispatch({
      type: 'alertModel/deleteAlertRule',
      payload: { id },
    }).then(() => {
      actionRef.current?.reload();
    });
  };

  const handleCreate = () => {
    history.push(getLinkUrl('/configuration/objects/alerts/rule/create'));
  };

  const handleUpdate = (id: string) => {
    history.push(getLinkUrl(`/configuration/objects/alerts/rule/${id}/update`));
  };

  const handleEnableAlertRule = (id: string) => {
    dispatch({ type: 'alertModel/enableAlertRule', payload: { id } }).then((success: boolean) => {
      if (success) {
        actionRef.current?.reload();
      }
    });
  };

  const handleDisableAlertRule = (id: string) => {
    dispatch({ type: 'alertModel/disableAlertRule', payload: { id } }).then((success: boolean) => {
      if (success) {
        actionRef.current?.reload();
      }
    });
  };

  const columns: ProColumns<IAlertRule>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
      render: (text, record) => {
        return (
          <Badge
            status={record.status === EAlertRuleStatus.ENABLE ? 'success' : 'error'}
            text={<span>{text}</span>}
          />
        );
      },
    },
    {
      title: '分类',
      dataIndex: 'category',
      align: 'center',
      render: (text) => ALERT_CATEGORY_ENUM[String(text)] || text,
      renderFormItem: () => {
        return (
          <Select placeholder="选择告警分类">
            <Select.Option value="">全部</Select.Option>
            {enumObj2List(ALERT_CATEGORY_ENUM).map((item) => (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '级别',
      dataIndex: 'level',
      align: 'center',
      render: (text) => ALERT_LEVEL_ENUM[String(text)] || text,
      renderFormItem: () => {
        return (
          <Select placeholder="选择告警级别">
            <Select.Option value="">全部</Select.Option>
            {enumObj2List(ALERT_LEVEL_ENUM).map((item) => (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '网络',
      dataIndex: 'networkIds',
      align: 'center',
      render: (networkIds: any) => {
        return networkIds === 'allNetwork'
          ? '所有网络'
          : networkIds
              ?.split(',')
              .map((networkId: any) => {
                return { ...allNetworkMap, ...allLogicalSubnetMap }[networkId]?.name || networkId;
              })
              .join(',');
      },
      renderFormItem: () => {
        return (
          <Select placeholder="选择网络">
            <Select.Option value="">全部</Select.Option>
            {networkTree
              .concat(
                allLogicalSubnets.map((item) => ({
                  title: `${item.name}(子网)`,
                  key: item.id,
                  value: item.id,
                })),
              )
              .map((item: any) => (
                <Select.Option key={item.key} value={item.key}>
                  {item.title}
                </Select.Option>
              ))}
          </Select>
        );
      },
    },
    {
      title: '业务',
      dataIndex: 'serviceIds',
      align: 'center',
      render: (serviceIds: any) => {
        return serviceIds
          ?.split(',')
          .map((serviceId: any) => {
            return allServices.find((sub) => sub.id === serviceId.split('^')[0])?.name || serviceId;
          })
          .join(',');
      },
      renderFormItem: () => {
        return (
          <Select placeholder="选择业务">
            <Select.Option value="">全部</Select.Option>
            {allServices.map((item: any) => (
              <Select.Option key={item.id} value={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '描述',
      dataIndex: 'description',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: '操作',
      width: 160,
      align: 'center',
      search: false,
      dataIndex: 'action',
      render: (text, record) => {
        return (
          <Fragment>
            <Button
              type="link"
              size="small"
              disabled={record.status === EAlertRuleStatus.ENABLE || cmsConnectFlag}
              onClick={() => handleEnableAlertRule(record.id)}
            >
              启用
            </Button>
            <Button
              type="link"
              size="small"
              disabled={record.status === EAlertRuleStatus.DISENABLE || cmsConnectFlag}
              onClick={() => handleDisableAlertRule(record.id)}
            >
              停用
            </Button>
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete(record)}
              disabled={cmsConnectFlag}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type="link" size="small" disabled={cmsConnectFlag}>
                删除
              </Button>
            </Popconfirm>
            <Button
              type="link"
              size="small"
              disabled={total > MAX_ALERTRULE_LIMIT || cmsConnectFlag}
              onClick={() => handleCopy(record.id)}
            >
              复制
            </Button>
            <Button
              type="link"
              size="small"
              onClick={() => handleUpdate(record.id)}
              disabled={cmsConnectFlag}
            >
              编辑
            </Button>
            <AlertRuleProfile id={record.id} category={record.category}>
              <Button type="link" size="small">
                详情
              </Button>
            </AlertRuleProfile>
          </Fragment>
        );
      },
    },
  ];

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <ProTable
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params = {}) => {
          const { current, pageSize, startTime, endTime, networkIds, serviceIds, ...rest } = params;
          const newParams = {
            pageSize,
            page: current! - 1,
            networkId: networkIds,
            serviceId: serviceIds,
            ...rest,
          } as any;
          const { success, result } = await queryAlertRules(newParams);
          // 填充总数
          setTotal(success ? result.totalElements : 0);

          return {
            data: result.content,
            page: result.number,
            total: result.totalElements,
            success,
          };
        }}
        search={{
          ...proTableSerchConfig,
          labelWidth: 'auto',
          span: 6,
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            <Button
              key="created"
              icon={<PlusOutlined />}
              type="primary"
              onClick={handleCreate}
              disabled={total > MAX_ALERTRULE_LIMIT || cmsConnectFlag}
            >
              新建
            </Button>,
          ],
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
    </>
  );
};

export default connect(
  ({
    networkModel: { allNetworkMap, networkTree },
    serviceModel: { allServices },
    logicSubnetModel: { allLogicalSubnetMap, allLogicalSubnets },
  }: ConnectState) => {
    return {
      allNetworkMap,
      allLogicalSubnetMap,
      allServices,
      networkTree,
      allLogicalSubnets,
    };
  },
)(AlertRule);
