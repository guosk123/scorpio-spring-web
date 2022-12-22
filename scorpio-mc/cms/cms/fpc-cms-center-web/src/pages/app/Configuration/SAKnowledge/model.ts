import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import {
  createCustomSA,
  updateCustomSA,
  deleteCustomSA,
  queryCustomSADetail,
  importCustomSA,
} from './service';
import type { Effect } from 'umi';
import { message } from 'antd';
import type { AppCategoryItem, ApplicationItem, AppSubCategoryItem } from './typings';
import { ECustomSAApiType } from './typings';

export interface CustomSAModelState {
  /**
   * 自定义应用详情
   */
  customApplicationDetail: ApplicationItem;
  /**
   * 自定义子分类详情
   */
  customSubCategoryDetail: AppSubCategoryItem;
  /**
   * 自定义分类详情
   */
  customCategoryDetail: AppCategoryItem;
}

export interface CustomSAModelType {
  namespace: string;
  state: CustomSAModelState;
  effects: {
    queryCustomSADetail: Effect;
    createCustomSA: Effect;
    updateCustomSA: Effect;
    deleteCustomSA: Effect;
    importCustomSA: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'customSAModel',
  state: {
    customCategoryDetail: {} as AppCategoryItem,
    customSubCategoryDetail: {} as AppSubCategoryItem,
    customApplicationDetail: {} as ApplicationItem,
  },
  reducers: {},
  effects: {
    *queryCustomSADetail({ payload }, { call, put }) {
      const { type } = payload;
      const { success, result } = yield call(queryCustomSADetail, payload);

      let stateKey = '';
      if (type === ECustomSAApiType.CATEGORY) {
        stateKey = 'customCategoryDetail';
      }
      if (type === ECustomSAApiType.SUB_CATEGORY) {
        stateKey = 'customSubCategoryDetail';
      }
      if (type === ECustomSAApiType.APPLICATION) {
        stateKey = 'customApplicationDetail';
      }

      if (stateKey) {
        yield put({
          type: 'updateState',
          payload: {
            [stateKey]: success ? result : {},
          },
        });
      }
    },
    *createCustomSA({ payload }, { call, put }) {
      const { success } = yield call(createCustomSA, payload);
      if (success) {
        message.success('添加成功');
        // 重新拉取SA规则库内容
        yield put({
          type: 'SAKnowledgeModel/queryAllApplications',
        });
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateCustomSA({ payload }, { call, put }) {
      const {
        data: { id },
        type,
      } = payload;
      const { success } = yield call(updateCustomSA, payload);
      if (success) {
        message.success('编辑成功');
        // 重新拉取SA规则库内容
        yield put({
          type: 'SAKnowledgeModel/queryAllApplications',
        });
        yield put({
          type: 'queryCustomSADetail',
          payload: {
            id,
            type,
          },
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteCustomSA({ payload }, { call, put }) {
      const { success } = yield call(deleteCustomSA, payload);
      if (success) {
        message.success('删除成功');
        // 重新拉取SA规则库内容
        yield put({
          type: 'SAKnowledgeModel/queryAllApplications',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *importCustomSA({ payload }, { call, put }) {
      const { success } = yield call(importCustomSA, payload);
      if (success) {
        message.success('导入成功');
        // 重新拉取SA规则库内容
        yield put({
          type: 'SAKnowledgeModel/queryAllApplications',
        });
      } else {
        message.error('导入失败');
      }
      return success;
    },
  },
} as CustomSAModelType);
