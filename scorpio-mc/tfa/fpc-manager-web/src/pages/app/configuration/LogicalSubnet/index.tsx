import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { Button, Divider, Popconfirm, Table, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { Fragment, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import ConnectCmsState from '../components/ConnectCmsState';
import type { ILogicalSubnet } from './typings';
import { MAX_CUSTOM_SUBNETWORK_LIMIT } from './typings';

interface LogicalSubnetProps {
  dispatch: Dispatch;
  loading: boolean | undefined;
  allLogicalSubnets: ILogicalSubnet[];
}

const LogicalSubnet: React.FC<LogicalSubnetProps> = ({ dispatch, loading, allLogicalSubnets }) => {
  useEffect(() => {
    dispatch({
      type: 'logicSubnetModel/queryAllLogicalSubnets',
    });
  }, [dispatch]);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
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
      ellipsis: true,
    },
    {
      title: '所属网络',
      dataIndex: 'networkName',
    },
    {
      title: '子网类型',
      dataIndex: 'typeText',
    },
    {
      title: '总带宽（Mbps）',
      dataIndex: 'bandwidth',
    },
    {
      title: '操作',
      dataIndex: 'option',
      width: 140,
      align: 'center',
      render: (text, record) => (
        <Fragment>
          <Button
            type={'link'}
            size={'small'}
            onClick={() => {
              history.push(
                getLinkUrl(`/configuration/network-netif/logical-subnet/${record.id}/update`),
              );
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            disabled={cmsConnectFlag}
          >
            <Button type={'link'} size={'small'} disabled={cmsConnectFlag}>
              删除
            </Button>
          </Popconfirm>
        </Fragment>
      ),
    },
  ];
  const isMaxSubNetwork = useMemo(() => {
    return allLogicalSubnets.length >= MAX_CUSTOM_SUBNETWORK_LIMIT;
  }, [allLogicalSubnets.length]);

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
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
                history.push(getLinkUrl('/configuration/network-netif/logical-subnet/create'))
              }
              disabled={cmsConnectFlag}
            >
              新建
            </Button>
          </Tooltip>
        </div>
      </div>

      <Table
        size={'small'}
        bordered
        key={`${cmsConnectFlag}`}
        rowKey="id"
        loading={loading}
        columns={tableColumns}
        dataSource={allLogicalSubnets}
        pagination={false}
      />
    </>
  );
};

export default connect(({ logicSubnetModel: { allLogicalSubnets }, loading }: ConnectState) => ({
  allLogicalSubnets,
  loading: loading.effects['logicSubnetModel/queryAllLogicalSubnets'],
}))(LogicalSubnet);
