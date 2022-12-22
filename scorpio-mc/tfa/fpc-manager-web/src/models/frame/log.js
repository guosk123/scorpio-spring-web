import modelExtend from 'dva-model-extend';
import { pageModel, doPageRequest } from '@/utils/frame/model';
import { queryLogs } from '@/services/frame/log';

export default modelExtend(pageModel, {
  namespace: 'logModel',

  state: {
    logs: [],
    backupLogs: [],
  },

  effects: {
    *queryLogs({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: queryLogs, payload, call, put, stateKey: 'logs' });
    },
    *queryBackupLogs({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: queryLogs, payload, call, put, stateKey: 'backupLogs' });
    },
  },
});
