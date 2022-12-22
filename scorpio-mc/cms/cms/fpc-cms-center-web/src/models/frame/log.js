import modelExtend from 'dva-model-extend';
import { querySystemLogs } from '@/services/frame/systemLog';
import { pageModel, doPageRequest } from '@/utils/frame/model';

export default modelExtend(pageModel, {
  namespace: 'logModel',

  state: {
    logs: [],
    backupLogs: [],
  },

  effects: {
    *querySystemLogs({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: querySystemLogs, payload, call, put, stateKey: 'logs' });
    },
    *queryBackupLogs({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: querySystemLogs, payload, call, put, stateKey: 'backupLogs' });
    },
  },
});
