import type { IPktAnalysisSharedProps } from '@/pages/app/PktAnalysis';
import PktAnalysis from '@/pages/app/PktAnalysis';
import { EPktAnalysisDataSource } from '@/pages/app/PktAnalysis/typings';
import React from 'react';
import type { Dispatch } from 'umi';

interface IPacketAnalysisProps {
  dispatch: Dispatch;
}

const PacketAnalysis: React.FC<IPacketAnalysisProps> = () => {

  // URI中会携带一些参数，直接从 PktAnalysis 组件中 service.ts 拼接 ur 的时候取吧

  const pktAnalysisProps: IPktAnalysisSharedProps = {
    sourceType: EPktAnalysisDataSource.packet,
  };

  return <PktAnalysis {...pktAnalysisProps} />;
};

export default PacketAnalysis;
