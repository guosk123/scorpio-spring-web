import EnhancedTable from '@/components/EnhancedTable';
import { ConnectState } from '@/models/connect';
import { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';
import { INetworkGroupMap, INetworkSensorMap } from '@/pages/app/Configuration/Network/typings';
// import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import { useSetState } from 'ahooks';
// import { snakeCase } from '@/utils/utils';
import type { TableColumnProps, TablePaginationConfig } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import { connect, useSelector } from 'dva';
import { useEffect, useMemo } from 'react';
import type { INetworkDetails } from '../../../../typings';
// import { EsegmentAnalysisSearchType, SegmentAnalysisSearchMapping } from '../../../../typings';
import type { ISearchBoxInfo } from '../../../SearchBox';
import { SegmentsCol } from './constant';
export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

interface Props {
  searchBoxInfo: ISearchBoxInfo;
  sortProperty: string;
  sortDirection: ESortDirection;
  changeSortProperty: any;
  changeSortDirection: any;
  allApplicationMap: IApplicationMap;
  dataSource: INetworkDetails[];
  defaultShowColumns: string[];
  tableColumnsChange: any;
  queryLoding: boolean;
}

function SegmentsTable(props: Props) {
  const {
    searchBoxInfo,
    sortProperty,
    sortDirection,
    changeSortProperty,
    changeSortDirection,
    dataSource,
    defaultShowColumns,
    tableColumnsChange,
    allApplicationMap,
    // drilldown,
    queryLoding,
  } = props;

  // const translationCol = useCallback(
  //   (dataIndex, text) => {
  //     let tmpFunc: any = () => text;
  //     const translationType = searchBoxInfo?.segmentAnalysisSearchType;
  //     if (translationType === EsegmentAnalysisSearchType.APPLICATION) {
  //       tmpFunc = (index: any, content: any) => {
  //         return allApplicationMap[content]?.nameText || (content as string);
  //       };
  //     }
  //     return tmpFunc(dataIndex, text);
  //   },
  //   [allApplicationMap, searchBoxInfo?.segmentAnalysisSearchType],
  // );
// const [dataSet] = useSetState(()=>{
//   return dataSource.map((item)=>{
//     if(item.networkGroupId){}
//   });
// });

const allNetworkSensorMap = useSelector<ConnectState, Required<INetworkSensorMap>>(
  (state) => state.networkModel.allNetworkSensorMap,
);

const allNetworkGroupMap = useSelector<ConnectState, Required<INetworkGroupMap>>(
  (state) => state.networkModel.allNetworkGroupMap,
);

const allLogicalSubnetMap = useSelector<ConnectState, Required<ILogicalSubnetMap>>(
  (state) => state.logicSubnetModel.allLogicalSubnetMap,
);

const networkIdMap = useMemo(()=>{
  return {
    ...allNetworkSensorMap,
    ...allNetworkGroupMap,
    ...allLogicalSubnetMap,
  };
},[allLogicalSubnetMap, allNetworkGroupMap, allNetworkSensorMap]);

const networkTableData = useMemo(() => {
  return dataSource.map((item) => {
    const networkIdValue = item.networkId || item.networkGroupId;
    const networKName = networkIdMap[networkIdValue]?.name || networkIdValue;
    if (item.networkId) {
      return { ...item, networkId: networKName, networkType: '探针网络' };
    }
    if (item.networkGroupId) {
      return { ...item, networkId: networKName, networkType: '网络组' };
    }
    return item;
  });
}, [dataSource, networkIdMap]);

// useEffect(() => {
//   console.log(networkTableData, 'networkTableData');
// }, [networkTableData]);

  const segmentsColumns: ColumnProps<any>[] = useMemo(() => {
    const cols = [
      {
        title: '#',
        align: 'center',
        dataIndex: 'index',
        width: 60,
        fixed: true,
        render: (text: any, record: any, index: number) => index + 1,
      },
      ...SegmentsCol,
    ];
    return cols.map((col) => {
      return {
        ...col,
        key: col.dataIndex,
        sortOrder: sortProperty === col.dataIndex ? `${sortDirection}end` : undefined,
        align: 'center',
        // render: (_,record) => {
        //   const dataIndex = col.dataIndex;
        //   return <EllipsisCom>{record[dataIndex]}</EllipsisCom>;
        // },
      };
    });
  }, [sortDirection, sortProperty]);
  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
    let newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    const newSortProperty = sorter.field;
    // 如果当前排序字段不是现在的字段，默认是倒序
    if (newSortProperty !== sortProperty) {
      newSortDirection = ESortDirection.DESC;
    }
    // setSortDirection(newSortDirection);
    // setSortProperty(newSortProperty);
    changeSortDirection(newSortDirection);
    changeSortProperty(newSortProperty);
  };

  return (
    <div>
      <EnhancedTable
        tableKey={`table-key`}
        columns={segmentsColumns}
        rowKey={(record) => record.index}
        loading={queryLoding}
        dataSource={networkTableData}
        size="small"
        bordered
        onChange={handleTableChange}
        defaultShowColumns={defaultShowColumns}
        onColumnChange={(cols) => tableColumnsChange(cols)}
        autoHeight
        // onRow={(record) => {
        //   return {
        //     onClick: (e) => {
        //       clickRow(e, record);
        //     },
        //   };
        // }}
      />
    </div>
  );
}

export default connect((state: any) => {
  const {
    // appModel: { globalSelectedTime },
    SAKnowledgeModel: { allApplicationMap },
  } = state;
  return { allApplicationMap };
})(SegmentsTable);
