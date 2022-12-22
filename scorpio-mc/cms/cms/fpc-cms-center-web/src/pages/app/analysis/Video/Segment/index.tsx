import { filterCondition2Spl, filterTraversal } from '@/components/FieldFilter';
import type { IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { INetworkSensor } from '@/pages/app/Configuration/Network/typings';
import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { SegmentOriginData } from '../../components/Segment/typing';
import { VideoContext } from '../components/NetworkTimeLayout';
import { VideoTabsContext } from '../components/VideoEditTabs';
import { queryNetworkSegmentation, queryNetworkSegmentationHistogram } from '../services';
import SegmentHeader from './SegmentHeader';
import { SEGMENT_ITEM_LIST } from './SegmentHeader/typing';
import type { SegmentTableData } from './SegmentTables';
import SegmentTables from './SegmentTables';

/** 过滤列 */
const filterField = [
  {
    title: '源IP',
    dataIndex: 'src_ip',
    type: EFieldType.IP,
    operandType: EFieldOperandType.IP,
  },
  {
    title: '目的IP',
    dataIndex: 'dest_ip',
    type: EFieldType.IP,
    operandType: EFieldOperandType.IP,
  },
];

export default function Segment() {
  const { network, globalSelectedTime, setNetworkSelect } = useContext(VideoContext)!;
  const [state] = useContext(VideoTabsContext);
  /** 选择的分段id */
  const [selectedSegmentId, setSelectedSegmentId] = useState<string>('');
  const [segmentData, setSegmentData] = useState<Record<string, SegmentOriginData>>({});
  const [segmentDataLoading, setSegmentDataLoading] = useState<boolean>(true);
  /** 图表数据 */
  const [histgramData, setHistgramData] = useState<SegmentTableData>({});
  const [histgramDataLoading, setHistgramDataLoading] = useState<boolean>(true);
  /** 过滤条件 */
  const filterCondition = useMemo<IFilterCondition>(() => {
    const filter = (state as any)?.shareInfo?.filter;
    return typeof filter === 'string' ? JSON.parse(filter || '[]') : filter || [];
  }, []);

  /** dsl */
  const dsl = useMemo(() => {
    // 过滤条件转dsl
    const filterSpl = filterCondition2Spl(filterCondition, filterField);
    return filterSpl;
  }, [filterCondition]);

  /** 获取分段分析数据 */
  const querySegmentData = useCallback(async () => {
    setSegmentDataLoading(true);
    const { success, result } = await queryNetworkSegmentation({
      dsl,
      startTime: globalSelectedTime?.startTime || '',
      endTime: globalSelectedTime?.endTime || '',
    });
    if (success) {
      let dataObj = {} as any;
      let dataObjExceptCurrent = {} as any;
      for (const i in result) {
        if (i === (network as INetworkSensor)?.networkInSensorId || network?.id) {
          dataObj = {
            ...dataObj,
            [i]: result[i],
          };
        } else {
          dataObjExceptCurrent = {
            ...dataObjExceptCurrent,
            [i]: result[i],
          };
        }
      }
      dataObj = {
        ...dataObj,
        ...dataObjExceptCurrent,
      };
      setSegmentData(dataObj);
    }
    setSegmentDataLoading(false);
  }, [dsl, globalSelectedTime?.startTime, globalSelectedTime?.endTime, network]);

  /** 获取图表数据 */
  const queryHistgram = useCallback(async () => {
    setHistgramDataLoading(true);
    const { success, result } = await queryNetworkSegmentationHistogram({
      dsl: `${dsl} | gentimes report_time start="${globalSelectedTime?.startTime}" end="${globalSelectedTime?.endTime}"`,
      networkId: selectedSegmentId,
      interval: globalSelectedTime?.interval || 3600,
    });
    if (success) {
      let segmentTableObj = {} as SegmentTableData;
      SEGMENT_ITEM_LIST.forEach(({ index, label }) => {
        const seriesList = (result as any[]).map((item) => {
          const time = item.reportTime;
          return [time, item[index]];
        }) as any;
        segmentTableObj = {
          ...segmentTableObj,
          [index]: {
            [label]: seriesList,
          },
        };
      });
      setHistgramData(segmentTableObj);
    }
    setHistgramDataLoading(false);
  }, [
    globalSelectedTime?.startTime,
    globalSelectedTime?.endTime,
    globalSelectedTime?.interval,
    selectedSegmentId,
    dsl,
  ]);

  const rtpMetaDataFilter = useMemo(() => {
    return filterTraversal(filterCondition, (filter) => {
      const formatIp = (type: 'src' | 'dest') => {
        const ip = filter?.operand as string;
        const isV4 = ip && isIpv4(ip);
        const field = (() => {
          if (isV4) {
            return type === 'src' ? 'ipv4_initiator' : 'ipv4_responder';
          } else {
            return type === 'src' ? 'ipv6_initiator' : 'ipv6_responder';
          }
        })();
        return {
          ...filter,
          field,
        };
      };
      switch (filter?.field) {
        case 'src_ip':
          return formatIp('src');
        case 'dest_ip':
          return formatIp('dest');
      }
      return filter;
    }).concat({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: (network as INetworkSensor)?.networkInSensorId || network?.id,
    });
  }, [filterCondition, network]);

  useEffect(() => {
    queryHistgram();
  }, [queryHistgram]);

  /** 初始化分段分析数据 */
  useEffect(() => {
    querySegmentData();
  }, [querySegmentData]);

  useEffect(() => {
    /** 初始化网络id */
    setSelectedSegmentId((network as INetworkSensor)?.networkInSensorId || network?.id || '');
  }, [network, network?.id]);

  useEffect(() => {
    if (setNetworkSelect) {
      setNetworkSelect(false);
    }
  }, []);

  return (
    <>
      <SegmentHeader
        segmentData={segmentData}
        loading={segmentDataLoading}
        selectedSegmentId={selectedSegmentId}
        onFooterClick={setSelectedSegmentId}
        onItemClick={() => {
          jumpNewPage(
            getLinkUrl(
              `/analysis/trace/flow-record?&filter=${encodeURIComponent(
                JSON.stringify(rtpMetaDataFilter),
              )}`,
            ),
          );
        }}
      />
      <SegmentTables loading={histgramDataLoading} dataSources={histgramData} />
    </>
  );
}
