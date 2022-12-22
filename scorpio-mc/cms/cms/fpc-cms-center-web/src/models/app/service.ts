import {
  createService,
  deleteService,
  importService,
  queryAllServices,
  queryServiceDetail,
  queryServiceFollows,
  updateService,
  updateServiceFollow,
} from '@/pages/app/Configuration/Service/service';
import type {
  IFollowService,
  IFollowServiceParams,
  IService,
  IServiceMap,
} from '@/pages/app/Configuration/Service/typings';
import { EServiceFollowState } from '@/pages/app/Configuration/Service/typings';
import { queryServiceLink, updateServiceLink } from '@/services/app/serviceLink';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';

export interface ServiceModelState {
  allServices: IService[];
  allServiceMap: IServiceMap;
  serviceDetail: IService;

  /** 关注的业务列表 */
  followServices: IFollowService[];
}

export interface ServiceModelType {
  namespace: string;
  state: ServiceModelState;
  effects: {
    queryAllServices: Effect;

    queryServiceDetail: Effect;
    createService: Effect;
    updateService: Effect;
    deleteService: Effect;
    importService: Effect;

    /** 查询用户关注的业务 */
    queryServiceFollows: Effect;
    /** 关注（取消关注）业务 */
    updateServiceFollow: Effect;

    /** 更新业务路径 */
    updateServiceLink: Effect;
    /** 获取业务路径 */
    queryServiceLink: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'serviceModel',
  state: {
    allServices: [],
    allServiceMap: {},
    serviceDetail: <IService>{},
    followServices: [],
  },
  effects: {
    *queryAllServices({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllServices, payload);
      const allServices: IService[] = success ? result : [];
      const allServiceMap = {};
      allServices.forEach((item) => {
        allServiceMap[item.id] = item;
      });

      yield put({
        type: 'updateState',
        payload: {
          allServices,
          allServiceMap,
        },
      });
    },
    *queryServiceDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryServiceDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          serviceDetail: success ? result : {},
        },
      });
    },
    *createService({ payload }, { call, put }) {
      const { success } = yield call(createService, payload);
      if (success) {
        message.success('添加成功');
        yield put({
          type: 'queryAllServices',
        });
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateService({ payload }, { call, put }) {
      const { success } = yield call(updateService, payload);
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'queryAllServices',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteService({ payload }, { call, put }) {
      const { success } = yield call(deleteService, payload);
      if (success) {
        message.success('删除成功');
        yield put({
          type: 'queryAllServices',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *importService({ payload }, { call, put }) {
      const { success } = yield call(importService, payload);
      if (success) {
        message.success('导入成功');
        yield put({
          type: 'queryAllServices',
        });
      } else {
        message.error('导入失败');
      }
      return success;
    },

    *queryServiceFollows({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryServiceFollows, payload);
      yield put({
        type: 'updateState',
        payload: {
          followServices: success ? result : [],
        },
      });
    },
    *updateServiceFollow({ payload = <IFollowServiceParams>{} }, { call }) {
      const { state } = payload;
      const stateText = state === EServiceFollowState.FOLLOW ? '关注' : '取消关注';
      const { success } = yield call(updateServiceFollow, payload);
      if (success) {
        message.success(`${stateText}成功`);
      } else {
        message.error(`${stateText}失败`);
      }
      return success;
    },
    *updateServiceLink({ payload }, { call }) {
      const { success } = yield call(updateServiceLink, payload);
      if (success) {
        message.success(`更新业务路径成功`);
      } else {
        message.error(`更新业务路径失败`);
      }
      return success;
    },
    *queryServiceLink({ payload }, { call }) {
      const { success, result } = yield call(queryServiceLink, payload);
      return success ? result : {};
    },
  },
} as ServiceModelType);
