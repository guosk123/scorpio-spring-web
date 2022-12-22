import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { message } from 'antd';
import type { Effect } from 'umi';
import {
  createLowerCMS,
  createSensor,
  createUpperCMSSetting,
  deleteLowerCMS,
  deleteSensorItem,
  deleteSensorUpgradeItem,
  queryLowerCMSList,
  querySensorList,
  querySensorUpgradeList,
  queryUpperCMSSetting,
} from './service';

interface IConfigurationModel {
  namespace: string;
  state: any;
  effects: {
    queryLowerCMSList: Effect;
    deleteLowerCMS: Effect;
    querySensorList: Effect;
    deleteSensorItem: Effect;
    querySensorUpgradeList: Effect;
    deleteSensorUpgradeItem: Effect;
    queryUpperCMSSetting: Effect;
    createSensor: Effect;
    createLowerCMS: Effect;
    createUpperCMSSetting: Effect;
  };
}

const ConfigurationModel: IConfigurationModel = {
  namespace: 'ConfigurationModel',
  state: {},
  effects: {
    *queryLowerCMSList({ payload = {} }, { call }) {
      const { success, result } = yield call(queryLowerCMSList, payload);
      return success ? result : [];
    },
    *deleteLowerCMS({ payload = {} }, { call }) {
      const { success } = yield call(deleteLowerCMS, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *querySensorList({ payload = {} }, { call }) {
      const { success, result } = yield call(querySensorList, payload);
      return success ? result : [];
    },
    *deleteSensorItem({ payload = {} }, { call }) {
      const { success } = yield call(deleteSensorItem, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *querySensorUpgradeList({ payload = {} }, { call }) {
      const { success, result } = yield call(querySensorUpgradeList, payload);
      return success ? result : [];
    },
    *deleteSensorUpgradeItem({ payload = {} }, { call }) {
      const { success } = yield call(deleteSensorUpgradeItem, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *queryUpperCMSSetting({ payload = {} }, { call }) {
      const { success, result } = yield call(queryUpperCMSSetting, payload);
      return success ? result : [];
    },
    *createSensor({ payload = {} }, { call }) {
      const { success } = yield call(createSensor, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *createLowerCMS({ payload = {} }, { call }) {
      const { success } = yield call(createLowerCMS, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *createUpperCMSSetting({ payload = {} }, { call }) {
      const tmpPayload = {
        ...payload,
        state: payload.state ? BOOL_YES : BOOL_NO,
      };

      const { success } = yield call(createUpperCMSSetting, tmpPayload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
  },
};
export default ConfigurationModel;
