/* eslint-disable react-hooks/exhaustive-deps */
import { bytesToSize, convertBandwidth, getLinkUrl } from '@/utils/utils';
import { QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Input, Popconfirm, Space } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import numeral from 'numeral';
import { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, Link } from 'umi';
import OfflinePcapUpload from './components/OfflinePcapUpload';
import styles from './index.less';
import type { IOfflinePcapData, IPcapConnectState, IPcapState } from '../typing';
import { EPcapState } from '../typing';
import EnhancedTable from '@/components/EnhancedTable';
import { ONE_KILO_1024 } from '@/common/dict';
import { DEFAULT_PAGE_SIZE_KEY } from '@/common/app';
import storage from '@/utils/frame/storage';
import { useInterval } from 'ahooks';

const { Search } = Input;

export interface IPcapProps {
  queryPcapList: any;
  deletePcapFile: any;
  queryLoading: boolean | undefined;
  pcapState: IPcapState;
}

function Pcap(props: IPcapProps) {
  const { queryPcapList, deletePcapFile, pcapState, queryLoading } = props;
  const [fileName, setFileName] = useState('');

  const handleDeletePcapFile = (record: IOfflinePcapData) => {
    deletePcapFile({ id: record.id }).then((success: boolean) => {
      if (success) {
        const { number, size, pcapList } = pcapState;
        // 如果删除前只有1条数据，并且页码不是1，就请求上一页数据
        const newPage = pcapList.length === 1 && number > 0 ? number - 1 : number;
        queryPcapList(fileName, newPage, size);
      }
    });
  };

  const columns: ColumnProps<IOfflinePcapData>[] = [
    {
      title: '文件名称',
      dataIndex: 'name',
      key: 'name',
      align: 'center',
      width: 200,
      fixed: 'left',
      ellipsis: true,
      render: (text, row) => {
        const { id, status } = row;
        return (
          <div style={{ textAlign: 'left' }}>
            {id && status === EPcapState.COMPLETE ? (
              <Link to={getLinkUrl(`/analysis/offline/${row.id}/dashboard`)}>{text}</Link>
            ) : (
              text
            )}
          </div>
        );
      },
    },
    {
      title: '数据开始时间',
      dataIndex: 'packetStartTime',
      key: 'packetStartTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '数据结束时间',
      dataIndex: 'packetEndTime',
      key: 'packetEndTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '文件大小',
      dataIndex: 'size',
      key: 'size',
      width: 150,
      align: 'center',
      render: (text = 0) => bytesToSize(text, 3, ONE_KILO_1024),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      align: 'center',
      width: 150,
      render: (text = '') => {
        switch (text) {
          case EPcapState.WAITANALYSIS:
            return '等待分析';
          case EPcapState.EXCEPTION:
            return '分析异常';
          case EPcapState.ANALYZING:
            return '正在分析';
          case EPcapState.COMPLETE:
            return '分析完成';
          case EPcapState.DELETED:
            return '已删除';
          default:
            return '-';
        }
      },
    },
    {
      title: '分析进度',
      dataIndex: 'executionProgress',
      key: 'executionProgress',
      width: 150,
      align: 'center',
      render: (text = 0) => `${numeral(text.toFixed(2)).value()}%`,
    },
    {
      title: '峰值带宽',
      dataIndex: ['executionResult', 'bytepsPeak'],
      key: 'bytepsPeak',
      width: 150,
      align: 'center',
      render: (text = 0) => convertBandwidth(text * 8),
    },
    {
      title: '总流量',
      dataIndex: ['executionResult', 'totalBytes'],
      key: 'totalBytes',
      width: 150,
      align: 'center',
      render: (text = 0) => bytesToSize(text),
    },
    {
      title: '峰值包速率',
      dataIndex: ['executionResult', 'packetpsPeak'],
      key: 'packetpsPeak',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '总包数',
      dataIndex: ['executionResult', 'totalPackets'],
      key: 'totalPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '新建会话数',
      dataIndex: ['executionResult', 'establishedSessions'],
      key: 'establishedSessions',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '最大并发会话数',
      dataIndex: ['executionResult', 'concurrentSessions'],
      key: 'concurrentSessions',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端平均网络时延(ms)',
      dataIndex: ['executionResult', 'tcpClientNetworkLatencyAvg'],
      key: 'tcpClientNetworkLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器平均网络时延(ms)',
      dataIndex: ['executionResult', 'tcpServerNetworkLatencyAvg'],
      key: 'tcpServerNetworkLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器平均响应时延(ms)',
      dataIndex: ['executionResult', 'serverResponseLatencyAvg'],
      key: 'serverResponseLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端重传包数',
      dataIndex: ['executionResult', 'tcpClientRetransmissionPackets'],
      key: 'tcpClientRetransmissionPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端重传率',
      dataIndex: ['executionResult', 'tcpClientRetransmissionRate'],
      key: 'tcpClientRetransmissionRate',
      width: 150,
      align: 'center',
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '服务器重传包数',
      dataIndex: ['executionResult', 'tcpServerRetransmissionPackets'],
      key: 'tcpServerRetransmissionPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器重传率',
      dataIndex: ['executionResult', 'tcpServerRetransmissionRate'],
      key: 'tcpServerRetransmissionRate',
      width: 150,
      align: 'center',
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '客户端零窗口包数',
      dataIndex: ['executionResult', 'tcpClientZeroWindowPackets'],
      key: 'tcpClientZeroWindowPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器零窗口包数',
      dataIndex: ['executionResult', 'tcpServerZeroWindowPackets'],
      key: 'tcpServerZeroWindowPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: 'TCP建连成功数',
      dataIndex: ['executionResult', 'tcpEstablishedSuccessCounts'],
      key: 'tcpEstablishedSuccessCounts',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: 'TCP建连失败数',
      dataIndex: ['executionResult', 'tcpEstablishedFailCounts'],
      key: 'tcpEstablishedFailCounts',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      fixed: 'right',
      width: 100,
      align: 'center',
      render: (text, record) => {
        return (
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDeletePcapFile(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <a>删除</a>
          </Popconfirm>
        );
      },
    },
  ];

  useEffect(() => {
    queryPcapList('', 0, pcapState.size);
  }, []);

  useInterval(
    () => {
      queryPcapList('', pcapState.number, pcapState.size);
    },
    pcapState.pcapList.some(
      (pcap) => pcap.status === EPcapState.WAITANALYSIS || pcap.status === EPcapState.ANALYZING,
    )
      ? 3 * 1000
      : null,
  );

  const handleSearch = (_fileName: string) => {
    setFileName(_fileName);
    queryPcapList(_fileName, pcapState.number, pcapState.size);
  };

  const handleRefresh = () => {
    const { number, size } = pcapState;
    queryPcapList(fileName, number, size);
  };
  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    queryPcapList(fileName, page - 1, pageSize || 10);
    storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
  };

  return (
    <>
      <EnhancedTable<IOfflinePcapData>
        tableKey="offline-pcap-list"
        columns={columns}
        rowKey={'id'}
        loading={queryLoading}
        dataSource={pcapState.pcapList}
        size="small"
        bordered
        scroll={{ x: 1500 }}
        extraTool={
          <div className={styles['pcap-header']}>
            <Space>
              <Search
                placeholder="请输入离线数据包名称关键字"
                onSearch={handleSearch}
                enterButton
                style={{ width: 300 }}
              />
              <OfflinePcapUpload />
              <Button type="primary" icon={<ReloadOutlined />} onClick={handleRefresh}>
                刷新
              </Button>
            </Space>
          </div>
        }
        pagination={{
          onChange: onPaginationChange,
          total: pcapState.totalElements,
        }}
      />
    </>
  );
}
export default connect(
  ({ offlinePcapModel: { pcapState }, loading: { effects } }: IPcapConnectState) => {
    return {
      pcapState,
      queryLoading: effects['offlinePcapModel/queryPcapList'],
    };
  },
  (dispatch: Dispatch) => ({
    queryPcapList: (name: string, page?: number, pageSize?: number) => {
      return dispatch({
        type: 'offlinePcapModel/queryPcapList',
        payload: {
          name,
          page,
          pageSize,
        },
      });
    },
    deletePcapFile: (delItem: IOfflinePcapData) => {
      return dispatch({ type: 'offlinePcapModel/deletePcapFile', payload: { ...delItem } });
    },
  }),
)(Pcap);
