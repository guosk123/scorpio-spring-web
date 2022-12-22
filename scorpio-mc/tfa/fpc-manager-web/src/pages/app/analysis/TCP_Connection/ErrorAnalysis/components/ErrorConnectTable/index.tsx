import AutoHeightContainer from '@/components/AutoHeightContainer';
import EnhancedTable from '@/components/EnhancedTable';
import { queryEstablishedFail } from '@/pages/app/analysis/service';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { EServiceType, ESourceType } from '@/pages/app/analysis/typings';
import { ESortDirection } from '@/pages/app/Home/typings';
import { snakeCase } from '@/utils/frame/utils';
import { timeFormatter } from '@/utils/utils';
import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import type { ENumericalValue } from '../..';
import { tableColumns } from './constant';

interface Props {
  selectedTimeInfo: any;
  serviceType: EServiceType;
  compareProperty: ENumericalValue;
  defSortProperty?: string;
  changeSortKey?: any;
  dsl: string;
}

function ErrorConnectTable(props: Props) {
  const { selectedTimeInfo, serviceType, changeSortKey, dsl } = props;
  const [tableWrapHeight, setTableWrapperHeight] = useState(200);
  const [sortProperty, setSortProperty] = useState<string>('tcpEstablishedFailCounts');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const [queryLoading, setQueryLoading] = useState(false);
  const [tablePage, setTablePage] = useState({ page: 1, pageSize: 20 });
  const columns = tableColumns(serviceType, tablePage).map((item) => {
    // sortOrder: (sortProperty === field ? `${sortDirection}end` : false) as any,
    return {
      ...item,
      sortOrder: (sortProperty === item.dataIndex ? `${sortDirection}end` : false) as any,
    };
  });

  useEffect(() => {
    changeSortKey((serviceKey: EServiceType) => {
      if (serviceKey === EServiceType.INTRANETSERVICE) {
        setSortProperty('tcpEstablishedFailCountsInsideService');
      } else if (serviceKey === EServiceType.INTERNETSERVICE) {
        setSortProperty('tcpEstablishedFailCountsOutsideService');
      } else {
        setSortProperty('tcpEstablishedFailCounts');
      }
    });
  }, [changeSortKey, serviceType]);

  const payload = useMemo(() => {
    let sourceType = ESourceType.NETWORK;
    if (serviceId) {
      sourceType = ESourceType.SERVICE;
    } else if (pcapFileId) {
      sourceType = ESourceType.OFFLINE;
    }
    return {
      sourceType,
      [networkId ? 'networkId' : 'packetFileId']: networkId || pcapFileId,
      serviceId: serviceId,
      startTime: selectedTimeInfo.startTime,
      endTime: selectedTimeInfo.endTime,
      interval: timeFormatter(selectedTimeInfo.startTime, selectedTimeInfo.endTime).interval,
      sortProperty: snakeCase(sortProperty || ''),
      sortDirection,
      serviceType,
      count: 300,
      dsl,
      drilldown: 0,
      queryId: uuidv1(),
    };
  }, [
    dsl,
    networkId,
    pcapFileId,
    selectedTimeInfo.endTime,
    selectedTimeInfo.startTime,
    serviceId,
    serviceType,
    sortDirection,
    sortProperty,
  ]);
  const [dataSource, setDataSource] = useState([]);
  useEffect(() => {
    setQueryLoading(true);
    queryEstablishedFail(payload).then((res) => {
      const { success, result } = res;
      setQueryLoading(false);
      if (success) {
        setDataSource(result);
      }
    });
  }, [payload]);
  const handleTableChange = (pagination: any, filters: any, sorter: any) => {
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

  return (
    <AutoHeightContainer onHeightChange={(h) => setTableWrapperHeight(h)} fixHeight={5}>
      <EnhancedTable
        tableKey="error_connect_table"
        columns={columns as any}
        rowKey={() => uuidv1()}
        loading={queryLoading}
        dataSource={dataSource}
        size="small"
        bordered={true}
        onChange={handleTableChange}
        style={{ marginTop: '4px' }}
        sortProperty={sortProperty}
        sortDirection={`${sortDirection}end`}
        scroll={{ x: 3000, y: tableWrapHeight - 120 }}
        pagination={{
          total: dataSource.length || 0,
          onChange: (page, pageSize) => {
            setTablePage({ page, pageSize });
          },
        }}
      />
    </AutoHeightContainer>
  );
}

export default ErrorConnectTable;
