import { EMetricApiType } from '@/common/api/analysis';
import React from 'react';
import { connect } from 'dva';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EFormatterType } from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IIpSessionProps {}

const IpSession: React.FC<IIpSessionProps> = () => {
  const fieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '字节A -> B', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '字节B -> A', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '数据包A -> B', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '数据包B -> A', formatterType: EFormatterType.COUNT },
  };

  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      default: {
        return fieldId as string;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    const { ipAAddress, ipBAddress } = record;
    return `${ipAAddress}⇋${ipBAddress}`;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.ipConversation}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={fieldsMapping}
    />
  );
};

export default connect()(IpSession);
