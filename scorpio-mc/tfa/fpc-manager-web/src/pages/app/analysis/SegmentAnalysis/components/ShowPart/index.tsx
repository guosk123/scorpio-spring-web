import { useEffect, useMemo, useState } from 'react';
import type { INetworkDetails } from '../../typings';
import { SEGMENT_ITEM_LIST } from '../../typings';
import {
  NetworkDetailNameMap,
  EsegmentAnalysisSearchType,
  EsegmentAnalysisInterfaceMap,
  EsegmentAnalysisTypeToFlowFilterMap,
} from '../../typings';
import { createContext } from 'react';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ISearchBoxInfo } from '../SearchBox';
import type { TTheme } from 'umi';
import { connect, useSelector } from 'umi';
import { snakeCase } from '@/utils/utils';
import { ESortDirection } from './components/SegmentsTable';
import SegmentsTable from '../ShowPart/components/SegmentsTable';
import { SegmentsCol } from './components/SegmentsTable/constant';
import { querySegmentAnalysisShowData } from '../../service';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { v1 as uuidv1 } from 'uuid';
import storage from '@/utils/frame/storage';
import SegmentContainer from '../../../components/Segment/components/SegmentContainer';
import type { ConnectState } from '@/models/connect';
import SegmentCard from '../../../components/Segment/components/SegmentCard';
import SegmentAnalysis from '../../../components/Segment/components/SegmentAnalysis';
import SegmentConnector from '../../../components/Segment/components/SegmentConnector';
import type { ISegmentItemFormattedType } from '../../../components/Segment/typing';
import { formatSegmentData } from '../../../components/Segment/utils';
import type { INetworkMap } from '@/pages/app/configuration/Network/typings';
import type { ILogicalSubnetMap } from '@/pages/app/configuration/LogicalSubnet/typings';

export const TotalLinesContext = createContext(1);

interface Props {
  searchInfo: ISearchBoxInfo;
  onChangeInfo?: any;
  globalSelectedTime: IGlobalTime;
}

export const getDefaultTableKeys = () => {
  const localKeys = storage.get('SegmentTableKey-order');
  return localKeys;
};

function ShowPart(props: Props) {
  const { searchInfo, globalSelectedTime } = props;
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

  const allNetworkMap = useSelector<ConnectState, Required<INetworkMap>>(
    (state) => state.networkModel.allNetworkMap,
  );

  const allLogicalSubnetMap = useSelector<ConnectState, Required<ILogicalSubnetMap>>(
    (state) => state.logicSubnetModel.allLogicalSubnetMap,
  );

  const allNetworksMap = useMemo(() => {
    return {
      ...allNetworkMap,
      ...allLogicalSubnetMap,
    };
  }, [allLogicalSubnetMap, allNetworkMap]);

  const payloadDsl = useMemo(() => {
    let filterSpl = `(${
      EsegmentAnalysisTypeToFlowFilterMap[searchInfo?.segmentAnalysisSearchType]
    } ${EFilterOperatorTypes.EQ} ${searchInfo?.content})`;
    if (searchInfo?.segmentAnalysisSearchType === EsegmentAnalysisSearchType.IPCONVERSATION) {
      const ips = searchInfo?.content.split('-');
      // let queryAType = '';
      // let queryBType = '';
      // if (ipV4Regex.test(ips[0]) || isCidr(ips[0], 'IPv4')) {
      //   queryAType = 'ip_a_address<IPv4>';
      // }
      // if (ipV6Regex.test(ips[0]) || isCidr(ips[0], 'IPv6')) {
      //   queryAType = 'ip_a_address<IPv6>';
      // }
      // if (ipV4Regex.test(ips[1]) || isCidr(ips[1], 'IPv4')) {
      //   queryBType = 'ip_b_address<IPv4>';
      // }
      // if (ipV6Regex.test(ips[1]) || isCidr(ips[1], 'IPv6')) {
      //   queryBType = 'ip_b_address<IPv6>';
      // }
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

  const payload = useMemo(() => {
    return {
      sourceType: 'network',
      networkId: searchInfo?.networkIds.join(','),
      serviceId: '',
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortProperty: snakeCase(sortProperty || ''),
      sortDirection: sortDirection,
      dsl: payloadDsl,
      columns: queryColumns.join(','),
      queryId: uuidv1(),
      // drilldown,
    };
  }, [
    searchInfo?.networkIds,
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
          setNetworks(result);
        }
      });
    }
  }, [payload, searchInfo?.segmentAnalysisSearchType]);

  const networksData = useMemo(() => {
    networks.forEach((i: INetworkDetails) => {
      const name = allNetworksMap[i.networkId]?.name;
      i.networkId = name ?? i.networkId;
    });
    return networks;
  }, [allNetworksMap, networks]);

  const ImportantNetworks = useMemo(() => {
    const filterKeys = [...Object.keys(NetworkDetailNameMap), 'networkId'];
    const res = networks.map((item) => {
      const network = {};
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
      const currentNetworkId = item.networkId;
      const networkDel = item;
      delete networkDel.networkId;
      networkCollections[currentNetworkId] = networkDel;
    });
    // console.log(networkCollections, 'networkCollections');
    const newData = formatSegmentData(SEGMENT_ITEM_LIST, networkCollections);
    return newData;
  }, [ImportantNetworks]);

  // const networkBaseSet = useMemo(() => {
  //   const currentMinSet = {};
  //   Object.keys(NetworkDetailNameMap).forEach((i: string) => {
  //     const currentItem = ImportantNetworks.map((item) => item[i]);
  //     const minValue = Math.min.apply(null, currentItem);
  //     currentMinSet[i] = minValue == Infinity ? 0 : minValue;
  //   });
  //   // console.log(currentMinSet, 'currentMinSet');
  //   return currentMinSet;
  // }, [ImportantNetworks]);

  // const totalLines = useMemo(() => {
  //   const lines = Math.ceil(networks.length / 5);
  //   return lines;
  // }, [networks.length]);

  return (
    <>
      {searchInfo ? (
        <>
          {/* <TotalLinesContext.Provider value={totalLines}>
            <div className={styles.wholeDisplay}>
              <Spin spinning={queryLoding}>
                {totalLines > 0 ? (
                  <div className={styles.wholeDisplay__displayLine}>
                    {ImportantNetworks.map((item: any, index: number) => {
                      return (
                        <Segment
                          networkIndex={index}
                          networkDetail={item}
                          networkId={item.networkId}
                          networkBaseSet={networkBaseSet}
                        />
                      );
                    })}
                  </div>
                ) : (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
                )}
              </Spin>
            </div>
          </TotalLinesContext.Provider> */}
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
