import { Modal, Space, TableProps } from 'antd';
import { Tooltip, Card } from 'antd';
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
import { queryHistogramData, queryHistogramDataTotalNumer } from '../../../service';
import { ESortDirection } from '@/pages/app/analysis/typings';
// import { BOOL_NO } from '@/common/dict';
// import { v1 as uuidv1 } from 'uuid';
import PieChart from '../PieChart';
import BarLineChart from '../LineBarChart';
import type { ColumnsType } from 'antd/lib/table';
import { bytesToSize } from '@/utils/utils';
import LinkMenu, { EIP_DRILLDOWN_MENU_KEY } from '../LinkMenu';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import SmallTable from '../SmallTable';
import ExtraBar from '../ExtraBar';
import EllipsisCom from '@/components/EllipsisCom';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/analysis/Network/IPImage/IpImageContent/components/ShowFullCard';
import { SearchIpImageContext } from '../../..';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import useIpDataTable from '../hooks/useIpdataTable';
import MoreInformation from '../MoreInformation';
import { SingleWindowWidth } from '../..';
export interface IVisitingIpProps {
  title: string;
  height?: number;
  // IpAddress: string;
  category: string;
  // networkId: string | null;
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
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalNumber, setTotalNumber] = useState(0);
  // const [totalLoading, setTotalLoading] = useState(false);
  const selectedfiltersVal = useMemo(() => {
    if (isIntranet === NetworkLocationType.ALL) {
      return [NetworkLocationType.EXTRANET, NetworkLocationType.INTRANET];
    }
    return [isIntranet];
  }, [isIntranet]);

  const networkId = useMemo(() => {
    if (networkIds === 'ALL') {
      return undefined;
    }
    return networkIds;
  }, [networkIds]);

  const [windowWidth] = useContext(SingleWindowWidth);
  const Tablecolumns = useIpDataTable({
    category,
    IpAddress,
    networkId,
    filterValue: selectedfiltersVal,
    sortProperty,
    sortDirection,
    widowWidth: windowWidth * 0.3,
  });

  // const Tablecolumns: ColumnsType<IpTableTitle> = useMemo(() => {
  //   return [
  //     {
  //       title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
  //       dataIndex: 'ip_address',
  //       width: 200,
  //       render: (_, record) => {
  //         let srcIp = '',
  //           desIp = '';
  //         if (category === IShowCategory.VISITEDIP) {
  //           //表格的内容是访问的IP说明表格中的内容是目的IP，props传过来的是源IP
  //           srcIp = IpAddress;
  //           desIp = record.ip_address;
  //         }
  //         if (category === IShowCategory.VISITINGIP) {
  //           //表格的内容是来访的IP，说明props传过来的时目的IP
  //           srcIp = record.ip_address;
  //           desIp = IpAddress;
  //         }
  //         // 可以跳转到会话详单
  //         return (
  //           <FilterBubble
  //             dataIndex={record.ip_address}
  //             label={<EllipsisCom style={{ width: 200 }}>{record.ip_address}</EllipsisCom>}
  //             hasFilter={false}
  //             DrilldownMenu={
  //               <LinkMenu
  //                 MenuItemsGroup={[
  //                   {
  //                     label: '跳转到其他页',
  //                     key: 'jumpToOtherPage',
  //                     children: [{ label: '会话详单', key: EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD }],
  //                   },
  //                 ]}
  //                 settings={{
  //                   networkId: networkId,
  //                   ipPair: { srcIp: srcIp, desIp: desIp },
  //                 }}
  //               />
  //             }
  //           />
  //         );
  //       },
  //     },
  //     {
  //       title: <span style={{ whiteSpace: 'nowrap' }}>IP位置</span>,
  //       dataIndex: 'isIntranet',
  //       width: 120,
  //       filters: NetworklocationTypeFilters,
  //       filteredValue: selectedfiltersVal || null,
  //       render: (_, record) => {
  //         if (record.ip_locality_responder === 0 || record.ip_locality_initiator === 0) {
  //           return '内网';
  //         }
  //         return '外网';
  //       },
  //     },
  //     {
  //       title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
  //       dataIndex: 'tcpEstablishedCounts',
  //       width: 100,
  //       sorter: true,
  //       sortOrder: (sortProperty === 'tcpEstablishedCounts' ? `${sortDirection}end` : false) as any,
  //       render: (_, record) => {
  //         return (
  //           <>
  //             {record.tcpEstablishedCounts}/
  //             <span style={{ color: 'red' }}>{record.tcpEstablishedFailCounts}</span>
  //           </>
  //         );
  //       },
  //     },
  //     {
  //       title: <span style={{ whiteSpace: 'nowrap' }}>流量</span>,
  //       dataIndex: 'totalBytes',
  //       width: 120,
  //       sorter: true,
  //       defaultSortOrder: 'descend',
  //       sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
  //       render: (_, record) => {
  //         return bytesToSize(record.totalBytes);
  //       },
  //     },
  //   ];
  // }, [IpAddress, category, networkId, selectedfiltersVal, sortDirection, sortProperty]);

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

  const queryDsl = useMemo(() => {
    let dsl = `| gentimes report_time start="${selectedTime.startTime}" end="${selectedTime.endTime}"`;
    if (networkId) {
      dsl = `(network_id<Array>=${networkId}) ` + dsl;
    }
    return dsl;
  }, [networkId, selectedTime.endTime, selectedTime.startTime]);

  const queryParams = useMemo(() => {
    return {
      sourceType: 'network',
      packetFileId: null,
      networkId,
      ...selectedTime,
      ...queryType,
      queryProperty: category,
      dsl: queryDsl,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [networkId, selectedTime, queryType, category, queryDsl]);

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

  // const queryTotalNumber = useCallback(async () => {
  //   setTotalLoading(true);
  //   const { success, result } = await queryHistogramDataTotalNumer(queryParams);
  //   setTotalLoading(false);
  //   if (success) {
  //     const { total } = result;
  //     setTotalNumber(total ?? 0);
  //   }
  // }, [queryParams]);

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
    console.log(pagination, 'pageAndPage');
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
    setSortDirection(newSortDirection);
    setSortProperty(newSortProperty);
    const newNetworkFilters = filters.isIntranet;
    if (!newNetworkFilters || (newNetworkFilters && newNetworkFilters.length === 2)) {
      console.log(newNetworkFilters);
      setIsIntranet(NetworkLocationType.ALL);
    }
    if (newNetworkFilters && newNetworkFilters.length === 1) {
      setIsIntranet(newNetworkFilters[0]);
    }
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
          networkId={networkId}
          IpAddress={IpAddress}
          selectedTime={selectedTime}
          querydata={queryHistogramData}
          queryTotalNumber={queryHistogramDataTotalNumer}
        />
      );
    };
  }, [IpAddress, category, networkId, selectedTime]);

  const extraBar = (
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
  );

  return (
    <>
      <FullScreenCard title={categoryMap[category]} extra={extraBar} loading={isLoading}>
        <HightContext.Consumer>
          {(isFullscreen) => {
            return (
              <div
                style={{
                  height: isFullscreen ? 'calc(100vh - 40px)' : '440px',
                  overflow: 'hidden',
                }}
              >
                {/* <Card
                  size="small"
                  title={categoryMap[category]}
                  style={{ height: height, marginBottom: 15 }}
                  bodyStyle={{ height: 'calc(100% - 41px)', padding: 5 }}
                  extra={
                    <ExtraBar
                      hasTopSelectionBar={true}
                      top={top}
                      changeTop={setTop}
                      hasTypeChangeBar={true}
                      types={[DataShowedType.TABLE, DataShowedType.PIECHART]}
                      showedType={showedType}
                      changeShowedType={setShowedType}
                    />
                  }
                  loading={isLoading}
                > */}
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
                {showedType == DataShowedType.BARLINECHART && (
                  <BarLineChart
                    configDataOne={LineBarChart.connectionNumber}
                    configDataTwo={LineBarChart.connectionFailedNumber}
                    configDataThree={LineBarChart.flowCount}
                    configName={LineBarChart.xAxisDataName}
                    topIndexName={LineBarChart.titleName}
                  />
                )}
                {/* </Card> */}
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
        // zIndex={10}
        maskClosable={false}
      >
        {getMoreArea()()}
      </Modal>
    </>
  );
};

export default VisitingIp;
