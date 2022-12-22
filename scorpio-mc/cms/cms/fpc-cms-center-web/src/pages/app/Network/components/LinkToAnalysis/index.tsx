import { ServiceAnalysisContext, ServiceContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { useContext, useEffect } from 'react';
import { history, useParams } from 'umi';
import { AnalysisContext } from '../../Analysis';
import { jumpToAnalysisTabNew } from '../../Analysis/constant';
import { LayoutContext } from '../../NetworkLayout';
import type { ENetworkTabs } from '../../typing';

export const JUMP_TO_NEW_TAB = true;

export default function LinkToAnalysis() {
  const { serviceId }: IUriParams = useParams();
  const [state, dispatch] = useContext<any>(serviceId ? ServiceAnalysisContext : AnalysisContext);
  const [, , setShowTimeSelect] = useContext(serviceId ? ServiceContext : LayoutContext);
  // 在线分析隐藏时间选择
  useEffect(() => {
    if (state?.activeKey.includes('packetAnalysis')) {
      setShowTimeSelect(false);
    } else {
      setShowTimeSelect(true);
    }
  }, [setShowTimeSelect, state]);

  useEffect(() => {
    if (history.location?.query?.jumpTabs) {
      const tmpInfo = JSON.parse((history.location.query?.shareInfo as string) || '[]');
      const networkId = tmpInfo?.networkId;
      const jumpTabs = history.location?.query?.jumpTabs as ENetworkTabs;
      const newTabsParams = history.location.query.newTabsParams as any;
      if (networkId) {
        delete tmpInfo.networkId;
      }
      jumpToAnalysisTabNew(state, dispatch, jumpTabs, {
        autoJump: true,
      });
      const tmpQuery = history.location?.query || {};

      history.replace({
        pathname: history.location.pathname,
        query: tmpQuery?.timeType
          ? { from: tmpQuery?.from, to: tmpQuery?.to, timeType: tmpQuery?.timeType, newTabsParams }
          : { newTabsParams },
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
}
