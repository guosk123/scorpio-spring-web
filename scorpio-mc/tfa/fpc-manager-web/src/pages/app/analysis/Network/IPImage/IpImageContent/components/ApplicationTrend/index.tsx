import { EMetricApiType } from '@/common/api/analysis';
import { Card, Modal } from 'antd';
import { connect, useSelector } from 'umi';
import type { Dispatch } from 'umi';
import type { ConnectState } from '@/models/connect';
import type { IApplicationMap } from '@/pages/app/configuration/SAKnowledge/typings';
import {
  ANALYSIS_APPLICATION_TYPE_ENUM,
  ESortDirection,
  ESourceType,
} from '@/pages/app/analysis/typings';
import { v4 as uuidv4 } from 'uuid';
import { useContext, useEffect, useMemo, useState } from 'react';
import { categoryMap, IShowCategory } from '../../../typings';
import { queryNetworkFlowHistogram } from '@/pages/app/analysis/service';
import type { TrendChartData } from '@/pages/app/analysis/components/AnalysisChart';
import { completeTimePoint } from '@/pages/app/analysis/components/FlowAnalysis';
import AnalysisChart from '@/pages/app/analysis/components/AnalysisChart';
import {
  EFormatterType,
  EModelAlias,
  fieldFormatterFuncMap,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import type { ICityMap, IProvinceMap } from '@/pages/app/configuration/Geolocation/typings';
import { SearchIpImageContext } from '../../..';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ipV4Regex, ipV6Regex, isCidr } from '@/utils/utils';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IApplicationProps } from '@/pages/app/analysis/Flow/Application';
import Application from '@/pages/app/analysis/Flow/Application';
import Location from '@/pages/app/analysis/Flow/Location';
interface TrendProps extends IApplicationProps {
  dispatch: Dispatch;
  category: string;
  // IpAddress: string;
  // networkId: string;
  // globalSelectedTime: IGlobalSelectedTime;
}

function ApplicationTrend(props: TrendProps) {
  const { category } = props;
  //利用context拿到搜索关键字，比如说IP地址和网络名称
  const [searchInfo] = useContext(SearchIpImageContext);
  const { IpAddress, networkIds } = searchInfo;
  //时间应该直接获取
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );

  const networkId = useMemo(() => {
    if (networkIds === 'ALL') {
      return undefined;
    }
    return networkIds;
  }, [networkIds]);

  // 获取应用字典
  const applicationDic = useSelector<ConnectState, IApplicationMap>(
    (state) => state.SAKnowledgeModel.allApplicationMap,
  );
  //获取省份的字典
  const provinceMap = useMemo(() => {
    return getEnumValueFromModelNext(EModelAlias.province)?.map as IProvinceMap;
  }, []);
  //获取城市的字典
  const cityMap = useMemo(() => {
    return getEnumValueFromModelNext(EModelAlias.city)?.map as ICityMap;
  }, []);

  // 是否loading的状态
  const [queryLoding, setQueryLoding] = useState(true);
  // 查看前多少个数据

  const [sortDirection] = useState(ESortDirection.DESC);
  const [sortProperty] = useState('total_bytes');

  const [flowHistogramData, setFlowHistogramData] = useState([]);

  const currentTitle = useMemo(() => {
    switch (category) {
      case EMetricApiType.application:
        return 'Top10应用宽带趋势';
      case EMetricApiType.location:
        return 'Top10地区宽带趋势';
      default:
        return '';
    }
  }, [category]);

  // 获取dsl查询语句
  const currentDsl = useMemo(() => {
    let dsl = undefined;
    let filterSpl = `(ip_address = ${IpAddress})`;
    if (networkId) {
      filterSpl += ` AND (network_id = ${networkId})`;
    }
    if (category === IShowCategory.APPLICATIONTREND) {
      filterSpl += ` AND (type = ${String(ANALYSIS_APPLICATION_TYPE_ENUM['应用'])})`;
    }
    if (category === IShowCategory.LOCATIONTREND) {
      filterSpl += ` AND (country_id = 1)`;
    }
    dsl = `${filterSpl} | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`;
    return dsl;
  }, [IpAddress, category, globalSelectedTime.endTime, globalSelectedTime.startTime, networkId]);

  const LocationFilterData: any = useMemo(() => {
    if (category === EMetricApiType.location) {
      return [
        {
          field: 'country_id',
          operator: '=',
          operand: '1',
          operandText: '中国',
        },
      ];
    }
    return [];
  }, [category]);

  const IpAddressFilterData: any = useMemo(() => {
    const filters = [];
    // console.log(IpAddress, 'ip');
    if (ipV4Regex.test(IpAddress) || isCidr(IpAddress, 'IPv4')) {
      filters.push({
        field: 'ip_address',
        operator: '=',
        operand: IpAddress,
        fieldText: 'IP',
        type: 'IPv4',
        operandText: IpAddress,
      });
    }
    if (ipV6Regex.test(IpAddress) || isCidr(IpAddress, 'IPv6')) {
      filters.push({
        field: 'ip_address',
        operator: '=',
        operand: IpAddress,
        fieldText: 'IP',
        type: 'IPv6',
        operandText: IpAddress,
      });
    }
    return filters;
  }, [IpAddress]);

  const networkIdFilterData = useMemo(() => {
    const filters = [];
    if (networkId) {
      filters.push({ field: 'network_id', operator: EFilterOperatorTypes.EQ, operand: networkId });
    }
    return filters;
  }, [networkId]);

  const hasType = useMemo(() => {
    switch (category) {
      case EMetricApiType.application:
        return ANALYSIS_APPLICATION_TYPE_ENUM['应用'];
      case EMetricApiType.location:
        return undefined;
      default:
        return undefined;
    }
  }, [category]);
  // TODO: 查询参数约束
  const queryParams = useMemo(() => {
    return {
      sourceType: ESourceType.NETWORK,
      networkId: networkId,
      metricApi: category as EMetricApiType,
      sortProperty: sortProperty,
      sortDirection: sortDirection,
      startTime: globalSelectedTime.startTime as string,
      endTime: globalSelectedTime.endTime as string,
      interval: globalSelectedTime.interval as number,
      type: hasType,
      count: 10,
      dsl: currentDsl,
      drilldown: '1' as const,
    };
  }, [
    networkId,
    category,
    sortProperty,
    sortDirection,
    globalSelectedTime.startTime,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    hasType,
    currentDsl,
  ]);

  useEffect(() => {
    setQueryLoding(true);
    // 请求图表的参数
    queryNetworkFlowHistogram(queryParams).then((res) => {
      const { success, result } = res;
      if (success) {
        setFlowHistogramData(result);
        setQueryLoding(false);
      }
    });
  }, [queryParams]);

  const currentMap = useMemo(() => {
    switch (category) {
      case EMetricApiType.application:
        return applicationDic;
      case EMetricApiType.location:
        return provinceMap;
      default:
        return '';
    }
  }, [applicationDic, category, provinceMap]);

  const currentIndex = useMemo(() => {
    switch (category) {
      case EMetricApiType.application:
        return 'applicationId';
      case EMetricApiType.location:
        return 'provinceId';
      default:
        return '';
    }
  }, [category]);

  const chartData = useMemo(() => {
    const tmp: Record<string, TrendChartData> = {};
    if (flowHistogramData.length > 0) {
      flowHistogramData.forEach(
        (item: {
          countryId: number;
          cityId: number;
          provinceId: number;
          applicationId: number;
          timestamp: string;
          totalBytes: number;
        }) => {
          // console.log(currentIndex, 'currentIndex');
          const currentId = item[currentIndex];
          // console.log(currentId, 'currentId');
          const currentName = currentMap[currentId];
          const seriesName = currentName?.nameText || String(currentId);
          if (seriesName) {
            if (!tmp[seriesName]) {
              tmp[seriesName] = [];
            }
            tmp[seriesName].push([item.timestamp as string, (item.totalBytes as number) || 0]);
            // console.log(tmp[seriesName], 'tmp[seriesName]');
          }
        },
      );
    }

    console.log(tmp, 'tmp');
    Object.keys(tmp).forEach((seriesName) => {
      tmp[seriesName] = completeTimePoint(
        tmp[seriesName],
        globalSelectedTime.startTime!,
        globalSelectedTime.endTime!,
        globalSelectedTime.interval,
      );
      tmp[seriesName] = tmp[seriesName].map((item) => {
        return [item[0], item[1] / (globalSelectedTime.interval || 1)];
      });
    });

    return tmp;
  }, [
    currentIndex,
    currentMap,
    flowHistogramData,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
  ]);

  const topTenChart = useMemo(() => {
    return (
      <AnalysisChart
        key={uuidv4()}
        data={chartData}
        loading={queryLoding}
        // seriesOrder={seriesOrder}
        unitConverter={fieldFormatterFuncMap[EFormatterType.BYTE_PS]}
        filterCondition={[...IpAddressFilterData, ...networkIdFilterData, ...LocationFilterData]}
        networkId={networkId}
        // serviceId={serviceId}
        brushMenus={[{ text: '数据包', key: 'packet' }]}
        selectedTimeInfo={globalSelectedTime}
        // markArea={beforeOldestPacketArea}
      />
    );
  }, [
    IpAddressFilterData,
    LocationFilterData,
    chartData,
    globalSelectedTime,
    networkId,
    networkIdFilterData,
    queryLoding,
  ]);

  const [showMore, setShowMore] = useState<boolean>(false);

  const moreInformation = () => {
    setShowMore(true);
  };

  const getShowArea = (type: any) => {
    if (type === EMetricApiType.application) {
      return () => {
        return (
          <Application
            {...props}
            currentNetworkId={networkIds}
            currentFilterCondition={IpAddressFilterData}
            needHeight={100}
          />
        );
      };
    }
    if (type === EMetricApiType.location) {
      return () => {
        return (
          <Location
            networkId={networkIds}
            filterCondition={[...IpAddressFilterData, ...LocationFilterData]}
            needHeight={100}
          />
        );
      };
    }
    return () => {
      return <></>;
    };
  };

  return (
    <>
      <Card
        size="small"
        title={currentTitle}
        bodyStyle={{ padding: 0, paddingTop: '8px' }}
        style={{ marginBottom: '15px' }}
        loading={queryLoding}
        extra={
          <span className="link" onClick={moreInformation}>
            更多
          </span>
        }
      >
        {topTenChart}
      </Card>
      <Modal
        title={`更多${categoryMap[category]}`}
        visible={showMore}
        onCancel={() => {
          setShowMore(false);
        }}
        destroyOnClose={true}
        width="auto"
        footer={null}
      >
        {getShowArea(category)()}
      </Modal>
    </>
  );
}

const mapStateToProps = ({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
});

export default connect(mapStateToProps)(ApplicationTrend);
