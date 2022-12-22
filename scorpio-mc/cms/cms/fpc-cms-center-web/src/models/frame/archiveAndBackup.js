import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import {
  queryArchiveSettings,
  updateArchiveSettings,
  queryBackupSettings,
  updateBackupSettings,
} from '@/services/frame/archiveAndBackup';

export default modelExtend(model, {
  namespace: 'archiveAndBackupModel',
  state: {
    archiveSettings: {},
    backupSettings: {},
  },
  reducers: {},
  effects: {
    *queryArchiveSettings(_, { call, put }) {
      const { success, result } = yield call(queryArchiveSettings);
      yield put({
        type: 'updateState',
        payload: {
          archiveSettings: success ? result : {},
        },
      });
    },
    *updateArchiveSettings({ payload }, { call, put }) {
      const { success } = yield call(updateArchiveSettings, payload);
      if (!success) {
        message.error('设置失败');
      } else {
        yield put({
          type: 'queryArchiveSettings',
        });
        message.success('设置成功');
      }
      return success;
    },
    *queryBackupSettings(_, { call, put }) {
      const { success, result } = yield call(queryBackupSettings);
      yield put({
        type: 'updateState',
        payload: {
          backupSettings: success ? result : {},
        },
      });
    },
    *updateBackupSettings({ payload }, { call, put }) {
      const { success } = yield call(updateBackupSettings, payload);
      if (!success) {
        message.error('设置失败');
      } else {
        yield put({
          type: 'queryBackupSettings',
        });
        message.success('设置成功');
      }
      return success;
    },
  },
});
