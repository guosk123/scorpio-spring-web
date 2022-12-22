import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { querySuricataMitreAttack, querySuricataRuleClasstype } from '../../security/service';
import type { IMitreAttack, IRuleClasstype } from '../../security/typings';
import {
  importSuricataRule,
  importThreatIntelligenceRule,
  importSuricataClasstype,
} from './service';

export interface ISuricataModelState {
  mitreAttackList: IMitreAttack[];
  classtypes: IRuleClasstype[];
  mitreDict: Record<string, IMitreAttack>;
  classtypeDict: Record<string, IRuleClasstype>;
}

export interface SuricataModelType {
  namespace: string;
  state: ISuricataModelState;
  effects: {
    querySuricataMitreAttack: Effect;
    querySuricataRuleClasstype: Effect;
    importSuricataRule: Effect;
    importSuricataClasstype: Effect;
    importThreatIntelligenceRule: Effect;
  };
}

const suricataModel: SuricataModelType = {
  namespace: 'suricataModel',
  state: {
    mitreAttackList: [],
    classtypes: [],
    mitreDict: {},
    classtypeDict: {},
  },
  effects: {
    *querySuricataMitreAttack({ payload }, { call, put }) {
      const { success, result } = yield call(querySuricataMitreAttack, payload);

      if (!success) return;

      // 构造mitre字典
      const dict = (result as IMitreAttack[]).reduce((total, current) => {
        return {
          ...total,
          [current.id]: current,
        };
      }, {});

      yield put({
        type: 'updateState',
        payload: {
          mitreAttackList: result,
          mitreDict: dict,
        },
      });
    },
    *querySuricataRuleClasstype({ payload }, { call, put }) {
      const { success, result } = yield call(querySuricataRuleClasstype, payload);
      if (!success) return;

      const dict = (result as IRuleClasstype[]).reduce((total, current) => {
        return {
          ...total,
          [current.id]: current,
        };
      }, {});

      yield put({
        type: 'updateState',
        payload: {
          classtypes: success ? result : [],
          classtypeDict: dict,
        },
      });
      return {
        result,
      };
    },
    *importSuricataRule({ payload }, { call }) {
      const { success } = yield call(importSuricataRule, payload);
      if (success) {
        message.success('导入成功');
      } else {
        message.error('导入失败');
      }
      return success;
    },
    *importSuricataClasstype({ payload }, { call }) {
      const { success } = yield call(importSuricataClasstype, payload);
      if (success) {
        message.success('导入成功');
      } else {
        message.error('导入失败');
      }
      return success;
    },
    *importThreatIntelligenceRule({ payload }: any, { call }: any) {
      const { success } = yield call(importThreatIntelligenceRule, payload);
      if (success) {
        message.success('导入成功');
      } else {
        message.error('导入失败');
      }
      return success;
    },
  },
};

export default modelExtend(model, suricataModel);
