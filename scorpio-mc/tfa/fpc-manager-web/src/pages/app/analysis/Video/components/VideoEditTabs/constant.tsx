import Devices from '../../Devices';
import type { ITabsState } from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import { newPanesFn } from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import type { Dispatch } from 'react';
import { EVideoTabType } from '../../typings';
import RTP from '../../RTP';

import IpGraph from '../../IpGraph';
import Segment from '../../Segment';

export const VideoTabList = {
  [EVideoTabType.VIDEO_DEVICES_LIST]: {
    title: 'IP设备列表',
    defShow: true,
    content: <Devices />,
  },
  [EVideoTabType.RTP_FLOW_LIST]: {
    title: 'RTP流分析',
    defShow: false,
    content: <RTP />,
  },
  [EVideoTabType.IP_GRAPH]: {
    title: '访问关系',
    defShow: false,
    content: <IpGraph />,
  },
  [EVideoTabType.SEGMENT]: {
    title: '分段分析',
    defShow: false,
    content: <Segment />,
  },
};

export const openNewVideoTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EVideoTabType,
  info?: any,
  newTitle?: string,
) => {
  if (newTitle) {
    newPanesFn(
      {
        ...VideoTabList[tabType],
        isNewTab: true,
        detail: info,
        title: newTitle,
      },
      dispatch,
      state,
      tabType,
      info,
    );
  } else {
    newPanesFn({ ...VideoTabList[tabType], isNewTab: true }, dispatch, state, tabType, info);
  }
};
