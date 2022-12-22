/* eslint-disable react-hooks/exhaustive-deps */
import { bytesToSize, convertBandwidth, getLinkUrl, parseObjJson } from '@/utils/utils';
import { QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, Input, Popconfirm, Space } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import numeral from 'numeral';
import { useContext, useEffect, useMemo, useState } from 'react';
import { connect, Link, history } from 'umi';
import OfflinePcapUpload from './components/OfflinePcapUpload';
import styles from './index.less';
import type { IOfflinePcapData } from '../typing';
import { ESource } from '../typing';
import { EPcapState } from '../typing';
import EnhancedTable from '@/components/EnhancedTable';
import { ONE_KILO_1024 } from '@/common/dict';
import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import storage from '@/utils/frame/storage';
import { deleteOfflineSubTask, queryOfflineSubtaskList } from '../service';
import { EditOfflineTabsContext, OfflineTabsContext } from '../OfflineTaskTab';
import { clearShareInfo } from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import { useInterval } from 'ahooks';

const { Search } = Input;

export interface IPcapProps {
  showUpLoadBtn?: boolean;
}

function List(props: IPcapProps) {
  const { showUpLoadBtn = true } = props;
  const [state, offlineDispatch] = useContext<any>(OfflineTabsContext);
  const [editTabObj] = useContext(EditOfflineTabsContext);
  useEffect(() => {
    clearShareInfo(offlineDispatch);
  }, [offlineDispatch]);
  // console.log('shareInfo', shareInfo, state);

  const subTaskDetail = useMemo(() => {
    const tmpDetail = state.panes.find((item: any) => state.activeKey === item.key);
    return tmpDetail?.paneDetail;
  }, []);

  const [fileName, setFileName] = useState('');
  const [queryLoading, setQueryLoading] = useState(false);
  const [offlineListDetail, setOfflineListDetail] = useState({
    list: [] as any[],
    totalPages: 0,
    totalElements: 0,
    number: 0,
    pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
  });

  const queryOfflineList = (param: {
    name?: string;
    page: number;
    pageSize: number;
    taskId?: string;
    source?: string;
  }) => {
    if (subTaskDetail) {
      param.taskId = subTaskDetail.record.id;
      param.source = subTaskDetail.record.source;
    } else {
      param.source = ESource.UPLOAD;
    }
    setQueryLoading(true);
    queryOfflineSubtaskList(param).then((res) => {
      const { success, result } = res;
      if (success) {
        setOfflineListDetail({
          list: result.content.map((item: any) => {
            const tmp = { ...item };
            const executionResult = parseObjJson(item.executionResult);
            return { ...tmp, ...executionResult };
          }),
          totalElements: result.totalElements,
          totalPages: result.totalPages,
          number: result.number,
          pageSize: result.size,
        });
      }
      setQueryLoading(false);
    });
  };

  useEffect(() => {
    queryOfflineList({ page: 0, pageSize: offlineListDetail.pageSize });
  }, []);

  useInterval(
    () => {
      queryOfflineList({
        page: offlineListDetail.number,
        pageSize: offlineListDetail.pageSize,
      });
    },
    offlineListDetail.list.some(
      (pcap: any) =>
        pcap?.status === EPcapState.WAITANALYSIS || pcap?.status === EPcapState.ANALYZING,
    )
      ? 3 * 1000
      : undefined,
  );

  const handleDeletePcapFile = (record: IOfflinePcapData) => {
    deleteOfflineSubTask({ id: record.id }).then((success: boolean) => {
      if (success) {
        const { number, pageSize, list, totalElements } = offlineListDetail;
        // TODO: 如果当前页是0，总数量是1，删除后关闭当前tab
        if (false && !number && totalElements === 1) {
          editTabObj.current.remove(editTabObj.current.state.activeKey);
          return;
        }
        // 如果删除前只有1条数据，并且页码不是1，就请求上一页数据
        const newPage = list.length === 1 && number > 0 ? number - 1 : number;
        queryOfflineList({ name: fileName, page: newPage, pageSize });
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
            {id && (status === EPcapState.COMPLETE || status === EPcapState.CONTINUE) ? (
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
      dataIndex: 'statusText',
      key: 'statusText',
      align: 'center',
      width: 150,
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
      dataIndex: 'bytepsPeak',
      key: 'bytepsPeak',
      width: 150,
      align: 'center',
      render: (text = 0) => convertBandwidth(text * 8),
    },
    {
      title: '总流量',
      dataIndex: 'totalBytes',
      key: 'totalBytes',
      width: 150,
      align: 'center',
      render: (text = 0) => bytesToSize(text),
    },
    {
      title: '峰值包速率',
      dataIndex: 'packetpsPeak',
      key: 'packetpsPeak',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '总包数',
      dataIndex: 'totalPackets',
      key: 'totalPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '新建会话数',
      dataIndex: 'establishedSessions',
      key: 'establishedSessions',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '最大并发会话数',
      dataIndex: 'concurrentSessions',
      key: 'concurrentSessions',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端平均网络时延(ms)',
      dataIndex: 'tcpClientNetworkLatencyAvg',
      key: 'tcpClientNetworkLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器平均网络时延(ms)',
      dataIndex: 'tcpServerNetworkLatencyAvg',
      key: 'tcpServerNetworkLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器平均响应时延(ms)',
      dataIndex: 'serverResponseLatencyAvg',
      key: 'serverResponseLatencyAvg',
      width: 180,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端重传包数',
      dataIndex: 'tcpClientRetransmissionPackets',
      key: 'tcpClientRetransmissionPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '客户端重传率',
      dataIndex: 'tcpClientRetransmissionRate',
      key: 'tcpClientRetransmissionRate',
      width: 150,
      align: 'center',
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '服务器重传包数',
      dataIndex: 'tcpServerRetransmissionPackets',
      key: 'tcpServerRetransmissionPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器重传率',
      dataIndex: 'tcpServerRetransmissionRate',
      key: 'tcpServerRetransmissionRate',
      width: 150,
      align: 'center',
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '客户端零窗口包数',
      dataIndex: 'tcpClientZeroWindowPackets',
      key: 'tcpClientZeroWindowPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: '服务器零窗口包数',
      dataIndex: 'tcpServerZeroWindowPackets',
      key: 'tcpServerZeroWindowPackets',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: 'TCP建连成功数',
      dataIndex: 'tcpEstablishedSuccessCounts',
      key: 'tcpEstablishedSuccessCounts',
      width: 150,
      align: 'center',
      render: (text = 0) => numeral(text).format('0,0'),
    },
    {
      title: 'TCP建连失败数',
      dataIndex: 'tcpEstablishedFailCounts',
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
      align: 'center',
      width: 250,
      render: (text, record) => {
        return (
          <>
            <Button
              type={'link'}
              size={'small'}
              onClick={() => {
                history.push(
                  getLinkUrl(
                    `/analysis/trace/transmit-task/create?pcapFileId=${record.id}&pcapFileName=${record.name}`,
                  ),
                );
              }}
            >
              创建全包下载任务
            </Button>
            <Divider type="vertical" />
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDeletePcapFile(record)}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type={'link'} size={'small'}>
                删除
              </Button>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  const handleSearch = (name: string) => {
    setFileName(name);
    queryOfflineList({ name, page: 0, pageSize: offlineListDetail.pageSize });
  };

  const handleRefresh = () => {
    const { number, pageSize } = offlineListDetail;
    queryOfflineList({ name: fileName, page: number, pageSize });
  };
  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    queryOfflineList({ name: fileName, page: page - 1, pageSize: pageSize || 10 });
    storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
  };

  return (
    <>
      <EnhancedTable<IOfflinePcapData>
        tableKey="offline-pcap-list"
        columns={columns}
        rowKey={'id'}
        loading={queryLoading}
        dataSource={offlineListDetail.list}
        size="small"
        bordered
        scroll={{ x: 1500 }}
        extraTool={
          <div className={styles['pcap-header']}>
            <Space>
              <Search
                placeholder="搜索文件名称"
                onSearch={handleSearch}
                enterButton
                style={{ width: 300 }}
              />
              <OfflinePcapUpload show={showUpLoadBtn} refresh={handleRefresh} />
              <Button type="primary" icon={<ReloadOutlined />} onClick={handleRefresh}>
                刷新
              </Button>
            </Space>
          </div>
        }
        pagination={{
          onChange: onPaginationChange,
          total: offlineListDetail.totalElements,
        }}
      />
    </>
  );
}
export default connect()(List);
