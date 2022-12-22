import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import { queryCmsSettings, updateCmsSettings } from './service';
import type { Effect } from 'umi';

export interface StateType {
  settings: any;
}

export interface CmsModelType {
  namespace: string;
  state: StateType;
  effects: {
    queryCmsSettings: Effect;
    updateCmsSettings: Effect;
  };
  reducers: {};
}

export default modelExtend(model, {
  namespace: 'cmsModel',
  state: {
    settings: {},
  },
  reducers: {},
  effects: {
    *queryCmsSettings(_, { call, put }) {
      const { success, result } = yield call(queryCmsSettings);
      yield put({
        type: 'updateState',
        payload: {
          settings: success ? result : {},
        },
      });
    },
    *updateCmsSettings({ payload }, { call, put }) {
      const { success } = yield call(updateCmsSettings, payload);
      if (!success) {
        message.error('配置失败');
      } else {
        yield put({
          type: 'queryCmsSettings',
        });
        message.success('配置成功');
      }
      return success;
    },
  },
} as CmsModelType);
