import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import {
  queryDeviceNetifs,
  queryReceiveDeviceNetifUsages,
  updateDeviceNetifs,
} from '@/services/app/deviceNetif';
import { updateSystemServerIpSettings } from '@/pages/frame/system/ServerIp/service';
import { ENetifCategory, INetif } from '@/pages/app/configuration/DeviceNetif/typings';
import type { Effect } from 'umi';
import type { Reducer } from 'umi';
import { queryMetricAnalysysHistogram } from '@/services/app/analysis';
import type { INetifAnalysis } from '@/pages/app/configuration/DeviceNetif/typings';
import { EMetricApiType } from '@/common/api/analysis';

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
      const { category, netifName, netifListJson } = payload;
      const { ipv4Address, ipv4Gateway, ipv6Address, ipv6Gateway } = JSON.parse(netifListJson)[0];
      if (category === ENetifCategory.NETFLOW) {
        const params = {
          netifName,
          ipv4Address,
          ipv4Gateway,
          ipv6Address,
          ipv6Gateway,
          dns: '',
        };

        const { success: deviceNetifSuccess } = yield call(updateDeviceNetifs, { netifListJson });
        const { success: serverIpSettingSuccess } = yield call(
          updateSystemServerIpSettings,
          params,
        );
        if (deviceNetifSuccess && serverIpSettingSuccess) {
          message.success('编辑成功');
        } else {
          message.error('编辑失败');
        }
        return deviceNetifSuccess;
      } else {
        const { success } = yield call(updateDeviceNetifs, { netifListJson });
        if (success) {
          message.success('编辑成功');
        } else {
          message.error('编辑失败');
        }
        return success;
      }
    },

    *queryNetifHistogram({ payload }, { call, put }) {
      const { success, result = [] } = yield call(queryMetricAnalysysHistogram, {
        metricApi: EMetricApiType.netif,
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
