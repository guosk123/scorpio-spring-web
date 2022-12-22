import EditTabs from '@/pages/app/appliance/Metadata/Analysis/components/EditTabs';
import { createContext, useRef } from 'react';
import { OfflineAnalysisTabs } from './constant';

export const OfflineTabsContext = createContext([]);
export const EditOfflineTabsContext = createContext<any>([]);
export default function Analysis() {
  const editTabRef = useRef({});
  return (
    <EditOfflineTabsContext.Provider value={[editTabRef]}>
      <EditTabs
        tabs={OfflineAnalysisTabs}
        consumerContext={OfflineTabsContext}
        // linkToTab={<LinkToAnalysis />}
        destroyInactiveTabPane={true}
        // dirTabName={paneTitle}
        // showTabSettingTool={true}
        editTab={editTabRef}
        tabsKey="offline-analysis"
      />
    </EditOfflineTabsContext.Provider>
  );
}
