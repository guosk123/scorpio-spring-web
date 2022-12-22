import { getTablePaginationDefaultSettings } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import type { INetworkGroup, INetworkSensor } from '@/pages/app/Configuration/Network/typings';
import type { ILogicalSubnet } from '@/pages/app/Configuration/LogicalSubnet/typings';
import { ReloadOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import { EditableProTable } from '@ant-design/pro-table';
import { Alert, Button, Form, message, Select } from 'antd';
import { useEffect, useRef } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import { queryNetworkPerms, updateNetworkPerms } from './service';
import type { INetworkPerm } from './typings';

interface Props {
  dispatch: Dispatch;
  queryLoading?: boolean;
  allNetworkGroup: INetworkGroup[];
  allNetworkSensor: INetworkSensor[];
  allLogicalSubnets: ILogicalSubnet[];
}

interface INetworkPermTableData extends Omit<INetworkPerm, 'networkIds' | 'networkGroupIds'> {
  networkIds: string[];
  networkGroupIds: string[];
}

const NetworkPerm = ({
  dispatch,
  allNetworkSensor = [],
  allNetworkGroup = [],
  allLogicalSubnets = [],
}: Props) => {
  const actionRef = useRef<ActionType>();
  const [form] = Form.useForm();

  useEffect(() => {
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
    dispatch({
      type: 'networkModel/queryAllNetworkGroups',
    });
    dispatch({
      type: 'logicSubnetModel/queryAllLogicalSubnets',
    });
  }, []);

  const columns: ProColumns<INetworkPermTableData>[] = [
    {
      title: '用户名',
      dataIndex: 'userName',
      align: 'center',
      editable: false,
    },
    {
      title: '网络',
      dataIndex: 'networkIds',
      align: 'center',
      valueType: 'select',
      valueEnum: {
        ...allNetworkSensor.reduce(
          (obj, item) => ({
            ...obj,
            [item.networkInSensorId]: { text: item.networkInSensorName },
          }),
          {},
        ),
        ...allLogicalSubnets.reduce(
          (obj, item) => ({
            ...obj,
            [item.id]: { text: item.name },
          }),
          {},
        ),
      },
      renderFormItem: () => {
        return (
          <Select showSearch mode="multiple" placeholder="请选择网络/逻辑子网">
            <Select.OptGroup label="网络">
              {allNetworkSensor.map((item) => (
                <Select.Option value={item.networkInSensorId} key={item.networkInSensorId}>
                  {item.name}
                </Select.Option>
              ))}
            </Select.OptGroup>
            <Select.OptGroup label="逻辑网络">
              {allLogicalSubnets.map((item) => (
                <Select.Option value={item.id} key={item.id}>
                  {item.name}
                </Select.Option>
              ))}
            </Select.OptGroup>
          </Select>
        );
      },
    },
    {
      title: '网络组',
      dataIndex: 'networkGroupIds',
      align: 'center',
      valueType: 'select',
      valueEnum: {
        ...allNetworkGroup.reduce(
          (obj, item) => ({
            ...obj,
            [item.id]: { text: item.name },
          }),
          {},
        ),
      },
      renderFormItem: () => {
        return (
          <Select showSearch mode="multiple" placeholder="请选择网络组">
            {allNetworkGroup.map((item) => (
              <Select.Option value={item.id} key={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '操作',
      valueType: 'option',
      align: 'center',
      render: (text, record, _, action) => [
        <a
          key="editable"
          onClick={() => {
            action?.startEditable?.(record.userId);
          }}
        >
          编辑
        </a>,
      ],
    },
  ];

  return (
    <>
      <Alert
        style={{ marginBottom: 10 }}
        showIcon
        type="info"
        message="仅展示除【配置管理员】之外，拥有【业务用户角色】人员的网络权限。"
        action={
          <Button
            icon={<ReloadOutlined />}
            type="primary"
            onClick={() => actionRef?.current?.reload()}
          >
            刷新
          </Button>
        }
      />
      <EditableProTable<INetworkPermTableData>
        rowKey="userId"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params = {}) => {
          const { current = 0, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...rest,
          };
          const { success, result } = await queryNetworkPerms(newParams);
          return {
            data: result.content.map((row) => ({
              ...row,
              networkGroupIds: row.networkGroupIds ? row.networkGroupIds.split(',') : [],
              networkIds: row.networkIds ? row.networkIds.split(',') : [],
            })),
            page: result.number,
            total: result.totalElements,
            success,
          };
        }}
        search={false}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
        recordCreatorProps={false}
        // 编辑
        editable={{
          form,
          type: 'multiple',
          actionRender: (row, config, defaultDom) => [defaultDom.save, defaultDom.cancel],
          onSave: async (rowKey, data, row) => {
            const { success } = await updateNetworkPerms({
              userId: row.userId,
              networkGroupIds: data.networkGroupIds?.join(','),
              networkIds: data.networkIds?.join(','),
            });
            if (!success) {
              message.error('保存失败');
              return;
            }
            actionRef?.current?.reload();
          },
        }}
      />
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    networkModel: { allNetworkSensor, allNetworkGroup },
    logicSubnetModel: { allLogicalSubnets },
  }: ConnectState) => ({
    allNetworkGroup,
    allNetworkSensor,
    allLogicalSubnets,
    queryLoading:
      effects['networkModel/queryAllNetworkSensor'] ||
      effects['networkModel/queryAllNetworkGroups'] ||
      effects['logicSubnetModel/queryAllLogicalSubnets'],
  }),
)(NetworkPerm);
