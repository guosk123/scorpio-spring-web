import { useCallback, useEffect, useMemo, useState } from 'react';
import { ESortDirection } from '@/pages/app/Home/typings';
import { IShowCategory, NetworkLocationType } from '../../../typings';
import useIpDataTable from '../hooks/useIpdataTable';
import MoreDataTable from '../MoreDataTable';
import usePortdataTable from '../hooks/usePortdataTable';
import useDomainNameTable from '../hooks/useDomainNameTable';
import useAlarmTable from '../hooks/useAlarmTable';

interface IMoreInformationProps {
  category: string;
  networkId: string;
  IpAddress: string;
  selectedTime: { startTime: string; endTime: string };
  querydata: (params: any) => Promise<any>;
  queryTotalNumber: (params: any) => Promise<any>;
}

export default function MoreInformation(props: IMoreInformationProps) {
  const { category, networkId, IpAddress, selectedTime, querydata, queryTotalNumber } = props;
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [sortProperty, setSortProperty] = useState<string>(() => {
    if (
      category === IShowCategory.VISITEDIP ||
      category === IShowCategory.VISITINGIP ||
      category === IShowCategory.SHARINGPORT ||
      category === IShowCategory.VISITINGPORT
    ) {
      return 'totalBytes';
    }
    if (
      category === IShowCategory.VISITINGDOMAINNAME ||
      category === IShowCategory.SHARINGDOMAINNAME
    ) {
      return 'totalCounts';
    }
    if (category === IShowCategory.SECURITYALERTS) {
      return 'counts';
    }
    return '';
  });
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [totalData, setTotalData] = useState<any>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [totalLoading, setTotalLoading] = useState(false);
  const [totalNumber, setTotalNumber] = useState(0);
  //来访IP和访问IP判断是否是内网
  const [isIntranet, setIsIntranet] = useState<string>(NetworkLocationType.ALL);
  const selectedfiltersVal = useMemo(() => {
    if (isIntranet === NetworkLocationType.ALL) {
      return [NetworkLocationType.EXTRANET, NetworkLocationType.INTRANET];
    }
    return [isIntranet];
  }, [isIntranet]);

  const IpDataColumns = useIpDataTable({
    category,
    IpAddress,
    networkId,
    filterValue: selectedfiltersVal,
    sortProperty,
    sortDirection,
  });

  const PortDataColumns = usePortdataTable({
    category,
    IpAddress,
    networkId,
    sortProperty,
    sortDirection,
  });

  const DomainNameDataColums = useDomainNameTable({
    category,
    IpAddress,
    networkId,
    sortProperty,
    sortDirection,
  });

  const AlarmDataColumns = useAlarmTable({
    category,
    IpAddress,
    networkId,
    sortProperty,
    sortDirection,
  });
  const columns = useMemo(() => {
    if (category === IShowCategory.VISITINGIP || category === IShowCategory.VISITEDIP) {
      return IpDataColumns;
    }
    if (category === IShowCategory.SHARINGPORT || category === IShowCategory.VISITINGPORT) {
      return PortDataColumns;
    }
    if (
      category === IShowCategory.VISITINGDOMAINNAME ||
      category === IShowCategory.SHARINGDOMAINNAME
    ) {
      return DomainNameDataColums;
    }
    if(category === IShowCategory.SECURITYALERTS){
      return AlarmDataColumns;
    }
    return [];
  }, [AlarmDataColumns, DomainNameDataColums, IpDataColumns, PortDataColumns, category]);

  //查询DSL字符串
  const queryDsl = useMemo(() => {
    let dsl = `| gentimes report_time start="${selectedTime.startTime}" end="${selectedTime.endTime}"`;
    if (networkId) {
      dsl = `(network_id<Array>=${networkId}) ` + dsl;
    }
    return dsl;
  }, [networkId, selectedTime.endTime, selectedTime.startTime]);

  const queryProperty = useMemo(() => {
    if (
      category === IShowCategory.VISITEDIP ||
      category === IShowCategory.VISITINGIP ||
      category === IShowCategory.VISITINGPORT ||
      category === IShowCategory.SHARINGPORT
    ) {
      return { queryProperty: category };
    }
    return {};
  }, [category]);

  const queryTypeParams = useMemo(() => {
    const type = category.split('_')[1];
    let typeParams = {};
    if (type === 'responder') {
      typeParams = { ipInitiator: IpAddress };
    }
    if (type === 'initiator') {
      typeParams = { ipResponder: IpAddress };
    }
    if (type === 'dest') {
      typeParams = { destIp: IpAddress };
    }
    if (type === 'src') {
      typeParams = { srcIp: IpAddress };
    }
    if (type === 'alert') {
      typeParams = { srcIp: IpAddress, destIp: IpAddress };
    }
    return typeParams;
  }, [IpAddress, category]);

  const getLocalityTypeParams = useCallback(() => {
    switch (isIntranet) {
      case NetworkLocationType.ALL:
        return undefined;
      case NetworkLocationType.EXTRANET:
        return NetworkLocationType.EXTRANET;
      case NetworkLocationType.INTRANET:
        return NetworkLocationType.INTRANET;
      default:
        return '';
    }
  }, [isIntranet]);

  const queryIpLocalityTypeParams = useMemo(() => {
    let localityTypeParams = {};
    //是来访IP和访问IP才有的情况
    if (category === IShowCategory.VISITEDIP || category === IShowCategory.VISITINGIP) {
      const type = category.split('_')[1];
      const value = getLocalityTypeParams();
      if (type === 'responder') {
        localityTypeParams = { ipLocalityResponder: value };
      }
      if (type === 'initiator') {
        localityTypeParams = { ipLocalityInitiator: value };
      }
      return localityTypeParams;
    }
    return {};
  }, [category, getLocalityTypeParams]);

  const queryParams = useMemo(() => {
    return {
      sourceType: 'network',
      packetFileId: null,
      networkId,
      ...selectedTime,
      ...queryTypeParams,
      ...queryIpLocalityTypeParams,
      ...queryProperty,
      dsl: queryDsl,
      // drilldown: BOOL_NO,
      // queryId: uuidv1(),
    };
  }, [
    networkId,
    selectedTime,
    queryTypeParams,
    queryIpLocalityTypeParams,
    queryProperty,
    queryDsl,
  ]);

  const basicQueryParams = useMemo(() => {
    return { sortProperty, sortDirection, page: page - 1, pageSize };
  }, [page, pageSize, sortDirection, sortProperty]);

  const recallNewTotalData = useCallback(async () => {
    setIsLoading(true);
    const { success, result } = await querydata({ ...queryParams, ...basicQueryParams });
    setIsLoading(false);
    if (success) {
      setTotalData(result.content || []);
    }
  }, [basicQueryParams, queryParams, querydata]);

  const recallTotalNumber = useCallback(async () => {
    setTotalLoading(true);
    const { success, result } = await queryTotalNumber(queryParams);
    setTotalLoading(false);
    if (success) {
      const { total } = result;
      setTotalNumber(total ?? 0);
    }
  }, [queryParams, queryTotalNumber]);

  useEffect(() => {
    recallNewTotalData();
  }, [recallNewTotalData]);

  useEffect(() => {
    recallTotalNumber();
  }, [recallTotalNumber]);

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
    const newNetworkFilters = filters.isIntranet;
    if (!newNetworkFilters || (newNetworkFilters && newNetworkFilters.length === 2)) {
      console.log(newNetworkFilters);
      setIsIntranet(NetworkLocationType.ALL);
    }
    if (newNetworkFilters && newNetworkFilters.length === 1) {
      setIsIntranet(newNetworkFilters[0]);
    }
  };

  return (
    <MoreDataTable
      isLoading={isLoading && totalLoading}
      columns={columns}
      dataSource={totalData}
      onChange={handleTableColSortChange}
      page={page}
      pageSize={pageSize}
      totalNumber={totalNumber}
    />
  );
}
