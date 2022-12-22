import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import type { ConnectState, Loading } from '@/models/connect';
import type { IPageModelState } from '@/utils/frame/model';
import storage from '@/utils/frame/storage';
import { getLinkUrl } from '@/utils/utils';
import type { TableColumnProps } from 'antd';
import type { TablePaginationConfig } from 'antd/lib/table/interface';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch, IScenarioTaskModelState } from 'umi';
import { Link, useDispatch, useSelector } from 'umi';
import type { IQueryScenarioTaskResultParams, IScenarioTaskBeaconResult } from '../../../typings';
import { SORT_DIRECTION_ASC, SORT_DIRECTION_DESC } from '../../index';
import ResultTable from '../ResultTable';

const BeaconResult: React.FC = () => {
  const { scenarioTaskDetail, scenarioTaskResult, pagination } = useSelector<
    ConnectState,
    IScenarioTaskModelState & IPageModelState
  >((state) => state.scenarioTaskModel);
  const dispatch = useDispatch<Dispatch>();

  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(() => {
    return parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE;
  });
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>(SORT_DIRECTION_DESC);

  const queryLoading = useSelector<ConnectState, Loading>((state) => state.loading).effects[
    'scenarioTaskModel/queryScenarioTaskResults'
  ];

  const columns: TableColumnProps<any>[] = useMemo(() => {
    return [
      {
        title: '源IP',
        dataIndex: 'src_ip',
        align: 'center',
      },
      {
        title: '目的IP',
        dataIndex: 'dest_ip',
        align: 'center',
      },
      {
        title: '目的端口',
        dataIndex: 'dest_port',
        align: 'center',
      },
      {
        title: '协议',
        dataIndex: 'protocol',
        align: 'center',
      },
      {
        title: '周期(s)',
        dataIndex: 'period',
        align: 'center',
      },
      {
        title: '数量',
        dataIndex: 'record_total_hit',
        sorter: true,
        sortOrder: `${sortDirection}end`,
        align: 'center',
      },
      {
        title: '操作',
        width: 100,
        align: 'center',
        render: (text, record) => (
          <Link
            to={getLinkUrl(
              `/detection/scenario-task/result/metadata-${record.protocol}?analysisResultId=${
                record.id
              }&analysisStartTime=${encodeURIComponent(
                scenarioTaskDetail.analysisStartTime,
              )}&analysisEndTime=${encodeURIComponent(scenarioTaskDetail.analysisEndTime)}`,
            )}
          >
            详情
          </Link>
        ),
      },
    ];
  }, [scenarioTaskDetail.analysisEndTime, scenarioTaskDetail.analysisStartTime, sortDirection]);

  const queryParams = useMemo<IQueryScenarioTaskResultParams>(() => {
    return {
      type: scenarioTaskDetail.type || 'beacon-detection',
      page: currentPage,
      pageSize,
      id: scenarioTaskDetail.id,
      sortDirection,
      sortProperty: 'record_total_hit',
    };
  }, [currentPage, pageSize, scenarioTaskDetail.id, scenarioTaskDetail.type, sortDirection]);

  const queryResult = useCallback(
    (params: IQueryScenarioTaskResultParams) => {
      dispatch({
        type: 'scenarioTaskModel/queryScenarioTaskResults',
        payload: params,
      });
    },
    [dispatch],
  );

  useEffect(() => {
    queryResult(queryParams);
  }, [queryResult, queryParams]);

  useEffect(() => {
    queryResult(queryParams);
  }, [queryParams, queryResult]);

  const handleTableChange = (pageObj: TablePaginationConfig, filters: any, sorter: any) => {
    const sortOrder = sorter.order || 'ascend';

    if (pageObj.current) {
      setCurrentPage(pageObj.current + 1);
    }
    if (pageObj.pageSize) {
      setPageSize(pageObj.pageSize);
      storage.put(DEFAULT_PAGE_SIZE_KEY, pageObj.pageSize);
    }

    setSortDirection(sortOrder === 'descend' ? SORT_DIRECTION_DESC : SORT_DIRECTION_ASC);
  };

  return (
    <ResultTable<IScenarioTaskBeaconResult>
      loading={queryLoading}
      columns={columns}
      dataSource={scenarioTaskResult as IScenarioTaskBeaconResult[]}
      pagination={{ ...pagination, pageSize }}
      onTableChange={handleTableChange}
    />
  );
};

export default BeaconResult;
