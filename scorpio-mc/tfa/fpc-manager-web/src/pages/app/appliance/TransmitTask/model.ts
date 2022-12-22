import { pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import * as transmitTaskService from './service';
import type { ITransmitTask } from './typings';

export interface ITransmitTaskModelState {
  detail: ITransmitTask;
}

export interface ITransmitTaskModelType {
  namespace: string;
  state: ITransmitTaskModelState;
  effects: {
    queryTransmitTasksDetail: Effect;
    clearTransmitTasksDetail: Effect;
    createTransmitTask: Effect;
    updateTransmitTask: Effect;
    /** 删除任务 */
    deleteTransmitTask: Effect;
    /** 下载任务的PCAP文件 */
    downloadTransmitTaskFile: Effect;
    /** 停止任务 */
    stopTransmitTask: Effect;
    /** 重新开始执行任务 */
    restartTransmitTask: Effect;
  };
}

export default modelExtend(pageModel, {
  namespace: 'transmitTaskModel',
  state: {
    detail: <ITransmitTask>{},
  },
  effects: {
    *queryTransmitTasksDetail({ payload: { id } }, { call, put }) {
      const { success, result } = yield call(transmitTaskService.queryTransmitTasksDetail, { id });
      let detail = {} as ITransmitTask;
      if (success) {
        detail = result;
      }
      yield put({
        type: 'updateState',
        payload: {
          detail,
        },
      });

      return detail;
    },
    *clearTransmitTasksDetail(_, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          detail: <ITransmitTask>{},
        },
      });
    },

    *createTransmitTask({ payload }, { call }) {
      const { success } = yield call(transmitTaskService.createTransmitTask, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateTransmitTask({ payload }, { call }) {
      const { success } = yield call(transmitTaskService.updateTransmitTask, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteTransmitTask({ payload: { id } }, { call }) {
      const { success } = yield call(transmitTaskService.deleteTransmitTask, { id });
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *downloadTransmitTaskFile({ payload: { id } }, { call }) {
      return yield call(transmitTaskService.downloadTransmitTaskFile, { id });
    },
    *stopTransmitTask({ payload: { id } }, { call }) {
      const { success } = yield call(transmitTaskService.stopTransmitTask, { id });
      if (success) {
        message.success('操作成功');
      } else {
        message.error('操作失败');
      }
      return success;
    },
    *restartTransmitTask({ payload: { id } }, { call }) {
      const { success } = yield call(transmitTaskService.restartTransmitTask, { id });
      if (success) {
        message.success('操作成功');
      } else {
        message.error('操作失败');
      }
      return success;
    },
  },
} as ITransmitTaskModelType);
