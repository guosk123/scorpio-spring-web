import { model } from '@/utils/frame/model';
import { parseObjJson } from '@/utils/utils';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import {
  createAlertRule,
  deleteAlertRule,
  disableAlertRule,
  disposeAlertRule,
  enableAlertRule,
  queryAlertAnalysisDetail,
  queryAlertMessageDetail,
  queryAlertRuleDetail,
  queryAlertSyslogSettings,
  queryAllAlertRules,
  updateAlertRule,
  updateAlertSyslogSettings,
} from './service';
import type {
  IAdvancedAlertRules,
  IAlertMessage,
  IAlertRefire,
  IAlertRule,
  IAlertSyslog,
  IThresholdAlertRules,
  ITrendAlertRules,
} from './typings';
import { EAlertCategory } from './typings';

export interface AlertModelState {
  alertMessageDetail: IAlertMessage;

  allAlertRule: IAlertRule[];
  alertRuleDetail: IAlertRule;

  alertSyslogSettings: IAlertSyslog;
}

export interface AlertModelType {
  namespace: string;
  state: AlertModelState;
  effects: {
    queryAlertMessageDetail: Effect;

    queryAllAlertRules: Effect;
    queryAlertRuleDetail: Effect;
    createAlertRule: Effect;
    updateAlertRule: Effect;
    enableAlertRule: Effect;
    disableAlertRule: Effect;
    deleteAlertRule: Effect;

    queryAlertSyslogSettings: Effect;
    updateAlertSyslogSettings: Effect;
    disposeAlertRule: Effect;
    queryAlertAnalysisDetail: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'alertModel',
  state: {
    // 告警
    alertMessageDetail: {} as IAlertMessage,

    // 告警设置
    allAlertRule: [] as IAlertRule[],
    alertRuleDetail: {} as IAlertRule,

    // 告警外发
    alertSyslogSettings: {} as IAlertSyslog,
  },
  reducers: {},
  effects: {
    *queryAlertMessageDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryAlertMessageDetail, payload);
      // 解析
      let detail = {} as IAlertMessage;
      if (success) {
        detail = {
          ...result,
          alertDefine: parseObjJson(result.alertDefine),
          components: parseObjJson(result.components),
        };
      }

      yield put({
        type: 'updateState',
        payload: {
          alertMessageDetail: detail,
        },
      });
      return detail;
    },

    *queryAllAlertRules({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllAlertRules, payload);
      yield put({
        type: 'updateState',
        payload: {
          allAlertRule: success ? result : [],
        },
      });
      return result;
    },

    *createAlertRule({ payload }, { call }) {
      const { success } = yield call(createAlertRule, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateAlertRule({ payload }, { call }) {
      const { success } = yield call(updateAlertRule, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *enableAlertRule({ payload }, { call }) {
      const { success } = yield call(enableAlertRule, payload);
      if (success) {
        message.success('已启用');
      } else {
        message.error('启用失败');
      }
      return success;
    },
    *disableAlertRule({ payload }, { call }) {
      const { success } = yield call(disableAlertRule, payload);
      if (success) {
        message.success('已停用');
      } else {
        message.error('停用失败');
      }
      return success;
    },
    *deleteAlertRule({ payload }, { call }) {
      const { success } = yield call(deleteAlertRule, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *queryAlertRuleDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryAlertRuleDetail, payload);

      // 解析
      let detail = {} as IAlertRule;
      if (success) {
        const thresholdSettings = parseObjJson(result.thresholdSettings) as IThresholdAlertRules;
        const trendSettings = parseObjJson(result.trendSettings) as ITrendAlertRules;
        const advancedSettings = parseObjJson(result.advancedSettings) as IAdvancedAlertRules;
        const refire = parseObjJson(result.refire) as IAlertRefire;

        detail = {
          ...result,
          thresholdSettings,
          trendSettings,
          advancedSettings,
          refire,
        };

        const { category } = result as IAlertRule;
        if (category === EAlertCategory.THRESHOLD) {
          detail.source = thresholdSettings.source;
          detail.fireCriteria = thresholdSettings.fireCriteria;
        }
        if (category === EAlertCategory.TREND) {
          detail.source = trendSettings.source;
          detail.fireCriteria = trendSettings.fireCriteria;
          detail.trendDefine = trendSettings.trend;
        }
      }
      yield put({
        type: 'updateState',
        payload: {
          alertRuleDetail: detail,
        },
      });
      return detail;
    },

    *queryAlertSyslogSettings({ payload }, { call, put }) {
      const { success, result } = yield call(queryAlertSyslogSettings, payload);
      yield put({
        type: 'updateState',
        payload: {
          alertSyslogSettings: success ? result : {},
        },
      });
    },

    *updateAlertSyslogSettings({ payload }, { call, put }) {
      const { success } = yield call(updateAlertSyslogSettings, payload);
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
    *disposeAlertRule({ payload }, { call }) {
      const { success } = yield call(disposeAlertRule, payload);
      if (success) {
        message.success('成功');
      } else {
        message.error('失败');
      }
      return success;
    },
    *queryAlertAnalysisDetail({ payload }, { call }) {
      const { success, result } = yield call(queryAlertAnalysisDetail, payload);
      if (success) {
        return result;
      }
      return [];
    },
  },
} as AlertModelType);
