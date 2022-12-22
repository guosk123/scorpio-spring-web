import type { Dispatch} from 'react';
import { useState } from 'react';
import { useMemo } from 'react';
import { createContext } from 'react';
import { history } from 'umi';
import { MetadataTabs, MetadataTabsAnalysis, MetadataTabsDetail } from './constant';
import type { ITabsState } from '../../Network/components/EditTabs';
import EditTabs, { handleActiveKey } from '../../Network/components/EditTabs';
import LinkToMetadataAnalysis from './components/LinkToMetadataAnalysis';
import { jumpNewPage } from '@/utils/utils';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { URLFilter } from '../../Network/Analysis/constant';

export const jumpToMetadataTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  activeKey: string,
  shareInfo?: any,
) => {
  // jumpNewPage(
  //   `${history.location.pathname}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${
  //     ETimeType.CUSTOM
  //   }&jumpTabs=${tabType}&shareInfo=${JSON.stringify(tmpJumpInfo || [])}&args=${JSON.stringify(
  //     args || {},
  //   )}`,
  // );
  handleActiveKey(dispatch, state, activeKey, shareInfo);
};

export const jumpToMetadataDetailTab = (
  timeInfo: { startTime: number; endTime: number },
  activeKey: string,
  filter?: URLFilter[],
) => {
  jumpNewPage(
    `/flow-trace/mata-data-detail?from=${timeInfo.startTime}&to=${timeInfo.endTime}&timeType=${
      ETimeType.CUSTOM
    }&jumpTabs=${activeKey}&filter=${encodeURIComponent(JSON.stringify(filter))}`,
  );
};

export const MetaDataContext = createContext([]);
export default function Metadata(props: any) {
  const { paneTitle } = props;
  const [loading, setLoading] = useState(true);
  const metaDataTab = useMemo(() => {
    if (history.location.pathname.includes('mata-data-analysis')) {
      return MetadataTabsAnalysis;
    } else if (history.location.pathname.includes('mata-data-detail')) {
      return MetadataTabsDetail;
    }
    return MetadataTabs;
  }, []);

  return (
    <EditTabs
      tabs={metaDataTab}
      loading={loading}
      consumerContext={MetaDataContext}
      linkToTab={
        <LinkToMetadataAnalysis
          onJumpDone={() => {
            setLoading(false);
          }}
        />
      }
      // destroyInactiveTabPane={history.location.query?.relative === 'true'}
      destroyInactiveTabPane={true}
      dirTabName={paneTitle}
      fixHeight={2}
    />
  );
}
