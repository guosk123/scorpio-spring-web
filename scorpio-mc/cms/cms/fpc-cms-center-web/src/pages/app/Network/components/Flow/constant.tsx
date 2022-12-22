import Application from '@/pages/app/analysis/Flow/Application';
import HostGroup from '@/pages/app/analysis/Flow/HostGroup';
import Ip from '@/pages/app/analysis/Flow/Ip';
import IpConversation from '@/pages/app/analysis/Flow/IpConversation';
import Location from '@/pages/app/analysis/Flow/Location';
import Mac from '@/pages/app/analysis/Flow/Mac';
import Port from '@/pages/app/analysis/Flow/Port';
import Protocol from '@/pages/app/analysis/Flow/Protocol';
import type { Dispatch } from 'react';
import type { ITabsState} from '../EditTabs';
import { newPanesFn } from '../EditTabs';
import { EFlowTabs } from './typing';

export const flowTabs = {
  [EFlowTabs.LOCATION]: {
    title: '地区',
    defShow: true,
    content: <Location />,
  },
  [EFlowTabs.APPLICATION]: {
    title: '应用',
    defShow: true,
    content: <Application />,
  },
  [EFlowTabs.PROTOCOL]: {
    title: '应用层协议',
    defShow: true,
    content: <Protocol />,
  },
  [EFlowTabs.PORT]: {
    title: '端口',
    defShow: true,
    content: <Port />,
  },
  [EFlowTabs.HOSTGROUP]: {
    title: 'IP地址组',
    defShow: true,
    content: <HostGroup />,
  },
  [EFlowTabs.MAC]: {
    title: 'MAC地址',
    defShow: true,
    content: <Mac />,
  },
  [EFlowTabs.IP]: {
    title: 'IP地址',
    defShow: true,
    content: <Ip />,
  },
  [EFlowTabs.IPCONVERSATION]: {
    title: 'IP会话',
    defShow: true,
    content: <IpConversation />,
  },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
 export const jumpToFlowTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EFlowTabs,
  info?: any,
) => {
  newPanesFn({ ...flowTabs[tabType], isNewTab: true }, dispatch, state, tabType, info);
};
