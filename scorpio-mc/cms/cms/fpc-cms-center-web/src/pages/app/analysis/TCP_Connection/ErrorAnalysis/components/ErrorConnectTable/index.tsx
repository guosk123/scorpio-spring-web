import AutoHeightContainer from '@/components/AutoHeightContainer';
import { queryEstablishedFail } from '@/pages/app/analysis/service';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { EServiceType } from '@/pages/app/analysis/typings';
import { ESortDirection } from '@/pages/app/Home/typings';
import { timeFormatter } from '@/utils/utils';
import { useEffect, useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { tableColumns } from './constant';
import { snakeCase } from '@/utils/frame/utils';
import type { ENumericalValue } from '../..';
import EnhancedTable from '@/components/EnhancedTable';

interface Props {
  selectedTimeInfo: any;
  serviceType: EServiceType;
  compareProperty: ENumericalValue;
  defSortProperty?: string;
  changeSortKey?: any;
  dsl: string;
}

function ErrorConnectTable(props: Props) {
  const { selectedTimeInfo, serviceType, compareProperty, defSortProperty, changeSortKey, dsl } =
    props;
  const [tableWrapHeight, setTableWrapperHeight] = useState(200);
  const [sortProperty, setSortProperty] = useState<string>('tcpEstablishedFailCounts');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const { networkId, serviceId }: IUriParams = useParams();
  const [queryLoading, setQueryLoading] = useState(false);
  const columns = tableColumns(serviceType).map((item) => {
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
    return {
      sourceType: serviceId ? 'service' : 'network',
      networkId: networkId,
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
        rowKey={(record: any) => record.index}
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
        }}
      />
    </AutoHeightContainer>
  );
}

export default connect(() => {})(ErrorConnectTable);
