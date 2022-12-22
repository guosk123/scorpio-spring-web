import type { RadioChangeEvent } from 'antd';
import { Card, Select, Radio } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import Map from '../Map';
import { MapType, MapTypeOptions, MapDataType, MapDataTypeOptions } from '../../../typings';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import type { IProvince, ICountry } from '@/pages/app/configuration/Geolocation/typings';
// import worldMapJson from '@/components/ReactECharts/assets/world.json';
import newWorldMapJson from '@/components/ReactECharts/assets/newWorldCountries.json';
import CountryCodeToName from '@/components/ReactECharts/assets/CodeToCountry.json';
import chinaMapJson from '@/components/ReactECharts/assets/china.json';
import { v1 as uuidv1 } from 'uuid';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { BOOL_NO } from '@/common/dict';
import { queryHistogramData } from '../../../service';
import { SearchIpImageContext } from '../../..';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/analysis/Network/IPImage/IpImageContent/components/ShowFullCard';

interface IConnectSourceProps {
  title: string;
  height?: number;
  // IpAddress: string;
  // globalSelectTime: IGlobalTime;
  // networkId: string | null;
  IpCategory: string;
}

export interface IconnectIpMapDataDel {
  country_id_responder?: string;
  country_id_initiator?: string;
  province_id_responder?: string;
  province_id_initiator?: string;
  establishedSessions: number;
  totalBytes: number;
}

export interface showedBarProps {
  maxValue: number;
  unit: string;
}

const ConnectSource: React.FC<IConnectSourceProps> = ({
  title,
  height = 482,
  // IpAddress,
  // globalSelectTime,
  // networkId,
  IpCategory,
}) => {
  //利用context拿到搜索关键字，比如说IP地址和网络名称
  const [searchInfo] = useContext(SearchIpImageContext);
  const { IpAddress, networkIds } = searchInfo;
  //时间应该直接获取
  const globalSelectTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );

  const chinaProvinceList = useSelector<ConnectState, IProvince[]>(
    (state) => state.geolocationModel.allProvinceList,
  );
  // console.log(chinaProvinceList, 'chinaProvinceList');
  const worldCountriesList = useSelector<ConnectState, ICountry[]>(
    (state) => state.geolocationModel.allCountryList,
  );
  // console.log(worldCountriesList, 'worldCountriesList');

  const [isLoading, setIsLoading] = useState(true);
  const [mapType, setMapType] = useState<string>(MapType.CHINA);
  const [mapDataType, setMapDataType] = useState<string>(MapDataType.FLOWCOUNT);
  const [mapInitData, setMapInitData] = useState([]);

  const showedWorldDataIndex = useMemo(() => {
    return newWorldMapJson.features.map((i) => i.properties).map((i) => i.countryCode);
  }, []);

  const showedChinaDataIndex = useMemo(() => {
    return chinaMapJson.features.map((i) => i.properties.name);
  }, []);

  const networkId = useMemo(() => {
    if (networkIds === 'ALL') {
      return undefined;
    }
    return networkIds;
  }, [networkIds]);

  const queryParams = useMemo(() => {
    let param = {};
    const queryType = IpCategory.split('_')[1];
    if (queryType === 'responder') {
      param = { ipInitiator: IpAddress };
    }
    if (queryType === 'initiator') {
      param = { ipResponder: IpAddress };
    }

    let queryDsl = `| gentimes timestamp start="${globalSelectTime.startTime}" end="${globalSelectTime.endTime}"`;
    if (networkId) {
      queryDsl = `(network_id<Array>=${networkId}) ` + queryDsl;
    }
    return {
      sourceType: 'network',
      networkId: networkId,
      startTime: globalSelectTime.startTime,
      endTime: globalSelectTime.endTime,
      ...param,
      sortProperty: 'tcp_established_counts',
      queryProperty: IpCategory,
      sortDirection: ESortDirection.DESC,
      dsl: queryDsl,
      drilldown: BOOL_NO,
      queryId: uuidv1(),
    };
  }, [IpAddress, globalSelectTime, networkId, IpCategory]);

  const recallNewMapInitData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryHistogramData(queryParams);
    if (success) {
      console.log(result, 'result');
      setMapInitData(result.content || []);
      setIsLoading(false);
    }
  }, [queryParams]);

  useEffect(() => {
    recallNewMapInitData();
  }, [recallNewMapInitData]);

  const manageMapInitData = useMemo(() => {
    const queryType = IpCategory.split('_')[1];
    let worldData: any[] = [],
      chinaData: any[] = [],
      chinaMapData: any[] = [],
      worldMapData: any[] = [];
    let worldOfChinaEstablishedSessions = 0,
      worldOfChinaTotalBytes = 0;
    let worldOfChina = {};
    if (queryType === 'initiator') {
      worldData = mapInitData.filter((item: any) => item.country_id_initiator != 1);
      console.log(worldData, 'worldDataInit', queryType);
      chinaData = mapInitData.filter((item: any) => item.country_id_initiator == 1);
      chinaData.forEach((item) => {
        worldOfChinaEstablishedSessions += item.establishedSessions;
        worldOfChinaTotalBytes += item.totalBytes;
      });
      console.log(
        'worldOfChinaEstablishedSessions: ',
        worldOfChinaEstablishedSessions,
        'worldOfChinaTotalBytes : ',
        worldOfChinaTotalBytes,
      );
      worldOfChina = {
        country_id_initiator: 1,
        province_id_initiator: null,
        establishedSessions: worldOfChinaEstablishedSessions,
        totalBytes: worldOfChinaTotalBytes,
      };
      console.log(worldOfChina, 'worldOfChina', queryType);
      worldData.push(worldOfChina);
      console.log(worldData, 'worldData', queryType);
      if (worldData.length > 0) {
        worldMapData = worldData.map((item: any) => {
          const neededCountry = worldCountriesList.find(
            (i) => i.countryId == item.country_id_initiator,
          );
          return {
            countryCode: neededCountry?.countryCode,
            establishedSessions: item.establishedSessions,
            totalBytes: item.totalBytes,
          };
        });
      }
      if (chinaData.length > 0) {
        chinaMapData = chinaData.map((item: any) => {
          const neededProvince = chinaProvinceList.find(
            (i) => i.provinceId == item.province_id_initiator,
          );
          return {
            name: neededProvince?.name,
            establishedSessions: item.establishedSessions,
            totalBytes: item.totalBytes,
          };
        });
      }
    }
    if (queryType === 'responder') {
      worldData = mapInitData.filter((item: any) => item.country_id_responder != 1);
      chinaData = mapInitData.filter((item: any) => item.country_id_responder == 1);
      chinaData.forEach((item) => {
        worldOfChinaEstablishedSessions += item.establishedSessions;
        worldOfChinaTotalBytes += item.totalBytes;
      });
      worldOfChina = {
        country_id_responder: 1,
        province_id_responder: null,
        establishedSessions: worldOfChinaEstablishedSessions,
        totalBytes: worldOfChinaTotalBytes,
      };
      worldData.push(worldOfChina);
      if (worldData.length > 0) {
        worldMapData = worldData.map((item: any) => {
          const neededCountry = worldCountriesList.find(
            (i) => i.countryId == item.country_id_responder,
          );
          return {
            countryCode: neededCountry?.countryCode,
            establishedSessions: item.establishedSessions,
            totalBytes: item.totalBytes,
          };
        });
      }
      if (chinaData.length > 0) {
        chinaMapData = chinaData.map((item: any) => {
          const neededProvince = chinaProvinceList.find(
            (i) => i.provinceId == item.province_id_responder,
          );
          return {
            name: neededProvince?.name,
            establishedSessions: item.establishedSessions,
            totalBytes: item.totalBytes,
          };
        });
        console.log(chinaMapData, 'responderChinaMapData');
      }
    }
    return {
      chinaMapData: chinaMapData,
      worldMapData: worldMapData,
    };
  }, [IpCategory, chinaProvinceList, mapInitData, worldCountriesList]);

  const currentRegionData = useMemo(() => {
    let selectedReion: any = [];
    if (mapType === MapType.CHINA) {
      selectedReion = showedChinaDataIndex.map((item) => {
        const foundProvince = manageMapInitData.chinaMapData.find((i: any) => i.name == item);
        if (foundProvince) {
          return foundProvince;
        }
        return { name: item, establishedSessions: 0, totalBytes: 0 };
      });
    }
    if (mapType === MapType.WORLD) {
      selectedReion = showedWorldDataIndex.map((item) => {
        const foundData = manageMapInitData.worldMapData.find((i: any) => item == i.countryCode);
        if (foundData) {
          return { name: CountryCodeToName[item], ...foundData };
        }
        return { name: CountryCodeToName[item], establishedSessions: 0, totalBytes: 0 };
      });
    }
    return selectedReion;
  }, [
    manageMapInitData.chinaMapData,
    manageMapInitData.worldMapData,
    mapType,
    showedChinaDataIndex,
    showedWorldDataIndex,
  ]);

  const trafficAndFlowData = useMemo(() => {
    let selectedDataTypeList: any = [];
    let maxValue: number = 0;
    if (mapDataType === MapDataType.TRFFICCOUNT) {
      selectedDataTypeList = currentRegionData.map((item: any) => {
        if (maxValue < item.establishedSessions) {
          maxValue = item.establishedSessions;
        }
        return {
          name: item.name,
          value: item.establishedSessions,
        };
      });
      maxValue = maxValue > 10 ? maxValue : 10;
    }
    if (mapDataType === MapDataType.FLOWCOUNT) {
      selectedDataTypeList = currentRegionData.map((item: any) => {
        if (maxValue < item.totalBytes) {
          maxValue = item.totalBytes;
        }
        return {
          name: item.name,
          value: item.totalBytes,
        };
      });
      maxValue = maxValue > 1000 ? maxValue : 1000;
    }
    // console.log(maxValue, 'maxValue');
    return {
      selectedDataTypeList: selectedDataTypeList,
      maxValue: maxValue,
    };
  }, [currentRegionData, mapDataType]);

  const changeMapType = (value: string) => {
    setMapType(value);
  };
  const handleSessionsAndTrafficCount = ({ target: { value } }: RadioChangeEvent) => {
    setMapDataType(value);
  };

  const MapSelectionBar = (
    <>
      <Radio.Group
        onChange={handleSessionsAndTrafficCount}
        value={mapDataType}
        options={MapDataTypeOptions}
      />

      <Select size="small" style={{ width: 100 }} onChange={changeMapType} defaultValue={mapType}>
        {MapTypeOptions.map((item) => (
          <Select.Option key={item.value} value={item.value}>
            {item.label}
          </Select.Option>
        ))}
      </Select>
    </>
  );

  return (
    <>
      <FullScreenCard title={title} extra={MapSelectionBar} loading={isLoading}>
        <HightContext.Consumer>
          {(isFullscreen) => {
            return (
              // <div
              //   style={{
              //     height: isFullscreen ? 'calc(100vh - 80px)' : '430px',
              //     overflow: 'hidden',
              //   }}
              // >
              <Map
                MapType={mapType}
                ShowedDataType={mapDataType}
                MapData={trafficAndFlowData.selectedDataTypeList}
                MapMaxValue={trafficAndFlowData.maxValue}
                isFull={isFullscreen}
              />
              // </div>
            );
          }}
        </HightContext.Consumer>
      </FullScreenCard>

      {/* <Card
        size="small"
        extra={MapSelectionBar}
        title={title}
        style={{ height: height }}
        bodyStyle={{ height: 'calc(100% - 41px)', padding: 5 }}
        loading={isLoading}
      >
        <Map
          MapType={mapType}
          ShowedDataType={mapDataType}
          MapData={trafficAndFlowData.selectedDataTypeList}
          MapMaxValue={trafficAndFlowData.maxValue}
        />
      </Card> */}
    </>
  );
};

export default ConnectSource;
