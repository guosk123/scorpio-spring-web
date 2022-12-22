import { Fragment, useContext, useMemo, useState } from 'react';
import { history, useParams } from 'umi';
import { ServiceAnalysisContext } from '../../analysis/Service/index';
import type { IUriParams } from '../../analysis/typings';
import { AnalysisContext } from '../../Network/Analysis';
import { clearShareInfo } from '../../Network/components/EditTabs';
import { PacketContext } from '../../Network/components/Packet';
import SensorNetworkSelectBox from './components/SensorNetworkSelectBox';
import PacketAnalysis from './PacketAnalysis';
import PacketPage from './PacketPage';

const getPaneDetail = (activeKey: string, panes: any[]) => {
  return panes.find((item) => item.key === activeKey)?.paneDetail;
};

export default function Packet() {
  const { serviceId }: IUriParams = useParams();
  const [state, analysisDispatch] = useContext<any>(
    (serviceId ? ServiceAnalysisContext : AnalysisContext) || PacketContext,
  );

  const { networkId } = JSON.parse((history.location.query?.shareInfo as string) || '{}') || {};

  const { activeKey, panes } = state || {};

  return (
    <Fragment>
      {history.location.pathname.includes('packet/analysis') ? (
        <PacketAnalysis />
      ) : (
        <SensorNetworkSelectBox
          drilldownNetworkId={
            networkId || (activeKey && getPaneDetail(activeKey, panes)?.networkId)
          }
          onNoFpcSensor={() => {
            if (analysisDispatch) {
              clearShareInfo(analysisDispatch);
            }
          }}
        >
          <PacketPage />
        </SensorNetworkSelectBox>
      )}
    </Fragment>
  );
}
