import { getLinkUrl, bytesToSize, convertBandwidth } from '@/utils/utils';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import numeral from 'numeral';
import { Link } from 'umi';
import DimensionsDirlldownMenu from '../../analysis/components/DimensionsDirlldownMenu';
import type { INetworkStatData } from '../../analysis/typings';
import { ESensorStatus } from '../../Configuration/Network/typings';
import DirlldownMenu from '../../GlobalSearch/DimensionsSearch/SeartchTabs/components/DirlldownMenu';
import { INetworkType } from '../typing';

/**
 * 计算 bytes/每秒
 */
export function getBytepsAvg(totalBytes: number = 0, startTime: string, endTime: string) {
  const timeDiff = moment(endTime).diff(startTime) / 1000;
  if (timeDiff !== 0) {
    return totalBytes / timeDiff;
  }
  return 0;
}

/**
 * 计算TCP连接成功率
 */
export function getTcpEstablishedSuccessRate(
  /** 成功次数 */
  tcpEstablishedSuccessCount: number,
  /** 失败次数 */
  tcpEstablishedFailedCount: number,
  /** 保留小数长度 */
  decimal: number = 2,
): number {
  if (tcpEstablishedSuccessCount + tcpEstablishedFailedCount !== 0) {
    return (
      (tcpEstablishedSuccessCount / (tcpEstablishedSuccessCount + tcpEstablishedFailedCount)) *
      100
    ).toFixed(decimal) as any;
  }
  return 0;
}

export function getColumns(
  startTime: string,
  endTime: string,
  isDimensionsTab?: boolean,
): ColumnProps<INetworkStatData>[] {
  return [
    {
      title: '网络',
      dataIndex: 'networkName',
      width: 150,
      fixed: 'left',
      ellipsis: true,
      render: (text, row) => {
        const { networkGroupId, networkId, status } = row;
        if (isDimensionsTab) {
          return (
            <DirlldownMenu
              label={`${text}${status === ESensorStatus.OFFLINE ? '(离线)' : ''}`}
              DrilldownMenu={
                isDimensionsTab ? (
                  <DimensionsDirlldownMenu
                    isNetworkList={true}
                    networkId={
                      networkGroupId ? `${networkGroupId}^networkGroup` : `${networkId}^network`
                    }
                  />
                ) : (
                  <div style={{ display: 'none' }} />
                )
              }
            />
          );
        }
        return (
          <div style={{ textAlign: 'left' }}>
            {networkId || networkGroupId ? (
              // /performance/network/bmNISYIBQbHW1CQj_faI/analysis
              <Link to={getLinkUrl(`/performance/network/${networkId || networkGroupId}/analysis`)}>
                {text}
                {status === ESensorStatus.OFFLINE ? '(离线)' : ''}
              </Link>
            ) : (
              text
            )}
          </div>
        );
      },
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 150,
    },
    {
      title: '总带宽（Mbps）',
      dataIndex: 'bandwidth',
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
      render: (_, record) => {
        if (record.totalBytes === undefined) {
          return 0;
        }
        return convertBandwidth(getBytepsAvg(record.totalBytes, startTime, endTime) * 8);
      },
    },
    {
      title: '峰值带宽',
      dataIndex: 'bytepsPeak',
      sorter: true,
      render: (text = 0, record) => {
        if (record?.type === INetworkType.GROUP) {
          return '';
        }
        return convertBandwidth(text * 8);
      },
    },
    {
      title: '上行带宽',
      dataIndex: 'upstreamByteps',
      sorter: false,
      render: (_, record) => {
        if (record.upstreamBytes === undefined) {
          return 0;
        }
        return convertBandwidth(getBytepsAvg(record.upstreamBytes, startTime, endTime) * 8);
      },
    },
    {
      title: '下行带宽',
      dataIndex: 'downstreamByteps',
      sorter: false,
      render: (_, record) => {
        if (record.downstreamBytes === undefined) {
          return 0;
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
          record.tcpEstablishedSuccessCounts || 0,
          record.tcpEstablishedFailCounts || 0,
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
}
