import { categoryMap, DataShowedType } from '../../../typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { queryHistogramData, queryHistogramDataTotalNumer } from '../../../service';
import type { PortTabelTitle } from '../../../typings';
import { ESortDirection } from '@/pages/app/analysis/typings';
// import { BOOL_NO } from '@/common/dict';
// import { v1 as uuidv1 } from 'uuid';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import PieChart from '../PieChart';
// import BarLineChart from '../LineBarChart';
import { SearchIpImageContext } from '../../..';
import { useSelector } from 'umi';
import type { ConnectState } from '@/models/connect';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/FlowTrace/IPImage/IpImageContent/components/ShowFullCard';
import ExtraBar from '../ExtraBar';
import SmallTable from '../SmallTable';
import BarLineChart from '../LineBarChart';
import usePortdataTable from '../hooks/usePortdataTable';
import MoreInformation from '../MoreInformation';
import { Modal, Space } from 'antd';
export interface IVisitingPortProps {
  title: string;
  height?: number;
  // IpAddress: string;
  category: string;
  // networkId: string;
  // globalSelectTime: IGlobalTime;
}

const VisitingPort: React.FC<IVisitingPortProps> = ({
  height = 440,
  // IpAddress,
  category,
  // networkId,
  // globalSelectTime,
}) => {
  //利用context拿到搜索关键字，比如说IP地址和网络名称
  const [searchInfo] = useContext(SearchIpImageContext);
  const { IpAddress, networkIds } = searchInfo;
  //时间应该直接获取
  const globalSelectTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );
  const [showedType, setShowedType] = useState<string>(DataShowedType.TABLE);
  const [isLoading, setIsLoading] = useState(true);
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [totalData, setTotalData] = useState<any>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalNumber, setTotalNumber] = useState(0);

  const Tablecolumns: ColumnsType<PortTabelTitle> = usePortdataTable({
    category,
    IpAddress,
    networkId: networkIds.split('^')[0],
    sortProperty,
    sortDirection,
  });
  // const Tablecolumns: ColumnsType<PortTabelTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
  //     dataIndex: 'port',
  //     align: 'center',
  //     render: (_, record) => {
  //       return record.port_responder;
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
  //     dataIndex: 'tcpEstablishedCounts',
  //     align: 'center',
  //     width: 100,
  //     sorter: true,
  //     sortOrder: (sortProperty === 'tcpEstablishedCounts' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return `${record.tcpEstablishedCounts}/${record.tcpEstablishedFailCounts}`;
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>流量</span>,
  //     dataIndex: 'totalBytes',
  //     align: 'center',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return bytesToSize(record.totalBytes);
  //     },
  //   },
  // ];

  const queryType = useMemo(() => {
    const type = category.split('_')[1];
    let typeParams = {};
    if (type === 'responder') {
      typeParams = { ipInitiator: IpAddress };
    }
    if (type === 'initiator') {
      typeParams = { ipResponder: IpAddress };
    }
    return typeParams;
  }, [IpAddress, category]);

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

  const selectedTime = useMemo(() => {
    return { startTime: globalSelectTime.originStartTime, endTime: globalSelectTime.originEndTime };
  }, [globalSelectTime.originEndTime, globalSelectTime.originStartTime]);

  const queryParams = useMemo(() => {
    return {
      sourceType: 'network',
      packetFileId: null,
      ...selectedTime,
      ...networkParams,
      ...queryType,
      queryProperty: category,
      dsl: `| gentimes report_time start="${globalSelectTime.originStartTime}" end="${globalSelectTime.originEndTime}"`,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [
    selectedTime,
    networkParams,
    queryType,
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
    const { success, result } = await queryHistogramDataTotalNumer(queryParams);
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
      titleName.push(item.port_responder);
      if (item.tcpEstablishedCounts > 0) {
        connectionNumber.push({
          name: item.port_responder,
          value: item.tcpEstablishedCounts,
        });
      }
      if (item.totalBytes > 0) {
        flowCount.push({
          name: item.port_responder,
          value: item.totalBytes,
        });
      }
    });
    return {
      titleName: titleName.concat(titleName),
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
      xAxisDataName.push(item.port_responder);
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

  const handleTableColSortChange = (pagination: any, filters: any, sorter: any) => {
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
      <Modal
        title={`更多${categoryMap[category]}`}
        visible={showMore}
        destroyOnClose={true}
        width="auto"
        footer={null}
        onCancel={() => {
          setShowMore(false);
        }}
      >
        {getMoreArea()()}
      </Modal>
      <FullScreenCard
        title={categoryMap[category]}
        extra={
          <Space>
            <ExtraBar
              hasTopSelectionBar={false}
              // top={top}
              // changeTop={setTop}
              hasTypeChangeBar={false}
              // types={[DataShowedType.TABLE, DataShowedType.PIECHART]}
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
                  height: isFullscreen ? 'calc(100vh - 40px)' : height,
                  overflow: 'hidden',
                }}
              >
                {showedType == DataShowedType.TABLE && (
                  <SmallTable
                    columns={Tablecolumns}
                    dataSource={totalData}
                    totalNumber={totalNumber}
                    page={page + 1}
                    pageSize={pageSize}
                    onChange={handleTableColSortChange}
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
              </div>
            );
          }}
        </HightContext.Consumer>
      </FullScreenCard>
    </>
  );
};

export default VisitingPort;
