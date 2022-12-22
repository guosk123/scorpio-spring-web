import { Modal, Tooltip } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
// import type { ActionType, ProColumns } from '@ant-design/pro-table';
// import { getTablePaginationDefaultSettings } from '@/common/app';
// import ProTable from '@ant-design/pro-table';
import type { DomainNameTableTitle } from '../../../typings';
import { categoryMap } from '../../../typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ESortDirection } from '@/pages/app/Home/typings';
import { queryIpDominName, queryIpDominNameTotalNumber } from '../../../service';
import type { ColumnsType } from 'antd/lib/table';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { SearchIpImageContext } from '../../..';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/FlowTrace/IPImage/IpImageContent/components/ShowFullCard';
import SmallTable from '../SmallTable';
import useDomainNameDataTable from '../hooks/useDomainNameDataTable';
import { SingleWindowWidth } from '../..';
import MoreInformation from '../MoreInformation';

export interface IVisitingDomainNameProps {
  title: string;
  height?: number;
  // IpAddress: string;
  category: string;
  // networkId: string;
  // globalSelectTime: IGlobalTime;
}

const VisitingDomainName: React.FC<IVisitingDomainNameProps> = ({
  // title,
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
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalNumber, setTotalNumber] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [totalData, setTotalData] = useState<any>([]);
  const [sortProperty, setSortProperty] = useState<string>('totalCounts');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [windowWidth] = useContext(SingleWindowWidth);
  const Tablecolumns: ColumnsType<DomainNameTableTitle> = useDomainNameDataTable({
    category,
    IpAddress,
    networkId: networkIds.split('^')[0],
    sortDirection,
    sortProperty,
    windowWidth: windowWidth * 0.6,
  });
  // const Tablecolumns: ColumnsType<DomainNameTableTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
  //     dataIndex: 'domain',
  //     align: 'center',
  //     ellipsis: true,
  //     render: (_, record) => {
  //       return <Tooltip title={record.domain}>{record.domain}</Tooltip>;
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>请求总数</span>,
  //     dataIndex: 'totalCounts',
  //     align: 'center',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'totalCounts' ? `${sortDirection}end` : false) as any,
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>请求失败</span>,
  //     dataIndex: 'failCounts',
  //     align: 'center',
  //     sorter: true,
  //     sortOrder: (sortProperty === 'failCounts' ? `${sortDirection}end` : false) as any,
  //   },
  // ];

  const queryType = useMemo(() => {
    const type = category.split('_')[1];
    let typeParams = {};
    if (type === 'dest') {
      typeParams = { destIp: IpAddress };
    }
    if (type === 'src') {
      typeParams = { srcIp: IpAddress };
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
      dsl: `| gentimes start_time start="${globalSelectTime.originStartTime}" end="${globalSelectTime.originEndTime}"`,
    };
  }, [
    selectedTime,
    networkParams,
    queryType,
    globalSelectTime.originStartTime,
    globalSelectTime.originEndTime,
  ]);

  const basicQueryParams = useMemo(() => {
    return { sortProperty: sortProperty, sortDirection: sortDirection, page, pageSize };
  }, [page, pageSize, sortDirection, sortProperty]);

  const recallNewTotalData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryIpDominName({ ...queryParams, ...basicQueryParams });
    setIsLoading(false);
    if (success) {
      setTotalData(result.content || []);
    }
  }, [basicQueryParams, queryParams]);

  const queryTotalNumber = useCallback(async () => {
    const { success, result } = await queryIpDominNameTotalNumber(queryParams);
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
          querydata={queryIpDominName}
          queryTotalNumber={queryIpDominNameTotalNumber}
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

export default VisitingDomainName;
