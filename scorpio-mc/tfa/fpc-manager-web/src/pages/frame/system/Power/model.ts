import { model } from '@/utils/frame/model';
import { message } from 'antd';
import type { Effect } from 'umi';
import modelExtend from 'dva-model-extend';
import { powerReboot, powerShutdown } from './service';

export interface SystemPowerModelState {}

export interface SystemPowerModelType {
  namespace: string;
  state: SystemPowerModelState;
  effects: {
    powerReboot: Effect;
    powerShutdown: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'systemPowerModel',
  state: {},
  reducers: {},
  effects: {
    *powerReboot(_, { call }) {
      const { success } = yield call(powerReboot);
      if (success) {
        message.success('重启成功');
      } else {
        message.error('重启失败');
      }
      return success;
    },

    *powerShutdown(_, { call }) {
      const { success } = yield call(powerShutdown);
      if (success) {
        message.success('关机成功');
      } else {
        message.error('关机失败');
      }
      return success;
    },
  },
} as SystemPowerModelType);
