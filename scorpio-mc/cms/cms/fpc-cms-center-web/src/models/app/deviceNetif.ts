import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import {
  queryDeviceNetifs,
  queryDeviceNetifUsages,
  queryReceiveDeviceNetifUsages,
  updateDeviceNetifs,
  queryMetricAnalysysHistogram,
} from '@/services/app/deviceNetif';
import type { Effect } from 'umi';
import type { Reducer } from 'umi';
import type { INetifAnalysis, INetif } from '@/typings/netif';

export interface DeviceNetifModelState {
  list: INetif[];
  receiveNetifUsages: Record<string, any>;
  currentItem: INetif;
  modalVisible: boolean;
  netifHistogram: INetifAnalysis[];
}

interface DeviceNetifModelType {
  namespace: string;
  state: DeviceNetifModelState;
  effects: {
    queryDeviceNetifs: Effect;
    queryDeviceNetifUsages: Effect;
    queryReceiveDeviceNetifUsages: Effect;
    updateDeviceNetifs: Effect;
    queryNetifHistogram: Effect;
  };
  reducers: {
    showModal: Reducer<DeviceNetifModelState>;
    hideModal: Reducer<DeviceNetifModelState>;
    clearNetifHistogram: Reducer<DeviceNetifModelState>;
  };
}

export default modelExtend(model, {
  namespace: 'deviceNetifModel',
  state: {
    list: [] as INetif[],
    receiveNetifUsages: {} as any,
    currentItem: {} as INetif,
    modalVisible: false,
    netifHistogram: [],
  },

  effects: {
    *queryDeviceNetifs(_, { call, put }) {
      const { success, result } = yield call(queryDeviceNetifs);
      yield put({
        type: 'updateState',
        payload: {
          list: success ? result.sort((a: any, b: any) => a.name - b.name) : [],
        },
      });
    },
    // 获取某个接口的使用情况
    *queryDeviceNetifUsages({ payload }, { call }) {
      const { success, result } = yield call(queryDeviceNetifUsages, payload);
      return success ? result : {};
    },
    // 获取所有接收接口的使用情况
    *queryReceiveDeviceNetifUsages({ payload }, { call, put }) {
      const { success, result } = yield call(queryReceiveDeviceNetifUsages, payload);
      yield put({
        type: 'updateState',
        payload: {
          receiveNetifUsages: success ? result : {},
        },
      });
    },
    *updateDeviceNetifs({ payload }, { call }) {
      const { success } = yield call(updateDeviceNetifs, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *queryNetifHistogram({ payload }, { call, put }) {
      const { success, result = [] } = yield call(queryMetricAnalysysHistogram, {
        ...payload,
      });

      yield put({
        type: 'updateState',
        payload: {
          netifHistogram: success ? result : [],
        },
      });
    },
  },

  reducers: {
    showModal(state, { payload }) {
      return { ...state, ...payload, modalVisible: true };
    },

    hideModal(state) {
      return { ...state, modalVisible: false, currentItem: {} as INetif };
    },

    clearNetifHistogram: (state) => {
      return {
        ...state,
        netifHistogram: [],
      };
    },
  },
} as DeviceNetifModelType);
