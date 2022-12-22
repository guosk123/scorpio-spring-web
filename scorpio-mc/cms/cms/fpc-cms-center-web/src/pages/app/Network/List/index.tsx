import EnhancedTable from '@/components/EnhancedTable';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { snakeCase } from '@/utils/utils';
import { Button, message, Result } from 'antd';
import type { ColumnProps, TablePaginationConfig } from 'antd/lib/table';
import { connect } from 'dva';
import { useContext, useEffect, useMemo, useState } from 'react';
import type { INetworkStatData } from '../../analysis/typings';
import { ESortDirection } from '../../analysis/typings';
import { history } from 'umi';
import { INetworkType } from '../typing';
import { queryAllNetworkStat } from '../service';
import { queryNetworkGroups, queryNetworkSensors } from '../../Configuration/Network/service';
import { queryAllLogicalSubnets } from '../../Configuration/LogicalSubnet/service';
import { getColumns } from './constant';
import { getTablePaginationDefaultSettings } from '@/common/app';
import { classifyDataSet } from '../utils/index';
import { DimensionsSearchContext } from '../../GlobalSearch/DimensionsSearch/SeartchTabs';
import { LayoutContext } from '../NetworkLayout';
import { getTabDetail } from '../components/EditTabs';
import InDimensionsPayload from './InDimensionsPayload';
import {
  DimensionsTypeToFlowFilterMap,
  EDimensionsSearchType,
} from '../../GlobalSearch/DimensionsSearch/typing';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { dimensionsUrl } from '../../GlobalSearch/DimensionsSearch/SeartchTabs/constant';
// import NetworkSearchBox from '../components/NetworkSearchBox';

interface Props {
  globalSelectedTime: Required<IGlobalTime>;
  queryId?: string;
  onLoading?: any;
  noDisplayColKeys?: string[];
}

function List(props: Props) {
  const {
    globalSelectedTime,
    queryId,
    onLoading = (args: string) => {},
    noDisplayColKeys = [],
  } = props;
  // const [allNetworkStatData, setAllNetworkStatData] = useState<INetworkStatData[]>();
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [queryLoading, setQueryLoading] = useState(false);
  // 当前排序的方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  const [networkTree, setNetworkTree] = useState<any>();
  const [tmpNetworkTree, setTmpNetworkTree] = useState<any>();
  const [, setDslPayload] = useState();

  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);
  const columns = useMemo(() => {
    return getColumns(globalSelectedTime.startTime, globalSelectedTime.endTime, isDimensionsTab);
  }, [globalSelectedTime.startTime, globalSelectedTime.endTime, isDimensionsTab]);

  const [flowState] = useContext(isDimensionsTab ? DimensionsSearchContext : LayoutContext);
  const flowAnalysisDetail = isDimensionsTab ? getTabDetail(flowState) : {};

  const dimensionsNetworkPayload = (() => {
    const tmpNetworkIds =
      flowAnalysisDetail?.searchBoxInfo?.networkIds
        .filter((item: string) => !item.includes('networkGroup'))
        .map((sub: string) => sub.replace('^network', '')) || [];
    const tmpNetworkGroupIds =
      flowAnalysisDetail?.searchBoxInfo?.networkIds
        .filter((item: string) => item.includes('networkGroup'))
        .map((sub: string) => sub.replace('^networkGroup', '')) || [];
    const tmpIds = {
      networkId: tmpNetworkIds.length ? tmpNetworkIds.join(',') : undefined,
      networkGroupId: tmpNetworkGroupIds.length ? tmpNetworkGroupIds.join(',') : undefined,
    };
    return {
      sortProperty: snakeCase(sortProperty),
      sortDirection,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      ...tmpIds,
    };
  })();

  useEffect(() => {
    const networkListPayload = {
      sortProperty: snakeCase(sortProperty),
      sortDirection,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
    };
    let dsl = undefined;
    if (isDimensionsTab) {
      let filterSpl = `${
        DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
      } ${EFilterOperatorTypes.EQ} ${flowAnalysisDetail?.searchBoxInfo?.content}`;
      if (
        flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType ===
        EDimensionsSearchType.IPCONVERSATION
      ) {
        const ips = flowAnalysisDetail?.searchBoxInfo?.content.split('-');
        filterSpl = `${
          DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
        } ${EFilterOperatorTypes.EQ} ${ips[0]} and ${
          DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
        } ${EFilterOperatorTypes.EQ} ${ips[1]}`;
      } else if (
        flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType === EDimensionsSearchType.LOCATION
      ) {
        const locationField = ['country_id', 'province_id', 'city_id'];
        const locationOperandArr = flowAnalysisDetail?.searchBoxInfo?.content.split('_');
        filterSpl = `${locationField[locationOperandArr.length - 1]} ${
          EFilterOperatorTypes.EQ
        } ${locationOperandArr.pop()}`;
      }
      console.log('filterSpl', filterSpl, flowAnalysisDetail);
      dsl = `${filterSpl} | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`;
      // 多维检索未同步dsl
    }
    setQueryLoading(true);
    Promise.all([
      queryNetworkGroups(),
      queryNetworkSensors(),
      queryAllLogicalSubnets(),
      queryAllNetworkStat(
        isDimensionsTab
          ? { ...dimensionsNetworkPayload, dsl, queryId, drilldown: 1 }
          : networkListPayload,
      ),
    ]).then((res) => {
      const [groupsRes, sensorsRes, logicalRes, allNetworkStatDataRes] = res;
      const resList: any = [];
      if (
        groupsRes?.success &&
        sensorsRes?.success &&
        logicalRes?.success &&
        allNetworkStatDataRes?.success
      ) {
        setQueryLoading(false);
        onLoading('');
        allNetworkStatDataRes.result.forEach((ele: any) => {
          const groupItem = groupsRes.result.find((item) => ele.networkGroupId === item.id);
          const sensorItem = sensorsRes.result.find(
            (item) => ele.networkId === item.networkInSensorId && !ele.parentId,
          );
          const logicalItem = isDimensionsTab
            ? logicalRes.result.find((item) => ele.networkId === item.id)
            : logicalRes.result.find((item) => ele.networkId === item.id && ele.parentId);

          if (groupItem) {
            resList.push({
              ...ele,
              ...groupItem,
              networkName: groupItem.name,
              type: INetworkType.GROUP,
            });
          }
          if (sensorItem) {
            resList.push({
              ...ele,
              networkName: sensorItem.name,
              ...sensorItem,
              type: INetworkType.SENSORNETWORK,
            });
          }
          if (logicalItem) {
            resList.push({
              ...ele,
              networkName: `${logicalItem.name}(逻辑子网)`,
              ...logicalItem,
              type: INetworkType.LOGICALSUBNET,
            });
          }
        });
      } else {
        message.error('获取列表失败');
      }
      setNetworkTree(classifyDataSet(resList));
      setTmpNetworkTree(classifyDataSet(resList));
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [globalSelectedTime, sortDirection, sortProperty]);

  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
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

  const tableColumns = useMemo(() => {
    return columns
      .filter((filterCol) => {
        return !noDisplayColKeys.includes(String(filterCol.dataIndex));
      })
      .map((col) => ({
        ...col,
        key: col.dataIndex,
        sortOrder: sortProperty === col.dataIndex ? `${sortDirection}end` : false,
        align: 'center',
      })) as ColumnProps<INetworkStatData>[];
  }, [columns, sortProperty, sortDirection]);

  const isDimensionsPage = useMemo(() => {
    if (queryId) {
      return true;
    }
    return false;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div>
      {!tmpNetworkTree?.length && !queryLoading && !isDimensionsPage ? (
        <Result
          status="info"
          title="还没有配置网络"
          extra={
            <Button type="primary" onClick={() => history.push('/configuration/network/sensor')}>
              配置网络
            </Button>
          }
        />
      ) : (
        <EnhancedTable
          sortProperty={sortProperty}
          // @ts-ignore
          sortDirection={`${sortDirection}end`}
          tableKey="all-network-stat-table"
          rowKey="networkId"
          loading={queryLoading}
          columns={tableColumns}
          dataSource={
            isDimensionsTab
              ? networkTree?.filter((item: any) =>
                  flowAnalysisDetail?.searchBoxInfo?.networkIds
                    ?.map((ele: string) => ele.split('^')[0])
                    .includes(item.sensorId ? item.networkInSensorId : item.id),
                )
              : networkTree || []
          }
          onChange={handleTableChange}
          pagination={getTablePaginationDefaultSettings()}
          expandable={{ defaultExpandAllRows: true }}
          scroll={{ x: 'max-content' }}
        />
      )}
      {isDimensionsTab && (
        <InDimensionsPayload
          dimensionsNetworkPayload={dimensionsNetworkPayload}
          globalSelectedTime={globalSelectedTime}
          sortProperty={sortProperty}
          sortDirection={sortDirection}
          dslPayload={setDslPayload}
          flowAnalysisDetail={flowAnalysisDetail}
        />
      )}
    </div>
  );
}

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(List);
