import List from '../List';
import { EOfflineTabType } from '../typing';
import CreateForm from '../Task/Create';
import type { ITabsState } from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import { newPanesFn } from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import type { Dispatch } from 'react';
import TaskList from '../Task/List';
import TaskLogList from '../Task/TaskLogList';

export const OfflineAnalysisTabs = {
  [EOfflineTabType.OFFLINE_PCAP_FILE_LIST]: {
    title: '离线文件列表',
    defShow: true,
    content: <List />,
  },
  [EOfflineTabType.OFFLINE_TASK_LIST]: {
    title: '离线任务列表',
    defShow: true,
    content: <TaskList />,
  },
  [EOfflineTabType.OFFLINE_TASK_DETAIL]: {
    title: '子任务',
    defShow: false,
    content: <List showUpLoadBtn={false} />,
  },
  [EOfflineTabType.OFFLINE_TASK_CREATE]: {
    title: '新建离线任务',
    defShow: false,
    content: <CreateForm />,
  },
  [EOfflineTabType.OFFLINE_TASK_LOG]: {
    title: '任务日志',
    defShow: false,
    content: <TaskLogList />,
  },
};

export const jumpToOfflineTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EOfflineTabType,
  info?: any,
  newTitle?: string,
  newTabType?: string,
) => {
  if (newTitle) {
    const tmpTitle = newTitle + (newTabType === 'log' ? '(日志)' : '(子任务)');
    newPanesFn(
      {
        ...OfflineAnalysisTabs[tabType],
        // title: `${newTitle}(子任务)`,
        title: tmpTitle,
        isNewTab: true,
        detail: info,
      },
      dispatch,
      state,
      tabType,
      info,
    );
  } else {
    newPanesFn({ ...OfflineAnalysisTabs[tabType], isNewTab: true }, dispatch, state, tabType, info);
  }
};
