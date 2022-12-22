import { getTablePaginationDefaultSettings, PAGE_DEFAULT_SIZE } from '@/common/app';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import CustomPagination from '@/components/CustomPagination';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type { IEnumValue, IField, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '@/pages/app/appliance/Metadata/components/Template';
import { snakeCase } from '@/utils/utils';
import { Row, Table } from 'antd';
import { useContext, useEffect, useMemo, useState } from 'react';
import { v1 } from 'uuid';
import { queryRestApiRecord, queryRestPermUserList } from '../../service';
import type { IRestAPIRecord } from '../../typings';
import { RestStatCommonDataContext } from '../Layout';

const RestAPIRecord = () => {
  const [contentHeight, setContentHeight] = useState<number>();
  const [pagination, setPagination] = useState<{ page: number; pageSize: number }>({
    page: 1,
    pageSize: getTablePaginationDefaultSettings().pageSize! || PAGE_DEFAULT_SIZE,
  });
  const [totalElements, setTotalElements] = useState(0);

  const [tableData, setData] = useState<IRestAPIRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const { startTime, endTime, userMap } = useContext(RestStatCommonDataContext);

  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  const [restUsers, setRestUsers] = useState<{ name: string; userId: string }[]>([]);

  const userEnum: IEnumValue[] = useMemo(() => {
    return restUsers.map((u) => {
      return {
        text: u.name,
        value: u.userId,
      };
    });
  }, [restUsers]);

  useEffect(() => {
    queryRestPermUserList().then((res) => {
      const { success, result } = res;
      if (success) {
        setRestUsers(result);
      }
    });
  }, []);

  const columns: IColumnProps<IRestAPIRecord>[] = useMemo(
    () =>
      [
        {
          dataIndex: 'apiName',
          title: 'API名称',
          align: 'center',
          width: 200,
        },
        {
          dataIndex: 'uri',
          title: 'URI',
          align: 'center',
          width: 400,
        },
        {
          dataIndex: 'userIp',
          title: '来访IP',
          align: 'center',
          width: 150,
        },
        {
          dataIndex: 'method',
          title: '方法',
          align: 'center',
          width: 100,
        },
        {
          dataIndex: 'userId',
          title: '用户',
          align: 'center',
          width: 150,
          operandType: EFieldOperandType.ENUM,
          enumValue: userEnum,
          render: (dom, record) => {
            const { userId } = record;
            return userMap[userId]?.name || userId;
          },
        },
        {
          dataIndex: 'timestamp',
          title: '访问时间',
          align: 'center',
          searchable: false,
          width: 200,
          sortOrder: 'descend',
        },
        {
          dataIndex: 'status',
          title: '访问结果',
          align: 'center',
          width: 100,
          enumValue: [
            { text: '成功', value: '1' },
            { text: '失败', value: '0' },
          ],
          operandType: EFieldOperandType.ENUM,
          render: (dom, record) => {
            const { status } = record;
            return status ? '成功' : '失败';
          },
        },
        {
          dataIndex: 'response',
          title: '响应信息',
          align: 'center',
        },
      ] as IColumnProps<IRestAPIRecord>[],
    [userMap, userEnum],
  );

  const filterFields: IField[] = useMemo(
    () =>
      columns
        .filter((col) => col.searchable !== false)
        .map((item) => {
          return {
            dataIndex: snakeCase(item.dataIndex as string),
            type: item.fieldType,
            operandType: item.operandType,
            enumValue: item.enumValue,
            title: item.title as string,
          };
        }),
    [columns],
  );

  const queryParams = useMemo(() => {
    const dsl = `${filterCondition2Spl(
      filterCondition,
      filterFields,
    )} | gentimes timestamp start="${startTime}" end="${endTime}"`;

    return {
      startTime: startTime,
      endTime: endTime,
      sortProperty: 'timestamp',
      sortDirection: 'desc',
      dsl,
    };
  }, [endTime, filterCondition, filterFields, startTime]);

  useEffect(() => {
    setPagination((prev) => {
      if (prev.page !== 1) {
        return {
          ...prev,
          page: 1,
        };
      }
      // 如果在首页，则放弃更新，可以少发一次请求
      return prev;
    });
  }, [queryParams]);

  useEffect(() => {
    setLoading(true);
    queryRestApiRecord({
      ...queryParams,
      pageSize: pagination.pageSize,
      page: pagination.page - 1,
    }).then((res) => {
      const { success, result } = res;
      if (success) {
        setData(result.content);
        setTotalElements(result.totalElements);
      }
      setLoading(false);
    });
  }, [pagination, queryParams]);

  return (
    <AutoHeightContainer
      onHeightChange={(height) => setContentHeight(height)}
      headerRender={
        <>
          <Row justify="space-between" style={{ marginBottom: 6 }}>
            <FieldFilter
              key="rest-api-record-filter"
              fields={filterFields}
              condition={filterCondition}
              onChange={(condition) => {
                setFilterCondition(condition);
              }}
              historyStorageKey={'rest-api-record-history-filter'}
            />
          </Row>
        </>
      }
    >
      <Table
        size="small"
        columns={columns}
        bordered
        rowKey={() => v1()}
        // onChange={(pageConfig) => {
        //   if (
        //     pageConfig.current !== pagination.page ||
        //     pageConfig.pageSize !== pagination.pageSize
        //   ) {
        //     setPagination({
        //       page: pageConfig.current || 0,
        //       pageSize: pageConfig.pageSize || PAGE_DEFAULT_SIZE,
        //     });
        //   }
        // }}
        dataSource={tableData}
        loading={loading}
        pagination={false}
        scroll={{
          x: 'max-content',
          // 39: 表头
          // 62: 表头高度
          // 24: 分页器高度
          // 32 分页器margin
          // 32 filter高度
          y: contentHeight ? contentHeight - 39 - 24 - 32 : 'auto',
        }}
      />
      <CustomPagination
        currentPage={pagination.page}
        pageSize={pagination.pageSize}
        total={totalElements}
        onChange={function (currentPage: number, pageSize: number): void {
          setPagination({
            page: currentPage,
            pageSize,
          });
        }}
      />
    </AutoHeightContainer>
  );
};

export default RestAPIRecord;
