import type { Reducer } from 'redux';
import type { Effect } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { downloadPcapFile, queryPacketList, queryPacketRefine } from './service';
import type {
  IPacket,
  IPacketRefine,
  IPacketRefineResponse,
  IPacketResponse,
  IQueryParams,
} from './typings';
import { EPacketRefineStatus, EResponseCode } from './typings';

export const initPacketRefineData: IPacketRefine = {
  status: EPacketRefineStatus.DONE,
  message: '',
  aggregations: [],
  execution: {
    searchBytes: 0,
    searchPacketCount: 0,
    searchFlowCount: 0,
    matchMinTimestamp: 0,
    matchMaxTimestamp: 0,
  },
};

export interface IPacketModelState {
  /** 数据包列表数据 */
  listData: IPacket[];
  /** 数据包聚合数据 */
  refineData: IPacketRefine;

  /** BPF过滤条件 */
  bpfData: string;
  /**
   * 数据包列表最大请求数量
   */
  limit: IQueryParams['limit'];
}

interface ReducerType {
  state: IPacketModelState;
  action: Record<string, any>;
}

export interface PacketModelType {
  namespace: string;
  state: IPacketModelState;
  effects: {
    /** 查询数据包列表 */
    queryPacketList: Effect;
    /** 查询数据包统计信息 */
    queryPacketRefine: Effect;
    /** 查询数据包统计信息 */
    downloadPcapFile: Effect;
    /** 更新数据包列表返回数量 */
    updatePacketListLimit: Effect;
    /** 更新BPF语句条件 */
    updateBpf: Effect;
  };
  reducers: {
    /** 保存数据包列表数据 */
    saveListData: Reducer<ReducerType>;
    /** 保存数据包聚合数据 */
    saveRefineData: Reducer<ReducerType>;
    /** 保存返回数量 */
    saveLimit: Reducer<ReducerType>;
    /** 保存 BPF条件 */
    saveBpf: Reducer<ReducerType>;
  };
}
const Packet = {
  namespace: 'packetModel',
  state: {
    listData: [],
    refineData: initPacketRefineData,

    limit: 100,
    filterData: [],
    bpfData: '',
  },
  effects: {
    *queryPacketList({ payload = {} }, { call, put }) {
      const { success, result }: { success: boolean; result: IPacketResponse } = yield call(
        queryPacketList,
        payload,
      );

      let listData: IPacket[] = [];
      if (success && result.code === EResponseCode.Success) {
        listData = result.result.map((item) => ({ ...item, id: uuidv1() }));
      }

      yield put({
        type: 'saveListData',
        payload: {
          listData,
        },
      });
    },

    *queryPacketRefine({ payload = {} }, { call, put }) {
      const { success, result }: { success: boolean; result: IPacketRefineResponse } = yield call(
        queryPacketRefine,
        payload,
      );

      let refineData: IPacketRefine = initPacketRefineData;
      if (success && result.code === EResponseCode.Success) {
        refineData = result.result;
      }

      yield put({
        type: 'saveRefineData',
        payload: {
          refineData,
        },
      });
    },

    *downloadPcapFile({ payload = {} }, { call }) {
      yield call(downloadPcapFile, payload);
    },

    *updatePacketListLimit({ payload }, { put }) {
      yield put({ type: 'saveLimit', payload });
    },

    *updateBpf({ payload }, { put }) {
      yield put({ type: 'saveBpf', payload });
    },
  },
  reducers: {
    saveListData(state, { payload }) {
      return {
        ...state,
        listData: payload.listData,
      };
    },
    saveRefineData(state, { payload }) {
      return { ...state, refineData: payload.refineData };
    },
    saveLimit(state, { payload }) {
      return { ...state, limit: payload.limit };
    },
    saveBpf(state, { payload }) {
      return { ...state, bpfData: payload.bpfData };
    },
  },
} as PacketModelType;

export default Packet;
