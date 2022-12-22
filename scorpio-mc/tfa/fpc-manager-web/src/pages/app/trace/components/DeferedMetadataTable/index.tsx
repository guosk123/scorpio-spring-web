import { getCurrentPageSize } from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import DeferedContainer from '@/components/DeferedContainer';
import EnhancedTable from '@/components/EnhancedTable';
import { filterCondition2Spl } from '@/components/FieldFilter';
import type {
  IField,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import {
  EFieldType,
  EFilterGroupOperatorTypes,
  EFilterOperatorTypes,
} from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { EQueryLogToPkt } from '@/pages/app/appliance/components/DownLoadPktBtn';
import ExportFile, { queryExportFile } from '@/pages/app/appliance/components/ExportFile';
import type { IColumnProps } from '@/pages/app/appliance/Metadata/components/Template';
import { getColumnParams } from '@/pages/app/appliance/Metadata/components/Template';
import useColumnForMetadata from '@/pages/app/appliance/Metadata/hooks/useColumnForMetadata';
import {
  queryMetadataLogs,
  queryMetadataMapfieldKeys,
  queryMetadataTotal,
} from '@/pages/app/appliance/Metadata/service';
import type { IMetadataLog, IQueryMetadataParams } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import { snakeCase } from '@/utils/utils';
import { useSafeState } from 'ahooks';
import { Spin } from 'antd';
import moment from 'moment';
import { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { useSelector } from 'umi';
import { v1 } from 'uuid';
import {
  ETabs,
  IOCContext,
  isFileName,
  isMd5,
  isSha1,
  isSha256,
  searchField,
  tabContent,
} from '../../IOC';
import commonStyle from '../common.less';
import FilterTag from '../FilterTag';

interface Props<RecordType> {
  tableColumns: IColumnProps<RecordType>[];
  isNewIpFieldType?: boolean;
  protocol: EMetadataProtocol;
  timeField?: string;
}

function DeferedMetadataTable<MetaLog extends IMetadataLog>(props: Props<MetaLog>) {
  const { currentTab, search, networkId } = useContext(IOCContext);

  const { tableColumns, isNewIpFieldType = false, protocol, timeField = 'start_time' } = props;

  const tableKey = `ioc-metadata-${protocol}`;

  const [visible, setVisible] = useState(false);
  const visibleRef = useRef(visible);
  visibleRef.current = visible;
  // 表格不可见后，查询条件是否发生变化，
  const queryChangeBeforeVisible = useRef(false);

  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [pageParams, setPageParams] = useState<{ currentPage: number; pageSize: number }>({
    currentPage: 1,
    pageSize: getCurrentPageSize(),
  });
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);
  const [columns, setColumns] = useState<string[]>();

  // 需要在异步动作内修改的状态，使用useSafeState,避免内存泄漏
  const [table, setTable] = useSafeState<{ data: MetaLog[]; loading: boolean }>({
    data: [],
    loading: false,
  });
  const [recordTotal, setRecordTotal] = useSafeState<{ total: number; loading: boolean }>({
    total: 0,
    loading: false,
  });
  // http协议头以后会使用Map字段过滤，所以这里提前准备好Map类型过滤
  const [mapFieldKeys, setMapFieldKeys] = useSafeState<Record<string, string[]>>({});

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const { fullTableColumns, fullColumns } = useColumnForMetadata({
    protocol,
    tableColumns,
    sortDirection,
    isNewIpFieldType,
    onFilterClick: setFilterCondition,
    selectedTimeInfo: globalSelectedTime,
  });

  const filterField = useMemo<IField[]>(() => {
    return fullColumns
      .filter((item) => item.searchable)
      .map((item) => {
        const fieldValue = snakeCase(item.dataIndex as string);
        return {
          title: item.title as string,
          dataIndex: fieldValue as string,
          ...(item.fieldType ? { type: item.fieldType } : {}),
          operandType: item.operandType!,
          enumValue: item.enumValue || [],
          ...(item.fieldType === EFieldType.Map
            ? {
                subFields: mapFieldKeys[fieldValue]?.map((key) => {
                  return {
                    dataIndex: key,
                    title: key,
                  };
                }),
              }
            : {}),
        };
      });
  }, [fullColumns, mapFieldKeys]);

  const dsl = useMemo(() => {
    const time = {
      startTime: moment(globalSelectedTime.originStartTime).format(),
      endTime: moment(globalSelectedTime.originEndTime).format(),
    };

    let filterSpl = filterCondition2Spl(filterCondition, filterField);

    // 网络可以多选
    if (networkId.length > 0) {
      if (filterSpl) {
        filterSpl += ' AND ';
      }
      const netFilter: IFilterGroup = { operator: EFilterGroupOperatorTypes.OR, group: [] };
      networkId.forEach((item) => {
        netFilter.group.push({
          field: 'network_id',
          operator: EFilterOperatorTypes.EQ,
          operand: item,
        });
      });
      filterSpl += filterCondition2Spl([netFilter], filterField);
    }

    if (search?.isValid && search.value) {
      if (filterSpl) {
        filterSpl += ' AND ';
      }
      const searchFields = searchField[currentTab][`metadata-${protocol}`];
      let operator = EFilterOperatorTypes.EQ;
      // 如果搜索的使fileName且查询的使mail详单，则使用like查询，
      // mail详单里存的attachment，不是单纯的文件名称，还包含Content-type
      // 邮箱地址是 username <a@b.com>的形式，所以只能使用like进行匹配
      if (protocol === EMetadataProtocol.MAIL && currentTab === ETabs.mail) {
        operator = EFilterOperatorTypes.LIKE;
      }
      // 如果是查文件详单，单独处理
      if (protocol === EMetadataProtocol.FILE && currentTab === ETabs.fileName) {
        const filter: IFilter = {
          field: 'name',
          operator,
          operand: search.value,
        };
        let isHash = false;
        if (isMd5(search.value)) {
          filter.field = 'md5';
          isHash = true;
        }
        if (isSha1(search.value)) {
          filter.field = 'sha1';
          isHash = true;
        }
        if (isSha256(search.value)) {
          filter.field = 'sha256';
          isHash = true;
        }
        if (isFileName(search.value) && isHash === false) {
          filter.operator = EFilterOperatorTypes.LIKE;
        }
        filterSpl += filterCondition2Spl([filter], filterField);
      } else {
        // 如果只有一个字段，就不管其他
        if (searchFields?.length === 1) {
          const filter: IFilter = {
            field: searchFields[0],
            operator,
            operand: search.value.trim(),
          };
          filterSpl += filterCondition2Spl([filter], filterField);
        } else {
          const filter: IFilterGroup = { operator: EFilterGroupOperatorTypes.OR, group: [] };
          searchFields?.map((field) => {
            filter.group.push({
              field,
              operator,
              operand: search.value.trim(),
            });
          });
          filterSpl += filterCondition2Spl([filter], filterField);
        }
      }
    }

    filterSpl += ` | gentimes ${timeField} start="${time.startTime}" end="${time.endTime}"`;

    return filterSpl;
  }, [
    currentTab,
    filterCondition,
    filterField,
    globalSelectedTime.originEndTime,
    globalSelectedTime.originStartTime,
    networkId,
    protocol,
    search,
    timeField,
  ]);

  const queryParams = useMemo(() => {
    const params: IQueryMetadataParams = {
      protocol,
      sortProperty: timeField,
      sortDirection,
      startTime: moment(globalSelectedTime.originStartTime).format(),
      endTime: moment(globalSelectedTime.originEndTime).format(),
      dsl,
    };

    if (!visibleRef.current) {
      queryChangeBeforeVisible.current = true;
    }

    return params;
  }, [
    dsl,
    globalSelectedTime.originEndTime,
    globalSelectedTime.originStartTime,
    protocol,
    sortDirection,
    timeField,
  ]);

  useEffect(() => {
    queryMetadataMapfieldKeys(protocol).then((res) => {
      const { success, result } = res;
      if (success) {
        setMapFieldKeys(result);
      }
    });
  }, [protocol, setMapFieldKeys]);

  const query = useCallback(
    (params: IQueryMetadataParams) => {
      setTable((prev) => {
        return {
          ...prev,
          loading: true,
        };
      });
      setRecordTotal((prev) => {
        return {
          ...prev,
          loading: true,
        };
      });
      queryMetadataLogs(params).then((res) => {
        const { success, result } = res;
        if (success) {
          setTable({
            data: result.content.map((item, index) => {
              return {
                ...item,
                index: index + 1,
              };
            }),
            loading: false,
          });
        } else {
          setTable((prev) => {
            return {
              ...prev,
              loading: false,
            };
          });
        }
      });
      queryMetadataTotal(params).then((res) => {
        const { success, result } = res;
        if (success) {
          setRecordTotal({ total: result.total, loading: false });
        } else {
          setRecordTotal((prev) => {
            return {
              ...prev,
              loading: false,
            };
          });
        }
      });
    },
    [setRecordTotal, setTable],
  );

  useEffect(() => {
    if (visibleRef.current) {
      query({
        ...pageParams,
        ...queryParams,
        page: pageParams.currentPage - 1,
        columns: getColumnParams({ cols: columns, tableKey }),
      });
    }
  }, [columns, pageParams, query, queryParams, tableKey]);

  useEffect(() => {
    if (visible && queryChangeBeforeVisible.current) {
      query({
        ...pageParams,
        ...queryParams,
        page: pageParams.currentPage - 1,
        columns: getColumnParams({ cols: columns, tableKey }),
      });
      queryChangeBeforeVisible.current = false;
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  const handlePageChange = (currentPage: number, pageSize: number) => {
    setPageParams({
      currentPage,
      pageSize,
    });
  };

  const removeFilterItem = (idx: number) => {
    setFilterCondition((prev) => {
      return prev.filter((item, i) => idx !== i);
    });
  };

  const handleTableChange = (page: any, filters: any, sorter: any) => {
    const newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    setSortDirection(newSortDirection);
  };

  return (
    <DeferedContainer visibleChange={setVisible}>
      <EnhancedTable
        autoHeight={tabContent[currentTab].length < 2}
        {...(tabContent[currentTab].length < 2 ? {} : { scroll: { y: 500 } })}
        tableKey={`ioc-metadata-${protocol}`}
        columns={fullTableColumns}
        dataSource={table.data || []}
        onChange={handleTableChange}
        pagination={false}
        loading={table.loading}
        rowKey={() => v1()}
        onColumnChange={setColumns}
        extraTool={
          <div className={commonStyle.header}>
            <div>
              <div className={commonStyle.title}>
                <div>{`${
                  protocol !== EMetadataProtocol.FILE ? protocol.toLocaleUpperCase() : '文件'
                }详单`}</div>
              </div>
              <FilterTag
                filter={filterCondition}
                onRemove={removeFilterItem}
                fields={filterField}
              />
            </div>
            <ExportFile
              loading={table.loading}
              totalNum={recordTotal.total}
              queryFn={(params: any) => {
                return queryExportFile(
                  {
                    ...params,
                    ...queryParams,
                    columns: getColumnParams({ cols: columns, tableKey }),
                  },
                  EQueryLogToPkt.MetaData,
                  protocol,
                );
              }}
            />
          </div>
        }
        extraFooter={
          <Spin spinning={recordTotal.loading} size="small">
            <CustomPagination
              loading={recordTotal.loading}
              onChange={handlePageChange}
              currentPage={pageParams.currentPage}
              pageSize={pageParams.pageSize}
              total={recordTotal.total}
            />
          </Spin>
        }
      />
    </DeferedContainer>
  );
}

export default DeferedMetadataTable;
