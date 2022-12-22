import { EMetricApiType } from '@/common/api/analysis';
import type { ConnectState } from '@/models/connect';
import type { IL7ProtocolMap } from '@/pages/app/appliance/Metadata/typings';
import React from 'react';
import { connect } from 'dva';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EFormatterType } from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IProtocolProps {
  allL7ProtocolMap: IL7ProtocolMap;
}

const Protocol: React.FC<IProtocolProps> = (props) => {
  const { allL7ProtocolMap } = props;

  const overloadFieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '下行字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '上行字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '下行数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '上行数据包数', formatterType: EFormatterType.COUNT },
    downstreamPayloadBytes: { name: '下行负载字节数', formatterType: EFormatterType.BYTE },
    upstreamPayloadBytes: { name: '上行负载字节数', formatterType: EFormatterType.BYTE },
    downstreamPayloadPackets: { name: '下行负载数据包数', formatterType: EFormatterType.COUNT },
    upstreamPayloadPackets: { name: '上行负载数据包数', formatterType: EFormatterType.COUNT },
  };

  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      case 'l7ProtocolId': {
        return allL7ProtocolMap[fieldId]?.nameText || fieldId;
      }
      default: {
        return fieldId;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    let seriesName = '[--]';
    const { l7ProtocolId } = record;
    seriesName = allL7ProtocolMap[l7ProtocolId]?.nameText || (l7ProtocolId as string);
    return seriesName;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.protocol}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={overloadFieldsMapping}
    />
  );
};

const mapStateToProps = ({ metadataModel: { allL7ProtocolMap } }: ConnectState) => ({
  allL7ProtocolMap,
});

export default connect(mapStateToProps)(Protocol);
