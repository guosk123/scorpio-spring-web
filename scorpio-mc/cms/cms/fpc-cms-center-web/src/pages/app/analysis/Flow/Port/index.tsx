import { EMetricApiType } from '@/common/api/analysis';
import { IP_PROTOCOL_ENUM_LIST } from '@/common/app';
import React from 'react';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EFormatterType } from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IPortProps {}

const Port: React.FC<IPortProps> = () => {
  const overloadFieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '接收字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '发送字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '接收数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '发送数据包数', formatterType: EFormatterType.COUNT },
    downstreamPayloadBytes: { name: '接收负载字节数', formatterType: EFormatterType.BYTE },
    upstreamPayloadBytes: { name: '发送负载字节数', formatterType: EFormatterType.BYTE },
    downstreamPayloadPackets: { name: '接收负载数据包数', formatterType: EFormatterType.COUNT },
    upstreamPayloadPackets: { name: '发送负载数据包数', formatterType: EFormatterType.COUNT },
  };
  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      case 'ipProtocol': {
        const find = IP_PROTOCOL_ENUM_LIST.find((item) => item.value === fieldId);
        if (find) {
          return find.text;
        }
        return fieldId;
      }
      default: {
        return fieldId;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    const { port } = record;
    return `${port}`;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.port}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={overloadFieldsMapping}
    />
  );
};

export default Port;
