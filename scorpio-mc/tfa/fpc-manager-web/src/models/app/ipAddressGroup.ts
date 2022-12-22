import type {
  IpAddressGroup,
  IpAddressGroupMap,
} from '@/pages/app/configuration/IpAddressGroup/typings';
import {
  createIpAddressGroup,
  deleteIpAddressGroup,
  importIpAddressGroups,
  queryAllIpAddressGroup,
  queryIpAddressGroupDetail,
  updateIpAddressGroup,
} from '@/services/app/ipAddressGroup';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';

export interface IIpAddressGroupModelState {
  allIpAddressGroupList: IpAddressGroup[];
  allIpAddressGroupMap: IpAddressGroupMap;
  ipAddressGroupDetail: IpAddressGroup;
}

export interface IIpAddressGroupModelType {
  namespace: string;
  state: IIpAddressGroupModelState;
  effects: {
    queryAllIpAddressGroup: Effect;
    queryIpAddressGroupDetail: Effect;
    createIpAddressGroup: Effect;
    updateIpAddressGroup: Effect;
    deleteIpAddressGroup: Effect;
    importIpAddressGroup: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'ipAddressGroupModel',
  state: {
    allIpAddressGroupList: [],
    allIpAddressGroupMap: {},
    ipAddressGroupDetail: {} as IpAddressGroup,
  },
  effects: {
    *queryAllIpAddressGroup({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryAllIpAddressGroup, payload);
      const allIpAddressGroupList: IpAddressGroup[] = success ? result : [];
      const allIpAddressGroupMap = {};
      allIpAddressGroupList.forEach((item) => {
        allIpAddressGroupMap[item.id] = item;
      });
      yield put({
        type: 'updateState',
        payload: {
          allIpAddressGroupList,
          allIpAddressGroupMap,
        },
      });
    },

    *importIpAddressGroup({ payload }, { call, put }) {
      const { success } = yield call(importIpAddressGroups, payload);
      if (success) {
        message.success('导入成功');
        yield put({
          type: 'queryAllIpAddressGroup',
        });
      } else {
        message.error('导入失败');
      }
      return success;
    },

    *queryIpAddressGroupDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryIpAddressGroupDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          ipAddressGroupDetail: success ? result : {},
        },
      });
    },

    *createIpAddressGroup({ payload }, { call, put }) {
      const response = yield call(createIpAddressGroup, payload);
      const { success } = response;
      if (success) {
        message.success('添加成功');
        yield put({
          type: 'queryAllIpAddressGroup',
        });
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateIpAddressGroup({ payload }, { call, put }) {
      const response = yield call(updateIpAddressGroup, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'queryAllIpAddressGroup',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteIpAddressGroup({ payload }, { call, put }) {
      const response = yield call(deleteIpAddressGroup, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
        yield put({
          type: 'queryAllIpAddressGroup',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
} as IIpAddressGroupModelType);
