import { bpfRuleValid } from '@/services/app/bpf';
import type { Effect } from 'umi';

export type IBpfValidModelState = Record<string, any>;
export interface BpfValidModelType {
  namespace: string;
  state: IBpfValidModelState;
  effects: {
    /** 获取菜单的前置数据 */
    bpfRuleValid: Effect;
  };
}

export default {
  namespace: 'bpfValidModel',
  state: {},
  effects: {
    *bpfRuleValid({ payload }, { call }) {
      const response = yield call(bpfRuleValid, payload);
      return response;
    },
  },
} as BpfValidModelType;
