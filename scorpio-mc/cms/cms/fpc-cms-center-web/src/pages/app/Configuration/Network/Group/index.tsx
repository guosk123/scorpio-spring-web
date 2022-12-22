import EnhancedTable from '@/components/EnhancedTable';
import type { ConnectState } from '@/models/connect';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Divider, Popconfirm } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useAccess } from 'umi';
import { ESensorStatus, INetworkGroup, INetworkSensor } from '../typings';

const Index: React.FC<{
  dispatch: Dispatch;
  allNetworkSensor: INetworkSensor[];
  allNetworkGroup: INetworkGroup[];
  queryAllNetworkSensorLoading: boolean;
  queryAllNetworkGroupLoading: boolean;
}> = ({
  dispatch,
  allNetworkSensor,
  allNetworkGroup,
  queryAllNetworkSensorLoading,
  queryAllNetworkGroupLoading,
}) => {
  const access = useAccess();
  // 记录选中的网络组
  const [selectedRowKeys, setSelectedRowKeys] = useState<any[]>([]);

  // 删除网络组
  const handleDelete = async (record: INetworkGroup) => {
    const { id } = record;
    const selectedIndex = selectedRowKeys.indexOf(id);
    selectedRowKeys.splice(selectedIndex, 1);
    dispatch({
      type: 'networkModel/deleteNetworkGroup',
      payload: id,
    });
  };

  // 表格列定义
  const tableColumns: ColumnProps<INetworkGroup>[] = [
    {
      title: '网络组ID',
      dataIndex: 'id',
      align: 'center',
      fixed: 'left',
      ellipsis: true,
      width: 100,
    },
    {
      title: '网络组名称',
      dataIndex: 'name',
      align: 'center',
      fixed: 'left',
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
      title: '探针网络',
      dataIndex: 'networkInSensorIds',
      align: 'center',
      ellipsis: true,
      width: 100,
      render: (_, record) => {
        const networkInSensorIds = record.networkInSensorIds?.split(',') || [];
        const networkInSensorNames: string[] = [];
        networkInSensorIds.forEach((networkInSensorId: string) => {
          const networkItem = allNetworkSensor.find(
            (item) => item.networkInSensorId === networkInSensorId,
          );
          if (networkItem) {
            networkInSensorNames.push(
              `${networkItem.name}${networkItem.status === ESensorStatus.OFFLINE ? '(离线)' : ''}`,
            );
          }
        });
        return networkInSensorNames.join(',');
      },
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      ellipsis: true,
      fixed: 'right',
      width: 100,
      render: (_, record) => {
        if (!access.hasServiceUserPerm) {
          return '没有操作权限';
        }

        return (
          <>
            <Button
              type="link"
              onClick={() => {
                history.push(`/configuration/network/group/${record.id}/update`);
              }}
              size="small"
            >
              编辑
            </Button>
            <Popconfirm
              title="是否确认删除?"
              onConfirm={() => {
                handleDelete(record);
              }}
              okText="确定"
              cancelText="取消"
            >
              <Divider type="vertical" />
              <Button type="link" style={{ color: 'red' }} size="small">
                删除
              </Button>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  // 批量删除网络组
  const handleBatchDelete = async () => {
    const ids = selectedRowKeys.join(',');
    dispatch({
      type: 'networkModel/deleteBatchNetworkGroup',
      payload: ids,
    });
  };
  // 获取表格数据
  const fetchTableData = async () => {
    dispatch({
      type: 'networkModel/queryAllNetworkGroups',
    });
  };
  // 获取探针中的网络数据
  const fetchNetworkInSensor = () => {
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
  };
  // 初始化
  useEffect(() => {
    fetchNetworkInSensor();
    fetchTableData();
  }, []);

  return (
    <div id="network-group-table">
      <EnhancedTable<INetworkGroup>
        tableKey="network-group-table"
        rowKey="id"
        rowSelection={
          access.hasServiceUserPerm
            ? ({
                selectedRowKeys,
                onChange: (values: any) => {
                  setSelectedRowKeys(values);
                },
              } as any)
            : undefined
        }
        loading={queryAllNetworkGroupLoading && queryAllNetworkSensorLoading}
        columns={tableColumns}
        dataSource={allNetworkGroup}
        pagination={false}
        extraTool={
          access.hasServiceUserPerm && (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Popconfirm
                title="是否确认删除?"
                onConfirm={() => {
                  handleBatchDelete();
                }}
                disabled={selectedRowKeys.length === 0}
              >
                <Button
                  type="primary"
                  danger
                  icon={<DeleteOutlined />}
                  disabled={selectedRowKeys.length === 0}
                >
                  删除
                </Button>
              </Popconfirm>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  history.push('/configuration/network/group/create');
                }}
                style={{ marginLeft: '10px' }}
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
  ({
    loading: { effects },
    networkModel: { allNetworkSensor, allNetworkGroup },
  }: ConnectState) => ({
    allNetworkSensor,
    allNetworkGroup,
    queryAllNetworkSensorLoading: effects['networkModel/queryAllNetworkSensor'],
    queryAllNetworkGroupLoading: effects['networkModel/queryAllNetworkGroups'],
  }),
)(Index as any);
