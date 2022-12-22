import TcpConnectError from '@/pages/app/analysis/TCP_Connection/Error';
import LongConnection from '@/pages/app/analysis/TCP_Connection/LongConnection';
import LongConnectionSetting from '@/pages/app/analysis/TCP_Connection/LongConnectionSetting';
import TCPErrorAnalysis from '@/pages/app/analysis/TCP_Connection/TCPErrorAnalysis';
import type { Dispatch } from 'react';
import type { ENetowrkType } from '../../typing';
import type { ITabsState } from '../EditTabs';
import { newPanesFn } from '../EditTabs';
import { EConnectTabs } from './typing';

export const connectTabs = {
  [EConnectTabs.CONNECTION_ANALYSIS]: {
    title: '建连失败分析',
    defShow: true,
    content: <TCPErrorAnalysis />,
  },
  [EConnectTabs.CONNECTION_ERROR]: {
    title: '建连失败详单',
    defShow: true,
    content: <TcpConnectError />,
  },
  [EConnectTabs.LONG_CONNECTION]: {
    title: '长连接分析',
    defShow: true,
    content: <LongConnection />,
  },
  [EConnectTabs.LONG_CONNECTION_SETTING]: {
    title: '长连接配置',
    defShow: false,
    content: <LongConnectionSetting />,
  },
};

export const connectSettings = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EConnectTabs,
  networkType?: ENetowrkType,
) => {
  if (networkType && tabType === EConnectTabs.LONG_CONNECTION_SETTING) {
    newPanesFn(
      { ...connectTabs[tabType], content: <LongConnectionSetting networkType={networkType} /> },
      dispatch,
      state,
      tabType,
    );
    return;
  }
  newPanesFn(connectTabs[tabType], dispatch, state, tabType);
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export const jumpToConnectTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EConnectTabs,
  info?: any,
) => {
  newPanesFn(
    {
      ...connectTabs[tabType],
      detail: { packetFilterJson: info, urlFilterString: '' },
      isNewTab: true,
    },
    dispatch,
    state,
    tabType,
    info,
  );
};
