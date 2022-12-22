import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
// import type { ConnectState } from '@/models/connect';
// import { IAlertMessage } from '@/pages/app/configuration/Alerts/typings';
import { AlarmTableTitle, categoryMap } from '../../../typings';
import { SeverityLevelMap } from '../../../typings';
import type { ColumnsType } from 'antd/lib/table';
import { Modal, Tooltip } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { queryIpAlarmData, queryIpAlarmDataTotalNumber } from '../../../service';
import { tableTop } from '../../../typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { getLinkUrl, jumpNewPage } from '@/utils/utils';
import moment from 'moment';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { SearchIpImageContext } from '../../..';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/FlowTrace/IPImage/IpImageContent/components/ShowFullCard';
import SmallTable from '../SmallTable';
import useAlarmDataTable from '../hooks/useAlarmDataTable';
import { SingleWindowWidth } from '../..';
import MoreInformation from '../MoreInformation';

export interface IAlarmProps {
  title: string;
  category: string;
  // IpAddress: string;
  height?: number;
  // networkId: string;
  // globalSelectTime: Required<IGlobalTime>;
}

const Alarm: React.FC<IAlarmProps> = ({
  title,
  category,
  // IpAddress,
  height = 440,
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
  const [windowWidth] = useContext(SingleWindowWidth);
  const Tablecolumns: ColumnsType<AlarmTableTitle> = useAlarmDataTable({
    category,
    IpAddress,
    networkId: networkIds.split('^')[0],
    sortProperty,
    sortDirection,
    windowWidth: windowWidth * 0.3,
  });
  // const Tablecolumns: ColumnsType<AlarmTableTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>名称</span>,
  //     dataIndex: 'msg',
  //     align: 'center',
  //     render: (_, record) => {
  //       const flowRecordFilter = {
  //         field: 'msg',
  //         operator: EFilterOperatorTypes.EQ,
  //         operand: record.msg,
  //       };
  //       return (
  //         <span
  //           className="link"
  //           onClick={() => {
  //             const url = getLinkUrl(
  //               `/analysis/security/alert?from=${moment(
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
  //           <Tooltip title={record.msg}>{record.msg}</Tooltip>
  //         </span>
  //       );
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>最近发生时间</span>,
  //     dataIndex: 'timestamp',
  //     width: 150,
  //     align: 'center',
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>严重级别</span>,
  //     dataIndex: 'signatureSeverity',
  //     width: 80,
  //     align: 'center',
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
  //     align: 'center',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'counts' ? `${sortDirection}end` : false) as any,
  //   },
  // ];

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
      ...selectedTime,
      srcIp: IpAddress,
      destIp: IpAddress,
      ...networkParams,
      sortProperty: 'counts',
      sortDirection: ESortDirection.DESC,
      dsl: `| gentimes timestamp start="${globalSelectTime.startTime}" end="${globalSelectTime.endTime}"`,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [
    IpAddress,
    globalSelectTime.endTime,
    globalSelectTime.startTime,
    networkParams,
    selectedTime,
  ]);

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
  }, [queryParams, basicQueryParams]);

  const queryTotalNumber = useCallback(async () => {
    const { success, result } = await queryIpAlarmDataTotalNumber(queryParams);
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
          querydata={queryIpAlarmData}
          queryTotalNumber={queryIpAlarmDataTotalNumber}
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
                  height: isFullscreen ? 'calc(100vh - 80px)' : height,
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
    </>
  );
};

export default Alarm;
