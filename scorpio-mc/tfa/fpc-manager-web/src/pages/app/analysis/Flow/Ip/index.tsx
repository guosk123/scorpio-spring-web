import { EMetricApiType } from '@/common/api/analysis';
import type { IEnumValue } from '@/components/FieldFilter/typings';
import React, { useMemo } from 'react';
import { connect } from 'dva';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EFormatterType, fieldsMapping } from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IIpProps {}

const Ip: React.FC<IIpProps> = () => {
  const overloadFieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '接收字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '发送字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '接收数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '发送数据包数', formatterType: EFormatterType.COUNT },
  };

  const ipLocalityMap = useMemo(() => {
    return (fieldsMapping.ipLocality?.enumValue as IEnumValue[]).reduce((prev, cur) => {
      return {
        ...prev,
        [cur.value]: cur.text,
      };
    }, {});
  }, []);

  const mapEnumFieldToName = (value: string, field: string) => {
    switch (field) {
      case 'ethernetType': {
        return value.toUpperCase();
      }
      case 'ipLocality': {
        if (value === 'ALL') {
          return value;
        }
        return ipLocalityMap[value];
      }
      default: {
        return value;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    const { ipAddress, ipLocality } = record;
    return `${ipAddress}-${ipLocalityMap[ipLocality]}`;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.ipAddress}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={overloadFieldsMapping}
    />
  );
};

const mapStateToProps = () => ({});

export default connect(mapStateToProps)(Ip);
