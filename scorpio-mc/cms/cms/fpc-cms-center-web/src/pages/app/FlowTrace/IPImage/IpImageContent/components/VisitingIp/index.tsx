import { Modal, Space, TableProps } from 'antd';
import { Tooltip } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { IpTableTitle } from '../../../typings';
import { IShowCategory } from '../../../typings';
import {
  categoryMap,
  DataShowedType,
  NetworkLocationType,
  NetworklocationTypeFilters,
} from '../../../typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { queryHistogramData, queryHistogramDataTotalNumer } from '../../../service';
import { ESortDirection } from '@/pages/app/analysis/typings';
// import { BOOL_NO } from '@/common/dict';
// import { v1 as uuidv1 } from 'uuid';
import PieChart from '../PieChart';
import type { ColumnsType } from 'antd/lib/table';
import { isIpv4, bytesToSize, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import moment from 'moment';
import { SearchIpImageContext } from '../../..';
import { useSelector } from 'umi';
import type { ConnectState } from '@/models/connect';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/FlowTrace/IPImage/IpImageContent/components/ShowFullCard';
import ExtraBar from '../ExtraBar';
import SmallTable from '../SmallTable';
import useIpDataTable from '../hooks/useIpDataTable';
import { SingleWindowWidth } from '../..';
import EllipsisCom from '@/components/EllipsisCom';
import MoreInformation from '../MoreInformation';
export interface IVisitingIpProps {
  title: string;
  height?: number;
  // IpAddress: string;
  category: string;
  // networkId: string;
  // globalSelectTime: Required<IGlobalTime>;
}

const VisitingIp: React.FC<IVisitingIpProps> = ({
  height,
  // IpAddress,
  // networkId,
  category,
  // globalSelectTime,
}) => {
  //利用context拿到搜索关键字，比如说IP地址和网络名称
  const [searchInfo] = useContext(SearchIpImageContext);
  const { IpAddress, networkIds } = searchInfo;

  //时间应该直接获取
  const globalSelectTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );
  const [isIntranet, setIsIntranet] = useState<string>(NetworkLocationType.ALL);
  const [showedType, setShowedType] = useState<string>(DataShowedType.TABLE);
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [totalData, setTotalData] = useState<any>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalNumber, setTotalNumber] = useState(0);
  const [totalLoading, setTotalLoading] = useState(false);
  const selectedfiltersVal = useMemo(() => {
    if (isIntranet === NetworkLocationType.ALL) {
      return [NetworkLocationType.EXTRANET, NetworkLocationType.INTRANET];
    }
    return [isIntranet];
  }, [isIntranet]);
  const [windowWidth] = useContext(SingleWindowWidth);

  const networkParams = useMemo(() => {
    const networkType = networkIds.split('^');
    if (networkType[1] === 'networkGroup') {
      return { networkGroupId: networkType[0] };
    }
    if (networkType[1] === 'network') {
      return { networkId: networkType[0] };
    }
    return {};
  }, [networkIds]);

  const Tablecolumns = useIpDataTable({
    category,
    IpAddress,
    networkId: networkIds.split('^')[0],
    filterValue: selectedfiltersVal,
    sortProperty,
    sortDirection,
    windowWidth: windowWidth * 0.3,
  });
  // const Tablecolumns: ColumnsType<IpTableTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
  //     dataIndex: 'ip_address',
  //     align: 'center',
  //     width: windowWidth * 0.3,
  //     render: (_, record) => {
  //       let flowRecordFilter = {};
  //       if (category === IShowCategory.VISITEDIP) {
  //         //表格的内容是访问的IP说明表格中的内容是目的IP，props传过来的是源IP
  //         const srcIsV4 = isIpv4(IpAddress);
  //         const targetIsV4 = isIpv4(record.ip_address);
  //         flowRecordFilter = {
  //           operator: EFilterGroupOperatorTypes.AND,
  //           group: [
  //             {
  //               field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
  //               operator: EFilterOperatorTypes.EQ,
  //               operand: IpAddress,
  //             },
  //             {
  //               field: targetIsV4 ? 'ipv4_responder' : 'ipv6_responder',
  //               operator: EFilterOperatorTypes.EQ,
  //               operand: record.ip_address,
  //             },
  //           ],
  //         };
  //       }
  //       if (category === IShowCategory.VISITINGIP) {
  //         //表格的内容是来访的IP，说明props传过来的时目的IP
  //         const srcIsV4 = isIpv4(record.ip_address);
  //         const targetIsV4 = isIpv4(IpAddress);
  //         flowRecordFilter = {
  //           operator: EFilterGroupOperatorTypes.AND,
  //           group: [
  //             {
  //               field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
  //               operator: EFilterOperatorTypes.EQ,
  //               operand: record.ip_address,
  //             },
  //             {
  //               field: targetIsV4 ? 'ipv4_responder' : 'ipv6_responder',
  //               operator: EFilterOperatorTypes.EQ,
  //               operand: IpAddress,
  //             },
  //           ],
  //         };
  //       }

  //       // 可以跳转到会话详单
  //       return (
  //         <div
  //           style={{
  //             whiteSpace: 'nowrap',
  //             width: windowWidth * 0.3,
  //             textOverflow: 'ellipsis',
  //             overflow: 'hidden',
  //             color: '#1890ff',
  //             cursor: 'pointer',
  //           }}
  //           onClick={() => {
  //             const url = getLinkUrl(
  //               `/analysis/trace/flow-record?from=${moment(
  //                 globalSelectTime.originStartTime,
  //               ).valueOf()}&to=${moment(globalSelectTime.originEndTime).valueOf()}&timeType=${
  //                 ETimeType.CUSTOM
  //               }&filter=${encodeURIComponent(
  //                 JSON.stringify([
  //                   flowRecordFilter,
  //                   {
  //                     field: 'network_id',
  //                     operator: EFilterOperatorTypes.EQ,
  //                     operand: networkIds,
  //                   },
  //                 ]),
  //               )}`,
  //             );
  //             jumpNewPage(url);
  //           }}
  //         >
  //           {record.ip_address}
  //         </div>
  //       );
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>IP位置</span>,
  //     dataIndex: 'isIntranet',
  //     align: 'center',
  //     filters: NetworklocationTypeFilters,
  //     // filtered: true,
  //     filteredValue: selectedfiltersVal || null,
  //     render: (_, record) => {
  //       if (record.ip_locality_responder === 0 || record.ip_locality_initiator === 0) {
  //         return '内网';
  //       }
  //       return '外网';
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
  //     dataIndex: 'tcpEstablishedCounts',
  //     align: 'center',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'tcpEstablishedCounts' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return (
  //         <>
  //           {record.tcpEstablishedCounts}/
  //           <span style={{ color: 'red' }}>{record.tcpEstablishedFailCounts}</span>
  //         </>
  //       );
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>流量</span>,
  //     dataIndex: 'totalBytes',
  //     align: 'center',
  //     sorter: true,
  //     defaultSortOrder: 'descend',
  //     sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return bytesToSize(record.totalBytes);
  //     },
  //   },
  // ];

  const queryType = useMemo(() => {
    const type = category.split('_')[1];
    let typeParams = {},
      localityTypeParams = {};
    if (type === 'responder') {
      typeParams = { ipInitiator: IpAddress };
      localityTypeParams = { ipLocalityResponder: isIntranet };
    }
    if (type === 'initiator') {
      typeParams = { ipResponder: IpAddress };
      localityTypeParams = { ipLocalityInitiator: isIntranet };
    }
    if (isIntranet === NetworkLocationType.ALL) {
      return typeParams;
    }
    return { ...typeParams, ...localityTypeParams };
  }, [IpAddress, category, isIntranet]);

  const selectedTime = useMemo(() => {
    return { startTime: globalSelectTime.originStartTime, endTime: globalSelectTime.originEndTime };
  }, [globalSelectTime.originEndTime, globalSelectTime.originStartTime]);

  const queryParams = useMemo(() => {
    return {
      sourceType: 'network',
      packetFileId: null,
      ...selectedTime,
      ...queryType,
      ...networkParams,
      queryProperty: category,
      dsl: `| gentimes report_time start="${globalSelectTime.originStartTime}" end="${globalSelectTime.originEndTime}"`,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [
    selectedTime,
    queryType,
    networkParams,
    category,
    globalSelectTime.originStartTime,
    globalSelectTime.originEndTime,
  ]);

  const basicQueryParams = useMemo(() => {
    return { sortProperty: sortProperty, sortDirection: sortDirection, page, pageSize };
  }, [page, pageSize, sortDirection, sortProperty]);

  const recallNewTotalData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryHistogramData({ ...queryParams, ...basicQueryParams });
    setIsLoading(false);
    if (success) {
      setTotalData(result.content || []);
    }
  }, [basicQueryParams, queryParams]);

  const queryTotalNumber = useCallback(async () => {
    setTotalLoading(true);
    const { success, result } = await queryHistogramDataTotalNumer(queryParams);
    setTotalLoading(false);
    if (success) {
      const { total } = result;
      setTotalNumber(total ?? 0);
    }
  }, [queryParams]);

  useEffect(() => {
    recallNewTotalData();
  }, [recallNewTotalData]);

  // useEffect(() => {
  //   queryTotalNumber();
  // }, [queryTotalNumber]);

  const pieChart = useMemo(() => {
    const titleName: string[] = [];
    const connectionNumber: any = [];
    const flowCount: any = [];
    totalData.forEach((item: any) => {
      titleName.push(item.ip_address);
      if (item.tcpEstablishedCounts > 0) {
        connectionNumber.push({
          name: item.ip_address,
          value: item.tcpEstablishedCounts,
        });
      }
      if (item.totalBytes > 0) {
        flowCount.push({ name: item.ip_address, value: item.totalBytes });
      }
    });
    return {
      titleName: titleName,
      connectionNumber: connectionNumber,
      flowCount: flowCount,
    };
  }, [totalData]);

  const LineBarChart = useMemo(() => {
    const titleName = ['访问次数', '失败次数', '流量数'];
    const xAxisDataName: string[] = [];
    const connectionNumber: number[] = [];
    const connectionFailedNumber: number[] = [];
    const flowCount: number[] = [];
    totalData.forEach((item: any) => {
      xAxisDataName.push(item.ip_address);
      connectionNumber.push(item.tcpEstablishedCounts);
      connectionFailedNumber.push(item.tcpEstablishedFailCounts);
      flowCount.push(item.totalBytes);
    });
    return {
      titleName: titleName,
      xAxisDataName: xAxisDataName,
      connectionNumber: connectionNumber,
      connectionFailedNumber: connectionFailedNumber,
      flowCount: flowCount,
    };
  }, [totalData]);

  const handleTableColSortChange: TableProps<IpTableTitle>['onChange'] = (
    pagination: any,
    filters: any,
    sorter: any,
  ) => {
    const newPage = pagination.current;
    const newPageSize = pagination.pageSize;
    setPage(newPage);
    setPageSize(newPageSize);
    let newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    const newSortProperty = sorter.field;
    // 如果当前排序字段不是现在的字段，默认是倒序
    if (newSortProperty !== sortProperty) {
      newSortDirection = ESortDirection.DESC;
    }
    const newNetworkFilters = filters.isIntranet;
    if (!newNetworkFilters || (newNetworkFilters && newNetworkFilters.length === 2)) {
      console.log(newNetworkFilters);
      setIsIntranet(NetworkLocationType.ALL);
    }
    if (newNetworkFilters && newNetworkFilters.length === 1) {
      setIsIntranet(newNetworkFilters[0]);
    }
    setSortDirection(newSortDirection);
    setSortProperty(newSortProperty);
  };

  const [showMore, setShowMore] = useState(false);

  const moreInformation = () => {
    setShowMore(true);
  };

  const getMoreArea = useCallback(() => {
    return () => {
      return (
        <MoreInformation
          category={category}
          // networkId={networkId}
          // IpAddress={IpAddress}
          selectedTime={selectedTime}
          querydata={queryHistogramData}
          queryTotalNumber={queryHistogramDataTotalNumer}
        />
      );
    };
  }, [category, selectedTime]);

  return (
    <>
      <FullScreenCard
        title={categoryMap[category]}
        extra={
          <Space>
            <ExtraBar
              hasTopSelectionBar={false}
              // top={top}
              // changeTop={setTop}
              hasTypeChangeBar={false}
              // types={[DataShowedType.PIECHART]}
              showedType={showedType}
              changeShowedType={setShowedType}
            />
            <span className="link" onClick={moreInformation}>
              更多
            </span>
          </Space>
        }
        loading={isLoading}
      >
        <HightContext.Consumer>
          {(isFullscreen) => {
            return (
              <div
                style={{
                  height: isFullscreen ? 'calc(100vh - 40px)' : '440px',
                  overflow: 'hidden',
                }}
              >
                {showedType != DataShowedType.PIECHART && (
                  <SmallTable
                    columns={Tablecolumns}
                    dataSource={totalData}
                    onChange={handleTableColSortChange}
                    totalNumber={totalNumber}
                    page={page + 1}
                    pageSize={pageSize}
                    isfull={isFullscreen}
                  />
                )}
                {showedType == DataShowedType.PIECHART && (
                  <PieChart
                    configName={pieChart.titleName}
                    configDataOne={pieChart.connectionNumber}
                    configDataTwo={pieChart.flowCount}
                    isFull={isFullscreen}
                  />
                )}
              </div>
            );
          }}
        </HightContext.Consumer>
      </FullScreenCard>
      <Modal
        title={`更多${categoryMap[category]}`}
        visible={showMore}
        destroyOnClose={true}
        width="auto"
        // style={{paddingTop: 10}}
        footer={null}
        onCancel={() => {
          setShowMore(false);
        }}
        maskClosable={false}
      >
        {getMoreArea()()}
      </Modal>
    </>
  );
};

export default VisitingIp;
