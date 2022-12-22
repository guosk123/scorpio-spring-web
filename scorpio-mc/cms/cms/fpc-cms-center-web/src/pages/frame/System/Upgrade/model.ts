import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { queryUpgradeInfos, queryUpgradeLogs, uploadUpgrade } from './service';
import type { EUpgradeState, IUpgradeInfo, IUpgradeLog } from './typings';

export interface ISystemUpgradeModelState {
  upgradeInfos: IUpgradeInfo;
  upgradeLog?: string;
  upgrageState?: EUpgradeState;
}
export interface ISystemUpgradeModel {
  namespace: string;
  state: ISystemUpgradeModelState;
  effects: {
    uploadUpgrade: Effect;
    queryUpgradeLogs: Effect;
    queryUpgradeInfos: Effect;
  };
}

const systemUpgradeModel = modelExtend(model, {
  namespace: 'systemUpgradeModel',
  state: {
    upgradeInfos: {} as IUpgradeInfo,
    upgradeLog: '',
    upgrageState: undefined,
  },
  effects: {
    *uploadUpgrade({ payload }, { call }) {
      const { success } = yield call(uploadUpgrade, payload);
      if (success) {
        message.success('升级包上传成功，升级中...');
      } else {
        message.error('升级包上传失败');
      }
      return success;
    },
    *queryUpgradeLogs({ payload }, { call, put, select }) {
      const { cursor = 0 } = payload;
      // 取当前的老日志
      const { upgradeLog: oldLog } = yield select((state: any) => state.systemUpgradeModel);
      const { success, result } = yield call(queryUpgradeLogs, payload);
      if (success) {
        const { logs = [], cursor: nextCursor = 0, state } = result as IUpgradeLog;
        const logText = logs.length > 0 ? logs.join('\n') : '';

        let nextLogs = oldLog;
        if (cursor === 0) {
          nextLogs = logText;
        } else {
          nextLogs = logText ? oldLog + '\n' + logText : oldLog;
        }

        yield put({
          type: 'updateState',
          payload: {
            // 成功时，追加日志
            upgradeLog: nextLogs,
            upgrageState: state,
          },
        });
        return {
          success,
          state,
          nextCursor: nextCursor,
        };
      }

      return {
        success: false,
      };
    },
    *queryUpgradeInfos({ payload }, { call, put }) {
      const { success, result } = yield call(queryUpgradeInfos, payload);
      yield put({
        type: 'updateState',
        payload: {
          upgradeInfos: success ? result : {},
        },
      });
    },
  },
} as ISystemUpgradeModel);

export default systemUpgradeModel;
