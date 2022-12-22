import EnhancedTable from '@/components/EnhancedTable';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { bytesToSize, convertBandwidth, getLinkUrl, snakeCase } from '@/utils/utils';
import { Col, Row } from 'antd';
import type { ColumnProps, TablePaginationConfig } from 'antd/lib/table';
import numeral from 'numeral';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, Link } from 'umi';
import NetworkFlowMetric from '../../components/NetworkFlowMetric';
import NetworkPacketStoreMetric from '../../components/NetworkPacketStoreMetric';
import NetworkSessionMetricsChart from '../../components/NetworkSessionMetricsChart';
import type { INetworkStatData } from '../../typings';
import { ESortDirection } from '../../typings';
import { getBytepsAvg, getTcpEstablishedSuccessRate } from '../../Service/List';
import { ONE_KILO_1000 } from '@/common/dict';

let startTime = '';
let endTime = '';

export const columns: ColumnProps<INetworkStatData>[] = [
  {
    title: '网络',
    dataIndex: 'networkName',
    width: 150,
    fixed: 'left',
    ellipsis: true,
    render: (text, row) => {
      const { networkId } = row;
      return (
        <div style={{ textAlign: 'left' }}>
          {networkId ? (
            <Link to={getLinkUrl(`/analysis/performance/network/${row.networkId}/dashboard`)}>{text}</Link>
          ) : (
            text
          )}
        </div>
      );
    },
  },
  {
    title: '总带宽（Mbps）',
    dataIndex: 'networkBandwidth',
    width: 150,
  },
  {
    title: '总流量',
    dataIndex: 'totalBytes',
    sorter: true,
    width: 150,
    render: (text = 0) => bytesToSize(text),
  },
  {
    title: '平均带宽利用率(%)',
    dataIndex: 'averageBandwidthUsed',
    width: 150,
    render: (_, record: any) => {
      if (record?.startTime && record?.endTime) {
        startTime = record.startTime;
        endTime = record.endTime;
      }
      const used = getBytepsAvg(record?.totalBytes || 0, startTime, endTime) * 8;
      const total = record?.networkBandwidth || 0;
      if (total) {
        return `${((used / (total * ONE_KILO_1000 * ONE_KILO_1000)) * 100).toFixed(2)} %`;
      }
      return '-';
    },
  },
  {
    title: '上行流量',
    dataIndex: 'upstreamBytes',
    sorter: true,
    width: 150,
    render: (text = 0) => bytesToSize(text),
  },
  {
    title: '下行流量',
    dataIndex: 'downstreamBytes',
    sorter: true,
    width: 150,
    render: (text = 0) => bytesToSize(text),
  },
  {
    title: '平均带宽',
    dataIndex: 'bytepsAvg',
    sorter: false,
    render: (_, record: any) => {
      if (record?.startTime && record?.endTime) {
        startTime = record.startTime;
        endTime = record.endTime;
      }
      return convertBandwidth(getBytepsAvg(record.totalBytes, startTime, endTime) * 8);
    },
  },
  {
    title: '峰值带宽',
    dataIndex: 'bytepsPeak',
    sorter: true,
    render: (text = 0) => convertBandwidth(text * 8),
  },
  {
    title: '上行带宽',
    dataIndex: 'upstreamByteps',
    sorter: false,
    render: (_, record: any) => {
      if (record?.startTime && record?.endTime) {
        startTime = record.startTime;
        endTime = record.endTime;
      }
      return convertBandwidth(getBytepsAvg(record.upstreamBytes, startTime, endTime) * 8);
    },
  },
  {
    title: '下行带宽',
    dataIndex: 'downstreamByteps',
    sorter: false,
    render: (_, record: any) => {
      if (record?.startTime && record?.endTime) {
        startTime = record.startTime;
        endTime = record.endTime;
      }
      return convertBandwidth(getBytepsAvg(record.downstreamBytes, startTime, endTime) * 8);
    },
  },
  {
    title: '总包数',
    dataIndex: 'totalPackets',
    sorter: true,
    width: 150,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '上行包数',
    dataIndex: 'upstreamPackets',
    sorter: true,
    width: 150,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '下行包数',
    dataIndex: 'downstreamPackets',
    sorter: true,
    width: 150,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '峰值包速率',
    dataIndex: 'packetpsPeak',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '新建会话数',
    dataIndex: 'establishedSessions',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '最大并发会话数',
    dataIndex: 'concurrentSessions',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '客户端平均网络时延(ms)',
    dataIndex: 'tcpClientNetworkLatencyAvg',
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '服务器平均网络时延(ms)',
    dataIndex: 'tcpServerNetworkLatencyAvg',
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '服务器平均响应时延(ms)',
    dataIndex: 'serverResponseLatencyAvg',
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '客户端重传包数',
    dataIndex: 'tcpClientRetransmissionPackets',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '客户端重传率',
    dataIndex: 'tcpClientRetransmissionRate',
    render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
  },
  {
    title: '服务器重传包数',
    dataIndex: 'tcpServerRetransmissionPackets',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '服务器重传率',
    dataIndex: 'tcpServerRetransmissionRate',
    render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
  },
  {
    title: '客户端零窗口包数',
    dataIndex: 'tcpClientZeroWindowPackets',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: '服务器零窗口包数',
    dataIndex: 'tcpServerZeroWindowPackets',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: 'TCP连接成功率',
    dataIndex: 'tcpEstablishedSuccessRate',
    sorter: false,
    render: (_, record) =>
      `${getTcpEstablishedSuccessRate(
        record.tcpEstablishedSuccessCounts,
        record.tcpEstablishedFailCounts,
      )}%`,
  },
  {
    title: 'TCP建连成功数',
    dataIndex: 'tcpEstablishedSuccessCounts',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
  {
    title: 'TCP建连失败数',
    dataIndex: 'tcpEstablishedFailCounts',
    sorter: true,
    render: (text = 0) => numeral(text).format('0,0'),
  },
];

interface INetworkListProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  allNetworkStatData: INetworkStatData[];
  globalSelectedTime: Required<IGlobalTime>;
  beforeOldestPacketArea: any;
}
const NetworkList: React.FC<INetworkListProps> = ({
  dispatch,
  queryLoading,
  globalSelectedTime,
  allNetworkStatData,
  beforeOldestPacketArea,
}) => {
  // 当前排序的字段
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  // 当前排序的方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryAllNetworkStat',
      payload: {
        sortProperty: snakeCase(sortProperty),
        sortDirection,
        startTime: globalSelectedTime.startTime,
        endTime: globalSelectedTime.endTime,
        interval: globalSelectedTime.interval,
      },
    });
    startTime = globalSelectedTime.startTime;
    endTime = globalSelectedTime.endTime;
  }, [
    dispatch,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    sortDirection,
    sortProperty,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
    let newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    const newSortProperty = sorter.field;
    // 如果当前排序字段不是现在的字段，默认是倒序
    if (newSortProperty !== sortProperty) {
      newSortDirection = ESortDirection.DESC;
    }

    setSortDirection(newSortDirection);
    setSortProperty(newSortProperty);
  };

  const tableColumns = useMemo(() => {
    return columns.map((col) => ({
      ...col,
      key: col.dataIndex,
      sortOrder: sortProperty === col.dataIndex ? `${sortDirection}end` : false,
      align: 'center',
    })) as ColumnProps<INetworkStatData>[];
  }, [sortProperty, sortDirection]);

  const tableExpandedKeys = useMemo(() => {
    return allNetworkStatData.map((item) => item.networkId);
  }, [allNetworkStatData]);

  return (
    <>
      <Row gutter={10} style={{ marginBottom: 10 }}>
        <Col span={24}>
          <NetworkPacketStoreMetric />
        </Col>
      </Row>
      <Row gutter={10}>
        <Col span={12}>
          <NetworkFlowMetric markArea={beforeOldestPacketArea} />
        </Col>
        <Col span={12}>
          <NetworkSessionMetricsChart markArea={beforeOldestPacketArea} />
        </Col>
      </Row>
      {allNetworkStatData.length && (
        <EnhancedTable<INetworkStatData>
          sortProperty={sortProperty}
          sortDirection={`${sortDirection}end`}
          tableKey="all-network-stat-table"
          rowKey="networkId"
          loading={queryLoading}
          columns={tableColumns}
          dataSource={allNetworkStatData}
          onChange={handleTableChange}
          pagination={false}
          expandable={{ defaultExpandAllRows: true, defaultExpandedRowKeys: tableExpandedKeys }}
          scroll={{ x: 'max-content' }}
        />
      )}
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    npmdModel: { allNetworkStatData, beforeOldestPacketArea },
    appModel: { globalSelectedTime },
  }: ConnectState) => ({
    queryLoading: effects['npmdModel/queryAllNetworkStat'],
    allNetworkStatData,
    globalSelectedTime,
    beforeOldestPacketArea,
  }),
)(NetworkList);
