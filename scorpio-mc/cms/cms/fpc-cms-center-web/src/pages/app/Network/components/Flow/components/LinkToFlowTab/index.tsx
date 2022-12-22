import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import React, { useContext, useEffect } from 'react';
import { history, useParams } from 'umi';
import { FlowContext } from '../..';
import { jumpToFlowTab } from '../../constant';
import { EFlowTabs } from '../../typing';

interface Props {
  onJumpDone: any;
}

export default function LinkToFlowTab(props: Props) {
  const { onJumpDone } = props;
  const [flowState, flowDispatch] = useContext(FlowContext);
  const { serviceId }: IUriParams = useParams();
  const [state, dispatch] = useContext<any>(serviceId ? ServiceAnalysisContext : AnalysisContext);
  // useEffect(() => {
  //   clearShareInfo(dispatch);
  // }, [dispatch]);
  const jumpToNewTab = JSON.parse(history.location.query?.newTabsParams?.toString() || '[]').pop();
  useEffect(() => {
    const tmpQuery = { ...history.location.query } || {};
    delete tmpQuery?.newTabsParams;
    history.replace({
      pathname: history.location.pathname,
      query: tmpQuery,
    });
    // const tmpInfo = JSON.parse(state?.shareInfo);
    console.log('jumpToNewTab', jumpToNewTab);
    if (state?.shareInfo?.jumptab) {
      jumpToFlowTab(
        flowState,
        flowDispatch,
        EFlowTabs[String(state?.shareInfo?.jumptab).toLocaleUpperCase()],
        state?.shareInfo?.filter,
      );
    }
    if (jumpToNewTab?.length) {
      jumpToFlowTab(flowState, flowDispatch, EFlowTabs[String(jumpToNewTab.toLocaleUpperCase())]);
    }
    if (
      history.location.query?.jumpTabs &&
      EFlowTabs[String(history.location.query?.jumpTabs).toLocaleUpperCase()]
    ) {
      jumpToFlowTab(
        flowState,
        flowDispatch,
        EFlowTabs[String(history.location.query?.jumpTabs).toLocaleUpperCase()],
      );
    }
    onJumpDone();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
}
