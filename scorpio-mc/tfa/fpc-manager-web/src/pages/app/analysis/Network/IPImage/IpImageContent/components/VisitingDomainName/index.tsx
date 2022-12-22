import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { DomainNameTableTitle } from '../../../typings';
import { DataShowedType } from '../../../typings';
import { categoryMap } from '../../../typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ESortDirection } from '@/pages/app/Home/typings';
import { queryIpDominName, queryIpDominNameTotalNumber } from '../../../service';
import type { ColumnsType } from 'antd/lib/table';
import SmallTable from '../SmallTable';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { SearchIpImageContext } from '../../..';
import FullScreenCard, {
  HightContext,
} from '@/pages/app/analysis/Network/IPImage/IpImageContent/components/ShowFullCard';
import useDomainNameTable from '../hooks/useDomainNameTable';
import MoreInformation from '../MoreInformation';
import { Modal } from 'antd';
import { SingleWindowWidth } from '../..';

export interface IVisitingDomainNameProps {
  title: string;
  height?: number;
  // IpAddress: string;
  category: string;
  // networkId: string | null;
  // globalSelectTime: IGlobalTime;
}

const VisitingDomainName: React.FC<IVisitingDomainNameProps> = ({ category }) => {
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

  const networkId = useMemo(() => {
    if (networkIds === 'ALL') {
      return undefined;
    }
    return networkIds;
  }, [networkIds]);
  const [windowWidth] = useContext(SingleWindowWidth);

  const Tablecolumns: ColumnsType<DomainNameTableTitle> = useDomainNameTable({
    category,
    IpAddress,
    networkId,
    sortDirection,
    sortProperty,
    widowWidth: windowWidth * 0.6,
  });
  // const Tablecolumns: ColumnsType<DomainNameTableTitle> = [
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
  //     dataIndex: 'domain',
  //     // width: 300,
  //     render: (_, record) => {
  //       return <EllipsisCom style={{}}>{record.domain}</EllipsisCom>;
  //     },
  //   },
  //   {
  //     title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
  //     dataIndex: 'totalCounts',
  //     width: 100,
  //     sorter: true,
  //     sortOrder: (sortProperty === 'totalCounts' ? `${sortDirection}end` : false) as any,
  //     render: (_, record) => {
  //       return `${record.totalCounts}/${record.failCounts}`;
  //     },
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
      networkId: networkId,
      ...selectedTime,
      ...queryType,
      dsl: queryDsl,
    };
  }, [networkId, selectedTime, queryType, queryDsl]);

  const basicQueryParams = useMemo(() => {
    return { sortProperty: sortProperty, sortDirection: sortDirection, page, pageSize };
  }, [page, pageSize, sortDirection, sortProperty]);

  const recallNewTotalData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await queryIpDominName({ ...queryParams, ...basicQueryParams });
    if (success) {
      setTotalData(result.content || []);
      setIsLoading(false);
    }
  }, [basicQueryParams, queryParams]);

  // const queryTotalNumber = useCallback(async () => {
  //   const { success, result } = await queryIpDominNameTotalNumber(queryParams);
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
          querydata={queryIpDominName}
          queryTotalNumber={queryIpDominNameTotalNumber}
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
        title={categoryMap[category]}
        style={{ height: height }}
        loading={isLoading}
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

export default VisitingDomainName;
