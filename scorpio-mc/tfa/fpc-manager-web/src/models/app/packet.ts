import { requestShowList, requestTreeData } from '@/services/app/packet';
import type { Effect, Reducer } from 'umi';
import { v1 as uuidv1 } from 'uuid';

export interface ListState {
  timestamp: string;
  networkId: string;
  serviceId: string;
  ipInitiator: string;
  portInitiator: string;
  ipResponder: string;
  portResponder: string;
  ipProtocol: string;
  tcpFlags: string;
  vlanId: string;
  totalBytes: string;
  applicationId: string;
  l7ProtocolId: string;
  ethernetType: string;
  ethernetInitiator: string;
  ethernetResponder: string;
  countryIdInitiator: string;
  provinceIdInitiator: string;
  cityIdInitiator: string;
  countryIdResponder: string;
  provinceIdResponder: string;
  cityIdResponder: string;
}

export interface PacketState {
  uuid: string;
  networkId: string;
  serviceId: string;
  totalPacket: number;
  listData: ListState[];
  tagData: string[];
  treeData: {
    result: {
      status: string;
      message: string;
      aggregations: string[];
      execution: {
        matchMinTimestamp: number;
        matchMaxTimestamp: number;
      };
    };
  };
}

interface ReducerType {
  state: PacketState;
  action: object;
}

export interface PacketModelType {
  namespace: 'packet';
  state: PacketState;
  effects: {
    getListData: Effect;
    getTreeData: Effect;
    addTag: Effect;
    changeUUID: Effect;
    closeTag: Effect;
    changeTotalPacket: Effect;
  };
  reducers: {
    saveListData: Reducer<ReducerType>;
    saveTreeData: Reducer<ReducerType>;
    saveTag: Reducer<ReducerType>;
    removeTag: Reducer<ReducerType>;
    saveUUID: Reducer<ReducerType>;
    saveTotalPacket: Reducer<ReducerType>;
  };
}
export interface PacketQueryInfo {
  queryId: string;
  startTime: string;
  endTime: string;
  networkId: string;
  serviceId: string;
  conditionType: string;
  bpf: string;
  tuple: string;
}
const Packet: PacketModelType = {
  namespace: 'packet',
  state: {
    uuid: uuidv1(),
    totalPacket: 100,
    tagData: [],
    listData: [],
    networkId: '',
    serviceId: '',
    treeData: {
      result: {
        status: '',
        message: '',
        aggregations: [],
        execution: {
          matchMinTimestamp: 0,
          matchMaxTimestamp: 0,
        },
      },
    },
  },
  effects: {
    // 获取列信息
    *getListData(action, { call, put }) {
      const data = yield requestShowList();
      if (data.code === 0) {
        yield put({
          type: 'saveListData',
          payload: {
            result: data!.result,
            networkId: action.networkId,
            serviceId: action.serviceId,
          },
        });
      }
    },
    // 获取统计信息
    *getTreeData(action, { put }) {
      const data = yield requestTreeData();
      if (data.code === 0) {
        yield put({ type: 'saveTreeData', payload: data });
      }
    },
    // 添加过滤条件
    *addTag(action, { put }) {
      yield put({ type: 'saveTag', payload: action.payload });
    },
    // 更换uuid
    *changeUUID(action, { put }) {
      yield put({ type: 'saveUUID', payload: action.newUUID });
    },
    // 取消过滤条件
    *closeTag(action, { put }) {
      yield put({ type: 'removeTag', payload: action });
    },
    // 更改展示包数
    *changeTotalPacket(action, { put }) {
      yield put({ type: 'saveTotalPacket', payload: action.totalPacket });
    },
  },
  reducers: {
    /**
     *保存
     * @param {当前的state值} state
     * @param {交付的数据信息} action
     * @returns 更变state值
     */
    saveListData(state: any, action: any) {
      return {
        ...state,
        listData: action.payload.result,
        networkId: action.payload.networkId,
        serviceId: action.payload.serviceId,
      };
    },
    saveTreeData(state: any, action: any) {
      return { ...state, treeData: action.payload };
    },
    saveTag(state: any, action: any) {
      const newTags = state?.tagData.concat(action.payload);
      return { ...state, tagData: newTags };
    },
    removeTag(state: any, action: any) {
      const newTags = state.tagData.filter((tag: string) => tag !== action.payload.tag);
      return { ...state, tagData: newTags };
    },
    saveUUID(state: any, action: any) {
      return { ...state, uuid: action.payload };
    },
    saveTotalPacket(state: any, action: any) {
      return { ...state, totalPacket: action.payload };
    },
  },
};

export default Packet;
