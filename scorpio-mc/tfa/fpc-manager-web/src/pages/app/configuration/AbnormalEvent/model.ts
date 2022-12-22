import { pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import type { Effect } from 'dva';
import modelExtend from 'dva-model-extend';
import {
  createAbnormalEventRule,
  deleteAbnormalEventRule,
  disableAbnormalEventRule,
  enableAbnormalEventRule,
  queryAbnormalEventRuleDetail,
  updateAbnormalEventRule,
  importAbnormalEventRule,
  importThreatIntelligenceRule,
} from './service';
import type { IAbnormalEventRule} from './typings';

export interface AbnormalEventModelState {
  abnormalEventRuleDetail: IAbnormalEventRule;
}

export interface AbnormalEventModelType {
  namespace: string;
  state: AbnormalEventModelState;
  effects: {
    // queryAbnormalEventMessages: Effect;
    // queryAbnormalEventMessageDetail: Effect;

    queryAbnormalEventRuleDetail: Effect;
    createAbnormalEventRule: Effect;
    updateAbnormalEventRule: Effect;
    enableAbnormalEventRule: Effect;
    disableAbnormalEventRule: Effect;
    deleteAbnormalEventRule: Effect;
    importAbnormalEventRule: Effect;
    /** 导入威胁情报 */
    importThreatIntelligenceRule: Effect;
  };
}

export default modelExtend(pageModel, {
  namespace: 'abnormalEventModel',
  state: {
    abnormalEventRuleDetail: {} as IAbnormalEventRule,
  },
  reducers: {},
  effects: {
    *queryAbnormalEventRuleDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryAbnormalEventRuleDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          abnormalEventRuleDetail: success ? result : {},
        },
      });
    },

    *createAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(createAbnormalEventRule, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(updateAbnormalEventRule, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *enableAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(enableAbnormalEventRule, payload);
      if (success) {
        message.success('已启用');
      } else {
        message.error('启用失败');
      }
      return success;
    },
    *disableAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(disableAbnormalEventRule, payload);
      if (success) {
        message.success('已停用');
      } else {
        message.error('停用失败');
      }
      return success;
    },
    *deleteAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(deleteAbnormalEventRule, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *importAbnormalEventRule({ payload }, { call }) {
      const { success } = yield call(importAbnormalEventRule, payload);
      if (success) {
        message.success('导入成功');
      } else {
        message.error('导入失败');
      }
      return success;
    },
    *importThreatIntelligenceRule({ payload }, { call }) {
      const { success } = yield call(importThreatIntelligenceRule, payload);
      if (success) {
        message.success('导入成功');
      } else {
        message.error('导入失败');
      }
      return success;
    },
  },
} as AbnormalEventModelType);
