import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { pageModel, doPageRequest } from '@/utils/frame/model';
import {
  queryScenarioTasks,
  queryScenarioTaskDetail,
  createScenarioTask,
  updateScenarioTask,
  deleteScenarioTask,
  queryScenarioTaskResults,
  queryScenarioTaskDynamicDomainTerms,
  queryAllScenarioCustomTemplates,
  queryScenarioCustomTemplateDetail,
  createScenarioCustomTemplate,
  deleteScenarioCustomTemplate,
} from '@/pages/app/appliance/ScenarioTask/service';
import type {
  IScenarioTask,
  IScenarioCustomTemplate,
  ScenarioTaskResult,
} from '@/pages/app/appliance/ScenarioTask/typings';

export interface IScenarioTaskModelState {
  scenarioTaskList: IScenarioTask[];
  scenarioTaskDetail: IScenarioTask;
  scenarioTaskResult: ScenarioTaskResult;

  scenarioCustomTemplateList: IScenarioCustomTemplate[];
  scenarioCustomTemplateDetail: IScenarioCustomTemplate;

  selectedResultRecord: Record<string, any>;
}

export interface IModelType {
  namespace: string;
  state: IScenarioTaskModelState;
  effects: {
    queryScenarioTasks: Effect;
    queryScenarioTaskDetail: Effect;
    createScenarioTask: Effect;
    updateScenarioTask: Effect;
    deleteScenarioTask: Effect;

    queryAllScenarioCustomTemplates: Effect;
    queryScenarioCustomTemplateDetail: Effect;
    createScenarioCustomTemplate: Effect;
    deleteScenarioCustomTemplate: Effect;

    queryScenarioTaskResults: Effect;
    queryScenarioTaskDynamicDomainTerms: Effect;
  };
}

const Model = modelExtend(pageModel, {
  namespace: 'scenarioTaskModel',
  state: {
    scenarioTaskList: [],
    scenarioTaskDetail: {} as IScenarioTask,
    scenarioTaskResult: [],

    scenarioCustomTemplateList: [],
    scenarioCustomTemplateDetail: {} as IScenarioCustomTemplate,

    selectedResultRecord: {},
  },
  reducers: {},
  effects: {
    *queryScenarioTasks({ payload = {} }, { call, put }) {
      yield doPageRequest({
        api: queryScenarioTasks,
        payload,
        call,
        put,
        stateKey: 'scenarioTaskList',
      });
    },

    *queryScenarioTaskDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryScenarioTaskDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          scenarioTaskDetail: success ? result : {},
        },
      });
      return result;
    },

    *createScenarioTask({ payload }, { call }) {
      const { success } = yield call(createScenarioTask, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateScenarioTask({ payload }, { call }) {
      const { success } = yield call(updateScenarioTask, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteScenarioTask({ payload }, { call }) {
      const { success } = yield call(deleteScenarioTask, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },

    *queryAllScenarioCustomTemplates({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryAllScenarioCustomTemplates, payload);
      yield put({
        type: 'updateState',
        payload: {
          scenarioCustomTemplateList: success ? result : [],
        },
      });
    },

    *queryScenarioCustomTemplateDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryScenarioCustomTemplateDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          scenarioCustomTemplateDetail: success ? result : {},
        },
      });
      return success;
    },

    *createScenarioCustomTemplate({ payload }, { call }) {
      const { success } = yield call(createScenarioCustomTemplate, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *deleteScenarioCustomTemplate({ payload }, { call }) {
      const { success } = yield call(deleteScenarioCustomTemplate, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },

    *queryScenarioTaskResults({ payload = {} }, { call, put }) {
      yield put({
        type: 'updateState',
        payload: {
          scenarioTaskResult: [],
          pagination: {
            current: 1,
            total: 0,
          },
        },
      });
      yield doPageRequest({
        api: queryScenarioTaskResults,
        payload,
        call,
        put,
        stateKey: 'scenarioTaskResult',
      });
    },
    *queryScenarioTaskDynamicDomainTerms({ payload }, { call }) {
      const { success, result } = yield call(queryScenarioTaskDynamicDomainTerms, payload);
      return success ? result : [];
    },
  },
} as IModelType);

export default Model;
