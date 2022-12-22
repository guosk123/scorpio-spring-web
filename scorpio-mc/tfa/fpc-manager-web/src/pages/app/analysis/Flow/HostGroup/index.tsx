import { EMetricApiType } from '@/common/api/analysis';
import React, { useMemo } from 'react';
import type { IIpAddressGroupModelState } from 'umi';
import type { IFieldProperty } from '../../components/fieldsManager';
import {
  EFormatterType,
  EModelAlias,
  getEnumValueFromModelNext,
} from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface IHostGroupProps {}

const HostGroup: React.FC<IHostGroupProps> = () => {
  const overloadFieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '接收字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '发送字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '接收数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '发送数据包数', formatterType: EFormatterType.COUNT },
  };

  const hostGroupMap = useMemo<IIpAddressGroupModelState['allIpAddressGroupMap']>(() => {
    return getEnumValueFromModelNext(EModelAlias.hostGroup)
      ?.map as IIpAddressGroupModelState['allIpAddressGroupMap'];
  }, []);

  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      case 'hostgroupId': {
        return hostGroupMap[fieldId]?.name || fieldId;
      }
      default: {
        return fieldId;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    const { hostgroupId } = record;
    return hostGroupMap[hostgroupId]?.name || (hostgroupId as string);
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.hostGroup}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={[]}
      overloadFieldsMapping={overloadFieldsMapping}
    />
  );
};

export default HostGroup;
