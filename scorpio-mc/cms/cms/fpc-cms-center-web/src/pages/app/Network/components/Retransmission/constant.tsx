import TCPRetransmissionAnalysis from '@/pages/app/analysis/TCP_Retransmission/TCPRetransmissionAnalysis';
import TCPRetransmissionDetail from '@/pages/app/analysis/TCP_Retransmission/TCPRetransmissionDetail';
import type { Dispatch } from 'react';
import type { ITabsState} from '../EditTabs';
import { newPanesFn } from '../EditTabs';

export enum ERetransmissionTabs {
  RETRANSMISSION_ANALYSIS = 'retransmission_analysis',
  RETRANSMISSION_DETAIL = 'retransmission_detail',
}

export const retransmissionTabs = {
  [ERetransmissionTabs.RETRANSMISSION_ANALYSIS]: {
    title: '重传分析',
    defShow: true,
    content: <TCPRetransmissionAnalysis />,
  },
  [ERetransmissionTabs.RETRANSMISSION_DETAIL]: {
    title: '重传分析详单',
    defShow: true,
    content: <TCPRetransmissionDetail />,
  },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
 export const jumpToRetransmissionTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: ERetransmissionTabs,
  info?: any,
) => {
  newPanesFn(
    {
      ...retransmissionTabs[tabType],
      detail: { packetFilterJson: info, urlFilterString: '' },
      isNewTab: true,
    },
    dispatch,
    state,
    tabType,
    info,
  );
};
