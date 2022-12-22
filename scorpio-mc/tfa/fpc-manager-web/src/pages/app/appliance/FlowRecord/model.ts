import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { model } from '@/utils/frame/model';
import storage from '@/utils/frame/storage';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Reducer } from 'redux';
import type { Effect } from 'umi';
import { v1 as uuidv1 } from 'uuid';

import {
  cancelQueryTask,
  downloadFlowLogFile,
  exportFlowRecords,
  pingQueryTask,
  queryFlowLogsStatistics,
  queryFlowRecordDetail,
  queryFlowRecords,
} from './service';
import type {
  IFlowRecordData,
  IFlowRecordResponse,
  IFlowRecordStatisticsResponse,
  IQueryRecordParams,
} from './typings';

interface IFlowRecordPage {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  total: number;
  // 当前页的记录数
  pageElements: number;
}

export interface FlowRecordModelState {
  recordData: IFlowRecordData[];
  recordQueryParams: IQueryRecordParams;
  flowLogDetail: IFlowRecordData;
  pagination: IFlowRecordPage;
}

export interface ModelType {
  namespace: string;
  state: FlowRecordModelState;
  effects: {
    /** 查询流日志列表 */
    queryFlowRecords: Effect;
    /** 导出流日志列表 */
    exportFlowRecords: Effect;
    /** 取消流日志查询 */
    cancelQueryTask: Effect;
    /** 查询心跳 */
    pingQueryTask: Effect;
    /**
     * 获取流日志详情
     * @deprecated
     */
    queryFlowLogDetail: Effect;
    /** 下载查询到的流日志列表 */
    downloadFlowLogFile: Effect;
    /**
     * 流日志统计信息，只返回总页数
     */
    queryFlowLogsStatistics: Effect;
  };
  reducers: {
    initState: Reducer<FlowRecordModelState>;
    clearFlowLogs: Reducer<FlowRecordModelState>;
  };
}

const getInitPageSize = () =>
  parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE;

export const initQueryParams = {
  page: 1,
  pageSize: getInitPageSize(),
  sortProperty: 'start_time',
  sortDirection: 'desc',
} as IQueryRecordParams;

const initStateData = {
  recordData: [], // 表格列表
  aggregations: [], // 左侧的统计聚合
  flowLogDetail: {} as IFlowRecordData, // 某一条流的详情
  recordQueryParams: {
    ...initQueryParams,
  } as IQueryRecordParams, // 搜索条件
  pagination: {
    currentPage: initQueryParams.page,
    pageSize: getInitPageSize(),
    total: 0,
    totalPages: 0,
    pageElements: 0,
  },
};

const Model = modelExtend(model, {
  namespace: 'flowRecordModel',
  state: {
    ...initStateData,
  },

  effects: {
    *queryFlowRecords({ payload }, { call, put, select }) {
      const params: IQueryRecordParams = {
        // @ts-ignore
        page: 1,
        ...initQueryParams,
        ...payload,
      };
      const { page, pageSize } = params;
      const {
        success,
        result,
        status,
      }: { status: number; success: boolean; result: IFlowRecordResponse } = yield call(
        queryFlowRecords,
        {
          ...params,
          page: page! - 1,
        },
      );
      // 原始的分页数据
      const { pagination: oldPage } = yield select((state: ConnectState) => state.flowRecordModel);

      if (payload.flowId && success) {
        oldPage.total = result.totalElements;
      }

      if (!success) {
        yield put({
          type: 'updateState',
          payload: {
            recordQueryParams: params,
            recordData: [],
            pagination: {
              ...oldPage,
              currentPage: page,
              pageSize,
              total: 0,
              totalPages: 0,
              pageElements: 0,
            },
          },
        });
        return {
          status,
          success,
        };
      }

      const content: IFlowRecordData[] = [];
      for (let index = 0; index < result.content.length; index += 1) {
        const row: IFlowRecordData = result.content[index];

        if (row.ip_nat_initiator === '0.0.0.0') {
          row.ip_nat_initiator = undefined;
        }
        if (row.ip_nat_responder === '0.0.0.0') {
          row.ip_nat_responder = undefined;
        }
        if (row.port_nat_initiator === 0) {
          row.port_nat_initiator = undefined;
        }
        if (row.port_nat_responder === 0) {
          row.port_nat_responder = undefined;
        }
        content.push({
          ...row,
          id: `${row.flow_id}_${uuidv1()}`,
          indexNumber: (page! - 1) * pageSize! + index + 1,
        });
      }

      yield put({
        type: 'updateState',
        payload: {
          recordQueryParams: params,
          recordData: content,
          pagination: {
            ...oldPage,
            currentPage: page,
            pageSize,
            // fix: 总数查询需要扫描全量的结果集，磁盘IO使用极高
            // 所以总页数由统计接口返回
            pageElements: result.content.length,
          },
        },
      });

      return {
        status,
        success,
        total: result.totalElements,
        totalPages: result.totalPages,
      };
    },

    *cancelQueryTask({ payload }, { call }) {
      const { success } = yield call(cancelQueryTask, payload);
      return success;
    },

    *pingQueryTask({ payload }, { call }) {
      const { success } = yield call(pingQueryTask, payload);
      return success;
    },

    *exportFlowRecords({ payload }, { call }) {
      yield call(exportFlowRecords, payload);
    },

    *queryFlowLogDetail({ payload }, { call, put }) {
      // 先清空
      yield put({
        type: 'updateState',
        payload: {
          flowLogDetail: {},
        },
      });

      const { success, result } = yield call(queryFlowRecordDetail, payload);

      let flowLogDetail = {} as IFlowRecordData;
      if (success && Array.isArray(result) && result.length > 0) {
        // eslint-disable-next-line prefer-destructuring
        flowLogDetail = result[0];
      }

      if (flowLogDetail.application_id) {
        // 处理应用分类和应用
        // @ts-ignore
        const applicationInfo = yield put.resolve({
          type: 'SAKnowledgeModel/getApplicationAndCategoryInfo',
          payload: {
            applicationId: String(flowLogDetail.application_id),
          },
        });

        flowLogDetail = {
          ...flowLogDetail,
          // 处理分类和应用名称
          ...applicationInfo,
        };
      }

      yield put({
        type: 'updateState',
        payload: {
          flowLogDetail,
        },
      });
    },

    *downloadFlowLogFile({ payload }, { call }) {
      const { success, result } = yield call(downloadFlowLogFile, payload);
      if (success && result) {
        window.open(result);
        return;
      }
      message.warning('下载失败');
    },

    *queryFlowLogsStatistics({ payload }, { call, put, select }) {
      const {
        status,
        success,
        result,
      }: { status: number; success: boolean; result: IFlowRecordStatisticsResponse } = yield call(
        queryFlowLogsStatistics,
        payload,
      );

      // 获取老的分页信息
      const oldPage: IFlowRecordPage = yield select(
        (state: ConnectState) => state.flowRecordModel.pagination,
      );
      const nextPage = { ...oldPage };

      if (success) {
        const { total = 0 } = result || ({} as IFlowRecordStatisticsResponse);
        // 更新分页信息
        nextPage.total = total;
        // 计算总页码
        nextPage.totalPages = Math.ceil(total / nextPage.pageSize);
      }

      yield put({
        type: 'updateState',
        payload: {
          pagination: nextPage,
        },
      });

      return { status };
    },
  },

  reducers: {
    initState(state, { payload }) {
      return {
        ...state,
        recordData: [], // 表格列表
        flowLogDetail: {} as IFlowRecordData,
        recordQueryParams: {
          ...payload.recordQueryParams,
        }, // 搜索条件
      };
    },
    clearFlowLogs(state) {
      return {
        ...state,
        recordQueryParams: initQueryParams,
        recordData: [],
      };
    },
  },
} as ModelType);

export default Model;
