import { EMetricApiType } from '@/common/api/analysis';
import { ETHERNET_TYPE_LIST } from '@/common/app';
import React from 'react';
import { connect } from 'dva';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EFormatterType } from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IMacProps {}

const Mac: React.FC<IMacProps> = () => {
  const overloadFieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '接收字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '发送字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '接收数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '发送数据包数', formatterType: EFormatterType.COUNT },
  };
  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      case 'ethernetType': {
        const find = ETHERNET_TYPE_LIST.find((item) => item.value === fieldId);
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
    const { macAddress } = record;
    return macAddress as string;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.macAddress}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={overloadFieldsMapping}
    />
  );
};

export default connect()(Mac);
