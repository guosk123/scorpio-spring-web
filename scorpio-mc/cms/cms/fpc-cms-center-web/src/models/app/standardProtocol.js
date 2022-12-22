import {
  createStandardProtocol,
  deleteStandardProtocol,
  queryAllStandardProtocols,
  queryStandardProtocolDetail,
  updateStandardProtocol,
} from '@/pages/app/Configuration/StandardProtocol/service';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';

export default modelExtend(model, {
  namespace: 'standardProtocolModel',
  state: {
    standardProtocolList: [],
    allStandardProtocols: [],
    standardProtocolDetail: {},
  },
  effects: {
    *queryAllStandardProtocols({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllStandardProtocols, payload);
      const allStandardProtocols = success && Array.isArray(result) ? result : [];
      yield put({
        type: 'updateState',
        payload: {
          allStandardProtocols,
        },
      });

      return allStandardProtocols;
    },

    *queryStandardProtocolDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryStandardProtocolDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          standardProtocolDetail: success ? result : {},
        },
      });
    },

    *createStandardProtocol({ payload }, { call }) {
      const { success } = yield call(createStandardProtocol, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateStandardProtocol({ payload }, { call }) {
      const { success } = yield call(updateStandardProtocol, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteStandardProtocol({ payload }, { call }) {
      const { success } = yield call(deleteStandardProtocol, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
});
