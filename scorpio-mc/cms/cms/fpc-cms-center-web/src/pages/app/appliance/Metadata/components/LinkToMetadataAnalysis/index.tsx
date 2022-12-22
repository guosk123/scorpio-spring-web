import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { useContext, useEffect } from 'react';
import { history, useParams } from 'umi';
import { MetaDataContext } from '../..';
import { jumpToMetadataTab } from '../../constant';
import { EMetadataTabType } from '../../typings';

interface Props {
  onJumpDone: any;
}

export default function LinkToMetadataAnalysis(props: Props) {
  const { onJumpDone } = props;
  const [metadataState, metadataDispatch] = useContext(MetaDataContext);
  const { serviceId }: IUriParams = useParams();
  const [state] = useContext<any>(serviceId ? ServiceAnalysisContext : AnalysisContext);
  // useEffect(() => {
  //   clearShareInfo(dispatch);
  // }, [dispatch]);
  const jumpToNewTab = JSON.parse(history.location.query?.newTabsParams?.toString() || '[]').pop();
  useEffect(() => {
    // const tmpInfo = JSON.parse(state?.shareInfo);
    // console.log('jumpToNewTab', jumpToNewTab);
    if (state?.shareInfo?.jumptab) {
      jumpToMetadataTab(
        metadataState,
        metadataDispatch,
        EMetadataTabType[String(state?.shareInfo?.jumptab).toLocaleUpperCase()],
        state?.shareInfo?.filter,
      );
    }
    if (jumpToNewTab?.length) {
      jumpToMetadataTab(
        metadataState,
        metadataDispatch,
        EMetadataTabType[String(jumpToNewTab.toLocaleUpperCase())],
      );
    }
    if (
      history.location.query?.jumpTabs &&
      EMetadataTabType[String(history.location.query?.jumpTabs).toLocaleUpperCase()]
    ) {
      jumpToMetadataTab(
        metadataState,
        metadataDispatch,
        EMetadataTabType[String(history.location.query?.jumpTabs).toLocaleUpperCase()],
      );
    }
    onJumpDone();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
}
