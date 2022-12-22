import { model } from '@/utils/frame/model';
import { message } from 'antd';
import type { Effect } from 'dva';
import modelExtend from 'dva-model-extend';
import type { IAlarmAndLogSyslog } from './typings';
import { querySystemSyslogSettings, updateSystemSyslogSettings } from './service';

export interface SystemSyslogModelState {
  systemSyslogSettings: IAlarmAndLogSyslog;
}

export interface SystemSyslogModelType {
  namespace: string;
  state: SystemSyslogModelState;
  effects: {
    querySystemSyslogSettings: Effect;
    updateSystemSyslogSettings: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'systemSyslogModel',
  state: {
    // 告警
    systemSyslogSettings: {} as IAlarmAndLogSyslog,
  },
  reducers: {},
  effects: {
    *querySystemSyslogSettings({ payload }, { call, put }) {
      const { success, result } = yield call(querySystemSyslogSettings, payload);
      yield put({
        type: 'updateState',
        payload: {
          systemSyslogSettings: success ? result : {},
        },
      });
    },

    *updateSystemSyslogSettings({ payload }, { call, put }) {
      const response = yield call(updateSystemSyslogSettings, payload);
      const { success } = response;
      if (success) {
        message.success('保存成功');
        yield put({
          type: 'queryAlertSyslogSettings',
        });
      } else {
        message.error('保存失败');
      }
      return success;
    },
  },
} as SystemSyslogModelType);
