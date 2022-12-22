import { DEVICE_NETIF_CATEGORY_MANAGER, DEVICE_NETIF_STATE_UP, ONE_KILO_1000 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import { convertBandwidth } from '@/utils/utils';
import { Table, Tag, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useCallback, Fragment, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import type { INetif, INetifAnalysis } from './typings';
import { ENetifType, ENetifCategory } from './typings';
import NetifStats from './components/StatChart';
import DeviceNetifUpdate from './components/UpdateNetif';
import _ from 'lodash';
import moment from 'moment';

interface IDeviceNetifProps {
  dispatch: Dispatch;
  netifList: INetif[];
  queryLoading: boolean | undefined;

  netifHistogram: INetifAnalysis[];
}
const DeviceNetif: React.FC<IDeviceNetifProps> = ({
  dispatch,
  netifList,
  queryLoading,
  netifHistogram,
}) => {
  const [netifInfo, setNetifInfo] = useState<INetif | undefined>(undefined);

  const queryDeviceNetifs = useCallback(() => {
    if (dispatch) {
      dispatch({
        type: 'deviceNetifModel/queryDeviceNetifs',
      });
    }
  }, [dispatch]);

  useEffect(() => {
    queryDeviceNetifs();
  }, [queryDeviceNetifs]);

  const showModal = (netif: INetif) => {
    dispatch({
      type: 'deviceNetifModel/showModal',
      payload: {
        currentItem: netif,
      },
    });
  };

  const handleNetifNameClick = (nextNetifInfo: INetif) => {
    if (!netifInfo || netifInfo.id !== nextNetifInfo.id) {
      setNetifInfo(nextNetifInfo);
    } else {
      setNetifInfo(undefined);
      // 清空原来的统计数据
      dispatch({
        type: 'deviceNetifModel/clearNetifHistogram',
      });
    }
  };
  const debouncedHandleClick = _.debounce(handleNetifNameClick, 300);

  const tableColumns: ColumnProps<INetif>[] = [
    {
      title: '#',
      dataIndex: 'index',
      align: 'center',
      width: 60,
      render: (text, record, index) => index + 1,
    },
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
    },
    {
      title: '类型',
      dataIndex: 'type',
      align: 'center',
      render: (__, record) => {
        if (record.typeText !== '') {
          return record.typeText;
        }
        if (record.type === ENetifType.DPDK) {
          return 'DPDK';
        }
        if (record.type === ENetifType.COMMON) {
          return '普通';
        }
        return '未知';
      },
    },
    {
      title: '用途',
      dataIndex: 'categoryText',
      align: 'center',
      render: (__, record) => {
        if (record.categoryText !== '') {
          return record.categoryText;
        }
        if (record.category === ENetifCategory.DEFAULT) {
          return '未配置';
        }
        switch (record.category) {
          case ENetifCategory.RECEIVE:
            return '流量接收';
          case ENetifCategory.REPLAY:
            return '流量重放';
          case ENetifCategory.NETFLOW:
            return 'Netflow';
          default:
            return '未知';
        }
      },
    },
    {
      title: '物理带宽',
      dataIndex: 'specification',
      align: 'center',
      render: (specification) => (specification ? `${specification / ONE_KILO_1000}Gbps` : '--'),
    },
    {
      title: '状态',
      dataIndex: 'state',
      align: 'center',
      render: (state) =>
        state === DEVICE_NETIF_STATE_UP ? (
          <Tag style={{ cursor: 'default', marginRight: 0 }} color="green">
            up
          </Tag>
        ) : (
          <Tag style={{ cursor: 'default', marginRight: 0 }}>down</Tag>
        ),
    },
    {
      title: '带宽',
      dataIndex: 'bandwidth',
      align: 'center',
      render: (bandwidth, record) => (
        <Tooltip
          title={
            record.metricTime ? (
              <Fragment>
                <div>带宽统计时间：</div>
                <div>{moment(record.metricTime).format('YYYY-MM-DD HH:mm:ss')}</div>
                <div>[点击查看最近1小时统计详情]</div>
              </Fragment>
            ) : (
              ''
            )
          }
        >
          <a onClick={() => debouncedHandleClick(record)}>{convertBandwidth(bandwidth || 0)}</a>
        </Tooltip>
      ),
    },
    {
      title: '描述信息',
      dataIndex: 'description',
      ellipsis: true,
      width: 240,
    },
    {
      title: '操作',
      dataIndex: 'action',
      align: 'center',
      width: 100,
      render: (text, record) => {
        return <a onClick={() => showModal(record)}>编辑</a>;
      },
    },
  ];

  const displayNetifList = netifList.filter(
    (netif) => netif.category !== DEVICE_NETIF_CATEGORY_MANAGER,
    // // 放开管理口
    // (netif) => true,
  );

  return (
    <Fragment>
      <Table
        rowKey="id"
        bordered
        size="small"
        dataSource={displayNetifList}
        columns={tableColumns}
        loading={queryLoading}
        pagination={false}
      />
      {/* 编辑框 */}
      <DeviceNetifUpdate onOk={() => queryDeviceNetifs()} />
      {/* 统计曲线 */}
      {/* @ts-ignore */}
      {netifInfo?.id && <NetifStats netifHistogram={netifHistogram} netif={netifInfo} />}
    </Fragment>
  );
};

export default connect(
  ({ deviceNetifModel: { list, netifHistogram }, loading: { effects } }: ConnectState) => ({
    netifList: list,
    netifHistogram,
    queryLoading: effects['deviceNetifModel/queryDeviceNetifs'],
  }),
)(DeviceNetif);
