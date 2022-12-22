import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { AlarmTableTitle, categoryMap } from '../../../typings';
import type { ColumnsType } from 'antd/lib/table';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { queryIpAlarmData, queryIpAlarmDataTotalNumber } from '../../../service';
import SmallTable from '../SmallTable';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { SearchIpImageContext } from '../../..';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/analysis/Network/IPImage/IpImageContent/components/ShowFullCard';
import useAlarmTable from '../hooks/useAlarmTable';
import MoreInformation from '../MoreInformation';
import { Modal } from 'antd';
import { SingleWindowWidth } from '../..';

export interface IAlarmProps {
  title: string;
  // IpAddress: string;
  height?: number;
  category: string;
  // networkId: string | null;
  // globalSelectTime: Required<IGlobalTime>;
}

const Alarm: React.FC<IAlarmProps> = ({
  title,
  category,
  // IpAddress,
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

  const [totalData, setTotalData] = useState<any>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [sortProperty, setSortProperty] = useState<string>('counts');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalNumber, setTotalNumber] = useState(0);
  const networkId = useMemo(() => {
    if (networkIds === 'ALL') {
      return undefined;
    }
    return networkIds;
  }, [networkIds]);

  const [windowWidth] = useContext(SingleWindowWidth);

  const Tablecolumns: ColumnsType<AlarmTableTitle> = useAlarmTable({
    category,
    IpAddress,
    networkId,
    sortProperty,
    sortDirection,
    widowWidth: windowWidth * 0.3,
  });
  // const Tablecolumns: ColumnsType<AlarmTableTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>名称</span>,
  //     dataIndex: 'msg',
  //     width: 200,
  //     render: (_, record) => {
  //       return (
  //         <FilterBubble
  //           dataIndex={record.msg}
  //           label={<EllipsisCom style={{ width: 200 }}>{record.msg}</EllipsisCom>}
  //           hasFilter={false}
  //           DrilldownMenu={
  //             <LinkMenu
  //               MenuItemsGroup={[
  //                 {
  //                   label: '跳转到其他页',
  //                   key: 'jumpToOtherPage',
  //                   children: [{ label: '安全告警', key: EIP_DRILLDOWN_MENU_KEY.SECURITY_ALARM }],
  //                 },
  //               ]}
  //               settings={{
  //                 alarmMessage: record.msg,
  //               }}
  //             />
  //           }
  //         />
  //       );
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>最近发生时间</span>,
  //     dataIndex: 'timestamp',
  //     width: 150,
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>严重级别</span>,
  //     dataIndex: 'signatureSeverity',
  //     width: 80,
  //     sorter: true,
  //     sortOrder: (sortProperty === 'signatureSeverity' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return SeverityLevelMap[record.signatureSeverity];
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>发生次数</span>,
  //     width: 80,
  //     dataIndex: 'counts',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'counts' ? `${sortDirection}end` : false) as any,
  //   },
  // ];

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
      networkId: networkId,
      ...selectedTime,
      srcIp: IpAddress,
      destIp: IpAddress,
      dsl: queryDsl,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [IpAddress, networkId, queryDsl, selectedTime]);

  const basicQueryParams = useMemo(() => {
    return { sortProperty: sortProperty, sortDirection: sortDirection, page, pageSize };
  }, [page, pageSize, sortDirection, sortProperty]);

  const recallNewTotalData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryIpAlarmData({ ...queryParams, ...basicQueryParams });
    setIsLoading(false);
    if (success) {
      setTotalData(result.content || []);
    }
  }, [basicQueryParams, queryParams]);

  // const queryTotalNumber = useCallback(async () => {
  //   const { success, result } = await queryIpAlarmDataTotalNumber(queryParams);
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
          networkId={networkId}
          IpAddress={IpAddress}
          selectedTime={selectedTime}
          querydata={queryIpAlarmData}
          queryTotalNumber={queryIpAlarmDataTotalNumber}
        />
      );
    };
  }, [IpAddress, category, networkId, selectedTime]);

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
        // zIndex={10}
      >
        {getMoreArea()()}
      </Modal>
      <FullScreenCard
        title={title}
        loading={isLoading}
        extra={
          <span className="link" onClick={moreInformation}>
            更多
          </span>
        }
      >
        <HightContext.Consumer>
          {(isFullscreen) => {
            return (
              <div
                style={{
                  height: isFullscreen ? 'calc(100vh - 80px)' : '440px',
                  overflow: 'hidden',
                }}
              >
                <SmallTable
                  columns={Tablecolumns}
                  dataSource={totalData}
                  onChange={handleTableColSortChange}
                  page={page + 1}
                  pageSize={pageSize}
                  totalNumber={totalNumber}
                />
              </div>
            );
          }}
        </HightContext.Consumer>
      </FullScreenCard>
      {/* <Card
        size="small"
        title={title}
        style={{ height: height, marginBottom: 15 }}
        extra={
          <ExtraBar
            hasTopSelectionBar={false}
            // top={top}
            // changeTop={setTop}
            hasTypeChangeBar={true}
            types={[DataShowedType.TABLE]}
            showedType={DataShowedType.TABLE}
          />
        }
        loading={isLoading}
        bodyStyle={{ height: 'calc(100% - 41px)', padding: 5 }}
      >
        <SmallTable
          columns={Tablecolumns}
          dataSource={totalData}
          onChange={handleTableColSortChange}
        />
      </Card> */}
    </>
  );
};

export default Alarm;
