import { useEffect, useMemo, useState } from 'react';
import type { INetworkDetails } from '../../typings';
import {
  EsegmentAnalysisInterfaceMap,
  EsegmentAnalysisSearchType,
  EsegmentAnalysisTypeToFlowFilterMap,
  SEGMENT_ITEM_LIST,
} from '../../typings';
import { NetworkDetailNameMap } from '../../typings';
import { createContext } from 'react';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ISearchBoxInfo } from '../SearchBox';
import { connect, useSelector } from 'umi';
import { snakeCase } from '@/utils/utils';
import { ESortDirection } from './components/SegmentsTable';
import SegmentsTable from '../ShowPart/components/SegmentsTable';
import { SegmentsCol } from './components/SegmentsTable/constant';
import { querySegmentAnalysisShowData } from '../../service';
import type { ConnectState } from '@/models/connect';
import type { TTheme } from 'umi';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { formatSegmentData } from '../../../components/Segment/utils';
import SegmentContainer from '../../../components/Segment/components/SegmentContainer';
import SegmentCard from '../../../components/Segment/components/SegmentCard';
import type { ISegmentItemFormattedType } from '../../../components/Segment/typing';
import SegmentConnector from '../../../components/Segment/components/SegmentConnector';
import SegmentAnalysis from '../../../components/Segment/components/SegmentAnalysis';
import storage from '@/utils/frame/storage';
import useNetwork from '../../../components/Segment/hooks/useNetworks';
import { INetworkGroupMap, INetworkSensorMap } from '@/pages/app/Configuration/Network/typings';
import { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';

export const TotalLinesContext = createContext(1);

export const getDefaultTableKeys = () => {
  const localKeys = storage.get('SegmentTableKey-order');
  return localKeys;
};

interface Props {
  searchInfo: ISearchBoxInfo;
  onChangeInfo?: any;
  globalSelectedTime: IGlobalTime;
}
function ShowPart(props: Props) {
  const { searchInfo, globalSelectedTime } = props;
  console.log(searchInfo, 'search');
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme);

  const [queryLoding, setQueryLoding] = useState(true);

  const [networks, setNetworks] = useState<INetworkDetails[]>([]);

  const [columns, setColumns] = useState<string[]>([]);
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  const changeSortProperty = (newSortProperty: string) => {
    setSortProperty(newSortProperty);
  };
  const changeSortDirection = (newSortDirection: ESortDirection) => {
    setSortDirection(newSortDirection);
  };

  const defaultShowColumns = useMemo(() => {
    const localcolKeys = getDefaultTableKeys();
    if (localcolKeys) {
      // console.log(localcolKeys, 'localcolKeys');
      return [
        ...new Set([
          ...JSON.parse(localcolKeys)
            .filter((item: any) => !item.hideInTable)
            .map((item: any) => item.dataIndex),
          ...Object.keys(NetworkDetailNameMap),
        ]),
      ];
    }
    return SegmentsCol.map((item) => item.dataIndex);
  }, []);

  const queryColumns = useMemo(() => {
    // console.log(columns, 'columns');
    if (columns.length == 0) {
      return defaultShowColumns;
    } else {
      return [...new Set([...columns, ...Object.keys(NetworkDetailNameMap)])];
    }
  }, [columns, defaultShowColumns]);
  const payloadDsl = useMemo(() => {
    let filterSpl = `(${
      EsegmentAnalysisTypeToFlowFilterMap[searchInfo?.segmentAnalysisSearchType]
    } ${EFilterOperatorTypes.EQ} ${searchInfo?.content})`;
    if (searchInfo?.segmentAnalysisSearchType === EsegmentAnalysisSearchType.IPCONVERSATION) {
      const ips = searchInfo?.content.split('-');
      filterSpl = `(ip_a_address ${EFilterOperatorTypes.EQ} "${ips[0]}") AND (ip_b_address ${EFilterOperatorTypes.EQ} "${ips[1]}")`;
    }
    return (
      filterSpl +
      ` | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`
    );
  }, [
    globalSelectedTime.endTime,
    globalSelectedTime.startTime,
    searchInfo?.content,
    searchInfo?.segmentAnalysisSearchType,
  ]);

  const networkParams = useMemo(() => {
    const networkValues = searchInfo?.networkIds;
    const networkIds: string[] = [],
      networkGroupIds: string[] = [];
    if (networkValues) {
      networkValues.forEach((item) => {
        const networkType = item.split('^')[1],
          networkItem = item.split('^')[0];
        if (networkType == 'networkGroup') {
          networkGroupIds.push(networkItem);
        }
        if (networkType == 'network') {
          networkIds.push(networkItem);
        }
      });
    }
    return {
      networkId: networkIds.length > 0 ? networkIds.join(',') : undefined,
      networkGroupId: networkGroupIds.length > 0 ? networkGroupIds.join(',') : undefined,
    };
  }, [searchInfo?.networkIds]);

  const payload = useMemo(() => {
    return {
      sourceType: 'network',
      ...networkParams,
      serviceId: '',
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortProperty: snakeCase(sortProperty || ''),
      sortDirection: sortDirection,
      dsl: payloadDsl,
      columns: queryColumns.join(','),
      // drilldown,
    };
  }, [
    networkParams,
    globalSelectedTime.startTime,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    sortProperty,
    sortDirection,
    payloadDsl,
    queryColumns,
  ]);

  useEffect(() => {
    setQueryLoding(true);
    const interfaceName = EsegmentAnalysisInterfaceMap[searchInfo?.segmentAnalysisSearchType];
    if (interfaceName) {
      querySegmentAnalysisShowData(interfaceName, payload).then((res) => {
        const { success, result } = res;
        if (success) {
          setQueryLoding(false);
          // setDataSource(result);
          setNetworks(result);
        }
      });
    }
  }, [payload, searchInfo?.segmentAnalysisSearchType]);

  const ImportantNetworks = useMemo(() => {
    const filterKeys = [...Object.keys(NetworkDetailNameMap), 'networkId','networkGroupId'];
    const res = networks.map((item) => {
      const network: any = {};
      const networkKeys = Object.keys(item);
      filterKeys.forEach((i: string) => {
        if (networkKeys.includes(i)) {
          network[i] = item[i] ?? 0;
        }
        if (!networkKeys.includes(i)) {
          if (i == 'tcpClientRetransmissionRate') {
            network[i] =
              +(item.tcpClientRetransmissionPackets / item.tcpClientPackets).toFixed(2) || 0;
          }
          if (i == 'tcpServerRetransmissionRate') {
            network[i] =
              +(item.tcpServerRetransmissionPackets / item.tcpServerPackets).toFixed(2) || 0;
          }
        }
      });
      return network;
    });
    return res;
  }, [networks]);
  const [selectedSegmentId, setSelectedSegmentId] = useState<string>('');

  const networkData = useMemo(() => {
    const networkCollections = {};
    ImportantNetworks.forEach((item: any) => {
      const currentNetworkId = item.networkId || item.networkGroupId;
      const networkDel = item;
      delete networkDel.networkId;
      delete networkDel.networkGroupId;
      networkCollections[currentNetworkId] = networkDel;
    });
    const newData = formatSegmentData(SEGMENT_ITEM_LIST, networkCollections);
    return newData;
  }, [ImportantNetworks]);

  

  // const NetworksItems = useMemo(() => {
  //   const fullnetworks = ImportantNetworks.reduce(
  //     (acc: any, item) => {
  //       if (acc[acc.length - 1].length >= 5) {
  //         return [...acc, [item]];
  //       }
  //       acc[acc.length - 1].push(item);
  //       return acc;
  //     },
  //     [[]],
  //   );
  //   let lastNetworksGroup = fullnetworks.pop();
  //   if (lastNetworksGroup.length < 5 && lastNetworksGroup.length > 0) {
  //     lastNetworksGroup = lastNetworksGroup.concat(
  //       new Array(5 - lastNetworksGroup.length).fill({}),
  //     );
  //   }
  //   return [...fullnetworks, lastNetworksGroup];
  // }, [ImportantNetworks]);

  // const totalLines = useMemo(() => {
  //   const lines = Math.ceil(networks.length / 5);
  //   return lines;
  // }, [networks.length]);

  return (
    <>
      {searchInfo ? (
        <>
          <SegmentContainer>
            {Object.keys(networkData).map((key, index: number) => {
              return (
                <>
                  <SegmentCard
                    onFooterClick={(id) => {
                      setSelectedSegmentId(id);
                    }}
                    id={key}
                    theme={theme}
                    bordered={false}
                    align={'top'}
                    footer={key}
                    footerConverter={true}
                    selectedId={selectedSegmentId}
                  >
                    <SegmentAnalysis
                      id={key}
                      theme={theme}
                      dataSource={networkData[key]}
                      onClick={(id: string, item: ISegmentItemFormattedType) => {
                        console.log(id, item);
                        // TODO 跳转下钻
                      }}
                    />
                  </SegmentCard>
                  {/* 分段连接器 */}
                  {index !== Object.keys(networkData).length - 1 && <SegmentConnector />}
                </>
              );
            })}
          </SegmentContainer>

          <SegmentsTable
            searchBoxInfo={searchInfo}
            sortProperty={sortProperty}
            sortDirection={sortDirection}
            changeSortProperty={changeSortProperty}
            changeSortDirection={changeSortDirection}
            dataSource={networks}
            defaultShowColumns={defaultShowColumns}
            tableColumnsChange={setColumns}
            queryLoding={queryLoding}
          />
        </>
      ) : (
        <div style={{ display: 'none' }} />
      )}
    </>
  );
}

export default connect((state: any) => {
  const {
    appModel: { globalSelectedTime },
  } = state;
  return { globalSelectedTime };
})(ShowPart);
