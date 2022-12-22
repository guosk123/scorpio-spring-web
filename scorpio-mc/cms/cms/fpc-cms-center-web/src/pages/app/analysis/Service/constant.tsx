import { EServiceTabs } from './typing';
import Dashboard from '@/pages/app/Network/components/Dashboard';
import Payload from '../../Network/components/Payload';
import Performance from '../../Network/components/Performance';
import TcpStats from '../../Network/components/TcpStats';
import ApplicationTopology from './components/ApplicationTopology';
import type { Dispatch } from 'react';
import type { ITabsState } from '../../Network/components/EditTabs';
import { newPanesFn } from '../../Network/components/EditTabs';
import Metadata from '../../Network/components/Metadata';
import Retransmission from '../../Network/components/Retransmission';
import Connection from '../../Network/components/Connection';
import FlowRecord from '../../Network/components/FlowRecord';
import Packet from '@/pages/app/appliance/Packet';
import Alert from '../../Network/components/Alert';
import Flow from '../../Network/components/Flow';
import PacketAnalysis from '../../appliance/Packet/PacketAnalysis';
import Baseline from '../../Network/components/Payload/Baseline';
import PerformanceSetting from '../../Network/components/Performance/PerformanceSetting';
import storage from '@/utils/frame/storage';
import { jumpNewPage } from '@/utils/utils';
import { history } from 'umi';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { JUMP_TO_NEW_TAB } from '../../Network/components/LinkToAnalysis';
import { jumpToAnalysisTabNew } from '../../Network/Analysis/constant';

export const ServiceTabs = {
  [EServiceTabs.DASHBOARD]: {
    title: '概览',
    defShow: true,
    content: <Dashboard />,
  },
  [EServiceTabs.PAYLOAD]: {
    title: '负载量',
    defShow: true,
    content: <Payload />,
  },
  [EServiceTabs.BASELINE]: {
    title: '基线配置',
    defShow: false,
    content: <Baseline />,
  },
  [EServiceTabs.PERFORMANCE]: {
    title: '性能',
    defShow: true,
    content: <Performance />,
  },
  [EServiceTabs.TCPSTATS]: {
    title: 'TCP指标',
    defShow: true,
    content: <TcpStats />,
  },
  [EServiceTabs.TOPOLOGY]: {
    title: '业务路径',
    defShow: true,
    content: <ApplicationTopology />,
  },
  [EServiceTabs.ALERT]: {
    title: '告警消息',
    defShow: true,
    content: <Alert />,
  },
  [EServiceTabs.METADATA]: {
    title: '应用层协议分析',
    defShow: false,
    content: <Metadata />,
  },
  [EServiceTabs.FLOW]: {
    title: '流量分析',
    defShow: true,
    content: <Flow />,
  },
  [EServiceTabs.RETRANSMISSION]: {
    title: '重传分析',
    defShow: false,
    content: <Retransmission />,
  },
  [EServiceTabs.CONNECTION]: {
    title: '建连分析',
    defShow: false,
    content: <Connection />,
  },
  [EServiceTabs.FLOWRECORD]: {
    title: '会话详单',
    defShow: false,
    content: <FlowRecord />,
  },
  [EServiceTabs.PACKET]: {
    title: '数据包',
    defShow: false,
    content: <Packet />,
  },
  [EServiceTabs.PACKETANALYSIS]: {
    title: '在线分析',
    defShow: false,
    content: <PacketAnalysis />,
  },
  [EServiceTabs.PERFORMANCESETTING]: {
    title: '响应时间配置',
    defShow: false,
    content: <PerformanceSetting />,
  },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export const jumpToSericeAnalysisTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EServiceTabs,
  info?: any,
  autoJump?: boolean,
  networkId?: string,
) => {
  jumpToAnalysisTabNew(state, dispatch, tabType as any, info);
  const jumpToNewPage =
    JUMP_TO_NEW_TAB ||
    (storage.get('jumpToNew') === null ? false : storage.get('jumpToNew') === 'true');
  if (jumpToNewPage && !autoJump) {
    const tmpJumpInfo = { ...info, networkId };
    if (tmpJumpInfo && tmpJumpInfo.brushTimeInfo) {
      const { brushTimeInfo: brushTime } = tmpJumpInfo;
      delete tmpJumpInfo.brushTimeInfo;
      jumpNewPage(
        `${history.location.pathname}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${
          ETimeType.CUSTOM
        }&jumpTabs=${tabType}&shareInfo=${JSON.stringify(tmpJumpInfo || [])}`,
      );
    }
    jumpNewPage(
      `${history.location.pathname}?jumpTabs=${tabType}&shareInfo=${JSON.stringify(
        tmpJumpInfo || [],
      )}`,
    );
  } else {
    newPanesFn(
      {
        ...ServiceTabs[tabType],
        detail: { packetFilterJson: info, urlFilterString: '', networkId },
        isNewTab: true,
      },
      dispatch,
      state,
      tabType,
      info,
    );
  }
};
