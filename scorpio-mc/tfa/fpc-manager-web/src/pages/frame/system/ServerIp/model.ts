import { message, Modal } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { model } from '@/utils/frame/model';
import { querySystemServerIpSettings, updateSystemServerIpSettings } from './service';
import type { IServerIpSettings } from './typings';

export interface IModelState {
  serverIpSettings: IServerIpSettings;
}
export interface IServerIpSettingModel {
  namespace: string;
  state: IModelState;
  effects: {
    querySystemServerIpSettings: Effect;
    updateSystemServerIpSettings: Effect;
  };
}

const serverIpSettingModel = modelExtend(model, {
  namespace: 'serverIpSettingModel',
  state: {
    serverIpSettings: {} as IServerIpSettings,
  },
  reducers: {},
  effects: {
    *querySystemServerIpSettings({ payload }, { call, put }) {
      const { success, result } = yield call(querySystemServerIpSettings, payload);
      yield put({
        type: 'updateState',
        payload: {
          serverIpSettings: success ? result : {},
        },
      });
    },
    *updateSystemServerIpSettings({ payload }, { call, put }) {
      const { success } = yield call(updateSystemServerIpSettings, payload);
      if (success) {
        Modal.success({
          title: '保存成功',
          content: '3秒后将重启服务...',
        });
        yield put({
          type: 'serverIpSettingModel/querySystemServerIpSettings',
        });
      } else {
        message.error('保存失败');
      }
    },
  },
} as IServerIpSettingModel);

export default serverIpSettingModel;
