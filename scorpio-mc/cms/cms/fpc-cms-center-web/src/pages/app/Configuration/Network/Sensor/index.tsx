import EnhancedTable from '@/components/EnhancedTable';
import type { ConnectState } from '@/models/connect';
import { PlusOutlined } from '@ant-design/icons';
import { Button, Divider, Popconfirm, Popover } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useAccess } from 'umi';
import type { INetworkSensor } from '../typings';
import { ENetworkSensorType, ESensorStatus } from '../typings';

const Index: React.FC<{
  dispatch: Dispatch;
  allNetworkSensor: INetworkSensor[];
  queryAllNetworkSensorLoading?: boolean;
}> = ({ dispatch, allNetworkSensor, queryAllNetworkSensorLoading }) => {
  const access = useAccess();

  // 获取表格数据
  const fetchTableData = async () => {
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
  };
  // 添加所有探针
  const handleAddAllSensors = async () => {
    dispatch({
      type: 'networkModel/createAllSensorNetworks',
    });
  };
  // 删除探针网络
  const handleDelete = async (record: INetworkSensor) => {
    dispatch({
      type: 'networkModel/deleteSensorNetwork',
      payload: record.id,
    });
  };
  // 表格列定义
  const tableColumns: ColumnProps<INetworkSensor>[] = [
    {
      title: '网络ID',
      dataIndex: 'networkInSensorId',
      align: 'center',
      fixed: 'left',
      ellipsis: true,
      width: 140,
    },
    {
      title: '网络名称',
      dataIndex: 'name',
      align: 'center',
      ellipsis: true,
      width: 100,
      render: (_, record) => {
        return `${record.name}${record.status === ESensorStatus.OFFLINE ? '(离线)' : ''}`;
      },
    },
    {
      title: '探针名称',
      dataIndex: 'sensorName',
      align: 'center',
      ellipsis: true,
      width: 100,
    },
    {
      title: '探针类型',
      dataIndex: 'sensorType',
      align: 'center',
      ellipsis: true,
      width: 100,
      render: (_, record) => {
        return ENetworkSensorType[record.sensorType];
      },
    },
    {
      title: '管理CMS',
      dataIndex: 'owner',
      align: 'center',
      ellipsis: true,
      width: 100,
    },
    {
      title: '描述',
      dataIndex: 'description',
      align: 'center',
      ellipsis: true,
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      ellipsis: true,
      fixed: 'right',
      width: 180,
      render: (_, record) => {
        if (!access.hasServiceUserPerm) {
          return '没有操作权限';
        }
        return (
          <>
            <Button
              type="link"
              onClick={() => {
                history.push(`/configuration/network/sensor/${record.id}/update`);
              }}
              size="small"
            >
              编辑
            </Button>
            <Divider type="vertical" />
            <Popconfirm
              title="是否确认删除?"
              onConfirm={() => {
                handleDelete(record);
              }}
              //   onCancel={cancel}
              okText="确定"
              cancelText="取消"
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

  useEffect(() => {
    fetchTableData();
  }, []);

  return (
    <div id="network-sensor-table">
      <EnhancedTable<INetworkSensor>
        tableKey="network-sensor-table"
        rowKey="id"
        loading={queryAllNetworkSensorLoading}
        columns={tableColumns}
        dataSource={allNetworkSensor}
        pagination={false}
        extraTool={
          access.hasServiceUserPerm && (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Popover
                content={<>将所有探针的网络一键全部添加到进来</>}
                title={false}
                trigger="hover"
              >
                <Button
                  icon={<PlusOutlined />}
                  style={{ marginRight: '10px' }}
                  type="primary"
                  onClick={handleAddAllSensors}
                >
                  添加所有
                </Button>
              </Popover>

              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  history.push('/configuration/network/sensor/create');
                }}
              >
                新建
              </Button>
            </div>
          )
        }
      />
    </div>
  );
};

export default connect(
  ({ loading: { effects }, networkModel: { allNetworkSensor } }: ConnectState) => ({
    allNetworkSensor,
    queryAllNetworkSensorLoading: effects['networkModel/queryAllNetworkSensor'],
  }),
)(Index);
