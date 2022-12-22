import type { EFilterGroupOperatorTypes } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { jumpNewPage } from '@/utils/utils';
import moment from 'moment';
import type { Dispatch } from 'react';
import { history } from 'umi';
import PacketAnalysis from '../../appliance/Packet/PacketAnalysis';
import Alert from '../components/Alert';
import Connection from '../components/Connection';
import Dashboard from '../components/Dashboard';
import type { ITabsState } from '../components/EditTabs';
import { newPanesFn } from '../components/EditTabs';
import Flow from '../components/Flow';
import FlowRecord from '../components/FlowRecord';
import Metadata from '../components/Metadata';
import Packet from '../components/Packet';
import Payload from '../components/Payload';
import Baseline from '../components/Payload/Baseline';
import Performance from '../components/Performance';
import PerformanceSetting from '../components/Performance/PerformanceSetting';
import Retransmission from '../components/Retransmission';
import TcpStats from '../components/TcpStats';
import { ENetworkTabs } from '../typing';

export const NetworkTabs = {
  [ENetworkTabs.DASHBOARD]: {
    title: '概览',
    defShow: true,
    content: <Dashboard />,
  },
  [ENetworkTabs.PAYLOAD]: {
    title: '负载量',
    defShow: true,
    content: <Payload />,
  },
  [ENetworkTabs.BASELINE]: {
    title: '基线配置',
    defShow: false,
    content: <Baseline />,
  },
  [ENetworkTabs.PERFORMANCE]: {
    title: '性能',
    defShow: true,
    content: <Performance />,
  },
  [ENetworkTabs.PERFORMANCESETTING]: {
    title: '响应时间配置',
    defShow: false,
    content: <PerformanceSetting />,
  },
  [ENetworkTabs.TCPSTATS]: {
    title: 'TCP指标',
    defShow: true,
    content: <TcpStats />,
  },
  [ENetworkTabs.ALERT]: {
    title: '告警消息',
    defShow: true,
    content: <Alert />,
  },
  [ENetworkTabs.METADATA]: {
    title: '应用层协议分析',
    defShow: false,
    content: <Metadata />,
  },
  [ENetworkTabs.FLOW]: {
    title: '流量分析',
    defShow: true,
    content: <Flow />,
  },
  [ENetworkTabs.RETRANSMISSION]: {
    title: '重传分析',
    defShow: false,
    content: <Retransmission />,
  },
  [ENetworkTabs.CONNECTION]: {
    title: '建连分析',
    defShow: false,
    content: <Connection />,
  },
  [ENetworkTabs.FLOWRECORD]: {
    title: '会话详单',
    defShow: false,
    content: <FlowRecord />,
  },
  [ENetworkTabs.PACKET]: {
    title: '数据包',
    defShow: false,
    content: <Packet />,
  },
  [ENetworkTabs.PACKETANALYSIS]: {
    title: '在线分析',
    defShow: false,
    content: <PacketAnalysis />,
  },
};

export const showRealTimeBtnTabs = [
  ENetworkTabs.DASHBOARD,
  ENetworkTabs.PAYLOAD,
  ENetworkTabs.PERFORMANCE,
  ENetworkTabs.TCPSTATS,
];

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export interface URLFilter {
  field: string;
  operator: string;
  operand: string;
}

export interface URLFilterGroup {
  operator: EFilterGroupOperatorTypes;
  group: (URLFilter | URLFilterGroup)[];
}

export const jumpToAnalysisTabNew = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: ENetworkTabs,
  info?: {
    filter?: (URLFilter | URLFilterGroup)[];
    autoJump?: boolean;
    networkId?: string;
    serviceId?: string;
    globalSelectedTime?: { startTime: number | string; endTime: number | string };
    flowId?: string;
    jumptab?: string;
    queryId?: string;
    pktAnalysisParams?: any;
    jumpNewTabs?: any[];
  },
) => {
  // 从网络分析中独立出去的页面
  const tabToNewPage = [ENetworkTabs.METADATA, ENetworkTabs.FLOWRECORD, ENetworkTabs.PACKET];
  // const { networkId, serviceId }: IUriParams = useParams();
  const {
    filter = [],
    autoJump,
    networkId,
    serviceId,
    globalSelectedTime,
    flowId,
    pktAnalysisParams,
    jumpNewTabs,
  } = info || {};

  const filterArr: URLFilter[] = [] as any;
  if (networkId) {
    filterArr.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId || '',
    });
  }
  if (serviceId) {
    filterArr.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId || '',
    });
  }
  console.log('globalSelectedTime', globalSelectedTime);
  const timestampTimes = {
    startTime: moment(globalSelectedTime?.startTime).valueOf(),
    endTime: moment(globalSelectedTime?.endTime).valueOf(),
  };
  console.log('timestampTimes', timestampTimes);
  // 处理要跳转到的页面
  const urlParamMap = {} as any;
  Object.values(ENetworkTabs).forEach((item) => {
    urlParamMap[item] = `${history.location.pathname}`;
  });
  const urlParam = {
    ...urlParamMap,
    [ENetworkTabs.FLOWRECORD]: '/flow-trace/flow-record',
    [ENetworkTabs.PACKET]: '/flow-trace/packet/statistics',
    [ENetworkTabs.PACKETANALYSIS]: '/flow-trace/packet/analysis',
    [ENetworkTabs.METADATA]: '/flow-trace/mata-data-detail',
  }[tabType];

  // 处理url上的时间问题
  const urlTimeInfo = {
    startTime: moment(history.location.query?.from).valueOf(),
    endTime: moment(history.location.query?.to).valueOf(),
  };
  const timeParamMap = {} as any;
  Object.values(ETimeType).forEach((item) => {
    timeParamMap[item] = `relative=true&timeType=${history.location.query?.timeType}`;
  });
  let timeIndex = 'def';
  if (!globalSelectedTime) {
    timeIndex = String(history.location.query?.timeType);
  }
  const timeParam = {
    ...timeParamMap,
    [ETimeType.RANGE]: `from=${urlTimeInfo.startTime}&relative=${urlTimeInfo.endTime}&timeType=${ETimeType.RANGE}&unit=${history.location.query?.unit}`,
    [ETimeType.CUSTOM]: `from=${urlTimeInfo.startTime}&to=${urlTimeInfo.endTime}&relative=false&timeType=${ETimeType.CUSTOM}`,
    def: `from=${timestampTimes?.startTime}&to=${timestampTimes?.endTime}&relative=false&timeType=${ETimeType.CUSTOM}`,
  }[timeIndex];

  let finnalFilter = filter;
  if (tabToNewPage.includes(tabType)) {
    finnalFilter = filter.concat(filterArr);
  }
  // 需要携带的filter
  const filterParam = finnalFilter ? `filter=${JSON.stringify(finnalFilter)}` : undefined;

  // 详单下钻需要携带flowid
  const flowIdParam = flowId ? `flowId=${flowId}` : undefined;

  // 需要下钻的标签
  const jumpTabParam = tabType ? `jumpTabs=${tabType}` : undefined;

  // 在线分析的参数
  const pktParam = pktAnalysisParams
    ? `packetFilterJson=${encodeURIComponent(JSON.stringify(pktAnalysisParams))}`
    : undefined;
  // 下钻后打开其他tab
  const newTabsParams = jumpNewTabs ? `newTabsParams=${JSON.stringify(jumpNewTabs)}` : undefined;

  // 所有参数
  const urlParams = [
    timeParam,
    filterParam,
    jumpTabParam,
    flowIdParam,
    pktParam,
    newTabsParams,
  ].filter((item) => item);

  if (!autoJump) {
    jumpNewPage(`${urlParam || history.location.pathname}?${urlParams.join('&')}`);
  } else {
    newPanesFn(
      {
        ...NetworkTabs[tabType],
        detail: {
          packetFilterJson: JSON.stringify(pktAnalysisParams),
          urlFilterString: '',
          networkId,
          flowId,
        },
        isNewTab: true,
      },
      dispatch,
      state,
      tabType,
      info,
    );
  }

  // const jumpToNewPage =
  //   JUMP_TO_NEW_TAB ||
  //   (storage.get('jumpToNew') === null ? false : storage.get('jumpToNew') === 'true');
};
