import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { clearShareInfo } from '@/pages/app/Network/components/EditTabs';
import React, { useContext, useEffect, useMemo } from 'react';
import { history, useParams } from 'umi';
import type { IPktAnalysisSharedProps } from '../components/PktAnalysis';
import PktAnalysis from '../components/PktAnalysis';
import { EPktAnalysisDataSource } from '../components/PktAnalysis/typings';
import SensorNetworkSelectBox from '../components/SensorNetworkSelectBox';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import moment from 'moment';

const PacketAnalysis: React.FC = () => {
  const { serviceId }: IUriParams = useParams();
  const [state, analysisDispatch] = useContext<any>(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );
  useEffect(() => {
    clearShareInfo(analysisDispatch);
  }, [analysisDispatch]);

  const tabDetail = useMemo(() => {
    const tmpDetail = analysisDispatch
      ? state.panes.find((item: any) => state.activeKey === item.key)
      : {
          paneDetail: {
            packetFilterJson: String(history.location.query?.shareInfo) || '{}',
          },
        };
    const urlPktFilterJson = decodeURIComponent(history.location.query?.packetFilterJson?.toString() || '');
    const tmpPktJson = urlPktFilterJson.length ? urlPktFilterJson : '{}';
    const tmpjson = JSON.parse(tmpPktJson || tmpDetail?.paneDetail?.packetFilterJson);
    return (
      {
        ...tmpDetail?.paneDetail,
        packetFilterJson: JSON.stringify({
          ...tmpjson,
          startTime: moment(tmpjson?.startTime).format(),
          endTime: moment(tmpjson?.endTime).format(),
        }),
      } || {}
    );
    // return tmpDetail?.paneDetail || {};
  }, [state]);

  // URI中会携带一些参数，直接从 PktAnalysis 组件中 service.ts 拼接 ur 的时候取吧
  const pktAnalysisProps: IPktAnalysisSharedProps = {
    sourceType: EPktAnalysisDataSource.packet,
    packetFilterJson: tabDetail.packetFilterJson,
  };
  const filterObj = JSON.parse(tabDetail.packetFilterJson || '{}');
  console.log('pktAnalysisProps', pktAnalysisProps);
  console.log('filterObj', filterObj);
  return (
    <SensorNetworkSelectBox
      initSelect={{ fpcSerialNumber: filterObj?.fpcSerialNumber, networkId: filterObj?.networkId }}
      disabled={true}
    >
      <PktAnalysis {...pktAnalysisProps} showBackBtn={false} />
    </SensorNetworkSelectBox>
  );
};

export default PacketAnalysis;
