import EnhancedTable from '@/components/EnhancedTable';
import type { IApplicationMap } from '../../../../../../configuration/SAKnowledge/typings';
import type { TablePaginationConfig } from 'antd';
import { connect } from 'dva';
import { useMemo } from 'react';
import type { INetworkDetails } from '../../../../typings';
import type { ISearchBoxInfo } from '../../../SearchBox';
import { SegmentsCol } from './constant';
import type { ColumnProps } from 'antd/lib/table';
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
    queryLoding,
  } = props;

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
        tableKey={`SegmentTableKey`}
        columns={segmentsColumns}
        rowKey={(record) => record.index}
        loading={queryLoding}
        dataSource={dataSource}
        size="small"
        bordered
        onChange={handleTableChange}
        defaultShowColumns={defaultShowColumns}
        onColumnChange={(cols) => tableColumnsChange(cols)}
        autoHeight
      />
    </div>
  );
}

export default connect((state: any) => {
  const {
    SAKnowledgeModel: { allApplicationMap },
  } = state;
  return { allApplicationMap };
})(SegmentsTable);
