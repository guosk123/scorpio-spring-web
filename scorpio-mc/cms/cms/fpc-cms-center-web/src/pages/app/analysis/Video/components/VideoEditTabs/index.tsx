import EditTabs from '@/pages/app/Network/components/EditTabs';
import LinkToAnalysis from '../LinkToAnalysis';
import { createContext, useCallback, useContext, useEffect, useRef } from 'react';
import { VideoTabList } from './constant';
import { VideoContext } from '../NetworkTimeLayout';

export const VideoTabsContext = createContext([]);
export const EditVideoTabsContext = createContext<any>([]);

export default function VideoEditTabs() {
  /** 网络时间选择器上下文 */
  const { setNetworkSelect } = useContext(VideoContext)!;
  const editTabRef = useRef({});
  const refreshNetworkSelect = useCallback(
    (key?: string) => {
      if (setNetworkSelect && (editTabRef?.current || key)) {
        const {
          state: { activeKey },
        } = editTabRef?.current as any;
        if ((key || (activeKey as string))?.match(/^segment/)) {
          setNetworkSelect(false);
        } else {
          setNetworkSelect(true);
        }
      }
    },
    [setNetworkSelect],
  );

  useEffect(() => {
    refreshNetworkSelect();
  }, [refreshNetworkSelect]);

  return (
    <EditVideoTabsContext.Provider value={[editTabRef]}>
      <EditTabs
        tabs={VideoTabList}
        consumerContext={VideoTabsContext}
        linkToTab={<LinkToAnalysis />}
        // dirTabName={paneTitle}
        // showTabSettingTool={true}
        editTab={editTabRef}
        tabsKey="offline-analysis"
        destroyInactiveTabPane={false}
        onChangeTab={(key) => {
          refreshNetworkSelect(key);
        }}
      />
    </EditVideoTabsContext.Provider>
  );
}
