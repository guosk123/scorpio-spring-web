import { EMetricApiType } from '@/common/api/analysis';
import type { IFilter } from '@/components/FieldFilter/typings';
import type {
  ICountryMap,
  IProvinceMap,
  ICityMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import React, { useMemo } from 'react';
import type { IFieldProperty } from '../../components/fieldsManager';
import {
  EFormatterType,
  EModelAlias,
  getEnumValueFromModelNext,
} from '../../components/fieldsManager';
import FlowAnalysis from '../../components/FlowAnalysis';
import type { IFlowAnalysisData } from '../../typings';

interface ILocationProps {
  networkId?: string;
  filterCondition?: IFilter[];
  needHeight?: number;
}

const Location: React.FC<ILocationProps> = (props: ILocationProps) => {
  const { networkId, filterCondition, needHeight } = props;
  const fieldsMapping: Record<string, IFieldProperty> = {
    downstreamBytes: { name: '下行字节数', formatterType: EFormatterType.BYTE },
    upstreamBytes: { name: '上行字节数', formatterType: EFormatterType.BYTE },
    downstreamPackets: { name: '下行数据包数', formatterType: EFormatterType.COUNT },
    upstreamPackets: { name: '上行数据包数', formatterType: EFormatterType.COUNT },
    downstreamPayloadBytes: { name: '下行负载字节数', formatterType: EFormatterType.BYTE },
    upstreamPayloadBytes: { name: '上行负载字节数', formatterType: EFormatterType.BYTE },
    downstreamPayloadPackets: { name: '下行负载数据包数', formatterType: EFormatterType.COUNT },
    upstreamPayloadPackets: { name: '上行负载数据包数', formatterType: EFormatterType.COUNT },
  };

  const countryMap = useMemo(() => {
    return getEnumValueFromModelNext(EModelAlias.country)?.map as ICountryMap;
  }, []);

  const provinceMap = useMemo(() => {
    return getEnumValueFromModelNext(EModelAlias.province)?.map as IProvinceMap;
  }, []);
  const cityMap = useMemo(() => {
    return getEnumValueFromModelNext(EModelAlias.city)?.map as ICityMap;
  }, []);

  const mapEnumFieldToName = (fieldId: string, name: string) => {
    switch (name) {
      case 'countryId': {
        return countryMap[fieldId]?.nameText || (fieldId as string);
      }
      case 'provinceId': {
        return provinceMap[fieldId]?.nameText || (fieldId as string);
      }
      case 'cityId': {
        return cityMap[fieldId]?.nameText || (fieldId as string);
      }
      default: {
        return fieldId as string;
      }
    }
  };

  const getSeriesName = (record: IFlowAnalysisData) => {
    let seriesName = '[--]';
    const { countryId, provinceId, cityId } = record;
    if (cityId) {
      seriesName = (cityMap[cityId]?.fullName || cityId) as string;
    } else if (provinceId) {
      seriesName = (provinceMap[provinceId]?.fullName || provinceId) as string;
    } else {
      seriesName = (countryMap[countryId]?.fullName || countryId) as string;
    }
    return seriesName;
  };

  return (
    <FlowAnalysis
      flowAnalysisType={EMetricApiType.location}
      mapEnumFieldToName={mapEnumFieldToName}
      getSeriesName={getSeriesName}
      excludeFields={['cityId']}
      overloadFieldsMapping={fieldsMapping}
      currentNetworkId={networkId}
      currentFilterCondition={filterCondition}
      needHeight={needHeight}
    />
  );
};

export default Location;
