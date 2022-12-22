import type { ConnectState, Loading } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { Button, Divider, Popconfirm, Table, Tag, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { Fragment, useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { history, Link, useDispatch, useSelector } from 'umi';
import type { INetwork } from './typings';
import { MAX_CUSTOM_NETWORK_LIMIT } from './typings';

export default () => {
  const queryLoading = useSelector<ConnectState, Loading>((state) => state.loading).effects[
    'networkModel/queryAllNetworks'
  ];
  const dispatch = useDispatch<Dispatch>();
  const allNetworks = useSelector<ConnectState, INetwork[]>(
    (state) => state.networkModel.allNetworks,
  );

  useEffect(() => {
    dispatch({
      type: 'networkModel/queryAllNetworks',
    });
  }, [dispatch]);

  const handleDelete = ({ id }: INetwork) => {
    dispatch({
      type: 'networkModel/deleteNetwork',
      payload: id,
    });
  };

  const columns: ColumnProps<INetwork>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: '业务接口',
      dataIndex: 'netifNemes',
      render: (text, row) =>
        Array.isArray(row.netif)
          ? row.netif.map((netif) => <Tag key={netif.netifName}>{netif.netifName}</Tag>)
          : '',
    },
    {
      title: '流量方向',
      dataIndex: 'netifTypeText',
    },
    {
      title: '总带宽（Mbps）',
      dataIndex: 'bandwidth',
    },
    {
      title: '操作',
      dataIndex: 'option',
      width: 200,
      align: 'center',
      render: (text, record) => (
        <Fragment>
          <Link to={getLinkUrl(`/configuration/network-netif/network/${record.id}/update`)}>
            编辑
          </Link>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <a href="#">删除</a>
          </Popconfirm>
        </Fragment>
      ),
    },
  ];

  const isMaxNetwork = useMemo(() => {
    return allNetworks.length >= MAX_CUSTOM_NETWORK_LIMIT;
  }, [allNetworks.length]);

  return (
    <>
      <div style={{ marginBottom: 10, textAlign: 'right' }}>
        <Tooltip
          title={`最多支持网络${MAX_CUSTOM_NETWORK_LIMIT}个`}
          mouseEnterDelay={isMaxNetwork ? 0.1 : 9999}
        >
          <Button
            key="button"
            icon={<PlusOutlined />}
            type="primary"
            disabled={isMaxNetwork}
            onClick={() => history.push(getLinkUrl('/configuration/network-netif/network/create'))}
          >
            新建
          </Button>
        </Tooltip>
      </div>

      <Table
        rowKey="id"
        size="small"
        bordered
        loading={queryLoading}
        columns={columns}
        dataSource={allNetworks}
        pagination={false}
      />
    </>
  );
};
