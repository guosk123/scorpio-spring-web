import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import type { Effect } from 'umi';
import type { ISystemAlertRule, ISystemAlertRuleResponse } from './typings';
import {
  querySystemAlertRuleDetail,
  querySystemAlertRules,
  updateSystemAlertRule,
  updateSystemAlertRuleState,
} from './service';
import { parseArrayJson } from '@/utils/utils';
import { message } from 'antd';

export interface SystemAlertModelState {
  systemAlertRuleList: ISystemAlertRuleResponse[];
  systemAlertRuleDetail: ISystemAlertRule;
}

export interface SystemAlertType {
  namespace: string;
  state: SystemAlertModelState;
  effects: {
    querySystemAlertRules: Effect;
    querySystemAlertRuleDetail: Effect;
    updateSystemAlertRule: Effect;
    updateSystemAlertRuleState: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'systemAlertModel',
  state: {
    systemAlertRuleList: [],
    systemAlertRuleDetail: {} as ISystemAlertRule,
  },

  effects: {
    *querySystemAlertRules(_, { call, put }) {
      const { success, result } = yield call(querySystemAlertRules);
      yield put({
        type: 'updateState',
        payload: {
          systemAlertRuleList: success ? result : [],
        },
      });
    },
    *querySystemAlertRuleDetail({ payload }, { call, put }) {
      const { success, result }: { success: boolean; result: ISystemAlertRuleResponse } =
        yield call(querySystemAlertRuleDetail, payload);
      let detail = {} as ISystemAlertRule;
      if (success) {
        const fireCriteria = parseArrayJson(result.fireCriteria);
        detail = {
          ...result,
          fireCriteria,
        };
      }
      yield put({
        type: 'updateState',
        payload: {
          systemAlertRuleDetail: detail,
        },
      });
    },
    *updateSystemAlertRule({ payload }, { call, put }) {
      const { id } = payload;
      const response = yield call(updateSystemAlertRule, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'querySystemAlertRuleDetail',
          payload: {
            id,
          },
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *updateSystemAlertRuleState({ payload }, { call }) {
      const response = yield call(updateSystemAlertRuleState, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
  },
} as SystemAlertType);
