import Detail from '@/pages/app/Configuration/Alerts/Message/Detail';
import AlertMessage from '@/pages/app/Configuration/Alerts/Message/List';
import type { Dispatch } from 'react';
import type { ITabsState } from '../EditTabs';
import { newPanesFn } from '../EditTabs';
import { EAlertTabs } from './typing';

export const alertMessageTabs = {
  [EAlertTabs.ALERTMESSAGELIST]: {
    title: '消息列表',
    defShow: true,
    content: <AlertMessage />,
  },
  [EAlertTabs.ALERTMESSAGEDETAIL]: {
    title: '告警详情',
    defShow: false,
    content: <Detail />,
  },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export const jumpToAlertTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EAlertTabs,
  info?: any,
) => {
  newPanesFn(alertMessageTabs[tabType], dispatch, state, tabType, info);
};
