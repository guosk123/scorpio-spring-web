import StandardTable from '@/components/StandardTable';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { history, Link, useAccess } from 'umi';
import { ESensorStatus, INetworkSensorMap } from '../Network/typings';
import type { ILogicalSubnet } from './typings';
import { MAX_CUSTOM_SUBNETWORK_LIMIT } from './typings';

interface LogicalSubnetProps {
  dispatch: Dispatch;
  loading: boolean | undefined;
  allLogicalSubnets: ILogicalSubnet[];
  allNetworkSensorMap: INetworkSensorMap;
}

const LogicalSubnet: React.FC<LogicalSubnetProps> = ({
  dispatch,
  loading,
  allNetworkSensorMap,
  allLogicalSubnets,
}) => {
  const access = useAccess();
  useEffect(() => {
    dispatch({
      type: 'logicSubnetModel/queryAllLogicalSubnets',
    });
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
  }, [dispatch]);

  const handleDelete = ({ id }: ILogicalSubnet) => {
    dispatch({
      type: 'logicSubnetModel/deleteLogicalSubnet',
      payload: id,
    });
  };
  const tableColumns: ColumnProps<ILogicalSubnet>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
      ellipsis: true,
    },
    {
      title: '所属网络',
      align: 'center',
      ellipsis: true,
      dataIndex: 'networkInSensorIds',
      render: (networkInSensorIds: string) => {
        const networkInSensorIdList = networkInSensorIds.split(',');
        const networkInSensorNameList = networkInSensorIdList
          .map((id) => {
            const sensorNetwork = allNetworkSensorMap[id] || allNetworkSensorMap[id];
            if (!sensorNetwork) {
              return undefined;
            }
            return `${sensorNetwork.name || sensorNetwork.networkInSensorName}${
              sensorNetwork.status === ESensorStatus.OFFLINE ? '(离线)' : ''
            }`;
          })
          .filter((name) => name !== undefined);
        return networkInSensorNameList.join(',');
      },
    },
    {
      title: '子网类型',
      align: 'center',
      dataIndex: 'typeText',
    },
    {
      title: '总带宽（Mbps）',
      align: 'center',
      dataIndex: 'bandwidth',
    },
    {
      title: '操作',
      dataIndex: 'option',
      width: 240,
      align: 'center',
      render: (text, record) => {
        if (!access.hasServiceUserPerm) {
          return '没有操作权限';
        }
        return (
          <>
            <Link to={getLinkUrl(`/configuration/network/logical-subnet/${record.id}/update`)}>
              编辑
            </Link>
            <Divider type="vertical" />
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete(record)}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type="link" style={{ color: 'red' }} size="small">
                删除
              </Button>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  const isMaxSubNetwork = useMemo(() => {
    return allLogicalSubnets.length >= MAX_CUSTOM_SUBNETWORK_LIMIT;
  }, [allLogicalSubnets.length]);

  return (
    <>
      {access.hasServiceUserPerm && (
        <div className="searchForm small">
          <div className="searchFormOperate">
            <Tooltip
              title={`最多支持子网${MAX_CUSTOM_SUBNETWORK_LIMIT}个`}
              mouseEnterDelay={isMaxSubNetwork ? 0.1 : 9999}
            >
              <Button
                key="button"
                icon={<PlusOutlined />}
                type="primary"
                onClick={() =>
                  history.push(getLinkUrl('/configuration/network/logical-subnet/create'))
                }
              >
                新建
              </Button>
            </Tooltip>
          </div>
        </div>
      )}

      <StandardTable
        rowKey="id"
        loading={loading}
        columns={tableColumns}
        data={{ list: allLogicalSubnets, pagination: false }}
      />
    </>
  );
};

export default connect(
  ({
    logicSubnetModel: { allLogicalSubnets },
    networkModel: { allNetworkSensorMap },
    loading,
  }: ConnectState) => ({
    allLogicalSubnets,
    allNetworkSensorMap,
    loading: loading.effects['logicSubnetModel/queryAllLogicalSubnets'],
  }),
)(LogicalSubnet);
