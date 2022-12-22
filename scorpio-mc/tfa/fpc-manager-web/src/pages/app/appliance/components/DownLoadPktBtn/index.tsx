import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import type { IUriParams } from '@/pages/app/analysis/typings';
import ajax from '@/utils/frame/ajax';
import { DownloadOutlined } from '@ant-design/icons';
import { Alert, Button, Dropdown, Menu, Modal, Progress, Space } from 'antd';
import { stringify } from 'qs';
import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { EMetadataProtocol } from '../../Metadata/typings';

export interface IDownloadPacketParams {
  queryId: string;
  startTime: string;
  endTime: string;
  sortProperty: string;
  sortDirection: string;
  sourceType: string;
  dsl: string;
  tableKey: string;
}

export enum EQueryLogToPkt {
  MetaData = 'metadata',
  FlowLog = 'flowlog',
}

export async function queryPkt(
  params: any,
  type: EQueryLogToPkt,
  metadataType?: EMetadataProtocol,
) {
  let urlType = metadataType;
  if (
    [EMetadataProtocol.POP3, EMetadataProtocol.IMAP, EMetadataProtocol.SMTP].includes(
      metadataType || ('' as any),
    )
  ) {
    urlType = EMetadataProtocol.MAIL;
  } else if (metadataType === EMetadataProtocol.DHCPV6) {
    urlType = EMetadataProtocol.DHCP;
  } else if (
    [EMetadataProtocol.ICMPV4, EMetadataProtocol.ICMPV6].includes(metadataType || ('' as any))
  ) {
    urlType = EMetadataProtocol.ICMP;
  }
  if (type === EQueryLogToPkt.FlowLog) {
    return ajax(`${API_VERSION_PRODUCT_V1}/appliance/flow-logs/packets?${stringify(params)}`);
  } else if (type === EQueryLogToPkt.MetaData) {
    return ajax(
      `${API_VERSION_PRODUCT_V1}/metadata/protocol-${urlType}-logs/packets?${stringify(params)}`,
    );
  }
}

export enum EStatus {
  SUCCESS = 'success',
  PROGRESSING = 'progressing',
  FAIL = 'fail',
}

export interface IPacketsDownloadPreview {
  queryId: string;
  status: EStatus;
  progress: number;
  result: string;
  truncate: number;
}

export enum EPacketFileType {
  PCAP = 'pcap',
  PCAPNG = 'pcapng',
}

export const packetFileType = {
  [EPacketFileType.PCAP]: { label: '下载PCAP', key: EPacketFileType.PCAP },
  [EPacketFileType.PCAPNG]: { label: '下载PCAPNG', key: EPacketFileType.PCAPNG },
};

const MaxPacketSum = 10000;

interface Props {
  queryFn: (...args: any[]) => Promise<IAjaxResponseFactory<IPacketsDownloadPreview>>;
  queryId?: string;
  // 这里时间上传的都是类似会话详单total, 流量分析表格total, 等等，并不是真正的数据包总数
  totalPkt: number;
  params?: any;
  loading?: boolean;
}

export default function DownLoadPktBtn(props: Props) {
  const { queryFn, queryId: defQueryId, totalPkt = 0, params, loading } = props;
  // console.log('pkt id ', defQueryId);
  const [noCare, setNoCare] = useState(false);
  const [progress, setProgress] = useState(-1);
  const { networkId }: IUriParams = useParams();
  const [queryLoading, setQueryLoading] = useState(false);
  const queryIds = useMemo(() => {
    setNoCare(false);
    return {
      [EPacketFileType.PCAP]: defQueryId || uuidv1(),
      [EPacketFileType.PCAPNG]: uuidv1(),
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defQueryId, params]);
  // 上一次的查询ID
  const [askQueryIds, setAskQueryIds] = useState({
    [EPacketFileType.PCAP]: 'defPcap',
    [EPacketFileType.PCAPNG]: 'defPcapng',
  });
  const [loadPktState, setLoadPktState] = useState<{ status: EStatus } | undefined>();
  const [downloadPktUrl, setDownloadPktUrl] = useState<string | undefined | null>();
  const [isModalVisible, setIsModalVisible] = useState(false);
  // 当前要下载的数据包类型
  const downloadPKTFileTypeRef = useRef(EPacketFileType.PCAP);
  const soBigPacket = totalPkt > MaxPacketSum;
  const [isSplitPkt, setIsSplitPkt] = useState(true);

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = () => {
    setIsModalVisible(false);
    window.open(window.location.origin + downloadPktUrl);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    setProgress(0);
  };

  useEffect(() => {
    setProgress(-1);
    setLoadPktState(undefined);
  }, [props]);

  const query = () => {
    // console.log('downloadPKTFileTypeRef.current', downloadPKTFileTypeRef.current);
    const tmpQueryId = queryIds[downloadPKTFileTypeRef.current];
    setQueryLoading(true);
    queryFn({
      queryId: tmpQueryId,
      networkId,
      fileType: downloadPKTFileTypeRef.current,
    }).then((res: { success: boolean; result: IPacketsDownloadPreview }) => {
      const { success, result } = res;
      setQueryLoading(false);
      let splitPktFlag = true;
      console.log('tmp queryid', tmpQueryId, askQueryIds[downloadPKTFileTypeRef.current]);
      if (result.truncate && tmpQueryId !== askQueryIds[downloadPKTFileTypeRef.current]) {
        setAskQueryIds((pre) => ({
          ...pre,
          [packetFileType[downloadPKTFileTypeRef.current].key]: tmpQueryId,
        }));
        splitPktFlag = window.confirm(
          '当前数据包大小大于最大数据包下载容量（最大容量1GB），是否继续下载？',
        );
      }
      setIsSplitPkt(splitPktFlag);
      if (result.status === EStatus.SUCCESS) {
        setDownloadPktUrl(result.result);
      }
      if (success) {
        setProgress(result.progress);
        setLoadPktState({ status: result.status });
      } else {
        handleCancel();
      }
    });
  };

  useEffect(() => {
    if (
      !isModalVisible ||
      !loadPktState ||
      loadPktState?.status === EStatus.SUCCESS ||
      loadPktState?.status === EStatus.FAIL ||
      !isSplitPkt
    ) {
      return;
    }
    setTimeout(() => {
      query();
    }, 1000);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadPktState, askQueryIds]);

  const modalRender = (statusFlag: boolean) => {
    if (statusFlag) {
      return <Alert message={'获取数据包失败'} type="error" />;
    }
    if (soBigPacket && !loadPktState) {
      return (
        <Alert
          message={`最多下载前${MaxPacketSum}条，当前数据为${totalPkt}条`}
          type="warning"
          action={
            <Space>
              <Button
                loading={queryLoading}
                size="small"
                onClick={() => {
                  query();
                  setNoCare(true);
                }}
                type="ghost"
              >
                继续创建下载
              </Button>
            </Space>
          }
        />
      );
    } else {
      return (
        <Progress
          percent={progress}
          // status={loadPktState?.status === EStatus.FAIL ? 'exception' : 'active'}
        />
      );
    }
  };

  return (
    <Fragment>
      <Dropdown
        disabled={!totalPkt}
        overlay={
          <Menu
            onClick={(e) => {
              showModal();
              setDownloadPktUrl(null);
              downloadPKTFileTypeRef.current = packetFileType[e.key].key;
              if (!soBigPacket || noCare) {
                query();
              }
            }}
          >
            {Object.keys(packetFileType).map((pktKey) => {
              const pktItem = packetFileType[pktKey];
              return <Menu.Item key={pktItem.key}>{pktItem.label}</Menu.Item>;
            })}
          </Menu>
        }
        trigger={['click']}
      >
        <Button
          // onClick={() => {
          //   showModal();
          //   if (!soBigPacket || noCare) {
          //     query();
          //   }
          // }}
          icon={<DownloadOutlined />}
          type="primary"
          disabled={!totalPkt}
          loading={loading}
        >
          下载数据包
        </Button>
      </Dropdown>
      <Modal
        title="下载数据包"
        visible={isModalVisible}
        onOk={handleOk}
        okText={'下载'}
        okButtonProps={{ disabled: progress < 100 && !downloadPktUrl }}
        onCancel={handleCancel}
        maskClosable={false}
        destroyOnClose={true}
      >
        {modalRender(loadPktState?.status === EStatus.FAIL)}
      </Modal>
    </Fragment>
  );
}
